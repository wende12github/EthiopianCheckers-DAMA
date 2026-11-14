package com.dama.ethiopiancheckersgame.engine;

/**
 * Game Modes available.
 */
public enum GameMode {
    HUMAN_VS_HUMAN("Two Players (Pass & Play)"),
    HUMAN_VS_AI("Player vs Computer (AI)"),
    ONLINE_MULTIPLAYER("Online Multiplayer (በኢንተርኔት)");

    private final String description;

    GameMode(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
