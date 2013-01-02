/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2013 Daniel Sagenschneider
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
package net.officefloor.plugin.jdbc.connection;

import javax.sql.ConnectionPoolDataSource;

import net.officefloor.plugin.jdbc.connection.JdbcManagedObjectSource;

/**
 * Provides access to obtain the {@link ConnectionPoolDataSource} from the
 * {@link JdbcManagedObjectSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class JdbcDataSourceAccess {

	/**
	 * Obtains the {@link ConnectionPoolDataSource} from the
	 * {@link JdbcManagedObjectSource}.
	 * 
	 * @param jdbcManagedObjectSource
	 *            {@link JdbcManagedObjectSource}.
	 * @return {@link ConnectionPoolDataSource}.
	 */
	public static ConnectionPoolDataSource obtainConnectionPoolDataSource(
			JdbcManagedObjectSource jdbcManagedObjectSource) {
		return jdbcManagedObjectSource.dataSource;
	}

	/**
	 * All access via static methods.
	 */
	private JdbcDataSourceAccess() {
	}
}
