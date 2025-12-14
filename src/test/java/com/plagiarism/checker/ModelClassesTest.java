package com.plagiarism.checker;

import com.plagiarism.checker.model.CodeFragment;
import com.plagiarism.checker.model.PlagiarismResult;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class ModelClassesTest {

    @Test
    void testCodeFragment_ConstructorAndGetters() {
        CodeFragment fragment = new CodeFragment("normalized", "original", 10);

        assertEquals("normalized", fragment.getNormalizedContent());
        assertEquals("original", fragment.getOriginalContent());
        assertEquals(10, fragment.getLineNumber());
    }

    @Test
    void testCodeFragment_EqualsAndHashCode() {
        CodeFragment fragment1 = new CodeFragment("content", "original", 1);
        CodeFragment fragment2 = new CodeFragment("content", "different", 2);
        CodeFragment fragment3 = new CodeFragment("different", "original", 1);

        // equals должен сравнивать normalizedContent
        assertEquals(fragment1, fragment2);
        assertNotEquals(fragment1, fragment3);

        // hashCode должен быть одинаковым для одинакового normalizedContent
        assertEquals(fragment1.hashCode(), fragment2.hashCode());
    }

    @Test
    void testPlagiarismResult_ConstructorAndGetters() {
        Map<CodeFragment, List<String>> matches = new HashMap<>();
        matches.put(new CodeFragment("content", "original", 1),
                Collections.singletonList("file1.java"));

        PlagiarismResult result = new PlagiarismResult("test.java", 75.5, matches);

        assertEquals("test.java", result.getFileName());
        assertEquals(75.5, result.getSimilarityPercentage(), 0.001);
        assertEquals(1, result.getMatchingFragments().size());
    }

    @Test
    void testPlagiarismResult_EmptyFileName() {
        // Теперь конструктор допускает пустое имя файла
        PlagiarismResult result = new PlagiarismResult("", 0.0, Collections.emptyMap());

        assertEquals("", result.getFileName());
        assertEquals(0.0, result.getSimilarityPercentage(), 0.001);
        assertTrue(result.getMatchingFragments().isEmpty());
    }

    @Test
    void testPlagiarismResult_NullFileName() {
        // Теперь конструктор допускает null, преобразуя в пустую строку
        PlagiarismResult result = new PlagiarismResult(null, 0.0, Collections.emptyMap());

        assertEquals("", result.getFileName());  // null преобразуется в ""
        assertEquals(0.0, result.getSimilarityPercentage(), 0.001);
        assertTrue(result.getMatchingFragments().isEmpty());
    }

    @Test
    void testPlagiarismResult_EmptyMatches() {
        PlagiarismResult result = new PlagiarismResult("test.java", 0.0, Collections.emptyMap());

        assertEquals("test.java", result.getFileName());
        assertEquals(0.0, result.getSimilarityPercentage(), 0.001);
        assertTrue(result.getMatchingFragments().isEmpty());
    }

    @Test
    void testPlagiarismResult_NullMatches() {
        // Проверяем что null матчи обрабатываются
        PlagiarismResult result = new PlagiarismResult("test.java", 0.0, null);

        assertEquals("test.java", result.getFileName());
        assertEquals(0.0, result.getSimilarityPercentage(), 0.001);
        assertTrue(result.getMatchingFragments().isEmpty()); // должно быть пустым
    }

    @Test
    void testPlagiarismResult_EdgeCaseSimilarity() {
        // Тестируем граничные значения процента схожести
        PlagiarismResult result1 = new PlagiarismResult("file1.java", -10.0, Collections.emptyMap());
        PlagiarismResult result2 = new PlagiarismResult("file2.java", 150.0, Collections.emptyMap());
        PlagiarismResult result3 = new PlagiarismResult("file3.java", 50.5, Collections.emptyMap());

        // Значения должны быть скорректированы в диапазон 0-100
        assertEquals(0.0, result1.getSimilarityPercentage(), 0.001); // -10 → 0
        assertEquals(100.0, result2.getSimilarityPercentage(), 0.001); // 150 → 100
        assertEquals(50.5, result3.getSimilarityPercentage(), 0.001); // остается как есть
    }

    @Test
    void testPlagiarismResult_SeverityLevels() {
        // Проверяем все уровни серьезности
        PlagiarismResult none = new PlagiarismResult("none.java", 0.0, Collections.emptyMap());
        PlagiarismResult low = new PlagiarismResult("low.java", 10.0, Collections.emptyMap());
        PlagiarismResult medium = new PlagiarismResult("medium.java", 30.0, Collections.emptyMap());
        PlagiarismResult high = new PlagiarismResult("high.java", 70.0, Collections.emptyMap());
        PlagiarismResult critical = new PlagiarismResult("critical.java", 90.0, Collections.emptyMap());

        assertEquals(PlagiarismResult.SeverityLevel.NONE, none.getSeverity());
        assertEquals(PlagiarismResult.SeverityLevel.LOW, low.getSeverity());
        assertEquals(PlagiarismResult.SeverityLevel.MEDIUM, medium.getSeverity());
        assertEquals(PlagiarismResult.SeverityLevel.HIGH, high.getSeverity());
        assertEquals(PlagiarismResult.SeverityLevel.CRITICAL, critical.getSeverity());
    }

    @Test
    void testCodeFragment_WithNullContent() {
        // Проверяем что конструктор обрабатывает null
        CodeFragment fragment = new CodeFragment(null, null, 0);
        assertNull(fragment.getNormalizedContent());
        assertNull(fragment.getOriginalContent());
        assertEquals(0, fragment.getLineNumber());
    }

    @Test
    void testPlagiarismResult_LargeNumberOfMatches() {
        // Тестируем с большим количеством совпадений
        Map<CodeFragment, List<String>> matches = new HashMap<>();
        for (int i = 0; i < 10; i++) {
            matches.put(new CodeFragment("content" + i, "original" + i, i),
                    Collections.singletonList("file.java"));
        }

        PlagiarismResult result = new PlagiarismResult("file.java", 45.7, matches);

        assertEquals("file.java", result.getFileName());
        assertEquals(45.7, result.getSimilarityPercentage(), 0.001);
        assertEquals(10, result.getMatchingFragments().size());
        assertEquals(10, result.getMatchCount());
    }
}