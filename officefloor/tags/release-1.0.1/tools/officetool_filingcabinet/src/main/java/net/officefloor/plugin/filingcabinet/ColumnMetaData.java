/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2011 Daniel Sagenschneider
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

import java.sql.Types;

/**
 * Meta-data for a column.
 * 
 * @author Daniel Sagenschneider
 */
public class ColumnMetaData {

	/**
	 * Name of the column.
	 */
	private final String columnName;

	/**
	 * {@link Types} value for this column.
	 */
	private final int sqlType;

	/**
	 * Java {@link Class} for this column.
	 */
	private final Class<?> javaType;

	/**
	 * Indicates if this column may contain nulls.
	 */
	private final boolean isNullable;

	/**
	 * Initiate.
	 * 
	 * @param columnName
	 *            Name of the column.
	 * @param sqlType
	 *            {@link Types} value for this column.
	 * @param javaType
	 *            Java {@link Class} for this column.
	 * @param isNullable
	 *            Indicates if this column may contain nulls.
	 */
	public ColumnMetaData(String columnName, int sqlType, Class<?> javaType,
			boolean isNullable) {
		this.columnName = columnName;
		this.sqlType = sqlType;
		this.javaType = javaType;
		this.isNullable = isNullable;
	}

	/**
	 * Obtains the name of this column.
	 * 
	 * @return Name of this column.
	 */
	public String getColumnName() {
		return this.columnName;
	}

	/**
	 * Obtains the {@link Types} value for this column.
	 * 
	 * @return {@link Types} value for this column.
	 */
	public int getSqlType() {
		return this.sqlType;
	}

	/**
	 * Obtains the java {@link Class} for this column.
	 * 
	 * @return Java {@link Class} for this column.
	 */
	public Class<?> getJavaType() {
		return this.javaType;
	}

	/**
	 * Indicates if this column may contain nulls.
	 * 
	 * @return <code>true</code> if this column may contain nulls.
	 */
	public boolean isNullable() {
		return this.isNullable;
	}

	/**
	 * Obtains the get method name for this column.
	 * 
	 * @return Get method for this column.
	 */
	public String getGetMethodName() {
		return FilingCabinetUtil.getGetMethodName(this.columnName);
	}

	/**
	 * Obtains the set method name for this column.
	 * 
	 * @return Set method for this column.
	 */
	public String getSetMethodName() {
		return FilingCabinetUtil.getSetMethodName(this.columnName);
	}

	/**
	 * Obtains the field name for this column.
	 * 
	 * @return Field name for this column.
	 */
	public String getFieldName() {
		return FilingCabinetUtil.getFieldName(this.columnName);
	}
	
}
