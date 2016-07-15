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
import net.officefloor.compile.section.OfficeSectionManagedObjectSourceType;
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
	 * {@link OfficeSubSectionType} instances.
	 */
	private final OfficeSubSectionType[] subSections;

	/**
	 * {@link OfficeTaskType} instances.
	 */
	private final OfficeTaskType[] tasks;

	/**
	 * {@link OfficeSectionManagedObjectSourceType} instances.
	 */
	private final OfficeSectionManagedObjectSourceType[] managedObjectSources;

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
	 * Instantiate.
	 * 
	 * @param name
	 *            Name of this {@link OfficeSection}.
	 * @param subSections
	 *            {@link OfficeSubSectionType} instances.
	 * @param tasks
	 *            {@link OfficeTaskType} instances.
	 * @param managedObjectSources
	 *            {@link OfficeSectionManagedObjectSourceType} instances.
	 * @param inputs
	 *            {@link OfficeSectionInputType} instances.
	 * @param outputs
	 *            {@link OfficeSectionOutputType} instances.
	 * @param objects
	 *            {@link OfficeSectionObjectType} instances.
	 */
	public OfficeSectionTypeImpl(String name,
			OfficeSubSectionType[] subSections, OfficeTaskType[] tasks,
			OfficeSectionManagedObjectSourceType[] managedObjectSources,
			OfficeSectionInputType[] inputs, OfficeSectionOutputType[] outputs,
			OfficeSectionObjectType[] objects) {
		this.name = name;
		this.subSections = subSections;
		this.tasks = tasks;
		this.managedObjectSources = managedObjectSources;
		this.inputs = inputs;
		this.outputs = outputs;
		this.objects = objects;
	}

	/*
	 * ===================== OfficeSectionType ==========================
	 */

	@Override
	public String getOfficeSectionName() {
		throw new UnsupportedOperationException("TODO implement");
	}

	@Override
	public OfficeSubSectionType[] getOfficeSubSectionTypes() {
		throw new UnsupportedOperationException("TODO implement");
	}

	@Override
	public OfficeTaskType[] getOfficeTaskTypes() {
		throw new UnsupportedOperationException("TODO implement");
	}

	@Override
	public OfficeSectionManagedObjectSourceType[] getOfficeSectionManagedObjectSourceTypes() {
		throw new UnsupportedOperationException("TODO implement");
	}

	@Override
	public OfficeSectionInputType[] getOfficeSectionInputTypes() {
		throw new UnsupportedOperationException("TODO implement");
	}

	@Override
	public OfficeSectionOutputType[] getOfficeSectionOutputTypes() {
		throw new UnsupportedOperationException("TODO implement");
	}

	@Override
	public OfficeSectionObjectType[] getOfficeSectionObjectTypes() {
		throw new UnsupportedOperationException("TODO implement");
	}

}