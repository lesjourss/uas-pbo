package formbarang;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class FormNota extends JFrame {
    private JTextField txtNo = new JTextField(10);
    private JTextField txtIdPel = new JTextField(10);
    private JTextField txtKdBrg = new JTextField(10);
    private JTextField txtQty = new JTextField(5);
    private DefaultTableModel model;
    private JTable tabel;

    public FormNota() {
        setTitle("Input Transaksi Nota");
        setSize(600, 500);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        // --- PANEL ATAS (Input Header & Barang) ---
        JPanel pnlAtas = new JPanel(new GridLayout(3, 1, 5, 5));
        pnlAtas.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Baris 1: No Nota & ID Pelanggan
        JPanel baris1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        baris1.add(new JLabel("No Nota:  ")); baris1.add(txtNo);
        baris1.add(new JLabel("   ID Pel:   ")); baris1.add(txtIdPel);
        
        // Baris 2: Kode Barang & Qty
        JPanel baris2 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        baris2.add(new JLabel("Kode Brg:")); baris2.add(txtKdBrg);
        baris2.add(new JLabel("   Qty:      ")); baris2.add(txtQty);

        // Baris 3: Tombol Aksi
        JPanel baris3 = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton btnTambah = new JButton("Tambah Barang");
        JButton btnSimpanCetak = new JButton("Simpan & Cetak Nota");
        btnTambah.setBackground(new Color(200, 230, 255)); // Warna biru muda
        btnSimpanCetak.setBackground(new Color(200, 255, 200)); // Warna hijau muda
        baris3.add(btnTambah);
        baris3.add(btnSimpanCetak);

        pnlAtas.add(baris1);
        pnlAtas.add(baris2);
        pnlAtas.add(baris3);
        add(pnlAtas, BorderLayout.NORTH);

        // --- PANEL TENGAH (Tabel) ---
        model = new DefaultTableModel(new Object[]{"Kode Barang", "Qty", "Subtotal"}, 0);
        tabel = new JTable(model);
        JScrollPane scroll = new JScrollPane(tabel);
        scroll.setBorder(BorderFactory.createTitledBorder("Daftar Belanja"));
        add(scroll, BorderLayout.CENTER);

        // --- LOGIKA TOMBOL ---
        
        // 1. Tombol Tambah ke Tabel Sementara
        btnTambah.addActionListener(e -> {
            if(txtKdBrg.getText().isEmpty() || txtQty.getText().isEmpty()){
                JOptionPane.showMessageDialog(this, "Lengkapi Kode Barang & Qty!");
                return;
            }
            // Subtotal sementara diset 0, nanti dihitung saat simpan atau bisa via query
            model.addRow(new Object[]{txtKdBrg.getText(), txtQty.getText(), "0"});
            txtKdBrg.setText("");
            txtQty.setText("");
            txtKdBrg.requestFocus();
        });

        // 2. Tombol Simpan ke Database & Cetak
        btnSimpanCetak.addActionListener(e -> simpanDanCetak());

        setLocationRelativeTo(null);
    }

    private void simpanDanCetak() {
        if (model.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "Tabel belanja kosong!");
            return;
        }

        try (Connection conn = Koneksi.getConnection()) {
            conn.setAutoCommit(false); // Transaksi

            // 1. Simpan ke tabel Nota
            PreparedStatement psNota = conn.prepareStatement("INSERT INTO nota (no_nota, tanggal, id_pelanggan) VALUES (?, NOW(), ?)");
            psNota.setString(1, txtNo.getText());
            psNota.setString(2, txtIdPel.getText());
            psNota.executeUpdate();

            // 2. Simpan semua barang dari JTable ke DetilNota
            PreparedStatement psDetil = conn.prepareStatement("INSERT INTO detilnota (no_nota, kode_barang, qty, subtotal) VALUES (?, ?, ?, 0)");
            for (int i = 0; i < model.getRowCount(); i++) {
                psDetil.setString(1, txtNo.getText());
                psDetil.setString(2, model.getValueAt(i, 0).toString());
                psDetil.setInt(3, Integer.parseInt(model.getValueAt(i, 1).toString()));
                psDetil.addBatch();
            }
            psDetil.executeBatch();
            conn.commit();

            // 3. Simulasi Cetak Nota ke Console
            System.out.println("\n========= STRUK PEMBAYARAN =========");
            System.out.println("No Nota   : " + txtNo.getText());
            System.out.println("Pelanggan : " + txtIdPel.getText());
            System.out.println("------------------------------------");
            for (int i = 0; i < model.getRowCount(); i++) {
                System.out.println(model.getValueAt(i, 0) + "\t x" + model.getValueAt(i, 1));
            }
            System.out.println("====================================");

            JOptionPane.showMessageDialog(this, "Nota Berhasil Disimpan & Dicetak ke Console!");
            model.setRowCount(0); // Kosongkan tabel
            txtNo.setText("");
            txtIdPel.setText("");
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }
}