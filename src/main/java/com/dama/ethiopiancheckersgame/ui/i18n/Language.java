package com.dama.ethiopiancheckersgame.ui.i18n;

/**
 * Supported display languages.
 */
public enum Language {
    AMHARIC("አማርኛ"),
    ENGLISH("English");

    private final String displayName;

    Language(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
