package svp.lock_service.common;

import java.sql.*;

public class TableUtils {
    private static String driverName = "org.apache.hive.jdbc.HiveDriver";

    public static boolean isTableExists(String tableName) throws SQLException {
        try {
            Class.forName(driverName);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            System.exit(1);
        }
        Connection con = DriverManager.getConnection("jdbc:hive2://localhost:10000/default", "students", "students");
        DatabaseMetaData metadata = con.getMetaData();
        ResultSet res = metadata.getTables(null, null, tableName, null);
        return res.next();
    }

}
