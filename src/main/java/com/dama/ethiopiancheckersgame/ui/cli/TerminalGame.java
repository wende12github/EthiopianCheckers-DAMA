package com.dama.ethiopiancheckersgame.ui.cli;

import com.dama.ethiopiancheckersgame.core.model.*;
import com.dama.ethiopiancheckersgame.core.rules.DamaRuleStrategy;
import com.dama.ethiopiancheckersgame.core.rules.DamaVariant;
import com.dama.ethiopiancheckersgame.core.rules.EgregnaRules;
import com.dama.ethiopiancheckersgame.core.rules.TankegnaRules;
import com.dama.ethiopiancheckersgame.engine.GameMode;
import com.dama.ethiopiancheckersgame.engine.GameSession;
import com.dama.ethiopiancheckersgame.player.HumanPlayer;
import com.dama.ethiopiancheckersgame.player.Player;
import com.dama.ethiopiancheckersgame.player.ai.AIDifficulty;
import com.dama.ethiopiancheckersgame.player.ai.AIPlayer;

import java.util.List;
import java.util.Scanner;

/**
 * Text-based console interface running on the modernized rules and AI engine.
 */
public class TerminalGame {

    public static void run() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("========================================");
        System.out.println("   ETHIOPIAN CHECKERS (DAMA / ዳማ) v2.0  ");
        System.out.println("========================================");
        System.out.println("Select Rules Variant:");
        System.out.println("1) Tankegna (ታንከኛ - Flying King & 4-Way Capture)");
        System.out.println("2) Egregna (እግረኛ - Foot Soldier: Forward Capture & 1-Step King)");
        System.out.print("Variant (1-2): ");

        int vChoice = 1;
        try {
            vChoice = Integer.parseInt(scanner.nextLine().trim());
        } catch (Exception ignored) {
        }
        DamaRuleStrategy rules = (vChoice == 2) ? new EgregnaRules() : new TankegnaRules();

        System.out.println("\nSelect Game Mode:");
        System.out.println("1) Player vs Computer (AI)");
        System.out.println("2) Human vs Human (Local)");
        System.out.print("Choice (1-2): ");

        int choice = 1;
        try {
            choice = Integer.parseInt(scanner.nextLine().trim());
        } catch (Exception ignored) {
        }

        Player p1 = new HumanPlayer("Player 1", PieceColor.RED);
        Player p2;

        if (choice == 2) {
            p2 = new HumanPlayer("Player 2", PieceColor.BLACK);
        } else {
            System.out.println("Select AI Difficulty: 1) Easy  2) Medium  3) Hard");
            System.out.print("Choice: ");
            int dChoice = 2;
            try {
                dChoice = Integer.parseInt(scanner.nextLine().trim());
            } catch (Exception ignored) {
            }
            AIDifficulty diff = switch (dChoice) {
                case 1 -> AIDifficulty.EASY;
                case 3 -> AIDifficulty.HARD;
                default -> AIDifficulty.MEDIUM;
            };
            p2 = new AIPlayer("Computer", PieceColor.BLACK, diff, rules);
        }

        GameSession session = new GameSession(p1, p2, choice == 2 ? GameMode.HUMAN_VS_HUMAN : GameMode.HUMAN_VS_AI, rules);

        while (!session.getCurrentState().isGameOver()) {
            printBoard(session.getCurrentState().getBoard());
            Player current = session.getCurrentPlayer();
            System.out.println("\n>>> Turn: " + current.getName() + " (" + current.getColor() + ")");

            List<Move> legalMoves = rules.getLegalMoves(session.getCurrentState().getBoard(), current.getColor());
            if (legalMoves.isEmpty()) {
                break;
            }

            if (current.isAI()) {
                System.out.println("AI is calculating best move...");
                Move aiMove = current.requestMove(session.getCurrentState()).join();
                System.out.println("AI plays: " + aiMove);
                session.makeMove(aiMove);
            } else {
                System.out.println("Available legal moves:");
                for (int i = 0; i < legalMoves.size(); i++) {
                    System.out.printf("[%d] %s%n", (i + 1), legalMoves.get(i));
                }
                System.out.print("Select move (1-" + legalMoves.size() + "): ");
                try {
                    int moveIdx = Integer.parseInt(scanner.nextLine().trim()) - 1;
                    if (moveIdx >= 0 && moveIdx < legalMoves.size()) {
                        session.makeMove(legalMoves.get(moveIdx));
                    } else {
                        System.out.println("Invalid selection. Try again.");
                    }
                } catch (Exception e) {
                    System.out.println("Invalid input. Try again.");
                }
            }
        }

        printBoard(session.getCurrentState().getBoard());
        System.out.println("\n****************************************");
        System.out.println("GAME OVER! " + session.getCurrentState().getGameOverReason());
        System.out.println("****************************************");

        // Game Over Options: Review Board or New Game
        while (true) {
            System.out.println("\nOptions:");
            System.out.println("1) Review Final Board State");
            System.out.println("2) Start New Game");
            System.out.println("3) Exit");
            System.out.print("Choice (1-3): ");
            String postChoice = scanner.nextLine().trim();

            if ("1".equals(postChoice)) {
                System.out.println("\n--- [REVIEW MODE: FINAL BOARD] ---");
                printBoard(session.getCurrentState().getBoard());
                System.out.println("Winner: " + session.getCurrentState().getWinner());
                System.out.println("Total moves played: " + session.getCurrentState().getMoveHistory().size());
            } else if ("2".equals(postChoice)) {
                run();
                return;
            } else {
                System.out.println("Thank you for playing Ethiopian Checkers!");
                break;
            }
        }
    }

    private static void printBoard(Board board) {
        System.out.println("\n    A   B   C   D   E   F   G   H");
        System.out.println("  +---+---+---+---+---+---+---+---+");
        for (int r = 0; r < Board.SIZE; r++) {
            System.out.print((8 - r) + " |");
            for (int c = 0; c < Board.SIZE; c++) {
                Piece piece = board.getPiece(r, c);
                if (piece == null) {
                    boolean isDark = (r + c) % 2 != 0;
                    System.out.print(isDark ? " · |" : "   |");
                } else {
                    char symbol;
                    if (piece.color() == PieceColor.RED) {
                        symbol = piece.isKing() ? 'K' : 'O';
                    } else {
                        symbol = piece.isKing() ? 'Q' : 'X';
                    }
                    System.out.print(" " + symbol + " |");
                }
            }
            System.out.println(" " + (8 - r));
            System.out.println("  +---+---+---+---+---+---+---+---+");
        }
        System.out.println("    A   B   C   D   E   F   G   H\n");
    }
}
