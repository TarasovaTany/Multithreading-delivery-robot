import java.util.*;
import java.util.concurrent.*;

public class Main {
    private static final String LETTERS = "RLRFR";
    private static final int ROUTE_LENGTH = 100;
    private final static int AMOUNT_OF_THREAD = 1000;
    public static final Map<Integer, Integer> sizeToFreq = new HashMap<>();

    public static void main(String[] args) throws InterruptedException, ExecutionException {

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
                return count;
                }
            }));
        }

        for (Future<Integer> future : futures) {
            future.get();
        }
        executor.shutdown();

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
    public static String generateRoute (String letters,int length) {
            Random random = new Random();
            StringBuilder route = new StringBuilder();
            for (int i = 0; i < length; i++) {
                route.append(letters.charAt(random.nextInt(letters.length())));
            }
            return route.toString();
    }
}

