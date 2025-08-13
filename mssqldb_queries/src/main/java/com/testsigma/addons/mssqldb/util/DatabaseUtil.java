package com.testsigma.addons.mssqldb.util;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openqa.selenium.NoSuchElementException;

import com.testsigma.sdk.Result;

public class DatabaseUtil {
	private String dbClass = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
	public Connection getConnection(String dbURL) throws Exception {

		Class.forName(dbClass).getDeclaredConstructor().newInstance();
		Connection con = DriverManager.getConnection(dbURL);
		return con;
	}

}
