package com.dama.ethiopiancheckersgame.network;

/**
 * Dedicated headless entry point for cloud deployment (Render, Fly.io, Railway, Docker).
 * Runs the Matchmaking & Move Relay server without requiring GUI or display environment.
 */
public class ServerMain {
    public static void main(String[] args) {
        int port = NetworkProtocol.DEFAULT_PORT;

        // Allow overriding port via PORT environment variable (standard in Render/Heroku/Railway)
        String envPort = System.getenv("PORT");
        if (envPort != null && !envPort.isEmpty()) {
            try {
                port = Integer.parseInt(envPort);
            } catch (NumberFormatException e) {
                System.err.println("Invalid PORT env variable: " + envPort + ", falling back to " + port);
            }
        }

        System.out.println("=================================================");
        System.out.println("  Ethiopian Checkers (Dama) Matchmaking Server  ");
        System.out.println("=================================================");
        System.out.println("Starting server on port: " + port + "...");

        try {
            MatchmakingServer server = new MatchmakingServer(port);
            server.start();
            System.out.println("✓ Server is LIVE and listening for connections.");
            System.out.println("• Public Quick Match Queue: ACTIVE");
            System.out.println("• Private 4-Digit Room Matching: ACTIVE");

            // Keep the main thread alive in Docker
            Thread.currentThread().join();
        } catch (Exception e) {
            System.err.println("Fatal: Server failed to start: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
