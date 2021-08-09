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

import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.impl.properties.PropertyListImpl;
import net.officefloor.compile.impl.structure.AbstractStructureTestCase;
import net.officefloor.compile.officefloor.OfficeFloorLoader;
import net.officefloor.compile.properties.Property;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.officefloor.OfficeFloorDeployer;
import net.officefloor.compile.spi.officefloor.source.OfficeFloorSource;
import net.officefloor.compile.spi.officefloor.source.OfficeFloorSourceContext;
import net.officefloor.compile.spi.officefloor.source.OfficeFloorSourceSpecification;
import net.officefloor.compile.spi.officefloor.source.RequiredProperties;
import net.officefloor.compile.test.properties.PropertyListUtil;
import net.officefloor.frame.api.manage.OfficeFloor;

/**
 * Tests loading the required {@link PropertyList}.
 * 
 * @author Daniel Sagenschneider
 */
public class LoadRequiredPropertiesTest extends AbstractStructureTestCase {

	/**
	 * Location of the {@link OfficeFloor}.
	 */
	private final String OFFICE_FLOOR_LOCATION = "OFFICE_FLOOR";

	@Override
	protected void setUp() throws Exception {
		MockOfficeFloorSource.reset();
	}

	/**
	 * Ensure issue if fail to instantiate the {@link OfficeFloorSource}.
	 */
	public void testFailInstantiate() {

		final RuntimeException failure = new RuntimeException(
				"instantiate failure");

		// Record failure to instantiate
		this.issues.recordIssue("Failed to instantiate "
				+ MockOfficeFloorSource.class.getName()
				+ " by default constructor", failure);

		// Attempt to obtain specification
		MockOfficeFloorSource.instantiateFailure = failure;
		this.loadRequiredProperties(false, null);
	}

	/**
	 * Ensure obtain the correct {@link OfficeFloor} location.
	 */
	public void testOfficeFloorLocation() {
		this.loadRequiredProperties(true, new Loader() {
			@Override
			public void loadRequiredProperties(RequiredProperties properties,
					OfficeFloorSourceContext context) throws Exception {
				assertEquals("Incorrect office location",
						OFFICE_FLOOR_LOCATION, context.getOfficeFloorLocation());
			}
		});
	}

	/**
	 * Ensure issue if missing {@link Property}.
	 */
	public void testMissingProperty() {

		// Record missing property
		this.issues
				.recordIssue("Missing property 'missing' for OfficeFloorSource "
						+ MockOfficeFloorSource.class.getName());

		// Attempt to load required properties
		this.loadRequiredProperties(false, new Loader() {
			@Override
			public void loadRequiredProperties(RequiredProperties properties,
					OfficeFloorSourceContext context) throws Exception {
				context.getProperty("missing");
			}
		});
	}

	/**
	 * Ensure able to get properties.
	 */
	public void testGetProperties() {

		// Attempt to load required properties
		this.loadRequiredProperties(true, new Loader() {
			@Override
			public void loadRequiredProperties(
					RequiredProperties requiredProperties,
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

		// Record missing property
		this.issues
				.recordIssue("Can not load class 'missing' for OfficeFloorSource "
						+ MockOfficeFloorSource.class.getName());

		// Attempt to load required properties
		this.loadRequiredProperties(false, new Loader() {
			@Override
			public void loadRequiredProperties(RequiredProperties properties,
					OfficeFloorSourceContext context) throws Exception {
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
		this.issues
				.recordIssue("Can not obtain resource at location 'missing' for OfficeFloorSource "
						+ MockOfficeFloorSource.class.getName());

		// Attempt to load required properties
		this.loadRequiredProperties(false, new Loader() {
			@Override
			public void loadRequiredProperties(RequiredProperties properties,
					OfficeFloorSourceContext context) throws Exception {
				context.getResource("missing");
			}
		});
	}

	/**
	 * Ensure able to obtain resource.
	 */
	public void testGetResource() throws Exception {

		final String location = "LOCATION";
		final InputStream resource = new ByteArrayInputStream(new byte[0]);

		// Record obtaining the configuration item
		this.recordReturn(this.resourceSource,
				this.resourceSource.sourceResource(location), resource);

		// Obtain the configuration item
		this.loadRequiredProperties(true, new Loader() {
			@Override
			public void loadRequiredProperties(RequiredProperties properties,
					OfficeFloorSourceContext context) throws Exception {
				assertSame("Incorrect resource", resource,
						context.getResource(location));
			}
		});
	}

	/**
	 * Ensure able to get the {@link ClassLoader}.
	 */
	public void testGetClassLoader() {

		// Attempt to load required properties
		this.loadRequiredProperties(true, new Loader() {
			@Override
			public void loadRequiredProperties(RequiredProperties properties,
					OfficeFloorSourceContext context) throws Exception {
				assertEquals("Incorrect class loader",
						LoadRequiredPropertiesTest.class.getClassLoader(),
						context.getClassLoader());
			}
		});
	}

	/**
	 * Ensure issue if fails to source the {@link RequiredProperties}.
	 */
	public void testFailSourceRequiredProperties() {

		final NullPointerException failure = new NullPointerException(
				"Fail source required properties");

		// Record failure to source the required properties
		this.issues.recordIssue(
				"Failed to source required properties from OfficeFloorSource (source="
						+ MockOfficeFloorSource.class.getName() + ", location="
						+ OFFICE_FLOOR_LOCATION + ")", failure);

		// Attempt to load required properties
		this.loadRequiredProperties(false, new Loader() {
			@Override
			public void loadRequiredProperties(RequiredProperties properties,
					OfficeFloorSourceContext context) throws Exception {
				throw failure;
			}
		});
	}

	/**
	 * Ensure issue if add {@link Property} with <code>null</code> name.
	 */
	public void testNullPropertyName() {

		// Record null property name
		this.issues
				.recordIssue("Required property specified with null name (label=null)");

		// Attempt to load required properties
		this.loadRequiredProperties(false, new Loader() {
			@Override
			public void loadRequiredProperties(RequiredProperties properties,
					OfficeFloorSourceContext context) throws Exception {
				properties.addRequiredProperty(null);
			}
		});
	}

	/**
	 * Ensure issue if add {@link Property} with <code>null</code> name but has
	 * a label.
	 */
	public void testNullPropertyNameButWithLabel() {

		// Record null property name
		this.issues
				.recordIssue("Required property specified with null name (label=LABEL)");

		// Attempt to load required properties
		this.loadRequiredProperties(false, new Loader() {
			@Override
			public void loadRequiredProperties(RequiredProperties properties,
					OfficeFloorSourceContext context) throws Exception {
				properties.addRequiredProperty(null, "LABEL");
			}
		});
	}

	/**
	 * Ensure issue if add {@link Property} twice with the same name.
	 */
	public void testAddRequiredPropertyTwice() {

		// Record null property name
		this.issues.recordIssue("Required property PROPERTY already added");

		// Attempt to load required properties
		this.loadRequiredProperties(false, new Loader() {
			@Override
			public void loadRequiredProperties(RequiredProperties properties,
					OfficeFloorSourceContext context) throws Exception {
				properties.addRequiredProperty("PROPERTY");
				properties.addRequiredProperty("PROPERTY", "LABEL");
			}
		});
	}

	/**
	 * Ensure can add {@link Property} by name.
	 */
	public void testAddRequiredPropertyByName() {

		// Attempt to load required properties
		PropertyList properties = this.loadRequiredProperties(true,
				new Loader() {
					@Override
					public void loadRequiredProperties(
							RequiredProperties properties,
							OfficeFloorSourceContext context) throws Exception {
						properties.addRequiredProperty("PROPERTY");
					}
				});

		// Validate required properties
		assertRequiredProperties(properties, "PROPERTY", "PROPERTY");
	}

	/**
	 * Ensure can add {@link Property} with a <code>null</code> label.
	 */
	public void testAddRequiredPropertyWithNullLabel() {

		// Attempt to load required properties
		PropertyList properties = this.loadRequiredProperties(true,
				new Loader() {
					@Override
					public void loadRequiredProperties(
							RequiredProperties properties,
							OfficeFloorSourceContext context) throws Exception {
						properties.addRequiredProperty("PROPERTY", null);
					}
				});

		// Validate required properties (with label defaulted)
		assertRequiredProperties(properties, "PROPERTY", "PROPERTY");
	}

	/**
	 * Ensure can add {@link Property} with a blank label.
	 */
	public void testAddRequiredPropertyWithBlankLabel() {

		// Attempt to load required properties
		PropertyList properties = this.loadRequiredProperties(true,
				new Loader() {
					@Override
					public void loadRequiredProperties(
							RequiredProperties properties,
							OfficeFloorSourceContext context) throws Exception {
						properties.addRequiredProperty("PROPERTY", " "); // blank
					}
				});

		// Validate required properties (with label defaulted)
		assertRequiredProperties(properties, "PROPERTY", "PROPERTY");
	}

	/**
	 * Ensure can add {@link Property} with a label.
	 */
	public void testAddRequiredPropertyWithLabel() {

		// Attempt to load required properties
		PropertyList properties = this.loadRequiredProperties(true,
				new Loader() {
					@Override
					public void loadRequiredProperties(
							RequiredProperties properties,
							OfficeFloorSourceContext context) throws Exception {
						properties.addRequiredProperty("PROPERTY", "LABEL");
					}
				});

		// Validate required properties
		assertRequiredProperties(properties, "PROPERTY", "LABEL");
	}

	/**
	 * Ensure can add multiple {@link Property} instances.
	 */
	public void testAddMultipleRequiredProperties() {

		// Attempt to load required properties
		PropertyList properties = this.loadRequiredProperties(true,
				new Loader() {
					@Override
					public void loadRequiredProperties(
							RequiredProperties properties,
							OfficeFloorSourceContext context) throws Exception {
						properties.addRequiredProperty("PROP_A", "LABEL_A");
						properties.addRequiredProperty("PROP_B");
						properties.addRequiredProperty("PROP_C", " ");
						properties.addRequiredProperty("PROP_D", "LABEL_D");
					}
				});

		// Validate required properties
		assertRequiredProperties(properties, "PROP_A", "LABEL_A", "PROP_B",
				"PROP_B", "PROP_C", "PROP_C", "PROP_D", "LABEL_D");
	}

	/**
	 * Loads the {@link RequiredProperties} within the input {@link Loader}.
	 * 
	 * @param isExpectedToLoad
	 *            Flag indicating if expecting to load the
	 *            {@link RequiredProperties}.
	 * @param loader
	 *            {@link Loader}.
	 * @param propertyNameValuePairs
	 *            {@link Property} name value pairs.
	 * @return {@link RequiredProperties} within a {@link PropertyList}.
	 */
	private PropertyList loadRequiredProperties(boolean isExpectedToLoad,
			Loader loader, String... propertyNameValuePairs) {

		// Replay mock objects
		this.replayMockObjects();

		// Create the property list
		PropertyList propertyList = new PropertyListImpl();
		for (int i = 0; i < propertyNameValuePairs.length; i += 2) {
			String name = propertyNameValuePairs[i];
			String value = propertyNameValuePairs[i + 1];
			propertyList.addProperty(name).setValue(value);
		}

		// Create the office loader and load the required properties
		OfficeFloorCompiler compiler = OfficeFloorCompiler
				.newOfficeFloorCompiler(null);
		compiler.setCompilerIssues(this.issues);
		compiler.addResources(this.resourceSource);
		OfficeFloorLoader officeFloorLoader = compiler.getOfficeFloorLoader();
		MockOfficeFloorSource.loader = loader;
		PropertyList requiredProperties = officeFloorLoader
				.loadRequiredProperties(MockOfficeFloorSource.class,
						OFFICE_FLOOR_LOCATION, propertyList);

		// Verify the mock objects
		this.verifyMockObjects();

		// Ensure if should be loaded
		if (isExpectedToLoad) {
			assertNotNull("Expected to load the required properties",
					requiredProperties);
		} else {
			assertNull("Should not load the required properties",
					requiredProperties);
		}

		// Return the required properties
		return requiredProperties;
	}

	/**
	 * Asserts the correctness of the {@link RequiredProperties}.
	 * 
	 * @param requiredProperties
	 *            {@link RequiredProperties} {@link PropertyList}.
	 * @param propertyNameLabelPairs
	 *            Name/label pairs of the expected {@link Property} instances.
	 */
	private static void assertRequiredProperties(
			PropertyList requiredProperties, String... propertyNameLabelPairs) {
		PropertyListUtil.validatePropertyNameLabels(requiredProperties,
				propertyNameLabelPairs);
	}

	/**
	 * Implemented to load the {@link RequiredProperties}.
	 */
	private interface Loader {

		/**
		 * Implemented to load the {@link RequiredProperties}.
		 * 
		 * @param properties
		 *            {@link RequiredProperties}.
		 * @param context
		 *            {@link OfficeFloorSourceContext}.
		 * @throws Exception
		 *             If fails to load the {@link RequiredProperties}.
		 */
		void loadRequiredProperties(RequiredProperties properties,
				OfficeFloorSourceContext context) throws Exception;
	}

	/**
	 * Mock {@link OfficeFloorSource} for testing.
	 */
	public static class MockOfficeFloorSource implements OfficeFloorSource {

		/**
		 * {@link Loader} to load the {@link RequiredProperties}.
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
		 * ================ OfficeFloorSource ==============================
		 */

		@Override
		public OfficeFloorSourceSpecification getSpecification() {
			fail("Should not required specification");
			return null;
		}

		@Override
		public void specifyConfigurationProperties(
				RequiredProperties requiredProperties,
				OfficeFloorSourceContext context) throws Exception {

			// Load the required properties
			loader.loadRequiredProperties(requiredProperties, context);
		}

		@Override
		public void sourceOfficeFloor(OfficeFloorDeployer deployer,
				OfficeFloorSourceContext context) throws Exception {
			fail("Should not source the office floor");
		}
	}

}
