package inventory.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

/**
 * Creates database connections. Settings come from environment variables
 * (INVENTORY_DB_URL / _USER / _PASSWORD) or config.properties; nothing is hard-coded.
 */
public final class DBConnection {

    private static final String DEFAULT_URL = "jdbc:sqlite:inventory.db";
    private static final Properties CONFIG = loadConfig();

    private DBConnection() {
    }

    private static Properties loadConfig() {
        Properties props = new Properties();
        try (FileInputStream in = new FileInputStream("config.properties")) {
            props.load(in);
        } catch (IOException e) {
            // No config file: fall back to environment variables / defaults.
        }
        return props;
    }

    private static String setting(String envName, String propName, String fallback) {
        String value = System.getenv(envName);
        if (value == null || value.isEmpty()) {
            value = CONFIG.getProperty(propName, fallback);
        }
        return value;
    }

    public static Connection getConnection() throws SQLException {
        String url = setting("INVENTORY_DB_URL", "db.url", DEFAULT_URL);
        String user = setting("INVENTORY_DB_USER", "db.user", "");
        String password = setting("INVENTORY_DB_PASSWORD", "db.password", "");

        Connection conn = user.isEmpty()
                ? DriverManager.getConnection(url)
                : DriverManager.getConnection(url, user, password);

        if (url.startsWith("jdbc:sqlite")) {
            try (Statement st = conn.createStatement()) {
                st.execute("PRAGMA foreign_keys = ON");
            }
        }
        return conn;
    }

    /** Creates the tables on first run (same SQL as schema.sql). */
    public static void initSchema() throws SQLException {
        try (Connection conn = getConnection();
             Statement st = conn.createStatement()) {
            st.execute("CREATE TABLE IF NOT EXISTS products ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "sku TEXT NOT NULL UNIQUE, "
                    + "name TEXT NOT NULL, "
                    + "category TEXT NOT NULL, "
                    + "quantity INTEGER NOT NULL CHECK (quantity >= 0), "
                    + "unit_price REAL NOT NULL CHECK (unit_price >= 0), "
                    + "reorder_level INTEGER NOT NULL DEFAULT 10)");
            st.execute("CREATE TABLE IF NOT EXISTS stock_transactions ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "product_id INTEGER NOT NULL, "
                    + "type TEXT NOT NULL CHECK (type IN ('IN', 'OUT')), "
                    + "quantity INTEGER NOT NULL CHECK (quantity > 0), "
                    + "created_at TEXT NOT NULL, "
                    + "note TEXT, "
                    + "FOREIGN KEY (product_id) REFERENCES products(id))");
        }
    }
}
