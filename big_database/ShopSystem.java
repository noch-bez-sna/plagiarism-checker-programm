import java.util.*;

class Product {
    private String name;
    private double price;
    private int quantity;

    public Product(String name, double price, int quantity) {
        this.name = name;
        this.price = price;
        this.quantity = quantity;
    }

    public String getName() { return name; }
    public double getPrice() { return price; }
    public int getQuantity() { return quantity; }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}

public class ShopSystem {
    private Map<String, Product> inventory = new HashMap<>();

    public void addProduct(Product product) {
        inventory.put(product.getName(), product);
    }

    public boolean purchaseProduct(String productName, int quantity) {
        Product product = inventory.get(productName);
        if (product != null && product.getQuantity() >= quantity) {
            product.setQuantity(product.getQuantity() - quantity);
            return true;
        }
        return false;
    }

    public double calculateTotalValue() {
        return inventory.values().stream()
                .mapToDouble(p -> p.getPrice() * p.getQuantity())
                .sum();
    }

    public List<Product> getLowStockProducts(int threshold) {
        return inventory.values().stream()
                .filter(p -> p.getQuantity() < threshold)
                .toList();
    }
}