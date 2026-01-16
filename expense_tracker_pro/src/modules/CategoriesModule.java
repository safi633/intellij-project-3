package modules;

import database.DBConnection;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.Vector;

public class CategoriesModule extends JPanel {

    private int userId;
    private JTable categoryTable;
    private DefaultTableModel tableModel;

    private JTextField categoryField;
    private JButton addButton, updateButton, deleteButton;

    private int selectedCategoryId = -1;

    public CategoriesModule(int userId) {
        this.userId = userId;
        setLayout(new BorderLayout(15, 15));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        setBackground(new Color(245, 245, 250)); // Light gray background

        // -------- Form Panel --------
        JPanel formPanel = new JPanel();
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(65, 105, 225), 2, true),
                "Manage Categories",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 16),
                new Color(65, 105, 225)
        ));

        formPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 15, 10, 15);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel categoryLabel = new JLabel("Category Name:");
        categoryLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        categoryLabel.setForeground(new Color(65, 105, 225));

        categoryField = createStyledTextField();

        addButton = createStyledButton("Add", new Color(65, 105, 225));         // Royal Blue
        updateButton = createStyledButton("Update", new Color(255, 140, 0));    // Orange
        deleteButton = createStyledButton("Delete", new Color(220, 20, 60));    // Crimson Red

        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(categoryLabel, gbc);

        gbc.gridx = 1;
        formPanel.add(categoryField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        formPanel.add(addButton, gbc);

        gbc.gridx = 1;
        formPanel.add(updateButton, gbc);

        gbc.gridx = 2;
        formPanel.add(deleteButton, gbc);

        // -------- Table Panel --------
        tableModel = new DefaultTableModel(new String[]{"ID", "Category Name"}, 0) {
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        categoryTable = new JTable(tableModel);
        categoryTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        categoryTable.getTableHeader().setBackground(new Color(65, 105, 225));
        categoryTable.getTableHeader().setForeground(Color.WHITE);
        categoryTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        categoryTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        categoryTable.setRowHeight(25);
        categoryTable.setGridColor(new Color(200, 200, 200));

        JScrollPane scrollPane = new JScrollPane(categoryTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(65, 105, 225), 2, true),
                "Your Categories",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 16),
                new Color(65, 105, 225)
        ));
        scrollPane.setBackground(Color.WHITE);

        // -------- Add Panels to Layout --------
        add(formPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        // -------- Load Data and Add Listeners --------
        loadCategories();

        addButton.addActionListener(e -> addCategory());
        updateButton.addActionListener(e -> updateCategory());
        deleteButton.addActionListener(e -> deleteCategory());

        categoryTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = categoryTable.getSelectedRow();
                if (row >= 0) {
                    selectedCategoryId = (int) tableModel.getValueAt(row, 0);
                    categoryField.setText(tableModel.getValueAt(row, 1).toString());
                }
            }
        });
    }

    // --------- Styled Text Field ---------
    private JTextField createStyledTextField() {
        JTextField field = new JTextField();
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setPreferredSize(new Dimension(180, 30));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(65, 105, 225), 2, true),
                BorderFactory.createEmptyBorder(4, 8, 4, 8)
        ));
        return field;
    }

    // --------- Styled Button ---------
    private JButton createStyledButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createEmptyBorder(6, 16, 6, 16));
        button.setOpaque(true);
        return button;
    }

    // --------- Database Methods ---------
    private void loadCategories() {
        try (Connection con = DBConnection.connect()) {
            String sql = "SELECT * FROM categories WHERE user_id = ? ORDER BY category_name ASC";
            PreparedStatement pst = con.prepareStatement(sql);
            pst.setInt(1, userId);
            ResultSet rs = pst.executeQuery();

            tableModel.setRowCount(0);
            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                row.add(rs.getInt("id"));
                row.add(rs.getString("category_name"));
                tableModel.addRow(row);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading categories: " + e.getMessage());
        }
    }

    private void addCategory() {
        String categoryName = categoryField.getText().trim();

        if (categoryName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Category name cannot be empty.");
            return;
        }

        try (Connection con = DBConnection.connect()) {
            String sql = "INSERT INTO categories (user_id, category_name) VALUES (?, ?)";
            PreparedStatement pst = con.prepareStatement(sql);
            pst.setInt(1, userId);
            pst.setString(2, categoryName);
            pst.executeUpdate();

            JOptionPane.showMessageDialog(this, "Category added!");
            categoryField.setText("");
            loadCategories();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error adding category: " + e.getMessage());
        }
    }

    private void updateCategory() {
        if (selectedCategoryId == -1) {
            JOptionPane.showMessageDialog(this, "Please select a category to update.");
            return;
        }

        String categoryName = categoryField.getText().trim();

        if (categoryName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Category name cannot be empty.");
            return;
        }

        try (Connection con = DBConnection.connect()) {
            String sql = "UPDATE categories SET category_name = ? WHERE id = ? AND user_id = ?";
            PreparedStatement pst = con.prepareStatement(sql);
            pst.setString(1, categoryName);
            pst.setInt(2, selectedCategoryId);
            pst.setInt(3, userId);
            pst.executeUpdate();

            JOptionPane.showMessageDialog(this, "Category updated!");
            categoryField.setText("");
            loadCategories();
            selectedCategoryId = -1;
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error updating category: " + e.getMessage());
        }
    }

    private void deleteCategory() {
        if (selectedCategoryId == -1) {
            JOptionPane.showMessageDialog(this, "Please select a category to delete.");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this category?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection con = DBConnection.connect()) {
                String sql = "DELETE FROM categories WHERE id = ? AND user_id = ?";
                PreparedStatement pst = con.prepareStatement(sql);
                pst.setInt(1, selectedCategoryId);
                pst.setInt(2, userId);
                pst.executeUpdate();

                JOptionPane.showMessageDialog(this, "Category deleted!");
                categoryField.setText("");
                loadCategories();
                selectedCategoryId = -1;
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error deleting category: " + e.getMessage());
            }
        }
    }
    public void refresh() {
        loadCategories();
        categoryField.setText("");
        selectedCategoryId = -1;
        categoryTable.clearSelection();
    }

}
