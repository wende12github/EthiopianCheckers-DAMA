package com.dama.ethiopiancheckersgame.ui.gui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * Dialog window allowing players to customize board square colors and piece colors.
 */
public class ThemeCustomizerDialog {

    public static void show(Stage owner) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.initOwner(owner);
        dialog.setTitle("Customize Board & Piece Colors");

        VBox root = new VBox(16);
        root.setPadding(new Insets(20));
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: #212121;");

        Label title = new Label("🎨 Theme & Color Customizer");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        title.setTextFill(Color.web("#FFD54F"));

        // Preset selector
        HBox presetBox = new HBox(10);
        presetBox.setAlignment(Pos.CENTER_LEFT);
        Label presetLabel = new Label("Presets:");
        presetLabel.setTextFill(Color.WHITE);
        presetLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));

        ComboBox<Theme> presetCombo = new ComboBox<>();
        presetCombo.getItems().addAll(Theme.getPresets());
        presetCombo.setValue(Theme.getCurrent());
        presetBox.getChildren().addAll(presetLabel, presetCombo);

        // Color pickers
        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);
        grid.setAlignment(Pos.CENTER);

        Theme current = Theme.getCurrent();

        ColorPicker lightTilePicker = new ColorPicker(current.getTileLight());
        ColorPicker darkTilePicker = new ColorPicker(current.getTileDark());
        ColorPicker p1ColorPicker = new ColorPicker(current.getPieceRedFill());
        ColorPicker p2ColorPicker = new ColorPicker(current.getPieceBlackFill());

        presetCombo.setOnAction(e -> {
            Theme selected = presetCombo.getValue();
            if (selected != null) {
                lightTilePicker.setValue(selected.getTileLight());
                darkTilePicker.setValue(selected.getTileDark());
                p1ColorPicker.setValue(selected.getPieceRedFill());
                p2ColorPicker.setValue(selected.getPieceBlackFill());
            }
        });

        grid.add(createLabel("Board Light Square:"), 0, 0);
        grid.add(lightTilePicker, 1, 0);

        grid.add(createLabel("Board Dark Square:"), 0, 1);
        grid.add(darkTilePicker, 1, 1);

        grid.add(createLabel("Player 1 Piece:"), 0, 2);
        grid.add(p1ColorPicker, 1, 2);

        grid.add(createLabel("Player 2 Piece:"), 0, 3);
        grid.add(p2ColorPicker, 1, 3);

        // Action Buttons
        HBox buttonBox = new HBox(12);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);

        Button applyBtn = new Button("Apply Theme");
        applyBtn.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        applyBtn.setTextFill(Color.WHITE);
        applyBtn.setStyle("-fx-background-color: #2E7D32; -fx-background-radius: 5px; -fx-cursor: hand;");
        applyBtn.setOnAction(e -> {
            Theme customTheme = new Theme(
                    "Custom Palette",
                    lightTilePicker.getValue(),
                    darkTilePicker.getValue(),
                    p1ColorPicker.getValue(),
                    p2ColorPicker.getValue()
            );
            Theme.setCurrent(customTheme);
            dialog.close();
        });

        Button closeBtn = new Button("Cancel");
        closeBtn.setFont(Font.font("Segoe UI", 13));
        closeBtn.setTextFill(Color.WHITE);
        closeBtn.setStyle("-fx-background-color: #424242; -fx-background-radius: 5px; -fx-cursor: hand;");
        closeBtn.setOnAction(e -> dialog.close());

        buttonBox.getChildren().addAll(closeBtn, applyBtn);

        root.getChildren().addAll(title, presetBox, grid, buttonBox);

        Scene scene = new Scene(root, 380, 340);
        dialog.setScene(scene);
        dialog.show();
    }

    private static Label createLabel(String text) {
        Label lbl = new Label(text);
        lbl.setFont(Font.font("Segoe UI", 13));
        lbl.setTextFill(Color.web("#ECEFF1"));
        return lbl;
    }
}
