package com.dama.ethiopiancheckersgame.core.rules;

import com.dama.ethiopiancheckersgame.core.model.*;

import java.util.*;

/**
 * Mode B: Tankegna (ታንከኛ - Armored Tank Mode)
 * Rules:
 * - Regular Piece Normal Move: 1 square diagonally forward only.
 * - Regular Piece Capture: In all 4 diagonal directions (both forward and backward).
 * - King Piece Normal Move ("Flying King"): Slides any number of empty squares along all 4 diagonal rays.
 * - King Piece Capture ("Flying King"): Jumps over an enemy piece at any distance, landing on any open square behind it.
 * - Multi-jump: Direction changes allowed, pending removal prevents jumping the same piece twice.
 * - Edge case flag: promoteMidJump (defaults to false - continues jumping as regular, promotes at turn end).
 */
public class TankegnaRules extends AbstractDamaRules {

    private final boolean promoteMidJump;

    public TankegnaRules() {
        this(false);
    }

    public TankegnaRules(boolean promoteMidJump) {
        super(DamaVariant.TANKEGNA);
        this.promoteMidJump = promoteMidJump;
    }

    public boolean isPromoteMidJump() {
        return promoteMidJump;
    }

    /**
     * Flying King Normal Move:
     * Slides any number of empty squares along all 4 diagonal directions.
     */
    @Override
    protected List<Move> getKingSimpleMoves(Board board, Position from) {
        List<Move> moves = new ArrayList<>();
        for (int[] dir : ALL_DIRECTIONS) {
            int dRow = dir[0];
            int dCol = dir[1];
            Position current = from.add(dRow, dCol);

            while (current.isValid() && board.isEmpty(current)) {
                moves.add(new Move(from, current));
                current = current.add(dRow, dCol);
            }
        }
        return moves;
    }

    /**
     * Regular Piece Capture (All 4 Diagonal Directions):
     * Can capture both forward and backward by jumping 1 adjacent opponent piece.
     */
    @Override
    protected void findManCaptures(Board board, Position origin, Position current, PieceColor turn,
                                   Set<Position> pendingRemoval, List<Position> path, List<Move> outMoves) {
        boolean foundFurtherJump = false;

        // In Tankegna, ordinary pieces can jump in all 4 diagonal directions
        for (int[] dir : ALL_DIRECTIONS) {
            int dRow = dir[0];
            int dCol = dir[1];
            Position mid = current.add(dRow, dCol);
            Position land = current.add(2 * dRow, 2 * dCol);

            if (mid.isValid() && land.isValid() && !pendingRemoval.contains(mid)) {
                Piece midPiece = board.getPiece(mid);
                // Cannot jump over friendly pieces or empty spaces
                if (midPiece != null && midPiece.color() != turn && (board.isEmpty(land) || land.equals(origin))) {
                    foundFurtherJump = true;
                    pendingRemoval.add(mid);
                    path.add(land);

                    boolean touchesKingRow = (land.row() == turn.getKingRow());
                    if (touchesKingRow && promoteMidJump) {
                        // Edge case A: Stop immediately and promote
                        outMoves.add(new Move(origin, land, new ArrayList<>(path), new ArrayList<>(pendingRemoval), true));
                    } else {
                        // Standard: Continue jumping as regular piece; promotion is finalized at turn end if touches king row
                        findManCaptures(board, origin, land, turn, pendingRemoval, path, outMoves);
                    }

                    path.remove(path.size() - 1);
                    pendingRemoval.remove(mid);
                }
            }
        }

        if (!foundFurtherJump && path.size() > 1) {
            // Check if final landing square reaches king row (or touched during jump)
            boolean reachedKingRow = false;
            for (Position p : path) {
                if (p.row() == turn.getKingRow()) {
                    reachedKingRow = true;
                    break;
                }
            }
            outMoves.add(new Move(origin, current, new ArrayList<>(path), new ArrayList<>(pendingRemoval), reachedKingRow));
        }
    }

    /**
     * Flying King Capture:
     * Jumps over an opponent piece at any distance along a clear diagonal line.
     * Lands on ANY vacant square along the diagonal line beyond the captured piece.
     */
    @Override
    protected void findKingCaptures(Board board, Position origin, Position current, PieceColor turn,
                                    Set<Position> pendingRemoval, List<Position> path, List<Move> outMoves) {
        boolean foundFurtherJump = false;

        for (int[] dir : ALL_DIRECTIONS) {
            int dRow = dir[0];
            int dCol = dir[1];
            Position scan = current.add(dRow, dCol);

            // Slide until an obstacle is encountered
            while (scan.isValid() && (board.isEmpty(scan) || scan.equals(origin) || pendingRemoval.contains(scan))) {
                scan = scan.add(dRow, dCol);
            }

            // If an enemy piece is found and has NOT been captured yet in this multi-jump sequence
            if (scan.isValid() && !pendingRemoval.contains(scan)) {
                Piece obstacle = board.getPiece(scan);
                if (obstacle != null && obstacle.color() != turn) {
                    Position mid = scan;
                    Position land = mid.add(dRow, dCol);

                    // Landing squares: Any contiguous free squares behind the captured piece
                    while (land.isValid() && (board.isEmpty(land) || land.equals(origin))) {
                        foundFurtherJump = true;
                        pendingRemoval.add(mid);
                        path.add(land);

                        // Recurse to find subsequent captures
                        findKingCaptures(board, origin, land, turn, pendingRemoval, path, outMoves);

                        path.remove(path.size() - 1);
                        pendingRemoval.remove(mid);

                        land = land.add(dRow, dCol);
                    }
                }
            }
        }

        if (!foundFurtherJump && path.size() > 1) {
            outMoves.add(new Move(origin, current, new ArrayList<>(path), new ArrayList<>(pendingRemoval), false));
        }
    }
}
