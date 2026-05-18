import javax.swing.*;
import javax.swing.Timer;
import javax.swing.table.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.time.*;
import java.time.format.*;
import java.time.temporal.*;
import java.sql.*;
import java.util.*;
import java.util.List;

public class DashboardGUI extends JFrame {
    private User currentUser;
    private AVLTree<Event> eventTree;
    private JTable eventTable;
    private DefaultTableModel tableModel;
    private JLabel totalEventsLabel;
    private JLabel nextEventLabel;
    private JLabel monthYearLabel;
    private JLabel currentTimeLabel;
    private JLabel noteOfDayLabel;
    private JPanel calendarPanel;
    private LocalDate currentCalendarDate;
    private java.util.Map<LocalDate, List<Event>> eventsByDate;

    // Color palette
    private static final Color BG_TOP        = new Color(0x0A0818);
    private static final Color BG_MID        = new Color(0x1A1040);
    private static final Color BG_BOT        = new Color(0x2D1B69);
    private static final Color GLASS_FILL    = new Color(20, 14, 50, 220);
    private static final Color GLASS_BORDER  = new Color(255, 255, 255, 45);
    private static final Color SIDEBAR_BG    = new Color(0x4B3FA0);
    private static final Color SIDEBAR_ACTIVE = new Color(0x6C5CE7);
    private static final Color SIDEBAR_TEXT  = new Color(0xE8E4FF);
    private static final Color ACCENT        = new Color(0xA78BFA);
    private static final Color CONTENT_BG    = new Color(0x140C28);
    private static final Color CARD_BG       = new Color(20, 14, 50, 220);
    private static final Color CARD_BORDER   = new Color(255, 255, 255, 30);
    private static final Color TEXT_PRIMARY  = new Color(0xF8F7FF);
    private static final Color TEXT_SECONDARY= new Color(0xB8B3DC);
    private static final Color TEXT_DIM      = new Color(255, 255, 255, 170);
    private static final Color DANGER        = new Color(0xEF4444);
    private static final Color SUCCESS       = new Color(0x10B981);
    private static final Color CALENDAR_EVENT = new Color(0xA78BFA);
    private static final Color CALENDAR_TODAY = new Color(0xFBCFE8);

    private static final Font FONT_TITLE   = new Font("Segoe UI", Font.BOLD, 22);
    private static final Font FONT_HEADING = new Font("Segoe UI", Font.BOLD, 15);
    private static final Font FONT_BODY    = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font FONT_SMALL   = new Font("Segoe UI", Font.PLAIN, 12);
    private static final Font FONT_STAT    = new Font("Segoe UI", Font.BOLD, 28);
    private static final Font FONT_STAT_LBL= new Font("Segoe UI", Font.PLAIN, 12);

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter MONTH_YEAR_FORMAT = DateTimeFormatter.ofPattern("MMMM yyyy");

    private static final String[] DAILY_NOTES = {
        "A soft violet reminder: plan your evening goals before the day ends.",
        "A short review of today can unlock a stronger tomorrow.",
        "Your next hour is the best place to start something meaningful.",
        "Use the quiet of the night to map out your next bright idea.",
        "Tall tasks are easier when you take them one moonlit step at a time.",
        "A small win now creates space for a graceful tomorrow.",
        "Refresh your schedule: swap one task for a calm and creative pause."
    };

    private JPanel activeSidebarBtn = null;

    public DashboardGUI(User user) {
        this.currentUser = user;
        this.eventTree = new AVLTree<>();
        this.currentCalendarDate = LocalDate.now();
        this.eventsByDate = new HashMap<>();

        setTitle("ARC. - Dashboard");
        setSize(1200, 800);
        setMinimumSize(new Dimension(1000, 700));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setBackground(CONTENT_BG);

        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
        catch (Exception ignored) {}

        loadEventsFromDatabase();
        buildUI();
        startClock();
        refreshEventTable();
        updateStats();
        refreshCalendar();
    }

    private void buildUI() {
        setLayout(new BorderLayout());
        add(buildSidebar(), BorderLayout.WEST);
        add(buildMainContent(), BorderLayout.CENTER);
    }

    // ── Sidebar ──────────────────────────────────────────────────────────────

    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(SIDEBAR_BG);
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setPreferredSize(new Dimension(210, 0));
        sidebar.setOpaque(false);

        // 1. BRAND (TOP)
        JPanel brand = new JPanel(new FlowLayout(FlowLayout.LEFT, 18, 18));
        brand.setOpaque(false);

        JLabel brandIcon = new JLabel();
        try {
            ImageIcon logoIcon = new ImageIcon("src/images/white_logo.png");
            Image scaledLogo = logoIcon.getImage().getScaledInstance(40, 35, Image.SCALE_SMOOTH);
            brandIcon.setIcon(new ImageIcon(scaledLogo));
        } catch (Exception e) {
            brandIcon.setText("◈");
            brandIcon.setFont(new Font("Segoe UI", Font.BOLD, 22));
            brandIcon.setForeground(new Color(0xC4B5FD));
        }

        JLabel brandName = new JLabel("ARC.");
        brandName.setFont(new Font("Segoe UI", Font.BOLD, 15));
        brandName.setForeground(Color.WHITE);
        brand.add(brandIcon);
        brand.add(brandName);
        sidebar.add(brand);
        sidebar.add(Box.createVerticalStrut(20));

        // 2. NAVIGATION BUTTONS
        String[][] navItems = {
            {"⊞", "Dashboard"},
            {"◷", "All Events"},
            {"📊", "Statistics"},
        };
        
        for (String[] item : navItems) {
            JPanel btn = makeSidebarButton(item[0], item[1]);
            sidebar.add(btn);
            sidebar.add(Box.createVerticalStrut(4));
        }

        sidebar.add(Box.createVerticalGlue());

        // 3. USER PROFILE
        JPanel userPanel = buildUserProfilePanel();
        sidebar.add(userPanel);
        sidebar.add(Box.createVerticalStrut(10));

        // 4. LOGOUT BUTTON (BOTTOM)
        JPanel logoutPanel = buildLogoutPanel();
        sidebar.add(logoutPanel);
        sidebar.add(Box.createVerticalStrut(20));

        return sidebar;
    }

    private JPanel buildUserProfilePanel() {
        JPanel userPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 14));
        userPanel.setOpaque(false);
        userPanel.setMaximumSize(new Dimension(210, 70));
        
        JLabel avatar = new JLabel(getInitials(currentUser.getUsername()));
        avatar.setFont(new Font("Segoe UI", Font.BOLD, 13));
        avatar.setForeground(SIDEBAR_BG);
        avatar.setBackground(new Color(0xC4B5FD));
        avatar.setOpaque(true);
        avatar.setPreferredSize(new Dimension(36, 36));
        avatar.setHorizontalAlignment(SwingConstants.CENTER);
        
        JPanel avatarCircle = new JPanel(new BorderLayout());
        avatarCircle.setOpaque(false);
        avatarCircle.setPreferredSize(new Dimension(36, 36));
        avatarCircle.add(avatar);
        
        JLabel userLbl = new JLabel("<html><b style='color:white'>" + currentUser.getUsername() + "</b>"
                + "<br><span style='color:#C4B5FD;font-size:10px'>" + currentUser.getEmail() + "</span></html>");
        
        userPanel.add(avatarCircle);
        userPanel.add(userLbl);
        
        JButton updateBtn = new JButton("✎");
        updateBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        updateBtn.setForeground(Color.WHITE);
        updateBtn.setBackground(new Color(0x6C5CE7));
        updateBtn.setBorderPainted(false);
        updateBtn.setFocusPainted(false);
        updateBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        updateBtn.setPreferredSize(new Dimension(28, 28));
        updateBtn.addActionListener(e -> showUpdateProfileDialog());
        userPanel.add(updateBtn);
        
        return userPanel;
    }
    
    private JPanel buildLogoutPanel() {
        JPanel logoutPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 18, 10));
        logoutPanel.setOpaque(false);
        logoutPanel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        logoutPanel.setMaximumSize(new Dimension(210, 44));
        
        JLabel logoutIcon = new JLabel("🚪");
        logoutIcon.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        logoutIcon.setForeground(SIDEBAR_TEXT);
        
        JLabel logoutText = new JLabel("Logout");
        logoutText.setFont(FONT_BODY);
        logoutText.setForeground(SIDEBAR_TEXT);
        
        logoutPanel.add(logoutIcon);
        logoutPanel.add(logoutText);
        
        logoutPanel.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                logoutPanel.setBackground(new Color(0x5B4FBB));
                logoutPanel.setOpaque(true);
                logoutPanel.repaint();
            }
            @Override public void mouseExited(MouseEvent e) {
                logoutPanel.setOpaque(false);
                logoutPanel.repaint();
            }
            @Override public void mouseClicked(MouseEvent e) {
                logout();
            }
        });
        
        return logoutPanel;
    }

    private JPanel makeSidebarButton(String icon, String label) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 18, 10));
        panel.setOpaque(false);
        panel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        panel.setMaximumSize(new Dimension(210, 44));

        JLabel iconLbl = new JLabel(icon);
        iconLbl.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        iconLbl.setForeground(SIDEBAR_TEXT);

        JLabel textLbl = new JLabel(label);
        textLbl.setFont(FONT_BODY);
        textLbl.setForeground(SIDEBAR_TEXT);

        panel.add(iconLbl);
        panel.add(textLbl);

        panel.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                if (panel != activeSidebarBtn) panel.setBackground(new Color(0x5B4FBB));
                panel.setOpaque(true);
                panel.repaint();
            }
            @Override public void mouseExited(MouseEvent e) {
                if (panel != activeSidebarBtn) panel.setOpaque(false);
                panel.repaint();
            }
            @Override public void mouseClicked(MouseEvent e) {
                setActiveSidebarBtn(panel);
                handleSidebarAction(label);
            }
        });
        return panel;
    }

    private void setActiveSidebarBtn(JPanel btn) {
        if (activeSidebarBtn != null) {
            activeSidebarBtn.setOpaque(false);
            activeSidebarBtn.repaint();
        }
        activeSidebarBtn = btn;
        btn.setBackground(SIDEBAR_ACTIVE);
        btn.setOpaque(true);
        btn.repaint();
    }

    private void handleSidebarAction(String label) {
        switch (label) {
            case "Dashboard"  -> refreshAll();
            case "All Events" -> refreshEventTable();
            case "Statistics" -> showFullStats();
        }
    }

    // ── Update Profile Feature ──
    private void showUpdateProfileDialog() {
        JTextField usernameField = new JTextField(currentUser.getUsername(), 15);
        JTextField emailField = new JTextField(currentUser.getEmail(), 15);
        JPasswordField passwordField = new JPasswordField(15);
        JPasswordField confirmField = new JPasswordField(15);
        
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        gbc.gridx = 0; gbc.gridy = 0; panel.add(new JLabel("Username:"), gbc);
        gbc.gridx = 1; panel.add(usernameField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 1; panel.add(new JLabel("Email:"), gbc);
        gbc.gridx = 1; panel.add(emailField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 2; panel.add(new JLabel("New Password (optional):"), gbc);
        gbc.gridx = 1; panel.add(passwordField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 3; panel.add(new JLabel("Confirm Password:"), gbc);
        gbc.gridx = 1; panel.add(confirmField, gbc);
        
        int option = JOptionPane.showConfirmDialog(this, panel, "Update Profile", 
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        
        if (option == JOptionPane.OK_OPTION) {
            String newUsername = usernameField.getText().trim();
            String newEmail = emailField.getText().trim();
            String newPassword = new String(passwordField.getPassword());
            String confirmPassword = new String(confirmField.getPassword());
            
            if (newUsername.isEmpty() || newEmail.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Username and email cannot be empty!");
                return;
            }
            
            if (!newPassword.isEmpty() && !newPassword.equals(confirmPassword)) {
                JOptionPane.showMessageDialog(this, "Passwords do not match!");
                return;
            }
            
            try (Connection conn = DatabaseConnection.getConnection()) {
                String sql;
                PreparedStatement pstmt;
                
                if (!newPassword.isEmpty()) {
                    sql = "UPDATE users SET username = ?, email = ?, password = ? WHERE id = ?";
                    pstmt = conn.prepareStatement(sql);
                    pstmt.setString(1, newUsername);
                    pstmt.setString(2, newEmail);
                    pstmt.setString(3, newPassword);
                    pstmt.setInt(4, currentUser.getId());
                } else {
                    sql = "UPDATE users SET username = ?, email = ? WHERE id = ?";
                    pstmt = conn.prepareStatement(sql);
                    pstmt.setString(1, newUsername);
                    pstmt.setString(2, newEmail);
                    pstmt.setInt(3, currentUser.getId());
                }
                
                if (pstmt.executeUpdate() > 0) {
                    currentUser = new User(currentUser.getId(), newUsername, newEmail);
                    JOptionPane.showMessageDialog(this, "Profile updated successfully! Please login again.");
                    logout();
                } else {
                    JOptionPane.showMessageDialog(this, "Update failed!");
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Database error: " + e.getMessage());
            }
        }
    }

    // ── Main Content with Calendar ───────────────────────────────────────────

    private JPanel buildMainContent() {
        JPanel content = new JPanel(new BorderLayout(0, 0)) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, BG_TOP, 0, getHeight(), BG_BOT);
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(new Color(255, 255, 255, 15));
                g2.fillOval(getWidth() - 420, 20, 380, 260);
                g2.setColor(new Color(255, 255, 255, 8));
                g2.fillOval(60, getHeight() - 280, 300, 220);
                g2.dispose();
            }
        };
        content.setOpaque(false);
        content.add(buildTopBar(), BorderLayout.NORTH);

        JPanel body = new JPanel(new BorderLayout(18, 18)) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(new Color(255, 255, 255, 10));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        body.setOpaque(false);
        body.setBorder(BorderFactory.createEmptyBorder(16, 20, 20, 20));
        body.add(buildInsightRow(), BorderLayout.NORTH);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, buildCalendarPanel(), buildTableCard());
        splitPane.setDividerLocation(360);
        splitPane.setDividerSize(10);
        splitPane.setBorder(null);
        splitPane.setBackground(new Color(0,0,0,0));
        splitPane.setOpaque(false);

        body.add(splitPane, BorderLayout.CENTER);
        body.add(buildActionBar(), BorderLayout.SOUTH);

        content.add(body, BorderLayout.CENTER);
        return content;
    }

    // ── Calendar Panel ──
    private JPanel buildCalendarPanel() {
        JPanel calendarContainer = new JPanel(new BorderLayout());
        calendarContainer.setBackground(CARD_BG);
        calendarContainer.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(CARD_BORDER, 1, true),
            BorderFactory.createEmptyBorder(0, 0, 0, 0)
        ));
        
        // Calendar Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(CARD_BG);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 8, 12));
        
        JButton prevMonthBtn = new JButton("◀");
        prevMonthBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        prevMonthBtn.setBackground(CARD_BG);
        prevMonthBtn.setBorder(BorderFactory.createLineBorder(CARD_BORDER));
        prevMonthBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        prevMonthBtn.addActionListener(e -> changeMonth(-1));
        
        monthYearLabel = new JLabel();
        monthYearLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        monthYearLabel.setForeground(TEXT_PRIMARY);
        monthYearLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        JButton nextMonthBtn = new JButton("▶");
        nextMonthBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        nextMonthBtn.setBackground(CARD_BG);
        nextMonthBtn.setBorder(BorderFactory.createLineBorder(CARD_BORDER));
        nextMonthBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        nextMonthBtn.addActionListener(e -> changeMonth(1));
        
        headerPanel.add(prevMonthBtn, BorderLayout.WEST);
        headerPanel.add(monthYearLabel, BorderLayout.CENTER);
        headerPanel.add(nextMonthBtn, BorderLayout.EAST);
        
        calendarContainer.add(headerPanel, BorderLayout.NORTH);
        
        // Calendar Grid
        calendarPanel = new JPanel(new GridLayout(7, 7, 4, 4));
        calendarPanel.setBackground(CARD_BG);
        calendarPanel.setBorder(BorderFactory.createEmptyBorder(8, 12, 12, 12));
        
        calendarContainer.add(calendarPanel, BorderLayout.CENTER);
        
        return calendarContainer;
    }

    private void refreshCalendar() {
        if (calendarPanel == null) return;
        
        calendarPanel.removeAll();
        monthYearLabel.setText(currentCalendarDate.format(MONTH_YEAR_FORMAT));
        
        // Day headers
        String[] days = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
        for (String day : days) {
            JLabel dayLabel = new JLabel(day, SwingConstants.CENTER);
            dayLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
            dayLabel.setForeground(ACCENT);
            calendarPanel.add(dayLabel);
        }
        
        // Get first day of month
        YearMonth yearMonth = YearMonth.from(currentCalendarDate);
        LocalDate firstOfMonth = yearMonth.atDay(1);
        int startOffset = firstOfMonth.getDayOfWeek().getValue() % 7;
        
        // Add empty cells for days before month starts
        for (int i = 0; i < startOffset; i++) {
            JLabel emptyLabel = new JLabel("");
            calendarPanel.add(emptyLabel);
        }
        
        // Add days of the month
        int daysInMonth = yearMonth.lengthOfMonth();
        LocalDate today = LocalDate.now();
        
        for (int day = 1; day <= daysInMonth; day++) {
            LocalDate date = yearMonth.atDay(day);
            JPanel dayCell = createDayCell(date, day, date.equals(today));
            calendarPanel.add(dayCell);
        }
        
        calendarPanel.revalidate();
        calendarPanel.repaint();
    }

    private JPanel createDayCell(LocalDate date, int day, boolean isToday) {
        JPanel cell = new JPanel(new BorderLayout());
        cell.setBackground(CARD_BG);
        cell.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
        
        // Day number
        JLabel dayLabel = new JLabel(String.valueOf(day), SwingConstants.CENTER);
        dayLabel.setFont(new Font("Segoe UI", isToday ? Font.BOLD : Font.PLAIN, 12));
        
        if (isToday) {
            dayLabel.setForeground(CALENDAR_TODAY);
            dayLabel.setBackground(new Color(0xFEE2E2));
            dayLabel.setOpaque(true);
            dayLabel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        } else {
            dayLabel.setForeground(TEXT_PRIMARY);
        }
        
        cell.add(dayLabel, BorderLayout.NORTH);
        
        // Check if there are events on this date
        if (eventsByDate.containsKey(date)) {
            int eventCount = eventsByDate.get(date).size();
            JLabel eventIndicator = new JLabel("●", SwingConstants.CENTER);
            eventIndicator.setFont(new Font("Segoe UI", Font.BOLD, 8));
            eventIndicator.setForeground(CALENDAR_EVENT);
            eventIndicator.setToolTipText(eventCount + " event(s)");
            cell.add(eventIndicator, BorderLayout.CENTER);
        }
        
        // Add click listener to show events on selected date
        cell.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        cell.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                showEventsForDate(date);
            }
        });
        
        return cell;
    }

    private void changeMonth(int delta) {
        currentCalendarDate = currentCalendarDate.plusMonths(delta);
        refreshCalendar();
    }

    private void showEventsForDate(LocalDate date) {
        if (eventsByDate.containsKey(date)) {
            List<Event> events = eventsByDate.get(date);
            StringBuilder sb = new StringBuilder();
            sb.append("Events on ").append(date.format(DATE_FORMAT)).append(":\n\n");
            for (Event event : events) {
                sb.append("• ").append(event.getTime().format(TIME_FORMAT))
                  .append(" - ").append(event.getTitle())
                  .append(" (").append(event.getDuration()).append(" min)\n");
            }
            JOptionPane.showMessageDialog(this, sb.toString(), 
                "Events", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, 
                "No events scheduled on " + date.format(DATE_FORMAT),
                "No Events", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void buildEventsByDateMap() {
        eventsByDate.clear();
        List<Event> events = eventTree.inorder();
        for (Event event : events) {
            eventsByDate.computeIfAbsent(event.getDate(), k -> new ArrayList<>()).add(event);
        }
    }

    private JPanel buildTopBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setOpaque(false);
        bar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(255, 255, 255, 30)),
            BorderFactory.createEmptyBorder(14, 20, 14, 20)
        ));

        JPanel intro = new JPanel(new GridLayout(2, 1, 2, 2));
        intro.setOpaque(false);
        JLabel welcome = new JLabel("Welcome back, " + currentUser.getUsername() + "! 👋");
        welcome.setFont(FONT_HEADING);
        welcome.setForeground(TEXT_PRIMARY);
        JLabel subtitle = new JLabel("Your personal planner is now glowing with daily notes and focus cues.");
        subtitle.setFont(FONT_BODY);
        subtitle.setForeground(TEXT_DIM);
        intro.add(welcome);
        intro.add(subtitle);

        JPanel clockPanel = new JPanel(new GridLayout(2, 1, 0, 2));
        clockPanel.setOpaque(false);
        currentTimeLabel = new JLabel(LocalTime.now().format(TIME_FORMAT), SwingConstants.RIGHT);
        currentTimeLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        currentTimeLabel.setForeground(ACCENT);
        JLabel dateLabel = new JLabel(LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMM d")), SwingConstants.RIGHT);
        dateLabel.setFont(FONT_BODY);
        dateLabel.setForeground(TEXT_DIM);
        clockPanel.add(currentTimeLabel);
        clockPanel.add(dateLabel);

        bar.add(intro, BorderLayout.WEST);
        bar.add(clockPanel, BorderLayout.EAST);
        return bar;
    }

    private JPanel buildInsightRow() {
        JPanel row = new JPanel(new GridLayout(1, 2, 14, 0));
        row.setOpaque(false);
        row.add(buildStatsRow());
        row.add(buildDailyNoteCard());
        return row;
    }

    private JPanel buildStatsRow() {
        JPanel row = new JPanel(new GridLayout(1, 2, 14, 0));
        row.setOpaque(false);

        totalEventsLabel = new JLabel("0");
        nextEventLabel   = new JLabel("—");

        row.add(buildStatCard("Total Events", totalEventsLabel, "◷", new Color(0x3B1F8C), new Color(0xC4B5FD)));
        row.add(buildStatCard("Next Event",   nextEventLabel,   "⊞", new Color(0x5B3EEB), new Color(0xFBCFE8)));
        return row;
    }

    private JPanel buildDailyNoteCard() {
        JPanel card = new JPanel(new BorderLayout(14, 12)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(GLASS_FILL);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 22, 22);
                g2.setColor(GLASS_BORDER);
                g2.setStroke(new BasicStroke(1.3f));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 22, 22);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));

        JLabel header = new JLabel("Note of the Day");
        header.setFont(new Font("Segoe UI", Font.BOLD, 16));
        header.setForeground(TEXT_PRIMARY);

        noteOfDayLabel = new JLabel("<html><i>" + getDailyNote() + "</i></html>");
        noteOfDayLabel.setFont(FONT_BODY);
        noteOfDayLabel.setForeground(TEXT_DIM);
        noteOfDayLabel.setBorder(BorderFactory.createEmptyBorder(8, 0, 12, 0));

        JLabel prompt = new JLabel("Tonight: schedule one deep-focus session with a violet glow.");
        prompt.setFont(FONT_SMALL);
        prompt.setForeground(TEXT_SECONDARY);

        JLabel action = new JLabel("Today’s pulse: " + getDailyPulse());
        action.setFont(new Font("Segoe UI", Font.BOLD, 14));
        action.setForeground(ACCENT);

        card.add(header, BorderLayout.NORTH);

        JPanel center = new JPanel(new GridLayout(3, 1, 6, 6));
        center.setOpaque(false);
        center.add(noteOfDayLabel);
        center.add(prompt);
        center.add(action);

        card.add(center, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildStatCard(String title, JLabel valueLabel, String icon, Color bgColor, Color iconColor) {
        JPanel card = new JPanel(new BorderLayout(10, 6));
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(CARD_BORDER, 1, true),
            BorderFactory.createEmptyBorder(16, 18, 16, 18)
        ));

        JLabel iconLbl = new JLabel(icon, SwingConstants.CENTER);
        iconLbl.setFont(new Font("Segoe UI", Font.BOLD, 16));
        iconLbl.setForeground(iconColor);
        iconLbl.setBackground(bgColor);
        iconLbl.setOpaque(true);
        iconLbl.setPreferredSize(new Dimension(40, 40));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        left.setOpaque(false);
        left.add(iconLbl);

        JPanel textPanel = new JPanel();
        textPanel.setOpaque(false);
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(FONT_STAT_LBL);
        titleLbl.setForeground(TEXT_SECONDARY);

        valueLabel.setFont(FONT_STAT);
        valueLabel.setForeground(TEXT_PRIMARY);

        textPanel.add(titleLbl);
        textPanel.add(Box.createVerticalStrut(4));
        textPanel.add(valueLabel);

        card.add(left, BorderLayout.WEST);
        card.add(textPanel, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildTableCard() {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(CARD_BORDER, 1, true),
            BorderFactory.createEmptyBorder(0, 0, 0, 0)
        ));

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(CARD_BG);
        header.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, CARD_BORDER),
            BorderFactory.createEmptyBorder(14, 18, 14, 18)
        ));
        JLabel title = new JLabel("Your Events · Sorted by Date & Time");
        title.setFont(FONT_HEADING);
        title.setForeground(TEXT_PRIMARY);
        header.add(title, BorderLayout.WEST);
        card.add(header, BorderLayout.NORTH);

        String[] columns = {"ID", "Title", "Date", "Time", "Duration (min)", "End Time"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        eventTable = new JTable(tableModel);
        eventTable.setFont(FONT_BODY);
        eventTable.setRowHeight(36);
        eventTable.setShowGrid(false);
        eventTable.setIntercellSpacing(new Dimension(0, 0));
        eventTable.setBackground(CARD_BG);
        eventTable.setSelectionBackground(new Color(120, 80, 255, 90));
        eventTable.setSelectionForeground(TEXT_PRIMARY);
        eventTable.setFocusable(false);

        JTableHeader th = eventTable.getTableHeader();
        th.setFont(new Font("Segoe UI", Font.BOLD, 12));
        th.setBackground(new Color(255, 255, 255, 18));
        th.setForeground(TEXT_PRIMARY);
        th.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, CARD_BORDER));
        th.setReorderingAllowed(false);

        eventTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(
                    JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
                setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
                if (!isSelected) {
                    setBackground(row % 2 == 0 ? CARD_BG : new Color(255, 255, 255, 12));
                    setForeground(col == 1 ? TEXT_PRIMARY : TEXT_DIM);
                }
                setFont(col == 1 ? new Font("Segoe UI", Font.BOLD, 13) : FONT_BODY);
                return this;
            }
        });

        int[] widths = {50, 220, 100, 70, 110, 80};
        for (int i = 0; i < widths.length; i++)
            eventTable.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);

        JScrollPane scroll = new JScrollPane(eventTable);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(CARD_BG);
        card.add(scroll, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildActionBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        bar.setOpaque(false);

        JButton addBtn    = makePrimaryButton("+ Add Event");
        JButton deleteBtn = makeDangerButton("Delete Selected");
        JButton nextBtn   = makeSecondaryButton("⏭ Next Event");
        JButton filterBtn = makeSecondaryButton("Filter by Date");
        JButton refreshBtn= makeSecondaryButton("Refresh");

        addBtn.addActionListener(e -> addEvent());
        deleteBtn.addActionListener(e -> deleteEvent());
        nextBtn.addActionListener(e -> findNextEvent());
        filterBtn.addActionListener(e -> filterByDate());
        refreshBtn.addActionListener(e -> refreshAll());

        bar.add(addBtn);
        bar.add(deleteBtn);
        bar.add(nextBtn);
        bar.add(filterBtn);
        bar.add(refreshBtn);
        return bar;
    }

    // ── Button Factories ──

    private JButton makePrimaryButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setBackground(ACCENT);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(9, 18, 9, 18));
        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { btn.setBackground(new Color(0x6D28D9)); }
            @Override public void mouseExited(MouseEvent e)  { btn.setBackground(ACCENT); }
        });
        return btn;
    }

    private JButton makeDangerButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_BODY);
        btn.setBackground(new Color(0xFEE2E2));
        btn.setForeground(DANGER);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0xFCA5A5), 1, true),
            BorderFactory.createEmptyBorder(8, 16, 8, 16)
        ));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JButton makeSecondaryButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_BODY);
        btn.setBackground(Color.WHITE);
        btn.setForeground(TEXT_PRIMARY);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(CARD_BORDER, 1, true),
            BorderFactory.createEmptyBorder(8, 16, 8, 16)
        ));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { btn.setBackground(new Color(0xF5F3FF)); }
            @Override public void mouseExited(MouseEvent e)  { btn.setBackground(Color.WHITE); }
        });
        return btn;
    }

    // ── Helpers ──

    private String getInitials(String name) {
        if (name == null || name.isEmpty()) return "?";
        String[] parts = name.trim().split("\\s+");
        if (parts.length == 1) return parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase();
        return (parts[0].charAt(0) + "" + parts[1].charAt(0)).toUpperCase();
    }

    private void refreshEventTable() {
        tableModel.setRowCount(0);
        List<Event> events = eventTree.inorder();
        for (Event event : events) {
            tableModel.addRow(new Object[]{
                event.getId(),
                event.getTitle(),
                event.getDate().format(DATE_FORMAT),
                event.getTime().format(TIME_FORMAT),
                event.getDuration(),
                event.getEndTime().format(TIME_FORMAT)
            });
        }
        buildEventsByDateMap();
        refreshCalendar();
    }

    private void updateStats() {
        int size = eventTree.size();
        totalEventsLabel.setText(String.valueOf(size));

        Event next = eventTree.findMin();
        if (next != null) {
            nextEventLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
            nextEventLabel.setText("<html>" + next.getTitle() + "<br><span style='font-size:10px;color:#D4D0FF'>"
                + next.getDate().format(DATE_FORMAT) + " " + next.getTime().format(TIME_FORMAT) + "</span></html>");
        } else {
            nextEventLabel.setFont(FONT_STAT);
            nextEventLabel.setText("—");
        }
    }

    private void startClock() {
        if (currentTimeLabel == null) return;
        Timer timer = new Timer(1000, e -> currentTimeLabel.setText(LocalTime.now().format(TIME_FORMAT)));
        timer.setRepeats(true);
        timer.start();
    }

    private String getDailyNote() {
        return DAILY_NOTES[LocalDate.now().getDayOfMonth() % DAILY_NOTES.length];
    }

    private String getDailyPulse() {
        int hour = LocalTime.now().getHour();
        if (hour < 10) return "Morning momentum: set the tone.";
        if (hour < 14) return "Afternoon sprint: focus on the top priority.";
        if (hour < 18) return "Golden hour: add a mindful check-in.";
        return "Evening reset: close the loop gently.";
    }

    // ── Database & Logic ──

    private void loadEventsFromDatabase() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT * FROM events WHERE user_id = ? ORDER BY event_date, event_time";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, currentUser.getId());
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Event event = new Event(
                    rs.getInt("id"),
                    rs.getString("title"),
                    LocalDate.parse(rs.getString("event_date")),
                    LocalTime.parse(rs.getString("event_time")),
                    rs.getInt("duration")
                );
                eventTree.insert(event);
            }
            buildEventsByDateMap();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading events: " + e.getMessage());
        }
    }

    private void addEvent() {
        JTextField titleField = new JTextField();
        JTextField dateField  = new JTextField(LocalDate.now().format(DATE_FORMAT));
        JTextField timeField  = new JTextField("09:00");
        JComboBox<Integer> durationBox = new JComboBox<>(new Integer[]{30, 60, 90, 120});

        Object[] fields = {
            "Title:", titleField,
            "Date (YYYY-MM-DD):", dateField,
            "Time (HH:MM):", timeField,
            "Duration (min):", durationBox
        };

        int option = JOptionPane.showConfirmDialog(this, fields, "Add New Event", JOptionPane.OK_CANCEL_OPTION);
        if (option != JOptionPane.OK_OPTION) return;

        try {
            String title = titleField.getText().trim();
            if (title.isEmpty()) { JOptionPane.showMessageDialog(this, "Please enter a title!"); return; }
            LocalDate date = LocalDate.parse(dateField.getText().trim(), DATE_FORMAT);
            LocalTime time = LocalTime.parse(timeField.getText().trim());
            int duration   = (Integer) durationBox.getSelectedItem();

            if (eventTree.checkConflict(new Event(0, title, date, time, duration), duration)) {
                int confirm = JOptionPane.showConfirmDialog(this, "⚠️ Time conflict detected! Continue anyway?",
                    "Conflict Warning", JOptionPane.YES_NO_OPTION);
                if (confirm != JOptionPane.YES_OPTION) return;
            }

            try (Connection conn = DatabaseConnection.getConnection()) {
                PreparedStatement pstmt = conn.prepareStatement(
                    "INSERT INTO events (user_id, title, event_date, event_time, duration) VALUES (?, ?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS);
                pstmt.setInt(1, currentUser.getId());
                pstmt.setString(2, title);
                pstmt.setString(3, date.toString());
                pstmt.setString(4, time.toString());
                pstmt.setInt(5, duration);
                pstmt.executeUpdate();
                ResultSet rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    Event event = new Event(rs.getInt(1), title, date, time, duration);
                    eventTree.insert(event);
                }
                JOptionPane.showMessageDialog(this, "✅ Event added successfully!");
                refreshEventTable();
                updateStats();
                refreshCalendar();
            }
        } catch (DateTimeParseException e) {
            JOptionPane.showMessageDialog(this, "Invalid date/time format! Use YYYY-MM-DD and HH:MM");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Database error: " + e.getMessage());
        }
    }

    private void deleteEvent() {
        int selectedRow = eventTable.getSelectedRow();
        if (selectedRow == -1) { JOptionPane.showMessageDialog(this, "Please select an event to delete!"); return; }

        int eventId    = (int) tableModel.getValueAt(selectedRow, 0);
        String title   = (String) tableModel.getValueAt(selectedRow, 1);

        int confirm = JOptionPane.showConfirmDialog(this, "Delete \"" + title + "\"?",
            "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        try (Connection conn = DatabaseConnection.getConnection()) {
            PreparedStatement pstmt = conn.prepareStatement("DELETE FROM events WHERE id = ? AND user_id = ?");
            pstmt.setInt(1, eventId);
            pstmt.setInt(2, currentUser.getId());
            pstmt.executeUpdate();
            for (Event e : eventTree.inorder()) {
                if (e.getId() == eventId) { eventTree.delete(e); break; }
            }
            JOptionPane.showMessageDialog(this, "✅ Event deleted!");
            refreshEventTable();
            updateStats();
            refreshCalendar();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

    private void findNextEvent() {
        Event next = eventTree.findMin();
        if (next != null) {
            JOptionPane.showMessageDialog(this,
                "⏰ Next Event:\n\nTitle:    " + next.getTitle() +
                "\nDate:     " + next.getDate().format(DATE_FORMAT) +
                "\nTime:     " + next.getTime().format(TIME_FORMAT) +
                "\nDuration: " + next.getDuration() + " minutes",
                "Next Event", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "No events scheduled!");
        }
    }

    private void filterByDate() {
        String dateStr = JOptionPane.showInputDialog(this,
            "Enter date (YYYY-MM-DD):", "Filter by Date", JOptionPane.QUESTION_MESSAGE);
        if (dateStr == null || dateStr.trim().isEmpty()) return;
        try {
            LocalDate filterDate = LocalDate.parse(dateStr.trim(), DATE_FORMAT);
            List<Event> filtered = eventTree.rangeSearch(
                new Event(0, "", filterDate, LocalTime.MIN, 0),
                new Event(0, "", filterDate.plusDays(1), LocalTime.MIN, 0));
            if (filtered.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No events on " + dateStr);
            } else {
                StringBuilder sb = new StringBuilder("Events on " + dateStr + ":\n\n");
                for (Event e : filtered)
                    sb.append("• ").append(e.getTime().format(TIME_FORMAT))
                    .append("  —  ").append(e.getTitle())
                    .append("  (").append(e.getDuration()).append(" min)\n");
                JOptionPane.showMessageDialog(this, sb.toString(), "Filter Results", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (DateTimeParseException e) {
            JOptionPane.showMessageDialog(this, "Invalid date format! Use YYYY-MM-DD");
        }
    }

    private void refreshAll() {
        eventTree = new AVLTree<>();
        loadEventsFromDatabase();
        refreshEventTable();
        updateStats();
        refreshCalendar();
    }

    private void showFullStats() {
        List<Event> events = eventTree.inorder();
        if (events.isEmpty()) { JOptionPane.showMessageDialog(this, "No events to analyze!"); return; }

        int total = events.size();
        int totalMin = events.stream().mapToInt(Event::getDuration).sum();
        double avg  = (double) totalMin / total;

        java.util.Map<LocalDate, Integer> dayCount = new HashMap<>();
        for (Event e : events) dayCount.put(e.getDate(), dayCount.getOrDefault(e.getDate(), 0) + 1);
        LocalDate busiest = dayCount.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey).orElse(null);

        JOptionPane.showMessageDialog(this, String.format(
            "📊 Schedule Statistics\n\n" +
            "Total Events:    %d\n" +
            "Total Time:      %d min  (%.1f hrs)\n" +
            "Avg Duration:    %.1f min\n" +
            "Busiest Day:     %s  (%d events)\n" +
            "AVL Height:      %d\n\n" +
            "All operations run in O(log n) time!",
            total, totalMin, totalMin / 60.0, avg,
            busiest != null ? busiest.format(DATE_FORMAT) : "N/A",
            busiest != null ? dayCount.get(busiest) : 0,
            eventTree.getHeight()), "Statistics", JOptionPane.INFORMATION_MESSAGE);
    }

    private void logout() {
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to logout?",
            "Logout", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) { new LoginGUI().setVisible(true); dispose(); }
    }
}