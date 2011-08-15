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

package net.officefloor.plugin.autowire;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.officefloor.compile.OfficeFloorCompiler;
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
import net.officefloor.compile.spi.section.source.SectionSource;
import net.officefloor.frame.api.build.OfficeFloorIssues.AssetType;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.impl.spi.team.ProcessContextTeamSource;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.spi.team.source.TeamSource;
import net.officefloor.model.impl.officefloor.OfficeFloorModelOfficeFloorSource;
import net.officefloor.plugin.threadlocal.ThreadLocalDelegateManagedObjectSource;
import net.officefloor.plugin.threadlocal.ThreadLocalDelegateOfficeFloorSource;
import net.officefloor.plugin.threadlocal.ThreadLocalDelegateOfficeSource;

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
		this.officeSource = new AutoWireOfficeSource(this.compiler);

		// Override the OfficeFloorSource
		ThreadLocalDelegateOfficeFloorSource.bindDelegate(this, this.compiler);
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
	public <S extends SectionSource> AutoWireSection addSection(
			String sectionName, Class<S> sectionSourceClass,
			String sectionLocation) {
		return this.officeSource.addSection(sectionName, sectionSourceClass,
				sectionLocation);
	}

	@Override
	public <S extends SectionSource, A extends AutoWireSection> A addSection(
			String sectionName, Class<S> sectionSourceClass,
			String sectionLocation, AutoWireSectionFactory<A> sectionFactory) {
		return this.officeSource.addSection(sectionName, sectionSourceClass,
				sectionLocation, sectionFactory);
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
	public void addObject(Object object, Class<?>... objectTypes) {

		// Default the object type if not provided
		if ((objectTypes == null) || (objectTypes.length == 0)) {
			objectTypes = new Class<?>[] { object.getClass() };
		}

		// Add the raw object
		this.objectContexts.add(new AutoWireContext(new AutoWireObjectImpl(
				this.compiler, null, null, null, objectTypes), object));
	}

	@Override
	public <D extends Enum<D>, F extends Enum<F>, S extends ManagedObjectSource<D, F>> AutoWireObject addManagedObject(
			Class<S> managedObjectSourceClass, ManagedObjectSourceWirer wirer,
			Class<?>... objectTypes) {

		// Ensure have object types
		if ((objectTypes == null) || (objectTypes.length == 0)) {
			throw new IllegalArgumentException(
					"Must provide at least one object type");
		}

		// Create the properties
		PropertyList properties = this.compiler.createPropertyList();

		// Create the auto wire object
		AutoWireObject object = new AutoWireObjectImpl(this.compiler,
				managedObjectSourceClass, properties, wirer, objectTypes);

		// Add the object context
		this.objectContexts.add(new AutoWireContext(object, null));

		// Return the auto wire object
		return object;
	}

	@Override
	public boolean isObjectAvailable(Class<?> objectType) {

		// Determine if the object type is available
		for (AutoWireContext objectContext : this.objectContexts) {
			for (Class<?> availableType : objectContext.autoWireObject
					.getObjectTypes()) {
				if (objectType.equals(availableType)) {
					return true; // object type is available
				}
			}
		}

		// As here, object type not available
		return false;
	}

	@Override
	public <T extends TeamSource> AutoWireTeam assignTeam(
			Class<T> teamSourceClass, Class<?>... objectTypes) {

		// Must have object types
		if ((objectTypes == null) || (objectTypes.length == 0)) {
			throw new IllegalArgumentException(
					"Must provide at least one object type for team to be responsible");
		}

		// Determine name of team
		String teamName = "team-" + objectTypes[0].getName();

		// Create the responsibilities
		AutoWireResponsibility[] responsibilities = new AutoWireResponsibility[objectTypes.length];
		for (int i = 0; i < objectTypes.length; i++) {
			responsibilities[i] = this.officeSource
					.addResponsibility(objectTypes[i]);
		}

		// Create the properties
		PropertyList properties = this.compiler.createPropertyList();

		// Create and add the team
		AutoWireTeam team = new AutoWireTeamImpl(this.compiler, teamName,
				teamSourceClass, properties, responsibilities);
		this.teams.add(team);

		// Return the team
		return team;
	}

	@Override
	public <T extends TeamSource> AutoWireTeam assignDefaultTeam(
			Class<T> teamSourceClass) {

		// Create the properties
		PropertyList properties = this.compiler.createPropertyList();

		// Create the default team
		this.defaultTeam = new AutoWireTeamImpl(this.compiler, "team",
				teamSourceClass, properties);

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
		return AutoWireAdministration.createAutoWireOfficeFloor(officeFloor,
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
			team = deployer.addTeam("team", this.defaultTeam
					.getTeamSourceClass().getName());
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

		// Add the auto-wiring office
		DeployedOffice office = ThreadLocalDelegateOfficeSource.bindDelegate(
				OFFICE_NAME, this.officeSource, deployer);

		// Link default team for office
		OfficeTeam officeTeam = office.getDeployedOfficeTeam("team");
		deployer.link(officeTeam, team);

		// Link team via object dependency responsibility
		for (AutoWireTeam autoWireTeam : this.teams) {

			// Add the responsible team
			OfficeFloorTeam responsibleTeam = deployer
					.addTeam(autoWireTeam.getTeamName(), autoWireTeam
							.getTeamSourceClass().getName());
			for (Property property : autoWireTeam.getProperties()) {
				responsibleTeam.addProperty(property.getName(),
						property.getValue());
			}

			// Link responsible team to its responsibilities
			for (AutoWireResponsibility autoWireResponsibility : autoWireTeam
					.getResponsibilities()) {
				OfficeTeam responsibility = office
						.getDeployedOfficeTeam(autoWireResponsibility
								.getOfficeTeamName());
				deployer.link(responsibility, responsibleTeam);
			}
		}

		// Load then link the managed object sources
		Map<Class<?>, OfficeFloorManagedObject> managedObjects = new HashMap<Class<?>, OfficeFloorManagedObject>();
		Map<Class<?>, OfficeFloorInputManagedObject> inputManagedObjects = new HashMap<Class<?>, OfficeFloorInputManagedObject>();
		Map<Class<?>, Integer> typeIndexes = new HashMap<Class<?>, Integer>();
		for (AutoWireContext objectContext : this.objectContexts) {
			objectContext.loadManagedObject(office, deployer, managedObjects,
					inputManagedObjects, typeIndexes);
		}
		for (AutoWireContext objectContext : this.objectContexts) {
			objectContext.linkManagedObject(office, deployer, context,
					managedObjects, inputManagedObjects);
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
		 * {@link AutoWireDependency} instances.
		 */
		public final List<AutoWireDependency> dependencies = new LinkedList<AutoWireDependency>();

		/**
		 * {@link AutoWireFlow} instances.
		 */
		public final List<AutoWireFlow> flows = new LinkedList<AutoWireFlow>();

		/**
		 * {@link AutoWireTeam} instances.
		 */
		public final List<AutoWireTeam> teams = new LinkedList<AutoWireTeam>();

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
		 */
		public void loadManagedObject(
				DeployedOffice office,
				OfficeFloorDeployer deployer,
				Map<Class<?>, OfficeFloorManagedObject> managedObjects,
				Map<Class<?>, OfficeFloorInputManagedObject> inputManagedObjects,
				Map<Class<?>, Integer> typeIndex) {

			// Determine the details
			Class<?> managedObjectSourceClass = this.autoWireObject
					.getManagedObjectSourceClass();
			long timeout = this.autoWireObject.getTimeout();
			PropertyList properties = this.autoWireObject.getProperties();
			ManagedObjectSourceWirer wirer = this.autoWireObject
					.getManagedObjectSourceWirer();
			Class<?>[] objectTypes = this.autoWireObject.getObjectTypes();

			// Use first object type for naming
			Class<?> firstObjectType = objectTypes[0];
			String rawName = firstObjectType.getName();

			// Determine the managed object name
			Integer index = typeIndex.get(firstObjectType);
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
			typeIndex.put(firstObjectType, index);

			// Determine if raw object
			if (this.rawObject != null) {
				// Bind the raw object
				SingletonManagedObjectSource singleton = new SingletonManagedObjectSource(
						this.rawObject);
				this.managedObjectSource = ThreadLocalDelegateManagedObjectSource
						.bindDelegate(managedObjectName, singleton, deployer);

			} else {
				// Bind the managed object source
				this.managedObjectSource = deployer.addManagedObjectSource(
						this.managedObjectName,
						managedObjectSourceClass.getName());

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
			for (Class<?> objectType : objectTypes) {

				// Obtain the office object
				OfficeObject officeObject = office
						.getDeployedOfficeObject(objectType.getName());

				// Handle managed object
				if (this.isInput) {
					// Obtain the Input managed object
					OfficeFloorInputManagedObject inputMo = inputManagedObjects
							.get(objectType);
					if (inputMo == null) {
						// Create and register the input managed object
						inputMo = deployer.addInputManagedObject(objectType
								.getName());
						inputManagedObjects.put(objectType, inputMo);

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
					managedObjects.put(objectType, this.managedObject);
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
		 *            {@link OfficeFloorManagedObject} instances by type.
		 * @param inputManagedObjects
		 *            {@link OfficeFloorInputManagedObject} instances by type.
		 */
		public void linkManagedObject(DeployedOffice office,
				OfficeFloorDeployer deployer, OfficeFloorSourceContext context,
				Map<Class<?>, OfficeFloorManagedObject> managedObjects,
				Map<Class<?>, OfficeFloorInputManagedObject> inputManagedObjects) {

			// Obtain the managed object dependencies from type
			Map<String, Class<?>> typeDependencies = new HashMap<String, Class<?>>();
			if (this.rawObject == null) {
				// Load the managed object type
				ManagedObjectType<?> moType = context
						.loadManagedObjectType(this.autoWireObject
								.getManagedObjectSourceClass().getName(),
								this.autoWireObject.getProperties());
				if (moType != null) {
					// Have type so register its dependencies
					for (ManagedObjectDependencyType<?> typeDependency : moType
							.getDependencyTypes()) {
						typeDependencies.put(
								typeDependency.getDependencyName(),
								typeDependency.getDependencyType());
					}
				}
			}

			// Link the dependencies (from mapping)
			for (AutoWireDependency autoWireDependency : this.dependencies) {

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
						autoWireDependency.dependencyType, deployer,
						managedObjects, inputManagedObjects);

				// Remove the dependency from type (so not added later)
				typeDependencies.remove(autoWireDependency.dependencyName);
			}

			// Link the dependencies (from type)
			for (String dependencyName : typeDependencies.keySet()) {

				// Obtain the type of dependency
				Class<?> dependencyType = typeDependencies.get(dependencyName);

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
				this.linkDependency(dependency, dependencyType, deployer,
						managedObjects, inputManagedObjects);
			}

			// Link flows
			for (AutoWireFlow flow : this.flows) {

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
			for (AutoWireTeam team : this.teams) {

				// Obtain the managed object team
				ManagedObjectTeam moTeam = this.managedObjectSource
						.getManagedObjectTeam(team.getTeamName());

				// Add the team
				OfficeFloorTeam officeFloorTeam = deployer.addTeam(
						this.managedObjectSource
								.getOfficeFloorManagedObjectSourceName()
								+ "-"
								+ team.getTeamName(), team.getTeamSourceClass()
								.getName());
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
		 * @param dependencyType
		 *            Type of {@link ManagedObjectDependency}.
		 * @param deployer
		 *            {@link OfficeFloorDeployer}.
		 * @param managedObjects
		 *            {@link OfficeFloorManagedObject} instances by type.
		 * @param inputManagedObjects
		 *            {@link OfficeFloorInputManagedObject} instances by type.
		 */
		private void linkDependency(ManagedObjectDependency dependency,
				Class<?> dependencyType, OfficeFloorDeployer deployer,
				Map<Class<?>, OfficeFloorManagedObject> managedObjects,
				Map<Class<?>, OfficeFloorInputManagedObject> inputManagedObjects) {

			// Try first for the managed object
			OfficeFloorManagedObject mo = managedObjects.get(dependencyType);
			if (mo != null) {
				deployer.link(dependency, mo);
				return;
			}

			// Try next for input managed object
			OfficeFloorInputManagedObject inputMo = inputManagedObjects
					.get(dependencyType);
			if (inputMo != null) {
				deployer.link(dependency, inputMo);
				return;
			}

			// No managed object for dependency
			deployer.addIssue("No dependency managed object for type "
					+ dependencyType.getName(), AssetType.MANAGED_OBJECT,
					this.managedObjectName);
		}

		/*
		 * =============== ManagedObjectSourceWirerContext =================
		 */

		@Override
		public void setInput(boolean isInput) {
			this.isInput = isInput;
		}

		@Override
		public void mapDependency(String dependencyName, Class<?> type) {
			this.dependencies.add(new AutoWireDependency(dependencyName, type));
		}

		@Override
		public void mapFlow(String managedObjectSourceFlowName,
				String sectionName, String sectionInputName) {
			this.flows.add(new AutoWireFlow(managedObjectSourceFlowName,
					sectionName, sectionInputName));
		}

		@Override
		public <S extends TeamSource> AutoWireTeam mapTeam(
				String managedObjectSourceTeamName, Class<S> teamSourceClass) {

			// Create the properties for the team
			PropertyList properties = AutoWireOfficeFloorSource.this.compiler
					.createPropertyList();

			// Register the team mapping
			AutoWireTeam team = new AutoWireTeamImpl(
					AutoWireOfficeFloorSource.this.compiler,
					managedObjectSourceTeamName, teamSourceClass, properties);
			this.teams.add(team);

			// Return the team
			return team;
		}
	}

	/**
	 * Auto-wire dependency.
	 */
	private class AutoWireDependency {

		/**
		 * Name of the dependency.
		 */
		public final String dependencyName;

		/**
		 * Type of the dependency.
		 */
		public final Class<?> dependencyType;

		/**
		 * Initiate.
		 * 
		 * @param dependencyName
		 *            Name of the dependency.
		 * @param dependencyType
		 *            Type of the dependency.
		 */
		public AutoWireDependency(String dependencyName, Class<?> dependencyType) {
			this.dependencyName = dependencyName;
			this.dependencyType = dependencyType;
		}
	}

	/**
	 * Auto-wire {@link Flow}.
	 */
	private class AutoWireFlow {

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
		public AutoWireFlow(String managedObjectSourceFlowName,
				String sectionName, String sectionInputName) {
			this.managedObjectSourceFlowName = managedObjectSourceFlowName;
			this.sectionName = sectionName;
			this.sectionInputName = sectionInputName;
		}
	}

}