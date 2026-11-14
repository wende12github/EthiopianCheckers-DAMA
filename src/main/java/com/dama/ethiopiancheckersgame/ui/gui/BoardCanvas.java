package com.dama.ethiopiancheckersgame.ui.gui;

import com.dama.ethiopiancheckersgame.core.model.*;
import com.dama.ethiopiancheckersgame.engine.GameSession;
import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

/**
 * Isolated graphical canvas component for rendering the 8x8 Ethiopian Checkers board,
 * pieces, king crowns, mandatory capture indicators, and legal move targets.
 */
public class BoardCanvas extends Canvas {
    public static final double TILE_SIZE = 75.0;
    public static final double BOARD_PX = TILE_SIZE * Board.SIZE;

    private final GameSession session;
    private final Consumer<Move> onMoveRequested;

    private Position selectedPosition = null;
    private List<Move> activePieceMoves = Collections.emptyList();
    private double pulsePhase = 0.0;
    private boolean isReviewMode = false;

    public BoardCanvas(GameSession session, Consumer<Move> onMoveRequested) {
        super(BOARD_PX, BOARD_PX);
        this.session = session;
        this.onMoveRequested = onMoveRequested;

        setOnMouseClicked(e -> handleMouseClick(e.getX(), e.getY()));

        // Continuous subtle animation timer for mandatory capture glow
        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                pulsePhase += 0.05;
                render();
            }
        };
        timer.start();
    }

    public void setReviewMode(boolean reviewMode) {
        this.isReviewMode = reviewMode;
        this.selectedPosition = null;
        this.activePieceMoves = Collections.emptyList();
        render();
    }

    public boolean isReviewMode() {
        return isReviewMode;
    }

    private void handleMouseClick(double x, double y) {
        if (!session.isRunning() || session.getCurrentPlayer().isAI() || isReviewMode) {
            return;
        }

        int col = (int) (x / TILE_SIZE);
        int row = (int) (y / TILE_SIZE);
        Position clicked = new Position(row, col);

        if (!clicked.isValid()) {
            return;
        }

        Board board = session.getCurrentState().getBoard();
        PieceColor turn = session.getCurrentState().getCurrentTurn();
        Piece clickedPiece = board.getPiece(clicked);

        // Clicked on one of own pieces: select or change selection
        if (clickedPiece != null && clickedPiece.color() == turn) {
            List<Move> pieceMoves = session.getRules().getLegalMovesForPiece(board, turn, clicked);
            if (!pieceMoves.isEmpty()) {
                selectedPosition = clicked;
                activePieceMoves = pieceMoves;
            } else {
                // Piece has no legal moves (or another piece must capture)
                selectedPosition = null;
                activePieceMoves = Collections.emptyList();
            }
            return;
        }

        // If a piece is already selected, clicked on an empty destination
        if (selectedPosition != null) {
            for (Move move : activePieceMoves) {
                if (move.getTo().equals(clicked)) {
                    selectedPosition = null;
                    activePieceMoves = Collections.emptyList();
                    onMoveRequested.accept(move);
                    return;
                }
            }
            // Clicked on invalid square: deselect
            selectedPosition = null;
            activePieceMoves = Collections.emptyList();
        }
    }

    public void render() {
        GraphicsContext gc = getGraphicsContext2D();
        gc.clearRect(0, 0, BOARD_PX, BOARD_PX);

        Board board = session.getCurrentState().getBoard();
        PieceColor turn = session.getCurrentState().getCurrentTurn();
        List<Position> mandatoryPieces = session.getRules().getMandatoryCapturePieces(board, turn);

        // 1. Draw tiles
        for (int r = 0; r < Board.SIZE; r++) {
            for (int c = 0; c < Board.SIZE; c++) {
                double x = c * TILE_SIZE;
                double y = r * TILE_SIZE;
                boolean isDark = (r + c) % 2 != 0;

                gc.setFill(isDark ? Theme.TILE_DARK : Theme.TILE_LIGHT);
                gc.fillRect(x, y, TILE_SIZE, TILE_SIZE);

                // Coordinate hints on board border
                if (c == 0) {
                    gc.setFill(isDark ? Theme.TILE_LIGHT : Theme.TILE_DARK);
                    gc.setFont(Font.font("Segoe UI", 11));
                    gc.fillText(String.valueOf(8 - r), x + 4, y + 14);
                }
                if (r == 7) {
                    gc.setFill(isDark ? Theme.TILE_LIGHT : Theme.TILE_DARK);
                    gc.setFont(Font.font("Segoe UI", 11));
                    gc.fillText(String.valueOf((char) ('A' + c)), x + TILE_SIZE - 12, y + TILE_SIZE - 4);
                }
            }
        }

        // 2. Draw mandatory capture glow
        double alpha = 0.35 + 0.35 * Math.sin(pulsePhase);
        Color pulseColor = Color.color(
                Theme.MANDATORY_CAPTURE_PULSE.getRed(),
                Theme.MANDATORY_CAPTURE_PULSE.getGreen(),
                Theme.MANDATORY_CAPTURE_PULSE.getBlue(),
                alpha
        );

        for (Position pos : mandatoryPieces) {
            double x = pos.col() * TILE_SIZE;
            double y = pos.row() * TILE_SIZE;
            gc.setStroke(pulseColor);
            gc.setLineWidth(4.0);
            gc.strokeRoundRect(x + 4, y + 4, TILE_SIZE - 8, TILE_SIZE - 8, 12, 12);
        }

        // 3. Draw selected piece highlight
        if (selectedPosition != null) {
            double x = selectedPosition.col() * TILE_SIZE;
            double y = selectedPosition.row() * TILE_SIZE;
            gc.setStroke(Theme.SELECTED_GLOW);
            gc.setLineWidth(4.5);
            gc.strokeRoundRect(x + 3, y + 3, TILE_SIZE - 6, TILE_SIZE - 6, 12, 12);
        }

        // 4. Draw valid move destinations
        for (Move move : activePieceMoves) {
            Position to = move.getTo();
            double cx = to.col() * TILE_SIZE + TILE_SIZE / 2;
            double cy = to.row() * TILE_SIZE + TILE_SIZE / 2;

            if (move.isCapture()) {
                // Red ring for capture
                gc.setStroke(Theme.VALID_CAPTURE_RING);
                gc.setLineWidth(4.0);
                gc.strokeOval(cx - 16, cy - 16, 32, 32);
            } else {
                // Green dot for simple slide
                gc.setFill(Theme.VALID_MOVE_DOT);
                gc.fillOval(cx - 10, cy - 10, 20, 20);
            }
        }

        // 5. Draw pieces
        for (int r = 0; r < Board.SIZE; r++) {
            for (int c = 0; c < Board.SIZE; c++) {
                Piece piece = board.getPiece(r, c);
                if (piece != null) {
                    drawPiece(gc, c * TILE_SIZE, r * TILE_SIZE, piece);
                }
            }
        }
    }

    private void drawPiece(GraphicsContext gc, double x, double y, Piece piece) {
        double radius = TILE_SIZE * 0.38;
        double cx = x + TILE_SIZE / 2;
        double cy = y + TILE_SIZE / 2;

        boolean isRed = piece.color() == PieceColor.RED;
        Color fill = isRed ? Theme.PIECE_RED_FILL : Theme.PIECE_BLACK_FILL;
        Color border = isRed ? Theme.PIECE_RED_BORDER : Theme.PIECE_BLACK_BORDER;

        // Shadow
        gc.setFill(Color.rgb(0, 0, 0, 0.28));
        gc.fillOval(cx - radius + 2, cy - radius + 3, radius * 2, radius * 2);

        // Base piece body
        gc.setFill(fill);
        gc.fillOval(cx - radius, cy - radius, radius * 2, radius * 2);

        // Border
        gc.setStroke(border);
        gc.setLineWidth(3.0);
        gc.strokeOval(cx - radius, cy - radius, radius * 2, radius * 2);

        // Inner ridge
        gc.setStroke(Color.rgb(255, 255, 255, 0.25));
        gc.setLineWidth(1.8);
        gc.strokeOval(cx - radius * 0.72, cy - radius * 0.72, radius * 1.44, radius * 1.44);

        // King Crown (Dama / ንጉሥ)
        if (piece.isKing()) {
            drawKingCrown(gc, cx, cy, radius);
        }
    }

    /**
     * Renders a crisp, high-definition royal golden crown on King pieces (Dama).
     * Vector drawn with peaks, pearls, golden gradient, and embedded gems.
     */
    private void drawKingCrown(GraphicsContext gc, double cx, double cy, double radius) {
        double w = radius * 0.62;
        double h = radius * 0.52;
        double baseY = cy + h * 0.45;

        // Subtle ambient gold halo behind crown
        gc.setFill(Color.rgb(255, 215, 0, 0.35));
        gc.fillOval(cx - w * 1.15, cy - h * 1.05, w * 2.3, h * 1.8);

        // 7-point royal crown silhouette
        double[] xPoints = {
                cx - w,          // 0: Bottom-left
                cx - w * 1.05,   // 1: Left outer peak
                cx - w * 0.45,   // 2: Left inner valley
                cx,              // 3: Center peak (highest)
                cx + w * 0.45,   // 4: Right inner valley
                cx + w * 1.05,   // 5: Right outer peak
                cx + w           // 6: Bottom-right
        };
        double[] yPoints = {
                baseY,           // 0
                cy - h * 0.52,   // 1
                cy - h * 0.05,   // 2
                cy - h * 1.05,   // 3
                cy - h * 0.05,   // 4
                cy - h * 0.52,   // 5
                baseY            // 6
        };

        // Golden linear gradient for royal luster
        LinearGradient goldGrad = new LinearGradient(
                0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0.0, Color.web("#FFF59D")),
                new Stop(0.35, Color.web("#FFD54F")),
                new Stop(1.0, Color.web("#FF8F00"))
        );

        // 1. Crown base band
        gc.setFill(Color.web("#FFA000"));
        gc.fillRoundRect(cx - w * 0.95, baseY - 3, w * 1.9, 6.5, 3, 3);
        gc.setStroke(Color.web("#5D4037"));
        gc.setLineWidth(1.2);
        gc.strokeRoundRect(cx - w * 0.95, baseY - 3, w * 1.9, 6.5, 3, 3);

        // 2. Main crown body
        gc.setFill(goldGrad);
        gc.fillPolygon(xPoints, yPoints, 7);
        gc.setStroke(Color.web("#4E342E"));
        gc.setLineWidth(1.8);
        gc.strokePolygon(xPoints, yPoints, 7);

        // 3. Golden pearls on the 3 peaks
        gc.setFill(Color.web("#FFFDE7"));
        gc.setStroke(Color.web("#FF8F00"));
        gc.setLineWidth(1.0);

        // Left peak pearl
        gc.fillOval(xPoints[1] - 2.8, yPoints[1] - 3.8, 5.6, 5.6);
        gc.strokeOval(xPoints[1] - 2.8, yPoints[1] - 3.8, 5.6, 5.6);

        // Center peak pearl (larger)
        gc.fillOval(xPoints[3] - 3.6, yPoints[3] - 5.0, 7.2, 7.2);
        gc.strokeOval(xPoints[3] - 3.6, yPoints[3] - 5.0, 7.2, 7.2);

        // Right peak pearl
        gc.fillOval(xPoints[5] - 2.8, yPoints[5] - 3.8, 5.6, 5.6);
        gc.strokeOval(xPoints[5] - 2.8, yPoints[5] - 3.8, 5.6, 5.6);

        // 4. Embedded jewels on the base band
        // Center Ruby
        gc.setFill(Color.web("#E53935"));
        gc.fillOval(cx - 2.5, baseY - 2.2, 5.0, 5.0);

        // Left Sapphire
        gc.setFill(Color.web("#1E88E5"));
        gc.fillOval(cx - w * 0.55 - 2.0, baseY - 2.0, 4.2, 4.2);

        // Right Emerald
        gc.setFill(Color.web("#43A047"));
        gc.fillOval(cx + w * 0.55 - 2.2, baseY - 2.0, 4.2, 4.2);
    }
}
