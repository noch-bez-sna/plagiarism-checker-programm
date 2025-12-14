import java.util.Random;
import java.util.Scanner;

public class GameUtils {
    private static final Random random = new Random();
    private static final Scanner scanner = new Scanner(System.in);

    // Генерация случайного числа в диапазоне
    public static int randomInt(int min, int max) {
        return random.nextInt(max - min + 1) + min;
    }

    // Бросок игральной кости
    public static int rollDice() {
        return randomInt(1, 6);
    }

    // Подбрасывание монетки
    public static String flipCoin() {
        return random.nextBoolean() ? "HEADS" : "TAILS";
    }

    // Простая игра "Угадай число"
    public static void guessNumberGame() {
        int numberToGuess = randomInt(1, 100);
        int attempts = 0;
        boolean guessed = false;

        System.out.println("Угадайте число от 1 до 100!");

        while (!guessed && attempts < 10) {
            System.out.print("Ваша попытка: ");
            int guess = scanner.nextInt();
            attempts++;

            if (guess == numberToGuess) {
                System.out.println("Поздравляем! Вы угадали за " + attempts + " попыток!");
                guessed = true;
            } else if (guess < numberToGuess) {
                System.out.println("Загаданное число больше");
            } else {
                System.out.println("Загаданное число меньше");
            }
        }

        if (!guessed) {
            System.out.println("Вы проиграли! Загаданное число было: " + numberToGuess);
        }
    }

    // Генератор имен для игр
    public static String generateRandomName() {
        String[] prefixes = {"Dark", "Light", "Shadow", "Fire", "Ice", "Thunder", "Storm", "Iron", "Golden"};
        String[] suffixes = {"blade", "heart", "fist", "wolf", "dragon", "phoenix", "knight", "mage", "rogue"};

        return prefixes[random.nextInt(prefixes.length)] +
                suffixes[random.nextInt(suffixes.length)];
    }

    // Расчет урона с учетом брони
    public static int calculateDamage(int attack, int defense) {
        int damage = attack - defense / 2;
        return Math.max(1, damage); // Минимальный урон 1
    }
}