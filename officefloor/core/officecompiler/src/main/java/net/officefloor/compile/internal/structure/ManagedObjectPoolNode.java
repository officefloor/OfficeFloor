/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2017 Daniel Sagenschneider
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
package net.officefloor.compile.internal.structure;

import net.officefloor.compile.issues.CompilerIssues;
import net.officefloor.compile.managedobject.ManagedObjectType;
import net.officefloor.compile.pool.ManagedObjectPoolType;
import net.officefloor.compile.spi.office.OfficeManagedObjectPool;
import net.officefloor.compile.spi.officefloor.OfficeFloorManagedObjectPool;
import net.officefloor.compile.spi.pool.source.ManagedObjectPoolSource;
import net.officefloor.compile.spi.section.SectionManagedObjectPool;
import net.officefloor.frame.api.build.ManagedObjectBuilder;
import net.officefloor.frame.api.managedobject.pool.ManagedObjectPool;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;

/**
 * Node representing instance use of a {@link ManagedObjectPool}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ManagedObjectPoolNode
		extends LinkPoolNode, OfficeFloorManagedObjectPool, OfficeManagedObjectPool, SectionManagedObjectPool {

	/**
	 * {@link Node} type.
	 */
	String TYPE = "Managed Object Pool";

	/**
	 * Initialises the {@link ManagedObjectPoolNode}.
	 * 
	 * @param managedObjectPoolSourceClassName
	 *            Class name of the {@link ManagedObjectPoolSource}.
	 * @param managedObjectPoolSource
	 *            Optional instantiated {@link ManagedObjectPoolSource}. May be
	 *            <code>null</code>.
	 */
	void initialise(String managedObjectPoolSourceClassName, ManagedObjectPoolSource managedObjectPoolSource);

	/**
	 * Loads the {@link ManagedObjectPoolType}.
	 * 
	 * @return {@link ManagedObjectPoolType} or <code>null</code> if issue
	 *         loading with issue reported to the {@link CompilerIssues}.
	 */
	ManagedObjectPoolType loadManagedObjectPoolType();

	/**
	 * Builds {@link ManagedObjectPool} for this {@link ManagedObjectPoolNode}.
	 *
	 * @param managedObjectBuilder
	 *            {@link ManagedObjectBuilder}.
	 * @param managedObjectType
	 *            {@link ManagedObjectType} of the {@link ManagedObjectSource}
	 *            being pooled.
	 * @param compileContext
	 *            {@link CompileContext}.
	 */
	void buildManagedObjectPool(ManagedObjectBuilder<?> managedObjectBuilder, ManagedObjectType<?> managedObjectType,
			CompileContext compileContext);

}