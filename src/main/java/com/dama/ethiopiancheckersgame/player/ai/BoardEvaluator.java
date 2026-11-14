package com.dama.ethiopiancheckersgame.player.ai;

import com.dama.ethiopiancheckersgame.core.model.Board;
import com.dama.ethiopiancheckersgame.core.model.Piece;
import com.dama.ethiopiancheckersgame.core.model.PieceColor;

/**
 * Heuristic evaluation function for Ethiopian Checkers (Dama).
 */
public class BoardEvaluator {
    private static final int MAN_VALUE = 100;
    private static final int KING_VALUE = 320; // Flying King is very valuable
    private static final int ADVANCEMENT_WEIGHT = 7;
    private static final int CENTER_WEIGHT = 5;

    public int evaluate(Board board, PieceColor perspective) {
        int score = 0;

        for (int r = 0; r < Board.SIZE; r++) {
            for (int c = 0; c < Board.SIZE; c++) {
                Piece piece = board.getPiece(r, c);
                if (piece == null) continue;

                int pieceScore = 0;
                if (piece.isKing()) {
                    pieceScore += KING_VALUE;
                } else {
                    pieceScore += MAN_VALUE;
                    // Advancement bonus (closer to king row)
                    int advancement = (piece.color() == PieceColor.RED) ? (7 - r) : r;
                    pieceScore += advancement * ADVANCEMENT_WEIGHT;
                }

                // Center board control bonus
                if (r >= 2 && r <= 5 && c >= 2 && c <= 5) {
                    pieceScore += CENTER_WEIGHT;
                }

                // Edge protection bonus
                if (c == 0 || c == 7) {
                    pieceScore += 3;
                }

                if (piece.color() == perspective) {
                    score += pieceScore;
                } else {
                    score -= pieceScore;
                }
            }
        }

        return score;
    }
}
