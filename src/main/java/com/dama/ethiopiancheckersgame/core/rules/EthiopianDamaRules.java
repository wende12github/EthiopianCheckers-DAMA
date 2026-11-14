package com.dama.ethiopiancheckersgame.core.rules;

import com.dama.ethiopiancheckersgame.core.model.Board;
import com.dama.ethiopiancheckersgame.core.model.Move;
import com.dama.ethiopiancheckersgame.core.model.PieceColor;
import com.dama.ethiopiancheckersgame.core.model.Position;

import java.util.List;

/**
 * Universal rules facade implementing DamaRuleStrategy.
 * Dynamically delegates to the active strategy (EgregnaRules or TankegnaRules).
 */
public class EthiopianDamaRules implements DamaRuleStrategy {
    private DamaRuleStrategy activeStrategy;

    public EthiopianDamaRules() {
        this(new TankegnaRules());
    }

    public EthiopianDamaRules(DamaRuleStrategy strategy) {
        this.activeStrategy = strategy;
    }

    public EthiopianDamaRules(DamaVariant variant) {
        this.activeStrategy = switch (variant) {
            case EGREGNA -> new EgregnaRules();
            case TANKEGNA -> new TankegnaRules();
        };
    }

    public void setStrategy(DamaRuleStrategy strategy) {
        this.activeStrategy = strategy;
    }

    public DamaRuleStrategy getActiveStrategy() {
        return activeStrategy;
    }

    @Override
    public DamaVariant getVariant() {
        return activeStrategy.getVariant();
    }

    @Override
    public List<Move> getLegalMoves(Board board, PieceColor turn) {
        return activeStrategy.getLegalMoves(board, turn);
    }

    @Override
    public List<Position> getMandatoryCapturePieces(Board board, PieceColor turn) {
        return activeStrategy.getMandatoryCapturePieces(board, turn);
    }

    @Override
    public List<Move> getLegalMovesForPiece(Board board, PieceColor turn, Position from) {
        return activeStrategy.getLegalMovesForPiece(board, turn, from);
    }

    @Override
    public void applyMove(Board board, Move move, PieceColor turn) {
        activeStrategy.applyMove(board, move, turn);
    }

    @Override
    public boolean isGameOver(Board board, PieceColor turn) {
        return activeStrategy.isGameOver(board, turn);
    }
}
