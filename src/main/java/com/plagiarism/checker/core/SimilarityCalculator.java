package com.plagiarism.checker.core;

import com.plagiarism.checker.model.CodeFragment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Калькулятор для расчета схожести между фрагментами кода.
 * Использует различные алгоритмы сравнения для определения степени плагиата.
 */
public class SimilarityCalculator {
    private static final Logger logger = LogManager.getLogger(SimilarityCalculator.class);

    /** Порог схожести для учета фрагмента как совпадающего */
    private static final double SIMILARITY_THRESHOLD = 5.0;

    /** Порог токенного сходства */
    private static final double TOKEN_SIMILARITY_THRESHOLD = 80.0;

    /** Минимальная длина фрагмента для сравнения содержания */
    private static final int MIN_LENGTH_FOR_CONTAINMENT = 50;

    /** Минимальное соотношение длины для сравнения содержания */
    private static final double CONTAINMENT_RATIO = 0.7;

    /** Бонус за полное совпадение кода */
    private static final double FULL_CODE_MATCH_BONUS = 20.0;

    /** Множитель для усиления результата схожести */
    private static final double SIMILARITY_BOOST_FACTOR = 1.5;

    /** Паттерны алгоритмического сравнения */
    private static final String[] ALGORITHM_PATTERNS = {
            "for ( VAR = NUM ; VAR < VAR ; VAR ++ )",
            "for ( VAR = VAR ; VAR < VAR ; VAR ++ )",
            "if ( VAR > VAR )",
            "if ( VAR < VAR )",
            "if ( VAR == VAR )",
            "while ( VAR < VAR )",
            "return VAR ;",
            "VAR = VAR + VAR ;",
            "VAR = VAR * VAR ;"
    };

    /** Конструкции для алгоритмического сравнения */
    private static final String[] ALGORITHM_CONSTRUCTIONS = {
            "for (", "if (", "while (", "return", "VAR = VAR", "{", "}"
    };

    /**
     * Конструктор инициализирует калькулятор схожести.
     */
    public SimilarityCalculator() {
        logger.debug("SimilarityCalculator initialized");
    }

    /**
     * Сравнивает фрагменты проверяемого кода с фрагментами базы данных.
     *
     * @param checkFragments фрагменты из проверяемого кода
     * @param databaseEntries записи базы данных (файл → фрагменты)
     * @return отсортированный список результатов сравнения
     */
    public List<Map<String, Object>> calculateSimilarities(
            List<CodeFragment> checkFragments,
            Map<String, List<CodeFragment>> databaseEntries) {

        logger.info("Calculating similarities for {} fragments against {} files",
                checkFragments.size(), databaseEntries.size());

        return databaseEntries.entrySet().parallelStream()
                .map(entry -> calculateFileSimilarity(entry.getKey(), entry.getValue(), checkFragments))
                .filter(result -> result != null)
                .sorted((a, b) -> Double.compare(
                        (double) b.get("similarity"),
                        (double) a.get("similarity")))
                .collect(Collectors.toList());
    }

    /**
     * Рассчитывает схожесть для одного файла базы данных.
     */
    private Map<String, Object> calculateFileSimilarity(
            String fileName,
            List<CodeFragment> dbFragments,
            List<CodeFragment> checkFragments) {

        if (dbFragments == null || dbFragments.isEmpty()) {
            return null;
        }

        int matchesCount = 0;
        Map<CodeFragment, List<String>> matchedFragments = new HashMap<>();
        Set<String> matchedContentSamples = new HashSet<>();

        // Сравниваем фрагменты
        for (CodeFragment dbFragment : dbFragments) {
            for (CodeFragment checkFragment : checkFragments) {
                if (isContentSimilar(dbFragment.getNormalizedContent(),
                        checkFragment.getNormalizedContent(), fileName)) {
                    matchesCount++;
                    matchedFragments.put(checkFragment, Collections.singletonList(fileName));
                    matchedContentSamples.add(truncateText(dbFragment.getNormalizedContent(), 50));
                    break;
                }
            }
        }

        // Рассчитываем процент схожести
        double similarity = calculateSimilarityPercentage(matchesCount, dbFragments.size());

        // Усиливаем результат при полном совпадении
        if (hasFullCodeMatch(dbFragments, checkFragments)) {
            similarity = Math.min(100, similarity + FULL_CODE_MATCH_BONUS);
        }

        // Применяем логарифмическое масштабирование
        similarity = Math.min(100, similarity * SIMILARITY_BOOST_FACTOR);

        if (similarity > SIMILARITY_THRESHOLD) {
            logger.debug("File {}: similarity {}% (matches: {})",
                    fileName, String.format("%.2f", similarity), matchesCount);

            Map<String, Object> result = new HashMap<>();
            result.put("fileName", fileName);
            result.put("similarity", similarity);
            result.put("matchesCount", matchesCount);
            result.put("matchedFragments", matchedFragments);
            return result;
        }

        return null;
    }

    /**
     * Рассчитывает процент схожести на основе количества совпадений.
     */
    private double calculateSimilarityPercentage(int matchesCount, int totalFragments) {
        if (totalFragments == 0) {
            return 0.0;
        }
        return (double) matchesCount / totalFragments * 100;
    }

    /**
     * Определяет схожесть двух фрагментов кода.
     */
    public boolean isContentSimilar(String dbContent, String checkContent, String fileName) {
        if (dbContent == null || checkContent == null) {
            return false;
        }

        // 1. Точное совпадение
        if (dbContent.equals(checkContent)) {
            logMatch("Exact match", fileName, dbContent);
            return true;
        }

        // 2. Содержание одного фрагмента в другом
        if (isContentContained(dbContent, checkContent, fileName)) {
            return true;
        }

        // 3. Алгоритмическая схожесть
        if (isAlgorithmSimilar(dbContent, checkContent)) {
            logMatch("Algorithm match", fileName, dbContent);
            return true;
        }

        // 4. Токенное сходство (для коротких фрагментов)
        if (dbContent.length() < 100 && checkContent.length() < 100) {
            double tokenSimilarity = calculateTokenSimilarity(dbContent, checkContent);
            if (tokenSimilarity > TOKEN_SIMILARITY_THRESHOLD) {
                logMatch(String.format("Token match %.1f%%", tokenSimilarity), fileName, dbContent);
                return true;
            }
        }

        return false;
    }

    /**
     * Проверяет, содержится ли один фрагмент в другом.
     */
    private boolean isContentContained(String str1, String str2, String fileName) {
        if (str1.length() < MIN_LENGTH_FOR_CONTAINMENT || str2.length() < MIN_LENGTH_FOR_CONTAINMENT) {
            return false;
        }

        if (str1.contains(str2) && str2.length() > str1.length() * CONTAINMENT_RATIO) {
            logMatch("Containing match", fileName, str1);
            return true;
        }

        if (str2.contains(str1) && str1.length() > str2.length() * CONTAINMENT_RATIO) {
            return true;
        }

        return false;
    }

    /**
     * Логирует информацию о совпадении.
     */
    private void logMatch(String matchType, String fileName, String content) {
        if (logger.isTraceEnabled()) {
            logger.trace("{} in file {}: {}",
                    matchType, fileName, truncateText(content, 50));
        }
    }

    /**
     * Проверяет алгоритмическую схожесть двух строк кода.
     */
    public boolean isAlgorithmSimilar(String str1, String str2) {
        try {
            // Проверка паттернов
            for (String pattern : ALGORITHM_PATTERNS) {
                if (str1.contains(pattern) && str2.contains(pattern)) {
                    return true;
                }
            }

            // Подсчет общих конструкций
            int commonConstructions = countCommonConstructions(str1, str2);
            return commonConstructions >= 3;

        } catch (Exception e) {
            logger.error("Error in isAlgorithmSimilar: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Подсчитывает общие алгоритмические конструкции.
     */
    private int countCommonConstructions(String str1, String str2) {
        int count = 0;
        for (String constr : ALGORITHM_CONSTRUCTIONS) {
            if (str1.contains(constr) && str2.contains(constr)) {
                count++;
            }
        }
        return count;
    }

    /**
     * Вычисляет коэффициент сходства на основе общих токенов.
     */
    public double calculateTokenSimilarity(String str1, String str2) {
        try {
            if (str1 == null || str2 == null) {
                return 0.0;
            }

            String[] tokens1 = str1.split("\\s+");
            String[] tokens2 = str2.split("\\s+");

            Set<String> set1 = new HashSet<>(Arrays.asList(tokens1));
            Set<String> set2 = new HashSet<>(Arrays.asList(tokens2));

            // Пересечение
            Set<String> intersection = new HashSet<>(set1);
            intersection.retainAll(set2);

            // Объединение
            Set<String> union = new HashSet<>(set1);
            union.addAll(set2);

            if (union.isEmpty()) {
                return 0.0;
            }

            return (double) intersection.size() / union.size() * 100;

        } catch (Exception e) {
            logger.error("Error in calculateTokenSimilarity: {}", e.getMessage());
            return 0.0;
        }
    }

    /**
     * Проверяет наличие совпадения полного кода или алгоритма.
     */
    public boolean hasFullCodeMatch(List<CodeFragment> dbFragments, List<CodeFragment> checkFragments) {
        try {
            for (CodeFragment dbFragment : dbFragments) {
                if (isFullCodeOrAlgorithm(dbFragment)) {
                    for (CodeFragment checkFragment : checkFragments) {
                        if (isFullCodeOrAlgorithm(checkFragment)) {
                            double similarity = calculateTokenSimilarity(
                                    dbFragment.getNormalizedContent(),
                                    checkFragment.getNormalizedContent());
                            return similarity > 60.0;
                        }
                    }
                }
            }
            return false;
        } catch (Exception e) {
            logger.error("Error in hasFullCodeMatch: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Проверяет, является ли фрагмент полным кодом или алгоритмом.
     */
    private boolean isFullCodeOrAlgorithm(CodeFragment fragment) {
        return "FULL_CODE".equals(fragment.getOriginalContent()) ||
                "ALGORITHM".equals(fragment.getOriginalContent());
    }

    /**
     * Обрезает текст до указанной длины.
     */
    private String truncateText(String text, int maxLength) {
        if (text == null || text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength) + "...";
    }
}