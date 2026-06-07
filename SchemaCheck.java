import java.sql.*;
public class SchemaCheck {
  public static void main(String[] a) throws Exception {
    String url = System.getenv("DB_URL");
    String user = System.getenv("DB_USERNAME");
    String pass = System.getenv("DB_PASSWORD");
    try (Connection c = DriverManager.getConnection(url, user, pass);
         Statement s = c.createStatement();
         ResultSet r = s.executeQuery(\"SELECT column_name, data_type FROM information_schema.columns WHERE table_name='interviews' ORDER BY ordinal_position\")) {
      while (r.next()) System.out.println(r.getString(1)+\" | \"+r.getString(2));
    }
    System.out.println(\"--- interview_questions ---\");
    try (Connection c = DriverManager.getConnection(url, user, pass);
         Statement s = c.createStatement();
         ResultSet r = s.executeQuery(\"SELECT column_name, data_type FROM information_schema.columns WHERE table_name='interview_questions' ORDER BY ordinal_position\")) {
      while (r.next()) System.out.println(r.getString(1)+\" | \"+r.getString(2));
    }
  }
}
