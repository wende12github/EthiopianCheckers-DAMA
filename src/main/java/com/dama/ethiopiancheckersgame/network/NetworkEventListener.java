package com.dama.ethiopiancheckersgame.network;

import com.dama.ethiopiancheckersgame.core.model.Move;
import com.dama.ethiopiancheckersgame.core.model.PieceColor;
import com.dama.ethiopiancheckersgame.core.rules.DamaVariant;

/**
 * Callback listener for network game events.
 */
public interface NetworkEventListener {
    void onConnected();
    void onWaitingInQueue();
    void onRoomCreated(String roomCode);
    void onMatchStarted(PieceColor yourColor, String opponentName, DamaVariant variant);
    void onOpponentMove(Move move);
    void onOpponentDrawOffer();
    void onDrawAccepted();
    void onDrawDeclined();
    void onOpponentResigned();
    void onOpponentDisconnected();
    void onError(String errorMessage);
}
