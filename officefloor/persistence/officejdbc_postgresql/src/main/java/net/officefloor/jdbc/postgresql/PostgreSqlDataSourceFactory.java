/*-
 * #%L
 * PostgreSQL Persistence
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
