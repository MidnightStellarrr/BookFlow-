import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.time.*;
import java.time.format.*;
import java.sql.*;
import java.util.List;

public class DashboardGUI extends JFrame {
    private User currentUser;
    private AVLTree<Event> eventTree;
    private JTable eventTable;
    private DefaultTableModel tableModel;
    private JLabel statsLabel;
    
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");
    
    public DashboardGUI(User user) {
        this.currentUser = user;
        this.eventTree = new AVLTree<>();
        
        setTitle("AVL Scheduler - Welcome " + user.getUsername());
        setSize(900, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        // Load events from database
        loadEventsFromDatabase();
        
        // Create UI
        createMenuBar();
        createMainPanel();
        
        // Refresh display
        refreshEventTable();
        updateStats();
    }
    
    private void createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        
        JMenu fileMenu = new JMenu("File");
        JMenuItem logoutItem = new JMenuItem("Logout");
        JMenuItem exitItem = new JMenuItem("Exit");
        
        logoutItem.addActionListener(e -> logout());
        exitItem.addActionListener(e -> System.exit(0));
        
        fileMenu.add(logoutItem);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);
        
        JMenu helpMenu = new JMenu("Help");
        JMenuItem aboutItem = new JMenuItem("About");
        aboutItem.addActionListener(e -> showAbout());
        helpMenu.add(aboutItem);
        
        menuBar.add(fileMenu);
        menuBar.add(helpMenu);
        setJMenuBar(menuBar);
    }
    
    private void createMainPanel() {
        setLayout(new BorderLayout(10, 10));
        
        // Stats panel (top)
        JPanel topPanel = new JPanel();
        statsLabel = new JLabel();
        statsLabel.setFont(new Font("Arial", Font.BOLD, 14));
        topPanel.add(statsLabel);
        add(topPanel, BorderLayout.NORTH);
        
        // Button panel (left)
        JPanel buttonPanel = new JPanel(new GridLayout(7, 1, 10, 10));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JButton addBtn = createButton("➕ Add Event");
        JButton deleteBtn = createButton("🗑️ Delete Event");
        JButton nextBtn = createButton("⏭️ Find Next Event");
        JButton filterBtn = createButton("🔍 Filter by Date");
        JButton refreshBtn = createButton("🔄 Refresh");
        JButton statsBtn = createButton("📊 Statistics");
        JButton viewAllBtn = createButton("📋 View All Events");
        
        addBtn.addActionListener(e -> addEvent());
        deleteBtn.addActionListener(e -> deleteEvent());
        nextBtn.addActionListener(e -> findNextEvent());
        filterBtn.addActionListener(e -> filterByDate());
        refreshBtn.addActionListener(e -> refreshAll());
        statsBtn.addActionListener(e -> showFullStats());
        viewAllBtn.addActionListener(e -> refreshEventTable());
        
        buttonPanel.add(addBtn);
        buttonPanel.add(deleteBtn);
        buttonPanel.add(nextBtn);
        buttonPanel.add(filterBtn);
        buttonPanel.add(viewAllBtn);
        buttonPanel.add(statsBtn);
        buttonPanel.add(refreshBtn);
        
        add(buttonPanel, BorderLayout.WEST);
        
        // Table panel (center)
        String[] columns = {"ID", "Title", "Date", "Time", "Duration (min)", "End Time"};
        tableModel = new DefaultTableModel(columns, 0);
        eventTable = new JTable(tableModel);
        eventTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        JScrollPane scrollPane = new JScrollPane(eventTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Your Events (Sorted by Date & Time)"));
        add(scrollPane, BorderLayout.CENTER);
    }
    
    private JButton createButton(String text) {
        JButton btn = new JButton(text);
        btn.setPreferredSize(new Dimension(180, 40));
        return btn;
    }
    
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
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading events: " + e.getMessage());
        }
    }
    
    private void refreshEventTable() {
        tableModel.setRowCount(0);
        List<Event> events = eventTree.inorder();
        
        for (Event event : events) {
            Object[] row = {
                event.getId(),
                event.getTitle(),
                event.getDate().format(DATE_FORMAT),
                event.getTime().format(TIME_FORMAT),
                event.getDuration(),
                event.getEndTime().format(TIME_FORMAT)
            };
            tableModel.addRow(row);
        }
    }
    
    private void updateStats() {
        statsLabel.setText(String.format("📅 Total Events: %d | 🌳 AVL Tree Height: %d",
                          eventTree.size(), eventTree.getHeight()));
    }
    
    private void addEvent() {
        JTextField titleField = new JTextField();
        JTextField dateField = new JTextField(LocalDate.now().format(DATE_FORMAT));
        JTextField timeField = new JTextField("09:00");
        JComboBox<Integer> durationBox = new JComboBox<>(new Integer[]{30, 60, 90, 120});
        
        Object[] fields = {
            "Title:", titleField,
            "Date (YYYY-MM-DD):", dateField,
            "Time (HH:MM):", timeField,
            "Duration (minutes):", durationBox
        };
        
        int option = JOptionPane.showConfirmDialog(this, fields, "Add New Event",
                                                    JOptionPane.OK_CANCEL_OPTION);
        
        if (option == JOptionPane.OK_OPTION) {
            try {
                String title = titleField.getText().trim();
                LocalDate date = LocalDate.parse(dateField.getText().trim(), DATE_FORMAT);
                LocalTime time = LocalTime.parse(timeField.getText().trim());
                int duration = (Integer) durationBox.getSelectedItem();
                
                if (title.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Please enter a title!");
                    return;
                }
                
                // Create temp event for conflict check
                Event tempEvent = new Event(0, title, date, time, duration);
                
                // Check for conflicts
                if (eventTree.checkConflict(tempEvent, duration)) {
                    int confirm = JOptionPane.showConfirmDialog(this,
                        "⚠️ Time conflict detected! Continue anyway?",
                        "Conflict Warning",
                        JOptionPane.YES_NO_OPTION);
                    if (confirm != JOptionPane.YES_OPTION) return;
                }
                
                // Insert into database
                try (Connection conn = DatabaseConnection.getConnection()) {
                    String sql = "INSERT INTO events (user_id, title, event_date, event_time, duration) VALUES (?, ?, ?, ?, ?)";
                    PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
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
                }
                
            } catch (DateTimeParseException e) {
                JOptionPane.showMessageDialog(this, "Invalid date/time format! Use YYYY-MM-DD and HH:MM");
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Database error: " + e.getMessage());
            }
        }
    }
    
    private void deleteEvent() {
        int selectedRow = eventTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an event to delete!");
            return;
        }
        
        int eventId = (int) tableModel.getValueAt(selectedRow, 0);
        String title = (String) tableModel.getValueAt(selectedRow, 1);
        
        int confirm = JOptionPane.showConfirmDialog(this,
            "Delete event: " + title + "?",
            "Confirm Delete",
            JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection conn = DatabaseConnection.getConnection()) {
                String sql = "DELETE FROM events WHERE id = ? AND user_id = ?";
                PreparedStatement pstmt = conn.prepareStatement(sql);
                pstmt.setInt(1, eventId);
                pstmt.setInt(2, currentUser.getId());
                
                pstmt.executeUpdate();
                
                // Remove from AVL tree (find event by ID - simplified)
                List<Event> events = eventTree.inorder();
                for (Event e : events) {
                    if (e.getId() == eventId) {
                        eventTree.delete(e);
                        break;
                    }
                }
                
                JOptionPane.showMessageDialog(this, "✅ Event deleted!");
                refreshEventTable();
                updateStats();
                
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
            }
        }
    }
    
    private void findNextEvent() {
        Event next = eventTree.findMin();
        if (next != null) {
            JOptionPane.showMessageDialog(this,
                "⏰ Next Event:\n" +
                "Title: " + next.getTitle() + "\n" +
                "Date: " + next.getDate().format(DATE_FORMAT) + "\n" +
                "Time: " + next.getTime().format(TIME_FORMAT) + "\n" +
                "Duration: " + next.getDuration() + " minutes",
                "Next Event",
                JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "No events scheduled!");
        }
    }
    
    private void filterByDate() {
        String dateStr = JOptionPane.showInputDialog(this,
            "Enter date (YYYY-MM-DD):\nExample: 2024-03-25",
            "Filter by Date",
            JOptionPane.QUESTION_MESSAGE);
        
        if (dateStr != null && !dateStr.trim().isEmpty()) {
            try {
                LocalDate filterDate = LocalDate.parse(dateStr.trim(), DATE_FORMAT);
                LocalDate nextDay = filterDate.plusDays(1);
                
                List<Event> filtered = eventTree.rangeSearch(
                    new Event(0, "", filterDate, LocalTime.MIN, 0),
                    new Event(0, "", nextDay, LocalTime.MIN, 0)
                );
                
                if (filtered.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "No events on " + dateStr);
                } else {
                    StringBuilder sb = new StringBuilder();
                    sb.append("Events on ").append(dateStr).append(":\n\n");
                    for (Event e : filtered) {
                        sb.append("• ").append(e.getTime().format(TIME_FORMAT))
                            .append(" - ").append(e.getTitle())
                            .append(" (").append(e.getDuration()).append(" min)\n");
                    }
                    JOptionPane.showMessageDialog(this, sb.toString());
                }
                
            } catch (DateTimeParseException e) {
                JOptionPane.showMessageDialog(this, "Invalid date format!");
            }
        }
    }
    
    private void refreshAll() {
        eventTree = new AVLTree<>();
        loadEventsFromDatabase();
        refreshEventTable();
        updateStats();
        JOptionPane.showMessageDialog(this, "Refreshed!");
    }
    
    private void showFullStats() {
        List<Event> events = eventTree.inorder();
        if (events.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No events to analyze!");
            return;
        }
        
        // Calculate statistics
        int totalEvents = events.size();
        int totalMinutes = events.stream().mapToInt(Event::getDuration).sum();
        double avgDuration = (double) totalMinutes / totalEvents;
        
        // Find busiest day
        java.util.Map<LocalDate, Integer> dayCount = new java.util.HashMap<>();
        for (Event e : events) {
            dayCount.put(e.getDate(), dayCount.getOrDefault(e.getDate(), 0) + 1);
        }
        LocalDate busiestDay = dayCount.entrySet().stream()
            .max(java.util.Map.Entry.comparingByValue())
            .map(java.util.Map.Entry::getKey)
            .orElse(null);
        
        String stats = String.format(
            "📊 Schedule Statistics 📊\n\n" +
            "Total Events: %d\n" +
            "Total Time: %d minutes (%.1f hours)\n" +
            "Average Duration: %.1f minutes\n" +
            "Busiest Day: %s (%d events)\n" +
            "AVL Tree Height: %d\n" +
            "AVL Tree Operations: O(log n) performance!",
            totalEvents, totalMinutes, totalMinutes / 60.0,
            avgDuration,
            busiestDay != null ? busiestDay.format(DATE_FORMAT) : "N/A",
            busiestDay != null ? dayCount.get(busiestDay) : 0,
            eventTree.getHeight()
        );
        
        JOptionPane.showMessageDialog(this, stats, "Statistics", JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void logout() {
        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to logout?",
            "Logout",
            JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            new LoginGUI().setVisible(true);
            dispose();
        }
    }
    
    private void showAbout() {
        JOptionPane.showMessageDialog(this,
            "AVL Event Scheduler\n\n" +
            "Features powered by AVL Tree:\n" +
            "• O(log n) insert/delete\n" +
            "• O(log n) find minimum (next event)\n" +
            "• O(log n) range search (date filters)\n" +
            "• O(log n) conflict detection\n\n" +
            "All events are automatically sorted by date & time!",
            "About",
            JOptionPane.INFORMATION_MESSAGE);
    }
}