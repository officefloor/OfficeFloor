/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2009 Daniel Sagenschneider
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
package net.officefloor.frame.impl.construct.managedobjectsource;

import java.util.Properties;

import net.officefloor.frame.api.build.ManagedObjectBuilder;
import net.officefloor.frame.api.build.ManagingOfficeBuilder;
import net.officefloor.frame.internal.configuration.ManagedObjectSourceConfiguration;
import net.officefloor.frame.internal.configuration.ManagingOfficeConfiguration;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.pool.ManagedObjectPool;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;

/**
 * Implements the {@link ManagedObjectBuilder}.
 * 
 * @author Daniel Sagenschneider
 */
public class ManagedObjectBuilderImpl<D extends Enum<D>, F extends Enum<F>, MS extends ManagedObjectSource<D, F>>
		implements ManagedObjectBuilder<F>,
		ManagedObjectSourceConfiguration<F, MS> {

	/**
	 * Name of {@link ManagedObjectSource}.
	 */
	private final String managedObjectSourceName;

	/**
	 * {@link Class} of the {@link ManagedObjectSource}.
	 */
	private final Class<MS> managedObjectSourceClass;

	/**
	 * {@link ManagingOfficeConfiguration} for this {@link ManagedObject}.
	 */
	private ManagingOfficeConfiguration<F> managingOfficeConfiguration;

	/**
	 * {@link Properties} for the {@link ManagedObjectSource}.
	 */
	private final Properties properties = new Properties();

	/**
	 * {@link ManagedObjectPool}.
	 */
	private ManagedObjectPool pool;

	/**
	 * Default timeout for asynchronous operations on the {@link ManagedObject}.
	 */
	private long defaultTimeout = 0;

	/**
	 * Initiate.
	 * 
	 * @param managedObjectSourceName
	 *            Name of the {@link ManagedObjectSource}.
	 * @param managedObjectSourceClass
	 *            {@link Class} of the {@link ManagedObjectSource}.
	 */
	public ManagedObjectBuilderImpl(String managedObjectSourceName,
			Class<MS> managedObjectSourceClass) {
		this.managedObjectSourceName = managedObjectSourceName;
		this.managedObjectSourceClass = managedObjectSourceClass;
	}

	/*
	 * ================= ManagedObjectBuilder =============================
	 */

	@Override
	public void addProperty(String name, String value) {
		this.properties.put(name, value);
	}

	@Override
	public void setManagedObjectPool(ManagedObjectPool pool) {
		this.pool = pool;
	}

	@Override
	public void setDefaultTimeout(long timeout) {
		this.defaultTimeout = timeout;
	}

	@Override
	public ManagingOfficeBuilder<F> setManagingOffice(String officeName) {
		ManagingOfficeBuilderImpl<F> managingOfficeBuilder = new ManagingOfficeBuilderImpl<F>(
				officeName);
		this.managingOfficeConfiguration = managingOfficeBuilder;
		return managingOfficeBuilder;
	}

	/*
	 * ================= ManagedObjectSourceConfiguration =================
	 */

	@Override
	public String getManagedObjectSourceName() {
		return this.managedObjectSourceName;
	}

	@Override
	public ManagingOfficeConfiguration<F> getManagingOfficeConfiguration() {
		return this.managingOfficeConfiguration;
	}

	@Override
	public Class<MS> getManagedObjectSourceClass() {
		return this.managedObjectSourceClass;
	}

	@Override
	public Properties getProperties() {
		return this.properties;
	}

	@Override
	public ManagedObjectPool getManagedObjectPool() {
		return this.pool;
	}

	@Override
	public long getDefaultTimeout() {
		return this.defaultTimeout;
	}

}