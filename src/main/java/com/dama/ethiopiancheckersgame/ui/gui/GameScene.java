package com.dama.ethiopiancheckersgame.ui.gui;

import com.dama.ethiopiancheckersgame.core.model.Move;
import com.dama.ethiopiancheckersgame.engine.GameEventListener;
import com.dama.ethiopiancheckersgame.engine.GameSession;
import com.dama.ethiopiancheckersgame.player.HumanPlayer;
import com.dama.ethiopiancheckersgame.player.Player;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;

/**
 * Main gameplay layout containing the BoardCanvas and the ControlPanel.
 */
public class GameScene extends BorderPane implements GameEventListener {
    private final GameSession session;
    private final Runnable onMenuRequested;

    private final BoardCanvas boardCanvas;
    private final ControlPanel controlPanel;

    public GameScene(GameSession session, Runnable onMenuRequested) {
        this.session = session;
        this.onMenuRequested = onMenuRequested;

        setPadding(new Insets(16));
        setStyle("-fx-background-color: #1E1E1E;");

        // Board Canvas
        this.boardCanvas = new BoardCanvas(session, this::handleHumanMove);

        // Control Panel
        this.controlPanel = new ControlPanel(session,
                this::handleUndo,
                this::handleRestart,
                onMenuRequested
        );

        HBox mainLayout = new HBox(20);
        mainLayout.setAlignment(Pos.CENTER);
        mainLayout.getChildren().addAll(boardCanvas, controlPanel);

        setCenter(mainLayout);

        session.addListener(this);
        session.start();
    }

    private void handleHumanMove(Move move) {
        Player current = session.getCurrentPlayer();
        if (current instanceof HumanPlayer human) {
            human.submitMove(move);
            session.makeMove(move);
        }
    }

    private void handleUndo() {
        session.undo();
        controlPanel.updateState();
        boardCanvas.render();
    }

    private void handleRestart() {
        session.start();
        controlPanel.updateState();
        boardCanvas.render();
    }

    @Override
    public void onGameStarted(Player player1, Player player2) {
        Platform.runLater(() -> {
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

            Alert alert = new Alert(Alert.AlertType.INFORMATION, reason, ButtonType.OK);
            alert.setTitle("Game Finished");
            alert.setHeaderText("Winner: " + winner.getName());
            alert.showAndWait();
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
