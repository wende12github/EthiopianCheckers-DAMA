package com.dama.ethiopiancheckersgame.ui.gui;

import com.dama.ethiopiancheckersgame.core.model.Board;
import com.dama.ethiopiancheckersgame.core.model.PieceColor;
import com.dama.ethiopiancheckersgame.core.rules.DamaVariant;
import com.dama.ethiopiancheckersgame.engine.GameSession;
import com.dama.ethiopiancheckersgame.player.Player;
import com.dama.ethiopiancheckersgame.ui.i18n.I18n;
import javafx.application.Platform;
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
 * Modern in-game control sidebar with strict language support, live score, turn indicators,
 * player clocks, theme customizer, draw offer handler, and language toggle.
 */
public class ControlPanel extends VBox {
    private final GameSession session;
    private final Runnable onUndoRequested;
    private final Runnable onRestartRequested;
    private final Runnable onMenuRequested;
    private final Runnable onReviewRequested;

    private final Label titleLabel;
    private final Label turnBanner;
    private final Label variantBadge;
    private final Label gameOverTitle;
    private final Button newGameBtn;
    private final Button reviewBtn;
    private final Button undoBtn;
    private final Button drawBtn;
    private final Button themeBtn;
    private final Button restartBtn;
    private final Button menuBtn;

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
        setPadding(new Insets(16));
        setSpacing(10);
        setStyle("-fx-background-color: #2D2D2D; -fx-background-radius: 8px;");

        // Top Header: Game Title
        titleLabel = new Label();
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        titleLabel.setTextFill(Color.web("#FFC107"));

        // Language Toggle in Sidebar
        LanguageToggleBar langToggle = new LanguageToggleBar();

        // Rules Variant Badge (Egregna vs Tankegna)
        variantBadge = new Label();
        variantBadge.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));
        variantBadge.setTextFill(Color.web("#FFD54F"));
        variantBadge.setPadding(new Insets(4, 8, 4, 8));
        variantBadge.setStyle("-fx-background-color: #37474F; -fx-background-radius: 4px;");

        // Turn Banner
        turnBanner = new Label();
        turnBanner.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        turnBanner.setTextFill(Color.web("#FFFFFF"));
        turnBanner.setPadding(new Insets(8, 12, 8, 12));
        turnBanner.setMaxWidth(Double.MAX_VALUE);
        turnBanner.setAlignment(Pos.CENTER);
        turnBanner.setStyle("-fx-background-color: #43A047; -fx-background-radius: 6px;");

        // Player 1 Card
        p1ColorIndicator = new Circle(7, Theme.getCurrent().getPieceRedFill());
        p1ColorIndicator.setStroke(Color.WHITE);
        p1ColorIndicator.setStrokeWidth(1.2);
        p1Card = createPlayerCard(session.getPlayer1().getName(), p1ColorIndicator);
        p1ScoreLabel = new Label();
        p1ScoreLabel.setTextFill(Color.web("#ECEFF1"));
        p1KingLabel = new Label();
        p1KingLabel.setTextFill(Color.web("#FFD54F"));
        p1Card.getChildren().addAll(p1ScoreLabel, p1KingLabel);

        // Player 2 Card
        p2ColorIndicator = new Circle(7, Theme.getCurrent().getPieceBlackFill());
        p2ColorIndicator.setStroke(Color.WHITE);
        p2ColorIndicator.setStrokeWidth(1.2);
        p2Card = createPlayerCard(session.getPlayer2().getName(), p2ColorIndicator);
        p2ScoreLabel = new Label();
        p2ScoreLabel.setTextFill(Color.web("#ECEFF1"));
        p2KingLabel = new Label();
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

        gameOverTitle = new Label();
        gameOverTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        gameOverTitle.setTextFill(Color.web("#FFD54F"));
        gameOverTitle.setAlignment(Pos.CENTER);
        gameOverTitle.setMaxWidth(Double.MAX_VALUE);

        newGameBtn = createStyledButton("", "#2E7D32");
        newGameBtn.setOnAction(e -> onRestartRequested.run());

        reviewBtn = createStyledButton("", "#0288D1");
        reviewBtn.setOnAction(e -> onReviewRequested.run());

        gameOverBox.getChildren().addAll(gameOverTitle, newGameBtn, reviewBtn);

        // Standard Buttons
        undoBtn = createStyledButton("", "#546E7A");
        if (session.getGameMode() == com.dama.ethiopiancheckersgame.engine.GameMode.ONLINE_MULTIPLAYER) {
            undoBtn.setDisable(true); // Undo disabled in competitive online play
        }
        undoBtn.setOnAction(e -> onUndoRequested.run());

        drawBtn = createStyledButton("", "#455A64");
        drawBtn.setOnAction(e -> handleDrawOffer());

        themeBtn = createStyledButton("", "#00838F");
        themeBtn.setOnAction(e -> ThemeCustomizerDialog.show((Stage) getScene().getWindow()));

        restartBtn = createStyledButton("", "#37474F");
        restartBtn.setOnAction(e -> onRestartRequested.run());

        menuBtn = createStyledButton("", "#263238");
        menuBtn.setOnAction(e -> onMenuRequested.run());

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        getChildren().addAll(
                titleLabel,
                langToggle,
                variantBadge,
                turnBanner,
                gameOverBox,
                p1Card, p2Card,
                spacer,
                undoBtn, drawBtn, themeBtn, restartBtn, menuBtn
        );

        I18n.addListener(lang -> Platform.runLater(this::updateLanguage));

        updateLanguage();
        updateState();
    }

    private void updateLanguage() {
        titleLabel.setText(I18n.get("app.title"));

        String variantName = (session.getVariant() == DamaVariant.EGREGNA) ?
                I18n.get("variant.egregna.title") : I18n.get("variant.tankegna.title");
        variantBadge.setText(variantName);

        gameOverTitle.setText(I18n.get("game.concluded"));
        newGameBtn.setText(I18n.get("btn.new_game"));
        reviewBtn.setText(I18n.get("btn.review_board"));

        undoBtn.setText(I18n.get("btn.undo"));
        drawBtn.setText(I18n.get("btn.draw"));
        themeBtn.setText(I18n.get("btn.theme"));
        restartBtn.setText(I18n.get("btn.restart"));
        menuBtn.setText(I18n.get("btn.menu"));

        updateState();
    }

    private void handleDrawOffer() {
        if (!session.isRunning()) return;

        if (session.getGameMode() == com.dama.ethiopiancheckersgame.engine.GameMode.HUMAN_VS_AI) {
            boolean accepted = session.offerDraw(session.getCurrentPlayer());
            if (!accepted) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION,
                        I18n.get("dialog.draw_declined_ai"),
                        ButtonType.OK);
                alert.setTitle(I18n.get("dialog.draw_declined_title"));
                alert.setHeaderText(null);
                alert.showAndWait();
            }
        } else if (session.getGameMode() == com.dama.ethiopiancheckersgame.engine.GameMode.ONLINE_MULTIPLAYER) {
            Player other = (session.getPlayer1() instanceof com.dama.ethiopiancheckersgame.player.NetworkPlayer) ?
                    session.getPlayer1() : session.getPlayer2();
            if (other instanceof com.dama.ethiopiancheckersgame.player.NetworkPlayer np) {
                np.getNetworkClient().offerDraw();
                Alert alert = new Alert(Alert.AlertType.INFORMATION,
                        I18n.get("dialog.draw_sent_body"),
                        ButtonType.OK);
                alert.setTitle(I18n.get("dialog.draw_sent_title"));
                alert.setHeaderText(null);
                alert.showAndWait();
            }
        } else {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                    I18n.get("dialog.draw_offered_body"),
                    ButtonType.YES, ButtonType.NO);
            alert.setTitle(I18n.get("dialog.draw_offered_title"));
            alert.setHeaderText(I18n.get("dialog.draw_offered_header"));
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
        btn.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
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
        p1ScoreLabel.setText(I18n.get("game.pieces_remaining") + board.countPieces(PieceColor.RED));
        p1KingLabel.setText(I18n.get("game.kings") + board.countKings(PieceColor.RED));

        p2ScoreLabel.setText(I18n.get("game.pieces_remaining") + board.countPieces(PieceColor.BLACK));
        p2KingLabel.setText(I18n.get("game.kings") + board.countKings(PieceColor.BLACK));

        // Game Over and Review Mode handling
        gameOverBox.setVisible(isGameOver);
        gameOverBox.setManaged(isGameOver);

        if (isGameOver) {
            if (isReviewMode) {
                turnBanner.setText(I18n.get("game.review_mode"));
                turnBanner.setStyle("-fx-background-color: #0288D1; -fx-background-radius: 6px;");
            } else if (session.getCurrentState().isDraw()) {
                turnBanner.setText(I18n.get("game.draw_banner"));
                turnBanner.setStyle("-fx-background-color: #546E7A; -fx-background-radius: 6px;");
            } else {
                turnBanner.setText(I18n.get("game.game_over"));
                turnBanner.setStyle("-fx-background-color: #D32F2F; -fx-background-radius: 6px;");
            }
        } else {
            if (current.isAI()) {
                turnBanner.setText(I18n.get("game.ai_thinking"));
                turnBanner.setStyle("-fx-background-color: #F57C00; -fx-background-radius: 6px;");
            } else {
                turnBanner.setText(I18n.get("game.turn") + current.getName());
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
