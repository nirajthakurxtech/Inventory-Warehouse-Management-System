package inventory.main;

import inventory.exception.DuplicateSkuException;
import inventory.exception.InsufficientStockException;
import inventory.exception.InventoryException;
import inventory.exception.ProductNotFoundException;
import inventory.model.Product;
import inventory.model.StockTransaction;
import inventory.service.InventoryService;
import inventory.util.DBConnection;
import inventory.util.InputHelper;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class Main {

    private static final String HEADER = String.format("%-4s %-10s %-22s %-14s %6s %10s %8s",
            "ID", "SKU", "Name", "Category", "Qty", "Price", "Reorder");

    private final InventoryService service = new InventoryService();
    private final InputHelper input = new InputHelper();

    public static void main(String[] args) {
        try {
            DBConnection.initSchema();
        } catch (SQLException e) {
            System.err.println("Could not initialise the database: " + e.getMessage());
            System.err.println("Check your JDBC driver and config.properties, then try again.");
            return;
        }
        new Main().run();
    }

    private void run() {
        boolean running = true;
        while (running) {
            printMenu();
            int choice = input.readInt("Choose an option: ", 0);
            try {
                switch (choice) {
                    case 1: addProduct(); break;
                    case 2: viewAll(); break;
                    case 3: searchProducts(); break;
                    case 4: updateProduct(); break;
                    case 5: deleteProduct(); break;
                    case 6: stockIn(); break;
                    case 7: stockOut(); break;
                    case 8: lowStock(); break;
                    case 9: valueReport(); break;
                    case 10: history(); break;
                    case 0:
                        running = false;
                        System.out.println("Goodbye!");
                        break;
                    default:
                        System.out.println("Invalid option, please choose 0-10.");
                }
            } catch (InventoryException e) {
                System.out.println("Error: " + e.getMessage());
            } catch (SQLException e) {
                System.out.println("Database error: " + e.getMessage());
            }
        }
    }

    private void printMenu() {
        System.out.println("\n===== INVENTORY MANAGEMENT SYSTEM =====");
        System.out.println(" 1. Add product");
        System.out.println(" 2. View all products");
        System.out.println(" 3. Search products");
        System.out.println(" 4. Update product");
        System.out.println(" 5. Delete product");
        System.out.println(" 6. Stock IN");
        System.out.println(" 7. Stock OUT");
        System.out.println(" 8. Low-stock alert");
        System.out.println(" 9. Stock value by category");
        System.out.println("10. Transaction history");
        System.out.println(" 0. Exit");
    }

    private void addProduct() throws SQLException, DuplicateSkuException {
        String sku = input.readNonEmpty("SKU: ").toUpperCase();
        String name = input.readNonEmpty("Name: ");
        String category = input.readNonEmpty("Category: ");
        int quantity = input.readInt("Opening quantity: ", 0);
        double price = input.readDouble("Unit price: ", 0);
        int reorder = input.readInt("Reorder level: ", 0);

        Product product = new Product(0, sku, name, category, quantity, price, reorder);
        int id = service.addProduct(product);
        System.out.println("Product added with ID " + id + ".");
    }

    private void viewAll() throws SQLException {
        printProducts(service.getAllProducts());
    }

    private void searchProducts() throws SQLException {
        String keyword = input.readNonEmpty("Search (name / SKU / category): ");
        printProducts(service.searchProducts(keyword));
    }

    private void updateProduct() throws SQLException, ProductNotFoundException {
        Product p = service.getProduct(input.readInt("Product ID: ", 1));
        System.out.println("Editing: " + p.getName() + " (press Enter to keep current value)");

        String name = input.readOptional("Name [" + p.getName() + "]: ");
        String category = input.readOptional("Category [" + p.getCategory() + "]: ");
        String price = input.readOptional("Unit price [" + p.getUnitPrice() + "]: ");
        String reorder = input.readOptional("Reorder level [" + p.getReorderLevel() + "]: ");

        if (!name.isEmpty()) {
            p.setName(name);
        }
        if (!category.isEmpty()) {
            p.setCategory(category);
        }
        try {
            if (!price.isEmpty()) {
                double value = Double.parseDouble(price);
                if (value < 0) {
                    throw new NumberFormatException();
                }
                p.setUnitPrice(value);
            }
            if (!reorder.isEmpty()) {
                int value = Integer.parseInt(reorder);
                if (value < 0) {
                    throw new NumberFormatException();
                }
                p.setReorderLevel(value);
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid number entered. Update cancelled.");
            return;
        }
        service.updateProduct(p);
        System.out.println("Product updated.");
    }

    private void deleteProduct() throws SQLException, ProductNotFoundException {
        int id = input.readInt("Product ID to delete: ", 1);
        Product p = service.getProduct(id);
        String confirm = input.readNonEmpty("Delete '" + p.getName() + "' and its history? (y/n): ");
        if (confirm.equalsIgnoreCase("y")) {
            service.deleteProduct(id);
            System.out.println("Product deleted.");
        } else {
            System.out.println("Cancelled.");
        }
    }

    private void stockIn() throws SQLException, ProductNotFoundException {
        int id = input.readInt("Product ID: ", 1);
        int qty = input.readInt("Quantity received: ", 1);
        String note = input.readOptional("Note (optional): ");
        service.stockIn(id, qty, note);
        System.out.println("Stock updated. New quantity: " + service.getProduct(id).getQuantity());
    }

    private void stockOut() throws SQLException, ProductNotFoundException, InsufficientStockException {
        int id = input.readInt("Product ID: ", 1);
        int qty = input.readInt("Quantity to remove: ", 1);
        String note = input.readOptional("Note (optional): ");
        service.stockOut(id, qty, note);
        System.out.println("Stock updated. New quantity: " + service.getProduct(id).getQuantity());
    }

    private void lowStock() throws SQLException {
        List<Product> low = service.getLowStockProducts();
        if (low.isEmpty()) {
            System.out.println("All products are above their reorder level.");
            return;
        }
        System.out.println("LOW STOCK ALERT:");
        printProducts(low);
    }

    private void valueReport() throws SQLException {
        Map<String, Double> totals = service.getStockValueByCategory();
        if (totals.isEmpty()) {
            System.out.println("No products yet.");
            return;
        }
        double grand = 0;
        System.out.println(String.format("%-20s %14s", "Category", "Stock value"));
        System.out.println("-----------------------------------");
        for (Map.Entry<String, Double> entry : totals.entrySet()) {
            System.out.println(String.format("%-20s %14.2f", entry.getKey(), entry.getValue()));
            grand += entry.getValue();
        }
        System.out.println("-----------------------------------");
        System.out.println(String.format("%-20s %14.2f", "TOTAL", grand));
    }

    private void history() throws SQLException, ProductNotFoundException {
        int id = input.readInt("Product ID: ", 1);
        List<StockTransaction> list = service.getHistory(id);
        if (list.isEmpty()) {
            System.out.println("No transactions for this product.");
            return;
        }
        System.out.println(String.format("%-4s %-19s %-4s %6s  %s", "ID", "Date/Time", "Type", "Qty", "Note"));
        for (StockTransaction t : list) {
            System.out.println(t.getSummary());
        }
    }

    private void printProducts(List<Product> products) {
        if (products.isEmpty()) {
            System.out.println("No products found.");
            return;
        }
        System.out.println(HEADER);
        for (Product p : products) {
            System.out.println(p.getSummary());
        }
    }
}
