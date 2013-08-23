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
package net.officefloor.model.objects;

import java.util.List;

import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.model.RemoveConnectionsAction;
import net.officefloor.model.impl.repository.ModelRepositoryImpl;
import net.officefloor.model.impl.repository.filesystem.FileSystemConfigurationItem;
import net.officefloor.model.impl.repository.memory.MemoryConfigurationItem;
import net.officefloor.model.repository.ConfigurationItem;
import net.officefloor.model.repository.ModelRepository;

/**
 * Tests the marshaling/unmarshaling of the {@link AutoWireObjectsModel} via the
 * {@link ModelRepository}.
 * 
 * @author Daniel Sagenschneider
 */
public class AutoWireObjectsModelRepositoryTest extends OfficeFrameTestCase {

	/**
	 * {@link ConfigurationItem} containing the {@link AutoWireObjectsModel}.
	 */
	private ConfigurationItem configurationItem;

	@Override
	protected void setUp() throws Exception {
		// Specify location of the configuration
		this.configurationItem = new FileSystemConfigurationItem(this.findFile(
				this.getClass(), "Objects.objects.xml"), null);
	}

	/**
	 * Ensure retrieve the {@link AutoWireObjectsModel}.
	 */
	public void testRetrieveObjects() throws Exception {

		// Load the Objects
		ModelRepository repository = new ModelRepositoryImpl();
		AutoWireObjectsModel objects = new AutoWireObjectsModel();
		objects = repository.retrieve(objects, this.configurationItem);

		// ----------------------------------------
		// Validate the objects
		// ----------------------------------------
		List<AutoWireObjectSourceModel> objectSources = objects
				.getAutoWireObjectSources();
		assertEquals("Incorrect number of auto-wire object sources", 5,
				objectSources.size());

		final String[] MANAGED_OBJECT_METHODS = new String[] {
				"getManagedObjectSourceClassName",
				"getClassManagedObjectSourceClass", "getTimeout",
				"getQualifier", "getType" };

		// Validate the first object source (managed object)
		AutoWireManagedObjectModel moOne = assertType(
				AutoWireManagedObjectModel.class, objectSources.get(0));
		assertProperties(
				new AutoWireManagedObjectModel(
						"net.example.ExampleManagedObjectSourceA", null, 10,
						null, null), moOne, MANAGED_OBJECT_METHODS);
		assertProperties(new PropertyModel("MO_ONE", "VALUE_ONE"),
				new PropertyFileModel("example/object.properties"),
				new PropertyModel("MO_TWO", "VALUE_TWO"),
				moOne.getPropertySources());
		assertList(new String[] { "getQualifier", "getType" },
				moOne.getAutoWiring(), new AutoWireModel("QUALIFIED",
						"net.orm.Session"), new AutoWireModel(null,
						"net.orm.SessionLocal"));
		assertList(new String[] { "getName", "getSection", "getInput" },
				moOne.getFlows(), new AutoWireFlowModel("FLOW", "SECTION",
						"INPUT"));
		assertList(new String[] { "getName", "getQualifier", "getType" },
				moOne.getTeams(), new AutoWireTeamModel("TEAM", "QUALIFIER",
						"net.example.Type"));
		assertList(new String[] { "getName", "getQualifier", "getType" },
				moOne.getDependencies(), new AutoWireDependencyModel(
						"DEPENDENCY", "QUALIFIER", "net.example.Dependency"));

		// Validate the second object source (supplier)
		AutoWireSupplierModel supplierOne = assertType(
				AutoWireSupplierModel.class, objectSources.get(1));
		assertProperties(new AutoWireSupplierModel(
				"net.example.ExampleSupplierSourceA"), supplierOne,
				"getSupplierSourceClassName");
		assertProperties(new PropertyModel("SUPPLIER_A", "VALUE_A"),
				new PropertyFileModel("example/supplier.properties"),
				new PropertyModel("SUPPLIER_B", "VALUE_B"),
				supplierOne.getPropertySources());

		// Validate the third object source (managed object shortcut entry)
		assertProperties(
				new AutoWireManagedObjectModel(
						"net.example.ExampleManagedObjectSourceB", null, 0,
						"QUALIFIER", "net.example.Type"),
				assertType(AutoWireManagedObjectModel.class,
						objectSources.get(2)), MANAGED_OBJECT_METHODS);

		// Validate the fourth object source (supplier)
		assertProperties(new AutoWireSupplierModel(
				"net.example.ExampleSupplierSourceB"),
				assertType(AutoWireSupplierModel.class, objectSources.get(3)),
				"getSupplierSourceClassName");

		// Validate the fifth object source (managed object class/POJO shortcut)
		assertProperties(
				new AutoWireManagedObjectModel(null,
						"net.example.ExampleClass", 0, null, null),
				assertType(AutoWireManagedObjectModel.class,
						objectSources.get(4)), MANAGED_OBJECT_METHODS);
	}

	/**
	 * Asserts the object is of the type.
	 * 
	 * @param type
	 *            Expected type.
	 * @param object
	 *            Object to validate.
	 * @return Object cast to type for convenience.
	 */
	@SuppressWarnings("unchecked")
	private static <T> T assertType(Class<T> type, Object object) {
		assertEquals("Incorrect object type", type, object.getClass());
		return (T) object;
	}

	/**
	 * Asserts the {@link PropertySourceModel}.
	 * 
	 * @param propertyOne
	 *            Expected {@link PropertyModel}.
	 * @param propertyFile
	 *            Expected {@link PropertyFileModel}.
	 * @param propertyTwo
	 *            Expected {@link PropertyModel}.
	 * @param actual
	 *            Actual {@link PropertySourceModel} instances.
	 */
	private static void assertProperties(PropertyModel propertyOne,
			PropertyFileModel propertyFile, PropertyModel propertyTwo,
			List<PropertySourceModel> actual) {
		assertEquals("Incorrect number of property sources", 3, actual.size());
		assertProperties(propertyOne,
				assertType(PropertyModel.class, actual.get(0)), "getName",
				"getValue");
		assertProperties(propertyFile,
				assertType(PropertyFileModel.class, actual.get(1)), "getPath");
		assertProperties(propertyTwo,
				assertType(PropertyModel.class, actual.get(2)), "getName",
				"getValue");
	}

	/**
	 * Ensure able to round trip storing and retrieving the
	 * {@link AutoWireObjectsModel}.
	 */
	public void testRoundTripStoreRetrieveObjects() throws Exception {

		// Load the objects
		ModelRepository repository = new ModelRepositoryImpl();
		AutoWireObjectsModel objects = new AutoWireObjectsModel();
		objects = repository.retrieve(objects, this.configurationItem);

		// Store the objects
		MemoryConfigurationItem contents = new MemoryConfigurationItem();
		repository.store(objects, contents);

		// Reload the objects
		AutoWireObjectsModel reloadedObjects = new AutoWireObjectsModel();
		reloadedObjects = repository.retrieve(reloadedObjects, contents);

		// Validate round trip
		assertGraph(objects, reloadedObjects,
				RemoveConnectionsAction.REMOVE_CONNECTIONS_METHOD_NAME);
	}

}