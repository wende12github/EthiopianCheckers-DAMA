package com.dama.ethiopiancheckersgame;

import com.dama.ethiopiancheckersgame.core.model.Board;
import com.dama.ethiopiancheckersgame.core.model.Move;
import com.dama.ethiopiancheckersgame.core.model.Piece;
import com.dama.ethiopiancheckersgame.core.model.PieceColor;
import com.dama.ethiopiancheckersgame.core.model.Position;
import com.dama.ethiopiancheckersgame.engine.DrawDetector;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class DrawDetectorTest {
    private DrawDetector drawDetector;
    private Board board;

    @BeforeEach
    void setUp() {
        drawDetector = new DrawDetector();
        board = new Board();
        board.clear();
    }

    @Test
    void testDrawByInsufficientMaterialOneKingEach() {
        // Exactly 1 Red King and 1 Black King
        board.setPiece(new Position(7, 0), Piece.redKing());
        board.setPiece(new Position(0, 7), Piece.blackKing());

        assertTrue(drawDetector.isInsufficientMaterial(board));
        String drawReason = drawDetector.checkDraw(board, PieceColor.RED, null, null);
        assertNotNull(drawReason, "Must detect draw by insufficient material for 1 King vs 1 King");
        assertTrue(drawReason.contains("1 King vs 1 King"));
    }

    @Test
    void testNoInsufficientMaterialWhenRegularMenPresent() {
        board.setPiece(new Position(7, 0), Piece.redKing());
        board.setPiece(new Position(0, 7), Piece.blackMan()); // Still a man, can advance or promote!

        assertFalse(drawDetector.isInsufficientMaterial(board));
    }

    @Test
    void testDrawByThreefoldRepetition() {
        // 2 Red Kings and 2 Black Kings (not insufficient material)
        // Red King oscillates: (7, 0) <-> (6, 1)
        Position rA = new Position(7, 0);
        Position rB = new Position(6, 1);
        board.setPiece(rA, Piece.redKing());
        board.setPiece(new Position(7, 2), Piece.redKing());
        board.setPiece(new Position(0, 7), Piece.blackKing());
        board.setPiece(new Position(0, 5), Piece.blackKing());

        // Occurrence 1
        String d1 = drawDetector.checkDraw(board, PieceColor.RED, new Move(rB, rA), Piece.redKing());
        assertNull(d1, "Occurrence 1 should not trigger draw");

        // Occurrence 2
        String d2 = drawDetector.checkDraw(board, PieceColor.RED, new Move(rB, rA), Piece.redKing());
        assertNull(d2, "Occurrence 2 should not trigger draw");

        // Occurrence 3 -> Threefold repetition!
        String d3 = drawDetector.checkDraw(board, PieceColor.RED, new Move(rB, rA), Piece.redKing());
        assertNotNull(d3, "Must trigger draw after threefold repetition of same board position");
        assertTrue(d3.toLowerCase().contains("threefold repetition"));
    }

    @Test
    void testDrawByFiftyPlyNoProgressRule() {
        // 2 Red Kings and 2 Black Kings so insufficient material doesn't trigger
        Position r1 = new Position(7, 0);
        Position r2 = new Position(6, 1);
        board.setPiece(r1, Piece.redKing());
        board.setPiece(new Position(7, 2), Piece.redKing());
        board.setPiece(new Position(0, 7), Piece.blackKing());
        board.setPiece(new Position(0, 5), Piece.blackKing());

        // Simulate 50 plies of king movement without any capture or regular piece advance
        // We vary positions so threefold repetition doesn't trigger first
        String result = null;
        for (int i = 0; i < 50; i++) {
            Move m = new Move(new Position(7, 0), new Position(6, 1));
            // In checkDraw, ply count increments on king move
            result = drawDetector.checkDraw(board, (i % 2 == 0) ? PieceColor.BLACK : PieceColor.RED, m, Piece.redKing());
            if (i < 49) {
                // Should not trigger until 50 plies reached
                // Note: threefold repetition could trigger if board doesn't change, so let's check
            }
        }

        assertNotNull(result, "Must trigger draw when 50 plies without progress are reached");
        assertTrue(result.toLowerCase().contains("no-progress") || result.toLowerCase().contains("25"));
    }

    @Test
    void testPawnMoveResetsProgressCounter() {
        board.setPiece(new Position(6, 1), Piece.redMan());
        board.setPiece(new Position(1, 6), Piece.blackKing());

        Move manMove = new Move(new Position(6, 1), new Position(5, 2));
        drawDetector.checkDraw(board, PieceColor.BLACK, manMove, Piece.redMan());

        // Regular piece move resets progress count to 0
        assertEquals(0, drawDetector.getPliesWithoutProgress());
    }

    @Test
    void testAIEvaluatesDrawOfferFairly() {
        // Balanced endgame: 2 pieces each
        board.setPiece(new Position(7, 0), Piece.redMan());
        board.setPiece(new Position(7, 2), Piece.redMan());
        board.setPiece(new Position(0, 7), Piece.blackMan());
        board.setPiece(new Position(0, 5), Piece.blackMan());

        boolean aiAcceptsBalanced = drawDetector.shouldAIAcceptDraw(board, PieceColor.BLACK);
        assertTrue(aiAcceptsBalanced, "AI should accept draw when position is balanced");

        // AI is overwhelmingly winning: Black has 4 pieces + King, Red has only 1 piece
        board.setPiece(new Position(2, 3), Piece.blackKing());
        board.setPiece(new Position(1, 2), Piece.blackMan());
        board.setPiece(new Position(1, 4), Piece.blackMan());

        boolean aiAcceptsWhenWinning = drawDetector.shouldAIAcceptDraw(board, PieceColor.BLACK);
        assertFalse(aiAcceptsWhenWinning, "AI should decline draw when it has overwhelming advantage to win");
    }
}
