import java.util.*;

public class Robotics {

    // Система управления роботом-манипулятором
    static class RobotArm {

        static class Joint {
            double angle;      // Угол в градусах
            double length;     // Длина звена
            double minAngle;   // Минимальный угол
            double maxAngle;   // Максимальный угол

            public Joint(double length, double minAngle, double maxAngle) {
                this.length = length;
                this.minAngle = minAngle;
                this.maxAngle = maxAngle;
                this.angle = 0;
            }

            public void setAngle(double angle) {
                this.angle = Math.max(minAngle, Math.min(maxAngle, angle));
            }
        }

        private List<Joint> joints;
        private double[] endEffectorPos;

        public RobotArm(int numJoints, double linkLength) {
            joints = new ArrayList<>();
            for (int i = 0; i < numJoints; i++) {
                joints.add(new Joint(linkLength, -180, 180));
            }
            endEffectorPos = new double[2];
            updateEndEffector();
        }

        private void updateEndEffector() {
            double x = 0, y = 0;
            double cumulativeAngle = 0;

            for (Joint joint : joints) {
                cumulativeAngle += Math.toRadians(joint.angle);
                x += joint.length * Math.cos(cumulativeAngle);
                y += joint.length * Math.sin(cumulativeAngle);
            }

            endEffectorPos[0] = x;
            endEffectorPos[1] = y;
        }

        public boolean moveTo(double targetX, double targetY) {
            // Простая обратная кинематика для 2D
            if (joints.size() == 2) {
                double l1 = joints.get(0).length;
                double l2 = joints.get(1).length;

                double distance = Math.sqrt(targetX*targetX + targetY*targetY);
                if (distance > l1 + l2 || distance < Math.abs(l1 - l2)) {
                    return false; // Цель недостижима
                }

                double cosTheta2 = (targetX*targetX + targetY*targetY - l1*l1 - l2*l2) / (2*l1*l2);
                double sinTheta2 = Math.sqrt(1 - cosTheta2*cosTheta2);

                double theta2 = Math.atan2(sinTheta2, cosTheta2);
                double theta1 = Math.atan2(targetY, targetX) -
                        Math.atan2(l2*Math.sin(theta2), l1 + l2*Math.cos(theta2));

                joints.get(0).setAngle(Math.toDegrees(theta1));
                joints.get(1).setAngle(Math.toDegrees(theta2));
                updateEndEffector();

                return true;
            }
            return false;
        }

        public void setJointAngles(double[] angles) {
            for (int i = 0; i < Math.min(angles.length, joints.size()); i++) {
                joints.get(i).setAngle(angles[i]);
            }
            updateEndEffector();
        }

        public double[] getEndEffectorPosition() {
            return endEffectorPos.clone();
        }

        public void printStatus() {
            System.out.printf("Конечный эффектор: (%.2f, %.2f)%n",
                    endEffectorPos[0], endEffectorPos[1]);
            for (int i = 0; i < joints.size(); i++) {
                System.out.printf("Сустав %d: %.1f°%n", i+1, joints.get(i).angle);
            }
        }
    }

    // Система SLAM (Simultaneous Localization and Mapping)
    static class SLAMSystem {
        private double[][] map;
        private double[] robotPosition;
        private double robotOrientation;
        private List<double[]> landmarks;

        public SLAMSystem(int mapSize) {
            map = new double[mapSize][mapSize];
            robotPosition = new double[]{mapSize/2.0, mapSize/2.0};
            robotOrientation = 0;
            landmarks = new ArrayList<>();
            initializeMap();
        }

        private void initializeMap() {
            Random random = new Random();
            for (int i = 0; i < 10; i++) {
                landmarks.add(new double[]{
                        random.nextInt(map.length),
                        random.nextInt(map[0].length)
                });
            }
        }

        public void moveRobot(double distance, double rotation) {
            // Обновление позиции робота
            robotOrientation += Math.toRadians(rotation);
            robotPosition[0] += distance * Math.cos(robotOrientation);
            robotPosition[1] += distance * Math.sin(robotOrientation);

            // Ограничение в пределах карты
            robotPosition[0] = Math.max(0, Math.min(map.length-1, robotPosition[0]));
            robotPosition[1] = Math.max(0, Math.min(map[0].length-1, robotPosition[1]));

            // Обновление карты
            updateMap();
        }

        public List<double[]> senseLandmarks() {
            List<double[]> detected = new ArrayList<>();
            double maxSenseDistance = 5.0;

            for (double[] landmark : landmarks) {
                double dx = landmark[0] - robotPosition[0];
                double dy = landmark[1] - robotPosition[1];
                double distance = Math.sqrt(dx*dx + dy*dy);

                if (distance <= maxSenseDistance) {
                    double bearing = Math.atan2(dy, dx) - robotOrientation;
                    detected.add(new double[]{distance, bearing});
                }
            }

            return detected;
        }

        private void updateMap() {
            // Простое обновление карты - отмечаем пройденные клетки
            int x = (int) Math.round(robotPosition[0]);
            int y = (int) Math.round(robotPosition[1]);

            if (x >= 0 && x < map.length && y >= 0 && y < map[0].length) {
                map[x][y] += 0.1; // Увеличиваем уверенность в занятости
            }

            // Уменьшаем уверенность со временем (забывание)
            for (int i = 0; i < map.length; i++) {
                for (int j = 0; j < map[0].length; j++) {
                    map[i][j] *= 0.99;
                }
            }
        }

        public void printMap() {
            for (int y = 0; y < map[0].length; y++) {
                for (int x = 0; x < map.length; x++) {
                    if (Math.abs(x - robotPosition[0]) < 0.5 &&
                            Math.abs(y - robotPosition[1]) < 0.5) {
                        System.out.print("R "); // Робот
                    } else if (map[x][y] > 0.5) {
                        System.out.print("# "); // Препятствие
                    } else {
                        System.out.print(". "); // Свободно
                    }
                }
                System.out.println();
            }
        }

        public double[][] getMap() {
            return map.clone();
        }

        public double[] getRobotPosition() {
            return robotPosition.clone();
        }
    }

    // Система компьютерного зрения для робота
    static class RobotVision {

        public static class ObjectDetection {
            String label;
            Rectangle boundingBox;
            double confidence;

            public ObjectDetection(String label, Rectangle boundingBox, double confidence) {
                this.label = label;
                this.boundingBox = boundingBox;
                this.confidence = confidence;
            }
        }

        public static List<ObjectDetection> detectObjects(int[][] depthImage,
                                                          int[][] colorImage) {
            List<ObjectDetection> detections = new ArrayList<>();
            Random random = new Random();

            // Симуляция детекции объектов
            if (random.nextDouble() > 0.3) {
                detections.add(new ObjectDetection("person",
                        new Rectangle(random.nextInt(100), random.nextInt(100), 40, 120),
                        0.85));
            }
            if (random.nextDouble() > 0.5) {
                detections.add(new ObjectDetection("chair",
                        new Rectangle(random.nextInt(100), random.nextInt(100), 50, 60),
                        0.78));
            }
            if (random.nextDouble() > 0.7) {
                detections.add(new ObjectDetection("table",
                        new Rectangle(random.nextInt(100), random.nextInt(100), 100, 70),
                        0.92));
            }

            return detections;
        }

        public static double calculateDepth(double disparity, double baseline, double focalLength) {
            // Расчет глубины по стереопаре
            if (disparity == 0) return Double.MAX_VALUE;
            return (baseline * focalLength) / disparity;
        }

        public static int[][] edgeDetection(int[][] image) {
            int height = image.length;
            int width = image[0].length;
            int[][] edges = new int[height][width];

            int[][] kernel = {{-1, -1, -1}, {-1, 8, -1}, {-1, -1, -1}};

            for (int y = 1; y < height - 1; y++) {
                for (int x = 1; x < width - 1; x++) {
                    int sum = 0;

                    for (int ky = -1; ky <= 1; ky++) {
                        for (int kx = -1; kx <= 1; kx++) {
                            sum += image[y + ky][x + kx] * kernel[ky + 1][kx + 1];
                        }
                    }

                    edges[y][x] = Math.min(255, Math.abs(sum));
                }
            }

            return edges;
        }

        public static double[] calculateOpticalFlow(int[][] frame1, int[][] frame2) {
            // Упрощенный расчет оптического потока
            double flowX = 0, flowY = 0;
            int count = 0;

            for (int y = 0; y < Math.min(frame1.length, frame2.length); y++) {
                for (int x = 0; x < Math.min(frame1[0].length, frame2[0].length); x++) {
                    int diff = Math.abs(frame1[y][x] - frame2[y][x]);
                    if (diff > 10) {
                        flowX += x * 0.01;
                        flowY += y * 0.01;
                        count++;
                    }
                }
            }

            if (count > 0) {
                return new double[]{flowX / count, flowY / count};
            }
            return new double[]{0, 0};
        }
    }
}