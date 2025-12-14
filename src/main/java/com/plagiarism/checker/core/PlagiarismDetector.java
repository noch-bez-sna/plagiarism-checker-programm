package com.plagiarism.checker.core;

import com.plagiarism.checker.model.CodeFragment;
import com.plagiarism.checker.model.PlagiarismResult;
import com.plagiarism.checker.utils.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Основной фасадный класс для детектирования плагиата в Java коде.
 * Координирует работу DatabaseManager, FragmentExtractor и SimilarityCalculator.
 */
public class PlagiarismDetector {
    private static final Logger logger = LogManager.getLogger(PlagiarismDetector.class);

    /** Менеджер базы данных */
    private final DatabaseManager databaseManager;

    /** Экстрактор фрагментов */
    private final FragmentExtractor fragmentExtractor;

    /** Калькулятор схожести */
    private final SimilarityCalculator similarityCalculator;

    /** Флаг выполнения операции */
    private volatile boolean processingInProgress;

    /**
     * Конструктор инициализирует детектор плагиата.
     */
    public PlagiarismDetector() {
        this.databaseManager = new DatabaseManager();
        this.fragmentExtractor = new FragmentExtractor();
        this.similarityCalculator = new SimilarityCalculator();
        this.processingInProgress = false;
        logger.debug("PlagiarismDetector initialized");
    }

    /**
     * Загружает базу данных Java файлов из указанной директории.
     *
     * @param databasePath путь к директории с Java файлами
     * @throws IOException если возникает ошибка чтения файлов или директория не существует
     * @throws IllegalStateException если уже выполняется другая операция
     */
    public void loadDatabase(Path databasePath) throws IOException {
        if (processingInProgress) {
            throw new IllegalStateException("Operation already in progress. Please wait for completion.");
        }

        processingInProgress = true;
        try {
            databaseManager.loadDatabase(databasePath, fragmentExtractor);
            logger.info("=== DATABASE LOADING COMPLETED ===");
        } finally {
            processingInProgress = false;
        }
    }

    /**
     * Проверяет код на наличие плагиата в загруженной базе данных.
     *
     * @param codeToCheck код для проверки на плагиат
     * @return список результатов проверки с файлами и процентами схожести
     */
    public List<PlagiarismResult> checkForPlagiarism(String codeToCheck) {
        if (processingInProgress) {
            logger.error("Operation already in progress");
            return Collections.emptyList();
        }

        if (codeToCheck == null || codeToCheck.trim().isEmpty()) {
            logger.warn("Empty code provided for checking");
            return Collections.emptyList();
        }

        if (!databaseManager.isLoaded()) {
            logger.warn("Database not loaded or empty");
            return Collections.emptyList();
        }

        processingInProgress = true;
        logger.info("=== STARTING PLAGIARISM CHECK ===");

        try {
            // Извлекаем фрагменты из проверяемого кода
            List<CodeFragment> checkFragments = fragmentExtractor.extractFragments(codeToCheck);
            if (checkFragments.isEmpty()) {
                logger.warn("No fragments extracted from checked code");
                return Collections.emptyList();
            }

            logger.info("Checked code contains {} fragments", checkFragments.size());

            // Получаем все записи базы данных
            Map<String, List<CodeFragment>> databaseEntries = databaseManager.getAllDatabaseEntries();
            logger.info("Comparing against {} database files", databaseEntries.size());

            // Рассчитываем схожести
            List<Map<String, Object>> similarityResults =
                    similarityCalculator.calculateSimilarities(checkFragments, databaseEntries);

            // Преобразуем в PlagiarismResult
            List<PlagiarismResult> results = similarityResults.stream()
                    .map(this::createPlagiarismResult)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            logger.info("=== CHECK COMPLETED ===");
            logger.info("Found {} files with suspected plagiarism", results.size());

            return results;

        } catch (OutOfMemoryError e) {
            logger.error("Insufficient memory for plagiarism check");
            return Collections.emptyList();
        } catch (Exception e) {
            logger.error("Critical error during plagiarism check: {}", e.getMessage(), e);
            return Collections.emptyList();
        } finally {
            processingInProgress = false;
        }
    }

    /**
     * Создает объект PlagiarismResult из результата сравнения.
     */
    @SuppressWarnings("unchecked")
    private PlagiarismResult createPlagiarismResult(Map<String, Object> resultMap) {
        try {
            String fileName = (String) resultMap.get("fileName");
            double similarity = (double) resultMap.get("similarity");
            Map<CodeFragment, List<String>> matchedFragments =
                    (Map<CodeFragment, List<String>>) resultMap.get("matchedFragments");

            return new PlagiarismResult(fileName, similarity, matchedFragments);
        } catch (Exception e) {
            logger.error("Error creating PlagiarismResult: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Проверяет файл на наличие плагиата.
     *
     * @param filePath путь к файлу для проверки
     * @return список результатов проверки на плагиат
     * @throws IOException если файл не существует, не читается или не является Java файлом
     */
    public List<PlagiarismResult> checkFileForPlagiarism(Path filePath) throws IOException {
        logger.info("Checking file for plagiarism: {}", filePath);

        validateFilePath(filePath);
        String content = FileUtils.readFile(filePath);
        return checkForPlagiarism(content);
    }

    /**
     * Валидирует путь к файлу для проверки.
     */
    private void validateFilePath(Path filePath) throws IOException {
        if (filePath == null) {
            throw new IllegalArgumentException("File path cannot be null");
        }

        if (!filePath.toString().toLowerCase().endsWith(".java")) {
            throw new IOException("File must have .java extension: " + filePath);
        }
    }

    /**
     * Возвращает список файлов в загруженной базе данных.
     */
    public List<String> getDatabaseFiles() {
        return databaseManager.getDatabaseFiles();
    }

    /**
     * Возвращает статистику по загруженной базе данных.
     */
    public Map<String, Integer> getDatabaseStats() {
        return databaseManager.getStatistics();
    }

    /**
     * Очищает базу данных.
     */
    public void clearDatabase() {
        databaseManager.clear();
    }

    /**
     * Проверяет, загружена ли база данных.
     */
    public boolean isDatabaseLoaded() {
        return databaseManager.isLoaded();
    }

    /**
     * Проверяет, выполняется ли в данный момент какая-либо операция.
     */
    public boolean isProcessingInProgress() {
        return processingInProgress;
    }

    /**
     * Возвращает количество файлов в базе данных.
     */
    public int getDatabaseFileCount() {
        return databaseManager.getFileCount();
    }

    /**
     * Возвращает общее количество фрагментов во всех файлах базы данных.
     */
    public int getTotalFragmentsCount() {
        return databaseManager.getTotalFragmentsCount();
    }

    /**
     * Возвращает менеджер базы данных.
     */
    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    /**
     * Возвращает экстрактор фрагментов.
     */
    public FragmentExtractor getFragmentExtractor() {
        return fragmentExtractor;
    }

    /**
     * Возвращает калькулятор схожести.
     */
    public SimilarityCalculator getSimilarityCalculator() {
        return similarityCalculator;
    }
}