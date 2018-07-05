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
package net.officefloor.jdbc.h2;

import java.net.URL;

import javax.sql.DataSource;

import org.h2.jdbcx.JdbcDataSource;

import net.officefloor.compile.properties.Property;
import net.officefloor.frame.api.source.SourceContext;
import net.officefloor.jdbc.ConnectionManagedObjectSource;
import net.officefloor.jdbc.datasource.DataSourceFactory;
import net.officefloor.jdbc.datasource.DefaultDataSourceFactory;

/**
 * H2 {@link ConnectionManagedObjectSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class H2ConnectionManagedObjectSource extends ConnectionManagedObjectSource implements DataSourceFactory {

	/**
	 * {@link Property} for {@link URL}.
	 */
	public static final String PROPERTY_URL = "url";

	/**
	 * {@link Property} for user.
	 */
	public static final String PROPERTY_USER = "user";

	/**
	 * {@link Property} for password.
	 */
	public static final String PROPERTY_PASSWORD = "password";

	/*
	 * ============= ConnectionManagedObjectSource ===========
	 */

	@Override
	public void loadSpecification(SpecificationContext context) {
		context.addProperty(PROPERTY_URL, "URL");
		context.addProperty(PROPERTY_USER, "User");
		context.addProperty(PROPERTY_PASSWORD, "Password");
	}

	@Override
	protected DataSourceFactory getDataSourceFactory(SourceContext context) {
		return this;
	}

	/*
	 * ================= DataSourceFactory ====================
	 */

	@Override
	public DataSource createDataSource(SourceContext context) throws Exception {

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

}