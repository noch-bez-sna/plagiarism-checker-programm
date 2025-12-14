import java.util.*;

public class AutonomousDriving {

    // Система компьютерного зрения
    static class ComputerVision {
        public static class DetectedObject {
            String type; // "car", "pedestrian", "traffic_light"
            Rectangle bounds;
            double confidence;

            public DetectedObject(String type, Rectangle bounds, double confidence) {
                this.type = type;
                this.bounds = bounds;
                this.confidence = confidence;
            }
        }

        public static List<DetectedObject> detectObjects(int[][] frame) {
            List<DetectedObject> objects = new ArrayList<>();
            Random random = new Random();

            // Симуляция детекции объектов
            if (random.nextDouble() > 0.5) {
                objects.add(new DetectedObject("car",
                        new Rectangle(random.nextInt(100), random.nextInt(100), 50, 30),
                        0.85));
            }
            if (random.nextDouble() > 0.7) {
                objects.add(new DetectedObject("pedestrian",
                        new Rectangle(random.nextInt(100), random.nextInt(100), 20, 60),
                        0.92));
            }
            if (random.nextDouble() > 0.8) {
                objects.add(new DetectedObject("traffic_light",
                        new Rectangle(random.nextInt(100), random.nextInt(100), 10, 30),
                        0.95));
            }

            return objects;
        }

        public static String recognizeTrafficLightColor(int[][] trafficLightRegion) {
            // Простой анализ цвета
            int redCount = 0, greenCount = 0, yellowCount = 0;

            for (int[] row : trafficLightRegion) {
                for (int pixel : row) {
                    // Упрощенная логика определения цвета
                    if (pixel > 200) redCount++;
                    else if (pixel > 100) greenCount++;
                    else yellowCount++;
                }
            }

            if (redCount > greenCount && redCount > yellowCount) return "RED";
            if (greenCount > redCount && greenCount > yellowCount) return "GREEN";
            return "YELLOW";
        }
    }

    // Система управления
    static class ControlSystem {
        private double currentSpeed = 0;
        private double maxSpeed = 120; // км/ч
        private double acceleration = 2.0; // м/с²
        private double brakingDeceleration = 4.0; // м/с²

        public double calculateSafeSpeed(List<ComputerVision.DetectedObject> objects,
                                         double distanceToNextCar) {
            double safeSpeed = maxSpeed;

            for (ComputerVision.DetectedObject obj : objects) {
                if (obj.type.equals("car")) {
                    // Чем ближе машина, тем медленнее едем
                    double requiredSpeed = distanceToNextCar * 3.6; // Примерная формула
                    safeSpeed = Math.min(safeSpeed, requiredSpeed);
                }
                if (obj.type.equals("pedestrian")) {
                    safeSpeed = Math.min(safeSpeed, 30.0); // Ограничение возле пешеходов
                }
            }

            return Math.max(0, safeSpeed);
        }

        public void adjustSpeed(double targetSpeed) {
            if (targetSpeed > currentSpeed) {
                // Ускоряемся
                double timeToAccelerate = (targetSpeed - currentSpeed) / acceleration;
                currentSpeed = Math.min(targetSpeed, currentSpeed + acceleration * 0.1);
            } else {
                // Тормозим
                double timeToBrake = (currentSpeed - targetSpeed) / brakingDeceleration;
                currentSpeed = Math.max(targetSpeed, currentSpeed - brakingDeceleration * 0.1);
            }

            System.out.printf("Скорость: %.1f км/ч%n", currentSpeed);
        }

        public double calculateSteeringAngle(double laneDeviation, double curvature) {
            // Пропорционально-интегрально-дифференциальный регулятор (PID) упрощенный
            double kP = 0.8; // Пропорциональный коэффициент
            double kI = 0.1; // Интегральный коэффициент
            double kD = 0.2; // Дифференциальный коэффициент

            double angle = laneDeviation * kP + curvature * kI;
            angle = Math.max(-45, Math.min(45, angle)); // Ограничение угла

            return angle;
        }
    }

    // Система навигации
    static class NavigationSystem {
        private List<int[]> route;
        private int currentWaypoint = 0;

        public NavigationSystem(List<int[]> route) {
            this.route = route;
        }

        public int[] getNextWaypoint() {
            if (currentWaypoint < route.size()) {
                return route.get(currentWaypoint);
            }
            return null;
        }

        public void waypointReached() {
            currentWaypoint++;
        }

        public double calculateDistanceToDestination() {
            if (currentWaypoint >= route.size()) return 0;

            double totalDistance = 0;
            for (int i = currentWaypoint; i < route.size() - 1; i++) {
                int[] p1 = route.get(i);
                int[] p2 = route.get(i + 1);
                totalDistance += Math.sqrt(Math.pow(p2[0] - p1[0], 2) +
                        Math.pow(p2[1] - p1[1], 2));
            }

            return totalDistance;
        }

        public double calculateETA(double currentSpeed) {
            double distance = calculateDistanceToDestination();
            if (currentSpeed == 0) return Double.MAX_VALUE;
            return distance / currentSpeed * 3600; // в секундах
        }
    }

    // Главная система автономного вождения
    public static class SelfDrivingCar {
        private ControlSystem controlSystem = new ControlSystem();
        private NavigationSystem navigationSystem;
        private boolean isAutonomous = true;

        public SelfDrivingCar(List<int[]> route) {
            this.navigationSystem = new NavigationSystem(route);
        }

        public void driveAutonomously(int[][] cameraFrame) {
            if (!isAutonomous) return;

            // 1. Обнаружение объектов
            List<ComputerVision.DetectedObject> objects =
                    ComputerVision.detectObjects(cameraFrame);

            // 2. Получение следующей точки маршрута
            int[] nextWaypoint = navigationSystem.getNextWaypoint();

            // 3. Расчет безопасной скорости
            double safeSpeed = controlSystem.calculateSafeSpeed(objects, 50.0);

            // 4. Корректировка скорости
            controlSystem.adjustSpeed(safeSpeed);

            // 5. Расчет угла поворота (упрощенно)
            double laneDeviation = calculateLaneDeviation(cameraFrame);
            double steeringAngle = controlSystem.calculateSteeringAngle(laneDeviation, 0.1);

            System.out.printf("Управление: скорость=%.1f, угол=%.1f°%n",
                    safeSpeed, steeringAngle);

            // 6. Проверка достижения точки маршрута
            if (isWaypointReached(nextWaypoint)) {
                navigationSystem.waypointReached();
                System.out.println("Точка маршрута достигнута!");
            }

            // 7. Вывод информации
            System.out.printf("До цели: %.1f км, ETA: %.1f мин%n",
                    navigationSystem.calculateDistanceToDestination() / 1000,
                    navigationSystem.calculateETA(safeSpeed) / 60);
        }

        private double calculateLaneDeviation(int[][] frame) {
            // Упрощенный расчет отклонения от полосы
            return Math.sin(System.currentTimeMillis() / 1000.0) * 10;
        }

        private boolean isWaypointReached(int[] waypoint) {
            return Math.random() > 0.9; // Симуляция
        }

        public void toggleAutonomousMode() {
            isAutonomous = !isAutonomous;
            System.out.println("Автономный режим: " + (isAutonomous ? "ВКЛ" : "ВЫКЛ"));
        }

        public void emergencyStop() {
            controlSystem.adjustSpeed(0);
            System.out.println("АВАРИЙНАЯ ОСТАНОВКА!");
        }
    }
}