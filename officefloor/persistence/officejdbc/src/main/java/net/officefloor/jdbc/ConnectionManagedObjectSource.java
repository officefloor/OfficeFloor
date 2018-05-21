/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
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
package net.officefloor.jdbc;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sql.ConnectionPoolDataSource;
import javax.sql.DataSource;

import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.pool.ManagedObjectPool;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.managedobject.source.ManagedObjectUser;
import net.officefloor.frame.api.managedobject.source.impl.AbstractManagedObjectSource;
import net.officefloor.jdbc.datasource.DataSourceFactory;
import net.officefloor.jdbc.datasource.DefaultDataSourceFactory;

/**
 * {@link ManagedObjectSource} for {@link Connection}.
 * 
 * @author Daniel Sagenschneider
 */
public class ConnectionManagedObjectSource extends AbstractManagedObjectSource<None, None> {

	/**
	 * {@link Logger}.
	 */
	private static final Logger LOGGER = Logger.getLogger(ConnectionManagedObjectSource.class.getName());

	/**
	 * {@link DataSource}.
	 */
	private DataSource dataSource;

	/**
	 * Allows overriding to configure a different {@link DataSourceFactory}.
	 * 
	 * @return {@link DataSourceFactory}.
	 */
	protected DataSourceFactory getDataSourceFactory() {
		return new DefaultDataSourceFactory();
	}

	/**
	 * Obtains the {@link ConnectionPoolDataSource}.
	 * 
	 * @return {@link ConnectionPoolDataSource}.
	 * @throws IllegalStateException
	 *             If {@link DataSource} configured is not a
	 *             {@link ConnectionPoolDataSource}.
	 */
	public ConnectionPoolDataSource getConnectionPoolDataSource() throws IllegalStateException {

		// Ensure a connection pool data source

		// Return the connection pool data source
		return (ConnectionPoolDataSource) this.dataSource;
	}

	/*
	 * ================== AbstractManagedObjectSource ===================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
	}

	@Override
	protected void loadMetaData(MetaDataContext<None, None> context) throws Exception {

		// Create the data source
		DataSourceFactory factory = this.getDataSourceFactory();
		this.dataSource = factory.createDataSource(context.getManagedObjectSourceContext());

		// Configure meta-data
		context.setObjectClass(Connection.class);
		context.setManagedObjectClass(AbstractConnectionManagedObject.class);
		context.getManagedObjectSourceContext().setDefaultManagedObjectPool(
				(poolContext) -> new DefaultManagedObjectPool(poolContext.getManagedObjectSource()));
	}

	@Override
	protected ManagedObject getManagedObject() {
		return new ConnectionManagedObject();
	}

	/**
	 * {@link Connection} {@link ManagedObject}.
	 */
	private class ConnectionManagedObject extends AbstractConnectionManagedObject {

		@Override
		protected Connection getConnection() throws SQLException {
			return ConnectionManagedObjectSource.this.dataSource.getConnection();
		}
	}

	/**
	 * Default {@link ManagedObjectPool} for the
	 * {@link ConnectionManagedObjectSource}.
	 */
	private static class DefaultManagedObjectPool implements ManagedObjectPool {

		/**
		 * {@link ManagedObjectSource}.
		 */
		private final ManagedObjectSource<?, ?> managedObjectSource;

		/**
		 * Instantiate.
		 * 
		 * @param managedObjectSource
		 *            {@link ManagedObjectSource}.
		 */
		public DefaultManagedObjectPool(ManagedObjectSource<?, ?> managedObjectSource) {
			this.managedObjectSource = managedObjectSource;
		}

		/**
		 * Closes the {@link Connection}.
		 * 
		 * @param managedObject
		 *            {@link ConnectionManagedObject}.
		 */
		private void closeConnection(ManagedObject managedObject) {
			try {
				((ConnectionManagedObject) managedObject).connection.close();
			} catch (SQLException ex) {
				LOGGER.log(Level.WARNING, "Failed to close connection", ex);
			}
		}

		/*
		 * ============== ManagedObjectPool ====================
		 */

		@Override
		public void sourceManagedObject(ManagedObjectUser user) {
			this.managedObjectSource.sourceManagedObject(user);
		}

		@Override
		public void returnManagedObject(ManagedObject managedObject) {
			this.closeConnection(managedObject);
		}

		@Override
		public void lostManagedObject(ManagedObject managedObject, Throwable cause) {
			this.closeConnection(managedObject);
		}
	}

}