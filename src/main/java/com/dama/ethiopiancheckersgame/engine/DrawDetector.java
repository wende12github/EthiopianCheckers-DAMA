package com.dama.ethiopiancheckersgame.engine;

import com.dama.ethiopiancheckersgame.core.model.*;
import com.dama.ethiopiancheckersgame.player.ai.BoardEvaluator;

import java.util.HashMap;
import java.util.Map;

/**
 * Intelligent draw detection engine for Ethiopian Checkers (Dama).
 * Analyzes:
 * 1. Insufficient material (e.g., 1 King vs 1 King).
 * 2. Threefold repetition of board positions (cycle detection).
 * 3. Fifty-ply / 25-turn rule (no captures and no regular piece advances).
 * 4. AI draw evaluation (evaluating draw offers based on heuristic equality).
 */
public class DrawDetector {
    public static final int MAX_PLIES_WITHOUT_PROGRESS = 50; // 25 full turns (50 plies)
    public static final int REPETITION_THRESHOLD = 3;

    private final Map<String, Integer> positionHistory = new HashMap<>();
    private int pliesWithoutProgress = 0;
    private final BoardEvaluator evaluator = new BoardEvaluator();

    public void reset() {
        positionHistory.clear();
        pliesWithoutProgress = 0;
    }

    /**
     * Records a move and checks if it causes a draw.
     * Returns a non-null reason string if a draw condition is met, or null if game continues.
     */
    public String checkDraw(Board board, PieceColor turn, Move lastMove, Piece pieceMoved) {
        // 1. Check Insufficient Material (1 King vs 1 King)
        if (isInsufficientMaterial(board)) {
            return "Insufficient material (1 King vs 1 King - neither side can force a win).";
        }

        // 2. Track progress (reset on capture or regular Man advance)
        if (lastMove != null) {
            if (lastMove.isCapture() || (pieceMoved != null && !pieceMoved.isKing())) {
                pliesWithoutProgress = 0;
                // Irreversible move clears repetition history (pieces cannot return to previous states)
                positionHistory.clear();
            } else {
                pliesWithoutProgress++;
            }
        }

        // 3. Fifty-ply rule without capture or man advance
        if (pliesWithoutProgress >= MAX_PLIES_WITHOUT_PROGRESS) {
            return "25 consecutive turns completed without any captures or piece advance (No-Progress Rule).";
        }

        // 4. Threefold Repetition of Position
        String key = computePositionFingerprint(board, turn);
        int count = positionHistory.getOrDefault(key, 0) + 1;
        positionHistory.put(key, count);

        if (count >= REPETITION_THRESHOLD) {
            return "Threefold repetition of the exact same board position.";
        }

        return null;
    }

    /**
     * Checks if only 1 King remains for each player.
     */
    public boolean isInsufficientMaterial(Board board) {
        int redPieces = board.countPieces(PieceColor.RED);
        int blackPieces = board.countPieces(PieceColor.BLACK);
        int redKings = board.countKings(PieceColor.RED);
        int blackKings = board.countKings(PieceColor.BLACK);

        return redPieces == 1 && blackPieces == 1 && redKings == 1 && blackKings == 1;
    }

    /**
     * Decides whether the AI should accept a draw offer from the player.
     * Accepts if the board is evaluated as balanced, or if AI is at a disadvantage.
     */
    public boolean shouldAIAcceptDraw(Board board, PieceColor aiColor) {
        // If material is 1 King vs 1 King, definitely accept
        if (isInsufficientMaterial(board)) {
            return true;
        }

        // Evaluate board state
        int eval = evaluator.evaluate(board, aiColor);

        // AI accepts if score is <= 30 (even or losing), declines if clearly winning (> 120)
        return eval <= 35;
    }

    /**
     * Computes a canonical hash fingerprint of the board layout and active turn.
     */
    public static String computePositionFingerprint(Board board, PieceColor turn) {
        StringBuilder sb = new StringBuilder();
        sb.append(turn).append(":");
        for (int r = 0; r < Board.SIZE; r++) {
            for (int c = 0; c < Board.SIZE; c++) {
                Piece p = board.getPiece(r, c);
                if (p != null) {
                    sb.append(r).append(c)
                      .append(p.color() == PieceColor.RED ? 'R' : 'B')
                      .append(p.isKing() ? 'K' : 'M')
                      .append(";");
                }
            }
        }
        return sb.toString();
    }

    public int getPliesWithoutProgress() {
        return pliesWithoutProgress;
    }
}
