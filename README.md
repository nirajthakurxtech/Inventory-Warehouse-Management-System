# Inventory / Warehouse Management System

A menu-driven, terminal-based backend application written in **core Java (JDK 11+)** for tracking products, stock levels, and incoming/outgoing stock transactions in a small warehouse. It uses **JDBC with a SQLite database**, so all data persists between program runs. This project is my submission for the Java Course Project (**Option 5: Inventory / Warehouse Management System**).

---

## Table of Contents
1. [Features Implemented](#1-features-implemented)
2. [Technologies Used](#2-technologies-used)
3. [Project Structure](#3-project-structure)
4. [Database Setup and Schema](#4-database-setup-and-schema)
5. [Setup and Run Instructions](#5-setup-and-run-instructions)
6. [How to Use (Menu Guide)](#6-how-to-use-menu-guide)
7. [Screenshots](#7-screenshots)
8. [Requirements Coverage (Assignment Checklist)](#8-requirements-coverage-assignment-checklist)
9. [Design Notes](#9-design-notes)
10. [Known Limitations](#10-known-limitations)

---

## 1. Features Implemented

All **core features** required for Option 5 are implemented:

| Required feature (from assignment) | Implemented as |
|---|---|
| Add, update, delete, and search products (name, SKU, category, quantity, unit price) | Menu options 1, 4, 5, 3 (plus 2 to list all) |
| Record stock-in and stock-out transactions, updating quantity on hand | Menu options 6 and 7 |
| Prevent stock-out transactions that would result in negative inventory | `InsufficientStockException` thrown by `InventoryService`; also a database `CHECK (quantity >= 0)` |
| Low-stock alert list (products below a reorder threshold) | Menu option 8 (each product has its own `reorder_level`) |
| Report of current stock value by category | Menu option 9 (per-category totals plus grand total) |

**Additional functionality**
- Duplicate SKU protection (`DuplicateSkuException`).
- Per-product **transaction history** log (menu option 10). Opening stock is logged automatically.
- Search by name, SKU, or category (case-insensitive, partial match).
- Update keeps current values when you press Enter.
- Confirmation prompt before deleting a product (its history is deleted with it).
- Stock changes run as **database transactions** (commit/rollback), so the quantity and the history record never get out of sync.
- Full input validation (empty input, non-numeric input, negative numbers, invalid menu choice).

**Stretch goals:** not implemented (see [Known Limitations](#10-known-limitations)).

---

## 2. Technologies Used

| Item | Details |
|---|---|
| Language | Java 11 or higher (core Java only; no Spring Boot, no web/GUI framework) |
| Database | SQLite (relational; no server installation needed) |
| JDBC driver | `org.xerial:sqlite-jdbc` **3.46.1.3** |
| Build tool | Maven (optional) or plain `javac` / `java` |
| Version control | Git and GitHub |

---

## 3. Project Structure

```
inventory-system/
├── pom.xml                       Maven build file (JDBC driver + run plugin)
├── schema.sql                    SQL script that creates the tables
├── config.properties.example     Template for DB settings (copy to config.properties)
├── .gitignore                    Excludes config.properties, *.db, target/, out/
├── README.md                     This file
├── docs/screenshots/             Terminal screenshots used in this README
└── src/main/java/inventory/
    ├── main/
    │   └── Main.java                     Menu-driven console interface
    ├── model/
    │   ├── BaseEntity.java               ABSTRACT class (id + abstract getSummary())
    │   ├── Product.java                  extends BaseEntity
    │   └── StockTransaction.java         extends BaseEntity (enum Type: IN / OUT)
    ├── dao/
    │   ├── ProductDao.java               JDBC CRUD for products
    │   └── TransactionDao.java           JDBC access for stock transactions
    ├── service/
    │   └── InventoryService.java         Business rules, Collections, reports
    ├── exception/
    │   ├── InventoryException.java       Base custom (checked) exception
    │   ├── ProductNotFoundException.java
    │   ├── DuplicateSkuException.java
    │   └── InsufficientStockException.java
    └── util/
        ├── DBConnection.java             Connection factory and schema initialisation
        └── InputHelper.java              Validated Scanner input
```

**Layering:** `Main` (UI) → `InventoryService` (rules) → DAOs (SQL) → SQLite.

---

## 4. Database Setup and Schema

**No manual setup is needed.** The application creates the database file (`inventory.db`) and both tables automatically on first run. The identical SQL is provided in `schema.sql` for reference or if you want to create the tables yourself (for example `sqlite3 inventory.db < schema.sql`).

### Schema

```sql
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
```

**Relationship:** one product has many stock transactions (`stock_transactions.product_id → products.id`).

### Database credentials / configuration
Credentials are **never hard-coded**. Settings are read in this order:
1. Environment variables: `INVENTORY_DB_URL`, `INVENTORY_DB_USER`, `INVENTORY_DB_PASSWORD`
2. `config.properties` in the project root (**git-ignored**)
3. Default: `jdbc:sqlite:inventory.db` (no username/password needed)

To customise, copy the template:
```
cp config.properties.example config.properties      # Windows: copy config.properties.example config.properties
```
```properties
db.url=jdbc:sqlite:inventory.db
db.user=
db.password=
```
SQLite needs no username or password. The fields exist so a MySQL/PostgreSQL URL can be used instead without changing code.

---

## 5. Setup and Run Instructions

### Prerequisites
- JDK 11 or newer (`java -version` and `javac -version` to check)
- Maven 3.6+ (only for Option A)

### Option A: Maven (recommended)
```bash
git clone <your-repository-url>
cd inventory-system
mvn compile exec:java
```
Maven downloads the SQLite JDBC driver automatically and starts the application.

### Option B: Plain `javac` / `java`
1. Create a `lib/` folder in the project root and download into it:
   - `sqlite-jdbc-3.46.1.3.jar`
   - `slf4j-api` jar (2.x, required by the driver)
2. Compile and run:

**Linux / macOS**
```bash
javac -d out -cp "lib/*" $(find src -name "*.java")
java -cp "out:lib/*" inventory.main.Main
```
**Windows (Command Prompt)**
```bat
dir /s /b src\*.java > sources.txt
javac -d out -cp "lib/*" @sources.txt
java -cp "out;lib/*" inventory.main.Main
```

### Running tips
- Run from the **project root** so `inventory.db` and `config.properties` are found there.
- Data is saved automatically. Delete `inventory.db` to reset to an empty database.

---

## 6. How to Use (Menu Guide)

```
===== INVENTORY MANAGEMENT SYSTEM =====
 1. Add product
 2. View all products
 3. Search products
 4. Update product
 5. Delete product
 6. Stock IN
 7. Stock OUT
 8. Low-stock alert
 9. Stock value by category
10. Transaction history
 0. Exit
```

| Option | What it does |
|---|---|
| 1 Add product | Asks for SKU, name, category, opening quantity, unit price, reorder level. SKU must be unique. |
| 2 View all | Lists every product in a table. |
| 3 Search | Finds products whose name, SKU, or category contains your text. |
| 4 Update | Edits name, category, unit price, reorder level (Enter = keep). Quantity changes only via Stock IN/OUT. |
| 5 Delete | Deletes a product and its history after a y/n confirmation. |
| 6 Stock IN | Adds received quantity and logs a transaction. |
| 7 Stock OUT | Removes quantity and logs a transaction. Rejected if it would go below zero. |
| 8 Low-stock alert | Lists products whose quantity is below their reorder level (lowest first). |
| 9 Value report | Shows total stock value (quantity × unit price) per category plus a grand total. |
| 10 History | Shows all IN/OUT transactions for one product, newest first. |

**Example error handling:** entering `abc` where a number is expected re-prompts; removing more stock than available prints `Error: Insufficient stock for 'Widget': available 5, requested 10.` and changes nothing.

---

## 7. Screenshots

> Add at least 2–3 real screenshots of the running application in `docs/screenshots/` and keep the links below.

**Main menu and adding a product**

![Main menu and add product](docs/screenshots/01-menu-add-product.png)

**Stock in / stock out, including a rejected stock-out**

![Stock movements](docs/screenshots/02-stock-movements.png)

**Low-stock alert and stock value report**

![Reports](docs/screenshots/03-reports.png)

---

## 8. Requirements Coverage (Assignment Checklist)

### 8.1 Technical requirements

| Requirement | Where it is met |
|---|---|
| Core Java (JDK 11+), no Spring Boot / web / GUI framework | Plain Java terminal app; `pom.xml` targets Java 11 and only depends on the SQLite JDBC driver |
| At least one abstract class or interface | `BaseEntity` (abstract) |
| Encapsulation (private fields, public getters/setters) in all model classes | `Product`, `StockTransaction`, `BaseEntity` |
| At least one example of polymorphism | `getSummary()` is abstract in `BaseEntity` and overridden differently in `Product` and `StockTransaction`; also overloaded constructors in both models |
| At least 4–5 well-defined classes beyond Main | 15 classes: 3 models, 2 DAOs, 1 service, 4 exceptions, 2 utilities, plus `Main` |
| A `List` implementation | `ArrayList<Product>` / `ArrayList<StockTransaction>` in DAOs and service |
| A `Map` implementation | `HashMap<Integer, Product>` (ID → product lookup); `TreeMap<String, Double>` (category → stock value) |
| Iteration, sorting, or filtering over a collection | `Comparator` sorting (`getLowStockProducts`, `getProductsSortedByValue`), loops that filter low-stock items and aggregate values |
| Working connection to a relational database | SQLite via JDBC (`DBConnection`) |
| Full CRUD backed by the database | `ProductDao`: insert / find / update / delete; `TransactionDao`: insert / find / delete |
| `PreparedStatement` for all queries with user input | Every DAO query with parameters uses `PreparedStatement`; no string-concatenated SQL |
| try-with-resources for Connection, Statement, ResultSet | Used throughout the DAOs and service |
| Meaningful `SQLException` handling | `Main` catches it and prints `Database error: <message>`; service methods roll back transactions on failure |
| At least one custom exception | `ProductNotFoundException`, `DuplicateSkuException`, `InsufficientStockException` (extend `InventoryException`) |
| Menu-driven console UI with input validation | `Main` + `InputHelper` (rejects empty, non-numeric, and out-of-range input) |
| Data persists between runs | Stored in `inventory.db` |

### 8.2 Code quality requirements

| Requirement | Where it is met |
|---|---|
| Readable code, meaningful names | Descriptive class, method, and variable names |
| Logical package structure | `main`, `model`, `dao`, `service`, `exception`, `util` |
| No unused imports/variables or dead code | Checked before submission |
| No unhandled exceptions crashing the program in normal use | All checked exceptions are caught in the menu loop |
| Credentials not hard-coded; config file/env var excluded via `.gitignore` | `DBConnection` reads env vars / `config.properties`; `.gitignore` excludes `config.properties` and `*.db` |

### 8.3 Documentation requirements (this README)

| Required item | Section |
|---|---|
| Project title and 2–3 sentence description | Top of file |
| List of features implemented | [Section 1](#1-features-implemented) |
| Technologies / libraries (Java version, JDBC driver, database) | [Section 2](#2-technologies-used) |
| Database setup instructions including the schema (SQL script) | [Section 4](#4-database-setup-and-schema) and `schema.sql` |
| Setup and run instructions | [Section 5](#5-setup-and-run-instructions) |
| At least 2–3 screenshots | [Section 7](#7-screenshots) |
| Known limitations / features not completed | [Section 10](#10-known-limitations) |

### 8.4 Submission guidelines

| Requirement | Status |
|---|---|
| Public GitHub repository | Push the whole project to a public repo |
| At least 8–10 commits with clear messages, no single "final commit" | See suggested commit plan below |
| `README.md` in repo root plus SQL schema script | `README.md` and `schema.sql` are in the root |
| Repository link submitted through the LMS before the deadline | Submit the link |
| Compiles and runs cleanly from a fresh clone using this README | Follow [Section 5](#5-setup-and-run-instructions) on a fresh clone to verify |

**Suggested commit plan**
1. Initial commit: `.gitignore`, `pom.xml`
2. Add `schema.sql` and `DBConnection`
3. Add model classes (`BaseEntity`, `Product`, `StockTransaction`)
4. Add custom exceptions
5. Add `ProductDao` (CRUD)
6. Add `TransactionDao`
7. Add `InventoryService` (stock rules)
8. Add `InputHelper` and `Main` menu
9. Add reports (low stock, value by category, history)
10. Add README, screenshots, and final cleanup

---

## 9. Design Notes

- **Abstraction and inheritance:** `BaseEntity` holds the shared `id` and declares `getSummary()`; subtypes provide their own formatting.
- **Layered design:** SQL lives only in DAOs, business rules only in the service, and user interaction only in `Main`.
- **Atomic stock updates:** stock-in, stock-out, and delete run in one JDBC transaction (`setAutoCommit(false)`, `commit()`, `rollback()` on error).
- **Double protection against negative stock:** checked in Java, and enforced by the `CHECK (quantity >= 0)` constraint.
- **Security:** parameterised queries prevent SQL injection; credentials come from environment variables or an ignored config file.

---

## 10. Known Limitations

- Single-user console application (no login or roles).
- The following **stretch goals were not implemented**: supplier records linked to stock-in transactions, date filtering on the transaction history, and a reorder suggestion report.
- Quantity cannot be edited directly; it changes only through stock-in/stock-out (intentional, to keep the history accurate).
- No automated unit tests.
- Currency is shown as a plain number (no currency symbol or locale formatting).

---

## Academic Integrity

AI tools were used to learn and structure the project. I have reviewed the code and can explain every part of it.
