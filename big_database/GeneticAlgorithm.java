import java.util.Arrays;
import java.util.Random;

public class GeneticAlgorithm {
    private static final Random random = new Random();

    // Генерация начальной популяции
    public static int[][] generatePopulation(int populationSize, int chromosomeLength) {
        int[][] population = new int[populationSize][chromosomeLength];
        for (int i = 0; i < populationSize; i++) {
            for (int j = 0; j < chromosomeLength; j++) {
                population[i][j] = random.nextInt(2); // 0 или 1
            }
        }
        return population;
    }

    // Функция приспособленности (пример)
    public static int fitnessFunction(int[] chromosome) {
        return Arrays.stream(chromosome).sum(); // Чем больше единиц, тем лучше
    }

    // Селекция (турнирная)
    public static int[][] selection(int[][] population) {
        int[][] selected = new int[population.length][];
        for (int i = 0; i < population.length; i++) {
            // Турнир из 3 особей
            int a = random.nextInt(population.length);
            int b = random.nextInt(population.length);
            int c = random.nextInt(population.length);

            int fa = fitnessFunction(population[a]);
            int fb = fitnessFunction(population[b]);
            int fc = fitnessFunction(population[c]);

            if (fa >= fb && fa >= fc) selected[i] = population[a];
            else if (fb >= fa && fb >= fc) selected[i] = population[b];
            else selected[i] = population[c];
        }
        return selected;
    }

    // Кроссовер (одноточечный)
    public static int[][] crossover(int[][] parents) {
        int[][] offspring = new int[parents.length][parents[0].length];
        for (int i = 0; i < parents.length; i += 2) {
            int crossoverPoint = random.nextInt(parents[i].length);

            for (int j = 0; j < crossoverPoint; j++) {
                offspring[i][j] = parents[i][j];
                offspring[i + 1][j] = parents[i + 1][j];
            }
            for (int j = crossoverPoint; j < parents[i].length; j++) {
                offspring[i][j] = parents[i + 1][j];
                offspring[i + 1][j] = parents[i][j];
            }
        }
        return offspring;
    }

    // Мутация
    public static void mutate(int[][] population, double mutationRate) {
        for (int[] chromosome : population) {
            for (int j = 0; j < chromosome.length; j++) {
                if (random.nextDouble() < mutationRate) {
                    chromosome[j] = 1 - chromosome[j]; // Инвертируем бит
                }
            }
        }
    }
}