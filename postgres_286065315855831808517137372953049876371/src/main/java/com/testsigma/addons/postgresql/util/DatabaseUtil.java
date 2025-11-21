package com.testsigma.addons.postgresql.util;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DatabaseUtil {
	private String dbClass = "org.postgresql.Driver";
	
	public Connection getConnection(String dbURL) throws Exception {
		Class.forName(dbClass).getDeclaredConstructor().newInstance();
		Connection con = DriverManager.getConnection(dbURL);
		return con;
	}
	
	/**
	 * Converts a ResultSet to a List of Maps where each Map represents a row
	 * with column names as keys
	 */
	public List<Map<String, Object>> resultSetToList(ResultSet resultSet) throws Exception {
		ResultSetMetaData rsmd = resultSet.getMetaData();
		int columnCount = rsmd.getColumnCount();
		
		// Get column names
		List<String> columnNames = new ArrayList<>();
		for (int i = 1; i <= columnCount; i++) {
			columnNames.add(rsmd.getColumnName(i));
		}
		
		// Build result set as List of Maps (rows)
		List<Map<String, Object>> resultRows = new ArrayList<>();
		while (resultSet.next()) {
			Map<String, Object> row = new LinkedHashMap<>();
			for (int j = 1; j <= columnCount; j++) {
				String columnName = columnNames.get(j - 1);
				Object columnValue = resultSet.getObject(j);
				if (resultSet.wasNull()) {
					row.put(columnName, null);
				} else {
					row.put(columnName, columnValue);
				}
			}
			resultRows.add(row);
		}
		
		return resultRows;
	}
	
	/**
	 * Converts a List of Maps to JSON string format
	 * Format: [{"column1":"value1","column2":"value2"},{"column1":"value3","column2":"value4"}]
	 */
	public String convertToJson(List<Map<String, Object>> rows) {
		if (rows.isEmpty()) {
			return "[]";
		}
		
		StringBuilder json = new StringBuilder();
		json.append("[");
		
		for (int i = 0; i < rows.size(); i++) {
			if (i > 0) {
				json.append(",");
			}
			json.append("{");
			
			Map<String, Object> row = rows.get(i);
			int colIndex = 0;
			for (Map.Entry<String, Object> entry : row.entrySet()) {
				if (colIndex > 0) {
					json.append(",");
				}
				json.append("\"").append(escapeJson(entry.getKey())).append("\":");
				
				Object value = entry.getValue();
				if (value == null) {
					json.append("null");
				} else if (value instanceof String) {
					json.append("\"").append(escapeJson(value.toString())).append("\"");
				} else if (value instanceof Number || value instanceof Boolean) {
					json.append(value);
				} else {
					// For other types, convert to string
					json.append("\"").append(escapeJson(value.toString())).append("\"");
				}
				colIndex++;
			}
			
			json.append("}");
		}
		
		json.append("]");
		return json.toString();
	}
	
	/**
	 * Escapes special characters in JSON strings
	 */
	public String escapeJson(String str) {
		if (str == null) {
			return "";
		}
		return str.replace("\\", "\\\\")
		          .replace("\"", "\\\"")
		          .replace("\b", "\\b")
		          .replace("\f", "\\f")
		          .replace("\n", "\\n")
		          .replace("\r", "\\r")
		          .replace("\t", "\\t");
	}
	
	/**
	 * Normalizes JSON string by removing whitespace for comparison
	 */
	public String normalizeJson(String json) {
		if (json == null) {
			return "";
		}
		return json.replaceAll("\\s+", "").trim();
	}
	
	/**
	 * Masks sensitive information in database connection URLs for logging
	 * Masks passwords in JDBC URLs like: jdbc:postgresql://host:port/db?user=user&password=pass
	 */
	public String maskConnectionUrl(String url) {
		if (url == null || url.isEmpty()) {
			return url;
		}
		// Mask password in connection string
		return url.replaceAll("(?i)(password|pwd)=[^&;\\s]+", "$1=***");
	}
	
	/**
	 * Masks potentially sensitive data in JSON strings for logging
	 * Masks common sensitive field names like password, pwd, secret, token, etc.
	 */
	public String maskSensitiveJson(String json) {
		if (json == null || json.isEmpty()) {
			return json;
		}
		// Mask sensitive fields in JSON (password, pwd, secret, token, apiKey, etc.)
		return json.replaceAll("(?i)\"(password|pwd|secret|token|apikey|apikey|auth|credential)\"\\s*:\\s*\"[^\"]*\"", 
			"\"$1\":\"***\"");
	}
}
