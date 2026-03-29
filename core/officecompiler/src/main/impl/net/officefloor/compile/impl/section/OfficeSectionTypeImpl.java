/*-
 * #%L
 * OfficeCompiler
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package net.officefloor.compile.impl.section;

import net.officefloor.compile.section.OfficeSectionInputType;
import net.officefloor.compile.section.OfficeSectionManagedObjectType;
import net.officefloor.compile.section.OfficeSectionObjectType;
import net.officefloor.compile.section.OfficeSectionOutputType;
import net.officefloor.compile.section.OfficeSectionType;
import net.officefloor.compile.section.OfficeSubSectionType;
import net.officefloor.compile.section.OfficeFunctionType;
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
	 * @param functions
	 *            {@link OfficeFunctionType} instances.
	 * @param managedObjects
	 *            {@link OfficeSectionManagedObjectType} instances.
	 */
	public void initialiseAsOfficeSubSectionType(OfficeSubSectionType parent, OfficeSubSectionType[] subSections,
			OfficeFunctionType[] functions, OfficeSectionManagedObjectType[] managedObjects) {
		this.subSectionState = new SubSectionState(parent, subSections, functions, managedObjects);
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
	public OfficeFunctionType[] getOfficeFunctionTypes() {
		return this.subSectionState.functions;
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
		 * {@link OfficeFunctionType} instances.
		 */
		private final OfficeFunctionType[] functions;

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
		 * @param functions
		 *            {@link OfficeFunctionType} instances.
		 * @param managedObjects
		 *            {@link OfficeSectionManagedObjectType} instances.
		 */
		public SubSectionState(OfficeSubSectionType parent, OfficeSubSectionType[] subSections,
				OfficeFunctionType[] functions, OfficeSectionManagedObjectType[] managedObjects) {
			this.parent = parent;
			this.subSections = subSections;
			this.functions = functions;
			this.managedObjects = managedObjects;
		}
	}

}
