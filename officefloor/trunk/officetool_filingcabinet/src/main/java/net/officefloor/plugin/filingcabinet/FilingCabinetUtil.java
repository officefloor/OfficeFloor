/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2009 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.officefloor.plugin.filingcabinet;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Utility methods.
 * 
 * @author Daniel Sagenschneider
 */
class FilingCabinetUtil {

	/**
	 * Obtains the simple name of the {@lik Class} from the input table name.
	 * 
	 * @param tableName
	 *            Name of the table.
	 * @return Simple name of the corresponding {@link Class}.
	 */
	public static String getSimpleClassName(String tableName) {

		// Remove any sequential '_'
		while (tableName.contains("__")) {
			tableName.replace("__", "_");
		}

		// Split table name by '_' to find name parts
		String[] nameParts = tableName.split("_");

		// Make the name parts ready for java case
		for (int i = 0; i < nameParts.length; i++) {
			String namePart = nameParts[i];

			// Make name part start with upper and remaining lower
			namePart = namePart.substring(0, 1).toUpperCase()
					+ namePart.substring(1).toLowerCase();

			// Overwrite name part with formatted part
			nameParts[i] = namePart;
		}

		// Create the simple class name
		StringBuilder simpleClassName = new StringBuilder();
		for (String namePart : nameParts) {
			simpleClassName.append(namePart == null ? "" : namePart);
		}

		// Return the simple class name
		return simpleClassName.toString();
	}

	/**
	 * Creates the package name from the input package parts.
	 * 
	 * @param packageParts
	 *            Parts of the package name.
	 * @return Package name.
	 */
	public static String getPackageName(String... packageParts) {

		// Normalise the parts of the package
		List<String> packageNames = new LinkedList<String>();
		for (String packagePart : packageParts) {
			// Ignore if null
			if (packagePart == null) {
				continue;
			}

			// Package name is always lower case
			packagePart = packagePart.toLowerCase();

			// Remove any '_'
			packagePart = packagePart.replace("_", "");

			// Determine if a java keyword
			if (isJavaKeyword(packagePart)) {
				// Make it a non-java keyword
				packagePart += "_";
			}

			// Add the package part
			packageNames.add(packagePart);
		}

		// Construct the package name
		StringBuilder packageName = new StringBuilder();
		boolean isFirst = true;
		for (String packagePart : packageNames) {
			packageName.append(isFirst ? "" : ".");
			isFirst = false;
			packageName.append(packagePart);
		}

		// Return the package name
		return packageName.toString();
	}

	/**
	 * Obtains the get method name for the input column name.
	 * 
	 * @param columnName
	 *            Name of the column.
	 * @return Get method name.
	 */
	public static String getGetMethodName(String columnName) {
		return "get" + getSimpleClassName(columnName);
	}

	/**
	 * Obtains the set method name for the input column name.
	 * 
	 * @param columnName
	 *            Name of the column.
	 * @return Set method name.
	 */
	public static String getSetMethodName(String columnName) {
		return "set" + getSimpleClassName(columnName);
	}

	/**
	 * Obtains the field name for the input column name.
	 * 
	 * @param columnName
	 *            Name of the column.
	 * @return Field name.
	 */
	public static String getFieldName(String columnName) {
		// Transform to java casing
		String fieldName = getSimpleClassName(columnName);

		// Lower the case of the first letter
		fieldName = fieldName.substring(0, 1).toLowerCase()
				+ fieldName.substring(1);

		// Ensure not java keyword
		if (isJavaKeyword(fieldName)) {
			// Make a non-java keyword
			fieldName += "_";
		}

		// Return the field name
		return fieldName;
	}

	/**
	 * Determines if the input name is a Java keyword.
	 * 
	 * @param word
	 *            Work to determine if a Java keyword.
	 * @return <code>true</code> if a Java keyword.
	 */
	public static boolean isJavaKeyword(String word) {

		// Create the set of java keywords
		Set<String> javaKeywords = new HashSet<String>(Arrays.asList("public"));

		// Return if a java keyword
		return javaKeywords.contains(word);
	}

	/**
	 * Displays the input {@link ResultSet}.
	 * 
	 * @param headerText
	 *            Header text.
	 * @param resultSet
	 *            {@link ResultSet}.
	 */
	public static void displayResultSet(String headerText, ResultSet resultSet)
			throws Exception {

		// Write the header text
		System.out.println(headerText);

		// Obtain the meta-data
		ResultSetMetaData metaData = resultSet.getMetaData();

		// Obtain meta-data details
		String[] labels = new String[metaData.getColumnCount()];
		for (int i = 1; i <= labels.length; i++) {
			labels[i - 1] = metaData.getColumnLabel(i);
		}

		// Create the results
		List<Map<String, String>> results = new LinkedList<Map<String, String>>();

		// Include the headers
		Map<String, String> headers = new HashMap<String, String>();
		results.add(headers);
		for (String label : labels) {
			headers.put(label, label);
		}

		// Load the results
		while (resultSet.next()) {

			// Add then row entry
			Map<String, String> entry = new HashMap<String, String>();
			results.add(entry);

			// Add then values
			for (int i = 0; i < labels.length; i++) {

				// Obtain the value
				Object value = resultSet.getObject(labels[i]);
				if (value == null) {
					value = "[null]";
				}

				// Add the value
				String valueText = value.toString();
				entry.put(labels[i], valueText);
			}
		}

		// Obtain the max widths
		int[] widths = new int[labels.length];
		for (int i = 0; i < widths.length; i++) {
			widths[i] = Integer.MIN_VALUE;
		}
		for (Map<String, String> entry : results) {
			for (int i = 0; i < labels.length; i++) {
				String value = entry.get(labels[i]);
				if (value.length() > widths[i]) {
					widths[i] = value.length();
				}
			}
		}

		// Write the results
		for (Map<String, String> entry : results) {
			System.out.print("|");
			for (int i = 0; i < labels.length; i++) {

				// Obtain value
				String value = entry.get(labels[i]);

				// Pad value
				for (int j = value.length(); j < widths[i]; j++) {
					value += " ";
				}

				// Write the value
				System.out.print(value + "|");
			}
			System.out.println();
		}
	}

	/**
	 * All access via static methods.
	 */
	private FilingCabinetUtil() {
	}
}
