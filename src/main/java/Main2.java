import java.util.*;
import java.util.concurrent.*;

public class Main2 {
    private static final String LETTERS = "RLRFR";
    private static final int ROUTE_LENGTH = 100;
    private final static int AMOUNT_OF_THREAD = 1000;
    public static final ConcurrentHashMap<Integer, Integer> sizeToFreq = new ConcurrentHashMap<>();

    // Объект для сигнализации между потоками
    private static final Object signal = new Object();

    public static void main(String[] args) throws InterruptedException, ExecutionException {
        // Поток для вывода статистики
        Thread statsThread = new Thread(() -> {
            while (!Thread.interrupted()) {
                try {
                    synchronized (signal) {
                        signal.wait(); // Ожидание сигнала
                    }

                    // Поиск лидера в sizeToFreq
                    int maxValue = 0;
                    int maxKey = 0;
                    for (Map.Entry<Integer, Integer> entry : sizeToFreq.entrySet()) {
                        if (entry.getValue() > maxValue) {
                            maxValue = entry.getValue();
                            maxKey = entry.getKey();
                        }
                    }

                    System.out.println("Текущий лидер: " + maxKey + " (встретилось " + maxValue + " раз)");
                } catch (InterruptedException e) {
                    // Поток прерван
                    System.out.println("Поток статистики прерван.");
                    return;
                }
            }
        });
        statsThread.start();

        ExecutorService executor = Executors.newFixedThreadPool(AMOUNT_OF_THREAD);
        List<Future<Integer>> futures = new ArrayList<>();

        for (int i = 0; i < AMOUNT_OF_THREAD; i++) {
            futures.add(executor.submit(() -> {
                String route = generateRoute(LETTERS, ROUTE_LENGTH);
                Integer count = 0;
                for (char c : route.toCharArray()) {
                    if (c == 'R') {
                        count++;
                    }
                }
                System.out.println(route.substring(0, 100) + " -> " + count);

                synchronized (count) {
                    sizeToFreq.compute(count, (key, val) -> (val == null) ? 1 : val + 1);

                    // Отправка сигнала потоку статистики
                    synchronized (signal) {
                        signal.notify();
                    }

                    return count;
                }
            }));
        }

        for (Future<Integer> future : futures) {
            future.get();
        }

        executor.shutdown();
        statsThread.interrupt(); // Прерываем поток статистики

        int maxValue = 0;
        int maxKey = 0;
        for (Integer key : sizeToFreq.keySet()) {
            if (sizeToFreq.get(key) > maxValue) {
                maxValue = sizeToFreq.get(key);
                maxKey = key;
            }
        }
        System.out.println("Самое частое количество повторений " + maxKey + " (встретилось " + maxValue + " раз)");
        sizeToFreq.remove(maxKey);
        System.out.println("Другие размеры:");

        for (Integer key : sizeToFreq.keySet()) {
            int value = sizeToFreq.get(key);
            System.out.println("- " + key + " (" + value + " раз)");
        }
    }

    public static String generateRoute(String letters, int length) {
        Random random = new Random();
        StringBuilder route = new StringBuilder();
        for (int i = 0; i < length; i++) {
            route.append(letters.charAt(random.nextInt(letters.length())));
        }
        return route.toString();
    }
}