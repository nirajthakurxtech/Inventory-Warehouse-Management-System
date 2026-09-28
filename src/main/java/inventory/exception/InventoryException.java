package inventory.exception;

/** Base class for all domain-specific errors in the inventory system. */
public class InventoryException extends Exception {
    public InventoryException(String message) {
        super(message);
    }
}
