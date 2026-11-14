package com.dama.ethiopiancheckersgame.network;

import com.dama.ethiopiancheckersgame.core.model.Move;
import com.dama.ethiopiancheckersgame.core.model.Position;

/**
 * Protocol specifications and message formatting for Ethiopian Checkers network play.
 */
public final class NetworkProtocol {
    private NetworkProtocol() {}

    public static final int DEFAULT_PORT = 8989;

    // Commands sent by Client -> Server
    public static final String CMD_CONNECT = "CONNECT";           // CONNECT <playerName>
    public static final String CMD_QUICK_MATCH = "QUICK_MATCH";   // QUICK_MATCH <EGREGNA|TANKEGNA>
    public static final String CMD_CREATE_ROOM = "CREATE_ROOM";   // CREATE_ROOM <EGREGNA|TANKEGNA>
    public static final String CMD_JOIN_ROOM = "JOIN_ROOM";       // JOIN_ROOM <roomCode>
    public static final String CMD_CANCEL_QUEUE = "CANCEL_QUEUE"; // CANCEL_QUEUE
    public static final String CMD_MOVE = "MOVE";                 // MOVE <fromR> <fromC> <toR> <toC>
    public static final String CMD_OFFER_DRAW = "OFFER_DRAW";     // OFFER_DRAW
    public static final String CMD_ACCEPT_DRAW = "ACCEPT_DRAW";   // ACCEPT_DRAW
    public static final String CMD_DECLINE_DRAW = "DECLINE_DRAW"; // DECLINE_DRAW
    public static final String CMD_RESIGN = "RESIGN";             // RESIGN

    // Events sent by Server -> Client
    public static final String EVT_CONNECTED = "CONNECTED";           // CONNECTED
    public static final String EVT_WAITING = "WAITING";               // WAITING
    public static final String EVT_ROOM_CREATED = "ROOM_CREATED";     // ROOM_CREATED <roomCode>
    public static final String EVT_MATCH_START = "MATCH_START";       // MATCH_START <RED|BLACK> <opponentName> <variant>
    public static final String EVT_OPPONENT_MOVE = "OPPONENT_MOVE";   // OPPONENT_MOVE <fromR> <fromC> <toR> <toC>
    public static final String EVT_DRAW_OFFERED = "DRAW_OFFERED";     // DRAW_OFFERED
    public static final String EVT_DRAW_ACCEPTED = "DRAW_ACCEPTED";   // DRAW_ACCEPTED
    public static final String EVT_DRAW_DECLINED = "DRAW_DECLINED";   // DRAW_DECLINED
    public static final String EVT_OPPONENT_RESIGNED = "OPPONENT_RESIGNED"; // OPPONENT_RESIGNED
    public static final String EVT_OPPONENT_LEFT = "OPPONENT_LEFT";   // OPPONENT_LEFT
    public static final String EVT_ERROR = "ERROR";                   // ERROR <message>

    public static String formatMove(Move move) {
        return CMD_MOVE + " " +
                move.getFrom().row() + " " + move.getFrom().col() + " " +
                move.getTo().row() + " " + move.getTo().col();
    }

    public static Move parseMove(String[] tokens) {
        if (tokens.length < 5) return null;
        int fr = Integer.parseInt(tokens[1]);
        int fc = Integer.parseInt(tokens[2]);
        int tr = Integer.parseInt(tokens[3]);
        int tc = Integer.parseInt(tokens[4]);
        return new Move(new Position(fr, fc), new Position(tr, tc));
    }
}
