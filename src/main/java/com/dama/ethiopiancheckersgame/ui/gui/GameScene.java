package com.dama.ethiopiancheckersgame.ui.gui;

import com.dama.ethiopiancheckersgame.core.model.Move;
import com.dama.ethiopiancheckersgame.core.model.PieceColor;
import com.dama.ethiopiancheckersgame.core.rules.DamaVariant;
import com.dama.ethiopiancheckersgame.engine.GameEventListener;
import com.dama.ethiopiancheckersgame.engine.GameMode;
import com.dama.ethiopiancheckersgame.engine.GameSession;
import com.dama.ethiopiancheckersgame.network.NetworkEventListener;
import com.dama.ethiopiancheckersgame.player.HumanPlayer;
import com.dama.ethiopiancheckersgame.player.NetworkPlayer;
import com.dama.ethiopiancheckersgame.player.Player;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.Optional;

/**
 * Main gameplay layout containing the BoardCanvas, ControlPanel, and interactive Game Over Overlay.
 */
public class GameScene extends BorderPane implements GameEventListener {
    private final GameSession session;
    private final Runnable onMenuRequested;

    private final BoardCanvas boardCanvas;
    private final ControlPanel controlPanel;
    private final VBox gameOverOverlay;
    private final Label trophyLabel;
    private final Label titleLabel;
    private final Label winnerNameLabel;
    private final Label reasonLabel;

    public GameScene(GameSession session, Runnable onMenuRequested) {
        this.session = session;
        this.onMenuRequested = onMenuRequested;

        setPadding(new Insets(16));
        setStyle("-fx-background-color: #1E1E1E;");

        // Board Canvas
        this.boardCanvas = new BoardCanvas(session, this::handleHumanMove);

        // Game Over Floating Overlay
        this.gameOverOverlay = new VBox(14);
        this.gameOverOverlay.setAlignment(Pos.CENTER);
        this.gameOverOverlay.setMaxSize(360, 240);
        this.gameOverOverlay.setPadding(new Insets(24));
        this.gameOverOverlay.setStyle(
                "-fx-background-color: rgba(30, 30, 30, 0.94); " +
                "-fx-background-radius: 12px; " +
                "-fx-border-color: #FFD54F; " +
                "-fx-border-width: 2.5px; " +
                "-fx-border-radius: 12px; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.8), 20, 0, 0, 5);"
        );
        this.gameOverOverlay.setVisible(false);
        this.gameOverOverlay.setManaged(false);

        this.trophyLabel = new Label("👑");
        this.trophyLabel.setFont(Font.font("Segoe UI Emoji, Arial", 38));

        this.titleLabel = new Label("GAME CONCLUDED");
        this.titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));
        this.titleLabel.setTextFill(Color.web("#FFD54F"));

        this.winnerNameLabel = new Label();
        this.winnerNameLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        this.winnerNameLabel.setTextFill(Color.web("#FFFFFF"));

        this.reasonLabel = new Label();
        this.reasonLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 12));
        this.reasonLabel.setTextFill(Color.web("#B0BEC5"));
        this.reasonLabel.setWrapText(true);

        // New Game & Review Game Action Buttons
        HBox buttonBox = new HBox(12);
        buttonBox.setAlignment(Pos.CENTER);

        Button newGameBtn = new Button("▶  New Game");
        newGameBtn.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        newGameBtn.setTextFill(Color.WHITE);
        newGameBtn.setPadding(new Insets(10, 16, 10, 16));
        newGameBtn.setStyle("-fx-background-color: #2E7D32; -fx-background-radius: 6px; -fx-cursor: hand;");
        newGameBtn.setOnAction(e -> handleRestart());

        Button reviewBtn = new Button("🔍  Review Game");
        reviewBtn.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        reviewBtn.setTextFill(Color.WHITE);
        reviewBtn.setPadding(new Insets(10, 16, 10, 16));
        reviewBtn.setStyle("-fx-background-color: #0288D1; -fx-background-radius: 6px; -fx-cursor: hand;");
        reviewBtn.setOnAction(e -> handleReview());

        buttonBox.getChildren().addAll(newGameBtn, reviewBtn);
        this.gameOverOverlay.getChildren().addAll(trophyLabel, titleLabel, winnerNameLabel, reasonLabel, buttonBox);

        // Center Area: Stack board and overlay
        StackPane boardStack = new StackPane();
        boardStack.setAlignment(Pos.CENTER);
        boardStack.getChildren().addAll(boardCanvas, gameOverOverlay);

        // Control Panel
        this.controlPanel = new ControlPanel(
                session,
                this::handleUndo,
                this::handleRestart,
                onMenuRequested,
                this::handleReview
        );

        HBox mainLayout = new HBox(20);
        mainLayout.setAlignment(Pos.CENTER);
        mainLayout.getChildren().addAll(boardStack, controlPanel);

        setCenter(mainLayout);

        session.addListener(this);
        setupNetworkSync();
        session.start();
    }

    private NetworkPlayer getNetworkPlayer() {
        if (session.getPlayer1() instanceof NetworkPlayer np) return np;
        if (session.getPlayer2() instanceof NetworkPlayer np) return np;
        return null;
    }

    private void setupNetworkSync() {
        if (session.getGameMode() != GameMode.ONLINE_MULTIPLAYER) return;
        NetworkPlayer np = getNetworkPlayer();
        if (np == null) return;

        np.getNetworkClient().setListener(new NetworkEventListener() {
            @Override
            public void onConnected() {}
            @Override
            public void onWaitingInQueue() {}
            @Override
            public void onRoomCreated(String roomCode) {}
            @Override
            public void onMatchStarted(PieceColor yourColor, String opponentName, DamaVariant variant) {}

            @Override
            public void onOpponentMove(Move move) {
                Platform.runLater(() -> np.submitRemoteMove(move));
            }

            @Override
            public void onOpponentDrawOffer() {
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                            "Online opponent offered a draw (አቻ). Do you accept?",
                            ButtonType.YES, ButtonType.NO);
                    alert.setTitle("Draw Offer");
                    alert.setHeaderText("Opponent Offered a Draw");
                    Optional<ButtonType> res = alert.showAndWait();
                    if (res.isPresent() && res.get() == ButtonType.YES) {
                        np.getNetworkClient().acceptDraw();
                        session.declareDraw("Mutual agreement with online opponent.");
                    } else {
                        np.getNetworkClient().declineDraw();
                    }
                });
            }

            @Override
            public void onDrawAccepted() {
                Platform.runLater(() -> session.declareDraw("Online opponent accepted the draw offer."));
            }

            @Override
            public void onDrawDeclined() {
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION,
                            "Online opponent declined the draw offer.", ButtonType.OK);
                    alert.setTitle("Draw Declined");
                    alert.showAndWait();
                });
            }

            @Override
            public void onOpponentResigned() {
                Platform.runLater(() -> {
                    Player local = (session.getPlayer1() instanceof HumanPlayer) ? session.getPlayer1() : session.getPlayer2();
                    session.getCurrentState().setGameOver(local.getColor(), "Online opponent resigned the match.");
                    onGameOver(local, "Online opponent resigned.");
                });
            }

            @Override
            public void onOpponentDisconnected() {
                Platform.runLater(() -> {
                    Player local = (session.getPlayer1() instanceof HumanPlayer) ? session.getPlayer1() : session.getPlayer2();
                    session.getCurrentState().setGameOver(local.getColor(), "Online opponent disconnected.");
                    onGameOver(local, "Online opponent disconnected.");
                });
            }

            @Override
            public void onError(String errorMessage) {
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.ERROR, errorMessage, ButtonType.OK);
                    alert.setTitle("Network Error");
                    alert.showAndWait();
                });
            }
        });
    }

    private void handleHumanMove(Move move) {
        Player current = session.getCurrentPlayer();
        if (current instanceof HumanPlayer human) {
            human.submitMove(move);
            boolean executed = session.makeMove(move);
            if (executed && session.getGameMode() == GameMode.ONLINE_MULTIPLAYER) {
                NetworkPlayer np = getNetworkPlayer();
                if (np != null) {
                    np.getNetworkClient().sendMove(move);
                }
            }
        }
    }

    private void handleUndo() {
        hideGameOverOverlay();
        boardCanvas.setReviewMode(false);
        controlPanel.setReviewMode(false);
        session.undo();
        controlPanel.updateState();
        boardCanvas.render();
    }

    private void handleRestart() {
        hideGameOverOverlay();
        boardCanvas.setReviewMode(false);
        controlPanel.setReviewMode(false);
        session.start();
        controlPanel.updateState();
        boardCanvas.render();
    }

    private void handleReview() {
        hideGameOverOverlay();
        boardCanvas.setReviewMode(true);
        controlPanel.setReviewMode(true);
        controlPanel.updateState();
        boardCanvas.render();
    }

    private void showGameOverOverlay(Player winner, String reason) {
        if (winner == null) {
            trophyLabel.setText("🤝");
            titleLabel.setText("MATCH DRAWN (አቻ)");
            titleLabel.setTextFill(Color.web("#90A4AE"));
            winnerNameLabel.setText("Result: Draw (No Winner)");
        } else {
            trophyLabel.setText("👑");
            titleLabel.setText("GAME CONCLUDED");
            titleLabel.setTextFill(Color.web("#FFD54F"));
            winnerNameLabel.setText("Winner: " + winner.getName());
        }
        reasonLabel.setText(reason);
        gameOverOverlay.setVisible(true);
        gameOverOverlay.setManaged(true);
    }

    private void hideGameOverOverlay() {
        gameOverOverlay.setVisible(false);
        gameOverOverlay.setManaged(false);
    }

    @Override
    public void onGameStarted(Player player1, Player player2) {
        Platform.runLater(() -> {
            hideGameOverOverlay();
            boardCanvas.setReviewMode(false);
            controlPanel.setReviewMode(false);
            controlPanel.updateState();
            boardCanvas.render();
        });
    }

    @Override
    public void onTurnStarted(Player currentPlayer) {
        Platform.runLater(() -> {
            controlPanel.updateState();
            boardCanvas.render();
        });
    }

    @Override
    public void onMoveExecuted(Move move, Player player) {
        Platform.runLater(() -> {
            controlPanel.updateState();
            boardCanvas.render();
        });
    }

    @Override
    public void onGameOver(Player winner, String reason) {
        Platform.runLater(() -> {
            controlPanel.updateState();
            boardCanvas.render();
            showGameOverOverlay(winner, reason);
        });
    }

    @Override
    public void onBoardReset() {
        Platform.runLater(() -> {
            controlPanel.updateState();
            boardCanvas.render();
        });
    }
}

