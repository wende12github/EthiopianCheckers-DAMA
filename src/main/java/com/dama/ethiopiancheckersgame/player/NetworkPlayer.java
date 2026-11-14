package com.dama.ethiopiancheckersgame.player;

import com.dama.ethiopiancheckersgame.core.model.GameState;
import com.dama.ethiopiancheckersgame.core.model.Move;
import com.dama.ethiopiancheckersgame.core.model.PieceColor;
import com.dama.ethiopiancheckersgame.network.NetworkClient;

import java.util.concurrent.CompletableFuture;

/**
 * Remote online player communicating over network sockets.
 */
public class NetworkPlayer implements Player {
    private final String name;
    private final PieceColor color;
    private final NetworkClient networkClient;
    private CompletableFuture<Move> pendingMovePromise;

    public NetworkPlayer(String name, PieceColor color, NetworkClient networkClient) {
        this.name = name;
        this.color = color;
        this.networkClient = networkClient;
    }

    @Override
    public String getName() {
        return name + " (🌐 Online)";
    }

    @Override
    public PieceColor getColor() {
        return color;
    }

    @Override
    public boolean isAI() {
        return false;
    }

    public NetworkClient getNetworkClient() {
        return networkClient;
    }

    @Override
    public CompletableFuture<Move> requestMove(GameState state) {
        pendingMovePromise = new CompletableFuture<>();
        return pendingMovePromise;
    }

    /**
     * Called when the remote opponent's move is received over the network socket.
     */
    public void submitRemoteMove(Move move) {
        if (pendingMovePromise != null && !pendingMovePromise.isDone()) {
            pendingMovePromise.complete(move);
        }
    }
}
