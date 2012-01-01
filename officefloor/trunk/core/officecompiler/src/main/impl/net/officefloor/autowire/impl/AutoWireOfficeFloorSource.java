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
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.officefloor.autowire.AutoWire;
import net.officefloor.autowire.AutoWireApplication;
import net.officefloor.autowire.AutoWireGovernance;
import net.officefloor.autowire.AutoWireManagement;
import net.officefloor.autowire.AutoWireObject;
import net.officefloor.autowire.AutoWireOfficeFloor;
import net.officefloor.autowire.AutoWireResponsibility;
import net.officefloor.autowire.AutoWireSection;
import net.officefloor.autowire.AutoWireSectionFactory;
import net.officefloor.autowire.AutoWireSupplier;
import net.officefloor.autowire.AutoWireTeam;
import net.officefloor.autowire.ManagedObjectSourceWirer;
import net.officefloor.autowire.ManagedObjectSourceWirerContext;
import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.impl.OfficeFloorCompilerAdapter;
import net.officefloor.compile.impl.issues.FailCompilerIssues;
import net.officefloor.compile.managedobject.ManagedObjectDependencyType;
import net.officefloor.compile.managedobject.ManagedObjectType;
import net.officefloor.compile.office.OfficeInputType;
import net.officefloor.compile.office.OfficeManagedObjectType;
import net.officefloor.compile.office.OfficeType;
import net.officefloor.compile.properties.Property;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.office.ManagedObjectTeam;
import net.officefloor.compile.spi.office.OfficeObject;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.office.OfficeSectionInput;
import net.officefloor.compile.spi.office.OfficeTeam;
import net.officefloor.compile.spi.office.source.OfficeSource;
import net.officefloor.compile.spi.officefloor.DeployedOffice;
import net.officefloor.compile.spi.officefloor.DeployedOfficeInput;
import net.officefloor.compile.spi.officefloor.OfficeFloorDeployer;
import net.officefloor.compile.spi.officefloor.OfficeFloorInputManagedObject;
import net.officefloor.compile.spi.officefloor.OfficeFloorManagedObject;
import net.officefloor.compile.spi.officefloor.OfficeFloorManagedObjectSource;
import net.officefloor.compile.spi.officefloor.OfficeFloorTeam;
import net.officefloor.compile.spi.officefloor.source.OfficeFloorSource;
import net.officefloor.compile.spi.officefloor.source.OfficeFloorSourceContext;
import net.officefloor.compile.spi.officefloor.source.RequiredProperties;
import net.officefloor.compile.spi.officefloor.source.impl.AbstractOfficeFloorSource;
import net.officefloor.compile.spi.section.ManagedObjectDependency;
import net.officefloor.compile.spi.section.ManagedObjectFlow;
import net.officefloor.frame.api.build.OfficeFloorIssues.AssetType;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.impl.spi.team.ProcessContextTeamSource;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.spi.team.Team;
import net.officefloor.frame.spi.team.source.TeamSource;
import net.officefloor.model.impl.officefloor.OfficeFloorModelOfficeFloorSource;

/**
 * <p>
 * {@link OfficeFloorSource} implementation that auto-wires the configuration
 * based on type.
 * <p>
 * To allow auto-wiring this implementation provides facades over the
 * complexities of more advanced {@link OfficeFloor} configuration to allow
 * simpler configuration. It is anticipated in majority of cases that this will
 * be adequate for most applications. Should however more flexibility be
 * required then please use the {@link OfficeFloorModelOfficeFloorSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class AutoWireOfficeFloorSource extends AbstractOfficeFloorSource
		implements AutoWireApplication {

	/**
	 * Obtains the appropriate available {@link AutoWire}.
	 * 
	 * @param qualifier
	 *            Qualifier. May be <code>null</code>.
	 * @param type
	 *            Type.
	 * @param availableAutoWiring
	 *            Available {@link AutoWire} instances to select from.
	 * @return Appropriate available {@link AutoWire} or <code>null</code> if no
	 *         appropriate matching {@link AutoWire}.
	 */
	public static AutoWire getAppropriateAutoWire(String qualifier,
			String type, Collection<AutoWire> availableAutoWiring) {

		// Create a copy of the auto-wiring (allow sorting without side effect)
		List<AutoWire> orderedAutoWiring = new ArrayList<AutoWire>(
				availableAutoWiring);

		// Sort the available auto-wiring (so qualifiers first)
		Collections.sort(orderedAutoWiring, new Comparator<AutoWire>() {
			@Override
			public int compare(AutoWire a, AutoWire b) {
				// Sort by type first
				int match = String.CASE_INSENSITIVE_ORDER.compare(a.getType(),
						b.getType());
				if (match != 0) {
					// Not matching type, so return compare ordering
					return match;
				}

				// Matching types, so sort by qualifier (with default last)
				String aQualifier = a.getQualifier();
				String bQualifier = b.getQualifier();
				if ((aQualifier == null) && (bQualifier == null)) {
					// Duplicate default types, so match
					return 0;
				} else if ((aQualifier != null) && (bQualifier != null)) {
					// Same type, so compare on qualifiers
					return String.CASE_INSENSITIVE_ORDER.compare(aQualifier,
							bQualifier);
				} else {
					// Sort so default (null) qualifier is last
					return (aQualifier == null ? 1 : -1);
				}
			}
		});

		// Iterate over to find first matching (as sorted)
		for (AutoWire autoWire : orderedAutoWiring) {

			// Ensure matches on type
			if (!(type.equals(autoWire.getType()))) {
				continue; // ignore as must match on type
			}

			// Determine if unqualified auto-wire
			String autoWireQualifier = autoWire.getQualifier();
			if (autoWireQualifier == null) {
				// Use unqualified auto-wire (as not qualified)
				return autoWire;
			}

			// May use if qualifiers match
			if (autoWireQualifier.equals(qualifier)) {
				// Use the matching qualified auto-wire
				return autoWire;
			}
		}

		// As here, no matching auto-wire
		return null;
	}

	/**
	 * Name of the single {@link Office}.
	 */
	private static final String OFFICE_NAME = "OFFICE";

	/**
	 * {@link OfficeFloorCompiler}.
	 */
	private final OfficeFloorCompiler compiler;

	/**
	 * {@link OfficeSource} for auto-wiring.
	 */
	private final AutoWireOfficeSource officeSource;

	/**
	 * {@link AutoWireObjectInstance} instances for the {@link AutoWireObject}
	 * instances.
	 */
	private final List<AutoWireObjectInstance> objectInstances = new LinkedList<AutoWireObjectInstance>();

	/**
	 * Default {@link AutoWireTeam}.
	 */
	private AutoWireTeam defaultTeam = null;

	/**
	 * {@link AutoWireTeam} instances.
	 */
	private final List<AutoWireTeam> teams = new LinkedList<AutoWireTeam>();

	/**
	 * Initiate.
	 * 
	 * @param compiler
	 *            {@link OfficeFloorCompiler}.
	 */
	public AutoWireOfficeFloorSource(OfficeFloorCompiler compiler) {
		this.compiler = compiler;
		this.compiler.setOfficeFloorSource(this);
		this.officeSource = new AutoWireOfficeSource(this.compiler);
	}

	/**
	 * Default constructor.
	 */
	public AutoWireOfficeFloorSource() {
		this(OfficeFloorCompiler.newOfficeFloorCompiler(null));
	}

	/**
	 * Allows overriding for initialising the {@link OfficeFloor} before
	 * auto-wiring.
	 * 
	 * @param deployer
	 *            {@link OfficeFloorDeployer}.
	 * @param context
	 *            {@link OfficeFloorSourceContext}.
	 * @throws Exception
	 *             If fails to initialise.
	 */
	protected void initOfficeFloor(OfficeFloorDeployer deployer,
			OfficeFloorSourceContext context) throws Exception {
		// By default do nothing
	}

	/*
	 * ====================== AutoWireApplication ===========================
	 */

	@Override
	public OfficeFloorCompiler getOfficeFloorCompiler() {
		return this.compiler;
	}

	@Override
	public AutoWireSection addSection(String sectionName,
			String sectionSourceClassName, String sectionLocation) {
		return this.officeSource.addSection(sectionName,
				sectionSourceClassName, sectionLocation);
	}

	@Override
	public <A extends AutoWireSection> A addSection(String sectionName,
			String sectionSourceClassName, String sectionLocation,
			AutoWireSectionFactory<A> sectionFactory) {
		return this.officeSource.addSection(sectionName,
				sectionSourceClassName, sectionLocation, sectionFactory);
	}

	@Override
	public AutoWireSection getSection(String sectionName) {
		return this.officeSource.getSection(sectionName);
	}

	@Override
	public void link(AutoWireSection sourceSection, String sourceOutputName,
			AutoWireSection targetSection, String targetInputName) {
		this.officeSource.link(sourceSection, sourceOutputName, targetSection,
				targetInputName);
	}

	@Override
	public boolean isLinked(AutoWireSection section, String sectionOutputName) {
		return this.officeSource.isLinked(section, sectionOutputName);
	}

	@Override
	public void linkEscalation(Class<? extends Throwable> escalation,
			AutoWireSection section, String inputName) {
		this.officeSource.linkEscalation(escalation, section, inputName);
	}

	@Override
	public void addObject(Object object, AutoWire... autoWiring) {

		// Default the object type if not provided
		if ((autoWiring == null) || (autoWiring.length == 0)) {
			autoWiring = new AutoWire[] { new AutoWire(object.getClass()) };
		}

		// Create the properties
		PropertyList properties = this.compiler.createPropertyList();

		// Register the raw object
		this.objectInstances.add(new AutoWireObjectInstance(
				new AutoWireObjectImpl(this.compiler, null, properties, null,
						autoWiring), object, this.compiler));
	}

	@Override
	public AutoWireObject addManagedObject(String managedObjectSourceClassName,
			ManagedObjectSourceWirer wirer, AutoWire... autoWiring) {

		// Ensure have auto-wiring
		if ((autoWiring == null) || (autoWiring.length == 0)) {
			throw new IllegalArgumentException("Must provide at least one "
					+ AutoWire.class.getSimpleName());
		}

		// Create the properties
		PropertyList properties = this.compiler.createPropertyList();

		// Create the auto wire object
		AutoWireObject object = new AutoWireObjectImpl(this.compiler,
				managedObjectSourceClassName, properties, wirer, autoWiring);

		// Register the managed object
		this.objectInstances.add(new AutoWireObjectInstance(object, null,
				this.compiler));

		// Return the auto wire object
		return object;
	}

	@Override
	public AutoWireSupplier addSupplier(String supplierSourceClassName) {
		// TODO implement AutoWireApplication.addSupplier
		throw new UnsupportedOperationException(
				"TODO implement AutoWireApplication.addSupplier");
	}

	@Override
	public boolean isObjectAvailable(AutoWire autoWire) {

		// Determine if the auto wire is available
		for (AutoWireObjectInstance objectInstance : this.objectInstances) {
			for (AutoWire availableAutoWire : objectInstance.autoWireObject
					.getAutoWiring()) {
				if (autoWire.equals(availableAutoWire)) {
					return true; // auto wire is available
				}
			}
		}

		// TODO should this indicate if supplied object available?

		// As here, auto wire not available
		return false;
	}

	@Override
	public AutoWireGovernance addGovernance(String governanceName,
			String governanceSourceClassName) {
		return this.officeSource.addGovernance(governanceName,
				governanceSourceClassName);
	}

	@Override
	public AutoWireTeam assignTeam(String teamSourceClassName,
			AutoWire... autoWiring) {

		// Must have auto-wiring
		if ((autoWiring == null) || (autoWiring.length == 0)) {
			throw new IllegalArgumentException("Must provide at least one "
					+ AutoWire.class.getSimpleName()
					+ " for team to be responsible");
		}

		// Determine name of team
		String teamName = "team-" + autoWiring[0].getQualifiedType();

		// Create the responsibilities
		AutoWireResponsibility[] responsibilities = new AutoWireResponsibility[autoWiring.length];
		for (int i = 0; i < autoWiring.length; i++) {
			responsibilities[i] = this.officeSource
					.addResponsibility(autoWiring[i]);
		}

		// Create the properties
		PropertyList properties = this.compiler.createPropertyList();

		// Create and add the team
		AutoWireTeam team = new AutoWireTeamImpl(this.compiler, teamName,
				teamSourceClassName, properties, responsibilities);
		this.teams.add(team);

		// Return the team
		return team;
	}

	@Override
	public AutoWireTeam assignDefaultTeam(String teamSourceClassName) {

		// Create the properties
		PropertyList properties = this.compiler.createPropertyList();

		// Create the default team
		this.defaultTeam = new AutoWireTeamImpl(this.compiler, "team",
				teamSourceClassName, properties);

		// Return the default team
		return this.defaultTeam;
	}

	@Override
	public AutoWireOfficeFloor openOfficeFloor() throws Exception {

		// Open the OfficeFloor
		OfficeFloor officeFloor = FailCompilerIssues.compile(this.compiler,
				"auto-wire");
		officeFloor.openOfficeFloor();

		// Create and return the auto-wire OfficeFloor
		return AutoWireManagement.createAutoWireOfficeFloor(officeFloor,
				OFFICE_NAME);
	}

	/*
	 * =================== OfficeFloorSource =========================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		// No specification
	}

	@Override
	public void specifyConfigurationProperties(
			RequiredProperties requiredProperties,
			OfficeFloorSourceContext context) throws Exception {
		// No required properties
	}

	@Override
	public void sourceOfficeFloor(OfficeFloorDeployer deployer,
			OfficeFloorSourceContext context) throws Exception {

		// Allow initialising the OfficeFloor
		this.initOfficeFloor(deployer, context);

		// Add the default team
		OfficeFloorTeam team;
		if (this.defaultTeam != null) {
			// Configure the default team
			team = deployer.addTeam("team",
					this.defaultTeam.getTeamSourceClassName());
			for (Property property : this.defaultTeam.getProperties()) {
				team.addProperty(property.getName(), property.getValue());
			}

		} else {
			/*
			 * Create the default Team by default to always use invoking thread.
			 * This is to allow use within an application server, as use within
			 * other contexts should specify an appropriate default Team if
			 * requiring different default behaviour.
			 */
			team = deployer.addTeam("team",
					ProcessContextTeamSource.class.getName());
		}

		// Load available office objects
		Set<AutoWire> availableAutoWires = new HashSet<AutoWire>();
		for (AutoWireObjectInstance objectInstance : this.objectInstances) {

			// Obtain the available auto-wiring
			AutoWire[] autoWiring = objectInstance.autoWireObject
					.getAutoWiring();

			// Obtain the extension interfaces for object type
			Class<?>[] extensionInterfaces = objectInstance
					.getExtensionInterfaces(context);

			// Load the first available auto-wire for office objects
			for (AutoWire autoWire : autoWiring) {

				// Ensure only load the first auto-wire
				if (availableAutoWires.contains(autoWire)) {
					continue; // already identified available
				}
				availableAutoWires.add(autoWire);

				// Load the auto-wire as an available office object
				this.officeSource.addAvailableOfficeObject(autoWire,
						extensionInterfaces);
			}
		}

		// Load the office type
		OfficeType officeType = context.loadOfficeType(this.officeSource,
				"auto-wire", context.createPropertyList());

		// Load all handled office inputs
		Set<AutoWire> handledInputs = new HashSet<AutoWire>();
		for (OfficeInputType officeInput : officeType.getOfficeInputTypes()) {

			// Obtain the auto-wire for the office input
			String sectionName = officeInput.getOfficeSectionName();
			String inputName = officeInput.getOfficeSectionInputName();
			AutoWire handledInput = new AutoWire(sectionName, inputName);

			// Add the handled input
			handledInputs.add(handledInput);
		}

		// Obtain all used auto-wiring
		List<AutoWire> usedAutoWiring = new LinkedList<AutoWire>();
		for (OfficeManagedObjectType officeObject : officeType
				.getOfficeManagedObjectTypes()) {

			// Obtain the auto-wire for the office object
			String type = officeObject.getObjectType();
			String qualifier = officeObject.getTypeQualifier();
			AutoWire usedAutoWire = new AutoWire(qualifier, type);

			// Add the used auto-wire (uniquely)
			if (!(usedAutoWiring.contains(usedAutoWire))) {
				usedAutoWiring.add(usedAutoWire);
			}
		}

		// Add the auto-wiring office
		DeployedOffice office = deployer.addDeployedOffice(OFFICE_NAME,
				this.officeSource, "auto-wire");

		// Link default team for office
		OfficeTeam officeTeam = office.getDeployedOfficeTeam("team");
		deployer.link(officeTeam, team);

		// Link team via object dependency responsibility
		Map<AutoWire, AutoWireTeamInstance> autoWireTeamInstances = new HashMap<AutoWire, AutoWireTeamInstance>();
		for (AutoWireTeam autoWireTeam : this.teams) {

			/*
			 * TODO derive used Teams in similar way to Objects. In other words,
			 * provide AutoWireOfficeSource.addAvailableResponsibility(AutoWire)
			 * so that required teams can be defined by the OfficeType (using
			 * naming convention to extract the AutoWire from the
			 * OfficeTeamType). Then load only the necessary teams.
			 */

			// Add the responsible team
			OfficeFloorTeam responsibleTeam = deployer.addTeam(
					autoWireTeam.getTeamName(),
					autoWireTeam.getTeamSourceClassName());
			for (Property property : autoWireTeam.getProperties()) {
				responsibleTeam.addProperty(property.getName(),
						property.getValue());
			}

			// Link responsible team to its responsibilities
			for (AutoWireResponsibility autoWireResponsibility : autoWireTeam
					.getResponsibilities()) {

				// Link responsibility to team
				OfficeTeam responsibility = office
						.getDeployedOfficeTeam(autoWireResponsibility
								.getOfficeTeamName());
				deployer.link(responsibility, responsibleTeam);

				// Only load the first registered for auto-wire
				AutoWire dependencyAutoWire = autoWireResponsibility
						.getDependencyAutoWire();
				if (!(autoWireTeamInstances.containsKey(dependencyAutoWire))) {
					autoWireTeamInstances.put(dependencyAutoWire,
							new AutoWireTeamInstance(responsibleTeam));
				}
			}
		}

		// Create the mapping of auto-wiring to realised objects
		Map<AutoWire, AutoWireObjectInstance> autoWireObjectInstances = new HashMap<AutoWire, AutoWireObjectInstance>();
		List<AutoWireObjectInstance> inputObjectInstances = new LinkedList<AutoWireObjectInstance>();
		for (AutoWireObjectInstance objectInstance : this.objectInstances) {

			// Initialise the object
			objectInstance.initManagedObject();

			// Register if input object instance
			if (objectInstance.isInput) {

				// Ensure only one auto-wire for input managed object
				AutoWire[] inputAutoWiring = objectInstance.autoWireObject
						.getAutoWiring();
				if (inputAutoWiring.length != 1) {
					String inputManagedObjectName = (inputAutoWiring.length == 0 ? "UNKNOWN"
							: inputAutoWiring[0].getQualifiedType());
					deployer.addIssue(
							OfficeFloorInputManagedObject.class.getSimpleName()
									+ " " + inputManagedObjectName
									+ " must have only one "
									+ AutoWire.class.getSimpleName(),
							AssetType.MANAGED_OBJECT, inputManagedObjectName);
					continue; // do not include
				}

				// Include input managed object only if all flows handled
				boolean isInclude = true;
				for (AutoWireManagedObjectFlow flow : objectInstance.moFlows) {
					if (!(handledInputs.contains(new AutoWire(flow.sectionName,
							flow.sectionInputName)))) {
						// Flow not handled, so do not include
						isInclude = false;
					}
				}

				// Include Input Managed Object (if handled)
				if (isInclude) {
					// Include Input Managed Object
					inputObjectInstances.add(objectInstance);
				} else {
					// Not include, so do not make available
					continue;
				}
			}

			// Only load the first registered for auto-wire
			for (AutoWire autoWire : objectInstance.autoWireObject
					.getAutoWiring()) {
				if (!(autoWireObjectInstances.containsKey(autoWire))) {
					autoWireObjectInstances.put(autoWire, objectInstance);
				}
			}
		}

		// Create the auto-wire state
		AutoWireState state = new AutoWireState(deployer, context, office,
				autoWireObjectInstances, autoWireTeamInstances);

		// Build the input auto-wire objects
		for (AutoWireObjectInstance inputObjectInstance : inputObjectInstances) {
			inputObjectInstance.buildInputManagedObject(state,
					inputObjectInstance.autoWireObject.getAutoWiring()[0]);
		}

		// Link the input auto-wire objects
		for (AutoWireObjectInstance inputObjectInstance : inputObjectInstances) {
			inputObjectInstance.linkManagedObject(state);
		}

		// Load the required auto-wire objects
		for (AutoWire usedAutoWire : usedAutoWiring) {

			// Obtain the corresponding auto-wire context
			AutoWireObjectInstance autoWireObjectInstance = autoWireObjectInstances
					.get(usedAutoWire);
			if (autoWireObjectInstance == null) {
				deployer.addIssue("No auto-wire object available for "
						+ usedAutoWire.getQualifiedType(),
						AssetType.MANAGED_OBJECT,
						usedAutoWire.getQualifiedType());
				continue; // must have auto-wire context to load managed object
			}

			// Build the used managed object
			autoWireObjectInstance.buildManagedObject(state, usedAutoWire);
		}
	}

	/**
	 * State for auto-wiring.
	 */
	private static class AutoWireState {

		/**
		 * {@link OfficeFloorDeployer}.
		 */
		public final OfficeFloorDeployer deployer;

		/**
		 * {@link OfficeFloorSourceContext}.
		 */
		public final OfficeFloorSourceContext context;

		/**
		 * {@link DeployedOffice}.
		 */
		public final DeployedOffice office;

		/**
		 * {@link AutoWireObjectInstance} by its {@link AutoWire}.
		 */
		private final Map<AutoWire, AutoWireObjectInstance> autoWireObjectInstances;

		/**
		 * Index suffix for {@link AutoWireInstance} naming.
		 */
		private final Map<AutoWire, Integer> typeIndex = new HashMap<AutoWire, Integer>();

		/**
		 * {@link OfficeFloorInputManagedObject} for {@link AutoWire}.
		 */
		private final Map<AutoWire, OfficeFloorInputManagedObject> inputManagedObjects = new HashMap<AutoWire, OfficeFloorInputManagedObject>();

		/**
		 * {@link OfficeFloorTeam} instance for {@link AutoWire}.
		 */
		private final Map<AutoWire, AutoWireTeamInstance> autoWireTeamInstances;

		/**
		 * Initiate.
		 * 
		 * @param deployer
		 *            {@link OfficeFloorDeployer}.
		 * @param context
		 *            {@link DeployedOffice}.
		 * @param office
		 *            {@link DeployedOffice}.
		 * @param autoWireObjectInstances
		 *            {@link AutoWireObjectInstance} by its {@link AutoWire}.
		 * @param autoWireTeamInstances
		 *            {@link AutoWireTeamInstance} by its {@link AutoWire}.
		 */
		public AutoWireState(OfficeFloorDeployer deployer,
				OfficeFloorSourceContext context, DeployedOffice office,
				Map<AutoWire, AutoWireObjectInstance> autoWireObjectInstances,
				Map<AutoWire, AutoWireTeamInstance> autoWireTeamInstances) {
			this.deployer = deployer;
			this.context = context;
			this.office = office;
			this.autoWireObjectInstances = autoWireObjectInstances;
			this.autoWireTeamInstances = autoWireTeamInstances;
		}

		/**
		 * Obtains the unique {@link ManagedObject} name.
		 * 
		 * @param autoWire
		 *            {@link AutoWire} for naming.
		 * @return Unique {@link ManagedObject} name.
		 */
		public String getUniqueManagedObjectName(AutoWire autoWire) {

			// Obtain the raw name
			String name = autoWire.getQualifiedType();

			// Determine the unique managed object name
			Integer index = this.typeIndex.get(autoWire);
			if (index == null) {
				// First, so do not index name (as most cases only one)
				index = new Integer(1);
			} else {
				// Provide index on name and increment for next
				int indexValue = index.intValue();
				name = name + String.valueOf(indexValue);
				index = new Integer(indexValue + 1);
			}

			// Register index for next naming
			this.typeIndex.put(autoWire, index);

			// Return the unique name
			return name;
		}

		/**
		 * Creates the {@link OfficeFloorInputManagedObject}.
		 * 
		 * @param autoWire
		 *            {@link AutoWire}.
		 * @param managedObjectSource
		 *            {@link OfficeFloorManagedObjectSource}.
		 * @return {@link OfficeFloorInputManagedObject}.
		 */
		public OfficeFloorInputManagedObject createInputManagedObject(
				AutoWire autoWire,
				OfficeFloorManagedObjectSource managedObjectSource) {

			// Obtain the single input managed object for the auto-wire
			OfficeFloorInputManagedObject inputMo = this.inputManagedObjects
					.get(autoWire);
			if (inputMo == null) {
				// Create and register the input managed object
				inputMo = this.deployer.addInputManagedObject(autoWire
						.getQualifiedType());
				this.inputManagedObjects.put(autoWire, inputMo);

				// Only first is specified as bound
				inputMo.setBoundOfficeFloorManagedObjectSource(managedObjectSource);

				// Link to office object (only once for input managed object)
				OfficeObject officeObject = this.office
						.getDeployedOfficeObject(autoWire.getQualifiedType());
				this.deployer.link(officeObject, inputMo);
			}

			// Return the input managed object
			return inputMo;
		}

		/**
		 * Obtains the appropriate {@link AutoWireObjectInstance}.
		 * 
		 * @param qualifier
		 *            Qualifier. May be <code>null</code>.
		 * @param type
		 *            Type.
		 * @return Appropriate {@link AutoWireObjectInstance}. May be
		 *         <code>null</code> if no appropriately matching
		 *         {@link AutoWireObjectInstance}.
		 */
		public AutoWireObjectInstance getAppropriateObjectInstance(
				String qualifier, String type) {

			// Obtain the appropriate auto-wire
			AutoWire appropriateAutoWire = getAppropriateAutoWire(qualifier,
					type, this.autoWireObjectInstances.keySet());

			// Obtain the corresponding object instance
			AutoWireObjectInstance objectInstance = null;
			if (appropriateAutoWire != null) {
				objectInstance = this.autoWireObjectInstances
						.get(appropriateAutoWire);
			}

			// Return the object instance
			return objectInstance;
		}

		/**
		 * Obtains the appropriate {@link AutoWireTeamInstance}.
		 * 
		 * @param qualifier
		 *            Qualifier. May be <code>null</code>.
		 * @param type
		 *            Type.
		 * @return Appropriate {@link AutoWireTeamInstance}. May be
		 *         <code>null</code> if no appropriately matching
		 *         {@link AutoWireTeamInstance}.
		 */
		private AutoWireTeamInstance getAppropriateTeamInstance(
				String qualifier, String type) {

			// Obtain the appropriate auto-wire
			AutoWire appropriateAutoWire = getAppropriateAutoWire(qualifier,
					type, this.autoWireTeamInstances.keySet());

			// Obtain the corresponding team instance
			AutoWireTeamInstance teamInstance = null;
			if (appropriateAutoWire != null) {
				teamInstance = this.autoWireTeamInstances
						.get(appropriateAutoWire);
			}

			// Return the team instance
			return teamInstance;
		}
	}

	/**
	 * Instance of an {@link AutoWire} object.
	 */
	private static class AutoWireObjectInstance implements
			ManagedObjectSourceWirerContext {

		/**
		 * {@link AutoWireObject}.
		 */
		private final AutoWireObject autoWireObject;

		/**
		 * Raw object.
		 */
		private final Object rawObject;

		/**
		 * {@link OfficeFloorCompiler}.
		 */
		private final OfficeFloorCompiler compiler;

		/**
		 * Name of the {@link ManagedObject}.
		 */
		private String managedObjectName;

		/**
		 * Flag indicating is if {@link OfficeFloorInputManagedObject}.
		 */
		private boolean isInput = false;

		/**
		 * {@link AutoWireManagedObjectDependency} instances.
		 */
		private final List<AutoWireManagedObjectDependency> moDependencies = new LinkedList<AutoWireManagedObjectDependency>();

		/**
		 * {@link AutoWireManagedObjectFlow} instances.
		 */
		private final List<AutoWireManagedObjectFlow> moFlows = new LinkedList<AutoWireManagedObjectFlow>();

		/**
		 * {@link AutoWireManagedObjectTeam} instances.
		 */
		private final List<AutoWireManagedObjectTeam> moTeams = new LinkedList<AutoWireManagedObjectTeam>();

		/**
		 * {@link AutoWireTeam} instances.
		 */
		private final List<AutoWireTeam> wiredTeams = new LinkedList<AutoWireTeam>();

		/**
		 * {@link ManagedObjectType}.
		 */
		private ManagedObjectType<?> managedObjectType = null;

		/**
		 * {@link OfficeFloorManagedObjectSource}.
		 */
		private OfficeFloorManagedObjectSource managedObjectSource = null;

		/**
		 * {@link OfficeFloorManagedObject}.
		 */
		private OfficeFloorManagedObject managedObject = null;

		/**
		 * {@link OfficeFloorInputManagedObject}.
		 */
		private OfficeFloorInputManagedObject inputManagedObject = null;

		/**
		 * Indicates if built.
		 */
		private boolean isBuilt = false;

		/**
		 * Initiate.
		 * 
		 * @param autoWireObject
		 *            {@link AutoWireObject}.
		 * @param rawObject
		 *            Raw object.
		 * @param compiler
		 *            {@link OfficeFloorCompiler}.
		 */
		public AutoWireObjectInstance(AutoWireObject autoWireObject,
				Object rawObject, OfficeFloorCompiler compiler) {
			this.autoWireObject = autoWireObject;
			this.rawObject = rawObject;
			this.compiler = compiler;
		}

		/**
		 * Obtains the extension interfaces.
		 * 
		 * @param context
		 *            {@link OfficeFloorSourceContext}.
		 * @return Extension interfaces.
		 */
		public Class<?>[] getExtensionInterfaces(
				OfficeFloorSourceContext context) {

			// Obtain the managed object type
			this.loadManagedObjectType(context);
			if (this.managedObjectType == null) {
				return new Class<?>[0]; // no type
			}

			// Return the extension interfaces from type
			return managedObjectType.getExtensionInterfaces();
		}

		/**
		 * Obtains the {@link AutoWireManagedObjectDependency} instances for the
		 * type.
		 * 
		 * @param context
		 *            {@link OfficeFloorSourceContext}.
		 * @return {@link AutoWireManagedObjectDependency} instances for the
		 *         type.
		 */
		public AutoWireManagedObjectDependency[] getTypeDependencies(
				OfficeFloorSourceContext context) {

			// No dependencies if raw object
			if (this.rawObject != null) {
				return new AutoWireManagedObjectDependency[0];
			}

			// Obtain the managed object type
			this.loadManagedObjectType(context);
			if (this.managedObjectType == null) {
				// No type, no dependencies
				return new AutoWireManagedObjectDependency[0];
			}

			// Create the listing of auto-wire dependencies
			ManagedObjectDependencyType<?>[] dependencyTypes = this.managedObjectType
					.getDependencyTypes();
			AutoWireManagedObjectDependency[] dependencies = new AutoWireManagedObjectDependency[dependencyTypes.length];
			for (int i = 0; i < dependencyTypes.length; i++) {
				ManagedObjectDependencyType<?> dependencyType = dependencyTypes[i];

				// Create the managed object dependency
				String name = dependencyType.getDependencyName();
				String qualifier = dependencyType.getTypeQualifier();
				String type = dependencyType.getDependencyType().getName();
				AutoWireManagedObjectDependency dependency = new AutoWireManagedObjectDependency(
						name, new AutoWire(qualifier, type));

				// Register the dependency
				dependencies[i] = dependency;
			}

			// Return the dependencies
			return dependencies;
		}

		/**
		 * Loads the {@link ManagedObjectType}.
		 * 
		 * @param context
		 *            {@link OfficeFloorSourceContext}.
		 */
		private void loadManagedObjectType(OfficeFloorSourceContext context) {

			// Lazy load the managed object type
			if (this.managedObjectType == null) {

				// Determine if raw object
				if (this.rawObject != null) {
					// Obtain type from raw object
					this.managedObjectType = context.loadManagedObjectType(
							new SingletonManagedObjectSource(this.rawObject),
							this.autoWireObject.getProperties());

				} else {
					// Obtain type from managed object
					this.managedObjectType = context.loadManagedObjectType(
							this.autoWireObject
									.getManagedObjectSourceClassName(),
							this.autoWireObject.getProperties());
				}
			}
		}

		/**
		 * Initialise the {@link ManagedObject}.
		 */
		public void initManagedObject() {

			// Wire the managed object
			ManagedObjectSourceWirer wirer = this.autoWireObject
					.getManagedObjectSourceWirer();
			if (wirer != null) {
				wirer.wire(this);
			}
		}

		/**
		 * Builds the {@link OfficeFloorManagedObjectSource}.
		 * 
		 * @param state
		 *            {@link AutoWireState}.
		 */
		private void buildManagedObjectSource(AutoWireState state) {

			// Determine the details
			String managedObjectSourceClassName = this.autoWireObject
					.getManagedObjectSourceClassName();
			long timeout = this.autoWireObject.getTimeout();
			PropertyList properties = this.autoWireObject.getProperties();
			AutoWire[] autoWiring = this.autoWireObject.getAutoWiring();

			// Use first auto wiring for naming
			AutoWire firstAutoWire = autoWiring[0];
			this.managedObjectName = state
					.getUniqueManagedObjectName(firstAutoWire);

			// Determine if raw object
			if (this.rawObject != null) {
				// Bind the raw object
				ManagedObjectSource<?, ?> singleton;
				try {
					singleton = OfficeFloorCompilerAdapter
							.createSingletonManagedObjectSource(this.compiler,
									this.rawObject, autoWiring);
				} catch (ClassNotFoundException ex) {
					// Not able to create singleton
					state.deployer.addIssue("Unable to manage raw object "
							+ this.managedObjectName, ex,
							AssetType.MANAGED_OBJECT, this.managedObjectName);
					return; // must be able to create singleton
				}
				this.managedObjectSource = state.deployer
						.addManagedObjectSource(this.managedObjectName,
								singleton);

			} else {
				// Bind the managed object source
				this.managedObjectSource = state.deployer
						.addManagedObjectSource(this.managedObjectName,
								managedObjectSourceClassName);

				// Specify time out to source the managed object
				this.managedObjectSource.setTimeout(timeout);

				// Configure properties for managed object source
				for (Property property : properties) {
					this.managedObjectSource.addProperty(property.getName(),
							property.getValue());
				}
			}

			// Bind to managing office
			state.deployer.link(this.managedObjectSource.getManagingOffice(),
					state.office);
		}

		/**
		 * Builds this as an {@link OfficeFloorInputManagedObject}.
		 * 
		 * @param state
		 *            {@link AutoWireState}.
		 * @param officeObjectAutoWire
		 *            {@link OfficeObject} {@link AutoWire}.
		 */
		public void buildInputManagedObject(AutoWireState state,
				AutoWire officeObjectAutoWire) {

			// Build the managed object source
			this.buildManagedObjectSource(state);
			if (this.managedObjectSource == null) {
				return; // must load to be used
			}

			// Create the Input Managed Object
			this.inputManagedObject = state.createInputManagedObject(
					officeObjectAutoWire, this.managedObjectSource);

			// Link source to input
			state.deployer.link(this.managedObjectSource,
					this.inputManagedObject);
		}

		/**
		 * Builds the {@link ManagedObject}.
		 * 
		 * @param state
		 *            {@link AutoWireState}.
		 * @param officeObjectAutoWire
		 *            {@link AutoWire} to load this
		 *            {@link AutoWireObjectInstance} as an {@link OfficeObject}.
		 *            May be <code>null</code> to not load the
		 *            {@link OfficeObject}.
		 */
		public void buildManagedObject(AutoWireState state,
				AutoWire officeObjectAutoWire) {

			// Determine if input managed object
			if (this.isInput) {

				// Ensure that the input managed object built
				if (this.inputManagedObject == null) {
					state.deployer.addIssue(
							"May only use "
									+ OfficeFloorInputManagedObject.class
											.getSimpleName() + " if "
									+ DeployedOffice.class.getSimpleName()
									+ " providing handling of its input",
							AssetType.MANAGED_OBJECT, this.managedObjectName);
					return;
				}

				// Linked as input managed object
				return;
			}

			// Only build once
			if (!this.isBuilt) {

				// Only attempt to build once (also stops cyclic loops)
				this.isBuilt = true;

				// Build the managed object source
				this.buildManagedObjectSource(state);
				if (this.managedObjectSource == null) {
					return; // must load to be used
				}

				// Build the managed object
				this.managedObject = this.managedObjectSource
						.addOfficeFloorManagedObject(this.managedObjectName,
								ManagedObjectScope.PROCESS);

				// Link managed object
				this.linkManagedObject(state);
			}

			// Link managed object to office object (if loading to office)
			if (officeObjectAutoWire != null) {

				// Create the Office Object
				OfficeObject officeObject = state.office
						.getDeployedOfficeObject(officeObjectAutoWire
								.getQualifiedType());

				// Link to Managed Object to Office Object
				if (this.managedObject != null) {
					state.deployer.link(officeObject, this.managedObject);
				}
			}
		}

		/**
		 * Links the {@link ManagedObject}.
		 * 
		 * @param state
		 *            {@link AutoWireState}.
		 */
		public void linkManagedObject(AutoWireState state) {

			// Obtain the managed object dependencies from appropriate auto-wire
			Map<String, AutoWire> dependencies = new HashMap<String, AutoWire>();

			// Load the type dependencies
			for (AutoWireManagedObjectDependency typeDependency : this
					.getTypeDependencies(state.context)) {
				dependencies.put(typeDependency.dependencyName,
						typeDependency.dependencyAutoWire);
			}

			// Load overrides of the dependency mapping
			for (AutoWireManagedObjectDependency overrideDependency : this.moDependencies) {
				dependencies.put(overrideDependency.dependencyName,
						overrideDependency.dependencyAutoWire);
			}

			// Load the dependencies in deterministic order (i.e. sorted)
			List<String> dependencyNames = new ArrayList<String>(
					dependencies.keySet());
			Collections.sort(dependencyNames);

			// Link the dependencies
			for (String dependencyName : dependencyNames) {

				// Obtain the auto-wire of dependency
				AutoWire dependencyAutoWire = dependencies.get(dependencyName);

				// Obtain the dependency
				ManagedObjectDependency dependency;
				if (this.isInput) {
					dependency = this.managedObjectSource
							.getInputManagedObjectDependency(dependencyName);
				} else {
					dependency = this.managedObject
							.getManagedObjectDependency(dependencyName);
				}

				// Link the dependency
				this.linkDependency(dependency, dependencyAutoWire, state);
			}

			// Link flows
			for (AutoWireManagedObjectFlow flow : this.moFlows) {

				// Obtain the managed object flow
				ManagedObjectFlow moFlow = this.managedObjectSource
						.getManagedObjectFlow(flow.managedObjectSourceFlowName);

				// Obtain the office input
				DeployedOfficeInput officeInput = state.office
						.getDeployedOfficeInput(flow.sectionName,
								flow.sectionInputName);

				// Link managed object flow to office input
				state.deployer.link(moFlow, officeInput);
			}

			// Link teams
			for (AutoWireManagedObjectTeam team : this.moTeams) {

				// Obtain the managed object team
				String teamName = team.teamName;
				ManagedObjectTeam moTeam = this.managedObjectSource
						.getManagedObjectTeam(teamName);

				// Obtain the appropriate auto-wire for the team
				String teamType = team.teamAutoWire.getType();
				String teamQualifier = team.teamAutoWire.getQualifier();
				AutoWireTeamInstance teamInstance = state
						.getAppropriateTeamInstance(teamQualifier, teamType);
				if (teamInstance == null) {
					// No appropriate team for auto-wiring
					state.deployer.addIssue(
							"No " + Team.class.getSimpleName()
									+ " for auto-wiring "
									+ ManagedObjectTeam.class.getSimpleName()
									+ " " + teamName + " (qualifier="
									+ teamQualifier + ", type=" + teamType
									+ ")", AssetType.MANAGED_OBJECT,
							this.managedObjectName);
					continue; // can not link team
				}

				// Link managed object team to office floor team
				state.deployer.link(moTeam, teamInstance.team);
			}

			// Build wired in teams
			for (AutoWireTeam team : this.wiredTeams) {

				// Obtain the managed object team
				ManagedObjectTeam moTeam = this.managedObjectSource
						.getManagedObjectTeam(team.getTeamName());

				// Add the team
				OfficeFloorTeam officeFloorTeam = state.deployer.addTeam(
						this.managedObjectSource
								.getOfficeFloorManagedObjectSourceName()
								+ "-"
								+ team.getTeamName(), team
								.getTeamSourceClassName());
				for (Property property : team.getProperties()) {
					officeFloorTeam.addProperty(property.getName(),
							property.getValue());
				}

				// Link managed object team to office floor team
				state.deployer.link(moTeam, officeFloorTeam);
			}
		}

		/**
		 * Links the {@link ManagedObjectDependency}.
		 * 
		 * @param dependency
		 *            {@link ManagedObjectDependency}.
		 * @param dependencyAutoWire
		 *            {@link AutoWire} of {@link ManagedObjectDependency}.
		 * @param state
		 *            {@link AutoWireState}.
		 */
		private void linkDependency(ManagedObjectDependency dependency,
				AutoWire dependencyAutoWire, AutoWireState state) {

			// Obtain the auto-wire object instance
			AutoWireObjectInstance objectInstance = state
					.getAppropriateObjectInstance(
							dependencyAutoWire.getQualifier(),
							dependencyAutoWire.getType());

			// Only attempt to link if have object instance
			if (objectInstance != null) {

				// First try as Input Managed Object.
				// (This stops the Managed Object from having to be built)
				OfficeFloorInputManagedObject inputMo = objectInstance.inputManagedObject;
				if (inputMo != null) {
					state.deployer.link(dependency, inputMo);
					return; // depends on input managed object
				}

				// Not input, so ensure managed object is built for trying
				objectInstance.buildManagedObject(state, null);

				// Now try to link as Managed Object
				OfficeFloorManagedObject mo = objectInstance.managedObject;
				if (mo != null) {
					state.deployer.link(dependency, mo);
					return; // depends on managed object
				}
			}

			// As here, no managed object for dependency
			state.deployer.addIssue(
					"No dependent managed object for auto-wiring dependency "
							+ dependency.getManagedObjectDependencyName()
							+ " (qualifier="
							+ dependencyAutoWire.getQualifier() + ", type="
							+ dependencyAutoWire.getType() + ")",
					AssetType.MANAGED_OBJECT, this.managedObjectName);
		}

		/*
		 * =============== ManagedObjectSourceWirerContext =================
		 */

		@Override
		public void setInput(boolean isInput) {
			this.isInput = isInput;
		}

		@Override
		public void mapDependency(String dependencyName, AutoWire autoWire) {
			this.moDependencies.add(new AutoWireManagedObjectDependency(
					dependencyName, autoWire));
		}

		@Override
		public void mapFlow(String managedObjectSourceFlowName,
				String sectionName, String sectionInputName) {
			this.moFlows
					.add(new AutoWireManagedObjectFlow(
							managedObjectSourceFlowName, sectionName,
							sectionInputName));
		}

		@Override
		public <S extends TeamSource> AutoWireTeam mapTeam(
				String managedObjectSourceTeamName, String teamSourceClassName) {

			// Create the properties for the team
			PropertyList properties = this.compiler.createPropertyList();

			// Register the team mapping
			AutoWireTeam team = new AutoWireTeamImpl(this.compiler,
					managedObjectSourceTeamName, teamSourceClassName,
					properties);
			this.wiredTeams.add(team);

			// Return the team
			return team;
		}

		@Override
		public void mapTeam(String managedObjectSourceTeamName,
				AutoWire autoWire) {
			this.moTeams.add(new AutoWireManagedObjectTeam(
					managedObjectSourceTeamName, autoWire));
		}
	}

	/**
	 * Instance of an {@link AutoWire} {@link Team}.
	 */
	private static class AutoWireTeamInstance {

		/**
		 * {@link OfficeFloorTeam}.
		 */
		public final OfficeFloorTeam team;

		/**
		 * Initiate.
		 * 
		 * @param team
		 *            {@link OfficeFloorTeam}.
		 */
		public AutoWireTeamInstance(OfficeFloorTeam team) {
			this.team = team;
		}
	}

	/**
	 * Auto-wire {@link ManagedObject} dependency.
	 */
	private static class AutoWireManagedObjectDependency {

		/**
		 * Name of the dependency.
		 */
		public final String dependencyName;

		/**
		 * {@link AutoWire} of the dependency.
		 */
		public final AutoWire dependencyAutoWire;

		/**
		 * Initiate.
		 * 
		 * @param dependencyName
		 *            Name of the dependency.
		 * @param dependencyAutoWire
		 *            {@link AutoWire} of the dependency.
		 */
		public AutoWireManagedObjectDependency(String dependencyName,
				AutoWire dependencyAutoWire) {
			this.dependencyName = dependencyName;
			this.dependencyAutoWire = dependencyAutoWire;
		}
	}

	/**
	 * Auto-wire {@link ManagedObject} flow.
	 */
	private static class AutoWireManagedObjectFlow {

		/**
		 * Name of the {@link ManagedObjectFlow}.
		 */
		public final String managedObjectSourceFlowName;

		/**
		 * Name of the {@link OfficeSection}.
		 */
		public final String sectionName;

		/**
		 * Name of the {@link OfficeSectionInput}.
		 */
		public final String sectionInputName;

		/**
		 * Initiate.
		 * 
		 * @param managedObjectSourceFlowName
		 *            Name of the {@link ManagedObjectFlow}.
		 * @param sectionName
		 *            Name of the {@link OfficeSection}.
		 * @param sectionInputName
		 *            Name of the {@link OfficeSectionInput}.
		 */
		public AutoWireManagedObjectFlow(String managedObjectSourceFlowName,
				String sectionName, String sectionInputName) {
			this.managedObjectSourceFlowName = managedObjectSourceFlowName;
			this.sectionName = sectionName;
			this.sectionInputName = sectionInputName;
		}
	}

	/**
	 * Auto-wire {@link ManagedObject} {@link Team}.
	 */
	private static class AutoWireManagedObjectTeam {

		/**
		 * Name of the {@link ManagedObjectTeam}.
		 */
		public final String teamName;

		/**
		 * {@link AutoWire} of the {@link ManagedObjectTeam}.
		 */
		public final AutoWire teamAutoWire;

		/**
		 * Initiate.
		 * 
		 * @param teamName
		 *            Name of the {@link ManagedObjectTeam}.
		 * @param teamAutoWire
		 *            {@link AutoWire} of the {@link ManagedObjectTeam}.
		 */
		public AutoWireManagedObjectTeam(String teamName, AutoWire teamAutoWire) {
			this.teamName = teamName;
			this.teamAutoWire = teamAutoWire;
		}
	}

}