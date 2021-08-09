/*-
 * #%L
 * H2 Persistence
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
