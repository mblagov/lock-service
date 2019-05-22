package svp.lock_service.common;

import java.sql.*;

public class HiveHelper {

    private static String driverName = "org.apache.hive.jdbc.HiveDriver";

    private Connection connection;

    public HiveHelper() throws SQLException {
        try {
            Class.forName(driverName);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            System.exit(1);
        }
        connection = DriverManager.getConnection("jdbc:hive2://localhost:10000/default", "students", "students");
    }

    public String getTables() throws SQLException {
        return connection.getMetaData().getTables(null, null, null, null).getString(1);
    }

    public boolean isTableExists(String tableName) throws SQLException {
        DatabaseMetaData metadata = connection.getMetaData();
        ResultSet res = metadata.getTables(null, null, tableName, null);
        return res.next();
    }

}
