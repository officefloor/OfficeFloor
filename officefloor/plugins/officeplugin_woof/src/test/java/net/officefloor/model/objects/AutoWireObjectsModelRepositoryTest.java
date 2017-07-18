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

import net.officefloor.configuration.ConfigurationItem;
import net.officefloor.configuration.impl.filesystem.FileSystemConfigurationItem;
import net.officefloor.configuration.impl.memory.MemoryConfigurationItem;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.model.RemoveConnectionsAction;
import net.officefloor.model.impl.repository.ModelRepositoryImpl;
import net.officefloor.model.repository.ModelRepository;

/**
 * Tests the marshaling/unmarshaling of the {@link WoofObjectsModel} via the
 * {@link ModelRepository}.
 * 
 * @author Daniel Sagenschneider
 */
public class AutoWireObjectsModelRepositoryTest extends OfficeFrameTestCase {

	/**
	 * {@link ConfigurationItem} containing the {@link WoofObjectsModel}.
	 */
	private ConfigurationItem configurationItem;

	@Override
	protected void setUp() throws Exception {
		// Specify location of the configuration
		this.configurationItem = new FileSystemConfigurationItem(this.findFile(
				this.getClass(), "Objects.objects.xml"), null);
	}

	/**
	 * Ensure retrieve the {@link WoofObjectsModel}.
	 */
	public void testRetrieveObjects() throws Exception {

		// Load the Objects
		ModelRepository repository = new ModelRepositoryImpl();
		WoofObjectsModel objects = new WoofObjectsModel();
		objects = repository.retrieve(objects, this.configurationItem);

		// ----------------------------------------
		// Validate the objects
		// ----------------------------------------
		List<WoofObjectSourceModel> objectSources = objects
				.getAutoWireObjectSources();
		assertEquals("Incorrect number of auto-wire object sources", 5,
				objectSources.size());

		final String[] MANAGED_OBJECT_METHODS = new String[] {
				"getManagedObjectSourceClassName",
				"getClassManagedObjectSourceClass", "getTimeout",
				"getQualifier", "getType", "getScope" };

		// Validate the first object source (managed object)
		WoofManagedObjectModel moOne = assertType(
				WoofManagedObjectModel.class, objectSources.get(0));
		assertProperties(new WoofManagedObjectModel(
				"net.example.ExampleManagedObjectSourceA", null, 10, null,
				null, "thread"), moOne, MANAGED_OBJECT_METHODS);
		assertProperties(new PropertyModel("MO_ONE", "VALUE_ONE"),
				new PropertyFileModel("example/object.properties"),
				new PropertyModel("MO_TWO", "VALUE_TWO"),
				moOne.getPropertySources());
		assertList(new String[] { "getQualifier", "getType" },
				moOne.getAutoWiring(), new TypeQualificationModel("QUALIFIED",
						"net.orm.Session"), new TypeQualificationModel(null,
						"net.orm.SessionLocal"));
		assertList(new String[] { "getName", "getSection", "getInput" },
				moOne.getFlows(), new WoofFlowModel("FLOW", "SECTION",
						"INPUT"));
		assertList(new String[] { "getName", "getQualifier", "getType" },
				moOne.getTeams(), new WoofTeamModel("TEAM", "QUALIFIER",
						"net.example.Type"));
		assertList(new String[] { "getName", "getQualifier", "getType" },
				moOne.getDependencies(), new WoofDependencyModel(
						"DEPENDENCY", "QUALIFIER", "net.example.Dependency"));

		// Validate the second object source (supplier)
		WoofSupplierModel supplierOne = assertType(
				WoofSupplierModel.class, objectSources.get(1));
		assertProperties(new WoofSupplierModel(
				"net.example.ExampleSupplierSourceA"), supplierOne,
				"getSupplierSourceClassName");
		assertProperties(new PropertyModel("SUPPLIER_A", "VALUE_A"),
				new PropertyFileModel("example/supplier.properties"),
				new PropertyModel("SUPPLIER_B", "VALUE_B"),
				supplierOne.getPropertySources());

		// Validate the third object source (managed object shortcut entry)
		assertProperties(
				new WoofManagedObjectModel(
						"net.example.ExampleManagedObjectSourceB", null, 0,
						"QUALIFIER", "net.example.Type", "process"),
				assertType(WoofManagedObjectModel.class,
						objectSources.get(2)), MANAGED_OBJECT_METHODS);

		// Validate the fourth object source (supplier)
		assertProperties(new WoofSupplierModel(
				"net.example.ExampleSupplierSourceB"),
				assertType(WoofSupplierModel.class, objectSources.get(3)),
				"getSupplierSourceClassName");

		// Validate the fifth object source (managed object class/POJO shortcut)
		assertProperties(
				new WoofManagedObjectModel(null,
						"net.example.ExampleClass", 0, null, null, null),
				assertType(WoofManagedObjectModel.class,
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
	 * {@link WoofObjectsModel}.
	 */
	public void testRoundTripStoreRetrieveObjects() throws Exception {

		// Load the objects
		ModelRepository repository = new ModelRepositoryImpl();
		WoofObjectsModel objects = new WoofObjectsModel();
		objects = repository.retrieve(objects, this.configurationItem);

		// Store the objects
		MemoryConfigurationItem contents = new MemoryConfigurationItem();
		repository.store(objects, contents);

		// Reload the objects
		WoofObjectsModel reloadedObjects = new WoofObjectsModel();
		reloadedObjects = repository.retrieve(reloadedObjects, contents);

		// Validate round trip
		assertGraph(objects, reloadedObjects,
				RemoveConnectionsAction.REMOVE_CONNECTIONS_METHOD_NAME);
	}

}