package formbarang;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class FormPelanggan extends JFrame {
    // Komponen UI
    private JTextField txtId = new JTextField(10);
    private JTextField txtNama = new JTextField(20);
    private JTable tabelPelanggan = new JTable();
    private DefaultTableModel model;

    public FormPelanggan() {
        setTitle("Master Data Pelanggan");
        setSize(500, 450);
        setLayout(new BorderLayout(10, 10));

        // Panel Input (Atas)
        JPanel pnlInput = new JPanel(new GridLayout(3, 2, 5, 5));
        pnlInput.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        pnlInput.add(new JLabel("ID Pelanggan:"));
        pnlInput.add(txtId);
        pnlInput.add(new JLabel("Nama Pelanggan:"));
        pnlInput.add(txtNama);

        // Panel Tombol
        JPanel pnlTombol = new JPanel();
        JButton btnSimpan = new JButton("Simpan");
        JButton btnUbah = new JButton("Ubah");
        JButton btnHapus = new JButton("Hapus");
        JButton btnBersih = new JButton("Bersih");
        pnlTombol.add(btnSimpan);
        pnlTombol.add(btnUbah);
        pnlTombol.add(btnHapus);
        pnlTombol.add(btnBersih);
        pnlInput.add(new JLabel("")); // Spacer
        pnlInput.add(pnlTombol);

        add(pnlInput, BorderLayout.NORTH);

        // Tabel (Tengah)
        model = new DefaultTableModel(new Object[]{"ID Pelanggan", "Nama Pelanggan"}, 0);
        tabelPelanggan.setModel(model);
        add(new JScrollPane(tabelPelanggan), BorderLayout.CENTER);

        // Load Data Awal
        tampilData();

        // Event Klik Tabel (untuk memindahkan data ke textfield saat mau ubah/hapus)
        tabelPelanggan.getSelectionModel().addListSelectionListener(e -> {
            if (tabelPelanggan.getSelectedRow() != -1) {
                int row = tabelPelanggan.getSelectedRow();
                txtId.setText(model.getValueAt(row, 0).toString());
                txtNama.setText(model.getValueAt(row, 1).toString());
                txtId.setEditable(false); // ID tidak boleh diubah saat mode edit
            }
        });

        // Action Listeners
        btnSimpan.addActionListener(e -> simpan());
        btnUbah.addActionListener(e -> ubah());
        btnHapus.addActionListener(e -> hapus());
        btnBersih.addActionListener(e -> bersih());

        setLocationRelativeTo(null);
    }

    private void tampilData() {
        model.setRowCount(0);
        try (Connection conn = Koneksi.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM Pelanggan")) {
            while (rs.next()) {
                model.addRow(new Object[]{rs.getString("id_pelanggan"), rs.getString("nama_pelanggan")});
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void simpan() {
        String sql = "INSERT INTO Pelanggan (id_pelanggan, nama_pelanggan) VALUES (?, ?)";
        try (Connection conn = Koneksi.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, txtId.getText());
            ps.setString(2, txtNama.getText());
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Data Berhasil Disimpan!");
            tampilData();
            bersih();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Gagal Simpan: " + e.getMessage());
        }
    }

    private void ubah() {
        String sql = "UPDATE Pelanggan SET nama_pelanggan=? WHERE id_pelanggan=?";
        try (Connection conn = Koneksi.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, txtNama.getText());
            ps.setString(2, txtId.getText());
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Data Berhasil Diperbarui!");
            tampilData();
            bersih();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Gagal Ubah: " + e.getMessage());
        }
    }

    private void hapus() {
    String id = txtId.getText();
    if (id.equals("")) {
        JOptionPane.showMessageDialog(this, "Pilih data yang akan dihapus!");
        return;
    }

    int confirm = JOptionPane.showConfirmDialog(this, 
        "Menghapus pelanggan akan menghapus SEMUA riwayat nota miliknya. Yakin?", 
        "Konfirmasi Hapus", JOptionPane.YES_NO_OPTION);

    if (confirm == JOptionPane.YES_OPTION) {
        Connection conn = Koneksi.getConnection();
        try {
            conn.setAutoCommit(false); 

            String sqlDetil = "DELETE FROM detilnota WHERE no_nota IN (SELECT no_nota FROM nota WHERE id_pelanggan=?)";
            PreparedStatement ps1 = conn.prepareStatement(sqlDetil);
            ps1.setString(1, id);
            ps1.executeUpdate();

            String sqlNota = "DELETE FROM nota WHERE id_pelanggan=?";
            PreparedStatement ps2 = conn.prepareStatement(sqlNota);
            ps2.setString(1, id);
            ps2.executeUpdate();

            String sqlPel = "DELETE FROM pelanggan WHERE id_pelanggan=?";
            PreparedStatement ps3 = conn.prepareStatement(sqlPel);
            ps3.setString(1, id);
            ps3.executeUpdate();

            conn.commit();
            
            JOptionPane.showMessageDialog(this, "Data Pelanggan dan Transaksinya Berhasil Dihapus!");
            tampilData();
            bersih();

        } catch (SQLException e) {
            try { conn.rollback(); } catch (SQLException ex) {} // Batalkan jika error
            JOptionPane.showMessageDialog(this, "Gagal Hapus: " + e.getMessage());
        } finally {
            try { conn.setAutoCommit(true); } catch (SQLException e) {}
        }
    }
}

    private void bersih() {
        txtId.setText("");
        txtNama.setText("");
        txtId.setEditable(true);
        tabelPelanggan.clearSelection();
    }
}