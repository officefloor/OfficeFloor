/*
 *  Office Floor, Application Server
 *  Copyright (C) 2006 Daniel Sagenschneider
 *
 *  This program is free software; you can redistribute it and/or modify it under the terms 
 *  of the GNU General Public License as published by the Free Software Foundation; either 
 *  version 2 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program; 
 *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, 
 *  MA 02111-1307 USA
 */
package net.officefloor.plugin.filingcabinet;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Generates the Filing Cabinet meta-data.
 * 
 * @author Daniel
 */
public class FilingCabinetGenerator {

	/**
	 * {@link TableMetaData} by the fully qualified class name of the table.
	 */
	private final Map<String, TableMetaData> tables = new HashMap<String, TableMetaData>();

	/**
	 * Package prefix.
	 */
	private final String packagePrefix;

	/**
	 * Initiate.
	 * 
	 * @param packagePrefix
	 *            Package prefix. Use <code>null</code> if no prefix.
	 */
	public FilingCabinetGenerator(String packagePrefix) {
		this.packagePrefix = packagePrefix;
	}

	/**
	 * Loads the meta-data from the input {@link DatabaseMetaData}.
	 * 
	 * @param databaseMetaData
	 *            {@link DatabaseMetaData}.
	 * @param databaseAwareness
	 *            {@link DatabaseAwareness}.
	 * @throws Exception
	 *             If fails to load meta-data.
	 */
	public void loadMetaData(DatabaseMetaData databaseMetaData,
			DatabaseAwareness databaseAwareness) throws Exception {

		// Obtain the listing of tables
		ResultSet tableResultSet = databaseMetaData.getTables(null, null, null,
				null);
		while (tableResultSet.next()) {

			// Obtain details of the table
			String catalogName = tableResultSet.getString("TABLE_CAT");
			String schemaName = tableResultSet.getString("TABLE_SCHEM");
			String tableName = tableResultSet.getString("TABLE_NAME");

			// Ignore system tables of the database
			if (databaseAwareness.isSystemTable(catalogName, schemaName,
					tableName)) {
				// Ignore this as is a system table
				continue;
			}

			// Obtain the column meta-data
			List<ColumnMetaData> columns = new LinkedList<ColumnMetaData>();
			ResultSet columnResultSet = databaseMetaData.getColumns(
					catalogName, schemaName, tableName, null);
			while (columnResultSet.next()) {

				// Obtain the details of the column
				String columnName = columnResultSet.getString("COLUMN_NAME");
				int sqlType = columnResultSet.getInt("DATA_TYPE");
				String isNullableText = columnResultSet
						.getString("IS_NULLABLE");

				// Derive values for the column
				boolean isNullable = ("YES".equalsIgnoreCase(isNullableText));
				Class<?> javaType = databaseAwareness.getJavaType(sqlType);

				// Create the column meta-data
				ColumnMetaData columnMetaData = new ColumnMetaData(columnName,
						sqlType, javaType, isNullable);

				// Add the column
				columns.add(columnMetaData);
			}
			columnResultSet.close();

			// Obtain the primary keys
			Map<Integer, String> unorderedPrimaryKey = new HashMap<Integer, String>();
			String primaryKeyName = "";
			ResultSet primaryKeyResultSet = databaseMetaData.getPrimaryKeys(
					catalogName, schemaName, tableName);
			while (primaryKeyResultSet.next()) {

				// Obtain the name of the primary key
				primaryKeyName = primaryKeyResultSet.getString("PK_NAME");
				int keySeq = primaryKeyResultSet.getInt("KEY_SEQ");

				// Obtain the details of the column of the primary key
				String columnName = primaryKeyResultSet
						.getString("COLUMN_NAME");

				// Add the column of the primary key
				unorderedPrimaryKey.put(new Integer(keySeq), columnName);
			}
			primaryKeyResultSet.close();
			List<String> primaryKey = this.getSortedList(unorderedPrimaryKey);

			// Obtain the indexes
			Map<String, Map<Integer, String>> unorderedIndexColumns = new HashMap<String, Map<Integer, String>>();
			Map<String, Boolean> indexUniqueness = new HashMap<String, Boolean>();
			ResultSet indexResultSet = databaseMetaData.getIndexInfo(
					catalogName, schemaName, tableName, false, false);
			while (indexResultSet.next()) {

				// Obtain the name of the index
				String indexName = indexResultSet.getString("INDEX_NAME");

				// Do not include primary key as index
				if (indexName.equals(primaryKeyName)) {
					continue;
				}

				// Obtain the remaining details of the index
				String columnName = indexResultSet.getString("COLUMN_NAME");
				boolean isUnique = !indexResultSet.getBoolean("NON_UNIQUE");
				int ordinalPosition = indexResultSet.getInt("ORDINAL_POSITION");

				// Indicate whether the index is unique
				indexUniqueness.put(indexName, new Boolean(isUnique));

				// Add the index details for this row
				Map<Integer, String> indexColumnNames = unorderedIndexColumns
						.get(indexName);
				if (indexColumnNames == null) {
					indexColumnNames = new HashMap<Integer, String>();
					unorderedIndexColumns.put(indexName, indexColumnNames);
				}
				indexColumnNames.put(new Integer(ordinalPosition), columnName);
			}
			indexResultSet.close();

			// Order the columns of the indexes
			Map<String, List<String>> indexColumns = this
					.getSortedList(unorderedIndexColumns);

			// Create the table
			TableMetaData tableMetaData = new TableMetaData(this.packagePrefix,
					catalogName, schemaName, tableName, columns
							.toArray(new ColumnMetaData[0]), primaryKey
							.toArray(new String[0]), indexColumns,
					indexUniqueness);

			// Register the table
			this.tables.put(tableMetaData.getFullyQualifiedClassName(),
					tableMetaData);
		}
		tableResultSet.close();

		// Obtain the listing of cross references
		Set<String> foreignKeyNames = new HashSet<String>();
		Map<String, TableMetaData> primaryTables = new HashMap<String, TableMetaData>();
		Map<String, Map<Integer, String>> unorderedPrimaryColumns = new HashMap<String, Map<Integer, String>>();
		Map<String, TableMetaData> foreignTables = new HashMap<String, TableMetaData>();
		Map<String, Map<Integer, String>> unorderedForeignColumns = new HashMap<String, Map<Integer, String>>();

		// Load cross references for each table
		for (TableMetaData table : this.tables.values()) {

			try {

				// Load for current table
				ResultSet crossReferenceResultSet = databaseMetaData
						.getImportedKeys(table.getCatalogName(), table
								.getSchemaName(), table.getTableName());
				while (crossReferenceResultSet.next()) {

					// Obtain details of cross reference
					String foreignKeyName = crossReferenceResultSet
							.getString("FK_NAME");
					String primaryCatalogName = crossReferenceResultSet
							.getString("PKTABLE_CAT");
					String primarySchemaName = crossReferenceResultSet
							.getString("PKTABLE_SCHEM");
					String primaryTableName = crossReferenceResultSet
							.getString("PKTABLE_NAME");
					String primaryColumnName = crossReferenceResultSet
							.getString("PKCOLUMN_NAME");
					String foreignCatalogName = crossReferenceResultSet
							.getString("FKTABLE_CAT");
					String foreignSchemaName = crossReferenceResultSet
							.getString("FKTABLE_SCHEM");
					String foreignTableName = crossReferenceResultSet
							.getString("FKTABLE_NAME");
					String foreignColumnName = crossReferenceResultSet
							.getString("FKCOLUMN_NAME");
					int keySeq = crossReferenceResultSet.getInt("KEY_SEQ");

					// Add the foreign key name
					foreignKeyNames.add(foreignKeyName);

					// Add the primary detail
					this.addCrossReferenceEndPointDetails(foreignKeyName,
							primaryCatalogName, primarySchemaName,
							primaryTableName, primaryColumnName, keySeq,
							primaryTables, unorderedPrimaryColumns);

					// Add the second detail
					this.addCrossReferenceEndPointDetails(foreignKeyName,
							foreignCatalogName, foreignSchemaName,
							foreignTableName, foreignColumnName, keySeq,
							foreignTables, unorderedForeignColumns);

				}
				crossReferenceResultSet.close();

			} catch (Exception ex) {
				System.err.println("CROSS REFERENCES FOR TABLE FAILED");
				System.err.println("  " + table.getFullyQualifiedClassName()
						+ " [" + table.getTableName() + "]");
				ex.printStackTrace();
			}
		}

		// Order the columns of the cross references
		Map<String, List<String>> primaryColumns = this
				.getSortedList(unorderedPrimaryColumns);
		Map<String, List<String>> foreignColumns = this
				.getSortedList(unorderedForeignColumns);

		// Load the cross references
		List<CrossReferenceMetaData> crossReferences = new LinkedList<CrossReferenceMetaData>();
		for (String foreignKeyName : foreignKeyNames) {

			// Obtain the details of the cross reference
			TableMetaData primaryTable = primaryTables.get(foreignKeyName);
			List<String> primaryColumnList = primaryColumns.get(foreignKeyName);
			TableMetaData foreignTable = foreignTables.get(foreignKeyName);
			List<String> foreignColumnList = foreignColumns.get(foreignKeyName);

			// Create the cross reference
			CrossReferenceMetaData crossReference = new CrossReferenceMetaData(
					foreignKeyName, primaryTable, primaryColumnList
							.toArray(new String[0]), foreignTable,
					foreignColumnList.toArray(new String[0]));

			// Add the cross reference
			crossReferences.add(crossReference);
		}

		// Load cross references on the tables
		for (TableMetaData table : this.tables.values()) {
			List<CrossReferenceMetaData> tableCrossReferences = new LinkedList<CrossReferenceMetaData>();
			for (CrossReferenceMetaData crossReference : crossReferences) {
				if ((crossReference.getPrimaryTable() == table)
						|| (crossReference.getForeignTable() == table)) {
					tableCrossReferences.add(crossReference);
				}
			}
			table.setCrossReferences(tableCrossReferences
					.toArray(new CrossReferenceMetaData[0]));
		}
	}

	/**
	 * Obtains the listing of {@link TableMetaData}.
	 * 
	 * @return Listing of {@link TableMetaData}.
	 */
	public TableMetaData[] getTableMetaData() {
		return this.tables.values().toArray(new TableMetaData[0]);
	}

	/**
	 * Obtains the map of where the values have been sorted.
	 * 
	 * @param unorderedMap
	 *            Map of unordered list.
	 * @return Map of sorted list.
	 */
	public <K, T> Map<K, List<T>> getSortedList(
			Map<K, Map<Integer, T>> unorderedMap) {
		Map<K, List<T>> sortedMap = new HashMap<K, List<T>>();
		for (K key : unorderedMap.keySet()) {
			Map<Integer, T> unorderedList = unorderedMap.get(key);
			List<T> sortedList = this.getSortedList(unorderedList);
			sortedMap.put(key, sortedList);
		}
		return sortedMap;
	}

	/**
	 * Obtains the unordered items in sorted order.
	 * 
	 * @param unorderedItems
	 *            Unordered items.
	 * @return Items sorted.
	 */
	private <T> List<T> getSortedList(Map<Integer, T> unorderedItems) {
		List<T> sortedItems = new LinkedList<T>();
		int index = 1;
		for (;;) {

			// Obtain the next item
			T item = unorderedItems.get(new Integer(index++));
			if (item == null) {
				// At end of list, therefore return list
				return sortedItems;
			}

			// Add item in sorted ordered
			sortedItems.add(item);
		}
	}

	/**
	 * Adds the cross reference information.
	 * 
	 * @param foreignKeyName
	 *            Name of the foreign key.
	 * @param catalogName
	 *            Catalog name.
	 * @param schemaName
	 *            Schema name.
	 * @param tableName
	 *            Table name.
	 * @param columnName
	 *            Column name.
	 * @param keySeq
	 *            Key sequence of the column.
	 * @param tables
	 *            Map of {@link TableMetaData} to foreign key names.
	 * @param columns
	 *            Map of column names and index by foreign key name.
	 */
	private void addCrossReferenceEndPointDetails(String foreignKeyName,
			String catalogName, String schemaName, String tableName,
			String columnName, int keySeq, Map<String, TableMetaData> tables,
			Map<String, Map<Integer, String>> columns) {

		// Register table under the foreign key
		TableMetaData table = this.tables.get(new TableMetaData(
				this.packagePrefix, catalogName, schemaName, tableName)
				.getFullyQualifiedClassName());
		if (table == null) {
			throw new IllegalStateException("Can not find table '" + tableName
					+ "' of foreign key " + foreignKeyName);
		}
		tables.put(foreignKeyName, table);

		// Add the column
		Map<Integer, String> columnList = columns.get(foreignKeyName);
		if (columnList == null) {
			columnList = new HashMap<Integer, String>();
			columns.put(foreignKeyName, columnList);
		}
		columnList.put(new Integer(keySeq), columnName);
	}

}
