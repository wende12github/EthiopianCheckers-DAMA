package com.dama.ethiopiancheckersgame;

class Player2 extends Player {
    public Player2(String name, char pieceSymbol, char kingSymbol) {
        super(name, pieceSymbol, kingSymbol);
    }

    @Override
    public boolean isPlayerTurn() {
        return false; // Custom logic for Player 2 if needed
    }
}

