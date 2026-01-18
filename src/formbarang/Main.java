package formbarang;
import javax.swing.*;
import java.awt.*;

public class Main extends JFrame {
    public Main() {
        setTitle("Aplikasi Penjualan Toko");
        setSize(300, 200);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new GridLayout(3, 1));

        JButton btnBarang = new JButton("Data Barang");
        JButton btnPelanggan = new JButton("Data Pelanggan");
        JButton btnNota = new JButton("Transaksi Nota");

        add(btnBarang); add(btnPelanggan); add(btnNota);

        btnBarang.addActionListener(e -> new FormBarang().setVisible(true));
        btnPelanggan.addActionListener(e -> new FormPelanggan().setVisible(true));
        btnNota.addActionListener(e -> new FormNota().setVisible(true));
        
        setLocationRelativeTo(null);
    }

    public static void main(String[] args) {
        new Main().setVisible(true);
    }
}