package com.plagiarism.checker.gui;

import com.plagiarism.checker.core.PlagiarismDetector;
import com.plagiarism.checker.model.CodeFragment;
import com.plagiarism.checker.model.PlagiarismResult;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Контроллер главного окна приложения для детектирования плагиата.
 * Управляет пользовательским интерфейсом и координирует взаимодействие
 * между UI и движком детектора плагиата.
 * <p>
 * Обеспечивает:
 * <ul>
 *   <li>Загрузку базы данных кода</li>
 *   <li>Выбор файлов для проверки</li>
 *   <li>Ввод кода вручную</li>
 *   <li>Отображение результатов проверки</li>
 *   <li>Детальную информацию о найденных совпадениях</li>
 * </ul>
 */
public class MainController {
    private static final Logger logger = LogManager.getLogger(MainController.class);

    /** Детектор плагиата для выполнения проверок */
    private final PlagiarismDetector detector = new PlagiarismDetector();

    /** Флаг, указывающий что выполняется какая-либо задача */
    private volatile boolean taskInProgress = false;

    // Компоненты пользовательского интерфейса, связанные с FXML
    @FXML private TextField filePathField;
    @FXML private TextArea codeTextArea;
    @FXML private TableView<PlagiarismResult> resultsTable;
    @FXML private TextArea detailsTextArea;
    @FXML private TextField dbPathField;
    @FXML private Label dbStatusLabel;
    @FXML private ListView<String> dbFilesListView;
    @FXML private Label statusLabel;
    @FXML private ProgressBar progressBar;

    /**
     * Инициализирует контроллер после загрузки FXML.
     * Настраивает компоненты интерфейса, устанавливает начальные значения
     * и подготавливает приложение к работе.
     *
     * @throws RuntimeException если возникает критическая ошибка инициализации
     */
    @FXML
    public void initialize() {
        logger.info("Initializing MainController");
        try {
            setupResultsTable();
            dbStatusLabel.setText("Database not loaded");
            statusLabel.setText("Ready");
            progressBar.setProgress(0.0);

            setupTextAreaLimits();

            logger.info("MainController successfully initialized");
        } catch (Exception e) {
            logger.error("Critical error during MainController initialization: {}", e.getMessage(), e);
            showFatalError("Initialization Error",
                    "Failed to initialize application: " + e.getMessage());
        }
    }

    /**
     * Настраивает ограничения для текстовой области ввода кода.
     * Ограничивает максимальный размер вводимого кода 5 МБ.
     */
    private void setupTextAreaLimits() {
        codeTextArea.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && newValue.length() > 5 * 1024 * 1024) {
                Platform.runLater(() -> {
                    codeTextArea.setText(oldValue);
                    showWarning("Size Limit",
                            "Code size must not exceed 5 MB. " +
                                    "Current size: " + newValue.length() + " characters");
                });
            }
        });
    }

    /**
     * Настраивает таблицу для отображения результатов проверки.
     * Создает колонки с информацией о файлах, проценте схожести и количестве совпадений.
     *
     * @throws RuntimeException если не удается настроить таблицу
     */
    private void setupResultsTable() {
        try {
            // Колонка с именем файла (только имя, без пути)
            TableColumn<PlagiarismResult, String> fileCol = new TableColumn<>("File");
            fileCol.setCellValueFactory(data -> {
                try {
                    if (data != null && data.getValue() != null) {
                        return new SimpleStringProperty(getFileNameWithoutPath(data.getValue().getFileName()));
                    }
                    return new SimpleStringProperty("");
                } catch (Exception e) {
                    logger.warn("Error getting file name for table: {}", e.getMessage());
                    return new SimpleStringProperty("Error");
                }
            });

            // Добавляем Tooltip с полным путем
            fileCol.setCellFactory(column -> new TableCell<>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    try {
                        if (empty || item == null) {
                            setText(null);
                            setTooltip(null);
                        } else {
                            setText(item);
                            // Получаем полный путь из данных строки
                            if (getTableView() != null && !getTableView().getItems().isEmpty() &&
                                    getIndex() >= 0 && getIndex() < getTableView().getItems().size()) {
                                PlagiarismResult result = getTableView().getItems().get(getIndex());
                                if (result != null) {
                                    Tooltip tooltip = new Tooltip(result.getFileName());
                                    setTooltip(tooltip);
                                }
                            }
                        }
                    } catch (Exception e) {
                        logger.error("Error updating table cell: {}", e.getMessage());
                        setText("Error");
                        setTooltip(null);
                    }
                }
            });

            // Колонка с процентом сходства
            TableColumn<PlagiarismResult, String> similarityCol = new TableColumn<>("Similarity %");
            similarityCol.setCellValueFactory(data -> {
                try {
                    if (data != null && data.getValue() != null) {
                        return new SimpleStringProperty(
                                String.format("%.2f%%", data.getValue().getSimilarityPercentage()));
                    }
                    return new SimpleStringProperty("0.00%");
                } catch (Exception e) {
                    logger.warn("Error getting similarity percentage: {}", e.getMessage());
                    return new SimpleStringProperty("Error");
                }
            });

            // Колонка с количеством совпадений
            TableColumn<PlagiarismResult, String> matchesCol = new TableColumn<>("Matches");
            matchesCol.setCellValueFactory(data -> {
                try {
                    if (data != null && data.getValue() != null) {
                        return new SimpleStringProperty(
                                String.valueOf(data.getValue().getMatchingFragments().size()));
                    }
                    return new SimpleStringProperty("0");
                } catch (Exception e) {
                    logger.warn("Error getting matches count: {}", e.getMessage());
                    return new SimpleStringProperty("Error");
                }
            });

            // Устанавливаем ширину колонок
            fileCol.setPrefWidth(250);
            similarityCol.setPrefWidth(100);
            matchesCol.setPrefWidth(100);

            resultsTable.getColumns().setAll(fileCol, similarityCol, matchesCol);

            // Обработчик выбора строки
            resultsTable.getSelectionModel().selectedItemProperty().addListener(
                    (obs, oldVal, newVal) -> {
                        try {
                            showResultDetails(newVal);
                        } catch (Exception e) {
                            logger.error("Error showing result details: {}", e.getMessage(), e);
                            showError("Error", "Failed to display result details");
                        }
                    });

        } catch (Exception e) {
            logger.error("Error setting up results table: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to set up results table", e);
        }
    }

    /**
     * Извлекает имя файла из полного пути.
     *
     * @param fullPath полный путь к файлу
     * @return только имя файла без пути или пустая строка если путь некорректен
     */
    private String getFileNameWithoutPath(String fullPath) {
        try {
            if (fullPath == null || fullPath.isEmpty()) {
                return "";
            }

            // Извлекаем только имя файла из пути
            int lastSeparator = Math.max(fullPath.lastIndexOf('\\'), fullPath.lastIndexOf('/'));
            if (lastSeparator != -1 && lastSeparator + 1 < fullPath.length()) {
                return fullPath.substring(lastSeparator + 1);
            }
            return fullPath;
        } catch (Exception e) {
            logger.warn("Error extracting file name from path '{}': {}", fullPath, e.getMessage());
            return fullPath != null ? fullPath : "";
        }
    }

    /**
     * Обрабатывает нажатие кнопки "Browse" для выбора файла Java.
     * Открывает диалог выбора файла и загружает его содержимое.
     */
    @FXML
    private void handleBrowseFile() {
        try {
            Window window = filePathField.getScene().getWindow();
            if (window == null) {
                showError("Error", "Could not get application window");
                return;
            }

            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Select Java File to Check");
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("Java Files", "*.java"));

            File file = fileChooser.showOpenDialog(window);
            if (file != null) {
                filePathField.setText(file.getAbsolutePath());
                loadFileContent(file.toPath());
            }
        } catch (SecurityException e) {
            logger.error("Security error selecting file: {}", e.getMessage(), e);
            showError("Security Error", "No access rights to file system");
        } catch (Exception e) {
            logger.error("Error selecting file: {}", e.getMessage(), e);
            showError("Error", "Failed to select file: " + e.getMessage());
        }
    }

    /**
     * Загружает содержимое файла в текстовую область.
     * Проверяет существование, доступность и размер файла перед загрузкой.
     *
     * @param filePath путь к файлу для загрузки
     */
    private void loadFileContent(Path filePath) {
        try {
            if (!Files.exists(filePath)) {
                throw new IOException("File does not exist: " + filePath);
            }

            if (!Files.isRegularFile(filePath)) {
                throw new IOException("Path is not a file: " + filePath);
            }

            if (!Files.isReadable(filePath)) {
                throw new IOException("No read permission for file: " + filePath);
            }

            long fileSize = Files.size(filePath);
            if (fileSize > 10 * 1024 * 1024) { // 10 MB
                throw new IOException("File is too large (maximum 10 MB). Current size: " +
                        String.format("%.2f", fileSize / 1024.0 / 1024.0) + " MB");
            }

            String content = Files.readString(filePath);
            codeTextArea.setText(content);
            statusLabel.setText("File loaded: " + filePath.getFileName());

        } catch (InvalidPathException e) {
            logger.error("Invalid file path: {}", e.getMessage());
            showError("Error", "Invalid file path: " + filePath);
        } catch (SecurityException e) {
            logger.error("File access problem: {}", e.getMessage());
            showError("Access Error", "No access rights to file: " + filePath);
        } catch (IOException e) {
            logger.error("File read error: {}", e.getMessage());
            showError("Read Error", "Failed to read file: " + e.getMessage());
        } catch (OutOfMemoryError e) {
            logger.error("Insufficient memory to load file: {}", filePath, e);
            showError("Memory Error", "Insufficient memory to load file");
        } catch (Exception e) {
            logger.error("Unexpected error loading file: {}", e.getMessage(), e);
            showError("Error", "Unexpected error: " + e.getMessage());
        }
    }

    /**
     * Обрабатывает анализ выбранного файла на плагиат.
     * Проверяет наличие выбранного файла и загруженной базы данных,
     * затем запускает асинхронную задачу проверки.
     */
    @FXML
    private void handleAnalyzeFile() {
        if (taskInProgress) {
            showWarning("Operation in Progress", "Please wait for current operation to complete");
            return;
        }

        String filePath = filePathField.getText();
        if (filePath == null || filePath.trim().isEmpty()) {
            showError("Error", "Select a file to check");
            return;
        }

        if (!detector.isDatabaseLoaded()) {
            showError("Error", "Load database first");
            return;
        }

        taskInProgress = true;
        resetProgress();
        statusLabel.setText("Preparing to analyze file...");

        Task<List<PlagiarismResult>> task = new Task<>() {
            @Override
            protected List<PlagiarismResult> call() throws Exception {
                try {
                    updateMessage("Analyzing file...");
                    updateProgress(0.1, 1);
                    Thread.sleep(50); // Небольшая задержка для плавности

                    updateProgress(0.3, 1);
                    List<PlagiarismResult> results = detector.checkFileForPlagiarism(Path.of(filePath));

                    updateProgress(0.9, 1);
                    Thread.sleep(50);

                    return results != null ? results : Collections.emptyList();

                } catch (InterruptedException e) {
                    logger.warn("File analysis task interrupted");
                    Thread.currentThread().interrupt();
                    throw e;
                } catch (InvalidPathException e) {
                    logger.error("Invalid file path: {}", filePath, e);
                    throw new IOException("Invalid file path: " + filePath, e);
                } catch (SecurityException e) {
                    logger.error("File access error: {}", filePath, e);
                    throw new IOException("No access rights to file: " + filePath, e);
                } catch (IOException e) {
                    logger.error("File read error: {}", filePath, e);
                    throw e; // Перебрасываем дальше
                } catch (Exception e) {
                    logger.error("Unexpected error analyzing file: {}", e.getMessage(), e);
                    throw new IOException("Unexpected error analyzing file: " + e.getMessage(), e);
                }
            }
        };

        setupTaskHandlers(task, "file analysis");
        new Thread(task).start();
    }

    /**
     * Обрабатывает анализ кода из текстовой области на плагиат.
     * Проверяет наличие кода и загруженной базы данных,
     * затем запускает асинхронную задачу проверки.
     */
    @FXML
    private void handleAnalyzeCode() {
        if (taskInProgress) {
            showWarning("Operation in Progress", "Please wait for current operation to complete");
            return;
        }

        String code = codeTextArea.getText();
        if (code == null || code.trim().isEmpty()) {
            showError("Error", "Enter code to check");
            return;
        }

        if (!detector.isDatabaseLoaded()) {
            showError("Error", "Load database first");
            return;
        }

        if (code.length() > 5 * 1024 * 1024) {
            showError("Error", "Code is too large (maximum 5 MB)");
            return;
        }

        taskInProgress = true;
        resetProgress();
        statusLabel.setText("Preparing to analyze code...");

        Task<List<PlagiarismResult>> task = new Task<>() {
            @Override
            protected List<PlagiarismResult> call() throws Exception {
                try {
                    updateMessage("Analyzing code...");
                    updateProgress(0.1, 1);
                    Thread.sleep(50);

                    updateProgress(0.4, 1);
                    List<PlagiarismResult> results = detector.checkForPlagiarism(code);

                    updateProgress(0.9, 1);
                    Thread.sleep(50);

                    return results != null ? results : Collections.emptyList();

                } catch (InterruptedException e) {
                    logger.warn("Code analysis task interrupted");
                    Thread.currentThread().interrupt();
                    throw e;
                } catch (OutOfMemoryError e) {
                    logger.error("Insufficient memory for code analysis");
                    throw new IOException("Insufficient memory for code analysis", e);
                } catch (Exception e) {
                    logger.error("Unexpected error analyzing code: {}", e.getMessage(), e);
                    throw new IOException("Unexpected error analyzing code: " + e.getMessage(), e);
                }
            }
        };

        setupTaskHandlers(task, "code analysis");
        new Thread(task).start();
    }

    /**
     * Обрабатывает нажатие кнопки "Browse" для выбора директории базы данных.
     * Открывает диалог выбора директории с Java файлами.
     */
    @FXML
    private void handleBrowseDatabase() {
        try {
            Window window = dbPathField.getScene().getWindow();
            if (window == null) {
                showError("Error", "Could not get application window");
                return;
            }

            DirectoryChooser dirChooser = new DirectoryChooser();
            dirChooser.setTitle("Select Folder with Java Files for Database");

            File dir = dirChooser.showDialog(window);
            if (dir != null) {
                dbPathField.setText(dir.getAbsolutePath());
            }
        } catch (SecurityException e) {
            logger.error("Security error selecting folder: {}", e.getMessage(), e);
            showError("Security Error", "No access rights to file system");
        } catch (Exception e) {
            logger.error("Error selecting folder: {}", e.getMessage(), e);
            showError("Error", "Failed to select folder: " + e.getMessage());
        }
    }

    /**
     * Обрабатывает загрузку базы данных из выбранной директории.
     * Запускает асинхронную задачу загрузки и обработки Java файлов.
     */
    @FXML
    private void handleLoadDatabase() {
        if (taskInProgress) {
            showWarning("Operation in Progress", "Please wait for current operation to complete");
            return;
        }

        String dbPath = dbPathField.getText();
        if (dbPath == null || dbPath.trim().isEmpty()) {
            showError("Error", "Select database folder");
            return;
        }

        taskInProgress = true;
        resetProgress();
        statusLabel.setText("Preparing to load database...");

        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                try {
                    updateMessage("Loading database...");
                    updateProgress(0.1, 1);
                    Thread.sleep(50);

                    updateProgress(0.3, 1);
                    detector.loadDatabase(Path.of(dbPath));

                    updateProgress(0.8, 1);
                    Thread.sleep(100);

                    updateProgress(1.0, 1);
                    return null;

                } catch (InterruptedException e) {
                    logger.warn("Database load task interrupted");
                    Thread.currentThread().interrupt();
                    throw e;
                } catch (InvalidPathException e) {
                    logger.error("Invalid database path: {}", dbPath, e);
                    throw new IOException("Invalid database path: " + dbPath, e);
                } catch (SecurityException e) {
                    logger.error("Directory access error: {}", dbPath, e);
                    throw new IOException("No access rights to directory: " + dbPath, e);
                } catch (IOException e) {
                    logger.error("Database load error: {}", e.getMessage(), e);
                    throw e; // Перебрасываем дальше
                } catch (Exception e) {
                    logger.error("Unexpected error loading database: {}", e.getMessage(), e);
                    throw new IOException("Unexpected error loading database: " + e.getMessage(), e);
                }
            }
        };

        task.setOnSucceeded(e -> {
            try {
                taskInProgress = false;
                List<String> files = detector.getDatabaseFiles();
                dbStatusLabel.setText(String.format("Database loaded (%d files)", files.size()));
                dbFilesListView.setItems(FXCollections.observableArrayList(files));
                statusLabel.setText("Database successfully loaded");
                progressBar.setProgress(1.0);

                // Очищаем старые результаты
                resultsTable.getItems().clear();
                detailsTextArea.clear();

                showInfo("Success", String.format("Database successfully loaded. Files: %d", files.size()));

            } catch (Exception ex) {
                logger.error("Error updating UI after database load: {}", ex.getMessage(), ex);
                dbStatusLabel.setText("UI update error");
                showError("Error", "Failed to update interface: " + ex.getMessage());
            }
        });

        task.setOnFailed(e -> {
            taskInProgress = false;
            Throwable exception = task.getException();
            logger.error("Database load error: {}", exception != null ? exception.getMessage() : "Unknown error", exception);

            String errorMessage = "Unknown error";
            if (exception != null) {
                errorMessage = exception.getMessage();
                if (errorMessage == null) {
                    errorMessage = exception.getClass().getSimpleName();
                }
            }

            showError("Database Load Error", errorMessage);
            dbStatusLabel.setText("Load error");
            statusLabel.setText("Error loading database");
            progressBar.setProgress(0.0);
        });

        task.setOnCancelled(e -> {
            taskInProgress = false;
            statusLabel.setText("Load cancelled");
            progressBar.setProgress(0.0);
        });

        task.messageProperty().addListener((obs, oldVal, newVal) -> {
            Platform.runLater(() -> {
                if (newVal != null) {
                    statusLabel.setText(newVal);
                }
            });
        });

        task.progressProperty().addListener((obs, oldVal, newVal) -> {
            Platform.runLater(() -> {
                progressBar.setProgress(newVal.doubleValue());
            });
        });

        new Thread(task).start();
    }

    /**
     * Настраивает обработчики событий для асинхронной задачи анализа.
     *
     * @param task задача для настройки обработчиков
     * @param taskName имя задачи для отображения в UI
     */
    private void setupTaskHandlers(Task<List<PlagiarismResult>> task, String taskName) {
        AtomicReference<List<PlagiarismResult>> resultsRef = new AtomicReference<>(Collections.emptyList());

        task.setOnSucceeded(e -> {
            try {
                taskInProgress = false;
                List<PlagiarismResult> results = task.getValue();
                resultsRef.set(results != null ? results : Collections.emptyList());

                Platform.runLater(() -> {
                    try {
                        resultsTable.setItems(FXCollections.observableArrayList(resultsRef.get()));
                        statusLabel.setText(String.format("%s completed. Found %d matches",
                                taskName, resultsRef.get().size()));
                        progressBar.setProgress(1.0);

                        // Если есть результаты, выбираем первую строку
                        if (!resultsRef.get().isEmpty()) {
                            resultsTable.getSelectionModel().selectFirst();
                        } else {
                            showInfo("Result", "No matches found");
                        }
                    } catch (Exception ex) {
                        logger.error("Error updating UI: {}", ex.getMessage(), ex);
                        showError("UI Error", "Failed to display results");
                    }
                });
            } catch (Exception ex) {
                logger.error("Error processing results: {}", ex.getMessage(), ex);
                Platform.runLater(() -> showError("Error", "Failed to process results"));
            }
        });

        task.setOnFailed(e -> {
            taskInProgress = false;
            Throwable exception = task.getException();
            logger.error("Task execution error {}: {}", taskName,
                    exception != null ? exception.getMessage() : "Unknown error",
                    exception);

            Platform.runLater(() -> {
                String errorMessage = "Unknown error";
                if (exception != null) {
                    errorMessage = exception.getMessage();
                    if (errorMessage == null) {
                        errorMessage = exception.getClass().getSimpleName();
                    }
                }

                showError("Error " + taskName, errorMessage);
                statusLabel.setText("Execution error");
                progressBar.setProgress(0.0);
            });
        });

        task.setOnCancelled(e -> {
            taskInProgress = false;
            Platform.runLater(() -> {
                statusLabel.setText(taskName + " cancelled");
                progressBar.setProgress(0.0);
            });
        });

        task.messageProperty().addListener((obs, oldVal, newVal) -> {
            Platform.runLater(() -> {
                if (newVal != null) {
                    statusLabel.setText(newVal);
                }
            });
        });

        task.progressProperty().addListener((obs, oldVal, newVal) -> {
            Platform.runLater(() -> {
                progressBar.setProgress(newVal.doubleValue());
            });
        });
    }

    /**
     * Отображает детальную информацию о выбранном результате проверки.
     *
     * @param result результат проверки для отображения деталей
     */
    private void showResultDetails(PlagiarismResult result) {
        try {
            if (result == null) {
                detailsTextArea.setText("");
                return;
            }

            StringBuilder details = new StringBuilder();

            // Показываем только имя файла, но с возможностью увидеть полный путь
            String shortName = getFileNameWithoutPath(result.getFileName());
            details.append("File: ").append(shortName).append("\n");
            details.append("Full path: ").append(result.getFileName()).append("\n");
            details.append(String.format("Similarity: %.2f%%\n", result.getSimilarityPercentage()));
            details.append(String.format("Matches found: %d\n\n",
                    result.getMatchingFragments().size()));

            if (!result.getMatchingFragments().isEmpty()) {
                details.append("Matching fragments:\n");
                details.append("----------------------\n");

                int counter = 1;
                for (Map.Entry<CodeFragment, List<String>> entry : result.getMatchingFragments().entrySet()) {
                    CodeFragment fragment = entry.getKey();
                    details.append(String.format("%d. Line %d:\n", counter++, fragment.getLineNumber()));
                    String content = fragment.getOriginalContent();
                    if (content.length() > 200) {
                        content = content.substring(0, 200) + "...";
                    }
                    details.append("   ").append(content).append("\n\n");

                    if (counter > 10) { // Ограничиваем количество выводимых фрагментов
                        details.append("... and ").append(result.getMatchingFragments().size() - 10)
                                .append(" more fragments\n");
                        break;
                    }
                }
            } else {
                details.append("No detailed match information.\n");
            }

            detailsTextArea.setText(details.toString());
        } catch (Exception e) {
            logger.error("Error formatting result details: {}", e.getMessage(), e);
            detailsTextArea.setText("Error displaying result details");
        }
    }

    /**
     * Отображает диалоговое окно с сообщением об ошибке.
     *
     * @param title заголовок окна
     * @param message сообщение об ошибке
     */
    private void showError(String title, String message) {
        logger.error("{}: {}", title, message);
        Platform.runLater(() -> {
            try {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle(title);
                alert.setHeaderText(null);
                alert.setContentText(message);
                alert.showAndWait();
            } catch (Exception e) {
                logger.error("Error showing error dialog: {}", e.getMessage(), e);
                // Фолбэк в консоль
                System.err.println("ERROR: " + title + " - " + message);
            }
        });
    }

    /**
     * Отображает диалоговое окно с предупреждением.
     *
     * @param title заголовок окна
     * @param message предупреждающее сообщение
     */
    private void showWarning(String title, String message) {
        logger.warn("{}: {}", title, message);
        Platform.runLater(() -> {
            try {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle(title);
                alert.setHeaderText(null);
                alert.setContentText(message);
                alert.showAndWait();
            } catch (Exception e) {
                logger.error("Error showing warning: {}", e.getMessage(), e);
            }
        });
    }

    /**
     * Отображает диалоговое окно с информационным сообщением.
     *
     * @param title заголовок окна
     * @param message информационное сообщение
     */
    private void showInfo(String title, String message) {
        logger.info("{}: {}", title, message);
        Platform.runLater(() -> {
            try {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle(title);
                alert.setHeaderText(null);
                alert.setContentText(message);
                alert.showAndWait();
            } catch (Exception e) {
                logger.error("Error showing info dialog: {}", e.getMessage(), e);
            }
        });
    }

    /**
     * Отображает диалоговое окно с критической ошибкой.
     *
     * @param title заголовок окна
     * @param message сообщение о критической ошибке
     */
    private void showFatalError(String title, String message) {
        logger.fatal("{}: {}", title, message);
        Platform.runLater(() -> {
            try {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle(title);
                alert.setHeaderText("Critical Error");
                alert.setContentText(message + "\n\nApplication may work incorrectly.");
                alert.showAndWait();
            } catch (Exception e) {
                logger.error("Error showing fatal error: {}", e.getMessage(), e);
                // Фолбэк - закрываем приложение
                Platform.exit();
            }
        });
    }

    /**
     * Сбрасывает индикатор прогресса к нулевому значению.
     */
    private void resetProgress() {
        Platform.runLater(() -> {
            try {
                progressBar.progressProperty().unbind();
                progressBar.setProgress(0.0);
            } catch (Exception e) {
                logger.warn("Error resetting progress: {}", e.getMessage());
            }
        });
    }

    /**
     * Останавливает все выполняющиеся задачи и освобождает ресурсы.
     * Вызывается при закрытии приложения для корректного завершения работы.
     */
    public void shutdown() {
        logger.info("=== CONTROLLER SHUTDOWN ===");

        try {
            // Шаг 1: Останавливаем все задачи
            stopAllTasks();

            // Шаг 2: Очищаем детектор
            detector.clearDatabase();

            // Шаг 3: Очищаем все поля
            clearAll();

            // Шаг 4: Принудительно вызываем сборку мусора
            System.gc();

            logger.info("Controller successfully stopped");

        } catch (Exception e) {
            logger.error("Error stopping controller: {}", e.getMessage(), e);
        }
    }

    /**
     * Останавливает все выполняющиеся задачи.
     */
    public void stopAllTasks() {
        taskInProgress = false;
        resetProgress();
        statusLabel.setText("Operations stopped");
        logger.info("All tasks stopped");
    }

    /**
     * Очищает все поля пользовательского интерфейса.
     */
    public void clearAll() {
        Platform.runLater(() -> {
            try {
                filePathField.clear();
                codeTextArea.clear();
                resultsTable.getItems().clear();
                detailsTextArea.clear();
                dbPathField.clear();
                dbStatusLabel.setText("Database not loaded");
                dbFilesListView.getItems().clear();
                statusLabel.setText("Ready");
                progressBar.setProgress(0.0);
                logger.info("All fields cleared");
            } catch (Exception e) {
                logger.error("Error clearing fields: {}", e.getMessage());
            }
        });
    }

    /**
     * Проверяет, выполняется ли в данный момент какая-либо задача.
     *
     * @return true если задача выполняется, false в противном случае
     */
    public boolean isTaskInProgress() {
        return taskInProgress;
    }
}