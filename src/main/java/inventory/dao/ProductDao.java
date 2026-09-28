package inventory.dao;

import inventory.model.Product;
import inventory.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class ProductDao {

    private static final String SELECT_ALL =
            "SELECT id, sku, name, category, quantity, unit_price, reorder_level FROM products";

    public int insert(Product p) throws SQLException {
        String sql = "INSERT INTO products (sku, name, category, quantity, unit_price, reorder_level) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, p.getSku());
            ps.setString(2, p.getName());
            ps.setString(3, p.getCategory());
            ps.setInt(4, p.getQuantity());
            ps.setDouble(5, p.getUnitPrice());
            ps.setInt(6, p.getReorderLevel());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                return keys.next() ? keys.getInt(1) : -1;
            }
        }
    }

    public Product findById(int id) throws SQLException {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_ALL + " WHERE id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    /** Same as findById but joins an existing connection (for transactions). */
    public Product findById(Connection conn, int id) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(SELECT_ALL + " WHERE id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    public Product findBySku(String sku) throws SQLException {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_ALL + " WHERE sku = ?")) {
            ps.setString(1, sku);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    public List<Product> findAll() throws SQLException {
        List<Product> products = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_ALL + " ORDER BY id");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                products.add(map(rs));
            }
        }
        return products;
    }

    public List<Product> search(String keyword) throws SQLException {
        List<Product> products = new ArrayList<>();
        String sql = SELECT_ALL + " WHERE LOWER(name) LIKE ? OR LOWER(sku) LIKE ? "
                + "OR LOWER(category) LIKE ? ORDER BY name";
        String pattern = "%" + keyword.toLowerCase() + "%";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, pattern);
            ps.setString(2, pattern);
            ps.setString(3, pattern);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    products.add(map(rs));
                }
            }
        }
        return products;
    }

    public boolean update(Product p) throws SQLException {
        String sql = "UPDATE products SET name = ?, category = ?, unit_price = ?, reorder_level = ? "
                + "WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, p.getName());
            ps.setString(2, p.getCategory());
            ps.setDouble(3, p.getUnitPrice());
            ps.setInt(4, p.getReorderLevel());
            ps.setInt(5, p.getId());
            return ps.executeUpdate() > 0;
        }
    }

    public void updateQuantity(Connection conn, int productId, int newQuantity) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("UPDATE products SET quantity = ? WHERE id = ?")) {
            ps.setInt(1, newQuantity);
            ps.setInt(2, productId);
            ps.executeUpdate();
        }
    }

    public boolean delete(Connection conn, int id) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("DELETE FROM products WHERE id = ?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    private Product map(ResultSet rs) throws SQLException {
        return new Product(
                rs.getInt("id"),
                rs.getString("sku"),
                rs.getString("name"),
                rs.getString("category"),
                rs.getInt("quantity"),
                rs.getDouble("unit_price"),
                rs.getInt("reorder_level"));
    }
}
