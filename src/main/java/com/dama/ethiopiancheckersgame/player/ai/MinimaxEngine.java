package com.dama.ethiopiancheckersgame.player.ai;

import com.dama.ethiopiancheckersgame.core.model.Board;
import com.dama.ethiopiancheckersgame.core.model.Move;
import com.dama.ethiopiancheckersgame.core.model.PieceColor;
import com.dama.ethiopiancheckersgame.core.rules.DamaRuleStrategy;

import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Minimax decision engine with Alpha-Beta pruning for Ethiopian Checkers.
 */
public class MinimaxEngine {
    private final DamaRuleStrategy rules;
    private final BoardEvaluator evaluator;
    private final Random random;

    public MinimaxEngine(DamaRuleStrategy rules) {
        this.rules = rules;
        this.evaluator = new BoardEvaluator();
        this.random = new Random();
    }

    /**
     * Finds the best move for the given color at the specified depth.
     */
    public Move findBestMove(Board board, PieceColor color, AIDifficulty difficulty) {
        List<Move> legalMoves = rules.getLegalMoves(board, color);
        if (legalMoves.isEmpty()) {
            return null;
        }

        // Fast path for 1 legal move
        if (legalMoves.size() == 1) {
            return legalMoves.get(0);
        }

        // Easy mode: pick random move from legal moves (with slight preference for captures)
        if (difficulty == AIDifficulty.EASY) {
            return legalMoves.get(random.nextInt(legalMoves.size()));
        }

        // Medium & Hard: Minimax with Alpha-Beta
        int depth = difficulty.getSearchDepth();
        Move bestMove = null;
        int bestScore = Integer.MIN_VALUE + 1;
        int alpha = Integer.MIN_VALUE + 1;
        int beta = Integer.MAX_VALUE - 1;

        // Shuffle equal moves to avoid predictable play
        Collections.shuffle(legalMoves, random);

        for (Move move : legalMoves) {
            Board simulated = board.copy();
            rules.applyMove(simulated, move, color);

            int score = minimax(simulated, depth - 1, alpha, beta, false, color, color.opponent());
            if (score > bestScore) {
                bestScore = score;
                bestMove = move;
            }
            alpha = Math.max(alpha, bestScore);
            if (beta <= alpha) {
                break; // Alpha-beta cutoff
            }
        }

        return bestMove != null ? bestMove : legalMoves.get(0);
    }

    private int minimax(Board board, int depth, int alpha, int beta, boolean isMaximizing,
                        PieceColor aiColor, PieceColor currentTurn) {
        if (rules.isGameOver(board, currentTurn)) {
            // Loss for currentTurn
            return isMaximizing ? (-100000 - depth) : (100000 + depth);
        }

        if (depth <= 0) {
            return evaluator.evaluate(board, aiColor);
        }

        List<Move> legalMoves = rules.getLegalMoves(board, currentTurn);
        if (legalMoves.isEmpty()) {
            return isMaximizing ? (-100000 - depth) : (100000 + depth);
        }

        if (isMaximizing) {
            int maxEval = Integer.MIN_VALUE + 1;
            for (Move move : legalMoves) {
                Board simulated = board.copy();
                rules.applyMove(simulated, move, currentTurn);
                int evaluation = minimax(simulated, depth - 1, alpha, beta, false, aiColor, currentTurn.opponent());
                maxEval = Math.max(maxEval, evaluation);
                alpha = Math.max(alpha, evaluation);
                if (beta <= alpha) {
                    break;
                }
            }
            return maxEval;
        } else {
            int minEval = Integer.MAX_VALUE - 1;
            for (Move move : legalMoves) {
                Board simulated = board.copy();
                rules.applyMove(simulated, move, currentTurn);
                int evaluation = minimax(simulated, depth - 1, alpha, beta, true, aiColor, currentTurn.opponent());
                minEval = Math.min(minEval, evaluation);
                beta = Math.min(beta, evaluation);
                if (beta <= alpha) {
                    break;
                }
            }
            return minEval;
        }
    }
}
