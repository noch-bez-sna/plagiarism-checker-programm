package com.plagiarism.checker.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Утилитарный класс для работы с файлами и директориями.
 * Предоставляет методы для поиска Java файлов, чтения содержимого
 * и других файловых операций, необходимых для детектирования плагиата.
 * <p>
 * Все методы являются потокобезопасными и содержат
 * подробную обработку ошибок с логированием.
 */
public class FileUtils {
    private static final Logger logger = LogManager.getLogger(FileUtils.class);

    /**
     * Рекурсивно находит все Java файлы в указанной директории.
     *
     * <p>Метод выполняет следующие проверки:
     * <ol>
     *   <li>Валидация входных параметров</li>
     *   <li>Проверка существования директории</li>
     *   <li>Проверка прав доступа</li>
     *   <li>Рекурсивный обход с обработкой ошибок доступа</li>
     *   <li>Фильтрация только валидных Java файлов</li>
     * </ol>
     *
     * @param directory директория для поиска Java файлов
     * @return список путей к найденным Java файлам
     * @throws IllegalArgumentException если directory равен null
     * @throws NoSuchFileException если директория не существует
     * @throws NotDirectoryException если путь не является директорией
     * @throws AccessDeniedException если нет прав доступа к директории
     * @throws IOException если возникает ошибка ввода-вывода при обходе директории
     */
    public static List<Path> findJavaFiles(Path directory) throws IOException {
        logger.info("Searching for Java files in: {}", directory);

        // Проверяем что директория не null
        if (directory == null) {
            logger.error("Null passed instead of directory path");
            throw new IllegalArgumentException("Directory path cannot be null");
        }

        // Проверяем существование директории
        if (!Files.exists(directory)) {
            logger.error("Directory does not exist: {}", directory);
            throw new NoSuchFileException("Directory does not exist: " + directory);
        }

        // Проверяем что это действительно директория
        if (!Files.isDirectory(directory)) {
            logger.error("Path is not a directory: {}", directory);
            throw new NotDirectoryException("Path is not a directory: " + directory);
        }

        // Проверяем права доступа
        if (!Files.isReadable(directory)) {
            logger.error("No read permission for directory: {}", directory);
            throw new AccessDeniedException("No read permission for directory: " + directory);
        }

        List<Path> javaFiles = new ArrayList<>();

        try {
            // Используем walkFileTree для лучшей обработки ошибок
            Files.walkFileTree(directory, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                    try {
                        if (isValidJavaFile(file)) {
                            javaFiles.add(file);
                        }
                        return FileVisitResult.CONTINUE;
                    } catch (Exception e) {
                        logger.warn("Error processing file {}: {}", file, e.getMessage());
                        return FileVisitResult.CONTINUE; // Продолжаем обход других файлов
                    }
                }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc) {
                    logger.warn("Failed to access file {}: {}", file, exc.getMessage());
                    return FileVisitResult.CONTINUE; // Продолжаем обход
                }

                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    // Проверяем доступ к поддиректориям
                    if (!Files.isReadable(dir)) {
                        logger.warn("No read permission for directory: {}", dir);
                        return FileVisitResult.SKIP_SUBTREE; // Пропускаем эту поддиректорию
                    }
                    return FileVisitResult.CONTINUE;
                }
            });

            logger.info("Found {} Java files in {}", javaFiles.size(), directory);
            return javaFiles;

        } catch (SecurityException e) {
            logger.error("Security error traversing directory {}: {}", directory, e.getMessage());
            throw new IOException("Security error accessing directory: " + directory, e);
        } catch (IOException e) {
            logger.error("I/O error traversing directory {}: {}", directory, e.getMessage());
            throw new IOException("Error traversing directory: " + directory, e);
        } catch (Exception e) {
            logger.error("Unexpected error finding Java files in {}: {}", directory, e.getMessage());
            throw new IOException("Unexpected error finding files: " + e.getMessage(), e);
        }
    }

    /**
     * Читает содержимое файла в строку.
     *
     * <p>Метод выполняет следующие проверки:
     * <ol>
     *   <li>Валидация пути к файлу</li>
     *   <li>Проверка существования файла</li>
     *   <li>Проверка что это файл, а не директория</li>
     *   <li>Проверка прав доступа на чтение</li>
     *   <li>Проверка размера файла (максимум 10 МБ)</li>
     * </ol>
     *
     * @param filePath путь к файлу для чтения
     * @return содержимое файла в виде строки
     * @throws IllegalArgumentException если filePath равен null
     * @throws NoSuchFileException если файл не существует
     * @throws IOException если путь не является файлом или файл слишком большой
     * @throws AccessDeniedException если нет прав доступа для чтения файла
     * @throws OutOfMemoryError если недостаточно памяти для чтения файла
     */
    public static String readFile(Path filePath) throws IOException {
        logger.debug("Reading file: {}", filePath);

        // Проверяем что путь не null
        if (filePath == null) {
            logger.error("Null passed instead of file path");
            throw new IllegalArgumentException("File path cannot be null");
        }

        // Проверяем существование файла
        if (!Files.exists(filePath)) {
            logger.error("File does not exist: {}", filePath);
            throw new NoSuchFileException("File does not exist: " + filePath);
        }

        // Проверяем что это файл, а не директория
        if (!Files.isRegularFile(filePath)) {
            logger.error("Path is not a file: {}", filePath);
            throw new IOException("Path is not a file: " + filePath);
        }

        // Проверяем права доступа
        if (!Files.isReadable(filePath)) {
            logger.error("No read permission for file: {}", filePath);
            throw new AccessDeniedException("No read permission for file: " + filePath);
        }

        try {
            // Проверяем размер файла (защита от слишком больших файлов)
            long fileSize = Files.size(filePath);
            if (fileSize > 10 * 1024 * 1024) { // 10 MB лимит
                logger.error("File too large ({} bytes): {}", fileSize, filePath);
                throw new IOException("File too large (" + fileSize + " bytes). Maximum size: 10 MB");
            }

            // Читаем файл
            String content = Files.readString(filePath);
            logger.debug("File {} successfully read ({} bytes)", filePath, content.length());
            return content;

        } catch (SecurityException e) {
            logger.error("Security error reading file {}: {}", filePath, e.getMessage());
            throw new IOException("Security error reading file: " + filePath, e);
        } catch (IOException e) {
            logger.error("I/O error reading file {}: {}", filePath, e.getMessage());
            throw new IOException("Error reading file: " + filePath, e);
        } catch (OutOfMemoryError e) {
            logger.error("Insufficient memory to read file {}: {}", filePath, e.getMessage());
            throw new IOException("Insufficient memory to read file: " + filePath, e);
        } catch (Exception e) {
            logger.error("Unexpected error reading file {}: {}", filePath, e.getMessage());
            throw new IOException("Unexpected error reading file: " + e.getMessage(), e);
        }
    }

    /**
     * Проверяет, является ли файл валидным Java файлом для обработки.
     *
     * <p>Критерии валидности:
     * <ol>
     *   <li>Файл должен быть обычным файлом (не директорией, символьной ссылкой)</li>
     *   <li>Имя файла должно заканчиваться на .java (регистронезависимо)</li>
     *   <li>Файл не должен быть скрытым (не начинаться с точки)</li>
     *   <li>Файл не должен быть пустым</li>
     *   <li>Должны быть права на чтение файла</li>
     * </ol>
     *
     * @param file путь к файлу для проверки
     * @return true если файл удовлетворяет всем критериям, false в противном случае
     */
    static boolean isValidJavaFile(Path file) {
        try {
            // Проверяем что это обычный файл
            if (!Files.isRegularFile(file)) {
                return false;
            }

            // Проверяем расширение .java
            String fileName = file.getFileName().toString();
            if (!fileName.toLowerCase().endsWith(".java")) {
                return false;
            }

            // Игнорируем скрытые файлы (начинающиеся с точки)
            if (fileName.startsWith(".")) {
                return false;
            }

            // Проверяем что файл не пустой
            if (Files.size(file) == 0) {
                return false;
            }

            // Проверяем права на чтение
            if (!Files.isReadable(file)) {
                logger.warn("File not readable: {}", file);
                return false;
            }

            return true;

        } catch (IOException e) {
            logger.warn("Error checking file {}: {}", file, e.getMessage());
            return false;
        } catch (Exception e) {
            logger.warn("Unexpected error checking file {}: {}", file, e.getMessage());
            return false;
        }
    }

    /**
     * Создает директорию и все необходимые родительские директории, если они не существуют.
     *
     * @param directory путь к директории для создания
     * @throws IllegalArgumentException если directory равен null
     * @throws IOException если не удалось создать директорию
     * @throws SecurityException если нет прав на создание директории
     */
    public static void createDirectoryIfNotExists(Path directory) throws IOException {
        if (directory == null) {
            throw new IllegalArgumentException("Directory path cannot be null");
        }

        if (!Files.exists(directory)) {
            try {
                Files.createDirectories(directory);
                logger.info("Created directory: {}", directory);
            } catch (IOException e) {
                logger.error("Failed to create directory {}: {}", directory, e.getMessage());
                throw new IOException("Failed to create directory: " + directory, e);
            } catch (SecurityException e) {
                logger.error("No permission to create directory {}: {}", directory, e.getMessage());
                throw new IOException("No permission to create directory: " + directory, e);
            }
        }
    }

    /**
     * Безопасно удаляет файл если он существует.
     *
     * <p>Метод подавляет исключения и возвращает статус операции.
     * Используется для очистки временных файлов.
     *
     * @param filePath путь к файлу для удаления
     * @return true если файл был удален или не существовал, false если произошла ошибка
     */
    public static boolean deleteFileIfExists(Path filePath) {
        if (filePath == null) {
            return false;
        }

        try {
            if (Files.exists(filePath)) {
                Files.delete(filePath);
                logger.debug("File deleted: {}", filePath);
                return true;
            }
            return false;
        } catch (IOException e) {
            logger.warn("Failed to delete file {}: {}", filePath, e.getMessage());
            return false;
        } catch (SecurityException e) {
            logger.warn("No permission to delete file {}: {}", filePath, e.getMessage());
            return false;
        }
    }

    /**
     * Проверяет, существует ли файл и доступен ли он для чтения.
     *
     * @param filePath путь к файлу для проверки
     * @return true если файл существует, является обычным файлом и доступен для чтения
     */
    public static boolean isReadableFile(Path filePath) {
        if (filePath == null) {
            return false;
        }

        try {
            return Files.exists(filePath)
                    && Files.isRegularFile(filePath)
                    && Files.isReadable(filePath);
        } catch (SecurityException e) {
            logger.warn("Security error checking file {}: {}", filePath, e.getMessage());
            return false;
        } catch (Exception e) {
            logger.warn("Error checking file {}: {}", filePath, e.getMessage());
            return false;
        }
    }

    /**
     * Получает размер файла в байтах.
     *
     * @param filePath путь к файлу
     * @return размер файла в байтах или -1 если произошла ошибка
     */
    public static long getFileSize(Path filePath) {
        if (filePath == null) {
            return -1;
        }

        try {
            return Files.size(filePath);
        } catch (IOException e) {
            logger.warn("Error getting size of file {}: {}", filePath, e.getMessage());
            return -1;
        } catch (SecurityException e) {
            logger.warn("No permission to access file {}: {}", filePath, e.getMessage());
            return -1;
        } catch (Exception e) {
            logger.warn("Unexpected error getting file size {}: {}", filePath, e.getMessage());
            return -1;
        }
    }
}