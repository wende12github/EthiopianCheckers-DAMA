package com.dama.ethiopiancheckersgame.ui.gui;

import com.dama.ethiopiancheckersgame.ui.i18n.I18n;
import com.dama.ethiopiancheckersgame.ui.i18n.Language;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * Modern segmented pill button toggle for switching between Amharic and English.
 */
public class LanguageToggleBar extends HBox {
    private final Button amharicBtn;
    private final Button englishBtn;

    public LanguageToggleBar() {
        setAlignment(Pos.CENTER);
        setSpacing(2);
        setStyle(
                "-fx-background-color: #212121; " +
                "-fx-background-radius: 20px; " +
                "-fx-border-color: #424242; " +
                "-fx-border-width: 1px; " +
                "-fx-border-radius: 20px; " +
                "-fx-padding: 2px;"
        );

        amharicBtn = createSegment("🇪🇹 አማርኛ", Language.AMHARIC);
        englishBtn = createSegment("🇺🇸 English", Language.ENGLISH);

        amharicBtn.setOnAction(e -> I18n.setLanguage(Language.AMHARIC));
        englishBtn.setOnAction(e -> I18n.setLanguage(Language.ENGLISH));

        getChildren().addAll(amharicBtn, englishBtn);
        updateActiveState();

        I18n.addListener(lang -> updateActiveState());
    }

    private Button createSegment(String text, Language lang) {
        Button btn = new Button(text);
        btn.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));
        btn.setCursor(javafx.scene.Cursor.HAND);
        btn.setStyle("-fx-padding: 4px 12px;");
        return btn;
    }

    private void updateActiveState() {
        boolean isAm = (I18n.getCurrentLanguage() == Language.AMHARIC);

        if (isAm) {
            amharicBtn.setTextFill(Color.web("#121212"));
            amharicBtn.setStyle("-fx-background-color: #FFC107; -fx-background-radius: 16px; -fx-padding: 4px 12px; -fx-font-weight: bold;");

            englishBtn.setTextFill(Color.web("#B0BEC5"));
            englishBtn.setStyle("-fx-background-color: transparent; -fx-background-radius: 16px; -fx-padding: 4px 12px;");
        } else {
            englishBtn.setTextFill(Color.web("#121212"));
            englishBtn.setStyle("-fx-background-color: #FFC107; -fx-background-radius: 16px; -fx-padding: 4px 12px; -fx-font-weight: bold;");

            amharicBtn.setTextFill(Color.web("#B0BEC5"));
            amharicBtn.setStyle("-fx-background-color: transparent; -fx-background-radius: 16px; -fx-padding: 4px 12px;");
        }
    }
}
