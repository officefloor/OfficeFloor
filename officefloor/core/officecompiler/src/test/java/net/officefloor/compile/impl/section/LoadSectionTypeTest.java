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

package net.officefloor.compile.impl.section;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.sql.Connection;
import java.util.Properties;
import java.util.logging.Logger;

import junit.framework.TestCase;
import net.officefloor.compile.AvailableServiceFactory;
import net.officefloor.compile.FailServiceFactory;
import net.officefloor.compile.MissingServiceFactory;
import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.impl.managedfunction.MockLoadFunctionNamespace;
import net.officefloor.compile.impl.managedobject.MockLoadManagedObject;
import net.officefloor.compile.impl.structure.FunctionNamespaceNodeImpl;
import net.officefloor.compile.impl.structure.ManagedObjectSourceNodeImpl;
import net.officefloor.compile.impl.structure.OfficeNodeImpl;
import net.officefloor.compile.impl.structure.SectionInputNodeImpl;
import net.officefloor.compile.impl.structure.SectionNodeImpl;
import net.officefloor.compile.impl.structure.SectionObjectNodeImpl;
import net.officefloor.compile.impl.structure.SectionOutputNodeImpl;
import net.officefloor.compile.internal.structure.Node;
import net.officefloor.compile.issues.CompileError;
import net.officefloor.compile.issues.CompilerIssue;
import net.officefloor.compile.issues.CompilerIssues;
import net.officefloor.compile.managedfunction.FunctionNamespaceType;
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
import net.officefloor.compile.test.issues.MockCompilerIssues;
import net.officefloor.configuration.ConfigurationItem;
import net.officefloor.frame.api.source.ResourceSource;
import net.officefloor.frame.api.source.TestSource;
import net.officefloor.frame.test.Closure;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.managedfunction.clazz.ClassManagedFunctionSource;
import net.officefloor.plugin.managedobject.clazz.ClassManagedObjectSource;
import net.officefloor.plugin.section.clazz.ClassSectionSource;

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
	private final ResourceSource resourceSource = this.createMock(ResourceSource.class);

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

		final RuntimeException failure = new RuntimeException("instantiate failure");

		// Record failure to instantiate
		this.issues.recordIssue(OfficeFloorCompiler.TYPE, OfficeNodeImpl.class,
				"Failed to instantiate " + MockSectionSource.class.getName() + " by default constructor", failure);

		// Attempt to obtain specification
		MockSectionSource.instantiateFailure = failure;
		this.loadSectionType(false, null);
	}

	/**
	 * Ensure obtain the correct {@link OfficeSection} location.
	 */
	public void testSectionLocation() {
		this.loadSectionType(true, (section, context) -> {
			assertEquals("Incorrect section location", SECTION_LOCATION, context.getSectionLocation());
		});
	}

	/**
	 * Ensure issue if missing {@link Property}.
	 */
	public void testMissingProperty() {

		// Record missing property
		this.issues.recordIssue(OfficeFloorCompiler.TYPE, SectionNodeImpl.class, "Must specify property 'missing'");

		// Attempt to load section type
		this.loadSectionType(false, (section, context) -> {
			context.getProperty("missing");
		});
	}

	/**
	 * Ensure able to get properties.
	 */
	public void testGetProperties() {

		// Attempt to load section type
		this.loadSectionType(true, (section, context) -> {
			assertEquals("Ensure get defaulted property", "DEFAULT", context.getProperty("missing", "DEFAULT"));
			assertEquals("Ensure get property ONE", "1", context.getProperty("ONE"));
			assertEquals("Ensure get property TWO", "2", context.getProperty("TWO"));
			String[] names = context.getPropertyNames();
			assertEquals("Incorrect number of property names", 2, names.length);
			assertEquals("Incorrect property name 0", "ONE", names[0]);
			assertEquals("Incorrect property name 1", "TWO", names[1]);
			Properties properties = context.getProperties();
			assertEquals("Incorrect number of properties", 2, properties.size());
			assertEquals("Incorrect property ONE", "1", properties.get("ONE"));
			assertEquals("Incorrect property TWO", "2", properties.get("TWO"));
		}, "ONE", "1", "TWO", "2");
	}

	/**
	 * Ensure issue if missing {@link Class}.
	 */
	public void testMissingClass() {

		// Record missing class
		this.issues.recordIssue(OfficeFloorCompiler.TYPE, SectionNodeImpl.class, "Can not load class 'missing'");

		// Attempt to load section type
		this.loadSectionType(false, (section, context) -> {
			context.loadClass("missing");
		});
	}

	/**
	 * Ensure issue if missing resource.
	 */
	public void testMissingResource() {

		// Record missing resource
		this.recordReturn(this.resourceSource, this.resourceSource.sourceResource("missing"), null);
		this.issues.recordIssue(OfficeFloorCompiler.TYPE, SectionNodeImpl.class,
				"Can not obtain resource at location 'missing'");

		// Attempt to load section type
		this.loadSectionType(false, (section, context) -> {
			context.getResource("missing");
		});
	}

	/**
	 * Ensure able to obtain a resource.
	 */
	public void testGetResource() throws Exception {

		final String location = "LOCATION";
		final InputStream resource = new ByteArrayInputStream(new byte[0]);

		// Record obtaining the resource
		this.recordReturn(this.resourceSource, this.resourceSource.sourceResource(location), resource);

		// Obtain the configuration item
		this.loadSectionType(true, (section, context) -> {
			assertSame("Incorrect resource", resource, context.getResource(location));
		});
	}

	/**
	 * Ensure issue if missing service.
	 */
	public void testMissingService() {

		// Record missing service
		this.issues.recordIssue(OfficeFloorCompiler.TYPE, SectionNodeImpl.class,
				MissingServiceFactory.getIssueDescription());

		// Attempt to load
		this.loadSectionType(false, (section, context) -> context.loadService(MissingServiceFactory.class, null));
	}

	/**
	 * Ensure issue if fail to load service.
	 */
	public void testFailLoadService() {

		// Record load issue for service
		this.issues.recordIssue(OfficeFloorCompiler.TYPE, SectionNodeImpl.class,
				FailServiceFactory.getIssueDescription(), FailServiceFactory.getCause());

		// Attempt to load
		this.loadSectionType(false, (section, context) -> context.loadService(FailServiceFactory.class, null));
	}

	/**
	 * Ensure can load service.
	 */
	public void testLoadService() {
		Closure<Object> service = new Closure<>();
		this.loadSectionType(true, (section, context) -> {
			service.value = context.loadService(AvailableServiceFactory.class, null);
		});
		assertSame("Incorrect service", AvailableServiceFactory.getService(), service.value);
	}

	/**
	 * Ensure issue if missing {@link ConfigurationItem}.
	 */
	public void testMissingConfigurationItem() {

		// Record missing configuration item
		this.recordReturn(this.resourceSource, this.resourceSource.sourceResource("missing"), null);
		this.issues.recordIssue(OfficeFloorCompiler.TYPE, SectionNodeImpl.class,
				"Can not obtain ConfigurationItem at location 'missing'");

		// Attempt to load section
		this.loadSectionType(false, (section, context) -> {
			context.getConfigurationItem("missing", null);
		});
	}

	/**
	 * Ensure issue if missing {@link Property} for {@link ConfigurationItem}.
	 */
	public void testMissingPropertyForConfigurationItem() {

		// Record missing resource
		this.recordReturn(this.resourceSource, this.resourceSource.sourceResource("configuration"),
				new ByteArrayInputStream("${missing}".getBytes()));
		this.issues.recordIssue(OfficeFloorCompiler.TYPE, SectionNodeImpl.class,
				"Can not obtain ConfigurationItem at location 'configuration' as missing property 'missing'");

		// Attempt to load section
		this.loadSectionType(false, (section, context) -> {
			context.getConfigurationItem("configuration", null);
		});
	}

	/**
	 * Ensure able to obtain a {@link ConfigurationItem}.
	 */
	public void testGetConfigurationItem() throws Exception {

		final String location = "LOCATION";
		final InputStream resource = new ByteArrayInputStream("content".getBytes());

		// Record obtaining the configuration item
		this.recordReturn(this.resourceSource, this.resourceSource.sourceResource(location), resource);

		// Obtain the configuration item
		this.loadSectionType(true, (section, context) -> {
			Reader configuration = context.getConfigurationItem(location, null).getReader();
			assertContents(new StringReader("content"), configuration);
		});
	}

	/**
	 * Ensure able to tag replace {@link ConfigurationItem}.
	 */
	public void testTagReplaceConfigurationItem() throws Exception {

		final String location = "LOCATION";
		final InputStream resource = new ByteArrayInputStream("${tag}".getBytes());

		// Record obtaining the configuration item
		this.recordReturn(this.resourceSource, this.resourceSource.sourceResource(location), resource);

		// Obtain the configuration item
		this.loadSectionType(true, (section, context) -> {
			Reader configuration = context.getConfigurationItem(location, null).getReader();
			assertContents(new StringReader("replace"), configuration);
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
		this.issues.recordIssue(OfficeFloorCompiler.TYPE, SectionNodeImpl.class,
				"Failed to obtain ConfigurationItem at location 'LOCATION': TEST", failure);

		// Obtain the configuration item
		this.loadSectionType(false, (section, context) -> {
			context.getConfigurationItem(location, null);
			fail("Should not be successful");
		});
	}

	/**
	 * Ensure able to get the {@link Logger}.
	 */
	public void testGetLogger() {
		Closure<String> loggerName = new Closure<>();
		this.loadSectionType(true, (section, context) -> {
			loggerName.value = context.getLogger().getName();
		});
		assertEquals("Should log to section name", OfficeFloorCompiler.TYPE, loggerName.value);
	}

	/**
	 * Ensure able to get the {@link ClassLoader}.
	 */
	public void testGetClassLoader() {
		Closure<ClassLoader> classLoader = new Closure<>();
		this.loadSectionType(true, (section, context) -> {
			classLoader.value = context.getClassLoader();
		});
		assertEquals("Incorrect class loader", Thread.currentThread().getContextClassLoader(), classLoader.value);
	}

	/**
	 * Ensure issue if fails to source the {@link SectionType}.
	 */
	public void testFailSourceSectionType() {

		final NullPointerException failure = new NullPointerException("Fail source section type");

		// Record failure to source the section type
		this.issues.recordIssue(OfficeFloorCompiler.TYPE, SectionNodeImpl.class,
				"Failed to source SectionType definition from SectionSource " + MockSectionSource.class.getName(),
				failure);

		// Attempt to load section type
		this.loadSectionType(false, (section, context) -> {
			throw failure;
		});
	}

	/**
	 * Ensure issue if <code>null</code> {@link SectionInputType} name.
	 */
	public void testNullInputName() {

		// Record null input name
		this.issues.recordIssue(Node.qualify(OfficeFloorCompiler.TYPE, null), SectionInputNodeImpl.class,
				"Null name for Section Input");

		// Attempt to load section type
		this.loadSectionType(false, (section, context) -> {
			section.addSectionInput(null, String.class.getName());
		});
	}

	/**
	 * Ensure can create input with a parameter type.
	 */
	public void testInputWithParameterType() {
		// Load section type
		SectionType type = this.loadSectionType(true, (section, context) -> {
			section.addSectionInput("INPUT", String.class.getName());
		});

		// Ensure correct input
		assertEquals("Incorrect number of inputs", 1, type.getSectionInputTypes().length);
		assertEquals("Incorrect input name", "INPUT", type.getSectionInputTypes()[0].getSectionInputName());
		assertEquals("Incorrect parameter type", String.class.getName(),
				type.getSectionInputTypes()[0].getParameterType());
	}

	/**
	 * Ensure can create input without a parameter type.
	 */
	public void testInputWithoutAParameterType() {
		// Load section type
		SectionType type = this.loadSectionType(true, (section, context) -> {
			section.addSectionInput("INPUT", null);
		});

		// Ensure correct input
		assertEquals("Incorrect number of inputs", 1, type.getSectionInputTypes().length);
		assertEquals("Incorrect input name", "INPUT", type.getSectionInputTypes()[0].getSectionInputName());
		assertNull("Incorrect parameter type", type.getSectionInputTypes()[0].getParameterType());
	}

	/**
	 * Ensure can create input with annotations.
	 */
	public void testInputWithAnnotations() {
		// Load section type
		SectionType type = this.loadSectionType(true, (section, context) -> {
			section.addSectionInput("NO_ANNOTATION", null);
			section.addSectionInput("ANNOTATION", null).addAnnotation("TEST");
		});

		// Ensure correct input with annotation
		assertEquals("Incorrect number of inputs", 2, type.getSectionInputTypes().length);
		SectionInputType annotation = type.getSectionInputTypes()[0];
		assertEquals("Incorrect input", "ANNOTATION", annotation.getSectionInputName());
		assertEquals("Incorrect number of annotations", 1, annotation.getAnnotations().length);
		assertEquals("Incorrect annotations", "TEST", annotation.getAnnotations()[0]);
		assertEquals("Incorrect by type annotation", "TEST", annotation.getAnnotation(String.class));
		SectionInputType noAnnotation = type.getSectionInputTypes()[1];
		assertEquals("Should be no annotations", 0, noAnnotation.getAnnotations().length);
		assertNull("Should be no by type annotation", noAnnotation.getAnnotation(String.class));
	}

	/**
	 * Ensure issue if <code>null</code> {@link SectionOutputType} name.
	 */
	public void testNullOutputName() {

		// Record null output name
		this.issues.recordIssue(Node.qualify(OfficeFloorCompiler.TYPE, null), SectionOutputNodeImpl.class,
				"Null name for Section Output");

		// Attempt to load section type
		this.loadSectionType(false, (section, context) -> {
			section.addSectionOutput(null, String.class.getName(), false);
		});
	}

	/**
	 * Ensure can create output with an argument type.
	 */
	public void testOutputWithArgumentType() {
		// Load section type
		SectionType type = this.loadSectionType(true, (section, context) -> {
			section.addSectionOutput("OUTPUT", Exception.class.getName(), true);
		});

		// Ensure correct output
		assertEquals("Incorrect number of outputs", 1, type.getSectionOutputTypes().length);
		assertEquals("Incorrect output name", "OUTPUT", type.getSectionOutputTypes()[0].getSectionOutputName());
		assertEquals("Incorrect argument type", Exception.class.getName(),
				type.getSectionOutputTypes()[0].getArgumentType());
		assertTrue("Incorrect is escalation only", type.getSectionOutputTypes()[0].isEscalationOnly());
	}

	/**
	 * Ensure can create output without an argument type.
	 */
	public void testOutputWithoutAnArgumentType() {
		// Load section type
		SectionType type = this.loadSectionType(true, (section, context) -> {
			section.addSectionOutput("OUTPUT", null, false);
		});

		// Ensure correct output
		assertEquals("Incorrect number of outputs", 1, type.getSectionOutputTypes().length);
		assertEquals("Incorrect output name", "OUTPUT", type.getSectionOutputTypes()[0].getSectionOutputName());
		assertNull("Should be no argument type", type.getSectionOutputTypes()[0].getArgumentType());
		assertFalse("Incorrect is escalation only", type.getSectionOutputTypes()[0].isEscalationOnly());
	}

	/**
	 * Ensure can create output with annotations.
	 */
	public void testOutputWithAnnotations() {
		// Load section type
		SectionType type = this.loadSectionType(true, (section, context) -> {
			section.addSectionOutput("NO_ANNOTATION", null, false);
			section.addSectionOutput("ANNOTATION", null, false).addAnnotation("TEST");
		});

		// Ensure correct output with annotation
		assertEquals("Incorrect number of outputs", 2, type.getSectionOutputTypes().length);
		SectionOutputType annotation = type.getSectionOutputTypes()[0];
		assertEquals("Incorrect input", "ANNOTATION", annotation.getSectionOutputName());
		assertEquals("Incorrect number of annotations", 1, annotation.getAnnotations().length);
		assertEquals("Incorrect annotations", "TEST", annotation.getAnnotations()[0]);
		assertEquals("Incorrect by type annotation", "TEST", annotation.getAnnotation(String.class));
		SectionOutputType noAnnotation = type.getSectionOutputTypes()[1];
		assertEquals("Should be no annotations", 0, noAnnotation.getAnnotations().length);
		assertNull("Should be no by type annotation", noAnnotation.getAnnotation(String.class));
	}

	/**
	 * Ensure issue if <code>null</code> {@link SectionObjectNodeImpl} name.
	 */
	public void testNullObjectName() {

		// Record null object name
		this.issues.recordIssue(Node.qualify(OfficeFloorCompiler.TYPE, null), SectionObjectNodeImpl.class,
				"Null name for Section Object");

		// Attempt to load section type
		this.loadSectionType(false, (section, context) -> {
			section.addSectionObject(null, Double.class.getName());
		});
	}

	/**
	 * Ensure issue if <code>null</code> {@link SectionObjectNodeImpl} type.
	 */
	public void testNullObjectType() {

		// Record null object type
		this.issues.recordIssue(Node.qualify(OfficeFloorCompiler.TYPE, "OBJECT"), SectionObjectNodeImpl.class,
				"Null type for Section Object (name=OBJECT)");

		// Attempt to load section type
		this.loadSectionType(false, (section, context) -> {
			section.addSectionObject("OBJECT", null);
		});
	}

	/**
	 * Ensure can create object with an object type.
	 */
	public void testObjectWithObjectType() {
		// Load section type
		SectionType type = this.loadSectionType(true, (section, context) -> {
			section.addSectionObject("OBJECT", Double.class.getName());
		});

		// Ensure correct object
		assertEquals("Incorrect number of objects", 1, type.getSectionObjectTypes().length);
		SectionObjectType objectType = type.getSectionObjectTypes()[0];
		assertEquals("Incorrect object name", "OBJECT", objectType.getSectionObjectName());
		assertEquals("Incorrect object type", Double.class.getName(), objectType.getObjectType());
		assertNull("Should not be qualified", objectType.getTypeQualifier());
	}

	/**
	 * Ensure can create object with an qualified object type.
	 */
	public void testObjectWithQualifiedObjectType() {
		// Load section type
		SectionType type = this.loadSectionType(true, (section, context) -> {
			SectionObject object = section.addSectionObject("OBJECT", Double.class.getName());
			object.setTypeQualifier("QUALIFIED");
		});

		// Ensure correct object
		assertEquals("Incorrect number of objects", 1, type.getSectionObjectTypes().length);
		SectionObjectType objectType = type.getSectionObjectTypes()[0];
		assertEquals("Incorrect object name", "OBJECT", objectType.getSectionObjectName());
		assertEquals("Incorrect object type", Double.class.getName(), objectType.getObjectType());
		assertEquals("Incorrect type qualifier", "QUALIFIED", objectType.getTypeQualifier());
	}

	/**
	 * Ensure can create object with annotations.
	 */
	public void testObjectWithAnnotations() {
		// Load section type
		SectionType type = this.loadSectionType(true, (section, context) -> {
			section.addSectionObject("NO_ANNOTATION", String.class.getName());
			section.addSectionObject("ANNOTATION", String.class.getName()).addAnnotation("TEST");
		});

		// Ensure correct object with annotation
		assertEquals("Incorrect number of objects", 2, type.getSectionObjectTypes().length);
		SectionObjectType annotation = type.getSectionObjectTypes()[0];
		assertEquals("Incorrect object", "ANNOTATION", annotation.getSectionObjectName());
		assertEquals("Incorrect number of annotations", 1, annotation.getAnnotations().length);
		assertEquals("Incorrect annotations", "TEST", annotation.getAnnotations()[0]);
		assertEquals("Incorrect by type annotation", "TEST", annotation.getAnnotation(String.class));
		SectionObjectType noAnnotation = type.getSectionObjectTypes()[1];
		assertEquals("Should be no annotations", 0, noAnnotation.getAnnotations().length);
		assertNull("Should be no by type annotation", noAnnotation.getAnnotation(String.class));
	}

	/**
	 * Ensure can load {@link SectionType} with no inputs, outputs or objects.
	 */
	public void testEmptySectionType() {
		// Load section type
		SectionType type = this.loadSectionType(true, (section, context) -> {
			// Load nothing
		});

		// Ensure empty section type
		assertEquals("Incorrect number of inputs", 0, type.getSectionInputTypes().length);
		assertEquals("Incorrect number of outputs", 0, type.getSectionOutputTypes().length);
		assertEquals("Incorrect number of objects", 0, type.getSectionObjectTypes().length);
	}

	/**
	 * Ensure can load {@link SectionType} with multiple inputs, outputs or objects.
	 */
	public void testSectionTypeWithMultipleInputsOutputObjects() {
		// Load section type
		SectionType type = this.loadSectionType(true, (section, context) -> {
			// Inputs
			section.addSectionInput("INPUT_A", Integer.class.getName());
			section.addSectionInput("INPUT_B", String.class.getName());
			section.addSectionInput("INPUT_C", null);
			// Outputs
			section.addSectionOutput("OUTPUT_A", Exception.class.getName(), true);
			section.addSectionOutput("OUTPUT_B", Double.class.getName(), false);
			section.addSectionOutput("OUTPUT_C", null, false);
			// Objects
			section.addSectionObject("OBJECT_A", Object.class.getName());
			section.addSectionObject("OBJECT_B", Connection.class.getName()).setTypeQualifier("TYPE");
		});

		// Ensure section type correct
		assertList(new String[] { "getSectionInputName", "getParameterType" }, type.getSectionInputTypes(),
				new SectionInputTypeImpl("INPUT_A", Integer.class.getName(), new Object[0]),
				new SectionInputTypeImpl("INPUT_B", String.class.getName(), new Object[0]),
				new SectionInputTypeImpl("INPUT_C", null, new Object[0]));
		assertList(new String[] { "getSectionOutputName", "getArgumentType", "isEscalationOnly" },
				type.getSectionOutputTypes(),
				new SectionOutputTypeImpl("OUTPUT_A", Exception.class.getName(), true, new Object[0]),
				new SectionOutputTypeImpl("OUTPUT_B", Double.class.getName(), false, new Object[0]),
				new SectionOutputTypeImpl("OUTPUT_C", null, false, new Object[0]));
		assertList(new String[] { "getSectionObjectName", "getObjectType", "getTypeQualifier" },
				type.getSectionObjectTypes(), new SectionObjectTypeImpl("OBJECT_A", Object.class.getName(), null, null),
				new SectionObjectTypeImpl("OBJECT_B", Connection.class.getName(), "TYPE", null));
	}

	/**
	 * Ensure can load {@link FunctionNamespaceType}.
	 */
	public void testLoadWorkType() {

		// Record capture issues for loading work type
		this.issues.recordCaptureIssues(false);

		// Load the section type which loads the work type
		this.loadSectionType(true, (section, context) -> {

			// Load the work type
			PropertyList properties = context.createPropertyList();
			properties.addProperty(ClassManagedFunctionSource.CLASS_NAME_PROPERTY_NAME)
					.setValue(MockLoadFunctionNamespace.class.getName());
			FunctionNamespaceType namespaceType = context.loadManagedFunctionType("NAMESPACE",
					ClassManagedFunctionSource.class.getName(), properties);

			// Ensure correct namespace type
			MockLoadFunctionNamespace.assertFunctionNamespaceType(namespaceType);
		});
	}

	/**
	 * Ensure issue if fails to load the {@link FunctionNamespaceType}.
	 */
	public void testFailLoadingFunctionNamespaceType() {

		// Ensure issue in not loading work type
		CompilerIssue[] issues = this.issues.recordCaptureIssues(true);
		this.issues.recordIssue(Node.qualify(OfficeFloorCompiler.TYPE, "NAMESPACE"), FunctionNamespaceNodeImpl.class,
				"Must specify property 'class.name'");
		this.issues.recordIssue(OfficeFloorCompiler.TYPE, SectionNodeImpl.class,
				"Failure loading FunctionNamespaceType from source " + ClassManagedFunctionSource.class.getName(),
				issues);

		// Fail to load the function namespace type
		this.loadSectionType(false, (section, context) -> {

			// Do not specify class causing failure to load type
			PropertyList properties = context.createPropertyList();
			context.loadManagedFunctionType("NAMESPACE", ClassManagedFunctionSource.class.getName(), properties);

			// Should not reach this point
			fail("Should not successfully load function namespace type");
		});
	}

	/**
	 * Ensure can load {@link ManagedObjectType}.
	 */
	public void testLoadManagedObjectType() {

		// Record capture issues for loading managed object type
		this.issues.recordCaptureIssues(false);

		// Load the section type which loads the managed object type
		this.loadSectionType(true, (section, context) -> {

			// Load the managed object type
			PropertyList properties = context.createPropertyList();
			properties.addProperty(ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME)
					.setValue(MockLoadManagedObject.class.getName());
			ManagedObjectType<?> managedObjectType = context.loadManagedObjectType("NAMESPACE",
					ClassManagedObjectSource.class.getName(), properties);

			// Ensure correct managed object type
			MockLoadManagedObject.assertManagedObjectType(managedObjectType);
		});
	}

	/**
	 * Ensure issue if fails to load the {@link ManagedObjectType}.
	 */
	public void testFailLoadingManagedObjectType() {

		// Ensure issue in not loading managed object type
		CompilerIssue[] issues = this.issues.recordCaptureIssues(true);
		this.issues.recordIssue(Node.qualify(OfficeFloorCompiler.TYPE, "NAMESPACE"), ManagedObjectSourceNodeImpl.class,
				"Must specify property 'class.name'");
		this.issues.recordIssue(OfficeFloorCompiler.TYPE, SectionNodeImpl.class,
				"Failure loading ManagedObjectType from source " + ClassManagedObjectSource.class.getName(), issues);

		// Fail to load the managed object type
		this.loadSectionType(false, (section, context) -> {

			// Do not specify class causing failure to load type
			PropertyList properties = context.createPropertyList();
			context.loadManagedObjectType("NAMESPACE", ClassManagedObjectSource.class.getName(), properties);

			// Should not reach this point
			fail("Should not successfully load managed object type");
		});
	}

	/**
	 * Ensure can load {@link SectionType}.
	 */
	public void testLoadSubSectionType() {

		// Record capture issues for loading section and sub section type
		this.issues.recordCaptureIssues(false);
		this.issues.recordCaptureIssues(false);
		this.issues.recordCaptureIssues(false);

		// Load the section type which loads the sub section type
		this.loadSectionType(true, (section, context) -> {

			// Load the sub section type
			PropertyList properties = context.createPropertyList();
			SectionType sectionType = context.loadSectionType("SECTION", ClassSectionSource.class.getName(),
					MockLoadSection.class.getName(), properties);

			// Ensure correct section type
			MockLoadSection.assertSectionType(sectionType);
		});
	}

	/**
	 * Ensure issue if fails to load the {@link SectionType}.
	 */
	public void testIssueLoadingSectionType() {

		// Ensure issue in not loading managed object type
		final String SUB_SECTION_NAME = "SUB_SECTION";
		CompilerIssue[] issues = this.issues.recordCaptureIssues(true);
		this.issues.recordIssue(OfficeFloorCompiler.TYPE, SectionNodeImpl.class, "Must specify property 'missing'");
		this.issues.recordIssue(OfficeFloorCompiler.TYPE, SectionNodeImpl.class,
				"Failure loading SectionType from source " + FailSectionSource.class.getName(), issues);

		// Fail to load the managed object type
		this.loadSectionType(false, (section, context) -> {

			// Do not specify class causing failure to load type
			PropertyList properties = context.createPropertyList();
			context.loadSectionType(SUB_SECTION_NAME, FailSectionSource.class.getName(), "FailLocation", properties);
		});
	}

	public static class FailSectionSource extends AbstractSectionSource {

		@Override
		protected void loadSpecification(SpecificationContext context) {
			// No specification
		}

		@Override
		public void sourceSection(SectionDesigner designer, SectionSourceContext context) throws Exception {
			// Obtain property causing failure
			context.getProperty("missing");

			// Ensure failed
			TestCase.fail("Should not successfully obtain a missing property");
		}
	}

	/**
	 * Ensure can handle {@link CompileError}.
	 */
	public void testHandleCompileError() {

		// Record issue
		this.issues.recordIssue(OfficeFloorCompiler.TYPE, SectionNodeImpl.class, "test");

		// Ensure handle compile error
		this.loadSectionType(false, (section, context) -> {
			throw section.addIssue("test");
		});
	}

	/**
	 * Loads the {@link SectionType} within the input {@link Loader}.
	 * 
	 * @param isExpectedToLoad       Flag indicating if expecting to load the
	 *                               {@link SectionType}.
	 * @param loader                 {@link Loader}.
	 * @param propertyNameValuePairs {@link Property} name value pairs.
	 * @return Loaded {@link SectionType}.
	 */
	private SectionType loadSectionType(boolean isExpectedToLoad, Loader loader, String... propertyNameValuePairs) {

		// Replay mock objects
		this.replayMockObjects();

		// Create the compiler
		OfficeFloorCompiler compiler = OfficeFloorCompiler.newOfficeFloorCompiler(null);

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
		SectionType sectionType = sectionLoader.loadSectionType(MockSectionSource.class, SECTION_LOCATION,
				propertyList);

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
		 * @param section {@link SectionDesigner}.
		 * @param context {@link SectionSourceContext}.
		 * @throws Exception If fails to source {@link SectionType}.
		 */
		void sourceSection(SectionDesigner section, SectionSourceContext context) throws Exception;
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
		public void sourceSection(SectionDesigner sectionBuilder, SectionSourceContext context) throws Exception {
			loader.sourceSection(sectionBuilder, context);
		}
	}

}
