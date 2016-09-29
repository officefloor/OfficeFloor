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
import net.officefloor.autowire.AutoWireTeam;
import net.officefloor.autowire.ManagedObjectSourceWirer;
import net.officefloor.autowire.ManagedObjectSourceWirerContext;
import net.officefloor.autowire.impl.supplier.MockTypeManagedObjectSource;
import net.officefloor.autowire.spi.supplier.source.SupplierSource;
import net.officefloor.autowire.spi.supplier.source.SupplierSourceContext;
import net.officefloor.autowire.spi.supplier.source.impl.AbstractSupplierSource;
import net.officefloor.autowire.supplier.SuppliedManagedObject;
import net.officefloor.autowire.supplier.SuppliedManagedObjectType;
import net.officefloor.autowire.supplier.SupplierType;
import net.officefloor.compile.integrate.managedobject.CompileOfficeFloorManagedObjectTest.InputManagedObject;
import net.officefloor.compile.managedobject.ManagedObjectDependencyType;
import net.officefloor.compile.managedobject.ManagedObjectFlowType;
import net.officefloor.compile.managedobject.ManagedObjectTeamType;
import net.officefloor.compile.managedobject.ManagedObjectType;
import net.officefloor.compile.office.OfficeAvailableSectionInputType;
import net.officefloor.compile.office.OfficeManagedObjectType;
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
import net.officefloor.compile.spi.officefloor.ManagingOffice;
import net.officefloor.compile.spi.officefloor.OfficeFloorDeployer;
import net.officefloor.compile.spi.officefloor.OfficeFloorInputManagedObject;
import net.officefloor.compile.spi.officefloor.OfficeFloorManagedObject;
import net.officefloor.compile.spi.officefloor.OfficeFloorManagedObjectSource;
import net.officefloor.compile.spi.officefloor.OfficeFloorSupplier;
import net.officefloor.compile.spi.officefloor.OfficeFloorTeam;
import net.officefloor.compile.spi.officefloor.source.OfficeFloorSource;
import net.officefloor.compile.spi.officefloor.source.OfficeFloorSourceContext;
import net.officefloor.compile.spi.section.ManagedObjectDependency;
import net.officefloor.compile.spi.section.ManagedObjectFlow;
import net.officefloor.compile.test.supplier.SupplierLoaderUtil;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.impl.spi.team.PassiveTeamSource;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.spi.TestSource;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.spi.team.source.TeamSource;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.managedobject.clazz.ClassManagedObjectSource;

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
	 * Flag indicating if the {@link SupplierType} {@link AbstractMatcher} has
	 * been loaded.
	 */
	private boolean isSupplierTypeMatcherLoaded = false;

	/**
	 * Adds {@link SupplierSource} along with recording obtaining its
	 * {@link SupplierType}.
	 * 
	 * @param init
	 *            {@link SupplierInit}.
	 * @return Identifier for the {@link OfficeFloorSupplier}.
	 */
	protected int addSupplierAndRecordType(SupplierInit init) {

		// Add the supplier
		int identifier = DynamicSupplierSource.addSupplier(this.source, init);

		// Obtain the supplier type
		SupplierType supplierType = SupplierLoaderUtil.loadSupplierType(
				DynamicSupplierSource.class,
				DynamicSupplierSource.PROPERTY_SUPPLIER_IDENTIFIER,
				String.valueOf(identifier));

		// Record obtaining the supplier type (as always first)
		this.recordReturn(
				this.context,
				this.context.loadSupplierType(
						DynamicSupplierSource.class.getName(), null),
				supplierType);
		if (!this.isSupplierTypeMatcherLoaded) {
			this.control(this.context).setMatcher(new AbstractMatcher() {
				@Override
				public boolean matches(Object[] expected, Object[] actual) {
					// SupplierSources must match and have properties
					assertNotNull("Must have properties", actual[1]);
					String supplierSourceA = (String) expected[0];
					String supplierSourceB = (String) actual[0];
					return supplierSourceA.equals(supplierSourceB);
				}
			});
			this.isSupplierTypeMatcherLoaded = true;
		}

		// Return the identifier
		return identifier;
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
		 * @return Identifier for the instance.
		 */
		public static int addSupplier(AutoWireApplication application,
				SupplierInit init) {

			// Obtain the next identifier (and register init)
			int identifier = supplierInits.size();
			supplierInits.put(Integer.valueOf(identifier), init);

			// Add the supplier
			AutoWireSupplier supplier = application
					.addSupplier(DynamicSupplierSource.class.getName());
			supplier.addProperty(PROPERTY_SUPPLIER_IDENTIFIER,
					String.valueOf(identifier));

			// Return the identifier
			return identifier;
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
	 * Type of generic sourcing for the object.
	 */
	protected static enum SourceObjectType {
		RawObject, ManagedObject, InputManagedObject, SuppliedManagedObject
	}

	/**
	 * Generic method to add a source object.
	 * 
	 * @param clazz
	 *            {@link Class} of the object.
	 * @param sourceType
	 *            {@link SourceObjectType}.
	 * @param typeQualifier
	 *            Type qualifier for the object.
	 * @param dependencyName
	 *            Name of dependency. May be <code>null</code> for no
	 *            dependency. Required for recording
	 *            {@link SuppliedManagedObjectType}.
	 * @param dependencyType
	 *            Type of dependency.
	 * @param dependencyQualifier
	 *            Qualifier for the required dependency.
	 * @return Raw Object, {@link AutoWireObject} or {@link OfficeFloorSupplier}
	 *         identifier based on {@link SourceObjectType}.
	 */
	protected Object addObject(Class<?> clazz, SourceObjectType sourceType,
			String typeQualifier, String dependencyName,
			Class<?> dependencyType, String dependencyQualifier) {

		final AutoWire autoWire = new AutoWire(typeQualifier, clazz.getName());

		// Load based on source type
		switch (sourceType) {
		case RawObject:
			// Add and return the raw object
			Object rawObject = this.createMock(clazz);
			this.source.addObject(rawObject, autoWire);
			return rawObject;

		case ManagedObject:
			// Add and return the managed object
			AutoWireObject managedObject = this.source.addManagedObject(
					ClassManagedObjectSource.class.getName(), null, autoWire);
			managedObject.addProperty(
					ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME,
					clazz.getName());
			return managedObject;

		case InputManagedObject:
			// Add and return the input managed object
			ManagedObjectSourceWirer inputManagedObjectWirer = new ManagedObjectSourceWirer() {
				@Override
				public void wire(ManagedObjectSourceWirerContext context) {
					context.mapFlow("flow", "SECTION", "INPUT");
				}
			};
			AutoWireObject inputManagedObject = this.source.addManagedObject(
					ClassManagedObjectSource.class.getName(),
					inputManagedObjectWirer, autoWire);
			inputManagedObject.addProperty(
					ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME,
					clazz.getName());
			return inputManagedObject;

		case SuppliedManagedObject:
			// Add and return the supplied managed object
			final MockTypeManagedObjectSource supplied = new MockTypeManagedObjectSource(
					clazz);

			// Add dependency (if required)
			if (dependencyName != null) {
				supplied.addDependency(dependencyName, dependencyType,
						dependencyQualifier);
			}

			int identifier = this.addSupplierAndRecordType(new SupplierInit() {
				@Override
				public void supply(SupplierSourceContext context)
						throws Exception {
					context.addManagedObject(supplied, null, autoWire);
				}
			});
			return Integer.valueOf(identifier);

		default:
			fail("Unknown source type");
			return null;
		}
	}

	/**
	 * Generic method to record the type for the object.
	 * 
	 * @param object
	 *            Raw Object, {@link AutoWireObject} or <code>null</code>.
	 * @param sourceType
	 *            {@link SourceObjectType}.
	 */
	protected void recordObjectType(Object object, SourceObjectType sourceType) {
		switch (sourceType) {
		case RawObject:
			// Record raw object type
			this.recordRawObjectType(object);
			break;

		case ManagedObject:
			// Record managed object type
			AutoWireObject managedObject = (AutoWireObject) object;
			this.recordManagedObjectType(managedObject);
			break;

		case InputManagedObject:
			// Record input managed object type
			AutoWireObject inputManagedObject = (AutoWireObject) object;
			this.registerManagedObjectFlowType(inputManagedObject, "flow");
			this.recordManagedObjectType(inputManagedObject);
			break;

		case SuppliedManagedObject:
			// Type already recorded on adding supplier
			break;

		default:
			fail("Unknown source type");
		}
	}

	/**
	 * Generic method to record adding the object source.
	 * 
	 * @param object
	 *            Raw Object, {@link AutoWireObject} or <code>null</code>.
	 * @param objectType
	 *            Type of object.
	 * @param typeQualifier
	 *            Qualifier of object.
	 * @param sourceType
	 *            {@link SourceObjectType}.
	 * @return {@link OfficeFloorManagedObjectSource}.
	 */
	protected OfficeFloorManagedObjectSource recordObjectSource(Object object,
			Class<?> objectType, String typeQualifier,
			SourceObjectType sourceType) {

		final AutoWire autoWire = new AutoWire(typeQualifier,
				objectType.getName());

		// Record obtaining the managed object source
		OfficeFloorManagedObjectSource mos = null;
		switch (sourceType) {
		case RawObject:
			// Recorded in creating the Raw Object
			break;

		case ManagedObject:
		case InputManagedObject:
			// Record managed object source
			mos = this.recordManagedObjectSource(autoWire,
					ClassManagedObjectSource.class, 0, 0,
					ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME,
					objectType.getName());
			break;

		case SuppliedManagedObject:
			// Record supplied managed object source
			Integer identifier = (Integer) object;
			mos = this.recordSuppliedManagedObjectSource(identifier.intValue(),
					autoWire);
			break;

		default:
			fail("Unknown source type");
		}

		// Return the managed object source
		return mos;
	}

	/**
	 * Generic method to record adding the object.
	 * 
	 * @param object
	 *            Raw Object, {@link AutoWireObject} or <code>null</code>.
	 * @param objectType
	 *            Type of object.
	 * @param typeQualifier
	 *            Qualifier of object.
	 * @param sourceType
	 *            {@link SourceObjectType}.
	 * @param mos
	 *            {@link OfficeFloorManagedObjectSource}.
	 * @param dependencyName
	 *            Name of dependency. May be <code>null</code> for no
	 *            dependency.
	 * @param dependencyType
	 *            Type of dependency.
	 * @param dependencyQualifier
	 *            Qualifier for the required dependency.
	 */
	protected OfficeFloorManagedObject recordObject(Object object,
			Class<?> objectType, String typeQualifier,
			SourceObjectType sourceType, OfficeFloorManagedObjectSource mos,
			String dependencyName, Class<?> dependencyType,
			String dependencyQualifier) {

		// Create the listing for the dependency
		Object[] dependencyNameAutoWirePairing;
		if (dependencyName == null) {
			// No dependencies
			dependencyNameAutoWirePairing = new Object[0];
		} else {
			// Provide dependency
			dependencyNameAutoWirePairing = new Object[] { dependencyName,
					new AutoWire(dependencyQualifier, dependencyType.getName()) };
		}

		final AutoWire autoWire = new AutoWire(typeQualifier,
				objectType.getName());

		// Record source object
		OfficeFloorManagedObject mo = null;
		switch (sourceType) {
		case RawObject:
			// Record adding raw object
			mo = this.recordRawObject(object, autoWire);
			assertNull("Raw object can not have dependencies", dependencyName);
			break;

		case ManagedObject:
			// Record adding managed object
			mo = this.recordManagedObject(mos, autoWire);
			AutoWireObject autoWireManagedObject = (AutoWireObject) object;
			this.recordManagedObjectDependencies(autoWireManagedObject,
					dependencyNameAutoWirePairing);
			break;

		case InputManagedObject:
			// Record adding input managed object
			this.recordInputManagedObject(mos, autoWire);
			AutoWireObject autoWireInputManagedObject = (AutoWireObject) object;
			this.recordManagedObjectDependencies(autoWireInputManagedObject,
					dependencyNameAutoWirePairing);
			break;

		case SuppliedManagedObject:
			// Record adding supplied managed object (dependencies from type)
			mo = this.recordManagedObject(mos, autoWire);
			break;

		default:
			fail("Unknown source type");
		}

		// Return the managed object
		return mo;
	}

	/**
	 * Generic method to record a flow.
	 * 
	 * @param mos
	 *            {@link OfficeFloorManagedObjectSource}.
	 * @param sourceType
	 *            {@link SourceObjectType}.
	 */
	protected void recordObjectFlow(OfficeFloorManagedObjectSource mos,
			SourceObjectType sourceType) {

		// Record source object
		switch (sourceType) {
		case RawObject:
		case ManagedObject:
			// No flows
			break;

		case InputManagedObject:
			// Record flow for input managed object
			this.recordManagedObjectFlow(mos, "flow", "SECTION", "INPUT");
			break;

		case SuppliedManagedObject:
			// No flows
			break;

		default:
			fail("Unknown source type");
		}
	}

	/**
	 * {@link AutoWire} for the default {@link AutoWireTeam}.
	 */
	protected static final AutoWire DEFAULT_TEAM = new AutoWire("team");

	/**
	 * {@link DeployedOffice}.
	 */
	private final DeployedOffice office = this.createMock(DeployedOffice.class);

	/**
	 * {@link AutoWire} for the {@link OfficeManagedObjectType} instances on the
	 * {@link OfficeType}.
	 */
	private final List<AutoWire> officeObjectTypes = new LinkedList<AutoWire>();

	/**
	 * {@link AutoWire} for the {@link OfficeTeamType} instances on the
	 * {@link OfficeType}.
	 */
	private final List<AutoWire> officeTeamTypes = new LinkedList<AutoWire>();

	/**
	 * Handled {@link OfficeSectionInput} instances by the {@link OfficeType}.
	 */
	private final List<AutoWire> officeInputTypes = new LinkedList<AutoWire>();

	/**
	 * {@link OfficeFloorSupplier} by its identifier.
	 */
	private final Map<Integer, OfficeFloorSupplier> suppliers = new HashMap<Integer, OfficeFloorSupplier>();

	/**
	 * {@link OfficeFloorTeam} instances by {@link AutoWire}.
	 */
	private final Map<AutoWire, OfficeFloorTeam> teams = new HashMap<AutoWire, OfficeFloorTeam>();

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
	 * Records creating the {@link OfficeFloorTeam}.
	 * 
	 * @param teamSourceClass
	 *            {@link TeamSource} {@link Class}.
	 * @param nameAutoWire
	 *            First {@link AutoWire} for the {@link AutoWireTeam}. Allows
	 *            naming the {@link OfficeFloorTeam}.
	 * @param propertyNameValuePairs
	 *            {@link Property} name value pairs.
	 * @return {@link OfficeFloorTeam}.
	 */
	protected OfficeFloorTeam recordTeam(Class<?> teamSourceClass,
			AutoWire nameAutoWire, String... propertyNameValuePairs) {

		// Create the Office Floor team
		OfficeFloorTeam officeFloorTeam = this
				.createMock(OfficeFloorTeam.class);
		this.recordReturn(this.deployer, this.deployer.addTeam(
				nameAutoWire.getQualifiedType(), teamSourceClass.getName()),
				officeFloorTeam);
		if (propertyNameValuePairs != null) {
			for (int i = 0; i < propertyNameValuePairs.length; i += 2) {
				String name = propertyNameValuePairs[i];
				String value = propertyNameValuePairs[i + 1];
				officeFloorTeam.addProperty(name, value);
			}
		}

		// Register the team
		this.teams.put(nameAutoWire, officeFloorTeam);

		// Return the Office Floor team
		return officeFloorTeam;
	}

	/**
	 * Convenience method to record the default {@link OfficeFloorTeam}.
	 */
	protected OfficeFloorTeam recordDefaultTeam() {
		return this.recordTeam(PassiveTeamSource.class, DEFAULT_TEAM);
	}

	/**
	 * Convenience method to record linking the default {@link OfficeFloorTeam}
	 * to its respective {@link OfficeTeam}.
	 */
	protected void recordDefaultTeamLinkedToOffice() {
		this.recordDefaultTeam();
		this.recordLinkTeamToOffice(DEFAULT_TEAM, DEFAULT_TEAM);
	}

	/**
	 * Records linking the {@link OfficeFloorTeam} to the {@link OfficeTeam}.
	 * 
	 * @param teamAutoWire
	 *            {@link AutoWire} to identify the {@link OfficeFloorTeam}.
	 * @param officeTeamAutoWire
	 *            {@link AutoWire} to identify the {@link OfficeTeam}.
	 */
	protected void recordLinkTeamToOffice(AutoWire teamAutoWire,
			AutoWire officeTeamAutoWire) {

		// Obtain the OfficeFloorTeam
		OfficeFloorTeam officeFloorTeam = this.teams.get(teamAutoWire);
		if (officeFloorTeam == null) {
			// Ensure have OfficeFloorTeam
			fail("No OfficeFloorTeam by name "
					+ teamAutoWire.getQualifiedType()
					+ " has been recorded as built");
		}

		// Record obtaining the OfficeTeam
		OfficeTeam officeTeam = this.createMock(OfficeTeam.class);
		this.recordReturn(this.office, this.office
				.getDeployedOfficeTeam(officeTeamAutoWire.getQualifiedType()),
				officeTeam);

		// Record linking OfficeTeam with OfficeFloorTeam
		this.deployer.link(officeTeam, officeFloorTeam);
	}

	/**
	 * Records the {@link OfficeManagedObjectType}.
	 * 
	 * @param objectAutoWire
	 *            {@link AutoWire} for the {@link OfficeManagedObjectType}.
	 */
	protected void registerOfficeObject(AutoWire objectAutoWire) {
		this.officeObjectTypes.add(objectAutoWire);
	}

	/**
	 * Records the {@link OfficeAvailableSectionInputType}.
	 * 
	 * @param sectionName
	 *            Name of the {@link OfficeSection}.
	 * @param inputName
	 *            Name of the {@link OfficeSectionInput}.
	 */
	protected void registerOfficeInput(String sectionName, String inputName) {
		this.officeInputTypes.add(new AutoWire(sectionName, inputName));
	}

	/**
	 * Records the {@link OfficeTeamType}.
	 * 
	 * @param teamAutoWire
	 *            {@link AutoWire} for the {@link OfficeTeamType}.
	 */
	protected void registerOfficeTeam(AutoWire teamAutoWire) {
		this.officeTeamTypes.add(teamAutoWire);
	}

	/**
	 * Register the default {@link OfficeTeamType}.
	 */
	protected void registerDefaultOfficeTeam() {
		this.registerOfficeTeam(new AutoWire("team"));
	}

	/**
	 * Records the office.
	 * 
	 * @param officeObjectAutoWiring
	 *            Convenience to add additional {@link AutoWire} instances to
	 *            identify the used {@link OfficeManagedObjectType} instances.
	 * @return {@link DeployedOffice}.
	 */
	protected DeployedOffice recordOffice(AutoWire... officeObjectAutoWiring) {

		final PropertyList propertyList = this.createMock(PropertyList.class);
		final OfficeType officeType = this.createMock(OfficeType.class);

		// Create the office input types
		final OfficeAvailableSectionInputType[] officeInputs = new OfficeAvailableSectionInputType[this.officeInputTypes
				.size()];
		for (int i = 0; i < officeInputs.length; i++) {
			officeInputs[i] = this.createMock(OfficeAvailableSectionInputType.class);
		}

		// Create the office team types
		final OfficeTeamType[] officeTeams = new OfficeTeamType[this.officeTeamTypes
				.size()];
		for (int i = 0; i < officeTeams.length; i++) {
			officeTeams[i] = this.createMock(OfficeTeamType.class);
		}

		// Add the additional object auto-wiring
		this.officeObjectTypes.addAll(Arrays.asList(officeObjectAutoWiring));

		// Create the office managed object types
		final OfficeManagedObjectType[] officeObjects = new OfficeManagedObjectType[this.officeObjectTypes
				.size()];
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
		this.recordReturn(officeType, officeType.getOfficeSectionInputTypes(),
				officeInputs);
		for (int i = 0; i < officeInputs.length; i++) {
			OfficeAvailableSectionInputType officeInput = officeInputs[i];
			AutoWire handledInput = this.officeInputTypes.get(i);
			this.recordReturn(officeInput, officeInput.getOfficeSectionName(),
					handledInput.getQualifier());
			this.recordReturn(officeInput,
					officeInput.getOfficeSectionInputName(),
					handledInput.getType());
		}

		// Record the office team types
		this.recordReturn(officeType, officeType.getOfficeTeamTypes(),
				officeTeams);
		for (int i = 0; i < officeTeams.length; i++) {
			OfficeTeamType officeTeam = officeTeams[i];
			AutoWire officeTeamAutoWire = this.officeTeamTypes.get(i);
			this.recordReturn(officeTeam, officeTeam.getOfficeTeamName(),
					officeTeamAutoWire.getQualifiedType());
		}

		// Record the office managed object types
		this.recordReturn(officeType, officeType.getOfficeManagedObjectTypes(),
				officeObjects);
		for (int i = 0; i < officeObjects.length; i++) {
			OfficeManagedObjectType officeObject = officeObjects[i];
			AutoWire officeObjectAutoWire = this.officeObjectTypes.get(i);
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

		// Raw object should never be input managed object
		this.recordReturn(this.context,
				this.context.isInputManagedObject(managedObjectType), false);

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

		// Record whether an input managed object
		List<String> flowNames = this.managedObjectFlowTypes.get(object);
		boolean isInputManagedObject = (flowNames == null ? false : (flowNames
				.size() > 0));
		this.recordReturn(this.context,
				this.context.isInputManagedObject(managedObjectType),
				isInputManagedObject);

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
	 * Convenience method to record building both the
	 * {@link OfficeFloorSupplier} and the
	 * {@link OfficeFloorManagedObjectSource}.
	 * 
	 * @param identifier
	 *            Identifier of the {@link OfficeFloorSupplier}.
	 * @param autoWire
	 *            {@link AutoWire}.
	 * @return {@link OfficeFloorManagedObjectSource}.
	 */
	protected OfficeFloorManagedObjectSource recordSuppliedManagedObjectSource(
			int identifier, AutoWire autoWire) {
		this.recordSupplier(identifier);
		return this.recordManagedObjectSource(identifier, autoWire);
	}

	/**
	 * Records a {@link OfficeFloorSupplier}.
	 * 
	 * @param identifier
	 *            Identifier for the {@link OfficeFloorSupplier}.
	 * @return {@link OfficeFloorSupplier}.
	 */
	protected OfficeFloorSupplier recordSupplier(int identifier) {

		final OfficeFloorSupplier supplier = this
				.createMock(OfficeFloorSupplier.class);

		// Record adding the Supplier
		this.recordReturn(this.deployer, this.deployer.addSupplier(
				DynamicSupplierSource.class.getName(),
				DynamicSupplierSource.class.getName()), supplier);

		// Record properties for supplier
		supplier.addProperty(
				DynamicSupplierSource.PROPERTY_SUPPLIER_IDENTIFIER,
				String.valueOf(identifier));

		// Register the supplier
		this.suppliers.put(Integer.valueOf(identifier), supplier);

		// Return the supplier
		return supplier;
	}

	/**
	 * Records a {@link SuppliedManagedObject}.
	 * 
	 * @param identifier
	 *            Identifier of the {@link OfficeFloorSupplier}.
	 * @param autoWire
	 *            {@link AutoWire} to identify the {@link ManagedObjectSource}.
	 * @return {@link OfficeFloorManagedObjectSource}.
	 */
	protected OfficeFloorManagedObjectSource recordManagedObjectSource(
			int identifier, AutoWire autoWire) {

		// Obtain the supplier
		OfficeFloorSupplier supplier = this.suppliers.get(Integer
				.valueOf(identifier));
		if (supplier == null) {
			fail("Unknown identifier " + identifier + " to obtain supplier");
		}

		final OfficeFloorManagedObjectSource source = this
				.createMock(OfficeFloorManagedObjectSource.class);

		// Record obtaining the managed object source
		this.recordReturn(supplier, supplier.addManagedObjectSource(
				autoWire.getQualifiedType(), autoWire), source);

		// Record managed by office
		ManagingOffice managingOffice = this.createMock(ManagingOffice.class);
		this.recordReturn(source, source.getManagingOffice(), managingOffice);
		this.deployer.link(managingOffice, this.office);

		// Return the managed object source
		return source;
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
		String managedObjectSourceName = mosName.getQualifiedType()
				+ (typeIndex <= 0 ? "" : String.valueOf(typeIndex));
		if (managedObjectSource == null) {
			// Build from class
			this.recordReturn(this.deployer, this.deployer
					.addManagedObjectSource(managedObjectSourceName,
							managedObjectSourceClass.getName()), source);
		} else {
			// Build from instance
			this.recordReturn(this.deployer, this.deployer
					.addManagedObjectSource(managedObjectSourceName,
							managedObjectSource), source);
		}

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

		// Record managed by office
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
				autoWire.getQualifiedType(), ManagedObjectScope.THREAD), mo);
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
		this.recordReturn(source,
				source.getOfficeFloorManagedObjectSourceName(),
				"TestManagedObjectSourceName");
		OfficeFloorTeam team = this.recordTeam(teamSourceClass, new AutoWire(
				"TestManagedObjectSourceName", managedObjectTeamName),
				propertyNameValues);

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
		if (officeFloorTeam == null) {
			fail("No OfficeFloorTeam " + autoWire.getQualifiedType()
					+ " recorded to link to ManagedObjectTeam");
		}

		// Record linking team
		this.deployer.link(moTeam, officeFloorTeam);
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