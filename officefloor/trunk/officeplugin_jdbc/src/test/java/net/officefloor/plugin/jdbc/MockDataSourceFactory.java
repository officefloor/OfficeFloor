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

import java.util.Properties;

import javax.sql.ConnectionPoolDataSource;
import javax.sql.XADataSource;

import net.officefloor.frame.api.build.BuildException;
import net.officefloor.frame.api.build.ManagedObjectBuilder;

/**
 * Mock {@link net.officefloor.plugin.jdbc.DataSourceFactory} for testing.
 * 
 * @author Daniel
 */
public class MockDataSourceFactory implements DataSourceFactory {

	/**
	 * {@link ConnectionPoolDataSource}.
	 */
	private static ConnectionPoolDataSource connectionPoolDataSource = null;

	/**
	 * Binds the {@link ConnectionPoolDataSource} for use in testing.
	 * 
	 * @param dataSource
	 *            {@link ConnectionPoolDataSource} for use in testing.
	 * @param builder
	 *            {@link ManagedObjectBuilder} for the
	 *            {@link net.officefloor.frame.spi.managedobject.source.ManagedObjectSource}.
	 * @throws BuildException
	 *             If fails.
	 */
	public static void bind(ConnectionPoolDataSource dataSource,
			ManagedObjectBuilder builder) throws BuildException {
		// Bind the data source
		connectionPoolDataSource = dataSource;

		// Register with builder
		builder.addProperty(
				JdbcManagedObjectSource.DATA_SOURCE_FACTORY_CLASS_PROPERTY,
				MockDataSourceFactory.class.getName());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.plugin.jdbc.DataSourceFactory#init(java.util.Properties)
	 */
	public void init(Properties properties) throws Exception {
		// Do nothing
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.plugin.jdbc.DataSourceFactory#createConnectionPoolDataSource()
	 */
	public ConnectionPoolDataSource createConnectionPoolDataSource()
			throws Exception {
		// Return the connection pool data source
		return connectionPoolDataSource;
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
