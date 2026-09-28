CREATE TABLE IF NOT EXISTS products (
    id            INTEGER PRIMARY KEY AUTOINCREMENT,
    sku           TEXT    NOT NULL UNIQUE,
    name          TEXT    NOT NULL,
    category      TEXT    NOT NULL,
    quantity      INTEGER NOT NULL CHECK (quantity >= 0),
    unit_price    REAL    NOT NULL CHECK (unit_price >= 0),
    reorder_level INTEGER NOT NULL DEFAULT 10
);

CREATE TABLE IF NOT EXISTS stock_transactions (
    id         INTEGER PRIMARY KEY AUTOINCREMENT,
    product_id INTEGER NOT NULL,
    type       TEXT    NOT NULL CHECK (type IN ('IN', 'OUT')),
    quantity   INTEGER NOT NULL CHECK (quantity > 0),
    created_at TEXT    NOT NULL,
    note       TEXT,
    FOREIGN KEY (product_id) REFERENCES products(id)
);
