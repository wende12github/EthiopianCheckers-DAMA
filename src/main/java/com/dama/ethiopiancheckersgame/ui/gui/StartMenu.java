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
import com.dama.ethiopiancheckersgame.ui.i18n.I18n;
import com.dama.ethiopiancheckersgame.ui.i18n.Language;
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
 * Onboarding startup menu with strict Language Toggle separation (Amharic vs English).
 * Features:
 * - Language Toggle Pill Bar (አማርኛ | English)
 * - Authentic Ethiopian Tibeb (ጥበብ) decorative geometric textile banner.
 * - Interactive Mode Cards: Solo vs AI, Pass & Play, and Online Multiplayer.
 * - Online Quick Match & Private 4-digit Room Codes.
 * - Built-in embedded server fallback.
 */
public class StartMenu extends VBox {
    private final Consumer<GameSession> onStartGame;

    // Selected state
    private GameMode selectedMode = GameMode.HUMAN_VS_AI;
    private DamaVariant selectedVariant = DamaVariant.TANKEGNA;
    private AIDifficulty selectedDifficulty = AIDifficulty.MEDIUM;

    // Dynamic UI Controls for i18n
    private final Label mainTitle;
    private final Label subtitle;
    private final Label variantSectionHeader;
    private final Label egregnaTitle;
    private final Label egregnaDesc;
    private final Label tankegnaTitle;
    private final Label tankegnaDesc;
    private final Label modeSectionHeader;
    private final Button aiModeBtn;
    private final Button passModeBtn;
    private final Button onlineModeBtn;
    private final Label diffSectionLabel;
    private final Button easyChip;
    private final Button medChip;
    private final Button hardChip;
    private final Label p1Label;
    private final Label p2Label;
    private final TextField p1Field;
    private final TextField p2Field;
    private final Label onlineTitle;
    private final Button quickMatchBtn;
    private final Button createRoomBtn;
    private final TextField joinCodeField;
    private final Button joinRoomBtn;
    private final Label onlineStatusLabel;
    private final Button cancelSearchBtn;
    private final Button fallbackAIBtn;
    private final VBox onlineMatchmakingBox;
    private final Label themeLabel;
    private final Button customizeBtn;
    private final Button startButton;

    // Network Client
    private NetworkClient networkClient;
    private String createdRoomCode = null;
    private Timeline searchingDotsAnimation;
    private int dotsCount = 0;

    public StartMenu(Consumer<GameSession> onStartGame) {
        this.onStartGame = onStartGame;

        setAlignment(Pos.TOP_CENTER);
        setSpacing(10);
        setStyle("-fx-background-color: #121212;");

        // 1. Top Ethiopian Tibeb (ጥበብ) Decorative Geometric Banner
        TibebBanner tibebTop = new TibebBanner(920, 24);

        // 2. Header Bar: Title on center, Language Toggle on top-right
        HBox topNavBar = new HBox(16);
        topNavBar.setAlignment(Pos.CENTER);
        topNavBar.setPadding(new Insets(6, 24, 0, 24));

        VBox titleBox = new VBox(2);
        titleBox.setAlignment(Pos.CENTER);

        mainTitle = new Label();
        mainTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 32));
        mainTitle.setTextFill(Color.web("#FFC107"));
        mainTitle.setStyle("-fx-effect: dropshadow(three-pass-box, rgba(255,193,7,0.35), 14, 0, 0, 2);");

        subtitle = new Label();
        subtitle.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 12));
        subtitle.setTextFill(Color.web("#B0BEC5"));

        titleBox.getChildren().addAll(mainTitle, subtitle);

        Region leftSpacer = new Region();
        Region rightSpacer = new Region();
        HBox.setHgrow(leftSpacer, Priority.ALWAYS);
        HBox.setHgrow(rightSpacer, Priority.ALWAYS);

        LanguageToggleBar langToggle = new LanguageToggleBar();

        // Balanced header with spacers so title is centered and toggle sits on the right
        topNavBar.getChildren().addAll(leftSpacer, titleBox, rightSpacer, langToggle);

        // 3. Central Content Card
        VBox contentBox = new VBox(12);
        contentBox.setAlignment(Pos.CENTER);
        contentBox.setMaxWidth(800);
        contentBox.setPadding(new Insets(10, 24, 14, 24));
        contentBox.setStyle(
                "-fx-background-color: #1E1E1E; " +
                "-fx-background-radius: 12px; " +
                "-fx-border-color: #37474F; " +
                "-fx-border-width: 1px; " +
                "-fx-border-radius: 12px; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.6), 18, 0, 0, 6);"
        );

        // 4. Rule Variant Selector (Egregna vs Tankegna)
        variantSectionHeader = createSectionHeader("");
        egregnaTitle = new Label();
        egregnaTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        egregnaTitle.setTextFill(Color.web("#FFFFFF"));

        egregnaDesc = new Label();
        egregnaDesc.setFont(Font.font("Segoe UI", 11));
        egregnaDesc.setTextFill(Color.web("#90A4AE"));
        egregnaDesc.setWrapText(true);

        tankegnaTitle = new Label();
        tankegnaTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        tankegnaTitle.setTextFill(Color.web("#FFFFFF"));

        tankegnaDesc = new Label();
        tankegnaDesc.setFont(Font.font("Segoe UI", 11));
        tankegnaDesc.setTextFill(Color.web("#90A4AE"));
        tankegnaDesc.setWrapText(true);

        HBox variantCards = createVariantCards();

        // 5. Game Mode Selector
        modeSectionHeader = createSectionHeader("");
        aiModeBtn = createModeButton("", selectedMode == GameMode.HUMAN_VS_AI);
        passModeBtn = createModeButton("", selectedMode == GameMode.HUMAN_VS_HUMAN);
        onlineModeBtn = createModeButton("", selectedMode == GameMode.ONLINE_MULTIPLAYER);

        HBox modeTabs = new HBox(12, aiModeBtn, passModeBtn, onlineModeBtn);
        modeTabs.setAlignment(Pos.CENTER);

        aiModeBtn.setOnAction(e -> {
            selectedMode = GameMode.HUMAN_VS_AI;
            setButtonActive(aiModeBtn, true); setButtonActive(passModeBtn, false); setButtonActive(onlineModeBtn, false);
            updateModeView();
        });
        passModeBtn.setOnAction(e -> {
            selectedMode = GameMode.HUMAN_VS_HUMAN;
            setButtonActive(aiModeBtn, false); setButtonActive(passModeBtn, true); setButtonActive(onlineModeBtn, false);
            updateModeView();
        });
        onlineModeBtn.setOnAction(e -> {
            selectedMode = GameMode.ONLINE_MULTIPLAYER;
            setButtonActive(aiModeBtn, false); setButtonActive(passModeBtn, false); setButtonActive(onlineModeBtn, true);
            updateModeView();
        });

        // 6. Player Names & AI Difficulty
        p1Label = createSubLabel("");
        p1Field = createTextField("Player 1");

        p2Label = createSubLabel("");
        p2Field = createTextField("Computer");
        p2Field.setDisable(true);

        HBox namesBox = new HBox(16);
        namesBox.setAlignment(Pos.CENTER);
        namesBox.getChildren().addAll(new VBox(4, p1Label, p1Field), new VBox(4, p2Label, p2Field));

        diffSectionLabel = createSubLabel("");
        easyChip = createChip("", selectedDifficulty == AIDifficulty.EASY);
        medChip = createChip("", selectedDifficulty == AIDifficulty.MEDIUM);
        hardChip = createChip("", selectedDifficulty == AIDifficulty.HARD);

        easyChip.setOnAction(e -> {
            selectedDifficulty = AIDifficulty.EASY;
            setChipActive(easyChip, true); setChipActive(medChip, false); setChipActive(hardChip, false);
        });
        medChip.setOnAction(e -> {
            selectedDifficulty = AIDifficulty.MEDIUM;
            setChipActive(easyChip, false); setChipActive(medChip, true); setChipActive(hardChip, false);
        });
        hardChip.setOnAction(e -> {
            selectedDifficulty = AIDifficulty.HARD;
            setChipActive(easyChip, false); setChipActive(medChip, false); setChipActive(hardChip, true);
        });

        HBox diffBox = new HBox(10, diffSectionLabel, easyChip, medChip, hardChip);
        diffBox.setAlignment(Pos.CENTER);

        // 7. Online Matchmaking Box
        onlineTitle = new Label();
        onlineTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        onlineTitle.setTextFill(Color.web("#29B6F6"));

        quickMatchBtn = new Button();
        quickMatchBtn.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        quickMatchBtn.setTextFill(Color.WHITE);
        quickMatchBtn.setStyle("-fx-background-color: #0288D1; -fx-background-radius: 6px; -fx-cursor: hand;");

        createRoomBtn = new Button();
        createRoomBtn.setFont(Font.font("Segoe UI", 12));
        createRoomBtn.setTextFill(Color.WHITE);
        createRoomBtn.setStyle("-fx-background-color: #455A64; -fx-background-radius: 6px; -fx-cursor: hand;");

        joinCodeField = new TextField();
        joinCodeField.setPrefWidth(120);
        joinCodeField.setStyle("-fx-background-color: #37474F; -fx-text-fill: white; -fx-alignment: center;");

        joinRoomBtn = new Button();
        joinRoomBtn.setFont(Font.font("Segoe UI", 12));
        joinRoomBtn.setTextFill(Color.WHITE);
        joinRoomBtn.setStyle("-fx-background-color: #2E7D32; -fx-background-radius: 6px; -fx-cursor: hand;");

        onlineStatusLabel = new Label();
        onlineStatusLabel.setFont(Font.font("Segoe UI", 12));
        onlineStatusLabel.setTextFill(Color.web("#B0BEC5"));

        cancelSearchBtn = new Button();
        cancelSearchBtn.setFont(Font.font("Segoe UI", 11));
        cancelSearchBtn.setTextFill(Color.web("#EF5350"));
        cancelSearchBtn.setStyle("-fx-background-color: #37474F; -fx-background-radius: 4px; -fx-cursor: hand;");
        cancelSearchBtn.setVisible(false);

        fallbackAIBtn = new Button();
        fallbackAIBtn.setFont(Font.font("Segoe UI", 11));
        fallbackAIBtn.setTextFill(Color.web("#FFC107"));
        fallbackAIBtn.setStyle("-fx-background-color: #37474F; -fx-background-radius: 4px; -fx-cursor: hand;");
        fallbackAIBtn.setVisible(false);

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

        HBox onlineButtons = new HBox(12, quickMatchBtn, createRoomBtn, joinCodeField, joinRoomBtn);
        onlineButtons.setAlignment(Pos.CENTER);

        HBox onlineActions = new HBox(10, cancelSearchBtn, fallbackAIBtn);
        onlineActions.setAlignment(Pos.CENTER);

        onlineMatchmakingBox = new VBox(8, onlineTitle, onlineButtons, onlineStatusLabel, onlineActions);
        onlineMatchmakingBox.setAlignment(Pos.CENTER);
        onlineMatchmakingBox.setPadding(new Insets(10));
        onlineMatchmakingBox.setStyle(
                "-fx-background-color: #262626; -fx-background-radius: 8px; " +
                "-fx-border-color: #0288D1; -fx-border-width: 1.5px; -fx-border-radius: 8px;"
        );
        onlineMatchmakingBox.setVisible(false);
        onlineMatchmakingBox.setManaged(false);

        VBox dynamicOptionsBox = new VBox(10, namesBox, diffBox, onlineMatchmakingBox);
        dynamicOptionsBox.setAlignment(Pos.CENTER);

        // 8. Theme Quick Bar
        themeLabel = createSubLabel("");
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

        customizeBtn = new Button();
        customizeBtn.setFont(Font.font("Segoe UI", 11));
        customizeBtn.setTextFill(Color.WHITE);
        customizeBtn.setStyle("-fx-background-color: #37474F; -fx-background-radius: 4px; -fx-cursor: hand;");
        customizeBtn.setOnAction(e -> ThemeCustomizerDialog.show((Stage) getScene().getWindow()));

        HBox themeBar = new HBox(10, themeLabel, themeCombo, customizeBtn);
        themeBar.setAlignment(Pos.CENTER);

        // 9. Start Button
        startButton = new Button();
        startButton.setFont(Font.font("Segoe UI", FontWeight.BOLD, 15));
        startButton.setTextFill(Color.WHITE);
        startButton.setPrefWidth(280);
        startButton.setPadding(new Insets(10, 28, 10, 28));
        startButton.setStyle(
                "-fx-background-color: linear-gradient(to right, #2E7D32, #1B5E20); " +
                "-fx-background-radius: 8px; -fx-border-color: #4CAF50; -fx-border-radius: 8px; -fx-cursor: hand;"
        );
        startButton.setOnAction(e -> handleStartAction());

        contentBox.getChildren().addAll(
                variantSectionHeader, variantCards,
                modeSectionHeader, modeTabs,
                dynamicOptionsBox,
                themeBar,
                startButton
        );

        // Bottom Tibeb border
        TibebBanner tibebBottom = new TibebBanner(920, 16);

        getChildren().addAll(tibebTop, topNavBar, contentBox, tibebBottom);

        // Listen for Language changes
        I18n.addListener(lang -> Platform.runLater(this::updateLanguage));

        // Initial Language setup
        updateLanguage();
        updateModeView();
    }

    /**
     * Updates all UI text dynamically according to the active language.
     */
    private void updateLanguage() {
        mainTitle.setText(I18n.get("app.title"));
        subtitle.setText(I18n.get("app.subtitle"));

        variantSectionHeader.setText(I18n.get("section.rules"));
        egregnaTitle.setText(I18n.get("variant.egregna.title"));
        egregnaDesc.setText(I18n.get("variant.egregna.desc"));
        tankegnaTitle.setText(I18n.get("variant.tankegna.title"));
        tankegnaDesc.setText(I18n.get("variant.tankegna.desc"));

        modeSectionHeader.setText(I18n.get("section.modes"));
        aiModeBtn.setText(I18n.get("mode.ai"));
        passModeBtn.setText(I18n.get("mode.pass"));
        onlineModeBtn.setText(I18n.get("mode.online"));

        diffSectionLabel.setText(I18n.get("ai.difficulty"));
        easyChip.setText(I18n.get("diff.easy"));
        medChip.setText(I18n.get("diff.medium"));
        hardChip.setText(I18n.get("diff.hard"));

        p1Label.setText(I18n.get("player.1"));
        p2Label.setText(I18n.get("player.2"));

        onlineTitle.setText(I18n.get("online.title"));
        quickMatchBtn.setText(I18n.get("online.quick_match"));
        createRoomBtn.setText(I18n.get("online.create_room"));
        joinRoomBtn.setText(I18n.get("online.join"));
        joinCodeField.setPromptText(I18n.get("online.code_prompt"));
        onlineStatusLabel.setText(I18n.get("online.hint"));
        cancelSearchBtn.setText(I18n.get("online.cancel"));
        fallbackAIBtn.setText(I18n.get("online.play_ai"));

        themeLabel.setText(I18n.get("theme.label"));
        customizeBtn.setText(I18n.get("theme.customize"));
        startButton.setText(I18n.get("btn.start"));

        // Update default player 2 name text based on active mode
        if (selectedMode == GameMode.HUMAN_VS_AI) {
            p2Field.setText(I18n.get("player.computer"));
        } else if (selectedMode == GameMode.ONLINE_MULTIPLAYER) {
            p2Field.setText(I18n.get("player.online_opponent"));
        }
    }

    private HBox createVariantCards() {
        HBox box = new HBox(16);
        box.setAlignment(Pos.CENTER);

        VBox egregnaCard = new VBox(4, egregnaTitle, egregnaDesc);
        egregnaCard.setAlignment(Pos.CENTER_LEFT);
        egregnaCard.setPrefWidth(350);
        egregnaCard.setPadding(new Insets(10, 14, 10, 14));
        egregnaCard.setCursor(javafx.scene.Cursor.HAND);

        VBox tankegnaCard = new VBox(4, tankegnaTitle, tankegnaDesc);
        tankegnaCard.setAlignment(Pos.CENTER_LEFT);
        tankegnaCard.setPrefWidth(350);
        tankegnaCard.setPadding(new Insets(10, 14, 10, 14));
        tankegnaCard.setCursor(javafx.scene.Cursor.HAND);

        updateCardSelection(egregnaCard, selectedVariant == DamaVariant.EGREGNA);
        updateCardSelection(tankegnaCard, selectedVariant == DamaVariant.TANKEGNA);

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

    private void updateCardSelection(VBox card, boolean active) {
        if (active) {
            card.setStyle(
                    "-fx-background-color: #2E3B2E; -fx-border-color: #FFC107; -fx-border-width: 2px; " +
                    "-fx-border-radius: 8px; -fx-background-radius: 8px;"
            );
        } else {
            card.setStyle(
                    "-fx-background-color: #262626; -fx-border-color: #424242; -fx-border-width: 1px; " +
                    "-fx-border-radius: 8px; -fx-background-radius: 8px;"
            );
        }
    }

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
            p2Field.setText(I18n.get("player.computer"));
            p2Field.setDisable(true);
        } else if (isOnline) {
            p2Field.setText(I18n.get("player.online_opponent"));
            p2Field.setDisable(true);
        } else {
            p2Field.setText(I18n.getCurrentLanguage() == Language.AMHARIC ? "ተጫዋች 2" : "Player 2");
            p2Field.setDisable(false);
        }
    }

    // --- Online Matchmaking Logic ---

    private void ensureNetworkConnected(Runnable onSuccess) {
        if (networkClient != null && networkClient.isConnected()) {
            onSuccess.run();
            return;
        }

        new Thread(() -> {
            try {
                networkClient = new NetworkClient("127.0.0.1", 8989);
                setupClientListeners();
                networkClient.connect(p1Field.getText().trim());
                Platform.runLater(onSuccess);
            } catch (IOException ex) {
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
                        onlineStatusLabel.setText("Connection failed: " + e.getMessage());
                    });
                }
            }
        }).start();
    }

    private void setupClientListeners() {
        networkClient.setListener(new NetworkEventListener() {
            @Override
            public void onConnected() {
                Platform.runLater(() -> onlineStatusLabel.setText(I18n.get("online.hint")));
            }

            @Override
            public void onWaitingInQueue() {
                Platform.runLater(() -> {
                    startSearchingAnimation(I18n.get("online.searching"));
                    cancelSearchBtn.setVisible(true);
                    fallbackAIBtn.setVisible(true);
                });
            }

            @Override
            public void onRoomCreated(String roomCode) {
                Platform.runLater(() -> {
                    createdRoomCode = roomCode;
                    startSearchingAnimation(I18n.get("online.room_created", roomCode));
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
            public void onOpponentMove(com.dama.ethiopiancheckersgame.core.model.Move move) {}

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
                    onlineStatusLabel.setText(I18n.get("online.disconnected"));
                });
            }

            @Override
            public void onError(String errorMessage) {
                Platform.runLater(() -> {
                    stopSearchingAnimation();
                    onlineStatusLabel.setTextFill(Color.web("#EF5350"));
                    onlineStatusLabel.setText(errorMessage);
                });
            }
        });
    }

    private void startQuickMatch() {
        ensureNetworkConnected(() -> {
            onlineStatusLabel.setTextFill(Color.web("#29B6F6"));
            onlineStatusLabel.setText(I18n.get("online.searching") + "...");
            networkClient.findQuickMatch(selectedVariant);
        });
    }

    private void startCreateRoom() {
        ensureNetworkConnected(() -> {
            onlineStatusLabel.setTextFill(Color.web("#29B6F6"));
            onlineStatusLabel.setText(I18n.get("online.create_room") + "...");
            networkClient.createRoom(selectedVariant);
        });
    }

    private void startJoinRoom(String code) {
        if (code.isEmpty()) {
            onlineStatusLabel.setTextFill(Color.web("#EF5350"));
            onlineStatusLabel.setText(I18n.get("online.code_prompt"));
            return;
        }
        ensureNetworkConnected(() -> {
            onlineStatusLabel.setTextFill(Color.web("#29B6F6"));
            onlineStatusLabel.setText(I18n.get("online.join") + " #" + code + "...");
            networkClient.joinRoom(code);
        });
    }

    private void cancelOnlineSearch() {
        if (networkClient != null) {
            networkClient.cancelQueue();
        }
        stopSearchingAnimation();
        onlineStatusLabel.setTextFill(Color.web("#B0BEC5"));
        onlineStatusLabel.setText(I18n.get("online.hint"));
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
        if (localName.isEmpty()) localName = "Player 1";

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
        if (p1Name.isEmpty()) p1Name = (I18n.getCurrentLanguage() == Language.AMHARIC) ? "ተጫዋች 1" : "Player 1";

        DamaRuleStrategy rules = (selectedVariant == DamaVariant.EGREGNA) ? new EgregnaRules() : new TankegnaRules();
        Player player1 = new HumanPlayer(p1Name, PieceColor.RED);
        Player player2;

        if (selectedMode == GameMode.HUMAN_VS_AI) {
            player2 = new AIPlayer(I18n.get("player.computer"), PieceColor.BLACK, selectedDifficulty, rules);
        } else {
            String p2Name = p2Field.getText().trim();
            if (p2Name.isEmpty()) p2Name = (I18n.getCurrentLanguage() == Language.AMHARIC) ? "ተጫዋች 2" : "Player 2";
            player2 = new HumanPlayer(p2Name, PieceColor.BLACK);
        }

        GameSession session = new GameSession(player1, player2, selectedMode, rules);
        onStartGame.accept(session);
    }
}
