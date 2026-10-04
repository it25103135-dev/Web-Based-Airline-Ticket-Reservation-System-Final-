package com.lankawings.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/** Shared infrastructure only: JDBC connection factory for the isolated Payment Management module. */
public final class DBConnection {
    private static final String DB_URL = setting("LW_DB_URL", "lw.db.url",
            "jdbc:sqlserver://localhost:1433;databaseName=LankaWingsPaymentDB;encrypt=true;trustServerCertificate=true;loginTimeout=10");
    private static final String DB_USER = setting("LW_DB_USER", "lw.db.user", "Lankawings");
    private static final String DB_PASSWORD = setting("LW_DB_PASSWORD", "lw.db.password", "Lankawings123");
    private DBConnection() {}
    static {
        try { Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver"); }
        catch (ClassNotFoundException e) { throw new ExceptionInInitializerError("Microsoft SQL Server JDBC driver was not found: " + e.getMessage()); }
    }
    private static String setting(String env, String prop, String fallback) {
        String v = System.getProperty(prop);
        if (v == null || v.isBlank()) v = System.getenv(env);
        return (v == null || v.isBlank()) ? fallback : v;
    }
    public static Connection getConnection() throws SQLException { return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD); }
}
