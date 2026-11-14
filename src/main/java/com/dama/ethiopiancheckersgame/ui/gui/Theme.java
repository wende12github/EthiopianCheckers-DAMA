package com.dama.ethiopiancheckersgame.ui.gui;

import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Customizable theme model for the board and pieces.
 * Supports built-in themes and user-defined custom color palettes.
 */
public class Theme {
    private final String name;
    private final Color tileLight;
    private final Color tileDark;
    private final Color tileBorder;
    private final Color pieceRedFill;
    private final Color pieceRedBorder;
    private final Color pieceBlackFill;
    private final Color pieceBlackBorder;
    private final Color selectedGlow;
    private final Color validMoveDot;
    private final Color validCaptureRing;
    private final Color mandatoryCapturePulse;

    public Theme(String name, Color tileLight, Color tileDark, Color pieceRedFill, Color pieceBlackFill) {
        this(name, tileLight, tileDark,
                tileDark.darker(),
                pieceRedFill, pieceRedFill.darker(),
                pieceBlackFill, pieceBlackFill.darker(),
                Color.web("#FFD700", 0.75),
                Color.web("#4CAF50", 0.65),
                Color.web("#E53935", 0.8),
                Color.web("#FF9800", 0.8)
        );
    }

    public Theme(String name, Color tileLight, Color tileDark, Color tileBorder,
                 Color pieceRedFill, Color pieceRedBorder,
                 Color pieceBlackFill, Color pieceBlackBorder,
                 Color selectedGlow, Color validMoveDot,
                 Color validCaptureRing, Color mandatoryCapturePulse) {
        this.name = name;
        this.tileLight = tileLight;
        this.tileDark = tileDark;
        this.tileBorder = tileBorder;
        this.pieceRedFill = pieceRedFill;
        this.pieceRedBorder = pieceRedBorder;
        this.pieceBlackFill = pieceBlackFill;
        this.pieceBlackBorder = pieceBlackBorder;
        this.selectedGlow = selectedGlow;
        this.validMoveDot = validMoveDot;
        this.validCaptureRing = validCaptureRing;
        this.mandatoryCapturePulse = mandatoryCapturePulse;
    }

    // Built-in Presets
    public static final Theme CLASSIC_WOOD = new Theme(
            "Classic Wood (እንጨት)",
            Color.web("#F0D9B5"), Color.web("#B58863"),
            Color.web("#D32F2F"), Color.web("#263238")
    );

    public static final Theme ETHIOPIAN_HERITAGE = new Theme(
            "Ethiopian Heritage (የኢትዮጵያ ቅርስ)",
            Color.web("#FFF8E1"), Color.web("#2E7D32"),
            Color.web("#C62828"), Color.web("#FBC02D")
    );

    public static final Theme MODERN_TOURNAMENT = new Theme(
            "Modern Tournament (ውድድር)",
            Color.web("#ECEFF1"), Color.web("#546E7A"),
            Color.web("#E53935"), Color.web("#1E88E5")
    );

    public static final Theme DARK_MODE = new Theme(
            "Midnight Dark (ጨለማ)",
            Color.web("#424242"), Color.web("#212121"),
            Color.web("#FF5252"), Color.web("#40C4FF")
    );

    public static final Theme FOREST_EMERALD = new Theme(
            "Forest Emerald (አረንጓዴ)",
            Color.web("#E8F5E9"), Color.web("#388E3C"),
            Color.web("#D81B60"), Color.web("#37474F")
    );

    public static List<Theme> getPresets() {
        return List.of(CLASSIC_WOOD, ETHIOPIAN_HERITAGE, MODERN_TOURNAMENT, DARK_MODE, FOREST_EMERALD);
    }

    // Global Active Theme State
    private static Theme currentTheme = CLASSIC_WOOD;
    private static final List<Consumer<Theme>> themeListeners = new ArrayList<>();

    public static Theme getCurrent() {
        return currentTheme;
    }

    public static void setCurrent(Theme newTheme) {
        if (newTheme != null && !Objects.equals(currentTheme, newTheme)) {
            currentTheme = newTheme;
            for (Consumer<Theme> listener : new ArrayList<>(themeListeners)) {
                listener.accept(newTheme);
            }
        }
    }

    public static void addThemeListener(Consumer<Theme> listener) {
        themeListeners.add(listener);
    }

    public static void removeThemeListener(Consumer<Theme> listener) {
        themeListeners.remove(listener);
    }

    // Getters
    public String getName() { return name; }
    public Color getTileLight() { return tileLight; }
    public Color getTileDark() { return tileDark; }
    public Color getTileBorder() { return tileBorder; }
    public Color getPieceRedFill() { return pieceRedFill; }
    public Color getPieceRedBorder() { return pieceRedBorder; }
    public Color getPieceBlackFill() { return pieceBlackFill; }
    public Color getPieceBlackBorder() { return pieceBlackBorder; }
    public Color getSelectedGlow() { return selectedGlow; }
    public Color getValidMoveDot() { return validMoveDot; }
    public Color getValidCaptureRing() { return validCaptureRing; }
    public Color getMandatoryCapturePulse() { return mandatoryCapturePulse; }

    @Override
    public String toString() {
        return name;
    }
}
