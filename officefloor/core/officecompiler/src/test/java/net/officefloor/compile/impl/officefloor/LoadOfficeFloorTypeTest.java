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

import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.impl.properties.PropertyListImpl;
import net.officefloor.compile.impl.structure.AbstractStructureTestCase;
import net.officefloor.compile.impl.structure.ManagedObjectSourceNodeImpl;
import net.officefloor.compile.impl.structure.OfficeFloorNodeImpl;
import net.officefloor.compile.impl.structure.TeamNodeImpl;
import net.officefloor.compile.internal.structure.OfficeFloorNode;
import net.officefloor.compile.office.OfficeType;
import net.officefloor.compile.officefloor.OfficeFloorLoader;
import net.officefloor.compile.officefloor.OfficeFloorManagedObjectSourcePropertyType;
import net.officefloor.compile.officefloor.OfficeFloorManagedObjectSourceType;
import net.officefloor.compile.officefloor.OfficeFloorPropertyType;
import net.officefloor.compile.officefloor.OfficeFloorTeamSourcePropertyType;
import net.officefloor.compile.officefloor.OfficeFloorTeamSourceType;
import net.officefloor.compile.officefloor.OfficeFloorType;
import net.officefloor.compile.properties.Property;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.officefloor.OfficeFloorDeployer;
import net.officefloor.compile.spi.officefloor.source.OfficeFloorSource;
import net.officefloor.compile.spi.officefloor.source.OfficeFloorSourceContext;
import net.officefloor.compile.spi.officefloor.source.RequiredProperties;
import net.officefloor.compile.spi.officefloor.source.impl.AbstractOfficeFloorSource;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.managedobject.source.impl.AbstractManagedObjectSource;
import net.officefloor.frame.api.source.TestSource;
import net.officefloor.frame.api.team.Team;
import net.officefloor.frame.api.team.source.TeamSource;
import net.officefloor.frame.api.team.source.TeamSourceContext;
import net.officefloor.frame.api.team.source.impl.AbstractTeamSource;

/**
 * Tests loading the {@link OfficeFloorType}.
 *
 * @author Daniel Sagenschneider
 */
public class LoadOfficeFloorTypeTest extends AbstractStructureTestCase {

	/**
	 * Location of the {@link OfficeFloor}.
	 */
	private final String OFFICE_FLOOR_LOCATION = "OFFICE_FLOOR_LOCATION";

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		// Reset the mock OfficeFloor source
		MockOfficeFloorSource.reset();
	}

	/**
	 * Ensure issue if fail to instantiate the {@link OfficeFloorSource}.
	 */
	public void testFailInstantiate() {

		// Record failure to instantiate
		final RuntimeException failure = new RuntimeException(
				"instantiate failure");
		this.issues.recordIssue("Failed to instantiate "
				+ MockOfficeFloorSource.class.getName()
				+ " by default constructor", failure);

		// Load the office floor type
		MockOfficeFloorSource.instantiateFailure = failure;
		this.loadType(null, null);
	}

	/**
	 * Ensure obtain the correct {@link OfficeFloor} location.
	 */
	public void testOfficeFloorLocation() {
		this.loadType(new Loader() {
			@Override
			public void sourceOffice(OfficeFloorDeployer officeFloor,
					OfficeFloorSourceContext context) throws Exception {
				assertEquals("Incorrect office floor location",
						OFFICE_FLOOR_LOCATION, context.getOfficeFloorLocation());
			}
		}, new LoadedValidator());
	}

	/**
	 * Ensure issue if missing {@link Property}.
	 */
	public void testMissingProperty() {

		// Record missing property
		this.issues.recordIssue(OfficeFloorNode.OFFICE_FLOOR_NAME,
				OfficeFloorNodeImpl.class,
				"Missing property 'missing' for OfficeFloorSource "
						+ MockOfficeFloorSource.class.getName());

		// Attempt to load office floor type
		this.loadType(new Loader() {
			@Override
			public void sourceOffice(OfficeFloorDeployer officeFloor,
					OfficeFloorSourceContext context) throws Exception {
				context.getProperty("missing");
			}
		}, null);
	}

	/**
	 * Ensure includes required properties.
	 */
	public void testSpecificationAndRequiredProperties() {
		this.loadType(new Loader() {
			@Override
			public void sourceOffice(OfficeFloorDeployer officeFloor,
					OfficeFloorSourceContext context) throws Exception {
				// Do nothing as testing specification of properties
			}
		}, new Validator() {
			@Override
			void validate(OfficeFloorType type) {
				OfficeFloorPropertyType[] properties = type
						.getOfficeFloorPropertyTypes();
				assertEquals("Incorrect number of properties", 2,
						properties.length);
				OfficeFloorPropertyType propertyOne = properties[0];
				assertEquals("Incorrect property one name", "ONE",
						propertyOne.getName());
				assertEquals("Incorrect property one label", "A",
						propertyOne.getLabel());
				OfficeFloorPropertyType propertyTwo = properties[1];
				assertEquals("Incorrect property two name", "TWO",
						propertyTwo.getName());
				assertEquals("Incorrect property two label", "B",
						propertyTwo.getLabel());
			}
		});
	}

	/**
	 * Ensure able to get properties.
	 */
	public void testGetProperties() {
		this.loadType(new Loader() {
			@Override
			public void sourceOffice(OfficeFloorDeployer officeFloor,
					OfficeFloorSourceContext context) throws Exception {
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
				assertEquals("Incorrect proeprty name 1", "TWO", names[1]);
				Properties properties = context.getProperties();
				assertEquals("Incorrect number of properties", 2,
						properties.size());
				assertEquals("Incorrect property ONE", "1",
						properties.getProperty("ONE"));
				assertEquals("Incorrect property TWO", "2",
						properties.getProperty("TWO"));
			}
		}, new Validator() {
			@Override
			void validate(OfficeFloorType type) {
				OfficeFloorPropertyType[] properties = type
						.getOfficeFloorPropertyTypes();
				assertEquals("Incorrect number of properties", 2,
						properties.length);
				OfficeFloorPropertyType propertyOne = properties[0];
				assertEquals("Incorrect property one name", "ONE",
						propertyOne.getName());
				assertEquals("Incorrect property one label", "A",
						propertyOne.getLabel());
				assertEquals("Incorrect property one default value", "1",
						propertyOne.getDefaultValue());
				OfficeFloorPropertyType propertyTwo = properties[1];
				assertEquals("Incorrect property two name", "TWO",
						propertyTwo.getName());
				assertEquals("Incorrect property two label", "B",
						propertyTwo.getLabel());
				assertEquals("Incorrect property two default value", "2",
						propertyTwo.getDefaultValue());
			}
		}, "ONE", "1", "TWO", "2");
	}

	/**
	 * Ensure issue if missing {@link Class}.
	 */
	public void testMissingClass() {
		this.issues.recordIssue(OfficeFloorNode.OFFICE_FLOOR_NAME,
				OfficeFloorNodeImpl.class,
				"Can not load class 'missing' for OfficeFloorSource "
						+ MockOfficeFloorSource.class.getName());
		this.loadType(new Loader() {
			@Override
			public void sourceOffice(OfficeFloorDeployer officeFloor,
					OfficeFloorSourceContext context) throws Exception {
				context.loadClass("missing");
			}
		}, null);
	}

	/**
	 * Ensure issue if missing resource.
	 */
	public void testMissingResource() {
		this.recordReturn(this.resourceSource,
				this.resourceSource.sourceResource("missing"), null);
		this.issues.recordIssue(OfficeFloorNode.OFFICE_FLOOR_NAME,
				OfficeFloorNodeImpl.class,
				"Can not obtain resource at location 'missing' for OfficeFloorSource "
						+ MockOfficeFloorSource.class.getName());
		this.loadType(new Loader() {
			@Override
			public void sourceOffice(OfficeFloorDeployer officeFloor,
					OfficeFloorSourceContext context) throws Exception {
				context.getResource("missing");
			}
		}, null);
	}

	/**
	 * Ensure able to obtain a resource.
	 */
	public void testGetResource() {
		final String location = "LOCATION";
		final InputStream resource = new ByteArrayInputStream(new byte[0]);
		this.recordReturn(this.resourceSource,
				this.resourceSource.sourceResource(location), resource);
		this.loadType(new Loader() {
			@Override
			public void sourceOffice(OfficeFloorDeployer officeFloor,
					OfficeFloorSourceContext context) throws Exception {
				assertSame("Incorrect resource", resource,
						context.getResource(location));
			}
		}, new LoadedValidator());
	}

	/**
	 * Ensure able to get the {@link ClassLoader}.
	 */
	public void testGetClassLoader() {
		this.loadType(new Loader() {
			@Override
			public void sourceOffice(OfficeFloorDeployer officeFloor,
					OfficeFloorSourceContext context) throws Exception {
				assertEquals("Incorrect class loader",
						LoadOfficeFloorTypeTest.class.getClassLoader(),
						context.getClassLoader());
			}
		}, new LoadedValidator());
	}

	/**
	 * Ensure issue if fails to source the {@link OfficeFloorType}.
	 */
	public void testFailSourceOfficeFloorType() {
		final NullPointerException failure = new NullPointerException(
				"Fail to source office floor type");
		this.issues.recordIssue(OfficeFloorNode.OFFICE_FLOOR_NAME,
				OfficeFloorNodeImpl.class,
				"Failed to source OfficeFloor from OfficeFloorSource (source="
						+ MockOfficeFloorSource.class.getName() + ", location="
						+ OFFICE_FLOOR_LOCATION + ")", failure);
		this.loadType(new Loader() {
			@Override
			public void sourceOffice(OfficeFloorDeployer officeFloor,
					OfficeFloorSourceContext context) throws Exception {
				throw failure;
			}
		}, null);
	}

	/**
	 * Ensure issue if <code>null</code>
	 * {@link OfficeFloorManagedObjectSourceType} name.
	 */
	public void testNullManagedObjectSourceName() {
		this.issues.recordIssue(null, ManagedObjectSourceNodeImpl.class,
				"Null name for Managed Object Source");
		this.loadType(new Loader() {
			@Override
			public void sourceOffice(OfficeFloorDeployer officeFloor,
					OfficeFloorSourceContext context) throws Exception {
				officeFloor.addManagedObjectSource(null,
						TestManagedObjectSource.class.getName());
			}
		}, null);
	}

	/**
	 * Ensure issue if <code>null</code> {@link ManagedObjectSource} type.
	 */
	public void testNullManagedObjectSourceType() {
		this.issues.recordIssue("MO", ManagedObjectSourceNodeImpl.class,
				"Null source for Managed Object Source MO");
		this.loadType(new Loader() {
			@Override
			public void sourceOffice(OfficeFloorDeployer officeFloor,
					OfficeFloorSourceContext context) throws Exception {
				officeFloor.addManagedObjectSource("MO", (String) null);
			}
		}, null);
	}

	/**
	 * Ensure provide properties of {@link ManagedObjectSource} type.
	 */
	public void testManagedObjectSourceType() {
		this.loadType(new Loader() {
			@Override
			public void sourceOffice(OfficeFloorDeployer officeFloor,
					OfficeFloorSourceContext context) throws Exception {
				officeFloor.addManagedObjectSource("MO",
						TestManagedObjectSource.class.getName()).addProperty(
						"example.test", "DEFAULT_VALUE");
			}
		}, new Validator() {
			@Override
			void validate(OfficeFloorType type) {
				OfficeFloorManagedObjectSourceType[] mos = type
						.getOfficeFloorManagedObjectSourceTypes();
				assertNotNull("Should have types", mos);
				assertEquals("Incorrect number of managed object sources", 1,
						mos.length);
				OfficeFloorManagedObjectSourceType mosType = mos[0];
				assertEquals("Incorrect name", "MO",
						mosType.getOfficeFloorManagedObjectSourceName());
				OfficeFloorManagedObjectSourcePropertyType[] properties = mosType
						.getOfficeFloorManagedObjectSourcePropertyTypes();
				assertNotNull("Should have properties", properties);
				assertEquals("Incorrect number of properties", 1,
						properties.length);
				OfficeFloorManagedObjectSourcePropertyType property = properties[0];
				assertEquals("Incorrect property name", "example.test",
						property.getName());
				assertEquals("Incorrect property label", "Test Property",
						property.getLabel());
				assertEquals("Incorrect default value", "DEFAULT_VALUE",
						property.getDefaultValue());
			}
		});
	}

	/**
	 * Test {@link ManagedObjectSource}.
	 */
	@TestSource
	public static class TestManagedObjectSource extends
			AbstractManagedObjectSource<None, None> {

		@Override
		protected void loadSpecification(SpecificationContext context) {
			context.addProperty("example.test", "Test Property");
		}

		@Override
		protected void loadMetaData(MetaDataContext<None, None> context)
				throws Exception {
			context.setObjectClass(Object.class);
		}

		@Override
		protected ManagedObject getManagedObject() throws Throwable {
			fail("Should not require to create the managed object");
			return null;
		}
	}

	/**
	 * Ensure issue if <code>null</code> {@link OfficeFloorTeamSourceType} name.
	 */
	public void testNullTeamSourceName() {
		this.issues.recordIssue(null, TeamNodeImpl.class, "Null name for Team");
		this.loadType(new Loader() {
			@Override
			public void sourceOffice(OfficeFloorDeployer officeFloor,
					OfficeFloorSourceContext context) throws Exception {
				officeFloor.addTeam(null, TestTeamSource.class.getName());
			}
		}, null);
	}

	/**
	 * Ensure issue if <code>null</code> {@link OfficeFloorTeamSourceType} type.
	 */
	public void testNullTeamSourceType() {
		this.issues.recordIssue("TEAM", TeamNodeImpl.class,
				"Null source for Team TEAM");
		this.loadType(new Loader() {
			@Override
			public void sourceOffice(OfficeFloorDeployer officeFloor,
					OfficeFloorSourceContext context) throws Exception {
				officeFloor.addTeam("TEAM", null);
			}
		}, null);
	}

	/**
	 * Ensure obtain the {@link OfficeFloorTeamSourceType}.
	 */
	public void testTeamSourceType() {
		this.loadType(new Loader() {
			@Override
			public void sourceOffice(OfficeFloorDeployer officeFloor,
					OfficeFloorSourceContext context) throws Exception {
				officeFloor.addTeam("TEAM", TestTeamSource.class.getName())
						.addProperty("example.test", "DEFAULT_VALUE");
			}
		}, new Validator() {
			@Override
			void validate(OfficeFloorType type) {
				OfficeFloorTeamSourceType[] teams = type
						.getOfficeFloorTeamSourceTypes();
				assertEquals("Incorrect number of teams", 1, teams.length);
				OfficeFloorTeamSourceType team = teams[0];
				assertEquals("Incorrect team source name", "TEAM",
						team.getOfficeFloorTeamSourceName());
				OfficeFloorTeamSourcePropertyType[] properties = team
						.getOfficeFloorTeamSourcePropertyTypes();
				assertNotNull("Ensure have properties", properties);
				assertEquals("Incorrect number of properties", 1,
						properties.length);
				OfficeFloorTeamSourcePropertyType property = properties[0];
				assertEquals("Incorrect property name", "example.test",
						property.getName());
				assertEquals("Incorrect property label", "Test Property",
						property.getLabel());
				assertEquals("Incorrect property default value",
						"DEFAULT_VALUE", property.getDefaultValue());
			}
		});
	}

	/**
	 * Test {@link TeamSource}.
	 */
	@TestSource
	public static class TestTeamSource extends AbstractTeamSource {

		@Override
		protected void loadSpecification(SpecificationContext context) {
			context.addProperty("example.test", "Test Property");
		}

		@Override
		public Team createTeam(TeamSourceContext context) throws Exception {
			return null; // Team is ignored
		}
	}

	/**
	 * Loads the {@link OfficeFloorType} within the input {@link Loader}.
	 * 
	 * @param loader
	 *            {@link Loader}.
	 * @param validator
	 *            {@link Validator} to validate the {@link OfficeFloorType}. May
	 *            be <code>null</code> to indicate the {@link OfficeFloorType}
	 *            should fail to be loaded.
	 * @param propertyNameValuePairs
	 *            {@link Property} name value pairs.
	 * @return Loaded {@link OfficeFloorType}.
	 */
	private OfficeFloorType loadType(Loader loader, Validator validator,
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

		// Create the office floor loader and load the office floor
		OfficeFloorCompiler compiler = OfficeFloorCompiler
				.newOfficeFloorCompiler(null);
		compiler.setCompilerIssues(this.issues);
		compiler.addResources(this.resourceSource);
		OfficeFloorLoader officeFloorLoader = compiler.getOfficeFloorLoader();
		MockOfficeFloorSource.loader = loader;
		OfficeFloorType officeFloorType = officeFloorLoader
				.loadOfficeFloorType(MockOfficeFloorSource.class,
						OFFICE_FLOOR_LOCATION, propertyList);

		// Verify the mock objects
		this.verifyMockObjects();

		// Ensure if should be loaded
		if (validator != null) {
			validator.validate(officeFloorType);
		} else {
			assertNull("Should not load the OfficeFloor type", officeFloorType);
		}

		// Return the office floor type
		return officeFloorType;
	}

	/**
	 * Implemented to load the {@link OfficeFloorType}.
	 */
	private interface Loader {

		/**
		 * Implemented to load the {@link OfficeFloorType}.
		 * 
		 * @param officeFloor
		 *            {@link OfficeFloorDeployer}.
		 * @param context
		 *            {@link OfficeFloorSourceContext}.
		 * @throws Exception
		 *             If fails to source {@link OfficeFloorType}.
		 */
		void sourceOffice(OfficeFloorDeployer officeFloor,
				OfficeFloorSourceContext context) throws Exception;
	}

	/**
	 * Implemented to validate the {@link OfficeFloorType}.
	 */
	private abstract class Validator {

		/**
		 * Implemented to validate the {@link OfficeFloorType}.
		 * 
		 * @param type
		 *            {@link OfficeFloorType} to validate.
		 */
		abstract void validate(OfficeFloorType type);
	}

	/**
	 * {@link Validator} to ensure type is loaded.
	 */
	private class LoadedValidator extends Validator {
		@Override
		void validate(OfficeFloorType type) {
			assertNotNull("Ensure type loaded", type);
		}
	}

	/**
	 * Mock {@link OfficeFloorSource} for testing.
	 */
	@TestSource
	public static class MockOfficeFloorSource extends AbstractOfficeFloorSource {

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
		public MockOfficeFloorSource() {
			if (instantiateFailure != null) {
				throw instantiateFailure;
			}
		}

		/*
		 * ================ OfficeFloorSource ================================
		 */

		@Override
		protected void loadSpecification(SpecificationContext context) {
			context.addProperty("ONE", "A");
		}

		@Override
		public void specifyConfigurationProperties(
				RequiredProperties requiredProperties,
				OfficeFloorSourceContext context) throws Exception {
			requiredProperties.addRequiredProperty("TWO", "B");
		}

		@Override
		public void sourceOfficeFloor(OfficeFloorDeployer officeFloorDeployer,
				OfficeFloorSourceContext context) throws Exception {
			loader.sourceOffice(officeFloorDeployer, context);
		}

	}

}