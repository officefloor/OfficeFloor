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
package net.officefloor.compile.internal.structure;

import net.officefloor.compile.issues.CompilerIssues;
import net.officefloor.compile.section.OfficeSectionManagedObjectType;
import net.officefloor.compile.section.TypeQualification;
import net.officefloor.compile.spi.office.ExecutionManagedObject;
import net.officefloor.compile.spi.office.OfficeManagedObject;
import net.officefloor.compile.spi.office.OfficeSectionManagedObject;
import net.officefloor.compile.spi.officefloor.OfficeFloorManagedObject;
import net.officefloor.compile.spi.section.SectionManagedObject;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.internal.structure.ManagedObjectScope;

/**
 * Node representing an instance use of a {@link ManagedObject}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ManagedObjectNode extends DependentObjectNode, BoundManagedObjectNode, SectionManagedObject,
		OfficeSectionManagedObject, OfficeManagedObject, OfficeFloorManagedObject {

	/**
	 * {@link Node} type.
	 */
	String TYPE = "Managed Object";

	/**
	 * Initialises the {@link ManagedObjectNode}.
	 * 
	 * @param managedObjectScope
	 *            {@link ManagedObjectScope} for the {@link ManagedObject}.
	 * @param managedObjectSourceNode
	 *            {@link ManagedObjectSourceNode} for the
	 *            {@link ManagedObjectNode}.
	 */
	void initialise(ManagedObjectScope managedObjectScope, ManagedObjectSourceNode managedObjectSourceNode);

	/**
	 * Sources the {@link ManagedObject}.
	 * 
	 * @param compileContext
	 *            {@link CompileContext}.
	 * @return <code>true</code> if successfully sourced the
	 *         {@link ManagedObject}. <code>false</code> if failed to source,
	 *         with issures reported to the {@link CompilerIssues}.
	 */
	boolean sourceManagedObject(CompileContext compileContext);

	/**
	 * Obtains the {@link ManagedObjectSourceNode} for this
	 * {@link ManagedObjectNode}.
	 * 
	 * @return {@link ManagedObjectSourceNode} for this
	 *         {@link ManagedObjectNode}.
	 */
	ManagedObjectSourceNode getManagedObjectSourceNode();

	/**
	 * Obtains the {@link TypeQualification} instances for the
	 * {@link ManagedObject}.
	 * 
	 * @param compileContext
	 *            {@link CompileContext}.
	 * @return {@link TypeQualification} instances for the
	 *         {@link ManagedObject}.
	 */
	TypeQualification[] getTypeQualifications(CompileContext compileContext);

	/**
	 * Obtains the {@link ManagedObjectDependencyNode} instances.
	 * 
	 * @return {@link ManagedObjectDependencyNode} instances.
	 */
	ManagedObjectDependencyNode[] getManagedObjectDepdendencies();

	/**
	 * Auto-wires the dependencies for the {@link ManagedObject}.
	 * 
	 * @param autoWirer
	 *            {@link AutoWirer}.
	 * @param office
	 *            {@link OfficeNode} requiring the auto-wiring.
	 * @param compileContext
	 *            {@link CompileContext}.
	 */
	void autoWireDependencies(AutoWirer<LinkObjectNode> autoWirer, OfficeNode office, CompileContext compileContext);

	/**
	 * Creates the {@link ExecutionManagedObject} for this
	 * {@link ManagedObjectNode}.
	 * 
	 * @param compileContext
	 *            {@link CompileContext}.
	 * @return {@link ExecutionManagedObject} for this
	 *         {@link ManagedObjectNode}.
	 */
	ExecutionManagedObject createExecutionManagedObject(CompileContext compileContext);

	/**
	 * Loads the {@link OfficeSectionManagedObjectType}.
	 * 
	 * @param compileContext
	 *            {@link CompileContext}.
	 * @return {@link OfficeSectionManagedObjectType} or <code>null</code> with
	 *         issues reported to the {@link CompilerIssues}.
	 */
	OfficeSectionManagedObjectType loadOfficeSectionManagedObjectType(CompileContext compileContext);

}