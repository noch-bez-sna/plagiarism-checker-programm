import java.util.Arrays;

public class StatisticsCalculator {

    // Среднее значение
    public static double mean(double[] data) {
        double sum = 0;
        for (double value : data) {
            sum += value;
        }
        return sum / data.length;
    }

    // Медиана
    public static double median(double[] data) {
        Arrays.sort(data);
        int middle = data.length / 2;
        if (data.length % 2 == 0) {
            return (data[middle - 1] + data[middle]) / 2.0;
        } else {
            return data[middle];
        }
    }

    // Мода
    public static double mode(double[] data) {
        java.util.Map<Double, Integer> frequency = new java.util.HashMap<>();
        for (double value : data) {
            frequency.put(value, frequency.getOrDefault(value, 0) + 1);
        }

        double mode = data[0];
        int maxCount = 0;
        for (java.util.Map.Entry<Double, Integer> entry : frequency.entrySet()) {
            if (entry.getValue() > maxCount) {
                maxCount = entry.getValue();
                mode = entry.getKey();
            }
        }
        return mode;
    }

    // Дисперсия
    public static double variance(double[] data) {
        double mean = mean(data);
        double sumSquaredDiff = 0;
        for (double value : data) {
            sumSquaredDiff += Math.pow(value - mean, 2);
        }
        return sumSquaredDiff / data.length;
    }

    // Стандартное отклонение
    public static double standardDeviation(double[] data) {
        return Math.sqrt(variance(data));
    }

    // Минимальное значение
    public static double min(double[] data) {
        double min = data[0];
        for (double value : data) {
            if (value < min) min = value;
        }
        return min;
    }

    // Максимальное значение
    public static double max(double[] data) {
        double max = data[0];
        for (double value : data) {
            if (value > max) max = value;
        }
        return max;
    }
}