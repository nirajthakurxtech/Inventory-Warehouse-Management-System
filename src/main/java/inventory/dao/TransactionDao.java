package inventory.dao;

import inventory.model.StockTransaction;
import inventory.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TransactionDao {

    public void insert(Connection conn, StockTransaction t) throws SQLException {
        String sql = "INSERT INTO stock_transactions (product_id, type, quantity, created_at, note) "
                + "VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, t.getProductId());
            ps.setString(2, t.getType().name());
            ps.setInt(3, t.getQuantity());
            ps.setString(4, t.getCreatedAt().toString());
            ps.setString(5, t.getNote());
            ps.executeUpdate();
        }
    }

    public List<StockTransaction> findByProduct(int productId) throws SQLException {
        List<StockTransaction> list = new ArrayList<>();
        String sql = "SELECT id, product_id, type, quantity, created_at, note "
                + "FROM stock_transactions WHERE product_id = ? ORDER BY created_at DESC, id DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, productId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new StockTransaction(
                            rs.getInt("id"),
                            rs.getInt("product_id"),
                            StockTransaction.Type.valueOf(rs.getString("type")),
                            rs.getInt("quantity"),
                            LocalDateTime.parse(rs.getString("created_at")),
                            rs.getString("note")));
                }
            }
        }
        return list;
    }

    public void deleteByProduct(Connection conn, int productId) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "DELETE FROM stock_transactions WHERE product_id = ?")) {
            ps.setInt(1, productId);
            ps.executeUpdate();
        }
    }
}
