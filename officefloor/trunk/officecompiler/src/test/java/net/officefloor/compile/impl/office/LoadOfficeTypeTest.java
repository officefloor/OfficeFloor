/*
 * Office Floor, Application Server
 * Copyright (C) 2005-2009 Daniel Sagenschneider
 *
 * This program is free software; you can redistribute it and/or modify it under the terms
 * of the GNU General Public License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program;
 * if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 * MA 02111-1307 USA
 */
package net.officefloor.compile.impl.office;

import java.io.IOException;
import java.sql.Connection;
import java.util.Properties;

import javax.transaction.xa.XAResource;

import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.impl.properties.PropertyListImpl;
import net.officefloor.compile.impl.structure.AbstractStructureTestCase;
import net.officefloor.compile.issues.CompilerIssues.LocationType;
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
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.spi.TestSource;
import net.officefloor.model.repository.ConfigurationItem;

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

	/*
	 * (non-Javadoc)
	 * 
	 * @see junit.framework.TestCase#setUp()
	 */
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
		this.record_issue("Failed to instantiate "
				+ MockOfficeSource.class.getName() + " by default constructor",
				failure);

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
	 * Ensures issue if fails to obtain the {@link ConfigurationItem}.
	 */
	public void testFailGetConfigurationItem() throws Exception {

		final String location = "LOCATION";
		final IOException failure = new IOException(
				"Configuration Item failure");

		// Record failing to obtain the configuration item
		this.control(this.configurationContext).expectAndThrow(
				this.configurationContext.getConfigurationItem(location),
				failure);
		this
				.record_issue("Failure obtaining configuration 'LOCATION'",
						failure);

		// Attempt to obtain the configuration item
		this.loadOfficeType(false, new Loader() {
			@Override
			public void sourceOffice(OfficeArchitect office,
					OfficeSourceContext context) throws Exception {
				context.getConfiguration(location);
			}
		});
	}

	/**
	 * Ensure able to obtain a {@link ConfigurationItem}.
	 */
	public void testGetConfigurationItem() throws Exception {

		final String location = "LOCATION";
		final ConfigurationItem item = this.createMock(ConfigurationItem.class);

		// Record obtaining the configuration item
		this.recordReturn(this.configurationContext, this.configurationContext
				.getConfigurationItem(location), item);

		// Obtain the configuration item
		this.loadOfficeType(true, new Loader() {
			@Override
			public void sourceOffice(OfficeArchitect office,
					OfficeSourceContext context) throws Exception {
				assertEquals("Incorrect configuation item", item, context
						.getConfiguration(location));
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
				assertEquals("Ensure get property ONE", "1", context
						.getProperty("ONE"));
				assertEquals("Ensure get property TWO", "2", context
						.getProperty("TWO"));
				String[] names = context.getPropertyNames();
				assertEquals("Incorrect number of property names", 2,
						names.length);
				assertEquals("Incorrect property name 0", "ONE", names[0]);
				assertEquals("Incorrect property name 1", "TWO", names[1]);
				Properties properties = context.getProperties();
				assertEquals("Incorrect number of properties", 2, properties
						.size());
				assertEquals("Incorrect property ONE", "1", properties
						.get("ONE"));
				assertEquals("Incorrect property TWO", "2", properties
						.get("TWO"));
			}
		}, "ONE", "1", "TWO", "2");
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
				assertEquals("Incorrect class loader", LoadOfficeTypeTest.class
						.getClassLoader(), context.getClassLoader());
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
				office.addOfficeObject("MO", Connection.class.getName());
			}
		});

		// Validate type
		assertEquals("Incorrect number of managed object types", 1, officeType
				.getOfficeManagedObjectTypes().length);
		OfficeManagedObjectType moType = officeType
				.getOfficeManagedObjectTypes()[0];
		assertEquals("Incorrect name", "MO", moType
				.getOfficeManagedObjectName());
		assertEquals("Incorrect type", Connection.class.getName(), moType
				.getObjectType());
		assertEquals("Should be no required extension interfaces", 0, moType
				.getExtensionInterfaces().length);
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
				OfficeObject mo = office.addOfficeObject("MO", Connection.class
						.getName());
				OfficeAdministrator admin = LoadOfficeTypeTest.this
						.addAdministrator(office, "ADMIN", XAResource.class,
								SimpleDutyKey.DUTY);
				admin.administerManagedObject(mo);
			}
		});

		// Validate type
		assertEquals("Incorrect number of managed object types", 1, officeType
				.getOfficeManagedObjectTypes().length);
		OfficeManagedObjectType moType = officeType
				.getOfficeManagedObjectTypes()[0];
		assertEquals("Incorrect name", "MO", moType
				.getOfficeManagedObjectName());
		assertEquals("Incorrect type", Connection.class.getName(), moType
				.getObjectType());
		assertEquals("Incorrect number of extension interfaces", 1, moType
				.getExtensionInterfaces().length);
		String extensionInterface = moType.getExtensionInterfaces()[0];
		assertEquals("Incorrect extension interface", XAResource.class
				.getName(), extensionInterface);
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
		assertEquals("Incorrect number of teams", 1, officeType
				.getOfficeTeamTypes().length);
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
		assertEquals("Incorrect number of inputs", 1, officeType
				.getOfficeInputTypes().length);
		OfficeInputType input = officeType.getOfficeInputTypes()[0];
		assertEquals("Incorrect section name", "SECTION", input
				.getOfficeSectionName());
		assertEquals("Incorrect input name", "INPUT", input
				.getOfficeSectionInputName());
		assertEquals("Incorrect parameter type", String.class.getName(), input
				.getParameterType());
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
				.newOfficeFloorCompiler();
		compiler.setCompilerIssues(this.issues);
		compiler.setConfigurationContext(this.configurationContext);
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