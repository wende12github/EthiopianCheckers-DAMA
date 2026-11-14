package com.dama.ethiopiancheckersgame.ui.gui;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * Authentic Ethiopian Tibeb (ጥበብ) decorative geometric textile banner.
 * Procedurally draws traditional Ethiopian diamond, chevron, and cross patterns
 * in emerald green, radiant gold, and imperial crimson.
 */
public class TibebBanner extends Canvas {
    private static final Color GOLD = Color.web("#FFC107");
    private static final Color GREEN = Color.web("#2E7D32");
    private static final Color CRIMSON = Color.web("#C62828");
    private static final Color IVORY = Color.web("#FFF9C4");
    private static final Color OBSIDIAN = Color.web("#181818");

    public TibebBanner(double width, double height) {
        super(width, height);
        drawPattern();
    }

    public void updateWidth(double newWidth) {
        setWidth(newWidth);
        drawPattern();
    }

    private void drawPattern() {
        GraphicsContext gc = getGraphicsContext2D();
        double w = getWidth();
        double h = getHeight();

        // Background
        gc.setFill(OBSIDIAN);
        gc.fillRect(0, 0, w, h);

        // Thin top and bottom gold accent bands
        gc.setStroke(GOLD);
        gc.setLineWidth(2.0);
        gc.strokeLine(0, 2, w, 2);
        gc.strokeLine(0, h - 2, w, h - 2);

        // Repeated Ethiopian diamond & chevron units
        double unitWidth = 32.0;
        int count = (int) Math.ceil(w / unitWidth) + 1;

        double midY = h / 2.0;
        double radius = Math.min(unitWidth / 2.2, h / 2.4);

        for (int i = 0; i < count; i++) {
            double cx = i * unitWidth + (unitWidth / 2.0);

            // Outer Diamond (Green)
            gc.setFill(GREEN);
            drawDiamond(gc, cx, midY, radius, radius * 0.85);

            // Middle Diamond (Gold)
            gc.setFill(GOLD);
            drawDiamond(gc, cx, midY, radius * 0.65, radius * 0.55);

            // Inner Core (Crimson)
            gc.setFill(CRIMSON);
            drawDiamond(gc, cx, midY, radius * 0.35, radius * 0.30);

            // Tiny central ivory cross dot
            gc.setFill(IVORY);
            gc.fillOval(cx - 1.5, midY - 1.5, 3.0, 3.0);

            // Connecting chevrons in between
            if (i < count - 1) {
                double connX = cx + (unitWidth / 2.0);
                gc.setStroke(GOLD);
                gc.setLineWidth(1.5);
                gc.strokeLine(connX - 4, midY - 4, connX, midY);
                gc.strokeLine(connX, midY, connX + 4, midY - 4);
                gc.strokeLine(connX - 4, midY + 4, connX, midY);
                gc.strokeLine(connX, midY, connX + 4, midY + 4);
            }
        }
    }

    private void drawDiamond(GraphicsContext gc, double cx, double cy, double rx, double ry) {
        double[] xPoints = {cx, cx + rx, cx, cx - rx};
        double[] yPoints = {cy - ry, cy, cy + ry, cy};
        gc.fillPolygon(xPoints, yPoints, 4);
    }
}
