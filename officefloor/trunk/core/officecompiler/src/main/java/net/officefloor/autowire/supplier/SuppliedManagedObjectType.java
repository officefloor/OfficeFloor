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
package net.officefloor.autowire.supplier;

import net.officefloor.autowire.AutoWire;
import net.officefloor.compile.managedobject.ManagedObjectFlowType;
import net.officefloor.frame.spi.team.Team;
import net.officefloor.plugin.section.clazz.ManagedObject;

/**
 * <code>Type definition</code> of a potentially supplied {@link ManagedObject}.
 * 
 * @author Daniel Sagenschneider
 */
public interface SuppliedManagedObjectType {

	/**
	 * Obtains the {@link AutoWire} instances that this {@link ManagedObject}
	 * may full fill.
	 * 
	 * @return {@link AutoWire} instances that this {@link ManagedObject} may
	 *         full fill.
	 */
	AutoWire[] getAutoWiring();

	/**
	 * Obtains the {@link SuppliedManagedObjectDependencyType} instances
	 * required for this {@link SuppliedManagedObjectType}.
	 * 
	 * @return {@link SuppliedManagedObjectDependencyType} instances required
	 *         for this {@link SuppliedManagedObjectType}.
	 */
	SuppliedManagedObjectDependencyType[] getDependencyTypes();

	/**
	 * Obtains the {@link SuppliedManagedObjectFlowType} definitions of the
	 * flows.
	 * 
	 * @return {@link ManagedObjectFlowType} definitions of the flows.
	 */
	SuppliedManagedObjectFlowType[] getFlowTypes();

	/**
	 * Obtains the {@link SuppliedManagedObjectTeamType} definitions of the
	 * required {@link Team} instances.
	 * 
	 * @return {@link SuppliedManagedObjectTeamType} definitions of the required
	 *         {@link Team} instances.
	 */
	SuppliedManagedObjectTeamType[] getTeamTypes();

}