package com.testsigma.addons.postgresql.util;


import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
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
	
	/**
	 * Checks if the input string is a file path
	 * @param input The input string to check
	 * @return true if it appears to be a file path, false otherwise
	 */
	public boolean isFilePath(String input) {
		if (input == null || input.trim().isEmpty()) {
			return false;
		}
		String trimmed = input.trim();
		// Check for common file path patterns
		return trimmed.startsWith("/") || 
		       trimmed.startsWith("./") || 
		       trimmed.startsWith("../") ||
		       (trimmed.length() > 2 && trimmed.contains(File.separator)) ||
		       (trimmed.length() > 3 && trimmed.matches("^[A-Za-z]:[/\\\\].*")) ||
		       new File(trimmed).exists();
	}
	
	/**
	 * Reads JSON content from a file or returns the input if it's already JSON text
	 * @param input File path or JSON text
	 * @return JSON content as string
	 * @throws IOException if file cannot be read
	 */
	public String readJsonFromFileOrText(String input) throws IOException {
		if (input == null || input.trim().isEmpty()) {
			return input;
		}
		String trimmed = input.trim();
		
		if (isFilePath(trimmed)) {
			// Try to read from file
			try {
				byte[] bytes = Files.readAllBytes(Paths.get(trimmed));
				return new String(bytes, "UTF-8");
			} catch (Exception e) {
				throw new IOException("Failed to read JSON from file: " + trimmed + " - " + e.getMessage(), e);
			}
		}
		// Return as-is if it's not a file path
		return trimmed;
	}
	
	/**
	 * Parses a JSON string to List of Maps
	 * Handles format: [{"key1":"value1","key2":"value2"},{"key1":"value3","key2":"value4"}]
	 * @param jsonString The JSON string to parse
	 * @return List of Maps representing the JSON array
	 * @throws Exception if JSON parsing fails
	 */
	public List<Map<String, Object>> parseJsonToList(String jsonString) throws Exception {
		List<Map<String, Object>> result = new ArrayList<>();
		if (jsonString == null || jsonString.trim().isEmpty()) {
			return result;
		}
		
		String normalized = normalizeJson(jsonString);
		if (!normalized.startsWith("[") || !normalized.endsWith("]")) {
			throw new IllegalArgumentException("JSON must be an array starting with [ and ending with ]");
		}
		
		// Remove outer brackets
		String content = normalized.substring(1, normalized.length() - 1).trim();
		if (content.isEmpty()) {
			return result; // Empty array
		}
		
		// Split by objects (simple approach - look for }{ pattern)
		List<String> objectStrings = new ArrayList<>();
		int depth = 0;
		int start = 0;
		boolean inString = false;
		boolean escapeNext = false;
		
		for (int i = 0; i < content.length(); i++) {
			char c = content.charAt(i);
			
			if (escapeNext) {
				escapeNext = false;
				continue;
			}
			
			if (c == '\\') {
				escapeNext = true;
				continue;
			}
			
			if (c == '"') {
				inString = !inString;
				continue;
			}
			
			if (inString) {
				continue;
			}
			
			if (c == '{') {
				if (depth == 0) {
					start = i;
				}
				depth++;
			} else if (c == '}') {
				depth--;
				if (depth == 0) {
					objectStrings.add(content.substring(start, i + 1));
				}
			}
		}
		
		// Parse each object
		for (String objStr : objectStrings) {
			Map<String, Object> map = parseJsonObject(objStr);
			result.add(map);
		}
		
		return result;
	}
	
	/**
	 * Parses a JSON object string to a Map
	 * Format: {"key1":"value1","key2":"value2"}
	 */
	private Map<String, Object> parseJsonObject(String objStr) throws Exception {
		Map<String, Object> map = new LinkedHashMap<>();
		if (objStr == null || objStr.trim().isEmpty() || !objStr.startsWith("{") || !objStr.endsWith("}")) {
			return map;
		}
		
		String content = objStr.substring(1, objStr.length() - 1).trim();
		if (content.isEmpty()) {
			return map; // Empty object
		}
		
		// Split by comma, but respect string boundaries
		List<String> pairs = new ArrayList<>();
		int depth = 0;
		int start = 0;
		boolean inString = false;
		boolean escapeNext = false;
		
		for (int i = 0; i < content.length(); i++) {
			char c = content.charAt(i);
			
			if (escapeNext) {
				escapeNext = false;
				continue;
			}
			
			if (c == '\\') {
				escapeNext = true;
				continue;
			}
			
			if (c == '"') {
				inString = !inString;
				continue;
			}
			
			if (inString) {
				continue;
			}
			
			if (c == '{' || c == '[') {
				depth++;
			} else if (c == '}' || c == ']') {
				depth--;
			} else if (c == ',' && depth == 0) {
				pairs.add(content.substring(start, i).trim());
				start = i + 1;
			}
		}
		// Add last pair
		if (start < content.length()) {
			pairs.add(content.substring(start).trim());
		}
		
		// Parse each key-value pair
		for (String pair : pairs) {
			if (pair.isEmpty()) continue;
			
			int colonIndex = pair.indexOf(':');
			if (colonIndex == -1) {
				throw new IllegalArgumentException("Invalid JSON pair: " + pair);
			}
			
			String keyStr = pair.substring(0, colonIndex).trim();
			String valueStr = pair.substring(colonIndex + 1).trim();
			
			// Remove quotes from key
			if (keyStr.startsWith("\"") && keyStr.endsWith("\"")) {
				keyStr = unescapeJson(keyStr.substring(1, keyStr.length() - 1));
			}
			
			// Parse value
			Object value = parseJsonValue(valueStr);
			map.put(keyStr, value);
		}
		
		return map;
	}
	
	/**
	 * Parses a JSON value (string, number, boolean, null, or object/array)
	 */
	private Object parseJsonValue(String valueStr) {
		if (valueStr == null || valueStr.trim().isEmpty()) {
			return null;
		}
		
		valueStr = valueStr.trim();
		
		// null
		if ("null".equals(valueStr)) {
			return null;
		}
		
		// boolean
		if ("true".equals(valueStr)) {
			return true;
		}
		if ("false".equals(valueStr)) {
			return false;
		}
		
		// string
		if (valueStr.startsWith("\"") && valueStr.endsWith("\"")) {
			return unescapeJson(valueStr.substring(1, valueStr.length() - 1));
		}
		
		// number
		try {
			if (valueStr.contains(".")) {
				return Double.parseDouble(valueStr);
			} else {
				return Long.parseLong(valueStr);
			}
		} catch (NumberFormatException e) {
			// Not a number, return as string
			return valueStr;
		}
	}
	
	/**
	 * Unescapes JSON string (reverse of escapeJson)
	 */
	private String unescapeJson(String str) {
		if (str == null) {
			return "";
		}
		return str.replace("\\\"", "\"")
		          .replace("\\\\", "\\")
		          .replace("\\b", "\b")
		          .replace("\\f", "\f")
		          .replace("\\n", "\n")
		          .replace("\\r", "\r")
		          .replace("\\t", "\t");
	}
	
	/**
	 * Checks if all expected rows are present in actual results
	 * Expected rows must be a subset of actual rows (actual can have extra columns/data)
	 * @param expectedRows The expected rows (subset)
	 * @param actualRows The actual rows from query (superset)
	 * @return true if all expected rows are found in actual rows
	 */
	public boolean containsAllRows(List<Map<String, Object>> expectedRows, List<Map<String, Object>> actualRows) {
		if (expectedRows == null || expectedRows.isEmpty()) {
			return true; // Empty expected means all found
		}
		
		if (actualRows == null || actualRows.isEmpty()) {
			return false; // Expected has rows but actual is empty
		}
		
		// For each expected row, check if it exists in actual rows
		for (Map<String, Object> expectedRow : expectedRows) {
			boolean found = false;
			
			// Check each actual row
			for (Map<String, Object> actualRow : actualRows) {
				if (rowContains(expectedRow, actualRow)) {
					found = true;
					break;
				}
			}
			
			if (!found) {
				return false; // Expected row not found in actual
			}
		}
		
		return true; // All expected rows found
	}
	
	/**
	 * Checks if actualRow contains all key-value pairs from expectedRow
	 * Actual row can have additional columns, but must contain all expected columns with matching values
	 */
	private boolean rowContains(Map<String, Object> expectedRow, Map<String, Object> actualRow) {
		if (expectedRow == null || expectedRow.isEmpty()) {
			return true; // Empty expected row matches any actual row
		}
		
		if (actualRow == null) {
			return false;
		}
		
		// Check each expected key-value pair
		for (Map.Entry<String, Object> expectedEntry : expectedRow.entrySet()) {
			String key = expectedEntry.getKey();
			Object expectedValue = expectedEntry.getValue();
			
			// Check if actual row has this key
			if (!actualRow.containsKey(key)) {
				return false; // Key not found in actual row
			}
			
			// Compare values (handle null and type conversion)
			Object actualValue = actualRow.get(key);
			if (!valuesMatch(expectedValue, actualValue)) {
				return false; // Values don't match
			}
		}
		
		return true; // All expected key-value pairs found and match
	}
	
	/**
	 * Compares two values for equality, handling type conversion
	 */
	private boolean valuesMatch(Object expected, Object actual) {
		if (expected == null && actual == null) {
			return true;
		}
		if (expected == null || actual == null) {
			return false;
		}
		
		// Direct equality
		if (expected.equals(actual)) {
			return true;
		}
		
		// String comparison (convert both to string and compare)
		String expectedStr = expected.toString();
		String actualStr = actual.toString();
		return expectedStr.equals(actualStr);
	}
}
