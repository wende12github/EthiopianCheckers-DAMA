package com.dama.ethiopiancheckersgame.ui.gui;

import com.dama.ethiopiancheckersgame.engine.GameSession;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Modern JavaFX Application Entry Point for Ethiopian Checkers (Dama).
 */
public class EthiopianCheckersApp extends Application {
    private Stage primaryStage;

    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;
        stage.setTitle("Ethiopian Checkers (Dama / የኢትዮጵያ ዳማ)");
        showStartMenu();
        stage.show();
    }

    public void showStartMenu() {
        StartMenu menu = new StartMenu(this::startNewGame);
        Scene scene = new Scene(menu, 920, 680);
        primaryStage.setScene(scene);
    }

    public void startNewGame(GameSession session) {
        GameScene gameScene = new GameScene(session, this::showStartMenu);
        Scene scene = new Scene(gameScene, 920, 680);
        primaryStage.setScene(scene);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
