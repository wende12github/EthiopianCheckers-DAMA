package com.dama.ethiopiancheckersgame;

import com.dama.ethiopiancheckersgame.core.model.Board;
import com.dama.ethiopiancheckersgame.core.model.Move;
import com.dama.ethiopiancheckersgame.core.model.Piece;
import com.dama.ethiopiancheckersgame.core.model.PieceColor;
import com.dama.ethiopiancheckersgame.core.model.Position;
import com.dama.ethiopiancheckersgame.core.rules.DamaVariant;
import com.dama.ethiopiancheckersgame.core.rules.EgregnaRules;
import com.dama.ethiopiancheckersgame.core.rules.EthiopianDamaRules;
import com.dama.ethiopiancheckersgame.core.rules.TankegnaRules;
import com.dama.ethiopiancheckersgame.player.ai.AIDifficulty;
import com.dama.ethiopiancheckersgame.player.ai.MinimaxEngine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class EthiopianDamaRulesTest {
    private EthiopianDamaRules rules;
    private EgregnaRules egregnaRules;
    private TankegnaRules tankegnaRules;
    private Board board;

    @BeforeEach
    void setUp() {
        rules = new EthiopianDamaRules(DamaVariant.TANKEGNA);
        egregnaRules = new EgregnaRules();
        tankegnaRules = new TankegnaRules();
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

    // =============================================================
    // Mode A: Egregna (እግረኛ - Foot Soldier Mode) Tests
    // =============================================================

    @Test
    void testEgregnaRegularPieceCannotCaptureBackward() {
        // Red Man at (4, 3) (forward direction is -1, so backward is +1)
        // Enemy Black piece placed behind Red Man at (5, 4)
        board.setPiece(new Position(4, 3), Piece.redMan());
        board.setPiece(new Position(5, 4), Piece.blackMan());

        List<Move> legalMoves = egregnaRules.getLegalMoves(board, PieceColor.RED);
        // In Egregna, regular piece cannot capture backward, so no capture exists!
        assertTrue(legalMoves.stream().noneMatch(Move::isCapture),
                "In Egregna, regular pieces must NOT capture backward");
    }

    @Test
    void testEgregnaRegularPieceCapturesForwardOnly() {
        // Red Man at (4, 3), Black piece ahead at (3, 2). Square (2, 1) is empty.
        board.setPiece(new Position(4, 3), Piece.redMan());
        board.setPiece(new Position(3, 2), Piece.blackMan());

        List<Move> legalMoves = egregnaRules.getLegalMoves(board, PieceColor.RED);
        assertEquals(1, legalMoves.size());
        assertTrue(legalMoves.get(0).isCapture());
        assertEquals(new Position(2, 1), legalMoves.get(0).getTo());
    }

    @Test
    void testEgregnaShortKingMovesOneStepOnly() {
        // Red King at center (3, 4) with empty diagonals
        board.setPiece(new Position(3, 4), Piece.redKing());

        List<Move> legalMoves = egregnaRules.getLegalMoves(board, PieceColor.RED);
        // In Egregna, Short King moves only 1 step in all 4 diagonal directions (4 moves total)
        assertEquals(4, legalMoves.size(), "Egregna Short King must only move 1 square");
        for (Move m : legalMoves) {
            int dRow = Math.abs(m.getTo().row() - m.getFrom().row());
            int dCol = Math.abs(m.getTo().col() - m.getFrom().col());
            assertEquals(1, dRow);
            assertEquals(1, dCol);
        }
    }

    @Test
    void testEgregnaShortKingCapturesOneStepOnly() {
        // Red King at (4, 3), enemy at distance (1, 0)
        // In Egregna, Short King cannot jump over distant enemy
        board.setPiece(new Position(4, 3), Piece.redKing());
        board.setPiece(new Position(1, 0), Piece.blackMan());

        List<Move> legalMoves = egregnaRules.getLegalMoves(board, PieceColor.RED);
        assertTrue(legalMoves.stream().noneMatch(Move::isCapture),
                "Short King cannot jump pieces located at a distance");

        // Now place adjacent enemy at (3, 2) with (2, 1) empty
        board.setPiece(new Position(3, 2), Piece.blackMan());
        List<Move> captureMoves = egregnaRules.getLegalMoves(board, PieceColor.RED);
        assertTrue(captureMoves.stream().anyMatch(Move::isCapture));
        assertEquals(new Position(2, 1), captureMoves.get(0).getTo());
    }

    // =============================================================
    // Mode B: Tankegna (ታንከኛ - Armored Tank Mode) Tests
    // =============================================================

    @Test
    void testTankegnaRegularPieceCapturesBackward() {
        // Red Man at (4, 3), Black piece behind at (5, 4). Empty square at (6, 5).
        board.setPiece(new Position(4, 3), Piece.redMan());
        board.setPiece(new Position(5, 4), Piece.blackMan());

        List<Move> legalMoves = tankegnaRules.getLegalMoves(board, PieceColor.RED);
        // In Tankegna, regular pieces CAN capture backward!
        assertFalse(legalMoves.isEmpty());
        assertTrue(legalMoves.stream().allMatch(Move::isCapture));
        assertEquals(new Position(6, 5), legalMoves.get(0).getTo());
    }

    @Test
    void testTankegnaFlyingKingCanSlideAnyDistance() {
        // Place Red King at (7, 0)
        Position kingPos = new Position(7, 0);
        board.setPiece(kingPos, Piece.redKing());

        List<Move> legalMoves = tankegnaRules.getLegalMoves(board, PieceColor.RED);
        // Can slide along (6,1), (5,2), (4,3), (3,4), (2,5), (1,6), (0,7) -> 7 diagonal squares
        assertEquals(7, legalMoves.size());
    }

    @Test
    void testTankegnaFlyingKingCanCaptureAtDistance() {
        // Red King at (7, 0), Black Man at (4, 3)
        Position kingPos = new Position(7, 0);
        board.setPiece(kingPos, Piece.redKing());
        board.setPiece(new Position(4, 3), Piece.blackMan());

        List<Move> captures = tankegnaRules.getLegalMoves(board, PieceColor.RED);
        assertFalse(captures.isEmpty());
        assertTrue(captures.stream().allMatch(Move::isCapture));

        // Landing squares can be (3, 4), (2, 5), (1, 6), (0, 7) -> 4 possible landing squares!
        assertEquals(4, captures.size());
    }

    @Test
    void testTankegnaPendingRemovalPreventsDoubleJump() {
        // In a multi-jump, cannot jump over the same piece twice
        board.setPiece(new Position(6, 1), Piece.redKing());
        board.setPiece(new Position(4, 3), Piece.blackMan());

        List<Move> captures = tankegnaRules.getLegalMoves(board, PieceColor.RED);
        for (Move m : captures) {
            assertEquals(1, m.getCapturedPieces().size());
        }
    }

    @Test
    void testMandatoryCaptureEnforcedInBothVariants() {
        // Red Man at (5, 2), Black Man at (4, 3). Empty square at (3, 4).
        board.setPiece(new Position(5, 2), Piece.redMan());
        board.setPiece(new Position(4, 3), Piece.blackMan());
        board.setPiece(new Position(5, 6), Piece.redMan());

        List<Move> egregnaMoves = egregnaRules.getLegalMoves(board, PieceColor.RED);
        assertEquals(1, egregnaMoves.size());
        assertTrue(egregnaMoves.get(0).isCapture());

        List<Move> tankegnaMoves = tankegnaRules.getLegalMoves(board, PieceColor.RED);
        assertEquals(1, tankegnaMoves.size());
        assertTrue(tankegnaMoves.get(0).isCapture());
    }

    @Test
    void testMaximumCaptureRuleEnforced() {
        // Piece A at (6, 1): jumps (5, 2) -> (4, 3) -> jumps (3, 4) -> (2, 5) (2 captures)
        board.setPiece(new Position(6, 1), Piece.redMan());
        board.setPiece(new Position(5, 2), Piece.blackMan());
        board.setPiece(new Position(3, 4), Piece.blackMan());

        // Piece B at (6, 5): jumps (5, 6) -> (4, 7) (1 capture)
        board.setPiece(new Position(6, 5), Piece.redMan());
        board.setPiece(new Position(5, 6), Piece.blackMan());

        List<Move> legalMoves = tankegnaRules.getLegalMoves(board, PieceColor.RED);
        assertFalse(legalMoves.isEmpty());
        for (Move m : legalMoves) {
            assertEquals(2, m.getCaptureCount(), "Must choose the move capturing maximum pieces!");
        }
    }

    @Test
    void testMinimaxEngineWithBothVariants() {
        Board initial = new Board();
        MinimaxEngine aiEgregna = new MinimaxEngine(egregnaRules);
        Move moveEgregna = aiEgregna.findBestMove(initial, PieceColor.BLACK, AIDifficulty.EASY);
        assertNotNull(moveEgregna);

        MinimaxEngine aiTankegna = new MinimaxEngine(tankegnaRules);
        Move moveTankegna = aiTankegna.findBestMove(initial, PieceColor.BLACK, AIDifficulty.MEDIUM);
        assertNotNull(moveTankegna);
    }
}
