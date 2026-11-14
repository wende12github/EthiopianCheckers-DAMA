package com.dama.ethiopiancheckersgame.core.model;

import java.util.ArrayList;
import java.util.List;

/**
 * 8x8 Ethiopian Checkers (Dama) Board.
 * Standard setup: 12 pieces per player on dark squares.
 */
public class Board {
    public static final int SIZE = 8;
    private final Piece[][] grid;

    public Board() {
        this.grid = new Piece[SIZE][SIZE];
        setupInitialPosition();
    }

    private Board(Piece[][] grid) {
        this.grid = grid;
    }

    public void setupInitialPosition() {
        clear();
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                if ((row + col) % 2 != 0) { // Dark squares
                    if (row < 3) {
                        grid[row][col] = Piece.blackMan();
                    } else if (row > 4) {
                        grid[row][col] = Piece.redMan();
                    }
                }
            }
        }
    }

    public void clear() {
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                grid[r][c] = null;
            }
        }
    }

    public Piece getPiece(int row, int col) {
        if (row < 0 || row >= SIZE || col < 0 || col >= SIZE) {
            return null;
        }
        return grid[row][col];
    }

    public Piece getPiece(Position pos) {
        return getPiece(pos.row(), pos.col());
    }

    public void setPiece(Position pos, Piece piece) {
        if (pos.isValid()) {
            grid[pos.row()][pos.col()] = piece;
        }
    }

    public void setPiece(int row, int col, Piece piece) {
        if (row >= 0 && row < SIZE && col >= 0 && col < SIZE) {
            grid[row][col] = piece;
        }
    }

    public void removePiece(Position pos) {
        setPiece(pos, null);
    }

    public boolean isEmpty(Position pos) {
        return getPiece(pos) == null;
    }

    public boolean isEmpty(int row, int col) {
        return getPiece(row, col) == null;
    }

    public List<Position> getPiecePositions(PieceColor color) {
        List<Position> list = new ArrayList<>();
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                Piece p = grid[r][c];
                if (p != null && p.color() == color) {
                    list.add(new Position(r, c));
                }
            }
        }
        return list;
    }

    public int countPieces(PieceColor color) {
        int count = 0;
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                Piece p = grid[r][c];
                if (p != null && p.color() == color) {
                    count++;
                }
            }
        }
        return count;
    }

    public int countKings(PieceColor color) {
        int count = 0;
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                Piece p = grid[r][c];
                if (p != null && p.color() == color && p.isKing()) {
                    count++;
                }
            }
        }
        return count;
    }

    public Board copy() {
        Piece[][] newGrid = new Piece[SIZE][SIZE];
        for (int r = 0; r < SIZE; r++) {
            System.arraycopy(grid[r], 0, newGrid[r], 0, SIZE);
        }
        return new Board(newGrid);
    }
}
