package inventory.exception;

public class InsufficientStockException extends InventoryException {
    public InsufficientStockException(String productName, int available, int requested) {
        super("Insufficient stock for '" + productName + "': available " + available
                + ", requested " + requested + ".");
    }
}
