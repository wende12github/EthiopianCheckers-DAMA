package com.dama.ethiopiancheckersgame.core.rules;

import com.dama.ethiopiancheckersgame.core.model.*;

import java.util.*;

/**
 * Mode A: Egregna (እግረኛ - Foot Soldier Mode)
 * Rules:
 * - Regular Piece Normal Move: 1 square diagonally forward only.
 * - Regular Piece Capture: Forward only by jumping 1 adjacent opponent piece. Cannot jump backward.
 * - King Piece Normal Move ("Short King"): 1 square diagonally in all 4 directions. Cannot slide.
 * - King Piece Capture ("Short King"): 1 adjacent piece in all 4 directions, landing on immediate square behind. Cannot slide.
 */
public class EgregnaRules extends AbstractDamaRules {

    public EgregnaRules() {
        super(DamaVariant.EGREGNA);
    }

    /**
     * Short King Normal Move:
     * Moves exactly 1 square diagonally in all 4 directions.
     */
    @Override
    protected List<Move> getKingSimpleMoves(Board board, Position from) {
        List<Move> moves = new ArrayList<>();
        for (int[] dir : ALL_DIRECTIONS) {
            Position to = from.add(dir[0], dir[1]);
            if (to.isValid() && board.isEmpty(to)) {
                moves.add(new Move(from, to));
            }
        }
        return moves;
    }

    /**
     * Regular Piece Capture (Forward Only):
     * Can only jump diagonally forward over an adjacent enemy piece onto the immediate square behind it.
     */
    @Override
    protected void findManCaptures(Board board, Position origin, Position current, PieceColor turn,
                                   Set<Position> pendingRemoval, List<Position> path, List<Move> outMoves) {
        boolean foundFurtherJump = false;
        int forward = turn.getForwardDirection();
        int[][] forwardDirs = {
                {forward, -1}, {forward, 1}
        };

        for (int[] dir : forwardDirs) {
            int dRow = dir[0];
            int dCol = dir[1];
            Position mid = current.add(dRow, dCol);
            Position land = current.add(2 * dRow, 2 * dCol);

            if (mid.isValid() && land.isValid() && !pendingRemoval.contains(mid)) {
                Piece midPiece = board.getPiece(mid);
                if (midPiece != null && midPiece.color() != turn && (board.isEmpty(land) || land.equals(origin))) {
                    foundFurtherJump = true;
                    pendingRemoval.add(mid);
                    path.add(land);

                    boolean isPromo = (land.row() == turn.getKingRow());
                    if (isPromo) {
                        outMoves.add(new Move(origin, land, new ArrayList<>(path), new ArrayList<>(pendingRemoval), true));
                    } else {
                        findManCaptures(board, origin, land, turn, pendingRemoval, path, outMoves);
                    }

                    path.remove(path.size() - 1);
                    pendingRemoval.remove(mid);
                }
            }
        }

        if (!foundFurtherJump && path.size() > 1) {
            boolean promo = (current.row() == turn.getKingRow());
            outMoves.add(new Move(origin, current, new ArrayList<>(path), new ArrayList<>(pendingRemoval), promo));
        }
    }

    /**
     * Short King Capture (All 4 Directions, 1 step jump):
     * Jumps over 1 immediately adjacent enemy piece to the immediate square behind it.
     * Cannot slide across multiple empty squares.
     */
    @Override
    protected void findKingCaptures(Board board, Position origin, Position current, PieceColor turn,
                                    Set<Position> pendingRemoval, List<Position> path, List<Move> outMoves) {
        boolean foundFurtherJump = false;

        for (int[] dir : ALL_DIRECTIONS) {
            int dRow = dir[0];
            int dCol = dir[1];
            Position mid = current.add(dRow, dCol);
            Position land = current.add(2 * dRow, 2 * dCol);

            if (mid.isValid() && land.isValid() && !pendingRemoval.contains(mid)) {
                Piece midPiece = board.getPiece(mid);
                if (midPiece != null && midPiece.color() != turn && (board.isEmpty(land) || land.equals(origin))) {
                    foundFurtherJump = true;
                    pendingRemoval.add(mid);
                    path.add(land);

                    findKingCaptures(board, origin, land, turn, pendingRemoval, path, outMoves);

                    path.remove(path.size() - 1);
                    pendingRemoval.remove(mid);
                }
            }
        }

        if (!foundFurtherJump && path.size() > 1) {
            outMoves.add(new Move(origin, current, new ArrayList<>(path), new ArrayList<>(pendingRemoval), false));
        }
    }
}
