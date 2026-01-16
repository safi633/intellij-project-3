package auth;

import database.DBConnection;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;

public class SignupPage extends JFrame {

    private JTextField nameField, emailField, usernameField;
    private JPasswordField passwordField;

    private final String NAME_HINT = "Full Name";
    private final String EMAIL_HINT = "Email Address";
    private final String USER_HINT = "Username";
    private final String PASS_HINT = "Password";

    public SignupPage() {
        setTitle("Expense Tracker • Sign Up");
        setSize(950, 530);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);

        setContentPane(new GradientBackground());
        setLayout(new GridBagLayout());

        add(new GlassCard());
    }

    /* ===================== GRADIENT BACKGROUND ===================== */
    class GradientBackground extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            GradientPaint gp = new GradientPaint(
                    0, 0, new Color(130, 0, 255),
                    getWidth(), getHeight(), new Color(0, 200, 255)
            );
            g2.setPaint(gp);
            g2.fillRect(0, 0, getWidth(), getHeight());
            g2.dispose();
        }
    }

    /* ===================== MAIN GLASS CARD ===================== */
    class GlassCard extends JPanel {
        GlassCard() {
            setPreferredSize(new Dimension(840, 440));
            setOpaque(false);
            setLayout(new GridLayout(1, 2));
            add(new LeftSignupPanel());
            add(new RightInfoPanel());
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Shadow
            g2.setColor(new Color(0, 0, 0, 90));
            g2.fillRoundRect(6, 6, getWidth() - 12, getHeight() - 12, 45, 45);

            // Glass card
            g2.setColor(new Color(18, 18, 38, 235));
            g2.fillRoundRect(0, 0, getWidth() - 12, getHeight() - 12, 45, 45);
            g2.dispose();
        }
    }

    /* ===================== LEFT SIGNUP PANEL ===================== */
    class LeftSignupPanel extends JPanel {
        LeftSignupPanel() {
            setOpaque(false);
            setBorder(new EmptyBorder(45, 50, 40, 40));
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

            JLabel title = new JLabel("Create Account");
            title.setFont(new Font("Segoe UI", Font.BOLD, 26));
            title.setForeground(Color.WHITE);
            title.setAlignmentX(CENTER_ALIGNMENT);

            JLabel subtitle = new JLabel("Start managing your finances today");
            subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            subtitle.setForeground(new Color(180, 180, 210));
            subtitle.setAlignmentX(CENTER_ALIGNMENT);

            add(title);
            add(Box.createVerticalStrut(0));
            add(subtitle);
            add(Box.createVerticalStrut(5));

            nameField = createHintTextField(NAME_HINT);
            emailField = createHintTextField(EMAIL_HINT);
            usernameField = createHintTextField(USER_HINT);
            passwordField = createHintPasswordField(PASS_HINT);

            add(nameField);
            add(Box.createVerticalStrut(18));
            add(emailField);
            add(Box.createVerticalStrut(18));
            add(usernameField);
            add(Box.createVerticalStrut(18));
            add(passwordField);
            add(Box.createVerticalStrut(25));

            JButton signupBtn = new GradientButton("CREATE ACCOUNT");
            signupBtn.setAlignmentX(CENTER_ALIGNMENT);
            signupBtn.addActionListener(e -> signup());
            add(signupBtn);

            add(Box.createVerticalStrut(18));

            JPanel loginPanel = new JPanel();
            loginPanel.setOpaque(false);
            loginPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 8, 0));

            JLabel text = new JLabel("Already have an account?");
            text.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            text.setForeground(new Color(180, 180, 210));

            JButton loginBtn = new JButton("Login");
            loginBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
            loginBtn.setForeground(new Color(0, 180, 255));
            loginBtn.setBorderPainted(false);
            loginBtn.setContentAreaFilled(false);
            loginBtn.setFocusPainted(false);
            loginBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            loginBtn.addActionListener(e -> {
                dispose();
                new LoginPage().setVisible(true);
            });

            loginPanel.add(text);
            loginPanel.add(loginBtn);
            add(loginPanel);
        }
    }

    /* ===================== RIGHT INFO PANEL WITH BULLET POINTS ===================== */
    class RightInfoPanel extends JPanel {
        RightInfoPanel() {
            setOpaque(false);
            setBorder(new EmptyBorder(80, 60, 60, 60));
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

            JLabel title = new JLabel("Why Expense Tracker?");
            title.setFont(new Font("Segoe UI", Font.BOLD, 22));
            title.setForeground(Color.WHITE);
            title.setAlignmentX(LEFT_ALIGNMENT);

            add(title);
            add(Box.createVerticalStrut(30));

            add(createBulletLabel("• Track daily expenses effortlessly"));
            add(Box.createVerticalStrut(12));
            add(createBulletLabel("• Create and manage monthly budgets"));
            add(Box.createVerticalStrut(12));
            add(createBulletLabel("• Analyze spending patterns & insights"));
            add(Box.createVerticalStrut(12));
            add(createBulletLabel("• Secure, simple, and organized"));
        }

        private JLabel createBulletLabel(String text) {
            JLabel label = new JLabel(text);
            label.setFont(new Font("Segoe UI", Font.PLAIN, 16));
            label.setForeground(new Color(190, 190, 220));
            label.setAlignmentX(LEFT_ALIGNMENT);
            return label;
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            GradientPaint gp = new GradientPaint(
                    0, 0, new Color(120, 0, 255),
                    getWidth(), getHeight(), new Color(0, 200, 255)
            );
            g2.setPaint(gp);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 45, 45);
            g2.dispose();
        }
    }

    /* ===================== INPUT FIELDS WITH HINTS ===================== */
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

    /* ===================== GRADIENT BUTTON ===================== */
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
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            GradientPaint gp = new GradientPaint(
                    0, 0, new Color(255, 0, 150),
                    getWidth(), getHeight(), new Color(0, 180, 255)
            );

            g2.setPaint(gp);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 40, 40);

            super.paintComponent(g);
            g2.dispose();
        }
    }

    /* ===================== SIGNUP LOGIC ===================== */
    private void signup() {
        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (name.equals(NAME_HINT) || name.isEmpty() ||
                email.equals(EMAIL_HINT) || email.isEmpty() ||
                username.equals(USER_HINT) || username.isEmpty() ||
                password.equals(PASS_HINT) || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all fields");
            return;
        }

        if (!email.contains("@") || !email.contains(".")) {
            JOptionPane.showMessageDialog(this, "Please enter a valid email address");
            return;
        }

        try (Connection con = DBConnection.connect()) {
            String sql = "INSERT INTO users(name, email, username, password) VALUES(?,?,?,?)";
            PreparedStatement pst = con.prepareStatement(sql);
            pst.setString(1, name);
            pst.setString(2, email);
            pst.setString(3, username);
            pst.setString(4, hash(password));
            pst.executeUpdate();

            JOptionPane.showMessageDialog(this, "Account created successfully!");
            dispose();
            new LoginPage().setVisible(true);

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Signup failed. Username or email may already exist.");
        }
    }

    private String hash(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] bytes = md.digest(password.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}