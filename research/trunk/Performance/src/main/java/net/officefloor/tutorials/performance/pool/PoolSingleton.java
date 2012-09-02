/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2012 Daniel Sagenschneider
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
package net.officefloor.tutorials.performance.pool;

import net.officefloor.frame.api.build.None;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.source.impl.AbstractManagedObjectSource;

/**
 * Singleton pool.
 * 
 * @author Daniel Sagenschneider
 */
public class PoolSingleton extends AbstractManagedObjectSource<None, None>
		implements ManagedObject {

	/**
	 * Indicates the number of pooled connections.
	 */
	public static final int NUMBER_OF_POOLED_CONNECTIONS = 100;

	/**
	 * Singleton {@link PooledDataSource}.
	 */
	private final static PooledDataSource singleton = new PooledDataSource(
			NUMBER_OF_POOLED_CONNECTIONS);

	/**
	 * Obtains the {@link PooledDataSource} singleton.
	 * 
	 * @return {@link PooledDataSource} singleton.
	 */
	public static PooledDataSource getPooledDataSource() {
		return singleton;
	}

	@Override
	protected void loadSpecification(SpecificationContext context) {
		// No properties required
	}

	@Override
	protected void loadMetaData(MetaDataContext<None, None> context)
			throws Exception {
		context.setObjectClass(PooledDataSource.class);
	}

	@Override
	protected ManagedObject getManagedObject() throws Throwable {
		return this;
	}

	@Override
	public Object getObject() throws Throwable {
		return singleton;
	}

}