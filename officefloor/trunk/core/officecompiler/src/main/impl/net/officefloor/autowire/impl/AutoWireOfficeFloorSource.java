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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.officefloor.autowire.AutoWire;
import net.officefloor.autowire.AutoWireManagement;
import net.officefloor.autowire.AutoWireApplication;
import net.officefloor.autowire.AutoWireGovernance;
import net.officefloor.autowire.AutoWireObject;
import net.officefloor.autowire.AutoWireOfficeFloor;
import net.officefloor.autowire.AutoWireResponsibility;
import net.officefloor.autowire.AutoWireSection;
import net.officefloor.autowire.AutoWireSectionFactory;
import net.officefloor.autowire.AutoWireSupplier;
import net.officefloor.autowire.AutoWireTeam;
import net.officefloor.autowire.ManagedObjectSourceWirer;
import net.officefloor.autowire.ManagedObjectSourceWirerContext;
import net.officefloor.autowire.supplier.SupplierLoader;
import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.impl.OfficeFloorCompilerAdapter;
import net.officefloor.compile.impl.issues.FailCompilerIssues;
import net.officefloor.compile.managedobject.ManagedObjectDependencyType;
import net.officefloor.compile.managedobject.ManagedObjectType;
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
	 * {@link AutoWireContext} instances for the {@link AutoWireObject}
	 * instances.
	 */
	private final List<AutoWireContext> objectContexts = new LinkedList<AutoWireContext>();

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

		// Add the raw object
		this.objectContexts.add(new AutoWireContext(new AutoWireObjectImpl(
				this.compiler, null, properties, null, autoWiring), object));
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

		// Add the object context
		this.objectContexts.add(new AutoWireContext(object, null));

		// Return the auto wire object
		return object;
	}

	@Override
	public SupplierLoader getSupplierLoader() {
		// TODO implement AutoWireApplication.getSupplierLoader
		throw new UnsupportedOperationException(
				"TODO implement AutoWireApplication.getSupplierLoader");
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
		for (AutoWireContext objectContext : this.objectContexts) {
			for (AutoWire availableAutoWire : objectContext.autoWireObject
					.getAutoWiring()) {
				if (autoWire.equals(availableAutoWire)) {
					return true; // auto wire is available
				}
			}
		}

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
			 * This is to allow use within an application server as typically
			 * would use OfficeFloor configuration (not auto-wire) for
			 * OfficeFloor hosted application.
			 */
			team = deployer.addTeam("team",
					ProcessContextTeamSource.class.getName());
		}

		// TODO obtain the listing of all used AutoWireObjects

		// TODO filter to just the used AutoWireObjects

		// Load extension interfaces for object types
		for (AutoWireContext objectContext : this.objectContexts) {

			// Load the available auto-wiring
			AutoWire[] autoWiring = objectContext.autoWireObject
					.getAutoWiring();
			for (AutoWire autoWire : autoWiring) {
				this.officeSource.addAvailableAutoWire(autoWire);
			}

			// Obtain the managed object type for the object
			ManagedObjectType<?> managedObjectType = objectContext
					.getManagedObjectType(context);
			if (managedObjectType == null) {
				continue; // failed to obtain type so can not load
			}

			// Obtain the extension interfaces for object type
			Class<?>[] extensionInterfaces = managedObjectType
					.getExtensionInterfaces();

			// Load the extension interfaces for object type
			for (AutoWire autoWire : autoWiring) {
				this.officeSource.addOfficeObjectExtension(autoWire,
						extensionInterfaces);
			}
		}

		// Add the auto-wiring office
		DeployedOffice office = deployer.addDeployedOffice(OFFICE_NAME,
				this.officeSource, "auto-wire");

		// Link default team for office
		OfficeTeam officeTeam = office.getDeployedOfficeTeam("team");
		deployer.link(officeTeam, team);

		// Link team via object dependency responsibility
		Map<AutoWire, OfficeFloorTeam> teamAutoWiring = new HashMap<AutoWire, OfficeFloorTeam>();
		for (AutoWireTeam autoWireTeam : this.teams) {

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

				// Register the team for auto-wiring
				teamAutoWiring.put(
						autoWireResponsibility.getDependencyAutoWire(),
						responsibleTeam);
			}
		}

		// Load then link the managed object sources
		Map<AutoWire, OfficeFloorManagedObject> managedObjects = new HashMap<AutoWire, OfficeFloorManagedObject>();
		Map<AutoWire, OfficeFloorInputManagedObject> inputManagedObjects = new HashMap<AutoWire, OfficeFloorInputManagedObject>();
		Map<AutoWire, Integer> typeIndexes = new HashMap<AutoWire, Integer>();
		for (AutoWireContext objectContext : this.objectContexts) {
			objectContext.loadManagedObject(office, deployer, managedObjects,
					inputManagedObjects, typeIndexes);
		}
		for (AutoWireContext objectContext : this.objectContexts) {
			objectContext.linkManagedObject(office, deployer, context,
					managedObjects, inputManagedObjects, teamAutoWiring);
		}
	}

	/**
	 * {@link ManagedObjectSourceWirerContext} implementation.
	 */
	private class AutoWireContext implements ManagedObjectSourceWirerContext {

		/**
		 * {@link AutoWireObject}.
		 */
		private final AutoWireObject autoWireObject;

		/**
		 * Raw object.
		 */
		private final Object rawObject;

		/**
		 * Name of the {@link ManagedObject}.
		 */
		private String managedObjectName;

		/**
		 * Flag indicating is if {@link OfficeFloorInputManagedObject}.
		 */
		public boolean isInput = false;

		/**
		 * {@link AutoWireManagedObjectDependency} instances.
		 */
		public final List<AutoWireManagedObjectDependency> moDependencies = new LinkedList<AutoWireManagedObjectDependency>();

		/**
		 * {@link AutoWireManagedObjectFlow} instances.
		 */
		public final List<AutoWireManagedObjectFlow> moFlows = new LinkedList<AutoWireManagedObjectFlow>();

		/**
		 * {@link AutoWireManagedObjectTeam} instances.
		 */
		private final List<AutoWireManagedObjectTeam> moTeams = new LinkedList<AutoWireManagedObjectTeam>();

		/**
		 * {@link AutoWireTeam} instances.
		 */
		public final List<AutoWireTeam> teams = new LinkedList<AutoWireTeam>();

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
		 * Initiate.
		 * 
		 * @param autoWireObject
		 *            {@link AutoWireObject}.
		 * @param rawObject
		 *            Raw object.
		 */
		public AutoWireContext(AutoWireObject autoWireObject, Object rawObject) {
			this.autoWireObject = autoWireObject;
			this.rawObject = rawObject;
		}

		/**
		 * Obtains the {@link ManagedObjectType}.
		 * 
		 * @param context
		 *            {@link OfficeFloorSourceContext}.
		 * @return {@link ManagedObjectType} of <code>null</code> if issue
		 *         obtaining.
		 */
		public ManagedObjectType<?> getManagedObjectType(
				OfficeFloorSourceContext context) {

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

			// Return the managed object type
			return managedObjectType;
		}

		/**
		 * Loads the {@link ManagedObjectSource}.
		 * 
		 * @param office
		 *            {@link DeployedOffice}.
		 * @param deployer
		 *            {@link OfficeFloorDeployer}.
		 * @param managedObjects
		 *            {@link OfficeFloorManagedObject} instances by type to be
		 *            loaded.
		 * @param inputManagedObjects
		 *            {@link OfficeFloorInputManagedObject} instances by type to
		 *            be loaded.
		 * @param typeIndex
		 *            Indexes for the type to ensure unique naming.
		 * @throws ClassNotFoundException
		 *             If fails to adapt loading {@link ManagedObjectSource}.
		 */
		public void loadManagedObject(
				DeployedOffice office,
				OfficeFloorDeployer deployer,
				Map<AutoWire, OfficeFloorManagedObject> managedObjects,
				Map<AutoWire, OfficeFloorInputManagedObject> inputManagedObjects,
				Map<AutoWire, Integer> typeIndex) throws ClassNotFoundException {

			// Determine the details
			String managedObjectSourceClassName = this.autoWireObject
					.getManagedObjectSourceClassName();
			long timeout = this.autoWireObject.getTimeout();
			PropertyList properties = this.autoWireObject.getProperties();
			ManagedObjectSourceWirer wirer = this.autoWireObject
					.getManagedObjectSourceWirer();
			AutoWire[] autoWiring = this.autoWireObject.getAutoWiring();

			// Use first auto wiring for naming
			AutoWire firstAutoWire = autoWiring[0];
			String rawName = firstAutoWire.getQualifiedType();

			// Determine the managed object name
			Integer index = typeIndex.get(firstAutoWire);
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
			typeIndex.put(firstAutoWire, index);

			// Determine if raw object
			if (this.rawObject != null) {
				// Bind the raw object
				ManagedObjectSource<?, ?> singleton = OfficeFloorCompilerAdapter
						.createSingletonManagedObjectSource(
								AutoWireOfficeFloorSource.this.compiler,
								this.rawObject, autoWiring);
				this.managedObjectSource = deployer.addManagedObjectSource(
						this.managedObjectName, singleton);

			} else {
				// Bind the managed object source
				this.managedObjectSource = deployer.addManagedObjectSource(
						this.managedObjectName, managedObjectSourceClassName);

				// Specify time out to source the managed object
				this.managedObjectSource.setTimeout(timeout);

				// Configure properties for managed object source
				for (Property property : properties) {
					this.managedObjectSource.addProperty(property.getName(),
							property.getValue());
				}
			}

			// Bind to managing office
			deployer.link(this.managedObjectSource.getManagingOffice(), office);

			// Wire the managed object source
			if (wirer != null) {
				wirer.wire(this);
			}

			// Link in the objects
			for (AutoWire autoWire : autoWiring) {

				// Obtain the office object
				OfficeObject officeObject = office
						.getDeployedOfficeObject(autoWire.getQualifiedType());

				// Handle managed object
				if (this.isInput) {
					// Obtain the Input managed object
					OfficeFloorInputManagedObject inputMo = inputManagedObjects
							.get(autoWire);
					if (inputMo == null) {
						// Create and register the input managed object
						inputMo = deployer.addInputManagedObject(autoWire
								.getQualifiedType());
						inputManagedObjects.put(autoWire, inputMo);

						// Only first is specified as bound
						inputMo.setBoundOfficeFloorManagedObjectSource(this.managedObjectSource);

						// Link office object to input managed object
						deployer.link(officeObject, inputMo);
					}

					// Link source to input
					deployer.link(this.managedObjectSource, inputMo);

				} else {
					// Ensure only create the one managed object
					if (this.managedObject == null) {
						this.managedObject = this.managedObjectSource
								.addOfficeFloorManagedObject(
										this.managedObjectName,
										ManagedObjectScope.PROCESS);
					}

					// Link managed object to office object
					deployer.link(officeObject, this.managedObject);

					// Register the managed object for each type
					managedObjects.put(autoWire, this.managedObject);
				}
			}
		}

		/**
		 * Links the {@link ManagedObjectSource}.
		 * 
		 * @param office
		 *            {@link DeployedOffice}.
		 * @param deployer
		 *            {@link OfficeFloorDeployer}.
		 * @param context
		 *            {@link OfficeFloorSourceContext}.
		 * @param managedObjects
		 *            {@link OfficeFloorManagedObject} instances by their
		 *            {@link AutoWire}.
		 * @param inputManagedObjects
		 *            {@link OfficeFloorInputManagedObject} instances by their
		 *            {@link AutoWire}.
		 * @param teams
		 *            {@link OfficeFloorTeam} instances by their
		 *            {@link AutoWire}.
		 */
		public void linkManagedObject(
				DeployedOffice office,
				OfficeFloorDeployer deployer,
				OfficeFloorSourceContext context,
				Map<AutoWire, OfficeFloorManagedObject> managedObjects,
				Map<AutoWire, OfficeFloorInputManagedObject> inputManagedObjects,
				Map<AutoWire, OfficeFloorTeam> teams) {

			// Obtain the managed object dependencies from appropriate auto-wire
			Map<String, AutoWire> typeDependencies = new HashMap<String, AutoWire>();
			if (this.rawObject == null) {
				// Load the managed object type
				ManagedObjectType<?> moType = this
						.getManagedObjectType(context);
				if (moType != null) {
					// Have type so register its dependencies
					for (ManagedObjectDependencyType<?> typeDependency : moType
							.getDependencyTypes()) {

						// Obtain the dependency name
						String dependencyName = typeDependency
								.getDependencyName();

						// Create the dependency auto-wire
						String type = typeDependency.getDependencyType()
								.getName();
						String qualifier = typeDependency.getTypeQualifier();
						AutoWire dependencyAutoWire = new AutoWire(qualifier,
								type);

						// Register the appropriate auto-wire for the dependency
						typeDependencies
								.put(dependencyName, dependencyAutoWire);
					}
				}
			}

			// Link the dependencies (from mapping)
			for (AutoWireManagedObjectDependency autoWireDependency : this.moDependencies) {

				// Obtain the dependency
				ManagedObjectDependency dependency;
				if (this.isInput) {
					dependency = this.managedObjectSource
							.getInputManagedObjectDependency(autoWireDependency.dependencyName);
				} else {
					dependency = this.managedObject
							.getManagedObjectDependency(autoWireDependency.dependencyName);
				}

				// Link the dependency
				this.linkDependency(dependency,
						autoWireDependency.dependencyAutoWire, deployer,
						managedObjects, inputManagedObjects);

				// Remove the dependency from type (so not added later)
				typeDependencies.remove(autoWireDependency.dependencyName);
			}

			// Link the dependencies (from type)
			for (String dependencyName : typeDependencies.keySet()) {

				// Obtain the auto-wire of dependency
				AutoWire dependencyAutoWire = typeDependencies
						.get(dependencyName);

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
				this.linkDependency(dependency, dependencyAutoWire, deployer,
						managedObjects, inputManagedObjects);
			}

			// Link flows
			for (AutoWireManagedObjectFlow flow : this.moFlows) {

				// Obtain the managed object flow
				ManagedObjectFlow moFlow = this.managedObjectSource
						.getManagedObjectFlow(flow.managedObjectSourceFlowName);

				// Obtain the office input
				DeployedOfficeInput officeInput = office
						.getDeployedOfficeInput(flow.sectionName,
								flow.sectionInputName);

				// Link managed object flow to office input
				deployer.link(moFlow, officeInput);
			}

			// Link teams
			Set<AutoWire> teamAutoWiring = teams.keySet();
			for (AutoWireManagedObjectTeam team : this.moTeams) {

				// Obtain the managed object team
				String teamName = team.teamName;
				ManagedObjectTeam moTeam = this.managedObjectSource
						.getManagedObjectTeam(teamName);

				// Obtain the appropriate auto-wire for the team
				String teamType = team.teamAutoWire.getType();
				String teamQualifier = team.teamAutoWire.getQualifier();
				AutoWire appropriateAutoWire = getAppropriateAutoWire(
						teamQualifier, teamType, teamAutoWiring);
				if (appropriateAutoWire == null) {
					// No appropriate team for auto-wiring
					deployer.addIssue(
							"No " + Team.class.getSimpleName()
									+ " for auto-wiring "
									+ ManagedObjectTeam.class.getSimpleName()
									+ " " + teamName + " (qualifier="
									+ teamQualifier + ", type=" + teamType
									+ ")", AssetType.MANAGED_OBJECT,
							this.managedObjectName);
					continue; // can not link team
				}

				// Obtain the team
				OfficeFloorTeam officeFloorTeam = teams
						.get(appropriateAutoWire);

				// Link managed object team to office floor team
				deployer.link(moTeam, officeFloorTeam);
			}

			// Build teams
			for (AutoWireTeam team : this.teams) {

				// Obtain the managed object team
				ManagedObjectTeam moTeam = this.managedObjectSource
						.getManagedObjectTeam(team.getTeamName());

				// Add the team
				OfficeFloorTeam officeFloorTeam = deployer.addTeam(
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
				deployer.link(moTeam, officeFloorTeam);
			}
		}

		/**
		 * Links the {@link ManagedObjectDependency}.
		 * 
		 * @param dependency
		 *            {@link ManagedObjectDependency}.
		 * @param dependencyAutoWire
		 *            {@link AutoWire} of {@link ManagedObjectDependency}.
		 * @param deployer
		 *            {@link OfficeFloorDeployer}.
		 * @param managedObjects
		 *            {@link OfficeFloorManagedObject} instances by their
		 *            {@link AutoWire}.
		 * @param inputManagedObjects
		 *            {@link OfficeFloorInputManagedObject} instances by their
		 *            {@link AutoWire}.
		 */
		private void linkDependency(ManagedObjectDependency dependency,
				AutoWire dependencyAutoWire, OfficeFloorDeployer deployer,
				Map<AutoWire, OfficeFloorManagedObject> managedObjects,
				Map<AutoWire, OfficeFloorInputManagedObject> inputManagedObjects) {

			// Create the listing of available auto-wiring
			List<AutoWire> availableAutoWiring = new ArrayList<AutoWire>(
					managedObjects.size() + inputManagedObjects.size());
			availableAutoWiring.addAll(managedObjects.keySet());
			availableAutoWiring.addAll(inputManagedObjects.keySet());

			// Obtain the appropriate auto-wire
			AutoWire appropriateAutoWire = getAppropriateAutoWire(
					dependencyAutoWire.getQualifier(),
					dependencyAutoWire.getType(), availableAutoWiring);

			// Only attempt to link if have appropriate auto-wire
			if (appropriateAutoWire != null) {

				// Try first for the managed object
				OfficeFloorManagedObject mo = managedObjects
						.get(appropriateAutoWire);
				if (mo != null) {
					deployer.link(dependency, mo);
					return;
				}

				// Try next for input managed object
				OfficeFloorInputManagedObject inputMo = inputManagedObjects
						.get(appropriateAutoWire);
				if (inputMo != null) {
					deployer.link(dependency, inputMo);
					return;
				}
			}

			// As here, no managed object for dependency
			deployer.addIssue(
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
			PropertyList properties = AutoWireOfficeFloorSource.this.compiler
					.createPropertyList();

			// Register the team mapping
			AutoWireTeam team = new AutoWireTeamImpl(
					AutoWireOfficeFloorSource.this.compiler,
					managedObjectSourceTeamName, teamSourceClassName,
					properties);
			this.teams.add(team);

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
	 * Auto-wire {@link ManagedObject} dependency.
	 */
	private class AutoWireManagedObjectDependency {

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
	private class AutoWireManagedObjectFlow {

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
	private class AutoWireManagedObjectTeam {

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