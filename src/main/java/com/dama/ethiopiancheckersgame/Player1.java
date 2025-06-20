package com.dama.ethiopiancheckersgame;

// Concrete Player Classes
class Player1 extends Player {
    public Player1(String name, char pieceSymbol, char kingSymbol) {
        super(name, pieceSymbol, kingSymbol);
    }

    @Override
    public boolean isPlayerTurn() {
        return true; // Custom logic for Player 1 if needed
    }
}
