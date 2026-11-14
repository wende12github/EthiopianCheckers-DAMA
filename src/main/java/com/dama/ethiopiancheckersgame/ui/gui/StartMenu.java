package com.dama.ethiopiancheckersgame.ui.gui;

import com.dama.ethiopiancheckersgame.core.model.PieceColor;
import com.dama.ethiopiancheckersgame.core.rules.DamaRuleStrategy;
import com.dama.ethiopiancheckersgame.core.rules.DamaVariant;
import com.dama.ethiopiancheckersgame.core.rules.EgregnaRules;
import com.dama.ethiopiancheckersgame.core.rules.TankegnaRules;
import com.dama.ethiopiancheckersgame.engine.GameMode;
import com.dama.ethiopiancheckersgame.engine.GameSession;
import com.dama.ethiopiancheckersgame.player.HumanPlayer;
import com.dama.ethiopiancheckersgame.player.Player;
import com.dama.ethiopiancheckersgame.player.ai.AIDifficulty;
import com.dama.ethiopiancheckersgame.player.ai.AIPlayer;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.util.function.Consumer;

/**
 * Modern startup menu scene for selecting game modes, rules (Egregna vs Tankegna),
 * player names, AI difficulty, and board/piece themes.
 */
public class StartMenu extends VBox {
    private final Consumer<GameSession> onStartGame;

    public StartMenu(Consumer<GameSession> onStartGame) {
        this.onStartGame = onStartGame;

        setAlignment(Pos.CENTER);
        setPadding(new Insets(30));
        setSpacing(16);
        setStyle("-fx-background-color: #1A1A1A;");

        // Header Title
        Label title = new Label("Ethiopian Checkers");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 32));
        title.setTextFill(Color.web("#FFFFFF"));

        Label subtitle = new Label("የኢትዮጵያ ዳማ (Dama)");
        subtitle.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 18));
        subtitle.setTextFill(Color.web("#FFB300")); // Ethiopian gold

        // Form Container
        GridPane form = new GridPane();
        form.setAlignment(Pos.CENTER);
        form.setHgap(16);
        form.setVgap(14);
        form.setPadding(new Insets(20));
        form.setStyle("-fx-background-color: #262626; -fx-background-radius: 10px;");

        // 1. Rules Variant Selection (Egregna vs Tankegna)
        Label variantLabel = createLabel("Rules Variant:");
        ComboBox<DamaVariant> variantCombo = new ComboBox<>();
        variantCombo.getItems().addAll(DamaVariant.EGREGNA, DamaVariant.TANKEGNA);
        variantCombo.setValue(DamaVariant.TANKEGNA);
        variantCombo.setPrefWidth(240);
        variantCombo.setStyle("-fx-font-size: 13px;");

        Label variantDesc = new Label(variantCombo.getValue().getDescription());
        variantDesc.setFont(Font.font("Segoe UI", 11));
        variantDesc.setTextFill(Color.web("#90A4AE"));
        variantDesc.setWrapText(true);
        variantDesc.setMaxWidth(240);

        variantCombo.setOnAction(e -> variantDesc.setText(variantCombo.getValue().getDescription()));

        VBox variantBox = new VBox(4, variantCombo, variantDesc);

        // 2. Game Mode Selection
        Label modeLabel = createLabel("Game Mode:");
        ComboBox<GameMode> modeCombo = new ComboBox<>();
        modeCombo.getItems().addAll(GameMode.HUMAN_VS_AI, GameMode.HUMAN_VS_HUMAN);
        modeCombo.setValue(GameMode.HUMAN_VS_AI);
        modeCombo.setPrefWidth(240);
        modeCombo.setStyle("-fx-font-size: 13px;");

        // 3. AI Difficulty
        Label diffLabel = createLabel("AI Difficulty:");
        ComboBox<AIDifficulty> diffCombo = new ComboBox<>();
        diffCombo.getItems().addAll(AIDifficulty.EASY, AIDifficulty.MEDIUM, AIDifficulty.HARD);
        diffCombo.setValue(AIDifficulty.MEDIUM);
        diffCombo.setPrefWidth(240);
        diffCombo.setStyle("-fx-font-size: 13px;");

        // 4. Player Names
        Label p1Label = createLabel("Player 1 (Red):");
        TextField p1Field = createTextField("Player 1");

        Label p2Label = createLabel("Player 2 (Black):");
        TextField p2Field = createTextField("Computer");

        // Mode change listener
        modeCombo.setOnAction(e -> {
            boolean isAI = modeCombo.getValue() == GameMode.HUMAN_VS_AI;
            diffLabel.setVisible(isAI);
            diffCombo.setVisible(isAI);
            if (isAI) {
                p2Field.setText("Computer");
                p2Field.setDisable(true);
            } else {
                p2Field.setText("Player 2");
                p2Field.setDisable(false);
            }
        });

        p2Field.setDisable(true); // AI default

        // 5. Theme / Color Customization
        Label themeLabel = createLabel("Theme / Colors:");
        ComboBox<Theme> themeCombo = new ComboBox<>();
        themeCombo.getItems().addAll(Theme.getPresets());
        themeCombo.setValue(Theme.getCurrent());
        themeCombo.setPrefWidth(150);
        themeCombo.setStyle("-fx-font-size: 12px;");
        themeCombo.setOnAction(e -> {
            if (themeCombo.getValue() != null) {
                Theme.setCurrent(themeCombo.getValue());
            }
        });

        Button customizeBtn = new Button("🎨 Customize...");
        customizeBtn.setFont(Font.font("Segoe UI", 12));
        customizeBtn.setTextFill(Color.WHITE);
        customizeBtn.setStyle("-fx-background-color: #455A64; -fx-background-radius: 4px; -fx-cursor: hand;");
        customizeBtn.setOnAction(e -> ThemeCustomizerDialog.show((Stage) getScene().getWindow()));

        HBox themeBox = new HBox(8, themeCombo, customizeBtn);
        themeBox.setAlignment(Pos.CENTER_LEFT);

        form.add(variantLabel, 0, 0);
        form.add(variantBox, 1, 0);
        form.add(modeLabel, 0, 1);
        form.add(modeCombo, 1, 1);
        form.add(diffLabel, 0, 2);
        form.add(diffCombo, 1, 2);
        form.add(p1Label, 0, 3);
        form.add(p1Field, 1, 3);
        form.add(p2Label, 0, 4);
        form.add(p2Field, 1, 4);
        form.add(themeLabel, 0, 5);
        form.add(themeBox, 1, 5);

        // Start Button
        Button startButton = new Button("▶  Start Match");
        startButton.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        startButton.setTextFill(Color.WHITE);
        startButton.setPrefWidth(240);
        startButton.setPadding(new Insets(12, 24, 12, 24));
        startButton.setStyle("-fx-background-color: #2E7D32; -fx-background-radius: 6px; -fx-cursor: hand;");

        startButton.setOnAction(e -> {
            String p1Name = p1Field.getText().trim();
            if (p1Name.isEmpty()) p1Name = "Player 1";

            DamaVariant variant = variantCombo.getValue();
            DamaRuleStrategy rules = (variant == DamaVariant.EGREGNA) ? new EgregnaRules() : new TankegnaRules();

            Player player1 = new HumanPlayer(p1Name, PieceColor.RED);
            Player player2;

            if (modeCombo.getValue() == GameMode.HUMAN_VS_AI) {
                player2 = new AIPlayer("Computer", PieceColor.BLACK, diffCombo.getValue(), rules);
            } else {
                String p2Name = p2Field.getText().trim();
                if (p2Name.isEmpty()) p2Name = "Player 2";
                player2 = new HumanPlayer(p2Name, PieceColor.BLACK);
            }

            GameSession session = new GameSession(player1, player2, modeCombo.getValue(), rules);
            onStartGame.accept(session);
        });

        getChildren().addAll(title, subtitle, form, startButton);
    }

    private Label createLabel(String text) {
        Label lbl = new Label(text);
        lbl.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        lbl.setTextFill(Color.web("#CFD8DC"));
        return lbl;
    }

    private TextField createTextField(String defaultText) {
        TextField tf = new TextField(defaultText);
        tf.setFont(Font.font("Segoe UI", 13));
        tf.setPrefWidth(240);
        tf.setStyle("-fx-background-color: #333333; -fx-text-fill: #FFFFFF; -fx-border-color: #555555; -fx-border-radius: 4px;");
        return tf;
    }
}
