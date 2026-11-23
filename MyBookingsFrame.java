import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class MyBookingsFrame extends JFrame {
    JTable table;
    DefaultTableModel model;
    JButton btnCancel, btnBack;

    public MyBookingsFrame() {
        setTitle("Railway System - My Bookings");
        setSize(750, 420);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(5, 5));

        // title
        JLabel lblTitle = new JLabel("  My Bookings History", JLabel.LEFT);
        lblTitle.setFont(new Font("Arial", Font.BOLD, 14));
        add(lblTitle, BorderLayout.NORTH);

        // table in center
        String[] cols = {"Booking ID", "PNR", "Passenger", "Train", "From", "To", "Date", "Seats", "Fare", "Status"};
        model = new DefaultTableModel(cols, 0);
        table = new JTable(model);
        add(new JScrollPane(table), BorderLayout.CENTER);

        // bottom panel
        JPanel bottomPanel = new JPanel(new FlowLayout());
        btnCancel = new JButton("Cancel Selected Ticket");
        btnBack = new JButton("Back to Search");
        bottomPanel.add(btnCancel);
        bottomPanel.add(btnBack);
        add(bottomPanel, BorderLayout.SOUTH);

        // load bookings from view
        loadBookings();

        // cancel ticket button
        btnCancel.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "Please select a booking to cancel");
                return;
            }

            int bookingId = Integer.parseInt(model.getValueAt(row, 0).toString());
            String status = model.getValueAt(row, 9).toString();

            if ("CANCELLED".equalsIgnoreCase(status)) {
                JOptionPane.showMessageDialog(this, "This ticket is already cancelled");
                return;
            }

            // insert into cancellation table (trigger restores seats)
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement("INSERT INTO Cancellation (booking_id) VALUES (?)")) {
                ps.setInt(1, bookingId);
                ps.executeUpdate();

                JOptionPane.showMessageDialog(this, "Ticket cancelled successfully!");
                loadBookings();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error cancelling: " + ex.getMessage());
            }
        });

        // back to search
        btnBack.addActionListener(e -> {
            new TrainSearchFrame().setVisible(true);
            dispose();
        });
    }

    // load bookings from booking_summary_view
    private void loadBookings() {
        model.setRowCount(0);
        int userId = Session.userId > 0 ? Session.userId : 2;

        String sql = "SELECT booking_id, pnr_number, passenger_name, train_name, source_station, " +
                     "destination_station, departure_date, seats_booked, total_fare, booking_status " +
                     "FROM booking_summary_view WHERE user_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt(1), rs.getString(2), rs.getString(3), rs.getString(4),
                    rs.getString(5), rs.getString(6), rs.getDate(7), rs.getInt(8),
                    rs.getDouble(9), rs.getString(10)
                });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error loading bookings: " + ex.getMessage());
        }
    }
}
