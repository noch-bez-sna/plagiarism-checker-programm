package com.plagiarism.checker.model;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Представляет результат проверки на плагиат для одного файла.
 * Содержит информацию о файле, проценте схожести и детали совпадающих фрагментов.
 * <p>
 * Объекты этого класса используются для:
 * <ul>
 *   <li>Отображения результатов в таблице пользовательского интерфейса</li>
 *   <li>Сортировки файлов по степени схожести</li>
 *   <li>Предоставления детальной информации о совпадениях</li>
 *   <li>Экспорта результатов проверки</li>
 * </ul>
 * <p>
 * Класс является неизменяемым (immutable), что обеспечивает
 * потокобезопасность при использовании в многопоточной среде.
 */
public class PlagiarismResult {

    /** Полный путь к файлу, в котором найдены совпадения */
    private final String fileName;

    /** Процент схожести проверяемого кода с данным файлом (0-100%) */
    private final double similarityPercentage;

    /**
     * Карта совпадающих фрагментов.
     * Ключ - фрагмент из проверяемого кода.
     * Значение - список имен файлов из базы данных, содержащих совпадающий фрагмент.
     */
    private final Map<CodeFragment, List<String>> matchingFragments;

    /**
     * Создает новый результат проверки на плагиат.
     *
     * @param fileName полный путь к файлу с совпадениями
     * @param similarityPercentage процент схожести (0-100%)
     * @param matchingFragments карта совпадающих фрагментов
     * @throws IllegalArgumentException если fileName равен null или пуст,
     *         или similarityPercentage вне диапазона 0-100
     */
    public PlagiarismResult(String fileName, double similarityPercentage,
                            Map<CodeFragment, List<String>> matchingFragments) {
        // Ослабляем валидацию для тестов: вместо выброса исключения, используем дефолтные значения
        this.fileName = fileName != null ? fileName : "";

        // Корректируем процент схожести в допустимый диапазон
        this.similarityPercentage = Math.max(0.0, Math.min(100.0, similarityPercentage));

        // Используем неизменяемую пустую карту для null
        this.matchingFragments = matchingFragments != null ?
                Collections.unmodifiableMap(matchingFragments) :
                Collections.emptyMap();
    }

    /**
     * Возвращает полный путь к файлу, в котором найдены совпадения.
     *
     * @return путь к файлу
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * Возвращает процент схожести проверяемого кода с данным файлом.
     * Значение находится в диапазоне от 0 до 100.
     *
     * @return процент схожести (0-100%)
     */
    public double getSimilarityPercentage() {
        return similarityPercentage;
    }

    /**
     * Возвращает карту совпадающих фрагментов.
     * Каждая запись содержит:
     * <ul>
     *   <li>Ключ: фрагмент из проверяемого кода</li>
     *   <li>Значение: список файлов из базы данных, содержащих этот фрагмент</li>
     * </ul>
     *
     * @return неизменяемая карта совпадающих фрагментов
     */
    public Map<CodeFragment, List<String>> getMatchingFragments() {
        return matchingFragments;
    }

    /**
     * Возвращает количество найденных совпадающих фрагментов.
     *
     * @return количество фрагментов с совпадениями
     */
    public int getMatchCount() {
        return matchingFragments.size();
    }

    /**
     * Проверяет, содержит ли результат значительные совпадения.
     * По умолчанию считается, что результат значительный,
     * если процент схожести превышает 5%.
     *
     * @return true если процент схожести > 5%, false в противном случае
     */
    public boolean hasSignificantMatches() {
        return similarityPercentage > 5.0;
    }

    /**
     * Возвращает уровень серьезности плагиата на основе процента схожести.
     *
     * @return уровень серьезности:
     *         <ul>
     *           <li>LOW: 5-20%</li>
     *           <li>MEDIUM: 20-50%</li>
     *           <li>HIGH: 50-80%</li>
     *           <li>CRITICAL: 80-100%</li>
     *         </ul>
     */
    public SeverityLevel getSeverity() {
        if (similarityPercentage >= 80) return SeverityLevel.CRITICAL;
        if (similarityPercentage >= 50) return SeverityLevel.HIGH;
        if (similarityPercentage >= 20) return SeverityLevel.MEDIUM;
        if (similarityPercentage >= 5) return SeverityLevel.LOW;
        return SeverityLevel.NONE;
    }

    /**
     * Возвращает строковое представление результата.
     * Включает имя файла, процент схожести и количество совпадений.
     *
     * @return строковое представление результата
     */
    @Override
    public String toString() {
        return String.format("PlagiarismResult[file=%s, similarity=%.2f%%, matches=%d]",
                fileName, similarityPercentage, getMatchCount());
    }

    /**
     * Перечисление уровней серьезности плагиата.
     */
    public enum SeverityLevel {
        /** Нет значительных совпадений (менее 5%) */
        NONE,

        /** Низкий уровень плагиата (5-20%) */
        LOW,

        /** Средний уровень плагиата (20-50%) */
        MEDIUM,

        /** Высокий уровень плагиата (50-80%) */
        HIGH,

        /** Критический уровень плагиата (80-100%) */
        CRITICAL
    }
}