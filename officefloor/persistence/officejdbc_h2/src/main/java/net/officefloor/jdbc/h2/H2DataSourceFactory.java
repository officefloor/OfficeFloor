/*-
 * #%L
 * H2 Persistence
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

package net.officefloor.jdbc.h2;

import java.net.URL;

import javax.sql.ConnectionPoolDataSource;
import javax.sql.DataSource;

import org.h2.jdbcx.JdbcDataSource;

import net.officefloor.compile.properties.Property;
import net.officefloor.frame.api.managedobject.source.impl.AbstractAsyncManagedObjectSource.SpecificationContext;
import net.officefloor.frame.api.source.SourceContext;
import net.officefloor.jdbc.datasource.ConnectionPoolDataSourceFactory;
import net.officefloor.jdbc.datasource.DataSourceFactory;
import net.officefloor.jdbc.datasource.DefaultDataSourceFactory;

/**
 * H2 {@link DataSourceFactory}.
 * 
 * @author Daniel Sagenschneider
 */
public interface H2DataSourceFactory extends DataSourceFactory, ConnectionPoolDataSourceFactory {

	/**
	 * {@link Property} for {@link URL}.
	 */
	String PROPERTY_URL = "url";

	/**
	 * {@link Property} for user.
	 */
	String PROPERTY_USER = "user";

	/**
	 * {@link Property} for password.
	 */
	String PROPERTY_PASSWORD = "password";

	/**
	 * Loads the specification.
	 * 
	 * @param context {@link SpecificationContext}.
	 */
	static void loadSpecification(SpecificationContext context) {
		context.addProperty(PROPERTY_URL, "URL");
		context.addProperty(PROPERTY_USER, "User");
		context.addProperty(PROPERTY_PASSWORD, "Password");
	}

	/*
	 * ================= DataSourceFactory ====================
	 */

	@Override
	default DataSource createDataSource(SourceContext context) throws Exception {

		// Create the data source
		JdbcDataSource dataSource = new JdbcDataSource();

		// Load optional properties
		DefaultDataSourceFactory.loadProperties(dataSource, context);

		// Load specification properties
		dataSource.setURL(context.getProperty(PROPERTY_URL));
		dataSource.setUser(context.getProperty(PROPERTY_USER));
		dataSource.setPassword(context.getProperty(PROPERTY_PASSWORD, ""));

		// Return the data source
		return dataSource;
	}

	/*
	 * ============ ConnectionPoolDataSourceFactory ============
	 */

	@Override
	default ConnectionPoolDataSource createConnectionPoolDataSource(SourceContext context) throws Exception {
		return (ConnectionPoolDataSource) this.createDataSource(context);
	}

}
