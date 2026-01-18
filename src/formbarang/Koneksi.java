package formbarang;

import java.sql.*;

public class Koneksi {
    public static Connection getConnection() {
        try {
            // Ganti 'db_toko' dengan nama database Anda
            String url = "jdbc:mysql://localhost:3306/db_toko";
            return DriverManager.getConnection(url, "root", "");
        } catch (SQLException e) {
            System.out.println("Gagal Koneksi: " + e.getMessage());
            return null;
        }
    }
}