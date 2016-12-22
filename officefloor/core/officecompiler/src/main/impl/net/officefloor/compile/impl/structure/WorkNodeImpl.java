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

import net.officefloor.compile.internal.structure.Node;
import net.officefloor.compile.internal.structure.NodeContext;
import net.officefloor.compile.internal.structure.SectionNode;
import net.officefloor.compile.internal.structure.TaskRegistry;
import net.officefloor.compile.internal.structure.WorkNode;
import net.officefloor.compile.managedfunction.ManagedFunctionLoader;
import net.officefloor.compile.managedfunction.FunctionNamespaceType;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionSource;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.section.SectionTask;
import net.officefloor.compile.spi.section.SectionWork;
import net.officefloor.compile.type.TypeContext;
import net.officefloor.frame.api.build.OfficeBuilder;
import net.officefloor.frame.api.build.WorkBuilder;

/**
 * {@link WorkNode} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class WorkNodeImpl implements WorkNode {

	/**
	 * Name of this {@link SectionWork}.
	 */
	private final String workName;

	/**
	 * {@link PropertyList}.
	 */
	private final PropertyList propertyList;

	/**
	 * {@link OfficeSection} containing this {@link WorkNode}.
	 */
	private final SectionNode section;

	/**
	 * {@link TaskRegistry}.
	 */
	private final TaskRegistry taskRegistry;

	/**
	 * {@link NodeContext}.
	 */
	private final NodeContext context;

	/**
	 * Initialised state.
	 */
	private InitialisedState state;

	/**
	 * Initialised state.
	 */
	private static class InitialisedState {

		/**
		 * Class name of the {@link ManagedFunctionSource}.
		 */
		private final String workSourceClassName;

		/**
		 * {@link ManagedFunctionSource} instance to use. If this is specified it overrides
		 * using the {@link Class} name.
		 */
		@SuppressWarnings("unused")
		private final ManagedFunctionSource<?> workSource;

		/**
		 * Instantiate.
		 * 
		 * @param workSourceClassName
		 *            Class name of the {@link ManagedFunctionSource}.
		 * @param workSource
		 *            {@link ManagedFunctionSource} instance to use. If this is specified
		 *            it overrides using the {@link Class} name.
		 */
		public InitialisedState(String workSourceClassName,
				ManagedFunctionSource<?> workSource) {
			this.workSourceClassName = workSourceClassName;
			this.workSource = workSource;
		}
	}

	/**
	 * Initial {@link SectionTask} for this {@link SectionWork}.
	 */
	private SectionTask initialTask = null;

	/**
	 * Instantiate.
	 * 
	 * @param workName
	 *            Name of this {@link SectionWork}.
	 * @param section
	 *            {@link OfficeSection} containing this {@link WorkNode}.
	 * @param context
	 *            {@link NodeContext}.
	 */
	public WorkNodeImpl(String workName, SectionNode section,
			NodeContext context) {
		this.workName = workName;
		this.section = section;
		this.taskRegistry = section;
		this.context = context;

		// Create additional objects
		this.propertyList = this.context.createPropertyList();
	}

	/*
	 * ======================== Node ============================
	 */

	@Override
	public String getNodeName() {
		return this.workName;
	}

	@Override
	public String getNodeType() {
		return TYPE;
	}

	@Override
	public String getLocation() {
		return null;
	}

	@Override
	public Node getParentNode() {
		return this.section;
	}

	@Override
	public boolean isInitialised() {
		return (this.state != null);
	}

	@Override
	public void initialise(String workSourceClassName, ManagedFunctionSource<?> workSource) {
		this.state = NodeUtil.initialise(this, this.context, this.state,
				() -> new InitialisedState(workSourceClassName, workSource));
	}

	/*
	 * ======================== SectionWork ============================
	 */

	@Override
	public String getSectionWorkName() {
		return this.workName;
	}

	@Override
	public void addProperty(String name, String value) {
		this.propertyList.addProperty(name).setValue(value);
	}

	@Override
	public SectionTask addSectionTask(String taskName, String taskTypeName) {
		return this.taskRegistry.addTaskNode(taskName, taskTypeName, this);
	}

	@Override
	public void setInitialTask(SectionTask task) {
		this.initialTask = task;
	}

	/*
	 * ===================== WorkNode ===================================
	 */

	@Override
	public SectionNode getSectionNode() {
		return this.section;
	}

	@Override
	public String getQualifiedWorkName() {
		return this.section.getSectionQualifiedName(this.workName);
	}

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public FunctionNamespaceType<?> loadWorkType() {

		// Obtain the work source class
		Class<? extends ManagedFunctionSource> workSourceClass = this.context
				.getWorkSourceClass(this.state.workSourceClassName, this);
		if (workSourceClass == null) {
			return null; // must obtain work source class
		}

		// Load and return the work type
		ManagedFunctionLoader workLoader = this.context.getWorkLoader(this);
		return workLoader.loadFunctionNamespaceType(workSourceClass, this.propertyList);
	}

	@Override
	public WorkBuilder<?> buildWork(OfficeBuilder builder,
			TypeContext typeContext) {

		// Obtain the work type
		FunctionNamespaceType<?> workType = typeContext.getOrLoadWorkType(this);
		if (workType == null) {
			return null; // must have WorkType to build work
		}

		// Obtain the fully qualified work name
		String fullyQualifiedWorkName = this.section
				.getSectionQualifiedName(this.workName);

		// Build the work
		WorkBuilder<?> workBuilder = builder.addWork(fullyQualifiedWorkName,
				workType.getWorkFactory());

		// Determine if initial task for work
		if (this.initialTask != null) {
			// Specify initial task for work
			String initialTaskName = this.initialTask.getSectionTaskName();
			workBuilder.setInitialTask(initialTaskName);
		}

		// Return the work builder
		return workBuilder;
	}

}