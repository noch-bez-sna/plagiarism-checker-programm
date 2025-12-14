public class MachineLearning {

    // Линейная регрессия
    public static class LinearRegression {
        private double slope;
        private double intercept;

        public void train(double[] x, double[] y) {
            double sumX = 0, sumY = 0, sumXY = 0, sumX2 = 0;
            int n = x.length;

            for (int i = 0; i < n; i++) {
                sumX += x[i];
                sumY += y[i];
                sumXY += x[i] * y[i];
                sumX2 += x[i] * x[i];
            }

            slope = (n * sumXY - sumX * sumY) / (n * sumX2 - sumX * sumX);
            intercept = (sumY - slope * sumX) / n;
        }

        public double predict(double x) {
            return slope * x + intercept;
        }

        public double calculateRSquared(double[] x, double[] y) {
            double ssTotal = 0, ssResidual = 0;
            double meanY = 0;

            for (double value : y) {
                meanY += value;
            }
            meanY /= y.length;

            for (int i = 0; i < x.length; i++) {
                double prediction = predict(x[i]);
                ssTotal += Math.pow(y[i] - meanY, 2);
                ssResidual += Math.pow(y[i] - prediction, 2);
            }

            return 1 - (ssResidual / ssTotal);
        }
    }

    // K-ближайших соседей (KNN)
    public static class KNNClassifier {
        private double[][] features;
        private int[] labels;

        public void train(double[][] features, int[] labels) {
            this.features = features;
            this.labels = labels;
        }

        public int predict(double[] sample, int k) {
            PriorityQueue<Neighbor> neighbors = new PriorityQueue<>();

            for (int i = 0; i < features.length; i++) {
                double distance = euclideanDistance(sample, features[i]);
                neighbors.add(new Neighbor(distance, labels[i]));
            }

            Map<Integer, Integer> voteCount = new HashMap<>();
            for (int i = 0; i < k && !neighbors.isEmpty(); i++) {
                Neighbor neighbor = neighbors.poll();
                voteCount.put(neighbor.label, voteCount.getOrDefault(neighbor.label, 0) + 1);
            }

            return Collections.max(voteCount.entrySet(), Map.Entry.comparingByValue()).getKey();
        }

        private double euclideanDistance(double[] a, double[] b) {
            double sum = 0;
            for (int i = 0; i < a.length; i++) {
                sum += Math.pow(a[i] - b[i], 2);
            }
            return Math.sqrt(sum);
        }

        static class Neighbor implements Comparable<Neighbor> {
            double distance;
            int label;

            Neighbor(double distance, int label) {
                this.distance = distance;
                this.label = label;
            }

            @Override
            public int compareTo(Neighbor other) {
                return Double.compare(this.distance, other.distance);
            }
        }
    }
}