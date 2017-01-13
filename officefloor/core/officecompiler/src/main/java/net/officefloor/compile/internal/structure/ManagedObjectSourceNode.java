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
import net.officefloor.compile.managedobject.ManagedObjectType;
import net.officefloor.compile.officefloor.OfficeFloorManagedObjectSourceType;
import net.officefloor.compile.section.OfficeSectionManagedObjectSourceType;
import net.officefloor.compile.spi.office.OfficeManagedObjectSource;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.office.OfficeSectionManagedObjectSource;
import net.officefloor.compile.spi.officefloor.ManagingOffice;
import net.officefloor.compile.spi.officefloor.OfficeFloorManagedObjectSource;
import net.officefloor.compile.spi.section.SectionManagedObjectSource;
import net.officefloor.compile.type.TypeContext;
import net.officefloor.frame.api.build.OfficeBuilder;
import net.officefloor.frame.api.build.OfficeFloorBuilder;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;

/**
 * Node representing a {@link ManagedObjectSource}.
 *
 * @author Daniel Sagenschneider
 */
public interface ManagedObjectSourceNode extends Node,
		SectionManagedObjectSource, OfficeManagedObjectSource,
		OfficeSectionManagedObjectSource, OfficeFloorManagedObjectSource {

	/**
	 * {@link Node} type.
	 */
	String TYPE = "Managed Object Source";

	/**
	 * Initialises the {@link ManagedObjectSourceNode}.
	 * 
	 * @param managedObjectSourceClassName
	 *            Class name of the {@link ManagedObjectSource}.
	 * @param managedObjectSource
	 *            Optional instantiated {@link ManagedObjectSource}. May be
	 *            <code>null</code>.
	 */
	void initialise(String managedObjectSourceClassName,
			ManagedObjectSource<?, ?> managedObjectSource);

	/**
	 * Indicates if have a {@link ManagedObjectSource} configured.
	 * 
	 * @return <code>true</code> if have the {@link ManagedObjectSource}
	 *         configured.
	 */
	@Deprecated
	// determine another means than exposing the method (such as isInitialised)
	boolean hasManagedObjectSource();

	/**
	 * Loads the {@link ManagedObjectType}.
	 * 
	 * @return {@link ManagedObjectType} or <code>null</code> if issue loading
	 *         with issue reported to the {@link CompilerIssues}.
	 */
	ManagedObjectType<?> loadManagedObjectType();

	/**
	 * Loads the {@link OfficeSectionManagedObjectSourceType}.
	 * 
	 * @param typeContext
	 *            {@link TypeContext}.
	 * @return {@link OfficeSectionManagedObjectSourceType} or <code>null</code>
	 *         if issue loading with issue reported to the
	 *         {@link CompilerIssues}.
	 */
	OfficeSectionManagedObjectSourceType loadOfficeSectionManagedObjectSourceType(
			TypeContext typeContext);

	/**
	 * Loads the {@link OfficeFloorManagedObjectSourceType}.
	 * 
	 * @param typeContext
	 *            {@link TypeContext}.
	 * @return {@link OfficeFloorManagedObjectSourceType} or <code>null</code>
	 *         if issue loading with issue reported to the
	 *         {@link CompilerIssues}.
	 */
	OfficeFloorManagedObjectSourceType loadOfficeFloorManagedObjectSourceType(
			TypeContext typeContext);

	/**
	 * Obtains the name that this {@link ManagedObjectSource} was added to the
	 * {@link OfficeFloor}.
	 *
	 * @return Name that this {@link ManagedObjectSource} was added to the
	 *         {@link OfficeFloor}.
	 */
	String getManagedObjectSourceName();

	/**
	 * Obtains the {@link SectionNode} containing this
	 * {@link ManagedObjectSourceNode}.
	 * 
	 * @return {@link SectionNode} containing this
	 *         {@link ManagedObjectSourceNode}. May be <code>null</code> if not
	 *         contained within an {@link OfficeSection} (in other words
	 *         included above the {@link SectionNode} instances).
	 */
	SectionNode getSectionNode();

	/**
	 * Obtains the {@link OfficeNode} containing this
	 * {@link ManagedObjectSourceNode}.
	 * 
	 * @return {@link OfficeNode} containing this
	 *         {@link ManagedObjectSourceNode}. May be <code>null</code> if not
	 *         contained within an {@link Office} (in other words included above
	 *         the {@link OfficeNode} instances).
	 */
	OfficeNode getOfficeNode();

	/**
	 * Obtains the {@link OfficeFloorNode} containing this
	 * {@link ManagedObjectSourceNode}.
	 * 
	 * @return {@link OfficeFloorNode} containing this
	 *         {@link ManagedObjectSourceNode}.
	 */
	OfficeFloorNode getOfficeFloorNode();

	/**
	 * Obtains the {@link OfficeNode} of the {@link ManagingOffice} for this
	 * {@link ManagedObjectSource}.
	 *
	 * @return {@link OfficeNode} of the {@link ManagingOffice} for this
	 *         {@link ManagedObjectSource} or <code>null</code> if can not
	 *         obtain it.
	 */
	OfficeNode getManagingOfficeNode();

	/**
	 * Links the {@link InputManagedObjectNode} for this
	 * {@link ManagedObjectSource}.
	 *
	 * @param inputManagedObject
	 *            {@link InputManagedObjectNode}.
	 * @return <code>true</code> if linked.
	 */
	boolean linkInputManagedObjectNode(InputManagedObjectNode inputManagedObject);

	/**
	 * Obtains the {@link InputManagedObjectNode} for this
	 * {@link ManagedObjectSource}.
	 *
	 * @return {@link InputManagedObjectNode} for this
	 *         {@link ManagedObjectSource} or <code>null</code> if can not
	 *         obtain it.
	 */
	InputManagedObjectNode getInputManagedObjectNode();

	/**
	 * Builds {@link ManagedObjectSource} for this {@link ManagedObjectNode}.
	 *
	 * @param builder
	 *            {@link OfficeFloorBuilder}.
	 * @param managingOffice
	 *            {@link OfficeNode} of the {@link ManagingOffice} for this
	 *            {@link ManagedObjectSource}.
	 * @param managingOfficeBuilder
	 *            {@link OfficeBuilder} for the {@link ManagingOffice}.
	 * @param officeBindings
	 *            {@link OfficeBindings}.
	 * @param typeContext
	 *            {@link TypeContext}.
	 */
	void buildManagedObject(OfficeFloorBuilder builder,
			OfficeNode managingOffice, OfficeBuilder managingOfficeBuilder,
			OfficeBindings officeBindings, TypeContext typeContext);

}