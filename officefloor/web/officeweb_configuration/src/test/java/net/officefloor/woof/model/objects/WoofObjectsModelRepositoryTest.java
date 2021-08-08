/*-
 * #%L
 * Web configuration
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package net.officefloor.woof.model.objects;

import java.util.List;

import net.officefloor.configuration.ConfigurationItem;
import net.officefloor.configuration.WritableConfigurationItem;
import net.officefloor.configuration.impl.configuration.FileSystemConfigurationContext;
import net.officefloor.configuration.impl.configuration.MemoryConfigurationContext;
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
public class WoofObjectsModelRepositoryTest extends OfficeFrameTestCase {

	/**
	 * {@link ConfigurationItem} containing the {@link WoofObjectsModel}.
	 */
	private ConfigurationItem configurationItem;

	@Override
	protected void setUp() throws Exception {
		// Specify location of the configuration
		this.configurationItem = FileSystemConfigurationContext
				.createWritableConfigurationItem(this.findFile(this.getClass(), "Objects.objects.xml"));
	}

	/**
	 * Ensure retrieve the {@link WoofObjectsModel}.
	 */
	public void testRetrieveObjects() throws Exception {

		// Load the Objects
		ModelRepository repository = new ModelRepositoryImpl();
		WoofObjectsModel objects = new WoofObjectsModel();
		repository.retrieve(objects, this.configurationItem);

		// ----------------------------------------
		// Validate the objects
		// ----------------------------------------
		List<WoofObjectSourceModel> objectSources = objects.getWoofObjectSources();
		assertEquals("Incorrect number of auto-wire object sources", 5, objectSources.size());

		final String[] MANAGED_OBJECT_METHODS = new String[] { "getManagedObjectSourceClassName",
				"getClassManagedObjectSourceClass", "getTimeout", "getQualifier", "getType", "getScope" };

		// Validate the first object source (managed object)
		WoofManagedObjectModel moOne = assertType(WoofManagedObjectModel.class, objectSources.get(0));
		assertProperties(
				new WoofManagedObjectModel("net.example.ExampleManagedObjectSourceA", null, 10, null, null, "thread"),
				moOne, MANAGED_OBJECT_METHODS);
		PropertySourceModel[] ePropertySources = new PropertySourceModel[] { new PropertyModel("MO_ONE", "VALUE_ONE"),
				new PropertyFileModel("example/object.properties"), new PropertyModel("MO_TWO", "VALUE_TWO") };
		List<PropertySourceModel> aPropertySources = moOne.getPropertySources();
		assertEquals("Incorrect number of property sources", ePropertySources.length, aPropertySources.size());
		assertProperties(ePropertySources[0], aPropertySources.get(0), "getName", "getValue");
		assertProperties(ePropertySources[1], aPropertySources.get(1), "getPath");
		assertProperties(ePropertySources[2], aPropertySources.get(2), "getName", "getValue");
		assertList(new String[] { "getQualifier", "getType" }, moOne.getTypeQualifications(),
				new TypeQualificationModel("QUALIFIED", "net.orm.Session"),
				new TypeQualificationModel(null, "net.orm.SessionLocal"));
		assertList(new String[] { "getName", "getSection", "getInput" }, moOne.getFlows(),
				new WoofFlowModel("FLOW", "SECTION", "INPUT"));
		assertList(new String[] { "getName", "getQualifier", "getType" }, moOne.getDependencies(),
				new WoofDependencyModel("DEPENDENCY", "QUALIFIER", "net.example.Dependency"));
		assertList(new String[] { "getManagedObjectType" }, moOne.getStartBefores(),
				new WoofStartBeforeModel("net.example.ExampleManagedObjectSourceB"));
		assertList(new String[] { "getManagedObjectType" }, moOne.getStartAfters(),
				new WoofStartAfterModel("net.example.ExampleClass"));

		// Validate the pool
		WoofPoolModel pool = moOne.getPool();
		assertProperties(new WoofPoolModel("net.example.ExampleManagedObjectPoolSource"), pool,
				"getManagedObjectPoolSourceClassName");
		PropertySourceModel[] ePoolProperties = new PropertySourceModel[] { new PropertyModel("POOL_ONE", "VALUE_ONE"),
				new PropertyFileModel("example/pool.properties") };
		List<PropertySourceModel> aPoolProperties = pool.getPropertySources();
		assertEquals("Incorrect number of pool property sources", ePoolProperties.length, aPoolProperties.size());
		assertProperties(ePoolProperties[0], aPoolProperties.get(0), "getName", "getValue");
		assertProperties(ePoolProperties[1], aPoolProperties.get(1), "getPath");

		// Validate the second object source (supplier)
		WoofSupplierModel supplierOne = assertType(WoofSupplierModel.class, objectSources.get(1));
		assertProperties(new WoofSupplierModel("net.example.ExampleSupplierSourceA"), supplierOne,
				"getSupplierSourceClassName");
		assertProperties(new PropertyModel("SUPPLIER_A", "VALUE_A"),
				new PropertyFileModel("example/supplier.properties"), new PropertyModel("SUPPLIER_B", "VALUE_B"),
				supplierOne.getPropertySources());

		// Validate the third object source (managed object shortcut entry)
		assertProperties(
				new WoofManagedObjectModel("net.example.ExampleManagedObjectSourceB", null, 0, "QUALIFIER",
						"net.example.Type", "process"),
				assertType(WoofManagedObjectModel.class, objectSources.get(2)), MANAGED_OBJECT_METHODS);

		// Validate the fourth object source (supplier)
		assertProperties(new WoofSupplierModel("net.example.ExampleSupplierSourceB"),
				assertType(WoofSupplierModel.class, objectSources.get(3)), "getSupplierSourceClassName");

		// Validate the fifth object source (managed object class/POJO shortcut)
		assertProperties(new WoofManagedObjectModel(null, "net.example.ExampleClass", 0, null, null, null),
				assertType(WoofManagedObjectModel.class, objectSources.get(4)), MANAGED_OBJECT_METHODS);
	}

	/**
	 * Asserts the object is of the type.
	 * 
	 * @param type   Expected type.
	 * @param object Object to validate.
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
	 * @param propertyOne  Expected {@link PropertyModel}.
	 * @param propertyFile Expected {@link PropertyFileModel}.
	 * @param propertyTwo  Expected {@link PropertyModel}.
	 * @param actual       Actual {@link PropertySourceModel} instances.
	 */
	private static void assertProperties(PropertyModel propertyOne, PropertyFileModel propertyFile,
			PropertyModel propertyTwo, List<PropertySourceModel> actual) {
		assertEquals("Incorrect number of property sources", 3, actual.size());
		assertProperties(propertyOne, assertType(PropertyModel.class, actual.get(0)), "getName", "getValue");
		assertProperties(propertyFile, assertType(PropertyFileModel.class, actual.get(1)), "getPath");
		assertProperties(propertyTwo, assertType(PropertyModel.class, actual.get(2)), "getName", "getValue");
	}

	/**
	 * Ensure able to round trip storing and retrieving the
	 * {@link WoofObjectsModel}.
	 */
	public void testRoundTripStoreRetrieveObjects() throws Exception {

		// Load the objects
		ModelRepository repository = new ModelRepositoryImpl();
		WoofObjectsModel objects = new WoofObjectsModel();
		repository.retrieve(objects, this.configurationItem);

		// Store the objects
		WritableConfigurationItem contents = MemoryConfigurationContext.createWritableConfigurationItem("test");
		repository.store(objects, contents);

		// Reload the objects
		WoofObjectsModel reloadedObjects = new WoofObjectsModel();
		repository.retrieve(reloadedObjects, contents);

		// Validate round trip
		assertGraph(objects, reloadedObjects, RemoveConnectionsAction.REMOVE_CONNECTIONS_METHOD_NAME);
	}

}
