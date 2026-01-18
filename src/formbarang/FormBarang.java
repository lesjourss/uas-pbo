package formbarang;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class FormBarang extends JFrame {
 
    private JTextField txtKode = new JTextField(10);
    private JTextField txtNama = new JTextField(20);
    private JTextField txtHarga = new JTextField(10);
    private JTable tabelBarang = new JTable();
    private DefaultTableModel model;

    public FormBarang() {
        setTitle("Master Data Barang");
        setSize(550, 500);
        setLayout(new BorderLayout(10, 10));

        JPanel pnlInput = new JPanel(new GridLayout(4, 2, 5, 5));
        pnlInput.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        pnlInput.add(new JLabel("Kode Barang:"));
        pnlInput.add(txtKode);
        pnlInput.add(new JLabel("Nama Barang:"));
        pnlInput.add(txtNama);
        pnlInput.add(new JLabel("Harga Satuan:"));
        pnlInput.add(txtHarga);

        JPanel pnlTombol = new JPanel();
        JButton btnSimpan = new JButton("Simpan");
        JButton btnUbah = new JButton("Ubah");
        JButton btnHapus = new JButton("Hapus");
        JButton btnBersih = new JButton("Bersih");
        
        pnlTombol.add(btnSimpan);
        pnlTombol.add(btnUbah);
        pnlTombol.add(btnHapus);
        pnlTombol.add(btnBersih);
        
        pnlInput.add(new JLabel("")); 
        pnlInput.add(pnlTombol);

        add(pnlInput, BorderLayout.NORTH);

        model = new DefaultTableModel(new Object[]{"Kode Barang", "Nama Barang", "Harga"}, 0);
        tabelBarang.setModel(model);
        add(new JScrollPane(tabelBarang), BorderLayout.CENTER);

        loadData();

        tabelBarang.getSelectionModel().addListSelectionListener(e -> {
            if (tabelBarang.getSelectedRow() != -1) {
                int row = tabelBarang.getSelectedRow();
                txtKode.setText(model.getValueAt(row, 0).toString());
                txtNama.setText(model.getValueAt(row, 1).toString());
                txtHarga.setText(model.getValueAt(row, 2).toString());
                txtKode.setEditable(false); 
            }
        });

        btnSimpan.addActionListener(e -> simpan());
        btnUbah.addActionListener(e -> ubah());
        btnHapus.addActionListener(e -> hapus());
        btnBersih.addActionListener(e -> bersih());

        setLocationRelativeTo(null);
    }

    private void loadData() {
        model.setRowCount(0);
        try (Connection conn = Koneksi.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM barang")) {
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getString("kode_barang"), 
                    rs.getString("nama_barang"), 
                    rs.getDouble("harga")
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Gagal Load Data: " + e.getMessage());
        }
    }

    private void simpan() {
        String sql = "INSERT INTO barang (kode_barang, nama_barang, harga) VALUES (?, ?, ?)";
        try (Connection conn = Koneksi.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, txtKode.getText());
            ps.setString(2, txtNama.getText());
            ps.setDouble(3, Double.parseDouble(txtHarga.getText()));
            ps.executeUpdate();
            
            JOptionPane.showMessageDialog(this, "Barang Berhasil Disimpan!");
            loadData();
            bersih();
        } catch (SQLException | NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Gagal Simpan: " + e.getMessage());
        }
    }

    private void ubah() {
        String sql = "UPDATE barang SET nama_barang=?, harga=? WHERE kode_barang=?";
        try (Connection conn = Koneksi.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, txtNama.getText());
            ps.setDouble(2, Double.parseDouble(txtHarga.getText()));
            ps.setString(3, txtKode.getText());
            ps.executeUpdate();
            
            JOptionPane.showMessageDialog(this, "Barang Berhasil Diperbarui!");
            loadData();
            bersih();
        } catch (SQLException | NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Gagal Update: " + e.getMessage());
        }
    }

    private void hapus() {
        String kode = txtKode.getText();
        if (kode.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Pilih barang dulu!");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, 
            "Menghapus barang ini akan menghapus semua riwayat transaksi terkait. Lanjutkan?", 
            "Konfirmasi Hapus", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection conn = Koneksi.getConnection()) {
                conn.setAutoCommit(false); 

                PreparedStatement ps1 = conn.prepareStatement("DELETE FROM detilnota WHERE kode_barang=?");
                ps1.setString(1, kode);
                ps1.executeUpdate();

                PreparedStatement ps2 = conn.prepareStatement("DELETE FROM barang WHERE kode_barang=?");
                ps2.setString(1, kode);
                ps2.executeUpdate();

                conn.commit(); 
                JOptionPane.showMessageDialog(this, "Barang Berhasil Dihapus!");
                loadData();
                bersih();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Gagal Hapus: " + e.getMessage());
            }
        }
    }

    private void bersih() {
        txtKode.setText("");
        txtNama.setText("");
        txtHarga.setText("");
        txtKode.setEditable(true);
        tabelBarang.clearSelection();
    }
}