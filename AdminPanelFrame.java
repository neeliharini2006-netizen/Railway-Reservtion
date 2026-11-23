import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

public class AdminPanelFrame extends JFrame {
    JTextField txtNo, txtName, txtSeats;
    JButton btnAdd, btnDelete, btnRefreshBookings, btnRefreshReport, btnLogout;
    JTable trainsTable, bookingsTable, reportTable;
    DefaultTableModel trainsModel, bookingsModel, reportModel;

    public AdminPanelFrame() {
        setTitle("Railway System - Admin Panel");
        setSize(850, 520);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(5, 5));

        // header
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnLogout = new JButton("Logout");
        topPanel.add(new JLabel("Admin Control Panel  "));
        topPanel.add(btnLogout);
        add(topPanel, BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();

        // TAB 1: Manage Trains
        JPanel trainPanel = new JPanel(new BorderLayout(5, 5));
        JPanel addPanel = new JPanel(new FlowLayout());
        addPanel.add(new JLabel("No:"));
        txtNo = new JTextField(6);
        addPanel.add(txtNo);
        addPanel.add(new JLabel("Name:"));
        txtName = new JTextField(10);
        addPanel.add(txtName);
        addPanel.add(new JLabel("Seats:"));
        txtSeats = new JTextField(5);
        addPanel.add(txtSeats);

        btnAdd = new JButton("Add Train");
        btnDelete = new JButton("Delete Selected");
        addPanel.add(btnAdd);
        addPanel.add(btnDelete);
        trainPanel.add(addPanel, BorderLayout.NORTH);

        trainsModel = new DefaultTableModel(new String[]{"ID", "Train No", "Train Name", "Total Seats"}, 0);
        trainsTable = new JTable(trainsModel);
        trainPanel.add(new JScrollPane(trainsTable), BorderLayout.CENTER);
        tabs.addTab("Manage Trains", trainPanel);

        // TAB 2: All Bookings
        JPanel bookingsPanel = new JPanel(new BorderLayout(5, 5));
        bookingsModel = new DefaultTableModel(new String[]{"ID", "PNR", "User", "Passenger", "Train", "From", "To", "Seats", "Fare", "Status"}, 0);
        bookingsTable = new JTable(bookingsModel);
        bookingsPanel.add(new JScrollPane(bookingsTable), BorderLayout.CENTER);

        btnRefreshBookings = new JButton("Refresh Bookings");
        bookingsPanel.add(btnRefreshBookings, BorderLayout.SOUTH);
        tabs.addTab("All Bookings", bookingsPanel);

        // TAB 3: Report (Bookings per train)
        JPanel reportPanel = new JPanel(new BorderLayout(5, 5));
        reportModel = new DefaultTableModel(new String[]{"Train No", "Train Name", "Bookings Count", "Seats Booked"}, 0);
        reportTable = new JTable(reportModel);
        reportPanel.add(new JScrollPane(reportTable), BorderLayout.CENTER);

        btnRefreshReport = new JButton("Refresh Report");
        reportPanel.add(btnRefreshReport, BorderLayout.SOUTH);
        tabs.addTab("Report (Group By)", reportPanel);

        add(tabs, BorderLayout.CENTER);

        // load data
        loadTrains();
        loadAllBookings();
        loadReport();

        // add train
        btnAdd.addActionListener(e -> {
            String no = txtNo.getText().trim();
            String name = txtName.getText().trim();
            String seats = txtSeats.getText().trim();

            if (no.isEmpty() || name.isEmpty() || seats.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill all fields");
                return;
            }

            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement("INSERT INTO Train (train_number, train_name, total_seats) VALUES (?, ?, ?)")) {
                ps.setString(1, no);
                ps.setString(2, name);
                ps.setInt(3, Integer.parseInt(seats));
                ps.executeUpdate();

                JOptionPane.showMessageDialog(this, "Train added successfully");
                txtNo.setText(""); txtName.setText(""); txtSeats.setText("");
                loadTrains();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error adding train: " + ex.getMessage());
            }
        });

        // delete train
        btnDelete.addActionListener(e -> {
            int row = trainsTable.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "Select a train to delete");
                return;
            }
            int trainId = Integer.parseInt(trainsModel.getValueAt(row, 0).toString());

            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement("DELETE FROM Train WHERE train_id = ?")) {
                ps.setInt(1, trainId);
                ps.executeUpdate();

                JOptionPane.showMessageDialog(this, "Train deleted successfully");
                loadTrains();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error deleting train: " + ex.getMessage());
            }
        });

        btnRefreshBookings.addActionListener(e -> loadAllBookings());
        btnRefreshReport.addActionListener(e -> loadReport());

        btnLogout.addActionListener(e -> {
            Session.logout();
            new LoginFrame().setVisible(true);
            dispose();
        });
    }

    private void loadTrains() {
        trainsModel.setRowCount(0);
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT train_id, train_number, train_name, total_seats FROM Train")) {
            while (rs.next()) {
                trainsModel.addRow(new Object[]{rs.getInt(1), rs.getString(2), rs.getString(3), rs.getInt(4)});
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    private void loadAllBookings() {
        bookingsModel.setRowCount(0);
        String sql = "SELECT booking_id, pnr_number, username, passenger_name, train_name, source_station, destination_station, seats_booked, total_fare, booking_status FROM booking_summary_view";
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                bookingsModel.addRow(new Object[]{
                    rs.getInt(1), rs.getString(2), rs.getString(3), rs.getString(4),
                    rs.getString(5), rs.getString(6), rs.getString(7), rs.getInt(8),
                    rs.getDouble(9), rs.getString(10)
                });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    // simple report using GROUP BY
    private void loadReport() {
        reportModel.setRowCount(0);
        String sql = "SELECT t.train_number, t.train_name, COUNT(b.booking_id) AS total_bookings, " +
                     "COALESCE(SUM(b.seats_booked), 0) AS total_seats " +
                     "FROM Train t " +
                     "LEFT JOIN Schedule s ON t.train_id = s.train_id " +
                     "LEFT JOIN Booking b ON s.schedule_id = b.schedule_id " +
                     "GROUP BY t.train_id, t.train_number, t.train_name";

        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                reportModel.addRow(new Object[]{rs.getString(1), rs.getString(2), rs.getInt(3), rs.getInt(4)});
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }
}
