package com.plagiarism.checker.core;

import com.plagiarism.checker.model.CodeFragment;
import com.plagiarism.checker.utils.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Менеджер базы данных для хранения и управления фрагментами кода.
 * Отвечает за загрузку, валидацию и очистку базы данных.
 */
public class DatabaseManager {
    private static final Logger logger = LogManager.getLogger(DatabaseManager.class);

    /** База данных файлов и их фрагментов кода (файл → список фрагментов) */
    private final Map<String, List<CodeFragment>> codeDatabase;

    /** Флаг, указывающий что база данных успешно загружена */
    private volatile boolean databaseLoaded;

    /**
     * Конструктор инициализирует менеджер базы данных.
     */
    public DatabaseManager() {
        this.codeDatabase = new ConcurrentHashMap<>();
        this.databaseLoaded = false;
        logger.debug("DatabaseManager initialized");
    }

    /**
     * Загружает базу данных Java файлов из указанной директории.
     *
     * @param databasePath путь к директории с Java файлами
     * @param fragmentExtractor экстрактор для извлечения фрагментов
     * @throws IOException если возникает ошибка чтения файлов
     */
    public void loadDatabase(Path databasePath, FragmentExtractor fragmentExtractor) throws IOException {
        logger.info("=== LOADING DATABASE FROM: {} ===", databasePath);

        validateDatabasePath(databasePath);
        codeDatabase.clear();
        databaseLoaded = false;

        AtomicInteger fileCount = new AtomicInteger(0);
        AtomicInteger errorCount = new AtomicInteger(0);

        List<Path> javaFiles = FileUtils.findJavaFiles(databasePath);
        logger.info("Found {} Java files to process", javaFiles.size());

        if (javaFiles.isEmpty()) {
            throw new IOException("No Java files found in the specified directory: " + databasePath);
        }

        javaFiles.parallelStream().forEach(file -> {
            try {
                if (!isValidJavaFile(file)) {
                    logger.debug("Skipping invalid file: {}", file);
                    return;
                }

                String content = FileUtils.readFile(file);
                List<CodeFragment> fragments = fragmentExtractor.extractFragments(content);

                if (!fragments.isEmpty()) {
                    codeDatabase.put(file.toString(), fragments);
                    fileCount.incrementAndGet();
                    logger.trace("Loaded file: {} (fragments: {})", file, fragments.size());
                }

            } catch (IOException e) {
                logger.warn("Failed to process file {}: {}", file, e.getMessage());
                errorCount.incrementAndGet();
            } catch (Exception e) {
                logger.error("Unexpected error processing file {}: {}", file, e.getMessage(), e);
                errorCount.incrementAndGet();
            }
        });

        if (fileCount.get() == 0) {
            throw new IOException("Failed to load any files into the database");
        }

        databaseLoaded = true;
        logger.info("=== DATABASE SUCCESSFULLY LOADED ===");
        logger.info("Files loaded: {}, Errors: {}", fileCount.get(), errorCount.get());
        logger.info("Total fragments in database: {}", getTotalFragmentsCount());
    }

    /**
     * Валидирует путь к базе данных.
     */
    private void validateDatabasePath(Path databasePath) throws IOException {
        if (databasePath == null) {
            throw new IllegalArgumentException("Database path cannot be null");
        }

        if (!Files.exists(databasePath)) {
            throw new NoSuchFileException("Directory does not exist: " + databasePath);
        }

        if (!Files.isDirectory(databasePath)) {
            throw new NotDirectoryException("Path is not a directory: " + databasePath);
        }

        if (!Files.isReadable(databasePath)) {
            throw new AccessDeniedException("No read permission for directory: " + databasePath);
        }
    }

    /**
     * Проверяет, является ли файл валидным Java файлом для обработки.
     */
    private boolean isValidJavaFile(Path file) {
        try {
            if (!Files.isRegularFile(file)) {
                return false;
            }

            String fileName = file.getFileName().toString();
            if (!fileName.toLowerCase().endsWith(".java")) {
                return false;
            }

            if (fileName.startsWith(".")) {
                return false;
            }

            if (!Files.isReadable(file)) {
                logger.warn("File not readable: {}", file);
                return false;
            }

            long size = Files.size(file);
            if (size > 1024 * 1024) { // 1 MB
                logger.warn("File too large ({} bytes), skipping: {}", size, file);
                return false;
            }

            return true;

        } catch (IOException e) {
            logger.warn("Failed to check file {}: {}", file, e.getMessage());
            return false;
        }
    }

    /**
     * Возвращает фрагменты кода для указанного файла.
     */
    public List<CodeFragment> getFragmentsForFile(String fileName) {
        return codeDatabase.getOrDefault(fileName, new ArrayList<>());
    }

    /**
     * Возвращает все записи базы данных.
     */
    public Map<String, List<CodeFragment>> getAllDatabaseEntries() {
        return new ConcurrentHashMap<>(codeDatabase);
    }

    /**
     * Возвращает список файлов в базе данных.
     */
    public List<String> getDatabaseFiles() {
        if (!databaseLoaded) {
            logger.warn("Attempting to get file list from unloaded database");
            return new ArrayList<>();
        }
        return new ArrayList<>(codeDatabase.keySet());
    }

    /**
     * Возвращает количество файлов в базе данных.
     */
    public int getFileCount() {
        return codeDatabase.size();
    }

    /**
     * Возвращает общее количество фрагментов во всех файлах.
     */
    public int getTotalFragmentsCount() {
        return codeDatabase.values().stream()
                .mapToInt(List::size)
                .sum();
    }

    /**
     * Проверяет, загружена ли база данных.
     */
    public boolean isLoaded() {
        return databaseLoaded && !codeDatabase.isEmpty();
    }

    /**
     * Очищает базу данных.
     */
    public void clear() {
        logger.info("Clearing database");
        codeDatabase.clear();
        databaseLoaded = false;
        logger.info("Database cleared");
    }

    /**
     * Возвращает статистику базы данных.
     */
    public Map<String, Integer> getStatistics() {
        Map<String, Integer> stats = new ConcurrentHashMap<>();
        stats.put("total_files", codeDatabase.size());

        int totalFragments = getTotalFragmentsCount();
        stats.put("total_fragments", totalFragments);

        double avgFragments = codeDatabase.isEmpty() ? 0 : (double) totalFragments / codeDatabase.size();
        stats.put("avg_fragments_per_file", (int) avgFragments);

        return stats;
    }
}