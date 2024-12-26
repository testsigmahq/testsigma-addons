package com.testsigma.addons.mysql.util;


import java.sql.Connection;
import java.sql.DriverManager;

public class DatabaseUtil {
	private String dbClass = "com.mysql.jdbc.Driver";
	public Connection getConnection(String dbURL) throws Exception {

		Class.forName(dbClass).getDeclaredConstructor().newInstance();
		Connection con = DriverManager.getConnection(dbURL);
		return con;
	}
}
