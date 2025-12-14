import java.util.*;

public class PathfindingAlgorithms {

    // A* алгоритм
    public static List<int[]> aStarSearch(int[][] grid, int[] start, int[] goal) {
        PriorityQueue<Node> openSet = new PriorityQueue<>();
        Map<String, String> cameFrom = new HashMap<>();
        Map<String, Double> gScore = new HashMap<>();
        Map<String, Double> fScore = new HashMap<>();

        String startKey = start[0] + "," + start[1];
        String goalKey = goal[0] + "," + goal[1];

        gScore.put(startKey, 0.0);
        fScore.put(startKey, heuristic(start, goal));
        openSet.add(new Node(start, 0, heuristic(start, goal)));

        while (!openSet.isEmpty()) {
            Node current = openSet.poll();
            String currentKey = current.x + "," + current.y;

            if (currentKey.equals(goalKey)) {
                return reconstructPath(cameFrom, currentKey);
            }

            for (int[] neighbor : getNeighbors(grid, new int[]{current.x, current.y})) {
                String neighborKey = neighbor[0] + "," + neighbor[1];
                double tentativeGScore = gScore.getOrDefault(currentKey, Double.MAX_VALUE) + 1;

                if (tentativeGScore < gScore.getOrDefault(neighborKey, Double.MAX_VALUE)) {
                    cameFrom.put(neighborKey, currentKey);
                    gScore.put(neighborKey, tentativeGScore);
                    fScore.put(neighborKey, tentativeGScore + heuristic(neighbor, goal));

                    if (!openSet.contains(new Node(neighbor, 0, 0))) {
                        openSet.add(new Node(neighbor, tentativeGScore, fScore.get(neighborKey)));
                    }
                }
            }
        }

        return new ArrayList<>(); // Путь не найден
    }

    static class Node implements Comparable<Node> {
        int x, y;
        double g, f;

        Node(int[] pos, double g, double f) {
            this.x = pos[0];
            this.y = pos[1];
            this.g = g;
            this.f = f;
        }

        @Override
        public int compareTo(Node other) {
            return Double.compare(this.f, other.f);
        }
    }

    private static double heuristic(int[] a, int[] b) {
        return Math.abs(a[0] - b[0]) + Math.abs(a[1] - b[1]); // Манхэттенское расстояние
    }

    private static List<int[]> getNeighbors(int[][] grid, int[] cell) {
        List<int[]> neighbors = new ArrayList<>();
        int[][] directions = {{0,1}, {1,0}, {0,-1}, {-1,0}};

        for (int[] dir : directions) {
            int nx = cell[0] + dir[0];
            int ny = cell[1] + dir[1];

            if (nx >= 0 && nx < grid.length && ny >= 0 && ny < grid[0].length && grid[nx][ny] == 0) {
                neighbors.add(new int[]{nx, ny});
            }
        }
        return neighbors;
    }

    private static List<int[]> reconstructPath(Map<String, String> cameFrom, String currentKey) {
        List<int[]> path = new ArrayList<>();
        while (cameFrom.containsKey(currentKey)) {
            String[] coords = currentKey.split(",");
            path.add(0, new int[]{Integer.parseInt(coords[0]), Integer.parseInt(coords[1])});
            currentKey = cameFrom.get(currentKey);
        }
        return path;
    }
}