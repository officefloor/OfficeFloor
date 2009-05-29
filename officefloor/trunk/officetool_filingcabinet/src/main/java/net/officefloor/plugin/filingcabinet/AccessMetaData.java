/*
 * Office Floor, Application Server
 * Copyright (C) 2005-2009 Daniel Sagenschneider
 *
 * This program is free software; you can redistribute it and/or modify it under the terms
 * of the GNU General Public License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program;
 * if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 * MA 02111-1307 USA
 */
package net.officefloor.plugin.filingcabinet;

/**
 * Meta-data about access to a table.
 * 
 * @author Daniel Sagenschneider
 */
public class AccessMetaData extends IndexMetaData {

	/**
	 * Initialise.
	 * 
	 * @param table
	 *            {@link TableMetaData}.
	 * @param accessName
	 *            Name of access.
	 * @param columns
	 *            {@link ColumnMetaData} instances.
	 * @param isUnique
	 *            Indicates if unique.
	 */
	public AccessMetaData(TableMetaData table, String accessName,
			ColumnMetaData[] columns, boolean isUnique) {
		super(table, accessName, columns, isUnique);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof AccessMetaData)) {
			return false;
		}
		AccessMetaData that = (AccessMetaData) obj;
		ColumnMetaData[] thisColumns = this.getColumns();
		ColumnMetaData[] thatColumns = that.getColumns();
		if (thisColumns.length != thatColumns.length) {
			return false;
		}
		for (int i = 0; i < thisColumns.length; i++) {
			if (!thisColumns[i].getColumnName().equals(
					thatColumns[i].getColumnName())) {
				return false;
			}
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		int hash = AccessMetaData.class.hashCode();
		for (ColumnMetaData column : this.getColumns()) {
			hash = (hash + column.getColumnName().hashCode()) * 31;
		}
		return hash;
	}

}
