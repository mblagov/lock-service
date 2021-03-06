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
    }

    public boolean isTableExists(String fullTableName) throws SQLException {
        String[] tableNameParts = fullTableName.split("\\.");
        String database = tableNameParts[0];
        String tableName = tableNameParts[1];
        getConnection(database);
        DatabaseMetaData metadata = connection.getMetaData();
        ResultSet res = metadata.getTables(null, database, tableName, null);
        boolean ans = res.next();
        connection.close();
        return ans;
    }

    private void getConnection(String database) throws SQLException {
        connection = DriverManager.getConnection("jdbc:hive2://localhost:10000/" + database, "students", "students");
    }

}
