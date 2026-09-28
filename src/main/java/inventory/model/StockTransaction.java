package inventory.model;

import java.time.LocalDateTime;

public class StockTransaction extends BaseEntity {

    public enum Type { IN, OUT }

    private int productId;
    private Type type;
    private int quantity;
    private LocalDateTime createdAt;
    private String note;

    public StockTransaction(int productId, Type type, int quantity, String note) {
        this(0, productId, type, quantity, LocalDateTime.now(), note);
    }

    public StockTransaction(int id, int productId, Type type, int quantity,
                            LocalDateTime createdAt, String note) {
        super(id);
        this.productId = productId;
        this.type = type;
        this.quantity = quantity;
        this.createdAt = createdAt;
        this.note = note;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    @Override
    public String getSummary() {
        return String.format("%-4d %-19s %-4s %6d  %s",
                getId(), createdAt.toString().replace('T', ' ').substring(0, 19),
                type, quantity, note == null ? "" : note);
    }
}
