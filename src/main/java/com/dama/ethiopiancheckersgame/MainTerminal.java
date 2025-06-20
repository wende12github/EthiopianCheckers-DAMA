package com.dama.ethiopiancheckersgame;

public class MainTerminal {
    public static void main(String[] args) {
        try {
            GameManager gameManager = new GameManager();
            gameManager.play();
        } catch (Exception ex) {
            ErrorLogger.logError("Error in terminal game execution", ex);
        }
    }
}
