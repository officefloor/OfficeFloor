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
 * Provides additional awareness of a database.
 * 
 * @author Daniel Sagenschneider
 */
public interface DatabaseAwareness {

	/**
	 * Obtains the java {@link Class} for the input {@link Types}.
	 * 
	 * @param sqlType
	 *            Type from {@link Types}.
	 * @return Java {@link Class} for the input sqlType.
	 * @throws If
	 *             fails to determine the {@link Class} for the {@link Types}.
	 */
	Class<?> getJavaType(int sqlType) throws Exception;

	/**
	 * Indicates if the input table is a system table.
	 * 
	 * @param catalogName
	 *            Catalog name.
	 * @param schemaName
	 *            Schema name.
	 * @param tableName
	 *            Table name.
	 * @return <code>true</code> if a system table.
	 */
	boolean isSystemTable(String catalogName, String schemaName,
			String tableName);
}
