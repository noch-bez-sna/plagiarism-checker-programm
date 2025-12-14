package com.plagiarism.checker;

import com.plagiarism.checker.core.PlagiarismDetector;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EdgeCasesTest {

    @Test
    void testVeryLongCode() {
        PlagiarismDetector detector = new PlagiarismDetector();

        // Создаем очень длинную строку (но меньше лимита)
        StringBuilder longCode = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            longCode.append("int x").append(i).append(" = ").append(i).append(";\n");
        }

        var results = detector.checkForPlagiarism(longCode.toString());
        assertNotNull(results);
        // Пустая база - результаты должны быть пустыми
        assertTrue(results.isEmpty());
    }

    @Test
    void testCodeWithSpecialCharacters() {
        PlagiarismDetector detector = new PlagiarismDetector();

        String code = "public class Test { \n" +
                "    // комментарий с кириллицей: привет\n" +
                "    String str = \"строка с спецсимволами: !@#$%^&*()\";\n" +
                "}";

        var results = detector.checkForPlagiarism(code);
        assertNotNull(results);
        assertTrue(results.isEmpty()); // Пустая база
    }

    @Test
    void testEmptyResultsFromDetector() {
        PlagiarismDetector detector = new PlagiarismDetector();

        // Проверяем разные вариации пустого ввода
        assertTrue(detector.checkForPlagiarism("").isEmpty());
        assertTrue(detector.checkForPlagiarism("   ").isEmpty());
        assertTrue(detector.checkForPlagiarism("\n\n\t").isEmpty());

        // Проверяем что методы не падают
        assertDoesNotThrow(() -> detector.checkForPlagiarism(null));
    }

    @Test
    void testDetectorMethodsDoNotThrow() {
        PlagiarismDetector detector = new PlagiarismDetector();

        // Все эти вызовы не должны бросать исключения
        assertDoesNotThrow(() -> detector.clearDatabase());
        assertDoesNotThrow(() -> detector.getDatabaseFiles());
        assertDoesNotThrow(() -> detector.getDatabaseStats());
        assertDoesNotThrow(() -> detector.isDatabaseLoaded());
        assertDoesNotThrow(() -> detector.getDatabaseFileCount());
        assertDoesNotThrow(() -> detector.getTotalFragmentsCount());
        assertDoesNotThrow(() -> detector.isProcessingInProgress());
    }

    @Test
    void testDetectorStateTransitions() {
        PlagiarismDetector detector = new PlagiarismDetector();

        // Проверяем начальное состояние
        assertFalse(detector.isDatabaseLoaded());
        assertFalse(detector.isProcessingInProgress());
        assertEquals(0, detector.getDatabaseFileCount());
        assertEquals(0, detector.getTotalFragmentsCount());

        // Очистка не должна влиять на состояние (уже пустая)
        detector.clearDatabase();
        assertFalse(detector.isDatabaseLoaded());
        assertFalse(detector.isProcessingInProgress());

        // Проверяем что доступ к внутренним компонентам не вызывает ошибок
        assertNotNull(detector.getDatabaseManager());
        assertNotNull(detector.getFragmentExtractor());
        assertNotNull(detector.getSimilarityCalculator());
    }
}