package com.dama.ethiopiancheckersgame.engine;

import com.dama.ethiopiancheckersgame.core.model.Move;
import com.dama.ethiopiancheckersgame.core.model.PieceColor;
import com.dama.ethiopiancheckersgame.player.Player;

/**
 * Event listener interface for decoupled views (JavaFX GUI, CLI, Web).
 */
public interface GameEventListener {
    void onGameStarted(Player player1, Player player2);
    void onTurnStarted(Player currentPlayer);
    void onMoveExecuted(Move move, Player player);
    void onGameOver(Player winner, String reason);
    void onBoardReset();
}
