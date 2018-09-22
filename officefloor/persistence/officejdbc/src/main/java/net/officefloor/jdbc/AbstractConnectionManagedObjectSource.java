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
import java.sql.SQLException;

import javax.sql.DataSource;

import net.officefloor.compile.properties.Property;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSourceContext;
import net.officefloor.frame.api.managedobject.source.impl.AbstractManagedObjectSource;
import net.officefloor.frame.api.source.SourceContext;
import net.officefloor.jdbc.datasource.ConnectionPoolDataSourceFactory;
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
	 * {@link Property} name to specify the SQL to run to validate the
	 * {@link DataSource} is configured correctly.
	 */
	public static final String PROPERTY_DATA_SOURCE_VALIDATE_SQL = "datasource.validate.sql";

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
	 * Allows overriding to configure a different
	 * {@link ConnectionPoolDataSourceFactory}.
	 * 
	 * @param context {@link SourceContext}.
	 * @return {@link ConnectionPoolDataSourceFactory}.
	 * @throws Exception If fails to obtain {@link ConnectionPoolDataSourceFactory}.
	 */
	protected ConnectionPoolDataSourceFactory getConnectionPoolDataSourceFactory(SourceContext context)
			throws Exception {

		// Obtain the data source factory
		String dataSourceFactoryClassName = context.getProperty(PROPERTY_DATA_SOURCE_FACTORY,
				DefaultDataSourceFactory.class.getName());
		Class<?> dataSourceFactoryClass = context.loadClass(dataSourceFactoryClassName);
		if (!ConnectionPoolDataSourceFactory.class.isAssignableFrom(dataSourceFactoryClass)) {
			throw new Exception(
					dataSourceFactoryClassName + " must implement " + ConnectionPoolDataSourceFactory.class.getName());
		}
		ConnectionPoolDataSourceFactory dataSourceFactory = (ConnectionPoolDataSourceFactory) dataSourceFactoryClass
				.getDeclaredConstructor().newInstance();

		// Return the data source factory
		return dataSourceFactory;
	}

	/**
	 * Enables overriding to load further meta-data.
	 * 
	 * @param context {@link MetaDataContext}.
	 * @throws Exception If fails to loader further meta-data.
	 */
	protected abstract void loadFurtherMetaData(MetaDataContext<None, None> context) throws Exception;

	/**
	 * {@link ConnectivityFactory}.
	 */
	private ConnectivityFactory connectivityFactory;

	/**
	 * Factory for {@link Connection} in confirming connectivity.
	 */
	@FunctionalInterface
	public static interface ConnectivityFactory {

		/**
		 * Obtains the {@link Connection} for connectivity.
		 *
		 * @return {@link Connection}.
		 * @throws SQLException If fails to obtain connectivity.
		 */
		Connection createConnectivity() throws SQLException;
	}

	/**
	 * Loads validation of connectivity on start up.
	 * 
	 * @param context {@link MetaDataContext}.
	 * @throws Exception If fails to load validation.
	 */
	protected void loadValidateConnectivity(MetaDataContext<None, None> context) throws Exception {
		ManagedObjectSourceContext<None> mosContext = context.getManagedObjectSourceContext();

		// Provide start up function to ensure can connect
		String validateSql = mosContext.getProperty(PROPERTY_DATA_SOURCE_VALIDATE_SQL, null);
		final String validateFunctionName = "confirm";
		mosContext.addManagedFunction(validateFunctionName, () -> (functionContext) -> {
			this.validateConnectivity(validateSql);
			return null;
		});
		mosContext.addStartupFunction(validateFunctionName);
	}

	/**
	 * Specifies {@link ConnectivityFactory} for validating connectivity on startup.
	 * 
	 * @param connectivityFactory {@link ConnectivityFactory}.
	 */
	public void setConnectivity(ConnectivityFactory connectivityFactory) {
		this.connectivityFactory = connectivityFactory;
	}

	/**
	 * Validates connectivity.
	 * 
	 * @param sql Optional SQL to be executed against the {@link Connection}. May be
	 *            <code>null</code>.
	 * @throws Exception If fails connectivity.
	 */
	protected void validateConnectivity(String sql) throws Exception {

		// Ensure have connectivity
		if (this.connectivityFactory == null) {
			throw new SQLException("Must specify " + ConnectivityFactory.class.getName());
		}

		// Undertake connectivity
		try (Connection connection = this.connectivityFactory.createConnectivity()) {
			if (sql != null) {
				connection.createStatement().execute(sql);
			}
		}
	}

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

		// Only load data source (if not loading type)
		if (mosContext.isLoadingType()) {
			return;
		}

		// Create further meta-data
		this.loadFurtherMetaData(context);

		// Ensure connected on startup
		this.loadValidateConnectivity(context);
	}

}