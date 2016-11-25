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
import java.util.Arrays;
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
import net.officefloor.autowire.AutoWireSection;
import net.officefloor.autowire.AutoWireSectionFactory;
import net.officefloor.autowire.AutoWireSectionTransformer;
import net.officefloor.autowire.AutoWireSupplier;
import net.officefloor.autowire.AutoWireTeam;
import net.officefloor.autowire.ManagedObjectSourceWirer;
import net.officefloor.autowire.ManagedObjectSourceWirerContext;
import net.officefloor.autowire.supplier.SuppliedManagedObjectDependencyType;
import net.officefloor.autowire.supplier.SuppliedManagedObjectFlowType;
import net.officefloor.autowire.supplier.SuppliedManagedObjectTeamType;
import net.officefloor.autowire.supplier.SuppliedManagedObjectType;
import net.officefloor.autowire.supplier.SupplierType;
import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.impl.issues.FailCompilerIssues;
import net.officefloor.compile.internal.structure.ManagedObjectNode;
import net.officefloor.compile.managedobject.ManagedObjectDependencyType;
import net.officefloor.compile.managedobject.ManagedObjectType;
import net.officefloor.compile.office.OfficeManagedObjectType;
import net.officefloor.compile.office.OfficeAvailableSectionInputType;
import net.officefloor.compile.office.OfficeTeamType;
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
import net.officefloor.compile.spi.officefloor.OfficeFloorSupplier;
import net.officefloor.compile.spi.officefloor.OfficeFloorTeam;
import net.officefloor.compile.spi.officefloor.source.OfficeFloorSource;
import net.officefloor.compile.spi.officefloor.source.OfficeFloorSourceContext;
import net.officefloor.compile.spi.officefloor.source.RequiredProperties;
import net.officefloor.compile.spi.officefloor.source.impl.AbstractOfficeFloorSource;
import net.officefloor.compile.spi.section.ManagedObjectDependency;
import net.officefloor.compile.spi.section.ManagedObjectFlow;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.profile.Profiler;
import net.officefloor.frame.impl.spi.team.PassiveTeamSource;
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
 * simpler configuration. It is anticipated in the majority of cases that this
 * will be adequate for most applications. Should however more flexibility be
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
	 * {@link AutoWireObjectSource} instances.
	 */
	private final List<AutoWireObjectSource> objectSources = new LinkedList<AutoWireObjectSource>();

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
	public void addSectionTransformer(AutoWireSectionTransformer transformer) {
		this.officeSource.addSectionTransformer(transformer);
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
	public void addStartupFlow(AutoWireSection section, String inputName) {
		this.officeSource.addStartupFlow(section, inputName);
	}

	@Override
	public void addObject(Object object, AutoWire... autoWiring) {

		// Default the object type if not provided
		if ((autoWiring == null) || (autoWiring.length == 0)) {
			autoWiring = new AutoWire[] { new AutoWire(object.getClass()) };
		}

		// Create the properties
		PropertyList properties = this.compiler.createPropertyList();

		// Register the raw object (always in process scope)
		this.objectSources.add(new AutoWireObjectInstance(
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
		this.objectSources.add(new AutoWireObjectInstance(object, null,
				this.compiler));

		// Return the auto wire object
		return object;
	}

	@Override
	public AutoWireSupplier addSupplier(String supplierSourceClassName) {

		// Create the properties
		PropertyList properties = this.compiler.createPropertyList();

		// Create the auto-wire supplier
		AutoWireSupplier supplier = new AutoWireSupplierImpl(this.compiler,
				supplierSourceClassName, properties);

		// Register the supplier
		this.objectSources.add(new AutoWireSupplierInstance(supplier));

		// Return the auto-wire supplier
		return supplier;
	}

	@Override
	public boolean isObjectAvailable(AutoWire autoWire) {

		// Determine if the auto wire is available
		for (AutoWireObjectSource objectSource : this.objectSources) {
			if (objectSource instanceof AutoWireObjectInstance) {
				AutoWireObjectInstance objectInstance = (AutoWireObjectInstance) objectSource;
				for (AutoWire availableAutoWire : objectInstance
						.getAutoWiring()) {
					if (autoWire.equals(availableAutoWire)) {
						return true; // auto wire is available
					}
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
		String teamName = autoWiring[0].getQualifiedType();

		// Create the properties
		PropertyList properties = this.compiler.createPropertyList();

		// Create and add the team
		AutoWireTeam team = new AutoWireTeamImpl(this.compiler, teamName,
				teamSourceClassName, properties, autoWiring);
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
	public void setProfiler(Profiler profiler) {
		// Add the profiler to the office
		this.getOfficeFloorCompiler().addProfiler(OFFICE_NAME, profiler);
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

		// Source the objects
		List<AutoWireObjectInstance> objectInstances = new LinkedList<AutoWireObjectInstance>();
		for (AutoWireObjectSource objectSource : this.objectSources) {
			AutoWireObjectInstance[] sourcedObjectInstances = objectSource
					.sourceObjectInstances(context, this.compiler);
			objectInstances.addAll(Arrays.asList(sourcedObjectInstances));
		}

		// Load available office objects
		Set<AutoWire> availableAutoWires = new HashSet<AutoWire>();
		for (AutoWireObjectInstance objectInstance : objectInstances) {

			/*
			 * TODO EDGE CASE: Handled InputManagedObject instances override
			 * ManagedObject instances. Issue however is that can not determine
			 * handling of InputManagedObject until the OfficeType is loaded
			 * (which requires the avaiableOfficeObjects).
			 * 
			 * To overcome may need to just work off premise that
			 * InputManagedObject always overrides ManagedObject (whether
			 * handled or not). Need to consider but for now leaving as EDGE
			 * CASE that very unlikely to occur with reasonable configuration.
			 */

			// Obtain the available auto-wiring
			AutoWire[] autoWiring = objectInstance.getAutoWiring();

			// Obtain the extension interfaces for object type
			Class<?>[] extensionInterfaces = objectInstance
					.getExtensionInterfaces(deployer, context);

			// Load the first available auto-wire for office objects
			for (AutoWire autoWire : autoWiring) {

				// Ensure only load the first auto-wire
				if (availableAutoWires.contains(autoWire)) {
					continue; // already available
				}
				availableAutoWires.add(autoWire);

				// Load the auto-wire as an available office object
				this.officeSource.addAvailableOfficeObject(autoWire,
						extensionInterfaces);
			}
		}

		// Load available office teams
		for (AutoWireTeam autoWireTeam : this.teams) {
			for (AutoWire autoWire : autoWireTeam.getAutoWiring()) {
				this.officeSource.addAvailableOfficeTeam(autoWire);
			}
		}

		// Load the office type
		OfficeType officeType = context.loadOfficeType(this.officeSource,
				"auto-wire", context.createPropertyList());

		// Load all handled office inputs
		Set<AutoWire> handledInputs = new HashSet<AutoWire>();
		for (OfficeAvailableSectionInputType officeInput : officeType
				.getOfficeSectionInputTypes()) {

			// Obtain the auto-wire for the office input
			String sectionName = officeInput.getOfficeSectionName();
			String inputName = officeInput.getOfficeSectionInputName();
			AutoWire handledInput = new AutoWire(sectionName, inputName);

			// Add the handled input
			handledInputs.add(handledInput);
		}

		// Obtain all used Team auto-wiring
		List<AutoWire> usedTeamAutoWiring = new LinkedList<AutoWire>();
		for (OfficeTeamType officeTeam : officeType.getOfficeTeamTypes()) {

			// Parse out the auto-wire
			AutoWire usedTeamAutoWire = AutoWire.valueOf(officeTeam
					.getOfficeTeamName());

			// Add the used team auto-wire (uniquely)
			if (!(usedTeamAutoWiring.contains(usedTeamAutoWire))) {
				usedTeamAutoWiring.add(usedTeamAutoWire);
			}
		}

		// Obtain all used Object auto-wiring
		List<AutoWire> usedObjectAutoWiring = new LinkedList<AutoWire>();
		for (OfficeManagedObjectType officeObject : officeType
				.getOfficeManagedObjectTypes()) {

			// Obtain the auto-wire for the office object
			String type = officeObject.getObjectType();
			String qualifier = officeObject.getTypeQualifier();
			AutoWire usedObjectAutoWire = new AutoWire(qualifier, type);

			// Add the used object auto-wire (uniquely)
			if (!(usedObjectAutoWiring.contains(usedObjectAutoWire))) {
				usedObjectAutoWiring.add(usedObjectAutoWire);
			}
		}

		// Add the auto-wiring office
		DeployedOffice office = deployer.addDeployedOffice(OFFICE_NAME,
				this.officeSource, "auto-wire");

		// Ensure have default AutoWireTeam
		AutoWireTeamInstance defaultAutoWireTeamInstance;
		if (this.defaultTeam != null) {
			// Use the specified default team
			defaultAutoWireTeamInstance = new AutoWireTeamInstance(
					this.defaultTeam);

		} else {
			// Unspecified default Team to use implicit thread
			defaultAutoWireTeamInstance = new AutoWireTeamInstance(
					new AutoWireTeamImpl(this.compiler, "team",
							PassiveTeamSource.class.getName(),
							this.compiler.createPropertyList()));
		}

		// Create the mapping of auto-wiring to realised teams
		Map<AutoWire, AutoWireTeamInstance> autoWireTeamInstances = new HashMap<AutoWire, AutoWireTeamInstance>();
		for (AutoWireTeam autoWireTeam : this.teams) {

			// Create the auto-wire team instance
			AutoWireTeamInstance teamInstance = new AutoWireTeamInstance(
					autoWireTeam);

			// Only load the first registered for auto-wire
			for (AutoWire autoWire : autoWireTeam.getAutoWiring()) {
				if (!(autoWireTeamInstances.containsKey(autoWire))) {
					autoWireTeamInstances.put(autoWire, teamInstance);
				}
			}
		}

		// Create the mapping of auto-wiring to realised objects
		Map<AutoWire, AutoWireObjectInstance> autoWireObjectInstances = new HashMap<AutoWire, AutoWireObjectInstance>();
		List<AutoWireObjectInstance> inputObjectInstances = new LinkedList<AutoWireObjectInstance>();
		List<AutoWireObjectInstance> unhandledInputObjectInstances = new LinkedList<AutoWireObjectInstance>();
		Map<AutoWire, Integer> nameIndex = new HashMap<AutoWire, Integer>();
		for (AutoWireObjectInstance objectInstance : objectInstances) {

			// Initialise the object
			objectInstance.initManagedObject(deployer, context, nameIndex);

			// Handle input to allow overriding non-input managed object
			if (objectInstance.isInput) {

				// Ensure only one auto-wire for input managed object
				AutoWire[] inputAutoWiring = objectInstance.getAutoWiring();
				if (inputAutoWiring.length != 1) {
					String inputManagedObjectName = (inputAutoWiring.length == 0 ? "UNKNOWN"
							: inputAutoWiring[0].getQualifiedType());
					deployer.addIssue(OfficeFloorInputManagedObject.class
							.getSimpleName()
							+ " "
							+ inputManagedObjectName
							+ " must have only one "
							+ AutoWire.class.getSimpleName());
					continue; // do not include
				}

				// Include input managed object only if all flows handled
				boolean isInclude = true;
				for (AutoWireManagedObjectFlow flow : objectInstance
						.getTypeFlows()) {
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

					// Override object instance if not input managed object
					AutoWire inputAutoWire = objectInstance.getAutoWiring()[0];
					AutoWireObjectInstance mappedObjectInstance = autoWireObjectInstances
							.get(inputAutoWire);
					if ((mappedObjectInstance != null)
							&& (mappedObjectInstance.isInput)) {
						continue; // already input managed object mapped
					}
					autoWireObjectInstances.put(inputAutoWire, objectInstance);

				} else {
					// Not handled, so do not include
					unhandledInputObjectInstances.add(objectInstance);
				}
			}

			// Only load the first registered for auto-wire (if not input)
			for (AutoWire autoWire : objectInstance.getAutoWiring()) {
				if (!(autoWireObjectInstances.containsKey(autoWire))) {
					autoWireObjectInstances.put(autoWire, objectInstance);
				}
			}
		}

		// Add unhandled input managed objects for better error messaging
		for (AutoWireObjectInstance unhandledInputObjectInstance : unhandledInputObjectInstances) {
			AutoWire autoWire = unhandledInputObjectInstance.getAutoWiring()[0];
			if (!(autoWireObjectInstances.containsKey(autoWire))) {
				autoWireObjectInstances.put(autoWire,
						unhandledInputObjectInstance);
			}
		}

		// Create the auto-wire state
		AutoWireState state = new AutoWireState(deployer, context, office,
				autoWireObjectInstances, autoWireTeamInstances,
				defaultAutoWireTeamInstance);

		// Build the used auto-wire teams by Office
		for (AutoWire usedTeamAutoWire : usedTeamAutoWiring) {

			// Obtain the corresponding auto-wire team instance
			String qualifier = usedTeamAutoWire.getQualifier();
			String type = usedTeamAutoWire.getType();
			AutoWireTeamInstance autoWireTeamInstance = state
					.getAppropriateTeamInstance(qualifier, type);

			// Build the used team (and link to Office)
			autoWireTeamInstance.buildTeam(state, usedTeamAutoWire);
		}

		// Build the input auto-wire objects
		Set<AutoWire> usedInputAutoWiring = new HashSet<AutoWire>();
		for (AutoWireObjectInstance inputObjectInstance : inputObjectInstances) {

			// Above should confirm the one auto-wire for input managed object
			AutoWire inputAutoWire = inputObjectInstance.getAutoWiring()[0];

			// Build the input managed object
			inputObjectInstance.buildInputManagedObject(state, inputAutoWire);

			// Register the input managed object as used
			usedInputAutoWiring.add(inputAutoWire);
		}

		// Link the input auto-wire objects
		for (AutoWireObjectInstance inputObjectInstance : inputObjectInstances) {
			inputObjectInstance.linkManagedObject(state);
		}

		// Build the used auto-wire objects by Office
		for (AutoWire usedObjectAutoWire : usedObjectAutoWiring) {

			// Obtain the corresponding auto-wire object instance
			String qualifier = usedObjectAutoWire.getQualifier();
			String type = usedObjectAutoWire.getType();
			AutoWireObjectInstance autoWireObjectInstance = state
					.getAppropriateObjectInstance(qualifier, type);
			if (autoWireObjectInstance == null) {
				deployer.addIssue("No auto-wire object available for "
						+ usedObjectAutoWire.getQualifiedType());
				continue; // must have auto-wire object to load managed object
			}

			// Do not build if input
			if (autoWireObjectInstance.isInput) {
				continue; // already loaded above

			} else if (usedInputAutoWiring.contains(usedObjectAutoWire)) {
				// Auto-wire already loaded as input managed object
				deployer.addIssue("Auto-wire "
						+ usedObjectAutoWire.getQualifiedType() + " has both "
						+ OfficeFloorInputManagedObject.class.getSimpleName()
						+ " and "
						+ OfficeFloorManagedObject.class.getSimpleName()
						+ " mapped to it");
				continue;
			}

			// Build the used managed object (and link to Office)
			autoWireObjectInstance
					.buildManagedObject(state, usedObjectAutoWire);
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
		 * {@link OfficeFloorInputManagedObject} for {@link AutoWire}.
		 */
		private final Map<AutoWire, OfficeFloorInputManagedObject> inputManagedObjects = new HashMap<AutoWire, OfficeFloorInputManagedObject>();

		/**
		 * {@link AutoWireTeamInstance} instance for {@link AutoWire}.
		 */
		private final Map<AutoWire, AutoWireTeamInstance> autoWireTeamInstances;

		/**
		 * Default {@link AutoWireTeamInstance}.
		 */
		private final AutoWireTeamInstance defaultAutoWireTeamInstance;

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
		 * @param defaultAutoWireTeamInstance
		 *            Default {@link AutoWireTeamInstance}.
		 */
		public AutoWireState(OfficeFloorDeployer deployer,
				OfficeFloorSourceContext context, DeployedOffice office,
				Map<AutoWire, AutoWireObjectInstance> autoWireObjectInstances,
				Map<AutoWire, AutoWireTeamInstance> autoWireTeamInstances,
				AutoWireTeamInstance defaultAutoWireTeamInstance) {
			this.deployer = deployer;
			this.context = context;
			this.office = office;
			this.autoWireObjectInstances = autoWireObjectInstances;
			this.autoWireTeamInstances = autoWireTeamInstances;
			this.defaultAutoWireTeamInstance = defaultAutoWireTeamInstance;
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

			// Use Default Team if no auto-wire Team
			if (teamInstance == null) {
				teamInstance = this.defaultAutoWireTeamInstance;
			}

			// Return the team instance
			return teamInstance;
		}
	}

	/**
	 * Source to obtain {@link AutoWireObjectInstance} instances.
	 */
	private static interface AutoWireObjectSource {

		/**
		 * Sources the {@link AutoWireObjectInstance}.
		 * 
		 * @param context
		 *            {@link OfficeFloorSourceContext}.
		 * @param compiler
		 *            {@link OfficeFloorCompiler}.
		 * @return Sourced {@link AutoWireObjectInstance} instances.
		 */
		public AutoWireObjectInstance[] sourceObjectInstances(
				OfficeFloorSourceContext context, OfficeFloorCompiler compiler);
	}

	/**
	 * Instance of an {@link AutoWire} object.
	 */
	private static class AutoWireObjectInstance implements
			AutoWireObjectSource, ManagedObjectSourceWirerContext {

		/**
		 * {@link AutoWireObject}.
		 */
		private final AutoWireObject autoWireObject;

		/**
		 * {@link SuppliedManagedObjectType}.
		 */
		private final SuppliedManagedObjectType suppliedManagedObjectType;

		/**
		 * {@link AutoWireSupplierInstance}.
		 */
		private final AutoWireSupplierInstance supplierInstance;

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
		 * {@link ManagedObjectScope}.
		 */
		private ManagedObjectScope managedObjectScope = null;

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
		 * Initiate for an {@link AutoWireObject}.
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
			this.suppliedManagedObjectType = null;
			this.supplierInstance = null;
			this.rawObject = rawObject;
			this.compiler = compiler;
		}

		/**
		 * Initiate for a {@link SuppliedManagedObjectType} from a
		 * {@link AutoWireSupplier}.
		 * 
		 * @param suppliedManagedObjectType
		 *            {@link SuppliedManagedObjectType}.
		 * @param supplierInstance
		 *            {@link AutoWireSupplierInstance}.
		 * @param compiler
		 *            {@link OfficeFloorCompiler}.
		 */
		public AutoWireObjectInstance(
				SuppliedManagedObjectType suppliedManagedObjectType,
				AutoWireSupplierInstance supplierInstance,
				OfficeFloorCompiler compiler) {
			this.autoWireObject = null;
			this.suppliedManagedObjectType = suppliedManagedObjectType;
			this.supplierInstance = supplierInstance;
			this.rawObject = null;
			this.compiler = compiler;
		}

		@Override
		public AutoWireObjectInstance[] sourceObjectInstances(
				OfficeFloorSourceContext context, OfficeFloorCompiler compiler) {
			// Only this to source
			return new AutoWireObjectInstance[] { this };
		}

		/**
		 * Obtains the {@link AutoWire} instances for this
		 * {@link AutoWireObjectInstance}.
		 * 
		 * @return {@link AutoWire} instances for this
		 *         {@link AutoWireObjectInstance}.
		 */
		public AutoWire[] getAutoWiring() {

			// Try auto-wiring from supplied managed object first
			if (this.suppliedManagedObjectType != null) {
				return this.suppliedManagedObjectType.getAutoWiring();
			}

			// Try next from auto-wire object
			if (this.autoWireObject != null) {
				return this.autoWireObject.getAutoWiring();
			}

			// Otherwise no auto-wiring (likely supplier not yet loaded)
			return new AutoWire[0];
		}

		/**
		 * Obtains the extension interfaces.
		 * 
		 * @param deployer
		 *            {@link OfficeFloorDeployer}.
		 * @param context
		 *            {@link OfficeFloorSourceContext}.
		 * @return Extension interfaces.
		 */
		public Class<?>[] getExtensionInterfaces(OfficeFloorDeployer deployer,
				OfficeFloorSourceContext context) {

			// Obtain from supplied managed object type
			if (this.suppliedManagedObjectType != null) {
				return this.suppliedManagedObjectType.getExtensionInterfaces();
			}

			// Obtain the managed object type
			this.loadManagedObjectType(deployer, context);
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
		 * @param deployer
		 *            {@link OfficeFloorDeployer}.
		 * @param context
		 *            {@link OfficeFloorSourceContext}.
		 * @return {@link AutoWireManagedObjectDependency} instances for the
		 *         type.
		 */
		public AutoWireManagedObjectDependency[] getTypeDependencies(
				OfficeFloorDeployer deployer, OfficeFloorSourceContext context) {

			// No dependencies if raw object
			if (this.rawObject != null) {
				return new AutoWireManagedObjectDependency[0];
			}

			// Obtain the dependencies
			AutoWireManagedObjectDependency[] dependencies;

			// Determine if supplied managed object
			if (this.suppliedManagedObjectType != null) {
				// Create listing of supplied managed object dependencies
				SuppliedManagedObjectDependencyType[] dependencyTypes = this.suppliedManagedObjectType
						.getDependencyTypes();
				dependencies = new AutoWireManagedObjectDependency[dependencyTypes.length];
				for (int i = 0; i < dependencyTypes.length; i++) {
					SuppliedManagedObjectDependencyType dependencyType = dependencyTypes[i];

					// Create the managed object dependency
					String name = dependencyType.getDependencyName();
					String qualifier = dependencyType.getTypeQualifier();
					String type = dependencyType.getDependencyType();
					AutoWireManagedObjectDependency dependency = new AutoWireManagedObjectDependency(
							name, new AutoWire(qualifier, type));

					// Register the dependency
					dependencies[i] = dependency;
				}

			} else {
				// Managed object so load its type
				this.loadManagedObjectType(deployer, context);
				if (this.managedObjectType == null) {
					// No type, no dependencies
					dependencies = new AutoWireManagedObjectDependency[0];

				} else {
					// Create listing of managed object dependencies
					ManagedObjectDependencyType<?>[] dependencyTypes = this.managedObjectType
							.getDependencyTypes();
					dependencies = new AutoWireManagedObjectDependency[dependencyTypes.length];
					for (int i = 0; i < dependencyTypes.length; i++) {
						ManagedObjectDependencyType<?> dependencyType = dependencyTypes[i];

						// Create the managed object dependency
						String name = dependencyType.getDependencyName();
						String qualifier = dependencyType.getTypeQualifier();
						String type = dependencyType.getDependencyType()
								.getName();
						AutoWireManagedObjectDependency dependency = new AutoWireManagedObjectDependency(
								name, new AutoWire(qualifier, type));

						// Register the dependency
						dependencies[i] = dependency;
					}
				}
			}

			// Return the dependencies
			return dependencies;
		}

		/**
		 * Obtains the {@link AutoWireManagedObjectFlow} instances for the type.
		 * 
		 * @return {@link AutoWireManagedObjectFlow} instances for the type.
		 */
		public AutoWireManagedObjectFlow[] getTypeFlows() {

			// Determine if supplied managed object
			List<AutoWireManagedObjectFlow> flows;
			if (this.suppliedManagedObjectType != null) {
				// Supplied, so obtain from supplied type
				SuppliedManagedObjectFlowType[] flowTypes = this.suppliedManagedObjectType
						.getFlowTypes();
				flows = new ArrayList<AutoWireManagedObjectFlow>(
						flowTypes.length);
				for (int i = 0; i < flowTypes.length; i++) {
					SuppliedManagedObjectFlowType flowType = flowTypes[i];
					flows.add(new AutoWireManagedObjectFlow(flowType
							.getFlowName(), flowType.getSectionName(), flowType
							.getSectionInputName()));
				}

			} else {
				// Managed Object, so use flows configured from wiring
				flows = this.moFlows;
			}

			// Return the listing of type flows
			return flows.toArray(new AutoWireManagedObjectFlow[flows.size()]);
		}

		/**
		 * Loads the {@link ManagedObjectType}.
		 * 
		 * @param deployer
		 *            {@link OfficeFloorDeployer}.
		 * @param context
		 *            {@link OfficeFloorSourceContext}.
		 */
		private void loadManagedObjectType(OfficeFloorDeployer deployer,
				OfficeFloorSourceContext context) {

			// Lazy load the managed object type
			if (this.suppliedManagedObjectType != null) {
				// Determine if input from the supplied managed object type
				this.isInput = this.suppliedManagedObjectType
						.isInputManagedObject();

			} else if (this.managedObjectType == null) {
				// Determine if raw object
				if (this.rawObject != null) {

					// Create the managed object source
					ManagedObjectSource<?, ?> singleton = new SingletonManagedObjectSource(
							this.rawObject);

					// Obtain type from raw object
					this.managedObjectType = context.loadManagedObjectType(
							singleton, this.autoWireObject.getProperties());

				} else {
					// Obtain type from managed object
					this.managedObjectType = context.loadManagedObjectType(
							this.autoWireObject
									.getManagedObjectSourceClassName(),
							this.autoWireObject.getProperties());
				}

				// Determine if input
				this.isInput = context
						.isInputManagedObject(this.managedObjectType);
			}
		}

		/**
		 * Initialise the {@link ManagedObject}.
		 * 
		 * @param deployer
		 *            {@link OfficeFloorDeployer}.
		 * @param context
		 *            {@link OfficeFloorSourceContext}.
		 * @param nameIndex
		 *            Index suffix for naming the {@link ManagedObject}.
		 */
		public void initManagedObject(OfficeFloorDeployer deployer,
				OfficeFloorSourceContext context,
				Map<AutoWire, Integer> nameIndex) {

			// Ensure load managed object type (and subsequent state)
			this.loadManagedObjectType(deployer, context);

			// Obtain the raw managed object name (from first auto-wire)
			AutoWire firstAutoWire = this.getAutoWiring()[0];
			String rawName = firstAutoWire.getQualifiedType();

			// Specify the unique managed object name
			Integer index = nameIndex.get(firstAutoWire);
			if (index == null) {
				// First, so do not index name (as most cases only one)
				this.managedObjectName = rawName;
				index = new Integer(1);
			} else {
				// Provide index on name and increment for next
				int indexValue = index.intValue();
				this.managedObjectName = rawName + String.valueOf(indexValue);
				index = new Integer(indexValue + 1);
			}
			nameIndex.put(firstAutoWire, index); // for next naming

			// Wire the managed object (if auto-wire object)
			if (this.autoWireObject != null) {
				ManagedObjectSourceWirer wirer = this.autoWireObject
						.getManagedObjectSourceWirer();
				if (wirer != null) {
					wirer.wire(this);
				}
			}

			// Default the managed object scope (if not specified by wirer)
			if (this.managedObjectScope == null) {
				this.managedObjectScope = (this.isInput ? ManagedObjectScope.PROCESS
						: ManagedObjectScope.THREAD);
			}
		}

		/**
		 * Builds the {@link OfficeFloorManagedObjectSource}.
		 * 
		 * @param state
		 *            {@link AutoWireState}.
		 */
		private void buildManagedObjectSource(AutoWireState state) {

			// Build the managed object source
			if (this.suppliedManagedObjectType != null) {
				// Build managed object source from supplier
				OfficeFloorSupplier supplier = this.supplierInstance
						.buildSupplier(state);
				this.managedObjectSource = supplier.addManagedObjectSource(
						this.managedObjectName, this.getAutoWiring()[0]);

			} else {
				// Build managed object source from auto-wire object
				String managedObjectSourceClassName = this.autoWireObject
						.getManagedObjectSourceClassName();
				long timeout = this.autoWireObject.getTimeout();
				PropertyList properties = this.autoWireObject.getProperties();

				// Determine if raw object
				if (this.rawObject != null) {
					// Create singleton for the raw object
					ManagedObjectSource<?, ?> singleton = new SingletonManagedObjectSource(
							this.rawObject);

					// Build the managed object source
					this.managedObjectSource = state.deployer
							.addManagedObjectSource(this.managedObjectName,
									singleton);

				} else {
					// Build the managed object source
					this.managedObjectSource = state.deployer
							.addManagedObjectSource(this.managedObjectName,
									managedObjectSourceClassName);

					// Specify time out to source the managed object
					this.managedObjectSource.setTimeout(timeout);

					// Configure properties for managed object source
					for (Property property : properties) {
						this.managedObjectSource.addProperty(
								property.getName(), property.getValue());
					}
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

			// Ensure if Input Managed Object that not built as Managed Object
			if (this.isInput) {
				throw new IllegalStateException(
						"Can not build InputManagedObject as ManagedObject");
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
								this.managedObjectScope);

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
					.getTypeDependencies(state.deployer, state.context)) {
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
			for (AutoWireManagedObjectFlow flow : this.getTypeFlows()) {

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

			// Obtain the listing of auto-wire teams
			List<AutoWireManagedObjectTeam> teams;
			if (this.suppliedManagedObjectType != null) {
				// Supplied, so obtain from supplied type
				SuppliedManagedObjectTeamType[] teamTypes = this.suppliedManagedObjectType
						.getTeamTypes();
				teams = new ArrayList<AutoWireManagedObjectTeam>(
						teamTypes.length);
				for (int i = 0; i < teamTypes.length; i++) {
					SuppliedManagedObjectTeamType teamType = teamTypes[i];
					teams.add(new AutoWireManagedObjectTeam(teamType
							.getTeamName(), teamType.getTeamAutoWire()));
				}

			} else {
				// Managed Object, so use teams configured from wiring
				teams = this.moTeams;
			}

			// Link teams
			for (AutoWireManagedObjectTeam team : teams) {

				// Obtain the managed object team
				String teamName = team.teamName;
				ManagedObjectTeam moTeam = this.managedObjectSource
						.getManagedObjectTeam(teamName);

				// Obtain the appropriate auto-wire for the team
				String teamType = team.teamAutoWire.getType();
				String teamQualifier = team.teamAutoWire.getQualifier();
				AutoWireTeamInstance teamInstance = state
						.getAppropriateTeamInstance(teamQualifier, teamType);

				// Build the Team (if necessary)
				OfficeFloorTeam officeFloorTeam = teamInstance.buildTeam(state,
						null);

				// Link managed object team to office floor team
				state.deployer.link(moTeam, officeFloorTeam);
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
				if (objectInstance.isInput) {

					// Obtain the Input Managed Object
					OfficeFloorInputManagedObject inputMo = objectInstance.inputManagedObject;
					if (inputMo == null) {
						// Must be handled Input Managed Object
						state.deployer.addIssue("May only depend on "
								+ OfficeFloorInputManagedObject.class
										.getSimpleName() + " "
								+ objectInstance.managedObjectName
								+ " if all of its flows are handled");
						return;
					}

					// Depends on input managed object
					state.deployer.link(dependency, inputMo);
					return;
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
			state.deployer
					.addIssue(ManagedObjectNode.TYPE
							+ " "
							+ this.managedObjectName
							+ " has no dependent managed object for auto-wiring dependency "
							+ dependency.getManagedObjectDependencyName()
							+ " (qualifier="
							+ dependencyAutoWire.getQualifier() + ", type="
							+ dependencyAutoWire.getType() + ")");
		}

		/*
		 * =============== ManagedObjectSourceWirerContext =================
		 */

		@Override
		public void setManagedObjectScope(ManagedObjectScope managedobjectScope) {
			this.managedObjectScope = managedobjectScope;
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
	 * Instance of an {@link AutoWireSupplier}.
	 */
	private static class AutoWireSupplierInstance implements
			AutoWireObjectSource {

		/**
		 * {@link AutoWireSupplier}.
		 */
		private final AutoWireSupplier autoWireSupplier;

		/**
		 * Flag indicating if the {@link OfficeFloorSupplier} has been built.
		 */
		private boolean isBuilt = false;

		/**
		 * Built {@link OfficeFloorSupplier}.
		 */
		private OfficeFloorSupplier officeFloorSupplier;

		/**
		 * Initiate.
		 * 
		 * @param autoWireSupplier
		 *            {@link AutoWireSupplier}.
		 */
		public AutoWireSupplierInstance(AutoWireSupplier autoWireSupplier) {
			this.autoWireSupplier = autoWireSupplier;
		}

		@Override
		public AutoWireObjectInstance[] sourceObjectInstances(
				OfficeFloorSourceContext context, OfficeFloorCompiler compiler) {

			// Load the supplier type
			SupplierType supplier = context.loadSupplierType(
					this.autoWireSupplier.getSupplierSourceClassName(),
					this.autoWireSupplier.getProperties());

			// Create an object instance for each supplied object
			SuppliedManagedObjectType[] suppliedManagedObjects = supplier
					.getSuppliedManagedObjectTypes();
			AutoWireObjectInstance[] suppliedObjectInstances = new AutoWireObjectInstance[suppliedManagedObjects.length];
			for (int i = 0; i < suppliedManagedObjects.length; i++) {
				SuppliedManagedObjectType suppliedManagedObject = suppliedManagedObjects[i];
				suppliedObjectInstances[i] = new AutoWireObjectInstance(
						suppliedManagedObject, this, compiler);
			}

			// Return the supplied object instances
			return suppliedObjectInstances;
		}

		/**
		 * Builds the {@link OfficeFloorSupplier} for this
		 * {@link AutoWireSupplierInstance}.
		 * 
		 * @param state
		 *            {@link AutoWireState}.
		 * @return {@link OfficeFloorSupplier}.
		 */
		public OfficeFloorSupplier buildSupplier(AutoWireState state) {

			// Determine if built
			if (!this.isBuilt) {

				// Only one attempt to build
				this.isBuilt = true;

				// Build the Supplier
				String supplierSourceClassName = this.autoWireSupplier
						.getSupplierSourceClassName();
				this.officeFloorSupplier = state.deployer.addSupplier(
						supplierSourceClassName, supplierSourceClassName);
				for (Property property : this.autoWireSupplier.getProperties()) {
					this.officeFloorSupplier.addProperty(property.getName(),
							property.getValue());
				}
			}

			// Return the Supplier
			return this.officeFloorSupplier;
		}
	}

	/**
	 * Instance of an {@link AutoWire} {@link Team}.
	 */
	private static class AutoWireTeamInstance {

		/**
		 * {@link AutoWireTeam}.
		 */
		private final AutoWireTeam autoWireTeam;

		/**
		 * Indicates if the {@link OfficeFloorTeam} has been built.
		 */
		private boolean isBuilt = false;

		/**
		 * {@link OfficeFloorTeam}.
		 */
		private OfficeFloorTeam officeFloorTeam = null;

		/**
		 * Initiate.
		 * 
		 * @param team
		 *            {@link AutoWireTeam}.
		 */
		public AutoWireTeamInstance(AutoWireTeam autoWireTeam) {
			this.autoWireTeam = autoWireTeam;
		}

		/**
		 * Builds the {@link OfficeFloorTeam} for this
		 * {@link AutoWireTeamInstance}.
		 * 
		 * @param state
		 *            {@link AutoWireState}.
		 * @param officeTeamAutoWire
		 *            {@link AutoWire} to load this {@link AutoWireTeamInstance}
		 *            as an {@link OfficeTeam}. May be <code>null</code> to not
		 *            load the {@link OfficeTeam}.
		 */
		public OfficeFloorTeam buildTeam(AutoWireState state,
				AutoWire officeTeamAutoWire) {

			// Only build once
			if (!this.isBuilt) {

				// Only one attempt to build
				this.isBuilt = true;

				// Build the team
				this.officeFloorTeam = state.deployer.addTeam(
						this.autoWireTeam.getTeamName(),
						this.autoWireTeam.getTeamSourceClassName());
				for (Property property : this.autoWireTeam.getProperties()) {
					this.officeFloorTeam.addProperty(property.getName(),
							property.getValue());
				}
			}

			// Load team to office if OfficeTeam auto-wire
			if (officeTeamAutoWire != null) {
				// Link Team to the Office
				OfficeTeam officeTeam = state.office
						.getDeployedOfficeTeam(officeTeamAutoWire
								.getQualifiedType());
				state.deployer.link(officeTeam, this.officeFloorTeam);
			}

			// Return the team
			return this.officeFloorTeam;
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