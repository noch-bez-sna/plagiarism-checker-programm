// Collections.java - Коллекции Java
public class Collections {

    // Работа со списками
    public void listExamples() {
        java.util.List<String> fruits = new java.util.ArrayList<>();

        // Добавление элементов
        fruits.add("Apple");
        fruits.add("Banana");
        fruits.add("Orange");

        // Получение элементов
        System.out.println("First fruit: " + fruits.get(0));

        // Итерация по списку
        for (String fruit : fruits) {
            System.out.println(fruit);
        }

        // Удаление элемента
        fruits.remove("Banana");

        // Проверка размера
        System.out.println("Size: " + fruits.size());

        // Проверка наличия элемента
        System.out.println("Contains Apple: " + fruits.contains("Apple"));
    }

    // Работа с множествами
    public void setExamples() {
        java.util.Set<Integer> numbers = new java.util.HashSet<>();

        numbers.add(1);
        numbers.add(2);
        numbers.add(3);
        numbers.add(1); // Дубликат не добавится

        System.out.println("Set size: " + numbers.size());

        // Проверка наличия элемента
        System.out.println("Contains 2: " + numbers.contains(2));
    }

    // Работа с отображениями
    public void mapExamples() {
        java.util.Map<String, Integer> scores = new java.util.HashMap<>();

        // Добавление пар ключ-значение
        scores.put("Alice", 95);
        scores.put("Bob", 87);
        scores.put("Charlie", 92);

        // Получение значения по ключу
        System.out.println("Alice's score: " + scores.get("Alice"));

        // Проверка наличия ключа
        System.out.println("Has Bob: " + scores.containsKey("Bob"));

        // Итерация по записям
        for (java.util.Map.Entry<String, Integer> entry : scores.entrySet()) {
            System.out.println(entry.getKey() + ": " + entry.getValue());
        }
    }

    // Сортировка коллекций
    public void sortList() {
        java.util.List<Integer> numbers = new java.util.ArrayList<>();
        numbers.add(5);
        numbers.add(1);
        numbers.add(8);
        numbers.add(3);

        // Сортировка
        java.util.Collections.sort(numbers);

        System.out.println("Sorted list: " + numbers);
    }

    // Очередь
    public void queueExample() {
        java.util.Queue<String> queue = new java.util.LinkedList<>();

        queue.offer("First");
        queue.offer("Second");
        queue.offer("Third");

        // Извлечение элементов
        while (!queue.isEmpty()) {
            System.out.println(queue.poll());
        }
    }

    // Стек
    public void stackExample() {
        java.util.Stack<String> stack = new java.util.Stack<>();

        stack.push("First");
        stack.push("Second");
        stack.push("Third");

        // Извлечение элементов (LIFO)
        while (!stack.isEmpty()) {
            System.out.println(stack.pop());
        }
    }
}