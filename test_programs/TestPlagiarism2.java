// TestPlagiarism2.java - Плагиат с изменениями
import java.util.Scanner;

public class TestPlagiarism2 {

    // Плагиат из BranchingPrograms.java с изменением имен
    public String evaluateScore(int points) {
        if (points >= 90) {
            return "Excellent";
        } else if (points >= 80) {
            return "Good";
        } else if (points >= 70) {
            return "Average";
        } else if (points >= 60) {
            return "Pass";
        } else {
            return "Fail";
        }
    }

    // Плагиат из StringProcessing.java с незначительными изменениями
    public void processText() {
        String text1 = "Hello";
        String text2 = "Programming";

        String combined = text1 + " " + text2;
        System.out.println(combined);

        System.out.println("Length: " + combined.length());
        System.out.println("Uppercase: " + combined.toUpperCase());
    }

    // Плагиат из Loops.java - factorial (изменены имена переменных)
    public int computeFactorial(int value) {
        int output = 1;
        int counter = 1;
        while (counter <= value) {
            output *= counter;
            counter++;
        }
        return output;
    }
}