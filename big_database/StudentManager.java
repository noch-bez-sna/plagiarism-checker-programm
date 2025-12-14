import java.util.*;

class Student {
    private String name;
    private int id;
    private double grade;

    public Student(String name, int id, double grade) {
        this.name = name;
        this.id = id;
        this.grade = grade;
    }

    // Геттеры и сеттеры
    public String getName() { return name; }
    public int getId() { return id; }
    public double getGrade() { return grade; }
}

public class StudentManager {
    private List<Student> students = new ArrayList<>();

    public void addStudent(Student student) {
        students.add(student);
    }

    public Student findStudentById(int id) {
        return students.stream()
                .filter(s -> s.getId() == id)
                .findFirst()
                .orElse(null);
    }

    public double calculateAverageGrade() {
        return students.stream()
                .mapToDouble(Student::getGrade)
                .average()
                .orElse(0.0);
    }
}