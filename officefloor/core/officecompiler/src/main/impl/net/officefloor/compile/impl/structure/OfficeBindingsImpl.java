/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.officefloor.compile.impl.structure;

import java.util.HashSet;
import java.util.Set;

import net.officefloor.compile.internal.structure.BoundManagedObjectNode;
import net.officefloor.compile.internal.structure.InputManagedObjectNode;
import net.officefloor.compile.internal.structure.ManagedFunctionNode;
import net.officefloor.compile.internal.structure.ManagedObjectSourceNode;
import net.officefloor.compile.internal.structure.OfficeBindings;
import net.officefloor.compile.internal.structure.OfficeNode;
import net.officefloor.compile.internal.structure.CompileContext;
import net.officefloor.frame.api.build.OfficeBuilder;
import net.officefloor.frame.api.build.OfficeFloorBuilder;

/**
 * {@link OfficeBindings} implementation.
 *
 * @author Daniel Sagenschneider
 */
public class OfficeBindingsImpl implements OfficeBindings {

	/**
	 * {@link OfficeNode}.
	 */
	private final OfficeNode office;

	/**
	 * {@link OfficeBuilder}.
	 */
	private final OfficeBuilder officeBuilder;

	/**
	 * {@link OfficeFloorBuilder}.
	 */
	private final OfficeFloorBuilder officeFloorBuilder;

	/**
	 * {@link CompileContext}.
	 */
	private final CompileContext compileContext;

	/**
	 * Built {@link ManagedObjectSourceNode} instances.
	 */
	private final Set<ManagedObjectSourceNode> builtManagedObjectSources = new HashSet<ManagedObjectSourceNode>();

	/**
	 * Built {@link BoundManagedObjectNode} instances.
	 */
	private final Set<BoundManagedObjectNode> builtManagedObjects = new HashSet<BoundManagedObjectNode>();

	/**
	 * Built {@link InputManagedObjectNode} instances.
	 */
	private final Set<InputManagedObjectNode> builtInputManagedObjects = new HashSet<InputManagedObjectNode>();

	/**
	 * Built {@link ManagedFunctionNode} instances.
	 */
	private final Set<ManagedFunctionNode> builtManagedFunctions = new HashSet<ManagedFunctionNode>();

	/**
	 * Instantiates.
	 * 
	 * @param office             {@link OfficeNode}.
	 * @param officeBuilder      {@link OfficeBuilder}.
	 * @param officeFloorBuilder {@link OfficeFloorBuilder}.
	 * @param compileContext     {@link CompileContext}.
	 */
	public OfficeBindingsImpl(OfficeNode office, OfficeBuilder officeBuilder, OfficeFloorBuilder officeFloorBuilder,
			CompileContext compileContext) {
		this.office = office;
		this.officeBuilder = officeBuilder;
		this.officeFloorBuilder = officeFloorBuilder;
		this.compileContext = compileContext;
	}

	/*
	 * ================ OfficeBindings =======================
	 */

	@Override
	public void buildManagedObjectSourceIntoOffice(ManagedObjectSourceNode managedObjectSourceNode) {

		// Ensure not already built into Office
		if (this.builtManagedObjectSources.contains(managedObjectSourceNode)) {
			return; // already built into office
		}
		this.builtManagedObjectSources.add(managedObjectSourceNode);

		// Build the managed object source into the office
		managedObjectSourceNode.buildManagedObject(this.officeFloorBuilder, this.office, this.officeBuilder, this,
				this.compileContext);
	}

	@Override
	public void buildManagedObjectIntoOffice(BoundManagedObjectNode managedObjectNode) {

		// Ensure the managed object source is built into the office
		ManagedObjectSourceNode managedObjectSourceNode = managedObjectNode.getManagedObjectSourceNode();
		if (managedObjectSourceNode != null) {
			this.buildManagedObjectSourceIntoOffice(managedObjectSourceNode);
		}

		// Ensure not already built into Office
		if (this.builtManagedObjects.contains(managedObjectNode)) {
			return; // already built into office
		}
		this.builtManagedObjects.add(managedObjectNode);

		// Build the managed object into the office
		managedObjectNode.buildOfficeManagedObject(this.office, this.officeBuilder, this, this.compileContext);
	}

	@Override
	public void buildInputManagedObjectIntoOffice(InputManagedObjectNode inputManagedObjectNode) {

		// Ensure not already built into Office
		if (this.builtInputManagedObjects.contains(inputManagedObjectNode)) {
			return; // already built into office
		}
		this.builtInputManagedObjects.add(inputManagedObjectNode);

		// Build the Input managed object
		this.officeBuilder.setBoundInputManagedObject(inputManagedObjectNode.getBoundManagedObjectName(),
				inputManagedObjectNode.getBoundManagedObjectSourceNode().getQualifiedName());
	}

	@Override
	public void buildManagedFunctionIntoOffice(ManagedFunctionNode managedFunctionNode) {

		// Ensure not already built into Office
		if (this.builtManagedFunctions.contains(managedFunctionNode)) {
			return; // already built into office
		}
		this.builtManagedFunctions.add(managedFunctionNode);

		// Build the function
		managedFunctionNode.buildManagedFunction(this.officeBuilder, this.compileContext);
	}

}