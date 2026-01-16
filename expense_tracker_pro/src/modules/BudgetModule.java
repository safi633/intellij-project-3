package modules;

import database.DBConnection;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.Vector;

public class BudgetModule extends JPanel {

    private int userId;
    private JTable budgetTable;
    private DefaultTableModel tableModel;
    private JTextField monthField, amountField;
    private JButton addUpdateButton, deleteButton;
    private int selectedBudgetId = -1;

    public BudgetModule(int userId) {
        this.userId = userId;
        setLayout(new BorderLayout(15, 15));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        setBackground(new Color(245, 245, 250));  // Light subtle background

        // Form Panel with colorful titled border
        JPanel formPanel = new JPanel();
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(65, 105, 225), 2, true),  // Royal Blue border
                "Add / Update Budget",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 16),
                new Color(65, 105, 225)
        ));
        formPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 15, 10, 15);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel monthLabel = new JLabel("Month (YYYY-MM):");
        JLabel amountLabel = new JLabel("Budget Amount:");

        monthLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        monthLabel.setForeground(new Color(65, 105, 225));
        amountLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        amountLabel.setForeground(new Color(65, 105, 225));

        monthField = createStyledTextField();
        amountField = createStyledTextField();

        addUpdateButton = new JButton("Add / Update");
        styleButton(addUpdateButton, new Color(65, 105, 225));

        deleteButton = new JButton("Delete");
        styleButton(deleteButton, new Color(220, 20, 60)); // Crimson red for delete

        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(monthLabel, gbc);

        gbc.gridx = 1;
        formPanel.add(monthField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        formPanel.add(amountLabel, gbc);

        gbc.gridx = 1;
        formPanel.add(amountField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        formPanel.add(addUpdateButton, gbc);

        gbc.gridx = 1;
        formPanel.add(deleteButton, gbc);

        // Table setup with colored header
        tableModel = new DefaultTableModel(new String[]{"ID", "Month", "Budget Amount", "Spent"}, 0) {
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        budgetTable = new JTable(tableModel);
        budgetTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        budgetTable.getTableHeader().setBackground(new Color(65, 105, 225));
        budgetTable.getTableHeader().setForeground(Color.WHITE);
        budgetTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        budgetTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        budgetTable.setRowHeight(25);
        budgetTable.setGridColor(new Color(200, 200, 200));

        JScrollPane tableScrollPane = new JScrollPane(budgetTable);
        tableScrollPane.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(65, 105, 225), 2, true),
                "Budgets",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 16),
                new Color(65, 105, 225)
        ));
        tableScrollPane.setBackground(Color.WHITE);

        // Add components
        add(formPanel, BorderLayout.NORTH);
        add(tableScrollPane, BorderLayout.CENTER);

        // Load data
        loadBudgets();

        // Button actions
        addUpdateButton.addActionListener(e -> addOrUpdateBudget());
        deleteButton.addActionListener(e -> deleteBudget());

        budgetTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                int row = budgetTable.getSelectedRow();
                if (row >= 0) {
                    selectedBudgetId = (int) tableModel.getValueAt(row, 0);
                    monthField.setText(tableModel.getValueAt(row, 1).toString());
                    amountField.setText(tableModel.getValueAt(row, 2).toString());
                }
            }
        });
    }

    private JTextField createStyledTextField() {
        JTextField field = new JTextField();
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setColumns(12);  // Limit width to make text field smaller
        field.setPreferredSize(new Dimension(150, 30)); // Width and height
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(65, 105, 225), 2, true),
                BorderFactory.createEmptyBorder(4, 8, 4, 8)  // Inner padding
        ));
        return field;
    }

    private void styleButton(JButton button, Color bgColor) {
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createEmptyBorder(6, 16, 6, 16));  // Smaller padding
        button.setOpaque(true);
    }

    private void loadBudgets() {
        try (Connection con = DBConnection.connect()) {
            String sql = "SELECT b.id, b.month, b.amount, " +
                    "(SELECT IFNULL(SUM(amount), 0) FROM expenses WHERE user_id = b.user_id AND DATE_FORMAT(date, '%Y-%m') = b.month) AS spent " +
                    "FROM budgets b WHERE user_id = ? ORDER BY b.month DESC";
            PreparedStatement pst = con.prepareStatement(sql);
            pst.setInt(1, userId);
            ResultSet rs = pst.executeQuery();

            tableModel.setRowCount(0);
            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                row.add(rs.getInt("id"));
                row.add(rs.getString("month"));
                row.add(rs.getDouble("amount"));
                row.add(rs.getDouble("spent"));
                tableModel.addRow(row);
            }

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to load budgets: " + e.getMessage());
        }
    }

    private void addOrUpdateBudget() {
        String month = monthField.getText().trim();
        String amountStr = amountField.getText().trim();

        if (month.isEmpty() || amountStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Month and budget amount are required.");
            return;
        }

        if (!month.matches("\\d{4}-\\d{2}")) {
            JOptionPane.showMessageDialog(this, "Month must be in YYYY-MM format.");
            return;
        }

        try {
            double amount = Double.parseDouble(amountStr);

            try (Connection con = DBConnection.connect()) {
                if (selectedBudgetId == -1) {
                    // Check if month exists
                    String checkSql = "SELECT id FROM budgets WHERE user_id = ? AND month = ?";
                    PreparedStatement checkPst = con.prepareStatement(checkSql);
                    checkPst.setInt(1, userId);
                    checkPst.setString(2, month);
                    ResultSet rs = checkPst.executeQuery();

                    if (rs.next()) {
                        int existingId = rs.getInt("id");
                        String updateSql = "UPDATE budgets SET amount = ? WHERE id = ?";
                        PreparedStatement updatePst = con.prepareStatement(updateSql);
                        updatePst.setDouble(1, amount);
                        updatePst.setInt(2, existingId);
                        updatePst.executeUpdate();
                        JOptionPane.showMessageDialog(this, "Budget updated for " + month);
                    } else {
                        String insertSql = "INSERT INTO budgets (user_id, month, amount) VALUES (?, ?, ?)";
                        PreparedStatement insertPst = con.prepareStatement(insertSql);
                        insertPst.setInt(1, userId);
                        insertPst.setString(2, month);
                        insertPst.setDouble(3, amount);
                        insertPst.executeUpdate();
                        JOptionPane.showMessageDialog(this, "Budget added for " + month);
                    }
                } else {
                    String sql = "UPDATE budgets SET month = ?, amount = ? WHERE id = ? AND user_id = ?";
                    PreparedStatement pst = con.prepareStatement(sql);
                    pst.setString(1, month);
                    pst.setDouble(2, amount);
                    pst.setInt(3, selectedBudgetId);
                    pst.setInt(4, userId);
                    pst.executeUpdate();
                    JOptionPane.showMessageDialog(this, "Budget updated!");
                }

                clearForm();
                loadBudgets();
                selectedBudgetId = -1;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Amount must be a valid number.");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error saving budget: " + e.getMessage());
        }
    }

    private void deleteBudget() {
        if (selectedBudgetId == -1) {
            JOptionPane.showMessageDialog(this, "Please select a budget to delete.");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this budget?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection con = DBConnection.connect()) {
                String sql = "DELETE FROM budgets WHERE id = ? AND user_id = ?";
                PreparedStatement pst = con.prepareStatement(sql);
                pst.setInt(1, selectedBudgetId);
                pst.setInt(2, userId);
                pst.executeUpdate();

                JOptionPane.showMessageDialog(this, "Budget deleted!");
                clearForm();
                loadBudgets();
                selectedBudgetId = -1;
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error deleting budget: " + e.getMessage());
            }
        }
    }

    private void clearForm() {
        monthField.setText("");
        amountField.setText("");
        selectedBudgetId = -1;
        budgetTable.clearSelection();
    }
    public void refresh() {
        loadBudgets();
        clearForm(); // optional if you want to reset form as well
    }

}
