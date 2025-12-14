import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

public class FileManager {

    // Чтение всех строк файла
    public List<String> readAllLines(String filePath) throws IOException {
        List<String> lines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
        }
        return lines;
    }

    // Запись в файл
    public void writeToFile(String filePath, String content) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            writer.write(content);
        }
    }

    // Копирование файла
    public void copyFile(String sourcePath, String destPath) throws IOException {
        Files.copy(Paths.get(sourcePath), Paths.get(destPath), StandardCopyOption.REPLACE_EXISTING);
    }

    // Проверка существования файла
    public boolean fileExists(String filePath) {
        return Files.exists(Paths.get(filePath));
    }

    // Получение размера файла
    public long getFileSize(String filePath) throws IOException {
        return Files.size(Paths.get(filePath));
    }
}