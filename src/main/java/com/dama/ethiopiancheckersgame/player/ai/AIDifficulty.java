package com.dama.ethiopiancheckersgame.player.ai;

/**
 * AI Difficulty Levels for Computer Player.
 */
public enum AIDifficulty {
    EASY("Easy (Novice)", 1, 150),
    MEDIUM("Medium (Tactical)", 3, 350),
    HARD("Hard (Master)", 5, 500);

    private final String label;
    private final int searchDepth;
    private final int simulatedThinkingDelayMs;

    AIDifficulty(String label, int searchDepth, int simulatedThinkingDelayMs) {
        this.label = label;
        this.searchDepth = searchDepth;
        this.simulatedThinkingDelayMs = simulatedThinkingDelayMs;
    }

    public String getLabel() {
        return label;
    }

    public int getSearchDepth() {
        return searchDepth;
    }

    public int getSimulatedThinkingDelayMs() {
        return simulatedThinkingDelayMs;
    }
}
