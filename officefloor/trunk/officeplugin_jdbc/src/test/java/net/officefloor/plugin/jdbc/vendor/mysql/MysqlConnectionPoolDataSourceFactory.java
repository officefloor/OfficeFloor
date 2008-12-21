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
package net.officefloor.plugin.jdbc.vendor.mysql;

import javax.sql.ConnectionPoolDataSource;

import com.mysql.jdbc.jdbc2.optional.MysqlConnectionPoolDataSource;

import net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceContext;
import net.officefloor.plugin.jdbc.ConnectionPoolDataSourceFactory;

/**
 * MySQL {@link ConnectionPoolDataSourceFactory}.
 * 
 * @author Daniel
 */
public class MysqlConnectionPoolDataSourceFactory implements
		ConnectionPoolDataSourceFactory {

	/*
	 * (non-Javadoc)
	 * 
	 * @seenet.officefloor.plugin.jdbc.ConnectionPoolDataSourceFactory#
	 * createConnectionPoolDataSource
	 * (net.officefloor.frame.spi.managedobject.source
	 * .ManagedObjectSourceContext)
	 */
	@Override
	public ConnectionPoolDataSource createConnectionPoolDataSource(
			ManagedObjectSourceContext context) throws Exception {

		// Create and configure the data source
		MysqlConnectionPoolDataSource dataSource = new MysqlConnectionPoolDataSource();
		dataSource.setServerName(context.getProperty("serverName"));
		dataSource.setPort(Integer.parseInt(context.getProperty("port")));
		dataSource.setDatabaseName(context.getProperty("databaseName"));
		dataSource.setUser(context.getProperty("user"));
		dataSource.setPassword(context.getProperty("password"));
		
		// Return the configured data source
		return dataSource;
	}

}
