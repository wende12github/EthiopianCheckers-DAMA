package com.dama.ethiopiancheckersgame.core.rules;

import com.dama.ethiopiancheckersgame.core.model.*;

import java.util.*;

/**
 * Base template implementation for Ethiopian Dama rules.
 * Handles mandatory captures, maximum capture rule, turn validation, and promotion.
 */
public abstract class AbstractDamaRules implements DamaRuleStrategy {

    protected static final int[][] ALL_DIRECTIONS = {
            {-1, -1}, {-1, 1}, {1, -1}, {1, 1}
    };

    protected final DamaVariant variant;

    public AbstractDamaRules(DamaVariant variant) {
        this.variant = variant;
    }

    @Override
    public DamaVariant getVariant() {
        return variant;
    }

    @Override
    public List<Move> getLegalMoves(Board board, PieceColor turn) {
        List<Move> allCaptures = getAllCaptures(board, turn);

        if (!allCaptures.isEmpty()) {
            // Maximum capture rule: Find the highest capture count achieved by any sequence
            int maxCaptures = 0;
            for (Move move : allCaptures) {
                if (move.getCaptureCount() > maxCaptures) {
                    maxCaptures = move.getCaptureCount();
                }
            }

            // Filter to only move sequences that capture the maximum number of pieces
            List<Move> maxMoves = new ArrayList<>();
            for (Move move : allCaptures) {
                if (move.getCaptureCount() == maxCaptures) {
                    maxMoves.add(move);
                }
            }
            return maxMoves;
        }

        // If no captures are available, simple moves are permitted
        return getAllSimpleMoves(board, turn);
    }

    @Override
    public List<Position> getMandatoryCapturePieces(Board board, PieceColor turn) {
        List<Move> legalMoves = getLegalMoves(board, turn);
        Set<Position> fromPositions = new LinkedHashSet<>();
        for (Move m : legalMoves) {
            if (m.isCapture()) {
                fromPositions.add(m.getFrom());
            }
        }
        return new ArrayList<>(fromPositions);
    }

    @Override
    public List<Move> getLegalMovesForPiece(Board board, PieceColor turn, Position from) {
        List<Move> allMoves = getLegalMoves(board, turn);
        List<Move> pieceMoves = new ArrayList<>();
        for (Move m : allMoves) {
            if (m.getFrom().equals(from)) {
                pieceMoves.add(m);
            }
        }
        return pieceMoves;
    }

    @Override
    public void applyMove(Board board, Move move, PieceColor turn) {
        Piece piece = board.getPiece(move.getFrom());
        board.removePiece(move.getFrom());

        // Remove all captured pieces
        for (Position cap : move.getCapturedPieces()) {
            board.removePiece(cap);
        }

        // Promotion check
        boolean becomesKing = piece.isKing() || move.isPromotion() || move.getTo().row() == turn.getKingRow();
        if (becomesKing && !piece.isKing()) {
            piece = piece.promote();
        }

        board.setPiece(move.getTo(), piece);
    }

    @Override
    public boolean isGameOver(Board board, PieceColor turn) {
        if (board.countPieces(turn) == 0) {
            return true;
        }
        return getLegalMoves(board, turn).isEmpty();
    }

    // -------------------------------------------------------------
    // Simple Moves
    // -------------------------------------------------------------

    protected List<Move> getAllSimpleMoves(Board board, PieceColor turn) {
        List<Move> moves = new ArrayList<>();
        for (Position pos : board.getPiecePositions(turn)) {
            Piece piece = board.getPiece(pos);
            if (piece.isKing()) {
                moves.addAll(getKingSimpleMoves(board, pos));
            } else {
                moves.addAll(getManSimpleMoves(board, pos, turn));
            }
        }
        return moves;
    }

    /**
     * Regular Piece Normal Move (Identical in both Egregna & Tankegna):
     * Can move 1 square diagonally forward only onto an empty square.
     */
    protected List<Move> getManSimpleMoves(Board board, Position from, PieceColor turn) {
        List<Move> moves = new ArrayList<>();
        int forward = turn.getForwardDirection();
        int[] dCols = {-1, 1};

        for (int dCol : dCols) {
            Position to = from.add(forward, dCol);
            if (to.isValid() && board.isEmpty(to)) {
                boolean promo = (to.row() == turn.getKingRow());
                moves.add(new Move(from, to, List.of(from, to), List.of(), promo));
            }
        }
        return moves;
    }

    protected abstract List<Move> getKingSimpleMoves(Board board, Position from);

    // -------------------------------------------------------------
    // Captures
    // -------------------------------------------------------------

    protected List<Move> getAllCaptures(Board board, PieceColor turn) {
        List<Move> captures = new ArrayList<>();
        for (Position pos : board.getPiecePositions(turn)) {
            Piece piece = board.getPiece(pos);
            List<Move> pieceCaptures = new ArrayList<>();
            Set<Position> pendingRemoval = new HashSet<>();
            List<Position> path = new ArrayList<>();
            path.add(pos);

            if (piece.isKing()) {
                findKingCaptures(board, pos, pos, turn, pendingRemoval, path, pieceCaptures);
            } else {
                findManCaptures(board, pos, pos, turn, pendingRemoval, path, pieceCaptures);
            }
            captures.addAll(pieceCaptures);
        }
        return captures;
    }

    protected abstract void findManCaptures(Board board, Position origin, Position current, PieceColor turn,
                                            Set<Position> pendingRemoval, List<Position> path, List<Move> outMoves);

    protected abstract void findKingCaptures(Board board, Position origin, Position current, PieceColor turn,
                                             Set<Position> pendingRemoval, List<Position> path, List<Move> outMoves);
}
