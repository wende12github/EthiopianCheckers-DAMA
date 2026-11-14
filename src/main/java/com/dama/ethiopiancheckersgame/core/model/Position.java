package com.dama.ethiopiancheckersgame.core.model;

import java.util.Objects;

/**
 * Immutable board coordinate (row, col) between 0 and 7.
 */
public record Position(int row, int col) {
    public static final int BOARD_SIZE = 8;

    public boolean isValid() {
        return row >= 0 && row < BOARD_SIZE && col >= 0 && col < BOARD_SIZE;
    }

    public boolean isDarkSquare() {
        return (row + col) % 2 != 0;
    }

    public Position add(int dRow, int dCol) {
        return new Position(row + dRow, col + dCol);
    }

    @Override
    public String toString() {
        return "(" + (row + 1) + "," + (col + 1) + ")";
    }
}
