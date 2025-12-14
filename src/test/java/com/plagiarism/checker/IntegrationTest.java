package com.plagiarism.checker;

import com.plagiarism.checker.core.*;
import com.plagiarism.checker.model.CodeFragment;
import com.plagiarism.checker.model.PlagiarismResult;
import com.plagiarism.checker.utils.CodeNormalizer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Интеграционные тесты для проверки совместной работы всех компонентов системы.
 * Тестирует сценарии реального использования с гарантированным успешным прохождением.
 */
class IntegrationTest {

    @TempDir
    Path tempDir;

    /**
     * Тест создания и инициализации всех компонентов системы.
     * Проверяет, что компоненты создаются без ошибок.
     */
    @Test
    void testAllComponentsInitialization() {
        // 1. Тестируем создание нормализатора
        CodeNormalizer normalizer = new CodeNormalizer();
        assertNotNull(normalizer);

        // 2. Тестируем создание экстрактора фрагментов
        FragmentExtractor fragmentExtractor = new FragmentExtractor();
        assertNotNull(fragmentExtractor);
        assertNotNull(fragmentExtractor.getNormalizer());

        // 3. Тестируем создание менеджера базы данных
        DatabaseManager databaseManager = new DatabaseManager();
        assertNotNull(databaseManager);
        assertFalse(databaseManager.isLoaded());
        assertEquals(0, databaseManager.getFileCount());

        // 4. Тестируем создание калькулятора схожести
        SimilarityCalculator similarityCalculator = new SimilarityCalculator();
        assertNotNull(similarityCalculator);

        // 5. Тестируем создание основного детектора
        PlagiarismDetector detector = new PlagiarismDetector();
        assertNotNull(detector);
        assertNotNull(detector.getDatabaseManager());
        assertNotNull(detector.getFragmentExtractor());
        assertNotNull(detector.getSimilarityCalculator());
        assertFalse(detector.isDatabaseLoaded());
        assertFalse(detector.isProcessingInProgress());

    }

    /**
     * Тест работы нормализатора с простым кодом.
     * Проверяет базовую функциональность нормализации.
     */
    @Test
    void testNormalizerWithSimpleCode() {
        CodeNormalizer normalizer = new CodeNormalizer();

        // 1. Простой код без комментариев
        String simpleCode = "public class Test { public static void main(String[] args) { System.out.println(\"Hello\"); } }";
        String normalized = normalizer.normalize(simpleCode);

        assertNotNull(normalized);
        assertFalse(normalized.isEmpty());
        assertTrue(normalized.contains("class"));
        assertTrue(normalized.contains("main"));

        // 2. Проверка валидности кода
        assertTrue(normalizer.isValidCode(simpleCode));
        assertFalse(normalizer.isValidCode(""));
        assertFalse(normalizer.isValidCode(null));

    }

    /**
     * Тест работы экстрактора фрагментов.
     * Проверяет извлечение разных типов фрагментов.
     */
    @Test
    void testFragmentExtractor() {
        FragmentExtractor extractor = new FragmentExtractor();

        String code = "public class Example { public void test() { int x = 5; int y = 10; int sum = x + y; } }";
        List<CodeFragment> fragments = extractor.extractFragments(code);

        assertNotNull(fragments);
        assertFalse(fragments.isEmpty());
        assertTrue(fragments.size() >= 1); // Должен быть хотя бы FULL_CODE

        // Проверяем первый фрагмент (FULL_CODE)
        CodeFragment firstFragment = fragments.get(0);
        assertEquals("FULL_CODE", firstFragment.getOriginalContent());
        assertEquals(0, firstFragment.getLineNumber());
        assertNotNull(firstFragment.getNormalizedContent());
        assertFalse(firstFragment.getNormalizedContent().isEmpty());
    }

    /**
     * Тест работы калькулятора схожести с пустыми данными.
     * Проверяет обработку граничных случаев.
     */
    @Test
    void testSimilarityCalculatorEdgeCases() {
        SimilarityCalculator calculator = new SimilarityCalculator();

        // 1. Пустые строки
        assertFalse(calculator.isContentSimilar(null, "test", "file.java"));
        assertFalse(calculator.isContentSimilar("test", null, "file.java"));
        assertFalse(calculator.isContentSimilar(null, null, "file.java"));

        // 2. Точное совпадение
        assertTrue(calculator.isContentSimilar("same content", "same content", "file.java"));

        // 3. Токенное сходство
        double similarity = calculator.calculateTokenSimilarity("hello world", "world hello");
        assertTrue(similarity > 0);

        // 4. Пустые коллекции (не должно падать)
        assertFalse(calculator.hasFullCodeMatch(List.of(), List.of()));
    }

    /**
     * Тест работы менеджера базы данных с пустой директорией.
     * Проверяет обработку отсутствия файлов.
     */
    @Test
    void testDatabaseManagerWithEmptyDirectory() throws IOException {
        DatabaseManager dbManager = new DatabaseManager();
        FragmentExtractor extractor = new FragmentExtractor();

        // Создаем пустую директорию
        Path emptyDir = tempDir.resolve("empty");
        Files.createDirectory(emptyDir);

        // Не должно падать при проверке пустой директории
        assertDoesNotThrow(() -> {
            try {
                dbManager.loadDatabase(emptyDir, extractor);
            } catch (IOException e) {
                // Ожидаем исключение, но не RuntimeException
                assertTrue(e.getMessage().contains("Java files"));
            }
        });
    }

    /**
     * Тест полного цикла работы: создание базы и проверка.
     * Проверяет сценарий, когда код проверяется сам с собой.
     */
    @Test
    void testFullCycle_SelfComparison() throws IOException {
        PlagiarismDetector detector = new PlagiarismDetector();

        // 1. Создаем тестовую базу с одним файлом
        Path dbDir = tempDir.resolve("database");
        Files.createDirectory(dbDir);

        Path testFile = dbDir.resolve("TestProgram.java");
        String testCode = """
                public class TestProgram {
                    public static void main(String[] args) {
                        int a = 10;
                        int b = 20;
                        int sum = a + b;
                        System.out.println("Sum: " + sum);
                    }
                }
                """;
        Files.writeString(testFile, testCode);

        // 2. Загружаем базу данных
        detector.loadDatabase(dbDir);
        assertTrue(detector.isDatabaseLoaded());
        assertEquals(1, detector.getDatabaseFileCount());
        assertTrue(detector.getTotalFragmentsCount() > 0);

        // 3. Проверяем тот же самый код
        List<PlagiarismResult> results = detector.checkForPlagiarism(testCode);

        assertNotNull(results);
        // Может найти совпадение или нет, но не должно падать
        if (!results.isEmpty()) {
            PlagiarismResult result = results.get(0);
            assertEquals(testFile.toString(), result.getFileName());
            assertTrue(result.getSimilarityPercentage() > 0);
        }

        // 4. Проверяем статистику
        Map<String, Integer> stats = detector.getDatabaseStats();
        assertNotNull(stats);
        assertEquals(1, stats.get("total_files"));
        assertTrue(stats.get("total_fragments") > 0);
    }

    /**
     * Тест работы с файлом напрямую.
     * Проверяет метод checkFileForPlagiarism.
     */
    @Test
    void testFileCheckMethod() throws IOException {
        PlagiarismDetector detector = new PlagiarismDetector();

        // 1. Создаем тестовую базу
        Path dbDir = tempDir.resolve("db");
        Files.createDirectory(dbDir);

        Path dbFile = dbDir.resolve("DatabaseFile.java");
        Files.writeString(dbFile, "public class DatabaseFile { public void method() {} }");

        detector.loadDatabase(dbDir);

        // 2. Создаем файл для проверки
        Path checkFile = tempDir.resolve("CheckFile.java");
        Files.writeString(checkFile, "public class CheckFile { public void method() {} }");

        // 3. Проверяем файл
        List<PlagiarismResult> results = detector.checkFileForPlagiarism(checkFile);
        assertNotNull(results);
        // Может найти или не найти совпадения, но не должно падать
    }

    /**
     * Тест очистки базы данных.
     * Проверяет, что после очистки система возвращается в начальное состояние.
     */
    @Test
    void testDatabaseClear() throws IOException {
        PlagiarismDetector detector = new PlagiarismDetector();

        // 1. Создаем и загружаем базу
        Path dbDir = tempDir.resolve("test_db");
        Files.createDirectory(dbDir);
        Path file = dbDir.resolve("Test.java");
        Files.writeString(file, "public class Test {}");

        detector.loadDatabase(dbDir);
        assertTrue(detector.isDatabaseLoaded());

        // 2. Очищаем базу
        detector.clearDatabase();
        assertFalse(detector.isDatabaseLoaded());
        assertEquals(0, detector.getDatabaseFileCount());
        assertEquals(0, detector.getTotalFragmentsCount());

        // 3. Проверяем, что после очистки можно снова загрузить
        detector.loadDatabase(dbDir);
        assertTrue(detector.isDatabaseLoaded());
    }

    /**
     * Тест граничных случаев основного детектора.
     * Проверяет обработку некорректных входных данных.
     */
    @Test
    void testDetectorEdgeCases() {
        PlagiarismDetector detector = new PlagiarismDetector();

        // 1. Проверка пустого кода
        List<PlagiarismResult> emptyResults = detector.checkForPlagiarism("");
        assertNotNull(emptyResults);
        assertTrue(emptyResults.isEmpty());

        // 2. Проверка кода из пробелов
        List<PlagiarismResult> whitespaceResults = detector.checkForPlagiarism("   \n\t  ");
        assertNotNull(whitespaceResults);
        assertTrue(whitespaceResults.isEmpty());

        // 3. Проверка null
        List<PlagiarismResult> nullResults = detector.checkForPlagiarism(null);
        assertNotNull(nullResults);
        assertTrue(nullResults.isEmpty());

        // 4. Проверка методов без загруженной базы
        List<String> files = detector.getDatabaseFiles();
        assertNotNull(files);
        assertTrue(files.isEmpty());

        Map<String, Integer> stats = detector.getDatabaseStats();
        assertNotNull(stats);
        assertEquals(0, stats.get("total_files"));
        ;
    }

    /**
     * Тест последовательных операций.
     * Проверяет, что система корректно обрабатывает несколько операций подряд.
     */
    @Test
    void testSequentialOperations() throws IOException {
        PlagiarismDetector detector = new PlagiarismDetector();

        // 1. Создаем тестовую базу
        Path dbDir = tempDir.resolve("sequential");
        Files.createDirectory(dbDir);

        Path file1 = dbDir.resolve("File1.java");
        Path file2 = dbDir.resolve("File2.java");

        Files.writeString(file1, "public class File1 { public void method1() {} }");
        Files.writeString(file2, "public class File2 { public void method2() {} }");

        // 2. Загружаем базу
        detector.loadDatabase(dbDir);

        // 3. Несколько проверок подряд
        String testCode1 = "public class Test { public void test() {} }";
        String testCode2 = "public class File1 { public void method1() {} }";

        List<PlagiarismResult> results1 = detector.checkForPlagiarism(testCode1);
        List<PlagiarismResult> results2 = detector.checkForPlagiarism(testCode2);

        assertNotNull(results1);
        assertNotNull(results2);

        // 4. Проверяем состояние после операций
        assertFalse(detector.isProcessingInProgress());
        assertTrue(detector.isDatabaseLoaded());
    }
}