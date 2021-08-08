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

package net.officefloor.woof.objects;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.function.BiConsumer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.issues.CompileError;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.managedobject.ManagedObjectDependency;
import net.officefloor.compile.spi.managedobject.ManagedObjectFlow;
import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.OfficeManagedObject;
import net.officefloor.compile.spi.office.OfficeManagedObjectDependency;
import net.officefloor.compile.spi.office.OfficeManagedObjectFlow;
import net.officefloor.compile.spi.office.OfficeManagedObjectPool;
import net.officefloor.compile.spi.office.OfficeManagedObjectSource;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.office.OfficeSectionInput;
import net.officefloor.compile.spi.office.OfficeSupplier;
import net.officefloor.compile.spi.office.extension.OfficeExtensionContext;
import net.officefloor.compile.spi.pool.source.ManagedObjectPoolSource;
import net.officefloor.compile.spi.supplier.source.SupplierSource;
import net.officefloor.configuration.ConfigurationContext;
import net.officefloor.configuration.ConfigurationItem;
import net.officefloor.configuration.impl.configuration.ClassLoaderConfigurationContext;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.test.FileTestSupport;
import net.officefloor.frame.test.MockTestSupport;
import net.officefloor.frame.test.TestSupportExtension;
import net.officefloor.model.impl.repository.ModelRepositoryImpl;
import net.officefloor.plugin.managedobject.clazz.ClassManagedObjectSource;
import net.officefloor.woof.model.objects.WoofObjectsRepositoryImpl;

/**
 * Tests the {@link WoofObjectsLoader}.
 * 
 * @author Daniel Sagenschneider
 */
@ExtendWith(TestSupportExtension.class)
public class WoofObjectsLoaderTest {

	/**
	 * {@link OfficeFloorCompiler}.
	 */
	private final OfficeFloorCompiler compiler = OfficeFloorCompiler.newOfficeFloorCompiler(null);

	/**
	 * {@link WoofObjectsLoader} to test.
	 */
	private final WoofObjectsLoader loader = new WoofObjectsLoaderImpl(
			new WoofObjectsRepositoryImpl(new ModelRepositoryImpl()));

	/**
	 * {@link MockTestSupport}.
	 */
	private final MockTestSupport mocks = new MockTestSupport();

	/**
	 * {@link FileTestSupport}.
	 */
	private final FileTestSupport files = new FileTestSupport();

	/**
	 * Mock {@link WoofObjectsLoaderContext}.
	 */
	private WoofObjectsLoaderContext loaderContext;

	/**
	 * Mock {@link OfficeArchitect}.
	 */
	private OfficeArchitect office;

	/**
	 * Mock {@link OfficeExtensionContext}.
	 */
	private OfficeExtensionContext extensionContext;

	@BeforeEach
	public void setup() {
		this.loaderContext = this.mocks.createMock(WoofObjectsLoaderContext.class);
		this.office = this.mocks.createMock(OfficeArchitect.class);
		this.extensionContext = this.mocks.createMock(OfficeExtensionContext.class);
	}

	/**
	 * Ensure can load configuration to {@link OfficeArchitect} with objects.
	 */
	@Test
	public void loading() throws Exception {

		// Initialise loading
		this.recordInitLoader("load.objects.xml");

		// Record managed object source
		OfficeManagedObjectSource mosOne = this.recordManagedObjectSource("QUALIFIED:net.orm.Session",
				"net.example.ExampleManagedObjectSourceA", 10, "MO_ONE", "VALUE_ONE", "file-example/object.properties",
				"MO_TWO=VALUE_TWO", "MO_THREE", "VALUE_THREE");

		// Record link pool
		this.recordManagedObjectPool("QUALIFIED:net.orm.Session_pool", "net.example.ExampleManagedObjectPoolSource",
				mosOne, "POOL_ONE", "VALUE_ONE", "file-example/pool.properties", "POOL_TWO=VALUE_TWO");

		// Record linking flow
		this.recordManagedObjectFlow(mosOne, "FLOW", "SECTION", "INPUT");

		// Record managed object
		OfficeManagedObject moOne = this.recordManagedObject(mosOne, "QUALIFIED:net.orm.Session",
				ManagedObjectScope.PROCESS, "QUALIFIED", "net.orm.Session", null, "net.orm.SessionLocal");

		// Record dependency
		this.recordManagedObjectDependency(moOne, "DEPENDENCY", "QUALIFIER", "net.example.Dependency");

		// Record start before/after
		this.office.startBefore(mosOne, "net.example.ExampleManagedObjectSourceB");
		this.office.startAfter(mosOne, "net.example.ExampleClass");

		// Record first supplier
		this.recordSupplier("net.example.ExampleSupplierSourceA", "net.example.ExampleSupplierSourceA", "SUPPLIER_A",
				"VALUE_A", "file-example/supplier.properties", "SUPPLIER_B=VALUE_B", "SUPPLIER_C", "VALUE_C");

		// Record second managed object
		OfficeManagedObjectSource mosTwo = this.recordManagedObjectSource("QUALIFIER:net.example.Type",
				"net.example.ExampleManagedObjectSourceB", 0);
		this.recordManagedObject(mosTwo, "QUALIFIER:net.example.Type", ManagedObjectScope.THREAD, "QUALIFIER",
				"net.example.Type");

		// Record second supplier
		this.recordSupplier("net.example.ExampleSupplierSourceB", "net.example.ExampleSupplierSourceB");

		// Test
		this.mocks.replayMockObjects();
		this.loader.loadWoofObjectsConfiguration(this.loaderContext);
		this.mocks.verifyMockObjects();
	}

	/**
	 * Ensure can load {@link ClassManagedObjectSource} shortcut configuration.
	 */
	@Test
	public void classShortcuts() throws Exception {

		// Record initialise loader
		this.recordInitLoader("class.objects.xml");

		// Record class A
		OfficeManagedObjectSource mosA = this.recordManagedObjectSource("net.example.ExampleClassA",
				ClassManagedObjectSource.class.getName(), 0, ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME,
				"net.example.ExampleClassA");
		this.recordManagedObject(mosA, "net.example.ExampleClassA", ManagedObjectScope.THREAD);

		// Record class B
		OfficeManagedObjectSource mosB = this.recordManagedObjectSource("QUALIFIER:net.example.ExampleClassB",
				ClassManagedObjectSource.class.getName(), 0, ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME,
				"net.example.ExampleClassB");
		this.recordManagedObject(mosB, "QUALIFIER:net.example.ExampleClassB", ManagedObjectScope.THREAD, "QUALIFIER",
				"net.example.ExampleClassB");

		// Record class C
		OfficeManagedObjectSource mosC = this.recordManagedObjectSource("net.example.Type",
				ClassManagedObjectSource.class.getName(), 0, ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME,
				"net.example.ExampleClassC");
		this.recordManagedObject(mosC, "net.example.Type", ManagedObjectScope.THREAD, null, "net.example.Type");

		// Record class D
		OfficeManagedObjectSource mosD = this.recordManagedObjectSource("QUALIFIER:net.example.Type",
				ClassManagedObjectSource.class.getName(), 0, ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME,
				"net.example.ExampleClassD");
		this.recordManagedObject(mosD, "QUALIFIER:net.example.Type", ManagedObjectScope.THREAD, "QUALIFIER",
				"net.example.Type");

		// Record class E
		OfficeManagedObjectSource mosE = this.recordManagedObjectSource("net.example.Type",
				ClassManagedObjectSource.class.getName(), 0, ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME,
				"net.example.ExampleClassE");
		this.recordManagedObject(mosE, "net.example.Type", ManagedObjectScope.THREAD, null, "net.example.Type");

		// Record object F
		OfficeManagedObjectSource mosF = this.recordManagedObjectSource("QUALIFIED:net.orm.Session",
				ClassManagedObjectSource.class.getName(), 10, ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME,
				"net.example.ExampleClassF", "MO_ONE", "VALUE_ONE", "file-example/object.properties",
				"MO_TWO=VALUE_TWO", "MO_THREE", "VALUE_THREE");
		this.recordManagedObjectFlow(mosF, "FLOW", "SECTION", "INPUT");
		OfficeManagedObject moF = this.recordManagedObject(mosF, "QUALIFIED:net.orm.Session", ManagedObjectScope.THREAD,
				"QUALIFIED", "net.orm.Session", null, "net.orm.SessionLocal");
		this.recordManagedObjectDependency(moF, "DEPENDENCY", "QUALIFIER", "net.example.Dependency");

		// Record source (ignores class)
		OfficeManagedObjectSource mosG = this.recordManagedObjectSource("net.example.Type",
				"net.example.ExampleManagedObjectSource", 0);
		this.recordManagedObject(mosG, "net.example.Type", ManagedObjectScope.THREAD, null, "net.example.Type");

		// Test
		this.mocks.replayMockObjects();
		this.loader.loadWoofObjectsConfiguration(this.loaderContext);
		this.mocks.verifyMockObjects();
	}

	/**
	 * Ensure handles {@link ManagedObjectScope} values.
	 */
	@Test
	public void managedObjectScopes() throws Exception {

		// Record initialise loader
		this.recordInitLoader("scoped.objects.xml");

		// Record first managed object
		OfficeManagedObjectSource processMos = this.recordManagedObjectSource("net.example.ExampleManagedObjectSourceA",
				"net.example.ExampleManagedObjectSourceA", 0);
		this.recordManagedObject(processMos, "net.example.ExampleManagedObjectSourceA", ManagedObjectScope.PROCESS);

		// Record second managed object
		OfficeManagedObjectSource threadMos = this.recordManagedObjectSource("net.example.ExampleManagedObjectSourceB",
				"net.example.ExampleManagedObjectSourceB", 0);
		this.recordManagedObject(threadMos, "net.example.ExampleManagedObjectSourceB", ManagedObjectScope.THREAD);

		// Record third managed object
		OfficeManagedObjectSource functionMos = this.recordManagedObjectSource(
				"net.example.ExampleManagedObjectSourceC", "net.example.ExampleManagedObjectSourceC", 0);
		this.recordManagedObject(functionMos, "net.example.ExampleManagedObjectSourceC", ManagedObjectScope.FUNCTION);

		// Record fourth managed object (using default scope as not specified)
		OfficeManagedObjectSource defaultMos = this.recordManagedObjectSource("net.example.ExampleManagedObjectSourceD",
				"net.example.ExampleManagedObjectSourceD", 0);
		this.recordManagedObject(defaultMos, "net.example.ExampleManagedObjectSourceD", ManagedObjectScope.THREAD);

		// Fifth managed object should report issue
		this.mocks.recordReturn(this.office, this.office.addIssue(
				"Invalid managed object scope 'invalid' for managed object net.example.ExampleManagedObjectSourceE"),
				new CompileError("TEST"));

		// Test
		this.mocks.replayMockObjects();
		this.loader.loadWoofObjectsConfiguration(this.loaderContext);
		this.mocks.verifyMockObjects();
	}

	/**
	 * Records initialising the {@link WoofObjectsLoader}.
	 * 
	 * @param fileName File name for {@link ConfigurationItem}.
	 */
	private void recordInitLoader(String fileName) throws Exception {

		// Obtain the configuration
		String location = this.files.getFileLocation(this.getClass(), fileName);
		ConfigurationContext context = new ClassLoaderConfigurationContext(this.compiler.getClassLoader(), null);
		ConfigurationItem configuration = context.getConfigurationItem(location, null);
		assertNotNull(configuration, "Can not find configuration '" + fileName + "'");
		this.mocks.recordReturn(this.loaderContext, this.loaderContext.getConfiguration(), configuration);

		// Obtain the application
		this.mocks.recordReturn(this.loaderContext, this.loaderContext.getOfficeArchitect(), this.office);
		this.mocks.recordReturn(this.loaderContext, this.loaderContext.getOfficeExtensionContext(),
				this.extensionContext);
	}

	/**
	 * Records the {@link OfficeManagedObjectSource}.
	 * 
	 * @param managedObjectSourceName      Name of the
	 *                                     {@link OfficeManagedObjectSource}.
	 * @param managedObjectSourceClassName {@link OfficeManagedObjectSource}
	 *                                     {@link Class} name.
	 * @param timeout                      Timeout.
	 * @param propertyNameValuePairs       {@link PropertyList} name/value pairs.
	 * @return Mock {@link OfficeManagedObjectSource}.
	 */
	private OfficeManagedObjectSource recordManagedObjectSource(String managedObjectSourceName,
			String managedObjectSourceClassName, int timeout, String... propertyNameValuePairs) {
		final OfficeManagedObjectSource mos = this.mocks.createMock(OfficeManagedObjectSource.class);
		this.mocks.recordReturn(this.office,
				this.office.addOfficeManagedObjectSource(managedObjectSourceName, managedObjectSourceClassName), mos);
		this.recordProperties(propertyNameValuePairs, (name, value) -> mos.addProperty(name, value));
		if (timeout > 0) {
			mos.setTimeout(timeout);
		}
		return mos;
	}

	/**
	 * Records the {@link OfficeManagedObjectPool}.
	 * 
	 * @param managedObjectPoolName            Name of the
	 *                                         {@link OfficeManagedObjectPool}.
	 * @param managedObjectPoolSourceClassName {@link ManagedObjectPoolSource}
	 *                                         {@link Class} name.
	 * @param managedObjectSource              {@link OfficeManagedObjectSource} to
	 *                                         link the
	 *                                         {@link OfficeManagedObjectPool}.
	 * @param propertyNameValuePairs           {@link PropertyList} name/value
	 *                                         pairs.
	 * @return Mock {@link OfficeManagedObjectPool}.
	 */
	private OfficeManagedObjectPool recordManagedObjectPool(String managedObjectPoolName,
			String managedObjectPoolSourceClassName, OfficeManagedObjectSource managedObjectSource,
			String... propertyNameValuePairs) {
		final OfficeManagedObjectPool pool = this.mocks.createMock(OfficeManagedObjectPool.class);
		this.mocks.recordReturn(this.office,
				this.office.addManagedObjectPool(managedObjectPoolName, managedObjectPoolSourceClassName), pool);
		this.recordProperties(propertyNameValuePairs, (name, value) -> pool.addProperty(name, value));
		this.office.link(managedObjectSource, pool);
		return pool;
	}

	/**
	 * Records the {@link ManagedObjectFlow}.
	 * 
	 * @param mos         Mock {@link OfficeManagedObjectSource}.
	 * @param flowName    Name of {@link ManagedObjectFlow}.
	 * @param sectionName Name of {@link OfficeSection} handling the {@link Flow}.
	 * @param inputName   Name of the {@link OfficeSectionInput} handling the
	 *                    {@link Flow}.
	 */
	private void recordManagedObjectFlow(OfficeManagedObjectSource mos, String flowName, String sectionName,
			String inputName) {
		OfficeManagedObjectFlow mosFlow = this.mocks.createMock(OfficeManagedObjectFlow.class);
		this.mocks.recordReturn(mos, mos.getOfficeManagedObjectFlow(flowName), mosFlow);
		OfficeSection section = this.mocks.createMock(OfficeSection.class);
		this.mocks.recordReturn(this.office, this.office.getOfficeSection(sectionName), section);
		OfficeSectionInput sectionInput = this.mocks.createMock(OfficeSectionInput.class);
		this.mocks.recordReturn(section, section.getOfficeSectionInput(inputName), sectionInput);
		this.office.link(mosFlow, sectionInput);
	}

	/**
	 * Records the {@link OfficeManagedObject}.
	 * 
	 * @param mos                Mock {@link OfficeManagedObjectSource}.
	 * @param managedObjectName  Name of the {@link OfficeManagedObject}.
	 * @param scope              {@link ManagedObjectScope}.
	 * @param typeQualifierPairs Type qualifier name/value pairs.
	 * @return Mock {@link OfficeManagedObject}.
	 */
	private OfficeManagedObject recordManagedObject(OfficeManagedObjectSource mos, String managedObjectName,
			ManagedObjectScope scope, String... typeQualifierPairs) {
		OfficeManagedObject mo = this.mocks.createMock(OfficeManagedObject.class);
		this.mocks.recordReturn(mos, mos.addOfficeManagedObject(managedObjectName, scope), mo);
		for (int i = 0; i < typeQualifierPairs.length; i += 2) {
			mo.addTypeQualification(typeQualifierPairs[i], typeQualifierPairs[i + 1]);
		}
		return mo;
	}

	/**
	 * Records the {@link ManagedObjectDependency}.
	 * 
	 * @param mo             Mock {@link OfficeManagedObject}.
	 * @param dependencyName Name of the {@link ManagedObjectDependency}.
	 * @param qualifier      Qualifier.
	 * @param type           Type.
	 */
	private void recordManagedObjectDependency(OfficeManagedObject mo, String dependencyName, String qualifier,
			String type) {
		OfficeManagedObjectDependency dependency = this.mocks.createMock(OfficeManagedObjectDependency.class);
		this.mocks.recordReturn(mo, mo.getOfficeManagedObjectDependency(dependencyName), dependency);
		dependency.setOverrideQualifier(qualifier);
		dependency.setSpecificType(type);
	}

	/**
	 * Records the {@link OfficeSupplier}.
	 * 
	 * @param supplierName            Name of the {@link OfficeSupplier}.
	 * @param supplierSourceClassName {@link SupplierSource} {@link Class} name.
	 * @param propertyNameValuePairs  {@link PropertyList} name/value pairs.
	 * @return Mock {@link OfficeSupplier}.
	 */
	private OfficeSupplier recordSupplier(String supplierName, String supplierSourceClassName,
			String... propertyNameValuePairs) {
		OfficeSupplier supplier = this.mocks.createMock(OfficeSupplier.class);
		this.mocks.recordReturn(this.office, this.office.addSupplier(supplierName, supplierSourceClassName), supplier);
		this.recordProperties(propertyNameValuePairs, (name, value) -> supplier.addProperty(name, value));
		return supplier;
	}

	/**
	 * Records properties.
	 * 
	 * @param propertyNameValuePairs Property name/value pairs.
	 * @param recordProperty         {@link BiConsumer} to record the property.
	 */
	private void recordProperties(String[] propertyNameValuePairs, BiConsumer<String, String> recordProperty) {
		for (int i = 0; i < propertyNameValuePairs.length; i += 2) {
			String name = propertyNameValuePairs[i];
			String value = propertyNameValuePairs[i + 1];
			if (name.startsWith("file-")) {
				// Load file properties
				String resourcePath = name.split("-")[1];
				this.mocks.recordReturn(this.extensionContext, this.extensionContext.getResource(resourcePath),
						new ByteArrayInputStream(value.getBytes()));
				Properties fileProperties = new Properties();
				try {
					fileProperties.load(new StringReader(value));
				} catch (IOException ex) {
					fail(ex);
				}
				List<String> filePropertyNames = new ArrayList<>(fileProperties.stringPropertyNames());
				filePropertyNames.sort((a, b) -> a.compareTo(b));
				for (String filePropertyName : filePropertyNames) {
					String filePropertyValue = fileProperties.getProperty(filePropertyName);
					recordProperty.accept(filePropertyName, filePropertyValue);
				}
			} else {
				// Load the property
				recordProperty.accept(name, value);
			}
		}
	}

}
