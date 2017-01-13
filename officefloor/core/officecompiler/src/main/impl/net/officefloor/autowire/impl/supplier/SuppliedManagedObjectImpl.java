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
package net.officefloor.autowire.impl.supplier;

import net.officefloor.autowire.supplier.SuppliedManagedObject;
import net.officefloor.autowire.supplier.SuppliedManagedObjectTeam;
import net.officefloor.compile.managedobject.ManagedObjectType;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;

/**
 * {@link SuppliedManagedObject} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class SuppliedManagedObjectImpl<D extends Enum<D>, F extends Enum<F>>
		implements SuppliedManagedObject<D, F> {

	/**
	 * {@link ManagedObjectType}.
	 */
	private final ManagedObjectType<D> managedObjectType;

	/**
	 * {@link ManagedObjectSource}.
	 */
	private final ManagedObjectSource<D, F> managedObjectSource;

	/**
	 * {@link PropertyList}.
	 */
	private final PropertyList properties;

	/**
	 * Timeout.
	 */
	private final long timeout;

	/**
	 * {@link SuppliedManagedObjectTeam} instances.
	 */
	private final SuppliedManagedObjectTeam[] teams;

	/**
	 * Initiate.
	 * 
	 * @param managedObjectType
	 *            {@link ManagedObjectType}.
	 * @param managedObjectSource
	 *            {@link ManagedObjectSource}.
	 * @param properties
	 *            {@link PropertyList}.
	 * @param timeout
	 *            Timeout.
	 * @param teams
	 *            {@link SuppliedManagedObjectTeam} instances.
	 */
	public SuppliedManagedObjectImpl(ManagedObjectType<D> managedObjectType,
			ManagedObjectSource<D, F> managedObjectSource,
			PropertyList properties, long timeout,
			SuppliedManagedObjectTeam[] teams) {
		this.managedObjectType = managedObjectType;
		this.managedObjectSource = managedObjectSource;
		this.properties = properties;
		this.timeout = timeout;
		this.teams = teams;
	}

	/*
	 * ======================== SuppliedManagedObject =========================
	 */

	@Override
	public ManagedObjectType<D> getManagedObjectType() {
		return this.managedObjectType;
	}

	@Override
	public ManagedObjectSource<D, F> getManagedObjectSource() {
		return this.managedObjectSource;
	}

	@Override
	public PropertyList getProperties() {
		return this.properties;
	}

	@Override
	public long getTimeout() {
		return this.timeout;
	}

	@Override
	public SuppliedManagedObjectTeam[] getSuppliedTeams() {
		return this.teams;
	}

}