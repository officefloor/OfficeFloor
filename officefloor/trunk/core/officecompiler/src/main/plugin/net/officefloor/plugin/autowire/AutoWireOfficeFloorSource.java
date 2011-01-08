/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2009 Daniel Sagenschneider
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
import net.officefloor.compile.spi.section.SectionInput;
import net.officefloor.compile.spi.section.SectionOutput;
import net.officefloor.compile.spi.section.source.SectionSource;
import net.officefloor.frame.api.build.OfficeFloorIssues.AssetType;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.manage.TaskManager;
import net.officefloor.frame.api.manage.WorkManager;
import net.officefloor.frame.impl.spi.team.ProcessContextTeam;
import net.officefloor.frame.impl.spi.team.ProcessContextTeamSource;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
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
 * To allow auto-wiring this implementation provides facades over the the
 * complexities of more advanced {@link OfficeFloor} configuration to allow
 * simpler configuration. It is anticipated in majority of cases that this will
 * be adequate for most applications. Should however more flexibility be
 * required then please use the {@link OfficeFloorModelOfficeFloorSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class AutoWireOfficeFloorSource extends AbstractOfficeFloorSource {

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
	 * {@link AutoWireContext} instances.
	 */
	private final List<AutoWireContext> objectContexts = new LinkedList<AutoWireContext>();

	/**
	 * Opened {@link OfficeFloor}.
	 */
	private OfficeFloor officeFloor = null;

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
		this(OfficeFloorCompiler.newOfficeFloorCompiler());
	}

	/**
	 * <p>
	 * Obtains the {@link OfficeFloorCompiler} being used.
	 * <p>
	 * This allows manipulation of the {@link OfficeFloorCompiler} before
	 * auto-wiring to compile and open the {@link OfficeFloor}.
	 * 
	 * @return {@link OfficeFloorCompiler} being used.
	 */
	public OfficeFloorCompiler getOfficeFloorCompiler() {
		return this.compiler;
	}

	/**
	 * Adds an {@link OfficeSection}.
	 * 
	 * @param sectionName
	 *            Name of the {@link OfficeSection}.
	 * @param sectionSourceClass
	 *            {@link SectionSource} class.
	 * @param sectionLocation
	 *            {@link OfficeSection} location.
	 * @return {@link AutoWireSection} to configure properties and link flows.
	 */
	public <S extends SectionSource> AutoWireSection addSection(
			String sectionName, Class<S> sectionSourceClass,
			String sectionLocation) {
		return this.officeSource.addSection(sectionName, sectionSourceClass,
				sectionLocation);
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
		this.officeSource.link(sourceSection, sourceOutputName, targetSection,
				targetInputName);
	}

	/**
	 * Adds a raw object for dependency injection for the particular type.
	 * 
	 * @param type
	 *            Type that the Object is to provide dependency injection via
	 *            auto-wiring.
	 * @param object
	 *            Object implementing the type to be dependency injected.
	 */
	public <T, O extends T> void addObject(Class<T> type, O object) {

		// Ensure not already added
		this.ensureObjectTypeNotAdded(type);

		// Add the raw object
		this.objectContexts.add(new AutoWireContext(type, object));
	}

	/**
	 * Adds a {@link ManagedObjectSource} for dependency injection.
	 * 
	 * @param type
	 *            Type that the {@link ManagedObjectSource} is to provide
	 *            dependency injection via auto-wiring.
	 * @param sourceClass
	 *            {@link ManagedObjectSource} class.
	 * @param wirer
	 *            {@link ManagedObjectSourceWirer} to assist in configuring the
	 *            {@link ManagedObjectSource}. May be <code>null</code> if no
	 *            assistance is required.
	 * @return {@link PropertyList} for the {@link ManagedObjectSource}.
	 */
	public <D extends Enum<D>, F extends Enum<F>, S extends ManagedObjectSource<D, F>> PropertyList addObject(
			Class<?> type, Class<S> sourceClass, ManagedObjectSourceWirer wirer) {

		// Ensure not already added
		this.ensureObjectTypeNotAdded(type);

		// Create the properties
		PropertyList properties = this.compiler.createPropertyList();

		// Add the object context
		this.objectContexts.add(new AutoWireContext(type, sourceClass,
				properties, wirer));

		// Return the properties for the managed object source
		return properties;
	}

	/**
	 * Ensures the object type is not already added.
	 * 
	 * @param type
	 *            Type of the object for the {@link ManagedObjectSource}.
	 */
	private void ensureObjectTypeNotAdded(Class<?> type) {
		for (AutoWireContext objectContext : this.objectContexts) {
			if (objectContext.type.equals(type)) {
				// Obtain already added
				throw new IllegalStateException("Object of type "
						+ type.getName() + " already added");
			}
		}
	}

	/**
	 * Opens the {@link OfficeFloor}.
	 * 
	 * @return {@link OfficeFloor}.
	 * @throws Exception
	 *             If fails to open the {@link OfficeFloor}.
	 */
	public OfficeFloor openOfficeFloor() throws Exception {

		// Lazy open the OfficeFloor
		if (this.officeFloor == null) {
			this.officeFloor = this.compiler.compile("auto-wire");
		}

		// Ensure the OfficeFloor is open
		if (this.officeFloor != null) {
			this.officeFloor.openOfficeFloor();
		}

		// Return the OfficeFloor
		return this.officeFloor;
	}

	/**
	 * <p>
	 * Invokes a {@link Task} on the {@link OfficeFloor}.
	 * <p>
	 * Should the {@link OfficeFloor} not be open, it is opened before invoking
	 * the {@link Task}. Please note however the {@link OfficeFloor} will not be
	 * re-opened after being closed.
	 * 
	 * @param workName
	 *            Name of the {@link Work}.
	 * @param taskName
	 *            Name of the {@link Task}.
	 * @param parameter
	 *            Parameter for the {@link Task}. May be <code>null</code>.
	 * @throws Exception
	 *             If fails invoking the {@link Task}.
	 */
	public void invokeTask(String workName, String taskName, Object parameter)
			throws Exception {

		// Ensure OfficeFloor is open
		if (this.officeFloor == null) {
			this.openOfficeFloor();

			// Ensure opened
			if (this.officeFloor == null) {
				throw new IllegalStateException("Failed opening OfficeFloor");
			}
		}

		// Obtain the Task
		Office office = this.officeFloor.getOffice(OFFICE_NAME);
		WorkManager workManager = office.getWorkManager(workName);
		TaskManager taskManager = workManager.getTaskManager(taskName);

		// Invoke the task
		ProcessContextTeam.doTask(taskManager, parameter);
	}

	/**
	 * Closes the {@link OfficeFloor}.
	 */
	public void closeOfficeFloor() {

		// Ensure open
		if (this.officeFloor == null) {
			return;
		}

		try {
			// Close
			this.officeFloor.closeOfficeFloor();

		} finally {
			// Ensure release OfficeFloor
			this.officeFloor = null;
		}
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

		/*
		 * Create the Team to always use invoking thread. This is to allow use
		 * within an application server as typically would use OfficeFloor
		 * configuration (not auto-wire) for OfficeFloor hosted application.
		 */
		OfficeFloorTeam team = deployer.addTeam("team",
				ProcessContextTeamSource.class.getName());

		// Add the auto-wiring office
		DeployedOffice office = ThreadLocalDelegateOfficeSource.bindDelegate(
				OFFICE_NAME, this.officeSource, deployer);

		// Link team for office
		OfficeTeam officeTeam = office.getDeployedOfficeTeam("team");
		deployer.link(officeTeam, team);

		// Load then link the managed object sources
		Map<Class<?>, OfficeFloorManagedObject> managedObjects = new HashMap<Class<?>, OfficeFloorManagedObject>();
		Map<Class<?>, OfficeFloorInputManagedObject> inputManagedObjects = new HashMap<Class<?>, OfficeFloorInputManagedObject>();
		for (AutoWireContext objectContext : this.objectContexts) {
			objectContext.loadManagedObject(office, deployer, managedObjects,
					inputManagedObjects);
		}
		for (AutoWireContext objectContext : this.objectContexts) {
			objectContext.linkManagedObject(office, deployer, managedObjects,
					inputManagedObjects);
		}
	}

	/**
	 * {@link ManagedObjectSourceWirerContext} implementation.
	 */
	private class AutoWireContext implements ManagedObjectSourceWirerContext {

		/**
		 * Type of the object.
		 */
		public final Class<?> type;

		/**
		 * Raw object.
		 */
		public final Object rawObject;

		/**
		 * {@link ManagedObjectSource} class.
		 */
		public final Class<?> managedObjectSourceClass;

		/**
		 * {@link PropertyList} for the {@link ManagedObjectSource}.
		 */
		public final PropertyList properties;

		/**
		 * {@link ManagedObjectSourceWirer}.
		 */
		public final ManagedObjectSourceWirer wirer;

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
		 * @param type
		 *            Type of the object.
		 * @param managedObjectSourceClass
		 *            {@link ManagedObjectSource} class.
		 * @param properties
		 *            {@link PropertyList} for the {@link ManagedObjectSource}.
		 * @param wirer
		 *            {@link ManagedObjectSourceWirer}.
		 */
		public AutoWireContext(Class<?> type,
				Class<?> managedObjectSourceClass, PropertyList properties,
				ManagedObjectSourceWirer wirer) {
			this.type = type;
			this.rawObject = null;
			this.managedObjectSourceClass = managedObjectSourceClass;
			this.properties = properties;
			this.wirer = wirer;
		}

		/**
		 * Initiate.
		 * 
		 * @param type
		 *            Type of the object.
		 * @param rawObject
		 *            Raw object.
		 */
		public AutoWireContext(Class<?> type, Object rawObject) {
			this.type = type;
			this.rawObject = rawObject;
			this.managedObjectSourceClass = null;
			this.properties = null;
			this.wirer = null;
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
		 */
		public void loadManagedObject(DeployedOffice office,
				OfficeFloorDeployer deployer,
				Map<Class<?>, OfficeFloorManagedObject> managedObjects,
				Map<Class<?>, OfficeFloorInputManagedObject> inputManagedObjects) {

			// Determine if raw object
			if (this.rawObject != null) {
				// Bind the raw object
				SingletonManagedObjectSource singleton = new SingletonManagedObjectSource(
						this.rawObject);
				this.managedObjectSource = ThreadLocalDelegateManagedObjectSource
						.bindDelegate(this.type.getName(), singleton, deployer);

			} else {
				// Bind the managed object source
				this.managedObjectSource = deployer.addManagedObjectSource(
						this.type.getName(),
						this.managedObjectSourceClass.getName());
				for (Property property : this.properties) {
					this.managedObjectSource.addProperty(property.getName(),
							property.getValue());
				}
			}

			// Bind to managing office
			deployer.link(this.managedObjectSource.getManagingOffice(), office);

			// Wire the managed object source
			if (this.wirer != null) {
				this.wirer.wire(this);
			}

			// Obtain the office object
			OfficeObject officeObject = office
					.getDeployedOfficeObject(this.type.getName());

			// Handle managed object
			if (this.isInput) {
				// Input managed object
				OfficeFloorInputManagedObject inputMo = deployer
						.addInputManagedObject(this.type.getName());

				// Link source to input
				deployer.link(this.managedObjectSource, inputMo);
				inputMo.setBoundOfficeFloorManagedObjectSource(this.managedObjectSource);

				// Link input to office object
				deployer.link(officeObject, inputMo);

				// Register the input managed object
				inputManagedObjects.put(this.type, inputMo);

			} else {
				// Managed object
				this.managedObject = this.managedObjectSource
						.addOfficeFloorManagedObject(this.type.getName(),
								ManagedObjectScope.PROCESS);

				// Link managed object to office object
				deployer.link(officeObject, this.managedObject);

				// Register the managed object
				managedObjects.put(this.type, this.managedObject);
			}
		}

		/**
		 * Links the {@link ManagedObjectSource}.
		 * 
		 * @param office
		 *            {@link DeployedOffice}.
		 * @param deployer
		 *            {@link OfficeFloorDeployer}.
		 * @param managedObjects
		 *            {@link OfficeFloorManagedObject} instances by type.
		 * @param inputManagedObjects
		 *            {@link OfficeFloorInputManagedObject} instances by type.
		 */
		public void linkManagedObject(DeployedOffice office,
				OfficeFloorDeployer deployer,
				Map<Class<?>, OfficeFloorManagedObject> managedObjects,
				Map<Class<?>, OfficeFloorInputManagedObject> inputManagedObjects) {

			// Link dependencies
			if (this.isInput) {
				// Link the dependencies for the input managed object
				for (AutoWireDependency dependency : this.dependencies) {

					// Obtain the dependency
					ManagedObjectDependency inputDependency = this.managedObjectSource
							.getInputManagedObjectDependency(dependency.dependencyName);

					// Link the dependency
					this.linkDependency(inputDependency,
							dependency.dependencyType, deployer,
							managedObjects, inputManagedObjects);
				}

			} else {
				// Link the dependencies for the managed object
				for (AutoWireDependency dependency : this.dependencies) {

					// Obtain the dependency
					ManagedObjectDependency moDependency = this.managedObject
							.getManagedObjectDependency(dependency.dependencyName);

					// Link the dependency
					this.linkDependency(moDependency,
							dependency.dependencyType, deployer,
							managedObjects, inputManagedObjects);
				}
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
						.getManagedObjectTeam(team.managedObjectSourceTeamName);

				// Add the team
				OfficeFloorTeam officeFloorTeam = deployer.addTeam(
						this.type.getName() + "-"
								+ team.managedObjectSourceTeamName,
						team.teamSourceClass.getName());
				for (Property property : team.properties) {
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
					this.type.getName());
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
		public <S extends TeamSource> PropertyList mapTeam(
				String managedObjectSourceTeamName, Class<S> teamSourceClass) {

			// Create the properties for the team
			PropertyList properties = AutoWireOfficeFloorSource.this.compiler
					.createPropertyList();

			// Register the team mapping
			this.teams.add(new AutoWireTeam(managedObjectSourceTeamName,
					teamSourceClass, properties));

			// Return the team properties
			return properties;
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

	/**
	 * Auto-wire {@link ManagedObjectTeam}.
	 */
	private class AutoWireTeam {

		/**
		 * Name of the {@link ManagedObjectTeam}.
		 */
		public final String managedObjectSourceTeamName;

		/**
		 * {@link TeamSource} class.
		 */
		public final Class<?> teamSourceClass;

		/**
		 * {@link PropertyList}.
		 */
		public final PropertyList properties;

		/**
		 * Initiate.
		 * 
		 * @param managedObjectSourceTeamName
		 *            Name of the {@link ManagedObjectTeam}.
		 * @param teamSourceClass
		 *            {@link TeamSource} class.
		 * @param properties
		 *            {@link PropertyList}.
		 */
		public AutoWireTeam(String managedObjectSourceTeamName,
				Class<?> teamSourceClass, PropertyList properties) {
			this.managedObjectSourceTeamName = managedObjectSourceTeamName;
			this.teamSourceClass = teamSourceClass;
			this.properties = properties;
		}
	}

}