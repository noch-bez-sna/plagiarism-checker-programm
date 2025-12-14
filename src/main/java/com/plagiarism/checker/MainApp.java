package com.plagiarism.checker;

import com.plagiarism.checker.gui.MainController;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Основной класс JavaFX приложения для детектирования плагиата.
 * Управляет жизненным циклом приложения, инициализацией GUI и корректным завершением работы.
 * <p>
 * Класс реализует следующие функции:
 * <ul>
 *   <li>Загрузка и отображение главного окна приложения</li>
 *   <li>Обработка неотловленных исключений</li>
 *   <li>Корректное завершение работы с освобождением ресурсов</li>
 *   <li>Обработка сигналов завершения (Ctrl+C, закрытие окна)</li>
 * </ul>
 */
public class MainApp extends Application {
    private static final Logger logger = LogManager.getLogger(MainApp.class);

    /** Флаг, указывающий что выполняется завершение работы */
    private static final AtomicBoolean shutdownInProgress = new AtomicBoolean(false);

    /** Executor для выполнения операций завершения работы */
    private static final ExecutorService shutdownExecutor = Executors.newSingleThreadExecutor();

    /** Основное окно приложения */
    private Stage primaryStage;

    /** Контроллер главного окна */
    private MainController mainController;

    /** Слабая ссылка на контроллер для предотвращения утечек памяти */
    private WeakReference<MainController> controllerRef;

    /**
     * Инициализирует приложение перед запуском GUI.
     * Устанавливает обработчик неотловленных исключений.
     */
    @Override
    public void init() {
        logger.info("=== INITIALIZING THE APPLICATION ===");

        // Устанавливаем обработчик неотловленных исключений
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            logger.error("Uncaught exception in thread {}: {}",
                    thread.getName(), throwable.getMessage(), throwable);
        });
    }

    /**
     * Запускает главное окно приложения.
     * Загружает FXML файл, настраивает сцену и отображает окно.
     *
     * @param primaryStage основной Stage, предоставляемый JavaFX
     */
    @Override
    public void start(Stage primaryStage) {
        logger.info("=== LAUNCHING THE MAIN WINDOW ===");
        this.primaryStage = primaryStage;

        try {
            // Загружаем FXML и получаем контроллер
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/main_window.fxml"));
            Parent root = loader.load();

            // Сохраняем ссылку на контроллер
            mainController = loader.getController();
            controllerRef = new WeakReference<>(mainController);

            // Настраиваем сцену
            Scene scene = new Scene(root, 1000, 750);

            // Настраиваем основное окно
            primaryStage.setTitle("Java Plagiarism Checker v1.0");
            primaryStage.setScene(scene);
            primaryStage.setMinWidth(800);
            primaryStage.setMinHeight(600);

            // Обработка закрытия окна
            primaryStage.setOnCloseRequest(event -> {
                logger.info("The user closed the window");
                performShutdown();
            });

            // Показываем окно
            primaryStage.show();

            logger.info("=== THE APPLICATION HAS BEEN LAUNCHED SUCCESSFULLY ===");

        } catch (Exception e) {
            logger.error("Error when starting the application", e);
            showErrorAndExit("Startup error", e);
        }
    }

    /**
     * Останавливает приложение при закрытии.
     * Вызывается JavaFX платформой при завершении работы.
     */
    @Override
    public void stop() {
        logger.info("=== STOPPING THE APPLICATION ===");
        performShutdown();
    }

    /**
     * Выполняет корректное завершение работы приложения.
     * Останавливает контроллер, закрывает окно и освобождает ресурсы.
     */
    private void performShutdown() {
        if (shutdownInProgress.getAndSet(true)) {
            logger.debug("Completion is already in progress, skip it");
            return;
        }

        logger.info("We are starting to shut down the application...");

        // Используем отдельный поток для shutdown чтобы не блокировать JavaFX
        shutdownExecutor.execute(() -> {
            try {
                // Шаг 1: Останавливаем контроллер
                stopController();

                // Шаг 2: Закрываем JavaFX
                Platform.runLater(() -> {
                    try {
                        if (primaryStage != null && primaryStage.isShowing()) {
                            primaryStage.close();
                        }
                    } catch (Exception e) {
                        logger.warn("Error closing window: {}", e.getMessage());
                    }

                    // Корректный выход из JavaFX
                    Platform.exit();
                });

                // Шаг 3: Ждем завершения JavaFX
                Thread.sleep(500);

                // Шаг 4: Завершаем все потоки
                shutdownExecutor.shutdown();
                try {
                    if (!shutdownExecutor.awaitTermination(3, TimeUnit.SECONDS)) {
                        shutdownExecutor.shutdownNow();
                    }
                } catch (InterruptedException e) {
                    shutdownExecutor.shutdownNow();
                    Thread.currentThread().interrupt();
                }

                logger.info("=== APPLICATION COMPLETED SUCCESSFULLY ===");

                // Даем время на запись логов
                Thread.sleep(100);

            } catch (Exception e) {
                logger.error("Error when shutting down", e);
                // Корректно завершаем JavaFX
                Platform.runLater(Platform::exit);
            }
        });
    }

    /**
     * Останавливает контроллер главного окна и освобождает ресурсы.
     */
    private void stopController() {
        try {
            if (mainController != null) {
                // Вызываем метод shutdown у контроллера
                Platform.runLater(() -> {
                    try {
                        mainController.shutdown();
                    } catch (Exception e) {
                        logger.warn("Error stopping controller: {}", e.getMessage());
                    }
                });

                // Ждем завершения операций контроллера
                Thread.sleep(300);
            }

            // Очищаем ссылки для помощи сборщику мусора
            mainController = null;
            if (controllerRef != null) {
                controllerRef.clear();
            }

        } catch (Exception e) {
            logger.warn("Error stopping controller: {}", e.getMessage());
        }
    }

    /**
     * Отображает диалог с ошибкой и завершает работу приложения.
     * Используется при критических ошибках запуска.
     *
     * @param title заголовок ошибки
     * @param throwable исключение, вызвавшее ошибку
     */
    private void showErrorAndExit(String title, Throwable throwable) {
        // Создаем effectively final переменную
        final String errorMessage;

        // Инициализируем переменную сразу
        try {
            errorMessage = throwable != null ? throwable.getMessage() : "Unknown error";
        } catch (Exception e) {
            // Если произошла ошибка при получении сообщения, используем дефолтное
            final String defaultMessage = "Error while generating message";
            showErrorDialog(title, defaultMessage);
            return;
        }

        // Используем для диалога (если errorMessage не null)
        if (errorMessage == null) {
            final String fallbackMessage = throwable != null ?
                    throwable.getClass().getName() : "Unknown error";
            showErrorDialog(title, fallbackMessage);
        } else {
            showErrorDialog(title, errorMessage);
        }
    }

    /**
     * Отображает диалоговое окно с сообщением об ошибке.
     *
     * @param title заголовок диалога
     * @param message сообщение об ошибке
     */
    private void showErrorDialog(String title, final String message) {
        Platform.runLater(() -> {
            try {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle(title);
                alert.setHeaderText("Application launch error");
                alert.setContentText("Message: " + message);

                // Обработка закрытия диалога
                alert.setOnCloseRequest(e -> Platform.exit());

                alert.showAndWait();
            } catch (Exception e) {
                System.err.println("ERROR: " + title + " - " + message);
                Platform.exit();
            }
        });

        try {
            // Даем время на показ диалога
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Точка входа для запуска приложения.
     * Инициализирует приложение и запускает JavaFX платформу.
     *
     * @param args аргументы командной строки
     */
    public static void main(String[] args) {
        logger.info("=== LAUNCHING THE APPLICATION ===");

        try {
            // Добавляем shutdown hook для обработки Ctrl+C
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                if (!shutdownInProgress.get()) {
                    logger.info("Termination signal received (Ctrl+C)");
                    shutdownExecutor.shutdownNow();
                }
            }));

            // Запускаем JavaFX приложение
            launch(args);

        } catch (Exception e) {
            logger.error("Critical error on startup", e);
            System.err.println("Critical error: " + e.getMessage());
            // Корректно завершаем JavaFX
            Platform.exit();
        }
    }
}