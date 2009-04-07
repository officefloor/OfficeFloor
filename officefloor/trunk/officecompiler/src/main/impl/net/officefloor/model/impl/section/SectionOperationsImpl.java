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

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import net.officefloor.compile.properties.Property;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.section.SectionInputType;
import net.officefloor.compile.section.SectionObjectType;
import net.officefloor.compile.section.SectionOutputType;
import net.officefloor.compile.section.SectionType;
import net.officefloor.model.change.Change;
import net.officefloor.model.impl.change.AbstractChange;
import net.officefloor.model.section.ExternalFlowModel;
import net.officefloor.model.section.ExternalManagedObjectModel;
import net.officefloor.model.section.PropertyModel;
import net.officefloor.model.section.SectionModel;
import net.officefloor.model.section.SectionOperations;
import net.officefloor.model.section.SubSectionInputModel;
import net.officefloor.model.section.SubSectionModel;
import net.officefloor.model.section.SubSectionObjectModel;
import net.officefloor.model.section.SubSectionOutputModel;

/**
 * {@link SectionOperations} implementation.
 * 
 * @author Daniel
 */
public class SectionOperationsImpl implements SectionOperations {

	/**
	 * Sorts the {@link SubSectionModel} instances.
	 * 
	 * @param subSections
	 *            Listing of {@link SubSectionModel} instances to sort.
	 */
	public static void sortSubSections(List<SubSectionModel> subSections) {
		Collections.sort(subSections, new Comparator<SubSectionModel>() {
			@Override
			public int compare(SubSectionModel a, SubSectionModel b) {
				return a.getSubSectionName().compareTo(b.getSubSectionName());
			}
		});
	}

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

	/**
	 * Sorts the {@link SubSectionModel} instances on the {@link SectionModel}.
	 */
	public void sortSubSections() {
		sortSubSections(this.section.getSubSections());
	}

	/*
	 * ====================== SectionOperations ============================
	 */

	@Override
	public Change<SubSectionModel> addSubSection(String subSectionName,
			String sectionSourceClassName, String sectionLocation,
			PropertyList properties, SectionType sectionType) {

		// Create the sub section model
		final SubSectionModel subSection = new SubSectionModel(subSectionName,
				sectionSourceClassName, sectionLocation);

		// Add the properties in the order defined
		for (Property property : properties.getPropertyList()) {
			subSection.addProperty(new PropertyModel(property.getName(),
					property.getValue()));
		}

		// Add the inputs ensuring ordering
		for (SectionInputType inputType : sectionType.getInputTypes()) {
			subSection
					.addSubSectionInput(new SubSectionInputModel(inputType
							.getInputName(), inputType.getParameterType(),
							false, null));
		}
		Collections.sort(subSection.getSubSectionInputs(),
				new Comparator<SubSectionInputModel>() {
					@Override
					public int compare(SubSectionInputModel a,
							SubSectionInputModel b) {
						return a.getSubSectionInputName().compareTo(
								b.getSubSectionInputName());
					}
				});

		// Add the outputs ensuring ordering
		for (SectionOutputType outputType : sectionType.getOutputTypes()) {
			subSection.addSubSectionOutput(new SubSectionOutputModel(outputType
					.getOutputName(), outputType.getArgumentType(), outputType
					.isEscalationOnly()));
		}
		Collections.sort(subSection.getSubSectionOutputs(),
				new Comparator<SubSectionOutputModel>() {
					@Override
					public int compare(SubSectionOutputModel a,
							SubSectionOutputModel b) {
						return a.getSubSectionOutputName().compareTo(
								b.getSubSectionOutputName());
					}
				});

		// Add the objects ensuring ordering
		for (SectionObjectType objectType : sectionType.getObjectTypes()) {
			subSection.addSubSectionObject(new SubSectionObjectModel(objectType
					.getObjectName(), objectType.getObjectType()));
		}
		Collections.sort(subSection.getSubSectionObjects(),
				new Comparator<SubSectionObjectModel>() {
					@Override
					public int compare(SubSectionObjectModel a,
							SubSectionObjectModel b) {
						return a.getSubSectionObjectName().compareTo(
								b.getSubSectionObjectName());
					}
				});

		// Create the change to add the sub section
		return new AbstractChange<SubSectionModel>(subSection,
				"Add sub section " + subSectionName) {
			@Override
			public void apply() {
				// Add the sub section (ensuring ordering)
				SectionOperationsImpl.this.section.addSubSection(subSection);
				SectionOperationsImpl.this.sortSubSections();
			}

			@Override
			public void revert() {
				// Remove the sub section (should maintain order)
				SectionOperationsImpl.this.section.removeSubSection(subSection);
			}
		};
	}

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