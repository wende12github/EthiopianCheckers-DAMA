package com.dama.ethiopiancheckersgame;

import com.dama.ethiopiancheckersgame.ui.cli.TerminalGame;

public class MainTerminal {
    public static void main(String[] args) {
        try {
            TerminalGame.run();
        } catch (Exception ex) {
            ErrorLogger.logError("Error in terminal game execution", ex);
        }
    }
}

