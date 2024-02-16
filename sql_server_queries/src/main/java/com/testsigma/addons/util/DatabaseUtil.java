package com.testsigma.addons.util;


import java.sql.Connection;
import java.sql.DriverManager;

public class DatabaseUtil {

	public Connection getConnection(String dbURL) throws Exception {
		Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
		Connection con = DriverManager.getConnection(dbURL);
		return con;
	}
}
