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