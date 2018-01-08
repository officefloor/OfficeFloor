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
package net.officefloor.frame.impl.construct.managedobjectsource;

import net.officefloor.frame.api.build.ManagedObjectBuilder;
import net.officefloor.frame.api.build.ManagedObjectPoolBuilder;
import net.officefloor.frame.api.build.ManagingOfficeBuilder;
import net.officefloor.frame.api.managedobject.AsynchronousManagedObject;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.pool.ManagedObjectPoolFactory;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.source.SourceProperties;
import net.officefloor.frame.impl.construct.managedobjectpool.ManagedObjectPoolBuilderImpl;
import net.officefloor.frame.impl.construct.source.SourcePropertiesImpl;
import net.officefloor.frame.internal.configuration.ManagedObjectPoolConfiguration;
import net.officefloor.frame.internal.configuration.ManagedObjectSourceConfiguration;
import net.officefloor.frame.internal.configuration.ManagingOfficeConfiguration;

/**
 * Implements the {@link ManagedObjectBuilder}.
 * 
 * @author Daniel Sagenschneider
 */
public class ManagedObjectBuilderImpl<O extends Enum<O>, F extends Enum<F>, MS extends ManagedObjectSource<O, F>>
		implements ManagedObjectBuilder<F>, ManagedObjectSourceConfiguration<F, MS> {

	/**
	 * Name of {@link ManagedObjectSource}.
	 */
	private final String managedObjectSourceName;

	/**
	 * {@link ManagedObjectSource} instance.
	 */
	private final MS managedObjectSourceInstance;

	/**
	 * {@link Class} of the {@link ManagedObjectSource}.
	 */
	private final Class<MS> managedObjectSourceClass;

	/**
	 * {@link ManagingOfficeConfiguration} for this {@link ManagedObject}.
	 */
	private ManagingOfficeConfiguration<F> managingOfficeConfiguration;

	/**
	 * {@link SourceProperties} for the {@link ManagedObjectSource}.
	 */
	private final SourcePropertiesImpl properties = new SourcePropertiesImpl();

	/**
	 * {@link ManagedObjectPoolConfiguration}.
	 */
	private ManagedObjectPoolConfiguration poolConfiguration;

	/**
	 * Timeout for {@link AsynchronousManagedObject}.
	 */
	private long timeout = 0;

	/**
	 * Initiate.
	 * 
	 * @param managedObjectSourceName
	 *            Name of the {@link ManagedObjectSource}.
	 * @param managedObjectSourceClass
	 *            {@link Class} of the {@link ManagedObjectSource}.
	 */
	public ManagedObjectBuilderImpl(String managedObjectSourceName, Class<MS> managedObjectSourceClass) {
		this.managedObjectSourceName = managedObjectSourceName;
		this.managedObjectSourceInstance = null;
		this.managedObjectSourceClass = managedObjectSourceClass;
	}

	/**
	 * Initiate.
	 * 
	 * @param managedObjectSourceName
	 *            Name of the {@link ManagedObjectSource}.
	 * @param managedObjectSource
	 *            {@link ManagedObjectSource} instance to use.
	 */
	public ManagedObjectBuilderImpl(String managedObjectSourceName, MS managedObjectSource) {
		this.managedObjectSourceName = managedObjectSourceName;
		this.managedObjectSourceInstance = managedObjectSource;
		this.managedObjectSourceClass = null;
	}

	/*
	 * ================= ManagedObjectBuilder =============================
	 */

	@Override
	public void addProperty(String name, String value) {
		this.properties.addProperty(name, value);
	}

	@Override
	public ManagedObjectPoolBuilder setManagedObjectPool(ManagedObjectPoolFactory managedObjectPoolFactory) {
		ManagedObjectPoolBuilderImpl poolBuilder = new ManagedObjectPoolBuilderImpl(managedObjectPoolFactory);
		this.poolConfiguration = poolBuilder;
		return poolBuilder;
	}

	@Override
	public void setTimeout(long timeout) {
		this.timeout = timeout;
	}

	@Override
	public ManagingOfficeBuilder<F> setManagingOffice(String officeName) {
		ManagingOfficeBuilderImpl<F> managingOfficeBuilder = new ManagingOfficeBuilderImpl<F>(officeName);
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
	public MS getManagedObjectSource() {
		return this.managedObjectSourceInstance;
	}

	@Override
	public Class<MS> getManagedObjectSourceClass() {
		return this.managedObjectSourceClass;
	}

	@Override
	public SourceProperties getProperties() {
		return this.properties;
	}

	@Override
	public ManagedObjectPoolConfiguration getManagedObjectPoolConfiguration() {
		return this.poolConfiguration;
	}

	@Override
	public long getTimeout() {
		return this.timeout;
	}

}