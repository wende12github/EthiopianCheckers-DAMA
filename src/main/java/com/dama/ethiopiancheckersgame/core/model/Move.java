package com.dama.ethiopiancheckersgame.core.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a complete move in Ethiopian Dama.
 * May contain multiple intermediate jumps in a multi-capture sequence.
 */
public class Move {
    private final Position from;
    private final Position to;
    private final List<Position> steps; // All path points [from, jump1, jump2, ... to]
    private final List<Position> capturedPieces;
    private final boolean isPromotion;

    public Move(Position from, Position to) {
        this(from, to, List.of(from, to), List.of(), false);
    }

    public Move(Position from, Position to, List<Position> steps, List<Position> capturedPieces, boolean isPromotion) {
        this.from = from;
        this.to = to;
        this.steps = Collections.unmodifiableList(new ArrayList<>(steps));
        this.capturedPieces = Collections.unmodifiableList(new ArrayList<>(capturedPieces));
        this.isPromotion = isPromotion;
    }

    public Position getFrom() {
        return from;
    }

    public Position getTo() {
        return to;
    }

    public List<Position> getSteps() {
        return steps;
    }

    public List<Position> getCapturedPieces() {
        return capturedPieces;
    }

    public boolean isCapture() {
        return !capturedPieces.isEmpty();
    }

    public int getCaptureCount() {
        return capturedPieces.size();
    }

    public boolean isPromotion() {
        return isPromotion;
    }

    @Override
    public String toString() {
        if (isCapture()) {
            StringBuilder sb = new StringBuilder();
            sb.append(from);
            for (int i = 1; i < steps.size(); i++) {
                sb.append(" x ").append(steps.get(i));
            }
            return sb.toString();
        }
        return from + " -> " + to;
    }
}
