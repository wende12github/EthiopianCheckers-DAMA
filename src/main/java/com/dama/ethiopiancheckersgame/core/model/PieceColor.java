package com.dama.ethiopiancheckersgame.core.model;

/**
 * The two sides playing the game.
 * RED (Player 1) starts at the bottom (rows 5-7) and moves UP (row delta = -1).
 * BLACK (Player 2 / AI) starts at the top (rows 0-2) and moves DOWN (row delta = +1).
 */
public enum PieceColor {
    RED(-1, 0, "Red (Player 1)"),
    BLACK(1, 7, "Black (Player 2)");

    private final int forwardDirection;
    private final int kingRow;
    private final String displayName;

    PieceColor(int forwardDirection, int kingRow, String displayName) {
        this.forwardDirection = forwardDirection;
        this.kingRow = kingRow;
        this.displayName = displayName;
    }

    public int getForwardDirection() {
        return forwardDirection;
    }

    public int getKingRow() {
        return kingRow;
    }

    public String getDisplayName() {
        return displayName;
    }

    public PieceColor opponent() {
        return this == RED ? BLACK : RED;
    }
}
