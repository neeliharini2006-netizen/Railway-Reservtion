import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Random;

public class BookingFrame extends JFrame {
    int scheduleId;
    String trainName;
    double fare;
    int availableSeats;

    JTextField txtName, txtAge, txtSeats;
    JButton btnConfirm, btnBack;

    public BookingFrame(int scheduleId, String trainName, double fare, int availableSeats) {
        this.scheduleId = scheduleId;
        this.trainName = trainName;
        this.fare = fare;
        this.availableSeats = availableSeats;

        setTitle("Book Ticket - " + trainName);
        setSize(400, 320);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new GridLayout(6, 2, 8, 8));

        add(new JLabel("  Train:"));
        add(new JLabel(trainName));

        add(new JLabel("  Fare per seat:"));
        add(new JLabel("INR " + fare + " (Seats: " + availableSeats + ")"));

        add(new JLabel("  Passenger Name:"));
        txtName = new JTextField();
        add(txtName);

        add(new JLabel("  Passenger Age:"));
        txtAge = new JTextField();
        add(txtAge);

        add(new JLabel("  Number of Seats:"));
        txtSeats = new JTextField("1");
        add(txtSeats);

        btnConfirm = new JButton("Confirm Booking");
        btnBack = new JButton("Back");
        add(btnConfirm);
        add(btnBack);

        // confirm booking inside JDBC transaction
        btnConfirm.addActionListener(e -> {
            String name = txtName.getText().trim();
            String ageStr = txtAge.getText().trim();
            String seatStr = txtSeats.getText().trim();

            if (name.isEmpty() || ageStr.isEmpty() || seatStr.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill all fields");
                return;
            }

            int age = Integer.parseInt(ageStr);
            int seatsToBook = Integer.parseInt(seatStr);

            if (seatsToBook > availableSeats) {
                JOptionPane.showMessageDialog(this, "Not enough seats available!");
                return;
            }

            String pnr = "PNR" + (1000 + new Random().nextInt(9000));
            int userId = Session.userId > 0 ? Session.userId : 2;

            Connection conn = null;
            try {
                conn = DBConnection.getConnection();
                // start transaction
                conn.setAutoCommit(false);

                // 1. insert booking
                String sql1 = "INSERT INTO Booking (pnr_number, user_id, schedule_id, passenger_name, passenger_age, passenger_gender, seats_booked, booking_status) VALUES (?, ?, ?, ?, ?, 'Male', ?, 'CONFIRMED')";
                try (PreparedStatement ps1 = conn.prepareStatement(sql1)) {
                    ps1.setString(1, pnr);
                    ps1.setInt(2, userId);
                    ps1.setInt(3, scheduleId);
                    ps1.setString(4, name);
                    ps1.setInt(5, age);
                    ps1.setInt(6, seatsToBook);
                    ps1.executeUpdate();
                }

                // 2. reduce available seats
                String sql2 = "UPDATE Schedule SET available_seats = available_seats - ? WHERE schedule_id = ?";
                try (PreparedStatement ps2 = conn.prepareStatement(sql2)) {
                    ps2.setInt(1, seatsToBook);
                    ps2.setInt(2, scheduleId);
                    ps2.executeUpdate();
                }

                // commit transaction
                conn.commit();

                JOptionPane.showMessageDialog(this, "Booking Successful!\nPNR: " + pnr + "\nTotal Fare: INR " + (seatsToBook * fare));
                new MyBookingsFrame().setVisible(true);
                dispose();

            } catch (Exception ex) {
                // rollback on error
                if (conn != null) {
                    try {
                        conn.rollback();
                    } catch (Exception rex) {
                        rex.printStackTrace();
                    }
                }
                JOptionPane.showMessageDialog(this, "Booking Failed: " + ex.getMessage());
            } finally {
                if (conn != null) {
                    try {
                        conn.setAutoCommit(true);
                        conn.close();
                    } catch (Exception cex) {
                        cex.printStackTrace();
                    }
                }
            }
        });

        // back button
        btnBack.addActionListener(e -> {
            new TrainSearchFrame().setVisible(true);
            dispose();
        });
    }

    public BookingFrame() {
        this(1, "Rajdhani Express", 2150.00, 100);
    }
}
