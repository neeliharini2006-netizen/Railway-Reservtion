import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class LoginFrame extends JFrame {
    JTextField txtUser;
    JPasswordField txtPass;
    JButton btnLogin, btnSignUp;

    public LoginFrame() {
        setTitle("Railway System - Login");
        setSize(360, 220);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new GridLayout(4, 2, 10, 10));

        add(new JLabel("  Username:"));
        txtUser = new JTextField();
        add(txtUser);

        add(new JLabel("  Password:"));
        txtPass = new JPasswordField();
        add(txtPass);

        btnLogin = new JButton("Login");
        btnSignUp = new JButton("Sign Up");
        add(btnLogin);
        add(btnSignUp);

        // login button click
        btnLogin.addActionListener(e -> {
            String user = txtUser.getText();
            String pass = new String(txtPass.getPassword());

            if (user.isEmpty() || pass.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter username and password");
                return;
            }

            // check login in database
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement("SELECT * FROM Users WHERE username = ? AND password = ?")) {
                ps.setString(1, user);
                ps.setString(2, pass);

                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    Session.userId = rs.getInt("user_id");
                    Session.username = rs.getString("username");
                    Session.role = rs.getString("role");

                    JOptionPane.showMessageDialog(this, "Login successful!");
                    if ("Admin".equalsIgnoreCase(Session.role)) {
                        new AdminPanelFrame().setVisible(true);
                    } else {
                        new TrainSearchFrame().setVisible(true);
                    }
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(this, "Invalid username or password");
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
        });

        // go to sign up
        btnSignUp.addActionListener(e -> {
            new SignUpFrame().setVisible(true);
            dispose();
        });
    }
}
