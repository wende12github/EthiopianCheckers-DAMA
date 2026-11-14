package com.dama.ethiopiancheckersgame.engine;

import com.dama.ethiopiancheckersgame.core.model.*;
import com.dama.ethiopiancheckersgame.core.rules.DamaRuleStrategy;
import com.dama.ethiopiancheckersgame.core.rules.DamaVariant;
import com.dama.ethiopiancheckersgame.core.rules.EgregnaRules;
import com.dama.ethiopiancheckersgame.core.rules.TankegnaRules;
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
    private final DamaRuleStrategy rules;
    private final GameMode gameMode;
    private final Player player1;
    private final Player player2;
    private final List<GameEventListener> listeners = new CopyOnWriteArrayList<>();
    private final Stack<GameState> undoStack = new Stack<>();
    private final DrawDetector drawDetector = new DrawDetector();

    private GameState currentState;
    private boolean isRunning = false;

    public GameSession(Player player1, Player player2, GameMode gameMode) {
        this(player1, player2, gameMode, new TankegnaRules());
    }

    public GameSession(Player player1, Player player2, GameMode gameMode, DamaVariant variant) {
        this(player1, player2, gameMode, variant == DamaVariant.EGREGNA ? new EgregnaRules() : new TankegnaRules());
    }

    public GameSession(Player player1, Player player2, GameMode gameMode, DamaRuleStrategy rules) {
        this.rules = rules != null ? rules : new TankegnaRules();
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
        this.drawDetector.reset();
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

    public DamaRuleStrategy getRules() {
        return rules;
    }

    public DamaVariant getVariant() {
        return rules.getVariant();
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

        Piece pieceMoved = currentState.getBoard().getPiece(move.getFrom());

        // Apply move
        rules.applyMove(currentState.getBoard(), move, currentState.getCurrentTurn());
        currentState.recordMove(move);

        for (GameEventListener l : listeners) {
            l.onMoveExecuted(move, current);
        }

        // Check if move results in a Draw (insufficient material, 3-fold repetition, 50-ply rule)
        PieceColor nextTurn = currentState.getCurrentTurn().opponent();
        String drawReason = drawDetector.checkDraw(currentState.getBoard(), nextTurn, move, pieceMoved);
        if (drawReason != null) {
            currentState.setDraw(drawReason);
            isRunning = false;
            for (GameEventListener l : listeners) {
                l.onGameOver(null, currentState.getGameOverReason());
            }
            return true;
        }

        // Check if game over for opponent (loss)
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
     * Declares a draw manually or by mutual agreement.
     */
    public synchronized void declareDraw(String reason) {
        if (!isRunning) return;
        currentState.setDraw(reason);
        isRunning = false;
        for (GameEventListener l : listeners) {
            l.onGameOver(null, currentState.getGameOverReason());
        }
    }

    /**
     * Offers a draw.
     * In AI mode: AI decides based on board position evaluation.
     * Returns true if the draw was accepted and enacted.
     */
    public synchronized boolean offerDraw(Player offeringPlayer) {
        if (!isRunning) return false;

        if (gameMode == GameMode.HUMAN_VS_AI) {
            Player ai = (player1.isAI()) ? player1 : player2;
            boolean accepted = drawDetector.shouldAIAcceptDraw(currentState.getBoard(), ai.getColor());
            if (accepted) {
                declareDraw("Agreed to draw offer with " + ai.getName() + ".");
                return true;
            }
            return false;
        }

        // For human vs human, caller prompts the other player
        return false;
    }

    public DrawDetector getDrawDetector() {
        return drawDetector;
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

        // If current player is AI or remote Network player, automatically request move
        if (current.isAI() || current instanceof com.dama.ethiopiancheckersgame.player.NetworkPlayer) {
            current.requestMove(currentState.copy()).thenAccept(move -> {
                if (move != null) {
                    makeMove(move);
                }
            });
        }
    }
}
