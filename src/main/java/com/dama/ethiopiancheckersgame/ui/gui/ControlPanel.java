package com.dama.ethiopiancheckersgame.ui.gui;

import com.dama.ethiopiancheckersgame.core.model.Board;
import com.dama.ethiopiancheckersgame.core.model.PieceColor;
import com.dama.ethiopiancheckersgame.engine.GameSession;
import com.dama.ethiopiancheckersgame.player.Player;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import java.util.Optional;

/**
 * Isolated status and control sidebar panel.
 * Displays player scores, active variant badge, active turn, piece counts, and control buttons.
 */
public class ControlPanel extends VBox {
    private final GameSession session;
    private final Runnable onUndoRequested;
    private final Runnable onRestartRequested;
    private final Runnable onMenuRequested;
    private final Runnable onReviewRequested;

    private final Label turnBanner;
    private final Label variantBadge;
    private final Label p1ScoreLabel;
    private final Label p2ScoreLabel;
    private final Label p1KingLabel;
    private final Label p2KingLabel;
    private final Circle p1ColorIndicator;
    private final Circle p2ColorIndicator;
    private final VBox p1Card;
    private final VBox p2Card;
    private final VBox gameOverBox;
    private boolean isReviewMode = false;

    public ControlPanel(GameSession session, Runnable onUndoRequested, Runnable onRestartRequested, Runnable onMenuRequested, Runnable onReviewRequested) {
        this.session = session;
        this.onUndoRequested = onUndoRequested;
        this.onRestartRequested = onRestartRequested;
        this.onMenuRequested = onMenuRequested;
        this.onReviewRequested = onReviewRequested;

        setPrefWidth(260);
        setPadding(new Insets(18));
        setSpacing(12);
        setStyle("-fx-background-color: #2D2D2D; -fx-background-radius: 8px;");

        // Game Title
        Label titleLabel = new Label("Ethiopian Checkers");
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        titleLabel.setTextFill(Color.web("#FFFFFF"));

        Label amharicSubtitle = new Label("የኢትዮጵያ ዳማ (Dama)");
        amharicSubtitle.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 13));
        amharicSubtitle.setTextFill(Color.web("#B0BEC5"));

        // Rules Variant Badge (Egregna vs Tankegna)
        variantBadge = new Label("Rules: " + session.getVariant().getDisplayName());
        variantBadge.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));
        variantBadge.setTextFill(Color.web("#FFD54F"));
        variantBadge.setPadding(new Insets(4, 8, 4, 8));
        variantBadge.setStyle("-fx-background-color: #37474F; -fx-background-radius: 4px;");

        // Turn Banner
        turnBanner = new Label("Turn: Player 1");
        turnBanner.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        turnBanner.setTextFill(Color.web("#FFFFFF"));
        turnBanner.setPadding(new Insets(10, 14, 10, 14));
        turnBanner.setMaxWidth(Double.MAX_VALUE);
        turnBanner.setAlignment(Pos.CENTER);
        turnBanner.setStyle("-fx-background-color: #43A047; -fx-background-radius: 6px;");

        // Player 1 Card
        p1ColorIndicator = new Circle(7, Theme.getCurrent().getPieceRedFill());
        p1ColorIndicator.setStroke(Color.WHITE);
        p1ColorIndicator.setStrokeWidth(1.2);
        p1Card = createPlayerCard(session.getPlayer1().getName(), p1ColorIndicator);
        p1ScoreLabel = new Label("Pieces: 12");
        p1ScoreLabel.setTextFill(Color.web("#ECEFF1"));
        p1KingLabel = new Label("Kings: 0");
        p1KingLabel.setTextFill(Color.web("#FFD54F"));
        p1Card.getChildren().addAll(p1ScoreLabel, p1KingLabel);

        // Player 2 Card
        p2ColorIndicator = new Circle(7, Theme.getCurrent().getPieceBlackFill());
        p2ColorIndicator.setStroke(Color.WHITE);
        p2ColorIndicator.setStrokeWidth(1.2);
        p2Card = createPlayerCard(session.getPlayer2().getName(), p2ColorIndicator);
        p2ScoreLabel = new Label("Pieces: 12");
        p2ScoreLabel.setTextFill(Color.web("#ECEFF1"));
        p2KingLabel = new Label("Kings: 0");
        p2KingLabel.setTextFill(Color.web("#FFD54F"));
        p2Card.getChildren().addAll(p2ScoreLabel, p2KingLabel);

        // Listen for Theme Updates
        Theme.addThemeListener(t -> {
            p1ColorIndicator.setFill(t.getPieceRedFill());
            p2ColorIndicator.setFill(t.getPieceBlackFill());
        });

        // Game Over Actions Box
        gameOverBox = new VBox(8);
        gameOverBox.setPadding(new Insets(10));
        gameOverBox.setStyle("-fx-background-color: #212121; -fx-background-radius: 6px; -fx-border-color: #FFB300; -fx-border-width: 1.5px; -fx-border-radius: 6px;");
        gameOverBox.setVisible(false);
        gameOverBox.setManaged(false);

        Label gameOverTitle = new Label("Match Concluded");
        gameOverTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        gameOverTitle.setTextFill(Color.web("#FFD54F"));
        gameOverTitle.setAlignment(Pos.CENTER);
        gameOverTitle.setMaxWidth(Double.MAX_VALUE);

        Button newGameBtn = createStyledButton("▶ New Game", "#2E7D32");
        newGameBtn.setOnAction(e -> onRestartRequested.run());

        Button reviewBtn = createStyledButton("🔍 Review Board", "#0288D1");
        reviewBtn.setOnAction(e -> onReviewRequested.run());

        gameOverBox.getChildren().addAll(gameOverTitle, newGameBtn, reviewBtn);

        // Standard Buttons
        Button undoBtn = createStyledButton("⤺ Undo Move", "#546E7A");
        undoBtn.setOnAction(e -> onUndoRequested.run());

        Button drawBtn = createStyledButton("🤝 Offer Draw", "#455A64");
        drawBtn.setOnAction(e -> handleDrawOffer());

        Button themeBtn = createStyledButton("🎨 Theme / Colors", "#00838F");
        themeBtn.setOnAction(e -> ThemeCustomizerDialog.show((Stage) getScene().getWindow()));

        Button restartBtn = createStyledButton("↻ Restart Game", "#37474F");
        restartBtn.setOnAction(e -> onRestartRequested.run());

        Button menuBtn = createStyledButton("☰ Main Menu", "#263238");
        menuBtn.setOnAction(e -> onMenuRequested.run());

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        getChildren().addAll(
                titleLabel, amharicSubtitle,
                variantBadge,
                turnBanner,
                gameOverBox,
                p1Card, p2Card,
                spacer,
                undoBtn, drawBtn, themeBtn, restartBtn, menuBtn
        );

        updateState();
    }

    private void handleDrawOffer() {
        if (!session.isRunning()) return;

        if (session.getGameMode() == com.dama.ethiopiancheckersgame.engine.GameMode.HUMAN_VS_AI) {
            boolean accepted = session.offerDraw(session.getCurrentPlayer());
            if (!accepted) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION,
                        "The Computer evaluated the current position and determined there is still a chance to find a winner. The draw offer was declined.",
                        ButtonType.OK);
                alert.setTitle("Draw Offer Declined");
                alert.setHeaderText("AI Continues Playing");
                alert.showAndWait();
            }
        } else {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                    "A draw has been offered. Does the opponent accept?",
                    ButtonType.YES, ButtonType.NO);
            alert.setTitle("Draw Offer");
            alert.setHeaderText("Mutual Draw Proposed");
            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.YES) {
                session.declareDraw("Mutual agreement between players.");
            }
        }
    }

    private VBox createPlayerCard(String name, Circle colorIndicator) {
        VBox card = new VBox(6);
        card.setPadding(new Insets(10));
        card.setStyle("-fx-background-color: #383838; -fx-background-radius: 6px;");

        HBox header = new HBox(8);
        header.setAlignment(Pos.CENTER_LEFT);

        Label nameLabel = new Label(name);
        nameLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        nameLabel.setTextFill(Color.web("#FFFFFF"));

        header.getChildren().addAll(colorIndicator, nameLabel);
        card.getChildren().add(header);
        return card;
    }

    private Button createStyledButton(String text, String bgColor) {
        Button btn = new Button(text);
        btn.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        btn.setTextFill(Color.WHITE);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setPadding(new Insets(8, 12, 8, 12));
        btn.setStyle("-fx-background-color: " + bgColor + "; -fx-background-radius: 5px; -fx-cursor: hand;");
        return btn;
    }

    public void setReviewMode(boolean reviewMode) {
        this.isReviewMode = reviewMode;
        updateState();
    }

    public void updateState() {
        Board board = session.getCurrentState().getBoard();
        Player current = session.getCurrentPlayer();
        PieceColor turn = session.getCurrentState().getCurrentTurn();
        boolean isGameOver = session.getCurrentState().isGameOver();

        // Update counts
        p1ScoreLabel.setText("Pieces remaining: " + board.countPieces(PieceColor.RED));
        p1KingLabel.setText("Kings (Dama): " + board.countKings(PieceColor.RED));

        p2ScoreLabel.setText("Pieces remaining: " + board.countPieces(PieceColor.BLACK));
        p2KingLabel.setText("Kings (Dama): " + board.countKings(PieceColor.BLACK));

        // Game Over and Review Mode handling
        gameOverBox.setVisible(isGameOver);
        gameOverBox.setManaged(isGameOver);

        if (isGameOver) {
            if (isReviewMode) {
                turnBanner.setText("🔍 Review Mode");
                turnBanner.setStyle("-fx-background-color: #0288D1; -fx-background-radius: 6px;");
            } else if (session.getCurrentState().isDraw()) {
                turnBanner.setText("Match Drawn (አቻ)");
                turnBanner.setStyle("-fx-background-color: #546E7A; -fx-background-radius: 6px;");
            } else {
                turnBanner.setText("Game Over!");
                turnBanner.setStyle("-fx-background-color: #D32F2F; -fx-background-radius: 6px;");
            }
        } else {
            if (current.isAI()) {
                turnBanner.setText("AI Thinking...");
                turnBanner.setStyle("-fx-background-color: #F57C00; -fx-background-radius: 6px;");
            } else {
                turnBanner.setText("Turn: " + current.getName());
                turnBanner.setStyle(turn == PieceColor.RED ?
                        "-fx-background-color: #C62828; -fx-background-radius: 6px;" :
                        "-fx-background-color: #1E88E5; -fx-background-radius: 6px;");
            }
        }

        // Active card highlight
        p1Card.setStyle(turn == PieceColor.RED && !isGameOver ?
                "-fx-background-color: #424242; -fx-border-color: #E53935; -fx-border-width: 2px; -fx-background-radius: 6px; -fx-border-radius: 6px;" :
                "-fx-background-color: #383838; -fx-background-radius: 6px;");

        p2Card.setStyle(turn == PieceColor.BLACK && !isGameOver ?
                "-fx-background-color: #424242; -fx-border-color: #1E88E5; -fx-border-width: 2px; -fx-background-radius: 6px; -fx-border-radius: 6px;" :
                "-fx-background-color: #383838; -fx-background-radius: 6px;");
    }
}
