// TestPlagiarism3.java - Почти прямой плагиат
public class TestPlagiarism3 {

    /*
     * Этот метод взят из Introduction.java
     * с незначительными изменениями
     */
    public static void sayHello() {
        System.out.println("Hello, World!");
    }

    /*
     * Метод из BranchingPrograms.java
     * с изменением текстовых сообщений
     */
    public String checkValue(int value) {
        if (value > 0) {
            return "Positive number";
        } else if (value < 0) {
            return "Negative number";
        } else {
            return "Zero";
        }
    }

    /*
     * Метод из Collections.java - listExamples
     * с изменением содержимого списка
     */
    public void demonstrateList() {
        java.util.List<String> colors = new java.util.ArrayList<>();

        colors.add("Red");
        colors.add("Green");
        colors.add("Blue");

        System.out.println("First color: " + colors.get(0));

        for (String color : colors) {
            System.out.println(color);
        }

        colors.remove("Green");
        System.out.println("Size: " + colors.size());
    }

    // Оригинальная часть
    private int calculate(int x, int y) {
        return (x * y) + (x - y);
    }
}