/*-
 * #%L
 * OfficeCompiler
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

package net.officefloor.compile.impl.officefloor;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.util.Properties;

import net.officefloor.compile.impl.managedobject.MockLoadManagedObject;
import net.officefloor.compile.impl.office.MockLoadOfficeSource;
import net.officefloor.compile.impl.structure.ManagedObjectSourceNodeImpl;
import net.officefloor.compile.impl.structure.OfficeFloorNodeImpl;
import net.officefloor.compile.impl.structure.OfficeNodeImpl;
import net.officefloor.compile.impl.structure.SupplierNodeImpl;
import net.officefloor.compile.impl.supplier.MockLoadSupplierSource;
import net.officefloor.compile.internal.structure.OfficeFloorNode;
import net.officefloor.compile.issues.CompilerIssue;
import net.officefloor.compile.managedobject.ManagedObjectType;
import net.officefloor.compile.office.OfficeType;
import net.officefloor.compile.properties.Property;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.office.source.OfficeSource;
import net.officefloor.compile.spi.officefloor.source.OfficeFloorSource;
import net.officefloor.compile.spi.officefloor.source.OfficeFloorSourceContext;
import net.officefloor.compile.spi.officefloor.source.RequiredProperties;
import net.officefloor.compile.supplier.InitialSupplierType;
import net.officefloor.configuration.ConfigurationItem;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.plugin.managedobject.clazz.ClassManagedObjectSource;

/**
 * Tests the {@link OfficeFloorSourceContext} when loading the
 * {@link OfficeFloor}.
 * 
 * @author Daniel Sagenschneider
 */
public class LoadOfficeFloorSourceContextTest extends AbstractOfficeFloorTestCase {

	/**
	 * Ensure issue if fail to instantiate the {@link OfficeFloorSource}.
	 */
	public void testFailInstantiate() {

		final RuntimeException failure = new RuntimeException("instantiate failure");

		// Record failure to instantiate
		this.issues.recordIssue(
				"Failed to instantiate " + MakerOfficeFloorSource.class.getName() + " by default constructor", failure);

		// Attempt to instantiate
		MakerOfficeFloorSource.instantiateFailure = failure;
		this.loadOfficeFloor(false, null);
	}

	/**
	 * Ensure obtain the correct {@link OfficeFloor} location.
	 */
	public void testOfficeFloorLocation() {

		// Record
		this.record_initiateOfficeFloorBuilder();

		// Test
		this.loadOfficeFloor(true, new OfficeFloorMaker() {
			@Override
			public void make(OfficeFloorMakerContext context) {
				assertEquals("Incorrect office location", OFFICE_FLOOR_LOCATION,
						context.getContext().getOfficeFloorLocation());
			}
		});
	}

	/**
	 * Ensure issue if missing {@link Property}.
	 */
	public void testMissingProperty() {

		// Record missing property
		this.issues.recordIssue(OfficeFloorNode.OFFICE_FLOOR_NAME, OfficeFloorNodeImpl.class,
				"Must specify property 'missing'");

		// Attempt to load office floor
		this.loadOfficeFloor(false, new OfficeFloorMaker() {
			@Override
			public void make(OfficeFloorMakerContext context) {
				context.getContext().getProperty("missing");
			}
		});
	}

	/**
	 * Ensure able to get properties.
	 */
	public void testGetProperties() {

		// Record
		this.record_initiateOfficeFloorBuilder();

		// Attempt to load office floor
		this.loadOfficeFloor(true, new OfficeFloorMaker() {
			@Override
			public void make(OfficeFloorMakerContext makerContext) {
				OfficeFloorSourceContext context = makerContext.getContext();
				assertEquals("Ensure get defaulted property", "DEFAULT", context.getProperty("missing", "DEFAULT"));
				assertEquals("Ensure get property ONE", "1", context.getProperty("ONE"));
				assertEquals("Ensure get property TWO", "2", context.getProperty("TWO"));
				String[] names = context.getPropertyNames();
				assertEquals("Incorrect number of property names", 3, names.length);
				assertEquals("Incorrect property name 0", "ONE", names[0]);
				assertEquals("Incorrect property name 1", "TWO", names[1]);
				assertEquals("Incorrect identifier", MakerOfficeFloorSource.MAKER_IDENTIFIER_PROPERTY_NAME, names[2]);
				Properties properties = context.getProperties();
				assertEquals("Incorrect number of properties", 3, properties.size());
				assertEquals("Incorrect property ONE", "1", properties.get("ONE"));
				assertEquals("Incorrect property TWO", "2", properties.get("TWO"));
				assertNotNull("Incorrect identifier",
						properties.get(MakerOfficeFloorSource.MAKER_IDENTIFIER_PROPERTY_NAME));
			}
		}, "ONE", "1", "TWO", "2");
	}

	/**
	 * Ensure issue if missing {@link Class}.
	 */
	public void testMissingClass() {

		// Record missing class
		this.issues.recordIssue(OfficeFloorNode.OFFICE_FLOOR_NAME, OfficeFloorNodeImpl.class,
				"Can not load class 'missing'");

		// Attempt to load office floor
		this.loadOfficeFloor(false, new OfficeFloorMaker() {
			@Override
			public void make(OfficeFloorMakerContext context) {
				context.getContext().loadClass("missing");
			}
		});
	}

	/**
	 * Ensure issue if missing resource.
	 */
	public void testMissingResource() {

		// Record missing resource
		this.recordReturn(this.resourceSource, this.resourceSource.sourceResource("missing"), null);
		this.issues.recordIssue(OfficeFloorNode.OFFICE_FLOOR_NAME, OfficeFloorNodeImpl.class,
				"Can not obtain resource at location 'missing'");

		// Attempt to load office floor
		this.loadOfficeFloor(false, new OfficeFloorMaker() {
			@Override
			public void make(OfficeFloorMakerContext context) {
				context.getContext().getResource("missing");
			}
		});
	}

	/**
	 * Ensure able to obtain a resource.
	 */
	public void testGetResource() throws Exception {

		final String location = "LOCATION";
		final InputStream resource = new ByteArrayInputStream(new byte[0]);

		// Record obtaining the resource
		this.record_initiateOfficeFloorBuilder();
		this.recordReturn(this.resourceSource, this.resourceSource.sourceResource(location), resource);

		// Obtain the configuration item
		this.loadOfficeFloor(true, new OfficeFloorMaker() {
			@Override
			public void make(OfficeFloorMakerContext context) {
				assertEquals("Incorrect configuation item", resource, context.getContext().getResource(location));
			}
		});
	}

	/**
	 * Ensure issue if missing {@link ConfigurationItem}.
	 */
	public void testMissingConfigurationItem() {

		// Record missing configuration item
		this.recordReturn(this.resourceSource, this.resourceSource.sourceResource("missing"), null);
		this.issues.recordIssue(OfficeFloorNode.OFFICE_FLOOR_NAME, OfficeFloorNodeImpl.class,
				"Can not obtain ConfigurationItem at location 'missing'");

		// Attempt to load office floor
		this.loadOfficeFloor(false, new OfficeFloorMaker() {
			@Override
			public void make(OfficeFloorMakerContext context) {
				context.getContext().getConfigurationItem("missing", null);
			}
		});
	}

	/**
	 * Ensure issue if missing {@link Property} for {@link ConfigurationItem}.
	 */
	public void testMissingPropertyForConfigurationItem() {

		// Record missing resource
		this.recordReturn(this.resourceSource, this.resourceSource.sourceResource("configuration"),
				new ByteArrayInputStream("${missing}".getBytes()));
		this.issues.recordIssue(OfficeFloorNode.OFFICE_FLOOR_NAME, OfficeFloorNodeImpl.class,
				"Can not obtain ConfigurationItem at location 'configuration' as missing property 'missing'");

		// Attempt to load office floor
		this.loadOfficeFloor(false, new OfficeFloorMaker() {
			@Override
			public void make(OfficeFloorMakerContext context) {
				context.getContext().getConfigurationItem("configuration", null);
			}
		});
	}

	/**
	 * Ensure able to obtain a {@link ConfigurationItem}.
	 */
	public void testGetConfigurationItem() throws Exception {

		final String location = "LOCATION";
		final InputStream resource = new ByteArrayInputStream("content".getBytes());

		// Record obtaining the configuration item
		this.record_initiateOfficeFloorBuilder();
		this.recordReturn(this.resourceSource, this.resourceSource.sourceResource(location), resource);

		// Obtain the configuration item
		this.loadOfficeFloor(true, new OfficeFloorMaker() {
			@Override
			public void make(OfficeFloorMakerContext context) {
				Reader configuration = context.getContext().getConfigurationItem(location, null).getReader();
				assertContents(new StringReader("content"), configuration);
			}
		});
	}

	/**
	 * Ensure able to tag replace {@link ConfigurationItem}.
	 */
	public void testTagReplaceConfigurationItem() throws Exception {

		final String location = "LOCATION";
		final InputStream resource = new ByteArrayInputStream("${tag}".getBytes());

		// Record obtaining the configuration item
		this.record_initiateOfficeFloorBuilder();
		this.recordReturn(this.resourceSource, this.resourceSource.sourceResource(location), resource);

		// Obtain the configuration item
		this.loadOfficeFloor(true, new OfficeFloorMaker() {
			@Override
			public void make(OfficeFloorMakerContext context) {
				Reader configuration = context.getContext().getConfigurationItem(location, null).getReader();
				assertContents(new StringReader("replace"), configuration);
			}
		}, "tag", "replace");
	}

	/**
	 * Ensure handle {@link ConfigurationItem} failure.
	 */
	public void testConfigurationItemFailure() throws Exception {

		final String location = "LOCATION";
		final IOException failure = new IOException("TEST");
		final InputStream resource = new InputStream() {
			@Override
			public int read() throws IOException {
				throw failure;
			}
		};

		// Record obtaining the configuration item
		this.recordReturn(this.resourceSource, this.resourceSource.sourceResource(location), resource);
		this.issues.recordIssue(OfficeFloorNode.OFFICE_FLOOR_NAME, OfficeFloorNodeImpl.class,
				"Failed to obtain ConfigurationItem at location 'LOCATION': TEST", failure);

		// Obtain the configuration item
		this.loadOfficeFloor(false, new OfficeFloorMaker() {
			@Override
			public void make(OfficeFloorMakerContext context) throws IOException {
				context.getContext().getConfigurationItem(location, null);
				fail("Should not be successful");
			}
		});
	}

	/**
	 * Ensure able to get the {@link ClassLoader}.
	 */
	public void testGetClassLoader() {

		// Record
		this.record_initiateOfficeFloorBuilder();

		// Attempt to load office floor
		this.loadOfficeFloor(true, new OfficeFloorMaker() {
			@Override
			public void make(OfficeFloorMakerContext context) {
				assertEquals("Incorrect class loader", LoadRequiredPropertiesTest.class.getClassLoader(),
						context.getContext().getClassLoader());
			}
		});
	}

	/**
	 * Ensure issue if fails to source the {@link RequiredProperties}.
	 */
	public void testFailSourceOfficeFloor() {

		final NullPointerException failure = new NullPointerException("Fail source office floor");

		// Record failure to source the office floor
		this.issues
				.recordIssue(OfficeFloorNode.OFFICE_FLOOR_NAME, OfficeFloorNodeImpl.class,
						"Failed to source OfficeFloor from OfficeFloorSource (source="
								+ MakerOfficeFloorSource.class.getName() + ", location=" + OFFICE_FLOOR_LOCATION + ")",
						failure);

		// Attempt to load office floor
		this.loadOfficeFloor(false, new OfficeFloorMaker() {
			@Override
			public void make(OfficeFloorMakerContext context) {
				throw failure;
			}
		});
	}

	/**
	 * Ensure can obtain the {@link ManagedObjectType}.
	 */
	public void testLoadManagedObjectType() {

		// Record
		this.record_initiateOfficeFloorBuilder();
		this.issues.recordCaptureIssues(false); // for managed object type

		// Test
		this.loadOfficeFloor(true, new OfficeFloorMaker() {
			@Override
			public void make(OfficeFloorMakerContext context) {
				OfficeFloorSourceContext ofsContext = context.getContext();

				// Load the managed object type
				PropertyList properties = ofsContext.createPropertyList();
				properties.addProperty(ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME)
						.setValue(MockLoadManagedObject.class.getName());
				ManagedObjectType<?> managedObjectType = ofsContext.loadManagedObjectType("MOS",
						ClassManagedObjectSource.class.getName(), properties);

				// Ensure correct managed object type
				MockLoadManagedObject.assertManagedObjectType(managedObjectType);
			}
		});
	}

	/**
	 * Ensure issue if fails to load the {@link ManagedObjectType}.
	 */
	public void testFailLoadingManagedObjectType() {

		// Ensure issue in not loading managed object type
		CompilerIssue[] issues = this.issues.recordCaptureIssues(true);
		this.issues.recordIssue("MOS", ManagedObjectSourceNodeImpl.class, "Must specify property 'class.name'");
		this.issues.recordIssue(OfficeFloorNode.OFFICE_FLOOR_NAME, OfficeFloorNodeImpl.class,
				"Failure loading ManagedObjectType from source " + ClassManagedObjectSource.class.getName(), issues);

		// Fail to load the managed object type
		this.loadOfficeFloor(false, new OfficeFloorMaker() {
			@Override
			public void make(OfficeFloorMakerContext context) {
				OfficeFloorSourceContext ofsContext = context.getContext();

				// Do not specify class causing failure to load type
				PropertyList properties = ofsContext.createPropertyList();
				ofsContext.loadManagedObjectType("MOS", ClassManagedObjectSource.class.getName(), properties);

				// Should not reach this point
				fail("Should not successfully load managed object type");
			}
		});
	}

	/**
	 * Ensure can obtain the {@link ManagedObjectType} by an
	 * {@link ManagedObjectSource} instance.
	 */
	public void testLoadManagedObjectTypeByInstance() {

		// Record
		this.record_initiateOfficeFloorBuilder();
		this.issues.recordCaptureIssues(false); // for managed object type

		// Test
		this.loadOfficeFloor(true, new OfficeFloorMaker() {
			@Override
			public void make(OfficeFloorMakerContext context) {
				OfficeFloorSourceContext ofsContext = context.getContext();

				// Load the managed object type
				PropertyList properties = ofsContext.createPropertyList();
				properties.addProperty(ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME)
						.setValue(MockLoadManagedObject.class.getName());
				ManagedObjectType<?> managedObjectType = ofsContext.loadManagedObjectType("MOS",
						new ClassManagedObjectSource(), properties);

				// Ensure correct managed object type
				MockLoadManagedObject.assertManagedObjectType(managedObjectType);
			}
		});
	}

	/**
	 * Ensure issue if fails to load the {@link ManagedObjectType} by
	 * {@link ManagedObjectSource} instance.
	 */
	public void testFailLoadingManagedObjectTypeByInstance() {

		// Ensure issue in not loading managed object type
		CompilerIssue[] issues = this.issues.recordCaptureIssues(true);
		this.issues.recordIssue("MOS", ManagedObjectSourceNodeImpl.class, "Must specify property 'class.name'");
		this.issues.recordIssue(OfficeFloorNode.OFFICE_FLOOR_NAME, OfficeFloorNodeImpl.class,
				"Failure loading ManagedObjectType from source " + ClassManagedObjectSource.class.getName(), issues);

		// Fail to load the managed object type
		this.loadOfficeFloor(false, new OfficeFloorMaker() {
			@Override
			public void make(OfficeFloorMakerContext context) {
				OfficeFloorSourceContext ofsContext = context.getContext();

				// Do not specify class causing failure to load type
				PropertyList properties = ofsContext.createPropertyList();
				ofsContext.loadManagedObjectType("MOS", new ClassManagedObjectSource(), properties);

				// Should not reach this point
				fail("Should not successfully load managed object type");
			}
		});
	}

	/**
	 * Ensure can obtain the {@link InitialSupplierType}.
	 */
	public void testLoadSupplierType() {

		// Record
		this.record_initiateOfficeFloorBuilder();

		// Load supplier type
		this.issues.recordCaptureIssues(false);

		// Test
		this.loadOfficeFloor(true, new OfficeFloorMaker() {
			@Override
			public void make(OfficeFloorMakerContext context) {
				OfficeFloorSourceContext ofsContext = context.getContext();

				// Load the supplier type
				PropertyList properties = ofsContext.createPropertyList();
				properties.addProperty(MockLoadSupplierSource.PROPERTY_TEST)
						.setValue(MockLoadSupplierSource.PROPERTY_TEST);
				InitialSupplierType supplierType = ofsContext.loadSupplierType("SUPPLIER",
						MockLoadSupplierSource.class.getName(), properties);

				// Ensure correct supplier type
				MockLoadSupplierSource.assertSupplierType(supplierType);
			}
		});
	}

	/**
	 * Ensure issue if fails to load the {@link InitialSupplierType}.
	 */
	public void testFailLoadingSupplierType() {

		// Ensure issue in not loading supplier type
		CompilerIssue[] issues = this.issues.recordCaptureIssues(true);
		this.issues.recordIssue("SUPPLIER", SupplierNodeImpl.class,
				"Missing property 'TEST' for SupplierSource " + MockLoadSupplierSource.class.getName());
		this.issues.recordIssue(OfficeFloorNode.OFFICE_FLOOR_NAME, OfficeFloorNodeImpl.class,
				"Failure loading InitialSupplierType from source " + MockLoadSupplierSource.class.getName(), issues);

		// Fail to load the supplier type
		this.loadOfficeFloor(false, new OfficeFloorMaker() {
			@Override
			public void make(OfficeFloorMakerContext context) {
				OfficeFloorSourceContext ofsContext = context.getContext();

				// Do not specify property causing failure to load type
				PropertyList properties = ofsContext.createPropertyList();
				ofsContext.loadSupplierType("SUPPLIER", MockLoadSupplierSource.class.getName(), properties);

				// Should not reach this point
				fail("Should not successfully load supplier type");
			}
		});
	}

	/**
	 * Ensure can obtain the {@link OfficeType}.
	 */
	public void testLoadOfficeType() {

		// Record
		this.record_initiateOfficeFloorBuilder();
		this.issues.recordCaptureIssues(false); // for office type
		this.issues.recordCaptureIssues(false); // for section type
		this.issues.recordCaptureIssues(false);

		// Test
		this.loadOfficeFloor(true, new OfficeFloorMaker() {
			@Override
			public void make(OfficeFloorMakerContext context) {
				OfficeFloorSourceContext ofsContext = context.getContext();

				// Load the office type
				PropertyList properties = ofsContext.createPropertyList();
				properties.addProperty(MockLoadOfficeSource.PROPERTY_REQUIRED).setValue("provided");
				OfficeType officeType = ofsContext.loadOfficeType("OFFICE", MockLoadOfficeSource.class.getName(),
						"mock", properties);

				// Ensure correct office type
				MockLoadOfficeSource.assertOfficeType(officeType);
			}
		});
	}

	/**
	 * Ensure issue if fails to load the {@link OfficeType}.
	 */
	public void testFailLoadingOfficeType() {

		// Ensure issue in not loading office type
		final String OFFICE_NAME = "OFFICE";
		CompilerIssue[] issues = this.issues.recordCaptureIssues(true);
		this.issues.recordIssue(OFFICE_NAME, OfficeNodeImpl.class, "Must specify property 'required.property'");
		this.issues.recordIssue(OfficeFloorNode.OFFICE_FLOOR_NAME, OfficeFloorNodeImpl.class,
				"Failure loading OfficeType from source " + MockLoadOfficeSource.class.getName(), issues);

		// Fail to load the office type
		this.loadOfficeFloor(false, new OfficeFloorMaker() {
			@Override
			public void make(OfficeFloorMakerContext context) {
				OfficeFloorSourceContext ofsContext = context.getContext();

				// Do not specify class causing failure to load type
				PropertyList properties = ofsContext.createPropertyList();
				ofsContext.loadOfficeType(OFFICE_NAME, MockLoadOfficeSource.class.getName(), "mock", properties);

				// Should not reach this point
				fail("Should not successfully load office type");
			}
		});
	}

	/**
	 * Ensure can obtain the {@link OfficeType} by {@link OfficeSource} instance.
	 */
	public void testLoadOfficeTypeByInstance() {

		// Record
		this.record_initiateOfficeFloorBuilder();
		this.issues.recordCaptureIssues(false); // for office type
		this.issues.recordCaptureIssues(false); // for section type
		this.issues.recordCaptureIssues(false);

		// Test
		this.loadOfficeFloor(true, new OfficeFloorMaker() {
			@Override
			public void make(OfficeFloorMakerContext context) {
				OfficeFloorSourceContext ofsContext = context.getContext();

				// Load the office type
				PropertyList properties = ofsContext.createPropertyList();
				properties.addProperty(MockLoadOfficeSource.PROPERTY_REQUIRED).setValue("provided");
				OfficeType officeType = ofsContext.loadOfficeType("OFFICE", new MockLoadOfficeSource(), "mock",
						properties);

				// Ensure correct office type
				MockLoadOfficeSource.assertOfficeType(officeType);
			}
		});
	}

	/**
	 * Ensure issue if fails to load the {@link OfficeType} by {@link OfficeSource}
	 * instance.
	 */
	public void testFailLoadingOfficeTypeByInstance() {

		// Ensure issue in not loading office type
		final String OFFICE_NAME = "OFFICE";
		CompilerIssue[] issues = this.issues.recordCaptureIssues(true);
		this.issues.recordIssue(OFFICE_NAME, OfficeNodeImpl.class, "Must specify property 'required.property'");
		this.issues.recordIssue(OfficeFloorNode.OFFICE_FLOOR_NAME, OfficeFloorNodeImpl.class,
				"Failure loading OfficeType from source " + MockLoadOfficeSource.class.getName(), issues);

		// Fail to load the office type
		this.loadOfficeFloor(false, new OfficeFloorMaker() {
			@Override
			public void make(OfficeFloorMakerContext context) {
				OfficeFloorSourceContext ofsContext = context.getContext();

				// Do not specify class causing failure to load type
				PropertyList properties = ofsContext.createPropertyList();
				ofsContext.loadOfficeType(OFFICE_NAME, new MockLoadOfficeSource(), "mock", properties);

				// Should not reach this point
				fail("Should not successfully load office type");
			}
		});
	}

}
