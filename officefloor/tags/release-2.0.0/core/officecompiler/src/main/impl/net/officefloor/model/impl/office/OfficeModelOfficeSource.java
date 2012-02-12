/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2011 Daniel Sagenschneider
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

package net.officefloor.model.impl.office;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.officefloor.compile.OfficeSourceService;
import net.officefloor.compile.impl.util.CompileUtil;
import net.officefloor.compile.impl.util.DoubleKeyMap;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.office.AdministerableManagedObject;
import net.officefloor.compile.spi.office.ManagedObjectTeam;
import net.officefloor.compile.spi.office.OfficeAdministrator;
import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.OfficeDuty;
import net.officefloor.compile.spi.office.OfficeEscalation;
import net.officefloor.compile.spi.office.OfficeGovernance;
import net.officefloor.compile.spi.office.OfficeManagedObject;
import net.officefloor.compile.spi.office.OfficeManagedObjectSource;
import net.officefloor.compile.spi.office.OfficeObject;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.office.OfficeSectionInput;
import net.officefloor.compile.spi.office.OfficeSectionManagedObject;
import net.officefloor.compile.spi.office.OfficeSectionManagedObjectSource;
import net.officefloor.compile.spi.office.OfficeSectionObject;
import net.officefloor.compile.spi.office.OfficeSectionOutput;
import net.officefloor.compile.spi.office.OfficeStart;
import net.officefloor.compile.spi.office.OfficeSubSection;
import net.officefloor.compile.spi.office.OfficeTask;
import net.officefloor.compile.spi.office.OfficeTeam;
import net.officefloor.compile.spi.office.source.OfficeSource;
import net.officefloor.compile.spi.office.source.OfficeSourceContext;
import net.officefloor.compile.spi.office.source.impl.AbstractOfficeSource;
import net.officefloor.compile.spi.section.ManagedObjectDependency;
import net.officefloor.compile.spi.section.ManagedObjectFlow;
import net.officefloor.frame.api.build.OfficeFloorIssues.AssetType;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.spi.administration.Duty;
import net.officefloor.frame.spi.governance.Governance;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.model.impl.repository.ModelRepositoryImpl;
import net.officefloor.model.impl.repository.inputstream.InputStreamConfigurationItem;
import net.officefloor.model.office.AdministratorModel;
import net.officefloor.model.office.AdministratorToOfficeTeamModel;
import net.officefloor.model.office.DutyModel;
import net.officefloor.model.office.ExternalManagedObjectModel;
import net.officefloor.model.office.ExternalManagedObjectToAdministratorModel;
import net.officefloor.model.office.ExternalManagedObjectToOfficeGovernanceModel;
import net.officefloor.model.office.OfficeChanges;
import net.officefloor.model.office.OfficeEscalationModel;
import net.officefloor.model.office.OfficeEscalationToOfficeSectionInputModel;
import net.officefloor.model.office.OfficeGovernanceAreaModel;
import net.officefloor.model.office.OfficeGovernanceModel;
import net.officefloor.model.office.OfficeGovernanceToOfficeTeamModel;
import net.officefloor.model.office.OfficeInputManagedObjectDependencyModel;
import net.officefloor.model.office.OfficeInputManagedObjectDependencyToExternalManagedObjectModel;
import net.officefloor.model.office.OfficeInputManagedObjectDependencyToOfficeManagedObjectModel;
import net.officefloor.model.office.OfficeManagedObjectDependencyModel;
import net.officefloor.model.office.OfficeManagedObjectDependencyToExternalManagedObjectModel;
import net.officefloor.model.office.OfficeManagedObjectDependencyToOfficeManagedObjectModel;
import net.officefloor.model.office.OfficeManagedObjectModel;
import net.officefloor.model.office.OfficeManagedObjectSourceFlowModel;
import net.officefloor.model.office.OfficeManagedObjectSourceFlowToOfficeSectionInputModel;
import net.officefloor.model.office.OfficeManagedObjectSourceModel;
import net.officefloor.model.office.OfficeManagedObjectSourceTeamModel;
import net.officefloor.model.office.OfficeManagedObjectSourceTeamToOfficeTeamModel;
import net.officefloor.model.office.OfficeManagedObjectToAdministratorModel;
import net.officefloor.model.office.OfficeManagedObjectToOfficeGovernanceModel;
import net.officefloor.model.office.OfficeManagedObjectToOfficeManagedObjectSourceModel;
import net.officefloor.model.office.OfficeModel;
import net.officefloor.model.office.OfficeSectionInputModel;
import net.officefloor.model.office.OfficeSectionManagedObjectModel;
import net.officefloor.model.office.OfficeSectionManagedObjectToOfficeGovernanceModel;
import net.officefloor.model.office.OfficeSectionModel;
import net.officefloor.model.office.OfficeSectionObjectModel;
import net.officefloor.model.office.OfficeSectionObjectToExternalManagedObjectModel;
import net.officefloor.model.office.OfficeSectionObjectToOfficeManagedObjectModel;
import net.officefloor.model.office.OfficeSectionOutputModel;
import net.officefloor.model.office.OfficeSectionOutputToOfficeSectionInputModel;
import net.officefloor.model.office.OfficeSectionResponsibilityModel;
import net.officefloor.model.office.OfficeSectionResponsibilityToOfficeTeamModel;
import net.officefloor.model.office.OfficeStartModel;
import net.officefloor.model.office.OfficeStartToOfficeSectionInputModel;
import net.officefloor.model.office.OfficeSubSectionModel;
import net.officefloor.model.office.OfficeSubSectionToOfficeGovernanceModel;
import net.officefloor.model.office.OfficeTaskModel;
import net.officefloor.model.office.OfficeTaskToOfficeGovernanceModel;
import net.officefloor.model.office.OfficeTaskToPostDutyModel;
import net.officefloor.model.office.OfficeTaskToPreDutyModel;
import net.officefloor.model.office.OfficeTeamModel;
import net.officefloor.model.office.PropertyModel;

/**
 * {@link OfficeModel} {@link OfficeSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeModelOfficeSource extends AbstractOfficeSource implements
		OfficeSourceService<OfficeModelOfficeSource> {

	/*
	 * ====================== OfficeSourceService ==============================
	 */

	@Override
	public String getOfficeSourceAlias() {
		return "OFFICE";
	}

	@Override
	public Class<OfficeModelOfficeSource> getOfficeSourceClass() {
		return OfficeModelOfficeSource.class;
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
		InputStream configuration = context.getResource(context
				.getOfficeLocation());
		if (configuration == null) {
			// Must have configuration
			throw new FileNotFoundException("Can not find office '"
					+ context.getOfficeLocation() + "'");
		}

		// Retrieve the office model
		OfficeModel office = new OfficeRepositoryImpl(new ModelRepositoryImpl())
				.retrieveOffice(new InputStreamConfigurationItem(configuration));

		// Create aggregate processor to add sub section processing
		AggregateSubSectionProcessor processors = new AggregateSubSectionProcessor();

		// Add the teams, keeping registry of the teams
		Map<String, OfficeTeam> teams = new HashMap<String, OfficeTeam>();
		for (OfficeTeamModel teamModel : office.getOfficeTeams()) {
			String teamName = teamModel.getOfficeTeamName();
			OfficeTeam team = architect.addOfficeTeam(teamName);
			teams.put(teamName, team);
		}

		// Obtain the listing of governances
		Map<String, OfficeGovernance> governances = new HashMap<String, OfficeGovernance>();
		for (OfficeGovernanceModel govModel : office.getOfficeGovernances()) {

			// Add the governance
			String governanceName = govModel.getOfficeGovernanceName();
			OfficeGovernance governance = architect.addOfficeGovernance(
					governanceName, govModel.getGovernanceSourceClassName());
			for (PropertyModel property : govModel.getProperties()) {
				governance.addProperty(property.getName(), property.getValue());
			}

			// Provide team responsible for governance
			OfficeGovernanceToOfficeTeamModel govToTeam = govModel
					.getOfficeTeam();
			if (govToTeam != null) {
				OfficeTeamModel teamModel = govToTeam.getOfficeTeam();
				if (teamModel != null) {
					OfficeTeam team = teams.get(teamModel.getOfficeTeamName());
					if (team != null) {
						architect.link(governance, team);
					}
				}
			}

			// Register the governance
			governances.put(governanceName, governance);
		}

		// Add governance processing for sub sections
		processors.addSubSectionProcessor(new GovernanceSubSectionProcessor(
				governances));

		// Add the external managed objects, keeping registry of them
		Map<String, OfficeObject> officeObjects = new HashMap<String, OfficeObject>();
		for (ExternalManagedObjectModel object : office
				.getExternalManagedObjects()) {

			// Create the office object
			String officeObjectName = object.getExternalManagedObjectName();
			OfficeObject officeObject = architect.addOfficeObject(
					officeObjectName, object.getObjectType());

			// Provide governance over managed object
			for (ExternalManagedObjectToOfficeGovernanceModel moToGov : object
					.getOfficeGovernances()) {
				OfficeGovernanceModel govModel = moToGov.getOfficeGovernance();
				if (govModel != null) {
					OfficeGovernance governance = governances.get(govModel
							.getOfficeGovernanceName());
					if (governance != null) {
						governance.governManagedObject(officeObject);
					}
				}
			}

			// Register the office object
			officeObjects.put(officeObjectName, officeObject);
		}

		// Add the managed object sources, keeping registry of them
		Map<String, OfficeManagedObjectSource> managedObjectSources = new HashMap<String, OfficeManagedObjectSource>();
		for (OfficeManagedObjectSourceModel mosModel : office
				.getOfficeManagedObjectSources()) {

			// Add the managed object source
			String mosName = mosModel.getOfficeManagedObjectSourceName();
			OfficeManagedObjectSource mos = architect
					.addOfficeManagedObjectSource(mosName,
							mosModel.getManagedObjectSourceClassName());
			for (PropertyModel property : mosModel.getProperties()) {
				mos.addProperty(property.getName(), property.getValue());
			}

			// Provide timeout
			String timeoutValue = mosModel.getTimeout();
			if (!CompileUtil.isBlank(timeoutValue)) {
				try {
					mos.setTimeout(Long.valueOf(timeoutValue));
				} catch (NumberFormatException ex) {
					architect.addIssue(
							"Invalid timeout value: " + timeoutValue,
							AssetType.MANAGED_OBJECT, mosName);
				}
			}

			// Register the managed object source
			managedObjectSources.put(mosName, mos);
		}

		// Add the managed objects, keeping registry of them
		Map<String, OfficeManagedObject> managedObjects = new HashMap<String, OfficeManagedObject>();
		for (OfficeManagedObjectModel moModel : office
				.getOfficeManagedObjects()) {

			// Obtain the managed object details
			String managedObjectName = moModel.getOfficeManagedObjectName();
			ManagedObjectScope managedObjectScope = this.getManagedObjectScope(
					moModel.getManagedObjectScope(), architect,
					managedObjectName);

			// Obtain the managed object source for the managed object
			OfficeManagedObjectSource moSource = null;
			OfficeManagedObjectToOfficeManagedObjectSourceModel moToSource = moModel
					.getOfficeManagedObjectSource();
			if (moToSource != null) {
				OfficeManagedObjectSourceModel moSourceModel = moToSource
						.getOfficeManagedObjectSource();
				if (moSourceModel != null) {
					moSource = managedObjectSources.get(moSourceModel
							.getOfficeManagedObjectSourceName());
				}
			}
			if (moSource == null) {
				continue; // must have managed object source
			}

			// Add the managed object and also register it
			OfficeManagedObject managedObject = moSource
					.addOfficeManagedObject(managedObjectName,
							managedObjectScope);
			managedObjects.put(managedObjectName, managedObject);

			// Provide governance over managed object
			for (OfficeManagedObjectToOfficeGovernanceModel moToGov : moModel
					.getOfficeGovernances()) {
				OfficeGovernanceModel govModel = moToGov.getOfficeGovernance();
				if (govModel != null) {
					OfficeGovernance governance = governances.get(govModel
							.getOfficeGovernanceName());
					if (governance != null) {
						governance.governManagedObject(managedObject);
					}
				}
			}
		}

		// Link the managed object dependencies
		for (OfficeManagedObjectModel moModel : office
				.getOfficeManagedObjects()) {

			// Obtain the managed object
			OfficeManagedObject managedObject = managedObjects.get(moModel
					.getOfficeManagedObjectName());
			if (managedObject == null) {
				continue; // should always have
			}

			// Link the dependencies
			for (OfficeManagedObjectDependencyModel dependencyModel : moModel
					.getOfficeManagedObjectDependencies()) {

				// Obtain the dependency
				ManagedObjectDependency dependency = managedObject
						.getManagedObjectDependency(dependencyModel
								.getOfficeManagedObjectDependencyName());

				// Determine if linked to managed object
				OfficeManagedObject linkedManagedObject = null;
				OfficeManagedObjectDependencyToOfficeManagedObjectModel dependencyToMo = dependencyModel
						.getOfficeManagedObject();
				if (dependencyToMo != null) {
					OfficeManagedObjectModel linkedMoModel = dependencyToMo
							.getOfficeManagedObject();
					if (linkedMoModel != null) {
						linkedManagedObject = managedObjects.get(linkedMoModel
								.getOfficeManagedObjectName());
					}
				}
				if (linkedManagedObject != null) {
					// Link to managed object
					architect.link(dependency, linkedManagedObject);
				}

				// Determine if linked to external managed object
				OfficeObject linkedObject = null;
				OfficeManagedObjectDependencyToExternalManagedObjectModel dependencyToExtMo = dependencyModel
						.getExternalManagedObject();
				if (dependencyToExtMo != null) {
					ExternalManagedObjectModel linkedExtMoModel = dependencyToExtMo
							.getExternalManagedObject();
					if (linkedExtMoModel != null) {
						linkedObject = officeObjects.get(linkedExtMoModel
								.getExternalManagedObjectName());
					}
				}
				if (linkedObject != null) {
					// Link to office object
					architect.link(dependency, linkedObject);
				}
			}
		}

		// Link the input managed object dependencies
		for (OfficeManagedObjectSourceModel mosModel : office
				.getOfficeManagedObjectSources()) {

			// Obtain the managed object source
			OfficeManagedObjectSource mos = managedObjectSources.get(mosModel
					.getOfficeManagedObjectSourceName());
			if (mos == null) {
				continue; // should always have
			}

			// Link the input dependencies
			for (OfficeInputManagedObjectDependencyModel dependencyModel : mosModel
					.getOfficeInputManagedObjectDependencies()) {

				// Obtain the input dependency
				ManagedObjectDependency dependency = mos
						.getInputManagedObjectDependency(dependencyModel
								.getOfficeInputManagedObjectDependencyName());

				// Determine if linked to managed object
				OfficeManagedObject linkedManagedObject = null;
				OfficeInputManagedObjectDependencyToOfficeManagedObjectModel dependencyToMo = dependencyModel
						.getOfficeManagedObject();
				if (dependencyToMo != null) {
					OfficeManagedObjectModel linkedMoModel = dependencyToMo
							.getOfficeManagedObject();
					if (linkedMoModel != null) {
						linkedManagedObject = managedObjects.get(linkedMoModel
								.getOfficeManagedObjectName());
					}
				}
				if (linkedManagedObject != null) {
					// Link to managed object
					architect.link(dependency, linkedManagedObject);
				}

				// Determine if linked to external managed object
				OfficeObject linkedObject = null;
				OfficeInputManagedObjectDependencyToExternalManagedObjectModel dependencyToExtMo = dependencyModel
						.getExternalManagedObject();
				if (dependencyToExtMo != null) {
					ExternalManagedObjectModel linkedExtMoModel = dependencyToExtMo
							.getExternalManagedObject();
					if (linkedExtMoModel != null) {
						linkedObject = officeObjects.get(linkedExtMoModel
								.getExternalManagedObjectName());
					}
				}
				if (linkedObject != null) {
					// Link to external managed object
					architect.link(dependency, linkedObject);
				}
			}
		}

		// Add the sections, keeping registry of inputs/outputs/objects
		Map<String, OfficeSection> sections = new HashMap<String, OfficeSection>();
		DoubleKeyMap<String, String, OfficeSectionInput> inputs = new DoubleKeyMap<String, String, OfficeSectionInput>();
		DoubleKeyMap<String, String, OfficeSectionOutput> outputs = new DoubleKeyMap<String, String, OfficeSectionOutput>();
		DoubleKeyMap<String, String, OfficeSectionObject> objects = new DoubleKeyMap<String, String, OfficeSectionObject>();
		for (OfficeSectionModel sectionModel : office.getOfficeSections()) {

			// Create the property list to add the section
			PropertyList propertyList = context.createPropertyList();
			for (PropertyModel property : sectionModel.getProperties()) {
				propertyList.addProperty(property.getName()).setValue(
						property.getValue());
			}

			// Add the section (register for later)
			String sectionName = sectionModel.getOfficeSectionName();
			OfficeSection section = architect.addOfficeSection(sectionName,
					sectionModel.getSectionSourceClassName(),
					sectionModel.getSectionLocation(), propertyList);
			sections.put(sectionName, section);

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

			// Register the section objects
			for (OfficeSectionObject object : section.getOfficeSectionObjects()) {
				objects.put(sectionName, object.getOfficeSectionObjectName(),
						object);
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

			// Obtain the governances of section
			OfficeGovernanceModel[] governingGovernances = this
					.getOfficeGovernancesOverLocation(sectionModel.getX(),
							sectionModel.getY(), office.getOfficeGovernances());
			for (OfficeGovernanceModel govModel : governingGovernances) {
				// Obtain the governance to govern the section
				OfficeGovernance governance = governances.get(govModel
						.getOfficeGovernanceName());
				if (governance != null) {
					// Add the governance to the section
					section.addGovernance(governance);
				}
			}
		}

		// Link start-up triggers to office section inputs
		for (OfficeStartModel startModel : office.getOfficeStarts()) {

			// Obtain the office start
			String startName = startModel.getStartName();
			OfficeStart start = architect.addOfficeStart(startName);

			// Obtain the flow to trigger on start-up
			OfficeSectionInput officeSectionInput = null;
			OfficeStartToOfficeSectionInputModel connToInput = startModel
					.getOfficeSectionInput();
			if (connToInput != null) {
				officeSectionInput = inputs.get(
						connToInput.getOfficeSectionName(),
						connToInput.getOfficeSectionInputName());
			}
			if (officeSectionInput != null) {
				// Link start-up to section input
				architect.link(start, officeSectionInput);
			}
		}

		// Link the sections to other sections and external office
		for (OfficeSectionModel sectionModel : office.getOfficeSections()) {

			// Obtain the section name
			String sectionName = sectionModel.getOfficeSectionName();

			// Link the objects to office objects
			for (OfficeSectionObjectModel objectModel : sectionModel
					.getOfficeSectionObjects()) {

				// Obtain the object
				OfficeSectionObject object = objects.get(sectionName,
						objectModel.getOfficeSectionObjectName());
				if (object == null) {
					continue; // must have the object
				}

				// Determine if link object to office object
				OfficeObject officeObject = null;
				OfficeSectionObjectToExternalManagedObjectModel connToExtMo = objectModel
						.getExternalManagedObject();
				if (connToExtMo != null) {
					ExternalManagedObjectModel extMo = connToExtMo
							.getExternalManagedObject();
					if (extMo != null) {
						officeObject = officeObjects.get(extMo
								.getExternalManagedObjectName());
					}
				}
				if (officeObject != null) {
					// Link object to office object
					architect.link(object, officeObject);
				}

				// Determine if link object to office managed object
				OfficeManagedObject officeMo = null;
				OfficeSectionObjectToOfficeManagedObjectModel connToMo = objectModel
						.getOfficeManagedObject();
				if (connToMo != null) {
					OfficeManagedObjectModel mo = connToMo
							.getOfficeManagedObject();
					if (mo != null) {
						officeMo = managedObjects.get(mo
								.getOfficeManagedObjectName());
					}
				}
				if (officeMo != null) {
					// Link object to office managed object
					architect.link(object, officeMo);
				}
			}

			// Link the outputs to the inputs
			for (OfficeSectionOutputModel outputModel : sectionModel
					.getOfficeSectionOutputs()) {

				// Obtain the output
				OfficeSectionOutput output = outputs.get(sectionName,
						outputModel.getOfficeSectionOutputName());
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
							input = inputs.get(
									inputSection.getOfficeSectionName(),
									inputModel.getOfficeSectionInputName());
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

		// Link managed object source flows to section inputs
		for (OfficeManagedObjectSourceModel mosModel : office
				.getOfficeManagedObjectSources()) {

			// Obtain the managed object source
			OfficeManagedObjectSource mos = managedObjectSources.get(mosModel
					.getOfficeManagedObjectSourceName());
			if (mos == null) {
				continue; // should always have managed object source
			}

			// Link managed object source flow to section input
			for (OfficeManagedObjectSourceFlowModel mosFlowModel : mosModel
					.getOfficeManagedObjectSourceFlows()) {

				// Obtain the managed object source flow
				ManagedObjectFlow mosFlow = mos
						.getManagedObjectFlow(mosFlowModel
								.getOfficeManagedObjectSourceFlowName());

				// Link to section input
				OfficeSectionInput sectionInput = null;
				OfficeManagedObjectSourceFlowToOfficeSectionInputModel conn = mosFlowModel
						.getOfficeSectionInput();
				if (conn != null) {
					OfficeSectionInputModel sectionInputModel = conn
							.getOfficeSectionInput();
					if (sectionInputModel != null) {
						OfficeSectionModel sectionModel = this
								.getOfficeSectionForInput(office,
										sectionInputModel);
						if (sectionModel != null) {
							sectionInput = inputs.get(sectionModel
									.getOfficeSectionName(), sectionInputModel
									.getOfficeSectionInputName());
						}
					}
				}
				if (sectionInput != null) {
					// Link managed object source flow to section input
					architect.link(mosFlow, sectionInput);
				}
			}
		}

		// Link managed object source teams to office teams
		for (OfficeManagedObjectSourceModel mosModel : office
				.getOfficeManagedObjectSources()) {

			// Obtain the managed object source
			OfficeManagedObjectSource mos = managedObjectSources.get(mosModel
					.getOfficeManagedObjectSourceName());
			if (mos == null) {
				continue; // should always have managed object source
			}

			// Link managed object source teams to office team
			for (OfficeManagedObjectSourceTeamModel mosTeamModel : mosModel
					.getOfficeManagedObjectSourceTeams()) {

				// Obtain the managed object source team
				ManagedObjectTeam mosTeam = mos
						.getManagedObjectTeam(mosTeamModel
								.getOfficeManagedObjectSourceTeamName());

				// Link managed object source team to office team
				OfficeTeam officeTeam = null;
				OfficeManagedObjectSourceTeamToOfficeTeamModel conn = mosTeamModel
						.getOfficeTeam();
				if (conn != null) {
					OfficeTeamModel teamModel = conn.getOfficeTeam();
					if (teamModel != null) {
						officeTeam = teams.get(teamModel.getOfficeTeamName());
					}
				}
				if (officeTeam != null) {
					// Link managed object source team to office team
					architect.link(mosTeam, officeTeam);
				}
			}
		}

		// Add the administrators (keeping registry of administrators, duties)
		Map<String, OfficeAdministrator> administrators = new HashMap<String, OfficeAdministrator>();
		Map<DutyModel, AdministratorModel> dutyAdmins = new HashMap<DutyModel, AdministratorModel>();
		DoubleKeyMap<String, String, OfficeDuty> duties = new DoubleKeyMap<String, String, OfficeDuty>();
		for (AdministratorModel adminModel : office.getOfficeAdministrators()) {

			// Add the administrator and register it
			String adminName = adminModel.getAdministratorName();
			OfficeAdministrator admin = architect.addOfficeAdministrator(
					adminName, adminModel.getAdministratorSourceClassName());
			for (PropertyModel property : adminModel.getProperties()) {
				admin.addProperty(property.getName(), property.getValue());
			}
			administrators.put(adminName, admin);

			// Obtain the office team responsible for this administration
			OfficeTeam officeTeam = null;
			AdministratorToOfficeTeamModel adminToTeam = adminModel
					.getOfficeTeam();
			if (adminToTeam != null) {
				OfficeTeamModel teamModel = adminToTeam.getOfficeTeam();
				if (teamModel != null) {
					officeTeam = teams.get(teamModel.getOfficeTeamName());
				}
			}
			if (officeTeam != null) {
				// Assign the team responsible for administration
				architect.link(admin, officeTeam);
			}

			// Add the duties for the administrator (register also)
			for (DutyModel dutyModel : adminModel.getDuties()) {

				// Add the duty and register it
				String dutyName = dutyModel.getDutyName();
				OfficeDuty duty = admin.getDuty(dutyName);
				duties.put(adminName, dutyName, duty);

				// Keep registry of duties to their administrators
				dutyAdmins.put(dutyModel, adminModel);
			}
		}

		// Add processor to link duties with tasks
		processors.addSubSectionProcessor(new TasksToDutiesSubSectionProcessor(
				dutyAdmins, duties));

		// Create the listing of objects to be administered
		Map<String, List<AdministeredManagedObject>> administration = new HashMap<String, List<AdministeredManagedObject>>();
		for (ExternalManagedObjectModel extMo : office
				.getExternalManagedObjects()) {

			// Obtain the office object
			OfficeObject officeObject = officeObjects.get(extMo
					.getExternalManagedObjectName());

			// Add the object for administration
			for (ExternalManagedObjectToAdministratorModel extMoToAdmin : extMo
					.getAdministrators()) {
				AdministratorModel adminModel = extMoToAdmin.getAdministrator();
				if (adminModel != null) {
					String administratorName = adminModel
							.getAdministratorName();
					List<AdministeredManagedObject> list = administration
							.get(administratorName);
					if (list == null) {
						list = new LinkedList<AdministeredManagedObject>();
						administration.put(administratorName, list);
					}
					list.add(new AdministeredManagedObject(extMoToAdmin
							.getOrder(), officeObject));
				}
			}
		}
		for (OfficeManagedObjectModel moModel : office
				.getOfficeManagedObjects()) {

			// Obtain the office managed object
			OfficeManagedObject mo = managedObjects.get(moModel
					.getOfficeManagedObjectName());

			// Add the managed object for administration
			for (OfficeManagedObjectToAdministratorModel moToAdmin : moModel
					.getAdministrators()) {
				AdministratorModel adminModel = moToAdmin.getAdministrator();
				if (adminModel != null) {
					String administratorName = adminModel
							.getAdministratorName();
					List<AdministeredManagedObject> list = administration
							.get(administratorName);
					if (list == null) {
						list = new LinkedList<AdministeredManagedObject>();
						administration.put(administratorName, list);
					}
					list.add(new AdministeredManagedObject(
							moToAdmin.getOrder(), mo));
				}
			}
		}

		// Administer the managed objects
		for (AdministratorModel adminModel : office.getOfficeAdministrators()) {

			// Obtain the administrator
			String administratorName = adminModel.getAdministratorName();
			OfficeAdministrator admin = administrators.get(administratorName);

			// Obtain the objects to administer
			List<AdministeredManagedObject> administeredManagedObjects = administration
					.get(administratorName);
			if (administeredManagedObjects == null) {
				continue; // no managed objects to administer
			}

			// Order the managed objects
			Collections.sort(administeredManagedObjects);

			// Add managed objects for administration
			for (AdministeredManagedObject managedObject : administeredManagedObjects) {
				admin.administerManagedObject(managedObject.managedObject);
			}
		}

		// Handle escalations
		for (OfficeEscalationModel escalationModel : office
				.getOfficeEscalations()) {

			// Obtain the escalation
			String escalationType = escalationModel.getEscalationType();
			OfficeEscalation escalation = architect
					.addOfficeEscalation(escalationType);

			// Link to section input for handling
			OfficeSectionInput sectionInput = null;
			OfficeEscalationToOfficeSectionInputModel conn = escalationModel
					.getOfficeSectionInput();
			if (conn != null) {
				OfficeSectionInputModel sectionInputModel = conn
						.getOfficeSectionInput();
				if (sectionInputModel != null) {
					OfficeSectionModel sectionModel = this
							.getOfficeSectionForInput(office, sectionInputModel);
					if (sectionModel != null) {
						sectionInput = inputs.get(
								sectionModel.getOfficeSectionName(),
								sectionInputModel.getOfficeSectionInputName());
					}
				}
			}
			if (sectionInput != null) {
				// Link escalation to section input handling
				architect.link(escalation, sectionInput);
			}
		}

		// Process the sub sections
		for (OfficeSectionModel sectionModel : office.getOfficeSections()) {

			// Obtain the top level office sub sections
			OfficeSection subSection = sections.get(sectionModel
					.getOfficeSectionName());
			OfficeSubSectionModel subSectionModel = sectionModel
					.getOfficeSubSection();

			// Process the section and its sub sections
			this.processSubSections(null, subSection, subSectionModel,
					sectionModel, processors, architect);
		}
	}

	/**
	 * Obtains the {@link OfficeGovernanceModel} instances that provide
	 * {@link Governance} over the particular location.
	 * 
	 * @param x
	 *            X co-ordinate of location.
	 * @param y
	 *            Y co-ordinate of location.
	 * @param governances
	 *            {@link OfficeGovernanceModel} instances.
	 * @return {@link OfficeGovernanceModel} instances that provide
	 *         {@link Governance} over the particular location. May be empty
	 *         array if no {@link Governance} for location.
	 */
	private OfficeGovernanceModel[] getOfficeGovernancesOverLocation(int x,
			int y, List<OfficeGovernanceModel> governances) {

		// Create listing of governances for the location
		List<OfficeGovernanceModel> governing = new LinkedList<OfficeGovernanceModel>();

		// Add governances that cover the location
		for (OfficeGovernanceModel governance : governances) {
			for (OfficeGovernanceAreaModel area : governance
					.getOfficeGovernanceAreas()) {

				// Calculate points for area
				int leftX = area.getX();
				int rightX = area.getX() + area.getWidth();
				if (leftX > rightX) {
					// Swap as may be negative width
					int temp = leftX;
					leftX = rightX;
					rightX = temp;
				}
				int topY = area.getY();
				int bottomY = area.getY() + area.getHeight();
				if (topY > bottomY) {
					// Swap as may be negative height
					int temp = topY;
					topY = bottomY;
					bottomY = temp;
				}

				// Determine if governance covers the location
				if (((leftX <= x) && (x <= rightX))
						&& ((topY <= y) && (y <= bottomY))) {
					// Governance is governing the location
					governing.add(governance);
				}
			}
		}

		// Return the listing of governing governances for the location
		return governing.toArray(new OfficeGovernanceModel[governing.size()]);
	}

	/**
	 * Obtains the {@link ManagedObjectScope} from the managed object scope
	 * name.
	 * 
	 * @param managedObjectScope
	 *            Name of the {@link ManagedObjectScope}.
	 * @param architect
	 *            {@link OfficeArchitect}.
	 * @param managedObjectName
	 *            Name of the {@link OfficeManagedObjectModel}.
	 * @return {@link ManagedObjectScope} or <code>null</code> with issue
	 *         reported to the {@link OfficeArchitect}.
	 */
	private ManagedObjectScope getManagedObjectScope(String managedObjectScope,
			OfficeArchitect architect, String managedObjectName) {

		// Obtain the managed object scope
		if (OfficeChanges.PROCESS_MANAGED_OBJECT_SCOPE
				.equals(managedObjectScope)) {
			return ManagedObjectScope.PROCESS;
		} else if (OfficeChanges.THREAD_MANAGED_OBJECT_SCOPE
				.equals(managedObjectScope)) {
			return ManagedObjectScope.THREAD;
		} else if (OfficeChanges.WORK_MANAGED_OBJECT_SCOPE
				.equals(managedObjectScope)) {
			return ManagedObjectScope.WORK;
		}

		// Unknown scope if at this point
		architect.addIssue(
				"Unknown managed object scope " + managedObjectScope,
				AssetType.MANAGED_OBJECT, managedObjectName);
		return null;
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
	 * Recurses through the {@link OfficeSubSectionModel} instances processing
	 * the {@link OfficeSubSectionModel} instances.
	 * 
	 * @param subSectionPath
	 *            Path from top level {@link OfficeSubSectionModel} to current
	 *            {@link OfficeSubSectionModel}.
	 * @param subSection
	 *            {@link OfficeSubSection}.
	 * @param subSectionModel
	 *            {@link OfficeSubSectionModel}.
	 * @param sectionModel
	 *            {@link OfficeSectionModel}.
	 * @param processor
	 *            {@link SubSectionProcessor}.
	 * @param architect
	 *            {@link OfficeArchitect}.
	 */
	private void processSubSections(String subSectionPath,
			OfficeSubSection subSection, OfficeSubSectionModel subSectionModel,
			OfficeSectionModel sectionModel, SubSectionProcessor processor,
			OfficeArchitect architect) {

		// Ensure have sub section model
		if (subSectionModel == null) {
			return;
		}

		// Determine the path for current sub section
		String subSectionName = subSectionModel.getOfficeSubSectionName();
		if (subSectionPath == null) {
			subSectionPath = sectionModel.getOfficeSectionName();
		} else {
			subSectionPath = subSectionPath + "/" + subSectionName;
		}

		// Process the sub section
		processor.processSubSection(subSectionModel, subSection, architect,
				subSectionPath);

		// Process managed objects for the current sub section
		for (OfficeSectionManagedObjectModel managedObjectModel : subSectionModel
				.getOfficeSectionManagedObjects()) {

			// Obtain the corresponding office section managed object
			String managedObjectName = managedObjectModel
					.getOfficeSectionManagedObjectName();
			OfficeSectionManagedObject managedObject = null;
			for (OfficeSectionManagedObjectSource checkManagedObjectSource : subSection
					.getOfficeSectionManagedObjectSources()) {
				for (OfficeSectionManagedObject checkManagedObject : checkManagedObjectSource
						.getOfficeSectionManagedObjects()) {
					if (managedObjectName.equals(checkManagedObject
							.getOfficeSectionManagedObjectName())) {
						managedObject = checkManagedObject;
					}
				}
			}
			if (managedObject == null) {
				architect.addIssue(
						"Office model is out of sync with sections. Can not find managed object '"
								+ managedObjectName + "' [" + subSectionPath
								+ "]", AssetType.MANAGED_OBJECT,
						managedObjectName);
				continue; // must have task
			}

			// Process the managed object
			processor.processManagedObject(managedObjectModel, managedObject,
					architect, subSectionPath);
		}

		// Process tasks for the current sub section
		for (OfficeTaskModel taskModel : subSectionModel.getOfficeTasks()) {

			// Obtain the corresponding office task
			String taskName = taskModel.getOfficeTaskName();
			OfficeTask task = null;
			for (OfficeTask checkTask : subSection.getOfficeTasks()) {
				if (taskName.equals(checkTask.getOfficeTaskName())) {
					task = checkTask;
				}
			}
			if (task == null) {
				architect.addIssue(
						"Office model is out of sync with sections. Can not find task '"
								+ taskName + "' [" + subSectionPath + "]",
						AssetType.TASK, taskName);
				continue; // must have task
			}

			// Process the office task
			processor.processOfficeTask(taskModel, task, architect,
					subSectionPath);
		}

		// Recurse into the sub sections
		for (OfficeSubSectionModel subSubSectionModel : subSectionModel
				.getOfficeSubSections()) {

			// Obtain the corresponding sub section
			String subSubSectionName = subSubSectionModel
					.getOfficeSubSectionName();
			OfficeSubSection subSubSection = null;
			for (OfficeSubSection checkSection : subSection
					.getOfficeSubSections()) {
				if (subSubSectionName.equals(checkSection
						.getOfficeSectionName())) {
					subSubSection = checkSection;
				}
			}
			if (subSubSection == null) {
				architect.addIssue(
						"Office model is out of sync with sections. Can not find sub section '"
								+ subSubSectionName + "' [" + subSectionPath
								+ "]", null, null);
				continue; // must have office sub section
			}

			// Recursively process the sub sections
			this.processSubSections(subSectionPath, subSubSection,
					subSubSectionModel, sectionModel, processor, architect);
		}
	}

	/**
	 * Processes the {@link OfficeSubSection} instances.
	 */
	private static interface SubSectionProcessor {

		/**
		 * Processes the {@link OfficeSubSection}.
		 * 
		 * @param subSectionModel
		 *            {@link OfficeSubSectionModel}.
		 * @param subSection
		 *            {@link OfficeSubSection}.
		 * @param architect
		 *            {@link OfficeArchitect}.
		 * @param subSectionPath
		 *            Path to the {@link OfficeSubSection}.
		 */
		void processSubSection(OfficeSubSectionModel subSectionModel,
				OfficeSubSection subSection, OfficeArchitect architect,
				String subSectionPath);

		/**
		 * Processes the {@link OfficeSectionManagedObject}.
		 * 
		 * @param managedObjectModel
		 *            {@link OfficeSectionManagedObjectModel}.
		 * @param managedObject
		 *            {@link OfficeSectionManagedObject}.
		 * @param architect
		 *            {@link OfficeArchitect}.
		 * @param subSectionPath
		 *            Path to the {@link OfficeSubSection}.
		 */
		void processManagedObject(
				OfficeSectionManagedObjectModel managedObjectModel,
				OfficeSectionManagedObject managedObject,
				OfficeArchitect architect, String subSectionPath);

		/**
		 * Processes the {@link OfficeTask}.
		 * 
		 * @param taskModel
		 *            {@link OfficeTaskModel}.
		 * @param task
		 *            {@link OfficeTask}.
		 * @param architect
		 *            {@link OfficeArchitect}.
		 * @param subSectionPath
		 *            Path to the {@link OfficeSubSection}.
		 */
		void processOfficeTask(OfficeTaskModel taskModel, OfficeTask task,
				OfficeArchitect architect, String subSectionPath);
	}

	/**
	 * {@link SubSectionProcessor} implementation that by default does nothing.
	 */
	private static abstract class AbstractSubSectionProcessor implements
			SubSectionProcessor {

		/*
		 * ==================== SubSectionProcessor ====================
		 */

		@Override
		public void processSubSection(OfficeSubSectionModel subSectionModel,
				OfficeSubSection subSection, OfficeArchitect architect,
				String subSectionPath) {
			// Override to provide processing
		}

		@Override
		public void processManagedObject(
				OfficeSectionManagedObjectModel managedObjectModel,
				OfficeSectionManagedObject managedObject,
				OfficeArchitect architect, String subSectionPath) {
			// Override to provide processing
		}

		@Override
		public void processOfficeTask(OfficeTaskModel taskModel,
				OfficeTask task, OfficeArchitect architect,
				String subSectionPath) {
			// Override to provide processing
		}
	}

	/**
	 * {@link SubSectionProcessor} to process multiple
	 * {@link SubSectionProcessor} instances.
	 */
	private static class AggregateSubSectionProcessor implements
			SubSectionProcessor {

		/**
		 * {@link SubSectionProcessor} instances.
		 */
		private final List<SubSectionProcessor> processors = new LinkedList<SubSectionProcessor>();

		/**
		 * Adds a {@link SubSectionProcessor}.
		 * 
		 * @param processor
		 *            {@link SubSectionProcessor}.
		 */
		public void addSubSectionProcessor(SubSectionProcessor processor) {
			this.processors.add(processor);
		}

		/*
		 * ================= SubSectionProcessor =========================
		 */

		@Override
		public void processSubSection(OfficeSubSectionModel subSectionModel,
				OfficeSubSection subSection, OfficeArchitect architect,
				String subSectionPath) {
			for (SubSectionProcessor processor : this.processors) {
				processor.processSubSection(subSectionModel, subSection,
						architect, subSectionPath);
			}
		}

		@Override
		public void processManagedObject(
				OfficeSectionManagedObjectModel managedObjectModel,
				OfficeSectionManagedObject managedObject,
				OfficeArchitect architect, String subSectionPath) {
			for (SubSectionProcessor processor : this.processors) {
				processor.processManagedObject(managedObjectModel,
						managedObject, architect, subSectionPath);
			}
		}

		@Override
		public void processOfficeTask(OfficeTaskModel taskModel,
				OfficeTask task, OfficeArchitect architect,
				String subSectionPath) {
			for (SubSectionProcessor processor : this.processors) {
				processor.processOfficeTask(taskModel, task, architect,
						subSectionPath);
			}
		}
	}

	/**
	 * {@link SubSectionProcessor} to link {@link Duty} instances to the
	 * {@link Task} instances.
	 */
	private static class TasksToDutiesSubSectionProcessor extends
			AbstractSubSectionProcessor {

		/**
		 * {@link AdministratorModel} for the {@link DutyModel}.
		 */
		private final Map<DutyModel, AdministratorModel> dutyAdministrators;

		/**
		 * {@link OfficeDuty} by {@link OfficeAdministrator} name then
		 * {@link OfficeDuty} name.
		 */
		private final DoubleKeyMap<String, String, OfficeDuty> duties;

		/**
		 * Initiate.
		 * 
		 * @param dutyAdministrators
		 *            {@link AdministratorModel} for the {@link DutyModel}.
		 * @param duties
		 *            {@link OfficeDuty} by {@link OfficeAdministrator} name
		 *            then {@link OfficeDuty} name.
		 */
		public TasksToDutiesSubSectionProcessor(
				Map<DutyModel, AdministratorModel> dutyAdministrators,
				DoubleKeyMap<String, String, OfficeDuty> duties) {
			this.dutyAdministrators = dutyAdministrators;
			this.duties = duties;
		}

		/*
		 * ==================== SubSectionProcessor ====================
		 */

		@Override
		public void processOfficeTask(OfficeTaskModel taskModel,
				OfficeTask task, OfficeArchitect architect,
				String subSectionPath) {

			// Determine if task is linked to duties
			List<OfficeTaskToPreDutyModel> preDuties = taskModel.getPreDuties();
			List<OfficeTaskToPostDutyModel> postDuties = taskModel
					.getPostDuties();
			if ((preDuties.size() == 0) && (postDuties.size() == 0)) {
				return; // no duties to link for task
			}

			// Obtain the task name
			String taskName = task.getOfficeTaskName();

			// Link the pre task duties
			for (int i = 0; i < preDuties.size(); i++) {
				OfficeTaskToPreDutyModel conn = preDuties.get(i);

				// Obtain the pre task duty
				OfficeDuty preTaskDuty = null;
				DutyModel dutyModel = conn.getDuty();
				if (dutyModel != null) {
					AdministratorModel adminModel = this.dutyAdministrators
							.get(dutyModel);
					if (adminModel != null) {
						preTaskDuty = duties.get(
								adminModel.getAdministratorName(),
								dutyModel.getDutyName());
					}
				}
				if (preTaskDuty == null) {
					architect.addIssue("Can not find pre duty " + i
							+ " for task '" + taskName + "' [" + subSectionPath
							+ "]", AssetType.TASK, taskName);
					continue; // must have duty
				}

				// Add the pre task duty
				task.addPreTaskDuty(preTaskDuty);
			}

			// Link the post task duties
			for (int i = 0; i < postDuties.size(); i++) {
				OfficeTaskToPostDutyModel conn = postDuties.get(i);

				// Obtain the post task duty
				OfficeDuty postTaskDuty = null;
				DutyModel dutyModel = conn.getDuty();
				if (dutyModel != null) {
					AdministratorModel adminModel = dutyAdministrators
							.get(dutyModel);
					if (adminModel != null) {
						postTaskDuty = duties.get(
								adminModel.getAdministratorName(),
								dutyModel.getDutyName());
					}
				}
				if (postTaskDuty == null) {
					architect.addIssue("Can not find post duty " + i
							+ " for task '" + taskName + "' [" + subSectionPath
							+ "]", AssetType.TASK, taskName);
					continue; // must have duty
				}

				// Add the post task duty
				task.addPostTaskDuty(postTaskDuty);
			}
		}
	}

	/**
	 * {@link SubSectionProcessor} to provide {@link Governance} to the
	 * {@link OfficeSubSection} instances.
	 */
	private static class GovernanceSubSectionProcessor extends
			AbstractSubSectionProcessor {

		/**
		 * {@link OfficeGovernance} instances by their name.
		 */
		private final Map<String, OfficeGovernance> governances;

		/**
		 * Initiate.
		 * 
		 * @param governances
		 *            {@link OfficeGovernance} instances by their name.
		 */
		public GovernanceSubSectionProcessor(
				Map<String, OfficeGovernance> governances) {
			this.governances = governances;
		}

		/*
		 * ================== SubSectionProcessor ======================
		 */

		@Override
		public void processSubSection(OfficeSubSectionModel subSectionModel,
				OfficeSubSection subSection, OfficeArchitect architect,
				String subSectionPath) {

			// Link the governances
			for (OfficeSubSectionToOfficeGovernanceModel conn : subSectionModel
					.getOfficeGovernances()) {
				OfficeGovernanceModel govModel = conn.getOfficeGovernance();
				if (govModel != null) {

					// Obtain the governance
					OfficeGovernance governance = this.governances.get(govModel
							.getOfficeGovernanceName());
					if (governance != null) {

						// Provide governance over the sub section
						subSection.addGovernance(governance);
					}
				}
			}
		}

		@Override
		public void processManagedObject(
				OfficeSectionManagedObjectModel managedObjectModel,
				OfficeSectionManagedObject managedObject,
				OfficeArchitect architect, String subSectionPath) {

			// Link the governances
			for (OfficeSectionManagedObjectToOfficeGovernanceModel conn : managedObjectModel
					.getOfficeGovernances()) {
				OfficeGovernanceModel govModel = conn.getOfficeGovernance();
				if (govModel != null) {

					// Obtain the governance
					OfficeGovernance governance = this.governances.get(govModel
							.getOfficeGovernanceName());
					if (governance != null) {

						// Provide governance over the managed object
						governance.governManagedObject(managedObject);
					}
				}
			}
		}

		@Override
		public void processOfficeTask(OfficeTaskModel taskModel,
				OfficeTask task, OfficeArchitect architect,
				String subSectionPath) {

			// Link the governances
			for (OfficeTaskToOfficeGovernanceModel conn : taskModel
					.getOfficeGovernances()) {
				OfficeGovernanceModel govModel = conn.getOfficeGovernance();
				if (govModel != null) {

					// Obtain the governance
					OfficeGovernance governance = this.governances.get(govModel
							.getOfficeGovernanceName());
					if (governance != null) {

						// Provide governance of the task
						task.addGovernance(governance);
					}
				}
			}
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

	/**
	 * {@link ManagedObject} to be administered.
	 */
	private static class AdministeredManagedObject implements
			Comparable<AdministeredManagedObject> {

		/**
		 * Position in the order that the objects are administered.
		 */
		public final String order;

		/**
		 * {@link AdministerableManagedObject}.
		 */
		public final AdministerableManagedObject managedObject;

		/**
		 * Initiate.
		 * 
		 * @param order
		 *            Position in the order that the objects are administered.
		 * @param managedObject
		 *            {@link AdministerableManagedObject}.
		 */
		public AdministeredManagedObject(String order,
				AdministerableManagedObject managedObject) {
			this.order = order;
			this.managedObject = managedObject;
		}

		/*
		 * ================== Comparable ==========================
		 */

		@Override
		public int compareTo(AdministeredManagedObject that) {
			return this.getOrder(this.order) - this.getOrder(that.order);
		}

		/**
		 * Obtains the order as an {@link Integer}.
		 * 
		 * @param order
		 *            Text order value.
		 * @return Numeric order value.
		 */
		private int getOrder(String order) {
			try {
				return Integer.parseInt(order);
			} catch (NumberFormatException ex) {
				return Integer.MAX_VALUE; // invalid number so make last
			}
		}
	}

}