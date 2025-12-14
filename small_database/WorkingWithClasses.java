// WorkingWithClasses.java - Классы и объекты
public class WorkingWithClasses {

    // Пример класса Student
    public class Student {
        private String name;
        private int age;
        private double gpa;

        // Конструктор
        public Student(String name, int age, double gpa) {
            this.name = name;
            this.age = age;
            this.gpa = gpa;
        }

        // Геттеры и сеттеры
        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getAge() {
            return age;
        }

        public void setAge(int age) {
            this.age = age;
        }

        public double getGpa() {
            return gpa;
        }

        public void setGpa(double gpa) {
            this.gpa = gpa;
        }

        // Метод класса
        public boolean isExcellentStudent() {
            return gpa >= 4.5;
        }

        @Override
        public String toString() {
            return String.format("Student{name='%s', age=%d, gpa=%.2f}", name, age, gpa);
        }
    }

    // Пример использования класса
    public void createAndUseStudent() {
        Student student = new Student("Alice", 20, 4.7);

        System.out.println("Student: " + student);
        System.out.println("Name: " + student.getName());
        System.out.println("Is excellent: " + student.isExcellentStudent());

        // Изменение данных
        student.setGpa(4.9);
        System.out.println("Updated GPA: " + student.getGpa());
    }

    // Статические методы и переменные
    public static class MathUtils {
        public static final double PI = 3.14159;

        public static int add(int a, int b) {
            return a + b;
        }

        public static int multiply(int a, int b) {
            return a * b;
        }
    }
}