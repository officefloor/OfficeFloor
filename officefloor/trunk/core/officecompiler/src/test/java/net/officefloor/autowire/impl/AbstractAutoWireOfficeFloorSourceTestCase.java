/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2012 Daniel Sagenschneider
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

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;
import net.officefloor.autowire.AutoWire;
import net.officefloor.autowire.AutoWireApplication;
import net.officefloor.autowire.AutoWireObject;
import net.officefloor.autowire.AutoWireSupplier;
import net.officefloor.autowire.spi.supplier.source.SupplierSource;
import net.officefloor.autowire.spi.supplier.source.SupplierSourceContext;
import net.officefloor.autowire.spi.supplier.source.impl.AbstractSupplierSource;
import net.officefloor.compile.integrate.managedobject.CompileOfficeFloorManagedObjectTest.InputManagedObject;
import net.officefloor.compile.managedobject.ManagedObjectDependencyType;
import net.officefloor.compile.managedobject.ManagedObjectFlowType;
import net.officefloor.compile.managedobject.ManagedObjectTeamType;
import net.officefloor.compile.managedobject.ManagedObjectType;
import net.officefloor.compile.office.OfficeInputType;
import net.officefloor.compile.office.OfficeManagedObjectType;
import net.officefloor.compile.office.OfficeType;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.office.ManagedObjectTeam;
import net.officefloor.compile.spi.office.OfficeObject;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.office.OfficeSectionInput;
import net.officefloor.compile.spi.office.OfficeTeam;
import net.officefloor.compile.spi.office.source.OfficeSource;
import net.officefloor.compile.spi.officefloor.DeployedOffice;
import net.officefloor.compile.spi.officefloor.DeployedOfficeInput;
import net.officefloor.compile.spi.officefloor.ManagingOffice;
import net.officefloor.compile.spi.officefloor.OfficeFloorDeployer;
import net.officefloor.compile.spi.officefloor.OfficeFloorInputManagedObject;
import net.officefloor.compile.spi.officefloor.OfficeFloorManagedObject;
import net.officefloor.compile.spi.officefloor.OfficeFloorManagedObjectSource;
import net.officefloor.compile.spi.officefloor.OfficeFloorTeam;
import net.officefloor.compile.spi.officefloor.source.OfficeFloorSource;
import net.officefloor.compile.spi.officefloor.source.OfficeFloorSourceContext;
import net.officefloor.compile.spi.section.ManagedObjectDependency;
import net.officefloor.compile.spi.section.ManagedObjectFlow;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.impl.spi.team.OnePersonTeamSource;
import net.officefloor.frame.impl.spi.team.ProcessContextTeamSource;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.spi.TestSource;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.spi.team.Team;
import net.officefloor.frame.spi.team.source.TeamSource;
import net.officefloor.frame.test.OfficeFrameTestCase;

import org.easymock.AbstractMatcher;

/**
 * <p>
 * Abstract test functionality to test the {@link AutoWireOfficeFloorSource}.
 * <p>
 * As there are a number of areas to test with the
 * {@link AutoWireOfficeFloorSource}, separate {@link TestCase} instances cover
 * the various aspects to make management of the tests easier (and more
 * focused).
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractAutoWireOfficeFloorSourceTestCase extends
		OfficeFrameTestCase {

	/**
	 * {@link AutoWireOfficeFloorSource} to test.
	 */
	protected final AutoWireApplication source = new AutoWireOfficeFloorSource();

	/**
	 * {@link OfficeFloorDeployer}.
	 */
	protected final OfficeFloorDeployer deployer = this
			.createMock(OfficeFloorDeployer.class);

	/**
	 * {@link OfficeFloorSourceContext}.
	 */
	protected final OfficeFloorSourceContext context = this
			.createMock(OfficeFloorSourceContext.class);

	/**
	 * Sources the {@link OfficeFloor} for the {@link AutoWireApplication}.
	 */
	protected void doSourceOfficeFloorTest() throws Exception {
		// Source the OfficeFloor
		this.replayMockObjects();
		((OfficeFloorSource) this.source).sourceOfficeFloor(this.deployer,
				this.context);
		this.verifyMockObjects();
	}

	/**
	 * Adds {@link SupplierSource}.
	 * 
	 * @param init
	 *            {@link SupplierInit}.
	 */
	protected void addSupplier(SupplierInit init) {
		DynamicSupplierSource.addSupplier(this.source, init);
	}

	/**
	 * Provides the supply for the {@link DynamicSupplierSource}.
	 */
	protected static interface SupplierInit {

		/**
		 * Provides the supply.
		 * 
		 * @param context
		 *            {@link SupplierSourceContext}.
		 * @throws Exception
		 *             If fails to supply.
		 */
		void supply(SupplierSourceContext context) throws Exception;
	}

	/**
	 * Dynamic {@link SupplierSource}.
	 */
	@TestSource
	public static class DynamicSupplierSource extends AbstractSupplierSource {

		/**
		 * Identifier to obtain the appropriate {@link SupplierInit}.
		 */
		public static final String PROPERTY_SUPPLIER_IDENTIFIER = "SUPPLIER_ID";

		/**
		 * {@link SupplierInit} instances by their identifiers.
		 */
		private static final Map<Integer, SupplierInit> supplierInits = new HashMap<Integer, SupplierInit>();

		/**
		 * Setup for the next test.
		 */
		public static void reset() {
			supplierInits.clear();
		}

		/**
		 * Adds this to the {@link AutoWireApplication} with the
		 * {@link SupplierInit}.
		 * 
		 * @param application
		 *            {@link AutoWireApplication}.
		 * @param init
		 *            {@link SupplierInit}.
		 */
		public static void addSupplier(AutoWireApplication application,
				SupplierInit init) {

			// Obtain the next identifier (and register init)
			int identifier = supplierInits.size();
			supplierInits.put(Integer.valueOf(identifier), init);

			// Add the supplier
			AutoWireSupplier supplier = application
					.addSupplier(DynamicSupplierSource.class.getName());
			supplier.addProperty(PROPERTY_SUPPLIER_IDENTIFIER,
					String.valueOf(identifier));
		}

		/*
		 * ==================== SupplierSource =====================
		 */

		@Override
		protected void loadSpecification(SpecificationContext context) {
			context.addProperty(PROPERTY_SUPPLIER_IDENTIFIER);
		}

		@Override
		public void supply(SupplierSourceContext context) throws Exception {

			// Obtain the supplier init
			Integer identifier = Integer.valueOf(context
					.getProperty(PROPERTY_SUPPLIER_IDENTIFIER));
			SupplierInit init = supplierInits.get(identifier);
			assertNotNull("No init configured for identifier " + identifier,
					init);

			// Initialise the supplier
			init.supply(context);
		}
	}

	/**
	 * {@link OfficeFloorTeam}.
	 */
	private final OfficeFloorTeam team = this.createMock(OfficeFloorTeam.class);

	/**
	 * {@link OfficeFloorTeam} instances by {@link AutoWire}.
	 */
	private final Map<AutoWire, OfficeFloorTeam> teams = new HashMap<AutoWire, OfficeFloorTeam>();

	/**
	 * {@link DeployedOffice}.
	 */
	private final DeployedOffice office = this.createMock(DeployedOffice.class);

	/**
	 * Handled instances.
	 */
	private final List<AutoWire> handledInputs = new LinkedList<AutoWire>();

	/**
	 * {@link OfficeFloorManagedObject} instances by {@link AutoWire}.
	 */
	private final Map<AutoWire, OfficeFloorManagedObject> managedObjects = new HashMap<AutoWire, OfficeFloorManagedObject>();

	/**
	 * {@link OfficeFloorInputManagedObject} instances by {@link AutoWire}.
	 */
	private final Map<AutoWire, OfficeFloorInputManagedObject> inputManagedObjects = new HashMap<AutoWire, OfficeFloorInputManagedObject>();

	/**
	 * {@link OfficeObject} instances by {@link AutoWire}.
	 */
	private final Map<AutoWire, OfficeObject> objects = new HashMap<AutoWire, OfficeObject>();

	/**
	 * {@link ManagedObjectType} instances by {@link AutoWireObject}.
	 */
	private final Map<AutoWireObject, ManagedObjectType<?>> managedObjectTypes = new HashMap<AutoWireObject, ManagedObjectType<?>>();

	/**
	 * {@link ManagedObjectFlowType} names by {@link AutoWireObject}.
	 */
	private final Map<AutoWireObject, List<String>> managedObjectFlowTypes = new HashMap<AutoWireObject, List<String>>();

	/**
	 * {@link ManagedObjectTeamType} names by {@link AutoWireObject}.
	 */
	private final Map<AutoWireObject, List<String>> managedObjectTeamTypes = new HashMap<AutoWireObject, List<String>>();

	/**
	 * Records the {@link Team}.
	 */
	protected void recordTeam() {
		this.recordReturn(
				this.deployer,
				this.deployer.addTeam("team",
						ProcessContextTeamSource.class.getName()), this.team);
	}

	/**
	 * Records the {@link DeployedOfficeInput}.
	 * 
	 * @param sectionName
	 *            Name of the {@link OfficeSection}.
	 * @param inputName
	 *            Name of the {@link OfficeSectionInput}.
	 */
	protected void registerOfficeInput(String sectionName, String inputName) {
		this.handledInputs.add(new AutoWire(sectionName, inputName));
	}

	/**
	 * Records the {@link Office}.
	 * 
	 * @param officeObjectAutoWiring
	 *            {@link AutoWire} instances to identify the used
	 *            {@link OfficeObject} instances.
	 * @return {@link DeployedOffice}.
	 */
	protected DeployedOffice recordOffice(AutoWire... officeObjectAutoWiring) {
		return this.recordOffice(this.team, officeObjectAutoWiring);
	}

	/**
	 * Records the office.
	 * 
	 * @param defaultTeam
	 *            Default {@link Team}.
	 * @param officeObjectAutoWiring
	 *            {@link AutoWire} instances to identify the used
	 *            {@link OfficeObject} instances.
	 * @return {@link DeployedOffice}.
	 */
	protected DeployedOffice recordOffice(OfficeFloorTeam defaultTeam,
			AutoWire... officeObjectAutoWiring) {

		final PropertyList propertyList = this.createMock(PropertyList.class);
		final OfficeType officeType = this.createMock(OfficeType.class);

		// Create the office input types
		final OfficeInputType[] officeInputs = new OfficeInputType[this.handledInputs
				.size()];
		for (int i = 0; i < officeInputs.length; i++) {
			officeInputs[i] = this.createMock(OfficeInputType.class);
		}

		// Create the office managed object types
		final OfficeManagedObjectType[] officeObjects = new OfficeManagedObjectType[officeObjectAutoWiring.length];
		for (int i = 0; i < officeObjects.length; i++) {
			officeObjects[i] = this.createMock(OfficeManagedObjectType.class);
		}

		// Record the office type (with used office objects)
		this.recordReturn(this.context, this.context.createPropertyList(),
				propertyList);
		this.recordReturn(this.context, this.context.loadOfficeType(
				(OfficeSource) null, "auto-wire", propertyList), officeType,
				new AbstractMatcher() {
					@Override
					public boolean matches(Object[] expected, Object[] actual) {
						assertTrue("Incorrect Office",
								actual[0] instanceof OfficeSource);
						assertEquals("Incorrect location", expected[1],
								actual[1]);
						assertEquals("Incorrect properties", expected[2],
								actual[2]);
						return true;
					}
				});

		// Record the office input types
		this.recordReturn(officeType, officeType.getOfficeInputTypes(),
				officeInputs);
		for (int i = 0; i < officeInputs.length; i++) {
			OfficeInputType officeInput = officeInputs[i];
			AutoWire handledInput = this.handledInputs.get(i);
			this.recordReturn(officeInput, officeInput.getOfficeSectionName(),
					handledInput.getQualifier());
			this.recordReturn(officeInput,
					officeInput.getOfficeSectionInputName(),
					handledInput.getType());
		}

		// Record the office managed object types
		this.recordReturn(officeType, officeType.getOfficeManagedObjectTypes(),
				officeObjects);
		for (int i = 0; i < officeObjects.length; i++) {
			OfficeManagedObjectType officeObject = officeObjects[i];
			AutoWire officeObjectAutoWire = officeObjectAutoWiring[i];
			this.recordReturn(officeObject, officeObject.getObjectType(),
					officeObjectAutoWire.getType());
			this.recordReturn(officeObject, officeObject.getTypeQualifier(),
					officeObjectAutoWire.getQualifier());
		}

		// Record the office
		this.recordReturn(this.deployer, this.deployer.addDeployedOffice(
				"OFFICE", (OfficeSource) null, "auto-wire"), this.office,
				new AbstractMatcher() {
					@Override
					public boolean matches(Object[] expected, Object[] actual) {
						assertEquals("Incorrect office name", expected[0],
								actual[0]);
						assertTrue("Incorrect Office",
								actual[1] instanceof OfficeSource);
						assertEquals("Incorrect location", expected[2],
								actual[2]);
						return true;
					}
				});

		// Record binding office team to team
		OfficeTeam officeTeam = this.createMock(OfficeTeam.class);
		this.recordReturn(this.office,
				this.office.getDeployedOfficeTeam("team"), officeTeam);
		this.deployer.link(officeTeam, defaultTeam);

		// Return the office
		return this.office;
	}

	/**
	 * Indicates if the raw object {@link ManagedObjectType} matcher is
	 * specified.
	 */
	private boolean isRawObjectTypeMatcherSpecified = false;

	/**
	 * Records obtaining the {@link ManagedObjectType} for the raw object.
	 * 
	 * @param object
	 *            Raw Object.
	 * @param extensionInterfaces
	 *            Extension interfaces.
	 */
	protected void recordRawObjectType(Object object,
			Class<?>... extensionInterfaces) {

		// Record the managed object type
		ManagedObjectType<?> managedObjectType = this
				.createMock(ManagedObjectType.class);
		this.recordReturn(this.context, this.context.loadManagedObjectType(
				new SingletonManagedObjectSource(object), null),
				managedObjectType);
		if (!this.isRawObjectTypeMatcherSpecified) {
			this.control(this.context).setMatcher(new AbstractMatcher() {
				@Override
				public boolean matches(Object[] expected, Object[] actual) {
					assertNotNull("Must have properties", actual[1]);

					// Match if raw objects match
					SingletonManagedObjectSource eMo = (SingletonManagedObjectSource) expected[0];
					SingletonManagedObjectSource aMo = (SingletonManagedObjectSource) actual[0];
					return eMo.getObject().equals(aMo.getObject());
				}
			});
			this.isRawObjectTypeMatcherSpecified = true;
		}

		// No flows and teams for raw object
		this.recordReturn(managedObjectType, managedObjectType.getFlowTypes(),
				new ManagedObjectFlowType<?>[0]);
		this.recordReturn(managedObjectType, managedObjectType.getTeamTypes(),
				new ManagedObjectTeamType[0]);

		// Record obtaining extension interfaces
		this.recordReturn(managedObjectType,
				managedObjectType.getExtensionInterfaces(), extensionInterfaces);
	}

	/**
	 * Register the {@link ManagedObjectFlowType} for the {@link ManagedObject}
	 * of the {@link AutoWireObject}.
	 * 
	 * @param object
	 *            {@link AutoWireObject}.
	 * @param flowNames
	 *            Names of {@link ManagedObjectFlowType}.
	 */
	protected void registerManagedObjectFlowType(AutoWireObject object,
			String... flowNames) {
		List<String> flows = this.managedObjectFlowTypes.get(object);
		if (flows == null) {
			flows = new LinkedList<String>();
			this.managedObjectFlowTypes.put(object, flows);
		}
		flows.addAll(Arrays.asList(flowNames));
	}

	/**
	 * Register the {@link ManagedObjectTeamType} for the {@link ManagedObject}
	 * of the {@link AutoWireObject}.
	 * 
	 * @param object
	 *            {@link AutoWireObject}.
	 * @param teamNames
	 *            Names of {@link ManagedObjectTeamType}.
	 */
	protected void registerManagedObjectTeamType(AutoWireObject object,
			String... teamNames) {
		List<String> teams = this.managedObjectTeamTypes.get(object);
		if (teams == null) {
			teams = new LinkedList<String>();
			this.managedObjectFlowTypes.put(object, teams);
		}
		teams.addAll(Arrays.asList(teamNames));
	}

	/**
	 * Records obtaining the {@link ManagedObjectType}.
	 * 
	 * @param object
	 *            {@link AutoWireObject}.
	 * @param extensionInterfaces
	 *            Extension interfaces.
	 */
	protected void recordManagedObjectType(AutoWireObject object,
			Class<?>... extensionInterfaces) {

		// Record the managed object type
		ManagedObjectType<?> managedObjectType = this
				.createMock(ManagedObjectType.class);
		this.recordReturn(
				this.context,
				this.context.loadManagedObjectType(
						object.getManagedObjectSourceClassName(),
						object.getProperties()), managedObjectType);

		// Record flow types
		List<String> flowNames = this.managedObjectFlowTypes.get(object);
		int flowCount = (flowNames == null ? 0 : flowNames.size());
		this.recordReturn(managedObjectType, managedObjectType.getFlowTypes(),
				new ManagedObjectFlowType<?>[flowCount]);

		// Record team types
		List<String> teamNames = this.managedObjectTeamTypes.get(object);
		int teamCount = (teamNames == null ? 0 : teamNames.size());
		this.recordReturn(managedObjectType, managedObjectType.getTeamTypes(),
				new ManagedObjectTeamType[teamCount]);

		// Record obtaining extension interfaces
		this.recordReturn(managedObjectType,
				managedObjectType.getExtensionInterfaces(), extensionInterfaces);

		// Register the managed object type
		this.managedObjectTypes.put(object, managedObjectType);
	}

	/**
	 * Indicates if the {@link AbstractMatcher} is specified for the raw object.
	 */
	private boolean isRawObjectMatcherSpecified = false;

	/**
	 * Records raw object.
	 */
	protected OfficeFloorManagedObject recordRawObject(Object dependency,
			AutoWire autoWire) {

		final OfficeFloorManagedObjectSource source = this
				.createMock(OfficeFloorManagedObjectSource.class);

		// Record the managed object source
		this.recordReturn(this.deployer, this.deployer.addManagedObjectSource(
				autoWire.getQualifiedType(), new SingletonManagedObjectSource(
						dependency)), source);
		if (!this.isRawObjectMatcherSpecified) {
			this.control(this.deployer).setMatcher(new AbstractMatcher() {
				@Override
				public boolean matches(Object[] expected, Object[] actual) {
					assertEquals("Incorrect name", expected[0], actual[0]);
					SingletonManagedObjectSource eMo = (SingletonManagedObjectSource) expected[1];
					SingletonManagedObjectSource aMo = (SingletonManagedObjectSource) actual[1];
					assertEquals("Incorrect singleton object", eMo.getObject(),
							aMo.getObject());
					return true;
				}
			});
			this.isRawObjectMatcherSpecified = true;
		}

		// Have managed by office
		ManagingOffice managingOffice = this.createMock(ManagingOffice.class);
		this.recordReturn(source, source.getManagingOffice(), managingOffice);
		this.deployer.link(managingOffice, this.office);

		// Record and return the managed object
		return this.recordManagedObject(source, autoWire);
	}

	/**
	 * Records a {@link ManagedObjectSource}.
	 */
	protected OfficeFloorManagedObjectSource recordManagedObjectSource(
			AutoWire mosName, Class<?> managedObjectSourceClass, int typeIndex,
			long timeout, String... propertyNameValues) {
		return this.recordManagedObjectSource(mosName,
				managedObjectSourceClass, null, typeIndex, timeout,
				propertyNameValues);
	}

	/**
	 * Records a {@link ManagedObjectSource}.
	 */
	protected OfficeFloorManagedObjectSource recordManagedObjectSource(
			AutoWire mosName, ManagedObjectSource<?, ?> managedObjectSource,
			int typeIndex, long timeout, String... propertyNameValues) {
		return this.recordManagedObjectSource(mosName, null,
				managedObjectSource, typeIndex, timeout, propertyNameValues);
	}

	/**
	 * Records a {@link ManagedObjectSource}.
	 */
	protected OfficeFloorManagedObjectSource recordManagedObjectSource(
			AutoWire mosName, Class<?> managedObjectSourceClass,
			ManagedObjectSource<?, ?> managedObjectSource, int typeIndex,
			long timeout, String... propertyNameValues) {

		final OfficeFloorManagedObjectSource source = this
				.createMock(OfficeFloorManagedObjectSource.class);

		// Record the managed object source
		this.recordReturn(this.deployer, this.deployer.addManagedObjectSource(
				mosName.getQualifiedType()
						+ (typeIndex <= 0 ? "" : String.valueOf(typeIndex)),
				managedObjectSourceClass.getName()), source);

		// Record time out (-1 for no timeout on raw object)
		if (timeout != -1) {
			source.setTimeout(timeout);
		}

		// Record properties
		for (int i = 0; i < propertyNameValues.length; i += 2) {
			String name = propertyNameValues[i];
			String value = propertyNameValues[i + 1];
			source.addProperty(name, value);
		}

		// Have managed by office
		ManagingOffice managingOffice = this.createMock(ManagingOffice.class);
		this.recordReturn(source, source.getManagingOffice(), managingOffice);
		this.deployer.link(managingOffice, this.office);

		// Return the managed object source
		return source;
	}

	/**
	 * Records the {@link ManagedObject}.
	 * 
	 * @return {@link OfficeFloorManagedObject}.
	 */
	protected OfficeFloorManagedObject recordManagedObject(
			OfficeFloorManagedObjectSource source, AutoWire autoWire) {

		// Record the managed object
		final OfficeFloorManagedObject mo = this
				.createMock(OfficeFloorManagedObject.class);
		this.recordReturn(source, source.addOfficeFloorManagedObject(
				autoWire.getQualifiedType(), ManagedObjectScope.PROCESS), mo);
		this.managedObjects.put(autoWire, mo);

		// Return the managed object
		return mo;
	}

	/**
	 * Records the {@link OfficeFloorInputManagedObject}.
	 */
	protected OfficeFloorInputManagedObject recordInputManagedObject(
			OfficeFloorManagedObjectSource source, AutoWire autoWire) {

		// Obtain the Input Managed Object
		OfficeFloorInputManagedObject input = this.inputManagedObjects
				.get(autoWire);
		if (input == null) {
			// Create and register the input managed object
			input = this.createMock(OfficeFloorInputManagedObject.class);
			this.inputManagedObjects.put(autoWire, input);
			this.recordReturn(this.deployer, this.deployer
					.addInputManagedObject(autoWire.getQualifiedType()), input);

			// First managed object source is bound
			input.setBoundOfficeFloorManagedObjectSource(source);

			// Record the input managed object into the office
			final OfficeObject object = this.createMock(OfficeObject.class);
			this.recordReturn(this.office, this.office
					.getDeployedOfficeObject(autoWire.getQualifiedType()),
					object);
			this.deployer.link(object, input);
			this.objects.put(autoWire, object);
		}

		// Link the source to input
		this.deployer.link(source, input);

		// Return the input managed object
		return input;
	}

	/**
	 * Record the {@link OfficeObject} for the {@link OfficeFloorManagedObject}.
	 */
	protected void recordOfficeObject(OfficeFloorManagedObject managedObject,
			AutoWire autoWire) {
		// Record the managed object into the office
		final OfficeObject object = this.createMock(OfficeObject.class);
		this.recordReturn(this.office, this.office
				.getDeployedOfficeObject(autoWire.getQualifiedType()), object);
		this.deployer.link(object, managedObject);
		this.objects.put(autoWire, object);
	}

	/**
	 * Records obtaining the {@link ManagedObjectDependencyType} instances from
	 * the {@link ManagedObjectType}.
	 * 
	 * @param object
	 *            {@link AutoWireObject}.
	 * @param dependencyNameAutoWirePairs
	 *            {@link ManagedObjectDependencyType} name/{@link AutoWire}
	 *            pairs.
	 */
	protected void recordManagedObjectDependencies(AutoWireObject object,
			Object... dependencyNameAutoWirePairs) {

		// Obtain the managed object type
		ManagedObjectType<?> managedObjectType = this.managedObjectTypes
				.get(object);
		assertNotNull("Unknown object", managedObjectType);

		// Record obtaining the dependency types
		ManagedObjectDependencyType<?>[] dependencyTypes = new ManagedObjectDependencyType<?>[dependencyNameAutoWirePairs.length / 2];
		this.recordReturn(managedObjectType,
				managedObjectType.getDependencyTypes(), dependencyTypes);

		try {
			// Load dependencies and return names/auto-wiring
			for (int i = 0; i < dependencyNameAutoWirePairs.length; i += 2) {
				ManagedObjectDependencyType<?> dependencyType = this
						.createMock(ManagedObjectDependencyType.class);
				dependencyTypes[i / 2] = dependencyType;

				// Obtain the dependency details
				String dependencyName = (String) dependencyNameAutoWirePairs[i];
				AutoWire dependencyAutoWire = (AutoWire) dependencyNameAutoWirePairs[i + 1];
				Class<?> dependencyClass = Thread.currentThread()
						.getContextClassLoader()
						.loadClass(dependencyAutoWire.getType());

				// Record the dependency
				this.recordReturn(dependencyType,
						dependencyType.getDependencyName(), dependencyName);
				this.recordReturn(dependencyType,
						dependencyType.getTypeQualifier(),
						dependencyAutoWire.getQualifier());
				this.recordReturn(dependencyType,
						dependencyType.getDependencyType(), dependencyClass);
			}
		} catch (Exception ex) {
			throw fail(ex);
		}
	}

	/**
	 * Records linking a {@link ManagedObject} dependency.
	 */
	protected void recordLinkManagedObjectDependency(AutoWire autoWire,
			String managedObjectDependencyName, AutoWire dependencyAutoWire) {

		// Obtain the managed object
		OfficeFloorManagedObject mo = this.managedObjects.get(autoWire);
		assertNotNull(
				"No managed object for type " + autoWire.getQualifiedType(), mo);

		// Record the dependency
		final ManagedObjectDependency dependency = this
				.createMock(ManagedObjectDependency.class);
		this.recordReturn(mo,
				mo.getManagedObjectDependency(managedObjectDependencyName),
				dependency);

		// Link dependency
		this.recordLinkDependency(dependency, dependencyAutoWire);
	}

	/**
	 * Records an {@link InputManagedObject} dependency.
	 */
	protected void recordLinkInputManagedObjectDependency(
			OfficeFloorManagedObjectSource source,
			String managedObjectDependencyName, AutoWire dependencyAutoWire) {

		// Record the dependency
		final ManagedObjectDependency dependency = this
				.createMock(ManagedObjectDependency.class);
		this.recordReturn(source, source
				.getInputManagedObjectDependency(managedObjectDependencyName),
				dependency);

		// Link dependency
		this.recordLinkDependency(dependency, dependencyAutoWire);
	}

	/**
	 * Link dependency.
	 */
	protected void recordLinkDependency(ManagedObjectDependency dependency,
			AutoWire dependencyAutoWire) {

		// Obtain the potential dependencies
		OfficeFloorManagedObject dependencyMo = this.managedObjects
				.get(dependencyAutoWire);
		OfficeFloorInputManagedObject dependencyInput = this.inputManagedObjects
				.get(dependencyAutoWire);

		// Link to dependency
		if (dependencyMo != null) {
			// Link dependency to managed object
			this.deployer.link(dependency, dependencyMo);

		} else if (dependencyInput != null) {
			// Link dependency to input managed object
			this.deployer.link(dependency, dependencyInput);

		} else {
			fail("No managed object dependency for type "
					+ dependencyAutoWire.getQualifiedType());
		}
	}

	/**
	 * Records the {@link ManagedObjectFlow}.
	 */
	protected void recordManagedObjectFlow(
			OfficeFloorManagedObjectSource source,
			String managedObjectFlowName, String sectionName,
			String sectionInputName) {

		// Obtain the managed object flow
		final ManagedObjectFlow flow = this.createMock(ManagedObjectFlow.class);
		this.recordReturn(source,
				source.getManagedObjectFlow(managedObjectFlowName), flow);

		// Obtain the section input
		final DeployedOfficeInput sectionInput = this
				.createMock(DeployedOfficeInput.class);
		this.recordReturn(this.office, this.office.getDeployedOfficeInput(
				sectionName, sectionInputName), sectionInput);

		// Link
		this.deployer.link(flow, sectionInput);
	}

	/**
	 * Records the {@link ManagedObjectTeam}.
	 */
	protected <S extends TeamSource> void recordManagedObjectTeam(
			OfficeFloorManagedObjectSource source,
			String managedObjectTeamName, Class<S> teamSourceClass,
			String... propertyNameValues) {

		// Obtain the managed object team
		final ManagedObjectTeam moTeam = this
				.createMock(ManagedObjectTeam.class);
		this.recordReturn(source,
				source.getManagedObjectTeam(managedObjectTeamName), moTeam);

		// Register the team
		final OfficeFloorTeam team = this.createMock(OfficeFloorTeam.class);
		this.recordReturn(source,
				source.getOfficeFloorManagedObjectSourceName(), "TestName");
		this.recordReturn(this.deployer, this.deployer.addTeam("TestName-"
				+ managedObjectTeamName, teamSourceClass.getName()), team);
		for (int i = 0; i < propertyNameValues.length; i += 2) {
			String name = propertyNameValues[i];
			String value = propertyNameValues[i + 1];
			team.addProperty(name, value);
		}

		// Link team
		this.deployer.link(moTeam, team);
	}

	/**
	 * Records the {@link ManagedObjectTeam}.
	 */
	protected void recordManagedObjectTeam(
			OfficeFloorManagedObjectSource source,
			String managedObjectTeamName, AutoWire autoWire) {

		// Obtain the managed object team
		final ManagedObjectTeam moTeam = this
				.createMock(ManagedObjectTeam.class);
		this.recordReturn(source,
				source.getManagedObjectTeam(managedObjectTeamName), moTeam);

		// Obtain the team to link
		OfficeFloorTeam officeFloorTeam = this.teams.get(autoWire);

		// Record linking team
		this.deployer.link(moTeam, officeFloorTeam);
	}

	/**
	 * Records the {@link Team}.
	 * 
	 * @param propertyNameValuePairs
	 *            Name value pairs. May be <code>null</code> to indicate no
	 *            properties.
	 * @param autoWiring
	 *            {@link AutoWire} instances.
	 * @return {@link OfficeFloorTeam}.
	 */
	protected OfficeFloorTeam recordTeam(String[] propertyNameValuePairs,
			AutoWire... autoWiring) {

		// Base name of first auto-wire
		AutoWire nameAutoWire = autoWiring[0];

		// Create the Office Floor team
		OfficeFloorTeam officeFloorTeam = this
				.createMock(OfficeFloorTeam.class);
		this.recordReturn(this.deployer, this.deployer.addTeam(
				nameAutoWire.getQualifiedType(),
				OnePersonTeamSource.class.getName()), officeFloorTeam);
		if (propertyNameValuePairs != null) {
			for (int i = 0; i < propertyNameValuePairs.length; i += 2) {
				String name = propertyNameValuePairs[i];
				String value = propertyNameValuePairs[i + 1];
				officeFloorTeam.addProperty(name, value);
			}
		}

		// Create and link the responsibilities, along with registering teams
		for (AutoWire autoWire : autoWiring) {
			OfficeTeam officeTeam = this.createMock(OfficeTeam.class);
			this.recordReturn(this.office, this.office
					.getDeployedOfficeTeam(autoWire.getQualifiedType()),
					officeTeam);
			this.deployer.link(officeTeam, officeFloorTeam);

			// Register the team
			this.teams.put(autoWire, officeFloorTeam);
		}

		// Return the team
		return officeFloorTeam;
	}

	/**
	 * Mock raw type.
	 */
	public static interface MockRawType {
	}

	/**
	 * Mock raw object.
	 */
	public static class MockRawObject implements MockRawType {
	}

}