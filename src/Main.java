import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        // Test database connection first
        if (!DatabaseConnection.testConnection()) {
            JOptionPane.showMessageDialog(null,
                "❌ Cannot connect to database!\n\n" +
                "Please ensure:\n" +
                "1. XAMPP MySQL is running\n" +
                "2. Database 'scheduler_db' exists\n" +
                "3. Tables are created\n\n" +
                "Run the SQL script first!",
                "Database Error",
                JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
        
        // Start the application
        SwingUtilities.invokeLater(() -> new LoginGUI().setVisible(true));
    }
}