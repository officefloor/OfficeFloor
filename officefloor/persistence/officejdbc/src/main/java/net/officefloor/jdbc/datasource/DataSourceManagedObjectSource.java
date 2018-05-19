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
package net.officefloor.jdbc.datasource;

import java.sql.Connection;

import javax.sql.DataSource;

import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.managedobject.source.impl.AbstractManagedObjectSource;

/**
 * {@link ManagedObjectSource} for {@link Connection}.
 * 
 * @author Daniel Sagenschneider
 */
public class DataSourceManagedObjectSource extends AbstractManagedObjectSource<None, None> {

	/**
	 * Allows overriding to configure a different {@link DataSourceFactory}.
	 * 
	 * @return {@link DataSourceFactory}.
	 */
	protected DataSourceFactory getDataSourceFactory() {
		return new DefaultDataSourceFactory();
	}

	/**
	 * {@link DataSource}.
	 */
	private DataSource dataSource;

	/**
	 * {@link ManagedObject} to provide {@link DataSource}.
	 */
	private final ManagedObject managedObject = () -> this.dataSource;

	/*
	 * ================== AbstractManagedObjectSource ===================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
	}

	@Override
	protected void loadMetaData(MetaDataContext<None, None> context) throws Exception {

		// Create the data source
		DataSourceFactory factory = this.getDataSourceFactory();
		this.dataSource = factory.createDataSource(context.getManagedObjectSourceContext());

		// Configure meta-data from data source implementation
		context.setObjectClass(this.dataSource.getClass());
	}

	@Override
	protected ManagedObject getManagedObject() throws Throwable {
		return this.managedObject;
	}

}