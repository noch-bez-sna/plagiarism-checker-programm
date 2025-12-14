// BranchingPrograms.java - Условные операторы
public class BranchingPrograms {

    // Простое условие if-else
    public String checkNumber(int number) {
        if (number > 0) {
            return "Positive";
        } else if (number < 0) {
            return "Negative";
        } else {
            return "Zero";
        }
    }

    // Вложенные условия
    public String gradeStudent(int score) {
        if (score >= 90) {
            return "A";
        } else if (score >= 80) {
            return "B";
        } else if (score >= 70) {
            return "C";
        } else if (score >= 60) {
            return "D";
        } else {
            return "F";
        }
    }

    // Оператор switch-case
    public String dayOfWeek(int day) {
        switch (day) {
            case 1:
                return "Monday";
            case 2:
                return "Tuesday";
            case 3:
                return "Wednesday";
            case 4:
                return "Thursday";
            case 5:
                return "Friday";
            case 6:
                return "Saturday";
            case 7:
                return "Sunday";
            default:
                return "Invalid day";
        }
    }

    // Тернарный оператор
    public int findMax(int a, int b) {
        return (a > b) ? a : b;
    }

    // Логические операторы
    public boolean validateAge(int age) {
        return age >= 18 && age <= 65;
    }
}