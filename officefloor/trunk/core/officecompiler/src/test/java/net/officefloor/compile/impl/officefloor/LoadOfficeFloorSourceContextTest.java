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
package net.officefloor.compile.impl.officefloor;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Properties;

import net.officefloor.autowire.impl.supplier.MockLoadSupplierSource;
import net.officefloor.autowire.supplier.SupplierType;
import net.officefloor.compile.impl.managedobject.MockLoadManagedObject;
import net.officefloor.compile.impl.office.MockLoadOfficeSource;
import net.officefloor.compile.impl.structure.ManagedObjectSourceNodeImpl;
import net.officefloor.compile.impl.structure.OfficeFloorNodeImpl;
import net.officefloor.compile.impl.structure.OfficeNodeImpl;
import net.officefloor.compile.impl.structure.SupplierNodeImpl;
import net.officefloor.compile.internal.structure.Node;
import net.officefloor.compile.internal.structure.OfficeFloorNode;
import net.officefloor.compile.issues.CompilerIssue;
import net.officefloor.compile.managedobject.ManagedObjectFlowType;
import net.officefloor.compile.managedobject.ManagedObjectType;
import net.officefloor.compile.office.OfficeType;
import net.officefloor.compile.properties.Property;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.office.source.OfficeSource;
import net.officefloor.compile.spi.officefloor.OfficeFloorInputManagedObject;
import net.officefloor.compile.spi.officefloor.source.OfficeFloorSource;
import net.officefloor.compile.spi.officefloor.source.OfficeFloorSourceContext;
import net.officefloor.compile.spi.officefloor.source.RequiredProperties;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.plugin.managedobject.clazz.ClassManagedObjectSource;

/**
 * Tests the {@link OfficeFloorSourceContext} when loading the
 * {@link OfficeFloor}.
 * 
 * @author Daniel Sagenschneider
 */
public class LoadOfficeFloorSourceContextTest extends
		AbstractOfficeFloorTestCase {

	/**
	 * Ensure issue if fail to instantiate the {@link OfficeFloorSource}.
	 */
	public void testFailInstantiate() {

		final RuntimeException failure = new RuntimeException(
				"instantiate failure");

		// Record failure to instantiate
		this.issues.recordIssue(OfficeFloorNode.OFFICE_FLOOR_NAME,
				OfficeFloorNodeImpl.class, "Failed to instantiate "
						+ MakerOfficeFloorSource.class.getName()
						+ " by default constructor", failure);

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
				assertEquals("Incorrect office location",
						OFFICE_FLOOR_LOCATION, context.getContext()
								.getOfficeFloorLocation());
			}
		});
	}

	/**
	 * Ensure issue if missing {@link Property}.
	 */
	public void testMissingProperty() {

		// Record missing property
		this.issues.recordIssue(OfficeFloorNode.OFFICE_FLOOR_NAME,
				OfficeFloorNodeImpl.class,
				"Missing property 'missing' for OfficeFloorSource "
						+ MakerOfficeFloorSource.class.getName());

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
				assertEquals("Ensure get defaulted property", "DEFAULT",
						context.getProperty("missing", "DEFAULT"));
				assertEquals("Ensure get property ONE", "1",
						context.getProperty("ONE"));
				assertEquals("Ensure get property TWO", "2",
						context.getProperty("TWO"));
				String[] names = context.getPropertyNames();
				assertEquals("Incorrect number of property names", 3,
						names.length);
				assertEquals("Incorrect property name 0", "ONE", names[0]);
				assertEquals("Incorrect property name 1", "TWO", names[1]);
				assertEquals("Incorrect identifier",
						MakerOfficeFloorSource.MAKER_IDENTIFIER_PROPERTY_NAME,
						names[2]);
				Properties properties = context.getProperties();
				assertEquals("Incorrect number of properties", 3,
						properties.size());
				assertEquals("Incorrect property ONE", "1",
						properties.get("ONE"));
				assertEquals("Incorrect property TWO", "2",
						properties.get("TWO"));
				assertNotNull(
						"Incorrect identifier",
						properties
								.get(MakerOfficeFloorSource.MAKER_IDENTIFIER_PROPERTY_NAME));
			}
		}, "ONE", "1", "TWO", "2");
	}

	/**
	 * Ensure issue if missing {@link Class}.
	 */
	public void testMissingClass() {

		// Record missing class
		this.issues.recordIssue(OfficeFloorNode.OFFICE_FLOOR_NAME,
				OfficeFloorNodeImpl.class,
				"Can not load class 'missing' for OfficeFloorSource "
						+ MakerOfficeFloorSource.class.getName());

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
		this.recordReturn(this.resourceSource,
				this.resourceSource.sourceResource("missing"), null);
		this.issues.recordIssue(OfficeFloorNode.OFFICE_FLOOR_NAME,
				OfficeFloorNodeImpl.class,
				"Can not obtain resource at location 'missing' for OfficeFloorSource "
						+ MakerOfficeFloorSource.class.getName());

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
		this.recordReturn(this.resourceSource,
				this.resourceSource.sourceResource(location), resource);

		// Obtain the configuration item
		this.loadOfficeFloor(true, new OfficeFloorMaker() {
			@Override
			public void make(OfficeFloorMakerContext context) {
				assertEquals("Incorrect configuation item", resource, context
						.getContext().getResource(location));
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
				assertEquals("Incorrect class loader",
						LoadRequiredPropertiesTest.class.getClassLoader(),
						context.getContext().getClassLoader());
			}
		});
	}

	/**
	 * Ensure issue if fails to source the {@link RequiredProperties}.
	 */
	public void testFailSourceOfficeFloor() {

		final NullPointerException failure = new NullPointerException(
				"Fail source office floor");

		// Record failure to source the office floor
		this.issues.recordIssue(OfficeFloorNode.OFFICE_FLOOR_NAME,
				OfficeFloorNodeImpl.class,
				"Failed to source OfficeFloor from OfficeFloorSource (source="
						+ MakerOfficeFloorSource.class.getName()
						+ ", location=" + OFFICE_FLOOR_LOCATION + ")", failure);

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

		// Test
		this.loadOfficeFloor(true, new OfficeFloorMaker() {
			@Override
			public void make(OfficeFloorMakerContext context) {
				OfficeFloorSourceContext ofsContext = context.getContext();

				// Load the managed object type
				PropertyList properties = ofsContext.createPropertyList();
				properties.addProperty(
						ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME)
						.setValue(MockLoadManagedObject.class.getName());
				ManagedObjectType<?> managedObjectType = ofsContext
						.loadManagedObjectType(
								ClassManagedObjectSource.class.getName(),
								properties);

				// Ensure correct managed object type
				MockLoadManagedObject
						.assertManagedObjectType(managedObjectType);
			}
		});
	}

	/**
	 * Ensure issue if fails to load the {@link ManagedObjectType}.
	 */
	public void testFailLoadingManagedObjectType() {

		// Ensure issue in not loading managed object type
		CompilerIssue[] issues = this.issues.recordCaptureIssues(true);
		this.issues.recordIssue(Node.TYPE_NAME,
				ManagedObjectSourceNodeImpl.class,
				"Missing property 'class.name'");
		this.issues.recordIssue(OfficeFloorNode.OFFICE_FLOOR_NAME,
				OfficeFloorNodeImpl.class,
				"Failure loading ManagedObjectType from source "
						+ ClassManagedObjectSource.class.getName(), issues);

		// Fail to load the managed object type
		this.loadOfficeFloor(false, new OfficeFloorMaker() {
			@Override
			public void make(OfficeFloorMakerContext context) {
				OfficeFloorSourceContext ofsContext = context.getContext();

				// Do not specify class causing failure to load type
				PropertyList properties = ofsContext.createPropertyList();
				ofsContext.loadManagedObjectType(
						ClassManagedObjectSource.class.getName(), properties);

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

		// Test
		this.loadOfficeFloor(true, new OfficeFloorMaker() {
			@Override
			public void make(OfficeFloorMakerContext context) {
				OfficeFloorSourceContext ofsContext = context.getContext();

				// Load the managed object type
				PropertyList properties = ofsContext.createPropertyList();
				properties.addProperty(
						ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME)
						.setValue(MockLoadManagedObject.class.getName());
				ManagedObjectType<?> managedObjectType = ofsContext
						.loadManagedObjectType(new ClassManagedObjectSource(),
								properties);

				// Ensure correct managed object type
				MockLoadManagedObject
						.assertManagedObjectType(managedObjectType);
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
		this.issues.recordIssue(Node.TYPE_NAME,
				ManagedObjectSourceNodeImpl.class,
				"Missing property 'class.name'");
		this.issues.recordIssue(OfficeFloorNode.OFFICE_FLOOR_NAME,
				OfficeFloorNodeImpl.class,
				"Failure loading ManagedObjectType from source "
						+ ClassManagedObjectSource.class.getName(), issues);

		// Fail to load the managed object type
		this.loadOfficeFloor(false, new OfficeFloorMaker() {
			@Override
			public void make(OfficeFloorMakerContext context) {
				OfficeFloorSourceContext ofsContext = context.getContext();

				// Do not specify class causing failure to load type
				PropertyList properties = ofsContext.createPropertyList();
				ofsContext.loadManagedObjectType(
						new ClassManagedObjectSource(), properties);

				// Should not reach this point
				fail("Should not successfully load managed object type");
			}
		});
	}

	/**
	 * Ensure able to determine if {@link ManagedObjectType} should be
	 * configured as an {@link OfficeFloorInputManagedObject}.
	 */
	public void testCheckIfManagedObjectTypeIsInput() {

		final ManagedObjectType<?> moType = this
				.createMock(ManagedObjectType.class);
		final ManagedObjectFlowType<?> flowType = this
				.createMock(ManagedObjectFlowType.class);

		// Record as input managed object
		this.record_initiateOfficeFloorBuilder();
		this.recordReturn(moType, moType.getFlowTypes(),
				new ManagedObjectFlowType<?>[] { flowType });

		// Test
		this.loadOfficeFloor(true, new OfficeFloorMaker() {
			@Override
			public void make(OfficeFloorMakerContext context) {
				OfficeFloorSourceContext ofsContext = context.getContext();

				// Determine if input managed object
				boolean isInput = ofsContext.isInputManagedObject(moType);
				assertTrue("Should be input managed object", isInput);
			}
		});
	}

	/**
	 * Ensure can obtain the {@link SupplierType}.
	 */
	public void testLoadSupplierType() {

		// Record
		this.record_initiateOfficeFloorBuilder();

		// Test
		this.loadOfficeFloor(true, new OfficeFloorMaker() {
			@Override
			public void make(OfficeFloorMakerContext context) {
				OfficeFloorSourceContext ofsContext = context.getContext();

				// Load the supplier type
				PropertyList properties = ofsContext.createPropertyList();
				properties.addProperty(MockLoadSupplierSource.PROPERTY_TEST)
						.setValue(MockLoadSupplierSource.PROPERTY_TEST);
				SupplierType supplierType = ofsContext.loadSupplierType(
						MockLoadSupplierSource.class.getName(), properties);

				// Ensure correct supplier type
				MockLoadSupplierSource.assertSupplierType(supplierType);
			}
		});
	}

	/**
	 * Ensure issue if fails to load the {@link SupplierType}.
	 */
	public void testFailLoadingSupplierType() {

		// Ensure issue in not loading supplier type
		CompilerIssue[] issues = this.issues.recordCaptureIssues(true);
		this.issues.recordIssue(Node.TYPE_NAME, SupplierNodeImpl.class,
				"Missing property 'TEST' for SupplierSource "
						+ MockLoadSupplierSource.class.getName());
		this.issues.recordIssue(OfficeFloorNode.OFFICE_FLOOR_NAME,
				OfficeFloorNodeImpl.class,
				"Failure loading SupplierType from source "
						+ MockLoadSupplierSource.class.getName(), issues);

		// Fail to load the supplier type
		this.loadOfficeFloor(false, new OfficeFloorMaker() {
			@Override
			public void make(OfficeFloorMakerContext context) {
				OfficeFloorSourceContext ofsContext = context.getContext();

				// Do not specify property causing failure to load type
				PropertyList properties = ofsContext.createPropertyList();
				ofsContext.loadSupplierType(
						MockLoadSupplierSource.class.getName(), properties);

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

		// Test
		this.loadOfficeFloor(true, new OfficeFloorMaker() {
			@Override
			public void make(OfficeFloorMakerContext context) {
				OfficeFloorSourceContext ofsContext = context.getContext();

				// Load the office type
				PropertyList properties = ofsContext.createPropertyList();
				properties.addProperty(MockLoadOfficeSource.PROPERTY_REQUIRED)
						.setValue("provided");
				OfficeType officeType = ofsContext.loadOfficeType(
						MockLoadOfficeSource.class.getName(), "mock",
						properties);

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
		CompilerIssue[] issues = this.issues.recordCaptureIssues(true);
		this.issues.recordIssue(Node.TYPE_NAME, OfficeNodeImpl.class,
				"Missing property 'required.property' for OfficeSource "
						+ MockLoadOfficeSource.class.getName());
		this.issues.recordIssue(OfficeFloorNode.OFFICE_FLOOR_NAME,
				OfficeFloorNodeImpl.class,
				"Failure loading OfficeType from source "
						+ MockLoadOfficeSource.class.getName(), issues);

		// Fail to load the office type
		this.loadOfficeFloor(false, new OfficeFloorMaker() {
			@Override
			public void make(OfficeFloorMakerContext context) {
				OfficeFloorSourceContext ofsContext = context.getContext();

				// Do not specify class causing failure to load type
				PropertyList properties = ofsContext.createPropertyList();
				ofsContext.loadOfficeType(MockLoadOfficeSource.class.getName(),
						"mock", properties);

				// Should not reach this point
				fail("Should not successfully load office type");
			}
		});
	}

	/**
	 * Ensure can obtain the {@link OfficeType} by {@link OfficeSource}
	 * instance.
	 */
	public void testLoadOfficeTypeByInstance() {

		// Record
		this.record_initiateOfficeFloorBuilder();

		// Test
		this.loadOfficeFloor(true, new OfficeFloorMaker() {
			@Override
			public void make(OfficeFloorMakerContext context) {
				OfficeFloorSourceContext ofsContext = context.getContext();

				// Load the office type
				PropertyList properties = ofsContext.createPropertyList();
				properties.addProperty(MockLoadOfficeSource.PROPERTY_REQUIRED)
						.setValue("provided");
				OfficeType officeType = ofsContext.loadOfficeType(
						new MockLoadOfficeSource(), "mock", properties);

				// Ensure correct office type
				MockLoadOfficeSource.assertOfficeType(officeType);
			}
		});
	}

	/**
	 * Ensure issue if fails to load the {@link OfficeType} by
	 * {@link OfficeSource} instance.
	 */
	public void testFailLoadingOfficeTypeByInstance() {

		// Ensure issue in not loading office type
		CompilerIssue[] issues = this.issues.recordCaptureIssues(true);
		this.issues.recordIssue(Node.TYPE_NAME, OfficeNodeImpl.class,
				"Missing property 'required.property' for OfficeSource "
						+ MockLoadOfficeSource.class.getName());
		this.issues.recordIssue(OfficeFloorNode.OFFICE_FLOOR_NAME,
				OfficeFloorNodeImpl.class,
				"Failure loading OfficeType from source "
						+ MockLoadOfficeSource.class.getName(), issues);

		// Fail to load the office type
		this.loadOfficeFloor(false, new OfficeFloorMaker() {
			@Override
			public void make(OfficeFloorMakerContext context) {
				OfficeFloorSourceContext ofsContext = context.getContext();

				// Do not specify class causing failure to load type
				PropertyList properties = ofsContext.createPropertyList();
				ofsContext.loadOfficeType(new MockLoadOfficeSource(), "mock",
						properties);

				// Should not reach this point
				fail("Should not successfully load office type");
			}
		});
	}

}