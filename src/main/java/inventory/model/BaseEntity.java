package inventory.model;

/** Abstract parent of every database-backed model class. */
public abstract class BaseEntity {

    private int id;

    protected BaseEntity() {
    }

    protected BaseEntity(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    /** Each subtype describes itself in one line (polymorphism). */
    public abstract String getSummary();

    @Override
    public String toString() {
        return getSummary();
    }
}
