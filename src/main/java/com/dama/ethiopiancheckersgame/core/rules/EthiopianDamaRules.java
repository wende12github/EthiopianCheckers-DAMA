package com.dama.ethiopiancheckersgame.core.rules;

import com.dama.ethiopiancheckersgame.core.model.*;

import java.util.*;

/**
 * Authentic Ethiopian Checkers (Dama / ዳማ) Rules Engine.
 * Features:
 * - Flying King (Dama / ንጉሥ): Slides any distance along diagonals, jumps over distance.
 * - Mandatory Capture: If captures are available, simple moves are forbidden.
 * - Maximum Capture Rule: Must execute the sequence that captures the maximum number of pieces.
 * - Multi-jump chaining: Explores full capture trees recursively.
 */
public class EthiopianDamaRules {

    private static final int[][] ALL_DIRECTIONS = {
            {-1, -1}, {-1, 1}, {1, -1}, {1, 1}
    };

    /**
     * Returns all legal moves for the current player.
     * Enforces mandatory captures and the maximum capture rule.
     */
    public List<Move> getLegalMoves(Board board, PieceColor turn) {
        List<Move> allCaptures = getAllCaptures(board, turn);

        if (!allCaptures.isEmpty()) {
            // Find maximum number of captures possible in any single move sequence
            int maxCaptures = 0;
            for (Move move : allCaptures) {
                if (move.getCaptureCount() > maxCaptures) {
                    maxCaptures = move.getCaptureCount();
                }
            }

            // Filter to only moves achieving the maximum capture count
            List<Move> maxMoves = new ArrayList<>();
            for (Move move : allCaptures) {
                if (move.getCaptureCount() == maxCaptures) {
                    maxMoves.add(move);
                }
            }
            return maxMoves;
        }

        // No captures available -> simple moves
        return getAllSimpleMoves(board, turn);
    }

    /**
     * Returns positions of pieces that are required to capture on this turn.
     * Used by the UI to highlight pieces with mandatory capture glow.
     */
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

    /**
     * Returns legal destination moves for a piece at the given position.
     */
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

    /**
     * Executes the given move on the board, updating positions, removing captured pieces,
     * and promoting pieces when reaching the king row.
     */
    public void applyMove(Board board, Move move, PieceColor turn) {
        Piece piece = board.getPiece(move.getFrom());
        board.removePiece(move.getFrom());

        // Remove all captured pieces
        for (Position cap : move.getCapturedPieces()) {
            board.removePiece(cap);
        }

        // Promotion check
        boolean becomesKing = piece.isKing() || move.getTo().row() == turn.getKingRow();
        if (becomesKing && !piece.isKing()) {
            piece = piece.promote();
        }

        board.setPiece(move.getTo(), piece);
    }

    /**
     * Checks if the game is over.
     */
    public boolean isGameOver(Board board, PieceColor turn) {
        if (board.countPieces(turn) == 0) {
            return true;
        }
        return getLegalMoves(board, turn).isEmpty();
    }

    // -------------------------------------------------------------
    // Simple (non-capturing) moves
    // -------------------------------------------------------------

    private List<Move> getAllSimpleMoves(Board board, PieceColor turn) {
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

    private List<Move> getManSimpleMoves(Board board, Position from, PieceColor turn) {
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

    private List<Move> getKingSimpleMoves(Board board, Position from) {
        List<Move> moves = new ArrayList<>();
        for (int[] dir : ALL_DIRECTIONS) {
            int dRow = dir[0];
            int dCol = dir[1];
            Position current = from.add(dRow, dCol);

            // Flying King slides any distance along empty diagonal
            while (current.isValid() && board.isEmpty(current)) {
                moves.add(new Move(from, current));
                current = current.add(dRow, dCol);
            }
        }
        return moves;
    }

    // -------------------------------------------------------------
    // Capture moves (Recursive multi-jumps)
    // -------------------------------------------------------------

    private List<Move> getAllCaptures(Board board, PieceColor turn) {
        List<Move> captures = new ArrayList<>();
        for (Position pos : board.getPiecePositions(turn)) {
            Piece piece = board.getPiece(pos);
            List<Move> pieceCaptures = new ArrayList<>();
            Set<Position> captured = new HashSet<>();
            List<Position> path = new ArrayList<>();
            path.add(pos);

            if (piece.isKing()) {
                findKingCaptures(board, pos, pos, turn, captured, path, pieceCaptures);
            } else {
                findManCaptures(board, pos, pos, turn, captured, path, pieceCaptures);
            }
            captures.addAll(pieceCaptures);
        }
        return captures;
    }

    private void findManCaptures(Board board, Position origin, Position current, PieceColor turn,
                                 Set<Position> captured, List<Position> path, List<Move> outMoves) {
        boolean foundFurtherJump = false;
        // In Ethiopian Dama, ordinary pieces jump forward diagonally
        int forward = turn.getForwardDirection();
        int[][] manJumpDirs = {
                {forward, -1}, {forward, 1}
        };

        for (int[] dir : manJumpDirs) {
            int dRow = dir[0];
            int dCol = dir[1];
            Position mid = current.add(dRow, dCol);
            Position land = current.add(2 * dRow, 2 * dCol);

            if (mid.isValid() && land.isValid() && !captured.contains(mid)) {
                Piece midPiece = board.getPiece(mid);
                if (midPiece != null && midPiece.color() != turn && (board.isEmpty(land) || land.equals(origin))) {
                    foundFurtherJump = true;
                    captured.add(mid);
                    path.add(land);

                    // Check if reaches king row
                    boolean isPromo = (land.row() == turn.getKingRow());
                    if (isPromo) {
                        // Promoted to king during multi-jump, continue as King or stop
                        outMoves.add(new Move(origin, land, new ArrayList<>(path), new ArrayList<>(captured), true));
                    } else {
                        findManCaptures(board, origin, land, turn, captured, path, outMoves);
                    }

                    path.remove(path.size() - 1);
                    captured.remove(mid);
                }
            }
        }

        if (!foundFurtherJump && path.size() > 1) {
            boolean promo = (current.row() == turn.getKingRow());
            outMoves.add(new Move(origin, current, new ArrayList<>(path), new ArrayList<>(captured), promo));
        }
    }

    private void findKingCaptures(Board board, Position origin, Position current, PieceColor turn,
                                  Set<Position> captured, List<Position> path, List<Move> outMoves) {
        boolean foundFurtherJump = false;

        for (int[] dir : ALL_DIRECTIONS) {
            int dRow = dir[0];
            int dCol = dir[1];
            Position scan = current.add(dRow, dCol);

            // Slide until an obstacle is found
            while (scan.isValid() && (board.isEmpty(scan) || scan.equals(origin))) {
                scan = scan.add(dRow, dCol);
            }

            // If an enemy piece is found and hasn't been captured in this turn
            if (scan.isValid() && !captured.contains(scan)) {
                Piece obstacle = board.getPiece(scan);
                if (obstacle != null && obstacle.color() != turn) {
                    Position mid = scan;
                    Position land = mid.add(dRow, dCol);

                    // Flying king can land on ANY free square behind the captured piece
                    while (land.isValid() && (board.isEmpty(land) || land.equals(origin))) {
                        foundFurtherJump = true;
                        captured.add(mid);
                        path.add(land);

                        findKingCaptures(board, origin, land, turn, captured, path, outMoves);

                        path.remove(path.size() - 1);
                        captured.remove(mid);

                        land = land.add(dRow, dCol);
                    }
                }
            }
        }

        if (!foundFurtherJump && path.size() > 1) {
            outMoves.add(new Move(origin, current, new ArrayList<>(path), new ArrayList<>(captured), false));
        }
    }
}
