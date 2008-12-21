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
package net.officefloor.plugin.jdbc;

import javax.sql.ConnectionPoolDataSource;

/**
 * Provides access to obtain the {@link ConnectionPoolDataSource} from the
 * {@link JdbcManagedObjectSource}.
 * 
 * @author Daniel
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
