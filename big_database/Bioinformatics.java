import java.util.*;

public class Bioinformatics {

    // Выравнивание последовательностей (алгоритм Нидлмана-Вунша)
    public static class SequenceAlignment {
        private int matchScore = 2;
        private int mismatchPenalty = -1;
        private int gapPenalty = -2;

        public AlignmentResult align(String seq1, String seq2) {
            int n = seq1.length();
            int m = seq2.length();

            int[][] scoreMatrix = new int[n + 1][m + 1];

            // Инициализация
            for (int i = 0; i <= n; i++) {
                scoreMatrix[i][0] = i * gapPenalty;
            }
            for (int j = 0; j <= m; j++) {
                scoreMatrix[0][j] = j * gapPenalty;
            }

            // Заполнение матрицы
            for (int i = 1; i <= n; i++) {
                for (int j = 1; j <= m; j++) {
                    int match = scoreMatrix[i-1][j-1] +
                            (seq1.charAt(i-1) == seq2.charAt(j-1) ? matchScore : mismatchPenalty);
                    int delete = scoreMatrix[i-1][j] + gapPenalty;
                    int insert = scoreMatrix[i][j-1] + gapPenalty;

                    scoreMatrix[i][j] = Math.max(match, Math.max(delete, insert));
                }
            }

            // Восстановление выравнивания
            StringBuilder aligned1 = new StringBuilder();
            StringBuilder aligned2 = new StringBuilder();

            int i = n, j = m;
            while (i > 0 || j > 0) {
                if (i > 0 && j > 0 &&
                        scoreMatrix[i][j] == scoreMatrix[i-1][j-1] +
                                (seq1.charAt(i-1) == seq2.charAt(j-1) ? matchScore : mismatchPenalty)) {
                    aligned1.append(seq1.charAt(i-1));
                    aligned2.append(seq2.charAt(j-1));
                    i--;
                    j--;
                } else if (i > 0 && scoreMatrix[i][j] == scoreMatrix[i-1][j] + gapPenalty) {
                    aligned1.append(seq1.charAt(i-1));
                    aligned2.append('-');
                    i--;
                } else {
                    aligned1.append('-');
                    aligned2.append(seq2.charAt(j-1));
                    j--;
                }
            }

            return new AlignmentResult(
                    aligned1.reverse().toString(),
                    aligned2.reverse().toString(),
                    scoreMatrix[n][m]
            );
        }
    }

    static class AlignmentResult {
        String alignedSeq1;
        String alignedSeq2;
        int score;

        public AlignmentResult(String alignedSeq1, String alignedSeq2, int score) {
            this.alignedSeq1 = alignedSeq1;
            this.alignedSeq2 = alignedSeq2;
            this.score = score;
        }

        public double calculateSimilarity() {
            int matches = 0;
            int length = alignedSeq1.length();

            for (int i = 0; i < length; i++) {
                if (alignedSeq1.charAt(i) == alignedSeq2.charAt(i)) {
                    matches++;
                }
            }

            return (double) matches / length * 100;
        }
    }

    // Анализ ДНК последовательности
    public static class DNAAnalyzer {

        public static Map<Character, Integer> nucleotideFrequency(String dna) {
            Map<Character, Integer> freq = new HashMap<>();
            dna = dna.toUpperCase();

            for (char c : dna.toCharArray()) {
                if ("ATCG".indexOf(c) != -1) {
                    freq.put(c, freq.getOrDefault(c, 0) + 1);
                }
            }

            return freq;
        }

        public static double gcContent(String dna) {
            dna = dna.toUpperCase();
            int gcCount = 0;

            for (char c : dna.toCharArray()) {
                if (c == 'G' || c == 'C') {
                    gcCount++;
                }
            }

            return dna.isEmpty() ? 0 : (double) gcCount / dna.length() * 100;
        }

        public static String reverseComplement(String dna) {
            StringBuilder complement = new StringBuilder();
            dna = dna.toUpperCase();

            for (int i = dna.length() - 1; i >= 0; i--) {
                char c = dna.charAt(i);
                switch (c) {
                    case 'A': complement.append('T'); break;
                    case 'T': complement.append('A'); break;
                    case 'C': complement.append('G'); break;
                    case 'G': complement.append('C'); break;
                    default: complement.append(c);
                }
            }

            return complement.toString();
        }

        public static List<String> findORF(String dna, int minLength) {
            List<String> orfs = new ArrayList<>();
            String[] frames = {dna, dna.substring(1), dna.substring(2)};

            for (String frame : frames) {
                for (int i = 0; i <= frame.length() - 3; i += 3) {
                    String codon = frame.substring(i, i + 3);
                    if (codon.equals("ATG")) { // Start codon
                        for (int j = i + 3; j <= frame.length() - 3; j += 3) {
                            String stopCodon = frame.substring(j, j + 3);
                            if (stopCodon.equals("TAA") || stopCodon.equals("TAG") || stopCodon.equals("TGA")) {
                                String orf = frame.substring(i, j + 3);
                                if (orf.length() >= minLength) {
                                    orfs.add(orf);
                                }
                                break;
                            }
                        }
                    }
                }
            }

            return orfs;
        }
    }

    // Анализ белковых последовательностей
    public static class ProteinAnalyzer {

        private static final Map<String, Double> AMINO_ACID_MASS = Map.ofEntries(
                Map.entry("A", 89.09), Map.entry("R", 174.20), Map.entry("N", 132.12),
                Map.entry("D", 133.10), Map.entry("C", 121.16), Map.entry("E", 147.13),
                Map.entry("Q", 146.15), Map.entry("G", 75.07), Map.entry("H", 155.16),
                Map.entry("I", 131.17), Map.entry("L", 131.17), Map.entry("K", 146.19),
                Map.entry("M", 149.21), Map.entry("F", 165.19), Map.entry("P", 115.13),
                Map.entry("S", 105.09), Map.entry("T", 119.12), Map.entry("W", 204.23),
                Map.entry("Y", 181.19), Map.entry("V", 117.15)
        );

        public static double calculateMolecularWeight(String protein) {
            double weight = 0;
            protein = protein.toUpperCase();

            for (int i = 0; i < protein.length(); i++) {
                String aa = protein.substring(i, i + 1);
                weight += AMINO_ACID_MASS.getOrDefault(aa, 0.0);
            }

            // Учитываем воду (H2O) на каждый пептид
            weight -= (protein.length() - 1) * 18.02;

            return weight;
        }

        public static double calculateIsoelectricPoint(String protein) {
            // Упрощенный расчет pI
            int acidicCount = 0; // D, E
            int basicCount = 0;  // R, K, H

            protein = protein.toUpperCase();
            for (char c : protein.toCharArray()) {
                if (c == 'D' || c == 'E') acidicCount++;
                if (c == 'R' || c == 'K' || c == 'H') basicCount++;
            }

            return 6.5 + (basicCount - acidicCount) * 0.1;
        }

        public static String predictSecondaryStructure(String protein) {
            // Упрощенный алгоритм Chou-Fasman
            StringBuilder structure = new StringBuilder();

            for (int i = 0; i < protein.length(); i++) {
                char aa = protein.charAt(i);

                // Простые правила
                if (aa == 'A' || aa == 'E' || aa == 'L' || aa == 'M') {
                    structure.append('H'); // Альфа-спираль
                } else if (aa == 'V' || aa == 'I' || aa == 'Y' || aa == 'F') {
                    structure.append('S'); // Бета-лист
                } else {
                    structure.append('C'); // Неупорядоченный
                }
            }

            return structure.toString();
        }

        public static Map<String, Integer> aminoAcidComposition(String protein) {
            Map<String, Integer> composition = new HashMap<>();
            protein = protein.toUpperCase();

            for (int i = 0; i < protein.length(); i++) {
                String aa = protein.substring(i, i + 1);
                composition.put(aa, composition.getOrDefault(aa, 0) + 1);
            }

            return composition;
        }
    }
}