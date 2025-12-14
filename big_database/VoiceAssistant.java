import java.util.*;
import java.time.LocalTime;

public class VoiceAssistant {

    private String name;
    private Map<String, Runnable> commands;

    public VoiceAssistant(String name) {
        this.name = name;
        this.commands = new HashMap<>();
        initializeCommands();
    }

    private void initializeCommands() {
        commands.put("привет", () -> System.out.println("Привет! Я " + name + ", чем могу помочь?"));
        commands.put("время", () -> System.out.println("Сейчас: " + LocalTime.now().format(
                java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss"))));
        commands.put("дата", () -> System.out.println("Сегодня: " + java.time.LocalDate.now()));
        commands.put("погода", () -> System.out.println("Погода: Солнечно, +22°C"));
        commands.put("шутка", this::tellJoke);
        commands.put("помощь", this::showHelp);
        commands.put("калькулятор", this::startCalculator);
    }

    public void processCommand(String command) {
        command = command.toLowerCase().trim();

        if (commands.containsKey(command)) {
            commands.get(command).run();
        } else if (command.contains("сложи")) {
            calculate(command, "+");
        } else if (command.contains("умнож")) {
            calculate(command, "*");
        } else {
            System.out.println("Извините, не понял команду. Скажите 'помощь' для списка команд.");
        }
    }

    private void calculate(String command, String operator) {
        try {
            String[] parts = command.split("\\s+");
            List<Integer> numbers = new ArrayList<>();

            for (String part : parts) {
                try {
                    numbers.add(Integer.parseInt(part));
                } catch (NumberFormatException e) {
                    // Игнорируем не-числа
                }
            }

            if (numbers.size() >= 2) {
                int result = 0;
                if (operator.equals("+")) {
                    result = numbers.stream().mapToInt(Integer::intValue).sum();
                    System.out.println("Результат: " + result);
                } else if (operator.equals("*")) {
                    result = 1;
                    for (int num : numbers) {
                        result *= num;
                    }
                    System.out.println("Результат: " + result);
                }
            }
        } catch (Exception e) {
            System.out.println("Не удалось выполнить вычисление");
        }
    }

    private void tellJoke() {
        String[] jokes = {
                "Почему программисты путают Хэллоуин и Рождество? Потому что Oct 31 == Dec 25!",
                "Сколько программистов нужно, чтобы вкрутить лампочку? Ни одного, это hardware проблема!",
                "Заходит как-то программист в бар и говорит: 'Hello, world!'"
        };
        System.out.println(jokes[new Random().nextInt(jokes.length)]);
    }

    private void showHelp() {
        System.out.println("Доступные команды:");
        commands.keySet().forEach(cmd -> System.out.println(" - " + cmd));
        System.out.println(" - сложи [числа] - сложить числа");
        System.out.println(" - умнож [числа] - умножить числа");
    }

    private void startCalculator() {
        System.out.println("Запущен калькулятор. Примеры:");
        System.out.println("  сложи 5 10 15 → 30");
        System.out.println("  умнож 2 3 4 → 24");
    }

    public void setAlarm(LocalTime time, String message) {
        new Thread(() -> {
            while (true) {
                if (LocalTime.now().isAfter(time)) {
                    System.out.println("Будильник! " + message);
                    break;
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }).start();
        System.out.println("Будильник установлен на " + time);
    }
}