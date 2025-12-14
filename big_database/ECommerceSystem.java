import java.util.*;

class Product {
    private String id;
    private String name;
    private double price;
    private int stock;
    private String category;

    public Product(String id, String name, double price, int stock, String category) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.stock = stock;
        this.category = category;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public double getPrice() { return price; }
    public int getStock() { return stock; }
    public String getCategory() { return category; }

    public void setStock(int stock) {
        this.stock = stock;
    }
}

class ShoppingCart {
    private Map<Product, Integer> items = new HashMap<>();

    public void addProduct(Product product, int quantity) {
        items.put(product, items.getOrDefault(product, 0) + quantity);
    }

    public void removeProduct(Product product) {
        items.remove(product);
    }

    public double getTotalPrice() {
        double total = 0;
        for (Map.Entry<Product, Integer> entry : items.entrySet()) {
            total += entry.getKey().getPrice() * entry.getValue();
        }
        return total;
    }

    public Map<Product, Integer> getItems() {
        return new HashMap<>(items);
    }
}

public class ECommerceSystem {
    private Map<String, Product> inventory = new HashMap<>();

    public void addProduct(Product product) {
        inventory.put(product.getId(), product);
    }

    public Product getProduct(String id) {
        return inventory.get(id);
    }

    public boolean processOrder(ShoppingCart cart) {
        // Проверяем наличие товаров
        for (Map.Entry<Product, Integer> entry : cart.getItems().entrySet()) {
            Product product = entry.getKey();
            int requested = entry.getValue();
            if (product.getStock() < requested) {
                return false;
            }
        }

        // Обновляем остатки
        for (Map.Entry<Product, Integer> entry : cart.getItems().entrySet()) {
            Product product = entry.getKey();
            int sold = entry.getValue();
            product.setStock(product.getStock() - sold);
        }

        return true;
    }

    public List<Product> searchProducts(String query) {
        List<Product> results = new ArrayList<>();
        String lowerQuery = query.toLowerCase();

        for (Product product : inventory.values()) {
            if (product.getName().toLowerCase().contains(lowerQuery) ||
                    product.getCategory().toLowerCase().contains(lowerQuery)) {
                results.add(product);
            }
        }
        return results;
    }

    public List<Product> getLowStockProducts(int threshold) {
        List<Product> lowStock = new ArrayList<>();
        for (Product product : inventory.values()) {
            if (product.getStock() < threshold) {
                lowStock.add(product);
            }
        }
        return lowStock;
    }
}