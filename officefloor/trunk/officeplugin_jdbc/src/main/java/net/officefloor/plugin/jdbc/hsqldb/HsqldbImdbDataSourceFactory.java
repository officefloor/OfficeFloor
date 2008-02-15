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
package net.officefloor.plugin.jdbc.hsqldb;

import java.util.Properties;

import javax.sql.ConnectionPoolDataSource;
import javax.sql.XADataSource;

import org.apache.commons.dbcp.cpdsadapter.DriverAdapterCPDS;
import org.hsqldb.jdbcDriver;

import net.officefloor.plugin.jdbc.DataSourceFactory;

/**
 * <p>
 * {@link DataSourceFactory} for HSQLDB IMDB.
 * <p>
 * As HSQLDB do not provide a {@link ConnectionPoolDataSource} implementation,
 * the {@link DriverAdapterCPDS} is used to provide this.
 * 
 * @author Daniel
 */
public class HsqldbImdbDataSourceFactory implements DataSourceFactory {

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.plugin.jdbc.DataSourceFactory#createConnectionPoolDataSource(java.util.Properties)
	 */
	@Override
	public ConnectionPoolDataSource createConnectionPoolDataSource(
			Properties properties) throws Exception {

		// Obtain the name of the database
		String name = properties.getProperty("hsqldb.database.name");

		// Create and initialise the connection pool of HSQLDB
		DriverAdapterCPDS dataSource = new DriverAdapterCPDS();
		dataSource.setDriver(jdbcDriver.class.getName());
		dataSource.setUrl("jdbc:hsqldb:mem:" + name);
		dataSource.setUser("sa");
		dataSource.setPassword("");

		// Return the data source
		return dataSource;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.plugin.jdbc.DataSourceFactory#createXADataSource(java.util.Properties)
	 */
	@Override
	public XADataSource createXADataSource(Properties properties)
			throws Exception {
		// TODO Implement
		throw new UnsupportedOperationException(
				"TODO implement HsqldbImdbDataSourceFactory.createXADataSource");
	}

}
