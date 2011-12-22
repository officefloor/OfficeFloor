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

package net.officefloor.autowire.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.officefloor.autowire.AutoWire;
import net.officefloor.autowire.AutoWireGovernance;
import net.officefloor.autowire.AutoWireResponsibility;
import net.officefloor.autowire.AutoWireSection;
import net.officefloor.autowire.AutoWireSectionFactory;
import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.governance.GovernanceType;
import net.officefloor.compile.properties.Property;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.governance.source.GovernanceSource;
import net.officefloor.compile.spi.office.DependentManagedObject;
import net.officefloor.compile.spi.office.ObjectDependency;
import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.OfficeEscalation;
import net.officefloor.compile.spi.office.OfficeGovernance;
import net.officefloor.compile.spi.office.OfficeObject;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.office.OfficeSectionInput;
import net.officefloor.compile.spi.office.OfficeSectionManagedObject;
import net.officefloor.compile.spi.office.OfficeSectionManagedObjectSource;
import net.officefloor.compile.spi.office.OfficeSectionObject;
import net.officefloor.compile.spi.office.OfficeSectionOutput;
import net.officefloor.compile.spi.office.OfficeSubSection;
import net.officefloor.compile.spi.office.OfficeTask;
import net.officefloor.compile.spi.office.OfficeTeam;
import net.officefloor.compile.spi.office.TypeQualification;
import net.officefloor.compile.spi.office.source.OfficeSource;
import net.officefloor.compile.spi.office.source.OfficeSourceContext;
import net.officefloor.compile.spi.office.source.impl.AbstractOfficeSource;
import net.officefloor.compile.spi.section.SectionInput;
import net.officefloor.compile.spi.section.SectionOutput;
import net.officefloor.compile.spi.section.source.SectionSource;
import net.officefloor.frame.api.build.OfficeFloorIssues.AssetType;
import net.officefloor.frame.api.escalate.Escalation;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.spi.governance.Governance;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.team.Team;

/**
 * {@link OfficeSource} implementation that auto-wires the configuration based
 * on type.
 * 
 * @author Daniel Sagenschneider
 */
public class AutoWireOfficeSource extends AbstractOfficeSource {

	/**
	 * {@link OfficeFloorCompiler}.
	 */
	private final OfficeFloorCompiler compiler;

	/**
	 * {@link AutoWireSection} instances.
	 */
	private final List<AutoWireSection> sections = new LinkedList<AutoWireSection>();

	/**
	 * {@link Link} instances.
	 */
	private final List<Link> links = new LinkedList<Link>();

	/**
	 * {@link EscalationLink} instances.
	 */
	private final List<EscalationLink> escalations = new LinkedList<EscalationLink>();

	/**
	 * {@link AutoWireResponsibility} instances.
	 */
	private final List<AutoWireResponsibility> responsibilities = new LinkedList<AutoWireResponsibility>();

	/**
	 * {@link AutoWireGovernance} instances.
	 */
	private final List<AutoWireGovernance> governances = new LinkedList<AutoWireGovernance>();

	/**
	 * Extension interface to their {@link AutoWire} instances.
	 */
	private final Map<Class<?>, List<AutoWire>> extensionInterfaceToAutoWiring = new HashMap<Class<?>, List<AutoWire>>();

	/**
	 * Available {@link AutoWire} instances.
	 */
	private final List<AutoWire> availableAutoWiring = new LinkedList<AutoWire>();

	/**
	 * Initiate.
	 * 
	 * @param compiler
	 *            {@link OfficeFloorCompiler}.
	 */
	public AutoWireOfficeSource(OfficeFloorCompiler compiler) {
		this.compiler = compiler;
	}

	/**
	 * Default constructor.
	 */
	public AutoWireOfficeSource() {
		this(OfficeFloorCompiler.newOfficeFloorCompiler(null));
	}

	/**
	 * Adds an {@link OfficeSection}.
	 * 
	 * @param sectionName
	 *            Name of the {@link OfficeSection}.
	 * @param sectionSourceClass
	 *            {@link SectionSource} class name.
	 * @param sectionLocation
	 *            {@link OfficeSection} location.
	 * @param sectionFactory
	 *            {@link AutoWireSectionFactory}.
	 * @return {@link AutoWireSection} to configure properties and link flows.
	 */
	@SuppressWarnings("unchecked")
	public <S extends SectionSource, A extends AutoWireSection> A addSection(
			String sectionName, String sectionSourceClassName,
			String sectionLocation, AutoWireSectionFactory<A> sectionFactory) {

		// Create the properties
		PropertyList properties = this.compiler.createPropertyList();

		// Create and add the section
		AutoWireSection section = new AutoWireSectionImpl(this.compiler,
				sectionName, sectionSourceClassName, sectionLocation,
				properties);

		// Determine if override the section
		if (sectionFactory != null) {
			A overridden = sectionFactory.createAutoWireSection(section);
			if (overridden != null) {
				// Override the section
				section = overridden;
			}
		}

		// Register the section
		this.sections.add(section);

		// Return the section
		return (A) section;
	}

	/**
	 * Adds an {@link OfficeSection} allowing to override the
	 * {@link AutoWireSection}.
	 * 
	 * @param sectionName
	 *            Name of the {@link OfficeSection}.
	 * @param sectionSourceClassName
	 *            {@link SectionSource} class name.
	 * @param sectionLocation
	 *            {@link OfficeSection} location.
	 * @return {@link AutoWireSection} to configure properties and link flows.
	 */
	public <S extends SectionSource> AutoWireSection addSection(
			String sectionName, String sectionSourceClassName,
			String sectionLocation) {
		return this.addSection(sectionName, sectionSourceClassName,
				sectionLocation, null);
	}

	/**
	 * Obtains the {@link AutoWireSection} by its name.
	 * 
	 * @param sectionName
	 *            Name of the {@link AutoWireSection}.
	 * @return {@link AutoWireSection} or <code>null</code> if no
	 *         {@link AutoWireSection} by the name.
	 */
	public AutoWireSection getSection(String sectionName) {
		// Find first section by the name
		for (AutoWireSection section : this.sections) {
			if (sectionName.equals(section.getSectionName())) {
				return section; // found
			}
		}

		// As here, did not find section
		return null;
	}

	/**
	 * Links the source {@link SectionOutput} to a target {@link SectionInput}.
	 * 
	 * @param sourceSection
	 *            Source section.
	 * @param sourceOutputName
	 *            Name of the source {@link SectionOutput}.
	 * @param targetSection
	 *            Target section.
	 * @param targetInputName
	 *            Name of the target {@link SectionInput}.
	 */
	public void link(AutoWireSection sourceSection, String sourceOutputName,
			AutoWireSection targetSection, String targetInputName) {
		this.links.add(new Link(sourceSection, sourceOutputName, targetSection,
				targetInputName));
	}

	/**
	 * <p>
	 * Determines if the {@link AutoWireSection} output is configured for
	 * linking.
	 * <p>
	 * This aids configuration by allowing to know if {@link SectionOutput}
	 * flows have been configured (linked).
	 * 
	 * @param section
	 *            {@link AutoWireSection}.
	 * @param sectionOutputName
	 *            {@link SectionOutput} name.
	 * @return <code>true</code> if configured for linking, otherwise
	 *         <code>false</code>.
	 */
	public boolean isLinked(AutoWireSection section, String sectionOutputName) {

		// Determine if linked
		for (Link link : this.links) {
			if ((link.sourceSection.getSectionName().equals(section
					.getSectionName()))
					&& (link.sourceOutputName.equals(sectionOutputName))) {
				// Matching section output so configured for linking
				return true;
			}
		}

		// As here, not linked
		return false;
	}

	/**
	 * Links the handling of the {@link Escalation} to the
	 * {@link AutoWireSection}.
	 * 
	 * @param escalationType
	 *            TYpe of {@link Escalation}.
	 * @param section
	 *            {@link AutoWireSection}.
	 * @param sectionInputName
	 *            Name of the {@link OfficeSectionInput}.
	 */
	public void linkEscalation(Class<? extends Throwable> escalationType,
			AutoWireSection section, String sectionInputName) {
		this.escalations.add(new EscalationLink(escalationType, section,
				sectionInputName));
	}

	/**
	 * <p>
	 * Adds {@link Governance} to this {@link Office}.
	 * <p>
	 * The {@link Governance} is auto-wired to {@link ManagedObject} instances
	 * with the matching extension interface.
	 * 
	 * @param governanceName
	 *            Name of the {@link Governance}.
	 * @param governanceSourceClassName
	 *            {@link Class} name of the {@link GovernanceSource}. May be an
	 *            alias.
	 * @return {@link AutoWireGovernance}.
	 * 
	 * @see #addObjectExtension(Class, Class...)
	 */
	public AutoWireGovernance addGovernance(String governanceName,
			String governanceSourceClassName) {

		// Create the properties
		PropertyList properties = this.compiler.createPropertyList();

		// Create the auto-wire governance
		AutoWireGovernance governance = new AutoWireGovernanceImpl(
				governanceName, governanceSourceClassName, this.compiler,
				properties);

		// Register the auto-wire governance
		this.governances.add(governance);

		// Return the auto-wire governance
		return governance;
	}

	/**
	 * <p>
	 * Adds that the {@link OfficeObject} has the ability to be used with the
	 * specified extension interfaces.
	 * <p>
	 * This aids auto-wiring the {@link Governance} for the {@link OfficeObject}.
	 * 
	 * @param autoWire
	 *            {@link OfficeObject} {@link AutoWire}.
	 * @param extensionInterfaces
	 *            Extension interfaces available for the {@link OfficeObject}.
	 */
	public void addOfficeObjectExtension(AutoWire autoWire,
			Class<?>... extensionInterfaces) {
		for (Class<?> extensionInterface : extensionInterfaces) {

			// Obtain the auto wiring for the extension type
			List<AutoWire> autoWiring = this.extensionInterfaceToAutoWiring
					.get(extensionInterface);
			if (autoWiring == null) {
				autoWiring = new LinkedList<AutoWire>();
				this.extensionInterfaceToAutoWiring.put(extensionInterface,
						autoWiring);
			}

			// Add the auto wire
			autoWiring.add(autoWire);
		}
	}

	/**
	 * Adds an available {@link AutoWire}.
	 * 
	 * @param autoWire
	 *            {@link AutoWire}.
	 */
	public void addAvailableAutoWire(AutoWire autoWire) {
		this.availableAutoWiring.add(autoWire);
	}

	/**
	 * Adds an {@link OfficeTeam} responsible for executing {@link Task}
	 * instances that has an object dependency of the input type.
	 * 
	 * @param autoWire
	 *            Object dependency {@link AutoWire} for the {@link Task}.
	 * @return {@link AutoWireResponsibility} for the {@link OfficeTeam}.
	 */
	public AutoWireResponsibility addResponsibility(AutoWire autoWire) {
		AutoWireResponsibility responsibility = new AutoWireResponsibilityImpl(
				autoWire);
		this.responsibilities.add(responsibility);
		return responsibility;
	}

	/*
	 * ===================== OfficeSource =========================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		// No specification
	}

	@Override
	public void sourceOffice(OfficeArchitect architect,
			OfficeSourceContext context) throws Exception {

		// Add the default team
		OfficeTeam defaultTeam = architect.addOfficeTeam("team");

		// Add the responsibility teams
		List<ResponsibleTeam> responsibleTeams = new ArrayList<ResponsibleTeam>(
				this.responsibilities.size());
		for (AutoWireResponsibility responsibility : this.responsibilities) {

			// Add the Office Team
			OfficeTeam officeTeam = architect.addOfficeTeam(responsibility
					.getOfficeTeamName());

			// Register the responsible team
			AutoWire dependencyAutoWire = responsibility
					.getDependencyAutoWire();
			ResponsibleTeam responsibleTeam = new ResponsibleTeam(
					dependencyAutoWire, officeTeam);

			// Add the responsible team
			responsibleTeams.add(responsibleTeam);
		}

		// Load the sections
		List<OfficeSection> officeSections = new LinkedList<OfficeSection>();
		Map<String, OfficeSection> officeSectionsByName = new HashMap<String, OfficeSection>();
		Map<String, Map<String, OfficeSectionInput>> inputs = new HashMap<String, Map<String, OfficeSectionInput>>();
		Map<String, Map<String, OfficeSectionOutput>> outputs = new HashMap<String, Map<String, OfficeSectionOutput>>();
		Map<AutoWire, OfficeObject> objects = new HashMap<AutoWire, OfficeObject>();
		for (AutoWireSection section : this.sections) {

			// Obtain the section name
			String sectionName = section.getSectionName();

			// Add the section
			OfficeSection officeSection = architect.addOfficeSection(
					sectionName, section.getSectionSourceClassName(),
					section.getSectionLocation(), section.getProperties());

			// Register the section
			officeSections.add(officeSection);
			officeSectionsByName.put(sectionName, officeSection);

			// Link the objects
			for (OfficeSectionObject object : officeSection
					.getOfficeSectionObjects()) {

				// Obtain object details
				String objectType = object.getObjectType();
				String typeQualifier = object.getTypeQualifier();

				// Obtain appropriate auto-wire
				AutoWire autoWire = AutoWireOfficeFloorSource
						.getAppropriateAutoWire(typeQualifier, objectType,
								this.availableAutoWiring);
				if (autoWire == null) {
					String objectName = object.getOfficeSectionObjectName();
					architect.addIssue("No available auto-wiring for "
							+ OfficeSectionObject.class.getSimpleName() + " "
							+ objectName + " (qualifier=" + typeQualifier
							+ ", type=" + objectType + ") from "
							+ OfficeSection.class.getSimpleName() + " "
							+ sectionName, null, null);
					continue; // no available auto-wiring for section object
				}

				// Link to appropriate Office Object
				OfficeObject officeObject = objects.get(autoWire);
				if (officeObject == null) {
					officeObject = architect.addOfficeObject(
							autoWire.getQualifiedType(), autoWire.getType());
					objects.put(autoWire, officeObject);
				}
				architect.link(object, officeObject);
			}

			// Register the inputs
			Map<String, OfficeSectionInput> sectionInputs = new HashMap<String, OfficeSectionInput>();
			inputs.put(sectionName, sectionInputs);
			for (OfficeSectionInput input : officeSection
					.getOfficeSectionInputs()) {
				String inputName = input.getOfficeSectionInputName();
				sectionInputs.put(inputName, input);
			}

			// Register the outputs
			Map<String, OfficeSectionOutput> sectionOutputs = new HashMap<String, OfficeSectionOutput>();
			outputs.put(sectionName, sectionOutputs);
			for (OfficeSectionOutput output : officeSection
					.getOfficeSectionOutputs()) {
				String outputName = output.getOfficeSectionOutputName();
				sectionOutputs.put(outputName, output);
			}

			// Link section tasks to team.
			// Must be after objects to ensure linked.
			for (OfficeTask task : officeSection.getOfficeTasks()) {
				this.assignTeam(task, responsibleTeams, defaultTeam, architect);
			}

			// Link sub section tasks to team
			for (OfficeSubSection subSection : officeSection
					.getOfficeSubSections()) {
				this.linkTasksToTeams(subSection, responsibleTeams,
						defaultTeam, architect);
			}
		}

		// Link outputs to inputs
		for (Link link : this.links) {

			// Obtain the link details
			String sourceSectionName = link.sourceSection.getSectionName();
			String sourceOutputName = link.sourceOutputName;
			String outputName = sourceSectionName + ":" + sourceOutputName;
			String targetSectionName = link.targetSection.getSectionName();
			String targetInputName = link.targetInputName;
			String inputName = targetSectionName + ":" + targetInputName;

			// Obtain the output
			OfficeSectionOutput sectionOutput = null;
			Map<String, OfficeSectionOutput> sectionOutputs = outputs
					.get(sourceSectionName);
			if (sectionOutputs != null) {
				sectionOutput = sectionOutputs.get(sourceOutputName);
			}
			if (sectionOutput == null) {
				architect.addIssue("Unknown section output '" + outputName
						+ "' to link to section input '" + inputName + "'",
						AssetType.TASK, outputName);
				continue; // no output so can not link
			}

			// Obtain the input
			OfficeSectionInput sectionInput = null;
			Map<String, OfficeSectionInput> sectionInputs = inputs
					.get(targetSectionName);
			if (sectionInputs != null) {
				sectionInput = sectionInputs.get(targetInputName);
			}
			if (sectionInput == null) {
				architect.addIssue("Unknown section input '" + inputName
						+ "' for linking section output '" + outputName + "'",
						AssetType.TASK, inputName);
				continue; // no input so can not link
			}

			// Link the output to the input
			architect.link(sectionOutput, sectionInput);
		}

		// Link escalations to inputs
		for (EscalationLink link : this.escalations) {

			// Obtain the link details
			String escalationTypeName = link.escalationType.getName();
			String sectionName = link.targetSection.getSectionName();
			String inputName = link.targetInputName;

			// Add the Escalation
			OfficeEscalation escalation = architect
					.addOfficeEscalation(escalationTypeName);

			// Obtain the input
			OfficeSectionInput sectionInput = null;
			Map<String, OfficeSectionInput> sectionInputs = inputs
					.get(sectionName);
			if (sectionInputs != null) {
				sectionInput = sectionInputs.get(inputName);
			}
			if (sectionInput == null) {
				architect.addIssue("Unknown section input '" + sectionName
						+ ":" + inputName + "' for linking escalation '"
						+ escalationTypeName + "'", null, null);
				continue; // no input so can not link
			}

			// Link the escalation to section input
			architect.link(escalation, sectionInput);
		}

		// Add the governances
		for (AutoWireGovernance governance : this.governances) {

			// Obtain the governance details
			String governanceName = governance.getGovernanceName();
			String governanceSourceClassName = governance
					.getGovernanceSourceClassName();
			PropertyList properties = governance.getProperties();

			// Add the Governance
			OfficeGovernance officeGovernance = architect.addOfficeGovernance(
					governance.getGovernanceName(), governanceSourceClassName);
			for (Property property : properties) {
				officeGovernance.addProperty(property.getName(),
						property.getValue());
			}

			// Obtain the extension interface for the governance
			GovernanceType<?, ?> governanceType = context.loadGovernanceType(
					governanceSourceClassName, properties);
			if (governanceType == null) {
				continue; // need extension interface from type
			}
			Class<?> extensionInterface = governanceType
					.getExtensionInterface();

			// Assign team for governance
			this.assignTeam(officeGovernance, extensionInterface,
					responsibleTeams, defaultTeam, architect);

			// Govern the sections
			for (AutoWireSection section : governance.getGovernedSections()) {

				// Obtain the section
				String sectionName = section.getSectionName();
				OfficeSection officeSection = officeSectionsByName
						.get(sectionName);
				if (officeSection == null) {
					architect.addIssue("Unknown section '" + sectionName
							+ "' to be governed", AssetType.GOVERNANCE,
							governanceName);
					continue; // can not govern unknown section
				}

				// Govern the section
				officeSection.addGovernance(officeGovernance);
			}

			// Obtain the auto wiring for the extension interface
			List<AutoWire> autoWiring = this.extensionInterfaceToAutoWiring
					.get(extensionInterface);
			if (autoWiring != null) {

				// Govern the office objects
				for (AutoWire autoWire : autoWiring) {

					// Obtain the office object
					OfficeObject officeObject = objects.get(autoWire);
					if (officeObject == null) {
						continue; // no office object for type
					}

					// Govern the office object
					officeGovernance.governManagedObject(officeObject);
				}
			}

			// Traverse the sections to govern managed objects
			for (OfficeSection section : officeSections) {
				this.governSectionManagedObjects(section, extensionInterface,
						officeGovernance);
			}
		}
	}

	/**
	 * Provides {@link Governance} to the {@link OfficeSectionManagedObject}
	 * instances that support the extension interface.
	 * 
	 * @param section
	 *            {@link OfficeSubSection} to check for
	 *            {@link OfficeSectionManagedObject} instances for
	 *            {@link Governance}.
	 * @param extensionInterface
	 *            Extension interface of the {@link Governance}.
	 * @param governance
	 *            {@link OfficeGovernance}.
	 */
	private void governSectionManagedObjects(OfficeSubSection section,
			Class<?> extensionInterface, OfficeGovernance governance) {

		// Check all section managed objects
		for (OfficeSectionManagedObjectSource moSource : section
				.getOfficeSectionManagedObjectSources()) {
			for (OfficeSectionManagedObject mo : moSource
					.getOfficeSectionManagedObjects()) {
				for (Class<?> supportedExtensionInterface : mo
						.getSupportedExtensionInterfaces()) {
					if (extensionInterface.equals(supportedExtensionInterface)) {
						// Supports extension so govern
						governance.governManagedObject(mo);
					}
				}
			}
		}

		// Recursively govern the sub sections
		for (OfficeSubSection subSection : section.getOfficeSubSections()) {
			this.governSectionManagedObjects(subSection, extensionInterface,
					governance);
		}
	}

	/**
	 * Links the {@link OfficeTask} instances of the {@link OfficeSubSection} to
	 * the {@link OfficeTeam}.
	 * 
	 * @param subSection
	 *            {@link OfficeSubSection}.
	 * @param responsibleTeams
	 *            {@link ResponsibleTeam} instances.
	 * @param defaultTeam
	 *            {@link OfficeTeam}.
	 * @param architect
	 *            {@link OfficeArchitect}.
	 */
	private void linkTasksToTeams(OfficeSubSection subSection,
			List<ResponsibleTeam> responsibleTeams, OfficeTeam defaultTeam,
			OfficeArchitect architect) {

		// Link section tasks to team
		for (OfficeTask task : subSection.getOfficeTasks()) {
			this.assignTeam(task, responsibleTeams, defaultTeam, architect);
		}

		// Recursively link the sub sections
		for (OfficeSubSection subSubSection : subSection.getOfficeSubSections()) {
			this.linkTasksToTeams(subSubSection, responsibleTeams, defaultTeam,
					architect);
		}
	}

	/**
	 * Assigns the {@link OfficeTeam} for the {@link OfficeTask}.
	 * 
	 * @param task
	 *            {@link OfficeTask}.
	 * @param responsibleTeams
	 *            {@link ResponsibleTeam} instances.
	 * @param defaultTeam
	 *            Default {@link OfficeTeam}.
	 * @param architect
	 *            {@link OfficeArchitect}.
	 */
	private void assignTeam(OfficeTask task,
			List<ResponsibleTeam> responsibleTeams, OfficeTeam defaultTeam,
			OfficeArchitect architect) {

		// Determine if team to be responsible
		for (ResponsibleTeam responsibleTeam : responsibleTeams) {
			if (responsibleTeam.isResponsible(task)) {
				// Team responsible for the task, so link
				architect.link(task.getTeamResponsible(),
						responsibleTeam.officeTeam);

				// Team assigned
				return;
			}
		}

		// As here, default team is responsible
		architect.link(task.getTeamResponsible(), defaultTeam);
	}

	/**
	 * Assigns the {@link OfficeTeam} for the {@link OfficeGovernance}.
	 * 
	 * @param governance
	 *            {@link OfficeGovernance}.
	 * @param extensionInterface
	 *            Extension interface for the {@link OfficeGovernance}.
	 * @param responsibleTeams
	 *            {@link ResponsibleTeam} instances.
	 * @param defaultTeam
	 *            Default {@link OfficeTeam}.
	 * @param architect
	 *            {@link OfficeArchitect}.
	 */
	private void assignTeam(OfficeGovernance governance,
			Class<?> extensionInterface,
			List<ResponsibleTeam> responsibleTeams, OfficeTeam defaultTeam,
			OfficeArchitect architect) {

		// Determine if team to be responsible
		for (ResponsibleTeam responsibleTeam : responsibleTeams) {
			if (responsibleTeam.isResponsible(extensionInterface)) {
				// Team responsible for the governance, so link
				architect.link(governance, responsibleTeam.officeTeam);

				// Team assigned
				return;
			}
		}

		// As here, default team is responsible
		architect.link(governance, defaultTeam);
	}

	/**
	 * Link of {@link SectionOutput} to {@link SectionInput}.
	 */
	private static class Link {

		/**
		 * Source {@link AutoWireSection}.
		 */
		public final AutoWireSection sourceSection;

		/**
		 * Source {@link SectionOutput} name.
		 */
		public final String sourceOutputName;

		/**
		 * Target {@link AutoWireSection}.
		 */
		public final AutoWireSection targetSection;

		/**
		 * Target {@link SectionInput} name.
		 */
		public final String targetInputName;

		/**
		 * Initiate.
		 * 
		 * @param sourceSection
		 *            Source {@link AutoWireSection}.
		 * @param sourceOutputName
		 *            Source {@link SectionOutput} name.
		 * @param targetSection
		 *            Target {@link AutoWireSection}.
		 * @param targetInputName
		 *            Target {@link SectionInput} name.
		 */
		public Link(AutoWireSection sourceSection, String sourceOutputName,
				AutoWireSection targetSection, String targetInputName) {
			this.sourceSection = sourceSection;
			this.sourceOutputName = sourceOutputName;
			this.targetSection = targetSection;
			this.targetInputName = targetInputName;
		}
	}

	/**
	 * Link of {@link Escalation} to {@link SectionInput}.
	 */
	private static class EscalationLink {

		/**
		 * Type of {@link Escalation}.
		 */
		public final Class<? extends Throwable> escalationType;

		/**
		 * Target {@link AutoWireSection}.
		 */
		public final AutoWireSection targetSection;

		/**
		 * Target {@link SectionInput} name.
		 */
		public final String targetInputName;

		/**
		 * Initiate.
		 * 
		 * @param escalationType
		 *            Type of {@link Escalation}.
		 * @param targetSection
		 *            Target {@link AutoWireSection}.
		 * @param targetInputName
		 *            Target {@link SectionInput} name.
		 */
		public EscalationLink(Class<? extends Throwable> escalationType,
				AutoWireSection targetSection, String targetInputName) {
			this.escalationType = escalationType;
			this.targetSection = targetSection;
			this.targetInputName = targetInputName;
		}
	}

	/**
	 * Responsible {@link Team}.
	 */
	private static class ResponsibleTeam {

		/**
		 * Dependency {@link AutoWire}.
		 */
		private final AutoWire dependencyAutoWire;

		/**
		 * {@link OfficeTeam}.
		 */
		public final OfficeTeam officeTeam;

		/**
		 * Initiate.
		 * 
		 * @param dependencyAutoWire
		 *            Dependency {@link AutoWire}.
		 * @param officeTeam
		 *            {@link OfficeTeam}.
		 */
		public ResponsibleTeam(AutoWire dependencyAutoWire,
				OfficeTeam officeTeam) {
			this.dependencyAutoWire = dependencyAutoWire;
			this.officeTeam = officeTeam;
		}

		/**
		 * Determines if the {@link OfficeTeam} is responsible for the
		 * {@link OfficeTask}.
		 * 
		 * @param task
		 *            {@link OfficeTeam}.
		 * @return <code>true</code> if the {@link OfficeTeam} is potentially
		 *         responsible for the {@link OfficeTask}.
		 */
		public boolean isResponsible(OfficeTask task) {
			return this.isResponsible(task.getObjectDependencies(),
					new HashSet<DependentManagedObject>());
		}

		/**
		 * Determines if the {@link OfficeTeam} is responsible for the
		 * {@link OfficeGovernance} for the extension interface.
		 * 
		 * @param extensionInterface
		 *            Extension interface of the {@link OfficeGovernance}.
		 * @return <code>true</code> if the {@link OfficeTeam} is potentially
		 *         responsible for the {@link OfficeGovernance}.
		 */
		public boolean isResponsible(Class<?> extensionInterface) {

			// Extension interface matching can not be qualified
			if (this.dependencyAutoWire.getQualifier() != null) {
				return false; // not match responsibility if qualified
			}

			// Return based on type matching
			return (this.dependencyAutoWire.getType().equals(extensionInterface
					.getName()));
		}

		/**
		 * Determines if {@link OfficeTeam} is responsible for the
		 * {@link ObjectDependency} instances.
		 * 
		 * @param dependencies
		 *            {@link ObjectDependency} instances.
		 * @param objects
		 *            Set of {@link DependentManagedObject} instances.
		 * @return <code>true</code> if the {@link OfficeTeam} is potentially
		 *         responsible for the {@link ObjectDependency} instances.
		 */
		private boolean isResponsible(ObjectDependency[] dependencies,
				Set<DependentManagedObject> objects) {

			// Obtain details of auto-wire
			String autoWireQualifier = this.dependencyAutoWire.getQualifier();
			String autoWireType = this.dependencyAutoWire.getType();

			// Determine if responsible for dependent objects
			for (ObjectDependency dependency : dependencies) {

				// Obtain the dependent object
				DependentManagedObject object = dependency
						.getDependentManagedObject();
				if (object == null) {
					continue; // ignore if dependency not linked
				}

				// Determine if already processed dependent object
				if (objects.contains(object)) {
					continue; // ignore as already processed object
				}
				objects.add(object);

				// Determine if responsible
				for (TypeQualification qualification : object
						.getTypeQualifications()) {

					// Must match on type
					if (!(autoWireType.equals(qualification.getType()))) {
						continue; // ignore as not match on type
					}

					// Determine match via qualifier
					if (autoWireQualifier == null) {
						// No qualifier, so matches just on type
						return true;

					} else if (autoWireQualifier.equals(qualification
							.getQualifier())) {
						// Qualifier and type matches, so matches
						return true;
					}
				}

				// Recursively determine if responsible
				if (this.isResponsible(object.getObjectDependencies(), objects)) {
					return true; // responsible
				}
			}

			// As here, not responsible
			return false;
		}
	}

}