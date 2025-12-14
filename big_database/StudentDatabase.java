import java.util.*;

class Student {
    private String name;
    private int id;
    private double grade;
    private String major;

    public Student(String name, int id, double grade, String major) {
        this.name = name;
        this.id = id;
        this.grade = grade;
        this.major = major;
    }

    public String getName() { return name; }
    public int getId() { return id; }
    public double getGrade() { return grade; }
    public String getMajor() { return major; }

    public void setGrade(double grade) {
        this.grade = grade;
    }
}

public class StudentDatabase {
    private Map<Integer, Student> students = new HashMap<>();

    // Добавление студента
    public boolean addStudent(Student student) {
        if (students.containsKey(student.getId())) {
            return false;
        }
        students.put(student.getId(), student);
        return true;
    }

    // Поиск по ID
    public Student findStudentById(int id) {
        return students.get(id);
    }

    // Поиск по имени
    public List<Student> findStudentsByName(String name) {
        List<Student> result = new ArrayList<>();
        for (Student student : students.values()) {
            if (student.getName().toLowerCase().contains(name.toLowerCase())) {
                result.add(student);
            }
        }
        return result;
    }

    // Средний балл
    public double calculateAverageGrade() {
        if (students.isEmpty()) return 0.0;
        double total = 0;
        for (Student student : students.values()) {
            total += student.getGrade();
        }
        return total / students.size();
    }

    // Лучшие студенты
    public List<Student> getTopStudents(int count) {
        return students.values().stream()
                .sorted((s1, s2) -> Double.compare(s2.getGrade(), s1.getGrade()))
                .limit(count)
                .toList();
    }

    // Студенты по специальности
    public List<Student> getStudentsByMajor(String major) {
        List<Student> result = new ArrayList<>();
        for (Student student : students.values()) {
            if (student.getMajor().equalsIgnoreCase(major)) {
                result.add(student);
            }
        }
        return result;
    }
}