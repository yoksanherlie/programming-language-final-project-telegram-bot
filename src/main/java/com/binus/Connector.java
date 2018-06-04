package com.binus;

import java.sql.Connection;
import java.sql.DriverManager;

public class Connector {

    private static Connection connection;

    public static Connection connectDB() {
        if (connection == null) {
            try {
                String db = "jdbc:mysql://localhost:3306/pl_fp";
                String user = "root";
                String pass = "ganteng";
                DriverManager.registerDriver(new com.mysql.jdbc.Driver());
                connection = (Connection) DriverManager.getConnection(db, user, pass);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return connection;
    }
}
