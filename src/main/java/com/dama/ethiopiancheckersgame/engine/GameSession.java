package com.dama.ethiopiancheckersgame.engine;

import com.dama.ethiopiancheckersgame.core.model.*;
import com.dama.ethiopiancheckersgame.core.rules.EthiopianDamaRules;
import com.dama.ethiopiancheckersgame.player.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Orchestrates a complete game session, coordinating turns between players,
 * enforcing Ethiopian Dama rules, maintaining undo history, and notifying event listeners.
 */
public class GameSession {
    private final EthiopianDamaRules rules;
    private final GameMode gameMode;
    private final Player player1;
    private final Player player2;
    private final List<GameEventListener> listeners = new CopyOnWriteArrayList<>();
    private final Stack<GameState> undoStack = new Stack<>();

    private GameState currentState;
    private boolean isRunning = false;

    public GameSession(Player player1, Player player2, GameMode gameMode) {
        this.rules = new EthiopianDamaRules();
        this.player1 = player1;
        this.player2 = player2;
        this.gameMode = gameMode;
        this.currentState = new GameState();
    }

    public void addListener(GameEventListener listener) {
        listeners.add(listener);
    }

    public void removeListener(GameEventListener listener) {
        listeners.remove(listener);
    }

    public void start() {
        this.currentState = new GameState();
        this.undoStack.clear();
        this.isRunning = true;

        for (GameEventListener l : listeners) {
            l.onGameStarted(player1, player2);
        }

        notifyTurn();
    }

    public Player getCurrentPlayer() {
        return currentState.getCurrentTurn() == PieceColor.RED ? player1 : player2;
    }

    public Player getPlayer1() {
        return player1;
    }

    public Player getPlayer2() {
        return player2;
    }

    public GameState getCurrentState() {
        return currentState;
    }

    public EthiopianDamaRules getRules() {
        return rules;
    }

    public GameMode getGameMode() {
        return gameMode;
    }

    public boolean isRunning() {
        return isRunning && !currentState.isGameOver();
    }

    /**
     * Executes a move chosen by a player.
     * Returns true if move was valid and executed.
     */
    public synchronized boolean makeMove(Move move) {
        if (!isRunning || currentState.isGameOver()) {
            return false;
        }

        Player current = getCurrentPlayer();
        List<Move> legalMoves = rules.getLegalMoves(currentState.getBoard(), currentState.getCurrentTurn());

        // Validate that this exact move is in legal moves
        boolean isLegal = legalMoves.stream().anyMatch(m ->
                m.getFrom().equals(move.getFrom()) && m.getTo().equals(move.getTo())
        );

        if (!isLegal) {
            return false;
        }

        // Save for undo
        undoStack.push(currentState.copy());

        // Apply move
        rules.applyMove(currentState.getBoard(), move, currentState.getCurrentTurn());
        currentState.recordMove(move);

        for (GameEventListener l : listeners) {
            l.onMoveExecuted(move, current);
        }

        // Check if game over for opponent
        PieceColor nextTurn = currentState.getCurrentTurn().opponent();
        if (rules.isGameOver(currentState.getBoard(), nextTurn)) {
            currentState.setGameOver(currentState.getCurrentTurn(),
                    current.getName() + " wins! Opponent has no remaining legal moves.");
            isRunning = false;
            for (GameEventListener l : listeners) {
                l.onGameOver(current, currentState.getGameOverReason());
            }
            return true;
        }

        // Advance turn
        currentState.switchTurn();
        notifyTurn();
        return true;
    }

    /**
     * Reverts the last turn (or 2 turns if against AI).
     */
    public synchronized boolean undo() {
        if (undoStack.isEmpty() || !isRunning) {
            return false;
        }

        // If playing against AI, undo both AI move and human move
        if (gameMode == GameMode.HUMAN_VS_AI && undoStack.size() >= 2) {
            undoStack.pop(); // pop AI move state
            this.currentState = undoStack.pop();
        } else {
            this.currentState = undoStack.pop();
        }

        for (GameEventListener l : listeners) {
            l.onBoardReset();
        }
        notifyTurn();
        return true;
    }

    private void notifyTurn() {
        Player current = getCurrentPlayer();
        for (GameEventListener l : listeners) {
            l.onTurnStarted(current);
        }

        // If current player is AI, automatically request its move
        if (current.isAI()) {
            current.requestMove(currentState.copy()).thenAccept(move -> {
                if (move != null) {
                    makeMove(move);
                }
            });
        }
    }
}
