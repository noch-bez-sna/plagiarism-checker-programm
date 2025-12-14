package com.plagiarism.checker.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Утилита для нормализации Java кода перед сравнением.
 * Выполняет приведение кода к единому формату для упрощения
 * детектирования плагиата.
 * <p>
 * Основные операции нормализации:
 * <ol>
 *   <li>Удаление комментариев (однострочных и многострочных)</li>
 *   <li>Удаление package и import statements</li>
 *   <li>Замена литералов (строк, чисел, символов) на общие метки</li>
 *   <li>Нормализация имен переменных и методов</li>
 *   <li>Унификация форматирования и пробелов</li>
 *   <li>Удаление избыточных ключевых слов</li>
 * </ol>
 * <p>
 * Класс обеспечивает обработку ошибок и безопасную работу
 * с большими объемами кода.
 */
public class CodeNormalizer {
    private static final Logger logger = LogManager.getLogger(CodeNormalizer.class);

    /** Максимальный размер кода для обработки (10 МБ) */
    private static final int MAX_CODE_SIZE = 10 * 1024 * 1024;

    /** Максимальная длина регулярного выражения для безопасной обработки */
    private static final int MAX_PATTERN_LENGTH = 1000;

    /**
     * Создает новый экземпляр нормализатора кода.
     * Инициализирует логгер и проверяет системные требования.
     */
    public CodeNormalizer() {
        logger.debug("Initializing CodeNormalizer");
    }

    /**
     * Нормализует Java код, приводя его к единому формату для сравнения.
     *
     * <p>Процесс нормализации включает:
     * <ol>
     *   <li>Проверку входных параметров и размера кода</li>
     *   <li>Удаление комментариев</li>
     *   <li>Удаление package и import statements</li>
     *   <li>Замену литералов на общие метки</li>
     *   <li>Нормализацию имен переменных и методов</li>
     *   <li>Унификацию форматирования</li>
     * </ol>
     *
     * @param javaCode исходный Java код для нормализации
     * @return нормализованная версия кода или пустая строка если код null
     * @throws IllegalArgumentException если код превышает максимальный размер
     * @throws OutOfMemoryError если недостаточно памяти для обработки
     */
    public String normalize(String javaCode) {
        try {
            // Проверка входных параметров
            if (javaCode == null) {
                logger.warn("Null code passed for normalization");
                return "";
            }

            // Проверка размера кода
            if (javaCode.length() > MAX_CODE_SIZE) {
                logger.error("Code too large for normalization: {} characters (max {})",
                        javaCode.length(), MAX_CODE_SIZE);
                throw new IllegalArgumentException(
                        String.format("Code too large: %d characters. Maximum size: %d characters",
                                javaCode.length(), MAX_CODE_SIZE));
            }

            // Проверка на пустой код
            String trimmedCode = javaCode.trim();
            if (trimmedCode.isEmpty()) {
                logger.debug("Empty code passed for normalization");
                return "";
            }

            logger.debug("Starting code normalization (length: {} characters)", trimmedCode.length());

            String result = trimmedCode;

            // 1. Удаляем комментарии
            result = removeComments(result);

            // 2. Удаляем package и import
            result = removePackageAndImports(result);

            // 3. Заменяем строковые литералы
            result = safeReplace(result, "\"[^\"]*\"", "\"STRING\"");

            // 4. Заменяем числовые литералы
            result = safeReplace(result, "\\b\\d+\\.?\\d*\\b", "NUMBER");

            // 5. Заменяем символьные литералы
            result = safeReplace(result, "'.'", "'CHAR'");

            // 6. Нормализуем имена переменных
            result = normalizeVariableNames(result);

            // 7. Нормализуем имена методов (кроме стандартных)
            result = normalizeMethodNames(result);

            // 8. Удаляем аннотации
            result = safeReplace(result, "@\\w+", "");

            // 9. Нормализуем пробелы и форматирование
            result = normalizeWhitespace(result);

            // 10. Удаляем лишние ключевые слова
            result = removeExcessKeywords(result);

            // Проверяем результат
            if (result == null) {
                logger.error("Normalization returned null");
                return trimmedCode; // Возвращаем оригинальный код без нормализации
            }

            String finalResult = result.trim();

            logger.debug("Code successfully normalized (length: {} characters)", finalResult.length());

            if (logger.isTraceEnabled()) {
                String preview = finalResult.length() > 200
                        ? finalResult.substring(0, 200) + "..."
                        : finalResult;
                logger.trace("Normalized code (first 200 characters): {}", preview);
            }

            return finalResult;

        } catch (IllegalArgumentException e) {
            // Перебрасываем проверенные исключения
            throw e;
        } catch (StackOverflowError e) {
            logger.error("Stack overflow during code normalization (possibly too complex regular expression)");
            return javaCode != null ? javaCode.trim() : "";
        } catch (OutOfMemoryError e) {
            logger.error("Insufficient memory for code normalization");
            throw e; // Перебрасываем дальше - это критическая ошибка
        } catch (Exception e) {
            logger.error("Unexpected error during code normalization: {}", e.getMessage(), e);
            return javaCode != null ? javaCode.trim() : "";
        }
    }

    /**
     * Удаляет комментарии из Java кода.
     * Обрабатывает как однострочные (//), так и многострочные (/* ... * /) комментарии.
     *
     * @param code код с комментариями
     * @return код без комментариев
     */
    private String removeComments(String code) {
        try {
            if (code == null || code.isEmpty()) {
                return code != null ? code : "";
            }

            String result = code;

            // Удаляем многострочные комментарии /* ... */
            result = safeReplace(result, "(?s)/\\*.*?\\*/", "");

            // Удаляем однострочные комментарии // ...
            result = safeReplace(result, "//.*", "");

            return result;

        } catch (Exception e) {
            logger.error("Error removing comments: {}", e.getMessage());
            return code;
        }
    }

    /**
     * Удаляет package и import statements из кода.
     * Эти конструкции не несут смысловой нагрузки для сравнения алгоритмов.
     *
     * @param code код с package и import statements
     * @return код без package и import statements
     */
    private String removePackageAndImports(String code) {
        try {
            if (code == null || code.isEmpty()) {
                return code != null ? code : "";
            }

            String result = code;

            // Удаляем package statement
            result = safeReplace(result, "(?m)^\\s*package\\s+[^;]+;\\s*", "");

            // Удаляем import statements
            result = safeReplace(result, "(?m)^\\s*import\\s+[^;]+;\\s*", "");

            return result;

        } catch (Exception e) {
            logger.error("Error removing package/import: {}", e.getMessage());
            return code;
        }
    }

    /**
     * Нормализует имена переменных, заменяя их на общую метку VAR.
     * Обрабатывает объявления переменных, присваивания и использования.
     *
     * @param code код с оригинальными именами переменных
     * @return код с нормализованными именами переменных
     */
    private String normalizeVariableNames(String code) {
        try {
            if (code == null || code.isEmpty()) {
                return code != null ? code : "";
            }

            String result = code;

            // Для длинного кода используем упрощенную нормализацию
            if (code.length() > 10000) {
                logger.warn("Code too long for full variable normalization, applying simplified version");
                return simplifiedVariableNormalization(code);
            }

            // Заменяем объявления переменных
            result = safeReplace(result,
                    "\\b(int|String|boolean|double|float|char|byte|short|long|final)\\s+([a-zA-Z_$][a-zA-Z0-9_$]*)\\b",
                    "$1 VAR"
            );

            // Заменяем присваивания
            try {
                result = safeReplace(result,
                        "\\b([a-zA-Z_$][a-zA-Z0-9_$]*)\\s*=\\s*([^;]+?);",
                        "VAR = EXPRESSION;"
                );
            } catch (Exception e) {
                logger.warn("Error normalizing assignments: {}", e.getMessage());
            }

            // Заменяем имена переменных в условиях
            try {
                result = safeReplace(result,
                        "\\b([a-zA-Z_$][a-zA-Z0-9_$]*)\\s*(>|<|==|!=|>=|<=)\\s*([a-zA-Z_$][a-zA-Z0-9_$]*)\\b",
                        "VAR OPERATOR VAR"
                );
            } catch (Exception e) {
                logger.warn("Error normalizing conditions: {}", e.getMessage());
            }

            // Заменяем имена переменных в аргументах методов
            try {
                result = safeReplace(result,
                        "\\(\\s*([a-zA-Z_$][a-zA-Z0-9_$]*)\\s*\\)",
                        "(VAR)"
                );
            } catch (Exception e) {
                logger.warn("Error normalizing arguments: {}", e.getMessage());
            }

            return result;

        } catch (StackOverflowError e) {
            logger.error("Stack overflow during variable name normalization");
            return simplifiedVariableNormalization(code);
        } catch (Exception e) {
            logger.error("Error normalizing variable names: {}", e.getMessage());
            return code;
        }
    }

    /**
     * Упрощенная нормализация имен переменных для больших объемов кода.
     * Обрабатывает только объявления переменных.
     *
     * @param code исходный код
     * @return код с упрощенной нормализацией переменных
     */
    private String simplifiedVariableNormalization(String code) {
        try {
            String result = code;
            // Простая замена - только объявления переменных
            result = safeReplace(result,
                    "\\b(int|String|boolean|double|float|char|byte|short|long|final)\\s+[a-zA-Z_$][a-zA-Z0-9_$]*\\b",
                    "$1 VAR"
            );
            return result;
        } catch (Exception e) {
            logger.error("Error in simplified variable normalization: {}", e.getMessage());
            return code;
        }
    }

    /**
     * Нормализует имена методов, заменяя нестандартные имена на метку METHOD.
     * Сохраняет имена стандартных методов (println, length, etc.).
     *
     * @param code код с оригинальными именами методов
     * @return код с нормализованными именами методов
     */
    private String normalizeMethodNames(String code) {
        try {
            if (code == null || code.isEmpty()) {
                return code != null ? code : "";
            }

            String result = code;

            // Для очень длинного кода используем упрощенный подход
            if (code.length() > 5000) {
                logger.debug("Using simplified method normalization for long code");
                return simplifiedMethodNormalization(code);
            }

            // Сохраняем стандартные методы, остальные заменяем на METHOD
            try {
                result = safeReplace(result,
                        "\\b(?!main|println|print|length|size|add|remove|get|set|toString|equals|hashCode|compareTo)" +
                                "([a-zA-Z_$][a-zA-Z0-9_$]*)\\s*\\(",
                        "METHOD("
                );
            } catch (PatternSyntaxException e) {
                logger.warn("Error in complex regular expression for methods, using simplified");
                result = simplifiedMethodNormalization(code);
            }

            return result;

        } catch (Exception e) {
            logger.error("Error normalizing method names: {}", e.getMessage());
            return code;
        }
    }

    /**
     * Упрощенная нормализация имен методов для больших объемов кода.
     * Заменяет все вызовы методов на общий шаблон.
     *
     * @param code исходный код
     * @return код с упрощенной нормализацией методов
     */
    private String simplifiedMethodNormalization(String code) {
        try {
            String result = code;
            // Простая замена - все вызовы методов
            result = safeReplace(result,
                    "\\b([a-zA-Z_$][a-zA-Z0-9_$]*)\\s*\\([^)]*\\)",
                    "METHOD(ARGS)"
            );
            return result;
        } catch (Exception e) {
            logger.error("Error in simplified method normalization: {}", e.getMessage());
            return code;
        }
    }

    /**
     * Нормализует пробелы и форматирование кода.
     * Унифицирует использование пробелов вокруг операторов и скобок.
     *
     * @param code код с оригинальным форматированием
     * @return код с унифицированным форматированием
     */
    private String normalizeWhitespace(String code) {
        try {
            if (code == null || code.isEmpty()) {
                return code != null ? code : "";
            }

            String result = code;

            // Заменяем все whitespace на один пробел
            result = safeReplace(result, "\\s+", " ");

            // Нормализуем скобки и операторы
            result = safeReplace(result, "\\s*\\{\\s*", " { ");
            result = safeReplace(result, "\\s*\\}\\s*", " } ");
            result = safeReplace(result, "\\s*\\(\\s*", " ( ");
            result = safeReplace(result, "\\s*\\)\\s*", " ) ");
            result = safeReplace(result, "\\s*\\[\\s*", " [ ");
            result = safeReplace(result, "\\s*\\]\\s*", " ] ");
            result = safeReplace(result, "\\s*;\\s*", " ; ");
            result = safeReplace(result, "\\s*,\\s*", " , ");
            result = safeReplace(result, "\\s*\\.\\s*", " . ");

            // Нормализуем операторы
            result = safeReplace(result, "\\s*=\\s*", " = ");
            result = safeReplace(result, "\\s*==\\s*", " == ");
            result = safeReplace(result, "\\s*!=\\s*", " != ");
            result = safeReplace(result, "\\s*>\\s*", " > ");
            result = safeReplace(result, "\\s*<\\s*", " < ");
            result = safeReplace(result, "\\s*>=\\s*", " >= ");
            result = safeReplace(result, "\\s*<=\\s*", " <= ");
            result = safeReplace(result, "\\s*\\+\\s*", " + ");
            result = safeReplace(result, "\\s*-\\s*", " - ");
            result = safeReplace(result, "\\s*\\*\\s*", " * ");
            result = safeReplace(result, "\\s*/\\s*", " / ");
            result = safeReplace(result, "\\s*%\\s*", " % ");
            result = safeReplace(result, "\\s*&&\\s*", " && ");
            result = safeReplace(result, "\\s*\\|\\|\\s*", " || ");
            result = safeReplace(result, "\\s*!\\s*", " ! ");

            // Убираем лишние пробелы
            result = safeReplace(result, "\\s+", " ");

            return result;

        } catch (Exception e) {
            logger.error("Error normalizing whitespace: {}", e.getMessage());
            return code;
        }
    }

    /**
     * Удаляет избыточные ключевые слова, которые не влияют на семантику сравнения.
     *
     * @param code код с модификаторами доступа
     * @return код без избыточных модификаторов доступа
     */
    private String removeExcessKeywords(String code) {
        try {
            if (code == null || code.isEmpty()) {
                return code != null ? code : "";
            }

            String result = code;

            // Удаляем лишние модификаторы доступа
            result = safeReplace(result, "\\b(public|private|protected)\\s+", "");

            return result;

        } catch (Exception e) {
            logger.error("Error removing excess keywords: {}", e.getMessage());
            return code;
        }
    }

    /**
     * Выполняет безопасную замену с использованием регулярных выражений.
     * Обеспечивает обработку ошибок и защиту от переполнения стека.
     *
     * @param input исходная строка
     * @param regex регулярное выражение для поиска
     * @param replacement строка замены
     * @return результат замены или исходная строка при ошибке
     */
    private String safeReplace(String input, String regex, String replacement) {
        try {
            if (input == null) {
                return "";
            }

            if (regex == null || regex.isEmpty()) {
                return input;
            }

            // Проверка длины регулярного выражения
            if (regex.length() > MAX_PATTERN_LENGTH) {
                logger.warn("Regular expression too long: {} characters (max {})",
                        regex.length(), MAX_PATTERN_LENGTH);
                return input;
            }

            try {
                return input.replaceAll(regex, replacement);
            } catch (PatternSyntaxException e) {
                logger.warn("Invalid regular expression: {} - {}", regex, e.getMessage());
                return input;
            } catch (StackOverflowError e) {
                logger.error("Stack overflow processing regular expression: {}", regex);
                return input;
            } catch (Exception e) {
                logger.warn("Error during replacement: {} - {}", regex, e.getMessage());
                return input;
            }

        } catch (Exception e) {
            logger.error("Critical error in safeReplace: {}", e.getMessage());
            return input != null ? input : "";
        }
    }

    /**
     * Дополнительная нормализация для алгоритмического сравнения.
     * Приводит алгоритмические конструкции к единому формату.
     *
     * @param code код для алгоритмической нормализации
     * @return код с нормализованными алгоритмическими конструкциями
     */
    public String normalizeAlgorithm(String code) {
        try {
            // Сначала применяем базовую нормализацию
            String normalized = normalize(code);

            if (normalized == null || normalized.isEmpty()) {
                return normalized != null ? normalized : "";
            }

            logger.debug("Starting algorithmic normalization (length: {} characters)", normalized.length());

            // Дополнительные преобразования для алгоритмов
            // Каждая операция в отдельном try-catch для изоляции ошибок
            try {
                normalized = safeReplace(normalized, "\\bVAR\\s*=\\s*VAR\\s*\\+\\s*VAR\\b", "VAR = VAR + VAR");
            } catch (Exception e) {
                logger.warn("Error normalizing addition: {}", e.getMessage());
            }

            try {
                normalized = safeReplace(normalized, "\\bVAR\\s*=\\s*VAR\\s*\\-\\s*VAR\\b", "VAR = VAR - VAR");
            } catch (Exception e) {
                logger.warn("Error normalizing subtraction: {}", e.getMessage());
            }

            try {
                normalized = safeReplace(normalized, "\\bVAR\\s*=\\s*VAR\\s*\\*\\s*VAR\\b", "VAR = VAR * VAR");
            } catch (Exception e) {
                logger.warn("Error normalizing multiplication: {}", e.getMessage());
            }

            try {
                normalized = safeReplace(normalized, "\\bVAR\\s*=\\s*VAR\\s*/\\s*VAR\\b", "VAR = VAR / VAR");
            } catch (Exception e) {
                logger.warn("Error normalizing division: {}", e.getMessage());
            }

            try {
                normalized = safeReplace(normalized, "\\bVAR\\s*\\+\\+\\b", "VAR ++");
                normalized = safeReplace(normalized, "\\bVAR\\s*\\-\\-\\b", "VAR --");
            } catch (Exception e) {
                logger.warn("Error normalizing increment/decrement: {}", e.getMessage());
            }

            try {
                normalized = safeReplace(normalized,
                        "\\bfor\\s*\\(\\s*VAR\\s*=\\s*NUMBER\\s*;\\s*VAR\\s*<\\s*VAR\\s*;\\s*VAR\\s*\\+\\+\\s*\\)",
                        "for ( VAR = NUMBER ; VAR < VAR ; VAR ++ )");
            } catch (Exception e) {
                logger.warn("Error normalizing for-loop: {}", e.getMessage());
            }

            try {
                normalized = safeReplace(normalized, "\\bif\\s*\\(\\s*VAR\\s*>\\s*VAR\\s*\\)", "if ( VAR > VAR )");
                normalized = safeReplace(normalized, "\\bif\\s*\\(\\s*VAR\\s*<\\s*VAR\\s*\\)", "if ( VAR < VAR )");
                normalized = safeReplace(normalized, "\\bif\\s*\\(\\s*VAR\\s*==\\s*VAR\\s*\\)", "if ( VAR == VAR )");
            } catch (Exception e) {
                logger.warn("Error normalizing if conditions: {}", e.getMessage());
            }

            logger.debug("Algorithmic normalization completed (length: {} characters)", normalized.length());

            return normalized.trim();

        } catch (Exception e) {
            logger.error("Error during algorithmic normalization: {}", e.getMessage());
            return code != null ? code.trim() : "";
        }
    }

    /**
     * Проверяет валидность кода перед нормализацией.
     *
     * @param code код для проверки
     * @return true если код может быть обработан, false в противном случае
     */
    public boolean isValidCode(String code) {
        try {
            if (code == null) {
                return false;
            }

            if (code.trim().isEmpty()) {
                return false;
            }

            if (code.length() > MAX_CODE_SIZE) {
                logger.warn("Code exceeds maximum size: {} characters", code.length());
                return false;
            }

            // Проверяем базовую структуру кода
            if (!code.contains("{") && !code.contains("}")) {
                logger.warn("Code doesn't contain curly braces, possibly not Java code");
                // Это не обязательно ошибка, но логируем
            }

            return true;

        } catch (Exception e) {
            logger.error("Error checking code validity: {}", e.getMessage());
            return false;
        }
    }
}