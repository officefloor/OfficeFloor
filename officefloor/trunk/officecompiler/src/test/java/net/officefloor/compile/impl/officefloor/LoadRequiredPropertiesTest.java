/*
 *  Office Floor, Application Server
 *  Copyright (C) 2006 Daniel Sagenschneider
 *
 *  This program is free software; you can redistribute it and/or modify it under the terms 
 *  of the GNU General Public License as published by the Free Software Foundation; either 
 *  version 2 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program; 
 *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, 
 *  MA 02111-1307 USA
 */
package net.officefloor.compile.impl.officefloor;

import java.io.IOException;
import java.util.Properties;

import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.impl.properties.PropertyListImpl;
import net.officefloor.compile.impl.structure.AbstractStructureTestCase;
import net.officefloor.compile.issues.CompilerIssues.LocationType;
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
import net.officefloor.model.repository.ConfigurationContext;
import net.officefloor.model.repository.ConfigurationItem;

/**
 * Tests loading the required {@link PropertyList}.
 * 
 * @author Daniel
 */
public class LoadRequiredPropertiesTest extends AbstractStructureTestCase {

	/**
	 * Location of the {@link OfficeFloor}.
	 */
	private final String OFFICE_FLOOR_LOCATION = "OFFICE_FLOOR";

	/**
	 * {@link ConfigurationContext}.
	 */
	private final ConfigurationContext configurationContext = this
			.createMock(ConfigurationContext.class);

	/*
	 * (non-Javadoc)
	 * 
	 * @see junit.framework.TestCase#setUp()
	 */
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
		this.record_issue("Failed to instantiate "
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
		this.loadRequiredProperties(false, new Loader() {
			@Override
			public void loadRequiredProperties(RequiredProperties properties,
					OfficeFloorSourceContext context) throws Exception {
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
		this.loadRequiredProperties(true, new Loader() {
			@Override
			public void loadRequiredProperties(RequiredProperties properties,
					OfficeFloorSourceContext context) throws Exception {
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
		this.record_issue("Missing property 'missing' for OfficeFloorSource "
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
		this.record_issue(
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
		this
				.record_issue("Required property specified with null name (label=null)");

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
		this
				.record_issue("Required property specified with null name (label=LABEL)");

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
		this.record_issue("Required property PROPERTY already added");

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
	 * Records an issue.
	 * 
	 * @param issueDescription
	 *            Description of the issue.
	 */
	private void record_issue(String issueDescription) {
		this.issues.addIssue(LocationType.OFFICE_FLOOR, OFFICE_FLOOR_LOCATION,
				null, null, issueDescription);
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
		this.issues.addIssue(LocationType.OFFICE_FLOOR, OFFICE_FLOOR_LOCATION,
				null, null, issueDescription, cause);
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
				.newOfficeFloorCompiler();
		compiler.setCompilerIssues(this.issues);
		compiler.setConfigurationContext(this.configurationContext);
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