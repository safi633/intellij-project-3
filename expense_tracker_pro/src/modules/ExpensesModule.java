package modules;

import database.DBConnection;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.time.LocalDate;
import java.util.Vector;

public class ExpensesModule extends JPanel {

    private int userId;
    private JTable expensesTable;
    private DefaultTableModel tableModel;

    private JTextField dateField, amountField, descriptionField;
    private JComboBox<String> categoryComboBox;

    private JButton addButton, updateButton, deleteButton;
    private int selectedExpenseId = -1;

    private final Color ROYAL_BLUE = new Color(65, 105, 225);
    private final Color LIGHT_BG = new Color(245, 245, 250);
    private final Font LABEL_FONT = new Font("Segoe UI", Font.BOLD, 14);
    private final Font FIELD_FONT = new Font("Segoe UI", Font.PLAIN, 14);
    private final Font TABLE_HEADER_FONT = new Font("Segoe UI", Font.BOLD, 14);
    private final Font TABLE_FONT = new Font("Segoe UI", Font.PLAIN, 13);

    public ExpensesModule(int userId) {
        this.userId = userId;
        setLayout(new BorderLayout(15, 15));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        setBackground(LIGHT_BG);

        // --- Form Panel with titled border ---
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(ROYAL_BLUE, 2, true),
                "Add / Update Expense",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 16),
                ROYAL_BLUE
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 15, 10, 15);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Labels
        JLabel dateLabel = createStyledLabel("Date (YYYY-MM-DD):");
        JLabel categoryLabel = createStyledLabel("Category:");
        JLabel amountLabel = createStyledLabel("Amount:");
        JLabel descriptionLabel = createStyledLabel("Description:");

        // Fields
        dateField = createStyledTextField(12);
        dateField.setText(LocalDate.now().toString());
        categoryComboBox = new JComboBox<>();
        styleComboBox(categoryComboBox);
        amountField = createStyledTextField(12);
        descriptionField = createStyledTextField(20);

        // Buttons
        addButton = createStyledButton("Add", ROYAL_BLUE);
        updateButton = createStyledButton("Update", ROYAL_BLUE);
        deleteButton = createStyledButton("Delete", new Color(220, 20, 60)); // Crimson red

        // Add to form panel
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(dateLabel, gbc);
        gbc.gridx = 1;
        formPanel.add(dateField, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(categoryLabel, gbc);
        gbc.gridx = 1;
        formPanel.add(categoryComboBox, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(amountLabel, gbc);
        gbc.gridx = 1;
        formPanel.add(amountField, gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        formPanel.add(descriptionLabel, gbc);
        gbc.gridx = 1;
        formPanel.add(descriptionField, gbc);

        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        btnPanel.setBackground(Color.WHITE);
        btnPanel.add(addButton);
        btnPanel.add(updateButton);
        btnPanel.add(deleteButton);
        formPanel.add(btnPanel, gbc);

        // --- Table setup ---
        tableModel = new DefaultTableModel(new String[]{"ID", "Date", "Category", "Amount", "Description"}, 0) {
            public boolean isCellEditable(int row, int column) { return false; }
        };
        expensesTable = new JTable(tableModel);
        expensesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        expensesTable.setFont(TABLE_FONT);
        expensesTable.setRowHeight(28);
        expensesTable.setGridColor(new Color(200, 200, 200));
        expensesTable.setShowGrid(true);

        // Table header styling
        JTableHeader header = expensesTable.getTableHeader();
        header.setBackground(ROYAL_BLUE);
        header.setForeground(Color.WHITE);
        header.setFont(TABLE_HEADER_FONT);
        header.setReorderingAllowed(false);

        // Alternate row coloring & selection
        expensesTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            private final Color evenColor = Color.WHITE;
            private final Color oddColor = new Color(235, 240, 250);

            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus,
                                                           int row, int column) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setForeground(Color.BLACK);
                if (isSelected) {
                    setBackground(ROYAL_BLUE);
                    setForeground(Color.WHITE);
                } else {
                    setBackground(row % 2 == 0 ? evenColor : oddColor);
                }
                return this;
            }
        });

        JScrollPane tableScrollPane = new JScrollPane(expensesTable);
        tableScrollPane.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(ROYAL_BLUE, 2, true),
                "Expenses",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 16),
                ROYAL_BLUE
        ));
        tableScrollPane.setBackground(Color.WHITE);

        // Add panels to main panel
        add(formPanel, BorderLayout.NORTH);
        add(tableScrollPane, BorderLayout.CENTER);

        // Load data
        loadCategories();
        loadExpenses();

        // Event listeners
        addButton.addActionListener(e -> addExpense());
        updateButton.addActionListener(e -> updateExpense());
        deleteButton.addActionListener(e -> deleteExpense());

        expensesTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                int row = expensesTable.getSelectedRow();
                if (row >= 0) {
                    selectedExpenseId = (int) tableModel.getValueAt(row, 0);
                    dateField.setText(tableModel.getValueAt(row, 1).toString());
                    categoryComboBox.setSelectedItem(tableModel.getValueAt(row, 2).toString());
                    amountField.setText(tableModel.getValueAt(row, 3).toString());
                    descriptionField.setText(tableModel.getValueAt(row, 4).toString());
                }
            }
        });
    }

    private JLabel createStyledLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(LABEL_FONT);
        label.setForeground(ROYAL_BLUE);
        return label;
    }

    private JTextField createStyledTextField(int columns) {
        JTextField field = new JTextField(columns);
        field.setFont(FIELD_FONT);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ROYAL_BLUE, 2, true),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
        field.setPreferredSize(new Dimension(180, 32));
        return field;
    }

    private void styleComboBox(JComboBox<String> comboBox) {
        comboBox.setFont(FIELD_FONT);
        comboBox.setForeground(ROYAL_BLUE);
        comboBox.setBackground(Color.WHITE);
        comboBox.setBorder(BorderFactory.createLineBorder(ROYAL_BLUE, 2, true));
        comboBox.setPreferredSize(new Dimension(180, 32));
        comboBox.setRenderer(new DefaultListCellRenderer() {
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
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
        button.setOpaque(true);

        // Hover effect
        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { button.setBackground(bgColor.darker()); }
            public void mouseExited(MouseEvent e) { button.setBackground(bgColor); }
        });
        return button;
    }

    private void loadCategories() {
        try (Connection con = DBConnection.connect()) {
            String sql = "SELECT category_name FROM categories WHERE user_id = ? ORDER BY category_name ASC";
            PreparedStatement pst = con.prepareStatement(sql);
            pst.setInt(1, userId);
            ResultSet rs = pst.executeQuery();

            categoryComboBox.removeAllItems();
            while (rs.next()) {
                categoryComboBox.addItem(rs.getString("category_name"));
            }
            categoryComboBox.setSelectedIndex(-1);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Failed to load categories: " + e.getMessage());
        }
    }

    private void loadExpenses() {
        try (Connection con = DBConnection.connect()) {
            String sql = "SELECT * FROM expenses WHERE user_id = ? ORDER BY date DESC";
            PreparedStatement pst = con.prepareStatement(sql);
            pst.setInt(1, userId);
            ResultSet rs = pst.executeQuery();

            tableModel.setRowCount(0);
            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                row.add(rs.getInt("id"));
                row.add(rs.getDate("date").toString());
                row.add(rs.getString("category"));
                row.add(rs.getDouble("amount"));
                row.add(rs.getString("description"));
                tableModel.addRow(row);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Failed to load expenses: " + e.getMessage());
        }
    }

    private void addExpense() {
        String date = dateField.getText().trim();
        String category = (String) categoryComboBox.getSelectedItem();
        String amountStr = amountField.getText().trim();
        String description = descriptionField.getText().trim();

        if (date.isEmpty() || category == null || category.isEmpty() || amountStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Date, category, and amount are required.");
            return;
        }

        if (!isValidDate(date)) {
            JOptionPane.showMessageDialog(this, "Invalid date format. Use YYYY-MM-DD.");
            return;
        }

        try {
            double amount = Double.parseDouble(amountStr);

            try (Connection con = DBConnection.connect()) {
                String sql = "INSERT INTO expenses (user_id, date, category, amount, description) VALUES (?, ?, ?, ?, ?)";
                PreparedStatement pst = con.prepareStatement(sql);
                pst.setInt(1, userId);
                pst.setDate(2, java.sql.Date.valueOf(date));
                pst.setString(3, category);
                pst.setDouble(4, amount);
                pst.setString(5, description);
                pst.executeUpdate();

                JOptionPane.showMessageDialog(this, "Expense added!");
                clearForm();
                loadExpenses();
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Amount must be a valid number.");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error adding expense: " + e.getMessage());
        }
    }

    private void updateExpense() {
        if (selectedExpenseId == -1) {
            JOptionPane.showMessageDialog(this, "Please select an expense to update.");
            return;
        }

        String date = dateField.getText().trim();
        String category = (String) categoryComboBox.getSelectedItem();
        String amountStr = amountField.getText().trim();
        String description = descriptionField.getText().trim();

        if (date.isEmpty() || category == null || category.isEmpty() || amountStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Date, category, and amount are required.");
            return;
        }

        if (!isValidDate(date)) {
            JOptionPane.showMessageDialog(this, "Invalid date format. Use YYYY-MM-DD.");
            return;
        }

        try {
            double amount = Double.parseDouble(amountStr);

            try (Connection con = DBConnection.connect()) {
                String sql = "UPDATE expenses SET date=?, category=?, amount=?, description=? WHERE id=? AND user_id=?";
                PreparedStatement pst = con.prepareStatement(sql);
                pst.setDate(1, java.sql.Date.valueOf(date));
                pst.setString(2, category);
                pst.setDouble(3, amount);
                pst.setString(4, description);
                pst.setInt(5, selectedExpenseId);
                pst.setInt(6, userId);
                pst.executeUpdate();

                JOptionPane.showMessageDialog(this, "Expense updated!");
                clearForm();
                loadExpenses();
                selectedExpenseId = -1;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Amount must be a valid number.");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error updating expense: " + e.getMessage());
        }
    }

    private void deleteExpense() {
        if (selectedExpenseId == -1) {
            JOptionPane.showMessageDialog(this, "Please select an expense to delete.");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure to delete this expense?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection con = DBConnection.connect()) {
                String sql = "DELETE FROM expenses WHERE id=? AND user_id=?";
                PreparedStatement pst = con.prepareStatement(sql);
                pst.setInt(1, selectedExpenseId);
                pst.setInt(2, userId);
                pst.executeUpdate();

                JOptionPane.showMessageDialog(this, "Expense deleted!");
                clearForm();
                loadExpenses();
                selectedExpenseId = -1;
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error deleting expense: " + e.getMessage());
            }
        }
    }

    private boolean isValidDate(String dateStr) {
        try {
            LocalDate.parse(dateStr);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private void clearForm() {
        dateField.setText(LocalDate.now().toString());
        categoryComboBox.setSelectedIndex(-1);
        amountField.setText("");
        descriptionField.setText("");
        selectedExpenseId = -1;
        expensesTable.clearSelection();
    }
    public void refresh() {
        loadCategories(); // in case new categories are added
        loadExpenses();
        clearForm(); // optional
    }

}
