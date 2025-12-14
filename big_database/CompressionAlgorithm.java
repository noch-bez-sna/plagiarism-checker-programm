import java.util.*;

public class CompressionAlgorithm {

    // Алгоритм RLE (Run-Length Encoding)
    public static String rleCompress(String input) {
        if (input == null || input.isEmpty()) return "";

        StringBuilder compressed = new StringBuilder();
        int count = 1;
        char current = input.charAt(0);

        for (int i = 1; i < input.length(); i++) {
            if (input.charAt(i) == current) {
                count++;
            } else {
                compressed.append(current).append(count);
                current = input.charAt(i);
                count = 1;
            }
        }
        compressed.append(current).append(count);

        return compressed.toString();
    }

    public static String rleDecompress(String compressed) {
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < compressed.length(); i += 2) {
            char character = compressed.charAt(i);
            int count = Character.getNumericValue(compressed.charAt(i + 1));

            for (int j = 0; j < count; j++) {
                result.append(character);
            }
        }

        return result.toString();
    }

    // Простое сжатие по словарю
    public static Map<String, String> createDictionary(String text) {
        Map<String, String> dictionary = new HashMap<>();
        String[] words = text.split("\\s+");
        int code = 0;

        for (String word : words) {
            if (!dictionary.containsKey(word)) {
                dictionary.put(word, "C" + code++);
            }
        }
        return dictionary;
    }

    public static String compressWithDictionary(String text, Map<String, String> dictionary) {
        String[] words = text.split("\\s+");
        StringBuilder compressed = new StringBuilder();

        for (String word : words) {
            compressed.append(dictionary.get(word)).append(" ");
        }

        return compressed.toString().trim();
    }
}