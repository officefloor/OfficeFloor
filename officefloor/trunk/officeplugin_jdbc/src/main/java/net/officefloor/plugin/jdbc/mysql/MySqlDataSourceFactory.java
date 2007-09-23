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
package net.officefloor.plugin.jdbc.mysql;

import java.util.Properties;

import javax.sql.ConnectionPoolDataSource;
import javax.sql.XADataSource;

import net.officefloor.plugin.jdbc.DataSourceFactory;

import com.mysql.jdbc.jdbc2.optional.MysqlConnectionPoolDataSource;

/**
 * {@link net.officefloor.plugin.jdbc.DataSourceFactory} for MySQL.
 * 
 * @author Daniel
 */
public class MySqlDataSourceFactory implements DataSourceFactory {

	/**
	 * Properties.
	 */
	protected Properties properties;

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.plugin.jdbc.DataSourceFactory#init(java.util.Properties)
	 */
	public void init(Properties properties) throws Exception {
		// Load mysql driver
		Class.forName("com.mysql.jdbc.Driver");

		// Record the properties
		this.properties = properties;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.plugin.jdbc.DataSourceFactory#createConnectionPoolDataSource()
	 */
	public ConnectionPoolDataSource createConnectionPoolDataSource()
			throws Exception {

		// Obtain the configuration details
		String server = properties.getProperty("server");
		String port = properties.getProperty("port");
		String database = properties.getProperty("database");
		String user = properties.getProperty("user");
		String password = properties.getProperty("password");

		// Create the MySql Data Source
		MysqlConnectionPoolDataSource dataSource = new MysqlConnectionPoolDataSource();
		if (server != null) {
			dataSource.setServerName(server);
		}
		if (port != null) {
			dataSource.setPort(Integer.parseInt(port));
		}
		if (database != null) {
			dataSource.setDatabaseName(database);
		}
		dataSource.setUser(user);
		dataSource.setPassword(password);

		// Return the Data Source
		return dataSource;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.plugin.jdbc.DataSourceFactory#createXADataSource()
	 */
	public XADataSource createXADataSource() throws Exception {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("TODO implement");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.plugin.jdbc.DataSourceFactory#getConnectionTimeout()
	 */
	public long getConnectionTimeout() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("TODO implement");
	}

}
