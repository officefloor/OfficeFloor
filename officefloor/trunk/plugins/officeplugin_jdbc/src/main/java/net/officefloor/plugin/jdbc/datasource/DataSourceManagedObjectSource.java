/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2013 Daniel Sagenschneider
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
package net.officefloor.plugin.jdbc.datasource;

import java.util.Properties;

import javax.sql.DataSource;

import net.officefloor.frame.api.build.None;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceContext;
import net.officefloor.frame.spi.managedobject.source.impl.AbstractManagedObjectSource;
import net.officefloor.plugin.jdbc.util.ReflectionUtil;

/**
 * {@link ManagedObjectSource} for a {@link DataSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class DataSourceManagedObjectSource extends
		AbstractManagedObjectSource<None, None> implements ManagedObject {

	/**
	 * Property name of the {@link DataSource} class name.
	 */
	public static final String PROPERTY_DATA_SOURCE_CLASS_NAME = "data.source.class.name";

	/**
	 * {@link DataSource}.
	 */
	private DataSource dataSource;

	/*
	 * ======================== ManagedObjectSource ===================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		context.addProperty(PROPERTY_DATA_SOURCE_CLASS_NAME, DataSource.class
				.getSimpleName());
	}

	@Override
	protected void loadMetaData(MetaDataContext<None, None> context)
			throws Exception {
		ManagedObjectSourceContext<None> mosContext = context
				.getManagedObjectSourceContext();

		// Obtain the DataSource class name
		String dataSourceClassName = mosContext
				.getProperty(PROPERTY_DATA_SOURCE_CLASS_NAME);

		// Initiate the DataSource
		ClassLoader classLoader = mosContext.getClassLoader();
		Properties properties = mosContext.getProperties();
		this.dataSource = ReflectionUtil.createInitialisedBean(
				dataSourceClassName, classLoader, DataSource.class, properties);

		// Specify types
		context.setObjectClass(DataSource.class);
		context.setManagedObjectClass(this.getClass());
	}

	@Override
	protected ManagedObject getManagedObject() throws Throwable {
		return this;
	}

	/*
	 * ========================== ManagedObject ===============================
	 */

	@Override
	public Object getObject() throws Throwable {
		return this.dataSource;
	}

}