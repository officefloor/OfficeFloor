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
package net.officefloor.compile.impl.work;

import java.sql.Connection;
import java.util.Properties;

import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.impl.properties.PropertyListImpl;
import net.officefloor.compile.issues.CompilerIssues;
import net.officefloor.compile.managedfunction.ManagedFunctionEscalationType;
import net.officefloor.compile.managedfunction.ManagedFunctionFlowType;
import net.officefloor.compile.managedfunction.ManagedFunctionObjectType;
import net.officefloor.compile.managedfunction.ManagedFunctionType;
import net.officefloor.compile.managedfunction.ManagedFunctionLoader;
import net.officefloor.compile.managedfunction.FunctionNamespaceType;
import net.officefloor.compile.properties.Property;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionSource;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionFlowTypeBuilder;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionTypeBuilder;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionSourceContext;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionSourceSpecification;
import net.officefloor.compile.spi.managedfunction.source.FunctionNamespaceBuilder;
import net.officefloor.compile.test.issues.MockCompilerIssues;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.build.WorkFactory;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.function.ManagedFunctionFactory;
import net.officefloor.frame.api.function.Work;
import net.officefloor.frame.api.source.TestSource;
import net.officefloor.frame.test.OfficeFrameTestCase;

/**
 * Tests loading the {@link FunctionNamespaceType} from the {@link ManagedFunctionSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class LoadWorkTypeTest extends OfficeFrameTestCase {

	/**
	 * {@link CompilerIssues}.
	 */
	private final MockCompilerIssues issues = new MockCompilerIssues(this);

	/**
	 * {@link WorkFactory}.
	 */
	@SuppressWarnings("unchecked")
	private final WorkFactory<Work> workFactory = this
			.createMock(WorkFactory.class);

	/**
	 * {@link TaskFactoryManufacturer}.
	 */
	@SuppressWarnings("unchecked")
	private final ManagedFunctionFactory<Work, Indexed, Indexed> taskFactory = this
			.createMock(ManagedFunctionFactory.class);

	@Override
	protected void setUp() throws Exception {
		MockWorkSource.reset();
	}

	/**
	 * Ensure issue if fail to instantiate the {@link ManagedFunctionSource}.
	 */
	public void testFailInstantiate() {

		final RuntimeException failure = new RuntimeException(
				"instantiate failure");

		// Record failure to instantiate
		this.issues.recordIssue(
				"Failed to instantiate " + MockWorkSource.class.getName()
						+ " by default constructor", failure);

		// Attempt to obtain specification
		MockWorkSource.instantiateFailure = failure;
		this.loadWorkType(false, null);
	}

	/**
	 * Ensure issue if missing {@link Property}.
	 */
	public void testMissingProperty() {

		// Record missing property
		this.issues.recordIssue("Missing property 'missing' for WorkSource "
				+ MockWorkSource.class.getName());

		// Attempt to load work type
		this.loadWorkType(false, new Loader() {
			@Override
			public void sourceWork(FunctionNamespaceBuilder<Work> work,
					ManagedFunctionSourceContext context) throws Exception {
				context.getProperty("missing");
			}
		});
	}

	/**
	 * Ensure able to get properties.
	 */
	public void testGetProperties() {

		// Attempt to load work type
		this.loadWorkType(true, new Loader() {
			@Override
			public void sourceWork(FunctionNamespaceBuilder<Work> work,
					ManagedFunctionSourceContext context) throws Exception {
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

				// Providing minimal work type
				work.setWorkFactory(workFactory);
				work.addManagedFunctionType("IGNORE", taskFactory, null, null);
			}
		}, "ONE", "1", "TWO", "2");
	}

	/**
	 * Ensure issue if missing {@link Class}.
	 */
	public void testMissingClass() {

		// Record missing class
		this.issues.recordIssue("Can not load class 'missing' for WorkSource "
				+ MockWorkSource.class.getName());

		// Attempt to load work type
		this.loadWorkType(false, new Loader() {
			@Override
			public void sourceWork(FunctionNamespaceBuilder<Work> work,
					ManagedFunctionSourceContext context) throws Exception {
				context.loadClass("missing");
			}
		});
	}

	/**
	 * Ensure issue if missing resource.
	 */
	public void testMissingResource() {

		// Record missing resource
		this.issues
				.recordIssue("Can not obtain resource at location 'missing' for WorkSource "
						+ MockWorkSource.class.getName());

		// Attempt to load work type
		this.loadWorkType(false, new Loader() {
			@Override
			public void sourceWork(FunctionNamespaceBuilder<Work> work,
					ManagedFunctionSourceContext context) throws Exception {
				context.getResource("missing");
			}
		});
	}

	/**
	 * Ensure able to get the {@link ClassLoader}.
	 */
	public void testGetClassLoader() {

		// Attempt to load work type
		this.loadWorkType(true, new Loader() {
			@Override
			public void sourceWork(FunctionNamespaceBuilder<Work> work,
					ManagedFunctionSourceContext context) throws Exception {
				assertEquals("Incorrect class loader",
						LoadWorkTypeTest.class.getClassLoader(),
						context.getClassLoader());

				// Providing minimal work type
				work.setWorkFactory(workFactory);
				work.addManagedFunctionType("IGNORE", taskFactory, null, null);
			}
		});
	}

	/**
	 * Ensure issue if fails to source the {@link FunctionNamespaceType}.
	 */
	public void testFailSourceWorkType() {

		final NullPointerException failure = new NullPointerException(
				"Fail source work type");

		// Record failure to source the work type
		this.issues.recordIssue(
				"Failed to source WorkType definition from WorkSource "
						+ MockWorkSource.class.getName(), failure);

		// Attempt to load work type
		this.loadWorkType(false, new Loader() {
			@Override
			public void sourceWork(FunctionNamespaceBuilder<Work> work,
					ManagedFunctionSourceContext context) throws Exception {
				throw failure;
			}
		});
	}

	/**
	 * Ensure issue if no {@link WorkFactory}.
	 */
	public void testNoWorkFactory() {

		// Record no work factory
		this.issues.recordIssue("No WorkFactory provided by WorkSource "
				+ MockWorkSource.class.getName());

		// Attempt to load work type
		this.loadWorkType(false, new Loader() {
			@Override
			public void sourceWork(FunctionNamespaceBuilder<Work> work,
					ManagedFunctionSourceContext context) throws Exception {
				// Not add work factory
			}
		});
	}

	/**
	 * Ensure issue if no {@link ManagedFunctionType} instances as no point of {@link Work}
	 * without at least one {@link ManagedFunction}.
	 */
	public void testNoTasks() {

		// Record no tasks
		this.issues
				.recordIssue("No TaskType definitions provided by WorkSource "
						+ MockWorkSource.class.getName());

		// Attempt to load work type
		this.loadWorkType(false, new Loader() {
			@Override
			public void sourceWork(FunctionNamespaceBuilder<Work> work,
					ManagedFunctionSourceContext context) throws Exception {
				work.setWorkFactory(workFactory);
				// No tasks
			}
		});
	}

	/**
	 * Ensure issue if no {@link ManagedFunctionType} name.
	 */
	public void testNoTaskName() {

		// Record no tasks
		this.issues
				.recordIssue("No task name provided for TaskType definition 0 by WorkSource "
						+ MockWorkSource.class.getName());

		// Attempt to load work type
		this.loadWorkType(false, new Loader() {
			@Override
			public void sourceWork(FunctionNamespaceBuilder<Work> work,
					ManagedFunctionSourceContext context) throws Exception {
				work.setWorkFactory(workFactory);
				work.addManagedFunctionType(null, taskFactory, null, null);
			}
		});
	}

	/**
	 * Ensure issue if duplicate {@link ManagedFunctionType} name.
	 */
	public void testDuplicateTaskNames() {

		// Record duplicate task names
		this.issues
				.recordIssue("Two or more TaskType definitions with the same name (SAME) provided by WorkSource "
						+ MockWorkSource.class.getName());

		// Attempt to load work type
		this.loadWorkType(false, new Loader() {
			@Override
			public void sourceWork(FunctionNamespaceBuilder<Work> work,
					ManagedFunctionSourceContext context) throws Exception {
				work.setWorkFactory(workFactory);
				work.addManagedFunctionType("SAME", taskFactory, null, null);
				work.addManagedFunctionType("SAME", taskFactory, null, null);
			}
		});
	}

	/**
	 * Ensure issue if no {@link TaskFactoryManufacturer}.
	 */
	public void testNoTaskFactory() {

		// Record no tasks
		this.issues
				.recordIssue("No TaskFactory provided for TaskType definition 0 (TASK) by WorkSource "
						+ MockWorkSource.class.getName());

		// Attempt to load work type
		this.loadWorkType(false, new Loader() {
			@Override
			public void sourceWork(FunctionNamespaceBuilder<Work> work,
					ManagedFunctionSourceContext context) throws Exception {
				work.setWorkFactory(workFactory);
				work.addManagedFunctionType("TASK", (ManagedFunctionFactory<Work, ?, ?>) null, null,
						null);
			}
		});
	}

	/**
	 * Ensure Differentiator not required.
	 */
	@SuppressWarnings("unchecked")
	public void testNoDifferentiator() {

		final ManagedFunctionFactory<Work, None, None> taskFactory = this
				.createMock(ManagedFunctionFactory.class);

		// Attempt to load differentiator
		FunctionNamespaceType<Work> work = this.loadWorkType(true, new Loader() {
			@Override
			public void sourceWork(FunctionNamespaceBuilder<Work> work,
					ManagedFunctionSourceContext context) throws Exception {
				work.setWorkFactory(workFactory);
				work.addManagedFunctionType("TASK", taskFactory, None.class, None.class);
				// Do not specify differentiator
			}
		});

		// Ensure differentiator available
		Object differentiator = work.getManagedFunctionTypes()[0].getDifferentiator();
		assertNull("Should not have differentiator", differentiator);
	}

	/**
	 * Ensure able to load the Differentiator.
	 */
	@SuppressWarnings("unchecked")
	public void testDifferentiator() {

		final ManagedFunctionFactory<Work, None, None> taskFactory = this
				.createMock(ManagedFunctionFactory.class);
		final Object DIFFERENTIATOR = "Differentiator";

		// Attempt to load differentiator
		FunctionNamespaceType<Work> work = this.loadWorkType(true, new Loader() {
			@Override
			public void sourceWork(FunctionNamespaceBuilder<Work> work,
					ManagedFunctionSourceContext context) throws Exception {
				work.setWorkFactory(workFactory);
				ManagedFunctionTypeBuilder<None, None> task = work.addManagedFunctionType("TASK",
						taskFactory, None.class, None.class);
				task.setDifferentiator(DIFFERENTIATOR);
			}
		});

		// Ensure differentiator available
		Object differentiator = work.getManagedFunctionTypes()[0].getDifferentiator();
		assertEquals("Incorrect differentiator", DIFFERENTIATOR, differentiator);
	}

	/**
	 * Ensure issue if using {@link Enum} but no {@link ManagedFunctionObjectType} provided
	 * for a key.
	 */
	@SuppressWarnings("unchecked")
	public void testMissingObjectForKey() {

		final ManagedFunctionFactory<Work, ObjectKey, Indexed> taskFactory = this
				.createMock(ManagedFunctionFactory.class);

		// Record no tasks
		this.issues.recordIssue("No TaskObjectType provided for key "
				+ ObjectKey.ONE
				+ " on TaskType definition 0 (TASK) by WorkSource "
				+ MockWorkSource.class.getName());

		// Attempt to load work type
		this.loadWorkType(false, new Loader() {
			@Override
			public void sourceWork(FunctionNamespaceBuilder<Work> work,
					ManagedFunctionSourceContext context) throws Exception {
				work.setWorkFactory(workFactory);
				work.addManagedFunctionType("TASK", taskFactory, ObjectKey.class, null);
			}
		});
	}

	/**
	 * Ensure issue if no key for {@link ManagedFunctionObjectType} by provided key class.
	 */
	@SuppressWarnings("unchecked")
	public void testNoKeyForObject() {

		final ManagedFunctionFactory<Work, ObjectKey, Indexed> taskFactory = this
				.createMock(ManagedFunctionFactory.class);

		// Record no tasks
		this.issues
				.recordIssue("No key provided for an object on TaskType definition 0 (TASK) by WorkSource "
						+ MockWorkSource.class.getName());

		// Attempt to load work type
		this.loadWorkType(false, new Loader() {
			@Override
			@SuppressWarnings("rawtypes")
			public void sourceWork(FunctionNamespaceBuilder<Work> work,
					ManagedFunctionSourceContext context) throws Exception {
				work.setWorkFactory(workFactory);
				ManagedFunctionTypeBuilder task = work.addManagedFunctionType("TASK", taskFactory,
						ObjectKey.class, null);

				// Add task without key
				task.addObject(Connection.class);
			}
		});
	}

	/**
	 * Ensure issue if incorrect key type for {@link ManagedFunctionObjectType} by provided
	 * key class.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void testIncorrectKeyTypeForObject() {

		final ManagedFunctionFactory<Work, ObjectKey, Indexed> taskFactory = this
				.createMock(ManagedFunctionFactory.class);

		// Record no tasks
		this.issues
				.recordIssue("Incorrect key type ("
						+ WrongKey.class.getName()
						+ ") provided for an object on TaskType definition 0 (TASK) by WorkSource "
						+ MockWorkSource.class.getName());

		// Attempt to load work type
		this.loadWorkType(false, new Loader() {
			@Override
			public void sourceWork(FunctionNamespaceBuilder<Work> work,
					ManagedFunctionSourceContext context) throws Exception {
				work.setWorkFactory(workFactory);
				ManagedFunctionTypeBuilder task = work.addManagedFunctionType("TASK", taskFactory,
						ObjectKey.class, null);

				// Add task with wrong key
				task.addObject(Connection.class).setKey(WrongKey.WRONG_KEY);
			}
		});
	}

	/**
	 * Ensure issue if more {@link ManagedFunctionObjectType} instances than keys.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void testMoreObjectsThanKeys() {

		final ManagedFunctionFactory<Work, ObjectKey, Indexed> taskFactory = this
				.createMock(ManagedFunctionFactory.class);

		// Record no tasks
		this.issues
				.recordIssue("More objects than keys on TaskType definition 0 (TASK) by WorkSource "
						+ MockWorkSource.class.getName());

		// Attempt to load work type
		this.loadWorkType(false, new Loader() {
			@Override
			public void sourceWork(FunctionNamespaceBuilder<Work> work,
					ManagedFunctionSourceContext context) throws Exception {
				work.setWorkFactory(workFactory);
				ManagedFunctionTypeBuilder task = work.addManagedFunctionType("TASK", taskFactory,
						ObjectKey.class, null);

				// Add extra objects than keys
				task.addObject(Connection.class).setKey(ObjectKey.ONE);
				task.addObject(String.class).setKey(ObjectKey.TWO);
				task.addObject(String.class).setKey(ObjectKey.TWO);
			}
		});
	}

	/**
	 * Ensure issue if key provided for {@link ManagedFunctionObjectType} but no key class.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void testObjectHasKeyButNoKeyClass() {

		final ManagedFunctionFactory<Work, ObjectKey, Indexed> taskFactory = this
				.createMock(ManagedFunctionFactory.class);

		// Record indexes out of order
		this.issues
				.recordIssue("Objects are not keyed but object has key on TaskType definition 0 (TASK) by WorkSource "
						+ MockWorkSource.class.getName());

		// Attempt to load work type
		this.loadWorkType(false, new Loader() {
			@Override
			public void sourceWork(FunctionNamespaceBuilder<Work> work,
					ManagedFunctionSourceContext context) throws Exception {
				work.setWorkFactory(workFactory);
				ManagedFunctionTypeBuilder task = work.addManagedFunctionType("TASK", taskFactory,
						null, null);

				// Add extra objects than keys
				task.addObject(Connection.class).setKey(ObjectKey.ONE);
			}
		});
	}

	/**
	 * Ensure issue if no object type.
	 */
	public void testNoObjectType() {

		// Record no object type
		this.issues
				.recordIssue("No object type provided for object 0 on TaskType definition 0 (TASK) by WorkSource "
						+ MockWorkSource.class.getName());

		// Attempt to load work type
		this.loadWorkType(false, new Loader() {
			@Override
			@SuppressWarnings({ "rawtypes", "unchecked" })
			public void sourceWork(FunctionNamespaceBuilder<Work> work,
					ManagedFunctionSourceContext context) throws Exception {
				work.setWorkFactory(workFactory);
				ManagedFunctionTypeBuilder task = work.addManagedFunctionType("TASK", taskFactory,
						null, null);

				// Add task wit no object type
				task.addObject(null);
			}
		});
	}

	/**
	 * Ensure issue if duplicate object name.
	 */
	public void testDuplicateObjectNames() {

		// Record no object type
		this.issues
				.recordIssue("Two or more TaskObjectType definitions with the same name (SAME) for TaskType definition 0 (TASK) by WorkSource "
						+ MockWorkSource.class.getName());

		// Attempt to load work type
		this.loadWorkType(false, new Loader() {
			@Override
			@SuppressWarnings({ "rawtypes", "unchecked" })
			public void sourceWork(FunctionNamespaceBuilder<Work> work,
					ManagedFunctionSourceContext context) throws Exception {
				work.setWorkFactory(workFactory);
				ManagedFunctionTypeBuilder task = work.addManagedFunctionType("TASK", taskFactory,
						null, null);

				// Add objects with same name
				task.addObject(Connection.class).setLabel("SAME");
				task.addObject(Connection.class).setLabel("SAME");
			}
		});
	}

	/**
	 * Ensure issue if {@link ManagedFunctionObjectType} are not ordered to {@link Enum}
	 * key order.
	 */
	@SuppressWarnings("unchecked")
	public void testKeyOrderingForObjectType() {

		final ManagedFunctionFactory<Work, ObjectKey, Indexed> taskFactory = this
				.createMock(ManagedFunctionFactory.class);
		final Class<?> oneType = String.class;
		final Class<?> twoType = Connection.class;

		// Attempt to load work type
		FunctionNamespaceType<Work> workType = this.loadWorkType(true, new Loader() {
			@Override
			@SuppressWarnings("rawtypes")
			public void sourceWork(FunctionNamespaceBuilder<Work> work,
					ManagedFunctionSourceContext context) throws Exception {
				work.setWorkFactory(workFactory);
				ManagedFunctionTypeBuilder task = work.addManagedFunctionType("TASK", taskFactory,
						ObjectKey.class, null);

				// Add in wrong order
				task.addObject(twoType).setKey(ObjectKey.TWO);
				task.addObject(oneType).setKey(ObjectKey.ONE);
			}
		});

		// Validate the key class
		ManagedFunctionType<Work, ?, ?> task = workType.getManagedFunctionTypes()[0];
		assertEquals("Incorrect object key class", ObjectKey.class,
				task.getObjectKeyClass());

		// Validate the object type order
		ManagedFunctionObjectType<?>[] objects = task.getObjectTypes();
		assertEquals("Incorrect number of objects", 2, objects.length);
		ManagedFunctionObjectType<?> one = objects[0];
		assertEquals("Incorrect key one", ObjectKey.ONE, one.getKey());
		assertEquals("Incorrect index one", ObjectKey.ONE.ordinal(),
				one.getIndex());
		assertEquals("Incorrect name one", ObjectKey.ONE.toString(),
				one.getObjectName());
		assertEquals("Incorrect type one", oneType, one.getObjectType());
		ManagedFunctionObjectType<?> two = objects[1];
		assertEquals("Incorrect key two", ObjectKey.TWO, two.getKey());
		assertEquals("Incorrect index two", ObjectKey.TWO.ordinal(),
				two.getIndex());
		assertEquals("Incorrect name two", ObjectKey.TWO.toString(),
				two.getObjectName());
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

		final ManagedFunctionFactory<Work, Indexed, Indexed> taskFactory = this
				.createMock(ManagedFunctionFactory.class);

		// Attempt to load work type
		FunctionNamespaceType<Work> workType = this.loadWorkType(true, new Loader() {
			@Override
			@SuppressWarnings("rawtypes")
			public void sourceWork(FunctionNamespaceBuilder<Work> work,
					ManagedFunctionSourceContext context) throws Exception {
				work.setWorkFactory(workFactory);
				ManagedFunctionTypeBuilder task = work.addManagedFunctionType("TASK", taskFactory,
						null, null);

				// Add objects with type qualified for on
				task.addObject(Connection.class).setTypeQualifier("QUALIFIED");
				task.addObject(Connection.class); // unqualified
			}
		});

		// Validate the type qualification
		ManagedFunctionType<Work, ?, ?> task = workType.getManagedFunctionTypes()[0];
		ManagedFunctionObjectType<?>[] objects = task.getObjectTypes();
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
	 * Ensure issue if using {@link Enum} but no {@link ManagedFunctionFlowType} provided
	 * for a key.
	 */
	@SuppressWarnings("unchecked")
	public void testMissingFlowForKey() {

		final ManagedFunctionFactory<Work, Indexed, FlowKey> taskFactory = this
				.createMock(ManagedFunctionFactory.class);

		// Record no tasks
		this.issues.recordIssue("No TaskFlowType provided for key "
				+ FlowKey.ONE
				+ " on TaskType definition 0 (TASK) by WorkSource "
				+ MockWorkSource.class.getName());

		// Attempt to load work type
		this.loadWorkType(false, new Loader() {
			@Override
			public void sourceWork(FunctionNamespaceBuilder<Work> work,
					ManagedFunctionSourceContext context) throws Exception {
				work.setWorkFactory(workFactory);
				work.addManagedFunctionType("TASK", taskFactory, null, FlowKey.class);
			}
		});
	}

	/**
	 * Ensure issue if no key for {@link ManagedFunctionFlowType} but provided key class.
	 */
	@SuppressWarnings("unchecked")
	public void testNoKeyForFlow() {

		final ManagedFunctionFactory<Work, Indexed, ObjectKey> taskFactory = this
				.createMock(ManagedFunctionFactory.class);

		// Record no tasks
		this.issues
				.recordIssue("No key provided for a flow on TaskType definition 0 (TASK) by WorkSource "
						+ MockWorkSource.class.getName());

		// Attempt to load work type
		this.loadWorkType(false, new Loader() {
			@Override
			@SuppressWarnings("rawtypes")
			public void sourceWork(FunctionNamespaceBuilder<Work> work,
					ManagedFunctionSourceContext context) throws Exception {
				work.setWorkFactory(workFactory);
				ManagedFunctionTypeBuilder task = work.addManagedFunctionType("TASK", taskFactory,
						null, ObjectKey.class);

				// Add task without key
				task.addFlow();
			}
		});
	}

	/**
	 * Ensure issue if incorrect key type for {@link ManagedFunctionFlowType} but provided
	 * key class.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void testIncorrectKeyTypeForFlow() {

		final ManagedFunctionFactory<Work, Indexed, FlowKey> taskFactory = this
				.createMock(ManagedFunctionFactory.class);

		// Record no tasks
		this.issues
				.recordIssue("Incorrect key type ("
						+ WrongKey.class.getName()
						+ ") provided for a flow on TaskType definition 0 (TASK) by WorkSource "
						+ MockWorkSource.class.getName());

		// Attempt to load work type
		this.loadWorkType(false, new Loader() {
			@Override
			public void sourceWork(FunctionNamespaceBuilder<Work> work,
					ManagedFunctionSourceContext context) throws Exception {
				work.setWorkFactory(workFactory);
				ManagedFunctionTypeBuilder task = work.addManagedFunctionType("TASK", taskFactory,
						null, FlowKey.class);

				// Add task with wrong key
				task.addFlow().setKey(WrongKey.WRONG_KEY);
			}
		});
	}

	/**
	 * Ensure issue if more {@link ManagedFunctionFlowType} instances than keys.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void testMoreFlowsThanKeys() {

		final ManagedFunctionFactory<Work, Indexed, FlowKey> taskFactory = this
				.createMock(ManagedFunctionFactory.class);

		// Record no tasks
		this.issues
				.recordIssue("More flows than keys on TaskType definition 0 (TASK) by WorkSource "
						+ MockWorkSource.class.getName());

		// Attempt to load work type
		this.loadWorkType(false, new Loader() {
			@Override
			public void sourceWork(FunctionNamespaceBuilder<Work> work,
					ManagedFunctionSourceContext context) throws Exception {
				work.setWorkFactory(workFactory);
				ManagedFunctionTypeBuilder task = work.addManagedFunctionType("TASK", taskFactory,
						null, FlowKey.class);

				// Add extra objects than keys
				task.addFlow().setKey(FlowKey.ONE);
				task.addFlow().setKey(FlowKey.TWO);
				task.addFlow().setKey(FlowKey.TWO);
			}
		});
	}

	/**
	 * Ensure issue if key provided for {@link ManagedFunctionFlowType} but no key class.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void testFlowHasKeyButNoKeyClass() {

		final ManagedFunctionFactory<Work, Indexed, FlowKey> taskFactory = this
				.createMock(ManagedFunctionFactory.class);

		// Record indexes out of order
		this.issues
				.recordIssue("Flows are not keyed but flow has key on TaskType definition 0 (TASK) by WorkSource "
						+ MockWorkSource.class.getName());

		// Attempt to load work type
		this.loadWorkType(false, new Loader() {
			@Override
			public void sourceWork(FunctionNamespaceBuilder<Work> work,
					ManagedFunctionSourceContext context) throws Exception {
				work.setWorkFactory(workFactory);
				ManagedFunctionTypeBuilder task = work.addManagedFunctionType("TASK", taskFactory,
						null, null);

				// Add flow with key
				task.addFlow().setKey(FlowKey.ONE);
			}
		});
	}

	/**
	 * Ensure issue if duplicate {@link ManagedFunctionFlowType} names.
	 */
	public void testDuplicateFlowNames() {

		// Record no object type
		this.issues
				.recordIssue("Two or more TaskFlowType definitions with the same name (SAME) for TaskType definition 0 (TASK) by WorkSource "
						+ MockWorkSource.class.getName());

		// Attempt to load work type
		this.loadWorkType(false, new Loader() {
			@Override
			@SuppressWarnings("rawtypes")
			public void sourceWork(FunctionNamespaceBuilder<Work> work,
					ManagedFunctionSourceContext context) throws Exception {
				work.setWorkFactory(workFactory);
				ManagedFunctionTypeBuilder task = work.addManagedFunctionType("TASK", taskFactory,
						null, null);

				// Add flows with same name
				task.addFlow().setLabel("SAME");
				task.addFlow().setLabel("SAME");
			}
		});
	}

	/**
	 * Ensure issue if {@link ManagedFunctionFlowType} are not ordered to {@link Enum} key
	 * order.
	 */
	@SuppressWarnings("unchecked")
	public void testKeyOrderingForFlowType() {

		final ManagedFunctionFactory<Work, Indexed, FlowKey> taskFactory = this
				.createMock(ManagedFunctionFactory.class);
		final Class<?> oneType = String.class;

		// Attempt to load work type
		FunctionNamespaceType<Work> workType = this.loadWorkType(true, new Loader() {
			@Override
			@SuppressWarnings("rawtypes")
			public void sourceWork(FunctionNamespaceBuilder<Work> work,
					ManagedFunctionSourceContext context) throws Exception {
				work.setWorkFactory(workFactory);
				ManagedFunctionTypeBuilder task = work.addManagedFunctionType("TASK", taskFactory,
						null, FlowKey.class);

				// Add in wrong order
				task.addFlow().setKey(FlowKey.TWO);
				ManagedFunctionFlowTypeBuilder<FlowKey> flowType = task.addFlow();
				flowType.setKey(FlowKey.ONE);
				flowType.setArgumentType(oneType);
			}
		});

		// Validate the key class
		ManagedFunctionType<Work, ?, ?> task = workType.getManagedFunctionTypes()[0];
		assertEquals("Incorrect flow key class", FlowKey.class,
				task.getFlowKeyClass());

		// Validate the flow type order
		ManagedFunctionFlowType<?>[] flows = task.getFlowTypes();
		assertEquals("Incorrect number of flows", 2, flows.length);
		ManagedFunctionFlowType<?> one = flows[0];
		assertEquals("Incorrect key one", FlowKey.ONE, one.getKey());
		assertEquals("Incorrect index one", FlowKey.ONE.ordinal(),
				one.getIndex());
		assertEquals("Incorrect name one", FlowKey.ONE.toString(),
				one.getFlowName());
		assertEquals("Incorrect argument type one", oneType,
				one.getArgumentType());
		ManagedFunctionFlowType<?> two = flows[1];
		assertEquals("Incorrect key two", FlowKey.TWO, two.getKey());
		assertEquals("Incorrect index two", FlowKey.TWO.ordinal(),
				two.getIndex());
		assertEquals("Incorrect name two", FlowKey.TWO.toString(),
				two.getFlowName());
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
		this.issues
				.recordIssue("No escalation type on TaskType definition 0 (TASK) by WorkSource "
						+ MockWorkSource.class.getName());

		// Attempt to load work type
		this.loadWorkType(false, new Loader() {
			@Override
			@SuppressWarnings({ "rawtypes", "unchecked" })
			public void sourceWork(FunctionNamespaceBuilder<Work> work,
					ManagedFunctionSourceContext context) throws Exception {
				work.setWorkFactory(workFactory);
				ManagedFunctionTypeBuilder task = work.addManagedFunctionType("TASK", taskFactory,
						null, null);

				// Add no escalation type
				task.addEscalation(null);
			}
		});
	}

	/**
	 * Ensure issue if duplicate escalation names.
	 */
	public void testDuplicateEscalationNames() {

		// Record no object type
		this.issues
				.recordIssue("Two or more TaskEscalationType definitions with the same name (SAME) for TaskType definition 0 (TASK) by WorkSource "
						+ MockWorkSource.class.getName());

		// Attempt to load work type
		this.loadWorkType(false, new Loader() {
			@Override
			@SuppressWarnings({ "rawtypes", "unchecked" })
			public void sourceWork(FunctionNamespaceBuilder<Work> work,
					ManagedFunctionSourceContext context) throws Exception {
				work.setWorkFactory(workFactory);
				ManagedFunctionTypeBuilder task = work.addManagedFunctionType("TASK", taskFactory,
						null, null);

				// Add duplicate escalation names
				task.addEscalation(Exception.class).setLabel("SAME");
				task.addEscalation(Exception.class).setLabel("SAME");
			}
		});
	}

	/**
	 * Ensure can include escalation.
	 */
	public void testEscalation() {

		// Attempt to load work type
		FunctionNamespaceType<Work> workType = this.loadWorkType(true, new Loader() {
			@Override
			@SuppressWarnings({ "rawtypes", "unchecked" })
			public void sourceWork(FunctionNamespaceBuilder<Work> work,
					ManagedFunctionSourceContext context) throws Exception {
				work.setWorkFactory(workFactory);
				ManagedFunctionTypeBuilder task = work.addManagedFunctionType("TASK", taskFactory,
						null, null);

				// Add escalations
				task.addEscalation(Error.class);
				task.addEscalation(RuntimeException.class);
			}
		});

		// Validate the escalation
		ManagedFunctionEscalationType[] escalations = workType.getManagedFunctionTypes()[0]
				.getEscalationTypes();
		assertEquals("Incorrect number of escalation", 2, escalations.length);
		assertEquals("Incorrect first escalation type", Error.class,
				escalations[0].getEscalationType());
		assertEquals("Incorrect first escalation name",
				Error.class.getSimpleName(), escalations[0].getEscalationName());
		assertEquals("Incorrect second escalation type",
				RuntimeException.class, escalations[1].getEscalationType());
		assertEquals("Incorrect second escalation name",
				RuntimeException.class.getSimpleName(),
				escalations[1].getEscalationName());
	}

	/**
	 * Ensure labels override names.
	 */
	public void testLabels() {

		final String TASK_NAME = "TASK";
		final String OBJECT_NAME = "OBJECT";
		final String FLOW_NAME = "FLOW";
		final String ESCALATION_NAME = "ESCALATION";

		// Attempt to load work type
		FunctionNamespaceType<Work> work = this.loadWorkType(true, new Loader() {
			@Override
			@SuppressWarnings({ "rawtypes", "unchecked" })
			public void sourceWork(FunctionNamespaceBuilder<Work> work,
					ManagedFunctionSourceContext context) throws Exception {
				work.setWorkFactory(workFactory);
				ManagedFunctionTypeBuilder task = work.addManagedFunctionType(TASK_NAME, taskFactory,
						null, null);

				// Add object, flow, escalation labels
				task.addObject(Connection.class).setLabel(OBJECT_NAME);
				task.addFlow().setLabel(FLOW_NAME);
				task.addEscalation(Throwable.class).setLabel(ESCALATION_NAME);
			}
		});

		// Validate no key classes
		ManagedFunctionType<Work, ?, ?> task = work.getManagedFunctionTypes()[0];
		assertNull("Should not have object key class", task.getObjectKeyClass());
		assertNull("Should not have flow key class", task.getFlowKeyClass());

		// Validate the names
		assertEquals("Incorrect task name", TASK_NAME, task.getFunctionName());
		assertEquals("Incorrect object name", OBJECT_NAME,
				task.getObjectTypes()[0].getObjectName());
		assertEquals("Incorrect flow name", FLOW_NAME,
				task.getFlowTypes()[0].getFlowName());
		assertEquals("Incorrect escalation name", ESCALATION_NAME,
				task.getEscalationTypes()[0].getEscalationName());
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
	 *            Flag indicating if expecting to load the {@link FunctionNamespaceType}.
	 * @param loader
	 *            {@link Loader}.
	 * @param propertyNameValuePairs
	 *            {@link Property} name value pairs.
	 * @return Loaded {@link FunctionNamespaceType}.
	 */
	private FunctionNamespaceType<Work> loadWorkType(boolean isExpectedToLoad,
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

		// Create the work loader and load the work
		OfficeFloorCompiler compiler = OfficeFloorCompiler
				.newOfficeFloorCompiler(null);
		compiler.setCompilerIssues(this.issues);
		ManagedFunctionLoader workLoader = compiler.getWorkLoader();
		MockWorkSource.loader = loader;
		FunctionNamespaceType<Work> workType = workLoader.loadManagedFunctionType(MockWorkSource.class,
				propertyList);

		// Verify the mock objects
		this.verifyMockObjects();

		// Ensure if should be loaded
		if (isExpectedToLoad) {
			assertNotNull("Expected to load the work type", workType);
		} else {
			assertNull("Should not load the work type", workType);
		}

		// Return the work type
		return workType;
	}

	/**
	 * Implemented to load the {@link FunctionNamespaceType}.
	 */
	private interface Loader {

		/**
		 * Implemented to load the {@link FunctionNamespaceType}.
		 * 
		 * @param work
		 *            {@link FunctionNamespaceBuilder}.
		 * @param context
		 *            {@link ManagedFunctionSourceContext}.
		 * @throws Exception
		 *             If fails to source {@link FunctionNamespaceType}.
		 */
		void sourceWork(FunctionNamespaceBuilder<Work> work, ManagedFunctionSourceContext context)
				throws Exception;
	}

	/**
	 * Mock {@link ManagedFunctionSource} for testing.
	 */
	@TestSource
	public static class MockWorkSource implements ManagedFunctionSource<Work> {

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
		public MockWorkSource() {
			if (instantiateFailure != null) {
				throw instantiateFailure;
			}
		}

		/*
		 * ================ WorkSource ======================================
		 */

		@Override
		public ManagedFunctionSourceSpecification getSpecification() {
			fail("Should not be invoked in obtaining work type");
			return null;
		}

		@Override
		public void sourceManagedFunctions(FunctionNamespaceBuilder<Work> workTypeBuilder,
				ManagedFunctionSourceContext context) throws Exception {
			loader.sourceWork(workTypeBuilder, context);
		}
	}

}