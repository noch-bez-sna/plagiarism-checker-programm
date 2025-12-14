// TestPlagiarism5.java - Смешанный плагиат
public class TestPlagiarism5 {

    // Комбинация из разных источников

    // Из WorkingWithClasses.java - класс Student (упрощенный)
    public class Pupil {
        private String fullName;
        private int years;
        private double average;

        public Pupil(String fullName, int years, double average) {
            this.fullName = fullName;
            this.years = years;
            this.average = average;
        }

        public String getFullName() { return fullName; }
        public int getYears() { return years; }
        public double getAverage() { return average; }

        public boolean isTopStudent() {
            return average >= 4.5;
        }
    }

    // Из Collections.java - mapExamples (упрощено)
    public void showMap() {
        java.util.Map<String, Integer> data = new java.util.HashMap<>();

        data.put("John", 85);
        data.put("Mary", 92);
        data.put("Peter", 78);

        for (java.util.Map.Entry<String, Integer> entry : data.entrySet()) {
            System.out.println(entry.getKey() + ": " + entry.getValue());
        }
    }

    // Из Loops.java - sumArray (переименовано)
    public int totalSum(int[] values) {
        int total = 0;
        for (int val : values) {
            total += val;
        }
        return total;
    }

    // Из StringProcessing.java - formatString (изменены имена параметров)
    public String createReport(String person, int yearsOld, double income) {
        return String.format("Person: %s, Age: %d, Income: %.2f",
                person, yearsOld, income);
    }

    // Из BranchingPrograms.java - dayOfWeek (добавлены дни)
    public String getWeekDay(int dayNum) {
        switch (dayNum) {
            case 1: return "Monday";
            case 2: return "Tuesday";
            case 3: return "Wednesday";
            case 4: return "Thursday";
            case 5: return "Friday";
            case 6: return "Saturday";
            case 7: return "Sunday";
            default: return "Invalid";
        }
    }

    // Оригинальный метод
    public void displayInfo() {
        System.out.println("This file contains mixed plagiarism examples");
        System.out.println("Some code is copied, some is modified, some is original");
    }
}