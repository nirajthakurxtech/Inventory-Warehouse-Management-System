package inventory.model;

public class Product extends BaseEntity {

    private String sku;
    private String name;
    private String category;
    private int quantity;
    private double unitPrice;
    private int reorderLevel;

    public Product(String sku, String name, String category, int quantity, double unitPrice) {
        this(0, sku, name, category, quantity, unitPrice, 10);
    }

    public Product(int id, String sku, String name, String category,
                   int quantity, double unitPrice, int reorderLevel) {
        super(id);
        this.sku = sku;
        this.name = name;
        this.category = category;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.reorderLevel = reorderLevel;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(double unitPrice) {
        this.unitPrice = unitPrice;
    }

    public int getReorderLevel() {
        return reorderLevel;
    }

    public void setReorderLevel(int reorderLevel) {
        this.reorderLevel = reorderLevel;
    }

    public double getStockValue() {
        return quantity * unitPrice;
    }

    public boolean isLowStock() {
        return quantity < reorderLevel;
    }

    @Override
    public String getSummary() {
        return String.format("%-4d %-10s %-22s %-14s %6d %10.2f %8d",
                getId(), sku, name, category, quantity, unitPrice, reorderLevel);
    }
}
