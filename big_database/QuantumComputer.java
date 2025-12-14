import java.util.Random;

public class QuantumComputer {

    // Квантовый регистр (упрощенный)
    static class QuantumRegister {
        private double[] amplitudes;
        private int size;

        public QuantumRegister(int qubits) {
            this.size = (int) Math.pow(2, qubits);
            this.amplitudes = new double[size];
            amplitudes[0] = 1.0; // Все в состоянии |0...0⟩
        }

        public void applyHadamard(int qubit) {
            int step = 1 << qubit;
            for (int i = 0; i < size; i += 2 * step) {
                for (int j = 0; j < step; j++) {
                    int idx1 = i + j;
                    int idx2 = i + j + step;

                    double a = amplitudes[idx1];
                    double b = amplitudes[idx2];

                    amplitudes[idx1] = (a + b) / Math.sqrt(2);
                    amplitudes[idx2] = (a - b) / Math.sqrt(2);
                }
            }
        }

        public void applyCNOT(int control, int target) {
            for (int i = 0; i < size; i++) {
                if (((i >> control) & 1) == 1) {
                    int targetBit = (i >> target) & 1;
                    int flipped = i ^ (1 << target);
                    if (targetBit == 0) {
                        double temp = amplitudes[i];
                        amplitudes[i] = amplitudes[flipped];
                        amplitudes[flipped] = temp;
                    }
                }
            }
        }

        public int measure() {
            double[] probabilities = new double[size];
            double sum = 0;

            for (int i = 0; i < size; i++) {
                probabilities[i] = amplitudes[i] * amplitudes[i];
                sum += probabilities[i];
            }

            double random = new Random().nextDouble() * sum;
            double cumulative = 0;

            for (int i = 0; i < size; i++) {
                cumulative += probabilities[i];
                if (random <= cumulative) {
                    return i;
                }
            }

            return size - 1;
        }
    }

    // Алгоритм Дойча-Йожи
    public static boolean deutschJozsa(int[] oracle) {
        QuantumRegister qr = new QuantumRegister((int) (Math.log(oracle.length) / Math.log(2)));

        // Применяем Адамара ко всем кубитам
        for (int i = 0; i < Math.log(oracle.length) / Math.log(2); i++) {
            qr.applyHadamard(i);
        }

        // Здесь должна быть оракулова операция, но упростим
        for (int i = 0; i < oracle.length; i++) {
            if (oracle[i] == 1) {
                // Инвертируем фазу
                // В реальности это сложная операция
            }
        }

        // Снова применяем Адамара
        for (int i = 0; i < Math.log(oracle.length) / Math.log(2); i++) {
            qr.applyHadamard(i);
        }

        int result = qr.measure();
        return result == 0; // Если все нули - константная функция
    }

    // Квантовый генератор суперпозиции
    public static double[] createSuperposition(int n) {
        double[] state = new double[n];
        double amplitude = 1.0 / Math.sqrt(n);

        for (int i = 0; i < n; i++) {
            state[i] = amplitude;
        }

        return state;
    }

    // Квантовое преобразование Фурье (упрощенное)
    public static double[] quantumFourierTransform(double[] amplitudes) {
        int n = amplitudes.length;
        double[] result = new double[n];

        for (int k = 0; k < n; k++) {
            double sumReal = 0;
            double sumImag = 0;

            for (int j = 0; j < n; j++) {
                double angle = -2 * Math.PI * j * k / n;
                sumReal += amplitudes[j] * Math.cos(angle);
                sumImag += amplitudes[j] * Math.sin(angle);
            }

            result[k] = Math.sqrt(sumReal * sumReal + sumImag * sumImag) / Math.sqrt(n);
        }

        return result;
    }
}