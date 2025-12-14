package com.plagiarism.checker;

import com.plagiarism.checker.utils.CodeNormalizer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CodeNormalizerTest {

    private final CodeNormalizer normalizer = new CodeNormalizer();

    @Test
    void testNormalize_NullInput() {
        String result = normalizer.normalize(null);
        assertEquals("", result);
    }

    @Test
    void testNormalize_EmptyInput() {
        String result = normalizer.normalize("");
        assertEquals("", result);
    }

    @Test
    void testNormalize_WhitespaceInput() {
        String result = normalizer.normalize("   \n\t  ");
        assertEquals("", result);
    }

    @Test
    void testNormalize_SimpleCode() {
        String code = "public class Test { public static void main(String[] args) {} }";
        String result = normalizer.normalize(code);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertTrue(result.contains("class"));
        assertTrue(result.contains("main"));
    }

    @Test
    void testNormalize_WithComments() {
        String code = "// This is a comment\npublic class Test {}";
        String result = normalizer.normalize(code);

        assertNotNull(result);
        assertFalse(result.contains("//")); // Комментарии должны быть удалены
        assertTrue(result.contains("class"));
    }

    @Test
    void testNormalizeAlgorithm_SimpleCode() {
        String code = "int x = 5; int y = 10; int sum = x + y;";
        String result = normalizer.normalizeAlgorithm(code);

        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    void testIsValidCode_Valid() {
        String code = "public class Test {}";
        boolean isValid = normalizer.isValidCode(code);
        assertTrue(isValid);
    }

    @Test
    void testIsValidCode_Invalid() {
        boolean isValid = normalizer.isValidCode(null);
        assertFalse(isValid);
    }
}