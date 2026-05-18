import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;

public class LandingGUI extends JFrame {

    private static final Color BG_TOP       = new Color(0x0A0818);
    private static final Color BG_MID       = new Color(0x1A1040);
    private static final Color BG_BOT       = new Color(0x2D1B69);
    private static final Color ACCENT       = new Color(0xA78BFA);
    private static final Color ACCENT_BRIGHT= new Color(0xC4B5FD);
    private static final Color CARD_FILL    = new Color(20, 14, 50, 220);
    private static final Color CARD_BORDER  = new Color(255, 255, 255, 30);
    private static final Color TEXT_PRIMARY = new Color(0xF8F7FF);
    private static final Color TEXT_DIM     = new Color(0xB8B3DC);
    private static final Color WHITE_FAINT  = new Color(255, 255, 255, 170);
    private static final Color FEATURE_BG   = new Color(255, 255, 255, 14);

    private static final int[][] STARS = generateStars(140);

    public LandingGUI() {
        setTitle("ARC. — Welcome");
        setSize(1080, 720);
        setMinimumSize(new Dimension(1080, 720));
        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        try { setIconImage(new ImageIcon("src/images/arc_logo.png").getImage()); } catch (Exception ignored) {}

        JPanel root = new JPanel(new GridBagLayout()) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int W = getWidth();
                int H = getHeight();

                GradientPaint bg = new GradientPaint(0, 0, BG_TOP, 0, H, BG_BOT);
                g2.setPaint(bg);
                g2.fillRect(0, 0, W, H);

                RadialGradientPaint glowA = new RadialGradientPaint(
                    W * 0.22f, H * 0.18f, H * 0.4f,
                    new float[]{0f, 1f},
                    new Color[]{new Color(167, 139, 250, 90), new Color(0,0,0,0)});
                g2.setPaint(glowA);
                g2.fillOval((int)(W * 0.04), (int)(H * -0.05), (int)(H * 0.8), (int)(H * 0.8));

                RadialGradientPaint glowB = new RadialGradientPaint(
                    W * 0.82f, H * 0.78f, H * 0.35f,
                    new float[]{0f, 1f},
                    new Color[]{new Color(80, 30, 180, 80), new Color(0,0,0,0)});
                g2.setPaint(glowB);
                g2.fillOval((int)(W * 0.55), (int)(H * 0.52), (int)(H * 0.6), (int)(H * 0.6));

                for (int[] star : STARS) {
                    int sx = (int)(star[0] / 1000.0 * W);
                    int sy = (int)(star[1] / 1000.0 * H);
                    int r  = star[2];
                    int a  = star[3];
                    g2.setColor(new Color(255, 255, 255, a));
                    g2.fillOval(sx - r, sy - r, r * 2, r * 2);
                }

                g2.dispose();
            }
        };
        root.setOpaque(true);

        JPanel card = buildLandingCard();
        root.add(card, new GridBagConstraints());
        setContentPane(root);
    }

    private JPanel buildLandingCard() {
        JPanel card = new JPanel(new GridBagLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CARD_FILL);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 32, 32);
                g2.setColor(CARD_BORDER);
                g2.setStroke(new BasicStroke(1.2f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 32, 32);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setPreferredSize(new Dimension(960, 640));

        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0; g.gridy = 0;
        g.gridwidth = 2;
        g.fill = GridBagConstraints.BOTH;
        g.insets = new Insets(24, 24, 24, 24);
        g.weightx = 1;
        g.weighty = 1;

        JPanel topRow = new JPanel(new GridBagLayout());
        topRow.setOpaque(false);
        GridBagConstraints t = new GridBagConstraints();
        t.gridx = 0; t.gridy = 0; t.anchor = GridBagConstraints.WEST; t.insets = new Insets(0, 0, 8, 0);

        JLabel title = new JLabel("ARC. Your planning companion");
        title.setFont(new Font("Segoe UI", Font.BOLD, 44));
        title.setForeground(TEXT_PRIMARY);
        topRow.add(title, t);

        t.gridy = 1; t.insets = new Insets(0, 0, 24, 0);
        JLabel subtitle = new JLabel("A smarter schedule, a calmer day, and more focus for what matters.");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        subtitle.setForeground(TEXT_DIM);
        topRow.add(subtitle, t);

        t.gridy = 2; t.gridwidth = 1; t.insets = new Insets(0, 0, 0, 0);
        topRow.add(buildFeatureRow(), t);

        card.add(topRow, g);

        JPanel contentRow = new JPanel(new GridLayout(1, 2, 24, 0));
        contentRow.setOpaque(false);
        contentRow.add(buildInfoPanel());
        contentRow.add(buildActionPanel());

        g.gridy = 1;
        g.gridwidth = 2;
        g.weighty = 1;
        card.add(contentRow, g);

        return card;
    }

    private JPanel buildFeatureRow() {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEADING, 16, 0));
        row.setOpaque(false);
        row.add(makeBadge("Fast setup", ACCENT_BRIGHT));
        row.add(makeBadge("Clear timeline", ACCENT));
        row.add(makeBadge("Focus mode", new Color(0x7C3AED)));
        return row;
    }

    private JPanel makeBadge(String text, Color color) {
        JPanel badge = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 4));
        badge.setOpaque(false);
        badge.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(color, 1, true),
            BorderFactory.createEmptyBorder(4, 10, 4, 10)));
        JLabel label = new JLabel(text);
        label.setForeground(color);
        label.setFont(new Font("Segoe UI", Font.BOLD, 12));
        badge.add(label);
        return badge;
    }

    private JPanel buildInfoPanel() {
        JPanel panel = new JPanel(new BorderLayout(20, 20));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JLabel sectionTitle = new JLabel("Why ARC?");
        sectionTitle.setFont(new Font("Segoe UI", Font.BOLD, 26));
        sectionTitle.setForeground(TEXT_PRIMARY);
        panel.add(sectionTitle, BorderLayout.NORTH);

        JPanel list = new JPanel(new GridLayout(4, 1, 12, 12));
        list.setOpaque(false);
        list.add(makeInfoItem("Smart calendar view", "See your week at a glance and jump to any date instantly."));
        list.add(makeInfoItem("Conflict alerts", "Avoid overlapping plans with intelligent schedule checks."));
        list.add(makeInfoItem("Daily focus help", "Get gentle guidance for your priorities each day."));
        list.add(makeInfoItem("Secure personalized data", "Your agenda stays tied to your account and login."));
        panel.add(list, BorderLayout.CENTER);

        return panel;
    }

    private JPanel makeInfoItem(String title, String text) {
        JPanel item = new JPanel(new BorderLayout(8, 2));
        item.setOpaque(false);
        JLabel bullet = new JLabel("•");
        bullet.setFont(new Font("Segoe UI", Font.BOLD, 18));
        bullet.setForeground(ACCENT);
        item.add(bullet, BorderLayout.WEST);

        JPanel textPanel = new JPanel(new GridLayout(2, 1, 2, 2));
        textPanel.setOpaque(false);
        JLabel heading = new JLabel(title);
        heading.setFont(new Font("Segoe UI", Font.BOLD, 14));
        heading.setForeground(TEXT_PRIMARY);
        JLabel body = new JLabel(text);
        body.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        body.setForeground(WHITE_FAINT);
        textPanel.add(heading);
        textPanel.add(body);

        item.add(textPanel, BorderLayout.CENTER);
        return item;
    }

    private JPanel buildActionPanel() {
        JPanel panel = new JPanel(new BorderLayout(18, 18));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JPanel hero = new JPanel(new GridLayout(2, 1, 12, 12));
        hero.setOpaque(false);
        JLabel heroTitle = new JLabel("Start your journey with ARC.");
        heroTitle.setFont(new Font("Segoe UI", Font.BOLD, 28));
        heroTitle.setForeground(TEXT_PRIMARY);
        JLabel heroText = new JLabel("Choose your entry point and begin organizing the way you want.");
        heroText.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        heroText.setForeground(TEXT_DIM);
        hero.add(heroTitle);
        hero.add(heroText);

        panel.add(hero, BorderLayout.NORTH);

        JPanel actions = new JPanel(new GridLayout(2, 1, 18, 18));
        actions.setOpaque(false);
        actions.add(makeActionButton("Login", ACCENT, e -> openLogin()));
        actions.add(makeActionButton("Register", new Color(0x7C3AED), e -> openRegister()));

        panel.add(actions, BorderLayout.CENTER);

        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        JLabel note = new JLabel("Already have an account? Tap Login.");
        note.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        note.setForeground(WHITE_FAINT);
        footer.add(note, BorderLayout.WEST);
        panel.add(footer, BorderLayout.SOUTH);

        return panel;
    }

    private JButton makeActionButton(String text, Color bg, ActionListener action) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 16));
        button.setForeground(Color.WHITE);
        button.setBackground(bg);
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setBorderPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(0, 56));
        button.addActionListener(action);
        button.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { button.setBackground(bg.darker()); }
            @Override public void mouseExited(MouseEvent e)  { button.setBackground(bg); }
        });
        return button;
    }

    private void openLogin() {
        new LoginGUI().setVisible(true);
        dispose();
    }

    private void openRegister() {
        new RegisterGUI().setVisible(true);
        dispose();
    }

    private static int[][] generateStars(int count) {
        int[][] stars = new int[count][4];
        for (int i = 0; i < count; i++) {
            stars[i][0] = (int)(Math.random() * 1000);
            stars[i][1] = (int)(Math.random() * 1000);
            stars[i][2] = 1 + (int)(Math.random() * 2);
            stars[i][3] = 120 + (int)(Math.random() * 135);
        }
        return stars;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LandingGUI().setVisible(true));
    }
}
