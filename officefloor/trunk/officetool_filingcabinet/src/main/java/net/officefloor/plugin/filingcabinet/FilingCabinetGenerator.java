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
	private Map<String, TableMetaData> tables = new HashMap<String, TableMetaData>();

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
			List<String> primaryKey = new LinkedList<String>();
			String primaryKeyName = "";
			ResultSet primaryKeyResultSet = databaseMetaData.getPrimaryKeys(
					catalogName, schemaName, tableName);
			while (primaryKeyResultSet.next()) {

				// Obtain the name of the primary key
				primaryKeyName = primaryKeyResultSet.getString("PK_NAME");

				// Obtain the details of the column of the primary key
				String columnName = primaryKeyResultSet
						.getString("COLUMN_NAME");

				// Add the column of the primary key
				primaryKey.add(columnName);
			}
			primaryKeyResultSet.close();

			// Obtain the indexes
			Map<String, List<String>> indexColumns = new HashMap<String, List<String>>();
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

				// Indicate whether the index is unique
				indexUniqueness.put(indexName, new Boolean(isUnique));

				// Add the index details for this row
				List<String> indexColumnNames = indexColumns.get(indexName);
				if (indexColumnNames == null) {
					indexColumnNames = new LinkedList<String>();
					indexColumns.put(indexName, indexColumnNames);
				}
				indexColumnNames.add(columnName);
			}
			indexResultSet.close();

			// Create the table
			TableMetaData tableMetaData = new TableMetaData(catalogName,
					schemaName, tableName, columns
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
		Map<String, List<String>> primaryColumns = new HashMap<String, List<String>>();
		Map<String, TableMetaData> foreignTables = new HashMap<String, TableMetaData>();
		Map<String, List<String>> foreignColumns = new HashMap<String, List<String>>();
		ResultSet crossReferenceResultSet = databaseMetaData.getCrossReference(
				null, null, null, null, null, null);
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

			// Add the foreign key name
			foreignKeyNames.add(foreignKeyName);

			// Add the primary detail
			this.addCrossReferenceEndPointDetails(foreignKeyName,
					primaryCatalogName, primarySchemaName, primaryTableName,
					primaryColumnName, primaryTables, primaryColumns);

			// Add the second detail
			this.addCrossReferenceEndPointDetails(foreignKeyName,
					foreignCatalogName, foreignSchemaName, foreignTableName,
					foreignColumnName, foreignTables, foreignColumns);

		}
		crossReferenceResultSet.close();

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
	 * @param tables
	 *            Map of {@link TableMetaData} to foreign key names.
	 * @param columns
	 *            Map of column names by foreign key name.
	 */
	private void addCrossReferenceEndPointDetails(String foreignKeyName,
			String catalogName, String schemaName, String tableName,
			String columnName, Map<String, TableMetaData> tables,
			Map<String, List<String>> columns) {

		// Register table under the foreign key
		TableMetaData table = this.tables.get(new TableMetaData(catalogName,
				schemaName, tableName).getFullyQualifiedClassName());
		if (table == null) {
			throw new IllegalStateException("Can not find table '" + tableName
					+ "' of foreign key " + foreignKeyName);
		}
		tables.put(foreignKeyName, table);

		// Add the column
		List<String> columnList = columns.get(foreignKeyName);
		if (columnList == null) {
			columnList = new LinkedList<String>();
			columns.put(foreignKeyName, columnList);
		}
		columnList.add(columnName);
	}
}
