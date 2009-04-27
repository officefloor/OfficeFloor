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

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.officefloor.compile.OfficeSourceService;
import net.officefloor.compile.impl.util.DoubleKeyMap;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.office.OfficeSectionInput;
import net.officefloor.compile.spi.office.OfficeSectionOutput;
import net.officefloor.compile.spi.office.OfficeSubSection;
import net.officefloor.compile.spi.office.OfficeTask;
import net.officefloor.compile.spi.office.OfficeTeam;
import net.officefloor.compile.spi.office.source.OfficeSource;
import net.officefloor.compile.spi.office.source.OfficeSourceContext;
import net.officefloor.compile.spi.office.source.impl.AbstractOfficeSource;
import net.officefloor.model.impl.repository.ModelRepositoryImpl;
import net.officefloor.model.office.ExternalManagedObjectModel;
import net.officefloor.model.office.OfficeModel;
import net.officefloor.model.office.OfficeSectionInputModel;
import net.officefloor.model.office.OfficeSectionModel;
import net.officefloor.model.office.OfficeSectionOutputModel;
import net.officefloor.model.office.OfficeSectionOutputToOfficeSectionInputModel;
import net.officefloor.model.office.OfficeSectionResponsibilityModel;
import net.officefloor.model.office.OfficeSectionResponsibilityToOfficeTeamModel;
import net.officefloor.model.office.OfficeTeamModel;
import net.officefloor.model.office.PropertyModel;
import net.officefloor.model.repository.ConfigurationItem;

/**
 * {@link OfficeModel} {@link OfficeSource}.
 * 
 * @author Daniel
 */
public class OfficeModelOfficeSource extends AbstractOfficeSource implements
		OfficeSourceService {

	/*
	 * ====================== OfficeSourceService ==============================
	 */

	@Override
	public String getOfficeSourceAlias() {
		return "OFFICE";
	}

	@Override
	public Class<? extends OfficeSource> getOfficeSourceClass() {
		return this.getClass();
	}

	/*
	 * ================= AbstractOfficeSource ================================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		// No specification
	}

	@Override
	public void sourceOffice(OfficeArchitect architect,
			OfficeSourceContext context) throws Exception {

		// Obtain the configuration to the section
		ConfigurationItem configuration = context.getConfiguration(context
				.getOfficeLocation());
		if (configuration == null) {
			// Must have configuration
			throw new FileNotFoundException("Can not find office '"
					+ context.getOfficeLocation() + "'");
		}

		// Retrieve the office model
		OfficeModel office = new OfficeRepositoryImpl(new ModelRepositoryImpl())
				.retrieveOffice(configuration);

		// Add the external managed objects
		for (ExternalManagedObjectModel object : office
				.getExternalManagedObjects()) {
			architect.addOfficeObject(object.getExternalManagedObjectName(),
					object.getObjectType());
		}

		// Add the teams, keeping registry of the teams
		Map<String, OfficeTeam> teams = new HashMap<String, OfficeTeam>();
		for (OfficeTeamModel teamModel : office.getOfficeTeams()) {
			String teamName = teamModel.getOfficeTeamName();
			OfficeTeam team = architect.addOfficeTeam(teamName);
			teams.put(teamName, team);
		}

		// Add the sections, keeping registry of inputs/outputs
		DoubleKeyMap<String, String, OfficeSectionInput> inputs = new DoubleKeyMap<String, String, OfficeSectionInput>();
		DoubleKeyMap<String, String, OfficeSectionOutput> outputs = new DoubleKeyMap<String, String, OfficeSectionOutput>();
		for (OfficeSectionModel sectionModel : office.getOfficeSections()) {

			// Create the property list to add the section
			PropertyList propertyList = architect.createPropertyList();
			for (PropertyModel property : sectionModel.getProperties()) {
				propertyList.addProperty(property.getName()).setValue(
						property.getValue());
			}

			// Add the section
			String sectionName = sectionModel.getOfficeSectionName();
			OfficeSection section = architect.addOfficeSection(sectionName,
					sectionModel.getSectionSourceClassName(), sectionModel
							.getSectionLocation(), propertyList);

			// Register the section inputs
			for (OfficeSectionInput input : section.getOfficeSectionInputs()) {
				inputs.put(sectionName, input.getOfficeSectionInputName(),
						input);
			}

			// Register the section outputs
			for (OfficeSectionOutput output : section.getOfficeSectionOutputs()) {
				outputs.put(sectionName, output.getOfficeSectionOutputName(),
						output);
			}

			// Create the listing of responsibilities
			List<Responsibility> responsibilities = new LinkedList<Responsibility>();
			for (OfficeSectionResponsibilityModel responsibilityModel : sectionModel
					.getOfficeSectionResponsibilities()) {

				// Obtain the office team responsible
				OfficeTeam officeTeam = null;
				OfficeSectionResponsibilityToOfficeTeamModel conn = responsibilityModel
						.getOfficeTeam();
				if (conn != null) {
					OfficeTeamModel teamModel = conn.getOfficeTeam();
					if (teamModel != null) {
						String teamName = teamModel.getOfficeTeamName();
						officeTeam = teams.get(teamName);
					}
				}
				if (officeTeam == null) {
					continue; // must have team responsible
				}

				// Add the responsibility
				responsibilities.add(new Responsibility(officeTeam));
			}

			// Create the listing of all tasks
			List<OfficeTask> tasks = new LinkedList<OfficeTask>();
			this.loadOfficeTasks(section, tasks);

			// Assign teams their responsibilities
			for (Responsibility responsibility : responsibilities) {
				for (OfficeTask task : new ArrayList<OfficeTask>(tasks)) {
					if (responsibility.isResponsible(task)) {
						// Assign the team responsible for task
						architect.link(task.getTeamResponsible(),
								responsibility.officeTeam);

						// Remove task from listing as assigned its team
						tasks.remove(task);
					}
				}
			}
		}

		// Link the outputs to the inputs
		for (OfficeSectionModel sectionModel : office.getOfficeSections()) {
			for (OfficeSectionOutputModel outputModel : sectionModel
					.getOfficeSectionOutputs()) {

				// Obtain the output
				OfficeSectionOutput output = outputs.get(sectionModel
						.getOfficeSectionName(), outputModel
						.getOfficeSectionOutputName());
				if (output == null) {
					continue; // must have the output
				}

				// Obtain the input
				OfficeSectionInput input = null;
				OfficeSectionOutputToOfficeSectionInputModel conn = outputModel
						.getOfficeSectionInput();
				if (conn != null) {
					OfficeSectionInputModel inputModel = conn
							.getOfficeSectionInput();
					if (inputModel != null) {
						OfficeSectionModel inputSection = this
								.getOfficeSectionForInput(office, inputModel);
						if (inputSection != null) {
							input = inputs.get(inputSection
									.getOfficeSectionName(), inputModel
									.getOfficeSectionInputName());
						}
					}
				}
				if (input == null) {
					continue; // must have the input
				}

				// Link output to the input
				architect.link(output, input);
			}
		}
	}

	/**
	 * Obtains the {@link OfficeSectionModel} containing the
	 * {@link OfficeSectionInputModel}.
	 * 
	 * @param office
	 *            {@link OfficeModel}.
	 * @param input
	 *            {@link OfficeSectionInput}.
	 * @return {@link OfficeSectionModel} containing the
	 *         {@link OfficeSectionInput}.
	 */
	private OfficeSectionModel getOfficeSectionForInput(OfficeModel office,
			OfficeSectionInputModel input) {

		// Find and return the office section for the input
		for (OfficeSectionModel section : office.getOfficeSections()) {
			for (OfficeSectionInputModel check : section
					.getOfficeSectionInputs()) {
				if (check == input) {
					// Found the input so subsequently return section
					return section;
				}
			}
		}

		// As here did not find the section
		return null;
	}

	/**
	 * Loads the {@link OfficeTask} instances for the {@link OfficeSubSection}
	 * and its {@link OfficeSubSection} instances.
	 * 
	 * @param section
	 *            {@link OfficeSubSection}.
	 * @param tasks
	 *            Listing to be populated with the {@link OfficeSubSection}
	 *            {@link OfficeTask} instances.
	 */
	private void loadOfficeTasks(OfficeSubSection section,
			List<OfficeTask> tasks) {

		// Ensure have section
		if (section == null) {
			return;
		}

		// Add the section office tasks
		for (OfficeTask task : section.getOfficeTasks()) {
			tasks.add(task);
		}

		// Recursively add the sub section office tasks
		for (OfficeSubSection subSection : section.getOfficeSubSections()) {
			this.loadOfficeTasks(subSection, tasks);
		}
	}

	/**
	 * Responsibility.
	 */
	private static class Responsibility {

		/**
		 * {@link OfficeTeam} responsible for this responsibility.
		 */
		public final OfficeTeam officeTeam;

		/**
		 * Initiate.
		 * 
		 * @param officeTeam
		 *            {@link OfficeTeam} responsible for this responsibility.
		 */
		public Responsibility(OfficeTeam officeTeam) {
			this.officeTeam = officeTeam;
		}

		/**
		 * Indicates if {@link OfficeTask} is within this responsibility.
		 * 
		 * @param task
		 *            {@link OfficeTask}.
		 * @return <code>true</code> if {@link OfficeTask} is within this
		 *         responsibility.
		 */
		public boolean isResponsible(OfficeTask task) {
			// TODO handle managed object matching for responsibility
			return true; // TODO for now always responsible
		}
	}

}