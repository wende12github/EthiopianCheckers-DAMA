package com.dama.ethiopiancheckersgame.player.ai;

import com.dama.ethiopiancheckersgame.core.model.GameState;
import com.dama.ethiopiancheckersgame.core.model.Move;
import com.dama.ethiopiancheckersgame.core.model.PieceColor;
import com.dama.ethiopiancheckersgame.core.rules.EthiopianDamaRules;
import com.dama.ethiopiancheckersgame.player.Player;

import java.util.concurrent.CompletableFuture;

/**
 * Autonomous AI Computer Player.
 */
public class AIPlayer implements Player {
    private final String name;
    private final PieceColor color;
    private final AIDifficulty difficulty;
    private final MinimaxEngine minimaxEngine;

    public AIPlayer(String name, PieceColor color, AIDifficulty difficulty, EthiopianDamaRules rules) {
        this.name = name;
        this.color = color;
        this.difficulty = difficulty;
        this.minimaxEngine = new MinimaxEngine(rules);
    }

    @Override
    public String getName() {
        return name + " (" + difficulty.getLabel() + ")";
    }

    @Override
    public PieceColor getColor() {
        return color;
    }

    @Override
    public boolean isAI() {
        return true;
    }

    public AIDifficulty getDifficulty() {
        return difficulty;
    }

    @Override
    public CompletableFuture<Move> requestMove(GameState state) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Natural pause for human player to observe
                Thread.sleep(difficulty.getSimulatedThinkingDelayMs());
            } catch (InterruptedException ignored) {
            }
            return minimaxEngine.findBestMove(state.getBoard(), color, difficulty);
        });
    }
}
