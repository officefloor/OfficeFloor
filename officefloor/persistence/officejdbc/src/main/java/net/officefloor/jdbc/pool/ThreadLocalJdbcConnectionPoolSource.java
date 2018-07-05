/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.officefloor.jdbc.pool;

import java.sql.Connection;

import javax.sql.ConnectionPoolDataSource;

import net.officefloor.compile.spi.pool.source.ManagedObjectPoolSource;
import net.officefloor.compile.spi.pool.source.impl.AbstractManagedObjectPoolSource;
import net.officefloor.frame.api.managedobject.pool.ManagedObjectPool;
import net.officefloor.frame.api.managedobject.pool.ManagedObjectPoolContext;
import net.officefloor.frame.api.managedobject.pool.ManagedObjectPoolFactory;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.jdbc.ConnectionManagedObjectSource;

/**
 * {@link Connection} {@link ManagedObjectPoolSource} implementation that uses
 * {@link ThreadLocal} instances to reduce contention of retrieving
 * {@link Connection} from singleton pool.
 * 
 * @author Daniel Sagenschneider
 */
public class ThreadLocalJdbcConnectionPoolSource extends AbstractManagedObjectPoolSource
		implements ManagedObjectPoolFactory {

	/**
	 * {@link ClassLoader}.
	 */
	private ClassLoader classLoader;

	/*
	 * ============= AbstractManagedObjectPoolSource ================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
	}

	@Override
	protected void loadMetaData(MetaDataContext context) throws Exception {

		// Obtain the class loader (necessary for Connection proxies)
		this.classLoader = context.getManagedObjectPoolSourceContext().getClassLoader();

		// Load the meta-data
		context.setPooledObjectType(Connection.class);
		context.setManagedObjectPoolFactory(this);
		context.addThreadCompleteListener((pool) -> (ThreadLocalJdbcConnectionPool) pool);
	}

	/*
	 * ================= ManagedObjectPoolFactory ====================
	 */

	@Override
	public ManagedObjectPool createManagedObjectPool(ManagedObjectPoolContext managedObjectPoolContext) {

		// Ensure have connection managed object source
		ManagedObjectSource<?, ?> managedObjectSource = managedObjectPoolContext.getManagedObjectSource();
		if (!(managedObjectSource instanceof ConnectionManagedObjectSource)) {
			throw new IllegalArgumentException(this.getClass().getSimpleName() + " can only be used with a "
					+ ConnectionManagedObjectSource.class.getName());
		}
		ConnectionManagedObjectSource connectionManagedObjectSource = (ConnectionManagedObjectSource) managedObjectSource;

		// Obtain the pooled data source
		ConnectionPoolDataSource dataSource = connectionManagedObjectSource.getConnectionPoolDataSource();

		// Create and return the pool
		return new ThreadLocalJdbcConnectionPool(dataSource, this.classLoader);
	}

}