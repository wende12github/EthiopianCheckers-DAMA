package com.dama.ethiopiancheckersgame.network;

import com.dama.ethiopiancheckersgame.core.model.Move;
import com.dama.ethiopiancheckersgame.core.model.PieceColor;
import com.dama.ethiopiancheckersgame.core.rules.DamaVariant;

import java.io.*;
import java.net.Socket;

/**
 * Client connection handling socket communications to the MatchmakingServer.
 */
public class NetworkClient {
    private final String host;
    private final int port;
    private Socket socket;
    private BufferedReader reader;
    private PrintWriter writer;
    private volatile boolean running = false;
    private NetworkEventListener listener;

    public NetworkClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void setListener(NetworkEventListener listener) {
        this.listener = listener;
    }

    public void connect(String playerName) throws IOException {
        this.socket = new Socket(host, port);
        this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.writer = new PrintWriter(socket.getOutputStream(), true);
        this.running = true;

        // Start listening thread
        Thread listenerThread = new Thread(this::listenLoop, "NetworkClient-Listener");
        listenerThread.setDaemon(true);
        listenerThread.start();

        // Send initial handshake
        sendRaw(NetworkProtocol.CMD_CONNECT + " " + playerName);
    }

    private void listenLoop() {
        try {
            String line;
            while (running && (line = reader.readLine()) != null) {
                handleServerMessage(line.trim());
            }
        } catch (IOException e) {
            if (running && listener != null) {
                listener.onError("Connection lost: " + e.getMessage());
                listener.onOpponentDisconnected();
            }
        } finally {
            disconnect();
        }
    }

    private void handleServerMessage(String message) {
        if (message.isEmpty() || listener == null) return;
        String[] tokens = message.split("\\s+");
        String event = tokens[0].toUpperCase();

        switch (event) {
            case NetworkProtocol.EVT_CONNECTED -> listener.onConnected();
            case NetworkProtocol.EVT_WAITING -> listener.onWaitingInQueue();
            case NetworkProtocol.EVT_ROOM_CREATED -> {
                if (tokens.length > 1) listener.onRoomCreated(tokens[1]);
            }
            case NetworkProtocol.EVT_MATCH_START -> {
                if (tokens.length >= 4) {
                    PieceColor myColor = PieceColor.valueOf(tokens[1].toUpperCase());
                    String opponent = tokens[2];
                    DamaVariant variant = DamaVariant.valueOf(tokens[3].toUpperCase());
                    listener.onMatchStarted(myColor, opponent, variant);
                }
            }
            case NetworkProtocol.EVT_OPPONENT_MOVE -> {
                Move m = NetworkProtocol.parseMove(tokens);
                if (m != null) listener.onOpponentMove(m);
            }
            case NetworkProtocol.EVT_DRAW_OFFERED -> listener.onOpponentDrawOffer();
            case NetworkProtocol.EVT_DRAW_ACCEPTED -> listener.onDrawAccepted();
            case NetworkProtocol.EVT_DRAW_DECLINED -> listener.onDrawDeclined();
            case NetworkProtocol.EVT_OPPONENT_RESIGNED -> listener.onOpponentResigned();
            case NetworkProtocol.EVT_OPPONENT_LEFT -> listener.onOpponentDisconnected();
            case NetworkProtocol.EVT_ERROR -> {
                String errMsg = message.substring(tokens[0].length()).trim();
                listener.onError(errMsg);
            }
        }
    }

    public void findQuickMatch(DamaVariant variant) {
        sendRaw(NetworkProtocol.CMD_QUICK_MATCH + " " + variant.name());
    }

    public void createRoom(DamaVariant variant) {
        sendRaw(NetworkProtocol.CMD_CREATE_ROOM + " " + variant.name());
    }

    public void joinRoom(String roomCode) {
        sendRaw(NetworkProtocol.CMD_JOIN_ROOM + " " + roomCode.trim());
    }

    public void cancelQueue() {
        sendRaw(NetworkProtocol.CMD_CANCEL_QUEUE);
    }

    public void sendMove(Move move) {
        sendRaw(NetworkProtocol.formatMove(move));
    }

    public void offerDraw() {
        sendRaw(NetworkProtocol.CMD_OFFER_DRAW);
    }

    public void acceptDraw() {
        sendRaw(NetworkProtocol.CMD_ACCEPT_DRAW);
    }

    public void declineDraw() {
        sendRaw(NetworkProtocol.CMD_DECLINE_DRAW);
    }

    public void resign() {
        sendRaw(NetworkProtocol.CMD_RESIGN);
    }

    private void sendRaw(String msg) {
        if (writer != null) {
            writer.println(msg);
        }
    }

    public void disconnect() {
        running = false;
        try {
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (IOException ignored) {}
    }

    public boolean isConnected() {
        return running && socket != null && socket.isConnected() && !socket.isClosed();
    }
}
