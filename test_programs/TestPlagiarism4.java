// TestPlagiarism4.java - Плагиат с обработкой исключений
public class TestPlagiarism4 {

    // Плагиат из ExceptionHandling.java - safeDivision
    public int divideSafely(int numerator, int denominator) {
        try {
            return numerator / denominator;
        } catch (ArithmeticException error) {
            System.out.println("Error: Cannot divide by zero!");
            return -1;
        }
    }

    // Плагиат из TextFileProcessing.java - readFileLineByLine
    public void readTextFile(String filepath) {
        try (java.io.BufferedReader reader = new java.io.BufferedReader(
                new java.io.FileReader(filepath))) {
            String line;
            int counter = 1;
            while ((line = reader.readLine()) != null) {
                System.out.println(counter + ": " + line);
                counter++;
            }
        } catch (java.io.IOException e) {
            System.out.println("Error reading file: " + e.getMessage());
        }
    }

    // Плагиат из GenericProgramming.java - Box класс
    public class Container<T> {
        private T item;

        public Container(T item) {
            this.item = item;
        }

        public T getItem() {
            return item;
        }

        public void setItem(T item) {
            this.item = item;
        }

        public void show() {
            System.out.println("Container holds: " + item);
        }
    }

    // Плагиат из BinaryFileProcessing.java - copyBinaryFile
    public void copyFileBinary(String sourceFile, String destFile) {
        try (java.io.BufferedInputStream input = new java.io.BufferedInputStream(
                new java.io.FileInputStream(sourceFile));
             java.io.BufferedOutputStream output = new java.io.BufferedOutputStream(
                     new java.io.FileOutputStream(destFile))) {

            byte[] buffer = new byte[4096];
            int bytesRead;

            while ((bytesRead = input.read(buffer)) != -1) {
                output.write(buffer, 0, bytesRead);
            }

            System.out.println("Binary file copied");

        } catch (java.io.IOException e) {
            System.out.println("Error copying file: " + e.getMessage());
        }
    }
}