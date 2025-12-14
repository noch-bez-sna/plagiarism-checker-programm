import java.util.*;

public class GraphAlgorithms {

    // Поиск в ширину (BFS)
    public List<Integer> bfs(Map<Integer, List<Integer>> graph, int start) {
        List<Integer> result = new ArrayList<>();
        Set<Integer> visited = new HashSet<>();
        Queue<Integer> queue = new LinkedList<>();

        queue.add(start);
        visited.add(start);

        while (!queue.isEmpty()) {
            int current = queue.poll();
            result.add(current);

            for (int neighbor : graph.getOrDefault(current, new ArrayList<>())) {
                if (!visited.contains(neighbor)) {
                    visited.add(neighbor);
                    queue.add(neighbor);
                }
            }
        }
        return result;
    }

    // Поиск в глубину (DFS)
    public List<Integer> dfs(Map<Integer, List<Integer>> graph, int start) {
        List<Integer> result = new ArrayList<>();
        Set<Integer> visited = new HashSet<>();
        dfsRecursive(graph, start, visited, result);
        return result;
    }

    private void dfsRecursive(Map<Integer, List<Integer>> graph, int node,
                              Set<Integer> visited, List<Integer> result) {
        visited.add(node);
        result.add(node);

        for (int neighbor : graph.getOrDefault(node, new ArrayList<>())) {
            if (!visited.contains(neighbor)) {
                dfsRecursive(graph, neighbor, visited, result);
            }
        }
    }

    // Алгоритм Дейкстры
    public Map<Integer, Integer> dijkstra(Map<Integer, Map<Integer, Integer>> graph, int start) {
        Map<Integer, Integer> distances = new HashMap<>();
        PriorityQueue<int[]> pq = new PriorityQueue<>(Comparator.comparingInt(a -> a[1]));

        for (int node : graph.keySet()) {
            distances.put(node, Integer.MAX_VALUE);
        }
        distances.put(start, 0);
        pq.add(new int[]{start, 0});

        while (!pq.isEmpty()) {
            int[] current = pq.poll();
            int node = current[0];
            int dist = current[1];

            if (dist > distances.get(node)) continue;

            for (Map.Entry<Integer, Integer> neighbor : graph.get(node).entrySet()) {
                int newDist = dist + neighbor.getValue();
                if (newDist < distances.get(neighbor.getKey())) {
                    distances.put(neighbor.getKey(), newDist);
                    pq.add(new int[]{neighbor.getKey(), newDist});
                }
            }
        }
        return distances;
    }
}