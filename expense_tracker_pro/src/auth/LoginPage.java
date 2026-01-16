package auth;

import database.DBConnection;
import dashboard.Dashboard;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class LoginPage extends JFrame {

    private JTextField usernameField;
    private JPasswordField passwordField;

    private final String USER_HINT = "Enter your username";
    private final String PASS_HINT = "Enter your password";

    public LoginPage() {
        setTitle("Expense Tracker • Secure Login");
        setSize(960, 540);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);

        setContentPane(new GradientBackground());
        setLayout(new GridBagLayout());

        add(new GlassCard());
    }

    /* ===================== MAIN GLASS CARD ===================== */

    class GlassCard extends JPanel {

        GlassCard() {
            setPreferredSize(new Dimension(840, 440));
            setOpaque(false);
            setLayout(new GridLayout(1, 2));
            add(new LeftLoginPanel());
            add(new RightInfoPanel());
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Shadow
            g2.setColor(new Color(0, 0, 0, 90));
            g2.fillRoundRect(6, 6, getWidth() - 12, getHeight() - 12, 45, 45);

            // Card
            g2.setColor(new Color(18, 18, 38, 235));
            g2.fillRoundRect(0, 0, getWidth() - 12, getHeight() - 12, 45, 45);
        }
    }

    /* ===================== LEFT LOGIN PANEL ===================== */

    class LeftLoginPanel extends JPanel {

        LeftLoginPanel() {
            setOpaque(false);
            setBorder(new EmptyBorder(45, 50, 50, 50));
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

            JLabel title = new JLabel("Expense Tracker");
            title.setFont(new Font("Segoe UI", Font.BOLD, 26));
            title.setForeground(Color.WHITE);
            title.setAlignmentX(CENTER_ALIGNMENT);

            JLabel subtitle = new JLabel("Manage your expenses smartly");
            subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            subtitle.setForeground(new Color(180, 180, 210));
            subtitle.setAlignmentX(CENTER_ALIGNMENT);

            add(title);
            add(Box.createVerticalStrut(5));
            add(subtitle);
            add(Box.createVerticalStrut(35));

            usernameField = createHintTextField(USER_HINT);
            passwordField = createHintPasswordField(PASS_HINT);

            add(usernameField);
            add(Box.createVerticalStrut(18));
            add(passwordField);
            add(Box.createVerticalStrut(30));

            JButton loginBtn = new GradientButton("LOGIN");
            loginBtn.setAlignmentX(CENTER_ALIGNMENT);
            loginBtn.addActionListener(e -> login());
            add(loginBtn);

            /* ---------- SIGN UP LINK ---------- */

            add(Box.createVerticalStrut(18));

            JPanel signupPanel = new JPanel();
            signupPanel.setOpaque(false);

            JLabel text = new JLabel("Don't have an account?");
            text.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            text.setForeground(new Color(180, 180, 210));

            JButton signupBtn = new JButton("Sign up");
            signupBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
            signupBtn.setForeground(new Color(0, 180, 255));
            signupBtn.setBorderPainted(false);
            signupBtn.setContentAreaFilled(false);
            signupBtn.setFocusPainted(false);
            signupBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            signupBtn.addActionListener(e -> {
                dispose();
                new SignupPage().setVisible(true); // CONNECTED
            });

            signupPanel.add(text);
            signupPanel.add(signupBtn);
            add(signupPanel);
        }
    }

    /* ===================== RIGHT INFO PANEL ===================== */

    class RightInfoPanel extends JPanel {

        RightInfoPanel() {
            setOpaque(false);
            setBorder(new EmptyBorder(80, 60, 60, 60));
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

            JLabel welcome = new JLabel("Welcome Back");
            welcome.setFont(new Font("Segoe UI", Font.BOLD, 38));
            welcome.setForeground(Color.WHITE);

            JLabel info = new JLabel(
                    "<html><br>•Track daily expenses<br>•Create monthly budgets<br>•Analyze your spending</html>"
            );
            info.setFont(new Font("Segoe UI", Font.PLAIN, 16));
            info.setForeground(new Color(190, 190, 220));

            add(welcome);
            add(info);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            GradientPaint gp = new GradientPaint(
                    0, 0, new Color(120, 0, 255),
                    getWidth(), getHeight(), new Color(0, 200, 255)
            );
            g2.setPaint(gp);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 45, 45);
        }
    }

    /* ===================== INPUT FIELDS WITH HINT ===================== */

    private JTextField createHintTextField(String hint) {
        JTextField tf = baseTextField();
        tf.setText(hint);
        tf.setForeground(Color.GRAY);

        tf.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                if (tf.getText().equals(hint)) {
                    tf.setText("");
                    tf.setForeground(Color.WHITE);
                }
            }

            public void focusLost(FocusEvent e) {
                if (tf.getText().isEmpty()) {
                    tf.setText(hint);
                    tf.setForeground(Color.GRAY);
                }
            }
        });
        return tf;
    }

    private JPasswordField createHintPasswordField(String hint) {
        JPasswordField pf = basePasswordField();
        pf.setText(hint);
        pf.setEchoChar((char) 0);
        pf.setForeground(Color.GRAY);

        pf.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                if (String.valueOf(pf.getPassword()).equals(hint)) {
                    pf.setText("");
                    pf.setEchoChar('●');
                    pf.setForeground(Color.WHITE);
                }
            }

            public void focusLost(FocusEvent e) {
                if (pf.getPassword().length == 0) {
                    pf.setText(hint);
                    pf.setEchoChar((char) 0);
                    pf.setForeground(Color.GRAY);
                }
            }
        });
        return pf;
    }

    private JTextField baseTextField() {
        JTextField tf = new JTextField();
        tf.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tf.setBackground(new Color(30, 30, 55));
        tf.setCaretColor(Color.WHITE);
        tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(90, 90, 140)),
                new EmptyBorder(10, 14, 10, 14)
        ));
        return tf;
    }

    private JPasswordField basePasswordField() {
        JPasswordField pf = new JPasswordField();
        pf.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        pf.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        pf.setBackground(new Color(30, 30, 55));
        pf.setCaretColor(Color.WHITE);
        pf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(90, 90, 140)),
                new EmptyBorder(10, 14, 10, 14)
        ));
        return pf;
    }

    /* ===================== PROFESSIONAL BUTTON ===================== */

    class GradientButton extends JButton {

        GradientButton(String text) {
            super(text);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setForeground(Color.WHITE);
            setFont(new Font("Segoe UI", Font.BOLD, 15));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setPreferredSize(new Dimension(220, 48));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            GradientPaint gp = new GradientPaint(
                    0, 0, new Color(255, 0, 150),
                    getWidth(), getHeight(), new Color(0, 180, 255)
            );

            g2.setPaint(gp);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 40, 40);

            super.paintComponent(g);
        }
    }

    /* ===================== LOGIN LOGIC ===================== */

    private void login() {
        String user = usernameField.getText().trim();
        String pass = new String(passwordField.getPassword());

        if (user.equals(USER_HINT) || pass.equals(PASS_HINT)) {
            JOptionPane.showMessageDialog(this, "Please enter username and password");
            return;
        }

        try (Connection con = DBConnection.connect()) {
            String sql = "SELECT id, name, password FROM users WHERE username=?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, user);
            ResultSet rs = ps.executeQuery();

            if (rs.next() && rs.getString("password").equals(hash(pass))) {
                dispose();
                new Dashboard(rs.getInt("id"), rs.getString("name")).setVisible(true);
            } else {
                JOptionPane.showMessageDialog(this, "Invalid username or password");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String hash(String p) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] b = md.digest(p.getBytes());
        StringBuilder sb = new StringBuilder();
        for (byte x : b) sb.append(String.format("%02x", x));
        return sb.toString();
    }

    /* ===================== BACKGROUND ===================== */

    class GradientBackground extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            GradientPaint gp = new GradientPaint(
                    0, 0, new Color(130, 0, 255),
                    getWidth(), getHeight(), new Color(0, 200, 255)
            );
            g2.setPaint(gp);
            g2.fillRect(0, 0, getWidth(), getHeight());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginPage().setVisible(true));
    }
}
