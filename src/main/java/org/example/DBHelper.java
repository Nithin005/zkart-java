package org.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DBHelper {
    private String url;
    private Connection conn = null;

    public DBHelper(String url) throws SQLException {
        this.url = url;
        this.conn = DriverManager.getConnection(url);
    }

    public void close() throws SQLException {
        if (conn != null){
            conn.close();
        }
    }

    private void _createTable() {
        try(Statement stmt = conn.createStatement();){
            String createCustomerTableSql = "CREATE TABLE customer (" +
                    "email TEXT PRIMARY KEY," +
                    "encryptedPwd TEXT," +
                    "name TEXT," +
                    "mobile TEXT" +
                    ");";
            String createInventoryTableSql = "CREATE TABLE inventory (" +
                    "category TEXT," +
                    "brand TEXT," +
                    "model TEXT," +
                    "price INTEGER," +
                    "stock INTEGER" +
                    ");";
//            String createInvoiceTableSql = "CREATE TABLE invoice (" +
//                    "email TEXT," +
//                    "invoiceNo INTEGER," +
//                    "date TIMESTAMP," +
//                    "totalValue INTEGER," +
//                    "discountedValue INTEGER" +
//                    ");";
            String createDiscountTableSql = "CREATE TABLE discount (" +
                    "email TEXT PRIMARY KEY," +
                    "code TEXT," +
                    "expiry INTEGER" +
                    ");";
            stmt.executeUpdate("DROP TABLE IF EXISTS customer");
            stmt.executeUpdate("DROP TABLE IF EXISTS inventory");
            stmt.executeUpdate("DROP TABLE IF EXISTS discount");
            stmt.executeUpdate(createCustomerTableSql);
            stmt.executeUpdate(createInventoryTableSql);
//            stmt.executeUpdate(createInvoiceTableSql);
            stmt.executeUpdate(createDiscountTableSql);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private List<String> _readAllLines(Path path){
        List<String> lines = new ArrayList<>();
        try(BufferedReader reader = Files.newBufferedReader(path)){
            // skip header
            reader.readLine();

            String line;
            while ((line = reader.readLine()) != null){
                lines.add(line);
            }
        } catch(IOException e) {
            throw new RuntimeException(e);
        }
        return lines;
    }
    public void readFromTextFile(Path customerPath, Path inventoryPath){

        this._createTable();
        Modal.Customer customer;
        List<String> lines = this._readAllLines(customerPath);

        for(String l: lines){
            String[] data = l.split(" ");
//                UserName/Email EncryptedPwd Name Mobile
//                abc@zoho.com ApipNbjm Rahul 9922992299
            customer = new Modal.Customer(data[0], data[1], data[2], data[3]);
            try(PreparedStatement stmt = conn.prepareStatement("INSERT INTO customer VALUES (?, ?, ?, ?)")){
                stmt.setString(1, customer.getEmail());
                stmt.setString(2, customer.getEncryptedPwd());
                stmt.setString(3, customer.getName());
                stmt.setString(4, customer.getMobile());
                stmt.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }

        Modal.Item item;
        lines = this._readAllLines(inventoryPath);

        for(String l: lines){
            String[] data = l.split(" ");
//            Category Brand Model Price Stock
//            Mobile Apple 6S 60000 10
            item = new Modal.Item(data[0], data[1], data[2], Integer.parseInt(data[3]), Integer.parseInt(data[4]));
            try(PreparedStatement stmt = conn.prepareStatement("INSERT INTO inventory VALUES (?, ?, ?, ?, ?)")){
                stmt.setString(1, item.getCategory());
                stmt.setString(2, item.getBrand());
                stmt.setString(3, item.getModel());
                stmt.setInt(4, item.getPrice());
                stmt.setInt(5, item.getStock());
                stmt.executeUpdate();
            } catch (SQLException e){
                throw new RuntimeException(e);
            }
        }
    }

    public List<Modal.Item> queryItems(String whereClause){
        List<Modal.Item> items = new ArrayList<>();
        try(Statement stmt = conn.createStatement();){
            String sql = "SELECT * FROM inventory " + whereClause;
            ResultSet res = stmt.executeQuery(sql);
            while(res.next()){
                items.add(new Modal.Item(res.getString(1), res.getString(2), res.getString(3), res.getInt(4), res.getInt(5)));
            }
        }catch(SQLException e){
            throw new RuntimeException(e);
        }
        return items;
    }

    public Modal.Customer getCustomer(String emailId){
        try(Statement stmt = conn.createStatement();){
            String sql = "SELECT * FROM customer WHERE email='"+emailId+"'";
            ResultSet res = stmt.executeQuery(sql);
            if(res.next()){
                return new Modal.Customer(res.getString(1), res.getString(2), res.getString(3), res.getString(4));
            } else {
                return null;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void addCustomer(Modal.Customer customer){
        try(PreparedStatement stmt = conn.prepareStatement("INSERT INTO customer VALUES (?, ?, ?, ?)")){
            stmt.setString(1, customer.getEmail());
            stmt.setString(2, customer.getEncryptedPwd());
            stmt.setString(3, customer.getName());
            stmt.setString(4, customer.getMobile());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void updateCustomerPwd(Modal.Customer customer, String password){
        try(PreparedStatement stmt = conn.prepareStatement("UPDATE customer SET encryptedPwd = ? WHERE email = ?")){
            stmt.setString(1, Modal.Customer.encryptPwd(password));
            stmt.setString(2, customer.getEmail());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Modal.Customer> queryCustomers(String whereClause){
        List<Modal.Customer> customers = new ArrayList<>();
        try(Statement stmt = conn.createStatement();){
            String sql = "SELECT * FROM customer " + whereClause;
            ResultSet res = stmt.executeQuery(sql);
            while(res.next()){
                customers.add(new Modal.Customer(res.getString(1), res.getString(2), res.getString(3), res.getString(4)));
            }
        }catch(SQLException e){
            throw new RuntimeException(e);
        }
        return customers;
    }

    public void updateStock(Modal.Item item, int newStock){
        try(PreparedStatement stmt = conn.prepareStatement("UPDATE inventory SET stock = stock + ? WHERE category = ? AND brand = ? AND model = ?");){
            stmt.setInt(1, newStock);
            stmt.setString(2, item.getCategory());
            stmt.setString(3, item.getBrand());
            stmt.setString(4, item.getModel());
            stmt.executeUpdate();
        } catch(SQLException e){
            throw new RuntimeException(e);
        }
    }

    public String getDiscountCode(Modal.Customer customer){
        try(PreparedStatement stmt = conn.prepareStatement("SELECT * FROM discount WHERE email=?");){
            stmt.setString(1, customer.getEmail());
            ResultSet res = stmt.executeQuery();
            if(res.next()){
                // check validity
                int expiry = res.getInt(3);
                if(expiry<=0){
                    return null;
                }
                return res.getString(2);
            }
            return null;
        } catch(SQLException e){
            throw new RuntimeException(e);
        }
    }

    public void updateDiscountCode(Modal.Customer customer, String discountCode){
        try(PreparedStatement stmt = conn.prepareStatement("INSERT INTO discount (email, code, expiry) VALUES (?, ?, ?) ON CONFLICT(email) DO UPDATE SET expiry=excluded.expiry;");){
            stmt.setString(1, customer.getEmail());
            stmt.setString(2, discountCode);
            stmt.setInt(3, 3);
            stmt.executeUpdate();
        } catch (SQLException e){
            throw new RuntimeException(e);
        }
    }

    public void updateDiscountCodeExpiry(Modal.Customer customer){
        try(PreparedStatement stmt = conn.prepareStatement("UPDATE discount SET expiry=expiry-1 WHERE email = ?");){
            stmt.setString(1, customer.getEmail());
            stmt.executeUpdate();
        } catch (SQLException e){
            throw new RuntimeException(e);
        }
    }

    public void invalidateDiscountCode(Modal.Customer customer){
        try(PreparedStatement stmt = conn.prepareStatement("UPDATE discount SET expiry=0 WHERE email = ?");){
            stmt.setString(1, customer.getEmail());
            stmt.executeUpdate();
        } catch (SQLException e){
            throw new RuntimeException(e);
        }
    }


}
