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
package net.officefloor.jdbc;

import java.sql.Connection;

import javax.sql.ConnectionPoolDataSource;
import javax.sql.DataSource;

import net.officefloor.compile.properties.Property;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSourceContext;
import net.officefloor.frame.api.managedobject.source.impl.AbstractManagedObjectSource;
import net.officefloor.frame.api.source.SourceContext;
import net.officefloor.jdbc.datasource.DataSourceFactory;
import net.officefloor.jdbc.datasource.DefaultDataSourceFactory;

/**
 * Abstract {@link ManagedObjectSource} for {@link Connection}.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractConnectionManagedObjectSource extends AbstractManagedObjectSource<None, None> {

	/**
	 * {@link Property} name to specify the {@link DataSourceFactory}
	 * implementation.
	 */
	public static final String PROPERTY_DATA_SOURCE_FACTORY = "datasource.factory";

	/**
	 * {@link DataSource}.
	 */
	protected DataSource dataSource;

	/**
	 * Allows overriding to configure a different {@link DataSourceFactory}.
	 * 
	 * @param context {@link SourceContext}.
	 * @return {@link DataSourceFactory}.
	 * @throws Exception If fails to obtain {@link DataSourceFactory}.
	 */
	protected DataSourceFactory getDataSourceFactory(SourceContext context) throws Exception {

		// Obtain the data source factory
		String dataSourceFactoryClassName = context.getProperty(PROPERTY_DATA_SOURCE_FACTORY,
				DefaultDataSourceFactory.class.getName());
		Class<?> dataSourceFactoryClass = context.loadClass(dataSourceFactoryClassName);
		if (!DataSourceFactory.class.isAssignableFrom(dataSourceFactoryClass)) {
			throw new Exception(dataSourceFactoryClassName + " must implement " + DataSourceFactory.class.getName());
		}
		DataSourceFactory dataSourceFactory = (DataSourceFactory) dataSourceFactoryClass.getDeclaredConstructor()
				.newInstance();

		// Return the data source factory
		return dataSourceFactory;
	}

	/**
	 * Obtains the {@link ConnectionPoolDataSource}.
	 * 
	 * @return {@link ConnectionPoolDataSource}.
	 * @throws IllegalStateException If {@link DataSource} configured is not a
	 *                               {@link ConnectionPoolDataSource}.
	 */
	public ConnectionPoolDataSource getConnectionPoolDataSource() throws IllegalStateException {

		// Ensure a connection pool data source
		if (!(this.dataSource instanceof ConnectionPoolDataSource)) {
			throw new IllegalStateException(DataSource.class.getSimpleName() + " provided does not implement "
					+ ConnectionPoolDataSource.class.getName() + " (implementing " + DataSource.class.getSimpleName()
					+ " " + this.dataSource.getClass().getName() + ")");
		}

		// Return the connection pool data source
		return (ConnectionPoolDataSource) this.dataSource;
	}

	/**
	 * Enables overriding to load further meta-data.
	 * 
	 * @param context {@link MetaDataContext}.
	 * @throws Exception If fails to loader further meta-data.
	 */
	protected abstract void loadFurtherMetaData(MetaDataContext<None, None> context) throws Exception;

	/*
	 * ================== AbstractManagedObjectSource ===================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
	}

	@Override
	protected void loadMetaData(MetaDataContext<None, None> context) throws Exception {
		ManagedObjectSourceContext<None> mosContext = context.getManagedObjectSourceContext();

		// Configure meta-data
		context.setObjectClass(Connection.class);
		context.setManagedObjectClass(AbstractConnectionManagedObject.class);
		this.loadFurtherMetaData(context);

		// Only load data source (if not loading type)
		if (mosContext.isLoadingType()) {
			return;
		}

		// Create the data source
		DataSourceFactory factory = this.getDataSourceFactory(mosContext);
		this.dataSource = factory.createDataSource(mosContext);
	}

}