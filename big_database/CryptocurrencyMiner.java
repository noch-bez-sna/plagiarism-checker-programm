import java.security.MessageDigest;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public class CryptocurrencyMiner {

    private static final Random random = new Random();
    private AtomicBoolean mining = new AtomicBoolean(false);
    private AtomicLong hashesCalculated = new AtomicLong(0);
    private String targetPrefix;

    public CryptocurrencyMiner(int difficulty) {
        this.targetPrefix = "0".repeat(difficulty);
    }

    public void startMining(int threads) {
        mining.set(true);
        ExecutorService executor = Executors.newFixedThreadPool(threads);

        for (int i = 0; i < threads; i++) {
            executor.submit(this::mineBlock);
        }

        // Статистика
        new Thread(() -> {
            while (mining.get()) {
                try {
                    Thread.sleep(1000);
                    long hashes = hashesCalculated.get();
                    System.out.printf("Hash rate: %d H/s, Total: %d%n", hashes, hashesCalculated.get());
                    hashesCalculated.set(0);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }).start();
    }

    private void mineBlock() {
        while (mining.get()) {
            String data = "block_data_" + System.currentTimeMillis() + random.nextInt();
            String hash = calculateSHA256(data);
            hashesCalculated.incrementAndGet();

            if (hash.startsWith(targetPrefix)) {
                System.out.println("BLOCK MINED! Hash: " + hash);
                System.out.println("Data: " + data);
                mining.set(false);
                break;
            }
        }
    }

    private String calculateSHA256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes());
            StringBuilder hexString = new StringBuilder();

            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }

            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void stopMining() {
        mining.set(false);
    }

    public double calculateMiningReward(int blocksMined, double blockReward) {
        return blocksMined * blockReward;
    }
}