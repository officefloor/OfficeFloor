/*-
 * #%L
 * OfficeCompiler
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package net.officefloor.compile.impl.managedobject;

import java.net.URLConnection;
import java.sql.Connection;
import java.util.Properties;
import java.util.logging.Logger;

import javax.transaction.xa.XAResource;

import net.officefloor.compile.FailServiceFactory;
import net.officefloor.compile.MissingServiceFactory;
import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.impl.properties.PropertyListImpl;
import net.officefloor.compile.issues.CompilerIssues;
import net.officefloor.compile.managedfunction.FunctionNamespaceType;
import net.officefloor.compile.managedobject.ManagedObjectDependencyType;
import net.officefloor.compile.managedobject.ManagedObjectExecutionStrategyType;
import net.officefloor.compile.managedobject.ManagedObjectFlowType;
import net.officefloor.compile.managedobject.ManagedObjectFunctionDependencyType;
import net.officefloor.compile.managedobject.ManagedObjectLoader;
import net.officefloor.compile.managedobject.ManagedObjectTeamType;
import net.officefloor.compile.managedobject.ManagedObjectType;
import net.officefloor.compile.properties.Property;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.test.issues.FailTestCompilerIssues;
import net.officefloor.compile.test.issues.MockCompilerIssues;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.function.ManagedFunctionFactory;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.extension.ExtensionFactory;
import net.officefloor.frame.api.managedobject.source.ManagedObjectDependencyMetaData;
import net.officefloor.frame.api.managedobject.source.ManagedObjectExecuteContext;
import net.officefloor.frame.api.managedobject.source.ManagedObjectExecutionMetaData;
import net.officefloor.frame.api.managedobject.source.ManagedObjectExtensionMetaData;
import net.officefloor.frame.api.managedobject.source.ManagedObjectFlowMetaData;
import net.officefloor.frame.api.managedobject.source.ManagedObjectFunctionBuilder;
import net.officefloor.frame.api.managedobject.source.ManagedObjectFunctionDependency;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSourceContext;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSourceMetaData;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSourceSpecification;
import net.officefloor.frame.api.managedobject.source.ManagedObjectUser;
import net.officefloor.frame.api.source.TestSource;
import net.officefloor.frame.api.team.Team;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.ManagedObjectMetaData;
import net.officefloor.frame.test.Closure;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.managedobject.clazz.ClassManagedObjectSource;

/**
 * Tests loading the {@link ManagedObjectType}.
 * 
 * @author Daniel Sagenschneider
 */
public class LoadManagedObjectTypeTest extends OfficeFrameTestCase {

	/**
	 * {@link CompilerIssues}.
	 */
	private final MockCompilerIssues issues = new MockCompilerIssues(this);

	/**
	 * {@link ManagedObjectSourceMetaData}.
	 */
	private final ManagedObjectSourceMetaData<?, ?> metaData = this.createMock(ManagedObjectSourceMetaData.class);

	/**
	 * {@link ManagedFunctionFactory}.
	 */
	private final ManagedFunctionFactory<?, ?> functionFactory = this.createMock(ManagedFunctionFactory.class);

	@Override
	protected void setUp() throws Exception {
		MockManagedObjectSource.reset(this.metaData, new InitUtil());
	}

	/**
	 * Ensure can load {@link ManagedObject} via {@link ClassManagedObjectSource}
	 * {@link Class}.
	 */
	@SuppressWarnings("rawtypes")
	public void testLoadByClass() {

		// Configure test
		OfficeFloorCompiler compiler = OfficeFloorCompiler.newOfficeFloorCompiler(null);
		compiler.setCompilerIssues(new FailTestCompilerIssues());

		// Configure to load simple class
		PropertyList properties = compiler.createPropertyList();
		properties.addProperty(ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME)
				.setValue(MockLoadManagedObject.class.getName());

		// Load the managed object type
		ManagedObjectLoader moLoader = compiler.getManagedObjectLoader();
		ManagedObjectType moType = moLoader.loadManagedObjectType(ClassManagedObjectSource.class, properties);
		MockLoadManagedObject.assertManagedObjectType(moType);
	}

	/**
	 * Ensure can load {@link ManagedObject} via {@link ClassManagedObjectSource}
	 * instance.
	 */
	@SuppressWarnings("rawtypes")
	public void testLoadByInstance() {

		// Configure test
		OfficeFloorCompiler compiler = OfficeFloorCompiler.newOfficeFloorCompiler(null);
		compiler.setCompilerIssues(new FailTestCompilerIssues());

		// Configure to load simple class
		PropertyList properties = compiler.createPropertyList();
		properties.addProperty(ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME)
				.setValue(MockLoadManagedObject.class.getName());

		// Load the managed object type
		ManagedObjectLoader moLoader = compiler.getManagedObjectLoader();
		ManagedObjectType moType = moLoader.loadManagedObjectType(new ClassManagedObjectSource(), properties);
		MockLoadManagedObject.assertManagedObjectType(moType);
	}

	/**
	 * Ensure issue if fail to instantiate the {@link ManagedObjectSource}.
	 */
	public void testFailInstantiate() {

		final RuntimeException failure = new RuntimeException("instantiate failure");

		// Record failure to instantiate
		this.issues.recordIssue(
				"Failed to instantiate " + MockManagedObjectSource.class.getName() + " by default constructor",
				failure);

		// Attempt to load
		MockManagedObjectSource.instantiateFailure = failure;
		this.loadManagedObjectType(false, (Init<?>) null);
	}

	/**
	 * Ensure issue if missing {@link Property}.
	 */
	public void testMissingProperty() {

		// Record missing property
		this.issues.recordIssue("Must specify property 'missing'");

		// Attempt to load
		this.loadManagedObjectType(false, (context, util) -> {
			context.getProperty("missing");
		});
	}

	/**
	 * Ensure able to get properties.
	 */
	public void testGetProperties() {

		// Record basic meta-data
		this.record_basicMetaData();

		// Attempt to load
		this.loadManagedObjectType(true, (context, util) -> {
			assertEquals("Ensure get defaulted property", "DEFAULT", context.getProperty("missing", "DEFAULT"));
			assertEquals("Ensure get property ONE", "1", context.getProperty("ONE"));
			assertEquals("Ensure get property TWO", "2", context.getProperty("TWO"));
			Properties properties = context.getProperties();
			assertEquals("Incorrect number of properties", 2, properties.size());
			assertEquals("Incorrect property ONE", "1", properties.get("ONE"));
			assertEquals("Incorrect property TWO", "2", properties.get("TWO"));
		}, "ONE", "1", "TWO", "2");
	}

	/**
	 * Ensure correctly named {@link Logger}.
	 */
	public void testLogger() {

		// Record basic meta-data
		this.record_basicMetaData();

		// Ensure correctly named logger
		Closure<String> loggerName = new Closure<>();
		this.loadManagedObjectType(true, (context, util) -> {
			loggerName.value = context.getLogger().getName();
		});
		assertEquals("Incorrect logger name", OfficeFloorCompiler.TYPE, loggerName.value);
	}

	/**
	 * Ensure issue if missing {@link Class}.
	 */
	public void testMissingClass() {

		// Record missing class
		this.issues.recordIssue("Can not load class 'missing'");

		// Attempt to load
		this.loadManagedObjectType(false, (context, util) -> {
			context.loadClass("missing");
		});
	}

	/**
	 * Ensure issue if missing resource.
	 */
	public void testMissingResource() {

		// Record missing resource
		this.issues.recordIssue("Can not obtain resource at location 'missing'");

		// Attempt to load
		this.loadManagedObjectType(false, (context, util) -> {
			context.getResource("missing");
		});
	}

	/**
	 * Ensure able to get resource.
	 */
	public void testGetResource() {

		// Record basic meta-data
		this.record_basicMetaData();

		// Obtain path
		final String objectPath = Object.class.getName().replace('.', '/') + ".class";

		// Attempt to load
		this.loadManagedObjectType(true, (context, util) -> {
			assertEquals("Incorrect resource locator",
					LoadManagedObjectTypeTest.class.getClassLoader().getResource(objectPath),
					context.getClassLoader().getResource(objectPath));
		});
	}

	/**
	 * Ensure issue if missing service.
	 */
	public void testMissingService() {

		// Record missing service
		this.issues.recordIssue(MissingServiceFactory.getIssueDescription());

		// Attempt to load
		this.loadManagedObjectType(false, (context, util) -> context.loadService(MissingServiceFactory.class, null));
	}

	/**
	 * Ensure issue if fail to load service.
	 */
	public void testFailLoadService() {

		// Record load issue for service
		this.issues.recordIssue(FailServiceFactory.getIssueDescription(), FailServiceFactory.getCause());

		// Attempt to load
		this.loadManagedObjectType(false, (context, util) -> context.loadService(FailServiceFactory.class, null));
	}

	/**
	 * Ensure issue if fails to init the {@link ManagedObjectSource}.
	 */
	public void testFailInitManagedObjectSource() {

		final NullPointerException failure = new NullPointerException("Fail init ManagedObjectSource");

		// Record failure to init the Managed Object Source
		this.issues.recordIssue("Failed to init", failure);

		// Attempt to load
		this.loadManagedObjectType(false, (context, util) -> {
			throw failure;
		});
	}

	/**
	 * Ensure issue if <code>null</code> {@link ManagedObjectSourceMetaData}.
	 */
	public void testNullManagedObjectSourceMetaData() {

		// Record null the Managed Object Source meta-data
		this.issues.recordIssue("Returned null ManagedObjectSourceMetaData");

		// Attempt to load
		this.loadManagedObjectType(false, (context, util) -> {
			MockManagedObjectSource.metaData = null;
		});
	}

	/**
	 * Ensure issue if fails to obtain the {@link ManagedObjectSourceMetaData}.
	 */
	public void testFailGetManagedObjectSourceMetaData() {

		final Error failure = new Error("Obtain meta-data failure");

		// Record failure to obtain the meta-data
		this.issues.recordIssue("Failed to init", failure);

		// Attempt to load
		this.loadManagedObjectType(false, (context, util) -> {
			MockManagedObjectSource.metaDataFailure = failure;
		});
	}

	/**
	 * Ensure issue if no {@link Object} class from meta-data.
	 */
	public void testNoObjectClass() {

		// Record no object class
		this.recordReturn(this.metaData, this.metaData.getObjectClass(), null);
		this.issues.recordIssue("No Object type provided");

		// Attempt to load
		this.loadManagedObjectType(false, (Init<None>) null);
	}

	/**
	 * Ensure issue if no {@link ManagedObject} class from meta-data.
	 */
	public void testNoManagedObjectClass() {

		// Record no managed object class
		this.recordReturn(this.metaData, this.metaData.getObjectClass(), Object.class);
		this.recordReturn(this.metaData, this.metaData.getManagedObjectClass(), null);
		this.issues.recordIssue("No ManagedObject type provided");

		// Attempt to load
		this.loadManagedObjectType(false, (Init<None>) null);
	}

	/**
	 * Ensure issue if no {@link ManagedObject} class does not implement
	 * {@link ManagedObject}.
	 */
	public void testManagedObjectClassMustImplementManagedObject() {

		// Record invalid managed object class
		this.recordReturn(this.metaData, this.metaData.getObjectClass(), Object.class);
		this.recordReturn(this.metaData, this.metaData.getManagedObjectClass(), Integer.class);
		this.issues.recordIssue("ManagedObject class must implement " + ManagedObject.class.getName() + " (class="
				+ Integer.class.getName() + ")");

		// Attempt to load
		this.loadManagedObjectType(false, (Init<None>) null);
	}

	/**
	 * Ensure issue if <code>null</code> {@link ManagedObjectDependencyMetaData} in
	 * array.
	 */
	public void testNullDependencyMetaData() {

		// Record no dependency type
		this.record_objectAndManagedObject();
		this.recordReturn(this.metaData, this.metaData.getDependencyMetaData(),
				new ManagedObjectDependencyMetaData[] { null });
		this.issues.recordIssue("Null ManagedObjectDependencyMetaData for dependency 0");

		// Attempt to load
		this.loadManagedObjectType(false, (Init<None>) null);
	}

	/**
	 * Ensure issue if no dependency type.
	 */
	public void testNoDependencyType() {

		final ManagedObjectDependencyMetaData<?> dependency = this.createMock(ManagedObjectDependencyMetaData.class);

		// Record no dependency type
		this.record_objectAndManagedObject();
		this.recordReturn(this.metaData, this.metaData.getDependencyMetaData(),
				new ManagedObjectDependencyMetaData[] { dependency });
		this.recordReturn(dependency, dependency.getLabel(), "NO TYPE");
		this.recordReturn(dependency, dependency.getKey(), null);
		this.recordReturn(dependency, dependency.getType(), null);
		this.issues.recordIssue("No type for dependency 0 (key=<indexed>, label=NO TYPE)");

		// Attempt to load
		this.loadManagedObjectType(false, (Init<None>) null);
	}

	/**
	 * Ensure issue if dependency keys of different types.
	 */
	public void testInvalidDependencyKey() {

		final ManagedObjectDependencyMetaData<?> dependencyOne = this.createMock(ManagedObjectDependencyMetaData.class);
		final ManagedObjectDependencyMetaData<?> dependencyTwo = this.createMock(ManagedObjectDependencyMetaData.class);

		// Record missing dependency key
		this.record_objectAndManagedObject();
		this.recordReturn(this.metaData, this.metaData.getDependencyMetaData(),
				new ManagedObjectDependencyMetaData[] { dependencyOne, dependencyTwo });
		this.recordReturn(dependencyOne, dependencyOne.getLabel(), null);
		this.recordReturn(dependencyOne, dependencyOne.getKey(), TwoKey.ONE);
		this.recordReturn(dependencyOne, dependencyOne.getType(), Connection.class);
		this.recordReturn(dependencyOne, dependencyOne.getTypeQualifier(), null);
		this.recordReturn(dependencyOne, dependencyOne.getAnnotations(), null);
		this.recordReturn(dependencyTwo, dependencyTwo.getLabel(), null);
		this.recordReturn(dependencyTwo, dependencyTwo.getKey(), InvalidKey.INVALID);
		this.issues.recordIssue("Dependencies identified by different key types (" + TwoKey.class.getName() + ", "
				+ InvalidKey.class.getName() + ")");

		// Attempt to load
		this.loadManagedObjectType(false, (Init<None>) null);
	}

	/**
	 * Ensure issue if dependency mixing using keys and indexes.
	 */
	public void testDependencyMixingKeyAndIndexes() {

		final ManagedObjectDependencyMetaData<?> dependencyOne = this.createMock(ManagedObjectDependencyMetaData.class);
		final ManagedObjectDependencyMetaData<?> dependencyTwo = this.createMock(ManagedObjectDependencyMetaData.class);

		// Record missing dependency key
		this.record_objectAndManagedObject();
		this.recordReturn(this.metaData, this.metaData.getDependencyMetaData(),
				new ManagedObjectDependencyMetaData[] { dependencyOne, dependencyTwo });
		this.recordReturn(dependencyOne, dependencyOne.getLabel(), null);
		this.recordReturn(dependencyOne, dependencyOne.getKey(), TwoKey.ONE);
		this.recordReturn(dependencyOne, dependencyOne.getType(), Connection.class);
		this.recordReturn(dependencyOne, dependencyOne.getTypeQualifier(), null);
		this.recordReturn(dependencyOne, dependencyOne.getAnnotations(), null);
		this.recordReturn(dependencyTwo, dependencyTwo.getLabel(), null);
		this.recordReturn(dependencyTwo, dependencyTwo.getKey(), null);
		this.issues.recordIssue("Dependencies mixing keys and indexes");

		// Attempt to load
		this.loadManagedObjectType(false, (Init<None>) null);
	}

	/**
	 * Ensure issue if key used more than once for a dependency.
	 */
	public void testDuplicateDependencyKey() {

		final ManagedObjectDependencyMetaData<?> dependencyOne = this.createMock(ManagedObjectDependencyMetaData.class);
		final ManagedObjectDependencyMetaData<?> dependencyTwo = this.createMock(ManagedObjectDependencyMetaData.class);

		// Record missing dependency key
		this.record_objectAndManagedObject();
		this.recordReturn(this.metaData, this.metaData.getDependencyMetaData(),
				new ManagedObjectDependencyMetaData[] { dependencyOne, dependencyTwo });
		this.recordReturn(dependencyOne, dependencyOne.getLabel(), null);
		this.recordReturn(dependencyOne, dependencyOne.getKey(), TwoKey.ONE);
		this.recordReturn(dependencyOne, dependencyOne.getType(), Connection.class);
		this.recordReturn(dependencyOne, dependencyOne.getTypeQualifier(), null);
		this.recordReturn(dependencyOne, dependencyOne.getAnnotations(), null);
		this.recordReturn(dependencyTwo, dependencyTwo.getLabel(), null);
		this.recordReturn(dependencyTwo, dependencyTwo.getKey(), TwoKey.ONE);
		this.recordReturn(dependencyTwo, dependencyTwo.getType(), String.class);
		this.recordReturn(dependencyTwo, dependencyTwo.getTypeQualifier(), null);
		this.recordReturn(dependencyTwo, dependencyTwo.getAnnotations(), null);
		this.issues.recordIssue("Must have exactly one dependency per key (key=" + TwoKey.ONE + ")");

		// Attempt to load
		this.loadManagedObjectType(false, (Init<None>) null);
	}

	/**
	 * Ensure issue if not have meta-data for each dependency key.
	 */
	public void testNotAllDependencyKeys() {

		final ManagedObjectDependencyMetaData<?> dependency = this.createMock(ManagedObjectDependencyMetaData.class);

		// Record not all dependency keys
		this.record_objectAndManagedObject();
		this.recordReturn(this.metaData, this.metaData.getDependencyMetaData(),
				new ManagedObjectDependencyMetaData[] { dependency });
		this.recordReturn(dependency, dependency.getLabel(), null);
		this.recordReturn(dependency, dependency.getKey(), TwoKey.ONE);
		this.recordReturn(dependency, dependency.getType(), Connection.class);
		this.recordReturn(dependency, dependency.getTypeQualifier(), null);
		this.recordReturn(dependency, dependency.getAnnotations(), null);
		this.issues.recordIssue("Missing dependency meta-data (keys=" + TwoKey.TWO + ")");

		// Attempt to load
		this.loadManagedObjectType(false, (Init<None>) null);
	}

	/**
	 * Ensure issue if <code>null</code> or blank
	 * {@link ManagedObjectFunctionDependency} name.
	 */
	public void testNullFunctionDependencyName() {

		// Record basic meta-data
		this.issues.recordIssue("Failed to init",
				new IllegalArgumentException("Must provide ManagedObjectFunctionDependency name"));

		// Attempt to load
		this.loadManagedObjectType(false, (context, util) -> {
			context.addFunctionDependency(null, String.class);
		});
	}

	/**
	 * Ensure issue if <code>null</code> {@link ManagedObjectFunctionDependency}
	 * type.
	 */
	public void testNullFunctionDependencyType() {

		// Record basic meta-data
		this.issues.recordIssue("Failed to init",
				new IllegalArgumentException("Must provide ManagedObjectFunctionDependency type"));

		// Attempt to load
		this.loadManagedObjectType(false, (context, util) -> {
			context.addFunctionDependency("DEPENDENCY", null);
		});
	}

	/**
	 * Ensure issue if duplicate {@link ManagedObjectFunctionDependency} name.
	 */
	public void testDuplicateFunctionDependencyName() {

		// Record basic meta-data
		this.record_basicMetaData();
		this.issues.recordIssue("Two ManagedObjectFunctionDependency instances added by same name 'DEPENDENCY'");

		// Attempt to load
		this.loadManagedObjectType(false, (context, util) -> {
			context.addFunctionDependency("DEPENDENCY", String.class);
			context.addFunctionDependency("DEPENDENCY", Connection.class);
		});
	}

	/**
	 * Ensure issue of the {@link ManagedObjectDependencyMetaData} name clashes with
	 * the {@link ManagedObjectFunctionDependency} name.
	 */
	public void testDependencyNameClashBetweenMetaDataAndFunction() {

		final ManagedObjectDependencyMetaData<?> dependency = this.createMock(ManagedObjectDependencyMetaData.class);

		// Record missing dependency key
		this.record_objectAndManagedObject();
		this.recordReturn(this.metaData, this.metaData.getDependencyMetaData(),
				new ManagedObjectDependencyMetaData[] { dependency });
		this.recordReturn(dependency, dependency.getLabel(), "DEPENDENCY");
		this.recordReturn(dependency, dependency.getKey(), null);
		this.recordReturn(dependency, dependency.getType(), Connection.class);
		this.recordReturn(dependency, dependency.getTypeQualifier(), null);
		this.recordReturn(dependency, dependency.getAnnotations(), null);
		this.recordReturn(this.metaData, this.metaData.getFlowMetaData(), new ManagedObjectFlowMetaData[0]);
		this.recordReturn(this.metaData, this.metaData.getExecutionMetaData(), null);
		this.recordReturn(this.metaData, this.metaData.getExtensionInterfacesMetaData(), null);
		this.issues.recordIssue("Name clash 'DEPENDENCY' between meta-data dependency and function dependency");

		// Attempt to load
		this.loadManagedObjectType(false, (context, util) -> {
			context.addFunctionDependency("DEPENDENCY", String.class);
		});
	}

	/**
	 * Ensure able to provide only {@link ManagedObjectFunctionDependency} (and no
	 * other dependencies).
	 */
	public void testOnlyFunctionDependency() {

		// Record basic meta-data
		this.record_basicMetaData();

		// Load the managed object type
		ManagedObjectType<?> type = this.loadManagedObjectType(true, (context, util) -> {
			context.addFunctionDependency("DEPENDENCY", Connection.class);
		});

		// Ensure has the function dependency
		assertEquals("Should be no managed object dependencies", 0, type.getDependencyTypes().length);
		ManagedObjectFunctionDependencyType[] functionDependencies = type.getFunctionDependencyTypes();
		assertEquals("Should have function dependency", 1, functionDependencies.length);
		ManagedObjectFunctionDependencyType functionDependency = functionDependencies[0];
		assertEquals("Incorrect function dependency name", "DEPENDENCY", functionDependency.getFunctionObjectName());
		assertEquals("Incorrect function dependency type", Connection.class,
				functionDependency.getFunctionObjectType());
	}

	/**
	 * Ensure able to provide qualified {@link ManagedObjectFunctionDependency}.
	 */
	public void testQualifiedFunctionDependency() {

		// Record basic meta-data
		this.record_basicMetaData();

		// Load the managed object type
		ManagedObjectType<?> type = this.loadManagedObjectType(true, (context, util) -> {
			context.addFunctionDependency("DEPENDENCY", Connection.class).setTypeQualifier("QUALIFIER");
		});

		// Ensure has the qualified function dependency
		assertEquals("Should be no managed object dependencies", 0, type.getDependencyTypes().length);
		ManagedObjectFunctionDependencyType[] functionDependencies = type.getFunctionDependencyTypes();
		assertEquals("Should have function dependency", 1, functionDependencies.length);
		ManagedObjectFunctionDependencyType functionDependency = functionDependencies[0];
		assertEquals("Incorrect function dependency name", "DEPENDENCY", functionDependency.getFunctionObjectName());
		assertEquals("Incorrect function dependency type", Connection.class,
				functionDependency.getFunctionObjectType());
		assertEquals("Incorrect function dependency qualifier", "QUALIFIER",
				functionDependency.getFunctionObjectTypeQualifier());
	}

	/**
	 * Ensure issue if <code>null</code> {@link ManagedObjectFlowMetaData} in array.
	 */
	public void testNullFlowMetaData() {

		// Record no flow type
		this.record_objectAndManagedObject();
		this.recordReturn(this.metaData, this.metaData.getDependencyMetaData(), null);
		this.recordReturn(this.metaData, this.metaData.getFlowMetaData(), new ManagedObjectFlowMetaData[] { null });
		this.issues.recordIssue("Null ManagedObjectFlowMetaData for flow 0");

		// Attempt to load
		this.loadManagedObjectType(false, (Init<None>) null);
	}

	/**
	 * Ensure able to have no flow argument type.
	 */
	public void testNoFlowArgumentType() {

		final ManagedObjectFlowMetaData<?> flowDefaulted = this.createMock(ManagedObjectFlowMetaData.class);
		final ManagedObjectFlowMetaData<?> flowProvided = this.createMock(ManagedObjectFlowMetaData.class);

		// Record no flow argument type
		this.record_objectAndManagedObject();
		this.recordReturn(this.metaData, this.metaData.getDependencyMetaData(), null);
		this.recordReturn(this.metaData, this.metaData.getFlowMetaData(),
				new ManagedObjectFlowMetaData[] { flowDefaulted, flowProvided });
		this.recordReturn(flowDefaulted, flowDefaulted.getLabel(), "NO_ARGUMENT");
		this.recordReturn(flowDefaulted, flowDefaulted.getKey(), null);
		this.recordReturn(flowDefaulted, flowDefaulted.getArgumentType(), null);
		this.recordReturn(flowProvided, flowProvided.getLabel(), null);
		this.recordReturn(flowProvided, flowProvided.getKey(), null);
		this.recordReturn(flowProvided, flowProvided.getArgumentType(), Connection.class);
		this.recordReturn(this.metaData, this.metaData.getExecutionMetaData(), null);
		this.recordReturn(this.metaData, this.metaData.getExtensionInterfacesMetaData(), null);

		// Attempt to load
		ManagedObjectType<?> moType = this.loadManagedObjectType(true, (Init<None>) null);

		// Validate argument types of flows
		ManagedObjectFlowType<?>[] flowTypes = moType.getFlowTypes();
		assertEquals("Incorrect number of flows", 2, flowTypes.length);
		ManagedObjectFlowType<?> defaulted = flowTypes[0];
		assertEquals("Incorrect name for defaulted argument flow", "NO_ARGUMENT", defaulted.getFlowName());
		assertNull("Incorrect no argument type", defaulted.getArgumentType());
		ManagedObjectFlowType<?> provided = flowTypes[1];
		assertEquals("Incorrect name for provided argument flow", "1", provided.getFlowName());
		assertEquals("Incorrect provided argument type", Connection.class, provided.getArgumentType());
	}

	/**
	 * Ensure issue if flow keys of different types.
	 */
	public void testInvalidFlowKey() {

		final ManagedObjectFlowMetaData<?> flowOne = this.createMock(ManagedObjectFlowMetaData.class);
		final ManagedObjectFlowMetaData<?> flowTwo = this.createMock(ManagedObjectFlowMetaData.class);

		// Record missing flow key
		this.record_objectAndManagedObject();
		this.recordReturn(this.metaData, this.metaData.getDependencyMetaData(), null);
		this.recordReturn(this.metaData, this.metaData.getFlowMetaData(),
				new ManagedObjectFlowMetaData[] { flowOne, flowTwo });
		this.recordReturn(flowOne, flowOne.getLabel(), null);
		this.recordReturn(flowOne, flowOne.getKey(), TwoKey.ONE);
		this.recordReturn(flowOne, flowOne.getArgumentType(), Connection.class);
		this.recordReturn(flowTwo, flowTwo.getLabel(), null);
		this.recordReturn(flowTwo, flowTwo.getKey(), InvalidKey.INVALID);
		this.issues.recordIssue("Meta-data flows identified by different key types (" + TwoKey.class.getName() + ", "
				+ InvalidKey.class.getName() + ")");

		// Attempt to load
		this.loadManagedObjectType(false, (Init<None>) null);
	}

	/**
	 * Ensure issue if flow mixing using keys and indexes.
	 */
	public void testFlowMixingKeyAndIndexes() {

		final ManagedObjectFlowMetaData<?> flowOne = this.createMock(ManagedObjectFlowMetaData.class);
		final ManagedObjectFlowMetaData<?> flowTwo = this.createMock(ManagedObjectFlowMetaData.class);

		// Record missing flow key
		this.record_objectAndManagedObject();
		this.recordReturn(this.metaData, this.metaData.getDependencyMetaData(), null);
		this.recordReturn(this.metaData, this.metaData.getFlowMetaData(),
				new ManagedObjectFlowMetaData[] { flowOne, flowTwo });
		this.recordReturn(flowOne, flowOne.getLabel(), null);
		this.recordReturn(flowOne, flowOne.getKey(), TwoKey.ONE);
		this.recordReturn(flowOne, flowOne.getArgumentType(), Connection.class);
		this.recordReturn(flowTwo, flowTwo.getLabel(), null);
		this.recordReturn(flowTwo, flowTwo.getKey(), null);
		this.issues.recordIssue("Meta-data flows mixing keys and indexes");

		// Attempt to load
		this.loadManagedObjectType(false, (Init<None>) null);
	}

	/**
	 * Ensure issue if key used more than once for a flow.
	 */
	public void testDuplicateFlowKey() {

		final ManagedObjectFlowMetaData<?> flowOne = this.createMock(ManagedObjectFlowMetaData.class);
		final ManagedObjectFlowMetaData<?> flowTwo = this.createMock(ManagedObjectFlowMetaData.class);

		// Record missing flow key
		this.record_objectAndManagedObject();
		this.recordReturn(this.metaData, this.metaData.getDependencyMetaData(), null);
		this.recordReturn(this.metaData, this.metaData.getFlowMetaData(),
				new ManagedObjectFlowMetaData[] { flowOne, flowTwo });
		this.recordReturn(flowOne, flowOne.getLabel(), null);
		this.recordReturn(flowOne, flowOne.getKey(), TwoKey.ONE);
		this.recordReturn(flowOne, flowOne.getArgumentType(), Connection.class);
		this.recordReturn(flowTwo, flowTwo.getLabel(), null);
		this.recordReturn(flowTwo, flowTwo.getKey(), TwoKey.ONE);
		this.recordReturn(flowTwo, flowTwo.getArgumentType(), String.class);
		this.issues.recordIssue("Must have exactly one flow per key (key=" + TwoKey.ONE + ")");

		// Attempt to load
		this.loadManagedObjectType(false, (Init<None>) null);
	}

	/**
	 * Ensure issue if not have meta-data for each flow key.
	 */
	public void testNotAllFlowKeys() {

		final ManagedObjectFlowMetaData<?> flow = this.createMock(ManagedObjectFlowMetaData.class);

		// Record not all flow keys
		this.record_objectAndManagedObject();
		this.recordReturn(this.metaData, this.metaData.getDependencyMetaData(), null);
		this.recordReturn(this.metaData, this.metaData.getFlowMetaData(), new ManagedObjectFlowMetaData[] { flow });
		this.recordReturn(flow, flow.getLabel(), null);
		this.recordReturn(flow, flow.getKey(), TwoKey.ONE);
		this.recordReturn(flow, flow.getArgumentType(), Connection.class);
		this.issues.recordIssue("Missing flow meta-data (keys=" + TwoKey.TWO + ")");

		// Attempt to load
		this.loadManagedObjectType(false, (Init<None>) null);
	}

	/**
	 * Ensure {@link ManagedObjectType} correct with keyed dependencies and flows.
	 */
	public void testKeyedDependenciesFunctionDependenciesAndFlows() {

		final ManagedObjectDependencyMetaData<?> dependencyOne = this.createMock(ManagedObjectDependencyMetaData.class);
		final ManagedObjectDependencyMetaData<?> dependencyTwo = this.createMock(ManagedObjectDependencyMetaData.class);
		final ManagedObjectFlowMetaData<?> flowOne = this.createMock(ManagedObjectFlowMetaData.class);
		final ManagedObjectFlowMetaData<?> flowTwo = this.createMock(ManagedObjectFlowMetaData.class);

		// Record keyed dependencies and flows
		this.record_objectAndManagedObject();
		this.recordReturn(this.metaData, this.metaData.getDependencyMetaData(),
				new ManagedObjectDependencyMetaData[] { dependencyOne, dependencyTwo });
		this.recordReturn(dependencyOne, dependencyOne.getLabel(), null);
		this.recordReturn(dependencyOne, dependencyOne.getKey(), TwoKey.TWO); // order
		this.recordReturn(dependencyOne, dependencyOne.getType(), Integer.class);
		this.recordReturn(dependencyOne, dependencyOne.getTypeQualifier(), "QUALIFIED");
		this.recordReturn(dependencyOne, dependencyOne.getAnnotations(), null);
		this.recordReturn(dependencyTwo, dependencyTwo.getLabel(), null);
		this.recordReturn(dependencyTwo, dependencyTwo.getKey(), TwoKey.ONE); // order
		this.recordReturn(dependencyTwo, dependencyTwo.getType(), Connection.class);
		this.recordReturn(dependencyTwo, dependencyTwo.getTypeQualifier(), null);
		this.recordReturn(dependencyTwo, dependencyTwo.getAnnotations(), null);
		this.recordReturn(this.metaData, this.metaData.getFlowMetaData(),
				new ManagedObjectFlowMetaData[] { flowOne, flowTwo });
		this.recordReturn(flowOne, flowOne.getLabel(), null);
		this.recordReturn(flowOne, flowOne.getKey(), TwoKey.TWO); // order
		this.recordReturn(flowOne, flowOne.getArgumentType(), Long.class);
		this.recordReturn(flowTwo, flowTwo.getLabel(), null);
		this.recordReturn(flowTwo, flowTwo.getKey(), TwoKey.ONE); // order
		this.recordReturn(flowTwo, flowTwo.getArgumentType(), String.class);
		this.recordReturn(this.metaData, this.metaData.getExecutionMetaData(), null);
		this.recordReturn(this.metaData, this.metaData.getExtensionInterfacesMetaData(), null);

		// Attempt to load
		ManagedObjectType<?> moType = this.loadManagedObjectType(true, (context, util) -> {
			context.addFunctionDependency("FUNCTION_DEPENDENCY", URLConnection.class);
		});

		// Validate dependencies ordered and correct values
		ManagedObjectDependencyType<?>[] dependencyTypes = moType.getDependencyTypes();
		assertEquals("Incorrect number of dependencies", 2, dependencyTypes.length);
		ManagedObjectDependencyType<?> dependencyTypeOne = dependencyTypes[0];
		assertEquals("Keys should be ordered", TwoKey.ONE, dependencyTypeOne.getKey());
		assertEquals("Incorrect first dependency index", TwoKey.ONE.ordinal(), dependencyTypeOne.getIndex());
		assertEquals("Incorrect first dependency name", TwoKey.ONE.toString(), dependencyTypeOne.getDependencyName());
		assertEquals("Incorrect first dependency type", Connection.class, dependencyTypeOne.getDependencyType());
		assertNull("First dependency type should not be qualified", dependencyTypeOne.getTypeQualifier());
		ManagedObjectDependencyType<?> dependencyTypeTwo = dependencyTypes[1];
		assertEquals("Keys should be ordered", TwoKey.TWO, dependencyTypeTwo.getKey());
		assertEquals("Incorrect second dependency index", TwoKey.TWO.ordinal(), dependencyTypeTwo.getIndex());
		assertEquals("Incorrect second dependency name", TwoKey.TWO.toString(), dependencyTypeTwo.getDependencyName());
		assertEquals("Incorrect second dependency type", Integer.class, dependencyTypeTwo.getDependencyType());
		assertEquals("Incorrect second dependency type qualification", "QUALIFIED",
				dependencyTypeTwo.getTypeQualifier());

		// Validate function dependencies
		ManagedObjectFunctionDependencyType[] functionDependencyTypes = moType.getFunctionDependencyTypes();
		assertEquals("Should have function dependency", 1, functionDependencyTypes.length);
		ManagedObjectFunctionDependencyType functionDependencyType = functionDependencyTypes[0];
		assertEquals("Incorrect function dependency name", "FUNCTION_DEPENDENCY",
				functionDependencyType.getFunctionObjectName());
		assertEquals("Incorrect function dependency type", URLConnection.class,
				functionDependencyType.getFunctionObjectType());

		// Validate flows ordered and correct values
		ManagedObjectFlowType<?>[] flowTypes = moType.getFlowTypes();
		assertEquals("Incorrect number of dependencies", 2, flowTypes.length);
		ManagedObjectFlowType<?> flowTypeOne = flowTypes[0];
		assertEquals("Keys should be ordered", TwoKey.ONE, flowTypeOne.getKey());
		assertEquals("Incorrect first flow index", TwoKey.ONE.ordinal(), flowTypeOne.getIndex());
		assertEquals("Incorrect first flow name", TwoKey.ONE.toString(), flowTypeOne.getFlowName());
		assertEquals("Incorrect first flow argument type", String.class, flowTypeOne.getArgumentType());
		ManagedObjectFlowType<?> flowTypeTwo = flowTypes[1];
		assertEquals("Keys should be ordered", TwoKey.TWO, flowTypeTwo.getKey());
		assertEquals("Incorrect second flow index", TwoKey.TWO.ordinal(), flowTypeTwo.getIndex());
		assertEquals("Incorrect second flow name", TwoKey.TWO.toString(), flowTypeTwo.getFlowName());
		assertEquals("Incorrect second flow argument type", Long.class, flowTypeTwo.getArgumentType());
	}

	/**
	 * Ensure {@link ManagedObjectType} correct with indexed dependencies and flows.
	 */
	public void testIndexedDependenciesFunctionDependenciesAndFlows() {

		final ManagedObjectDependencyMetaData<?> dependencyOne = this.createMock(ManagedObjectDependencyMetaData.class);
		final ManagedObjectDependencyMetaData<?> dependencyTwo = this.createMock(ManagedObjectDependencyMetaData.class);
		final ManagedObjectFlowMetaData<?> flowOne = this.createMock(ManagedObjectFlowMetaData.class);
		final ManagedObjectFlowMetaData<?> flowTwo = this.createMock(ManagedObjectFlowMetaData.class);

		// Record keyed dependencies and flows
		this.record_objectAndManagedObject();
		this.recordReturn(this.metaData, this.metaData.getDependencyMetaData(),
				new ManagedObjectDependencyMetaData[] { dependencyOne, dependencyTwo });
		this.recordReturn(dependencyOne, dependencyOne.getLabel(), null);
		this.recordReturn(dependencyOne, dependencyOne.getKey(), null);
		this.recordReturn(dependencyOne, dependencyOne.getType(), Integer.class);
		this.recordReturn(dependencyOne, dependencyOne.getTypeQualifier(), "QUALIFIED");
		this.recordReturn(dependencyOne, dependencyOne.getAnnotations(), null);
		this.recordReturn(dependencyTwo, dependencyTwo.getLabel(), null);
		this.recordReturn(dependencyTwo, dependencyTwo.getKey(), null);
		this.recordReturn(dependencyTwo, dependencyTwo.getType(), Connection.class);
		this.recordReturn(dependencyTwo, dependencyTwo.getTypeQualifier(), null);
		this.recordReturn(dependencyTwo, dependencyTwo.getAnnotations(), null);
		this.recordReturn(this.metaData, this.metaData.getFlowMetaData(),
				new ManagedObjectFlowMetaData[] { flowOne, flowTwo });
		this.recordReturn(flowOne, flowOne.getLabel(), null);
		this.recordReturn(flowOne, flowOne.getKey(), null);
		this.recordReturn(flowOne, flowOne.getArgumentType(), Long.class);
		this.recordReturn(flowTwo, flowTwo.getLabel(), null);
		this.recordReturn(flowTwo, flowTwo.getKey(), null);
		this.recordReturn(flowTwo, flowTwo.getArgumentType(), String.class);
		this.recordReturn(this.metaData, this.metaData.getExecutionMetaData(), null);
		this.recordReturn(this.metaData, this.metaData.getExtensionInterfacesMetaData(), null);

		// Attempt to load
		ManagedObjectType<?> moType = this.loadManagedObjectType(true, (context, util) -> {
			context.addFunctionDependency("B", URLConnection.class);
			context.addFunctionDependency("A", Short.class).setTypeQualifier("QUALIFIER");
		});

		// Validate dependencies
		ManagedObjectDependencyType<?>[] dependencyTypes = moType.getDependencyTypes();
		assertEquals("Incorrect number of dependencies", 2, dependencyTypes.length);
		ManagedObjectDependencyType<?> dependencyTypeOne = dependencyTypes[0];
		assertEquals("Incorrect first dependency index", 0, dependencyTypeOne.getIndex());
		assertNull("Should be no dependency key", dependencyTypeOne.getKey());
		assertEquals("Incorrect first dependency name", Integer.class.getSimpleName(),
				dependencyTypeOne.getDependencyName());
		assertEquals("Incorrect first dependency type", Integer.class, dependencyTypeOne.getDependencyType());
		assertEquals("Incorrect first dependency type qualification", "QUALIFIED",
				dependencyTypeOne.getTypeQualifier());
		ManagedObjectDependencyType<?> dependencyTypeTwo = dependencyTypes[1];
		assertEquals("Incorrect second dependency index", 1, dependencyTypeTwo.getIndex());
		assertNull("Should be no dependency key", dependencyTypeTwo.getKey());
		assertEquals("Incorrect second dependency name", Connection.class.getSimpleName(),
				dependencyTypeTwo.getDependencyName());
		assertEquals("Incorrect second dependency type", Connection.class, dependencyTypeTwo.getDependencyType());
		assertNull("Second dependency should not be qualified", dependencyTypeTwo.getTypeQualifier());

		// Validate function dependencies (ordered by name)
		ManagedObjectFunctionDependencyType[] functionDependencyTypes = moType.getFunctionDependencyTypes();
		assertEquals("Should have function dependency", 2, functionDependencyTypes.length);
		ManagedObjectFunctionDependencyType functionDependencyTypeOne = functionDependencyTypes[0];
		assertEquals("Incorrect first function dependency name", "A",
				functionDependencyTypeOne.getFunctionObjectName());
		assertEquals("Incorrect first function dependency type", Short.class,
				functionDependencyTypeOne.getFunctionObjectType());
		assertEquals("Incorrect first function dependency type qualifier", "QUALIFIER",
				functionDependencyTypeOne.getFunctionObjectTypeQualifier());
		ManagedObjectFunctionDependencyType functionDependencyTypeTwo = functionDependencyTypes[1];
		assertEquals("Incorrect second function dependency name", "B",
				functionDependencyTypeTwo.getFunctionObjectName());
		assertEquals("Incorrect second function dependency type", URLConnection.class,
				functionDependencyTypeTwo.getFunctionObjectType());
		assertNull("Should be no qualifier for second function dependency type",
				functionDependencyTypeTwo.getFunctionObjectTypeQualifier());

		// Validate flows
		ManagedObjectFlowType<?>[] flowTypes = moType.getFlowTypes();
		assertEquals("Incorrect number of dependencies", 2, flowTypes.length);
		ManagedObjectFlowType<?> flowTypeOne = flowTypes[0];
		assertEquals("Incorrect first flow index", 0, flowTypeOne.getIndex());
		assertNull("Should be no flow key", flowTypeOne.getKey());
		assertEquals("Incorrect first flow name", "0", flowTypeOne.getFlowName());
		assertEquals("Incorrect first flow argument type", Long.class, flowTypeOne.getArgumentType());
		ManagedObjectFlowType<?> flowTypeTwo = flowTypes[1];
		assertEquals("Incorrect second flow index", 1, flowTypeTwo.getIndex());
		assertNull("Should be no dependency key", flowTypeTwo.getKey());
		assertEquals("Incorrect second flow name", "1", flowTypeTwo.getFlowName());
		assertEquals("Incorrect second flow argument type", String.class, flowTypeTwo.getArgumentType());
	}

	/**
	 * Ensure issue if <code>null</code> extension interface meta-data.
	 */
	public void testNullExtensionMetaData() {

		// Record null extension interface meta-data
		this.record_objectAndManagedObject();
		this.recordReturn(this.metaData, this.metaData.getDependencyMetaData(), null);
		this.recordReturn(this.metaData, this.metaData.getFlowMetaData(), null);
		this.recordReturn(this.metaData, this.metaData.getExecutionMetaData(), null);
		this.recordReturn(this.metaData, this.metaData.getExtensionInterfacesMetaData(),
				new ManagedObjectExtensionMetaData[] { null });
		this.issues.recordIssue("Null extension interface meta-data");

		// Attempt to load
		this.loadManagedObjectType(false, (Init<None>) null);
	}

	/**
	 * Ensure issue if <code>null</code> extension interface type.
	 */
	public void testNullExtensionType() {

		final ManagedObjectExtensionMetaData<?> eiMetaData = this.createMock(ManagedObjectExtensionMetaData.class);

		// Record null extension interface type
		this.record_objectAndManagedObject();
		this.recordReturn(this.metaData, this.metaData.getDependencyMetaData(), null);
		this.recordReturn(this.metaData, this.metaData.getFlowMetaData(), null);
		this.recordReturn(this.metaData, this.metaData.getExecutionMetaData(), null);
		this.recordReturn(this.metaData, this.metaData.getExtensionInterfacesMetaData(),
				new ManagedObjectExtensionMetaData[] { eiMetaData });
		this.recordReturn(eiMetaData, eiMetaData.getExtensionType(), null);
		this.issues.recordIssue("Null extension interface type");

		// Attempt to load
		this.loadManagedObjectType(false, (Init<None>) null);
	}

	/**
	 * Ensure issue if no {@link ExtensionFactory} for an extension.
	 */
	public void testNoFactoryForExtension() {

		final ManagedObjectExtensionMetaData<?> eiMetaData = this.createMock(ManagedObjectExtensionMetaData.class);

		// Record null extension interface factory
		this.record_objectAndManagedObject();
		this.recordReturn(this.metaData, this.metaData.getDependencyMetaData(), null);
		this.recordReturn(this.metaData, this.metaData.getFlowMetaData(), null);
		this.recordReturn(this.metaData, this.metaData.getExecutionMetaData(), null);
		this.recordReturn(this.metaData, this.metaData.getExtensionInterfacesMetaData(),
				new ManagedObjectExtensionMetaData[] { eiMetaData });
		this.recordReturn(eiMetaData, eiMetaData.getExtensionType(), XAResource.class);
		this.recordReturn(eiMetaData, eiMetaData.getExtensionFactory(), null);
		this.issues.recordIssue("No extension factory (type=" + XAResource.class.getName() + ")");

		// Attempt to load
		this.loadManagedObjectType(false, (Init<None>) null);
	}

	/**
	 * Ensure can define extension.
	 */
	public void testExtension() {

		final ManagedObjectExtensionMetaData<?> eiMetaData = this.createMock(ManagedObjectExtensionMetaData.class);
		final ExtensionFactory<?> factory = this.createMock(ExtensionFactory.class);

		// Record null extension interface factory
		this.record_objectAndManagedObject();
		this.recordReturn(this.metaData, this.metaData.getDependencyMetaData(), null);
		this.recordReturn(this.metaData, this.metaData.getFlowMetaData(), null);
		this.recordReturn(this.metaData, this.metaData.getExecutionMetaData(), null);
		this.recordReturn(this.metaData, this.metaData.getExtensionInterfacesMetaData(),
				new ManagedObjectExtensionMetaData[] { eiMetaData });
		this.recordReturn(eiMetaData, eiMetaData.getExtensionType(), XAResource.class);
		this.recordReturn(eiMetaData, eiMetaData.getExtensionFactory(), factory);

		// Attempt to load
		ManagedObjectType<?> moType = this.loadManagedObjectType(true, (Init<None>) null);

		// Ensure provide extension interface
		assertEquals("Incorrect number of extension interfaces", 1, moType.getExtensionTypes().length);
		assertEquals("Incorrect extension interface type", XAResource.class, moType.getExtensionTypes()[0]);
	}

	/**
	 * Ensure issue if {@link ManagedFunction} added without a
	 * {@link ManagedFunction} name.
	 */
	public void testAddFunctionWithNoFunctionName() {

		// Record no name for function
		this.record_basicMetaData();
		this.issues.recordIssue("Function added without a name");

		// Attempt to load
		this.loadManagedObjectType(false, (context, util) -> {
			context.addManagedFunction(null, util.getManagedFunctionFactory());
		});
	}

	/**
	 * Ensure issue if {@link ManagedFunction} added without a {@link Team}.
	 */
	public void testAddFunctionWithoutTeam() {

		// Record no team for function (use any team)
		this.record_basicMetaData();

		// Attempt to load
		ManagedObjectType<?> moType = this.loadManagedObjectType(true, (context, util) -> {
			context.addManagedFunction("FUNCTION", util.getManagedFunctionFactory());
		});

		// Ensure only team added
		assertFalse("Is input", moType.isInput());
		assertEquals("Should have no flows", 0, moType.getFlowTypes().length);
		assertEquals("Should have no teams", 0, moType.getTeamTypes().length);
	}

	/**
	 * Ensures {@link ManagedObjectTeamType} is available for the added
	 * {@link ManagedFunction}.
	 */
	public void testAddFunction() {

		// Record basic meta-data
		this.record_basicMetaData();

		// Attempt to load
		ManagedObjectType<?> moType = this.loadManagedObjectType(true, (context, util) -> {
			context.addManagedFunction("FUNCTION", util.getManagedFunctionFactory()).setResponsibleTeam("TEAM");
		});

		// Ensure only team added
		assertFalse("Is input", moType.isInput());
		assertEquals("Should have no flows", 0, moType.getFlowTypes().length);
		ManagedObjectTeamType[] teams = moType.getTeamTypes();
		assertEquals("Incorrect number of teams", 1, teams.length);
		assertEquals("Incorrect team", "TEAM", teams[0].getTeamName());
	}

	/**
	 * Ensures {@link ManagedObjectTeamType} is available for the added recycle
	 * {@link ManagedFunction}.
	 */
	public void testAddRecycleFunction() {

		// Record basic meta-data
		this.record_basicMetaData();

		// Attempt to load
		ManagedObjectType<?> moType = this.loadManagedObjectType(true, (context, util) -> {
			context.getRecycleFunction(util.getManagedFunctionFactory()).setResponsibleTeam("TEAM");
		});

		// Ensure only team added
		assertFalse("Not input", moType.isInput());
		assertEquals("Should have no flows", 0, moType.getFlowTypes().length);
		ManagedObjectTeamType[] teams = moType.getTeamTypes();
		assertEquals("Incorrect number of teams", 1, teams.length);
		assertEquals("Incorrect team", "TEAM", teams[0].getTeamName());
	}

	/**
	 * Ensures only a single {@link ManagedObjectTeamType} is available
	 * {@link ManagedFunction} instances added using the same {@link Team}.
	 */
	public void testAddFunctionsWithSameTeam() {

		final String TEAM_NAME = "TEAM";

		// Record basic meta-data
		this.record_basicMetaData();

		// Attempt to load
		ManagedObjectType<?> moType = this.loadManagedObjectType(true, (context, util) -> {
			// Recycle function uses same team
			context.getRecycleFunction(util.getManagedFunctionFactory()).setResponsibleTeam(TEAM_NAME);

			// Two functions (both using team)
			context.addManagedFunction("FUNCTION_ONE", util.getManagedFunctionFactory()).setResponsibleTeam(TEAM_NAME);
			context.addManagedFunction("FUNCTION_TWO", util.getManagedFunctionFactory()).setResponsibleTeam(TEAM_NAME);
		});

		// Ensure only team added
		assertFalse("Is input", moType.isInput());
		assertEquals("Should have no flows", 0, moType.getFlowTypes().length);
		ManagedObjectTeamType[] teams = moType.getTeamTypes();
		assertEquals("Incorrect number of teams", 1, teams.length);
		assertEquals("Incorrect team", TEAM_NAME, teams[0].getTeamName());
	}

	/**
	 * Ensures only a multiple {@link ManagedObjectTeamType} are available for added
	 * {@link ManagedFunction} instances that use various {@link Team} instances.
	 */
	public void testMultipleTeams() {

		final String TEAM_ONE = "TEAM_ONE";
		final String TEAM_TWO = "TEAM_TWO";

		// Record basic meta-data
		this.record_basicMetaData();

		// Attempt to load
		ManagedObjectType<?> moType = this.loadManagedObjectType(true, (context, util) -> {
			// Two functions (using different teams)
			context.addManagedFunction("FUNCTION_ONE", util.getManagedFunctionFactory()).setResponsibleTeam(TEAM_ONE);
			context.addManagedFunction("FUNCTION_TWO", util.getManagedFunctionFactory()).setResponsibleTeam(TEAM_TWO);
		});

		// Ensure only team added
		assertFalse("Is input", moType.isInput());
		assertEquals("Should have no flows", 0, moType.getFlowTypes().length);
		ManagedObjectTeamType[] teams = moType.getTeamTypes();
		assertEquals("Incorrect number of teams", 2, teams.length);
		assertEquals("Incorrect first team", TEAM_ONE, teams[0].getTeamName());
		assertEquals("Incorrect second team", TEAM_TWO, teams[1].getTeamName());
	}

	/**
	 * Ensures issue if an added {@link ManagedFunction} is linking an unknown added
	 * {@link ManagedFunction}.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void testAddFunctionLinkingUnknownFunction() {

		// Record basic meta-data
		this.record_basicMetaData();
		this.issues
				.recordIssue("Unknown function being linked (function=FUNCTION, flow=0, link function=LINK_FUNCTION)");

		// Attempt to load
		this.loadManagedObjectType(false, (context, util) -> {
			ManagedObjectFunctionBuilder function = context.addManagedFunction("FUNCTION",
					util.getManagedFunctionFactory());
			function.setResponsibleTeam("TEAM");
			function.linkFlow(0, "LINK_FUNCTION", null, false);
		});
	}

	/**
	 * Ensures only the {@link ManagedObjectTeamType} is available for the added
	 * {@link ManagedFunction} with a link to another added {@link ManagedFunction}.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void testAddFunctionWithLinkToAddedFunction() {

		// Record basic meta-data
		this.record_basicMetaData();

		// Attempt to load
		ManagedObjectType<?> moType = this.loadManagedObjectType(true, (context, util) -> {
			// Add function that links to other function
			ManagedObjectFunctionBuilder linkFunction = context.addManagedFunction("FUNCTION",
					util.getManagedFunctionFactory());
			linkFunction.setResponsibleTeam("TEAM");
			linkFunction.linkFlow(0, "LINK_FUNCTION", null, false);

			// Add function being linked too
			ManagedObjectFunctionBuilder targetFunction = context.addManagedFunction("LINK_FUNCTION",
					util.getManagedFunctionFactory());
			targetFunction.setResponsibleTeam("TEAM");
		});

		// Ensure only team added
		assertFalse("Is input", moType.isInput());
		assertEquals("Should have no flows", 0, moType.getFlowTypes().length);
		ManagedObjectTeamType[] teams = moType.getTeamTypes();
		assertEquals("Incorrect number of teams", 1, teams.length);
		assertEquals("Incorrect team", "TEAM", teams[0].getTeamName());
	}

	/**
	 * Ensures a {@link ManagedObjectFlowType} is added for the link from the added
	 * {@link ManagedFunction}.
	 */
	public void testAddFunctionRequiringFlow() {

		// Record basic meta-data
		this.record_basicMetaData(OneKey.KEY);

		// Attempt to load
		ManagedObjectType<?> moType = this.loadManagedObjectType(true, new Init<OneKey>() {
			@Override
			@SuppressWarnings({ "rawtypes", "unchecked" })
			public void init(ManagedObjectSourceContext<OneKey> context, InitUtil util) {
				// Add function that links to external function
				ManagedObjectFunctionBuilder linkFunction = context.addManagedFunction("FUNCTION",
						util.getManagedFunctionFactory());
				linkFunction.setResponsibleTeam("TEAM");
				linkFunction.linkFlow(0, context.getFlow(OneKey.KEY), Integer.class, false);
			}
		});

		// Ensure Flow added
		assertTrue("Is input", moType.isInput());
		ManagedObjectFlowType<?>[] flows = moType.getFlowTypes();
		assertEquals("Should have a flow", 1, flows.length);
		ManagedObjectFlowType<?> flow = flows[0];
		assertEquals("Incorrect flow name", "KEY", flow.getFlowName());
		assertEquals("Incorrect index", 0, flow.getIndex());
		assertEquals("Incorrect flow key", OneKey.KEY, flow.getKey());
		assertEquals("Incorrect argument type", Integer.class, flow.getArgumentType());
	}

	/**
	 * Ensure can have {@link Flow}.
	 */
	public void testHasFlow() {

		// Record
		this.record_basicMetaData(OneKey.KEY);
		ManagedObjectType<?> moType = this.loadManagedObjectType(true, (context, util) -> {
		});

		// Ensure is input (but no flows/teams)
		assertTrue("Is input", moType.isInput());
		ManagedObjectFlowType<?>[] flowTypes = moType.getFlowTypes();
		assertEquals("Should have no flows", 1, flowTypes.length);
		assertEquals("Incorrect flow key", OneKey.KEY, flowTypes[0].getKey());
		assertEquals("Should have no teams", 0, moType.getTeamTypes().length);
	}

	/**
	 * Ensures issue if {@link ManagedObjectSourceMetaData} uses keys for
	 * {@link ManagedObjectFlowMetaData} but linking a process is done with an
	 * index.
	 */
	public void testLinkProcessWithoutRequiredKey() {

		// Record
		this.record_basicMetaData(OneKey.KEY);
		this.issues.recordIssue("ManagedObjectFlowMetaData requires linking by keys (not indexes)");

		// Attempt to load
		this.loadManagedObjectType(false, (context, util) -> {
			context.getFlow(0).linkFunction("FUNCTION");
		});
	}

	/**
	 * Ensures issue if {@link ManagedObjectSourceMetaData} uses keys for
	 * {@link ManagedObjectFlowMetaData} but linking by different key type.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void testLinkProcessWithInvalidKey() {

		// Record
		this.record_basicMetaData(OneKey.KEY);
		this.issues.recordIssue("Link key does not match type for ManagedObjectFlowMetaData (meta-data key type="
				+ OneKey.class.getName() + ", link key type=" + InvalidKey.class.getName() + ", link key="
				+ InvalidKey.INVALID + ")");

		// Attempt to load
		this.loadManagedObjectType(false, new Init() {
			@Override
			public void init(ManagedObjectSourceContext context, InitUtil util) {
				context.getFlow(InvalidKey.INVALID).linkFunction("FUNCTION");
			}
		});
	}

	/**
	 * Ensures issue if {@link ManagedObjectSourceMetaData} uses indexing for
	 * {@link ManagedObjectFlowMetaData} but linking a process is done with a key.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void testLinkProcessWithKeyWhenIndexed() {

		// Record
		this.record_basicMetaData((Indexed) null);
		this.issues.recordIssue("ManagedObjectFlowMetaData requires linking by indexes (not keys)");

		// Attempt to load
		this.loadManagedObjectType(false, new Init() {
			@Override
			public void init(ManagedObjectSourceContext context, InitUtil util) {
				context.getFlow(OneKey.KEY).linkFunction("FUNCTION");
			}
		});
	}

	/**
	 * Ensures issue if {@link ManagedObjectSourceMetaData} uses indexing for
	 * {@link ManagedObjectFlowMetaData} and links an unknown flow index.
	 */
	public void testLinkProcessWithUnknownIndex() {

		// Record
		this.record_basicMetaData((Indexed) null);
		this.issues.recordIssue("ManagedObjectFlowMetaData does not define index (index=1)");

		// Attempt to load
		this.loadManagedObjectType(false, (context, util) -> {
			context.getFlow(0).linkFunction("LINKED");
			context.getFlow(1).linkFunction("FUNCTION");

			// Add the linked function
			context.addManagedFunction("LINKED", util.getManagedFunctionFactory()).setResponsibleTeam("TEAM");
		});
	}

	/**
	 * Ensures issue if link process without a {@link ManagedFunction} name.
	 */
	public void testLinkProcessWithoutFunctionName() {

		// Record
		this.record_basicMetaData(OneKey.KEY);
		this.issues.recordIssue("Must provide function name for linked process 0 (key=" + OneKey.KEY + ")");

		// Attempt to load
		this.loadManagedObjectType(false, new Init<OneKey>() {
			@Override
			public void init(ManagedObjectSourceContext<OneKey> context, InitUtil util) {
				context.getFlow(OneKey.KEY).linkFunction(null);
			}
		});
	}

	/**
	 * Ensures issue if linking {@link ManagedObjectFlowMetaData} to an unknown
	 * added {@link ManagedFunction}.
	 */
	public void testLinkProcessToUnknownFunction() {

		// Record
		this.record_basicMetaData(OneKey.KEY);
		this.issues
				.recordIssue("Unknown function for linked process 0 (key=" + OneKey.KEY + ", link function=FUNCTION)");

		// Attempt to load
		this.loadManagedObjectType(false, new Init<OneKey>() {
			@Override
			public void init(ManagedObjectSourceContext<OneKey> context, InitUtil util) {
				context.getFlow(OneKey.KEY).linkFunction("FUNCTION");
			}
		});
	}

	/**
	 * Ensures can link to added {@link ManagedFunction} which removes the
	 * {@link ManagedObjectFlowType} for the {@link ManagedObjectFlowMetaData} from
	 * being included in the {@link ManagedObjectType}.
	 */
	public void testLinkProcessToFunction() {

		// Record
		this.record_basicMetaData((Indexed) null);

		// Attempt to load
		ManagedObjectType<?> moType = this.loadManagedObjectType(true, (context, util) -> {
			// Add function being linked too
			context.addManagedFunction("FUNCTION", util.getManagedFunctionFactory()).setResponsibleTeam("TEAM");

			// Link to function
			context.getFlow(0).linkFunction("FUNCTION");
		});

		// Should only have team of function (as linked process hidden)
		assertTrue("Is input", moType.isInput());
		assertEquals("Should have not flows", 0, moType.getFlowTypes().length);
		assertEquals("Incorrect number of teams", 1, moType.getTeamTypes().length);
		assertEquals("Incorrect team", "TEAM", moType.getTeamTypes()[0].getTeamName());
	}

	/**
	 * Ensure issue if <code>null</code> {@link ManagedObjectExecutionMetaData}.
	 */
	public void testNullExecutionStrategy() {

		// Record
		this.record_objectAndManagedObject();
		this.recordReturn(this.metaData, this.metaData.getDependencyMetaData(), null);
		this.recordReturn(this.metaData, this.metaData.getFlowMetaData(), null);
		this.recordReturn(this.metaData, this.metaData.getExecutionMetaData(),
				new ManagedObjectExecutionMetaData[] { null });
		this.issues.recordIssue("Null ManagedObjectExecutionMetaData for execution strategy 0");

		// Attempt to load
		this.loadManagedObjectType(false, null);
	}

	/**
	 * Ensure default label for {@link ManagedObjectExecutionStrategyType}.
	 */
	public void testNonLabelledExecutionStrategy() {

		// Record
		this.record_objectAndManagedObject();
		this.recordReturn(this.metaData, this.metaData.getDependencyMetaData(), null);
		this.recordReturn(this.metaData, this.metaData.getFlowMetaData(), null);
		ManagedObjectExecutionMetaData executionStrategy = this.createMock(ManagedObjectExecutionMetaData.class);
		this.recordReturn(this.metaData, this.metaData.getExecutionMetaData(),
				new ManagedObjectExecutionMetaData[] { executionStrategy });
		this.recordReturn(executionStrategy, executionStrategy.getLabel(), null);
		this.recordReturn(this.metaData, this.metaData.getExtensionInterfacesMetaData(), null);

		// Attempt to load
		ManagedObjectType<?> moType = this.loadManagedObjectType(true, null);

		// Should have execution strategy
		ManagedObjectExecutionStrategyType[] strategies = moType.getExecutionStrategyTypes();
		assertEquals("Incorrect number of execution strategies", 1, strategies.length);
		assertEquals("Incorrect execution strategy", "0", strategies[0].getExecutionStrategyName());
	}

	/**
	 * Ensure provides {@link ManagedObjectExecutionStrategyType}.
	 */
	public void testExecutionStrategy() {

		// Record
		this.record_objectAndManagedObject();
		this.recordReturn(this.metaData, this.metaData.getDependencyMetaData(), null);
		this.recordReturn(this.metaData, this.metaData.getFlowMetaData(), null);
		ManagedObjectExecutionMetaData executionStrategy = this.createMock(ManagedObjectExecutionMetaData.class);
		this.recordReturn(this.metaData, this.metaData.getExecutionMetaData(),
				new ManagedObjectExecutionMetaData[] { executionStrategy });
		this.recordReturn(executionStrategy, executionStrategy.getLabel(), "STRATEGY");
		this.recordReturn(this.metaData, this.metaData.getExtensionInterfacesMetaData(), null);

		// Attempt to load
		ManagedObjectType<?> moType = this.loadManagedObjectType(true, null);

		// Should have execution strategy
		ManagedObjectExecutionStrategyType[] strategies = moType.getExecutionStrategyTypes();
		assertEquals("Incorrect number of execution strategies", 1, strategies.length);
		assertEquals("Incorrect execution strategy", "STRATEGY", strategies[0].getExecutionStrategyName());
	}

	/**
	 * Single key {@link Enum}.
	 */
	private enum OneKey {
		KEY
	}

	/**
	 * Two key {@link Enum}.
	 */
	private enum TwoKey {
		ONE, TWO
	}

	/**
	 * Invalid key {@link Enum}.
	 */
	private enum InvalidKey {
		INVALID
	}

	/**
	 * Records obtaining the {@link Object} and {@link ManagedObject} class from the
	 * {@link ManagedObjectSourceMetaData}.
	 */
	private void record_objectAndManagedObject() {
		this.recordReturn(this.metaData, this.metaData.getObjectClass(), Connection.class);
		this.recordReturn(this.metaData, this.metaData.getManagedObjectClass(), ManagedObject.class);
	}

	/**
	 * Records obtaining simple meta-data from the
	 * {@link ManagedObjectSourceMetaData}.
	 * 
	 * @param flowKeys Flow keys to be defined in meta-data. Provide
	 *                 <code>null</code> values for indexing.
	 */
	@SuppressWarnings("unchecked")
	private <F extends Enum<?>> void record_basicMetaData(F... flowKeys) {
		this.record_objectAndManagedObject();

		// Return no dependencies
		this.recordReturn(this.metaData, this.metaData.getDependencyMetaData(), null);

		// Record the flows
		if (flowKeys.length == 0) {
			// Record no flows
			this.recordReturn(this.metaData, this.metaData.getFlowMetaData(), null);
		} else {
			// Create the meta-data for each flow
			ManagedObjectFlowMetaData<?>[] flowMetaDatas = new ManagedObjectFlowMetaData[flowKeys.length];
			for (int i = 0; i < flowMetaDatas.length; i++) {
				flowMetaDatas[i] = this.createMock(ManagedObjectFlowMetaData.class);
			}

			// Record the flow meta-data
			this.recordReturn(this.metaData, this.metaData.getFlowMetaData(), flowMetaDatas);
			for (int i = 0; i < flowMetaDatas.length; i++) {
				ManagedObjectFlowMetaData<?> flowMetaData = flowMetaDatas[i];
				this.recordReturn(flowMetaData, flowMetaData.getLabel(), null);
				this.recordReturn(flowMetaData, flowMetaData.getKey(), flowKeys[i]);
				this.recordReturn(flowMetaData, flowMetaData.getArgumentType(), Integer.class);
			}
		}

		// Record no execution strategies
		this.recordReturn(this.metaData, this.metaData.getExecutionMetaData(), null);

		// Record no supported extension interfaces
		this.recordReturn(this.metaData, this.metaData.getExtensionInterfacesMetaData(), null);
	}

	/**
	 * Loads the {@link ManagedObjectType}.
	 * 
	 * @param isExpectedToLoad       Flag indicating if expecting to load the
	 *                               {@link FunctionNamespaceType}.
	 * @param init                   {@link Init}.
	 * @param propertyNameValuePairs {@link Property} name value pairs.
	 * @return Loaded {@link ManagedObjectType}.
	 */
	@SuppressWarnings("rawtypes")
	public <F extends Enum<F>> ManagedObjectType<?> loadManagedObjectType(boolean isExpectedToLoad, Init<F> init,
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

		// Create the managed object loader and load the managed object type
		OfficeFloorCompiler compiler = OfficeFloorCompiler.newOfficeFloorCompiler(null);
		compiler.setCompilerIssues(this.issues);
		ManagedObjectLoader moLoader = compiler.getManagedObjectLoader();
		MockManagedObjectSource.init = init;
		ManagedObjectType moType = moLoader.loadManagedObjectType(MockManagedObjectSource.class, propertyList);

		// Verify the mock objects
		this.verifyMockObjects();

		// Ensure if should be loaded
		if (isExpectedToLoad) {
			assertNotNull("Expected to load the managed object type", moType);
		} else {
			assertNull("Should not load the managed object type", moType);
		}

		// Return the managed object type
		return moType;
	}

	/**
	 * Implement to initialise the {@link MockManagedObjectSource}.
	 */
	private static interface Init<F extends Enum<F>> {

		/**
		 * Implemented to init the {@link ManagedObjectSource}.
		 * 
		 * @param context {@link ManagedObjectSourceContext}.
		 * @param util    {@link InitUtil}.
		 */
		void init(ManagedObjectSourceContext<F> context, InitUtil util);
	}

	/**
	 * Provides useful functionality for {@link Init}.
	 */
	private class InitUtil {

		/**
		 * Obtains the {@link ManagedFunctionFactory}.
		 * 
		 * @return {@link ManagedFunctionFactory}.
		 */
		public ManagedFunctionFactory<?, ?> getManagedFunctionFactory() {
			return LoadManagedObjectTypeTest.this.functionFactory;
		}
	}

	/**
	 * Mock {@link ManagedObjectSource}.
	 */
	@TestSource
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static class MockManagedObjectSource implements ManagedObjectSource<None, None> {

		/**
		 * Failure to instantiate an instance.
		 */
		public static RuntimeException instantiateFailure = null;

		/**
		 * {@link Init} to init the {@link ManagedObjectSource}.
		 */
		public static Init init = null;

		/**
		 * Failure to obtain the {@link ManagedObjectMetaData}.
		 */
		public static Error metaDataFailure = null;

		/**
		 * {@link ManagedObjectSourceSpecification}.
		 */
		public static ManagedObjectSourceMetaData metaData;

		/**
		 * {@link InitUtil}.
		 */
		public static InitUtil initUtil;

		/**
		 * Resets the state for next test.
		 * 
		 * @param metaData {@link ManagedObjectSourceMetaData}.
		 * @param initUtil {@link InitUtil}.
		 */
		public static void reset(ManagedObjectSourceMetaData<?, ?> metaData, InitUtil initUtil) {
			instantiateFailure = null;
			init = null;
			metaDataFailure = null;
			MockManagedObjectSource.metaData = metaData;
			MockManagedObjectSource.initUtil = initUtil;
		}

		/**
		 * Initiate with possible failure.
		 */
		public MockManagedObjectSource() {
			// Throw instantiate failure
			if (instantiateFailure != null) {
				throw instantiateFailure;
			}
		}

		/*
		 * ================== ManagedObjectSource ===========================
		 */

		@Override
		public ManagedObjectSourceSpecification getSpecification() {
			fail("Should not obtain specification");
			return null;
		}

		@Override
		public ManagedObjectSourceMetaData init(ManagedObjectSourceContext context) throws Exception {

			// Run the init if available
			if (init != null) {
				init.init(context, initUtil);
			}

			// Throw meta-data failure
			if (metaDataFailure != null) {
				throw metaDataFailure;
			}

			// Return the meta-data
			return metaData;
		}

		@Override
		public void start(ManagedObjectExecuteContext context) throws Exception {
			fail("Should not start the ManagedObjectSource");
		}

		@Override
		public void sourceManagedObject(ManagedObjectUser user) {
			fail("Should not source ManagedObject");
		}

		@Override
		public void stop() {
			fail("Should not stop the ManagedObjectSource");
		}
	}

}
