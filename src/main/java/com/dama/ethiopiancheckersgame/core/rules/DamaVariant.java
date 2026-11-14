package com.dama.ethiopiancheckersgame.core.rules;

/**
 * The two official selectable variants of Ethiopian Dama.
 */
public enum DamaVariant {
    EGREGNA("Egregna (እግረኛ - Foot Soldier)",
            "Regular: moves/captures forward only. King: moves/captures 1 step in 4 directions."),
    TANKEGNA("Tankegna (ታንከኛ - Armored Tank)",
            "Regular: moves forward, captures in 4 directions. King: Flying King (slides & captures over distance).");

    private final String displayName;
    private final String description;

    DamaVariant(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
