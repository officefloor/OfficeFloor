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
package net.officefloor.compile.impl.section;

import net.officefloor.compile.section.OfficeSectionInputType;
import net.officefloor.compile.section.OfficeSectionManagedObjectType;
import net.officefloor.compile.section.OfficeSectionObjectType;
import net.officefloor.compile.section.OfficeSectionOutputType;
import net.officefloor.compile.section.OfficeSectionType;
import net.officefloor.compile.section.OfficeSubSectionType;
import net.officefloor.compile.section.OfficeTaskType;
import net.officefloor.compile.spi.office.OfficeSection;

/**
 * {@link OfficeSectionType} implementation.
 *
 * @author Daniel Sagenschneider
 */
public class OfficeSectionTypeImpl implements OfficeSectionType {

	/**
	 * Name of this {@link OfficeSection}.
	 */
	private final String name;

	/**
	 * {@link OfficeSectionInputType} instances.
	 */
	private final OfficeSectionInputType[] inputs;

	/**
	 * {@link OfficeSectionOutputType} instances.
	 */
	private final OfficeSectionOutputType[] outputs;

	/**
	 * {@link OfficeSectionObjectType} instances.
	 */
	private final OfficeSectionObjectType[] objects;

	/**
	 * State of the {@link OfficeSubSectionType}.
	 */
	private SubSectionState subSectionState = null;

	/**
	 * Instantiate.
	 * 
	 * @param name
	 *            Name of this {@link OfficeSection}.
	 * @param inputs
	 *            {@link OfficeSectionInputType} instances.
	 * @param outputs
	 *            {@link OfficeSectionOutputType} instances.
	 * @param objects
	 *            {@link OfficeSectionObjectType} instances.
	 */
	public OfficeSectionTypeImpl(String name, OfficeSectionInputType[] inputs, OfficeSectionOutputType[] outputs,
			OfficeSectionObjectType[] objects) {
		this.name = name;
		this.inputs = inputs;
		this.outputs = outputs;
		this.objects = objects;
	}

	/**
	 * Initialises the {@link OfficeSubSectionType} state.
	 * 
	 * @param parent
	 *            Parent {@link OfficeSubSectionType}.
	 * @param subSections
	 *            {@link OfficeSubSectionType} instances.
	 * @param tasks
	 *            {@link OfficeTaskType} instances.
	 * @param managedObjects
	 *            {@link OfficeSectionManagedObjectType} instances.
	 */
	public void initialiseAsOfficeSubSectionType(OfficeSubSectionType parent, OfficeSubSectionType[] subSections,
			OfficeTaskType[] tasks, OfficeSectionManagedObjectType[] managedObjects) {
		this.subSectionState = new SubSectionState(parent, subSections, tasks, managedObjects);
	}

	/*
	 * ===================== OfficeSectionType ==========================
	 */

	@Override
	public String getOfficeSectionName() {
		return this.name;
	}

	@Override
	public OfficeSubSectionType getParentOfficeSubSectionType() {
		return this.subSectionState.parent;
	}

	@Override
	public OfficeSubSectionType[] getOfficeSubSectionTypes() {
		return this.subSectionState.subSections;
	}

	@Override
	public OfficeTaskType[] getOfficeTaskTypes() {
		return this.subSectionState.tasks;
	}

	@Override
	public OfficeSectionManagedObjectType[] getOfficeSectionManagedObjectTypes() {
		return this.subSectionState.managedObjects;
	}

	@Override
	public OfficeSectionInputType[] getOfficeSectionInputTypes() {
		return this.inputs;
	}

	@Override
	public OfficeSectionOutputType[] getOfficeSectionOutputTypes() {
		return this.outputs;
	}

	@Override
	public OfficeSectionObjectType[] getOfficeSectionObjectTypes() {
		return this.objects;
	}

	/**
	 * {@link OfficeSubSectionType} state.
	 */
	private static class SubSectionState {

		/**
		 * Parent {@link OfficeSubSectionType}.
		 */
		private final OfficeSubSectionType parent;

		/**
		 * {@link OfficeSubSectionType} instances.
		 */
		private final OfficeSubSectionType[] subSections;

		/**
		 * {@link OfficeTaskType} instances.
		 */
		private final OfficeTaskType[] tasks;

		/**
		 * {@link OfficeSectionManagedObjectType} instances.
		 */
		private final OfficeSectionManagedObjectType[] managedObjects;

		/**
		 * Instantiate.
		 * 
		 * @param parent
		 *            Parent {@link OfficeSubSectionType}.
		 * @param subSections
		 *            {@link OfficeSubSectionType} instances.
		 * @param tasks
		 *            {@link OfficeTaskType} instances.
		 * @param managedObjects
		 *            {@link OfficeSectionManagedObjectType} instances.
		 */
		public SubSectionState(OfficeSubSectionType parent, OfficeSubSectionType[] subSections, OfficeTaskType[] tasks,
				OfficeSectionManagedObjectType[] managedObjects) {
			this.parent = parent;
			this.subSections = subSections;
			this.tasks = tasks;
			this.managedObjects = managedObjects;
		}
	}

}