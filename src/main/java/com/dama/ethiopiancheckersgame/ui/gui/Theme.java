package com.dama.ethiopiancheckersgame.ui.gui;

import javafx.scene.paint.Color;

/**
 * Visual styling constants for Ethiopian Checkers.
 * Changing values here or in theme.css modifies visual themes without affecting game logic.
 */
public class Theme {
    // Board tiles
    public static final Color TILE_LIGHT = Color.web("#F0D9B5"); // Soft birch wood
    public static final Color TILE_DARK = Color.web("#B58863");  // Warm walnut wood
    public static final Color TILE_BORDER = Color.web("#7A5538");

    // Pieces
    public static final Color PIECE_RED_FILL = Color.web("#D32F2F");
    public static final Color PIECE_RED_BORDER = Color.web("#8E0000");
    public static final Color PIECE_BLACK_FILL = Color.web("#263238");
    public static final Color PIECE_BLACK_BORDER = Color.web("#000A12");

    // Highlights & UI feedback
    public static final Color SELECTED_GLOW = Color.web("#FFD700", 0.7);       // Gold
    public static final Color VALID_MOVE_DOT = Color.web("#4CAF50", 0.65);     // Soft green
    public static final Color VALID_CAPTURE_RING = Color.web("#E53935", 0.75); // Vivid red
    public static final Color MANDATORY_CAPTURE_PULSE = Color.web("#FF9800", 0.75); // Amber pulse

    // Fonts & Accents
    public static final String FONT_FAMILY = "Segoe UI, Arial, sans-serif";
    public static final Color ACCENT_COLOR = Color.web("#5C6BC0");
}
