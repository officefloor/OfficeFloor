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
package net.officefloor.compile.impl.section;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.util.Properties;

import junit.framework.TestCase;
import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.impl.issues.MockCompilerIssues;
import net.officefloor.compile.impl.managedobject.MockLoadManagedObject;
import net.officefloor.compile.impl.structure.SectionInputNodeImpl;
import net.officefloor.compile.impl.structure.SectionNodeImpl;
import net.officefloor.compile.impl.structure.SectionObjectNodeImpl;
import net.officefloor.compile.impl.structure.SectionOutputNodeImpl;
import net.officefloor.compile.impl.work.MockLoadWork;
import net.officefloor.compile.issues.CompilerIssue;
import net.officefloor.compile.issues.CompilerIssues;
import net.officefloor.compile.managedobject.ManagedObjectType;
import net.officefloor.compile.properties.Property;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.section.SectionInputType;
import net.officefloor.compile.section.SectionLoader;
import net.officefloor.compile.section.SectionObjectType;
import net.officefloor.compile.section.SectionOutputType;
import net.officefloor.compile.section.SectionType;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.section.SectionDesigner;
import net.officefloor.compile.spi.section.SectionObject;
import net.officefloor.compile.spi.section.source.SectionSource;
import net.officefloor.compile.spi.section.source.SectionSourceContext;
import net.officefloor.compile.spi.section.source.SectionSourceSpecification;
import net.officefloor.compile.spi.section.source.impl.AbstractSectionSource;
import net.officefloor.compile.work.WorkType;
import net.officefloor.frame.spi.TestSource;
import net.officefloor.frame.spi.source.ResourceSource;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.managedobject.clazz.ClassManagedObjectSource;
import net.officefloor.plugin.section.clazz.ClassSectionSource;
import net.officefloor.plugin.work.clazz.ClassWorkSource;

/**
 * Tests loading the {@link SectionType} from the {@link SectionSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class LoadSectionTypeTest extends OfficeFrameTestCase {

	/**
	 * Location of the {@link OfficeSection}.
	 */
	private final String SECTION_LOCATION = "SECTION";

	/**
	 * {@link ResourceSource}.
	 */
	private final ResourceSource resourceSource = this
			.createMock(ResourceSource.class);

	/**
	 * {@link CompilerIssues}.
	 */
	private final MockCompilerIssues issues = new MockCompilerIssues(this);

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
		this.issues.recordIssue(
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
			public void sourceSection(SectionDesigner section,
					SectionSourceContext context) throws Exception {
				assertEquals("Incorrect section location", SECTION_LOCATION,
						context.getSectionLocation());
			}
		});
	}

	/**
	 * Ensure issue if missing {@link Property}.
	 */
	public void testMissingProperty() {

		// Record missing property
		this.issues.recordIssue("Type", SectionNodeImpl.class,
				"Missing property 'missing' for SectionSource "
						+ MockSectionSource.class.getName());

		// Attempt to load section type
		this.loadSectionType(false, new Loader() {
			@Override
			public void sourceSection(SectionDesigner section,
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
			public void sourceSection(SectionDesigner section,
					SectionSourceContext context) throws Exception {
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
		this.issues.recordIssue("Type", SectionNodeImpl.class,
				"Can not load class 'missing' for SectionSource "
						+ MockSectionSource.class.getName());

		// Attempt to load section type
		this.loadSectionType(false, new Loader() {
			@Override
			public void sourceSection(SectionDesigner section,
					SectionSourceContext context) throws Exception {
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
		this.issues.recordIssue("Type", SectionNodeImpl.class,
				"Can not obtain resource at location 'missing' for SectionSource "
						+ MockSectionSource.class.getName());

		// Attempt to load section type
		this.loadSectionType(false, new Loader() {
			@Override
			public void sourceSection(SectionDesigner section,
					SectionSourceContext context) throws Exception {
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
		this.loadSectionType(true, new Loader() {
			@Override
			public void sourceSection(SectionDesigner section,
					SectionSourceContext context) throws Exception {
				assertSame("Incorrect resource", resource,
						context.getResource(location));
			}
		});
	}

	/**
	 * Ensure able to get the {@link ClassLoader}.
	 */
	public void testGetClassLoader() {

		// Attempt to load section type
		this.loadSectionType(true, new Loader() {
			@Override
			public void sourceSection(SectionDesigner section,
					SectionSourceContext context) throws Exception {
				assertEquals("Incorrect class loader",
						LoadSectionTypeTest.class.getClassLoader(),
						context.getClassLoader());
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
		this.issues.recordIssue("Type", SectionNodeImpl.class,
				"Failed to source SectionType definition from SectionSource "
						+ MockSectionSource.class.getName(), failure);

		// Attempt to load section type
		this.loadSectionType(false, new Loader() {
			@Override
			public void sourceSection(SectionDesigner section,
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
		this.issues.recordIssue(null, SectionInputNodeImpl.class,
				"Null name for Section Input");

		// Attempt to load section type
		this.loadSectionType(false, new Loader() {
			@Override
			public void sourceSection(SectionDesigner section,
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
			public void sourceSection(SectionDesigner section,
					SectionSourceContext context) throws Exception {
				section.addSectionInput("INPUT", String.class.getName());
			}
		});

		// Ensure correct input
		assertEquals("Incorrect number of inputs", 1,
				type.getSectionInputTypes().length);
		assertEquals("Incorrect input name", "INPUT",
				type.getSectionInputTypes()[0].getSectionInputName());
		assertEquals("Incorrect parameter type", String.class.getName(),
				type.getSectionInputTypes()[0].getParameterType());
	}

	/**
	 * Ensure can create input without a parameter type.
	 */
	public void testInputWithoutAParameterType() {
		// Load section type
		SectionType type = this.loadSectionType(true, new Loader() {
			@Override
			public void sourceSection(SectionDesigner section,
					SectionSourceContext context) throws Exception {
				section.addSectionInput("INPUT", null);
			}
		});

		// Ensure correct input
		assertEquals("Incorrect number of inputs", 1,
				type.getSectionInputTypes().length);
		assertEquals("Incorrect input name", "INPUT",
				type.getSectionInputTypes()[0].getSectionInputName());
		assertNull("Incorrect parameter type",
				type.getSectionInputTypes()[0].getParameterType());
	}

	/**
	 * Ensure issue if <code>null</code> {@link SectionOutputType} name.
	 */
	public void testNullOutputName() {

		// Record null output name
		this.issues.recordIssue(null, SectionOutputNodeImpl.class,
				"Null name for Section Output");

		// Attempt to load section type
		this.loadSectionType(false, new Loader() {
			@Override
			public void sourceSection(SectionDesigner section,
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
			public void sourceSection(SectionDesigner section,
					SectionSourceContext context) throws Exception {
				section.addSectionOutput("OUTPUT", Exception.class.getName(),
						true);
			}
		});

		// Ensure correct output
		assertEquals("Incorrect number of outputs", 1,
				type.getSectionOutputTypes().length);
		assertEquals("Incorrect output name", "OUTPUT",
				type.getSectionOutputTypes()[0].getSectionOutputName());
		assertEquals("Incorrect argument type", Exception.class.getName(),
				type.getSectionOutputTypes()[0].getArgumentType());
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
			public void sourceSection(SectionDesigner section,
					SectionSourceContext context) throws Exception {
				section.addSectionOutput("OUTPUT", null, false);
			}
		});

		// Ensure correct output
		assertEquals("Incorrect number of outputs", 1,
				type.getSectionOutputTypes().length);
		assertEquals("Incorrect output name", "OUTPUT",
				type.getSectionOutputTypes()[0].getSectionOutputName());
		assertNull("Should be no argument type",
				type.getSectionOutputTypes()[0].getArgumentType());
		assertFalse("Incorrect is escalation only",
				type.getSectionOutputTypes()[0].isEscalationOnly());
	}

	/**
	 * Ensure issue if <code>null</code> {@link SectionObjectNodeImpl} name.
	 */
	public void testNullObjectName() {

		// Record null object name
		this.issues.recordIssue(null, SectionObjectNodeImpl.class,
				"Null name for Section Object");

		// Attempt to load section type
		this.loadSectionType(false, new Loader() {
			@Override
			public void sourceSection(SectionDesigner section,
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
		this.issues.recordIssue("OBJECT", SectionObjectNodeImpl.class,
				"Null type for Section Object (name=OBJECT)");

		// Attempt to load section type
		this.loadSectionType(false, new Loader() {
			@Override
			public void sourceSection(SectionDesigner section,
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
			public void sourceSection(SectionDesigner section,
					SectionSourceContext context) throws Exception {
				section.addSectionObject("OBJECT", Double.class.getName());
			}
		});

		// Ensure correct object
		assertEquals("Incorrect number of objects", 1,
				type.getSectionObjectTypes().length);
		SectionObjectType objectType = type.getSectionObjectTypes()[0];
		assertEquals("Incorrect object name", "OBJECT",
				objectType.getSectionObjectName());
		assertEquals("Incorrect object type", Double.class.getName(),
				objectType.getObjectType());
		assertNull("Should not be qualified", objectType.getTypeQualifier());
	}

	/**
	 * Ensure can create object with an qualified object type.
	 */
	public void testObjectWithQualifiedObjectType() {
		// Load section type
		SectionType type = this.loadSectionType(true, new Loader() {
			@Override
			public void sourceSection(SectionDesigner section,
					SectionSourceContext context) throws Exception {
				SectionObject object = section.addSectionObject("OBJECT",
						Double.class.getName());
				object.setTypeQualifier("QUALIFIED");
			}
		});

		// Ensure correct object
		assertEquals("Incorrect number of objects", 1,
				type.getSectionObjectTypes().length);
		SectionObjectType objectType = type.getSectionObjectTypes()[0];
		assertEquals("Incorrect object name", "OBJECT",
				objectType.getSectionObjectName());
		assertEquals("Incorrect object type", Double.class.getName(),
				objectType.getObjectType());
		assertEquals("Incorrect type qualifier", "QUALIFIED",
				objectType.getTypeQualifier());
	}

	/**
	 * Ensure can load {@link SectionType} with no inputs, outputs or objects.
	 */
	public void testEmptySectionType() {
		// Load section type
		SectionType type = this.loadSectionType(true, new Loader() {
			@Override
			public void sourceSection(SectionDesigner section,
					SectionSourceContext context) throws Exception {
				// Load nothing
			}
		});

		// Ensure empty section type
		assertEquals("Incorrect number of inputs", 0,
				type.getSectionInputTypes().length);
		assertEquals("Incorrect number of outputs", 0,
				type.getSectionOutputTypes().length);
		assertEquals("Incorrect number of objects", 0,
				type.getSectionObjectTypes().length);
	}

	/**
	 * Ensure can load {@link SectionType} with multiple inputs, outputs or
	 * objects.
	 */
	public void testSectionTypeWithMultipleInputsOutputObjects() {
		// Load section type
		SectionType type = this.loadSectionType(true, new Loader() {
			@Override
			public void sourceSection(SectionDesigner section,
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
				section.addSectionObject("OBJECT_B", Connection.class.getName())
						.setTypeQualifier("TYPE");
			}
		});

		// Ensure section type correct
		assertList(new String[] { "getSectionInputName", "getParameterType" },
				type.getSectionInputTypes(), new SectionInputTypeImpl(
						"INPUT_A", Integer.class.getName()),
				new SectionInputTypeImpl("INPUT_B", String.class.getName()),
				new SectionInputTypeImpl("INPUT_C", null));
		assertList(new String[] { "getSectionOutputName", "getArgumentType",
				"isEscalationOnly" }, type.getSectionOutputTypes(),
				new SectionOutputTypeImpl("OUTPUT_A",
						Exception.class.getName(), true),
				new SectionOutputTypeImpl("OUTPUT_B", Double.class.getName(),
						false), new SectionOutputTypeImpl("OUTPUT_C", null,
						false));
		assertList(new String[] { "getSectionObjectName", "getObjectType",
				"getTypeQualifier" }, type.getSectionObjectTypes(),
				new SectionObjectTypeImpl("OBJECT_A", Object.class.getName(),
						null), new SectionObjectTypeImpl("OBJECT_B",
						Connection.class.getName(), "TYPE"));
	}

	/**
	 * Ensure can load {@link WorkType}.
	 */
	public void testLoadWorkType() {

		// Record capture issues for loading work type
		this.issues.recordCaptureIssues(false);

		// Load the section type which loads the work type
		this.loadSectionType(true, new Loader() {
			@Override
			public void sourceSection(SectionDesigner section,
					SectionSourceContext context) throws Exception {

				// Load the work type
				PropertyList properties = context.createPropertyList();
				properties
						.addProperty(ClassWorkSource.CLASS_NAME_PROPERTY_NAME)
						.setValue(MockLoadWork.class.getName());
				WorkType<?> workType = context.loadWorkType(
						ClassWorkSource.class.getName(), properties);

				// Ensure correct work type
				MockLoadWork.assertWorkType(workType);
			}
		});
	}

	/**
	 * Ensure issue if fails to load the {@link WorkType}.
	 */
	public void testFailLoadingWorkType() {

		// Ensure issue in not loading work type
		CompilerIssue[] issues = this.issues.recordCaptureIssues(true);
		this.issues.recordIssue("Type", SectionNodeImpl.class,
				"Missing property 'class.name' for WorkSource "
						+ ClassWorkSource.class.getName());
		this.issues.recordIssue(
				"Type",
				SectionNodeImpl.class,
				"Failure loading WorkType from source "
						+ ClassWorkSource.class.getName(), issues);

		// Fail to load the work type
		this.loadSectionType(false, new Loader() {
			@Override
			public void sourceSection(SectionDesigner section,
					SectionSourceContext context) throws Exception {

				// Do not specify class causing failure to load work type
				PropertyList properties = context.createPropertyList();
				context.loadWorkType(ClassWorkSource.class.getName(),
						properties);

				// Should not reach this point
				fail("Should not successfully load work type");
			}
		});
	}

	/**
	 * Ensure can load {@link ManagedObjectType}.
	 */
	public void testLoadManagedObjectType() {

		// Record capture issues for loading managed object type
		this.issues.recordCaptureIssues(false);

		// Load the section type which loads the managed object type
		this.loadSectionType(true, new Loader() {
			@Override
			public void sourceSection(SectionDesigner section,
					SectionSourceContext context) throws Exception {

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
		CompilerIssue[] issues = this.issues.recordCaptureIssues(true);
		this.issues.recordIssue("Type", SectionNodeImpl.class,
				"Missing property 'class.name'");
		this.issues.recordIssue("Type", SectionNodeImpl.class,
				"Failure loading ManagedObjectType from source "
						+ ClassManagedObjectSource.class.getName(), issues);

		// Fail to load the managed object type
		this.loadSectionType(false, new Loader() {
			@Override
			public void sourceSection(SectionDesigner section,
					SectionSourceContext context) throws Exception {

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
	 * Ensure can load {@link SectionType}.
	 */
	public void testLoadSubSectionType() {

		// Record capture issues for loading section and sub section type
		this.issues.recordCaptureIssues(false);
		this.issues.recordCaptureIssues(false);

		// Load the section type which loads the sub section type
		this.loadSectionType(true, new Loader() {
			@Override
			public void sourceSection(SectionDesigner section,
					SectionSourceContext context) throws Exception {

				// Load the sub section type
				PropertyList properties = context.createPropertyList();
				SectionType sectionType = context.loadSectionType(
						ClassSectionSource.class.getName(),
						MockLoadSection.class.getName(), properties);

				// Ensure correct section type
				MockLoadSection.assertSectionType(sectionType);
			}
		});
	}

	/**
	 * Ensure issue if fails to load the {@link SectionType}.
	 */
	public void testIssueLoadingSectionType() {

		// Ensure issue in not loading managed object type
		CompilerIssue[] issues = this.issues.recordCaptureIssues(true);
		this.issues.recordIssue("Type", SectionNodeImpl.class,
				"Missing property 'missing' for SectionSource "
						+ FailSectionSource.class.getName());
		this.issues.recordIssue("Type", SectionNodeImpl.class,
				"Failure loading SectionType from source "
						+ FailSectionSource.class.getName(), issues);

		// Fail to load the managed object type
		this.loadSectionType(false, new Loader() {
			@Override
			public void sourceSection(SectionDesigner section,
					SectionSourceContext context) throws Exception {

				// Do not specify class causing failure to load type
				PropertyList properties = context.createPropertyList();
				context.loadSectionType(FailSectionSource.class.getName(),
						"FailLocation", properties);
			}
		});
	}

	public static class FailSectionSource extends AbstractSectionSource {

		@Override
		protected void loadSpecification(SpecificationContext context) {
			// No specification
		}

		@Override
		public void sourceSection(SectionDesigner designer,
				SectionSourceContext context) throws Exception {
			// Obtain property causing failure
			context.getProperty("missing");

			// Ensure failed
			TestCase.fail("Should not successfully obtain a missing property");
		}
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

		// Create the compiler
		OfficeFloorCompiler compiler = OfficeFloorCompiler
				.newOfficeFloorCompiler(null);

		// Create the property list
		PropertyList propertyList = compiler.createPropertyList();
		for (int i = 0; i < propertyNameValuePairs.length; i += 2) {
			String name = propertyNameValuePairs[i];
			String value = propertyNameValuePairs[i + 1];
			propertyList.addProperty(name).setValue(value);
		}

		// Create the section loader and load the section
		compiler.setCompilerIssues(this.issues);
		compiler.addResources(this.resourceSource);
		SectionLoader sectionLoader = compiler.getSectionLoader();
		MockSectionSource.loader = loader;
		SectionType sectionType = sectionLoader.loadSectionType(
				MockSectionSource.class, SECTION_LOCATION, propertyList);

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
		 *            {@link SectionDesigner}.
		 * @param context
		 *            {@link SectionSourceContext}.
		 * @throws Exception
		 *             If fails to source {@link SectionType}.
		 */
		void sourceSection(SectionDesigner section, SectionSourceContext context)
				throws Exception;
	}

	/**
	 * Mock {@link SectionSource} for testing.
	 */
	@TestSource
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
		public void sourceSection(SectionDesigner sectionBuilder,
				SectionSourceContext context) throws Exception {
			loader.sourceSection(sectionBuilder, context);
		}
	}

}