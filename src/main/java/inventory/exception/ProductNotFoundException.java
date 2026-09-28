package inventory.exception;

public class ProductNotFoundException extends InventoryException {
    public ProductNotFoundException(String identifier) {
        super("Product not found: " + identifier);
    }
}
