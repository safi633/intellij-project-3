package modules;

import database.DBConnection;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.sql.*;

public class SettingsModule extends JPanel {

    private int userId;

    private JComboBox<String> themeComboBox;
    private JCheckBox notificationsCheckBox;
    private JComboBox<String> languageComboBox;
    private JButton saveButton;

    private final Color ROYAL_BLUE = new Color(65, 105, 225);
    private final Color LIGHT_BG = new Color(245, 245, 250);
    private final Font LABEL_FONT = new Font("Segoe UI", Font.BOLD, 14);
    private final Font FIELD_FONT = new Font("Segoe UI", Font.PLAIN, 14);
    private final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 20);
    private final Font BUTTON_FONT = new Font("Segoe UI", Font.BOLD, 14);

    public SettingsModule(int userId) {
        this.userId = userId;

        setLayout(new BorderLayout(15, 15));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        setBackground(LIGHT_BG);

        // Title label
        JLabel titleLabel = new JLabel("Application Settings", JLabel.CENTER);
        titleLabel.setFont(TITLE_FONT);
        titleLabel.setForeground(ROYAL_BLUE);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(titleLabel, BorderLayout.NORTH);

        // Form panel with titled border
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(ROYAL_BLUE, 2, true),
                "Preferences",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 16),
                ROYAL_BLUE
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(12, 15, 12, 15);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Theme label and combobox
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(createStyledLabel("Theme:"), gbc);
        gbc.gridx = 1;
        themeComboBox = new JComboBox<>(new String[]{"Light", "Dark"});
        styleComboBox(themeComboBox);
        formPanel.add(themeComboBox, gbc);

        // Notifications label and checkbox
        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(createStyledLabel("Enable Notifications:"), gbc);
        gbc.gridx = 1;
        notificationsCheckBox = new JCheckBox();
        notificationsCheckBox.setBackground(Color.WHITE);
        formPanel.add(notificationsCheckBox, gbc);

        // Language label and combobox
        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(createStyledLabel("Language:"), gbc);
        gbc.gridx = 1;
        languageComboBox = new JComboBox<>(new String[]{"English", "Spanish", "French"});
        styleComboBox(languageComboBox);
        formPanel.add(languageComboBox, gbc);

        // Save button
        gbc.gridx = 1; gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.EAST;
        saveButton = createStyledButton("Save Settings", new Color(34, 139, 34));
        formPanel.add(saveButton, gbc);

        add(formPanel, BorderLayout.CENTER);

        loadSettings();

        saveButton.addActionListener(e -> saveSettings());
    }

    private JLabel createStyledLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(LABEL_FONT);
        label.setForeground(ROYAL_BLUE);
        return label;
    }

    private void styleComboBox(JComboBox<String> comboBox) {
        comboBox.setFont(FIELD_FONT);
        comboBox.setForeground(ROYAL_BLUE);
        comboBox.setBackground(Color.WHITE);
        comboBox.setBorder(BorderFactory.createLineBorder(ROYAL_BLUE, 2, true));
        comboBox.setPreferredSize(new Dimension(180, 32));
        comboBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                                                          int index, boolean isSelected, boolean cellHasFocus) {
                Component comp = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (isSelected) {
                    comp.setBackground(ROYAL_BLUE);
                    comp.setForeground(Color.WHITE);
                } else {
                    comp.setBackground(Color.WHITE);
                    comp.setForeground(ROYAL_BLUE);
                }
                return comp;
            }
        });
    }

    private JButton createStyledButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFont(BUTTON_FONT);
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
        button.setOpaque(true);

        // Hover effect
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(bgColor.darker());
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(bgColor);
            }
        });
        return button;
    }

    private void loadSettings() {
        try (Connection con = DBConnection.connect()) {
            String sql = "SELECT theme, notifications_enabled, language FROM user_settings WHERE user_id = ?";
            PreparedStatement pst = con.prepareStatement(sql);
            pst.setInt(1, userId);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                themeComboBox.setSelectedItem(rs.getString("theme"));
                notificationsCheckBox.setSelected(rs.getBoolean("notifications_enabled"));
                languageComboBox.setSelectedItem(rs.getString("language"));
            } else {
                String insertSql = "INSERT INTO user_settings (user_id) VALUES (?)";
                PreparedStatement insertPst = con.prepareStatement(insertSql);
                insertPst.setInt(1, userId);
                insertPst.executeUpdate();

                themeComboBox.setSelectedItem("Light");
                notificationsCheckBox.setSelected(true);
                languageComboBox.setSelectedItem("English");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Failed to load settings: " + e.getMessage());
        }
    }

    private void saveSettings() {
        try (Connection con = DBConnection.connect()) {
            String sql = "UPDATE user_settings SET theme = ?, notifications_enabled = ?, language = ? WHERE user_id = ?";
            PreparedStatement pst = con.prepareStatement(sql);
            pst.setString(1, (String) themeComboBox.getSelectedItem());
            pst.setBoolean(2, notificationsCheckBox.isSelected());
            pst.setString(3, (String) languageComboBox.getSelectedItem());
            pst.setInt(4, userId);
            int rows = pst.executeUpdate();

            if (rows == 0) {
                String insertSql = "INSERT INTO user_settings (user_id, theme, notifications_enabled, language) VALUES (?, ?, ?, ?)";
                PreparedStatement insertPst = con.prepareStatement(insertSql);
                insertPst.setInt(1, userId);
                insertPst.setString(2, (String) themeComboBox.getSelectedItem());
                insertPst.setBoolean(3, notificationsCheckBox.isSelected());
                insertPst.setString(4, (String) languageComboBox.getSelectedItem());
                insertPst.executeUpdate();
            }

            JOptionPane.showMessageDialog(this, "Settings saved!");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Failed to save settings: " + e.getMessage());
        }
    }
}
