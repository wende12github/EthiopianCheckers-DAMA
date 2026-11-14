package com.dama.ethiopiancheckersgame;

import com.dama.ethiopiancheckersgame.core.model.Board;
import com.dama.ethiopiancheckersgame.core.model.Move;
import com.dama.ethiopiancheckersgame.core.model.Piece;
import com.dama.ethiopiancheckersgame.core.model.PieceColor;
import com.dama.ethiopiancheckersgame.core.model.Position;
import com.dama.ethiopiancheckersgame.core.rules.EthiopianDamaRules;
import com.dama.ethiopiancheckersgame.player.ai.AIDifficulty;
import com.dama.ethiopiancheckersgame.player.ai.MinimaxEngine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class EthiopianDamaRulesTest {
    private EthiopianDamaRules rules;
    private Board board;

    @BeforeEach
    void setUp() {
        rules = new EthiopianDamaRules();
        board = new Board();
        board.clear();
    }

    @Test
    void testInitialBoardSetup() {
        Board initial = new Board();
        assertEquals(12, initial.countPieces(PieceColor.RED));
        assertEquals(12, initial.countPieces(PieceColor.BLACK));
        assertEquals(0, initial.countKings(PieceColor.RED));
        assertEquals(0, initial.countKings(PieceColor.BLACK));
    }

    @Test
    void testFlyingKingCanSlideAnyDistance() {
        // Place Red King at (7, 0)
        Position kingPos = new Position(7, 0);
        board.setPiece(kingPos, Piece.redKing());

        List<Move> legalMoves = rules.getLegalMoves(board, PieceColor.RED);
        // Can slide along (6,1), (5,2), (4,3), (3,4), (2,5), (1,6), (0,7) -> 7 diagonal squares
        assertEquals(7, legalMoves.size());
    }

    @Test
    void testFlyingKingCanCaptureAtDistance() {
        // Red King at (7, 0), Black Man at (4, 3)
        Position kingPos = new Position(7, 0);
        board.setPiece(kingPos, Piece.redKing());
        board.setPiece(new Position(4, 3), Piece.blackMan());

        List<Move> captures = rules.getLegalMoves(board, PieceColor.RED);
        assertFalse(captures.isEmpty());
        // All legal moves must be captures because capture is mandatory
        assertTrue(captures.stream().allMatch(Move::isCapture));

        // Landing squares can be (3, 4), (2, 5), (1, 6), (0, 7) -> 4 possible landing squares!
        assertEquals(4, captures.size());
    }

    @Test
    void testMandatoryCaptureEnforced() {
        // Red Man at (5, 2), Black Man at (4, 3). Empty square at (3, 4).
        board.setPiece(new Position(5, 2), Piece.redMan());
        board.setPiece(new Position(4, 3), Piece.blackMan());

        // Also add another Red piece at (5, 6) that has simple moves
        board.setPiece(new Position(5, 6), Piece.redMan());

        List<Move> legalMoves = rules.getLegalMoves(board, PieceColor.RED);
        // Since capture is mandatory, only the capture move is legal
        assertEquals(1, legalMoves.size());
        assertTrue(legalMoves.get(0).isCapture());
        assertEquals(new Position(5, 2), legalMoves.get(0).getFrom());
        assertEquals(new Position(3, 4), legalMoves.get(0).getTo());
    }

    @Test
    void testMaximumCaptureRule() {
        // Setup a situation where one piece captures 1, and another captures 2
        // Red piece 1 at (5, 2) can jump over (4, 3) -> (3, 4) (1 capture)
        board.setPiece(new Position(5, 2), Piece.redMan());
        board.setPiece(new Position(4, 3), Piece.blackMan());

        // Red piece 2 at (7, 0) can jump over (6, 1) to (5, 2) wait, (5,2) is occupied.
        // Let's setup: Red piece at (6, 1) jumps over (5, 2) to (4, 3) wait, let's keep it clear:
        board.clear();
        // Piece A at (6, 1): jumps (5, 2) -> (4, 3) -> jumps (3, 4) -> (2, 5) (2 captures)
        board.setPiece(new Position(6, 1), Piece.redMan());
        board.setPiece(new Position(5, 2), Piece.blackMan());
        board.setPiece(new Position(3, 4), Piece.blackMan());

        // Piece B at (6, 5): jumps (5, 6) -> (4, 7) (1 capture)
        board.setPiece(new Position(6, 5), Piece.redMan());
        board.setPiece(new Position(5, 6), Piece.blackMan());

        List<Move> legalMoves = rules.getLegalMoves(board, PieceColor.RED);
        // The maximum capture rule dictates that only the 2-capture sequence is legal!
        assertFalse(legalMoves.isEmpty());
        for (Move m : legalMoves) {
            assertEquals(2, m.getCaptureCount(), "Must choose the move capturing maximum pieces!");
        }
    }

    @Test
    void testMinimaxEnginePicksLegalMove() {
        Board initial = new Board();
        MinimaxEngine ai = new MinimaxEngine(rules);
        Move move = ai.findBestMove(initial, PieceColor.BLACK, AIDifficulty.EASY);
        assertNotNull(move);

        Move mediumMove = ai.findBestMove(initial, PieceColor.BLACK, AIDifficulty.MEDIUM);
        assertNotNull(mediumMove);
    }
}
