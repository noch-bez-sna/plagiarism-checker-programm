package com.plagiarism.checker.model;

/**
 * Представляет фрагмент кода для сравнения при детектировании плагиата.
 * Содержит как оригинальное содержимое, так и нормализованную версию
 * для алгоритмического сравнения.
 * <p>
 * Фрагмент кода может представлять:
 * <ul>
 *   <li>Отдельную строку кода</li>
 *   <li>Последовательность строк (биграмма, триграмма)</li>
 *   <li>Специфический паттерн кода (цикл, условие)</li>
 *   <li>Весь код файла в нормализованном виде</li>
 * </ul>
 * <p>
 * Класс является неизменяемым (immutable), что обеспечивает
 * потокобезопасность при использовании в многопоточной среде.
 */
public class CodeFragment {

    /** Нормализованное содержимое фрагмента для сравнения */
    private final String normalizedContent;

    /** Оригинальное содержимое фрагмента для отображения пользователю */
    private final String originalContent;

    /** Номер строки в исходном файле (0 для полного кода или алгоритмических фрагментов) */
    private final int lineNumber;

    /**
     * Создает новый фрагмент кода.
     *
     * @param normalizedContent нормализованное содержимое фрагмента
     *                          (после удаления комментариев, нормализации имен и т.д.)
     * @param originalContent оригинальное содержимое фрагмента
     *                        (как оно выглядит в исходном файле)
     * @param lineNumber номер строки в исходном файле, где начинается фрагмент
     *                   (0 для специальных фрагментов типа FULL_CODE или ALGORITHM)
     */
    public CodeFragment(String normalizedContent, String originalContent, int lineNumber) {
        this.normalizedContent = normalizedContent;
        this.originalContent = originalContent;
        this.lineNumber = lineNumber;
    }

    /**
     * Возвращает нормализованное содержимое фрагмента.
     * Нормализованное содержимое используется для сравнения фрагментов,
     * так как оно не содержит различий в форматировании, именах переменных и т.д.
     *
     * @return нормализованная версия фрагмента кода
     */
    public String getNormalizedContent() {
        return normalizedContent;
    }

    /**
     * Возвращает оригинальное содержимое фрагмента.
     * Используется для отображения пользователю при показе совпадений.
     *
     * @return оригинальная версия фрагмента кода
     */
    public String getOriginalContent() {
        return originalContent;
    }

    /**
     * Возвращает номер строки в исходном файле, где начинается фрагмент.
     * Для специальных фрагментов (полный код, алгоритмические структуры)
     * возвращает 0.
     *
     * @return номер строки или 0 для специальных фрагментов
     */
    public int getLineNumber() {
        return lineNumber;
    }

    /**
     * Сравнивает данный фрагмент с другим объектом.
     * Два фрагмента считаются равными, если их нормализованное содержимое
     * идентично. Оригинальное содержимое и номер строки не учитываются
     * при сравнении.
     *
     * @param o объект для сравнения
     * @return true если объекты равны, false в противном случае
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CodeFragment)) return false;
        CodeFragment that = (CodeFragment) o;
        return normalizedContent.equals(that.normalizedContent);
    }

    /**
     * Возвращает хеш-код фрагмента.
     * Хеш-код вычисляется на основе нормализованного содержимого,
     * что соответствует контракту equals/hashCode.
     *
     * @return хеш-код фрагмента
     */
    @Override
    public int hashCode() {
        return normalizedContent.hashCode();
    }

    /**
     * Возвращает строковое представление фрагмента кода.
     * Включает номер строки и укороченную версию оригинального содержимого
     * для удобства отладки.
     *
     * @return строковое представление фрагмента
     */
    @Override
    public String toString() {
        String shortContent = originalContent;
        if (shortContent.length() > 50) {
            shortContent = shortContent.substring(0, 47) + "...";
        }
        return "CodeFragment[line=" + lineNumber + ", content=" + shortContent + "]";
    }
}