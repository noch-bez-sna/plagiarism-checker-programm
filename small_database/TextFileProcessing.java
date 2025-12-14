// TextFileProcessing.java - Работа с текстовыми файлами
public class TextFileProcessing {

    // Чтение файла целиком
    public String readWholeFile(String filename) throws java.io.IOException {
        return new String(java.nio.file.Files.readAllBytes(
                java.nio.file.Paths.get(filename)));
    }

    // Чтение файла построчно
    public void readFileLineByLine(String filename) {
        try (java.io.BufferedReader reader = new java.io.BufferedReader(
                new java.io.FileReader(filename))) {
            String line;
            int lineNumber = 1;
            while ((line = reader.readLine()) != null) {
                System.out.println(lineNumber + ": " + line);
                lineNumber++;
            }
        } catch (java.io.IOException e) {
            System.out.println("Error reading file: " + e.getMessage());
        }
    }

    // Запись в файл
    public void writeToFile(String filename, String content) {
        try (java.io.FileWriter writer = new java.io.FileWriter(filename)) {
            writer.write(content);
            System.out.println("File written successfully");
        } catch (java.io.IOException e) {
            System.out.println("Error writing to file: " + e.getMessage());
        }
    }

    // Копирование файла
    public void copyFile(String source, String destination) {
        try (java.io.FileInputStream fis = new java.io.FileInputStream(source);
             java.io.FileOutputStream fos = new java.io.FileOutputStream(destination)) {

            byte[] buffer = new byte[1024];
            int bytesRead;

            while ((bytesRead = fis.read(buffer)) != -1) {
                fos.write(buffer, 0, bytesRead);
            }

            System.out.println("File copied successfully");
        } catch (java.io.IOException e) {
            System.out.println("Error copying file: " + e.getMessage());
        }
    }

    // Поиск текста в файле
    public boolean searchInFile(String filename, String searchText) {
        try (java.io.BufferedReader reader = new java.io.BufferedReader(
                new java.io.FileReader(filename))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains(searchText)) {
                    return true;
                }
            }
        } catch (java.io.IOException e) {
            System.out.println("Error searching in file: " + e.getMessage());
        }
        return false;
    }

    // Подсчет строк в файле
    public int countLines(String filename) {
        int count = 0;
        try (java.io.BufferedReader reader = new java.io.BufferedReader(
                new java.io.FileReader(filename))) {
            while (reader.readLine() != null) {
                count++;
            }
        } catch (java.io.IOException e) {
            System.out.println("Error counting lines: " + e.getMessage());
        }
        return count;
    }

    // Запись с помощью NIO
    public void writeWithNIO(String filename, String content) {
        try {
            java.nio.file.Files.write(
                    java.nio.file.Paths.get(filename),
                    content.getBytes(),
                    java.nio.file.StandardOpenOption.CREATE,
                    java.nio.file.StandardOpenOption.WRITE
            );
        } catch (java.io.IOException e) {
            System.out.println("Error writing with NIO: " + e.getMessage());
        }
    }
}