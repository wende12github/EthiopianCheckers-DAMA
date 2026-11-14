package com.dama.ethiopiancheckersgame.ui.gui;

import com.dama.ethiopiancheckersgame.core.model.PieceColor;
import com.dama.ethiopiancheckersgame.core.rules.DamaRuleStrategy;
import com.dama.ethiopiancheckersgame.core.rules.DamaVariant;
import com.dama.ethiopiancheckersgame.core.rules.EgregnaRules;
import com.dama.ethiopiancheckersgame.core.rules.TankegnaRules;
import com.dama.ethiopiancheckersgame.engine.GameMode;
import com.dama.ethiopiancheckersgame.engine.GameSession;
import com.dama.ethiopiancheckersgame.network.MatchmakingServer;
import com.dama.ethiopiancheckersgame.network.NetworkClient;
import com.dama.ethiopiancheckersgame.network.NetworkEventListener;
import com.dama.ethiopiancheckersgame.player.HumanPlayer;
import com.dama.ethiopiancheckersgame.player.NetworkPlayer;
import com.dama.ethiopiancheckersgame.player.Player;
import com.dama.ethiopiancheckersgame.player.ai.AIDifficulty;
import com.dama.ethiopiancheckersgame.player.ai.AIPlayer;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.util.function.Consumer;

/**
 * Culturally rich, onboarding startup menu for Ethiopian Checkers (Dama).
 * Features:
 * - Authentic Ethiopian Tibeb (ጥበብ) decorative textile border.
 * - Bilingual Amharic/English typography.
 * - Interactive Mode Cards: Solo vs AI, Pass & Play, and Online Multiplayer.
 * - Online Quick Match (public matchmaking queue) & Private 4-digit Room Codes.
 * - Built-in embedded server fallback.
 * - Visual Rule Variant Cards (Egregna vs Tankegna) and Theme presets.
 */
public class StartMenu extends VBox {
    private final Consumer<GameSession> onStartGame;

    // Selected state
    private GameMode selectedMode = GameMode.HUMAN_VS_AI;
    private DamaVariant selectedVariant = DamaVariant.TANKEGNA;
    private AIDifficulty selectedDifficulty = AIDifficulty.MEDIUM;

    // UI Controls
    private final TextField p1Field;
    private final TextField p2Field;
    private final Label statusBanner;
    private final VBox onlineMatchmakingBox;
    private Label onlineStatusLabel;
    private Button cancelSearchBtn;
    private Button fallbackAIBtn;

    // Network Client
    private NetworkClient networkClient;
    private String createdRoomCode = null;
    private Timeline searchingDotsAnimation;
    private int dotsCount = 0;

    public StartMenu(Consumer<GameSession> onStartGame) {
        this.onStartGame = onStartGame;

        setAlignment(Pos.TOP_CENTER);
        setSpacing(12);
        setStyle("-fx-background-color: #121212;");

        // 1. Top Ethiopian Tibeb (ጥበብ) Decorative Geometric Banner
        TibebBanner tibebTop = new TibebBanner(920, 24);

        // 2. Header Section
        VBox headerBox = new VBox(2);
        headerBox.setAlignment(Pos.CENTER);
        headerBox.setPadding(new Insets(10, 0, 4, 0));

        Label amharicTitle = new Label("የኢትዮጵያ ዳማ");
        amharicTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 34));
        amharicTitle.setTextFill(Color.web("#FFC107")); // Imperial Gold
        amharicTitle.setStyle("-fx-effect: dropshadow(three-pass-box, rgba(255,193,7,0.35), 14, 0, 0, 2);");

        Label englishTitle = new Label("ETHIOPIAN CHECKERS • TOURNAMENT EDITION");
        englishTitle.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 12));
        englishTitle.setTextFill(Color.web("#B0BEC5"));

        headerBox.getChildren().addAll(amharicTitle, englishTitle);

        // Scrollable Central Content Card
        VBox contentBox = new VBox(14);
        contentBox.setAlignment(Pos.CENTER);
        contentBox.setMaxWidth(800);
        contentBox.setPadding(new Insets(12, 24, 16, 24));
        contentBox.setStyle(
                "-fx-background-color: #1E1E1E; " +
                "-fx-background-radius: 12px; " +
                "-fx-border-color: #37474F; " +
                "-fx-border-width: 1px; " +
                "-fx-border-radius: 12px; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.6), 18, 0, 0, 6);"
        );

        // 3. Rule Variant Selector (Egregna vs Tankegna)
        Label variantHeading = createSectionHeader("1. ደንብ ይምረጡ • Select Rule Variant");
        HBox variantCards = createVariantCards();

        // 4. Game Mode Selector (Solo vs AI, Pass & Play, Online)
        Label modeHeading = createSectionHeader("2. የጨዋታ አይነት • Select Game Mode");
        HBox modeTabs = createModeTabs();

        // 5. Dynamic Mode Options Container
        VBox dynamicOptionsBox = new VBox(10);
        dynamicOptionsBox.setAlignment(Pos.CENTER);

        // Player Name Fields
        HBox namesBox = new HBox(16);
        namesBox.setAlignment(Pos.CENTER);

        p1Field = createTextField("Player 1");
        p2Field = createTextField("Computer");
        p2Field.setDisable(true);

        VBox p1Box = new VBox(4, createSubLabel("ቀይ (Red) Player 1:"), p1Field);
        VBox p2Box = new VBox(4, createSubLabel("ጥቁር (Black) Player 2:"), p2Field);
        namesBox.getChildren().addAll(p1Box, p2Box);

        // AI Difficulty Chips
        HBox diffBox = createDifficultyChips();

        // Online Matchmaking UI (Hidden by default until Online Mode is selected)
        onlineMatchmakingBox = createOnlineMatchmakingCard();
        onlineMatchmakingBox.setVisible(false);
        onlineMatchmakingBox.setManaged(false);

        // 6. Theme Quick Bar
        HBox themeBar = createThemeQuickBar();

        // 7. Status Banner
        statusBanner = new Label();
        statusBanner.setFont(Font.font("Segoe UI", 12));
        statusBanner.setTextFill(Color.web("#81C784"));
        statusBanner.setVisible(false);

        // 8. Start Button
        Button startButton = new Button("▶  ጀምር • START MATCH");
        startButton.setFont(Font.font("Segoe UI", FontWeight.BOLD, 15));
        startButton.setTextFill(Color.WHITE);
        startButton.setPrefWidth(280);
        startButton.setPadding(new Insets(12, 28, 12, 28));
        startButton.setStyle(
                "-fx-background-color: linear-gradient(to right, #2E7D32, #1B5E20); " +
                "-fx-background-radius: 8px; " +
                "-fx-border-color: #4CAF50; " +
                "-fx-border-radius: 8px; " +
                "-fx-cursor: hand; " +
                "-fx-font-weight: bold;"
        );
        startButton.setOnMouseEntered(e -> startButton.setStyle(
                "-fx-background-color: linear-gradient(to right, #388E3C, #2E7D32); " +
                "-fx-background-radius: 8px; -fx-border-color: #81C784; -fx-border-radius: 8px; -fx-cursor: hand;"
        ));
        startButton.setOnMouseExited(e -> startButton.setStyle(
                "-fx-background-color: linear-gradient(to right, #2E7D32, #1B5E20); " +
                "-fx-background-radius: 8px; -fx-border-color: #4CAF50; -fx-border-radius: 8px; -fx-cursor: hand;"
        ));

        startButton.setOnAction(e -> handleStartAction());

        // Assembly
        dynamicOptionsBox.getChildren().addAll(namesBox, diffBox, onlineMatchmakingBox);

        contentBox.getChildren().addAll(
                variantHeading, variantCards,
                modeHeading, modeTabs,
                dynamicOptionsBox,
                themeBar,
                statusBanner,
                startButton
        );

        // Bottom Tibeb border
        TibebBanner tibebBottom = new TibebBanner(920, 16);

        getChildren().addAll(tibebTop, headerBox, contentBox, tibebBottom);

        // Initialize UI view states
        updateModeView();
    }

    // --- Component Builders ---

    private Label createSectionHeader(String title) {
        Label lbl = new Label(title);
        lbl.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        lbl.setTextFill(Color.web("#FFC107"));
        return lbl;
    }

    private Label createSubLabel(String title) {
        Label lbl = new Label(title);
        lbl.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 11));
        lbl.setTextFill(Color.web("#B0BEC5"));
        return lbl;
    }

    private HBox createVariantCards() {
        HBox box = new HBox(16);
        box.setAlignment(Pos.CENTER);

        VBox egregnaCard = createCard(
                "🛡️ እግረኛ (Egregna)",
                "Foot Soldier Mode\n• Regular: Move & Capture forward only\n• King: 1-step Short King in all 4 diagonals",
                selectedVariant == DamaVariant.EGREGNA
        );

        VBox tankegnaCard = createCard(
                "⚡ ታንከኛ (Tankegna)",
                "Armored Tank Mode\n• Regular: 4-Way multi-directional jump\n• King: Long-range Flying King (Bishop slide)",
                selectedVariant == DamaVariant.TANKEGNA
        );

        egregnaCard.setOnMouseClicked(e -> {
            selectedVariant = DamaVariant.EGREGNA;
            updateCardSelection(egregnaCard, true);
            updateCardSelection(tankegnaCard, false);
        });

        tankegnaCard.setOnMouseClicked(e -> {
            selectedVariant = DamaVariant.TANKEGNA;
            updateCardSelection(tankegnaCard, true);
            updateCardSelection(egregnaCard, false);
        });

        box.getChildren().addAll(egregnaCard, tankegnaCard);
        return box;
    }

    private HBox createModeTabs() {
        HBox box = new HBox(12);
        box.setAlignment(Pos.CENTER);

        Button aiBtn = createModeButton("🤖 ነጠላ ተጫዋች (Solo vs AI)", selectedMode == GameMode.HUMAN_VS_AI);
        Button passBtn = createModeButton("👥 ሁለት ተጫዋች (Pass & Play)", selectedMode == GameMode.HUMAN_VS_HUMAN);
        Button onlineBtn = createModeButton("🌐 የመስመር ላይ (Online)", selectedMode == GameMode.ONLINE_MULTIPLAYER);

        aiBtn.setOnAction(e -> {
            selectedMode = GameMode.HUMAN_VS_AI;
            setButtonActive(aiBtn, true);
            setButtonActive(passBtn, false);
            setButtonActive(onlineBtn, false);
            updateModeView();
        });

        passBtn.setOnAction(e -> {
            selectedMode = GameMode.HUMAN_VS_HUMAN;
            setButtonActive(aiBtn, false);
            setButtonActive(passBtn, true);
            setButtonActive(onlineBtn, false);
            updateModeView();
        });

        onlineBtn.setOnAction(e -> {
            selectedMode = GameMode.ONLINE_MULTIPLAYER;
            setButtonActive(aiBtn, false);
            setButtonActive(passBtn, false);
            setButtonActive(onlineBtn, true);
            updateModeView();
        });

        box.getChildren().addAll(aiBtn, passBtn, onlineBtn);
        return box;
    }

    private HBox createDifficultyChips() {
        HBox box = new HBox(10);
        box.setAlignment(Pos.CENTER);

        Label label = createSubLabel("AI ደረጃ (Difficulty):");

        Button easy = createChip("ቀላል (Easy)", selectedDifficulty == AIDifficulty.EASY);
        Button med = createChip("መካከለኛ (Medium)", selectedDifficulty == AIDifficulty.MEDIUM);
        Button hard = createChip("ከባድ (Hard)", selectedDifficulty == AIDifficulty.HARD);

        easy.setOnAction(e -> {
            selectedDifficulty = AIDifficulty.EASY;
            setChipActive(easy, true); setChipActive(med, false); setChipActive(hard, false);
        });
        med.setOnAction(e -> {
            selectedDifficulty = AIDifficulty.MEDIUM;
            setChipActive(easy, false); setChipActive(med, true); setChipActive(hard, false);
        });
        hard.setOnAction(e -> {
            selectedDifficulty = AIDifficulty.HARD;
            setChipActive(easy, false); setChipActive(med, false); setChipActive(hard, true);
        });

        box.getChildren().addAll(label, easy, med, hard);
        return box;
    }

    private VBox createOnlineMatchmakingCard() {
        VBox card = new VBox(10);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(12));
        card.setStyle(
                "-fx-background-color: #262626; -fx-background-radius: 8px; " +
                "-fx-border-color: #0288D1; -fx-border-width: 1.5px; -fx-border-radius: 8px;"
        );

        Label title = new Label("🌐 የኢንተርኔት ጨዋታ • Online Matchmaking");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        title.setTextFill(Color.web("#29B6F6"));

        // Matchmaking Sub-modes: Quick Match vs Private Room
        HBox matchOptions = new HBox(12);
        matchOptions.setAlignment(Pos.CENTER);

        Button quickMatchBtn = new Button("⚡ ፈጣን ግጥሚያ • Quick Match");
        quickMatchBtn.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        quickMatchBtn.setTextFill(Color.WHITE);
        quickMatchBtn.setStyle("-fx-background-color: #0288D1; -fx-background-radius: 6px; -fx-cursor: hand;");

        Button createRoomBtn = new Button("🔒 ክፍል ፍጠር • Create Room");
        createRoomBtn.setFont(Font.font("Segoe UI", 12));
        createRoomBtn.setTextFill(Color.WHITE);
        createRoomBtn.setStyle("-fx-background-color: #455A64; -fx-background-radius: 6px; -fx-cursor: hand;");

        TextField joinCodeField = new TextField();
        joinCodeField.setPromptText("Code (e.g. 4821)");
        joinCodeField.setPrefWidth(120);
        joinCodeField.setStyle("-fx-background-color: #37474F; -fx-text-fill: white; -fx-alignment: center;");

        Button joinRoomBtn = new Button("ግባ • Join");
        joinRoomBtn.setFont(Font.font("Segoe UI", 12));
        joinRoomBtn.setTextFill(Color.WHITE);
        joinRoomBtn.setStyle("-fx-background-color: #2E7D32; -fx-background-radius: 6px; -fx-cursor: hand;");

        matchOptions.getChildren().addAll(quickMatchBtn, createRoomBtn, joinCodeField, joinRoomBtn);

        // Searching / Waiting Status box
        VBox statusBox = new VBox(6);
        statusBox.setAlignment(Pos.CENTER);

        onlineStatusLabel = new Label("Select Quick Match or enter a room code.");
        onlineStatusLabel.setFont(Font.font("Segoe UI", 12));
        onlineStatusLabel.setTextFill(Color.web("#B0BEC5"));

        HBox actionBox = new HBox(10);
        actionBox.setAlignment(Pos.CENTER);

        cancelSearchBtn = new Button("✕ ሰርዝ • Cancel");
        cancelSearchBtn.setFont(Font.font("Segoe UI", 11));
        cancelSearchBtn.setTextFill(Color.web("#EF5350"));
        cancelSearchBtn.setStyle("-fx-background-color: #37474F; -fx-background-radius: 4px; -fx-cursor: hand;");
        cancelSearchBtn.setVisible(false);

        fallbackAIBtn = new Button("🤖 ከኮምፒውተር ጋር ተጫወት • Play AI");
        fallbackAIBtn.setFont(Font.font("Segoe UI", 11));
        fallbackAIBtn.setTextFill(Color.web("#FFC107"));
        fallbackAIBtn.setStyle("-fx-background-color: #37474F; -fx-background-radius: 4px; -fx-cursor: hand;");
        fallbackAIBtn.setVisible(false);

        actionBox.getChildren().addAll(cancelSearchBtn, fallbackAIBtn);
        statusBox.getChildren().addAll(onlineStatusLabel, actionBox);

        // Action Handlers
        quickMatchBtn.setOnAction(e -> startQuickMatch());
        createRoomBtn.setOnAction(e -> startCreateRoom());
        joinRoomBtn.setOnAction(e -> startJoinRoom(joinCodeField.getText().trim()));
        cancelSearchBtn.setOnAction(e -> cancelOnlineSearch());
        fallbackAIBtn.setOnAction(e -> {
            cancelOnlineSearch();
            selectedMode = GameMode.HUMAN_VS_AI;
            updateModeView();
            handleStartAction();
        });

        card.getChildren().addAll(title, matchOptions, statusBox);
        return card;
    }

    private HBox createThemeQuickBar() {
        HBox box = new HBox(10);
        box.setAlignment(Pos.CENTER);

        Label label = createSubLabel("መልክ • Theme:");

        ComboBox<Theme> themeCombo = new ComboBox<>();
        themeCombo.getItems().addAll(Theme.getPresets());
        themeCombo.setValue(Theme.getCurrent());
        themeCombo.setPrefWidth(200);
        themeCombo.setStyle("-fx-font-size: 11px;");
        themeCombo.setOnAction(e -> {
            if (themeCombo.getValue() != null) {
                Theme.setCurrent(themeCombo.getValue());
            }
        });

        Button customizeBtn = new Button("🎨 Customize...");
        customizeBtn.setFont(Font.font("Segoe UI", 11));
        customizeBtn.setTextFill(Color.WHITE);
        customizeBtn.setStyle("-fx-background-color: #37474F; -fx-background-radius: 4px; -fx-cursor: hand;");
        customizeBtn.setOnAction(e -> ThemeCustomizerDialog.show((Stage) getScene().getWindow()));

        box.getChildren().addAll(label, themeCombo, customizeBtn);
        return box;
    }

    // --- Styling Helpers ---

    private VBox createCard(String title, String desc, boolean active) {
        VBox card = new VBox(4);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPrefWidth(350);
        card.setPadding(new Insets(10, 14, 10, 14));
        card.setCursor(javafx.scene.Cursor.HAND);

        Label titleLbl = new Label(title);
        titleLbl.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        titleLbl.setTextFill(Color.web("#FFFFFF"));

        Label descLbl = new Label(desc);
        descLbl.setFont(Font.font("Segoe UI", 11));
        descLbl.setTextFill(Color.web("#90A4AE"));
        descLbl.setWrapText(true);

        card.getChildren().addAll(titleLbl, descLbl);
        updateCardSelection(card, active);
        return card;
    }

    private void updateCardSelection(VBox card, boolean active) {
        if (active) {
            card.setStyle(
                    "-fx-background-color: #2E3B2E; " +
                    "-fx-border-color: #FFC107; -fx-border-width: 2px; -fx-border-radius: 8px; " +
                    "-fx-background-radius: 8px;"
            );
        } else {
            card.setStyle(
                    "-fx-background-color: #262626; " +
                    "-fx-border-color: #424242; -fx-border-width: 1px; -fx-border-radius: 8px; " +
                    "-fx-background-radius: 8px;"
            );
        }
    }

    private Button createModeButton(String text, boolean active) {
        Button btn = new Button(text);
        btn.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        btn.setPrefWidth(220);
        btn.setPadding(new Insets(8, 12, 8, 12));
        btn.setCursor(javafx.scene.Cursor.HAND);
        setButtonActive(btn, active);
        return btn;
    }

    private void setButtonActive(Button btn, boolean active) {
        if (active) {
            btn.setTextFill(Color.WHITE);
            btn.setStyle("-fx-background-color: #0288D1; -fx-background-radius: 6px; -fx-border-color: #40C4FF; -fx-border-radius: 6px;");
        } else {
            btn.setTextFill(Color.web("#B0BEC5"));
            btn.setStyle("-fx-background-color: #262626; -fx-background-radius: 6px; -fx-border-color: #424242; -fx-border-radius: 6px;");
        }
    }

    private Button createChip(String text, boolean active) {
        Button btn = new Button(text);
        btn.setFont(Font.font("Segoe UI", 11));
        btn.setPadding(new Insets(4, 10, 4, 10));
        btn.setCursor(javafx.scene.Cursor.HAND);
        setChipActive(btn, active);
        return btn;
    }

    private void setChipActive(Button btn, boolean active) {
        if (active) {
            btn.setTextFill(Color.BLACK);
            btn.setStyle("-fx-background-color: #FFC107; -fx-background-radius: 12px; -fx-font-weight: bold;");
        } else {
            btn.setTextFill(Color.web("#CFD8DC"));
            btn.setStyle("-fx-background-color: #37474F; -fx-background-radius: 12px;");
        }
    }

    private TextField createTextField(String defaultText) {
        TextField tf = new TextField(defaultText);
        tf.setFont(Font.font("Segoe UI", 12));
        tf.setPrefWidth(200);
        tf.setStyle("-fx-background-color: #2E2E2E; -fx-text-fill: #FFFFFF; -fx-border-color: #455A64; -fx-border-radius: 4px;");
        return tf;
    }

    private void updateModeView() {
        boolean isAI = (selectedMode == GameMode.HUMAN_VS_AI);
        boolean isOnline = (selectedMode == GameMode.ONLINE_MULTIPLAYER);

        onlineMatchmakingBox.setVisible(isOnline);
        onlineMatchmakingBox.setManaged(isOnline);

        if (isAI) {
            p2Field.setText("Computer");
            p2Field.setDisable(true);
        } else if (isOnline) {
            p2Field.setText("Online Opponent");
            p2Field.setDisable(true);
        } else {
            p2Field.setText("Player 2");
            p2Field.setDisable(false);
        }
    }

    // --- Online Matchmaking Logic ---

    private void ensureNetworkConnected(Runnable onSuccess) {
        if (networkClient != null && networkClient.isConnected()) {
            onSuccess.run();
            return;
        }

        // Try connecting to default server; if offline, auto-start embedded server!
        new Thread(() -> {
            try {
                networkClient = new NetworkClient("127.0.0.1", 8989);
                setupClientListeners();
                networkClient.connect(p1Field.getText().trim());
                Platform.runLater(onSuccess);
            } catch (IOException ex) {
                // Auto-start embedded server for seamless local testing
                System.out.println("Starting embedded relay server on port 8989...");
                MatchmakingServer.startEmbedded(8989);
                try {
                    Thread.sleep(150);
                    networkClient = new NetworkClient("127.0.0.1", 8989);
                    setupClientListeners();
                    networkClient.connect(p1Field.getText().trim());
                    Platform.runLater(onSuccess);
                } catch (Exception e) {
                    Platform.runLater(() -> {
                        onlineStatusLabel.setTextFill(Color.web("#EF5350"));
                        onlineStatusLabel.setText("Failed to connect to relay server: " + e.getMessage());
                    });
                }
            }
        }).start();
    }

    private void setupClientListeners() {
        networkClient.setListener(new NetworkEventListener() {
            @Override
            public void onConnected() {
                Platform.runLater(() -> onlineStatusLabel.setText("Connected to Matchmaking Server."));
            }

            @Override
            public void onWaitingInQueue() {
                Platform.runLater(() -> {
                    startSearchingAnimation("ተጫዋች በመፈለግ ላይ • Searching for opponent");
                    cancelSearchBtn.setVisible(true);
                    fallbackAIBtn.setVisible(true);
                });
            }

            @Override
            public void onRoomCreated(String roomCode) {
                Platform.runLater(() -> {
                    createdRoomCode = roomCode;
                    startSearchingAnimation("ክፍል #" + roomCode + " ተፈጥሯል • Waiting for friend");
                    cancelSearchBtn.setVisible(true);
                });
            }

            @Override
            public void onMatchStarted(PieceColor yourColor, String opponentName, DamaVariant variant) {
                Platform.runLater(() -> {
                    stopSearchingAnimation();
                    startOnlineSession(yourColor, opponentName, variant);
                });
            }

            @Override
            public void onOpponentMove(com.dama.ethiopiancheckersgame.core.model.Move move) {
                // Handled in NetworkPlayer during gameplay
            }

            @Override
            public void onOpponentDrawOffer() {}

            @Override
            public void onDrawAccepted() {}

            @Override
            public void onDrawDeclined() {}

            @Override
            public void onOpponentResigned() {}

            @Override
            public void onOpponentDisconnected() {
                Platform.runLater(() -> {
                    stopSearchingAnimation();
                    onlineStatusLabel.setTextFill(Color.web("#EF5350"));
                    onlineStatusLabel.setText("Opponent disconnected.");
                });
            }

            @Override
            public void onError(String errorMessage) {
                Platform.runLater(() -> {
                    stopSearchingAnimation();
                    onlineStatusLabel.setTextFill(Color.web("#EF5350"));
                    onlineStatusLabel.setText("Error: " + errorMessage);
                });
            }
        });
    }

    private void startQuickMatch() {
        ensureNetworkConnected(() -> {
            onlineStatusLabel.setTextFill(Color.web("#29B6F6"));
            onlineStatusLabel.setText("Joining public matchmaking queue...");
            networkClient.findQuickMatch(selectedVariant);
        });
    }

    private void startCreateRoom() {
        ensureNetworkConnected(() -> {
            onlineStatusLabel.setTextFill(Color.web("#29B6F6"));
            onlineStatusLabel.setText("Creating private room...");
            networkClient.createRoom(selectedVariant);
        });
    }

    private void startJoinRoom(String code) {
        if (code.isEmpty()) {
            onlineStatusLabel.setTextFill(Color.web("#EF5350"));
            onlineStatusLabel.setText("Please enter a 4-digit room code.");
            return;
        }
        ensureNetworkConnected(() -> {
            onlineStatusLabel.setTextFill(Color.web("#29B6F6"));
            onlineStatusLabel.setText("Joining room #" + code + "...");
            networkClient.joinRoom(code);
        });
    }

    private void cancelOnlineSearch() {
        if (networkClient != null) {
            networkClient.cancelQueue();
        }
        stopSearchingAnimation();
        onlineStatusLabel.setTextFill(Color.web("#B0BEC5"));
        onlineStatusLabel.setText("Matchmaking cancelled.");
        cancelSearchBtn.setVisible(false);
        fallbackAIBtn.setVisible(false);
    }

    private void startSearchingAnimation(String prefix) {
        stopSearchingAnimation();
        dotsCount = 0;
        searchingDotsAnimation = new Timeline(new KeyFrame(Duration.millis(450), e -> {
            dotsCount = (dotsCount + 1) % 4;
            String dots = ".".repeat(dotsCount);
            onlineStatusLabel.setText(prefix + dots);
        }));
        searchingDotsAnimation.setCycleCount(Animation.INDEFINITE);
        searchingDotsAnimation.play();
    }

    private void stopSearchingAnimation() {
        if (searchingDotsAnimation != null) {
            searchingDotsAnimation.stop();
            searchingDotsAnimation = null;
        }
    }

    private void startOnlineSession(PieceColor yourColor, String opponentName, DamaVariant variant) {
        DamaRuleStrategy rules = (variant == DamaVariant.EGREGNA) ? new EgregnaRules() : new TankegnaRules();
        String localName = p1Field.getText().trim();
        if (localName.isEmpty()) localName = "Player";

        Player player1;
        Player player2;

        if (yourColor == PieceColor.RED) {
            player1 = new HumanPlayer(localName, PieceColor.RED);
            player2 = new NetworkPlayer(opponentName, PieceColor.BLACK, networkClient);
        } else {
            player1 = new NetworkPlayer(opponentName, PieceColor.RED, networkClient);
            player2 = new HumanPlayer(localName, PieceColor.BLACK);
        }

        GameSession session = new GameSession(player1, player2, GameMode.ONLINE_MULTIPLAYER, rules);
        onStartGame.accept(session);
    }

    private void handleStartAction() {
        if (selectedMode == GameMode.ONLINE_MULTIPLAYER) {
            startQuickMatch();
            return;
        }

        String p1Name = p1Field.getText().trim();
        if (p1Name.isEmpty()) p1Name = "Player 1";

        DamaRuleStrategy rules = (selectedVariant == DamaVariant.EGREGNA) ? new EgregnaRules() : new TankegnaRules();
        Player player1 = new HumanPlayer(p1Name, PieceColor.RED);
        Player player2;

        if (selectedMode == GameMode.HUMAN_VS_AI) {
            player2 = new AIPlayer("Computer", PieceColor.BLACK, selectedDifficulty, rules);
        } else {
            String p2Name = p2Field.getText().trim();
            if (p2Name.isEmpty()) p2Name = "Player 2";
            player2 = new HumanPlayer(p2Name, PieceColor.BLACK);
        }

        GameSession session = new GameSession(player1, player2, selectedMode, rules);
        onStartGame.accept(session);
    }
}
