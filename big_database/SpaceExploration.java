import java.util.*;

public class SpaceExploration {

    // Симулятор орбитальной механики
    static class OrbitalMechanics {

        public static class Orbit {
            double semiMajorAxis; // Большая полуось (км)
            double eccentricity;  // Эксцентриситет
            double inclination;   // Наклонение (градусы)
            double period;        // Период обращения (секунды)

            public Orbit(double a, double e, double i) {
                this.semiMajorAxis = a;
                this.eccentricity = e;
                this.inclination = i;
                this.period = 2 * Math.PI * Math.sqrt(Math.pow(a, 3) / (3.986e5));
            }

            public double calculateOrbitalSpeed(double trueAnomaly) {
                double mu = 3.986e5; // Гравитационный параметр Земли (км³/с²)
                double r = semiMajorAxis * (1 - eccentricity * eccentricity) /
                        (1 + eccentricity * Math.cos(trueAnomaly));
                return Math.sqrt(mu * (2/r - 1/semiMajorAxis));
            }

            public double[] calculatePosition(double time) {
                // Упрощенный расчет положения на орбите
                double meanAnomaly = 2 * Math.PI * time / period;
                double eccentricAnomaly = solveKepler(meanAnomaly, eccentricity);

                double x = semiMajorAxis * (Math.cos(eccentricAnomaly) - eccentricity);
                double y = semiMajorAxis * Math.sqrt(1 - eccentricity*eccentricity) *
                        Math.sin(eccentricAnomaly);

                return new double[]{x, y};
            }

            private double solveKepler(double M, double e) {
                // Решение уравнения Кеплера методом Ньютона
                double E = M;
                for (int i = 0; i < 10; i++) {
                    double f = E - e * Math.sin(E) - M;
                    double fPrime = 1 - e * Math.cos(E);
                    E = E - f / fPrime;
                }
                return E;
            }
        }

        public static double calculateDeltaV(double mass, double thrust, double burnTime) {
            // Уравнение Циолковского
            double specificImpulse = 300; // Удельный импульс (с)
            double g0 = 9.81; // Ускорение свободного падения

            return specificImpulse * g0 * Math.log(mass / (mass - thrust * burnTime));
        }

        public static double calculateHohmannTransfer(double r1, double r2) {
            // Δv для перехода Гомана
            double mu = 3.986e5;
            double v1 = Math.sqrt(mu / r1);
            double v2 = Math.sqrt(mu / r2);
            double a_transfer = (r1 + r2) / 2;

            double v_transfer1 = Math.sqrt(mu * (2/r1 - 1/a_transfer));
            double v_transfer2 = Math.sqrt(mu * (2/r2 - 1/a_transfer));

            return Math.abs(v_transfer1 - v1) + Math.abs(v2 - v_transfer2);
        }
    }

    // Система телеметрии космического аппарата
    static class SpacecraftTelemetry {
        private Map<String, Double> sensors = new HashMap<>();
        private List<String> telemetryLog = new ArrayList<>();

        public SpacecraftTelemetry() {
            // Инициализация датчиков
            sensors.put("temperature", 20.0);
            sensors.put("pressure", 1013.25);
            sensors.put("power_level", 100.0);
            sensors.put("fuel_level", 100.0);
            sensors.put("altitude", 400.0); // км
            sensors.put("velocity", 7.66);  // км/с
        }

        public void updateTelemetry() {
            // Симуляция изменения телеметрии
            Random random = new Random();

            sensors.put("temperature", sensors.get("temperature") +
                    (random.nextDouble() - 0.5) * 2);
            sensors.put("pressure", Math.max(0, sensors.get("pressure") +
                    (random.nextDouble() - 0.5) * 10));
            sensors.put("power_level", Math.max(0, sensors.get("power_level") - 0.01));
            sensors.put("fuel_level", Math.max(0, sensors.get("fuel_level") - 0.005));
            sensors.put("altitude", sensors.get("altitude") +
                    (random.nextDouble() - 0.5) * 0.1);
            sensors.put("velocity", sensors.get("velocity") +
                    (random.nextDouble() - 0.5) * 0.01);

            // Логирование
            String logEntry = String.format("Time: %d, Temp: %.1f°C, Power: %.1f%%",
                    System.currentTimeMillis(),
                    sensors.get("temperature"),
                    sensors.get("power_level"));
            telemetryLog.add(logEntry);

            if (telemetryLog.size() > 1000) {
                telemetryLog.remove(0);
            }
        }

        public boolean checkCriticalConditions() {
            return sensors.get("temperature") > 50 ||
                    sensors.get("temperature") < -20 ||
                    sensors.get("power_level") < 10 ||
                    sensors.get("fuel_level") < 5;
        }

        public Map<String, Double> getSensorReadings() {
            return new HashMap<>(sensors);
        }

        public List<String> getTelemetryHistory() {
            return new ArrayList<>(telemetryLog);
        }
    }

    // Система управления полезной нагрузкой
    static class PayloadController {

        public static class ScientificInstrument {
            String name;
            double powerConsumption;
            boolean isActive;
            List<String> data;

            public ScientificInstrument(String name, double powerConsumption) {
                this.name = name;
                this.powerConsumption = powerConsumption;
                this.isActive = false;
                this.data = new ArrayList<>();
            }

            public void activate() {
                if (!isActive) {
                    isActive = true;
                    System.out.println(name + " активирован");
                }
            }

            public void deactivate() {
                if (isActive) {
                    isActive = false;
                    System.out.println(name + " деактивирован");
                }
            }

            public void collectData() {
                if (isActive) {
                    String dataPoint = String.format("%s: %d", name, System.currentTimeMillis());
                    data.add(dataPoint);
                    System.out.println("Собраны данные: " + dataPoint);
                }
            }

            public List<String> getCollectedData() {
                return new ArrayList<>(data);
            }
        }

        private List<ScientificInstrument> instruments = new ArrayList<>();
        private double availablePower;

        public PayloadController(double totalPower) {
            this.availablePower = totalPower;

            // Инициализация приборов
            instruments.add(new ScientificInstrument("Спектрометр", 50));
            instruments.add(new ScientificInstrument("Камера", 30));
            instruments.add(new ScientificInstrument("Детектор частиц", 70));
            instruments.add(new ScientificInstrument("Радиометр", 20));
        }

        public void activateInstrument(String instrumentName) {
            for (ScientificInstrument instrument : instruments) {
                if (instrument.name.equals(instrumentName)) {
                    if (availablePower >= instrument.powerConsumption) {
                        instrument.activate();
                        availablePower -= instrument.powerConsumption;
                    } else {
                        System.out.println("Недостаточно мощности для " + instrumentName);
                    }
                    break;
                }
            }
        }

        public void deactivateInstrument(String instrumentName) {
            for (ScientificInstrument instrument : instruments) {
                if (instrument.name.equals(instrumentName) && instrument.isActive) {
                    instrument.deactivate();
                    availablePower += instrument.powerConsumption;
                    break;
                }
            }
        }

        public void collectAllData() {
            for (ScientificInstrument instrument : instruments) {
                if (instrument.isActive) {
                    instrument.collectData();
                }
            }
        }

        public double getAvailablePower() {
            return availablePower;
        }

        public List<String> getAllCollectedData() {
            List<String> allData = new ArrayList<>();
            for (ScientificInstrument instrument : instruments) {
                allData.addAll(instrument.getCollectedData());
            }
            return allData;
        }
    }

    // Система навигации в дальнем космосе
    static class DeepSpaceNavigation {

        public static class CelestialBody {
            String name;
            double[] position; // Гелиоцентрические координаты (AU)
            double mass;       // Масса (кг)

            public CelestialBody(String name, double[] position, double mass) {
                this.name = name;
                this.position = position;
                this.mass = mass;
            }
        }

        private List<CelestialBody> solarSystem;

        public DeepSpaceNavigation() {
            // Инициализация Солнечной системы (упрощенно)
            solarSystem = new ArrayList<>();
            solarSystem.add(new CelestialBody("Sun", new double[]{0, 0, 0}, 1.989e30));
            solarSystem.add(new CelestialBody("Earth", new double[]{1, 0, 0}, 5.972e24));
            solarSystem.add(new CelestialBody("Mars", new double[]{1.524, 0, 0}, 6.39e23));
            solarSystem.add(new CelestialBody("Jupiter", new double[]{5.204, 0, 0}, 1.898e27));
        }

        public double[] calculateGravityAssist(double[] spacecraftPos,
                                               double[] targetBodyPos,
                                               double flybyAltitude) {
            // Расчет гравитационного маневра
            double[] deltaV = new double[3];
            double G = 6.67430e-11;
            double bodyMass = 0;

            for (CelestialBody body : solarSystem) {
                if (Math.abs(body.position[0] - targetBodyPos[0]) < 0.1) {
                    bodyMass = body.mass;
                    break;
                }
            }

            double distance = Math.sqrt(
                    Math.pow(spacecraftPos[0] - targetBodyPos[0], 2) +
                            Math.pow(spacecraftPos[1] - targetBodyPos[1], 2) +
                            Math.pow(spacecraftPos[2] - targetBodyPos[2], 2)
            );

            double velocity = Math.sqrt(2 * G * bodyMass / (distance * 1.496e11));

            // Упрощенный расчет прибавки скорости
            deltaV[0] = velocity * 0.1;
            deltaV[1] = velocity * 0.05;
            deltaV[2] = velocity * 0.02;

            return deltaV;
        }

        public double calculateInterplanetaryTrajectory(double[] startPos,
                                                        double[] targetPos,
                                                        double departureTime) {
            // Расчет межпланетной траектории
            double distance = Math.sqrt(
                    Math.pow(targetPos[0] - startPos[0], 2) +
                            Math.pow(targetPos[1] - startPos[1], 2) +
                            Math.pow(targetPos[2] - startPos[2], 2)
            );

            // Время перелета по эллиптической орбите
            double a = distance / 2; // Большая полуось
            double mu = 1.327e20; // Гравитационный параметр Солнца

            return Math.PI * Math.sqrt(Math.pow(a * 1.496e11, 3) / mu) / (3600 * 24); // в днях
        }

        public double[] getBodyPosition(String bodyName, double time) {
            for (CelestialBody body : solarSystem) {
                if (body.name.equals(bodyName)) {
                    // Упрощенное движение по круговой орбите
                    double angle = time * 2 * Math.PI / 365; // Радиан в день
                    return new double[]{
                            body.position[0] * Math.cos(angle),
                            body.position[0] * Math.sin(angle),
                            0
                    };
                }
            }
            return new double[]{0, 0, 0};
        }
    }
}