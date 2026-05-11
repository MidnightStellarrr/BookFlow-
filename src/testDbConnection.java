import java.sql.*;

public class testDbConnection  
{
    public static void main(String[] args)
    {
        try
        {
            Class.forName("com.mysql.cj.jdbc.Driver");
            
            Connection con = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/bookflow_db", 
                "root",      
                ""           
            );
            
            Statement stmt = con.createStatement();
            
            ResultSet rs = stmt.executeQuery("SELECT * FROM users");
            
            while(rs.next())
            {
                // Get actual data from each column
                System.out.println(rs.getString("username"));  
            }           
            con.close();
        }
        catch (Exception e)  
        {
            System.out.println(e);
        }
    }
}