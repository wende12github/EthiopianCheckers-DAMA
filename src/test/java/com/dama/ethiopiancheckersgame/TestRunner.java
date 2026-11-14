package com.dama.ethiopiancheckersgame;

import java.lang.reflect.Method;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class TestRunner {
    public static void main(String[] args) {
        System.out.println("Running Ethiopian Checkers (Dama) v2.0 Test Suite...\n");
        int passed = 0;
        int failed = 0;

        Class<?> testClass = EthiopianDamaRulesTest.class;
        for (Method method : testClass.getDeclaredMethods()) {
            if (method.isAnnotationPresent(Test.class)) {
                try {
                    Object instance = testClass.getDeclaredConstructor().newInstance();

                    // Run @BeforeEach
                    for (Method bm : testClass.getDeclaredMethods()) {
                        if (bm.isAnnotationPresent(BeforeEach.class)) {
                            bm.invoke(instance);
                        }
                    }

                    method.invoke(instance);
                    System.out.println("  ✓ [PASS] " + method.getName());
                    passed++;
                } catch (Exception ex) {
                    System.err.println("  ✗ [FAIL] " + method.getName() + " -> " + ex.getCause());
                    ex.printStackTrace();
                    failed++;
                }
            }
        }

        System.out.printf("%nTests summary: %d passed, %d failed.%n", passed, failed);
        if (failed > 0) {
            System.exit(1);
        }
    }
}
