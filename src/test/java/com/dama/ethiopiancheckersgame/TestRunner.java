package com.dama.ethiopiancheckersgame;

import java.lang.reflect.Method;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class TestRunner {
    public static void main(String[] args) {
        System.out.println("Running Ethiopian Checkers (Dama) v2.0 Test Suite...\n");
        int passed = 0;
        int failed = 0;

        Class<?>[] testClasses = {
                EthiopianDamaRulesTest.class,
                DrawDetectorTest.class,
                com.dama.ethiopiancheckersgame.network.NetworkMatchmakingTest.class
        };

        for (Class<?> testClass : testClasses) {
            System.out.println("==> Running " + testClass.getSimpleName() + ":");
            for (Method method : testClass.getDeclaredMethods()) {
                if (method.isAnnotationPresent(Test.class)) {
                    Object instance = null;
                    try {
                        instance = testClass.getDeclaredConstructor().newInstance();

                        // Run @BeforeEach
                        for (Method bm : testClass.getDeclaredMethods()) {
                            if (bm.isAnnotationPresent(BeforeEach.class)) {
                                bm.setAccessible(true);
                                bm.invoke(instance);
                            }
                        }

                        method.setAccessible(true);
                        method.invoke(instance);
                        System.out.println("  ✓ [PASS] " + method.getName());
                        passed++;
                    } catch (Exception ex) {
                        System.err.println("  ✗ [FAIL] " + method.getName() + " -> " + ex.getCause());
                        ex.printStackTrace();
                        failed++;
                    } finally {
                        // Run @AfterEach
                        if (instance != null) {
                            for (Method am : testClass.getDeclaredMethods()) {
                                if (am.isAnnotationPresent(AfterEach.class)) {
                                    try {
                                        am.invoke(instance);
                                    } catch (Exception ignored) {}
                                }
                            }
                        }
                    }
                }
            }
            System.out.println();
        }

        System.out.printf("%nTests summary: %d passed, %d failed.%n", passed, failed);
        if (failed > 0) {
            System.exit(1);
        }
    }
}
