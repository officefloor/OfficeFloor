/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2011 Daniel Sagenschneider
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
package net.officefloor.plugin.autowire;

import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;

/**
 * Object for configuring auto-wiring.
 * 
 * @author Daniel Sagenschneider
 */
public class AutoWireObjectImpl extends AutoWirePropertiesImpl implements
		AutoWireObject {

	/**
	 * {@link ManagedObjectSource} class.
	 */
	private final Class<?> managedObjectSourceClass;

	/**
	 * {@link ManagedObjectSourceWirer}.
	 */
	private final ManagedObjectSourceWirer wirer;

	/**
	 * Object types for linking this a dependency.
	 */
	private final Class<?>[] objectTypes;

	/**
	 * Time-out for sourcing the {@link ManagedObject} from the
	 * {@link ManagedObjectSource}.
	 */
	private long timeout = 0;

	/**
	 * Initiate.
	 * 
	 * @param compiler
	 *            {@link OfficeFloorCompiler}.
	 * @param managedObjectSourceClass
	 *            {@link ManagedObjectSource} class.
	 * @param properties
	 *            {@link PropertyList} for the {@link ManagedObjectSource}.
	 * @param wirer
	 *            {@link ManagedObjectSourceWirer}.
	 * @param objectTypes
	 *            Object types that the {@link ManagedObjectSource} is to
	 *            provide auto-wiring.
	 */
	public AutoWireObjectImpl(OfficeFloorCompiler compiler,
			Class<?> managedObjectSourceClass, PropertyList properties,
			ManagedObjectSourceWirer wirer, Class<?>... objectTypes) {
		super(compiler, properties);
		this.managedObjectSourceClass = managedObjectSourceClass;
		this.wirer = wirer;
		this.objectTypes = objectTypes;
	}

	/*
	 * ===================== AutoWireObject ============================
	 */

	@Override
	public Class<?> getManagedObjectSourceClass() {
		return this.managedObjectSourceClass;
	}

	@Override
	public ManagedObjectSourceWirer getManagedObjectSourceWirer() {
		return this.wirer;
	}

	@Override
	public Class<?>[] getObjectTypes() {
		return this.objectTypes;
	}

	@Override
	public long getTimeout() {
		return this.timeout;
	}

	@Override
	public void setTimeout(long timeout) {
		this.timeout = timeout;
	}

}