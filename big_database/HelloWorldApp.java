public class HelloWorldApp {
    public static void main(String[] args) {
        System.out.println("Hello, World!");
        int x = 10;
        int y = 20;
        int sum = x + y;
        System.out.println("Sum: " + sum);
    }

    public static void printGreeting(String name) {
        System.out.println("Hello, " + name + "!");
    }

    public void printNumbers(int n) {
        for (int i = 1; i <= n; i++) {
            System.out.print(i + " ");
        }
        System.out.println();
    }
}