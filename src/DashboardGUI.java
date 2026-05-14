import javax.swing.*;
import javax.swing.table.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.time.*;
import java.time.format.*;
import java.sql.*;
import java.util.List;

public class DashboardGUI extends JFrame {
    private User currentUser;
    private AVLTree<Event> eventTree;
    private JTable eventTable;
    private DefaultTableModel tableModel;
    private JLabel totalEventsLabel;
    private JLabel treeHeightLabel;
    private JLabel nextEventLabel;

    // Color palette inspired by reference (purple sidebar, clean white content)
    private static final Color SIDEBAR_BG     = new Color(0x4B3FA0);   // deep purple
    private static final Color SIDEBAR_ACTIVE = new Color(0x6C5CE7);   // lighter purple for active
    private static final Color SIDEBAR_TEXT   = new Color(0xE8E4FF);   // pale lavender
    private static final Color ACCENT         = new Color(0x7C6FF7);   // accent purple
    private static final Color CONTENT_BG     = new Color(0xF5F4FB);   // very light lavender-white
    private static final Color CARD_BG        = Color.WHITE;
    private static final Color CARD_BORDER    = new Color(0xE2DFFA);
    private static final Color TEXT_PRIMARY   = new Color(0x1E1B4B);
    private static final Color TEXT_SECONDARY = new Color(0x6B7280);
    private static final Color TABLE_HEADER   = new Color(0x7C6FF7);
    private static final Color TABLE_ALT      = new Color(0xF8F7FF);
    private static final Color DANGER         = new Color(0xEF4444);
    private static final Color SUCCESS        = new Color(0x10B981);

    private static final Font FONT_TITLE   = new Font("Segoe UI", Font.BOLD, 22);
    private static final Font FONT_HEADING = new Font("Segoe UI", Font.BOLD, 15);
    private static final Font FONT_BODY    = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font FONT_SMALL   = new Font("Segoe UI", Font.PLAIN, 12);
    private static final Font FONT_STAT    = new Font("Segoe UI", Font.BOLD, 28);
    private static final Font FONT_STAT_LBL= new Font("Segoe UI", Font.PLAIN, 12);

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    private JPanel activeSidebarBtn = null;

    public DashboardGUI(User user) {
        this.currentUser = user;
        this.eventTree = new AVLTree<>();

        setTitle("AVL Scheduler");
        setSize(1050, 700);
        setMinimumSize(new Dimension(900, 600));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setBackground(CONTENT_BG);

        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
        catch (Exception ignored) {}

        loadEventsFromDatabase();
        buildUI();
        refreshEventTable();
        updateStats();
    }

    // ── Layout ──────────────────────────────────────────────────────────────

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

        // Brand
        JPanel brand = new JPanel(new FlowLayout(FlowLayout.LEFT, 18, 18));
        brand.setOpaque(false);
        JLabel brandIcon = new JLabel("◈");
        brandIcon.setFont(new Font("Segoe UI", Font.BOLD, 22));
        brandIcon.setForeground(new Color(0xC4B5FD));
        JLabel brandName = new JLabel("AVL Scheduler");
        brandName.setFont(new Font("Segoe UI", Font.BOLD, 15));
        brandName.setForeground(Color.WHITE);
        brand.add(brandIcon);
        brand.add(brandName);
        sidebar.add(brand);

        sidebar.add(Box.createVerticalStrut(10));

        // Nav items
        String[][] navItems = {
            {"⊞", "Dashboard"},
            {"◷", "All Events"},
            {"◈", "Statistics"},
        };
        for (String[] item : navItems) {
            JPanel btn = makeSidebarButton(item[0], item[1]);
            sidebar.add(btn);
            sidebar.add(Box.createVerticalStrut(4));
        }

        sidebar.add(Box.createVerticalGlue());

        // User profile at bottom
        JPanel userPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 14));
        userPanel.setOpaque(false);
        JLabel avatar = new JLabel(getInitials(currentUser.getUsername()));
        avatar.setFont(new Font("Segoe UI", Font.BOLD, 13));
        avatar.setForeground(SIDEBAR_BG);
        avatar.setBackground(new Color(0xC4B5FD));
        avatar.setOpaque(true);
        avatar.setPreferredSize(new Dimension(36, 36));
        avatar.setHorizontalAlignment(SwingConstants.CENTER);
        avatar.setBorder(BorderFactory.createLineBorder(new Color(0xA78BFA), 0));
        JPanel avatarCircle = new JPanel(new BorderLayout());
        avatarCircle.setOpaque(false);
        avatarCircle.setPreferredSize(new Dimension(36, 36));
        avatarCircle.add(avatar);

        JLabel userLbl = new JLabel("<html><b style='color:white'>" + currentUser.getUsername() + "</b>"
                + "<br><span style='color:#C4B5FD;font-size:10px'>Active</span></html>");
        userPanel.add(avatarCircle);
        userPanel.add(userLbl);
        sidebar.add(userPanel);

        return sidebar;
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

    // ── Main Content ─────────────────────────────────────────────────────────

    private JPanel buildMainContent() {
        JPanel content = new JPanel(new BorderLayout(0, 0));
        content.setBackground(CONTENT_BG);

        content.add(buildTopBar(), BorderLayout.NORTH);

        JPanel body = new JPanel(new BorderLayout(16, 16));
        body.setBackground(CONTENT_BG);
        body.setBorder(BorderFactory.createEmptyBorder(16, 20, 20, 20));
        body.add(buildStatsRow(), BorderLayout.NORTH);
        body.add(buildTableCard(), BorderLayout.CENTER);
        body.add(buildActionBar(), BorderLayout.SOUTH);

        content.add(body, BorderLayout.CENTER);
        return content;
    }

    private JPanel buildTopBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(Color.WHITE);
        bar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, CARD_BORDER),
            BorderFactory.createEmptyBorder(12, 20, 12, 20)
        ));

        JLabel welcome = new JLabel("Welcome back, " + currentUser.getUsername() + "! 👋");
        welcome.setFont(FONT_HEADING);
        welcome.setForeground(TEXT_PRIMARY);

        // Right: logout button
        JButton logout = makeTextButton("Logout", DANGER);
        logout.addActionListener(e -> logout());

        bar.add(welcome, BorderLayout.WEST);
        bar.add(logout, BorderLayout.EAST);
        return bar;
    }

    private JPanel buildStatsRow() {
        JPanel row = new JPanel(new GridLayout(1, 3, 14, 0));
        row.setOpaque(false);

        totalEventsLabel = new JLabel("0");
        treeHeightLabel  = new JLabel("0");
        nextEventLabel   = new JLabel("—");

        row.add(buildStatCard("Total Events", totalEventsLabel, "◷", new Color(0xEDE9FE), new Color(0x7C3AED)));
        row.add(buildStatCard("Tree Height",  treeHeightLabel,  "⊕", new Color(0xD1FAE5), new Color(0x059669)));
        row.add(buildStatCard("Next Event",   nextEventLabel,   "⊞", new Color(0xFEF3C7), new Color(0xD97706)));

        return row;
    }

    private JPanel buildStatCard(String title, JLabel valueLabel, String icon, Color bgColor, Color iconColor) {
        JPanel card = new JPanel(new BorderLayout(10, 6));
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(CARD_BORDER, 1, true),
            BorderFactory.createEmptyBorder(16, 18, 16, 18)
        ));

        // Icon badge
        JLabel iconLbl = new JLabel(icon, SwingConstants.CENTER);
        iconLbl.setFont(new Font("Segoe UI", Font.BOLD, 16));
        iconLbl.setForeground(iconColor);
        iconLbl.setBackground(bgColor);
        iconLbl.setOpaque(true);
        iconLbl.setPreferredSize(new Dimension(40, 40));
        iconLbl.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

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

        // Card header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(CARD_BG);
        header.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, CARD_BORDER),
            BorderFactory.createEmptyBorder(14, 18, 14, 18)
        ));
        JLabel title = new JLabel("Your Events  ·  Sorted by Date & Time");
        title.setFont(FONT_HEADING);
        title.setForeground(TEXT_PRIMARY);
        header.add(title, BorderLayout.WEST);
        card.add(header, BorderLayout.NORTH);

        // Table
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
        eventTable.setSelectionBackground(new Color(0xEDE9FE));
        eventTable.setSelectionForeground(TEXT_PRIMARY);
        eventTable.setFocusable(false);

        // Header style
        JTableHeader th = eventTable.getTableHeader();
        th.setFont(new Font("Segoe UI", Font.BOLD, 12));
        th.setBackground(new Color(0xF5F3FF));
        th.setForeground(new Color(0x6D28D9));
        th.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, CARD_BORDER));
        th.setReorderingAllowed(false);

        // Alternating rows renderer
        eventTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(
                    JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
                setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
                if (!isSelected) {
                    setBackground(row % 2 == 0 ? CARD_BG : TABLE_ALT);
                    setForeground(col == 1 ? TEXT_PRIMARY : TEXT_SECONDARY);
                }
                setFont(col == 1 ? new Font("Segoe UI", Font.BOLD, 13) : FONT_BODY);
                return this;
            }
        });

        // Column widths
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

    // ── Button Factories ──────────────────────────────────────────────────────

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

    private JButton makeTextButton(String text, Color color) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_BODY);
        btn.setForeground(color);
        btn.setBackground(new Color(color.getRed(), color.getGreen(), color.getBlue(), 20));
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(6, 14, 6, 14));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

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
    }

    private void updateStats() {
        int size = eventTree.size();
        totalEventsLabel.setText(String.valueOf(size));
        treeHeightLabel.setText(String.valueOf(eventTree.getHeight()));

        Event next = eventTree.findMin();
        if (next != null) {
            nextEventLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
            nextEventLabel.setText("<html>" + next.getTitle() + "<br><span style='font-size:10px;color:gray'>"
                + next.getDate().format(DATE_FORMAT) + " " + next.getTime().format(TIME_FORMAT) + "</span></html>");
        } else {
            nextEventLabel.setFont(FONT_STAT);
            nextEventLabel.setText("—");
        }
    }

    // ── Database & Logic (unchanged logic, same as original) ──────────────────

    private void loadEventsFromDatabase() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT * FROM events WHERE user_id = ? ORDER BY event_date, event_time";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, currentUser.getId());
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                eventTree.insert(new Event(
                    rs.getInt("id"),
                    rs.getString("title"),
                    LocalDate.parse(rs.getString("event_date")),
                    LocalTime.parse(rs.getString("event_time")),
                    rs.getInt("duration")
                ));
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading events: " + e.getMessage());
        }
    }

    private void addEvent() {
        JTextField titleField = new JTextField();
        JTextField dateField  = new JTextField(LocalDate.now().format(DATE_FORMAT));
        JTextField timeField  = new JTextField("09:00");
        JComboBox<Integer> durationBox = new JComboBox<>(new Integer[]{30, 60, 90, 120});

        styleDialogField(titleField);
        styleDialogField(dateField);
        styleDialogField(timeField);

        Object[] fields = {
            label("Title:"),        titleField,
            label("Date (YYYY-MM-DD):"), dateField,
            label("Time (HH:MM):"), timeField,
            label("Duration (min):"), durationBox
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
                if (rs.next()) eventTree.insert(new Event(rs.getInt(1), title, date, time, duration));
                JOptionPane.showMessageDialog(this, "✅ Event added successfully!");
                refreshEventTable();
                updateStats();
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
    }

    private void showFullStats() {
        List<Event> events = eventTree.inorder();
        if (events.isEmpty()) { JOptionPane.showMessageDialog(this, "No events to analyze!"); return; }

        int total = events.size();
        int totalMin = events.stream().mapToInt(Event::getDuration).sum();
        double avg  = (double) totalMin / total;

        java.util.Map<LocalDate, Integer> dayCount = new java.util.HashMap<>();
        for (Event e : events) dayCount.put(e.getDate(), dayCount.getOrDefault(e.getDate(), 0) + 1);
        LocalDate busiest = dayCount.entrySet().stream()
            .max(java.util.Map.Entry.comparingByValue())
            .map(java.util.Map.Entry::getKey).orElse(null);

        JOptionPane.showMessageDialog(this, String.format(
            "📊  Schedule Statistics\n\n" +
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

    // ── Small Helpers ─────────────────────────────────────────────────────────

    private JLabel label(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        return l;
    }

    private void styleDialogField(JTextField field) {
        field.setFont(FONT_BODY);
        field.setPreferredSize(new Dimension(220, 30));
    }
}