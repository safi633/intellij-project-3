package modules;

import database.DBConnection;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;

public class ProfileModule extends JPanel {

    private int userId;

    private JTextField nameField, usernameField, emailField;
    private JPasswordField passwordField;

    private JButton updateButton;

    public ProfileModule(int userId) {
        this.userId = userId;

        setLayout(new BorderLayout(20, 20));
        setBackground(new Color(245, 245, 250)); // light lavender background
        setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        // Panel with titled border and custom color
        JPanel formPanel = new JPanel();
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(100, 149, 237), 2, true), // cornflower blue border, rounded
                "User Profile",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 20),
                new Color(72, 61, 139) // dark slate blue
        ));

        // Labels with consistent styling
        Font labelFont = new Font("Segoe UI", Font.BOLD, 14);
        Color labelColor = new Color(72, 61, 139); // dark slate blue

        JLabel nameLabel = new JLabel("Full Name:");
        nameLabel.setFont(labelFont);
        nameLabel.setForeground(labelColor);

        JLabel usernameLabel = new JLabel("Username:");
        usernameLabel.setFont(labelFont);
        usernameLabel.setForeground(labelColor);

        JLabel emailLabel = new JLabel("Email:");
        emailLabel.setFont(labelFont);
        emailLabel.setForeground(labelColor);

        JLabel passwordLabel = new JLabel("New Password:");
        passwordLabel.setFont(labelFont);
        passwordLabel.setForeground(labelColor);

        // Text fields with smaller width and border color
        nameField = createStyledTextField();
        usernameField = createStyledTextField();
        usernameField.setEditable(false);
        usernameField.setBackground(new Color(230, 230, 250)); // light lavender for readonly

        emailField = createStyledTextField();
        passwordField = createStyledPasswordField();

        // Update button with rounded corners and color
        updateButton = new JButton("Update Profile");
        updateButton.setBackground(new Color(65, 105, 225)); // royal blue
        updateButton.setForeground(Color.WHITE);
        updateButton.setFont(new Font("Segoe UI", Font.BOLD, 16));
        updateButton.setFocusPainted(false);
        updateButton.setBorder(BorderFactory.createEmptyBorder(10, 25, 10, 25));
        updateButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        updateButton.setOpaque(true);

        // Layout with GroupLayout for neat compact design
        GroupLayout layout = new GroupLayout(formPanel);
        formPanel.setLayout(layout);
        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);

        layout.setHorizontalGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                        .addComponent(nameLabel)
                        .addComponent(usernameLabel)
                        .addComponent(emailLabel)
                        .addComponent(passwordLabel))
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addComponent(nameField, 200, 200, 200)
                        .addComponent(usernameField, 200, 200, 200)
                        .addComponent(emailField, 200, 200, 200)
                        .addComponent(passwordField, 200, 200, 200)
                        .addComponent(updateButton, GroupLayout.Alignment.CENTER))
        );

        layout.setVerticalGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(nameLabel)
                        .addComponent(nameField))
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(usernameLabel)
                        .addComponent(usernameField))
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(emailLabel)
                        .addComponent(emailField))
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(passwordLabel)
                        .addComponent(passwordField))
                .addGap(20)
                .addComponent(updateButton)
        );

        add(formPanel, BorderLayout.CENTER);

        loadProfile();

        updateButton.addActionListener(e -> updateProfile());
    }

    private JTextField createStyledTextField() {
        JTextField field = new JTextField();
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(100, 149, 237), 2, true),
                BorderFactory.createEmptyBorder(5, 8, 5, 8)
        ));
        return field;
    }

    private JPasswordField createStyledPasswordField() {
        JPasswordField field = new JPasswordField();
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(100, 149, 237), 2, true),
                BorderFactory.createEmptyBorder(5, 8, 5, 8)
        ));
        return field;
    }

    private void loadProfile() {
        try (Connection con = DBConnection.connect()) {
            String sql = "SELECT name, username, email FROM users WHERE id = ?";
            PreparedStatement pst = con.prepareStatement(sql);
            pst.setInt(1, userId);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                nameField.setText(rs.getString("name"));
                usernameField.setText(rs.getString("username"));
                emailField.setText(rs.getString("email"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading profile: " + e.getMessage());
        }
    }

    private void updateProfile() {
        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Name cannot be empty.");
            return;
        }
        if (email.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Email cannot be empty.");
            return;
        }
        if (!email.matches("^[\\w-\\.]+@[\\w-]+\\.[a-z]{2,4}$")) {
            JOptionPane.showMessageDialog(this, "Please enter a valid email address.");
            return;
        }

        try (Connection con = DBConnection.connect()) {
            if (password.isEmpty()) {
                String sql = "UPDATE users SET name = ?, email = ? WHERE id = ?";
                PreparedStatement pst = con.prepareStatement(sql);
                pst.setString(1, name);
                pst.setString(2, email);
                pst.setInt(3, userId);
                pst.executeUpdate();
            } else {
                String sql = "UPDATE users SET name = ?, email = ?, password = ? WHERE id = ?";
                PreparedStatement pst = con.prepareStatement(sql);
                pst.setString(1, name);
                pst.setString(2, email);
                pst.setString(3, hashPassword(password)); // <-- hashed password here
                pst.setInt(4, userId);
                pst.executeUpdate();
            }
            JOptionPane.showMessageDialog(this, "Profile updated successfully!");
            passwordField.setText("");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error updating profile: " + e.getMessage());
        }
    }

    // SHA-256 hashing method (same as LoginPage and SignupPage)
    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashedBytes = md.digest(password.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hashedBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            JOptionPane.showMessageDialog(this, "Hashing error: " + e.getMessage());
            return null;
        }
    }
}
