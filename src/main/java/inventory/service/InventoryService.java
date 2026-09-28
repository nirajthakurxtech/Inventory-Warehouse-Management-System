package inventory.service;

import inventory.dao.ProductDao;
import inventory.dao.TransactionDao;
import inventory.exception.DuplicateSkuException;
import inventory.exception.InsufficientStockException;
import inventory.exception.ProductNotFoundException;
import inventory.model.Product;
import inventory.model.StockTransaction;
import inventory.util.DBConnection;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/** Business rules for the inventory; coordinates the DAOs. */
public class InventoryService {

    private final ProductDao productDao = new ProductDao();
    private final TransactionDao transactionDao = new TransactionDao();

    // ---------- CRUD ----------

    public int addProduct(Product product) throws SQLException, DuplicateSkuException {
        if (productDao.findBySku(product.getSku()) != null) {
            throw new DuplicateSkuException(product.getSku());
        }
        int id = productDao.insert(product);
        if (product.getQuantity() > 0) {
            try (Connection conn = DBConnection.getConnection()) {
                transactionDao.insert(conn,
                        new StockTransaction(id, StockTransaction.Type.IN, product.getQuantity(), "Opening stock"));
            }
        }
        return id;
    }

    public Product getProduct(int id) throws SQLException, ProductNotFoundException {
        Product product = productDao.findById(id);
        if (product == null) {
            throw new ProductNotFoundException("ID " + id);
        }
        return product;
    }

    public Product getProductBySku(String sku) throws SQLException, ProductNotFoundException {
        Product product = productDao.findBySku(sku);
        if (product == null) {
            throw new ProductNotFoundException("SKU " + sku);
        }
        return product;
    }

    public List<Product> getAllProducts() throws SQLException {
        return productDao.findAll();
    }

    public List<Product> searchProducts(String keyword) throws SQLException {
        return productDao.search(keyword);
    }

    public void updateProduct(Product product) throws SQLException, ProductNotFoundException {
        if (!productDao.update(product)) {
            throw new ProductNotFoundException("ID " + product.getId());
        }
    }

    public void deleteProduct(int id) throws SQLException, ProductNotFoundException {
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                transactionDao.deleteByProduct(conn, id);
                if (!productDao.delete(conn, id)) {
                    throw new ProductNotFoundException("ID " + id);
                }
                conn.commit();
            } catch (SQLException | ProductNotFoundException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    // ---------- Stock movements ----------

    public void stockIn(int productId, int quantity, String note)
            throws SQLException, ProductNotFoundException {
        try {
            moveStock(productId, quantity, StockTransaction.Type.IN, note);
        } catch (InsufficientStockException e) {
            throw new IllegalStateException("Stock-in can never be insufficient", e);
        }
    }

    public void stockOut(int productId, int quantity, String note)
            throws SQLException, ProductNotFoundException, InsufficientStockException {
        moveStock(productId, quantity, StockTransaction.Type.OUT, note);
    }

    private void moveStock(int productId, int quantity, StockTransaction.Type type, String note)
            throws SQLException, ProductNotFoundException, InsufficientStockException {
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                Product product = productDao.findById(conn, productId);
                if (product == null) {
                    throw new ProductNotFoundException("ID " + productId);
                }
                int newQuantity = product.getQuantity();
                if (type == StockTransaction.Type.OUT) {
                    if (quantity > product.getQuantity()) {
                        throw new InsufficientStockException(product.getName(), product.getQuantity(), quantity);
                    }
                    newQuantity -= quantity;
                } else {
                    newQuantity += quantity;
                }
                productDao.updateQuantity(conn, productId, newQuantity);
                transactionDao.insert(conn, new StockTransaction(productId, type, quantity, note));
                conn.commit();
            } catch (SQLException | ProductNotFoundException | InsufficientStockException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    public List<StockTransaction> getHistory(int productId) throws SQLException, ProductNotFoundException {
        getProduct(productId);
        return transactionDao.findByProduct(productId);
    }

    // ---------- Reports ----------

    public List<Product> getLowStockProducts() throws SQLException {
        List<Product> low = new ArrayList<>();
        for (Product p : productDao.findAll()) {
            if (p.isLowStock()) {
                low.add(p);
            }
        }
        low.sort(Comparator.comparingInt(Product::getQuantity));
        return low;
    }

    /** Category -> total stock value, alphabetically ordered. */
    public Map<String, Double> getStockValueByCategory() throws SQLException {
        Map<String, Double> totals = new TreeMap<>();
        for (Product p : productDao.findAll()) {
            totals.merge(p.getCategory(), p.getStockValue(), Double::sum);
        }
        return totals;
    }

    /** ID -> Product lookup map. */
    public Map<Integer, Product> getProductMap() throws SQLException {
        Map<Integer, Product> map = new HashMap<>();
        for (Product p : productDao.findAll()) {
            map.put(p.getId(), p);
        }
        return map;
    }

    public List<Product> getProductsSortedByValue() throws SQLException {
        List<Product> products = productDao.findAll();
        products.sort(Comparator.comparingDouble(Product::getStockValue).reversed());
        return products;
    }
}
