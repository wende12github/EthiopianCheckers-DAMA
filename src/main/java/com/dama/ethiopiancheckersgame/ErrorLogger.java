package com.dama.ethiopiancheckersgame;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;

public class ErrorLogger {
    private static final String LOG_FILE = "error_log.txt";

    public static void logError(String message, Exception e) {
        try (PrintWriter out = new PrintWriter(new FileWriter(LOG_FILE, true))) {
            out.println(LocalDateTime.now() + " - ERROR: " + message);
            if (e != null) {
                e.printStackTrace(out);
            }
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    public static void logError(String message) {
        logError(message, null);
    }
}
