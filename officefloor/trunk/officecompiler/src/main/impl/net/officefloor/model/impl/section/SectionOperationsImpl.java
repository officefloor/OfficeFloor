/*
 *  Office Floor, Application Server
 *  Copyright (C) 2006 Daniel Sagenschneider
 *
 *  This program is free software; you can redistribute it and/or modify it under the terms 
 *  of the GNU General Public License as published by the Free Software Foundation; either 
 *  version 2 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program; 
 *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, 
 *  MA 02111-1307 USA
 */
package net.officefloor.model.impl.section;

import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.section.SectionType;
import net.officefloor.model.change.Change;
import net.officefloor.model.section.ExternalFlowModel;
import net.officefloor.model.section.ExternalManagedObjectModel;
import net.officefloor.model.section.SectionModel;
import net.officefloor.model.section.SectionOperations;
import net.officefloor.model.section.SubSectionInputModel;
import net.officefloor.model.section.SubSectionModel;

/**
 * {@link SectionOperations} implementation.
 * 
 * @author Daniel
 */
public class SectionOperationsImpl implements SectionOperations {

	/**
	 * {@link SectionModel} to be operated on.
	 */
	private final SectionModel section;

	/**
	 * Initiate.
	 * 
	 * @param section
	 *            {@link SectionModel}.
	 */
	public SectionOperationsImpl(SectionModel section) {
		this.section = section;
	}

	/*
	 * ====================== SectionOperations ============================
	 */

	@Override
	public Change<ExternalFlowModel> addExternalFlow(String externalFlowName,
			String argumentType) {
		// TODO Implement
		throw new UnsupportedOperationException(
				"TODO implement SectionOperations.addExternalFlow");
	}

	@Override
	public Change<ExternalManagedObjectModel> addExternalManagedObject(
			String externalManagedObjectName, String objectType) {
		// TODO Implement
		throw new UnsupportedOperationException(
				"TODO implement SectionOperations.addExternalManagedObject");
	}

	@Override
	public Change<SubSectionModel> addSubSection(String subSectionName,
			String sectionSourceClassName, String sectionLocation,
			PropertyList properties, SectionType sectionType) {
		// TODO Implement
		throw new UnsupportedOperationException(
				"TODO implement SectionOperations.addSubSection");
	}

	@Override
	public Change<ExternalFlowModel> removeExternalFlow(
			ExternalFlowModel externalFlow) {
		// TODO Implement
		throw new UnsupportedOperationException(
				"TODO implement SectionOperations.removeExternalFlow");
	}

	@Override
	public Change<ExternalManagedObjectModel> removeExternalManagedObject(
			ExternalManagedObjectModel externalManagedObject) {
		// TODO Implement
		throw new UnsupportedOperationException(
				"TODO implement SectionOperations.removeExternalManagedObject");
	}

	@Override
	public Change<SubSectionModel> removeSubSection(SubSectionModel subSection) {
		// TODO Implement
		throw new UnsupportedOperationException(
				"TODO implement SectionOperations.removeSubSection");
	}

	@Override
	public Change<SubSectionModel> renameSubSection(SubSectionModel subSection,
			String newSubSectionName) {
		// TODO Implement
		throw new UnsupportedOperationException(
				"TODO implement SectionOperations.renameSubSection");
	}

	@Override
	public Change<SubSectionInputModel> setSubSectionInputPublic(
			boolean isPublic, String publicName, SubSectionInputModel input) {
		// TODO Implement
		throw new UnsupportedOperationException(
				"TODO implement SectionOperations.setSubSectionInputPublic");
	}

}