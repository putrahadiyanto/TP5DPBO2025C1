import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class Menu extends JFrame{
    public static void main(String[] args) {
        // buat object window
        Menu window = new Menu();

        // atur ukuran window
        window.setSize(480, 540);

        // letakkan window di tengah layar
        window.setLocationRelativeTo(null);

        // isi window
        window.setContentPane(window.mainPanel);

        // ubah warna background
        window.getContentPane().setBackground(Color.white);

        // tampilkan window
        window.setVisible(true);

        // agar program ikut berhenti saat window diclose
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    }

    // index baris yang diklik
    private int selectedIndex = -1;
    // list untuk menampung semua mahasiswa
    private ArrayList<Mahasiswa> listMahasiswa;

    private JPanel mainPanel;
    private JTextField nimField;
    private JTextField namaField;
    private JTable mahasiswaTable;
    private JButton addUpdateButton;
    private JButton cancelButton;
    private JComboBox jenisKelaminComboBox;
    private JButton deleteButton;
    private JLabel titleLabel;
    private JLabel nimLabel;
    private JLabel namaLabel;
    private JLabel jenisKelaminLabel;
    private JLabel statusLabel;
    private JRadioButton aktifRadioButton;
    private JRadioButton alumniRadioButton;
    private JRadioButton cutiRadioButton;
    private ButtonGroup statusGroup;
    private Database database;

    // constructor
    public Menu() {
        // inisialisasi listMahasiswa
        listMahasiswa = new ArrayList<>();

        database = new Database();

        statusGroup = new ButtonGroup();
        statusGroup.add(aktifRadioButton);
        statusGroup.add(cutiRadioButton);
        statusGroup.add(alumniRadioButton);

        // isi tabel mahasiswa
        mahasiswaTable.setModel(setTable());

        // ubah styling title
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 20f));

        // atur isi combo box
        String[] jenisKelaminData = {"Laki-laki", "Perempuan"};
        jenisKelaminComboBox.setModel(new DefaultComboBoxModel(jenisKelaminData));

        // sembunyikan button delete
        deleteButton.setVisible(false);

        // saat tombol add/update ditekan
        addUpdateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(selectedIndex == -1) {
                    insertData();
                } else{
                    updateData();
                }
            }
        });
        // saat tombol delete ditekan
        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(selectedIndex >= 0){
                    int confirm = JOptionPane.showConfirmDialog(
                            null,
                            "Apakah Anda yakin ingin menghapus data ini?",
                            "Konfirmasi Hapus",
                            JOptionPane.YES_NO_OPTION
                    );

                    // Delete only if user clicks "Yes"
                    if (confirm == JOptionPane.YES_OPTION) {
                        deleteData();
                    }
                }
            }
        });
        // saat tombol cancel ditekan
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                clearForm();
            }
        });
        // saat salah satu baris tabel ditekan
        mahasiswaTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                // ubah selectedIndex menjadi baris tabel yang diklik
                selectedIndex = mahasiswaTable.getSelectedRow();

                // simpan value textfield dan combo box
                String selectedNim = mahasiswaTable.getModel().getValueAt(selectedIndex, 1).toString();
                String selectedNama = mahasiswaTable.getModel().getValueAt(selectedIndex, 2).toString();
                String selectedJenisKelamin = mahasiswaTable.getModel().getValueAt(selectedIndex, 3).toString();
                String selectedStatusMahasiswa = mahasiswaTable.getModel().getValueAt(selectedIndex, 4).toString();

                // ubah isi textfield dan combo box
                nimField.setText(selectedNim);
                namaField.setText(selectedNama);
                jenisKelaminComboBox.setSelectedItem(selectedJenisKelamin);
                if(selectedStatusMahasiswa.equals("Aktif")){
                    aktifRadioButton.setSelected(true);
                }else if(selectedStatusMahasiswa.equals("Cuti")){
                    cutiRadioButton.setSelected(true);
                }else if(selectedStatusMahasiswa.equals("Alumni")){
                    alumniRadioButton.setSelected(true);
                }

                // ubah button "Add" menjadi "Update"
                addUpdateButton.setText("Update");

                // tampilkan button delete
                deleteButton.setVisible(true);
            }
        });
    }

    public final DefaultTableModel setTable() {
        // tentukan kolom tabel
        Object[] column = {"No", "Nim", "Nama", "Jenis Kelamin", "Status Mahasiswa"};

        // buat objek tabel dengan kolom yang sudah dibuat
        DefaultTableModel temp = new DefaultTableModel(null, column);

        try {
            ResultSet resultSet = database.selectQuery("SELECT * from mahasiswa");
            int i = 0;
            while (resultSet.next()) {
                Object[] row = new Object[6];
                row[0] = i + 1;
                row[1] = resultSet.getString("nim");
                row[2] = resultSet.getString("nama");
                row[3] = resultSet.getString("jenis_kelamin");
                row[4] = resultSet.getString("statusMahasiswa");

                temp.addRow(row);
                i++;
            }
        } catch(SQLException e){
            throw new RuntimeException(e);
        }

        return temp;

    }

    public void insertData() {
        // ambil value dari textfield dan combobox
        String nim = nimField.getText();
        String nama = namaField.getText();
        String jenisKelamin = jenisKelaminComboBox.getSelectedItem().toString();
        String statusMahasiswa = "";
        if(aktifRadioButton.isSelected()){
            statusMahasiswa = "Aktif";
        }else if(cutiRadioButton.isSelected()){
            statusMahasiswa = "Cuti";
        }else if(alumniRadioButton.isSelected()){
            statusMahasiswa = "Alumni";
        }

        String sql = "INSERT INTO mahasiswa (id, nim, nama, jenis_kelamin, statusMahasiswa) VALUES (null, ?, ?, ?, ?);";

        try{
            Connection conn = database.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, nim);
            pstmt.setString(2, nama);
            pstmt.setString(3, jenisKelamin);
            pstmt.setString(4, statusMahasiswa);

            pstmt.executeUpdate();
        } catch(SQLException e) {
            throw new RuntimeException();
        }

        // update tabel
        mahasiswaTable.setModel(setTable());

        // bersihkan form
        clearForm();

        // feedback
        System.out.println("Insert Berhasil");
        JOptionPane.showMessageDialog(null, "Data berhasil ditambahkan");
    }

    public void updateData() {
        // ambil data dari form
        String nim = nimField.getText();
        String nama = namaField.getText();
        String jenisKelamin = jenisKelaminComboBox.getSelectedItem().toString();
        String statusMahasiswa = "";
        if(aktifRadioButton.isSelected()){
            statusMahasiswa = "Aktif";
        }else if(cutiRadioButton.isSelected()){
            statusMahasiswa = "Cuti";
        }else if(alumniRadioButton.isSelected()){
            statusMahasiswa = "Alumni";
        }

        // dapet sql
        String sql = "UPDATE mahasiswa SET nim = ?, nama = ?, jenis_kelamin = ?, statusMahasiswa = ? WHERE nim = ?;";
        try{
            Connection conn = database.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, nim);
            pstmt.setString(2, nama);
            pstmt.setString(3, jenisKelamin);
            pstmt.setString(4, statusMahasiswa);
            pstmt.setString(5, nim);

            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException();
        }

        // update tabel
        mahasiswaTable.setModel(setTable());

        // bersihkan form
        clearForm();

        // feedback
        System.out.println("Update Berhasil");
        JOptionPane.showMessageDialog(null, "Data berhasil diubah");

    }

    public void deleteData() {
        // ambil nim
        String currentNim = nimField.getText();

        // hapus make sql
        String sql = "DELETE FROM mahasiswa WHERE nim = ?";
        try{
            Connection conn = database.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);

            pstmt.setString(1, currentNim);
            pstmt.executeUpdate();
        } catch(SQLException e){
            throw new RuntimeException();
        }

        // update tabel
        mahasiswaTable.setModel(setTable());

        // bersihkan form
        clearForm();

        // feedback
        System.out.println("Delete berhasil");
        JOptionPane.showMessageDialog(null, "Data berhasil dihapus");

    }

    public void clearForm() {
        // kosongkan semua texfield dan combo box
        nimField.setText("");
        namaField.setText("");
        jenisKelaminComboBox.setSelectedItem("");
        statusGroup.clearSelection();

        // ubah button "Update" menjadi "Add"
        addUpdateButton.setText("Add");

        // sembunyikan button delete
        deleteButton.setVisible(false);

        // ubah selectedIndex menjadi -1 (tidak ada baris yang dipilih)
        selectedIndex = -1;

    }
}
