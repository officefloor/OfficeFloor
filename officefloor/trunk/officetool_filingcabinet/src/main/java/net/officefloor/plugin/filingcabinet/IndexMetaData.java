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

/**
 * Meta-data of an index.
 * 
 * @author Daniel
 */
public class IndexMetaData {

	/**
	 * {@link TableMetaData} of the table that this index resides on.
	 */
	private final TableMetaData table;

	/**
	 * Name of this index.
	 */
	private final String indexName;

	/**
	 * Columns of this index.
	 */
	private final ColumnMetaData[] columns;

	/**
	 * Flags if this index is unique.
	 */
	private final boolean isUnqiue;

	/**
	 * Initiate.
	 * 
	 * @param table
	 *            {@link TableMetaData} of the table that this index resides on.
	 * @param indexName
	 *            Name of this index.
	 * @param columns
	 *            {@link ColumnMetaData} instances comprising this index.
	 * @param isUnique
	 *            Flags if this index is unique.
	 */
	public IndexMetaData(TableMetaData table, String indexName,
			ColumnMetaData[] columns, boolean isUnique) {
		this.table = table;
		this.indexName = indexName;
		this.columns = columns;
		this.isUnqiue = isUnique;
	}

	/**
	 * Obtains the name of this index.
	 * 
	 * @return Name of this index.
	 */
	public String getIndexName() {
		return this.indexName;
	}

	/**
	 * Obtains the {@link ColumnMetaData} instances comprising this index.
	 * 
	 * @return {@link ColumnMetaData} instances comprising this index.
	 */
	public ColumnMetaData[] getColumns() {
		return this.columns;
	}

	/**
	 * Indicates if this index is unique.
	 * 
	 * @return <code>true</code> if this index is unique.
	 */
	public boolean isUnique() {
		return this.isUnqiue;
	}

	/**
	 * Obtains the package name of this index.
	 * 
	 * @return Package name of this index.
	 */
	public String getPackageName() {
		return this.table.getPackageName();
	}

	/**
	 * Obtains the simple {@link Class} name for this index.
	 * 
	 * @return Simple {@link Class} name for this index.
	 */
	public String getSimpleClassName() {

		// Create the virtual database name identifying this index
		StringBuilder virtualDatabaseName = new StringBuilder();
		virtualDatabaseName.append(this.table.getTableName());
		virtualDatabaseName.append("_");
		virtualDatabaseName.append("INDEX");
		for (ColumnMetaData column : this.columns) {
			virtualDatabaseName.append("_");
			virtualDatabaseName.append(column.getColumnName());
		}

		// Return the simple class name
		return FilingCabinetUtil.getSimpleClassName(virtualDatabaseName
				.toString());
	}

	/**
	 * Obtains the fully qualified class name of this index.
	 * 
	 * @return Fully qualified class name of this index.
	 */
	public String getFullyQualifiedClassName() {
		String name = this.getPackageName();
		name = (name.length() > 0 ? name + "." : "")
				+ this.getSimpleClassName();
		return name;
	}
}
