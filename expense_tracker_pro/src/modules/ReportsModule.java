package modules;

import database.DBConnection;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Vector;

public class ReportsModule extends JPanel {

    private int userId;
    private DefaultTableModel tableModel;
    private JTable summaryTable;

    private final Color ROYAL_BLUE = new Color(65, 105, 225);
    private final Color LIGHT_BG = new Color(245, 245, 250);
    private final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 20);
    private final Font TABLE_HEADER_FONT = new Font("Segoe UI", Font.BOLD, 14);
    private final Font TABLE_FONT = new Font("Segoe UI", Font.PLAIN, 14);

    public ReportsModule(int userId) {
        this.userId = userId;

        setLayout(new BorderLayout(15, 15));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        setBackground(LIGHT_BG);

        // Title Label
        JLabel titleLabel = new JLabel("Expense Report by Category", SwingConstants.CENTER);
        titleLabel.setFont(TITLE_FONT);
        titleLabel.setForeground(ROYAL_BLUE);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(titleLabel, BorderLayout.NORTH);

        // Table model and JTable
        tableModel = new DefaultTableModel(new String[]{"Category", "Total Amount"}, 0) {
            public boolean isCellEditable(int row, int column) { return false; }
        };

        summaryTable = new JTable(tableModel);
        summaryTable.setFont(TABLE_FONT);
        summaryTable.setRowHeight(28);
        summaryTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        summaryTable.setShowGrid(true);
        summaryTable.setGridColor(new Color(220, 220, 220));

        // Table header style
        JTableHeader header = summaryTable.getTableHeader();
        header.setBackground(ROYAL_BLUE);
        header.setForeground(Color.WHITE);
        header.setFont(TABLE_HEADER_FONT);
        header.setReorderingAllowed(false);

        // Alternate row coloring and selection color
        summaryTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
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

        // Scroll pane with titled border matching theme
        JScrollPane scrollPane = new JScrollPane(summaryTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(ROYAL_BLUE, 2, true),
                "Summary Table",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 16),
                ROYAL_BLUE
        ));
        scrollPane.setBackground(Color.WHITE);

        add(scrollPane, BorderLayout.CENTER);

        loadSummary();
    }

    private void loadSummary() {
        try (Connection con = DBConnection.connect()) {
            String sql = "SELECT category, SUM(amount) as total_amount FROM expenses WHERE user_id = ? GROUP BY category ORDER BY total_amount DESC";
            PreparedStatement pst = con.prepareStatement(sql);
            pst.setInt(1, userId);
            ResultSet rs = pst.executeQuery();

            tableModel.setRowCount(0);
            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                row.add(rs.getString("category"));
                row.add(String.format("%.2f", rs.getDouble("total_amount")));
                tableModel.addRow(row);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Failed to load report: " + e.getMessage());
        }
    }

    // ✅ Refresh method to reload report data
    public void refresh() {
        loadSummary();
    }
}
