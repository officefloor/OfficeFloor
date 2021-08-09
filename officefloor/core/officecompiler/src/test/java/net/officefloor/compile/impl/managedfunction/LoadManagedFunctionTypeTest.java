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

package net.officefloor.compile.impl.managedfunction;

import java.sql.Connection;
import java.util.Properties;
import java.util.logging.Logger;

import net.officefloor.compile.FailServiceFactory;
import net.officefloor.compile.MissingServiceFactory;
import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.impl.properties.PropertyListImpl;
import net.officefloor.compile.issues.CompilerIssues;
import net.officefloor.compile.managedfunction.FunctionNamespaceType;
import net.officefloor.compile.managedfunction.ManagedFunctionEscalationType;
import net.officefloor.compile.managedfunction.ManagedFunctionFlowType;
import net.officefloor.compile.managedfunction.ManagedFunctionLoader;
import net.officefloor.compile.managedfunction.ManagedFunctionObjectType;
import net.officefloor.compile.managedfunction.ManagedFunctionType;
import net.officefloor.compile.properties.Property;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.managedfunction.source.FunctionNamespaceBuilder;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionFlowTypeBuilder;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionObjectTypeBuilder;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionSource;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionSourceContext;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionSourceSpecification;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionTypeBuilder;
import net.officefloor.compile.test.issues.MockCompilerIssues;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.function.ManagedFunctionFactory;
import net.officefloor.frame.api.source.TestSource;
import net.officefloor.frame.test.Closure;
import net.officefloor.frame.test.OfficeFrameTestCase;

/**
 * Tests loading the {@link FunctionNamespaceType} from the
 * {@link ManagedFunctionSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class LoadManagedFunctionTypeTest extends OfficeFrameTestCase {

	/**
	 * {@link CompilerIssues}.
	 */
	private final MockCompilerIssues issues = new MockCompilerIssues(this);

	/**
	 * {@link ManagedFunctionFactory}.
	 */
	@SuppressWarnings("unchecked")
	private final ManagedFunctionFactory<Indexed, Indexed> functionFactory = this
			.createMock(ManagedFunctionFactory.class);

	@Override
	protected void setUp() throws Exception {
		MockManagedFunctionSource.reset();
	}

	/**
	 * Ensure issue if fail to instantiate the {@link ManagedFunctionSource}.
	 */
	public void testFailInstantiate() {

		final RuntimeException failure = new RuntimeException("instantiate failure");

		// Record failure to instantiate
		this.issues.recordIssue(
				"Failed to instantiate " + MockManagedFunctionSource.class.getName() + " by default constructor",
				failure);

		// Attempt to obtain specification
		MockManagedFunctionSource.instantiateFailure = failure;
		this.loadManagedFunctionType(false, null);
	}

	/**
	 * Ensure issue if missing {@link Property}.
	 */
	public void testMissingProperty() {

		// Record missing property
		this.issues.recordIssue("Must specify property 'missing'");

		// Attempt to load namespace type
		this.loadManagedFunctionType(false, (namespace, context) -> {
			context.getProperty("missing");
		});
	}

	/**
	 * Ensure able to get properties.
	 */
	public void testGetProperties() {

		// Attempt to load namespace type
		this.loadManagedFunctionType(true, (namespace, context) -> {
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

			// Providing minimal namespace type
			namespace.addManagedFunctionType("IGNORE", Indexed.class, Indexed.class)
					.setFunctionFactory(this.functionFactory);
		}, "ONE", "1", "TWO", "2");
	}

	/**
	 * Ensure issue if missing {@link Class}.
	 */
	public void testMissingClass() {

		// Record missing class
		this.issues.recordIssue("Can not load class 'missing'");

		// Attempt to load namespace type
		this.loadManagedFunctionType(false, (namespace, context) -> {
			context.loadClass("missing");
		});
	}

	/**
	 * Ensure issue if missing resource.
	 */
	public void testMissingResource() {

		// Record missing resource
		this.issues.recordIssue("Can not obtain resource at location 'missing'");

		// Attempt to load namespace type
		this.loadManagedFunctionType(false, (namespace, context) -> {
			context.getResource("missing");
		});
	}

	/**
	 * Ensure issue if missing service.
	 */
	public void testMissingService() {

		// Record missing service
		this.issues.recordIssue(MissingServiceFactory.getIssueDescription());

		// Attempt to load
		this.loadManagedFunctionType(false,
				(namespace, context) -> context.loadService(MissingServiceFactory.class, null));
	}

	/**
	 * Ensure issue if fail to load service.
	 */
	public void testFailLoadService() {

		// Record load issue for service
		this.issues.recordIssue(FailServiceFactory.getIssueDescription(), FailServiceFactory.getCause());

		// Attempt to load
		this.loadManagedFunctionType(false,
				(namespace, context) -> context.loadService(FailServiceFactory.class, null));
	}

	/**
	 * Ensure correctly named {@link Logger}.
	 */
	public void testLogger() {
		Closure<String> loggerName = new Closure<>();
		this.loadManagedFunctionType(true, (namespace, context) -> {
			loggerName.value = context.getLogger().getName();

			// Providing minimal namespace type
			namespace.addManagedFunctionType("IGNORE", Indexed.class, Indexed.class)
					.setFunctionFactory(this.functionFactory);
		});
		assertEquals("Incorrect logger name", OfficeFloorCompiler.TYPE, loggerName.value);
	}

	/**
	 * Ensure able to get the {@link ClassLoader}.
	 */
	public void testGetClassLoader() {

		// Attempt to load namespace type
		this.loadManagedFunctionType(true, (namespace, context) -> {
			assertEquals("Incorrect class loader", LoadManagedFunctionTypeTest.class.getClassLoader(),
					context.getClassLoader());

			// Providing minimal namespace type
			namespace.addManagedFunctionType("IGNORE", Indexed.class, Indexed.class)
					.setFunctionFactory(this.functionFactory);
		});
	}

	/**
	 * Ensure issue if fails to source the {@link FunctionNamespaceType}.
	 */
	public void testFailSourceManagedFunctionType() {

		final NullPointerException failure = new NullPointerException("Fail source namespace type");

		// Record failure to source the namespace type
		this.issues.recordIssue("Failed to source FunctionNamespaceType definition from ManagedFunctionSource "
				+ MockManagedFunctionSource.class.getName(), failure);

		// Attempt to load namespace type
		this.loadManagedFunctionType(false, (namespace, context) -> {
			throw failure;
		});
	}

	/**
	 * Ensure issue if no {@link ManagedFunctionType} instances as no point of
	 * {@link FunctionNamespaceType} without at least one {@link ManagedFunction}.
	 */
	public void testNoFunctions() {

		// Record no functions
		this.issues.recordIssue("No ManagedFunctionType definitions provided by ManagedFunctionSource "
				+ MockManagedFunctionSource.class.getName());

		// Attempt to load namespace type
		this.loadManagedFunctionType(false, (namespace, context) -> {
			// No functions
		});
	}

	/**
	 * Ensure issue if no {@link ManagedFunctionType} name.
	 */
	public void testNoFunctionName() {

		// Record no functions
		this.issues
				.recordIssue("No function name provided for ManagedFunctionType definition 0 by ManagedFunctionSource "
						+ MockManagedFunctionSource.class.getName());

		// Attempt to load namespace type
		this.loadManagedFunctionType(false, (namespace, context) -> {
			namespace.addManagedFunctionType(null, null, null);
		});
	}

	/**
	 * Ensure issue if duplicate {@link ManagedFunctionType} name.
	 */
	public void testDuplicateFunctionNames() {

		// Record duplicate function names
		this.issues.recordIssue(
				"Two or more ManagedFunctionType definitions with the same name (SAME) provided by ManagedFunctionSource "
						+ MockManagedFunctionSource.class.getName());

		// Attempt to load namespace type
		this.loadManagedFunctionType(false, (namespace, context) -> {
			namespace.addManagedFunctionType("SAME", null, null);
			namespace.addManagedFunctionType("SAME", null, null);
		});
	}

	/**
	 * Ensure issue if no {@link ManagedFunctionFactory}.
	 */
	public void testNoFunctionFactory() {

		// Record no functions
		this.issues.recordIssue(
				"No ManagedFunctionFactory provided for ManagedFunctionType definition 0 (FUNCTION) by ManagedFunctionSource "
						+ MockManagedFunctionSource.class.getName());

		// Attempt to load namespace type
		this.loadManagedFunctionType(false, (namespace, context) -> {
			namespace.addManagedFunctionType("FUNCTION", null, null);
		});
	}

	/**
	 * Ensure annotation not required.
	 */
	@SuppressWarnings("unchecked")
	public void testNoAnnotation() {

		final ManagedFunctionFactory<None, None> functionFactory = this.createMock(ManagedFunctionFactory.class);

		// Attempt to load annotation
		FunctionNamespaceType namespaceType = this.loadManagedFunctionType(true, (namespace, context) -> {
			namespace.addManagedFunctionType("FUNCTION", None.class, None.class).setFunctionFactory(functionFactory);
			// Do not specify annotation
		});

		// Ensure annotation available
		Object[] annotations = namespaceType.getManagedFunctionTypes()[0].getAnnotations();
		assertEquals("Should not have annotation", 0, annotations.length);
	}

	/**
	 * Ensure able to load the annotation.
	 */
	@SuppressWarnings("unchecked")
	public void testAnnotation() {

		final ManagedFunctionFactory<None, None> functionFactory = this.createMock(ManagedFunctionFactory.class);
		final Object ANNOTATION = "Annotation";

		// Attempt to load annotation
		FunctionNamespaceType namespaceType = this.loadManagedFunctionType(true, (namespace, context) -> {
			ManagedFunctionTypeBuilder<None, None> function = namespace
					.addManagedFunctionType("FUNCTION", None.class, None.class).setFunctionFactory(functionFactory);
			function.addAnnotation(ANNOTATION);
		});
		ManagedFunctionType<?, ?> functionType = namespaceType.getManagedFunctionTypes()[0];

		// Ensure annotation available
		assertEquals("Incorrect annotation", ANNOTATION, functionType.getAnnotations()[0]);
		assertEquals("Incorrect by type annotation", ANNOTATION, functionType.getAnnotation(String.class));
	}

	/**
	 * Ensure able to load annotation for parameters.
	 */
	@SuppressWarnings("unchecked")
	public void testObjectAnnotation() {

		final ManagedFunctionFactory<Indexed, None> functionFactory = this.createMock(ManagedFunctionFactory.class);
		final Object ANNOTATION = "Annotation";

		// Attempt to load annotation
		FunctionNamespaceType namespaceType = this.loadManagedFunctionType(true, (namespace, context) -> {
			ManagedFunctionTypeBuilder<Indexed, None> function = namespace
					.addManagedFunctionType("FUNCTION", Indexed.class, None.class).setFunctionFactory(functionFactory);
			function.addObject(String.class).addAnnotation(ANNOTATION);
		});
		ManagedFunctionObjectType<?> objectType = namespaceType.getManagedFunctionTypes()[0].getObjectTypes()[0];

		// Ensure annotation available
		assertEquals("Incorrect annotation", ANNOTATION, objectType.getAnnotations()[0]);
		assertEquals("Incorrect by type annotation", ANNOTATION, objectType.getAnnotation(String.class));
	}

	/**
	 * Ensure issue if using {@link Enum} but no {@link ManagedFunctionObjectType}
	 * provided for a key.
	 */
	@SuppressWarnings("unchecked")
	public void testMissingObjectForKey() {

		final ManagedFunctionFactory<ObjectKey, Indexed> functionFactory = this
				.createMock(ManagedFunctionFactory.class);

		// Record no functions
		this.issues.recordIssue("No ManagedFunctionObjectType provided for key " + ObjectKey.ONE
				+ " on ManagedFunctionType definition 0 (FUNCTION) by ManagedFunctionSource "
				+ MockManagedFunctionSource.class.getName());

		// Attempt to load namespace type
		this.loadManagedFunctionType(false, (namespace, context) -> {
			namespace.addManagedFunctionType("FUNCTION", ObjectKey.class, Indexed.class)
					.setFunctionFactory(functionFactory);
		});
	}

	/**
	 * Ensure issue if no key for {@link ManagedFunctionObjectType} by provided key
	 * class.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void testNoKeyForObject() {

		final ManagedFunctionFactory<ObjectKey, Indexed> functionFactory = this
				.createMock(ManagedFunctionFactory.class);

		// Record no functions
		this.issues.recordIssue(
				"No key provided for an object on ManagedFunctionType definition 0 (FUNCTION) by ManagedFunctionSource "
						+ MockManagedFunctionSource.class.getName());

		// Attempt to load namespace type
		this.loadManagedFunctionType(false, (namespace, context) -> {
			ManagedFunctionTypeBuilder function = namespace
					.addManagedFunctionType("FUNCTION", ObjectKey.class, Indexed.class)
					.setFunctionFactory(functionFactory);

			// Add function without key
			ManagedFunctionObjectTypeBuilder object = function.addObject(Connection.class);

			// Ensure correct index
			assertEquals("Incorrect dependency index", 0, object.getIndex());
		});
	}

	/**
	 * Ensure issue if incorrect key type for {@link ManagedFunctionObjectType} by
	 * provided key class.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void testIncorrectKeyTypeForObject() {

		final ManagedFunctionFactory<ObjectKey, Indexed> functionFactory = this
				.createMock(ManagedFunctionFactory.class);

		// Record no functions
		this.issues.recordIssue("Incorrect key type (" + WrongKey.class.getName()
				+ ") provided for an object on ManagedFunctionType definition 0 (FUNCTION) by ManagedFunctionSource "
				+ MockManagedFunctionSource.class.getName());

		// Attempt to load namespace type
		this.loadManagedFunctionType(false, (namespace, context) -> {
			ManagedFunctionTypeBuilder function = namespace
					.addManagedFunctionType("FUNCTION", ObjectKey.class, Indexed.class)
					.setFunctionFactory(functionFactory);

			// Add function with wrong key
			function.addObject(Connection.class).setKey(WrongKey.WRONG_KEY);
		});
	}

	/**
	 * Ensure issue if more {@link ManagedFunctionObjectType} instances than keys.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void testMoreObjectsThanKeys() {

		final ManagedFunctionFactory<ObjectKey, Indexed> functionFactory = this
				.createMock(ManagedFunctionFactory.class);

		// Record no functions
		this.issues.recordIssue(
				"More objects than keys on ManagedFunctionType definition 0 (FUNCTION) by ManagedFunctionSource "
						+ MockManagedFunctionSource.class.getName());

		// Attempt to load namespace type
		this.loadManagedFunctionType(false, (namespace, context) -> {
			ManagedFunctionTypeBuilder function = namespace
					.addManagedFunctionType("FUNCTION", ObjectKey.class, Indexed.class)
					.setFunctionFactory(functionFactory);

			// Add extra objects than keys
			function.addObject(Connection.class).setKey(ObjectKey.ONE);
			function.addObject(String.class).setKey(ObjectKey.TWO);
			function.addObject(String.class).setKey(ObjectKey.TWO);
		});
	}

	/**
	 * Ensure issue if key provided for {@link ManagedFunctionObjectType} but no key
	 * class.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void testObjectHasKeyButNoKeyClass() {

		final ManagedFunctionFactory functionFactory = this.createMock(ManagedFunctionFactory.class);

		// Record indexes out of order
		this.issues.recordIssue(
				"Objects are not keyed but object has key on ManagedFunctionType definition 0 (FUNCTION) by ManagedFunctionSource "
						+ MockManagedFunctionSource.class.getName());

		// Attempt to load namespace type
		this.loadManagedFunctionType(false, (namespace, context) -> {
			ManagedFunctionTypeBuilder function = namespace.addManagedFunctionType("FUNCTION", null, null)
					.setFunctionFactory(functionFactory);

			// Add extra objects than keys
			function.addObject(Connection.class).setKey(ObjectKey.ONE);
		});
	}

	/**
	 * Ensure issue if no object type.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void testNoObjectType() {

		// Record no object type
		this.issues.recordIssue(
				"No object type provided for object 0 on ManagedFunctionType definition 0 (FUNCTION) by ManagedFunctionSource "
						+ MockManagedFunctionSource.class.getName());

		// Attempt to load namespace type
		this.loadManagedFunctionType(false, (namespace, context) -> {
			ManagedFunctionTypeBuilder function = namespace
					.addManagedFunctionType("FUNCTION", Indexed.class, Indexed.class)
					.setFunctionFactory(this.functionFactory);

			// Add function with no object type
			function.addObject(null);
		});
	}

	/**
	 * Ensure issue if duplicate object name.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void testDuplicateObjectNames() {

		// Record no object type
		this.issues.recordIssue(
				"Two or more ManagedFunctionObjectType definitions with the same name (SAME) for ManagedFunctionType definition 0 (FUNCTION) by ManagedFunctionSource "
						+ MockManagedFunctionSource.class.getName());

		// Attempt to load namespace type
		this.loadManagedFunctionType(false, (namespace, context) -> {
			ManagedFunctionTypeBuilder function = namespace
					.addManagedFunctionType("FUNCTION", Indexed.class, Indexed.class)
					.setFunctionFactory(this.functionFactory);

			// Add objects with same name
			function.addObject(Connection.class).setLabel("SAME");
			function.addObject(Connection.class).setLabel("SAME");
		});
	}

	/**
	 * Ensure issue if {@link ManagedFunctionObjectType} are not ordered to
	 * {@link Enum} key order.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void testKeyOrderingForObjectType() {

		final ManagedFunctionFactory<ObjectKey, Indexed> functionFactory = this
				.createMock(ManagedFunctionFactory.class);
		final Class<?> oneType = String.class;
		final Class<?> twoType = Connection.class;

		// Attempt to load namespace type
		FunctionNamespaceType namespaceType = this.loadManagedFunctionType(true, (namespace, context) -> {
			ManagedFunctionTypeBuilder function = namespace
					.addManagedFunctionType("FUNCTION", ObjectKey.class, Indexed.class)
					.setFunctionFactory(functionFactory);

			// Add second keyed object first
			ManagedFunctionObjectTypeBuilder object = function.addObject(twoType);
			assertEquals("Index should be initially added order", 0, object.getIndex());
			object.setKey(ObjectKey.TWO);
			assertEquals("Should update to key index", ObjectKey.TWO.ordinal(), object.getIndex());

			// Add first keyed object afterwards
			function.addObject(oneType).setKey(ObjectKey.ONE);
		});

		// Validate the key class
		ManagedFunctionType<?, ?> function = namespaceType.getManagedFunctionTypes()[0];
		assertEquals("Incorrect object key class", ObjectKey.class, function.getObjectKeyClass());

		// Validate the object type order
		ManagedFunctionObjectType<?>[] objects = function.getObjectTypes();
		assertEquals("Incorrect number of objects", 2, objects.length);
		ManagedFunctionObjectType<?> one = objects[0];
		assertEquals("Incorrect key one", ObjectKey.ONE, one.getKey());
		assertEquals("Incorrect index one", ObjectKey.ONE.ordinal(), one.getIndex());
		assertEquals("Incorrect name one", ObjectKey.ONE.toString(), one.getObjectName());
		assertEquals("Incorrect type one", oneType, one.getObjectType());
		ManagedFunctionObjectType<?> two = objects[1];
		assertEquals("Incorrect key two", ObjectKey.TWO, two.getKey());
		assertEquals("Incorrect index two", ObjectKey.TWO.ordinal(), two.getIndex());
		assertEquals("Incorrect name two", ObjectKey.TWO.toString(), two.getObjectName());
		assertEquals("Incorrect type two", twoType, two.getObjectType());
	}

	/**
	 * Object key {@link Enum}.
	 */
	private enum ObjectKey {
		ONE, TWO
	}

	/**
	 * Ensure able to provide type qualifier for object type.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void testObjectTypeQualification() {

		// Attempt to load namespace type
		FunctionNamespaceType namespaceType = this.loadManagedFunctionType(true, (namespace, context) -> {
			ManagedFunctionTypeBuilder function = namespace
					.addManagedFunctionType("FUNCTION", Indexed.class, Indexed.class)
					.setFunctionFactory(this.functionFactory);

			// Add objects with type qualified for on
			function.addObject(Connection.class).setTypeQualifier("QUALIFIED");
			function.addObject(Connection.class); // unqualified
		});

		// Validate the type qualification
		ManagedFunctionType<?, ?> function = namespaceType.getManagedFunctionTypes()[0];
		ManagedFunctionObjectType<?>[] objects = function.getObjectTypes();
		assertEquals("Incorrect number of objects", 2, objects.length);

		// Validate qualified object
		ManagedFunctionObjectType<?> one = objects[0];
		assertEquals("Incorrect type", Connection.class, one.getObjectType());
		assertEquals("Incorrect qualifier", "QUALIFIED", one.getTypeQualifier());

		// Validate unqualified object
		ManagedFunctionObjectType<?> two = objects[1];
		assertEquals("Incorrect type", Connection.class, two.getObjectType());
		assertNull("Should be unqualified", two.getTypeQualifier());
	}

	/**
	 * Ensure issue if using {@link Enum} but no {@link ManagedFunctionFlowType}
	 * provided for a key.
	 */
	@SuppressWarnings("unchecked")
	public void testMissingFlowForKey() {

		final ManagedFunctionFactory<Indexed, FlowKey> functionFactory = this.createMock(ManagedFunctionFactory.class);

		// Record no functions
		this.issues.recordIssue("No ManagedFunctionFlowType provided for key " + FlowKey.ONE
				+ " on ManagedFunctionType definition 0 (FUNCTION) by ManagedFunctionSource "
				+ MockManagedFunctionSource.class.getName());

		// Attempt to load namespace type
		this.loadManagedFunctionType(false, (namespace, context) -> {
			namespace.addManagedFunctionType("FUNCTION", Indexed.class, FlowKey.class)
					.setFunctionFactory(functionFactory);
		});
	}

	/**
	 * Ensure issue if no key for {@link ManagedFunctionFlowType} but provided key
	 * class.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void testNoKeyForFlow() {

		final ManagedFunctionFactory<Indexed, ObjectKey> functionFactory = this
				.createMock(ManagedFunctionFactory.class);

		// Record no functions
		this.issues.recordIssue(
				"No key provided for a flow on ManagedFunctionType definition 0 (FUNCTION) by ManagedFunctionSource "
						+ MockManagedFunctionSource.class.getName());

		// Attempt to load namespace type
		this.loadManagedFunctionType(false, (namespace, context) -> {
			ManagedFunctionTypeBuilder function = namespace
					.addManagedFunctionType("FUNCTION", Indexed.class, ObjectKey.class)
					.setFunctionFactory(functionFactory);

			// Add function without key
			ManagedFunctionFlowTypeBuilder flow = function.addFlow();

			// Ensure correct index
			assertEquals("Incorrect flow index", 0, flow.getIndex());
		});
	}

	/**
	 * Ensure issue if incorrect key type for {@link ManagedFunctionFlowType} but
	 * provided key class.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void testIncorrectKeyTypeForFlow() {

		final ManagedFunctionFactory<Indexed, FlowKey> functionFactory = this.createMock(ManagedFunctionFactory.class);

		// Record no functions
		this.issues.recordIssue("Incorrect key type (" + WrongKey.class.getName()
				+ ") provided for a flow on ManagedFunctionType definition 0 (FUNCTION) by ManagedFunctionSource "
				+ MockManagedFunctionSource.class.getName());

		// Attempt to load namespace type
		this.loadManagedFunctionType(false, (namespace, context) -> {
			ManagedFunctionTypeBuilder function = namespace
					.addManagedFunctionType("FUNCTION", Indexed.class, FlowKey.class)
					.setFunctionFactory(functionFactory);

			// Add function with wrong key
			function.addFlow().setKey(WrongKey.WRONG_KEY);
		});
	}

	/**
	 * Ensure issue if more {@link ManagedFunctionFlowType} instances than keys.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void testMoreFlowsThanKeys() {

		final ManagedFunctionFactory<Indexed, FlowKey> functionFactory = this.createMock(ManagedFunctionFactory.class);

		// Record no functions
		this.issues.recordIssue(
				"More flows than keys on ManagedFunctionType definition 0 (FUNCTION) by ManagedFunctionSource "
						+ MockManagedFunctionSource.class.getName());

		// Attempt to load namespace type
		this.loadManagedFunctionType(false, (namespace, context) -> {
			ManagedFunctionTypeBuilder function = namespace
					.addManagedFunctionType("FUNCTION", Indexed.class, FlowKey.class)
					.setFunctionFactory(functionFactory);

			// Add extra objects than keys
			function.addFlow().setKey(FlowKey.ONE);
			function.addFlow().setKey(FlowKey.TWO);
			function.addFlow().setKey(FlowKey.TWO);
		});
	}

	/**
	 * Ensure issue if key provided for {@link ManagedFunctionFlowType} but no key
	 * class.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void testFlowHasKeyButNoKeyClass() {

		final ManagedFunctionFactory functionFactory = this.createMock(ManagedFunctionFactory.class);

		// Record indexes out of order
		this.issues.recordIssue(
				"Flows are not keyed but flow has key on ManagedFunctionType definition 0 (FUNCTION) by ManagedFunctionSource "
						+ MockManagedFunctionSource.class.getName());

		// Attempt to load namespace type
		this.loadManagedFunctionType(false, (namespace, context) -> {
			ManagedFunctionTypeBuilder function = namespace.addManagedFunctionType("FUNCTION", Indexed.class, null)
					.setFunctionFactory(functionFactory);

			// Add flow with key
			function.addFlow().setKey(FlowKey.ONE);
		});
	}

	/**
	 * Ensure issue if duplicate {@link ManagedFunctionFlowType} names.
	 */
	@SuppressWarnings("rawtypes")
	public void testDuplicateFlowNames() {

		// Record no object type
		this.issues.recordIssue(
				"Two or more ManagedFunctionFlowType definitions with the same name (SAME) for ManagedFunctionType definition 0 (FUNCTION) by ManagedFunctionSource "
						+ MockManagedFunctionSource.class.getName());

		// Attempt to load namespace type
		this.loadManagedFunctionType(false, (namespace, context) -> {
			ManagedFunctionTypeBuilder function = namespace
					.addManagedFunctionType("FUNCTION", Indexed.class, Indexed.class)
					.setFunctionFactory(this.functionFactory);

			// Add flows with same name
			function.addFlow().setLabel("SAME");
			function.addFlow().setLabel("SAME");
		});
	}

	/**
	 * Ensure issue if {@link ManagedFunctionFlowType} are not ordered to
	 * {@link Enum} key order.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void testKeyOrderingForFlowType() {

		final ManagedFunctionFactory<Indexed, FlowKey> functionFactory = this.createMock(ManagedFunctionFactory.class);
		final Class<?> oneType = String.class;

		// Attempt to load namespace type
		FunctionNamespaceType namespaceType = this.loadManagedFunctionType(true, (namespace, context) -> {
			ManagedFunctionTypeBuilder function = namespace
					.addManagedFunctionType("FUNCTION", Indexed.class, FlowKey.class)
					.setFunctionFactory(functionFactory);

			// Add second keyed flow
			ManagedFunctionFlowTypeBuilder<FlowKey> flowTwoType = function.addFlow();
			assertEquals("Should be initially indexed by add order", 0, flowTwoType.getIndex());
			flowTwoType.setKey(FlowKey.TWO);
			assertEquals("Should be indexed by key", FlowKey.TWO.ordinal(), flowTwoType.getIndex());

			// Add first keyed flow after
			ManagedFunctionFlowTypeBuilder<FlowKey> flowOneType = function.addFlow();
			flowOneType.setKey(FlowKey.ONE);
			flowOneType.setArgumentType(oneType);
		});

		// Validate the key class
		ManagedFunctionType<?, ?> function = namespaceType.getManagedFunctionTypes()[0];
		assertEquals("Incorrect flow key class", FlowKey.class, function.getFlowKeyClass());

		// Validate the flow type order
		ManagedFunctionFlowType<?>[] flows = function.getFlowTypes();
		assertEquals("Incorrect number of flows", 2, flows.length);
		ManagedFunctionFlowType<?> one = flows[0];
		assertEquals("Incorrect key one", FlowKey.ONE, one.getKey());
		assertEquals("Incorrect index one", FlowKey.ONE.ordinal(), one.getIndex());
		assertEquals("Incorrect name one", FlowKey.ONE.toString(), one.getFlowName());
		assertEquals("Incorrect argument type one", oneType, one.getArgumentType());
		ManagedFunctionFlowType<?> two = flows[1];
		assertEquals("Incorrect key two", FlowKey.TWO, two.getKey());
		assertEquals("Incorrect index two", FlowKey.TWO.ordinal(), two.getIndex());
		assertEquals("Incorrect name two", FlowKey.TWO.toString(), two.getFlowName());
		assertNull("Should be no argument for two", two.getArgumentType());
	}

	/**
	 * Flow key {@link Enum}.
	 */
	private enum FlowKey {
		ONE, TWO
	}

	/**
	 * Ensure able to load annotation for parameters.
	 */
	@SuppressWarnings("unchecked")
	public void testFlowAnnotation() {

		final ManagedFunctionFactory<None, Indexed> functionFactory = this.createMock(ManagedFunctionFactory.class);
		final Object ANNOTATION = "Annotation";

		// Attempt to load annotation
		FunctionNamespaceType namespaceType = this.loadManagedFunctionType(true, (namespace, context) -> {
			ManagedFunctionTypeBuilder<None, Indexed> function = namespace
					.addManagedFunctionType("FUNCTION", None.class, Indexed.class).setFunctionFactory(functionFactory);
			function.addFlow().addAnnotation(ANNOTATION);
		});
		ManagedFunctionFlowType<?> flowType = namespaceType.getManagedFunctionTypes()[0].getFlowTypes()[0];

		// Ensure annotation available
		assertEquals("Incorrect annotation", ANNOTATION, flowType.getAnnotations()[0]);
		assertEquals("Incorrect by type annotation", ANNOTATION, flowType.getAnnotation(String.class));
	}

	/**
	 * Ensure issue if no escalation type.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void testNoEscalationType() {

		// Record no escalation type
		this.issues.recordIssue(
				"No escalation type on ManagedFunctionType definition 0 (FUNCTION) by ManagedFunctionSource "
						+ MockManagedFunctionSource.class.getName());

		// Attempt to load namespace type
		this.loadManagedFunctionType(false, (namespace, context) -> {
			ManagedFunctionTypeBuilder function = namespace
					.addManagedFunctionType("FUNCTION", Indexed.class, Indexed.class)
					.setFunctionFactory(this.functionFactory);

			// Add no escalation type
			function.addEscalation(null);
		});
	}

	/**
	 * Ensure issue if duplicate escalation names.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void testDuplicateEscalationNames() {

		// Record no object type
		this.issues.recordIssue(
				"Two or more ManagedFunctionEscalationType definitions with the same name (SAME) for ManagedFunctionType definition 0 (FUNCTION) by ManagedFunctionSource "
						+ MockManagedFunctionSource.class.getName());

		// Attempt to load namespace type
		this.loadManagedFunctionType(false, (namespace, context) -> {
			ManagedFunctionTypeBuilder function = namespace
					.addManagedFunctionType("FUNCTION", Indexed.class, Indexed.class)
					.setFunctionFactory(this.functionFactory);

			// Add duplicate escalation names
			function.addEscalation(Exception.class).setLabel("SAME");
			function.addEscalation(Exception.class).setLabel("SAME");
		});
	}

	/**
	 * Ensure can include escalation.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void testEscalation() {

		// Attempt to load namespace type
		FunctionNamespaceType namespaceType = this.loadManagedFunctionType(true, (namespace, context) -> {
			ManagedFunctionTypeBuilder function = namespace
					.addManagedFunctionType("FUNCTION", Indexed.class, Indexed.class)
					.setFunctionFactory(this.functionFactory);

			// Add escalations
			function.addEscalation(Error.class);
			function.addEscalation(RuntimeException.class);
		});

		// Validate the escalation
		ManagedFunctionEscalationType[] escalations = namespaceType.getManagedFunctionTypes()[0].getEscalationTypes();
		assertEquals("Incorrect number of escalation", 2, escalations.length);
		assertEquals("Incorrect first escalation type", Error.class, escalations[0].getEscalationType());
		assertEquals("Incorrect first escalation name", Error.class.getName(), escalations[0].getEscalationName());
		assertEquals("Incorrect second escalation type", RuntimeException.class, escalations[1].getEscalationType());
		assertEquals("Incorrect second escalation name", RuntimeException.class.getName(),
				escalations[1].getEscalationName());
	}

	/**
	 * Ensure labels override names.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void testLabels() {

		final String FUNCTION_NAME = "FUNCTION";
		final String OBJECT_NAME = "OBJECT";
		final String FLOW_NAME = "FLOW";
		final String ESCALATION_NAME = "ESCALATION";

		// Attempt to load namespace type
		FunctionNamespaceType namespaceType = this.loadManagedFunctionType(true, (namespace, context) -> {
			ManagedFunctionTypeBuilder function = namespace
					.addManagedFunctionType(FUNCTION_NAME, Indexed.class, Indexed.class)
					.setFunctionFactory(this.functionFactory);

			// Add object, flow, escalation labels
			function.addObject(Connection.class).setLabel(OBJECT_NAME);
			function.addFlow().setLabel(FLOW_NAME);
			function.addEscalation(Throwable.class).setLabel(ESCALATION_NAME);
		});

		// Validate the names
		ManagedFunctionType<?, ?> function = namespaceType.getManagedFunctionTypes()[0];
		assertEquals("Incorrect function name", FUNCTION_NAME, function.getFunctionName());
		assertEquals("Incorrect object name", OBJECT_NAME, function.getObjectTypes()[0].getObjectName());
		assertEquals("Incorrect flow name", FLOW_NAME, function.getFlowTypes()[0].getFlowName());
		assertEquals("Incorrect escalation name", ESCALATION_NAME,
				function.getEscalationTypes()[0].getEscalationName());
	}

	/**
	 * Wrong key {@link Enum}.
	 */
	private enum WrongKey {
		WRONG_KEY
	}

	/**
	 * Loads the {@link FunctionNamespaceType} within the input {@link Loader}.
	 * 
	 * @param isExpectedToLoad       Flag indicating if expecting to load the
	 *                               {@link FunctionNamespaceType}.
	 * @param loader                 {@link Loader}.
	 * @param propertyNameValuePairs {@link Property} name value pairs.
	 * @return Loaded {@link FunctionNamespaceType}.
	 */
	private FunctionNamespaceType loadManagedFunctionType(boolean isExpectedToLoad, Loader loader,
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

		// Create the namespace loader and load the namespace
		OfficeFloorCompiler compiler = OfficeFloorCompiler.newOfficeFloorCompiler(null);
		compiler.setCompilerIssues(this.issues);
		ManagedFunctionLoader functionLoader = compiler.getManagedFunctionLoader();
		MockManagedFunctionSource.loader = loader;
		FunctionNamespaceType namespaceType = functionLoader.loadManagedFunctionType(MockManagedFunctionSource.class,
				propertyList);

		// Verify the mock objects
		this.verifyMockObjects();

		// Ensure if should be loaded
		if (isExpectedToLoad) {
			assertNotNull("Expected to load the namespace type", namespaceType);
		} else {
			assertNull("Should not load the namespace type", namespaceType);
		}

		// Return the namespace type
		return namespaceType;
	}

	/**
	 * Implemented to load the {@link FunctionNamespaceType}.
	 */
	private interface Loader {

		/**
		 * Implemented to load the {@link FunctionNamespaceType}.
		 * 
		 * @param namespace {@link FunctionNamespaceBuilder}.
		 * @param context   {@link ManagedFunctionSourceContext}.
		 * @throws Exception If fails to source {@link FunctionNamespaceType}.
		 */
		void sourceManagedFunction(FunctionNamespaceBuilder namespace, ManagedFunctionSourceContext context)
				throws Exception;
	}

	/**
	 * Mock {@link ManagedFunctionSource} for testing.
	 */
	@TestSource
	public static class MockManagedFunctionSource implements ManagedFunctionSource {

		/**
		 * {@link Loader} to load the {@link FunctionNamespaceType}.
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
		public MockManagedFunctionSource() {
			if (instantiateFailure != null) {
				throw instantiateFailure;
			}
		}

		/*
		 * ================ ManagedFunctionSource ================
		 */

		@Override
		public ManagedFunctionSourceSpecification getSpecification() {
			fail("Should not be invoked in obtaining namespace type");
			return null;
		}

		@Override
		public void sourceManagedFunctions(FunctionNamespaceBuilder namespaceBuilder,
				ManagedFunctionSourceContext context) throws Exception {
			loader.sourceManagedFunction(namespaceBuilder, context);
		}
	}

}
