import java.util.HashMap;
import java.util.Map;

public class TextProcessor {

    // Подсчет слов
    public int countWords(String text) {
        if (text == null || text.trim().isEmpty()) {
            return 0;
        }
        String[] words = text.trim().split("\\s+");
        return words.length;
    }

    // Частота символов
    public Map<Character, Integer> characterFrequency(String text) {
        Map<Character, Integer> frequency = new HashMap<>();
        for (char c : text.toCharArray()) {
            frequency.put(c, frequency.getOrDefault(c, 0) + 1);
        }
        return frequency;
    }

    // Удаление дубликатов
    public String removeDuplicates(String text) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            char current = text.charAt(i);
            if (result.indexOf(String.valueOf(current)) == -1) {
                result.append(current);
            }
        }
        return result.toString();
    }

    // Форматирование строки (каждое слово с большой буквы)
    public String toTitleCase(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        String[] words = text.split(" ");
        StringBuilder result = new StringBuilder();
        for (String word : words) {
            if (!word.isEmpty()) {
                result.append(Character.toUpperCase(word.charAt(0)))
                        .append(word.substring(1).toLowerCase())
                        .append(" ");
            }
        }
        return result.toString().trim();
    }
}