import java.util.*;

public class CollectionMaster {

    // Удаление дубликатов из списка
    public static <T> List<T> removeDuplicates(List<T> list) {
        return new ArrayList<>(new LinkedHashSet<>(list));
    }

    // Инвертирование Map
    public static <K, V> Map<V, List<K>> invertMap(Map<K, V> map) {
        Map<V, List<K>> inverted = new HashMap<>();
        for (Map.Entry<K, V> entry : map.entrySet()) {
            inverted.computeIfAbsent(entry.getValue(), k -> new ArrayList<>())
                    .add(entry.getKey());
        }
        return inverted;
    }

    // Подсчет частоты элементов
    public static <T> Map<T, Integer> frequencyCounter(List<T> list) {
        Map<T, Integer> frequency = new HashMap<>();
        for (T item : list) {
            frequency.put(item, frequency.getOrDefault(item, 0) + 1);
        }
        return frequency;
    }

    // Объединение нескольких списков
    @SafeVarargs
    public static <T> List<T> mergeLists(List<T>... lists) {
        List<T> result = new ArrayList<>();
        for (List<T> list : lists) {
            result.addAll(list);
        }
        return result;
    }

    // Фильтрация по предикату
    public static <T> List<T> filterList(List<T> list, java.util.function.Predicate<T> predicate) {
        List<T> result = new ArrayList<>();
        for (T item : list) {
            if (predicate.test(item)) {
                result.add(item);
            }
        }
        return result;
    }
}