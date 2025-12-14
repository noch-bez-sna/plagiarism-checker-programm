// Introduction.java - Основные концепции Java
public class Introduction {

    // Простейшая программа Hello World
    public static void helloWorld() {
        System.out.println("Hello, World!");
    }

    // Объявление переменных разных типов
    public void variableDeclaration() {
        int number = 10;
        double pi = 3.14159;
        String message = "Welcome to Java";
        boolean flag = true;

        System.out.println("Number: " + number);
        System.out.println("Pi: " + pi);
        System.out.println("Message: " + message);
        System.out.println("Flag: " + flag);
    }

    // Основные арифметические операции
    public int arithmeticOperations(int a, int b) {
        int sum = a + b;
        int difference = a - b;
        int product = a * b;
        int quotient = a / b;
        int remainder = a % b;

        return sum + difference + product + quotient + remainder;
    }

    // Ввод/вывод с консоли (имитация)
    public void inputOutputExample() {
        // Имитация ввода
        String name = "John";
        int age = 25;

        // Вывод форматированной строки
        System.out.printf("Name: %s, Age: %d%n", name, age);
    }
}