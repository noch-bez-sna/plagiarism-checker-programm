package com.plagiarism.checker;

/**
 * Класс-загрузчик для запуска JavaFX приложения из JAR.
 * Не расширяет Application, поэтому может быть запущен напрямую.
 * <p>
 * Этот класс решает проблему модульной системы Java при запуске
 * JavaFX приложений из исполняемых JAR файлов.
 */
public class Launcher {
    /**
     * Точка входа приложения. Делегирует выполнение основному классу {@link MainApp}.
     *
     * @param args аргументы командной строки
     */
    public static void main(String[] args) {
        MainApp.main(args);
    }
}