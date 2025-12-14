// GenericProgramming.java - Дженерики
public class GenericProgramming {

    // Обобщённый класс
    public class Box<T> {
        private T content;

        public Box(T content) {
            this.content = content;
        }

        public T getContent() {
            return content;
        }

        public void setContent(T content) {
            this.content = content;
        }

        public void display() {
            System.out.println("Box contains: " + content);
        }
    }

    // Использование обобщённого класса
    public void useBox() {
        Box<String> stringBox = new Box<>("Hello");
        Box<Integer> integerBox = new Box<>(42);

        stringBox.display();
        integerBox.display();
    }

    // Обобщённый метод
    public <T> void printArray(T[] array) {
        for (T element : array) {
            System.out.print(element + " ");
        }
        System.out.println();
    }

    // Ограничения типов (bounds)
    public <T extends Number> double sumNumbers(T[] numbers) {
        double sum = 0;
        for (T number : numbers) {
            sum += number.doubleValue();
        }
        return sum;
    }

    // Обобщённый интерфейс
    public interface Comparator<T> {
        int compare(T a, T b);
    }

    // Множественные параметры типов
    public class Pair<K, V> {
        private K key;
        private V value;

        public Pair(K key, V value) {
            this.key = key;
            this.value = value;
        }

        public K getKey() {
            return key;
        }

        public V getValue() {
            return value;
        }

        public void setKey(K key) {
            this.key = key;
        }

        public void setValue(V value) {
            this.value = value;
        }
    }

    // Wildcards
    public void processList(java.util.List<? extends Number> numbers) {
        for (Number number : numbers) {
            System.out.println(number.doubleValue());
        }
    }
}