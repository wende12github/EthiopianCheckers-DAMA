module com.dama.ethiopiancheckersgame {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.dama.ethiopiancheckersgame to javafx.fxml;
    exports com.dama.ethiopiancheckersgame;
}