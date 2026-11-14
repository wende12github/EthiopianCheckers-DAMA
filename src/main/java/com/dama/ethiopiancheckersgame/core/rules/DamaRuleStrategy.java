package com.dama.ethiopiancheckersgame.core.rules;

import com.dama.ethiopiancheckersgame.core.model.Board;
import com.dama.ethiopiancheckersgame.core.model.Move;
import com.dama.ethiopiancheckersgame.core.model.PieceColor;
import com.dama.ethiopiancheckersgame.core.model.Position;

import java.util.List;

/**
 * Strategy interface defining move generation and rules for Ethiopian Dama variants.
 */
public interface DamaRuleStrategy {
    DamaVariant getVariant();

    List<Move> getLegalMoves(Board board, PieceColor turn);

    List<Position> getMandatoryCapturePieces(Board board, PieceColor turn);

    List<Move> getLegalMovesForPiece(Board board, PieceColor turn, Position from);

    void applyMove(Board board, Move move, PieceColor turn);

    boolean isGameOver(Board board, PieceColor turn);
}
