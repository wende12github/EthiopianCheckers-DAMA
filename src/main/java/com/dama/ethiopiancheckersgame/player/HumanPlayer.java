package com.dama.ethiopiancheckersgame.player;

import com.dama.ethiopiancheckersgame.core.model.GameState;
import com.dama.ethiopiancheckersgame.core.model.Move;
import com.dama.ethiopiancheckersgame.core.model.PieceColor;

import java.util.concurrent.CompletableFuture;

/**
 * Human player waiting for user interaction via UI or CLI.
 */
public class HumanPlayer implements Player {
    private final String name;
    private final PieceColor color;
    private CompletableFuture<Move> pendingMovePromise;

    public HumanPlayer(String name, PieceColor color) {
        this.name = name;
        this.color = color;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public PieceColor getColor() {
        return color;
    }

    @Override
    public boolean isAI() {
        return false;
    }

    @Override
    public CompletableFuture<Move> requestMove(GameState state) {
        pendingMovePromise = new CompletableFuture<>();
        return pendingMovePromise;
    }

    /**
     * Called by UI controller when the user makes a selection.
     */
    public void submitMove(Move move) {
        if (pendingMovePromise != null && !pendingMovePromise.isDone()) {
            pendingMovePromise.complete(move);
        }
    }

    public void cancelPendingMove() {
        if (pendingMovePromise != null && !pendingMovePromise.isDone()) {
            pendingMovePromise.cancel(true);
        }
    }
}
