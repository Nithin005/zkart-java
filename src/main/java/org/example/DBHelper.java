package org.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DBHelper {
    private final Connection conn;

    public DBHelper(String url) throws SQLException {
        this.conn = DriverManager.getConnection(url);
    }

    public void close() throws SQLException {
        if (conn != null){
            conn.close();
        }
    }

    private void _createTable() {
        String createCustomerTableSql = "CREATE TABLE customer (" +
                "customer_id INTEGER PRIMARY KEY," +
                "email TEXT NOT NULL," +
                "encrypted_pwd TEXT NOT NULL," +
                "name TEXT NOT NULL," +
                "mobile TEXT NOT NULL," +
                "discount_code TEXT," +
                "discount_expiry INTEGER" +
                ");";
        String createInventoryTableSql = "CREATE TABLE inventory (" +
                "item_id INTEGER PRIMARY KEY," +
                "category TEXT," +
                "brand TEXT," +
                "model TEXT," +
                "price INTEGER," +
                "stock INTEGER" +
                ");";
        try(Statement stmt = conn.createStatement()){
            stmt.executeUpdate("DROP TABLE IF EXISTS customer");
            stmt.executeUpdate("DROP TABLE IF EXISTS inventory");
            stmt.executeUpdate(createCustomerTableSql);
            stmt.executeUpdate(createInventoryTableSql);
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
        Customer customer;
        List<String> lines = this._readAllLines(customerPath);

        for(String l: lines){
            String[] data = l.split(" ");
//                UserName/Email EncryptedPwd Name Mobile
//                abc@zoho.com ApipNbjm Rahul 9922992299
            customer = new Customer(0, data[0], data[1], data[2], data[3]);
            try(PreparedStatement stmt = conn.prepareStatement("INSERT INTO customer (email, encrypted_pwd, name, mobile) VALUES (?, ?, ?, ?)")){
                stmt.setString(1, customer.getEmail());
                stmt.setString(2, customer.getEncryptedPwd());
                stmt.setString(3, customer.getName());
                stmt.setString(4, customer.getMobile());
                stmt.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }

        Item item;
        lines = this._readAllLines(inventoryPath);

        for(String l: lines){
            String[] data = l.split(" ");
//            Category Brand Model Price Stock
//            Mobile Apple 6S 60000 10
            item = new Item(0, data[0], data[1], data[2], Integer.parseInt(data[3]), Integer.parseInt(data[4]));
            try(PreparedStatement stmt = conn.prepareStatement("INSERT INTO inventory (category, brand, model, price, stock) VALUES (?, ?, ?, ?, ?)")){
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

    public List<Item> queryItems(String whereClause){
        List<Item> items = new ArrayList<>();
        try(Statement stmt = conn.createStatement()){
            String sql = "SELECT item_id, category, brand, model, price, stock FROM inventory " + whereClause;
            ResultSet res = stmt.executeQuery(sql);
            while(res.next()){
                items.add(new Item(res.getInt(1), res.getString(2), res.getString(3), res.getString(4), res.getInt(5), res.getInt(6)));
            }
        }catch(SQLException e){
            throw new RuntimeException(e);
        }
        return items;
    }


    public Customer getCustomer(String emailId){
        try(PreparedStatement stmt = conn.prepareStatement("SELECT customer_id, email, encrypted_pwd, name, mobile FROM customer WHERE email= ?")){
            stmt.setString(1, emailId);
            ResultSet res = stmt.executeQuery();
            if(res.next()){
                return new Customer(res.getInt(1), res.getString(2), res.getString(3), res.getString(4), res.getString(5));
            } else {
                return null;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public int getCustomerId(Customer customer){
        try(PreparedStatement stmt = conn.prepareStatement("SELECT customer_id FROM customer WHERE email = ?")){
            stmt.setString(1, customer.getEmail());
            ResultSet res = stmt.executeQuery();
            if(res.next()){
                return res.getInt(1);
            }
        } catch (SQLException e){
            throw new RuntimeException(e);
        }
        return -1;
    }

    public int addCustomer(Customer customer){
        try(PreparedStatement stmt = conn.prepareStatement("INSERT INTO customer (email, encrypted_pwd, name, mobile) VALUES (?, ?, ?, ?)")){
            stmt.setString(1, customer.getEmail());
            stmt.setString(2, customer.getEncryptedPwd());
            stmt.setString(3, customer.getName());
            stmt.setString(4, customer.getMobile());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return getCustomerId(customer);
    }

    public void updateCustomerPwd(Customer customer, String password){
        try(PreparedStatement stmt = conn.prepareStatement("UPDATE customer SET encrypted_pwd = ? WHERE customer_id = ?")){
            stmt.setString(1, Utils.encryptPwd(password));
            stmt.setInt(2, customer.getCustomerId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    public void updateStock(Item item, int newStock){
        try(PreparedStatement stmt = conn.prepareStatement("UPDATE inventory SET stock = stock + ? WHERE item_id = ?")){
            stmt.setInt(1, newStock);
            stmt.setInt(2, item.getItemId());
            stmt.executeUpdate();
        } catch(SQLException e){
            throw new RuntimeException(e);
        }
    }

    public String getDiscountCode(Customer customer){
        try(PreparedStatement stmt = conn.prepareStatement("SELECT discount_code, discount_expiry FROM customer WHERE customer_id = ?")){
            stmt.setInt(1, customer.getCustomerId());
            ResultSet res = stmt.executeQuery();
            if(res.next()){
                // check validity
                int expiry = res.getInt(2);
                if(expiry<=0){
                    return null;
                }
                return res.getString(1);
            }
            return null;
        } catch(SQLException e){
            throw new RuntimeException(e);
        }
    }

    public void updateDiscountCode(Customer customer, String discountCode){
        try(PreparedStatement stmt = conn.prepareStatement("UPDATE customer SET discount_code = ? WHERE customer_id = ?")){
            stmt.setString(1, discountCode);
            stmt.setInt(2, customer.getCustomerId());
            stmt.executeUpdate();
        } catch (SQLException e){
            throw new RuntimeException(e);
        }
    }

    public void updateDiscountCodeExpiry(Customer customer){
        try(PreparedStatement stmt = conn.prepareStatement("UPDATE customer SET discount_expiry=discount_expiry-1 WHERE customer_id = ?")){
            stmt.setInt(1, customer.getCustomerId());
            stmt.executeUpdate();
        } catch (SQLException e){
            throw new RuntimeException(e);
        }
    }

    public void invalidateDiscountCode(Customer customer){
        try(PreparedStatement stmt = conn.prepareStatement("UPDATE customer SET discount_expiry=0 WHERE customer_id = ?")){
            stmt.setInt(1, customer.getCustomerId());
            stmt.executeUpdate();
        } catch (SQLException e){
            throw new RuntimeException(e);
        }
    }

}
