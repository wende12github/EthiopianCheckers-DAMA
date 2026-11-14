package com.dama.ethiopiancheckersgame.network;

import com.dama.ethiopiancheckersgame.core.rules.DamaVariant;

import java.io.*;
import java.net.*;
import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.*;

/**
 * Lightweight, high-concurrency matchmaking & move relay server.
 * Supports:
 * 1. Public Auto-Matchmaking Queue (FIFO) separated by game variant.
 * 2. Private 4-digit Room Code pairing.
 * 3. Bidirectional message relaying.
 * 4. Can run standalone or embedded on a background daemon thread.
 */
public class MatchmakingServer {
    private final int port;
    private ServerSocket serverSocket;
    private volatile boolean running = false;
    private final ExecutorService threadPool = Executors.newCachedThreadPool();

    // Queues for Quick Match (by variant)
    private final Map<DamaVariant, Queue<ClientHandler>> publicQueues = new ConcurrentHashMap<>();

    // Private rooms by 4-digit code
    private final Map<String, PrivateRoom> privateRooms = new ConcurrentHashMap<>();
    private final SecureRandom random = new SecureRandom();

    private static MatchmakingServer embeddedInstance;

    public MatchmakingServer(int port) {
        this.port = port;
        for (DamaVariant v : DamaVariant.values()) {
            publicQueues.put(v, new ConcurrentLinkedQueue<>());
        }
    }

    public synchronized void start() throws IOException {
        if (running) return;
        serverSocket = new ServerSocket(port);
        running = true;

        threadPool.submit(() -> {
            while (running && !serverSocket.isClosed()) {
                try {
                    Socket socket = serverSocket.accept();
                    ClientHandler handler = new ClientHandler(socket, this);
                    threadPool.submit(handler);
                } catch (IOException e) {
                    if (!running) break;
                }
            }
        });
    }

    public synchronized void stop() {
        running = false;
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException ignored) {}
        threadPool.shutdownNow();
    }

    public boolean isRunning() {
        return running;
    }

    public int getPort() {
        return port;
    }

    // Static helper to start an embedded server locally
    public static synchronized MatchmakingServer startEmbedded(int port) {
        if (embeddedInstance != null && embeddedInstance.isRunning()) {
            return embeddedInstance;
        }
        try {
            embeddedInstance = new MatchmakingServer(port);
            embeddedInstance.start();
            return embeddedInstance;
        } catch (IOException e) {
            System.err.println("Failed to start embedded server on port " + port + ": " + e.getMessage());
            return null;
        }
    }

    public static synchronized void stopEmbedded() {
        if (embeddedInstance != null) {
            embeddedInstance.stop();
            embeddedInstance = null;
        }
    }

    // --- Matchmaking Handlers ---

    void handleQuickMatch(ClientHandler client, DamaVariant variant) {
        Queue<ClientHandler> queue = publicQueues.get(variant);
        synchronized (queue) {
            // Remove any disconnected players from queue
            queue.removeIf(c -> !c.isActive());

            if (queue.isEmpty() || queue.peek() == client) {
                if (!queue.contains(client)) {
                    queue.add(client);
                }
                client.sendMessage(NetworkProtocol.EVT_WAITING);
            } else {
                ClientHandler opponent = queue.poll();
                pairPlayers(client, opponent, variant);
            }
        }
    }

    void handleCancelQueue(ClientHandler client) {
        for (Queue<ClientHandler> queue : publicQueues.values()) {
            synchronized (queue) {
                queue.remove(client);
            }
        }
    }

    String handleCreateRoom(ClientHandler client, DamaVariant variant) {
        String code;
        do {
            code = String.format("%04d", random.nextInt(10000));
        } while (privateRooms.containsKey(code));

        privateRooms.put(code, new PrivateRoom(code, client, variant));
        client.sendMessage(NetworkProtocol.EVT_ROOM_CREATED + " " + code);
        return code;
    }

    void handleJoinRoom(ClientHandler client, String code) {
        PrivateRoom room = privateRooms.get(code);
        if (room == null) {
            client.sendMessage(NetworkProtocol.EVT_ERROR + " Room code #" + code + " not found.");
            return;
        }
        if (!room.host.isActive()) {
            privateRooms.remove(code);
            client.sendMessage(NetworkProtocol.EVT_ERROR + " Host has left the room.");
            return;
        }
        if (room.isFull()) {
            client.sendMessage(NetworkProtocol.EVT_ERROR + " Room #" + code + " is already full.");
            return;
        }

        privateRooms.remove(code);
        pairPlayers(room.host, client, room.variant);
    }

    private void pairPlayers(ClientHandler p1, ClientHandler p2, DamaVariant variant) {
        // Randomly assign Red (moves first) and Black
        boolean p1IsRed = random.nextBoolean();
        ClientHandler red = p1IsRed ? p1 : p2;
        ClientHandler black = p1IsRed ? p2 : p1;

        red.setOpponent(black);
        black.setOpponent(red);

        red.sendMessage(NetworkProtocol.EVT_MATCH_START + " RED " + black.getPlayerName() + " " + variant.name());
        black.sendMessage(NetworkProtocol.EVT_MATCH_START + " BLACK " + red.getPlayerName() + " " + variant.name());
    }

    void removeClient(ClientHandler client) {
        for (Queue<ClientHandler> q : publicQueues.values()) {
            synchronized (q) {
                q.remove(client);
            }
        }
        privateRooms.entrySet().removeIf(e -> e.getValue().host == client);

        if (client.getOpponent() != null) {
            client.getOpponent().sendMessage(NetworkProtocol.EVT_OPPONENT_LEFT);
            client.getOpponent().setOpponent(null);
        }
    }

    // --- Private Room Container ---
    private static class PrivateRoom {
        final String code;
        final ClientHandler host;
        final DamaVariant variant;

        PrivateRoom(String code, ClientHandler host, DamaVariant variant) {
            this.code = code;
            this.host = host;
            this.variant = variant;
        }

        boolean isFull() {
            return false;
        }
    }

    // --- Client Connection Worker ---
    static class ClientHandler implements Runnable {
        private final Socket socket;
        private final MatchmakingServer server;
        private BufferedReader reader;
        private PrintWriter writer;
        private volatile boolean active = true;
        private String playerName = "Anonymous";
        private ClientHandler opponent;

        ClientHandler(Socket socket, MatchmakingServer server) {
            this.socket = socket;
            this.server = server;
        }

        @Override
        public void run() {
            try {
                reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                writer = new PrintWriter(socket.getOutputStream(), true);

                String line;
                while (active && (line = reader.readLine()) != null) {
                    handleCommand(line.trim());
                }
            } catch (IOException ignored) {
            } finally {
                disconnect();
            }
        }

        private void handleCommand(String line) {
            if (line.isEmpty()) return;
            String[] tokens = line.split("\\s+");
            String cmd = tokens[0].toUpperCase();

            switch (cmd) {
                case NetworkProtocol.CMD_CONNECT -> {
                    if (tokens.length > 1) {
                        this.playerName = line.substring(tokens[0].length()).trim();
                    }
                    sendMessage(NetworkProtocol.EVT_CONNECTED);
                }
                case NetworkProtocol.CMD_QUICK_MATCH -> {
                    DamaVariant variant = (tokens.length > 1) ?
                            DamaVariant.valueOf(tokens[1].toUpperCase()) : DamaVariant.TANKEGNA;
                    server.handleQuickMatch(this, variant);
                }
                case NetworkProtocol.CMD_CREATE_ROOM -> {
                    DamaVariant variant = (tokens.length > 1) ?
                            DamaVariant.valueOf(tokens[1].toUpperCase()) : DamaVariant.TANKEGNA;
                    server.handleCreateRoom(this, variant);
                }
                case NetworkProtocol.CMD_JOIN_ROOM -> {
                    if (tokens.length > 1) {
                        server.handleJoinRoom(this, tokens[1]);
                    }
                }
                case NetworkProtocol.CMD_CANCEL_QUEUE -> server.handleCancelQueue(this);
                case NetworkProtocol.CMD_MOVE -> {
                    if (opponent != null) {
                        opponent.sendMessage(NetworkProtocol.EVT_OPPONENT_MOVE + " " +
                                line.substring(NetworkProtocol.CMD_MOVE.length()).trim());
                    }
                }
                case NetworkProtocol.CMD_OFFER_DRAW -> {
                    if (opponent != null) opponent.sendMessage(NetworkProtocol.EVT_DRAW_OFFERED);
                }
                case NetworkProtocol.CMD_ACCEPT_DRAW -> {
                    if (opponent != null) opponent.sendMessage(NetworkProtocol.EVT_DRAW_ACCEPTED);
                }
                case NetworkProtocol.CMD_DECLINE_DRAW -> {
                    if (opponent != null) opponent.sendMessage(NetworkProtocol.EVT_DRAW_DECLINED);
                }
                case NetworkProtocol.CMD_RESIGN -> {
                    if (opponent != null) opponent.sendMessage(NetworkProtocol.EVT_OPPONENT_RESIGNED);
                }
            }
        }

        public void sendMessage(String msg) {
            if (writer != null) {
                writer.println(msg);
            }
        }

        public void disconnect() {
            active = false;
            server.removeClient(this);
            try {
                if (!socket.isClosed()) socket.close();
            } catch (IOException ignored) {}
        }

        public boolean isActive() {
            return active && !socket.isClosed();
        }

        public String getPlayerName() {
            return playerName;
        }

        public ClientHandler getOpponent() {
            return opponent;
        }

        public void setOpponent(ClientHandler opponent) {
            this.opponent = opponent;
        }
    }
}
