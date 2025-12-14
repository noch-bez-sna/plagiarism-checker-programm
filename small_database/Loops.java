// Loops.java - Различные типы циклов
public class Loops {

    // Цикл for
    public int sumNumbers(int n) {
        int sum = 0;
        for (int i = 1; i <= n; i++) {
            sum += i;
        }
        return sum;
    }

    // Цикл while
    public int factorial(int n) {
        int result = 1;
        int i = 1;
        while (i <= n) {
            result *= i;
            i++;
        }
        return result;
    }

    // Цикл do-while
    public void countDown(int start) {
        int i = start;
        do {
            System.out.println(i);
            i--;
        } while (i > 0);
        System.out.println("Liftoff!");
    }

    // Вложенные циклы
    public void multiplicationTable(int n) {
        for (int i = 1; i <= n; i++) {
            for (int j = 1; j <= n; j++) {
                System.out.printf("%4d", i * j);
            }
            System.out.println();
        }
    }

    // Цикл for-each
    public int sumArray(int[] numbers) {
        int sum = 0;
        for (int num : numbers) {
            sum += num;
        }
        return sum;
    }

    // Break и continue
    public void findFirstEven(int[] numbers) {
        for (int num : numbers) {
            if (num % 2 == 0) {
                System.out.println("First even number: " + num);
                break;
            }
        }
    }
}