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

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Meta-data for a table.
 * 
 * @author Daniel
 */
public class TableMetaData {

	/**
	 * Prefix of the package name.
	 */
	private final String packagePrefix;

	/**
	 * Catalog name.
	 */
	private final String catalogName;

	/**
	 * Schema name.
	 */
	private final String schemaName;

	/**
	 * Table name.
	 */
	private final String tableName;

	/**
	 * Columns.
	 */
	private final ColumnMetaData[] columns;

	/**
	 * Primary key of this table.
	 */
	private final IndexMetaData primaryKey;

	/**
	 * Indexes on this table.
	 */
	private final IndexMetaData[] indexes;

	/**
	 * Cross references.
	 */
	private CrossReferenceMetaData[] crossReferences;

	/**
	 * Accesses on this table.
	 */
	private final Map<AccessMetaData, AccessMetaData> accesses = new HashMap<AccessMetaData, AccessMetaData>();

	/**
	 * Initiate.
	 * 
	 * @param packagePrefix
	 *            Prefix of the package name.
	 * @param catalogName
	 *            Catalog name.
	 * @param schemaName
	 *            Schema name.
	 * @param tableName
	 *            Table name.
	 * @param columns
	 *            {@link ColumnMetaData} instances.
	 * @param primaryKeyColumnNames
	 *            Names of primary key columns.
	 * @param indexNameAndColumnNames
	 *            Indexes on this table.
	 * @param indexUniqueness
	 *            Provides whether index is unique
	 */
	public TableMetaData(String packagePrefix, String catalogName,
			String schemaName, String tableName, ColumnMetaData[] columns,
			String[] primaryKeyColumnNames,
			Map<String, List<String>> indexNameAndColumnNames,
			Map<String, Boolean> indexUniqueness) {
		this.packagePrefix = packagePrefix;
		this.catalogName = catalogName;
		this.schemaName = schemaName;
		this.tableName = tableName;
		this.columns = columns;
		this.primaryKey = new IndexMetaData(this, "PRIMARY_KEY", this
				.getColumnsByName(primaryKeyColumnNames), true);

		// Obtain the names of the indexes
		String[] indexNames = indexNameAndColumnNames.keySet().toArray(
				new String[0]);

		// Load the index meta-data
		this.indexes = new IndexMetaData[indexNames.length];
		for (int i = 0; i < this.indexes.length; i++) {

			// Obtain the index details
			String indexName = indexNames[i];
			String[] indexColumnNames = indexNameAndColumnNames.get(indexName)
					.toArray(new String[0]);
			boolean isUnique = indexUniqueness.get(indexName);

			// Create and set the index meta-data
			this.indexes[i] = new IndexMetaData(this, indexName, this
					.getColumnsByName(indexColumnNames), isUnique);
		}

		// Load the access meta-data
		List<IndexMetaData> allIndexes = new LinkedList<IndexMetaData>();
		allIndexes.add(this.primaryKey);
		allIndexes.addAll(Arrays.asList(this.indexes));
		for (IndexMetaData index : allIndexes) {
			// Load the access
			for (int c = 1; c <= index.getColumns().length; c++) {

				// Determine if override to be non-unique
				boolean isUnique = (c == index.getColumns().length);
				isUnique &= index.isUnique();

				// Create the listing of columns
				ColumnMetaData[] accessColumns = new ColumnMetaData[c];
				for (int i = 0; i < accessColumns.length; i++) {
					accessColumns[i] = index.getColumns()[i];
				}

				// Create the access
				AccessMetaData access = new AccessMetaData(this, "Access",
						accessColumns, isUnique);

				// Determine if access is already registered
				if (isUnique) {
					// Override to indicate unique access
					this.accesses.put(access, access);
				} else {
					// Determine contains access
					if (!this.accesses.containsKey(access)) {
						// Add as not yet provided
						this.accesses.put(access, access);
					}
				}
			}
		}
	}

	/**
	 * Initiate the {@link TableMetaData} as a key into a {@link Map}.
	 * 
	 * @param packagePrefix
	 *            Prefix of the package name.
	 * @param catalogName
	 *            Catalog name.
	 * @param schemaName
	 *            Schema name.
	 * @param tableName
	 *            Table name.
	 */
	protected TableMetaData(String packagePrefix, String catalogName,
			String schemaName, String tableName) {
		this.packagePrefix = packagePrefix;
		this.catalogName = catalogName;
		this.schemaName = schemaName;
		this.tableName = tableName;
		this.columns = null;
		this.primaryKey = null;
		this.indexes = null;
	}

	/**
	 * Obtains the catalog name.
	 * 
	 * @return Catalog name.
	 */
	public String getCatalogName() {
		return this.catalogName;
	}

	/**
	 * Obtains the schema name.
	 * 
	 * @return Schema name.
	 */
	public String getSchemaName() {
		return this.schemaName;
	}

	/**
	 * Obtains the table name.
	 * 
	 * @return Table name.
	 */
	public String getTableName() {
		return this.tableName;
	}

	/**
	 * Obtains the {@link ColumnMetaData} of this table.
	 * 
	 * @return {@link ColumnMetaData} of this table.
	 */
	public ColumnMetaData[] getColumns() {
		return this.columns;
	}

	/**
	 * Obtains the {@link IndexMetaData} of the primary key.
	 * 
	 * @return {@link IndexMetaData} of the primary key.
	 */
	public IndexMetaData getPrimaryKey() {
		return this.primaryKey;
	}

	/**
	 * Obtains the {@link IndexMetaData} of this table.
	 * 
	 * @return {@link IndexMetaData} of this table.
	 */
	public IndexMetaData[] getIndexes() {
		return this.indexes;
	}

	/**
	 * Obtains the package name of this table.
	 * 
	 * @return Package name of this table.
	 */
	public String getPackageName() {
		return FilingCabinetUtil.getPackageName(this.packagePrefix,
				this.catalogName, this.schemaName, this.tableName);
	}

	/**
	 * Obtains the class name of this table.
	 * 
	 * @return Class name of this table.
	 */
	public String getSimpleClassName() {
		String className = FilingCabinetUtil.getSimpleClassName(this.tableName);
		return className;
	}

	/**
	 * Obtains the fully qualified class name of this table.
	 * 
	 * @return Fully qualified class name of this table.
	 */
	public String getFullyQualifiedClassName() {
		String name = this.getPackageName();
		name = (name.length() > 0 ? name + "." : "")
				+ this.getSimpleClassName();
		return name;
	}

	/**
	 * Obtains the {@link CrossReferenceMetaData} instances that this table is
	 * involved in.
	 * 
	 * @return {@link CrossReferenceMetaData} instances that this table is
	 *         involved in.
	 */
	public CrossReferenceMetaData[] getCrossReferences() {
		return this.crossReferences;
	}

	/**
	 * Obtains the {@link AccessMetaData} instances to access this table.
	 * 
	 * @return {@link AccessMetaData} instances to access this table.
	 */
	public AccessMetaData[] getAccesses() {
		return this.accesses.keySet().toArray(new AccessMetaData[0]);
	}

	/**
	 * Obtains the {@link AccessMetaData} for the {@link ColumnMetaData}
	 * combination.
	 * 
	 * @param columns
	 *            {@link ColumnMetaData} instances in order.
	 * @return {@link AccessMetaData}.
	 */
	public AccessMetaData getAccess(ColumnMetaData... columns) {

		// Create the access key
		AccessMetaData key = new AccessMetaData(this, "Key", columns, false);

		// Obtain and return the access
		AccessMetaData access = this.accesses.get(key);
		return access;
	}

	/**
	 * Specifies the {@link CrossReferenceMetaData} instances that this table is
	 * involved in.
	 * 
	 * @param crossReferences
	 *            {@link CrossReferenceMetaData} instances that this table is
	 *            involved in.
	 */
	protected void setCrossReferences(CrossReferenceMetaData[] crossReferences) {
		this.crossReferences = crossReferences;
	}

	/**
	 * Obtains the listing of {@link ColumnMetaData} in the order of the input
	 * names.
	 * 
	 * @param names
	 *            Names of the columns.
	 * @return Listing of corresponding {@link ColumnMetaData} instances.
	 */
	protected ColumnMetaData[] getColumnsByName(String... names) {
		ColumnMetaData[] columns = new ColumnMetaData[names.length];
		for (int i = 0; i < columns.length; i++) {
			columns[i] = this.getColumnByName(names[i]);
		}
		return columns;
	}

	/**
	 * Obtains the {@link ColumnMetaData} by its name.
	 * 
	 * @param name
	 *            Name of the column.
	 * @return {@link ColumnMetaData}.
	 * @throws IllegalStateException
	 *             If not column by the name.
	 */
	private ColumnMetaData getColumnByName(String name)
			throws IllegalStateException {

		// Obtain and return the column
		for (ColumnMetaData column : this.columns) {
			if (name.equals(column.getColumnName())) {
				// Found the column
				return column;
			}
		}

		// If here, no column by name
		throw new IllegalStateException("No column '" + name + "' on table "
				+ this.tableName);
	}
}
