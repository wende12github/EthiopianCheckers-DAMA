package com.dama.ethiopiancheckersgame.player;

import com.dama.ethiopiancheckersgame.core.model.Move;
import com.dama.ethiopiancheckersgame.core.model.PieceColor;
import com.dama.ethiopiancheckersgame.core.model.GameState;

import java.util.concurrent.CompletableFuture;

/**
 * Universal player contract for both human players and AI players.
 */
public interface Player {
    String getName();
    PieceColor getColor();
    boolean isAI();

    /**
     * Request the player to choose their move.
     * Completes asynchronously when a move is selected (from UI click, CLI input, or AI calculation).
     */
    CompletableFuture<Move> requestMove(GameState state);
}
