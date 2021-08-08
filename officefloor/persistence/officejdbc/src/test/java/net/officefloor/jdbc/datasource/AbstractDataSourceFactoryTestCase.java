/*-
 * #%L
 * JDBC Persistence
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
