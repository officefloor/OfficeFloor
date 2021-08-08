/*-
 * #%L
 * PostgreSQL Persistence
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package net.officefloor.jdbc.postgresql;

import javax.sql.ConnectionPoolDataSource;
import javax.sql.DataSource;

import org.postgresql.ds.PGConnectionPoolDataSource;
import org.postgresql.ds.PGSimpleDataSource;
import org.postgresql.ds.common.BaseDataSource;

import net.officefloor.compile.properties.Property;
import net.officefloor.frame.api.managedobject.source.impl.AbstractAsyncManagedObjectSource.SpecificationContext;
import net.officefloor.frame.api.source.SourceContext;
import net.officefloor.jdbc.datasource.ConnectionPoolDataSourceFactory;
import net.officefloor.jdbc.datasource.DataSourceFactory;
import net.officefloor.jdbc.datasource.DefaultDataSourceFactory;

/**
 * PostgreSql {@link DataSourceFactory}.
 * 
 * @author Daniel Sagenschneider
 */
public interface PostgreSqlDataSourceFactory extends DataSourceFactory, ConnectionPoolDataSourceFactory {

	/**
	 * {@link Property} for the server name.
	 */
	String PROPERTY_SERVER_NAME = "server";

	/**
	 * {@link Property} for the port.
	 */
	String PROPERTY_PORT = "port";

	/**
	 * {@link Property} for the database.
	 */
	String PROPERTY_DATABASE_NAME = "database";

	/**
	 * {@link Property} for the user name.
	 */
	String PROPERTY_USER = "user";

	/**
	 * {@link Property} for the password.
	 */
	String PROPERTY_PASSWORD = "password";

	/**
	 * Loads the specification.
	 * 
	 * @param context {@link SpecificationContext}.
	 */
	static void loadSpecification(SpecificationContext context) {
		context.addProperty(PROPERTY_SERVER_NAME, "Server");
		context.addProperty(PROPERTY_USER, "User");
		context.addProperty(PROPERTY_PASSWORD, "Password");
	}

	/**
	 * Configures the {@link BaseDataSource}.
	 * 
	 * @param dataSource {@link BaseDataSource}.
	 * @param context    {@link SourceContext}.
	 * @throws Exception If fails to configure the {@link BaseDataSource}.
	 */
	static <S extends BaseDataSource> S configureDataSource(S dataSource, SourceContext context) throws Exception {

		// Load optional configuration
		DefaultDataSourceFactory.loadProperties(dataSource, context);

		// Load required properties
		dataSource.setServerNames(new String[] { context.getProperty(PROPERTY_SERVER_NAME) });
		dataSource.setUser(context.getProperty(PROPERTY_USER));
		dataSource.setPassword(context.getProperty(PROPERTY_PASSWORD));

		// Load optional port
		String port = context.getProperty(PROPERTY_PORT, null);
		if (port != null) {
			dataSource.setPortNumbers(new int[] { Integer.parseInt(port) });
		}

		// Load optional database
		String database = context.getProperty(PROPERTY_DATABASE_NAME, null);
		if (database != null) {
			dataSource.setDatabaseName(database);
		}

		// Return the data source
		return dataSource;
	}

	/*
	 * ==================== DataSourceFactory =========================
	 */

	@Override
	default DataSource createDataSource(SourceContext context) throws Exception {
		return configureDataSource(new PGSimpleDataSource(), context);
	}

	/*
	 * ============== ConnectionPoolDataSourceFactory ===================
	 */

	@Override
	default ConnectionPoolDataSource createConnectionPoolDataSource(SourceContext context) throws Exception {
		return configureDataSource(new PGConnectionPoolDataSource(), context);
	}

}
