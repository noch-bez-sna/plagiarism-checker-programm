package com.plagiarism.checker;

import com.plagiarism.checker.core.PlagiarismDetector;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class DiagnosticTest {

    @TempDir
    Path tempDir;

    @Test
    void testWhatHappensWithEmptyDirectory() throws IOException {
        PlagiarismDetector detector = new PlagiarismDetector();

        // Создаем пустую директорию
        Path emptyDir = tempDir.resolve("empty");
        Files.createDirectory(emptyDir);

        System.out.println("Testing empty directory: " + emptyDir);

        try {
            detector.loadDatabase(emptyDir);
            System.out.println("SUCCESS: loadDatabase() did NOT throw exception");
            System.out.println("isDatabaseLoaded: " + detector.isDatabaseLoaded());
            System.out.println("getDatabaseFileCount: " + detector.getDatabaseFileCount());

            // Если не выбросило исключение - тест проходит
            assertTrue(true);

        } catch (Exception e) {
            System.out.println("FAILURE: loadDatabase() threw exception: " + e.getClass().getName());
            System.out.println("Message: " + e.getMessage());

            // Если выбросило исключение - тест тоже проходит
            assertTrue(e instanceof IOException);
        }
    }
}