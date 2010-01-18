/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2010 Daniel Sagenschneider
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

/**
 * Cross reference meta-data.
 * 
 * @author Daniel Sagenschneider
 */
public class CrossReferenceMetaData {

	/**
	 * Name of the foreign key for this cross reference.
	 */
	private final String foreignKeyName;

	/**
	 * Primary {@link TableMetaData}.
	 */
	private final TableMetaData primaryTable;

	/**
	 * Primary {@link ColumnMetaData} instances.
	 */
	private final ColumnMetaData[] primaryColumns;

	/**
	 * Foreign {@link TableMetaData}.
	 */
	private final TableMetaData foreignTable;

	/**
	 * Foreign {@link ColumnMetaData} instances.
	 */
	private final ColumnMetaData[] foreignColumns;

	/**
	 * Initiate.
	 * 
	 * @param foreignKeyName
	 *            Name of the foreign key for this cross reference.
	 * @param primaryTable
	 *            Primary {@link TableMetaData}.
	 * @param primaryColumnNames
	 *            Names of the primary columns.
	 * @param foreignTable
	 *            Foreign {@link TableMetaData}.
	 * @param foreignColumnNames
	 *            Names of the foreign columns.
	 */
	public CrossReferenceMetaData(String foreignKeyName,
			TableMetaData primaryTable, String[] primaryColumnNames,
			TableMetaData foreignTable, String[] foreignColumnNames) {
		this.foreignKeyName = foreignKeyName;
		this.primaryTable = primaryTable;
		this.primaryColumns = this.primaryTable
				.getColumnsByName(primaryColumnNames);
		this.foreignTable = foreignTable;
		this.foreignColumns = this.foreignTable
				.getColumnsByName(foreignColumnNames);
	}

	/**
	 * Obtains the foreign key name of this cross reference.
	 * 
	 * @return Foreign key name of this cross reference.
	 */
	public String getForeignKeyName() {
		return this.foreignKeyName;
	}

	/**
	 * Obtains the primary {@link TableMetaData}.
	 * 
	 * @return Primary {@link TableMetaData}.
	 */
	public TableMetaData getPrimaryTable() {
		return this.primaryTable;
	}

	/**
	 * Obtains the primary {@link ColumnMetaData} instance involved in this
	 * cross reference.
	 * 
	 * @return Primary {@link ColumnMetaData} instance involved in this cross
	 *         reference.
	 */
	public ColumnMetaData[] getPrimaryColumns() {
		return this.primaryColumns;
	}

	/**
	 * Obtains the foreign {@link TableMetaData}.
	 * 
	 * @return Foreign {@link TableMetaData}.
	 */
	public TableMetaData getForeignTable() {
		return this.foreignTable;
	}

	/**
	 * Obtains the foreign {@link ColumnMetaData} instance involved in this
	 * cross reference.
	 * 
	 * @return Foreign {@link ColumnMetaData} instance involved in this cross
	 *         reference.
	 */
	public ColumnMetaData[] getForeignColumns() {
		return this.foreignColumns;
	}

}
