// TestPlagiarism1.java - Файл с частичным плагиатом
public class TestPlagiarism1 {

    // Плагиат из Introduction.java - метод variableDeclaration
    public void declareVariables() {
        int number = 10;
        double pi = 3.14159;
        String message = "Welcome to Java";
        boolean flag = true;

        System.out.println("Number: " + number);
        System.out.println("Pi: " + pi);
        System.out.println("Message: " + message);
        System.out.println("Flag: " + flag);
    }

    // Плагиат из Loops.java - метод sumNumbers (немного изменен)
    public int calculateSum(int n) {
        int sum = 0;
        for (int i = 1; i <= n; i++) {
            sum += i;
        }
        return sum;
    }

    // Оригинальный код
    public void originalMethod() {
        System.out.println("This is original code");
        int[] arr = {1, 2, 3, 4, 5};
        for (int num : arr) {
            System.out.print(num + " ");
        }
    }
}