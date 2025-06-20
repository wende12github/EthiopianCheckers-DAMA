package com.dama.ethiopiancheckersgame;

import com.dama.ethiopiancheckersgame.GameManager;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.util.List;

public class CheckerGUI extends Application {
    private Button[][] buttons;
    private GameManager gameManager;
    private int selectedRow = -1;
    private int selectedCol = -1;
    private String player1Name;
    private String player2Name;
    private Label turnLabel;
    private Stage primaryStage; // Store reference for switching scenes

    public CheckerGUI() {
        // No-arg constructor for JavaFX application
    }

    public CheckerGUI(GameManager gameManager, String player1Name, String player2Name) {
        this.gameManager = gameManager;
        // Always get names from GameManager's Player objects to ensure consistency
        this.player1Name = gameManager.getPlayer1().getName();
        this.player2Name = gameManager.getPlayer2().getName();
    }

    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;
        if (gameManager == null) {
            showNameEntryScreen();
        } else {
            showGameBoard();
        }
    }

    private void showNameEntryScreen() {
        primaryStage.setTitle("Enter Player Names");
        GridPane gridPane = new GridPane();
        gridPane.setAlignment(Pos.CENTER);
        gridPane.setPadding(new Insets(20));
        gridPane.setHgap(10);
        gridPane.setVgap(10);

        Label player1Label = new Label("Player 1's name:");
        player1Label.setFont(Font.font("Arial", 14));
        TextField player1Field = new TextField();
        player1Field.setFont(Font.font("Arial", 14));
        player1Field.setPrefWidth(200);

        Label player2Label = new Label("Player 2's name:");
        player2Label.setFont(Font.font("Arial", 14));
        TextField player2Field = new TextField();
        player2Field.setFont(Font.font("Arial", 14));
        player2Field.setPrefWidth(200);

        Button startButton = new Button("Start Game");
        startButton.setFont(Font.font("Arial", 14));
        startButton.setStyle("-fx-background-color: #739552; -fx-text-fill: white; -fx-padding: 10 20 10 20;");

        startButton.setOnAction(e -> {
            String p1 = player1Field.getText().trim();
            String p2 = player2Field.getText().trim();
            if (!p1.isEmpty() && !p2.isEmpty()) {
                gameManager = new GameManager(p1, p2);
                player1Name = p1;
                player2Name = p2;
                showGameBoard();
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Input Error");
                alert.setHeaderText(null);
                alert.setContentText("Please enter names for both players.");
                alert.showAndWait();
            }
        });

        gridPane.add(player1Label, 0, 0);
        gridPane.add(player1Field, 1, 0);
        gridPane.add(player2Label, 0, 1);
        gridPane.add(player2Field, 1, 1);
        gridPane.add(startButton, 0, 2, 2, 1);
        GridPane.setMargin(startButton, new Insets(10, 0, 0, 0));

        Scene scene = new Scene(gridPane, 700, 550);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void showGameBoard() {
        GridPane gridPane = new GridPane();
        gridPane.setAlignment(Pos.CENTER);
        buttons = new Button[8][8];
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Button button = new Button();
                button.setPrefSize(80, 80);
                button.setStyle((row + col) % 2 == 0 ? "-fx-background-color: #EBECD0;" : "-fx-background-color: #739552;");

                int currentRow = row;
                int currentCol = col;

                button.setOnAction(e -> handleClick(currentRow, currentCol));

                buttons[row][col] = button;
                gridPane.add(button, col, row);
            }
        }

        turnLabel = new Label();
        turnLabel.setFont(Font.font("Arial", 20));
        updateTurnLabel();

        BorderPane root = new BorderPane();
        root.setTop(turnLabel);
        BorderPane.setAlignment(turnLabel, Pos.CENTER);
        root.setCenter(gridPane);

        updateBoard(); // Initial rendering of the board

        Scene scene = new Scene(root, 700, 750);
        primaryStage.setTitle("Ethiopian Checkers (Dama)");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void updateTurnLabel() {
        // Always use the name from the current Player object
        String name = gameManager.getCurrentPlayer().getName();
        turnLabel.setText("Turn: " + name);
    }

    private void handleClick(int row, int col) {
        Player currentPlayer = gameManager.getCurrentPlayer();
        List<int[]> capturingPieces = gameManager.findCapturingPieces(currentPlayer == gameManager.getPlayer1());

        if (selectedRow == -1 && selectedCol == -1) {
            // First click: select a piece
            if (gameManager.isValidPiece(row, col, currentPlayer)) {
                if (!capturingPieces.isEmpty() && capturingPieces.stream().noneMatch(p -> p[0] == row && p[1] == col)) {
                    showAlert("Error", "You must select a piece that can capture.");
                } else if (gameManager.hasCaptureMoves(row, col) || gameManager.hasLegalMoves(row, col)) {
                    selectedRow = row;
                    selectedCol = col;
                } else {
                    showAlert("Error", "The selected piece has no legal moves. Choose another piece.");
                }
            } else {
                showAlert("Error", "Invalid piece selected! Choose your own piece.");
            }
        } else {
            // If the player clicks on another piece of their own, change the selection
            if (gameManager.isValidPiece(row, col, currentPlayer)) {
                if (!capturingPieces.isEmpty() && capturingPieces.stream().noneMatch(p -> p[0] == row && p[1] == col)) {
                    showAlert("Error", "You must select a piece that can capture.");
                } else if (gameManager.hasCaptureMoves(row, col) || gameManager.hasLegalMoves(row, col)) {
                    selectedRow = row;
                    selectedCol = col;
                } else {
                    showAlert("Error", "The selected piece has no legal moves. Choose another piece.");
                }
            } else {
                // Second click: attempt to move
                if (gameManager.isValidMove(selectedRow, selectedCol, row, col)) {
                    if (!capturingPieces.isEmpty() && Math.abs(row - selectedRow) != 2) {
                        showAlert("Error", "You must select a destination that captures a piece.");
                    } else {
                        boolean moveSuccessful = gameManager.makeMove(selectedRow, selectedCol, row, col);
                        if (moveSuccessful) {
                            updateBoard();
                            if (gameManager.isGameOver()) {
                                showAlert("Game Over", "Winner: " + gameManager.getWinner());
                                System.exit(0);
                            } else if (Math.abs(row - selectedRow) == 2 && gameManager.canContinueCapturing(row, col)) {
                                showAlert("Notice", "Another capture is possible. Continue capturing.");
                                selectedRow = row;
                                selectedCol = col;
                                return; // Allow the player to continue capturing
                            } else {
                                // Reset selection and switch turns only if no more captures are available
                                selectedRow = -1;
                                selectedCol = -1;
                                gameManager.switchTurn();
                                updateTurnLabel();
                            }
                        } else {
                            showAlert("Error", "Invalid move! Try again.");
                        }
                    }
                } else {
                    showAlert("Error", "Invalid move! Try again.");
                }
            }
        }
    }

    private void updateBoard() {
        char[][] board = gameManager.getBoardState();
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                char piece = board[row][col];
                Button button = buttons[row][col];
                button.setText("");
                button.setGraphic(null);
                if (piece == 'X' || piece == 'O' || piece == 'K' || piece == 'Q') {
                    Canvas canvas = new Canvas(60, 60);
                    GraphicsContext gc = canvas.getGraphicsContext2D();
                    // Draw colored circle
                    if (piece == 'X' || piece == 'Q') {
                        gc.setFill(Color.BLUE); // X and Q: blue
                    } else {
                        gc.setFill(Color.RED); // O and K: red
                    }
                    gc.fillOval(10, 10, 40, 40);
                    // Draw crown for kings
                    if (piece == 'K' || piece == 'Q') {
                        gc.setFill(Color.GOLD);
                        gc.setFont(Font.font("Arial", 22));
                        gc.fillText("\uD83D\uDC51", 18, 30); // Unicode crown emoji
                    }
                    button.setGraphic(canvas);
                }
            }
        }
        updateTurnLabel();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
