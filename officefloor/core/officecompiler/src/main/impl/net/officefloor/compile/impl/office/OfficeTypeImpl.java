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

package net.officefloor.compile.impl.office;

import net.officefloor.compile.office.OfficeInputType;
import net.officefloor.compile.office.OfficeManagedObjectType;
import net.officefloor.compile.office.OfficeOutputType;
import net.officefloor.compile.office.OfficeAvailableSectionInputType;
import net.officefloor.compile.office.OfficeTeamType;
import net.officefloor.compile.office.OfficeType;

/**
 * {@link OfficeType} implementation.
 *
 * @author Daniel Sagenschneider
 */
public class OfficeTypeImpl implements OfficeType {

	/**
	 * {@link OfficeInputType} instances.
	 */
	private final OfficeInputType[] inputs;

	/**
	 * {@link OfficeOutputType} instances.
	 */
	private final OfficeOutputType[] outputs;

	/**
	 * {@link OfficeTeamType} instances.
	 */
	private final OfficeTeamType[] teams;

	/**
	 * {@link OfficeManagedObjectType} instances.
	 */
	private final OfficeManagedObjectType[] managedObjects;

	/**
	 * {@link OfficeAvailableSectionInputType} instances.
	 */
	private final OfficeAvailableSectionInputType[] sectionInputs;

	/**
	 * Initiate.
	 * 
	 * @param inputs
	 *            {@link OfficeInputType} instances.
	 * @param outputs
	 *            {@link OfficeOutputType} instances.
	 * @param teams
	 *            {@link OfficeTeamType} instances.
	 * @param managedObjects
	 *            {@link OfficeManagedObjectType} instances.
	 * @param sectionInputs
	 *            {@link OfficeAvailableSectionInputType} instances.
	 */
	public OfficeTypeImpl(OfficeInputType[] inputs, OfficeOutputType[] outputs,
			OfficeTeamType[] teams, OfficeManagedObjectType[] managedObjects,
			OfficeAvailableSectionInputType[] sectionInputs) {
		this.inputs = inputs;
		this.outputs = outputs;
		this.teams = teams;
		this.managedObjects = managedObjects;
		this.sectionInputs = sectionInputs;
	}

	/**
	 * =================== OfficeType =========================
	 */

	@Override
	public OfficeInputType[] getOfficeInputTypes() {
		return this.inputs;
	}

	@Override
	public OfficeOutputType[] getOfficeOutputTypes() {
		return this.outputs;
	}

	@Override
	public OfficeTeamType[] getOfficeTeamTypes() {
		return this.teams;
	}

	@Override
	public OfficeManagedObjectType[] getOfficeManagedObjectTypes() {
		return this.managedObjects;
	}

	@Override
	public OfficeAvailableSectionInputType[] getOfficeSectionInputTypes() {
		return this.sectionInputs;
	}

}
