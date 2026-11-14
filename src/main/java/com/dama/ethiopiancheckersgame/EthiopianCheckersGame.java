package com.dama.ethiopiancheckersgame;

import com.dama.ethiopiancheckersgame.ui.gui.EthiopianCheckersApp;
import javafx.application.Application;
import javafx.stage.Stage;

/**
 * Main application launcher for backward compatibility.
 * Delegates to the modernized modular EthiopianCheckersApp.
 */
public class EthiopianCheckersGame extends Application {

    @Override
    public void start(Stage primaryStage) {
        EthiopianCheckersApp modernApp = new EthiopianCheckersApp();
        modernApp.start(primaryStage);
    }

    public static void main(String[] args) {
        try {
            Application.launch(EthiopianCheckersApp.class, args);
        } catch (Exception ex) {
            ErrorLogger.logError("Failed to launch EthiopianCheckersGame GUI", ex);
        }
    }
}

