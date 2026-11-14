package com.dama.ethiopiancheckersgame.ui.gui;

import com.dama.ethiopiancheckersgame.core.model.Board;
import com.dama.ethiopiancheckersgame.core.model.PieceColor;
import com.dama.ethiopiancheckersgame.engine.GameSession;
import com.dama.ethiopiancheckersgame.player.Player;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * Isolated status and control sidebar panel.
 * Displays player scores, active turn, piece counts, and control buttons.
 */
public class ControlPanel extends VBox {
    private final GameSession session;
    private final Runnable onUndoRequested;
    private final Runnable onRestartRequested;
    private final Runnable onMenuRequested;

    private final Label turnBanner;
    private final Label p1ScoreLabel;
    private final Label p2ScoreLabel;
    private final Label p1KingLabel;
    private final Label p2KingLabel;
    private final VBox p1Card;
    private final VBox p2Card;

    public ControlPanel(GameSession session, Runnable onUndoRequested, Runnable onRestartRequested, Runnable onMenuRequested) {
        this.session = session;
        this.onUndoRequested = onUndoRequested;
        this.onRestartRequested = onRestartRequested;
        this.onMenuRequested = onMenuRequested;

        setPrefWidth(260);
        setPadding(new Insets(20));
        setSpacing(16);
        setStyle("-fx-background-color: #2D2D2D; -fx-background-radius: 8px;");

        // Game Title
        Label titleLabel = new Label("Ethiopian Checkers");
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        titleLabel.setTextFill(Color.web("#FFFFFF"));

        Label amharicSubtitle = new Label("የኢትዮጵያ ዳማ (Dama)");
        amharicSubtitle.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 13));
        amharicSubtitle.setTextFill(Color.web("#B0BEC5"));

        // Turn Banner
        turnBanner = new Label("Turn: Player 1");
        turnBanner.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        turnBanner.setTextFill(Color.web("#FFFFFF"));
        turnBanner.setPadding(new Insets(10, 14, 10, 14));
        turnBanner.setMaxWidth(Double.MAX_VALUE);
        turnBanner.setAlignment(Pos.CENTER);
        turnBanner.setStyle("-fx-background-color: #43A047; -fx-background-radius: 6px;");

        // Player 1 Card (Red)
        p1Card = createPlayerCard(session.getPlayer1().getName(), PieceColor.RED);
        p1ScoreLabel = new Label("Pieces: 12");
        p1ScoreLabel.setTextFill(Color.web("#ECEFF1"));
        p1KingLabel = new Label("Kings: 0");
        p1KingLabel.setTextFill(Color.web("#FFD54F"));
        p1Card.getChildren().addAll(p1ScoreLabel, p1KingLabel);

        // Player 2 Card (Black / AI)
        p2Card = createPlayerCard(session.getPlayer2().getName(), PieceColor.BLACK);
        p2ScoreLabel = new Label("Pieces: 12");
        p2ScoreLabel.setTextFill(Color.web("#ECEFF1"));
        p2KingLabel = new Label("Kings: 0");
        p2KingLabel.setTextFill(Color.web("#FFD54F"));
        p2Card.getChildren().addAll(p2ScoreLabel, p2KingLabel);

        // Buttons
        Button undoBtn = createStyledButton("⤺ Undo Move", "#546E7A");
        undoBtn.setOnAction(e -> onUndoRequested.run());

        Button restartBtn = createStyledButton("↻ Restart Game", "#455A64");
        restartBtn.setOnAction(e -> onRestartRequested.run());

        Button menuBtn = createStyledButton("☰ Main Menu", "#37474F");
        menuBtn.setOnAction(e -> onMenuRequested.run());

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        getChildren().addAll(
                titleLabel, amharicSubtitle,
                turnBanner,
                p1Card, p2Card,
                spacer,
                undoBtn, restartBtn, menuBtn
        );

        updateState();
    }

    private VBox createPlayerCard(String name, PieceColor color) {
        VBox card = new VBox(6);
        card.setPadding(new Insets(10));
        card.setStyle("-fx-background-color: #383838; -fx-background-radius: 6px;");

        HBox header = new HBox(8);
        header.setAlignment(Pos.CENTER_LEFT);

        Circle colorIndicator = new Circle(7, color == PieceColor.RED ? Theme.PIECE_RED_FILL : Theme.PIECE_BLACK_FILL);
        colorIndicator.setStroke(Color.WHITE);
        colorIndicator.setStrokeWidth(1.2);

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
        btn.setPadding(new Insets(9, 12, 9, 12));
        btn.setStyle("-fx-background-color: " + bgColor + "; -fx-background-radius: 5px; -fx-cursor: hand;");
        return btn;
    }

    public void updateState() {
        Board board = session.getCurrentState().getBoard();
        Player current = session.getCurrentPlayer();
        PieceColor turn = session.getCurrentState().getCurrentTurn();

        // Update counts
        p1ScoreLabel.setText("Pieces remaining: " + board.countPieces(PieceColor.RED));
        p1KingLabel.setText("Kings (Dama): " + board.countKings(PieceColor.RED));

        p2ScoreLabel.setText("Pieces remaining: " + board.countPieces(PieceColor.BLACK));
        p2KingLabel.setText("Kings (Dama): " + board.countKings(PieceColor.BLACK));

        // Active turn highlight
        if (session.getCurrentState().isGameOver()) {
            turnBanner.setText("Game Over!");
            turnBanner.setStyle("-fx-background-color: #D32F2F; -fx-background-radius: 6px;");
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
        p1Card.setStyle(turn == PieceColor.RED ?
                "-fx-background-color: #424242; -fx-border-color: #E53935; -fx-border-width: 2px; -fx-background-radius: 6px; -fx-border-radius: 6px;" :
                "-fx-background-color: #383838; -fx-background-radius: 6px;");

        p2Card.setStyle(turn == PieceColor.BLACK ?
                "-fx-background-color: #424242; -fx-border-color: #1E88E5; -fx-border-width: 2px; -fx-background-radius: 6px; -fx-border-radius: 6px;" :
                "-fx-background-color: #383838; -fx-background-radius: 6px;");
    }
}
