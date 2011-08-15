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

package net.officefloor.compile.impl.office;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.util.Properties;

import javax.transaction.xa.XAResource;

import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.administrator.AdministratorType;
import net.officefloor.compile.impl.administrator.MockLoadAdministrator;
import net.officefloor.compile.impl.managedobject.MockLoadManagedObject;
import net.officefloor.compile.impl.properties.PropertyListImpl;
import net.officefloor.compile.impl.structure.AbstractStructureTestCase;
import net.officefloor.compile.issues.CompilerIssues.LocationType;
import net.officefloor.compile.managedobject.ManagedObjectType;
import net.officefloor.compile.office.OfficeInputType;
import net.officefloor.compile.office.OfficeLoader;
import net.officefloor.compile.office.OfficeManagedObjectType;
import net.officefloor.compile.office.OfficeTeamType;
import net.officefloor.compile.office.OfficeType;
import net.officefloor.compile.properties.Property;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.office.OfficeAdministrator;
import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.OfficeObject;
import net.officefloor.compile.spi.office.source.OfficeSource;
import net.officefloor.compile.spi.office.source.OfficeSourceContext;
import net.officefloor.compile.spi.office.source.OfficeSourceSpecification;
import net.officefloor.frame.api.build.OfficeFloorIssues.AssetType;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.spi.TestSource;
import net.officefloor.plugin.administrator.clazz.ClassAdministratorSource;
import net.officefloor.plugin.managedobject.clazz.ClassManagedObjectSource;

/**
 * Tests loading the {@link OfficeType}.
 * 
 * @author Daniel Sagenschneider
 */
public class LoadOfficeTypeTest extends AbstractStructureTestCase {

	/**
	 * Location of the {@link Office}.
	 */
	private final String OFFICE_LOCATION = "OFFICE_LOCATION";

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		MockOfficeSource.reset();
	}

	/**
	 * Ensure issue if fail to instantiate the {@link OfficeSource}.
	 */
	public void testFailInstantiate() {

		final RuntimeException failure = new RuntimeException(
				"instantiate failure");

		// Record failure to instantiate
		this.record_issue(
				"Failed to instantiate " + MockOfficeSource.class.getName()
						+ " by default constructor", failure);

		// Attempt to obtain specification
		MockOfficeSource.instantiateFailure = failure;
		this.loadOfficeType(false, null);
	}

	/**
	 * Ensure obtain the correct {@link Office} location.
	 */
	public void testOfficeLocation() {
		this.loadOfficeType(true, new Loader() {
			@Override
			public void sourceOffice(OfficeArchitect office,
					OfficeSourceContext context) throws Exception {
				assertEquals("Incorrect office location", OFFICE_LOCATION,
						context.getOfficeLocation());
			}
		});
	}

	/**
	 * Ensure issue if missing {@link Property}.
	 */
	public void testMissingProperty() {

		// Record missing property
		this.record_issue("Missing property 'missing' for OfficeSource "
				+ MockOfficeSource.class.getName());

		// Attempt to load office type
		this.loadOfficeType(false, new Loader() {
			@Override
			public void sourceOffice(OfficeArchitect office,
					OfficeSourceContext context) throws Exception {
				context.getProperty("missing");
			}
		});
	}

	/**
	 * Ensure able to get properties.
	 */
	public void testGetProperties() {

		// Attempt to load office type
		this.loadOfficeType(true, new Loader() {
			@Override
			public void sourceOffice(OfficeArchitect office,
					OfficeSourceContext context) throws Exception {
				assertEquals("Ensure get defaulted property", "DEFAULT",
						context.getProperty("missing", "DEFAULT"));
				assertEquals("Ensure get property ONE", "1",
						context.getProperty("ONE"));
				assertEquals("Ensure get property TWO", "2",
						context.getProperty("TWO"));
				String[] names = context.getPropertyNames();
				assertEquals("Incorrect number of property names", 2,
						names.length);
				assertEquals("Incorrect property name 0", "ONE", names[0]);
				assertEquals("Incorrect property name 1", "TWO", names[1]);
				Properties properties = context.getProperties();
				assertEquals("Incorrect number of properties", 2,
						properties.size());
				assertEquals("Incorrect property ONE", "1",
						properties.get("ONE"));
				assertEquals("Incorrect property TWO", "2",
						properties.get("TWO"));
			}
		}, "ONE", "1", "TWO", "2");
	}

	/**
	 * Ensure issue if missing {@link Class}.
	 */
	public void testMissingClass() {

		// Record missing class
		this.record_issue("Can not load class 'missing' for OfficeSource "
				+ MockOfficeSource.class.getName());

		// Attempt to load office type
		this.loadOfficeType(false, new Loader() {
			@Override
			public void sourceOffice(OfficeArchitect office,
					OfficeSourceContext context) throws Exception {
				context.loadClass("missing");
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
		this.record_issue("Can not obtain resource at location 'missing' for OfficeSource "
				+ MockOfficeSource.class.getName());

		// Attempt to load office type
		this.loadOfficeType(false, new Loader() {
			@Override
			public void sourceOffice(OfficeArchitect office,
					OfficeSourceContext context) throws Exception {
				context.getResource("missing");
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
		this.recordReturn(this.resourceSource,
				this.resourceSource.sourceResource(location), resource);

		// Obtain the configuration item
		this.loadOfficeType(true, new Loader() {
			@Override
			public void sourceOffice(OfficeArchitect office,
					OfficeSourceContext context) throws Exception {
				assertSame("Incorrect resource", resource,
						context.getResource(location));
			}
		});
	}

	/**
	 * Ensure able to get the {@link ClassLoader}.
	 */
	public void testGetClassLoader() {

		// Attempt to load office type
		this.loadOfficeType(true, new Loader() {
			@Override
			public void sourceOffice(OfficeArchitect office,
					OfficeSourceContext context) throws Exception {
				assertEquals("Incorrect class loader",
						LoadOfficeTypeTest.class.getClassLoader(),
						context.getClassLoader());
			}
		});
	}

	/**
	 * Ensure issue if fails to source the {@link OfficeType}.
	 */
	public void testFailSourceOfficeType() {

		final NullPointerException failure = new NullPointerException(
				"Fail source office type");

		// Record failure to source the office type
		this.record_issue(
				"Failed to source OfficeType definition from OfficeSource "
						+ MockOfficeSource.class.getName(), failure);

		// Attempt to load office type
		this.loadOfficeType(false, new Loader() {
			@Override
			public void sourceOffice(OfficeArchitect office,
					OfficeSourceContext context) throws Exception {
				throw failure;
			}
		});
	}

	/**
	 * Ensure issue if <code>null</code> {@link OfficeManagedObjectType} name.
	 */
	public void testNullManagedObjectName() {

		// Record null managed object name
		this.record_issue("Null name for managed object 0");

		// Attempt to load office type
		this.loadOfficeType(false, new Loader() {
			@Override
			public void sourceOffice(OfficeArchitect office,
					OfficeSourceContext context) throws Exception {
				office.addOfficeObject(null, Connection.class.getName());
			}
		});
	}

	/**
	 * Ensure issue if <code>null</code> {@link OfficeManagedObjectType}
	 * required type.
	 */
	public void testNullManagedObjectType() {

		// Record null required type
		this.record_issue("Null type for managed object 0 (name=MO)");

		// Attempt to load office type
		this.loadOfficeType(false, new Loader() {
			@Override
			public void sourceOffice(OfficeArchitect office,
					OfficeSourceContext context) throws Exception {
				office.addOfficeObject("MO", null);
			}
		});
	}

	/**
	 * Ensure get {@link OfficeManagedObjectType} not being administered (no
	 * extension interfaces).
	 */
	public void testManagedObjectType() {
		// Load office type with office floor managed object
		OfficeType officeType = this.loadOfficeType(true, new Loader() {
			@Override
			public void sourceOffice(OfficeArchitect office,
					OfficeSourceContext context) throws Exception {

				final String MANAGED_OBJECT_NAME = "MO";

				// Add the office object
				OfficeObject officeObject = office.addOfficeObject(
						MANAGED_OBJECT_NAME, Connection.class.getName());

				// Ensure the office object is correct
				assertEquals("Incorrect office object name",
						MANAGED_OBJECT_NAME, officeObject.getOfficeObjectName());
				assertEquals("Incorrect dependent name", MANAGED_OBJECT_NAME,
						officeObject.getDependentManagedObjectName());
				assertEquals(
						"Always no dependencies for Office object in sourcing Office",
						0, officeObject.getObjectDependencies().length);
				assertEquals("Incorrect administerable name",
						MANAGED_OBJECT_NAME,
						officeObject.getAdministerableManagedObjectName());
			}
		});

		// Validate type
		assertEquals("Incorrect number of managed object types", 1,
				officeType.getOfficeManagedObjectTypes().length);
		OfficeManagedObjectType moType = officeType
				.getOfficeManagedObjectTypes()[0];
		assertEquals("Incorrect name", "MO",
				moType.getOfficeManagedObjectName());
		assertEquals("Incorrect type", Connection.class.getName(),
				moType.getObjectType());
		assertEquals("Should be no required extension interfaces", 0,
				moType.getExtensionInterfaces().length);
	}

	/**
	 * Ensure get {@link OfficeManagedObjectType} being administered (has
	 * extension interfaces).
	 */
	public void testAdministeredManagedObjectType() {
		// Load office type with administered office floor managed object
		OfficeType officeType = this.loadOfficeType(true, new Loader() {
			@Override
			public void sourceOffice(OfficeArchitect office,
					OfficeSourceContext context) throws Exception {
				OfficeObject mo = office.addOfficeObject("MO",
						Connection.class.getName());
				OfficeAdministrator admin = LoadOfficeTypeTest.this
						.addAdministrator(office, "ADMIN", XAResource.class,
								SimpleDutyKey.DUTY);
				admin.administerManagedObject(mo);
			}
		});

		// Validate type
		assertEquals("Incorrect number of managed object types", 1,
				officeType.getOfficeManagedObjectTypes().length);
		OfficeManagedObjectType moType = officeType
				.getOfficeManagedObjectTypes()[0];
		assertEquals("Incorrect name", "MO",
				moType.getOfficeManagedObjectName());
		assertEquals("Incorrect type", Connection.class.getName(),
				moType.getObjectType());
		assertEquals("Incorrect number of extension interfaces", 1,
				moType.getExtensionInterfaces().length);
		String extensionInterface = moType.getExtensionInterfaces()[0];
		assertEquals("Incorrect extension interface",
				XAResource.class.getName(), extensionInterface);
	}

	/**
	 * Ensure issue if <code>null</code> {@link OfficeTeamType} name.
	 */
	public void testNullTeamName() {

		// Record null required type
		this.record_issue("Null name for team 0");

		// Attempt to load office type
		this.loadOfficeType(false, new Loader() {
			@Override
			public void sourceOffice(OfficeArchitect office,
					OfficeSourceContext context) throws Exception {
				office.addOfficeTeam(null);
			}
		});
	}

	/**
	 * Ensure obtain the {@link OfficeTeamType}.
	 */
	public void testTeamType() {

		// Load office type
		OfficeType officeType = this.loadOfficeType(true, new Loader() {
			@Override
			public void sourceOffice(OfficeArchitect office,
					OfficeSourceContext context) throws Exception {
				office.addOfficeTeam("TEAM");
			}
		});

		// Validate type
		assertEquals("Incorrect number of teams", 1,
				officeType.getOfficeTeamTypes().length);
		OfficeTeamType team = officeType.getOfficeTeamTypes()[0];
		assertEquals("Incorrect team name", "TEAM", team.getOfficeTeamName());
	}

	/**
	 * Ensure obtain the {@link OfficeInputType}.
	 */
	public void testInputType() {

		// Load office type
		OfficeType officeType = this.loadOfficeType(true, new Loader() {
			@Override
			public void sourceOffice(OfficeArchitect office,
					OfficeSourceContext context) throws Exception {
				// Add section with an input
				LoadOfficeTypeTest.this.addSection(office, "SECTION",
						new SectionMaker() {
							@Override
							public void make(SectionMakerContext context) {
								context.getBuilder().addSectionInput("INPUT",
										String.class.getName());
							}
						});
			}
		});

		// Validate type
		assertEquals("Incorrect number of inputs", 1,
				officeType.getOfficeInputTypes().length);
		OfficeInputType input = officeType.getOfficeInputTypes()[0];
		assertEquals("Incorrect section name", "SECTION",
				input.getOfficeSectionName());
		assertEquals("Incorrect input name", "INPUT",
				input.getOfficeSectionInputName());
		assertEquals("Incorrect parameter type", String.class.getName(),
				input.getParameterType());
	}

	/**
	 * Ensure can obtain the {@link ManagedObjectType}.
	 */
	public void testLoadManagedObjectType() {
		this.loadOfficeType(true, new Loader() {
			@Override
			public void sourceOffice(OfficeArchitect architect,
					OfficeSourceContext context) throws Exception {

				// Load the managed object type
				PropertyList properties = context.createPropertyList();
				properties.addProperty(
						ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME)
						.setValue(MockLoadManagedObject.class.getName());
				ManagedObjectType<?> managedObjectType = context
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
		this.issues.addIssue(LocationType.OFFICE, OFFICE_LOCATION,
				AssetType.MANAGED_OBJECT, "loadManagedObjectType",
				"Missing property 'class.name'");
		this.record_issue("Failure loading ManagedObjectType from source "
				+ ClassManagedObjectSource.class.getName());

		// Fail to load the managed object type
		this.loadOfficeType(false, new Loader() {
			@Override
			public void sourceOffice(OfficeArchitect architect,
					OfficeSourceContext context) throws Exception {

				// Do not specify class causing failure to load type
				PropertyList properties = context.createPropertyList();
				context.loadManagedObjectType(
						ClassManagedObjectSource.class.getName(), properties);

				// Should not reach this point
				fail("Should not successfully load managed object type");
			}
		});
	}

	/**
	 * Ensure can obtain the {@link AdministratorType}.
	 */
	public void testLoadAdministratorType() {
		this.loadOfficeType(true, new Loader() {
			@Override
			public void sourceOffice(OfficeArchitect architect,
					OfficeSourceContext context) throws Exception {

				// Load the managed object type
				PropertyList properties = context.createPropertyList();
				properties.addProperty(
						ClassAdministratorSource.CLASS_NAME_PROPERTY_NAME)
						.setValue(MockLoadAdministrator.class.getName());
				AdministratorType<?, ?> administratorType = context
						.loadAdministratorType(
								ClassAdministratorSource.class.getName(),
								properties);

				// Ensure correct administrator type
				MockLoadAdministrator
						.assertAdministratorType(administratorType);
			}
		});
	}

	/**
	 * Ensure issue if fails to load the {@link AdministratorType}.
	 */
	public void testFailLoadingAdministratorType() {

		// Ensure issue in not loading managed object type
		this.issues.addIssue(LocationType.OFFICE, OFFICE_LOCATION,
				AssetType.ADMINISTRATOR, "loadAdministratorType",
				"Missing property 'class.name'");
		this.record_issue("Failure loading AdministratorType from source "
				+ ClassAdministratorSource.class.getName());

		// Fail to load the administrator type
		this.loadOfficeType(false, new Loader() {
			@Override
			public void sourceOffice(OfficeArchitect architect,
					OfficeSourceContext context) throws Exception {

				// Do not specify class causing failure to load type
				PropertyList properties = context.createPropertyList();
				context.loadAdministratorType(
						ClassAdministratorSource.class.getName(), properties);

				// Should not reach this point
				fail("Should not successfully load administrator type");
			}
		});
	}

	/**
	 * Records an issue.
	 * 
	 * @param issueDescription
	 *            Description of the issue.
	 */
	private void record_issue(String issueDescription) {
		this.issues.addIssue(LocationType.OFFICE, OFFICE_LOCATION, null, null,
				issueDescription);
	}

	/**
	 * Records an issue.
	 * 
	 * @param issueDescription
	 *            Description of the issue.
	 * @param cause
	 *            Cause of the issue.
	 */
	private void record_issue(String issueDescription, Throwable cause) {
		this.issues.addIssue(LocationType.OFFICE, OFFICE_LOCATION, null, null,
				issueDescription, cause);
	}

	/**
	 * Loads the {@link OfficeType} within the input {@link Loader}.
	 * 
	 * @param isExpectedToLoad
	 *            Flag indicating if expecting to load the {@link OfficeType}.
	 * @param loader
	 *            {@link Loader}.
	 * @param propertyNameValuePairs
	 *            {@link Property} name value pairs.
	 * @return Loaded {@link OfficeType}.
	 */
	private OfficeType loadOfficeType(boolean isExpectedToLoad, Loader loader,
			String... propertyNameValuePairs) {

		// Replay mock objects
		this.replayMockObjects();

		// Create the property list
		PropertyList propertyList = new PropertyListImpl();
		for (int i = 0; i < propertyNameValuePairs.length; i += 2) {
			String name = propertyNameValuePairs[i];
			String value = propertyNameValuePairs[i + 1];
			propertyList.addProperty(name).setValue(value);
		}

		// Create the office loader and load the office
		OfficeFloorCompiler compiler = OfficeFloorCompiler
				.newOfficeFloorCompiler(null);
		compiler.setCompilerIssues(this.issues);
		compiler.addResources(this.resourceSource);
		OfficeLoader officeLoader = compiler.getOfficeLoader();
		MockOfficeSource.loader = loader;
		OfficeType officeType = officeLoader.loadOfficeType(
				MockOfficeSource.class, OFFICE_LOCATION, propertyList);

		// Verify the mock objects
		this.verifyMockObjects();

		// Ensure if should be loaded
		if (isExpectedToLoad) {
			assertNotNull("Expected to load the office type", officeType);
		} else {
			assertNull("Should not load the office type", officeType);
		}

		// Return the office type
		return officeType;
	}

	/**
	 * Implemented to load the {@link OfficeType}.
	 */
	private interface Loader {

		/**
		 * Implemented to load the {@link OfficeType}.
		 * 
		 * @param office
		 *            {@link OfficeArchitect}.
		 * @param context
		 *            {@link OfficeSourceContext}.
		 * @throws Exception
		 *             If fails to source {@link OfficeType}.
		 */
		void sourceOffice(OfficeArchitect office, OfficeSourceContext context)
				throws Exception;
	}

	/**
	 * Mock {@link OfficeSource} for testing.
	 */
	@TestSource
	public static class MockOfficeSource implements OfficeSource {

		/**
		 * {@link Loader} to load the {@link OfficeType}.
		 */
		public static Loader loader;

		/**
		 * Failure in instantiating an instance.
		 */
		public static RuntimeException instantiateFailure;

		/**
		 * Resets the state for the next test.
		 */
		public static void reset() {
			loader = null;
			instantiateFailure = null;
		}

		/**
		 * Default constructor.
		 */
		public MockOfficeSource() {
			if (instantiateFailure != null) {
				throw instantiateFailure;
			}
		}

		/*
		 * ================ OfficeSource ======================================
		 */

		@Override
		public OfficeSourceSpecification getSpecification() {
			fail("Should not be invoked in obtaining office type");
			return null;
		}

		@Override
		public void sourceOffice(OfficeArchitect officeArchitect,
				OfficeSourceContext context) throws Exception {
			loader.sourceOffice(officeArchitect, context);
		}
	}

}