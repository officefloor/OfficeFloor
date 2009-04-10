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
package net.officefloor.compile.impl.section;

import java.io.IOException;
import java.sql.Connection;
import java.util.Properties;

import net.officefloor.compile.impl.properties.PropertyListImpl;
import net.officefloor.compile.issues.CompilerIssues;
import net.officefloor.compile.issues.CompilerIssues.LocationType;
import net.officefloor.compile.properties.Property;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.section.SectionInputType;
import net.officefloor.compile.section.SectionLoader;
import net.officefloor.compile.section.SectionOutputType;
import net.officefloor.compile.section.SectionType;
import net.officefloor.compile.spi.office.source.OfficeSection;
import net.officefloor.compile.spi.section.SectionBuilder;
import net.officefloor.compile.spi.section.source.SectionSource;
import net.officefloor.compile.spi.section.source.SectionSourceContext;
import net.officefloor.compile.spi.section.source.SectionSourceSpecification;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.model.repository.ConfigurationContext;
import net.officefloor.model.repository.ConfigurationItem;

/**
 * Tests loading the {@link SectionType} from the {@link SectionSource}.
 * 
 * @author Daniel
 */
public class LoadSectionTypeTest extends OfficeFrameTestCase {

	/**
	 * Location of the {@link OfficeSection}.
	 */
	private final String SECTION_LOCATION = "SECTION";

	/**
	 * {@link ConfigurationContext}.
	 */
	private final ConfigurationContext configurationContext = this
			.createMock(ConfigurationContext.class);

	/**
	 * {@link CompilerIssues}.
	 */
	private final CompilerIssues issues = this.createMock(CompilerIssues.class);

	/*
	 * (non-Javadoc)
	 * 
	 * @see junit.framework.TestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception {
		MockSectionSource.reset();
	}

	/**
	 * Ensure issue if fail to instantiate the {@link SectionSource}.
	 */
	public void testFailInstantiate() {

		final RuntimeException failure = new RuntimeException(
				"instantiate failure");

		// Record failure to instantiate
		this.record_issue(
				"Failed to instantiate " + MockSectionSource.class.getName()
						+ " by default constructor", failure);

		// Attempt to obtain specification
		MockSectionSource.instantiateFailure = failure;
		this.loadSectionType(false, null);
	}

	/**
	 * Ensure obtain the correct {@link OfficeSection} location.
	 */
	public void testSectionLocation() {
		this.loadSectionType(true, new Loader() {
			@Override
			public void sourceSection(SectionBuilder section,
					SectionSourceContext context) throws Exception {
				assertEquals("Incorrect section location", SECTION_LOCATION,
						context.getSectionLocation());
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
		this.loadSectionType(false, new Loader() {
			@Override
			public void sourceSection(SectionBuilder section,
					SectionSourceContext context) throws Exception {
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
		this.loadSectionType(true, new Loader() {
			@Override
			public void sourceSection(SectionBuilder section,
					SectionSourceContext context) throws Exception {
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
		this.record_issue("Missing property 'missing' for SectionSource "
				+ MockSectionSource.class.getName());

		// Attempt to load section type
		this.loadSectionType(false, new Loader() {
			@Override
			public void sourceSection(SectionBuilder section,
					SectionSourceContext context) throws Exception {
				context.getProperty("missing");
			}
		});
	}

	/**
	 * Ensure able to get properties.
	 */
	public void testGetProperties() {

		// Attempt to load section type
		this.loadSectionType(true, new Loader() {
			@Override
			public void sourceSection(SectionBuilder section,
					SectionSourceContext context) throws Exception {
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

		// Attempt to load section type
		this.loadSectionType(true, new Loader() {
			@Override
			public void sourceSection(SectionBuilder section,
					SectionSourceContext context) throws Exception {
				assertEquals("Incorrect class loader",
						LoadSectionTypeTest.class.getClassLoader(), context
								.getClassLoader());
			}
		});
	}

	/**
	 * Ensure issue if fails to source the {@link SectionType}.
	 */
	public void testFailSourceSectionType() {

		final NullPointerException failure = new NullPointerException(
				"Fail source section type");

		// Record failure to source the section type
		this.record_issue(
				"Failed to source SectionType definition from SectionSource "
						+ MockSectionSource.class.getName(), failure);

		// Attempt to load section type
		this.loadSectionType(false, new Loader() {
			@Override
			public void sourceSection(SectionBuilder section,
					SectionSourceContext context) throws Exception {
				throw failure;
			}
		});
	}

	/**
	 * Ensure issue if <code>null</code> {@link SectionInputType} name.
	 */
	public void testNullInputName() {

		// Record null input name
		this.record_issue("Null name for input 0");

		// Attempt to load section type
		this.loadSectionType(false, new Loader() {
			@Override
			public void sourceSection(SectionBuilder section,
					SectionSourceContext context) throws Exception {
				section.addSectionInput(null, String.class.getName());
			}
		});
	}

	/**
	 * Ensure can create input with a parameter type.
	 */
	public void testInputWithParameterType() {
		// Load section type
		SectionType type = this.loadSectionType(true, new Loader() {
			@Override
			public void sourceSection(SectionBuilder section,
					SectionSourceContext context) throws Exception {
				section.addSectionInput("INPUT", String.class.getName());
			}
		});

		// Ensure correct input
		assertEquals("Incorrect number of inputs", 1, type
				.getSectionInputTypes().length);
		assertEquals("Incorrect input name", "INPUT", type
				.getSectionInputTypes()[0].getSectionInputName());
		assertEquals("Incorrect parameter type", String.class.getName(), type
				.getSectionInputTypes()[0].getParameterType());
	}

	/**
	 * Ensure can create input without a parameter type.
	 */
	public void testInputWithoutAParameterType() {
		// Load section type
		SectionType type = this.loadSectionType(true, new Loader() {
			@Override
			public void sourceSection(SectionBuilder section,
					SectionSourceContext context) throws Exception {
				section.addSectionInput("INPUT", null);
			}
		});

		// Ensure correct input
		assertEquals("Incorrect number of inputs", 1, type
				.getSectionInputTypes().length);
		assertEquals("Incorrect input name", "INPUT", type
				.getSectionInputTypes()[0].getSectionInputName());
		assertNull("Incorrect parameter type", type.getSectionInputTypes()[0]
				.getParameterType());
	}

	/**
	 * Ensure issue if <code>null</code> {@link SectionOutputType} name.
	 */
	public void testNullOutputName() {

		// Record null output name
		this.record_issue("Null name for output 0");

		// Attempt to load section type
		this.loadSectionType(false, new Loader() {
			@Override
			public void sourceSection(SectionBuilder section,
					SectionSourceContext context) throws Exception {
				section.addSectionOutput(null, String.class.getName(), false);
			}
		});
	}

	/**
	 * Ensure can create output with an argument type.
	 */
	public void testOutputWithArgumentType() {
		// Load section type
		SectionType type = this.loadSectionType(true, new Loader() {
			@Override
			public void sourceSection(SectionBuilder section,
					SectionSourceContext context) throws Exception {
				section.addSectionOutput("OUTPUT", Exception.class.getName(),
						true);
			}
		});

		// Ensure correct output
		assertEquals("Incorrect number of outputs", 1, type
				.getSectionOutputTypes().length);
		assertEquals("Incorrect output name", "OUTPUT", type
				.getSectionOutputTypes()[0].getSectionOutputName());
		assertEquals("Incorrect argument type", Exception.class.getName(), type
				.getSectionOutputTypes()[0].getArgumentType());
		assertTrue("Incorrect is escalation only",
				type.getSectionOutputTypes()[0].isEscalationOnly());
	}

	/**
	 * Ensure can create output without an argument type.
	 */
	public void testOutputWithoutAnArgumentType() {
		// Load section type
		SectionType type = this.loadSectionType(true, new Loader() {
			@Override
			public void sourceSection(SectionBuilder section,
					SectionSourceContext context) throws Exception {
				section.addSectionOutput("OUTPUT", null, false);
			}
		});

		// Ensure correct output
		assertEquals("Incorrect number of outputs", 1, type
				.getSectionOutputTypes().length);
		assertEquals("Incorrect output name", "OUTPUT", type
				.getSectionOutputTypes()[0].getSectionOutputName());
		assertNull("Should be no argument type",
				type.getSectionOutputTypes()[0].getArgumentType());
		assertFalse("Incorrect is escalation only", type
				.getSectionOutputTypes()[0].isEscalationOnly());
	}

	/**
	 * Ensure issue if <code>null</code> {@link SectionObjectNodeImpl} name.
	 */
	public void testNullObjectName() {

		// Record null object name
		this.record_issue("Null name for object 0");

		// Attempt to load section type
		this.loadSectionType(false, new Loader() {
			@Override
			public void sourceSection(SectionBuilder section,
					SectionSourceContext context) throws Exception {
				section.addSectionObject(null, Double.class.getName());
			}
		});
	}

	/**
	 * Ensure issue if <code>null</code> {@link SectionObjectNodeImpl} type.
	 */
	public void testNullObjectType() {

		// Record null object type
		this.record_issue("Null type for object 0 (name=OBJECT)");

		// Attempt to load section type
		this.loadSectionType(false, new Loader() {
			@Override
			public void sourceSection(SectionBuilder section,
					SectionSourceContext context) throws Exception {
				section.addSectionObject("OBJECT", null);
			}
		});
	}

	/**
	 * Ensure can create object with an object type.
	 */
	public void testObjectWithObjectType() {
		// Load section type
		SectionType type = this.loadSectionType(true, new Loader() {
			@Override
			public void sourceSection(SectionBuilder section,
					SectionSourceContext context) throws Exception {
				section.addSectionObject("OBJECT", Double.class.getName());
			}
		});

		// Ensure correct object
		assertEquals("Incorrect number of objects", 1, type
				.getSectionObjectTypes().length);
		assertEquals("Incorrect object name", "OBJECT", type
				.getSectionObjectTypes()[0].getSectionObjectName());
		assertEquals("Incorrect object type", Double.class.getName(), type
				.getSectionObjectTypes()[0].getObjectType());
	}

	/**
	 * Ensure can load {@link SectionType} with no inputs, outputs or objects.
	 */
	public void testEmptySectionType() {
		// Load section type
		SectionType type = this.loadSectionType(true, new Loader() {
			@Override
			public void sourceSection(SectionBuilder section,
					SectionSourceContext context) throws Exception {
				// Load nothing
			}
		});

		// Ensure empty section type
		assertEquals("Incorrect number of inputs", 0, type
				.getSectionInputTypes().length);
		assertEquals("Incorrect number of outputs", 0, type
				.getSectionOutputTypes().length);
		assertEquals("Incorrect number of objects", 0, type
				.getSectionObjectTypes().length);
	}

	/**
	 * Ensure can load {@link SectionType} with multiple inputs, outputs or
	 * objects.
	 */
	public void testSectionTypeWithMultipleInputsOutputObjects() {
		// Load section type
		SectionType type = this.loadSectionType(true, new Loader() {
			@Override
			public void sourceSection(SectionBuilder section,
					SectionSourceContext context) throws Exception {
				// Inputs
				section.addSectionInput("INPUT_A", Integer.class.getName());
				section.addSectionInput("INPUT_B", String.class.getName());
				section.addSectionInput("INPUT_C", null);
				// Outputs
				section.addSectionOutput("OUTPUT_A", Exception.class.getName(),
						true);
				section.addSectionOutput("OUTPUT_B", Double.class.getName(),
						false);
				section.addSectionOutput("OUTPUT_C", null, false);
				// Objects
				section.addSectionObject("OBJECT_A", Object.class.getName());
				section
						.addSectionObject("OBJECT_B", Connection.class
								.getName());
			}
		});

		// Ensure section type correct
		assertList(new String[] { "getSectionInputName", "getParameterType" },
				type.getSectionInputTypes(), new SectionInputNodeImpl(
						"INPUT_A", Integer.class.getName(), null, null),
				new SectionInputNodeImpl("INPUT_B", String.class.getName(),
						null, null), new SectionInputNodeImpl("INPUT_C", null,
						null, null));
		assertList(new String[] { "getSectionOutputName", "getArgumentType",
				"isEscalationOnly" }, type.getSectionOutputTypes(),
				new SectionOutputNodeImpl("OUTPUT_A",
						Exception.class.getName(), true, null, null),
				new SectionOutputNodeImpl("OUTPUT_B", Double.class.getName(),
						false, null, null), new SectionOutputNodeImpl(
						"OUTPUT_C", null, false, null, null));
		assertList(new String[] { "getSectionObjectName", "getObjectType" },
				type.getSectionObjectTypes(), new SectionObjectNodeImpl(
						"OBJECT_A", Object.class.getName(), null, null),
				new SectionObjectNodeImpl("OBJECT_B", Connection.class
						.getName(), null, null));
	}

	/**
	 * Records an issue.
	 * 
	 * @param issueDescription
	 *            Description of the issue.
	 */
	private void record_issue(String issueDescription) {
		this.issues.addIssue(LocationType.SECTION, SECTION_LOCATION, null,
				null, issueDescription);
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
		this.issues.addIssue(LocationType.SECTION, SECTION_LOCATION, null,
				null, issueDescription, cause);
	}

	/**
	 * Loads the {@link SectionType} within the input {@link Loader}.
	 * 
	 * @param isExpectedToLoad
	 *            Flag indicating if expecting to load the {@link SectionType}.
	 * @param loader
	 *            {@link Loader}.
	 * @param propertyNameValuePairs
	 *            {@link Property} name value pairs.
	 * @return Loaded {@link SectionType}.
	 */
	private SectionType loadSectionType(boolean isExpectedToLoad,
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

		// Create the section loader and load the section
		SectionLoader sectionLoader = new SectionLoaderImpl(SECTION_LOCATION);
		MockSectionSource.loader = loader;
		SectionType sectionType = sectionLoader.loadSectionType(
				MockSectionSource.class, this.configurationContext,
				propertyList, LoadSectionTypeTest.class.getClassLoader(),
				this.issues);

		// Verify the mock objects
		this.verifyMockObjects();

		// Ensure if should be loaded
		if (isExpectedToLoad) {
			assertNotNull("Expected to load the section type", sectionType);
		} else {
			assertNull("Should not load the section type", sectionType);
		}

		// Return the section type
		return sectionType;
	}

	/**
	 * Implemented to load the {@link SectionType}.
	 */
	private interface Loader {

		/**
		 * Implemented to load the {@link SectionType}.
		 * 
		 * @param section
		 *            {@link SectionBuilder}.
		 * @param context
		 *            {@link SectionSourceContext}.
		 * @throws Exception
		 *             If fails to source {@link SectionType}.
		 */
		void sourceSection(SectionBuilder section, SectionSourceContext context)
				throws Exception;
	}

	/**
	 * Mock {@link SectionSource} for testing.
	 */
	public static class MockSectionSource implements SectionSource {

		/**
		 * {@link Loader} to load the {@link SectionType}.
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
		public MockSectionSource() {
			if (instantiateFailure != null) {
				throw instantiateFailure;
			}
		}

		/*
		 * ================ SectionSource ======================================
		 */

		@Override
		public SectionSourceSpecification getSpecification() {
			fail("Should not be invoked in obtaining section type");
			return null;
		}

		@Override
		public void sourceSection(SectionBuilder sectionBuilder,
				SectionSourceContext context) throws Exception {
			loader.sourceSection(sectionBuilder, context);
		}
	}

}