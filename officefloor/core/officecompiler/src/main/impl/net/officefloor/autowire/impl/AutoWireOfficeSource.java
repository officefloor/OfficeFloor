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
package net.officefloor.autowire.impl;

import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.officefloor.autowire.AutoWire;
import net.officefloor.autowire.AutoWireGovernance;
import net.officefloor.autowire.AutoWireSection;
import net.officefloor.autowire.AutoWireSectionFactory;
import net.officefloor.autowire.AutoWireSectionTransformer;
import net.officefloor.autowire.AutoWireSectionTransformerContext;
import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.governance.GovernanceType;
import net.officefloor.compile.impl.util.DoubleKeyMap;
import net.officefloor.compile.internal.structure.SectionObjectNode;
import net.officefloor.compile.object.DependentObjectType;
import net.officefloor.compile.object.ObjectDependencyType;
import net.officefloor.compile.properties.Property;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.section.OfficeFunctionType;
import net.officefloor.compile.section.OfficeSectionInputType;
import net.officefloor.compile.section.OfficeSectionManagedObjectType;
import net.officefloor.compile.section.OfficeSectionObjectType;
import net.officefloor.compile.section.OfficeSectionOutputType;
import net.officefloor.compile.section.OfficeSectionType;
import net.officefloor.compile.section.OfficeSubSectionType;
import net.officefloor.compile.section.TypeQualification;
import net.officefloor.compile.spi.governance.source.GovernanceSource;
import net.officefloor.compile.spi.office.DependentManagedObject;
import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.OfficeEscalation;
import net.officefloor.compile.spi.office.OfficeGovernance;
import net.officefloor.compile.spi.office.OfficeObject;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.office.OfficeSectionFunction;
import net.officefloor.compile.spi.office.OfficeSectionInput;
import net.officefloor.compile.spi.office.OfficeSectionManagedObject;
import net.officefloor.compile.spi.office.OfficeSectionObject;
import net.officefloor.compile.spi.office.OfficeSectionOutput;
import net.officefloor.compile.spi.office.OfficeStart;
import net.officefloor.compile.spi.office.OfficeSubSection;
import net.officefloor.compile.spi.office.OfficeTeam;
import net.officefloor.compile.spi.office.source.OfficeSource;
import net.officefloor.compile.spi.office.source.OfficeSourceContext;
import net.officefloor.compile.spi.office.source.impl.AbstractOfficeSource;
import net.officefloor.compile.spi.section.SectionInput;
import net.officefloor.compile.spi.section.SectionOutput;
import net.officefloor.compile.spi.section.source.SectionSource;
import net.officefloor.frame.api.escalate.Escalation;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.governance.Governance;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSourceMetaData;
import net.officefloor.frame.api.team.Team;
import net.officefloor.plugin.section.managedfunction.ManagedFunctionSectionSource;

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
	 * {@link AutoWireSectionTransformer} instances.
	 */
	private final List<AutoWireSectionTransformer> sectionTransformers = new LinkedList<AutoWireSectionTransformer>();

	/**
	 * {@link Link} instances.
	 */
	private final List<Link> links = new LinkedList<Link>();

	/**
	 * {@link EscalationLink} instances.
	 */
	private final List<EscalationLink> escalations = new LinkedList<EscalationLink>();

	/**
	 * {@link StartupLink} instances.
	 */
	private final List<StartupLink> startups = new LinkedList<StartupLink>();

	/**
	 * Available {@link OfficeObject} {@link AutoWire} instances.
	 */
	private final List<AutoWire> availableObjectAutoWiring = new LinkedList<AutoWire>();

	/**
	 * Extension interface to their {@link AutoWire} instances.
	 */
	private final Map<Class<?>, List<AutoWire>> extensionInterfaceToAutoWiring = new HashMap<Class<?>, List<AutoWire>>();

	/**
	 * {@link AutoWireGovernance} instances.
	 */
	private final List<AutoWireGovernance> governances = new LinkedList<AutoWireGovernance>();

	/**
	 * Available {@link OfficeTeam} {@link AutoWire} instances.
	 */
	private final List<AutoWire> availableTeamAutoWiring = new LinkedList<AutoWire>();

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
	 * @param <S>
	 *            {@link SectionSource} type.
	 * @param <A>
	 *            {@link AutoWireSection} type.
	 * @param sectionName
	 *            Name of the {@link OfficeSection}.
	 * @param sectionSourceClassName
	 *            {@link SectionSource} class name.
	 * @param sectionLocation
	 *            {@link OfficeSection} location.
	 * @param sectionFactory
	 *            {@link AutoWireSectionFactory}.
	 * @return {@link AutoWireSection} to configure properties and link flows.
	 */
	@SuppressWarnings("unchecked")
	public <S extends SectionSource, A extends AutoWireSection> A addSection(String sectionName,
			String sectionSourceClassName, String sectionLocation, AutoWireSectionFactory<A> sectionFactory) {

		// Create the section
		AutoWireSection section = this.createSection(sectionName, sectionSourceClassName, sectionLocation);

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
	 * @param <S>
	 *            {@link SectionSource} type.
	 * @param sectionName
	 *            Name of the {@link OfficeSection}.
	 * @param sectionSourceClassName
	 *            {@link SectionSource} class name.
	 * @param sectionLocation
	 *            {@link OfficeSection} location.
	 * @return {@link AutoWireSection} to configure properties and link flows.
	 */
	public <S extends SectionSource> AutoWireSection addSection(String sectionName, String sectionSourceClassName,
			String sectionLocation) {
		return this.addSection(sectionName, sectionSourceClassName, sectionLocation, null);
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
	 * Adds a {@link AutoWireSectionTransformer} to enable transforming all
	 * {@link AutoWireSection} instances before configuring them for compiling.
	 * 
	 * @param transformer
	 *            {@link AutoWireSectionTransformer}.
	 */
	public void addSectionTransformer(AutoWireSectionTransformer transformer) {
		this.sectionTransformers.add(transformer);
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
	public void link(AutoWireSection sourceSection, String sourceOutputName, AutoWireSection targetSection,
			String targetInputName) {
		this.links.add(new Link(sourceSection, sourceOutputName, targetSection, targetInputName));
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
			if ((link.sourceSection.getSectionName().equals(section.getSectionName()))
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
	public void linkEscalation(Class<? extends Throwable> escalationType, AutoWireSection section,
			String sectionInputName) {
		this.escalations.add(new EscalationLink(escalationType, section, sectionInputName));
	}

	/**
	 * Adds a flow to be triggered on start-up.
	 * 
	 * @param section
	 *            {@link AutoWireSection}.
	 * @param inputName
	 *            Name of the {@link AutoWireSection} input to trigger on
	 *            start-up.
	 */
	public void addStartupFlow(AutoWireSection section, String inputName) {
		this.startups.add(new StartupLink(section, inputName));
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
	 * @see ManagedObjectSourceMetaData#getExtensionInterfacesMetaData()
	 */
	public AutoWireGovernance addGovernance(String governanceName, String governanceSourceClassName) {

		// Create the properties
		PropertyList properties = this.compiler.createPropertyList();

		// Create the auto-wire governance
		AutoWireGovernance governance = new AutoWireGovernanceImpl(governanceName, governanceSourceClassName,
				this.compiler, properties);

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
	 * This aids both auto-wiring use and {@link Governance} for the
	 * {@link OfficeObject}.
	 * 
	 * @param autoWire
	 *            {@link OfficeObject} {@link AutoWire}.
	 * @param extensionInterfaces
	 *            Extension interfaces available for the {@link OfficeObject}.
	 */
	public void addAvailableOfficeObject(AutoWire autoWire, Class<?>... extensionInterfaces) {

		// Add the available Object auto-wiring
		this.availableObjectAutoWiring.add(autoWire);

		// Map auto-wiring for extensions
		for (Class<?> extensionInterface : extensionInterfaces) {

			// Obtain the auto wiring for the extension type
			List<AutoWire> autoWiring = this.extensionInterfaceToAutoWiring.get(extensionInterface);
			if (autoWiring == null) {
				autoWiring = new LinkedList<AutoWire>();
				this.extensionInterfaceToAutoWiring.put(extensionInterface, autoWiring);
			}

			// Add the auto wire
			autoWiring.add(autoWire);
		}
	}

	/**
	 * Adds an available {@link OfficeTeam} for executing
	 * {@link ManagedFunction} instances that has an object dependency of the
	 * input type.
	 * 
	 * @param autoWire
	 *            Object dependency {@link AutoWire} for the
	 *            {@link ManagedFunction}.
	 */
	public void addAvailableOfficeTeam(AutoWire autoWire) {
		this.availableTeamAutoWiring.add(autoWire);
	}

	/**
	 * Creates the {@link AutoWireSection}.
	 * 
	 * @param sectionName
	 *            Name of the {@link AutoWireSection}.
	 * @param sectionSourceClassName
	 *            {@link SectionSource} {@link Class} name.
	 * @param sectionLocation
	 *            {@link SectionSource} location.
	 * @return Created {@link AutoWireSection}.
	 */
	private AutoWireSection createSection(String sectionName, String sectionSourceClassName, String sectionLocation) {

		// Create the properties
		PropertyList properties = this.compiler.createPropertyList();

		// Create section
		AutoWireSection section = new AutoWireSectionImpl(this.compiler, sectionName, sectionSourceClassName,
				sectionLocation, properties);

		// Return the section
		return section;
	}

	/*
	 * ===================== OfficeSource =========================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		// No specification
	}

	@Override
	public void sourceOffice(OfficeArchitect architect, OfficeSourceContext context) throws Exception {

		// Add the available teams
		List<ResponsibleTeam> responsibleTeams = new ArrayList<ResponsibleTeam>(this.availableTeamAutoWiring.size());
		for (AutoWire availableTeamAutoWire : this.availableTeamAutoWiring) {
			responsibleTeams.add(new ResponsibleTeam(availableTeamAutoWire));
		}

		// Create the default team responsibility
		ResponsibleTeam defaultTeam = new ResponsibleTeam(new AutoWire("team"));

		// Load the sections
		Map<String, OfficeSection> officeSectionsByName = new HashMap<String, OfficeSection>();
		Map<String, OfficeSectionType> officeSectionTypesByName = new HashMap<String, OfficeSectionType>();
		Map<String, Map<String, OfficeSectionInput>> inputs = new HashMap<String, Map<String, OfficeSectionInput>>();
		Map<AutoWire, OfficeObject> objects = new HashMap<AutoWire, OfficeObject>();
		for (AutoWireSection section : this.sections) {

			// Apply all AutoWireSectionTransformers
			for (AutoWireSectionTransformer transformer : this.sectionTransformers) {
				final AutoWireSection currentSection = section;
				section = transformer.transformAutoWireSection(new AutoWireSectionTransformerContext() {

					@Override
					public AutoWireSection getSection() {
						return currentSection;
					}

					@Override
					public AutoWireSection createSection(String sectionName, String sectionSourceClassName,
							String sectionLocation) {
						return AutoWireOfficeSource.this.createSection(sectionName, sectionSourceClassName,
								sectionLocation);
					}
				});
			}

			// Obtain the details for the section
			String sectionName = section.getSectionName();
			String sectionSourceClassName = section.getSectionSourceClassName();
			String sectionLocation = section.getSectionLocation();
			PropertyList sectionProperties = section.getProperties();

			// Add the section
			OfficeSection officeSection = architect.addOfficeSection(sectionName, sectionSourceClassName,
					sectionLocation);
			sectionProperties.configureProperties(officeSection);

			// Register the section
			officeSectionsByName.put(sectionName, officeSection);

			// Obtain the section type
			OfficeSectionType officeSectionType = context.loadOfficeSectionType(sectionName, sectionSourceClassName,
					sectionLocation, sectionProperties);

			// Register the section type
			officeSectionTypesByName.put(sectionName, officeSectionType);

			// Link the objects
			for (OfficeSectionObjectType objectType : officeSectionType.getOfficeSectionObjectTypes()) {

				// Obtain object details
				String objectTypeName = objectType.getObjectType();
				String typeQualifier = objectType.getTypeQualifier();
				String objectName = objectType.getOfficeSectionObjectName();

				// Obtain appropriate auto-wire
				AutoWire autoWire = AutoWireOfficeFloorSource.getAppropriateAutoWire(typeQualifier, objectTypeName,
						this.availableObjectAutoWiring);
				if (autoWire == null) {
					architect.addIssue("No available auto-wiring for " + SectionObjectNode.TYPE + " " + objectName
							+ " (qualifier=" + typeQualifier + ", type=" + objectTypeName + ") from "
							+ OfficeSection.class.getSimpleName() + " " + sectionName);
					continue; // no available auto-wiring for section object
				}

				// Obtain the section object
				OfficeSectionObject sectionObject = officeSection.getOfficeSectionObject(objectName);

				// Link to appropriate Office Object
				OfficeObject officeObject = objects.get(autoWire);
				if (officeObject == null) {

					// Create the office object
					officeObject = architect.addOfficeObject(autoWire.getQualifiedType(), autoWire.getType());
					String autoWireQualifier = autoWire.getQualifier();
					if (autoWireQualifier != null) {
						officeObject.setTypeQualifier(autoWireQualifier);
					}

					// Register the office object
					objects.put(autoWire, officeObject);
				}
				architect.link(sectionObject, officeObject);
			}

			// Register the inputs
			Map<String, OfficeSectionInput> sectionInputs = new HashMap<String, OfficeSectionInput>();
			inputs.put(sectionName, sectionInputs);
			for (OfficeSectionInputType inputType : officeSectionType.getOfficeSectionInputTypes()) {

				// Obtain the section input
				String inputName = inputType.getOfficeSectionInputName();
				OfficeSectionInput input = officeSection.getOfficeSectionInput(inputName);

				// Register the input
				sectionInputs.put(inputName, input);
			}

			// Link section tasks to team.
			// Must be after objects to ensure linked.
			for (OfficeFunctionType functionType : officeSectionType.getOfficeFunctionTypes()) {

				// Obtain the function
				String functionName = functionType.getOfficeFunctionName();
				OfficeSectionFunction function = officeSection.getOfficeSectionFunction(functionName);

				// Assign the team
				this.assignTeam(function, functionType, responsibleTeams, defaultTeam, architect);
			}

			// Link sub section tasks to team
			for (OfficeSubSectionType subSectionType : officeSectionType.getOfficeSubSectionTypes()) {

				// Obtain the sub section
				String subSectionName = subSectionType.getOfficeSectionName();
				OfficeSubSection subSection = officeSection.getOfficeSubSection(subSectionName);

				// Link sub section tasks to team
				this.linkTasksToTeams(subSection, subSectionType, responsibleTeams, defaultTeam, architect);
			}
		}

		// Create the listing of links by section/output
		DoubleKeyMap<String, String, Link> keyedLinks = new DoubleKeyMap<String, String, Link>();
		for (Link link : this.links) {
			keyedLinks.put(link.sourceSection.getSectionName(), link.sourceOutputName, link);
		}

		// Link outputs to inputs (keeping track of used links)
		Set<Link> usedLinks = new HashSet<Link>();
		for (AutoWireSection section : this.sections) {

			// Obtain the office section and its type
			String sectionName = section.getSectionName();
			OfficeSection officeSection = officeSectionsByName.get(sectionName);
			OfficeSectionType officeSectionType = officeSectionTypesByName.get(sectionName);

			// Link the outputs
			NEXT_OUTPUT: for (OfficeSectionOutputType outputType : officeSectionType.getOfficeSectionOutputTypes()) {
				String outputName = outputType.getOfficeSectionOutputName();

				// Obtain the output name (for issues)
				String logOutputName = sectionName + ":" + outputName;

				// Search inheritance hierarchy for link
				// (also check not cyclic inheritance hierarchy)
				AutoWireSection searchSection = section;
				Link link = null;
				boolean isCyclicInheritance = false;
				Deque<String> inheritanceHierarchy = new LinkedList<String>();
				do {
					// Include current inheritance hierarchy
					String searchSectionName = searchSection.getSectionName();
					if (inheritanceHierarchy.contains(searchSectionName)) {
						// Cyclic inheritance (carry on include current section)
						isCyclicInheritance = true;
					}

					// Include inheritance section and search for link
					inheritanceHierarchy.push(searchSectionName);

					// Obtain link configuration from current section
					link = keyedLinks.get(searchSectionName, outputName);

					// Use super section in next iteration if no link
					searchSection = searchSection.getSuperSection();

				} while ((link == null) && (!isCyclicInheritance) && (searchSection != null));

				// Provide issue if cyclic inheritance hierarchy
				if (isCyclicInheritance) {
					// Cyclic inheritance, so provide issue
					StringBuilder hierarchyLog = new StringBuilder();
					for (Iterator<String> iterator = inheritanceHierarchy.iterator(); iterator.hasNext();) {
						hierarchyLog.append(iterator.next() + " : ");
					}
					architect.addIssue("Cyclic section inheritance hierarchy ( " + hierarchyLog.toString() + "... )");
				}

				// Provide issue if no link
				if (link == null) {
					// No link, issue only if not escalation only
					if (outputType.isEscalationOnly()) {
						continue NEXT_OUTPUT;
					}

					// Issue as require link configuration
					architect.addIssue("Section output '" + logOutputName + "' is not linked");
					continue NEXT_OUTPUT;
				}

				// Obtain the link details
				String targetSectionName = link.targetSection.getSectionName();
				String targetInputName = link.targetInputName;
				String logInputName = targetSectionName + ":" + targetInputName;

				// Indicate link used
				usedLinks.add(link);

				// Obtain the input
				OfficeSectionInput sectionInput = null;
				Map<String, OfficeSectionInput> sectionInputs = inputs.get(targetSectionName);
				if (sectionInputs != null) {
					sectionInput = sectionInputs.get(targetInputName);
				}
				if (sectionInput == null) {
					architect.addIssue("Unknown section input '" + logInputName + "' for linking section output '"
							+ logOutputName + "'");
					continue; // no input so can not link
				}

				// Obtain the section output
				OfficeSectionOutput output = officeSection.getOfficeSectionOutput(outputName);

				// Link the output to the input
				architect.link(output, sectionInput);
			}
		}

		// Check for unknown output link configuration
		for (Link link : this.links) {

			// Ignore if link is used
			if (usedLinks.contains(link)) {
				continue;
			}

			// Link is not used (so must be unknown output)
			String logOutputName = link.sourceSection.getSectionName() + ":" + link.sourceOutputName;
			String logInputName = link.targetSection.getSectionName() + ":" + link.targetInputName;
			architect.addIssue(
					"Unknown section output '" + logOutputName + "' to link to section input '" + logInputName + "'");
		}

		// Only configure if have escalations
		// (Stops auto escalation handling if nothing handling)
		if (this.escalations.size() > 0) {

			// Properties for failed to source managed object handling
			PropertyList failedToSourceProperties = context.createPropertyList();
			failedToSourceProperties.addProperty(ManagedFunctionSectionSource.PROPERTY_PARAMETER_PREFIX + "Handle").setValue("1");

			// Link escalations to inputs
			OfficeSectionInput[] escalationSectionInput = new OfficeSectionInput[this.escalations.size()];
			for (int i = 0; i < this.escalations.size(); i++) {
				EscalationLink link = this.escalations.get(i);

				// Obtain the link details
				String escalationTypeName = link.escalationType.getName();
				String sectionName = link.targetSection.getSectionName();
				String inputName = link.targetInputName;

				// Add the Escalation
				OfficeEscalation escalation = architect.addOfficeEscalation(escalationTypeName);

				// Obtain the input
				OfficeSectionInput sectionInput = null;
				Map<String, OfficeSectionInput> sectionInputs = inputs.get(sectionName);
				if (sectionInputs != null) {
					sectionInput = sectionInputs.get(inputName);
				}
				if (sectionInput == null) {
					architect.addIssue("Unknown section input '" + sectionName + ":" + inputName
							+ "' for linking escalation '" + escalationTypeName + "'");
					continue; // no input so can not link
				}

				// Link the escalation to section input
				architect.link(escalation, sectionInput);

				// Provide property and section input for failed to source
				failedToSourceProperties.addProperty(
						AutoWireEscalationCauseRouteManagedFunctionSource.PROPERTY_PREFIX_ESCALATION_TYPE + String.valueOf(i))
						.setValue(escalationTypeName);
				escalationSectionInput[i] = sectionInput;
			}
		}

		// Add the governances
		for (AutoWireGovernance governance : this.governances) {

			// Obtain the governance details
			String governanceName = governance.getGovernanceName();
			String governanceSourceClassName = governance.getGovernanceSourceClassName();
			PropertyList properties = governance.getProperties();

			// Add the Governance
			OfficeGovernance officeGovernance = architect.addOfficeGovernance(governance.getGovernanceName(),
					governanceSourceClassName);
			for (Property property : properties) {
				officeGovernance.addProperty(property.getName(), property.getValue());
			}

			// Obtain the extension interface for the governance
			GovernanceType<?, ?> governanceType = context.loadGovernanceType(governanceSourceClassName, properties);
			if (governanceType == null) {
				continue; // need extension interface from type
			}
			Class<?> extensionInterface = governanceType.getExtensionInterface();

			// Assign team for governance
			this.assignTeam(officeGovernance, extensionInterface, responsibleTeams, defaultTeam, architect);

			// Govern the sections
			for (AutoWireSection section : governance.getGovernedSections()) {

				// Obtain the section
				String sectionName = section.getSectionName();
				OfficeSection officeSection = officeSectionsByName.get(sectionName);
				if (officeSection == null) {
					architect.addIssue(
							"Unknown section '" + sectionName + "' to be governed by governance " + governanceName);
					continue; // can not govern unknown section
				}
				OfficeSectionType sectionType = officeSectionTypesByName.get(sectionName);

				// Govern the section
				officeSection.addGovernance(officeGovernance);

				// Govern the managed objects of the section
				this.governSectionManagedObjects(officeSection, sectionType, extensionInterface, officeGovernance);
			}

			// Govern the Office Objects (via auto-wiring extensions)
			List<AutoWire> autoWiring = this.extensionInterfaceToAutoWiring.get(extensionInterface);
			if (autoWiring != null) {

				// Govern the office objects
				for (AutoWire autoWire : autoWiring) {

					// Obtain the office object
					OfficeObject officeObject = objects.get(autoWire);
					if (officeObject == null) {
						continue; // office object not used
					}

					// Govern the office object
					officeGovernance.governManagedObject(officeObject);
				}
			}
		}

		// Link the start-up triggers
		for (StartupLink startup : this.startups) {

			// Create the start-up name
			String startupSection = startup.targetSection.getSectionName();
			String startupInput = startup.targetInputName;
			String startName = startupSection + "-" + startupInput;

			// Create the start-up
			OfficeStart start = architect.addOfficeStart(startName);

			// Obtain the section input
			OfficeSectionInput input = null;
			Map<String, OfficeSectionInput> sectionInputs = inputs.get(startupSection);
			if (sectionInputs != null) {
				input = sectionInputs.get(startupInput);
			}
			if (input == null) {
				architect.addIssue("Unknown section '" + startupSection + "' input '" + startupInput
						+ "' to be triggered on start-up");
				continue; // can not trigger flow on start-up
			}

			// Link the flow to be triggered on start-up
			architect.link(start, input);
		}
	}

	/**
	 * Provides {@link Governance} to the {@link OfficeSectionManagedObject}
	 * instances that support the extension interface.
	 * 
	 * @param section
	 *            {@link OfficeSubSection}.
	 * @param sectionType
	 *            {@link OfficeSubSectionType} to check for
	 *            {@link OfficeSectionManagedObject} instances for
	 *            {@link Governance}.
	 * @param extensionInterface
	 *            Extension interface of the {@link Governance}.
	 * @param governance
	 *            {@link OfficeGovernance}.
	 */
	private void governSectionManagedObjects(OfficeSubSection section, OfficeSubSectionType sectionType,
			Class<?> extensionInterface, OfficeGovernance governance) {

		// Check all section managed objects
		for (OfficeSectionManagedObjectType moType : sectionType.getOfficeSectionManagedObjectTypes()) {
			for (Class<?> supportedExtensionInterface : moType.getSupportedExtensionInterfaces()) {
				if (extensionInterface.equals(supportedExtensionInterface)) {

					// Obtain the managed object
					String moName = moType.getOfficeSectionManagedObjectName();
					OfficeSectionManagedObject mo = section.getOfficeSectionManagedObject(moName);

					// Supports extension so govern
					governance.governManagedObject(mo);
				}
			}
		}

		// Recursively govern the sub sections
		for (OfficeSubSectionType subSectionType : sectionType.getOfficeSubSectionTypes()) {

			// Obtain the sub section
			String subSectionName = subSectionType.getOfficeSectionName();
			OfficeSubSection subSection = section.getOfficeSubSection(subSectionName);

			// Govern the sub section
			this.governSectionManagedObjects(subSection, subSectionType, extensionInterface, governance);
		}
	}

	/**
	 * Links the {@link OfficeSectionFunction} instances of the
	 * {@link OfficeSubSection} to the {@link OfficeTeam}.
	 * 
	 * @param subSection
	 *            {@link OfficeSubSection}.
	 * @param subSectionType
	 *            {@link OfficeSubSectionType}.
	 * @param responsibleTeams
	 *            {@link ResponsibleTeam} instances.
	 * @param defaultTeam
	 *            {@link ResponsibleTeam}.
	 * @param architect
	 *            {@link OfficeArchitect}.
	 */
	private void linkTasksToTeams(OfficeSubSection subSection, OfficeSubSectionType subSectionType,
			List<ResponsibleTeam> responsibleTeams, ResponsibleTeam defaultTeam, OfficeArchitect architect) {

		// Link section tasks to team
		for (OfficeFunctionType functionType : subSectionType.getOfficeFunctionTypes()) {

			// Obtain the function
			String functionName = functionType.getOfficeFunctionName();
			OfficeSectionFunction function = subSection.getOfficeSectionFunction(functionName);

			// Assign the team
			this.assignTeam(function, functionType, responsibleTeams, defaultTeam, architect);
		}

		// Recursively link the sub sections
		for (OfficeSubSectionType subSubSectionType : subSectionType.getOfficeSubSectionTypes()) {

			// Obtain the sub section
			String subSubSectionName = subSubSectionType.getOfficeSectionName();
			OfficeSubSection subSubSection = subSection.getOfficeSubSection(subSubSectionName);

			// Link the tasks
			this.linkTasksToTeams(subSubSection, subSubSectionType, responsibleTeams, defaultTeam, architect);
		}
	}

	/**
	 * Assigns the {@link OfficeTeam} for the {@link OfficeSectionFunction}.
	 * 
	 * @param function
	 *            {@link OfficeSectionFunction}.
	 * @param functionType
	 *            {@link OfficeFunctionType}.
	 * @param responsibleTeams
	 *            {@link ResponsibleTeam} instances.
	 * @param defaultTeam
	 *            Default {@link ResponsibleTeam}.
	 * @param architect
	 *            {@link OfficeArchitect}.
	 */
	private void assignTeam(OfficeSectionFunction function, OfficeFunctionType functionType,
			List<ResponsibleTeam> responsibleTeams, ResponsibleTeam defaultTeam, OfficeArchitect architect) {

		// Determine if team to be responsible
		for (ResponsibleTeam responsibleTeam : responsibleTeams) {
			if (responsibleTeam.isResponsible(functionType)) {

				// Obtain the OfficeTeam responsible
				OfficeTeam team = responsibleTeam.getOfficeTeam(architect);

				// Team responsible for the task, so link
				architect.link(function.getResponsibleTeam(), team);

				// Team assigned
				return;
			}
		}

		// As here, default team is responsible
		OfficeTeam team = defaultTeam.getOfficeTeam(architect);
		architect.link(function.getResponsibleTeam(), team);
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
	 *            Default {@link ResponsibleTeam}.
	 * @param architect
	 *            {@link OfficeArchitect}.
	 */
	private void assignTeam(OfficeGovernance governance, Class<?> extensionInterface,
			List<ResponsibleTeam> responsibleTeams, ResponsibleTeam defaultTeam, OfficeArchitect architect) {

		// Determine if team to be responsible
		for (ResponsibleTeam responsibleTeam : responsibleTeams) {
			if (responsibleTeam.isResponsible(extensionInterface)) {

				// Obtain the OfficeTeam responsible
				OfficeTeam team = responsibleTeam.getOfficeTeam(architect);

				// Team responsible for the governance, so link
				architect.link(governance, team);

				// Team assigned
				return;
			}
		}

		// As here, default team is responsible
		OfficeTeam team = defaultTeam.getOfficeTeam(architect);
		architect.link(governance, team);
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
		public Link(AutoWireSection sourceSection, String sourceOutputName, AutoWireSection targetSection,
				String targetInputName) {
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
		public EscalationLink(Class<? extends Throwable> escalationType, AutoWireSection targetSection,
				String targetInputName) {
			this.escalationType = escalationType;
			this.targetSection = targetSection;
			this.targetInputName = targetInputName;
		}
	}

	/**
	 * Link for start-up trigger.
	 */
	private static class StartupLink {

		/**
		 * {@link AutoWireSection}.
		 */
		public final AutoWireSection targetSection;

		/**
		 * Target {@link SectionInput} name.
		 */
		public final String targetInputName;

		/**
		 * Initiate.
		 * 
		 * @param targetSection
		 *            Target {@link AutoWireSection}.
		 * @param targetInputName
		 *            Target {@link SectionInput} name.
		 */
		public StartupLink(AutoWireSection targetSection, String targetInputName) {
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
		 * {@link OfficeTeam} (loaded only if used).
		 */
		private OfficeTeam officeTeam = null;

		/**
		 * Initiate.
		 * 
		 * @param dependencyAutoWire
		 *            Dependency {@link AutoWire}.
		 */
		public ResponsibleTeam(AutoWire dependencyAutoWire) {
			this.dependencyAutoWire = dependencyAutoWire;
		}

		/**
		 * Obtains the {@link OfficeTeam} for this responsibility.
		 * 
		 * @param architect
		 *            {@link OfficeArchitect}.
		 * @return {@link OfficeTeam}.
		 */
		public OfficeTeam getOfficeTeam(OfficeArchitect architect) {

			// Lazy create the OfficeTeam
			if (this.officeTeam == null) {
				this.officeTeam = architect.addOfficeTeam(this.dependencyAutoWire.getQualifiedType());
			}

			// Return the OfficeTeam
			return this.officeTeam;
		}

		/**
		 * Determines if the {@link OfficeTeam} is responsible for the
		 * {@link OfficeSectionFunction}.
		 * 
		 * @param taskType
		 *            {@link OfficeFunctionType} to check if responsible.
		 * @return <code>true</code> if the {@link OfficeTeam} is potentially
		 *         responsible for the {@link OfficeSectionFunction}.
		 */
		public boolean isResponsible(OfficeFunctionType taskType) {
			return this.isResponsible(taskType.getObjectDependencies(), new HashSet<DependentObjectType>());
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
			return (this.dependencyAutoWire.getType().equals(extensionInterface.getName()));
		}

		/**
		 * Determines if {@link OfficeTeam} is responsible for the
		 * {@link ObjectDependencyType} instances.
		 * 
		 * @param dependencies
		 *            {@link ObjectDependencyType} instances.
		 * @param objects
		 *            Set of {@link DependentManagedObject} instances.
		 * @return <code>true</code> if the {@link OfficeTeam} is potentially
		 *         responsible for the {@link ObjectDependencyType} instances.
		 */
		private boolean isResponsible(ObjectDependencyType[] dependencies, Set<DependentObjectType> objects) {

			// Obtain details of auto-wire
			String autoWireQualifier = this.dependencyAutoWire.getQualifier();
			String autoWireType = this.dependencyAutoWire.getType();

			// Determine if responsible for dependent objects
			for (ObjectDependencyType dependencyType : dependencies) {

				// Obtain the dependent object
				DependentObjectType objectType = dependencyType.getDependentObjectType();
				if (objectType == null) {
					continue; // ignore if dependency not linked
				}

				// Determine if already processed dependent object
				if (objects.contains(objectType)) {
					continue; // ignore as already processed object
				}
				objects.add(objectType);

				// Determine if responsible
				for (TypeQualification qualification : objectType.getTypeQualifications()) {

					// Must match on type
					if (!(autoWireType.equals(qualification.getType()))) {
						continue; // ignore as not match on type
					}

					// Determine match via qualifier
					if (autoWireQualifier == null) {
						// No qualifier, so matches just on type
						return true;

					} else if (autoWireQualifier.equals(qualification.getQualifier())) {
						// Qualifier and type matches, so matches
						return true;
					}
				}

				// Recursively determine if responsible
				if (this.isResponsible(objectType.getObjectDependencies(), objects)) {
					return true; // responsible
				}
			}

			// As here, not responsible
			return false;
		}
	}

}