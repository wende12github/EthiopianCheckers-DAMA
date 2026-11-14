module com.dama.ethiopiancheckersgame {
    requires javafx.controls;
    requires javafx.fxml;

    opens com.dama.ethiopiancheckersgame to javafx.fxml;
    opens com.dama.ethiopiancheckersgame.ui.gui to javafx.fxml;

    exports com.dama.ethiopiancheckersgame;
    exports com.dama.ethiopiancheckersgame.core.model;
    exports com.dama.ethiopiancheckersgame.core.rules;
    exports com.dama.ethiopiancheckersgame.player;
    exports com.dama.ethiopiancheckersgame.player.ai;
    exports com.dama.ethiopiancheckersgame.engine;
    exports com.dama.ethiopiancheckersgame.ui.gui;
    exports com.dama.ethiopiancheckersgame.ui.cli;
}
