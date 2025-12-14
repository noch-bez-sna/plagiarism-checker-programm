// StringProcessing.java - Работа со строками
public class StringProcessing {

    // Основные операции со строками
    public void stringBasics() {
        String str1 = "Hello";
        String str2 = "World";

        // Конкатенация
        String result = str1 + " " + str2;
        System.out.println(result);

        // Длина строки
        System.out.println("Length: " + result.length());

        // Сравнение строк
        System.out.println("Equals: " + str1.equals("Hello"));

        // Преобразование регистра
        System.out.println("Upper: " + result.toUpperCase());
        System.out.println("Lower: " + result.toLowerCase());
    }

    // Поиск в строках
    public boolean containsSubstring(String text, String substring) {
        return text.contains(substring);
    }

    // Разделение строки
    public String[] splitString(String text, String delimiter) {
        return text.split(delimiter);
    }

    // Замена в строке
    public String replaceInString(String text, String oldStr, String newStr) {
        return text.replace(oldStr, newStr);
    }

    // Извлечение подстроки
    public String extractSubstring(String text, int start, int end) {
        return text.substring(start, end);
    }

    // Удаление пробелов
    public String trimString(String text) {
        return text.trim();
    }

    // Форматирование строки
    public String formatString(String name, int age, double salary) {
        return String.format("Name: %s, Age: %d, Salary: %.2f", name, age, salary);
    }
}