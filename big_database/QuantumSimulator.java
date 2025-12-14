import java.util.Random;

public class QuantumSimulator {

    // Квантовый бит (кубит)
    static class Qubit {
        double alpha; // Амплитуда для |0⟩
        double beta;  // Амплитуда для |1⟩

        public Qubit() {
            this.alpha = 1.0;
            this.beta = 0.0;
        }

        public void applyHadamard() {
            double newAlpha = (alpha + beta) / Math.sqrt(2);
            double newBeta = (alpha - beta) / Math.sqrt(2);
            alpha = newAlpha;
            beta = newBeta;
        }

        public void applyXGate() {
            double temp = alpha;
            alpha = beta;
            beta = temp;
        }

        public int measure() {
            double probZero = alpha * alpha;
            return new Random().nextDouble() < probZero ? 0 : 1;
        }
    }

    // Квантовая запутанность (EPR пара)
    public static Qubit[] createBellPair() {
        Qubit[] pair = new Qubit[2];
        pair[0] = new Qubit();
        pair[1] = new Qubit();

        pair[0].applyHadamard();
        // Здесь должна быть CNOT операция, но упростим
        return pair;
    }

    // Квантовая телепортация (упрощенная)
    public static int quantumTeleportation(Qubit input) {
        Qubit[] bellPair = createBellPair();

        // Измерение (упрощенно)
        int measurementResult = input.measure();

        // Коррекция на стороне получателя
        if (measurementResult == 1) {
            bellPair[1].applyXGate();
        }

        return bellPair[1].measure();
    }

    // Квантовый генератор случайных чисел
    public static int quantumRandomBit() {
        Qubit qubit = new Qubit();
        qubit.applyHadamard();
        return qubit.measure();
    }

    // Генерация квантового ключа
    public static String generateQuantumKey(int length) {
        StringBuilder key = new StringBuilder();
        for (int i = 0; i < length; i++) {
            key.append(quantumRandomBit());
        }
        return key.toString();
    }
}