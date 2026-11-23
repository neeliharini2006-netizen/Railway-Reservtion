import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;

public class SignUpFrame extends JFrame {
    JTextField txtName, txtAge, txtPhone, txtEmail, txtUser;
    JPasswordField txtPass;
    JComboBox<String> cbGender;
    JButton btnRegister, btnBack;

    public SignUpFrame() {
        setTitle("Railway System - Sign Up");
        setSize(400, 420);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new GridLayout(9, 2, 8, 8));

        add(new JLabel("  Full Name:"));
        txtName = new JTextField();
        add(txtName);

        add(new JLabel("  Age:"));
        txtAge = new JTextField();
        add(txtAge);

        add(new JLabel("  Gender:"));
        cbGender = new JComboBox<>(new String[]{"Male", "Female", "Other"});
        add(cbGender);

        add(new JLabel("  Phone:"));
        txtPhone = new JTextField();
        add(txtPhone);

        add(new JLabel("  Email:"));
        txtEmail = new JTextField();
        add(txtEmail);

        add(new JLabel("  Username:"));
        txtUser = new JTextField();
        add(txtUser);

        add(new JLabel("  Password:"));
        txtPass = new JPasswordField();
        add(txtPass);

        btnRegister = new JButton("Register");
        btnBack = new JButton("Back to Login");
        add(btnRegister);
        add(btnBack);

        // register button click
        btnRegister.addActionListener(e -> {
            String name = txtName.getText();
            String ageStr = txtAge.getText();
            String gender = (String) cbGender.getSelectedItem();
            String phone = txtPhone.getText();
            String email = txtEmail.getText();
            String user = txtUser.getText();
            String pass = new String(txtPass.getPassword());

            // check empty fields
            if (name.isEmpty() || ageStr.isEmpty() || phone.isEmpty() || email.isEmpty() || user.isEmpty() || pass.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill all fields");
                return;
            }

            // insert into Users table
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(
                     "INSERT INTO Users (full_name, age, gender, phone, email, username, password, role) VALUES (?, ?, ?, ?, ?, ?, ?, 'Passenger')")) {
                ps.setString(1, name);
                ps.setInt(2, Integer.parseInt(ageStr));
                ps.setString(3, gender);
                ps.setString(4, phone);
                ps.setString(5, email);
                ps.setString(6, user);
                ps.setString(7, pass);

                ps.executeUpdate();
                JOptionPane.showMessageDialog(this, "Registration successful! Please login.");
                new LoginFrame().setVisible(true);
                dispose();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
        });

        // back button click
        btnBack.addActionListener(e -> {
            new LoginFrame().setVisible(true);
            dispose();
        });
    }
}
