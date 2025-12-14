// ExceptionHandling.java - Обработка исключений
public class ExceptionHandling {

    // Простой try-catch
    public int safeDivision(int a, int b) {
        try {
            return a / b;
        } catch (ArithmeticException e) {
            System.out.println("Error: Division by zero!");
            return 0;
        }
    }

    // Множественные catch-блоки
    public void processArray(int[] array, int index) {
        try {
            System.out.println("Value: " + array[index]);
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("Error: Invalid index!");
        } catch (NullPointerException e) {
            System.out.println("Error: Array is null!");
        }
    }

    // Try-catch-finally
    public void fileOperationExample() {
        java.io.FileWriter writer = null;
        try {
            writer = new java.io.FileWriter("test.txt");
            writer.write("Hello, World!");
        } catch (java.io.IOException e) {
            System.out.println("Error writing to file: " + e.getMessage());
        } finally {
            try {
                if (writer != null) {
                    writer.close();
                }
            } catch (java.io.IOException e) {
                System.out.println("Error closing file: " + e.getMessage());
            }
        }
    }

    // Проброс исключений
    public void riskyMethod() throws IllegalArgumentException {
        throw new IllegalArgumentException("This is a test exception");
    }

    // Создание собственных исключений
    public class NegativeNumberException extends Exception {
        public NegativeNumberException(String message) {
            super(message);
        }
    }

    public void validateNumber(int number) throws NegativeNumberException {
        if (number < 0) {
            throw new NegativeNumberException("Number cannot be negative: " + number);
        }
    }

    // Try-with-resources
    public void readFile(String filename) {
        try (java.io.BufferedReader reader = new java.io.BufferedReader(
                new java.io.FileReader(filename))) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
        } catch (java.io.IOException e) {
            System.out.println("Error reading file: " + e.getMessage());
        }
    }
}