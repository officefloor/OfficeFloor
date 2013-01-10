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
package net.officefloor.plugin.web.http.security.type;

import java.sql.Connection;
import java.util.Properties;

import javax.transaction.xa.XAResource;

import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.impl.properties.PropertyListImpl;
import net.officefloor.compile.issues.CompilerIssues;
import net.officefloor.compile.managedobject.ManagedObjectDependencyType;
import net.officefloor.compile.managedobject.ManagedObjectFlowType;
import net.officefloor.compile.managedobject.ManagedObjectLoader;
import net.officefloor.compile.managedobject.ManagedObjectTeamType;
import net.officefloor.compile.managedobject.ManagedObjectType;
import net.officefloor.compile.properties.Property;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.test.issues.FailTestCompilerIssues;
import net.officefloor.compile.work.WorkType;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.build.OfficeFloorIssues.AssetType;
import net.officefloor.frame.api.build.TaskFactory;
import net.officefloor.frame.api.build.WorkFactory;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.internal.structure.FlowInstigationStrategyEnum;
import net.officefloor.frame.internal.structure.ManagedObjectMetaData;
import net.officefloor.frame.spi.TestSource;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.extension.ExtensionInterfaceFactory;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectDependencyMetaData;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectExecuteContext;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectExtensionInterfaceMetaData;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectFlowMetaData;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceContext;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceMetaData;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceSpecification;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectTaskBuilder;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectUser;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectWorkBuilder;
import net.officefloor.frame.spi.team.Team;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.managedobject.clazz.ClassManagedObjectSource;

/**
 * Tests loading the {@link ManagedObjectType}.
 * 
 * @author Daniel Sagenschneider
 */
public class LoadHttpSecurityTypeTest extends OfficeFrameTestCase {

	/**
	 * {@link CompilerIssues}.
	 */
	private final CompilerIssues issues = this.createMock(CompilerIssues.class);

	/**
	 * {@link ManagedObjectSourceMetaData}.
	 */
	private final ManagedObjectSourceMetaData<?, ?> metaData = this
			.createMock(ManagedObjectSourceMetaData.class);

	/**
	 * {@link WorkFactory}.
	 */
	@SuppressWarnings("unchecked")
	private final WorkFactory<Work> workFactory = this
			.createMock(WorkFactory.class);

	/**
	 * {@link TaskFactory}.
	 */
	@SuppressWarnings("unchecked")
	private final TaskFactory<Work, ?, ?> taskFactory = this
			.createMock(TaskFactory.class);

	@Override
	protected void setUp() throws Exception {
		MockManagedObjectSource.reset(this.metaData, new InitUtil());
	}

	/**
	 * Ensure can load {@link SimpleManagedObject} via
	 * {@link ClassManagedObjectSource} {@link Class}.
	 */
	public void testLoadByClass() {

		// Configure test
		OfficeFloorCompiler compiler = OfficeFloorCompiler
				.newOfficeFloorCompiler(null);
		compiler.setCompilerIssues(new FailTestCompilerIssues());

		// Configure to load simple class
		PropertyList properties = compiler.createPropertyList();
		properties.addProperty(
				ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME).setValue(
				MockLoadHttpSecurity.class.getName());

		// Load the managed object type
		ManagedObjectLoader moLoader = compiler.getManagedObjectLoader();
		ManagedObjectType<?> moType = moLoader.loadManagedObjectType(
				ClassManagedObjectSource.class, properties);
		MockLoadHttpSecurity.assertManagedObjectType(moType);
	}

	/**
	 * Ensure can load {@link SimpleManagedObject} via
	 * {@link ClassManagedObjectSource} instance.
	 */
	public void testLoadByInstance() {

		// Configure test
		OfficeFloorCompiler compiler = OfficeFloorCompiler
				.newOfficeFloorCompiler(null);
		compiler.setCompilerIssues(new FailTestCompilerIssues());

		// Configure to load simple class
		PropertyList properties = compiler.createPropertyList();
		properties.addProperty(
				ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME).setValue(
				MockLoadHttpSecurity.class.getName());

		// Load the managed object type
		ManagedObjectLoader moLoader = compiler.getManagedObjectLoader();
		ManagedObjectType<?> moType = moLoader.loadManagedObjectType(
				new ClassManagedObjectSource(), properties);
		MockLoadHttpSecurity.assertManagedObjectType(moType);
	}

	/**
	 * Ensure issue if fail to instantiate the {@link ManagedObjectSource}.
	 */
	public void testFailInstantiate() {

		final RuntimeException failure = new RuntimeException(
				"instantiate failure");

		// Record failure to instantiate
		this.record_issue("Failed to instantiate "
				+ MockManagedObjectSource.class.getName()
				+ " by default constructor", failure);

		// Attempt to load
		MockManagedObjectSource.instantiateFailure = failure;
		this.loadManagedObjectType(false, (Init<?>) null);
	}

	/**
	 * Ensure issue if missing {@link Property}.
	 */
	public void testMissingProperty() {

		// Record missing property
		this.record_issue("Missing property 'missing'");

		// Attempt to load
		this.loadManagedObjectType(false, new Init<None>() {
			@Override
			public void init(ManagedObjectSourceContext<None> context,
					InitUtil util) {
				context.getProperty("missing");
			}
		});
	}

	/**
	 * Ensure able to get properties.
	 */
	public void testGetProperties() {

		// Record basic meta-data
		this.record_basicMetaData();

		// Attempt to load
		this.loadManagedObjectType(true, new Init<None>() {
			@Override
			public void init(ManagedObjectSourceContext<None> context,
					InitUtil util) {
				assertEquals("Ensure get defaulted property", "DEFAULT",
						context.getProperty("missing", "DEFAULT"));
				assertEquals("Ensure get property ONE", "1",
						context.getProperty("ONE"));
				assertEquals("Ensure get property TWO", "2",
						context.getProperty("TWO"));
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
		this.record_issue("Can not load class 'missing'");

		// Attempt to load
		this.loadManagedObjectType(false, new Init<None>() {
			@Override
			public void init(ManagedObjectSourceContext<None> context,
					InitUtil util) {
				context.loadClass("missing");
			}
		});
	}

	/**
	 * Ensure issue if missing resource.
	 */
	public void testMissingResource() {

		// Record missing resource
		this.record_issue("Can not obtain resource at location 'missing'");

		// Attempt to load
		this.loadManagedObjectType(false, new Init<None>() {
			@Override
			public void init(ManagedObjectSourceContext<None> context,
					InitUtil util) {
				context.getResource("missing");
			}
		});
	}

	/**
	 * Ensure able to get resource.
	 */
	public void testGetResource() {

		// Record basic meta-data
		this.record_basicMetaData();

		// Obtain path
		final String objectPath = Object.class.getName().replace('.', '/')
				+ ".class";

		// Attempt to load
		this.loadManagedObjectType(true, new Init<None>() {
			@Override
			public void init(ManagedObjectSourceContext<None> context,
					InitUtil util) {
				assertEquals("Incorrect resource locator",
						LoadHttpSecurityTypeTest.class.getClassLoader()
								.getResource(objectPath), context
								.getClassLoader().getResource(objectPath));
			}
		});
	}

	/**
	 * Ensure issue if fails to init the {@link ManagedObjectSource}.
	 */
	public void testFailInitManagedObjectSource() {

		final NullPointerException failure = new NullPointerException(
				"Fail init ManagedObjectSource");

		// Record failure to init the Managed Object Source
		this.record_issue("Failed to init", failure);

		// Attempt to load
		this.loadManagedObjectType(false, new Init<None>() {
			@Override
			public void init(ManagedObjectSourceContext<None> context,
					InitUtil util) {
				throw failure;
			}
		});
	}

	/**
	 * Ensure issue if <code>null</code> {@link ManagedObjectSourceMetaData}.
	 */
	public void testNullManagedObjectSourceMetaData() {

		// Record null the Managed Object Source meta-data
		this.record_issue("Returned null ManagedObjectSourceMetaData");

		// Attempt to load
		this.loadManagedObjectType(false, new Init<None>() {
			@Override
			public void init(ManagedObjectSourceContext<None> context,
					InitUtil util) {
				MockManagedObjectSource.metaData = null;
			}
		});
	}

	/**
	 * Ensure issue if fails to obtain the {@link ManagedObjectSourceMetaData}.
	 */
	public void testFailGetManagedObjectSourceMetaData() {

		final Error failure = new Error("Obtain meta-data failure");

		// Record failure to obtain the meta-data
		this.record_issue("Failed to get ManagedObjectSourceMetaData", failure);

		// Attempt to load
		this.loadManagedObjectType(false, new Init<None>() {
			@Override
			public void init(ManagedObjectSourceContext<None> context,
					InitUtil util) {
				MockManagedObjectSource.metaDataFailure = failure;
			}
		});
	}

	/**
	 * Ensure issue if no {@link Object} class from meta-data.
	 */
	public void testNoObjectClass() {

		// Record no object class
		this.recordReturn(this.metaData, this.metaData.getObjectClass(), null);
		this.record_issue("No Object type provided");

		// Attempt to load
		this.loadManagedObjectType(false, (Init<None>) null);
	}

	/**
	 * Ensure issue if no {@link ManagedObject} class from meta-data.
	 */
	public void testNoManagedObjectClass() {

		// Record no managed object class
		this.recordReturn(this.metaData, this.metaData.getObjectClass(),
				Object.class);
		this.recordReturn(this.metaData, this.metaData.getManagedObjectClass(),
				null);
		this.record_issue("No ManagedObject type provided");

		// Attempt to load
		this.loadManagedObjectType(false, (Init<None>) null);
	}

	/**
	 * Ensure issue if no {@link ManagedObject} class does not implement
	 * {@link ManagedObject}.
	 */
	public void testManagedObjectClassMustImplementManagedObject() {

		// Record invalid managed object class
		this.recordReturn(this.metaData, this.metaData.getObjectClass(),
				Object.class);
		this.recordReturn(this.metaData, this.metaData.getManagedObjectClass(),
				Integer.class);
		this.record_issue("ManagedObject class must implement "
				+ ManagedObject.class.getName() + " (class="
				+ Integer.class.getName() + ")");

		// Attempt to load
		this.loadManagedObjectType(false, (Init<None>) null);
	}

	/**
	 * Ensure issue if <code>null</code> {@link ManagedObjectDependencyMetaData}
	 * in array.
	 */
	public void testNullDependencyMetaData() {

		// Record no dependency type
		this.record_objectAndManagedObject();
		this.recordReturn(this.metaData, this.metaData.getDependencyMetaData(),
				new ManagedObjectDependencyMetaData[] { null });
		this.record_issue("Null ManagedObjectDependencyMetaData for dependency 0");

		// Attempt to load
		this.loadManagedObjectType(false, (Init<None>) null);
	}

	/**
	 * Ensure issue if no dependency type.
	 */
	public void testNoDependencyType() {

		final ManagedObjectDependencyMetaData<?> dependency = this
				.createMock(ManagedObjectDependencyMetaData.class);

		// Record no dependency type
		this.record_objectAndManagedObject();
		this.recordReturn(this.metaData, this.metaData.getDependencyMetaData(),
				new ManagedObjectDependencyMetaData[] { dependency });
		this.recordReturn(dependency, dependency.getLabel(), "NO TYPE");
		this.recordReturn(dependency, dependency.getKey(), null);
		this.recordReturn(dependency, dependency.getType(), null);
		this.record_issue("No type for dependency 0 (key=<indexed>, label=NO TYPE)");

		// Attempt to load
		this.loadManagedObjectType(false, (Init<None>) null);
	}

	/**
	 * Ensure issue if dependency keys of different types.
	 */
	public void testInvalidDependencyKey() {

		final ManagedObjectDependencyMetaData<?> dependencyOne = this
				.createMock(ManagedObjectDependencyMetaData.class);
		final ManagedObjectDependencyMetaData<?> dependencyTwo = this
				.createMock(ManagedObjectDependencyMetaData.class);

		// Record missing dependency key
		this.record_objectAndManagedObject();
		this.recordReturn(this.metaData, this.metaData.getDependencyMetaData(),
				new ManagedObjectDependencyMetaData[] { dependencyOne,
						dependencyTwo });
		this.recordReturn(dependencyOne, dependencyOne.getLabel(), null);
		this.recordReturn(dependencyOne, dependencyOne.getKey(), TwoKey.ONE);
		this.recordReturn(dependencyOne, dependencyOne.getType(),
				Connection.class);
		this.recordReturn(dependencyOne, dependencyOne.getTypeQualifier(), null);
		this.recordReturn(dependencyTwo, dependencyTwo.getLabel(), null);
		this.recordReturn(dependencyTwo, dependencyTwo.getKey(),
				InvalidKey.INVALID);
		this.record_issue("Dependencies identified by different key types ("
				+ TwoKey.class.getName() + ", " + InvalidKey.class.getName()
				+ ")");

		// Attempt to load
		this.loadManagedObjectType(false, (Init<None>) null);
	}

	/**
	 * Ensure issue if dependency mixing using keys and indexes.
	 */
	public void testDependencyMixingKeyAndIndexes() {

		final ManagedObjectDependencyMetaData<?> dependencyOne = this
				.createMock(ManagedObjectDependencyMetaData.class);
		final ManagedObjectDependencyMetaData<?> dependencyTwo = this
				.createMock(ManagedObjectDependencyMetaData.class);

		// Record missing dependency key
		this.record_objectAndManagedObject();
		this.recordReturn(this.metaData, this.metaData.getDependencyMetaData(),
				new ManagedObjectDependencyMetaData[] { dependencyOne,
						dependencyTwo });
		this.recordReturn(dependencyOne, dependencyOne.getLabel(), null);
		this.recordReturn(dependencyOne, dependencyOne.getKey(), TwoKey.ONE);
		this.recordReturn(dependencyOne, dependencyOne.getType(),
				Connection.class);
		this.recordReturn(dependencyOne, dependencyOne.getTypeQualifier(), null);
		this.recordReturn(dependencyTwo, dependencyTwo.getLabel(), null);
		this.recordReturn(dependencyTwo, dependencyTwo.getKey(), null);
		this.record_issue("Dependencies mixing keys and indexes");

		// Attempt to load
		this.loadManagedObjectType(false, (Init<None>) null);
	}

	/**
	 * Ensure issue if key used more than once for a dependency.
	 */
	public void testDuplicateDependencyKey() {

		final ManagedObjectDependencyMetaData<?> dependencyOne = this
				.createMock(ManagedObjectDependencyMetaData.class);
		final ManagedObjectDependencyMetaData<?> dependencyTwo = this
				.createMock(ManagedObjectDependencyMetaData.class);

		// Record missing dependency key
		this.record_objectAndManagedObject();
		this.recordReturn(this.metaData, this.metaData.getDependencyMetaData(),
				new ManagedObjectDependencyMetaData[] { dependencyOne,
						dependencyTwo });
		this.recordReturn(dependencyOne, dependencyOne.getLabel(), null);
		this.recordReturn(dependencyOne, dependencyOne.getKey(), TwoKey.ONE);
		this.recordReturn(dependencyOne, dependencyOne.getType(),
				Connection.class);
		this.recordReturn(dependencyOne, dependencyOne.getTypeQualifier(), null);
		this.recordReturn(dependencyTwo, dependencyTwo.getLabel(), null);
		this.recordReturn(dependencyTwo, dependencyTwo.getKey(), TwoKey.ONE);
		this.recordReturn(dependencyTwo, dependencyTwo.getType(), String.class);
		this.recordReturn(dependencyTwo, dependencyTwo.getTypeQualifier(), null);
		this.record_issue("Must have exactly one dependency per key (key="
				+ TwoKey.ONE + ")");

		// Attempt to load
		this.loadManagedObjectType(false, (Init<None>) null);
	}

	/**
	 * Ensure issue if not have meta-data for each dependency key.
	 */
	public void testNotAllDependencyKeys() {

		final ManagedObjectDependencyMetaData<?> dependency = this
				.createMock(ManagedObjectDependencyMetaData.class);

		// Record not all dependency keys
		this.record_objectAndManagedObject();
		this.recordReturn(this.metaData, this.metaData.getDependencyMetaData(),
				new ManagedObjectDependencyMetaData[] { dependency });
		this.recordReturn(dependency, dependency.getLabel(), null);
		this.recordReturn(dependency, dependency.getKey(), TwoKey.ONE);
		this.recordReturn(dependency, dependency.getType(), Connection.class);
		this.recordReturn(dependency, dependency.getTypeQualifier(), null);
		this.record_issue("Missing dependency meta-data (keys=" + TwoKey.TWO
				+ ")");

		// Attempt to load
		this.loadManagedObjectType(false, (Init<None>) null);
	}

	/**
	 * Ensure issue if <code>null</code> {@link ManagedObjectFlowMetaData} in
	 * array.
	 */
	public void testNullFlowMetaData() {

		// Record no flow type
		this.record_objectAndManagedObject();
		this.recordReturn(this.metaData, this.metaData.getDependencyMetaData(),
				null);
		this.recordReturn(this.metaData, this.metaData.getFlowMetaData(),
				new ManagedObjectFlowMetaData[] { null });
		this.record_issue("Null ManagedObjectFlowMetaData for flow 0");

		// Attempt to load
		this.loadManagedObjectType(false, (Init<None>) null);
	}

	/**
	 * Ensure able to default the flow argument type.
	 */
	public void testDefaultFlowArgumentType() {

		final ManagedObjectFlowMetaData<?> flowDefaulted = this
				.createMock(ManagedObjectFlowMetaData.class);
		final ManagedObjectFlowMetaData<?> flowProvided = this
				.createMock(ManagedObjectFlowMetaData.class);

		// Record no flow argument type
		this.record_objectAndManagedObject();
		this.recordReturn(this.metaData, this.metaData.getDependencyMetaData(),
				null);
		this.recordReturn(this.metaData, this.metaData.getFlowMetaData(),
				new ManagedObjectFlowMetaData[] { flowDefaulted, flowProvided });
		this.recordReturn(flowDefaulted, flowDefaulted.getLabel(), "DEFAULTED");
		this.recordReturn(flowDefaulted, flowDefaulted.getKey(), null);
		this.recordReturn(flowDefaulted, flowDefaulted.getArgumentType(), null);
		this.recordReturn(flowProvided, flowProvided.getLabel(), null);
		this.recordReturn(flowProvided, flowProvided.getKey(), null);
		this.recordReturn(flowProvided, flowProvided.getArgumentType(),
				Connection.class);
		this.recordReturn(this.metaData,
				this.metaData.getExtensionInterfacesMetaData(), null);

		// Attempt to load
		ManagedObjectType<?> moType = this.loadManagedObjectType(true,
				(Init<None>) null);

		// Validate argument types of flows
		ManagedObjectFlowType<?>[] flowTypes = moType.getFlowTypes();
		assertEquals("Incorrect number of flows", 2, flowTypes.length);
		ManagedObjectFlowType<?> defaulted = flowTypes[0];
		assertEquals("Incorrect name for defaulted argument flow", "DEFAULTED",
				defaulted.getFlowName());
		assertEquals("Incorrect defaulted argument type", Void.class,
				defaulted.getArgumentType());
		ManagedObjectFlowType<?> provided = flowTypes[1];
		assertEquals("Incorrect name for provided argument flow", "1",
				provided.getFlowName());
		assertEquals("Incorrect provided argument type", Connection.class,
				provided.getArgumentType());
	}

	/**
	 * Ensure issue if flow keys of different types.
	 */
	public void testInvalidFlowKey() {

		final ManagedObjectFlowMetaData<?> flowOne = this
				.createMock(ManagedObjectFlowMetaData.class);
		final ManagedObjectFlowMetaData<?> flowTwo = this
				.createMock(ManagedObjectFlowMetaData.class);

		// Record missing flow key
		this.record_objectAndManagedObject();
		this.recordReturn(this.metaData, this.metaData.getDependencyMetaData(),
				null);
		this.recordReturn(this.metaData, this.metaData.getFlowMetaData(),
				new ManagedObjectFlowMetaData[] { flowOne, flowTwo });
		this.recordReturn(flowOne, flowOne.getLabel(), null);
		this.recordReturn(flowOne, flowOne.getKey(), TwoKey.ONE);
		this.recordReturn(flowOne, flowOne.getArgumentType(), Connection.class);
		this.recordReturn(flowTwo, flowTwo.getLabel(), null);
		this.recordReturn(flowTwo, flowTwo.getKey(), InvalidKey.INVALID);
		this.record_issue("Meta-data flows identified by different key types ("
				+ TwoKey.class.getName() + ", " + InvalidKey.class.getName()
				+ ")");

		// Attempt to load
		this.loadManagedObjectType(false, (Init<None>) null);
	}

	/**
	 * Ensure issue if flow mixing using keys and indexes.
	 */
	public void testFlowMixingKeyAndIndexes() {

		final ManagedObjectFlowMetaData<?> flowOne = this
				.createMock(ManagedObjectFlowMetaData.class);
		final ManagedObjectFlowMetaData<?> flowTwo = this
				.createMock(ManagedObjectFlowMetaData.class);

		// Record missing flow key
		this.record_objectAndManagedObject();
		this.recordReturn(this.metaData, this.metaData.getDependencyMetaData(),
				null);
		this.recordReturn(this.metaData, this.metaData.getFlowMetaData(),
				new ManagedObjectFlowMetaData[] { flowOne, flowTwo });
		this.recordReturn(flowOne, flowOne.getLabel(), null);
		this.recordReturn(flowOne, flowOne.getKey(), TwoKey.ONE);
		this.recordReturn(flowOne, flowOne.getArgumentType(), Connection.class);
		this.recordReturn(flowTwo, flowTwo.getLabel(), null);
		this.recordReturn(flowTwo, flowTwo.getKey(), null);
		this.record_issue("Meta-data flows mixing keys and indexes");

		// Attempt to load
		this.loadManagedObjectType(false, (Init<None>) null);
	}

	/**
	 * Ensure issue if key used more than once for a flow.
	 */
	public void testDuplicateFlowKey() {

		final ManagedObjectFlowMetaData<?> flowOne = this
				.createMock(ManagedObjectFlowMetaData.class);
		final ManagedObjectFlowMetaData<?> flowTwo = this
				.createMock(ManagedObjectFlowMetaData.class);

		// Record missing flow key
		this.record_objectAndManagedObject();
		this.recordReturn(this.metaData, this.metaData.getDependencyMetaData(),
				null);
		this.recordReturn(this.metaData, this.metaData.getFlowMetaData(),
				new ManagedObjectFlowMetaData[] { flowOne, flowTwo });
		this.recordReturn(flowOne, flowOne.getLabel(), null);
		this.recordReturn(flowOne, flowOne.getKey(), TwoKey.ONE);
		this.recordReturn(flowOne, flowOne.getArgumentType(), Connection.class);
		this.recordReturn(flowTwo, flowTwo.getLabel(), null);
		this.recordReturn(flowTwo, flowTwo.getKey(), TwoKey.ONE);
		this.recordReturn(flowTwo, flowTwo.getArgumentType(), String.class);
		this.record_issue("Must have exactly one flow per key (key="
				+ TwoKey.ONE + ")");

		// Attempt to load
		this.loadManagedObjectType(false, (Init<None>) null);
	}

	/**
	 * Ensure issue if not have meta-data for each flow key.
	 */
	public void testNotAllFlowKeys() {

		final ManagedObjectFlowMetaData<?> flow = this
				.createMock(ManagedObjectFlowMetaData.class);

		// Record not all flow keys
		this.record_objectAndManagedObject();
		this.recordReturn(this.metaData, this.metaData.getDependencyMetaData(),
				null);
		this.recordReturn(this.metaData, this.metaData.getFlowMetaData(),
				new ManagedObjectFlowMetaData[] { flow });
		this.recordReturn(flow, flow.getLabel(), null);
		this.recordReturn(flow, flow.getKey(), TwoKey.ONE);
		this.recordReturn(flow, flow.getArgumentType(), Connection.class);
		this.record_issue("Missing flow meta-data (keys=" + TwoKey.TWO + ")");

		// Attempt to load
		this.loadManagedObjectType(false, (Init<None>) null);
	}

	/**
	 * Ensure {@link ManagedObjectType} correct with keyed dependencies and
	 * flows.
	 */
	public void testKeyedDependenciesAndFlows() {

		final ManagedObjectDependencyMetaData<?> dependencyOne = this
				.createMock(ManagedObjectDependencyMetaData.class);
		final ManagedObjectDependencyMetaData<?> dependencyTwo = this
				.createMock(ManagedObjectDependencyMetaData.class);
		final ManagedObjectFlowMetaData<?> flowOne = this
				.createMock(ManagedObjectFlowMetaData.class);
		final ManagedObjectFlowMetaData<?> flowTwo = this
				.createMock(ManagedObjectFlowMetaData.class);

		// Record keyed dependencies and flows
		this.record_objectAndManagedObject();
		this.recordReturn(this.metaData, this.metaData.getDependencyMetaData(),
				new ManagedObjectDependencyMetaData[] { dependencyOne,
						dependencyTwo });
		this.recordReturn(dependencyOne, dependencyOne.getLabel(), null);
		this.recordReturn(dependencyOne, dependencyOne.getKey(), TwoKey.TWO); // order
		this.recordReturn(dependencyOne, dependencyOne.getType(), Integer.class);
		this.recordReturn(dependencyOne, dependencyOne.getTypeQualifier(),
				"QUALIFIED");
		this.recordReturn(dependencyTwo, dependencyTwo.getLabel(), null);
		this.recordReturn(dependencyTwo, dependencyTwo.getKey(), TwoKey.ONE); // order
		this.recordReturn(dependencyTwo, dependencyTwo.getType(),
				Connection.class);
		this.recordReturn(dependencyTwo, dependencyTwo.getTypeQualifier(), null);
		this.recordReturn(this.metaData, this.metaData.getFlowMetaData(),
				new ManagedObjectFlowMetaData[] { flowOne, flowTwo });
		this.recordReturn(flowOne, flowOne.getLabel(), null);
		this.recordReturn(flowOne, flowOne.getKey(), TwoKey.TWO); // order
		this.recordReturn(flowOne, flowOne.getArgumentType(), Long.class);
		this.recordReturn(flowTwo, flowTwo.getLabel(), null);
		this.recordReturn(flowTwo, flowTwo.getKey(), TwoKey.ONE); // order
		this.recordReturn(flowTwo, flowTwo.getArgumentType(), String.class);
		this.recordReturn(this.metaData,
				this.metaData.getExtensionInterfacesMetaData(), null);

		// Attempt to load
		ManagedObjectType<?> moType = this.loadManagedObjectType(true,
				(Init<None>) null);

		// Validate dependencies ordered and correct values
		ManagedObjectDependencyType<?>[] dependencyTypes = moType
				.getDependencyTypes();
		assertEquals("Incorrect number of dependencies", 2,
				dependencyTypes.length);
		ManagedObjectDependencyType<?> dependencyTypeOne = dependencyTypes[0];
		assertEquals("Keys should be ordered", TwoKey.ONE,
				dependencyTypeOne.getKey());
		assertEquals("Incorrect first dependency index", TwoKey.ONE.ordinal(),
				dependencyTypeOne.getIndex());
		assertEquals("Incorrect first dependency name", TwoKey.ONE.toString(),
				dependencyTypeOne.getDependencyName());
		assertEquals("Incorrect first dependency type", Connection.class,
				dependencyTypeOne.getDependencyType());
		assertNull("First dependency type should not be qualified",
				dependencyTypeOne.getTypeQualifier());
		ManagedObjectDependencyType<?> dependencyTypeTwo = dependencyTypes[1];
		assertEquals("Keys should be ordered", TwoKey.TWO,
				dependencyTypeTwo.getKey());
		assertEquals("Incorrect second dependency index", TwoKey.TWO.ordinal(),
				dependencyTypeTwo.getIndex());
		assertEquals("Incorrect second dependency name", TwoKey.TWO.toString(),
				dependencyTypeTwo.getDependencyName());
		assertEquals("Incorrect second dependency type", Integer.class,
				dependencyTypeTwo.getDependencyType());
		assertEquals("Incorrect second dependency type qualification",
				"QUALIFIED", dependencyTypeTwo.getTypeQualifier());

		// Validate flows ordered and correct values
		ManagedObjectFlowType<?>[] flowTypes = moType.getFlowTypes();
		assertEquals("Incorrect number of dependencies", 2, flowTypes.length);
		ManagedObjectFlowType<?> flowTypeOne = flowTypes[0];
		assertEquals("Keys should be ordered", TwoKey.ONE, flowTypeOne.getKey());
		assertEquals("Incorrect first flow index", TwoKey.ONE.ordinal(),
				flowTypeOne.getIndex());
		assertEquals("Incorrect first flow name", TwoKey.ONE.toString(),
				flowTypeOne.getFlowName());
		assertEquals("Incorrect first flow argument type", String.class,
				flowTypeOne.getArgumentType());
		ManagedObjectFlowType<?> flowTypeTwo = flowTypes[1];
		assertEquals("Keys should be ordered", TwoKey.TWO, flowTypeTwo.getKey());
		assertEquals("Incorrect second flow index", TwoKey.TWO.ordinal(),
				flowTypeTwo.getIndex());
		assertEquals("Incorrect second flow name", TwoKey.TWO.toString(),
				flowTypeTwo.getFlowName());
		assertEquals("Incorrect second flow argument type", Long.class,
				flowTypeTwo.getArgumentType());
	}

	/**
	 * Ensure {@link ManagedObjectType} correct with indexed dependencies and
	 * flows.
	 */
	public void testIndexedDependenciesAndFlows() {

		final ManagedObjectDependencyMetaData<?> dependencyOne = this
				.createMock(ManagedObjectDependencyMetaData.class);
		final ManagedObjectDependencyMetaData<?> dependencyTwo = this
				.createMock(ManagedObjectDependencyMetaData.class);
		final ManagedObjectFlowMetaData<?> flowOne = this
				.createMock(ManagedObjectFlowMetaData.class);
		final ManagedObjectFlowMetaData<?> flowTwo = this
				.createMock(ManagedObjectFlowMetaData.class);

		// Record keyed dependencies and flows
		this.record_objectAndManagedObject();
		this.recordReturn(this.metaData, this.metaData.getDependencyMetaData(),
				new ManagedObjectDependencyMetaData[] { dependencyOne,
						dependencyTwo });
		this.recordReturn(dependencyOne, dependencyOne.getLabel(), null);
		this.recordReturn(dependencyOne, dependencyOne.getKey(), null);
		this.recordReturn(dependencyOne, dependencyOne.getType(), Integer.class);
		this.recordReturn(dependencyOne, dependencyOne.getTypeQualifier(),
				"QUALIFIED");
		this.recordReturn(dependencyTwo, dependencyTwo.getLabel(), null);
		this.recordReturn(dependencyTwo, dependencyTwo.getKey(), null);
		this.recordReturn(dependencyTwo, dependencyTwo.getType(),
				Connection.class);
		this.recordReturn(dependencyTwo, dependencyTwo.getTypeQualifier(), null);
		this.recordReturn(this.metaData, this.metaData.getFlowMetaData(),
				new ManagedObjectFlowMetaData[] { flowOne, flowTwo });
		this.recordReturn(flowOne, flowOne.getLabel(), null);
		this.recordReturn(flowOne, flowOne.getKey(), null);
		this.recordReturn(flowOne, flowOne.getArgumentType(), Long.class);
		this.recordReturn(flowTwo, flowTwo.getLabel(), null);
		this.recordReturn(flowTwo, flowTwo.getKey(), null);
		this.recordReturn(flowTwo, flowTwo.getArgumentType(), String.class);
		this.recordReturn(this.metaData,
				this.metaData.getExtensionInterfacesMetaData(), null);

		// Attempt to load
		ManagedObjectType<?> moType = this.loadManagedObjectType(true,
				(Init<None>) null);

		// Validate dependencies
		ManagedObjectDependencyType<?>[] dependencyTypes = moType
				.getDependencyTypes();
		assertEquals("Incorrect number of dependencies", 2,
				dependencyTypes.length);
		ManagedObjectDependencyType<?> dependencyTypeOne = dependencyTypes[0];
		assertEquals("Incorrect first dependency index", 0,
				dependencyTypeOne.getIndex());
		assertNull("Should be no dependency key", dependencyTypeOne.getKey());
		assertEquals("Incorrect first dependency name",
				Integer.class.getSimpleName(),
				dependencyTypeOne.getDependencyName());
		assertEquals("Incorrect first dependency type", Integer.class,
				dependencyTypeOne.getDependencyType());
		assertEquals("Incorrect first dependency type qualification",
				"QUALIFIED", dependencyTypeOne.getTypeQualifier());
		ManagedObjectDependencyType<?> dependencyTypeTwo = dependencyTypes[1];
		assertEquals("Incorrect second dependency index", 1,
				dependencyTypeTwo.getIndex());
		assertNull("Should be no dependency key", dependencyTypeTwo.getKey());
		assertEquals("Incorrect second dependency name",
				Connection.class.getSimpleName(),
				dependencyTypeTwo.getDependencyName());
		assertEquals("Incorrect second dependency type", Connection.class,
				dependencyTypeTwo.getDependencyType());
		assertNull("Second dependency should not be qualified",
				dependencyTypeTwo.getTypeQualifier());

		// Validate flows
		ManagedObjectFlowType<?>[] flowTypes = moType.getFlowTypes();
		assertEquals("Incorrect number of dependencies", 2, flowTypes.length);
		ManagedObjectFlowType<?> flowTypeOne = flowTypes[0];
		assertEquals("Incorrect first flow index", 0, flowTypeOne.getIndex());
		assertNull("Should be no flow key", flowTypeOne.getKey());
		assertEquals("Incorrect first flow name", "0",
				flowTypeOne.getFlowName());
		assertEquals("Incorrect first flow argument type", Long.class,
				flowTypeOne.getArgumentType());
		ManagedObjectFlowType<?> flowTypeTwo = flowTypes[1];
		assertEquals("Incorrect second flow index", 1, flowTypeTwo.getIndex());
		assertNull("Should be no dependency key", flowTypeTwo.getKey());
		assertEquals("Incorrect second flow name", "1",
				flowTypeTwo.getFlowName());
		assertEquals("Incorrect second flow argument type", String.class,
				flowTypeTwo.getArgumentType());
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
	 * Records obtaining the {@link Object} and {@link ManagedObject} class from
	 * the {@link ManagedObjectSourceMetaData}.
	 */
	private void record_objectAndManagedObject() {
		this.recordReturn(this.metaData, this.metaData.getObjectClass(),
				Connection.class);
		this.recordReturn(this.metaData, this.metaData.getManagedObjectClass(),
				ManagedObject.class);
	}

	/**
	 * Records obtaining simple meta-data from the
	 * {@link ManagedObjectSourceMetaData}.
	 * 
	 * @param flowKeys
	 *            Flow keys to be defined in meta-data. Provide
	 *            <code>null</code> values for indexing.
	 */
	private <F extends Enum<?>> void record_basicMetaData(F... flowKeys) {
		this.record_objectAndManagedObject();

		// Return no dependencies
		this.recordReturn(this.metaData, this.metaData.getDependencyMetaData(),
				null);

		// Record the flows
		if (flowKeys.length == 0) {
			// Record no flows
			this.recordReturn(this.metaData, this.metaData.getFlowMetaData(),
					null);
		} else {
			// Create the meta-data for each flow
			ManagedObjectFlowMetaData<?>[] flowMetaDatas = new ManagedObjectFlowMetaData[flowKeys.length];
			for (int i = 0; i < flowMetaDatas.length; i++) {
				flowMetaDatas[i] = this
						.createMock(ManagedObjectFlowMetaData.class);
			}

			// Record the flow meta-data
			this.recordReturn(this.metaData, this.metaData.getFlowMetaData(),
					flowMetaDatas);
			for (int i = 0; i < flowMetaDatas.length; i++) {
				ManagedObjectFlowMetaData<?> flowMetaData = flowMetaDatas[i];
				this.recordReturn(flowMetaData, flowMetaData.getLabel(), null);
				this.recordReturn(flowMetaData, flowMetaData.getKey(),
						flowKeys[i]);
				this.recordReturn(flowMetaData, flowMetaData.getArgumentType(),
						Integer.class);
			}
		}

		// Record no supported extension interfaces
		this.recordReturn(this.metaData,
				this.metaData.getExtensionInterfacesMetaData(), null);
	}

	/**
	 * Records an issue.
	 * 
	 * @param issueDescription
	 *            Description of the issue.
	 */
	private void record_issue(String issueDescription) {
		this.issues.addIssue(null, null, AssetType.MANAGED_OBJECT, null,
				issueDescription);
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
		this.issues.addIssue(null, null, AssetType.MANAGED_OBJECT, null,
				issueDescription, cause);
	}

	/**
	 * Loads the {@link ManagedObjectType}.
	 * 
	 * @param isExpectedToLoad
	 *            Flag indicating if expecting to load the {@link WorkType}.
	 * @param init
	 *            {@link Init}.
	 * @param propertyNameValuePairs
	 *            {@link Property} name value pairs.
	 * @return Loaded {@link ManagedObjectType}.
	 */
	public <F extends Enum<F>> ManagedObjectType<?> loadManagedObjectType(
			boolean isExpectedToLoad, Init<F> init,
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
		OfficeFloorCompiler compiler = OfficeFloorCompiler
				.newOfficeFloorCompiler(null);
		compiler.setCompilerIssues(this.issues);
		ManagedObjectLoader moLoader = compiler.getManagedObjectLoader();
		MockManagedObjectSource.init = init;
		ManagedObjectType<?> moType = moLoader.loadManagedObjectType(
				MockManagedObjectSource.class, propertyList);

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
		 * @param context
		 *            {@link ManagedObjectSourceContext}.
		 * @param util
		 *            {@link InitUtil}.
		 */
		void init(ManagedObjectSourceContext<F> context, InitUtil util);
	}

	/**
	 * Provides useful functionality for {@link Init}.
	 */
	private class InitUtil {

		/**
		 * Obtains the {@link WorkFactory}.
		 * 
		 * @return {@link WorkFactory}.
		 */
		public WorkFactory<Work> getWorkFactory() {
			return LoadHttpSecurityTypeTest.this.workFactory;
		}

		/**
		 * Obtains the {@link TaskFactory}.
		 * 
		 * @return {@link TaskFactory}.
		 */
		public TaskFactory<Work, ?, ?> getTaskFactory() {
			return LoadHttpSecurityTypeTest.this.taskFactory;
		}
	}

	/**
	 * Mock {@link ManagedObjectSource}.
	 */
	@TestSource
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static class MockManagedObjectSource implements
			ManagedObjectSource<None, None> {

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
		 * @param metaData
		 *            {@link ManagedObjectSourceMetaData}.
		 * @param initUtil
		 *            {@link InitUtil}.
		 */
		public static void reset(ManagedObjectSourceMetaData<?, ?> metaData,
				InitUtil initUtil) {
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
		public void init(ManagedObjectSourceContext context) throws Exception {
			// Run the init if available
			if (init != null) {
				init.init(context, initUtil);
			}
		}

		@Override
		public ManagedObjectSourceMetaData getMetaData() {

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