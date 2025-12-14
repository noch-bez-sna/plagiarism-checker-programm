import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class BlockchainSimulator {

    static class Block {
        public String hash;
        public String previousHash;
        private String data;
        private long timestamp;
        private int nonce;

        public Block(String data, String previousHash) {
            this.data = data;
            this.previousHash = previousHash;
            this.timestamp = new Date().getTime();
            this.hash = calculateHash();
        }

        public String calculateHash() {
            return SecurityUtils.md5Hash(previousHash +
                    Long.toString(timestamp) +
                    Integer.toString(nonce) +
                    data);
        }

        public void mineBlock(int difficulty) {
            String target = new String(new char[difficulty]).replace('\0', '0');
            while (!hash.substring(0, difficulty).equals(target)) {
                nonce++;
                hash = calculateHash();
            }
            System.out.println("Block mined: " + hash);
        }
    }

    private List<Block> blockchain;
    private int difficulty;

    public BlockchainSimulator(int difficulty) {
        this.blockchain = new ArrayList<>();
        this.difficulty = difficulty;
        // Создаем генезис-блок
        blockchain.add(new Block("Genesis Block", "0"));
        blockchain.get(0).mineBlock(difficulty);
    }

    public void addBlock(String data) {
        Block latestBlock = blockchain.get(blockchain.size() - 1);
        Block newBlock = new Block(data, latestBlock.hash);
        newBlock.mineBlock(difficulty);
        blockchain.add(newBlock);
    }

    public boolean isChainValid() {
        for (int i = 1; i < blockchain.size(); i++) {
            Block current = blockchain.get(i);
            Block previous = blockchain.get(i - 1);

            if (!current.hash.equals(current.calculateHash())) {
                return false;
            }

            if (!current.previousHash.equals(previous.hash)) {
                return false;
            }
        }
        return true;
    }

    public void printBlockchain() {
        for (Block block : blockchain) {
            System.out.println("Block Hash: " + block.hash);
            System.out.println("Prev Hash: " + block.previousHash);
            System.out.println("Data: " + block.data);
            System.out.println("---");
        }
    }
}