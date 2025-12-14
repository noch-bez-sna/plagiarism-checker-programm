import java.util.*;

public class AISentimentAnalyzer {

    private static final Set<String> POSITIVE_WORDS = Set.of(
            "good", "great", "excellent", "awesome", "happy", "love",
            "best", "perfect", "wonderful", "beautiful", "nice"
    );

    private static final Set<String> NEGATIVE_WORDS = Set.of(
            "bad", "terrible", "awful", "hate", "worst", "sad",
            "poor", "horrible", "dislike", "angry", "ugly"
    );

    public String analyzeSentiment(String text) {
        if (text == null || text.trim().isEmpty()) {
            return "NEUTRAL";
        }

        String[] words = text.toLowerCase().split("\\s+");
        int positiveCount = 0;
        int negativeCount = 0;

        for (String word : words) {
            word = word.replaceAll("[^a-zA-Z]", "");
            if (POSITIVE_WORDS.contains(word)) {
                positiveCount++;
            }
            if (NEGATIVE_WORDS.contains(word)) {
                negativeCount++;
            }
        }

        if (positiveCount > negativeCount) {
            return "POSITIVE";
        } else if (negativeCount > positiveCount) {
            return "NEGATIVE";
        } else {
            return "NEUTRAL";
        }
    }

    public double calculateSentimentScore(String text) {
        String[] words = text.toLowerCase().split("\\s+");
        int total = words.length;
        if (total == 0) return 0;

        int score = 0;
        for (String word : words) {
            word = word.replaceAll("[^a-zA-Z]", "");
            if (POSITIVE_WORDS.contains(word)) score++;
            if (NEGATIVE_WORDS.contains(word)) score--;
        }

        return (double) score / total;
    }

    public Map<String, Integer> wordFrequencyAnalysis(String text) {
        Map<String, Integer> frequency = new HashMap<>();
        String[] words = text.toLowerCase().split("\\s+");

        for (String word : words) {
            word = word.replaceAll("[^a-zA-Z]", "");
            if (!word.isEmpty()) {
                frequency.put(word, frequency.getOrDefault(word, 0) + 1);
            }
        }
        return frequency;
    }
}