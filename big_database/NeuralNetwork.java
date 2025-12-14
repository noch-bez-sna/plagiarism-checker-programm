import java.util.Random;

public class NeuralNetwork {
    private double[][] weights;
    private Random random = new Random();

    public NeuralNetwork(int inputSize, int hiddenSize, int outputSize) {
        weights = new double[2][];
        weights[0] = new double[hiddenSize * (inputSize + 1)]; // +1 для bias
        weights[1] = new double[outputSize * (hiddenSize + 1)];

        // Инициализация случайными весами
        for (int i = 0; i < weights[0].length; i++) {
            weights[0][i] = random.nextDouble() * 2 - 1;
        }
        for (int i = 0; i < weights[1].length; i++) {
            weights[1][i] = random.nextDouble() * 2 - 1;
        }
    }

    // Сигмоидальная функция активации
    private double sigmoid(double x) {
        return 1.0 / (1.0 + Math.exp(-x));
    }

    // Производная сигмоиды
    private double sigmoidDerivative(double x) {
        return x * (1 - x);
    }

    // Прямое распространение
    public double[] feedForward(double[] inputs) {
        double[] hiddenLayer = new double[weights[0].length / (inputs.length + 1)];
        double[] outputs = new double[weights[1].length / (hiddenLayer.length + 1)];

        // Активация скрытого слоя
        for (int i = 0; i < hiddenLayer.length; i++) {
            double sum = 0;
            for (int j = 0; j < inputs.length; j++) {
                sum += inputs[j] * weights[0][i * (inputs.length + 1) + j];
            }
            sum += weights[0][i * (inputs.length + 1) + inputs.length]; // bias
            hiddenLayer[i] = sigmoid(sum);
        }

        // Активация выходного слоя
        for (int i = 0; i < outputs.length; i++) {
            double sum = 0;
            for (int j = 0; j < hiddenLayer.length; j++) {
                sum += hiddenLayer[j] * weights[1][i * (hiddenLayer.length + 1) + j];
            }
            sum += weights[1][i * (hiddenLayer.length + 1) + hiddenLayer.length]; // bias
            outputs[i] = sigmoid(sum);
        }

        return outputs;
    }

    // Обратное распространение ошибки
    public void backpropagation(double[] inputs, double[] targets, double learningRate) {
        double[] hiddenLayer = new double[weights[0].length / (inputs.length + 1)];
        double[] outputs = feedForward(inputs);

        // Вычисление ошибок
        double[] outputErrors = new double[outputs.length];
        for (int i = 0; i < outputs.length; i++) {
            outputErrors[i] = targets[i] - outputs[i];
        }

        // Обновление весов
        for (int i = 0; i < outputs.length; i++) {
            double delta = outputErrors[i] * sigmoidDerivative(outputs[i]);
            for (int j = 0; j < hiddenLayer.length; j++) {
                weights[1][i * (hiddenLayer.length + 1) + j] += learningRate * delta * hiddenLayer[j];
            }
            weights[1][i * (hiddenLayer.length + 1) + hiddenLayer.length] += learningRate * delta; // bias
        }
    }
}