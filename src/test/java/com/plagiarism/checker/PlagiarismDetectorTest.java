package com.plagiarism.checker;

import com.plagiarism.checker.core.PlagiarismDetector;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class PlagiarismDetectorTest {

    @TempDir
    Path tempDir;

    @Test
    void testConstructor() {
        PlagiarismDetector detector = new PlagiarismDetector();
        assertNotNull(detector);
        assertNotNull(detector.getDatabaseManager());
        assertNotNull(detector.getFragmentExtractor());
        assertNotNull(detector.getSimilarityCalculator());
    }

    @Test
    void testCheckForPlagiarism_EmptyDatabase() {
        PlagiarismDetector detector = new PlagiarismDetector();
        String code = "public class Test {}";
        var results = detector.checkForPlagiarism(code);

        assertNotNull(results);
        assertTrue(results.isEmpty());
    }

    @Test
    void testCheckForPlagiarism_NullInput() {
        PlagiarismDetector detector = new PlagiarismDetector();
        var results = detector.checkForPlagiarism(null);

        assertNotNull(results);
        assertTrue(results.isEmpty());
    }

    @Test
    void testCheckForPlagiarism_EmptyInput() {
        PlagiarismDetector detector = new PlagiarismDetector();
        var results = detector.checkForPlagiarism("");

        assertNotNull(results);
        assertTrue(results.isEmpty());
    }

    @Test
    void testGetDatabaseFiles_Empty() {
        PlagiarismDetector detector = new PlagiarismDetector();
        var files = detector.getDatabaseFiles();

        assertNotNull(files);
        assertTrue(files.isEmpty());
    }

    @Test
    void testIsDatabaseLoaded_Initial() {
        PlagiarismDetector detector = new PlagiarismDetector();
        assertFalse(detector.isDatabaseLoaded());
    }

    @Test
    void testClearDatabase() {
        PlagiarismDetector detector = new PlagiarismDetector();
        detector.clearDatabase(); // Не должно падать
        assertFalse(detector.isDatabaseLoaded());
    }

    @Test
    void testGetDatabaseStats_Empty() {
        PlagiarismDetector detector = new PlagiarismDetector();
        var stats = detector.getDatabaseStats();

        assertNotNull(stats);
        assertEquals(0, stats.get("total_files"));
        assertEquals(0, stats.get("total_fragments"));
    }

    @Test
    void testGetDatabaseFileCount_Empty() {
        PlagiarismDetector detector = new PlagiarismDetector();
        assertEquals(0, detector.getDatabaseFileCount());
    }

    @Test
    void testGetTotalFragmentsCount_Empty() {
        PlagiarismDetector detector = new PlagiarismDetector();
        assertEquals(0, detector.getTotalFragmentsCount());
    }

    @Test
    void testIsProcessingInProgress_Initial() {
        PlagiarismDetector detector = new PlagiarismDetector();
        assertFalse(detector.isProcessingInProgress());
    }

    @Test
    void testValidDatabaseLoading() throws IOException {
        PlagiarismDetector detector = new PlagiarismDetector();

        // Создаем тестовый файл
        Path javaFile = tempDir.resolve("Valid.java");
        Files.writeString(javaFile, "public class Valid { public void test() {} }");

        // Загружаем базу
        detector.loadDatabase(tempDir);

        // Проверяем загруженное состояние
        assertTrue(detector.isDatabaseLoaded());
        assertEquals(1, detector.getDatabaseFileCount());
        assertTrue(detector.getTotalFragmentsCount() > 0);
        assertFalse(detector.isProcessingInProgress());
    }
}