import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.Random;

public class LandingGUI extends JFrame {

    // ── Palette: deep blue-purple night sky (matches Login/Register) ─────────
    private static final Color SKY_TOP      = new Color(0x05030F);   // near black
    private static final Color SKY_MID      = new Color(0x0D0B2E);   // deep indigo
    private static final Color SKY_HOR      = new Color(0x1A1254);   // horizon purple-blue
    private static final Color MTN_FAR      = new Color(0x1A1648);   // distant mountains
    private static final Color MTN_MID      = new Color(0x0F0D36);   // mid mountains
    private static final Color MTN_NEAR     = new Color(0x070518);   // near mountains (dark)
    private static final Color MTN_FLOOR    = new Color(0x040310);   // ground
    private static final Color MOON_FILL    = new Color(0xB8D4F0);   // cool moon
    private static final Color MOON_GLOW    = new Color(0x7AAED6);   // moon glow
    private static final Color ACCENT       = new Color(0xA78BFA);
    private static final Color ACCENT_BRIGHT= new Color(0xC4B5FD);
    private static final Color WHITE        = Color.WHITE;
    private static final Color WHITE_DIM    = new Color(255,255,255,190);
    private static final Color WHITE_FAINT  = new Color(255,255,255,110);
    private static final Color BTN_LOGIN    = new Color(255, 255, 255, 240);
    private static final Color BTN_REGISTER = new Color(255, 255, 255, 28);
    private static final Color BTN_BORDER   = new Color(255, 255, 255, 100);

    // Star + shooting star data
    private static final int[][]   STARS   = genStars(180);
    private static final double[][] SHOOTS = genShoots(6);

    public LandingGUI() {
        setTitle("ARC. — Welcome");
        setSize(1080, 720);
        setMinimumSize(new Dimension(1080, 720));
        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setUndecorated(false);

        try { setIconImage(new ImageIcon("src/images/arc_logo.png").getImage()); }
        catch (Exception ignored) {}

        JPanel root = new JPanel(null) {   // null layout so we layer everything
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                paintScene((Graphics2D) g, getWidth(), getHeight());
            }
        };
        root.setBackground(SKY_TOP);

        // ── Absolute-positioned overlay components ────────────────────────
        // They are added as transparent panels so mouse events work normally.
        // We use a resize listener to reposition them.

        // Top nav bar
        JPanel nav = buildNavBar();
        root.add(nav);

        // Centre content
        JPanel centre = buildCentreContent();
        root.add(centre);

        // Bottom tagline
        JLabel tagline = new JLabel("Powered by AVL Trees  ·  O(log n) everything", SwingConstants.CENTER);
        tagline.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        tagline.setForeground(new Color(255,255,255,60));
        root.add(tagline);

        // Position everything on resize
        root.addComponentListener(new ComponentAdapter() {
            @Override public void componentResized(ComponentEvent e) {
                int W = root.getWidth(), H = root.getHeight();
                nav.setBounds(0, 0, W, 60);
                centre.setBounds(0, (int)(H*0.22), W, (int)(H*0.62));
                tagline.setBounds(0, H-28, W, 22);
                root.repaint();
            }
        });

        setContentPane(root);
    }

    // ── Scene painter ─────────────────────────────────────────────────────────

    private void paintScene(Graphics2D g2, int W, int H) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        // ── Sky gradient (4-stop) ──────────────────────────────────────────
        float horizon = H * 0.52f;
        LinearGradientPaint sky = new LinearGradientPaint(
            0, 0, 0, horizon,
            new float[]{0f, 0.45f, 0.80f, 1f},
            new Color[]{SKY_TOP, SKY_MID, SKY_HOR, new Color(0x221660)});
        g2.setPaint(sky);
        g2.fillRect(0, 0, W, (int) horizon + 2);

        // ── Moon (top-right quadrant) ─────────────────────────────────────
        int moonR = (int)(Math.min(W, H) * 0.09);
        int moonX = (int)(W * 0.78);
        int moonY = (int)(H * 0.07);
        // glow
        RadialGradientPaint moonGlow = new RadialGradientPaint(
            moonX, moonY, moonR * 3.5f,
            new float[]{0f, 0.35f, 1f},
            new Color[]{new Color(120,180,230,70), new Color(70,120,200,25), new Color(0,0,0,0)});
        g2.setPaint(moonGlow);
        g2.fillOval(moonX - moonR*3, moonY - moonR*3, moonR*6, moonR*6);
        // moon body
        g2.setColor(MOON_FILL);
        g2.fillOval(moonX - moonR, moonY - moonR, moonR*2, moonR*2);
        // subtle inner gradient on moon
        RadialGradientPaint moonBody = new RadialGradientPaint(
            moonX - moonR*0.3f, moonY - moonR*0.3f, moonR,
            new float[]{0f, 0.6f, 1f},
            new Color[]{new Color(220,235,255,200), MOON_FILL, new Color(140,180,220)});
        g2.setPaint(moonBody);
        g2.fillOval(moonX - moonR, moonY - moonR, moonR*2, moonR*2);

        // ── Stars ─────────────────────────────────────────────────────────
        for (int[] s : STARS) {
            int sx = (int)(s[0]/1000.0*W);
            int sy = (int)(s[1]/1000.0*(horizon*0.9));
            g2.setColor(new Color(255,255,255,s[3]));
            g2.fillOval(sx-s[2], sy-s[2], s[2]*2, s[2]*2);
        }

        // ── Shooting stars ────────────────────────────────────────────────
        for (double[] sh : SHOOTS) {
            int x1 = (int)(sh[0]/1000.0*W), y1 = (int)(sh[1]/1000.0*horizon*0.7);
            int len = (int)(sh[2]/1000.0*W*0.12);
            int x2 = x1 + len, y2 = y1 + len/3;
            GradientPaint trail = new GradientPaint(x1,y1,new Color(255,255,255,200), x2,y2,new Color(255,255,255,0));
            g2.setPaint(trail);
            g2.setStroke(new BasicStroke((float)sh[3]));
            g2.drawLine(x1,y1,x2,y2);
        }

        // ── Far mountains (blue-purple silhouette) ────────────────────────
        int[] farX = mountainX(W, 0, W, 18, 0.08, 0.32, 42);
        int[] farY = mountainY(farX, H, horizon, 0.55, 0.78, 7);
        drawMountainLayer(g2, farX, farY, W, H,
            new Color[]{new Color(0x2A2278), new Color(0x1A1255)}, (int)horizon);

        // ── Mid mountains ─────────────────────────────────────────────────
        int[] midX = mountainX(W, 0, W, 14, 0.04, 0.42, 99);
        int[] midY = mountainY(midX, H, horizon, 0.35, 0.65, 13);
        drawMountainLayer(g2, midX, midY, W, H,
            new Color[]{new Color(0x13104A), new Color(0x0C0A2F)}, (int)horizon);

        // ── Near-left cluster (like monument buttes on left) ──────────────
        drawButtes(g2, W, H, (int)horizon, false);  // left side
        drawButtes(g2, W, H, (int)horizon, true);   // right side

        // ── Ground (flat dark floor) ──────────────────────────────────────
        GradientPaint ground = new GradientPaint(0, (int)horizon, new Color(0x0A0830), 0, H, MTN_FLOOR);
        g2.setPaint(ground);
        g2.fillRect(0, (int)horizon, W, H - (int)horizon);

        // subtle horizon glow
        RadialGradientPaint hGlow = new RadialGradientPaint(
            W/2f, (float)horizon,
            W*0.55f,
            new float[]{0f, 0.4f, 1f},
            new Color[]{new Color(80,60,200,55), new Color(40,30,120,20), new Color(0,0,0,0)});
        g2.setPaint(hGlow);
        g2.fillOval(-W/2, (int)(horizon-W/2f), W*2, W);
    }

    // ── Mountain geometry helpers ──────────────────────────────────────────────

    private int[] mountainX(int W, int startX, int endX, int peaks, double minGap, double maxGap, int seed) {
        Random r = new Random(seed);
        int[] xs = new int[peaks+2];
        xs[0] = startX;
        xs[peaks+1] = endX;
        for (int i = 1; i <= peaks; i++)
            xs[i] = startX + (int)((endX-startX) * (minGap + (maxGap-minGap) * i / peaks + r.nextDouble()*0.04 - 0.02));
        return xs;
    }

    private int[] mountainY(int[] xs, int H, double horizon, double minH, double maxH, int seed) {
        Random r = new Random(seed);
        int[] ys = new int[xs.length];
        ys[0] = (int)horizon; ys[xs.length-1] = (int)horizon;
        for (int i = 1; i < xs.length-1; i++) {
            double t = (double)i / (xs.length-1);
            double hFrac = minH + (maxH - minH) * Math.sin(Math.PI * t) + r.nextDouble()*0.08;
            ys[i] = (int)(horizon - horizon * hFrac);
        }
        return ys;
    }

    private void drawMountainLayer(Graphics2D g2, int[] xs, int[] ys, int W, int H,
                                    Color[] gradient, int horizon) {
        Polygon poly = new Polygon();
        poly.addPoint(0, H);
        for (int i = 0; i < xs.length; i++) poly.addPoint(xs[i], ys[i]);
        poly.addPoint(W, H);
        LinearGradientPaint lgp = new LinearGradientPaint(
            0, 0, 0, horizon,
            new float[]{0f, 1f}, gradient);
        g2.setPaint(lgp);
        g2.fill(poly);
    }

    /** Draw monument-valley style flat-top buttes */
    private void drawButtes(Graphics2D g2, int W, int H, int horizon, boolean rightSide) {
        Color darkBase = new Color(0x07051A);
        Color darkMid  = new Color(0x0A0828);

        if (!rightSide) {
            // Left cluster: 3 buttes of different heights
            drawButte(g2, W, H, horizon, (int)(W*0.04), (int)(W*0.19), 0.55, 0.10, darkBase, darkMid);
            drawButte(g2, W, H, horizon, (int)(W*0.10), (int)(W*0.13), 0.70, 0.10, darkBase, darkMid);
            drawButte(g2, W, H, horizon, (int)(W*0.0),  (int)(W*0.11), 0.42, 0.09, darkBase, darkMid);
        } else {
            // Right cluster
            drawButte(g2, W, H, horizon, (int)(W*0.76), (int)(W*0.18), 0.50, 0.10, darkBase, darkMid);
            drawButte(g2, W, H, horizon, (int)(W*0.87), (int)(W*0.13), 0.65, 0.09, darkBase, darkMid);
            drawButte(g2, W, H, horizon, (int)(W*0.92), (int)(W*0.10), 0.48, 0.08, darkBase, darkMid);
        }
    }

    private void drawButte(Graphics2D g2, int W, int H, int horizon,
                            int leftX, int width, double heightFrac, double topFrac,
                            Color base, Color mid) {
        int top  = (int)(horizon - horizon * heightFrac);
        int taper= (int)(width * topFrac);
        int bot  = H;

        Polygon butte = new Polygon();
        butte.addPoint(leftX,               bot);
        butte.addPoint(leftX,               top + taper);
        butte.addPoint(leftX + taper,       top);
        butte.addPoint(leftX + width-taper, top);
        butte.addPoint(leftX + width,       top + taper);
        butte.addPoint(leftX + width,       bot);

        LinearGradientPaint lgp = new LinearGradientPaint(
            0, top, 0, bot,
            new float[]{0f, 1f}, new Color[]{mid, base});
        g2.setPaint(lgp);
        g2.fill(butte);
    }

    // ── Nav bar ───────────────────────────────────────────────────────────────

    private JPanel buildNavBar() {
        JPanel bar = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                // fully transparent
            }
        };
        bar.setOpaque(false);
        bar.setBorder(BorderFactory.createEmptyBorder(0, 32, 0, 32));

        // Left: logo
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        left.setOpaque(false);
        JLabel logoIco = new JLabel();
        try {
            Image img = new ImageIcon("src/images/white_logo.png")
                .getImage().getScaledInstance(32, 28, Image.SCALE_SMOOTH);
            logoIco.setIcon(new ImageIcon(img));
        } catch (Exception e) {
            logoIco.setText("◈");
            logoIco.setFont(new Font("Segoe UI", Font.BOLD, 20));
            logoIco.setForeground(ACCENT_BRIGHT);
        }
        JLabel logoTxt = new JLabel("ARC.");
        logoTxt.setFont(new Font("Segoe UI", Font.BOLD, 20));
        logoTxt.setForeground(WHITE);
        left.add(logoIco); left.add(logoTxt);

        // Right: nav links
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 28, 0));
        right.setOpaque(false);


        bar.add(left,  BorderLayout.WEST);
        bar.add(right, BorderLayout.EAST);
        return bar;
    }

    // ── Centre content ────────────────────────────────────────────────────────

    private JPanel buildCentreContent() {
        JPanel panel = new JPanel(new GridBagLayout()) {
            @Override protected void paintComponent(Graphics g) { /* transparent */ }
        };
        panel.setOpaque(false);

        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = 0; gc.fill = GridBagConstraints.HORIZONTAL;
        gc.anchor = GridBagConstraints.CENTER;

        // ── "WELCOME" hero text ───────────────────────────────────────────
        gc.gridy = 0; gc.insets = new Insets(0, 0, 6, 0);
        JLabel welcome = new JLabel("WELCOME", SwingConstants.CENTER);
        welcome.setFont(new Font("Segoe UI", Font.BOLD, 88));
        welcome.setForeground(WHITE);
        // add text shadow via HTML workaround — keep it as JLabel with letter-spacing feel
        panel.add(welcome, gc);

        // ── Subtitle ──────────────────────────────────────────────────────
        gc.gridy = 1; gc.insets = new Insets(0, 0, 10, 0);
        JLabel sub = new JLabel(
            "<html><center>A smarter, calmer schedule — powered by AVL Tree technology.<br>"
            + "Plan faster, focus better, and never miss what matters.</center></html>",
            SwingConstants.CENTER);
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        sub.setForeground(WHITE_DIM);
        panel.add(sub, gc);

        // ── "Already have an account?" line ──────────────────────────────
        gc.gridy = 2; gc.insets = new Insets(0, 0, 22, 0);
        JLabel cta = new JLabel(
            "<html><center>Already have an account? <b>Log in below</b> — or create one to get started.</center></html>",
            SwingConstants.CENTER);
        cta.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        cta.setForeground(new Color(180,170,240,200));
        panel.add(cta, gc);

        // ── Button row ────────────────────────────────────────────────────
        gc.gridy = 3; gc.insets = new Insets(0, 0, 0, 0);
        panel.add(buildButtonRow(), gc);

        // ── Feature badges ────────────────────────────────────────────────
        gc.gridy = 4; gc.insets = new Insets(28, 0, 0, 0);
        panel.add(buildBadgeRow(), gc);

        return panel;
    }

    private JPanel buildButtonRow() {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.CENTER, 18, 0));
        row.setOpaque(false);

        JButton login    = makeHeroButton("Log In to My Account", true);
        JButton register = makeHeroButton("Create New Account", false);

        login.addActionListener(e    -> openLogin());
        register.addActionListener(e -> openRegister());

        row.add(login);
        row.add(register);
        return row;
    }

    private JButton makeHeroButton(String text, boolean primary) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (primary) {
                    g2.setColor(getModel().isRollover()
                        ? new Color(230,225,255) : BTN_LOGIN);
                } else {
                    g2.setColor(getModel().isRollover()
                        ? new Color(255,255,255,50) : BTN_REGISTER);
                }
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 50, 50);
                if (!primary) {
                    g2.setColor(BTN_BORDER);
                    g2.setStroke(new BasicStroke(1.4f));
                    g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 50, 50);
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btn.setForeground(primary ? new Color(0x1E0A4B) : WHITE);
        btn.setPreferredSize(new Dimension(240, 54));
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        // press feedback
        btn.addMouseListener(new MouseAdapter() {
            @Override public void mousePressed (MouseEvent e) { btn.setFont(new Font("Segoe UI",Font.BOLD,14)); }
            @Override public void mouseReleased(MouseEvent e) { btn.setFont(new Font("Segoe UI",Font.BOLD,15)); }
        });
        return btn;
    }

    private JPanel buildBadgeRow() {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        row.setOpaque(false);

        String[][] badges = {
            {"📅", "Smart Calendar"},
            {"🔒", "Secure Login"},
            {"📝", "Daily Notes"},
        };

        for (String[] b : badges) {
            JPanel badge = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 5)) {
                @Override protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(new Color(255,255,255,18));
                    g2.fillRoundRect(0,0,getWidth(),getHeight(),50,50);
                    g2.setColor(new Color(255,255,255,40));
                    g2.setStroke(new BasicStroke(1f));
                    g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,50,50);
                    g2.dispose();
                }
            };
            badge.setOpaque(false);
            JLabel ico = new JLabel(b[0]);
            ico.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            JLabel txt = new JLabel(b[1]);
            txt.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            txt.setForeground(WHITE_FAINT);
            badge.add(ico); badge.add(txt);
            row.add(badge);
        }
        return row;
    }

    // ── Star / shooting star generators ──────────────────────────────────────

    private static int[][] genStars(int n) {
        int[][] s = new int[n][4];
        Random r = new Random(12345);
        for (int i = 0; i < n; i++) {
            s[i][0] = r.nextInt(1000);
            s[i][1] = r.nextInt(1000);
            s[i][2] = r.nextInt(2) + 1;
            s[i][3] = 80 + r.nextInt(175);
        }
        return s;
    }

    private static double[][] genShoots(int n) {
        double[][] s = new double[n][4];
        Random r = new Random(54321);
        for (int i = 0; i < n; i++) {
            s[i][0] = 100 + r.nextInt(800);   // x start (0-1000)
            s[i][1] = 50  + r.nextInt(400);   // y start  (0-1000)
            s[i][2] = 200 + r.nextInt(600);   // length scale
            s[i][3] = 0.8 + r.nextDouble();   // stroke width
        }
        return s;
    }

    // ── Navigation ────────────────────────────────────────────────────────────

    private void openLogin() {
        new LoginGUI().setVisible(true);
        dispose();
    }

    private void openRegister() {
        new RegisterGUI().setVisible(true);
        dispose();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LandingGUI().setVisible(true));
    }
}