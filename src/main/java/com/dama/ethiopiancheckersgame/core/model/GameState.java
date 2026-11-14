package com.dama.ethiopiancheckersgame.core.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Snapshot of current game state.
 */
public class GameState {
    private final Board board;
    private PieceColor currentTurn;
    private boolean isGameOver;
    private boolean isDraw;
    private PieceColor winner;
    private String gameOverReason;
    private final List<Move> moveHistory;

    public GameState() {
        this.board = new Board();
        this.currentTurn = PieceColor.RED; // Red moves first
        this.isGameOver = false;
        this.isDraw = false;
        this.winner = null;
        this.gameOverReason = "";
        this.moveHistory = new ArrayList<>();
    }

    public GameState(Board board, PieceColor currentTurn) {
        this.board = board;
        this.currentTurn = currentTurn;
        this.isGameOver = false;
        this.isDraw = false;
        this.winner = null;
        this.gameOverReason = "";
        this.moveHistory = new ArrayList<>();
    }

    public Board getBoard() {
        return board;
    }

    public PieceColor getCurrentTurn() {
        return currentTurn;
    }

    public void setCurrentTurn(PieceColor currentTurn) {
        this.currentTurn = currentTurn;
    }

    public void switchTurn() {
        this.currentTurn = currentTurn.opponent();
    }

    public boolean isGameOver() {
        return isGameOver;
    }

    public void setGameOver(PieceColor winner, String reason) {
        this.isGameOver = true;
        this.winner = winner;
        this.isDraw = false;
        this.gameOverReason = reason;
    }

    public void setDraw(String reason) {
        this.isGameOver = true;
        this.winner = null;
        this.isDraw = true;
        this.gameOverReason = "Match Drawn: " + reason;
    }

    public boolean isDraw() {
        return isDraw;
    }

    public PieceColor getWinner() {
        return winner;
    }

    public String getGameOverReason() {
        return gameOverReason;
    }

    public List<Move> getMoveHistory() {
        return Collections.unmodifiableList(moveHistory);
    }

    public void recordMove(Move move) {
        this.moveHistory.add(move);
    }

    public GameState copy() {
        GameState copy = new GameState(board.copy(), currentTurn);
        copy.isGameOver = this.isGameOver;
        copy.isDraw = this.isDraw;
        copy.winner = this.winner;
        copy.gameOverReason = this.gameOverReason;
        copy.moveHistory.addAll(this.moveHistory);
        return copy;
    }
}
