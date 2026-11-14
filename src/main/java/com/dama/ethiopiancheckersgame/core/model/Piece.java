package com.dama.ethiopiancheckersgame.core.model;

/**
 * Immutable piece state.
 */
public record Piece(PieceColor color, PieceType type) {
    public static Piece redMan() {
        return new Piece(PieceColor.RED, PieceType.MAN);
    }

    public static Piece redKing() {
        return new Piece(PieceColor.RED, PieceType.KING);
    }

    public static Piece blackMan() {
        return new Piece(PieceColor.BLACK, PieceType.MAN);
    }

    public static Piece blackKing() {
        return new Piece(PieceColor.BLACK, PieceType.KING);
    }

    public boolean isKing() {
        return type == PieceType.KING;
    }

    public Piece promote() {
        return new Piece(color, PieceType.KING);
    }
}
