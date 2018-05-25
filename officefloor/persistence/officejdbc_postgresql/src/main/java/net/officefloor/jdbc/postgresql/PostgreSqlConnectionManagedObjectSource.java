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
package net.officefloor.jdbc.postgresql;

import java.lang.reflect.Proxy;

import javax.sql.ConnectionPoolDataSource;
import javax.sql.DataSource;

import org.postgresql.ds.PGConnectionPoolDataSource;

import net.officefloor.compile.properties.Property;
import net.officefloor.frame.api.source.SourceContext;
import net.officefloor.jdbc.ConnectionManagedObjectSource;
import net.officefloor.jdbc.datasource.DataSourceFactory;
import net.officefloor.jdbc.datasource.DefaultDataSourceFactory;

/**
 * PostgreSQL {@link ConnectionManagedObjectSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class PostgreSqlConnectionManagedObjectSource extends ConnectionManagedObjectSource
		implements DataSourceFactory {

	/**
	 * {@link Property} for the server name.
	 */
	public static final String PROPERTY_SERVER_NAME = "server";

	/**
	 * {@link Property} for the port.
	 */
	public static final String PROPERTY_PORT = "port";

	/**
	 * {@link Property} for the database.
	 */
	public static final String PROPERTY_DATABASE_NAME = "database";

	/**
	 * {@link Property} for the user name.
	 */
	public static final String PROPERTY_USER = "user";

	/**
	 * {@link Property} for the password.
	 */
	public static final String PROPERTY_PASSWORD = "password";

	/*
	 * =============== ConnectionManagedObjectSource =================
	 */

	@Override
	protected DataSourceFactory getDataSourceFactory() {
		return this;
	}

	/*
	 * ==================== DataSourceFactory =========================
	 */

	@Override
	public void loadSpecification(SpecificationContext context) {
		context.addProperty(PROPERTY_SERVER_NAME, "Server");
		context.addProperty(PROPERTY_PORT, "Port");
		context.addProperty(PROPERTY_DATABASE_NAME, "Database");
		context.addProperty(PROPERTY_USER, "User");
		context.addProperty(PROPERTY_PASSWORD, "Password");
	}

	@Override
	public DataSource createDataSource(SourceContext context) throws Exception {

		// Create the data source
		PGConnectionPoolDataSource dataSource = new PGConnectionPoolDataSource();

		// Load optional configuration
		DefaultDataSourceFactory.loadProperties(dataSource, context);

		// Load required properties
		dataSource.setServerName(context.getProperty(PROPERTY_SERVER_NAME));
		dataSource.setPortNumber(Integer.parseInt(context.getProperty(PROPERTY_PORT)));
		dataSource.setDatabaseName(context.getProperty(PROPERTY_DATABASE_NAME));
		dataSource.setUser(context.getProperty(PROPERTY_USER));
		dataSource.setPassword(context.getProperty(PROPERTY_PASSWORD));

		// Return the data source
		return (DataSource) Proxy.newProxyInstance(context.getClassLoader(),
				new Class[] { DataSource.class, ConnectionPoolDataSource.class }, (proxy, method, args) -> {
					return dataSource.getClass().getMethod(method.getName(), method.getParameterTypes())
							.invoke(dataSource, args);
				});
	}

}