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
package net.officefloor.compile.impl.structure;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.officefloor.compile.internal.structure.BoundManagedObjectNode;
import net.officefloor.compile.internal.structure.InputManagedObjectNode;
import net.officefloor.compile.internal.structure.ManagedObjectSourceNode;
import net.officefloor.compile.internal.structure.OfficeBindings;
import net.officefloor.compile.internal.structure.OfficeNode;
import net.officefloor.compile.internal.structure.TaskNode;
import net.officefloor.compile.internal.structure.WorkNode;
import net.officefloor.compile.type.TypeContext;
import net.officefloor.frame.api.build.OfficeBuilder;
import net.officefloor.frame.api.build.OfficeFloorBuilder;
import net.officefloor.frame.api.build.WorkBuilder;
import net.officefloor.frame.api.manage.Office;

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
	 * {@link TypeContext}.
	 */
	private final TypeContext typeContext;

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
	 * {@link WorkBuilder} instances by their {@link WorkNode}.
	 */
	private final Map<WorkNode, WorkBuilder<?>> workBuilders = new HashMap<WorkNode, WorkBuilder<?>>();

	/**
	 * Built {@link TaskNode} instances.
	 */
	private final Set<TaskNode> builtTasks = new HashSet<TaskNode>();

	/**
	 * Instantiates.
	 * 
	 * @param office
	 *            {@link OfficeNode}.
	 * @param officeBuilder
	 *            {@link OfficeBuilder}.
	 * @param officeFloorBuilder
	 *            {@link OfficeFloorBuilder}.
	 * @param typeContext
	 *            {@link TypeContext}.
	 */
	public OfficeBindingsImpl(OfficeNode office, OfficeBuilder officeBuilder,
			OfficeFloorBuilder officeFloorBuilder, TypeContext typeContext) {
		this.office = office;
		this.officeBuilder = officeBuilder;
		this.officeFloorBuilder = officeFloorBuilder;
		this.typeContext = typeContext;
	}

	/**
	 * Builds the {@link ManagedObjectSourceNode} into the {@link Office}.
	 * 
	 * @param managedObjectSourceNode
	 *            {@link ManagedObjectSourceNode}.
	 */
	private void buildManagedObjectSourceIntoOffice(
			ManagedObjectSourceNode managedObjectSourceNode) {
		if (this.builtManagedObjectSources.contains(managedObjectSourceNode)) {
			return; // already built into office
		}
		managedObjectSourceNode.buildManagedObject(this.officeFloorBuilder,
				this.office, this.officeBuilder, this, this.typeContext);
		this.builtManagedObjectSources.add(managedObjectSourceNode);
	}

	/**
	 * Builds the {@link WorkNode} into the {@link Office}.
	 * 
	 * @param workNode
	 *            {@link WorkNode}.
	 * @return {@link WorkBuilder} for the {@link WorkNode}.
	 */
	private WorkBuilder<?> buildWorkIntoOffice(WorkNode workNode) {
		WorkBuilder<?> workBuilder = this.workBuilders.get(workNode);
		if (workBuilder == null) {
			workBuilder = workNode.buildWork(this.officeBuilder,
					this.typeContext);
			this.workBuilders.put(workNode, workBuilder);
		}
		return workBuilder;
	}

	/*
	 * ================ OfficeBindings =======================
	 */

	@Override
	public void buildManagedObjectIntoOffice(
			BoundManagedObjectNode managedObjectNode) {

		// Ensure the managed object source is built into the office
		this.buildManagedObjectSourceIntoOffice(managedObjectNode
				.getManagedObjectSourceNode());

		// Build the managed object into the office
		if (this.builtManagedObjects.contains(managedObjectNode)) {
			return; // already built into office
		}
		managedObjectNode.buildOfficeManagedObject(this.office,
				this.officeBuilder, this, this.typeContext);
		this.builtManagedObjects.add(managedObjectNode);
	}

	@Override
	public void buildInputManagedObjectIntoOffice(
			InputManagedObjectNode inputManagedObjectNode) {
		if (this.builtInputManagedObjects.contains(inputManagedObjectNode)) {
			return; // already built into office
		}
		this.officeBuilder
				.setBoundInputManagedObject(inputManagedObjectNode
						.getBoundManagedObjectName(), inputManagedObjectNode
						.getBoundManagedObjectSourceNode()
						.getManagedObjectSourceName());
		this.builtInputManagedObjects.add(inputManagedObjectNode);
	}

	@Override
	public void buildTaskIntoOffice(TaskNode taskNode) {

		// Ensure the work is built
		WorkBuilder<?> workBuilder = this.buildWorkIntoOffice(taskNode
				.getWorkNode());
		if (workBuilder == null) {
			return; // must have the work
		}

		// Build the task into the office
		if (this.builtTasks.contains(taskNode)) {
			return; // already built into office
		}
		taskNode.buildTask(workBuilder, this.typeContext);
		this.builtTasks.add(taskNode);
	}

}