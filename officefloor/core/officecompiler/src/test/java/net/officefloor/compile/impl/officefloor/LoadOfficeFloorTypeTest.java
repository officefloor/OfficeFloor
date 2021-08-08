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
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Logger;

import net.officefloor.compile.AvailableServiceFactory;
import net.officefloor.compile.FailServiceFactory;
import net.officefloor.compile.MissingServiceFactory;
import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.impl.properties.PropertyListImpl;
import net.officefloor.compile.impl.structure.AbstractStructureTestCase;
import net.officefloor.compile.impl.structure.ManagedObjectSourceNodeImpl;
import net.officefloor.compile.impl.structure.OfficeFloorNodeImpl;
import net.officefloor.compile.impl.structure.TeamNodeImpl;
import net.officefloor.compile.internal.structure.OfficeFloorNode;
import net.officefloor.compile.issues.CompileError;
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
import net.officefloor.compile.test.officefloor.CompileOfficeFloor;
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
import net.officefloor.frame.test.Closure;

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
		final RuntimeException failure = new RuntimeException("instantiate failure");
		this.issues.recordIssue(
				"Failed to instantiate " + MockOfficeFloorSource.class.getName() + " by default constructor", failure);

		// Load the office floor type
		MockOfficeFloorSource.instantiateFailure = failure;
		this.loadType((Loader) null, null);
	}

	/**
	 * Ensure obtain the correct {@link OfficeFloor} location.
	 */
	public void testOfficeFloorLocation() {
		this.loadType((officeFloor, context) -> {
			assertEquals("Incorrect office floor location", OFFICE_FLOOR_LOCATION, context.getOfficeFloorLocation());
		}, new LoadedValidator());
	}

	/**
	 * Ensure issue if missing {@link Property}.
	 */
	public void testMissingProperty() {

		// Record missing property
		this.issues.recordIssue(OfficeFloorNode.OFFICE_FLOOR_NAME, OfficeFloorNodeImpl.class,
				"Must specify property 'missing'");

		// Attempt to load office floor type
		this.loadType((officeFloor, context) -> {
			context.getProperty("missing");
		}, null);
	}

	/**
	 * Ensure includes required properties.
	 */
	public void testSpecificationAndRequiredProperties() {
		this.loadType((officeFloor, context) -> {
			// Do nothing as testing specification of properties
		}, (type) -> {
			OfficeFloorPropertyType[] properties = type.getOfficeFloorPropertyTypes();
			assertEquals("Incorrect number of properties", 2, properties.length);
			OfficeFloorPropertyType propertyOne = properties[0];
			assertEquals("Incorrect property one name", "ONE", propertyOne.getName());
			assertEquals("Incorrect property one label", "A", propertyOne.getLabel());
			OfficeFloorPropertyType propertyTwo = properties[1];
			assertEquals("Incorrect property two name", "TWO", propertyTwo.getName());
			assertEquals("Incorrect property two label", "B", propertyTwo.getLabel());
		});
	}

	/**
	 * Ensure able to get properties.
	 */
	public void testGetProperties() {
		this.loadType((officeFloor, context) -> {
			assertEquals("Ensure get defaulted property", "DEFAULT", context.getProperty("missing", "DEFAULT"));
			assertEquals("Ensure get property ONE", "1", context.getProperty("ONE"));
			assertEquals("Ensure get property TWO", "2", context.getProperty("TWO"));
			String[] names = context.getPropertyNames();
			assertEquals("Incorrect number of property names", 2, names.length);
			assertEquals("Incorrect property name 0", "ONE", names[0]);
			assertEquals("Incorrect proeprty name 1", "TWO", names[1]);
			Properties properties = context.getProperties();
			assertEquals("Incorrect number of properties", 2, properties.size());
			assertEquals("Incorrect property ONE", "1", properties.getProperty("ONE"));
			assertEquals("Incorrect property TWO", "2", properties.getProperty("TWO"));
		}, (type) -> {
			OfficeFloorPropertyType[] properties = type.getOfficeFloorPropertyTypes();
			assertEquals("Incorrect number of properties", 2, properties.length);
			OfficeFloorPropertyType propertyOne = properties[0];
			assertEquals("Incorrect property one name", "ONE", propertyOne.getName());
			assertEquals("Incorrect property one label", "A", propertyOne.getLabel());
			assertEquals("Incorrect property one default value", "1", propertyOne.getDefaultValue());
			OfficeFloorPropertyType propertyTwo = properties[1];
			assertEquals("Incorrect property two name", "TWO", propertyTwo.getName());
			assertEquals("Incorrect property two label", "B", propertyTwo.getLabel());
			assertEquals("Incorrect property two default value", "2", propertyTwo.getDefaultValue());
		}, "ONE", "1", "TWO", "2");
	}

	/**
	 * Ensure issue if missing {@link Class}.
	 */
	public void testMissingClass() {
		this.issues.recordIssue(OfficeFloorNode.OFFICE_FLOOR_NAME, OfficeFloorNodeImpl.class,
				"Can not load class 'missing'");
		this.loadType((officeFloor, context) -> {
			context.loadClass("missing");
		}, null);
	}

	/**
	 * Ensure issue if missing resource.
	 */
	public void testMissingResource() {
		this.recordReturn(this.resourceSource, this.resourceSource.sourceResource("missing"), null);
		this.issues.recordIssue(OfficeFloorNode.OFFICE_FLOOR_NAME, OfficeFloorNodeImpl.class,
				"Can not obtain resource at location 'missing'");
		this.loadType((officeFloor, context) -> {
			context.getResource("missing");
		}, null);
	}

	/**
	 * Ensure able to obtain a resource.
	 */
	public void testGetResource() {
		final String location = "LOCATION";
		final InputStream resource = new ByteArrayInputStream(new byte[0]);
		this.recordReturn(this.resourceSource, this.resourceSource.sourceResource(location), resource);
		this.loadType((officeFloor, context) -> {
			assertSame("Incorrect resource", resource, context.getResource(location));
		}, new LoadedValidator());
	}

	/**
	 * Ensure issue if missing service.
	 */
	public void testMissingService() {

		// Record missing service
		this.issues.recordIssue(OfficeFloorNode.OFFICE_FLOOR_NAME, OfficeFloorNodeImpl.class,
				MissingServiceFactory.getIssueDescription());

		// Attempt to load
		this.loadType((officeFloor, context) -> context.loadService(MissingServiceFactory.class, null), null);
	}

	/**
	 * Ensure issue if fail to load service.
	 */
	public void testFailLoadService() {

		// Record load issue for service
		this.issues.recordIssue(OfficeFloorNode.OFFICE_FLOOR_NAME, OfficeFloorNodeImpl.class,
				FailServiceFactory.getIssueDescription(), FailServiceFactory.getCause());

		// Attempt to load
		this.loadType((officeFloor, context) -> context.loadService(FailServiceFactory.class, null), null);
	}

	/**
	 * Ensure can load service.
	 */
	public void testLoadService() {
		Closure<Object> service = new Closure<>();
		this.loadType((section, context) -> {
			service.value = context.loadService(AvailableServiceFactory.class, null);
		}, new LoadedValidator());
		assertSame("Incorrect service", AvailableServiceFactory.getService(), service.value);
	}

	/**
	 * Ensure correctly name {@link Logger}.
	 */
	public void testLogger() {
		Closure<String> loggerName = new Closure<>();
		this.loadType((officeFloor, context) -> {
			loggerName.value = context.getLogger().getName();
		}, new LoadedValidator());
		assertEquals("Incorrect logger name", OfficeFloorNode.OFFICE_FLOOR_NAME, loggerName.value);
	}

	/**
	 * Ensure able to get the {@link ClassLoader}.
	 */
	public void testGetClassLoader() {
		Closure<ClassLoader> classLoader = new Closure<>();
		this.loadType((officeFloor, context) -> {
			classLoader.value = context.getClassLoader();
		}, new LoadedValidator());
		assertEquals("Incorrect class loader", LoadOfficeFloorTypeTest.class.getClassLoader(), classLoader.value);
	}

	/**
	 * Ensure issue if fails to source the {@link OfficeFloorType}.
	 */
	public void testFailSourceOfficeFloorType() {
		final NullPointerException failure = new NullPointerException("Fail to source office floor type");
		this.issues
				.recordIssue(OfficeFloorNode.OFFICE_FLOOR_NAME, OfficeFloorNodeImpl.class,
						"Failed to source OfficeFloor from OfficeFloorSource (source="
								+ MockOfficeFloorSource.class.getName() + ", location=" + OFFICE_FLOOR_LOCATION + ")",
						failure);
		this.loadType((officeFloor, context) -> {
			throw failure;
		}, null);
	}

	/**
	 * Ensure issue if <code>null</code> {@link OfficeFloorManagedObjectSourceType}
	 * name.
	 */
	public void testNullManagedObjectSourceName() {
		this.issues.recordIssue(null, ManagedObjectSourceNodeImpl.class, "Null name for Managed Object Source");
		this.loadType((officeFloor, context) -> {
			officeFloor.addManagedObjectSource(null, TestManagedObjectSource.class.getName());
		}, null);
	}

	/**
	 * Ensure issue if <code>null</code> {@link ManagedObjectSource} type.
	 */
	public void testNullManagedObjectSourceType() {
		this.issues.recordIssue("MO", ManagedObjectSourceNodeImpl.class, "Null source for Managed Object Source MO");
		this.loadType((officeFloor, context) -> {
			officeFloor.addManagedObjectSource("MO", (String) null);
		}, null);
	}

	/**
	 * Ensure provide properties of {@link ManagedObjectSource} type.
	 */
	public void testOfficeFloorManagedObjectSourceType() {
		this.loadType((officeFloor, context) -> {
			officeFloor.addManagedObjectSource("MO", TestManagedObjectSource.class.getName())
					.addProperty("example.test", "DEFAULT_VALUE");
		}, (type) -> {
			OfficeFloorManagedObjectSourceType[] mos = type.getOfficeFloorManagedObjectSourceTypes();
			assertNotNull("Should have types", mos);
			assertEquals("Incorrect number of managed object sources", 1, mos.length);
			OfficeFloorManagedObjectSourceType mosType = mos[0];
			assertEquals("Incorrect name", "MO", mosType.getOfficeFloorManagedObjectSourceName());
			OfficeFloorManagedObjectSourcePropertyType[] properties = mosType
					.getOfficeFloorManagedObjectSourcePropertyTypes();
			assertNotNull("Should have properties", properties);
			assertEquals("Incorrect number of properties", 1, properties.length);
			OfficeFloorManagedObjectSourcePropertyType property = properties[0];
			assertEquals("Incorrect property name", "example.test", property.getName());
			assertEquals("Incorrect property label", "Test Property", property.getLabel());
			assertEquals("Incorrect default value", "DEFAULT_VALUE", property.getDefaultValue());
		});
	}

	/**
	 * Ensure provide properties of {@link ManagedObjectSource} type.
	 */
	public void testOfficeManagedObjectSourceType() {
		CompileOfficeFloor compile = new CompileOfficeFloor();
		compile.office((context) -> {
			context.getOfficeArchitect().addOfficeManagedObjectSource("MO", TestManagedObjectSource.class.getName())
					.addProperty("example.test", "DEFAULT_VALUE");
		});
		this.loadType(compile, (type) -> {
			assertEquals("Should only include OfficeFloor managed object sources", 0,
					type.getOfficeFloorManagedObjectSourceTypes().length);
		});
	}

	/**
	 * Test {@link ManagedObjectSource}.
	 */
	@TestSource
	public static class TestManagedObjectSource extends AbstractManagedObjectSource<None, None> {

		@Override
		protected void loadSpecification(SpecificationContext context) {
			context.addProperty("example.test", "Test Property");
		}

		@Override
		protected void loadMetaData(MetaDataContext<None, None> context) throws Exception {
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
		this.loadType((officeFloor, context) -> {
			officeFloor.addTeam(null, TestTeamSource.class.getName());
		}, null);
	}

	/**
	 * Ensure issue if <code>null</code> {@link OfficeFloorTeamSourceType} type.
	 */
	public void testNullTeamSourceType() {
		this.issues.recordIssue("TEAM", TeamNodeImpl.class, "Null source for Team TEAM");
		this.loadType((officeFloor, context) -> {
			officeFloor.addTeam("TEAM", (String) null);
		}, null);
	}

	/**
	 * Ensure obtain the {@link OfficeFloorTeamSourceType}.
	 */
	public void testTeamSourceType() {
		this.loadType((officeFloor, context) -> {
			officeFloor.addTeam("TEAM", TestTeamSource.class.getName()).addProperty("example.test", "DEFAULT_VALUE");
		}, (type) -> {
			OfficeFloorTeamSourceType[] teams = type.getOfficeFloorTeamSourceTypes();
			assertEquals("Incorrect number of teams", 1, teams.length);
			OfficeFloorTeamSourceType team = teams[0];
			assertEquals("Incorrect team source name", "TEAM", team.getOfficeFloorTeamSourceName());
			OfficeFloorTeamSourcePropertyType[] properties = team.getOfficeFloorTeamSourcePropertyTypes();
			assertNotNull("Ensure have properties", properties);
			assertEquals("Incorrect number of properties", 1, properties.length);
			OfficeFloorTeamSourcePropertyType property = properties[0];
			assertEquals("Incorrect property name", "example.test", property.getName());
			assertEquals("Incorrect property label", "Test Property", property.getLabel());
			assertEquals("Incorrect property default value", "DEFAULT_VALUE", property.getDefaultValue());
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
	 * Ensure can handle {@link CompileError}.
	 */
	public void testHandleCompileError() {

		// Record issue
		this.issues.recordIssue("OfficeFloor", OfficeFloorNodeImpl.class, "test");

		// Ensure handle compile error
		this.loadType((officeFloor, context) -> {
			throw officeFloor.addIssue("test");
		}, null);
	}

	/**
	 * Loads the {@link OfficeFloorType} within the input {@link Loader}.
	 * 
	 * @param loader                 {@link Loader}.
	 * @param validator              {@link Validator} to validate the
	 *                               {@link OfficeFloorType}. May be
	 *                               <code>null</code> to indicate the
	 *                               {@link OfficeFloorType} should fail to be
	 *                               loaded.
	 * @param propertyNameValuePairs {@link Property} name value pairs.
	 * @return Loaded {@link OfficeFloorType}.
	 */
	private OfficeFloorType loadType(Loader loader, Validator validator, String... propertyNameValuePairs) {

		// Replay mock objects
		this.replayMockObjects();

		// Create the property list
		PropertyList propertyList = new PropertyListImpl();
		for (int i = 0; i < propertyNameValuePairs.length; i += 2) {
			String name = propertyNameValuePairs[i];
			String value = propertyNameValuePairs[i + 1];
			propertyList.addProperty(name).setValue(value);
		}

		// Load the OfficeFloor type
		OfficeFloorCompiler compiler = OfficeFloorCompiler.newOfficeFloorCompiler(null);
		compiler.setCompilerIssues(this.issues);
		compiler.addResources(this.resourceSource);
		OfficeFloorLoader officeFloorLoader = compiler.getOfficeFloorLoader();
		MockOfficeFloorSource.loader = loader;
		OfficeFloorType officeFloorType = officeFloorLoader.loadOfficeFloorType(MockOfficeFloorSource.class,
				OFFICE_FLOOR_LOCATION, propertyList);

		// Verify the mock objects
		this.verifyMockObjects();

		// Ensure if should be loaded
		if (validator != null) {
			validator.validate(officeFloorType);
		} else {
			assertNull("Should not load the OfficeFloor type", officeFloorType);
		}

		// Return the OfficeFloor type
		return officeFloorType;
	}

	/**
	 * Loads the {@link OfficeFloorType} from the {@link CompileOfficeFloor}.
	 * 
	 * @param compile   {@link CompileOfficeFloor}.
	 * @param validator {@link Validator} to validate the {@link OfficeFloorType}.
	 *                  May be <code>null</code> to indicate the
	 *                  {@link OfficeFloorType} should fail to be loaded.
	 * @return {@link OfficeFloorType}.
	 */
	private OfficeFloorType loadType(CompileOfficeFloor compile, Validator validator) {

		// Replay mock objects
		this.replayMockObjects();

		// Load the OfficeFloor type
		compile.getOfficeFloorCompiler().setCompilerIssues(this.issues);
		OfficeFloorType officeFloorType = compile.loadOfficeFloorType();

		// Verify the mock objects
		this.verifyMockObjects();

		// Ensure if should be loaded
		if (validator != null) {
			validator.validate(officeFloorType);
		} else {
			assertNull("Should not load the OfficeFloor type", officeFloorType);
		}

		// Return the OfficeFloor type
		return officeFloorType;
	}

	/**
	 * Implemented to load the {@link OfficeFloorType}.
	 */
	private interface Loader {

		/**
		 * Implemented to load the {@link OfficeFloorType}.
		 * 
		 * @param officeFloor {@link OfficeFloorDeployer}.
		 * @param context     {@link OfficeFloorSourceContext}.
		 * @throws Exception If fails to source {@link OfficeFloorType}.
		 */
		void sourceOffice(OfficeFloorDeployer officeFloor, OfficeFloorSourceContext context) throws Exception;
	}

	/**
	 * Implemented to validate the {@link OfficeFloorType}.
	 */
	private interface Validator {

		/**
		 * Implemented to validate the {@link OfficeFloorType}.
		 * 
		 * @param type {@link OfficeFloorType} to validate.
		 */
		void validate(OfficeFloorType type);
	}

	/**
	 * {@link Validator} to ensure type is loaded.
	 */
	private class LoadedValidator implements Validator {
		@Override
		public void validate(OfficeFloorType type) {
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
		public void specifyConfigurationProperties(RequiredProperties requiredProperties,
				OfficeFloorSourceContext context) throws Exception {
			requiredProperties.addRequiredProperty("TWO", "B");
		}

		@Override
		public void sourceOfficeFloor(OfficeFloorDeployer officeFloorDeployer, OfficeFloorSourceContext context)
				throws Exception {
			loader.sourceOffice(officeFloorDeployer, context);
		}
	}

}
