package dashboard;

import modules.*;
import auth.LoginPage;
import database.DBConnection;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.Locale;

public class Dashboard extends JFrame {

    private int userId;
    private String userName;

    private JPanel contentPanel;
    private CardLayout cardLayout;

    private JButton btnDashboard, btnExpenses, btnCategories, btnBudget, btnReports, btnProfile, btnSettings, btnLogout;
    private JButton activeButton;

    // Module references
    private ExpensesModule expensesModule;
    private CategoriesModule categoriesModule;
    private BudgetModule budgetModule;
    private ReportsModule reportsModule;
    private ProfileModule profileModule;
    private SettingsModule settingsModule;

    // Welcome screen components
    private JLabel totalSpentLabel;
    private JLabel transactionsLabel;
    private JLabel avgTransactionLabel;
    private JLabel topCategoryLabel;
    private JTable recentTable;

    // Color Theme
    private final Color sidebarColor = new Color(31, 43, 62);
    private final Color activeColor = new Color(0, 184, 148);
    private final Color hoverColor = new Color(39, 56, 84);
    private final Color contentBgColor = new Color(240, 245, 250);
    private final Color textColor = Color.WHITE;

    public Dashboard(int userId, String userName) {
        this.userId = userId;
        this.userName = userName;

        setTitle("Expense Tracker");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1200, 850);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JPanel sidebar = createSidebar();
        add(sidebar, BorderLayout.WEST);

        JPanel header = createHeader();
        add(header, BorderLayout.NORTH);

        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBorder(new EmptyBorder(10, 20, 10, 20));
        contentPanel.setBackground(contentBgColor);
        add(contentPanel, BorderLayout.CENTER);

        // Welcome screen
        JPanel welcomePanel = new JPanel();
        welcomePanel.setLayout(new BoxLayout(welcomePanel, BoxLayout.Y_AXIS));
        welcomePanel.setBackground(contentBgColor);

        // Welcome header - with more padding/space in the middle (increased top and bottom struts)
        JLabel welcomeLabel = new JLabel(
                "<html><div style='text-align:center; font-size:22px;'>" +
                        "Welcome back, <span style='color:#00B894; font-weight:bold;'>" + userName + "!</span>" +
                        "</div></html>"
        );
        welcomeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 22));
        welcomeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        welcomePanel.add(Box.createVerticalStrut(80)); // Increased top padding
        welcomePanel.add(welcomeLabel);
        welcomePanel.add(Box.createVerticalStrut(60)); // Increased space below welcome text

        // Month summary
        LocalDate now = LocalDate.now();
        String monthName = now.getMonth().getDisplayName(TextStyle.FULL, Locale.getDefault());
        JLabel monthLabel = new JLabel("Summary for " + monthName + " " + now.getYear());
        monthLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        monthLabel.setForeground(new Color(45, 52, 54));
        monthLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        welcomePanel.add(monthLabel);
        welcomePanel.add(Box.createVerticalStrut(15));

        // Stats cards
        JPanel statsPanel = new JPanel(new GridLayout(1, 4, 20, 0));
        statsPanel.setOpaque(false);

        totalSpentLabel = createStatCard(statsPanel, "Spent<br>This Month", "$0.00", new Color(231, 76, 60));
        transactionsLabel = createStatCard(statsPanel, "Transactions", "0", new Color(52, 152, 219));
        avgTransactionLabel = createStatCard(statsPanel, "Avg.<br>Transaction", "$0.00", new Color(26, 188, 156));
        topCategoryLabel = createStatCard(statsPanel, "Top<br>Category", "None", new Color(155, 89, 182));

        JPanel statsWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        statsWrapper.setOpaque(false);
        statsWrapper.add(statsPanel);
        welcomePanel.add(statsWrapper);
        welcomePanel.add(Box.createVerticalStrut(25));

        // Recent expenses
        JLabel recentTitle = new JLabel("Recent Expenses");
        recentTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        recentTitle.setForeground(new Color(45, 52, 54));
        recentTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        welcomePanel.add(recentTitle);
        welcomePanel.add(Box.createVerticalStrut(8));

        DefaultTableModel tableModel = new DefaultTableModel(new String[]{"Date", "Description", "Category", "Amount"}, 0);
        recentTable = new JTable(tableModel);
        recentTable.setRowHeight(28);
        recentTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        recentTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        recentTable.getTableHeader().setBackground(sidebarColor);
        recentTable.getTableHeader().setForeground(Color.WHITE);
        recentTable.setGridColor(new Color(220, 220, 220));

        JScrollPane tableScroll = new JScrollPane(recentTable);
        tableScroll.setPreferredSize(new Dimension(900, 150));
        tableScroll.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1));

        JPanel tableWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER));
        tableWrapper.setOpaque(false);
        tableWrapper.add(tableScroll);
        welcomePanel.add(tableWrapper);
        welcomePanel.add(Box.createVerticalStrut(20));

        // Finance tip
        JLabel tipLabel = new JLabel(
                "<html><div style='text-align:center; font-size:13px; color:#636e72; max-width:750px;'>"
                        + "<b>Pro Tip:</b> Tracking daily expenses helps you identify small leaks in your budget. "
                        + "Even tiny savings add up to financial freedom over time! 💡"
                        + "</div></html>"
        );
        tipLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        welcomePanel.add(tipLabel);
        welcomePanel.add(Box.createVerticalStrut(15));

        welcomePanel.add(Box.createVerticalGlue());

        contentPanel.add(welcomePanel, "welcome");

        // Modules
        expensesModule = new ExpensesModule(userId);
        categoriesModule = new CategoriesModule(userId);
        budgetModule = new BudgetModule(userId);
        reportsModule = new ReportsModule(userId);
        profileModule = new ProfileModule(userId);
        settingsModule = new SettingsModule(userId);

        contentPanel.add(expensesModule, "expenses");
        contentPanel.add(categoriesModule, "categories");
        contentPanel.add(budgetModule, "budget");
        contentPanel.add(reportsModule, "reports");
        contentPanel.add(profileModule, "profile");
        contentPanel.add(settingsModule, "settings");

        cardLayout.show(contentPanel, "welcome");
        setActiveButton(btnDashboard);

        loadWelcomeData();
    }

    private JLabel createStatCard(JPanel container, String titleHtml, String initialValue, Color valueColor) {
        JPanel card = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0, 0, 0, 20));
                g2.fillRoundRect(5, 5, getWidth() - 5, getHeight() - 5, 18, 18);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 18, 18);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setPreferredSize(new Dimension(240, 130));
        card.setBorder(new EmptyBorder(15, 15, 15, 15));

        JLabel titleLabel = new JLabel("<html><div style='text-align:center;'>" + titleHtml + "</div></html>");
        titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        titleLabel.setForeground(Color.GRAY);

        JLabel valueLabel = new JLabel(initialValue);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        valueLabel.setForeground(valueColor);
        valueLabel.setHorizontalAlignment(SwingConstants.CENTER);

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);

        container.add(card);
        return valueLabel;
    }

    private void loadWelcomeData() {
        LocalDate now = LocalDate.now();
        double totalSpent = 0.0;
        int transactionCount = 0;
        String topCategory = "None";
        DefaultTableModel model = (DefaultTableModel) recentTable.getModel();
        model.setRowCount(0);

        try (Connection con = DBConnection.connect()) {
            String totalSql = "SELECT SUM(amount) AS total, COUNT(*) AS count FROM expenses WHERE user_id = ? AND YEAR(date) = ? AND MONTH(date) = ?";
            PreparedStatement pstTotal = con.prepareStatement(totalSql);
            pstTotal.setInt(1, userId);
            pstTotal.setInt(2, now.getYear());
            pstTotal.setInt(3, now.getMonthValue());
            ResultSet rsTotal = pstTotal.executeQuery();
            if (rsTotal.next()) {
                totalSpent = rsTotal.getDouble("total");
                transactionCount = rsTotal.getInt("count");
            }

            String topSql = "SELECT category, SUM(amount) AS sum_amt FROM expenses WHERE user_id = ? AND YEAR(date) = ? AND MONTH(date) = ? GROUP BY category ORDER BY sum_amt DESC LIMIT 1";
            PreparedStatement pstTop = con.prepareStatement(topSql);
            pstTop.setInt(1, userId);
            pstTop.setInt(2, now.getYear());
            pstTop.setInt(3, now.getMonthValue());
            ResultSet rsTop = pstTop.executeQuery();
            if (rsTop.next()) {
                topCategory = rsTop.getString("category");
            }

            String recentSql = "SELECT date, description, category, amount FROM expenses WHERE user_id = ? ORDER BY date DESC LIMIT 5";
            PreparedStatement pstRecent = con.prepareStatement(recentSql);
            pstRecent.setInt(1, userId);
            ResultSet rsRecent = pstRecent.executeQuery();

            SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy");
            while (rsRecent.next()) {
                String dateStr = rsRecent.getDate("date") != null ? dateFormat.format(rsRecent.getDate("date")) : "-";
                String desc = rsRecent.getString("description") != null ? rsRecent.getString("description") : "";
                String cat = rsRecent.getString("category") != null ? rsRecent.getString("category") : "Uncategorized";
                double amt = rsRecent.getDouble("amount");
                model.addRow(new Object[]{dateStr, desc, cat, "$" + String.format("%.2f", amt)});
            }

            if (model.getRowCount() == 0) {
                model.addRow(new Object[]{"-", "No expenses recorded yet", "-", "-"});
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        totalSpentLabel.setText("$" + String.format("%.2f", totalSpent));
        transactionsLabel.setText(String.valueOf(transactionCount));
        double avg = transactionCount > 0 ? totalSpent / transactionCount : 0.0;
        avgTransactionLabel.setText("$" + String.format("%.2f", avg));
        topCategoryLabel.setText(topCategory.isEmpty() ? "None" : topCategory);
    }

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setPreferredSize(new Dimension(240, getHeight()));
        sidebar.setBackground(sidebarColor);
        sidebar.setLayout(new GridLayout(0, 1, 0, 8));
        sidebar.setBorder(new EmptyBorder(20, 10, 20, 10));

        btnDashboard = createSidebarButton("Dashboard");
        btnExpenses = createSidebarButton("Expenses");
        btnCategories = createSidebarButton("Categories");
        btnBudget = createSidebarButton("Budget Goals");
        btnReports = createSidebarButton("Reports");
        btnProfile = createSidebarButton("Profile");
        btnSettings = createSidebarButton("Settings");
        btnLogout = createSidebarButton("Logout");

        btnDashboard.addActionListener(e -> {
            switchPanel("welcome", btnDashboard);
            loadWelcomeData();
            refreshAllModules();
        });

        btnExpenses.addActionListener(e -> switchPanel("expenses", btnExpenses));
        btnCategories.addActionListener(e -> switchPanel("categories", btnCategories));
        btnBudget.addActionListener(e -> switchPanel("budget", btnBudget));
        btnReports.addActionListener(e -> switchPanel("reports", btnReports));
        btnProfile.addActionListener(e -> switchPanel("profile", btnProfile));
        btnSettings.addActionListener(e -> switchPanel("settings", btnSettings));
        btnLogout.addActionListener(e -> logout());

        sidebar.add(btnDashboard);
        sidebar.add(btnExpenses);
        sidebar.add(btnCategories);
        sidebar.add(btnBudget);
        sidebar.add(btnReports);
        sidebar.add(btnProfile);
        sidebar.add(btnSettings);
        sidebar.add(Box.createVerticalGlue());
        sidebar.add(btnLogout);

        return sidebar;
    }

    private JButton createSidebarButton(String text) {
        JButton btn = new JButton(text);
        btn.setFocusPainted(false);
        btn.setForeground(textColor);
        btn.setBackground(sidebarColor);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        btn.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 20));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (!btn.equals(activeButton)) {
                    btn.setBackground(hoverColor);
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (!btn.equals(activeButton)) {
                    btn.setBackground(sidebarColor);
                }
            }
        });

        return btn;
    }

    private void switchPanel(String panelName, JButton btn) {
        cardLayout.show(contentPanel, panelName);
        setActiveButton(btn);
    }

    private void setActiveButton(JButton btn) {
        if (activeButton != null) {
            activeButton.setBackground(sidebarColor);
            activeButton.setFont(activeButton.getFont().deriveFont(Font.PLAIN));
        }
        activeButton = btn;
        btn.setBackground(activeColor);
        btn.setFont(btn.getFont().deriveFont(Font.BOLD));
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setPreferredSize(new Dimension(getWidth(), 60));
        header.setBackground(sidebarColor);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, hoverColor));

        JLabel title = new JLabel("  Expense Tracker");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(Color.WHITE);
        title.setBorder(new EmptyBorder(0, 10, 0, 0));

        JLabel dotsLabel = new JLabel("<html>••••</html>");
        dotsLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        dotsLabel.setForeground(Color.WHITE);
        dotsLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        dotsLabel.setToolTipText("About Expense Tracker");
        dotsLabel.setBorder(new EmptyBorder(0, 0, 0, 30));

        dotsLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                dotsLabel.setForeground(activeColor);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                dotsLabel.setForeground(Color.WHITE);
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                showAboutDialog();
            }
        });

        header.add(title, BorderLayout.WEST);
        header.add(dotsLabel, BorderLayout.EAST);

        return header;
    }

    private void showAboutDialog() {
        String message = "<html><div style='width: 450px; text-align: center; padding: 20px; font-family: Segoe UI;'>"
                + "<h2 style='color: #00B894;'>Why Track Your Expenses?</h2>"
                + "<p style='font-size: 14px;'>Tracking expenses is key to financial success. It helps you:</p>"
                + "<ul style='text-align: left; display: inline-block; font-size: 14px;'>"
                + "<li>Gain clear insight into your spending habits</li>"
                + "<li>Identify and reduce unnecessary expenses</li>"
                + "<li>Create realistic and effective budgets</li>"
                + "<li>Save more money and build wealth</li>"
                + "<li>Avoid debt and financial stress</li>"
                + "<li>Achieve your long-term financial goals</li>"
                + "</ul>"
                + "<p style='font-size: 14px;'>Expense Tracker makes managing your finances simple, secure, and insightful!</p>"
                + "<br>"
                + "<h3 style='color: #00B894;'>Developed By</h3>"
                + "<p style='font-size: 14px;'>"
                + "<b>C241087</b> - Safi Uddin<br>safi123@gmail.com<br><br>"
                + "<b>C241094</b> - Imtiaz Khan Ifti<br>imtiazforcoding@gmail.com<br><br>"
                + "<b>C241091</b> - Mohammad Akif<br>ak145@gmail.com"
                + "</p>"
                + "</div></html>";

        JOptionPane.showMessageDialog(this, message, "About Expense Tracker", JOptionPane.INFORMATION_MESSAGE);
    }

    private void refreshAllModules() {
        if (expensesModule != null) expensesModule.refresh();
        if (categoriesModule != null) categoriesModule.refresh();
        if (budgetModule != null) budgetModule.refresh();
        if (reportsModule != null) reportsModule.refresh();
    }

    private void logout() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to log out?", "Logout",
                JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            dispose();
            new LoginPage().setVisible(true);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Dashboard(1, "User").setVisible(true));
    }
}