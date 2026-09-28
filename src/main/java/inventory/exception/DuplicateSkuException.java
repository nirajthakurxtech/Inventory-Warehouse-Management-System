package inventory.exception;

public class DuplicateSkuException extends InventoryException {
    public DuplicateSkuException(String sku) {
        super("A product with SKU '" + sku + "' already exists.");
    }
}
