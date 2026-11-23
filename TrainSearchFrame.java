import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class TrainSearchFrame extends JFrame {
    JTextField txtFrom, txtTo;
    JButton btnSearch, btnBook, btnMyBookings, btnLogout;
    JTable table;
    DefaultTableModel model;

    public TrainSearchFrame() {
        setTitle("Railway System - Train Search");
        setSize(800, 480);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(5, 5));

        // top search panel
        JPanel topPanel = new JPanel(new FlowLayout());
        topPanel.add(new JLabel("From:"));
        txtFrom = new JTextField(10);
        topPanel.add(txtFrom);

        topPanel.add(new JLabel("To:"));
        txtTo = new JTextField(10);
        topPanel.add(txtTo);

        btnSearch = new JButton("Search");
        topPanel.add(btnSearch);
        add(topPanel, BorderLayout.NORTH);

        // table in center
        String[] cols = {"Schedule ID", "Train No", "Train Name", "From", "To", "Date", "Time", "Seats", "Fare"};
        model = new DefaultTableModel(cols, 0);
        table = new JTable(model);
        add(new JScrollPane(table), BorderLayout.CENTER);

        // bottom button panel
        JPanel bottomPanel = new JPanel(new FlowLayout());
        btnBook = new JButton("Book Ticket");
        btnMyBookings = new JButton("My Bookings");
        btnLogout = new JButton("Logout");
        bottomPanel.add(btnBook);
        bottomPanel.add(btnMyBookings);
        bottomPanel.add(btnLogout);
        add(bottomPanel, BorderLayout.SOUTH);

        // load all trains initially
        loadTrains("", "");

        // search button click
        btnSearch.addActionListener(e -> {
            loadTrains(txtFrom.getText().trim(), txtTo.getText().trim());
        });

        // book button click
        btnBook.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "Please select a train first");
                return;
            }

            int scheduleId = Integer.parseInt(model.getValueAt(row, 0).toString());
            String trainName = model.getValueAt(row, 2).toString();
            int seats = Integer.parseInt(model.getValueAt(row, 7).toString());
            double fare = Double.parseDouble(model.getValueAt(row, 8).toString());

            if (seats <= 0) {
                JOptionPane.showMessageDialog(this, "No seats available!");
                return;
            }

            new BookingFrame(scheduleId, trainName, fare, seats).setVisible(true);
            dispose();
        });

        btnMyBookings.addActionListener(e -> {
            new MyBookingsFrame().setVisible(true);
            dispose();
        });

        btnLogout.addActionListener(e -> {
            Session.logout();
            new LoginFrame().setVisible(true);
            dispose();
        });
    }

    // load trains using JOIN query
    private void loadTrains(String from, String to) {
        model.setRowCount(0);
        String sql = "SELECT s.schedule_id, t.train_number, t.train_name, src.station_name, dst.station_name, " +
                     "s.departure_date, s.departure_time, s.available_seats, s.fare " +
                     "FROM Schedule s " +
                     "JOIN Train t ON s.train_id = t.train_id " +
                     "JOIN Station src ON s.source_station_id = src.station_id " +
                     "JOIN Station dst ON s.destination_station_id = dst.station_id " +
                     "WHERE src.station_name ILIKE ? AND dst.station_name ILIKE ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "%" + from + "%");
            ps.setString(2, "%" + to + "%");

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt(1), rs.getString(2), rs.getString(3),
                    rs.getString(4), rs.getString(5), rs.getDate(6),
                    rs.getTime(7), rs.getInt(8), rs.getDouble(9)
                });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }
}
