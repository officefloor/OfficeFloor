/*-
 * #%L
 * JDBC Persistence
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

package net.officefloor.jdbc.datasource;

import javax.sql.CommonDataSource;
import javax.sql.DataSource;

import net.officefloor.compile.properties.PropertyList;
import net.officefloor.frame.api.source.SourceContext;
import net.officefloor.frame.api.source.SourceProperties;
import net.officefloor.frame.api.source.UnknownPropertyError;
import net.officefloor.frame.impl.construct.source.SourceContextImpl;
import net.officefloor.frame.impl.construct.source.SourcePropertiesImpl;
import net.officefloor.frame.test.AbstractOfficeConstructTestCase;
import net.officefloor.frame.test.MockClockFactory;

/**
 * Tests the {@link DataSourceFactory}.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractDataSourceFactoryTestCase extends AbstractOfficeConstructTestCase {

	/**
	 * Obtains the implementing {@link CommonDataSource} type.
	 * 
	 * @return Implementing {@link CommonDataSource} type.
	 */
	protected abstract Class<? extends CommonDataSource> getDataSourceType();

	/**
	 * Creates the {@link CommonDataSource} implementation to be tested.
	 * 
	 * @param sourceContext {@link SourceContext}.
	 * @return {@link CommonDataSource}.
	 * @throws Exception If fails to create {@link CommonDataSource}.
	 */
	protected abstract CommonDataSource createCommonDataSource(SourceContext sourceContext) throws Exception;

	/**
	 * Handle no required configuration for {@link DataSource}.
	 */
	public void testNoConfiguration() throws Exception {
		try {
			this.createCommonDataSource(this.createSourceContext());
			fail("Should not be successful");
		} catch (UnknownPropertyError ex) {
			assertEquals("Incorrect cause", DefaultDataSourceFactory.PROPERTY_DATA_SOURCE_CLASS_NAME,
					ex.getUnknownPropertyName());
		}
	}

	/**
	 * Ensure handle invalid {@link DataSource} {@link Class}.
	 */
	public void testInvalidDataSourceClass() throws Exception {
		this.replayMockObjects();
		try {
			this.createCommonDataSource(this.createSourceContext(
					DefaultDataSourceFactory.PROPERTY_DATA_SOURCE_CLASS_NAME, Object.class.getName()));
		} catch (Exception ex) {
			assertEquals("Incorrect error",
					"Non " + this.getDataSourceType().getSimpleName() + " class configured: " + Object.class.getName(),
					ex.getMessage());
		}
		this.verifyMockObjects();
	}

	/**
	 * Ensure able to create the {@link DataSource}.
	 */
	public void testCreateEmptyDataSource() throws Exception {
		CommonDataSource dataSource = this.createCommonDataSource(this.createSourceContext(
				DefaultDataSourceFactory.PROPERTY_DATA_SOURCE_CLASS_NAME, MockDataSource.class.getName()));
		MockDataSource.assertConfiguration(dataSource, null, null, null, -1, null, null, null);
	}

	/**
	 * Ensure able to create configured {@link DataSource}.
	 */
	public void testCreateConfiguredDataSource() throws Exception {
		CommonDataSource dataSource = this.createCommonDataSource(this.createSourceContext(
				DefaultDataSourceFactory.PROPERTY_DATA_SOURCE_CLASS_NAME, MockDataSource.class.getName(), "driver",
				"net.officefloor.Driver", "url", "jdbc:/test", "serverName", "test", "port", "2323", "databaseName",
				"database", "username", "username", "password", "password"));
		MockDataSource.assertConfiguration(dataSource, "net.officefloor.Driver", "jdbc:/test", "test", 2323, "database",
				"username", "password");
	}

	/**
	 * Ensure able to create partially configured {@link DataSource}.
	 */
	public void testCreatePartiallyConfiguredDataSource() throws Exception {
		CommonDataSource dataSource = this.createCommonDataSource(this.createSourceContext(
				DefaultDataSourceFactory.PROPERTY_DATA_SOURCE_CLASS_NAME, MockDataSource.class.getName(), "driver",
				"net.officefloor.Driver", "url", "jdbc:/test", "username", "username", "password", "password"));
		MockDataSource.assertConfiguration(dataSource, "net.officefloor.Driver", "jdbc:/test", null, -1, null,
				"username", "password");
	}

	/**
	 * Creates the {@link SourceContext}.
	 * 
	 * @param propertyNameValuePairs {@link PropertyList} name/value pairs.
	 * @return {@link SourceContext}.
	 */
	private SourceContext createSourceContext(String... propertyNameValuePairs) {
		SourceContextImpl root = new SourceContextImpl("ROOT", false, null, this.getClass().getClassLoader(),
				new MockClockFactory());
		SourceProperties properties = new SourcePropertiesImpl(propertyNameValuePairs);
		return new SourceContextImpl("ROOT.DataSource", false, null, root, properties);
	}

}
