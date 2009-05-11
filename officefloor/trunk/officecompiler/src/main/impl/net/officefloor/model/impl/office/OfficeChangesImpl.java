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
package net.officefloor.model.impl.office;

import net.officefloor.compile.administrator.AdministratorType;
import net.officefloor.compile.administrator.DutyType;
import net.officefloor.compile.properties.Property;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.office.OfficeSectionInput;
import net.officefloor.compile.spi.office.OfficeSectionObject;
import net.officefloor.compile.spi.office.OfficeSectionOutput;
import net.officefloor.model.change.Change;
import net.officefloor.model.impl.change.AbstractChange;
import net.officefloor.model.office.AdministratorModel;
import net.officefloor.model.office.DutyModel;
import net.officefloor.model.office.ExternalManagedObjectModel;
import net.officefloor.model.office.OfficeChanges;
import net.officefloor.model.office.OfficeModel;
import net.officefloor.model.office.OfficeSectionInputModel;
import net.officefloor.model.office.OfficeSectionModel;
import net.officefloor.model.office.OfficeSectionObjectModel;
import net.officefloor.model.office.OfficeSectionOutputModel;
import net.officefloor.model.office.OfficeTeamModel;
import net.officefloor.model.office.PropertyModel;

/**
 * {@link OfficeChanges} implementation.
 * 
 * @author Daniel
 */
public class OfficeChangesImpl implements OfficeChanges {

	/**
	 * {@link OfficeModel} to change.
	 */
	private final OfficeModel office;

	/**
	 * Initiate.
	 * 
	 * @param office
	 *            {@link OfficeModel} to change.
	 */
	public OfficeChangesImpl(OfficeModel office) {
		this.office = office;
	}

	/*
	 * =================== OfficeChanges =========================
	 */

	@Override
	public Change<OfficeSectionModel> addOfficeSection(
			String sectionSourceClassName, String sectionLocation,
			PropertyList properties, OfficeSection officeSection) {

		// TODO test this method (addOfficeSection)

		// Create the office section model
		String sectionName = officeSection.getOfficeSectionName();
		final OfficeSectionModel section = new OfficeSectionModel(sectionName,
				sectionSourceClassName, sectionLocation);
		for (Property property : properties) {
			section.addProperty(new PropertyModel(property.getName(), property
					.getValue()));
		}

		// Add the inputs
		for (OfficeSectionInput input : officeSection.getOfficeSectionInputs()) {
			section.addOfficeSectionInput(new OfficeSectionInputModel(input
					.getOfficeSectionInputName(), input.getParameterType()));
		}

		// Add the outputs
		for (OfficeSectionOutput output : officeSection
				.getOfficeSectionOutputs()) {
			section.addOfficeSectionOutput(new OfficeSectionOutputModel(output
					.getOfficeSectionOutputName(), output.getArgumentType(),
					output.isEscalationOnly()));
		}

		// Add the objects
		for (OfficeSectionObject object : officeSection
				.getOfficeSectionObjects()) {
			section.addOfficeSectionObject(new OfficeSectionObjectModel(object
					.getOfficeSectionObjectName(), object.getObjectType()));
		}

		// Return the change to add the section
		return new AbstractChange<OfficeSectionModel>(section, "Add section") {
			@Override
			public void apply() {
				OfficeChangesImpl.this.office.addOfficeSection(section);
			}

			@Override
			public void revert() {
				OfficeChangesImpl.this.office.removeOfficeSection(section);
			}
		};
	}

	@Override
	public Change<OfficeSectionModel> removeOfficeSection(
			final OfficeSectionModel officeSection) {

		// TODO test this method (removeOfficeSection)

		return new AbstractChange<OfficeSectionModel>(officeSection,
				"Remove section") {
			@Override
			public void apply() {
				OfficeChangesImpl.this.office
						.removeOfficeSection(officeSection);
			}

			@Override
			public void revert() {
				OfficeChangesImpl.this.office.addOfficeSection(officeSection);
			}
		};
	}

	@Override
	public Change<OfficeSectionModel> renameOfficeSection(
			final OfficeSectionModel officeSection,
			final String newOfficeSectionName) {

		// TODO test this method (renameOfficeSection)

		// Obtain the old name
		final String oldOfficeSectionName = officeSection
				.getOfficeSectionName();

		// Return the change to rename the office section
		return new AbstractChange<OfficeSectionModel>(officeSection,
				"Rename office section to " + newOfficeSectionName) {
			@Override
			public void apply() {
				officeSection.setOfficeSectionName(newOfficeSectionName);
			}

			@Override
			public void revert() {
				officeSection.setOfficeSectionName(oldOfficeSectionName);
			}
		};
	}

	@Override
	public Change<ExternalManagedObjectModel> addExternalManagedObject(
			String externalManagedObjectName, String objectType) {

		// TODO test this method (addExternalManagedObject)

		// Create the external managed object
		final ExternalManagedObjectModel mo = new ExternalManagedObjectModel(
				externalManagedObjectName, objectType);

		// Return change to add external managed object
		return new AbstractChange<ExternalManagedObjectModel>(mo,
				"Add external object") {
			@Override
			public void apply() {
				OfficeChangesImpl.this.office.addExternalManagedObject(mo);
			}

			@Override
			public void revert() {
				OfficeChangesImpl.this.office.removeExternalManagedObject(mo);
			}
		};
	}

	@Override
	public Change<ExternalManagedObjectModel> removeExternalManagedObject(
			final ExternalManagedObjectModel externalManagedObject) {

		// TODO test this method (removeExternalManagedObject)

		// Return change to remove external managed object
		return new AbstractChange<ExternalManagedObjectModel>(
				externalManagedObject, "Remove external object") {
			@Override
			public void apply() {
				OfficeChangesImpl.this.office
						.removeExternalManagedObject(externalManagedObject);
			}

			@Override
			public void revert() {
				OfficeChangesImpl.this.office
						.addExternalManagedObject(externalManagedObject);
			}
		};
	}

	@Override
	public Change<ExternalManagedObjectModel> renameExternalManagedObject(
			final ExternalManagedObjectModel externalManagedObject,
			final String newExternalManagedObjectName) {

		// TODO test this method (renameExternalManagedObject)

		// Obtain the old name for the external managed object
		final String oldExternalManagedObjectName = externalManagedObject
				.getExternalManagedObjectName();

		// Return change to rename external managed object
		return new AbstractChange<ExternalManagedObjectModel>(
				externalManagedObject, "Rename external object to "
						+ newExternalManagedObjectName) {
			@Override
			public void apply() {
				externalManagedObject
						.setExternalManagedObjectName(newExternalManagedObjectName);
			}

			@Override
			public void revert() {
				externalManagedObject
						.setExternalManagedObjectName(oldExternalManagedObjectName);
			}
		};
	}

	@Override
	public Change<OfficeTeamModel> addOfficeTeam(String teamName) {

		// TODO test this method (addOfficeTeam)

		// Create the office team
		final OfficeTeamModel team = new OfficeTeamModel(teamName);

		// Return change to add team
		return new AbstractChange<OfficeTeamModel>(team, "Add team") {
			@Override
			public void apply() {
				OfficeChangesImpl.this.office.addOfficeTeam(team);
			}

			@Override
			public void revert() {
				OfficeChangesImpl.this.office.removeOfficeTeam(team);
			}
		};
	}

	@Override
	public Change<OfficeTeamModel> removeOfficeTeam(
			final OfficeTeamModel officeTeam) {

		// TODO test this method (removeOfficeTeam)

		// Return change to remove team
		return new AbstractChange<OfficeTeamModel>(officeTeam, "Remove team") {
			@Override
			public void apply() {
				OfficeChangesImpl.this.office.removeOfficeTeam(officeTeam);
			}

			@Override
			public void revert() {
				OfficeChangesImpl.this.office.addOfficeTeam(officeTeam);
			}
		};
	}

	@Override
	public Change<OfficeTeamModel> renameOfficeTeam(
			final OfficeTeamModel officeTeam, final String newOfficeTeamName) {

		// TODO test this method (renameOfficeTeam)

		// Obtain the old team name
		final String oldOfficeTeamName = officeTeam.getOfficeTeamName();

		// Return change to rename the office team
		return new AbstractChange<OfficeTeamModel>(officeTeam,
				"Rename team to " + newOfficeTeamName) {
			@Override
			public void apply() {
				officeTeam.setOfficeTeamName(newOfficeTeamName);
			}

			@Override
			public void revert() {
				officeTeam.setOfficeTeamName(oldOfficeTeamName);
			}
		};
	}

	@Override
	public Change<AdministratorModel> addAdministrator(
			String administratorName, String administratorSourceClassName,
			PropertyList properties, AdministratorType<?, ?> administratorType) {

		// TODO test this method (addAdministrator)

		// Create the administrator
		final AdministratorModel administrator = new AdministratorModel(
				administratorName, administratorSourceClassName);
		for (Property property : properties) {
			administrator.addProperty(new PropertyModel(property.getName(),
					property.getValue()));
		}

		// Add the duties
		for (DutyType<?, ?> duty : administratorType.getDutyTypes()) {
			administrator.addDuty(new DutyModel(duty.getDutyKey().name()));
		}

		// Return change to add the administrator
		return new AbstractChange<AdministratorModel>(administrator,
				"Add administrator") {
			@Override
			public void apply() {
				OfficeChangesImpl.this.office
						.addOfficeAdministrator(administrator);
			}

			@Override
			public void revert() {
				OfficeChangesImpl.this.office
						.removeOfficeAdministrator(administrator);
			}
		};
	}

	@Override
	public Change<AdministratorModel> removeAdministrator(
			final AdministratorModel administrator) {

		// TODO test this method (removeAdministrator)

		// Return change to remove administrator
		return new AbstractChange<AdministratorModel>(administrator,
				"Remove administrator") {
			@Override
			public void apply() {
				OfficeChangesImpl.this.office
						.removeOfficeAdministrator(administrator);
			}

			@Override
			public void revert() {
				OfficeChangesImpl.this.office
						.addOfficeAdministrator(administrator);
			}
		};
	}

	@Override
	public Change<AdministratorModel> renameAdministrator(
			final AdministratorModel administrator,
			final String newAdministratorName) {

		// TODO test this method (renameAdministrator)

		// Obtain the old name
		final String oldAdministratorName = administrator
				.getAdministratorName();

		// Return change to rename the administrator
		return new AbstractChange<AdministratorModel>(administrator,
				"Rename administrator to " + newAdministratorName) {
			@Override
			public void apply() {
				administrator.setAdministratorName(newAdministratorName);
			}

			@Override
			public void revert() {
				administrator.setAdministratorName(oldAdministratorName);
			}
		};
	}

}