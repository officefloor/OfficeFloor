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
package net.officefloor.compile.impl.managedfunction;

import java.sql.Connection;
import java.util.Properties;

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
		this.issues.recordIssue(
				"Missing property 'missing' for ManagedFunctionSource " + MockManagedFunctionSource.class.getName());

		// Attempt to load namespace type
		this.loadManagedFunctionType(false, new Loader() {
			@Override
			public void sourceManagedFunction(FunctionNamespaceBuilder namespace, ManagedFunctionSourceContext context)
					throws Exception {
				context.getProperty("missing");
			}
		});
	}

	/**
	 * Ensure able to get properties.
	 */
	public void testGetProperties() {

		// Attempt to load namespace type
		this.loadManagedFunctionType(true, new Loader() {
			@Override
			public void sourceManagedFunction(FunctionNamespaceBuilder namespace, ManagedFunctionSourceContext context)
					throws Exception {
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
				namespace.addManagedFunctionType("IGNORE", functionFactory, null, null);
			}
		}, "ONE", "1", "TWO", "2");
	}

	/**
	 * Ensure issue if missing {@link Class}.
	 */
	public void testMissingClass() {

		// Record missing class
		this.issues.recordIssue(
				"Can not load class 'missing' for ManagedFunctionSource " + MockManagedFunctionSource.class.getName());

		// Attempt to load namespace type
		this.loadManagedFunctionType(false, new Loader() {
			@Override
			public void sourceManagedFunction(FunctionNamespaceBuilder namespace, ManagedFunctionSourceContext context)
					throws Exception {
				context.loadClass("missing");
			}
		});
	}

	/**
	 * Ensure issue if missing resource.
	 */
	public void testMissingResource() {

		// Record missing resource
		this.issues.recordIssue("Can not obtain resource at location 'missing' for ManagedFunctionSource "
				+ MockManagedFunctionSource.class.getName());

		// Attempt to load namespace type
		this.loadManagedFunctionType(false, new Loader() {
			@Override
			public void sourceManagedFunction(FunctionNamespaceBuilder namespace, ManagedFunctionSourceContext context)
					throws Exception {
				context.getResource("missing");
			}
		});
	}

	/**
	 * Ensure able to get the {@link ClassLoader}.
	 */
	public void testGetClassLoader() {

		// Attempt to load namespace type
		this.loadManagedFunctionType(true, new Loader() {
			@Override
			public void sourceManagedFunction(FunctionNamespaceBuilder namespace, ManagedFunctionSourceContext context)
					throws Exception {
				assertEquals("Incorrect class loader", LoadManagedFunctionTypeTest.class.getClassLoader(),
						context.getClassLoader());

				// Providing minimal namespace type
				namespace.addManagedFunctionType("IGNORE", functionFactory, null, null);
			}
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
		this.loadManagedFunctionType(false, new Loader() {
			@Override
			public void sourceManagedFunction(FunctionNamespaceBuilder namespace, ManagedFunctionSourceContext context)
					throws Exception {
				throw failure;
			}
		});
	}

	/**
	 * Ensure issue if no {@link ManagedFunctionType} instances as no point of
	 * {@link FunctionNamespaceType} without at least one
	 * {@link ManagedFunction}.
	 */
	public void testNoFunctions() {

		// Record no functions
		this.issues.recordIssue("No ManagedFunctionType definitions provided by ManagedFunctionSource "
				+ MockManagedFunctionSource.class.getName());

		// Attempt to load namespace type
		this.loadManagedFunctionType(false, new Loader() {
			@Override
			public void sourceManagedFunction(FunctionNamespaceBuilder namespace, ManagedFunctionSourceContext context)
					throws Exception {
				// No functions
			}
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
		this.loadManagedFunctionType(false, new Loader() {
			@Override
			public void sourceManagedFunction(FunctionNamespaceBuilder namespace, ManagedFunctionSourceContext context)
					throws Exception {
				namespace.addManagedFunctionType(null, functionFactory, null, null);
			}
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
		this.loadManagedFunctionType(false, new Loader() {
			@Override
			public void sourceManagedFunction(FunctionNamespaceBuilder namespace, ManagedFunctionSourceContext context)
					throws Exception {
				namespace.addManagedFunctionType("SAME", functionFactory, null, null);
				namespace.addManagedFunctionType("SAME", functionFactory, null, null);
			}
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
		this.loadManagedFunctionType(false, new Loader() {
			@Override
			public void sourceManagedFunction(FunctionNamespaceBuilder namespace, ManagedFunctionSourceContext context)
					throws Exception {
				namespace.addManagedFunctionType("FUNCTION", (ManagedFunctionFactory<?, ?>) null, null, null);
			}
		});
	}

	/**
	 * Ensure annotation not required.
	 */
	@SuppressWarnings("unchecked")
	public void testNoAnnotation() {

		final ManagedFunctionFactory<None, None> functionFactory = this.createMock(ManagedFunctionFactory.class);

		// Attempt to load annotation
		FunctionNamespaceType namespace = this.loadManagedFunctionType(true, new Loader() {
			@Override
			public void sourceManagedFunction(FunctionNamespaceBuilder namespace, ManagedFunctionSourceContext context)
					throws Exception {
				namespace.addManagedFunctionType("FUNCTION", functionFactory, None.class, None.class);
				// Do not specify annotation
			}
		});

		// Ensure annotation available
		Object[] annotations = namespace.getManagedFunctionTypes()[0].getAnnotations();
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
		FunctionNamespaceType namespace = this.loadManagedFunctionType(true, new Loader() {
			@Override
			public void sourceManagedFunction(FunctionNamespaceBuilder namespace, ManagedFunctionSourceContext context)
					throws Exception {
				ManagedFunctionTypeBuilder<None, None> function = namespace.addManagedFunctionType("FUNCTION",
						functionFactory, None.class, None.class);
				function.addAnnotation(ANNOTATION);
			}
		});

		// Ensure annotation available
		Object annotation = namespace.getManagedFunctionTypes()[0].getAnnotations()[0];
		assertEquals("Incorrect annotation", ANNOTATION, annotation);
	}

	/**
	 * Ensure able to load annotation for parameters.
	 */
	@SuppressWarnings("unchecked")
	public void testObjectAnnotation() {

		final ManagedFunctionFactory<Indexed, None> functionFactory = this.createMock(ManagedFunctionFactory.class);
		final Object ANNOTATION = "Annotation";

		// Attempt to load annotation
		FunctionNamespaceType namespace = this.loadManagedFunctionType(true, new Loader() {
			@Override
			public void sourceManagedFunction(FunctionNamespaceBuilder namespace, ManagedFunctionSourceContext context)
					throws Exception {
				ManagedFunctionTypeBuilder<Indexed, None> function = namespace.addManagedFunctionType("FUNCTION",
						functionFactory, Indexed.class, None.class);
				function.addObject(String.class).addAnnotation(ANNOTATION);
			}
		});

		// Ensure annotation available
		Object annotation = namespace.getManagedFunctionTypes()[0].getObjectTypes()[0].getAnnotations()[0];
		assertEquals("Incorrect annotation", ANNOTATION, annotation);
	}

	/**
	 * Ensure issue if using {@link Enum} but no
	 * {@link ManagedFunctionObjectType} provided for a key.
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
		this.loadManagedFunctionType(false, new Loader() {
			@Override
			public void sourceManagedFunction(FunctionNamespaceBuilder namespace, ManagedFunctionSourceContext context)
					throws Exception {
				namespace.addManagedFunctionType("FUNCTION", functionFactory, ObjectKey.class, null);
			}
		});
	}

	/**
	 * Ensure issue if no key for {@link ManagedFunctionObjectType} by provided
	 * key class.
	 */
	@SuppressWarnings("unchecked")
	public void testNoKeyForObject() {

		final ManagedFunctionFactory<ObjectKey, Indexed> functionFactory = this
				.createMock(ManagedFunctionFactory.class);

		// Record no functions
		this.issues.recordIssue(
				"No key provided for an object on ManagedFunctionType definition 0 (FUNCTION) by ManagedFunctionSource "
						+ MockManagedFunctionSource.class.getName());

		// Attempt to load namespace type
		this.loadManagedFunctionType(false, new Loader() {
			@Override
			@SuppressWarnings("rawtypes")
			public void sourceManagedFunction(FunctionNamespaceBuilder namespace, ManagedFunctionSourceContext context)
					throws Exception {
				ManagedFunctionTypeBuilder function = namespace.addManagedFunctionType("FUNCTION", functionFactory,
						ObjectKey.class, null);

				// Add function without key
				function.addObject(Connection.class);
			}
		});
	}

	/**
	 * Ensure issue if incorrect key type for {@link ManagedFunctionObjectType}
	 * by provided key class.
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
		this.loadManagedFunctionType(false, new Loader() {
			@Override
			public void sourceManagedFunction(FunctionNamespaceBuilder namespace, ManagedFunctionSourceContext context)
					throws Exception {
				ManagedFunctionTypeBuilder function = namespace.addManagedFunctionType("FUNCTION", functionFactory,
						ObjectKey.class, null);

				// Add function with wrong key
				function.addObject(Connection.class).setKey(WrongKey.WRONG_KEY);
			}
		});
	}

	/**
	 * Ensure issue if more {@link ManagedFunctionObjectType} instances than
	 * keys.
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
		this.loadManagedFunctionType(false, new Loader() {
			@Override
			public void sourceManagedFunction(FunctionNamespaceBuilder namespace, ManagedFunctionSourceContext context)
					throws Exception {
				ManagedFunctionTypeBuilder function = namespace.addManagedFunctionType("FUNCTION", functionFactory,
						ObjectKey.class, null);

				// Add extra objects than keys
				function.addObject(Connection.class).setKey(ObjectKey.ONE);
				function.addObject(String.class).setKey(ObjectKey.TWO);
				function.addObject(String.class).setKey(ObjectKey.TWO);
			}
		});
	}

	/**
	 * Ensure issue if key provided for {@link ManagedFunctionObjectType} but no
	 * key class.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void testObjectHasKeyButNoKeyClass() {

		final ManagedFunctionFactory<ObjectKey, Indexed> functionFactory = this
				.createMock(ManagedFunctionFactory.class);

		// Record indexes out of order
		this.issues.recordIssue(
				"Objects are not keyed but object has key on ManagedFunctionType definition 0 (FUNCTION) by ManagedFunctionSource "
						+ MockManagedFunctionSource.class.getName());

		// Attempt to load namespace type
		this.loadManagedFunctionType(false, new Loader() {
			@Override
			public void sourceManagedFunction(FunctionNamespaceBuilder namespace, ManagedFunctionSourceContext context)
					throws Exception {
				ManagedFunctionTypeBuilder function = namespace.addManagedFunctionType("FUNCTION", functionFactory,
						null, null);

				// Add extra objects than keys
				function.addObject(Connection.class).setKey(ObjectKey.ONE);
			}
		});
	}

	/**
	 * Ensure issue if no object type.
	 */
	public void testNoObjectType() {

		// Record no object type
		this.issues.recordIssue(
				"No object type provided for object 0 on ManagedFunctionType definition 0 (FUNCTION) by ManagedFunctionSource "
						+ MockManagedFunctionSource.class.getName());

		// Attempt to load namespace type
		this.loadManagedFunctionType(false, new Loader() {
			@Override
			@SuppressWarnings({ "rawtypes", "unchecked" })
			public void sourceManagedFunction(FunctionNamespaceBuilder namespace, ManagedFunctionSourceContext context)
					throws Exception {
				ManagedFunctionTypeBuilder function = namespace.addManagedFunctionType("FUNCTION", functionFactory,
						null, null);

				// Add function wit no object type
				function.addObject(null);
			}
		});
	}

	/**
	 * Ensure issue if duplicate object name.
	 */
	public void testDuplicateObjectNames() {

		// Record no object type
		this.issues.recordIssue(
				"Two or more ManagedFunctionObjectType definitions with the same name (SAME) for ManagedFunctionType definition 0 (FUNCTION) by ManagedFunctionSource "
						+ MockManagedFunctionSource.class.getName());

		// Attempt to load namespace type
		this.loadManagedFunctionType(false, new Loader() {
			@Override
			@SuppressWarnings({ "rawtypes", "unchecked" })
			public void sourceManagedFunction(FunctionNamespaceBuilder namespace, ManagedFunctionSourceContext context)
					throws Exception {
				ManagedFunctionTypeBuilder function = namespace.addManagedFunctionType("FUNCTION", functionFactory,
						null, null);

				// Add objects with same name
				function.addObject(Connection.class).setLabel("SAME");
				function.addObject(Connection.class).setLabel("SAME");
			}
		});
	}

	/**
	 * Ensure issue if {@link ManagedFunctionObjectType} are not ordered to
	 * {@link Enum} key order.
	 */
	@SuppressWarnings("unchecked")
	public void testKeyOrderingForObjectType() {

		final ManagedFunctionFactory<ObjectKey, Indexed> functionFactory = this
				.createMock(ManagedFunctionFactory.class);
		final Class<?> oneType = String.class;
		final Class<?> twoType = Connection.class;

		// Attempt to load namespace type
		FunctionNamespaceType namespaceType = this.loadManagedFunctionType(true, new Loader() {
			@Override
			@SuppressWarnings("rawtypes")
			public void sourceManagedFunction(FunctionNamespaceBuilder namespace, ManagedFunctionSourceContext context)
					throws Exception {
				ManagedFunctionTypeBuilder function = namespace.addManagedFunctionType("FUNCTION", functionFactory,
						ObjectKey.class, null);

				// Add in wrong order
				function.addObject(twoType).setKey(ObjectKey.TWO);
				function.addObject(oneType).setKey(ObjectKey.ONE);
			}
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
	@SuppressWarnings("unchecked")
	public void testObjectTypeQualification() {

		final ManagedFunctionFactory<Indexed, Indexed> functionFactory = this.createMock(ManagedFunctionFactory.class);

		// Attempt to load namespace type
		FunctionNamespaceType namespaceType = this.loadManagedFunctionType(true, new Loader() {
			@Override
			@SuppressWarnings("rawtypes")
			public void sourceManagedFunction(FunctionNamespaceBuilder namespace, ManagedFunctionSourceContext context)
					throws Exception {
				ManagedFunctionTypeBuilder function = namespace.addManagedFunctionType("FUNCTION", functionFactory,
						null, null);

				// Add objects with type qualified for on
				function.addObject(Connection.class).setTypeQualifier("QUALIFIED");
				function.addObject(Connection.class); // unqualified
			}
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
		this.loadManagedFunctionType(false, new Loader() {
			@Override
			public void sourceManagedFunction(FunctionNamespaceBuilder namespace, ManagedFunctionSourceContext context)
					throws Exception {
				namespace.addManagedFunctionType("FUNCTION", functionFactory, null, FlowKey.class);
			}
		});
	}

	/**
	 * Ensure issue if no key for {@link ManagedFunctionFlowType} but provided
	 * key class.
	 */
	@SuppressWarnings("unchecked")
	public void testNoKeyForFlow() {

		final ManagedFunctionFactory<Indexed, ObjectKey> functionFactory = this
				.createMock(ManagedFunctionFactory.class);

		// Record no functions
		this.issues.recordIssue(
				"No key provided for a flow on ManagedFunctionType definition 0 (FUNCTION) by ManagedFunctionSource "
						+ MockManagedFunctionSource.class.getName());

		// Attempt to load namespace type
		this.loadManagedFunctionType(false, new Loader() {
			@Override
			@SuppressWarnings("rawtypes")
			public void sourceManagedFunction(FunctionNamespaceBuilder namespace, ManagedFunctionSourceContext context)
					throws Exception {
				ManagedFunctionTypeBuilder function = namespace.addManagedFunctionType("FUNCTION", functionFactory,
						null, ObjectKey.class);

				// Add function without key
				function.addFlow();
			}
		});
	}

	/**
	 * Ensure issue if incorrect key type for {@link ManagedFunctionFlowType}
	 * but provided key class.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void testIncorrectKeyTypeForFlow() {

		final ManagedFunctionFactory<Indexed, FlowKey> functionFactory = this.createMock(ManagedFunctionFactory.class);

		// Record no functions
		this.issues.recordIssue("Incorrect key type (" + WrongKey.class.getName()
				+ ") provided for a flow on ManagedFunctionType definition 0 (FUNCTION) by ManagedFunctionSource "
				+ MockManagedFunctionSource.class.getName());

		// Attempt to load namespace type
		this.loadManagedFunctionType(false, new Loader() {
			@Override
			public void sourceManagedFunction(FunctionNamespaceBuilder namespace, ManagedFunctionSourceContext context)
					throws Exception {
				ManagedFunctionTypeBuilder function = namespace.addManagedFunctionType("FUNCTION", functionFactory,
						null, FlowKey.class);

				// Add function with wrong key
				function.addFlow().setKey(WrongKey.WRONG_KEY);
			}
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
		this.loadManagedFunctionType(false, new Loader() {
			@Override
			public void sourceManagedFunction(FunctionNamespaceBuilder namespace, ManagedFunctionSourceContext context)
					throws Exception {
				ManagedFunctionTypeBuilder function = namespace.addManagedFunctionType("FUNCTION", functionFactory,
						null, FlowKey.class);

				// Add extra objects than keys
				function.addFlow().setKey(FlowKey.ONE);
				function.addFlow().setKey(FlowKey.TWO);
				function.addFlow().setKey(FlowKey.TWO);
			}
		});
	}

	/**
	 * Ensure issue if key provided for {@link ManagedFunctionFlowType} but no
	 * key class.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void testFlowHasKeyButNoKeyClass() {

		final ManagedFunctionFactory<Indexed, FlowKey> functionFactory = this.createMock(ManagedFunctionFactory.class);

		// Record indexes out of order
		this.issues.recordIssue(
				"Flows are not keyed but flow has key on ManagedFunctionType definition 0 (FUNCTION) by ManagedFunctionSource "
						+ MockManagedFunctionSource.class.getName());

		// Attempt to load namespace type
		this.loadManagedFunctionType(false, new Loader() {
			@Override
			public void sourceManagedFunction(FunctionNamespaceBuilder namespace, ManagedFunctionSourceContext context)
					throws Exception {
				ManagedFunctionTypeBuilder function = namespace.addManagedFunctionType("FUNCTION", functionFactory,
						null, null);

				// Add flow with key
				function.addFlow().setKey(FlowKey.ONE);
			}
		});
	}

	/**
	 * Ensure issue if duplicate {@link ManagedFunctionFlowType} names.
	 */
	public void testDuplicateFlowNames() {

		// Record no object type
		this.issues.recordIssue(
				"Two or more ManagedFunctionFlowType definitions with the same name (SAME) for ManagedFunctionType definition 0 (FUNCTION) by ManagedFunctionSource "
						+ MockManagedFunctionSource.class.getName());

		// Attempt to load namespace type
		this.loadManagedFunctionType(false, new Loader() {
			@Override
			@SuppressWarnings("rawtypes")
			public void sourceManagedFunction(FunctionNamespaceBuilder namespace, ManagedFunctionSourceContext context)
					throws Exception {
				ManagedFunctionTypeBuilder function = namespace.addManagedFunctionType("FUNCTION", functionFactory,
						null, null);

				// Add flows with same name
				function.addFlow().setLabel("SAME");
				function.addFlow().setLabel("SAME");
			}
		});
	}

	/**
	 * Ensure issue if {@link ManagedFunctionFlowType} are not ordered to
	 * {@link Enum} key order.
	 */
	@SuppressWarnings("unchecked")
	public void testKeyOrderingForFlowType() {

		final ManagedFunctionFactory<Indexed, FlowKey> functionFactory = this.createMock(ManagedFunctionFactory.class);
		final Class<?> oneType = String.class;

		// Attempt to load namespace type
		FunctionNamespaceType namespaceType = this.loadManagedFunctionType(true, new Loader() {
			@Override
			@SuppressWarnings("rawtypes")
			public void sourceManagedFunction(FunctionNamespaceBuilder namespace, ManagedFunctionSourceContext context)
					throws Exception {
				ManagedFunctionTypeBuilder function = namespace.addManagedFunctionType("FUNCTION", functionFactory,
						null, FlowKey.class);

				// Add in wrong order
				function.addFlow().setKey(FlowKey.TWO);
				ManagedFunctionFlowTypeBuilder<FlowKey> flowType = function.addFlow();
				flowType.setKey(FlowKey.ONE);
				flowType.setArgumentType(oneType);
			}
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
	 * Ensure issue if no escalation type.
	 */
	public void testNoEscalationType() {

		// Record no escalation type
		this.issues.recordIssue(
				"No escalation type on ManagedFunctionType definition 0 (FUNCTION) by ManagedFunctionSource "
						+ MockManagedFunctionSource.class.getName());

		// Attempt to load namespace type
		this.loadManagedFunctionType(false, new Loader() {
			@Override
			@SuppressWarnings({ "rawtypes", "unchecked" })
			public void sourceManagedFunction(FunctionNamespaceBuilder namespace, ManagedFunctionSourceContext context)
					throws Exception {
				ManagedFunctionTypeBuilder function = namespace.addManagedFunctionType("FUNCTION", functionFactory,
						null, null);

				// Add no escalation type
				function.addEscalation(null);
			}
		});
	}

	/**
	 * Ensure issue if duplicate escalation names.
	 */
	public void testDuplicateEscalationNames() {

		// Record no object type
		this.issues.recordIssue(
				"Two or more ManagedFunctionEscalationType definitions with the same name (SAME) for ManagedFunctionType definition 0 (FUNCTION) by ManagedFunctionSource "
						+ MockManagedFunctionSource.class.getName());

		// Attempt to load namespace type
		this.loadManagedFunctionType(false, new Loader() {
			@Override
			@SuppressWarnings({ "rawtypes", "unchecked" })
			public void sourceManagedFunction(FunctionNamespaceBuilder namespace, ManagedFunctionSourceContext context)
					throws Exception {
				ManagedFunctionTypeBuilder function = namespace.addManagedFunctionType("FUNCTION", functionFactory,
						null, null);

				// Add duplicate escalation names
				function.addEscalation(Exception.class).setLabel("SAME");
				function.addEscalation(Exception.class).setLabel("SAME");
			}
		});
	}

	/**
	 * Ensure can include escalation.
	 */
	public void testEscalation() {

		// Attempt to load namespace type
		FunctionNamespaceType namespaceType = this.loadManagedFunctionType(true, new Loader() {
			@Override
			@SuppressWarnings({ "rawtypes", "unchecked" })
			public void sourceManagedFunction(FunctionNamespaceBuilder namespace, ManagedFunctionSourceContext context)
					throws Exception {
				ManagedFunctionTypeBuilder function = namespace.addManagedFunctionType("FUNCTION", functionFactory,
						null, null);

				// Add escalations
				function.addEscalation(Error.class);
				function.addEscalation(RuntimeException.class);
			}
		});

		// Validate the escalation
		ManagedFunctionEscalationType[] escalations = namespaceType.getManagedFunctionTypes()[0].getEscalationTypes();
		assertEquals("Incorrect number of escalation", 2, escalations.length);
		assertEquals("Incorrect first escalation type", Error.class, escalations[0].getEscalationType());
		assertEquals("Incorrect first escalation name", Error.class.getSimpleName(),
				escalations[0].getEscalationName());
		assertEquals("Incorrect second escalation type", RuntimeException.class, escalations[1].getEscalationType());
		assertEquals("Incorrect second escalation name", RuntimeException.class.getSimpleName(),
				escalations[1].getEscalationName());
	}

	/**
	 * Ensure labels override names.
	 */
	public void testLabels() {

		final String FUNCTION_NAME = "FUNCTION";
		final String OBJECT_NAME = "OBJECT";
		final String FLOW_NAME = "FLOW";
		final String ESCALATION_NAME = "ESCALATION";

		// Attempt to load namespace type
		FunctionNamespaceType namespace = this.loadManagedFunctionType(true, new Loader() {
			@Override
			@SuppressWarnings({ "rawtypes", "unchecked" })
			public void sourceManagedFunction(FunctionNamespaceBuilder namespace, ManagedFunctionSourceContext context)
					throws Exception {
				ManagedFunctionTypeBuilder function = namespace.addManagedFunctionType(FUNCTION_NAME, functionFactory,
						null, null);

				// Add object, flow, escalation labels
				function.addObject(Connection.class).setLabel(OBJECT_NAME);
				function.addFlow().setLabel(FLOW_NAME);
				function.addEscalation(Throwable.class).setLabel(ESCALATION_NAME);
			}
		});

		// Validate no key classes
		ManagedFunctionType<?, ?> function = namespace.getManagedFunctionTypes()[0];
		assertNull("Should not have object key class", function.getObjectKeyClass());
		assertNull("Should not have flow key class", function.getFlowKeyClass());

		// Validate the names
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
	 * @param isExpectedToLoad
	 *            Flag indicating if expecting to load the
	 *            {@link FunctionNamespaceType}.
	 * @param loader
	 *            {@link Loader}.
	 * @param propertyNameValuePairs
	 *            {@link Property} name value pairs.
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
		 * @param namespace
		 *            {@link FunctionNamespaceBuilder}.
		 * @param context
		 *            {@link ManagedFunctionSourceContext}.
		 * @throws Exception
		 *             If fails to source {@link FunctionNamespaceType}.
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