package com.dama.ethiopiancheckersgame.network;

import com.dama.ethiopiancheckersgame.core.model.Move;
import com.dama.ethiopiancheckersgame.core.model.PieceColor;
import com.dama.ethiopiancheckersgame.core.model.Position;
import com.dama.ethiopiancheckersgame.core.rules.DamaVariant;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

public class NetworkMatchmakingTest {
    private MatchmakingServer server;
    private final int testPort = 9876;

    @BeforeEach
    public void setUp() throws IOException {
        server = new MatchmakingServer(testPort);
        server.start();
    }

    @AfterEach
    public void tearDown() {
        if (server != null) {
            server.stop();
        }
    }

    @Test
    public void testQuickMatchPairing() throws Exception {
        NetworkClient client1 = new NetworkClient("127.0.0.1", testPort);
        NetworkClient client2 = new NetworkClient("127.0.0.1", testPort);

        CountDownLatch startLatch = new CountDownLatch(2);
        AtomicReference<PieceColor> c1Color = new AtomicReference<>();
        AtomicReference<PieceColor> c2Color = new AtomicReference<>();

        client1.setListener(new StubListener() {
            @Override
            public void onMatchStarted(PieceColor yourColor, String opponentName, DamaVariant variant) {
                c1Color.set(yourColor);
                startLatch.countDown();
            }
        });

        client2.setListener(new StubListener() {
            @Override
            public void onMatchStarted(PieceColor yourColor, String opponentName, DamaVariant variant) {
                c2Color.set(yourColor);
                startLatch.countDown();
            }
        });

        client1.connect("Alice");
        client2.connect("Bob");

        // Both join quick match for Tankegna
        client1.findQuickMatch(DamaVariant.TANKEGNA);
        Thread.sleep(50);
        client2.findQuickMatch(DamaVariant.TANKEGNA);

        boolean paired = startLatch.await(3, TimeUnit.SECONDS);
        assertTrue(paired, "Both players should be paired automatically in Quick Match");

        assertNotNull(c1Color.get());
        assertNotNull(c2Color.get());
        assertNotEquals(c1Color.get(), c2Color.get(), "Players must be assigned opposite colors");

        client1.disconnect();
        client2.disconnect();
    }

    @Test
    public void testPrivateRoomCreationAndJoin() throws Exception {
        NetworkClient host = new NetworkClient("127.0.0.1", testPort);
        NetworkClient guest = new NetworkClient("127.0.0.1", testPort);

        CountDownLatch roomCreatedLatch = new CountDownLatch(1);
        CountDownLatch startLatch = new CountDownLatch(2);
        AtomicReference<String> roomCode = new AtomicReference<>();

        host.setListener(new StubListener() {
            @Override
            public void onRoomCreated(String code) {
                roomCode.set(code);
                roomCreatedLatch.countDown();
            }

            @Override
            public void onMatchStarted(PieceColor yourColor, String opponentName, DamaVariant variant) {
                startLatch.countDown();
            }
        });

        guest.setListener(new StubListener() {
            @Override
            public void onMatchStarted(PieceColor yourColor, String opponentName, DamaVariant variant) {
                startLatch.countDown();
            }
        });

        host.connect("HostPlayer");
        guest.connect("GuestPlayer");

        host.createRoom(DamaVariant.EGREGNA);
        assertTrue(roomCreatedLatch.await(2, TimeUnit.SECONDS), "Room code must be received by host");
        assertNotNull(roomCode.get());
        assertEquals(4, roomCode.get().length(), "Room code must be a 4-digit code");

        // Guest joins using the generated code
        guest.joinRoom(roomCode.get());

        assertTrue(startLatch.await(3, TimeUnit.SECONDS), "Both players should start match upon joining room");

        host.disconnect();
        guest.disconnect();
    }

    @Test
    public void testMoveRelayBetweenClients() throws Exception {
        NetworkClient client1 = new NetworkClient("127.0.0.1", testPort);
        NetworkClient client2 = new NetworkClient("127.0.0.1", testPort);

        CountDownLatch moveLatch = new CountDownLatch(1);
        AtomicReference<Move> receivedMove = new AtomicReference<>();

        client1.setListener(new StubListener());
        client2.setListener(new StubListener() {
            @Override
            public void onOpponentMove(Move move) {
                receivedMove.set(move);
                moveLatch.countDown();
            }
        });

        client1.connect("Player1");
        client2.connect("Player2");

        client1.findQuickMatch(DamaVariant.TANKEGNA);
        client2.findQuickMatch(DamaVariant.TANKEGNA);

        Thread.sleep(100);

        Move original = new Move(new Position(6, 1), new Position(5, 2));
        client1.sendMove(original);

        assertTrue(moveLatch.await(2, TimeUnit.SECONDS), "Client 2 must receive move forwarded from Client 1");
        assertNotNull(receivedMove.get());
        assertEquals(original.getFrom(), receivedMove.get().getFrom());
        assertEquals(original.getTo(), receivedMove.get().getTo());

        client1.disconnect();
        client2.disconnect();
    }

    private static class StubListener implements NetworkEventListener {
        @Override public void onConnected() {}
        @Override public void onWaitingInQueue() {}
        @Override public void onRoomCreated(String roomCode) {}
        @Override public void onMatchStarted(PieceColor yourColor, String opponentName, DamaVariant variant) {}
        @Override public void onOpponentMove(Move move) {}
        @Override public void onOpponentDrawOffer() {}
        @Override public void onDrawAccepted() {}
        @Override public void onDrawDeclined() {}
        @Override public void onOpponentResigned() {}
        @Override public void onOpponentDisconnected() {}
        @Override public void onError(String errorMessage) {}
    }
}
