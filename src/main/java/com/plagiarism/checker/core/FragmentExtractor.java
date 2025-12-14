package com.plagiarism.checker.core;

import com.plagiarism.checker.model.CodeFragment;
import com.plagiarism.checker.utils.CodeNormalizer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Класс для извлечения и нормализации фрагментов кода из исходного текста.
 * Создает различные типы фрагментов для более точного сравнения.
 */
public class FragmentExtractor {
    private static final Logger logger = LogManager.getLogger(FragmentExtractor.class);

    /** Нормализатор кода для приведения к единому формату */
    private final CodeNormalizer normalizer;

    /**
     * Конструктор инициализирует экстрактор фрагментов.
     */
    public FragmentExtractor() {
        this.normalizer = new CodeNormalizer();
        logger.debug("FragmentExtractor initialized");
    }

    /**
     * Извлекает фрагменты кода из строки с исходным кодом.
     *
     * <p>Создает несколько типов фрагментов:
     * <ol>
     *   <li>Полный нормализованный код</li>
     *   <li>Упрощенная версия для алгоритмического сравнения</li>
     *   <li>Отдельные строки (по точкам с запятой)</li>
     *   <li>Биграммы (пары последовательных строк)</li>
     *   <li>Триграммы (тройки последовательных строк)</li>
     *   <li>Специфические паттерны (циклы, условия, возвраты)</li>
     * </ol>
     *
     * @param code исходный код для извлечения фрагментов
     * @return список извлеченных фрагментов кода
     */
    public List<CodeFragment> extractFragments(String code) {
        logger.trace("Extracting fragments from code (length: {} characters)",
                code != null ? code.length() : 0);

        if (code == null || code.trim().isEmpty()) {
            logger.debug("Empty code for fragment extraction");
            return Collections.emptyList();
        }

        List<CodeFragment> fragments = new ArrayList<>();

        try {
            // 1. Полный нормализованный код
            String normalized = normalizer.normalize(code);
            if (normalized != null && !normalized.trim().isEmpty()) {
                fragments.add(new CodeFragment(normalized.trim(), "FULL_CODE", 0));
            }

            // 2. Алгоритмическая нормализация
            String algorithmNormalized = normalizeForAlgorithm(code);
            if (algorithmNormalized.isBlank() && !algorithmNormalized.trim().isEmpty() &&
                    algorithmNormalized.length() > 20) {
                fragments.add(new CodeFragment(algorithmNormalized.trim(), "ALGORITHM", 0));
            }

            // 3. Отдельные строки, биграммы, триграммы
            if (normalized != null) {
                extractLinesAndNGrams(normalized, fragments);
            }

            // 4. Специфические паттерны
            extractPatternFragments(code, fragments);

            logger.debug("Extracted {} fragments from code", fragments.size());
            return fragments;

        } catch (OutOfMemoryError e) {
            logger.error("Insufficient memory for fragment extraction");
            return Collections.emptyList();
        } catch (Exception e) {
            logger.error("Critical error extracting fragments: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    /**
     * Извлекает отдельные строки, биграммы и триграммы из нормализованного кода.
     */
    private void extractLinesAndNGrams(String normalizedCode, List<CodeFragment> fragments) {
        String[] lines = normalizedCode.split(";");

        // Отдельные строки
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.length() > 10) {
                fragments.add(new CodeFragment(line, line, i + 1));
            }
        }

        // Биграммы
        for (int i = 0; i < lines.length - 1; i++) {
            String line1 = lines[i].trim();
            String line2 = lines[i + 1].trim();
            if (!line1.isEmpty() && !line2.isEmpty() &&
                    line1.length() > 8 && line2.length() > 8) {
                String pair = line1 + " ; " + line2;
                fragments.add(new CodeFragment(pair, pair, i + 1));
            }
        }

        // Триграммы
        for (int i = 0; i < lines.length - 2; i++) {
            String line1 = lines[i].trim();
            String line2 = lines[i + 1].trim();
            String line3 = lines[i + 2].trim();
            if (!line1.isEmpty() && !line2.isEmpty() && !line3.isEmpty() &&
                    line1.length() > 5 && line2.length() > 5 && line3.length() > 5) {
                String triple = line1 + " ; " + line2 + " ; " + line3;
                fragments.add(new CodeFragment(triple, triple, i + 1));
            }
        }
    }

    /**
     * Нормализует код для алгоритмического сравнения.
     */
    private String normalizeForAlgorithm(String code) {
        try {
            if (code == null || code.trim().isEmpty()) {
                return "";
            }

            String normalized = code
                    .replaceAll("//.*", "")
                    .replaceAll("/\\*.*?\\*/", "")
                    .replaceAll("@\\w+", "")
                    .replaceAll("\"[^\"]*\"", "STR")
                    .replaceAll("\\b\\d+\\.?\\d*\\b", "NUM")
                    .replaceAll("\\b(int|String|boolean|double|float|char|byte|short|long|void)\\b", "")
                    .replaceAll("\\b(public|private|protected|static|final|class|interface|extends|implements)\\b", "")
                    .replaceAll("\\b([a-zA-Z_$][a-zA-Z0-9_$]*)\\b", "VAR")
                    .replaceAll("\\s+", " ")
                    .replaceAll("\\s*\\{\\s*", " { ")
                    .replaceAll("\\s*\\}\\s*", " } ")
                    .replaceAll("\\s*\\(\\s*", " ( ")
                    .replaceAll("\\s*\\)\\s*", " ) ")
                    .replaceAll("\\s*;\\s*", " ; ")
                    .trim();

            return normalized.replaceAll("\\s+", " ");

        } catch (Exception e) {
            logger.error("Algorithm normalization error: {}", e.getMessage());
            return "";
        }
    }

    /**
     * Извлекает специфические паттерны кода (циклы, условия и т.д.).
     */
    private void extractPatternFragments(String code, List<CodeFragment> fragments) {
        try {
            if (code == null || code.trim().isEmpty()) {
                return;
            }

            String[] lines = code.split("\n");
            for (int i = 0; i < lines.length; i++) {
                String line = lines[i].trim();

                // Циклы for
                if (line.contains("for (") && line.contains(";") && line.contains("++")) {
                    addPatternFragment(fragments, line, i);
                }

                // Условия if
                if (line.startsWith("if (") || line.contains(" if (")) {
                    addPatternFragment(fragments, line, i);
                }

                // Циклы while
                if (line.contains("while (")) {
                    addPatternFragment(fragments, line, i);
                }

                // Возвраты
                if (line.contains("return ") && !line.contains("//")) {
                    addPatternFragment(fragments, line, i);
                }
            }
        } catch (Exception e) {
            logger.error("Error in extractPatternFragments: {}", e.getMessage());
        }
    }

    /**
     * Добавляет нормализованный паттерн в список фрагментов.
     */
    private void addPatternFragment(List<CodeFragment> fragments, String line, int lineNumber) {
        String normalized = normalizeForPattern(line);
        if (!normalized.isEmpty()) {
            fragments.add(new CodeFragment(normalized, line, lineNumber + 1));
        }
    }

    /**
     * Нормализует строку кода для паттерн-сравнения.
     */
    private String normalizeForPattern(String line) {
        try {
            if (line == null || line.trim().isEmpty()) {
                return "";
            }

            return line
                    .replaceAll("//.*", "")
                    .replaceAll("\"[^\"]*\"", "STR")
                    .replaceAll("\\b\\d+\\.?\\d*\\b", "NUM")
                    .replaceAll("\\b([a-zA-Z_$][a-zA-Z0-9_$]*)\\b", "VAR")
                    .replaceAll("\\s+", " ")
                    .trim();
        } catch (Exception e) {
            logger.error("Error in normalizeForPattern: {}", e.getMessage());
            return "";
        }
    }

    /**
     * Возвращает нормализатор кода.
     */
    public CodeNormalizer getNormalizer() {
        return normalizer;
    }
}