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
package net.officefloor.compile.impl.managedobject;

import java.sql.Connection;
import java.util.Properties;

import javax.transaction.xa.XAResource;

import net.officefloor.compile.impl.properties.PropertyListImpl;
import net.officefloor.compile.impl.work.LoadWorkTypeTest;
import net.officefloor.compile.issues.CompilerIssues;
import net.officefloor.compile.issues.CompilerIssues.LocationType;
import net.officefloor.compile.managedobject.ManagedObjectDependencyType;
import net.officefloor.compile.managedobject.ManagedObjectFlowType;
import net.officefloor.compile.managedobject.ManagedObjectLoader;
import net.officefloor.compile.managedobject.ManagedObjectTeamType;
import net.officefloor.compile.managedobject.ManagedObjectType;
import net.officefloor.compile.properties.Property;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.work.WorkType;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.build.TaskFactory;
import net.officefloor.frame.api.build.WorkFactory;
import net.officefloor.frame.api.build.OfficeFloorIssues.AssetType;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.internal.structure.FlowInstigationStrategyEnum;
import net.officefloor.frame.internal.structure.ManagedObjectMetaData;
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
import net.officefloor.frame.spi.managedobject.source.ResourceLocator;
import net.officefloor.frame.spi.team.Team;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.model.officefloor.OfficeFloorModel;

/**
 * Tests loading the {@link ManagedObjectType}.
 * 
 * @author Daniel
 */
public class LoadManagedObjectTypeTest extends OfficeFrameTestCase {

	/**
	 * Location of the {@link OfficeFloorModel}.
	 */
	private final String OFFICE_FLOOR_LOCATION = "OFFICE_FLOOR";

	/**
	 * Name of the {@link ManagedObjectSource}.
	 */
	private final String MANAGED_OBJECT_NAME = "MO";

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

	/*
	 * (non-Javadoc)
	 * 
	 * @see junit.framework.TestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception {
		MockManagedObjectSource.reset(this.metaData, new InitUtil());
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
				assertEquals("Ensure get property ONE", "1", context
						.getProperty("ONE"));
				assertEquals("Ensure get property TWO", "2", context
						.getProperty("TWO"));
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
	 * Ensure able to get the {@link ResourceLocator}.
	 */
	public void testGetResourceLocator() {

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
						LoadWorkTypeTest.class.getClassLoader().getResource(
								objectPath), context.getResourceLocator()
								.locateURL(objectPath));
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
		this
				.record_issue("Null ManagedObjectDependencyMetaData for dependency 0");

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
		this
				.record_issue("No type for dependency 0 (key=<indexed>, label=NO TYPE)");

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
		this.recordReturn(dependencyTwo, dependencyTwo.getLabel(), null);
		this.recordReturn(dependencyTwo, dependencyTwo.getKey(), TwoKey.ONE);
		this.recordReturn(dependencyTwo, dependencyTwo.getType(), String.class);
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
		this
				.recordReturn(this.metaData, this.metaData.getFlowMetaData(),
						new ManagedObjectFlowMetaData[] { flowDefaulted,
								flowProvided });
		this.recordReturn(flowDefaulted, flowDefaulted.getLabel(), "DEFAULTED");
		this.recordReturn(flowDefaulted, flowDefaulted.getKey(), null);
		this.recordReturn(flowDefaulted, flowDefaulted.getArgumentType(), null);
		this.recordReturn(flowProvided, flowProvided.getLabel(), null);
		this.recordReturn(flowProvided, flowProvided.getKey(), null);
		this.recordReturn(flowProvided, flowProvided.getArgumentType(),
				Connection.class);
		this.recordReturn(this.metaData, this.metaData
				.getExtensionInterfacesMetaData(), null);

		// Attempt to load
		ManagedObjectType<?> moType = this.loadManagedObjectType(true,
				(Init<None>) null);

		// Validate argument types of flows
		ManagedObjectFlowType<?>[] flowTypes = moType.getFlowTypes();
		assertEquals("Incorrect number of flows", 2, flowTypes.length);
		ManagedObjectFlowType<?> defaulted = flowTypes[0];
		assertEquals("Incorrect name for defaulted argument flow", "DEFAULTED",
				defaulted.getFlowName());
		assertEquals("Incorrect defaulted argument type", Void.class, defaulted
				.getArgumentType());
		ManagedObjectFlowType<?> provided = flowTypes[1];
		assertEquals("Incorrect name for provided argument flow", "1", provided
				.getFlowName());
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
		this
				.recordReturn(dependencyOne, dependencyOne.getType(),
						Integer.class);
		this.recordReturn(dependencyTwo, dependencyTwo.getLabel(), null);
		this.recordReturn(dependencyTwo, dependencyTwo.getKey(), TwoKey.ONE); // order
		this.recordReturn(dependencyTwo, dependencyTwo.getType(),
				Connection.class);
		this.recordReturn(this.metaData, this.metaData.getFlowMetaData(),
				new ManagedObjectFlowMetaData[] { flowOne, flowTwo });
		this.recordReturn(flowOne, flowOne.getLabel(), null);
		this.recordReturn(flowOne, flowOne.getKey(), TwoKey.TWO); // order
		this.recordReturn(flowOne, flowOne.getArgumentType(), Long.class);
		this.recordReturn(flowTwo, flowTwo.getLabel(), null);
		this.recordReturn(flowTwo, flowTwo.getKey(), TwoKey.ONE); // order
		this.recordReturn(flowTwo, flowTwo.getArgumentType(), String.class);
		this.recordReturn(this.metaData, this.metaData
				.getExtensionInterfacesMetaData(), null);

		// Attempt to load
		ManagedObjectType<?> moType = this.loadManagedObjectType(true,
				(Init<None>) null);

		// Validate dependencies ordered and correct values
		ManagedObjectDependencyType<?>[] dependencyTypes = moType
				.getDependencyTypes();
		assertEquals("Incorrect number of dependencies", 2,
				dependencyTypes.length);
		ManagedObjectDependencyType<?> dependencyTypeOne = dependencyTypes[0];
		assertEquals("Keys should be ordered", TwoKey.ONE, dependencyTypeOne
				.getKey());
		assertEquals("Incorrect first dependency index", TwoKey.ONE.ordinal(),
				dependencyTypeOne.getIndex());
		assertEquals("Incorrect first dependency name", TwoKey.ONE.toString(),
				dependencyTypeOne.getDependencyName());
		assertEquals("Incorrect first dependency type", Connection.class,
				dependencyTypeOne.getDependencyType());
		ManagedObjectDependencyType<?> dependencyTypeTwo = dependencyTypes[1];
		assertEquals("Keys should be ordered", TwoKey.TWO, dependencyTypeTwo
				.getKey());
		assertEquals("Incorrect second dependency index", TwoKey.TWO.ordinal(),
				dependencyTypeTwo.getIndex());
		assertEquals("Incorrect second dependency name", TwoKey.TWO.toString(),
				dependencyTypeTwo.getDependencyName());
		assertEquals("Incorrect second dependency type", Integer.class,
				dependencyTypeTwo.getDependencyType());

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
		this
				.recordReturn(dependencyOne, dependencyOne.getType(),
						Integer.class);
		this.recordReturn(dependencyTwo, dependencyTwo.getLabel(), null);
		this.recordReturn(dependencyTwo, dependencyTwo.getKey(), null);
		this.recordReturn(dependencyTwo, dependencyTwo.getType(),
				Connection.class);
		this.recordReturn(this.metaData, this.metaData.getFlowMetaData(),
				new ManagedObjectFlowMetaData[] { flowOne, flowTwo });
		this.recordReturn(flowOne, flowOne.getLabel(), null);
		this.recordReturn(flowOne, flowOne.getKey(), null);
		this.recordReturn(flowOne, flowOne.getArgumentType(), Long.class);
		this.recordReturn(flowTwo, flowTwo.getLabel(), null);
		this.recordReturn(flowTwo, flowTwo.getKey(), null);
		this.recordReturn(flowTwo, flowTwo.getArgumentType(), String.class);
		this.recordReturn(this.metaData, this.metaData
				.getExtensionInterfacesMetaData(), null);

		// Attempt to load
		ManagedObjectType<?> moType = this.loadManagedObjectType(true,
				(Init<None>) null);

		// Validate dependencies
		ManagedObjectDependencyType<?>[] dependencyTypes = moType
				.getDependencyTypes();
		assertEquals("Incorrect number of dependencies", 2,
				dependencyTypes.length);
		ManagedObjectDependencyType<?> dependencyTypeOne = dependencyTypes[0];
		assertEquals("Incorrect first dependency index", 0, dependencyTypeOne
				.getIndex());
		assertNull("Should be no dependency key", dependencyTypeOne.getKey());
		assertEquals("Incorrect first dependency name", Integer.class
				.getSimpleName(), dependencyTypeOne.getDependencyName());
		assertEquals("Incorrect first dependency type", Integer.class,
				dependencyTypeOne.getDependencyType());
		ManagedObjectDependencyType<?> dependencyTypeTwo = dependencyTypes[1];
		assertEquals("Incorrect second dependency index", 1, dependencyTypeTwo
				.getIndex());
		assertNull("Should be no dependency key", dependencyTypeTwo.getKey());
		assertEquals("Incorrect second dependency name", Connection.class
				.getSimpleName(), dependencyTypeTwo.getDependencyName());
		assertEquals("Incorrect second dependency type", Connection.class,
				dependencyTypeTwo.getDependencyType());

		// Validate flows
		ManagedObjectFlowType<?>[] flowTypes = moType.getFlowTypes();
		assertEquals("Incorrect number of dependencies", 2, flowTypes.length);
		ManagedObjectFlowType<?> flowTypeOne = flowTypes[0];
		assertEquals("Incorrect first flow index", 0, flowTypeOne.getIndex());
		assertNull("Should be no flow key", flowTypeOne.getKey());
		assertEquals("Incorrect first flow name", "0", flowTypeOne
				.getFlowName());
		assertEquals("Incorrect first flow argument type", Long.class,
				flowTypeOne.getArgumentType());
		ManagedObjectFlowType<?> flowTypeTwo = flowTypes[1];
		assertEquals("Incorrect second flow index", 1, flowTypeTwo.getIndex());
		assertNull("Should be no dependency key", flowTypeTwo.getKey());
		assertEquals("Incorrect second flow name", "1", flowTypeTwo
				.getFlowName());
		assertEquals("Incorrect second flow argument type", String.class,
				flowTypeTwo.getArgumentType());
	}

	/**
	 * Ensure issue if <code>null</code> extension interface meta-data.
	 */
	public void testNullExtensionInterfaceMetaData() {

		// Record null extension interface meta-data
		this.record_objectAndManagedObject();
		this.recordReturn(this.metaData, this.metaData.getDependencyMetaData(),
				null);
		this.recordReturn(this.metaData, this.metaData.getFlowMetaData(), null);
		this.recordReturn(this.metaData, this.metaData
				.getExtensionInterfacesMetaData(),
				new ManagedObjectExtensionInterfaceMetaData[] { null });
		this.record_issue("Null extension interface meta-data");

		// Attempt to load
		this.loadManagedObjectType(false, (Init<None>) null);
	}

	/**
	 * Ensure issue if <code>null</code> extension interface type.
	 */
	public void testNullExtensionInterfaceType() {

		final ManagedObjectExtensionInterfaceMetaData<?> eiMetaData = this
				.createMock(ManagedObjectExtensionInterfaceMetaData.class);

		// Record null extension interface type
		this.record_objectAndManagedObject();
		this.recordReturn(this.metaData, this.metaData.getDependencyMetaData(),
				null);
		this.recordReturn(this.metaData, this.metaData.getFlowMetaData(), null);
		this.recordReturn(this.metaData, this.metaData
				.getExtensionInterfacesMetaData(),
				new ManagedObjectExtensionInterfaceMetaData[] { eiMetaData });
		this.recordReturn(eiMetaData, eiMetaData.getExtensionInterfaceType(),
				null);
		this.record_issue("Null extension interface type");

		// Attempt to load
		this.loadManagedObjectType(false, (Init<None>) null);
	}

	/**
	 * Ensure issue if no {@link ExtensionInterfaceFactory} for an extension
	 * interface.
	 */
	public void testNoFactoryForExtensionInterface() {

		final ManagedObjectExtensionInterfaceMetaData<?> eiMetaData = this
				.createMock(ManagedObjectExtensionInterfaceMetaData.class);

		// Record null extension interface factory
		this.record_objectAndManagedObject();
		this.recordReturn(this.metaData, this.metaData.getDependencyMetaData(),
				null);
		this.recordReturn(this.metaData, this.metaData.getFlowMetaData(), null);
		this.recordReturn(this.metaData, this.metaData
				.getExtensionInterfacesMetaData(),
				new ManagedObjectExtensionInterfaceMetaData[] { eiMetaData });
		this.recordReturn(eiMetaData, eiMetaData.getExtensionInterfaceType(),
				XAResource.class);
		this.recordReturn(eiMetaData,
				eiMetaData.getExtensionInterfaceFactory(), null);
		this.record_issue("No extension interface factory (type="
				+ XAResource.class.getName() + ")");

		// Attempt to load
		this.loadManagedObjectType(false, (Init<None>) null);
	}

	/**
	 * Ensure can define extension interface.
	 */
	public void testExtensionInterface() {

		final ManagedObjectExtensionInterfaceMetaData<?> eiMetaData = this
				.createMock(ManagedObjectExtensionInterfaceMetaData.class);
		final ExtensionInterfaceFactory<?> factory = this
				.createMock(ExtensionInterfaceFactory.class);

		// Record null extension interface factory
		this.record_objectAndManagedObject();
		this.recordReturn(this.metaData, this.metaData.getDependencyMetaData(),
				null);
		this.recordReturn(this.metaData, this.metaData.getFlowMetaData(), null);
		this.recordReturn(this.metaData, this.metaData
				.getExtensionInterfacesMetaData(),
				new ManagedObjectExtensionInterfaceMetaData[] { eiMetaData });
		this.recordReturn(eiMetaData, eiMetaData.getExtensionInterfaceType(),
				XAResource.class);
		this.recordReturn(eiMetaData,
				eiMetaData.getExtensionInterfaceFactory(), factory);

		// Attempt to load
		ManagedObjectType<?> moType = this.loadManagedObjectType(true,
				(Init<None>) null);

		// Ensure provide extension interface
		assertEquals("Incorrect number of extension interfaces", 1, moType
				.getExtensionInterfaces().length);
		assertEquals("Incorrect extension interface type", XAResource.class,
				moType.getExtensionInterfaces()[0]);
	}

	/**
	 * Ensure issue if {@link Work} added without {@link Task} instances.
	 */
	public void testAddWorkWithNoTasks() {

		// Record no tasks for work
		this.record_basicMetaData();
		this.record_issue("No tasks added for work (work=WORK)");

		// Attempt to load
		this.loadManagedObjectType(false, new Init<None>() {
			@Override
			public void init(ManagedObjectSourceContext<None> context,
					InitUtil util) {
				context.addWork("WORK", util.getWorkFactory());
			}
		});
	}

	/**
	 * Ensure issue if {@link Task} added without a {@link Work} name.
	 */
	public void testAddTaskWithNoWorkName() {

		// Record no work name for task
		this.record_basicMetaData();
		this.record_issue("Work added without a name");

		// Attempt to load
		this.loadManagedObjectType(false, new Init<None>() {
			@Override
			public void init(ManagedObjectSourceContext<None> context,
					InitUtil util) {
				context.addWork(null, util.getWorkFactory()).addTask("TASK",
						util.getTaskFactory());
			}
		});
	}

	/**
	 * Ensure issue if {@link Task} added without a {@link Task} name.
	 */
	public void testAddTaskWithNoTaskName() {

		// Record no name for task
		this.record_basicMetaData();
		this.record_issue("Task added without a name (work=WORK)");

		// Attempt to load
		this.loadManagedObjectType(false, new Init<None>() {
			@Override
			public void init(ManagedObjectSourceContext<None> context,
					InitUtil util) {
				context.addWork("WORK", util.getWorkFactory()).addTask(null,
						util.getTaskFactory());
			}
		});
	}

	/**
	 * Ensure issue if {@link Task} added without a {@link Team}.
	 */
	public void testAddTaskWithoutTeam() {

		// Record no team for task
		this.record_basicMetaData();
		this.record_issue("Must specify team for task (work=WORK, task=TASK)");

		// Attempt to load
		this.loadManagedObjectType(false, new Init<None>() {
			@Override
			public void init(ManagedObjectSourceContext<None> context,
					InitUtil util) {
				context.addWork("WORK", util.getWorkFactory()).addTask("TASK",
						util.getTaskFactory());
			}
		});
	}

	/**
	 * Ensures {@link ManagedObjectTeamType} is available for the added
	 * {@link Task}.
	 */
	public void testAddTask() {

		// Record basic meta-data
		this.record_basicMetaData();

		// Attempt to load
		ManagedObjectType<?> moType = this.loadManagedObjectType(true,
				new Init<None>() {
					@Override
					public void init(ManagedObjectSourceContext<None> context,
							InitUtil util) {
						context.addWork("WORK", util.getWorkFactory()).addTask(
								"TASK", util.getTaskFactory()).setTeam("TEAM");
					}
				});

		// Ensure only team added
		assertEquals("Should have no flows", 0, moType.getFlowTypes().length);
		ManagedObjectTeamType[] teams = moType.getTeamTypes();
		assertEquals("Incorrect number of teams", 1, teams.length);
		assertEquals("Incorrect team", "TEAM", teams[0].getTeamName());
	}

	/**
	 * Ensures {@link ManagedObjectTeamType} is available for the added recycle
	 * {@link Task}.
	 */
	public void testAddRecycleTask() {

		// Record basic meta-data
		this.record_basicMetaData();

		// Attempt to load
		ManagedObjectType<?> moType = this.loadManagedObjectType(true,
				new Init<None>() {
					@Override
					public void init(ManagedObjectSourceContext<None> context,
							InitUtil util) {
						context.getRecycleWork(util.getWorkFactory()).addTask(
								"TASK", util.getTaskFactory()).setTeam("TEAM");
					}
				});

		// Ensure only team added
		assertEquals("Should have no flows", 0, moType.getFlowTypes().length);
		ManagedObjectTeamType[] teams = moType.getTeamTypes();
		assertEquals("Incorrect number of teams", 1, teams.length);
		assertEquals("Incorrect team", "TEAM", teams[0].getTeamName());
	}

	/**
	 * Ensures only a single {@link ManagedObjectTeamType} is available
	 * {@link Task} instances added using the same {@link Team}.
	 */
	public void testAddTasksWithSameTeam() {

		final String TEAM_NAME = "TEAM";

		// Record basic meta-data
		this.record_basicMetaData();

		// Attempt to load
		ManagedObjectType<?> moType = this.loadManagedObjectType(true,
				new Init<None>() {
					@Override
					public void init(ManagedObjectSourceContext<None> context,
							InitUtil util) {
						// Recycle task uses same team
						context.getRecycleWork(util.getWorkFactory()).addTask(
								"TASK", util.getTaskFactory()).setTeam(
								TEAM_NAME);

						// Work with two tasks (both using team)
						ManagedObjectWorkBuilder<Work> work = context.addWork(
								"WORK", util.getWorkFactory());
						work.addTask("TASK_ONE", util.getTaskFactory())
								.setTeam(TEAM_NAME);
						work.addTask("TASK_TWO", util.getTaskFactory())
								.setTeam(TEAM_NAME);
					}
				});

		// Ensure only team added
		assertEquals("Should have no flows", 0, moType.getFlowTypes().length);
		ManagedObjectTeamType[] teams = moType.getTeamTypes();
		assertEquals("Incorrect number of teams", 1, teams.length);
		assertEquals("Incorrect team", TEAM_NAME, teams[0].getTeamName());
	}

	/**
	 * Ensures only a multiple {@link ManagedObjectTeamType} are available for
	 * added {@link Task} instances that use various {@link Team} instances.
	 */
	public void testMultipleTeams() {

		final String TEAM_ONE = "TEAM_ONE";
		final String TEAM_TWO = "TEAM_TWO";

		// Record basic meta-data
		this.record_basicMetaData();

		// Attempt to load
		ManagedObjectType<?> moType = this.loadManagedObjectType(true,
				new Init<None>() {
					@Override
					public void init(ManagedObjectSourceContext<None> context,
							InitUtil util) {
						// Work with two tasks (using different teams)
						ManagedObjectWorkBuilder<Work> work = context.addWork(
								"WORK", util.getWorkFactory());
						work.addTask("TASK_ONE", util.getTaskFactory())
								.setTeam(TEAM_ONE);
						work.addTask("TASK_TWO", util.getTaskFactory())
								.setTeam(TEAM_TWO);
					}
				});

		// Ensure only team added
		assertEquals("Should have no flows", 0, moType.getFlowTypes().length);
		ManagedObjectTeamType[] teams = moType.getTeamTypes();
		assertEquals("Incorrect number of teams", 2, teams.length);
		assertEquals("Incorrect first team", TEAM_ONE, teams[0].getTeamName());
		assertEquals("Incorrect second team", TEAM_TWO, teams[1].getTeamName());
	}

	/**
	 * Ensures issue if link {@link Task} to another {@link Task} with
	 * <code>null</code> {@link FlowInstigationStrategyEnum}.
	 */
	public void testAddTaskLinkWithNoInstigationStrategy() {

		// Record basic meta-data
		this.record_basicMetaData();
		this
				.record_issue("No instigation strategy for flow (work=WORK, task=TASK, flow=0)");

		// Attempt to load
		this.loadManagedObjectType(false, new Init<None>() {
			@Override
			public void init(ManagedObjectSourceContext<None> context,
					InitUtil util) {
				ManagedObjectTaskBuilder<?, ?> task = context.addWork("WORK",
						util.getWorkFactory()).addTask("TASK",
						util.getTaskFactory());
				task.setTeam("TEAM");
				task.linkFlow(0, "WORK", "TASK", null, String.class);
			}
		});
	}

	/**
	 * Ensures issue if link {@link Task} to another {@link Task} providing only
	 * a {@link Work} name.
	 */
	public void testAddTaskLinkWithWorkNameButNoTaskName() {

		// Record basic meta-data
		this.record_basicMetaData();
		this
				.record_issue("No task name for flow (work=WORK, task=TASK, flow=0, link work=LINK_WORK)");

		// Attempt to load
		this.loadManagedObjectType(false, new Init<None>() {
			@Override
			public void init(ManagedObjectSourceContext<None> context,
					InitUtil util) {
				ManagedObjectTaskBuilder<?, ?> task = context.addWork("WORK",
						util.getWorkFactory()).addTask("TASK",
						util.getTaskFactory());
				task.setTeam("TEAM");
				task.linkFlow(0, "LINK_WORK", null,
						FlowInstigationStrategyEnum.SEQUENTIAL, null);
			}
		});
	}

	/**
	 * Ensures issue if an added {@link Task} is linking an unknown added
	 * {@link Task}.
	 */
	public void testAddTaskLinkingUnknownFlow() {

		// Record basic meta-data
		this.record_basicMetaData();
		this
				.record_issue("Unknown task being linked (work=WORK, task=TASK, flow=0, link work=LINK_WORK, link task=LINK_TASK)");

		// Attempt to load
		this.loadManagedObjectType(false, new Init<None>() {
			@Override
			public void init(ManagedObjectSourceContext<None> context,
					InitUtil util) {
				ManagedObjectTaskBuilder<?, ?> task = context.addWork("WORK",
						util.getWorkFactory()).addTask("TASK",
						util.getTaskFactory());
				task.setTeam("TEAM");
				task.linkFlow(0, "LINK_WORK", "LINK_TASK",
						FlowInstigationStrategyEnum.SEQUENTIAL, null);
			}
		});
	}

	/**
	 * Ensures only the {@link ManagedObjectTeamType} is available for the added
	 * {@link Task} with a link to another added {@link Task}.
	 */
	public void testAddTaskWithLinkToAddedTask() {

		// Record basic meta-data
		this.record_basicMetaData();

		// Attempt to load
		ManagedObjectType<?> moType = this.loadManagedObjectType(true,
				new Init<None>() {
					@Override
					public void init(ManagedObjectSourceContext<None> context,
							InitUtil util) {
						// Add task that links to other task
						ManagedObjectTaskBuilder<?, ?> linkTask = context
								.addWork("WORK", util.getWorkFactory())
								.addTask("TASK", util.getTaskFactory());
						linkTask.setTeam("TEAM");
						linkTask.linkFlow(0, "LINK_WORK", "LINK_TASK",
								FlowInstigationStrategyEnum.SEQUENTIAL, null);

						// Add task being linked too
						ManagedObjectTaskBuilder<?, ?> targetTask = context
								.addWork("LINK_WORK", util.getWorkFactory())
								.addTask("LINK_TASK", util.getTaskFactory());
						targetTask.setTeam("TEAM");
					}
				});

		// Ensure only team added
		assertEquals("Should have no flows", 0, moType.getFlowTypes().length);
		ManagedObjectTeamType[] teams = moType.getTeamTypes();
		assertEquals("Incorrect number of teams", 1, teams.length);
		assertEquals("Incorrect team", "TEAM", teams[0].getTeamName());
	}

	/**
	 * Ensures a {@link ManagedObjectFlowType} is added for the link from the
	 * added {@link Task}.
	 */
	public void testAddTaskRequiringFlow() {

		// Record basic meta-data
		this.record_basicMetaData();

		// Attempt to load
		ManagedObjectType<?> moType = this.loadManagedObjectType(true,
				new Init<None>() {
					@Override
					public void init(ManagedObjectSourceContext<None> context,
							InitUtil util) {
						// Add task that links to other task
						ManagedObjectTaskBuilder<?, ?> linkTask = context
								.addWork("WORK", util.getWorkFactory())
								.addTask("TASK", util.getTaskFactory());
						linkTask.setTeam("TEAM");
						linkTask.linkFlow(0, null,
								FlowInstigationStrategyEnum.SEQUENTIAL,
								Connection.class);
					}
				});

		// Ensure Flow added
		ManagedObjectFlowType<?>[] flows = moType.getFlowTypes();
		assertEquals("Should have a flow", 1, flows.length);
		ManagedObjectFlowType<?> flow = flows[0];
		assertEquals("Incorrect work name", "WORK", flow.getWorkName());
		assertEquals("Incorrect task name", "TASK", flow.getTaskName());
		assertEquals("Incorrect flow name", "0", flow.getFlowName());
		assertEquals("Incorrect index", 0, flow.getIndex());
		assertNull("Flow should not have key", flow.getKey());
		assertEquals("Incorrect argument type", Connection.class, flow
				.getArgumentType());
	}

	/**
	 * Ensures issue if {@link ManagedObjectSourceMetaData} uses keys for
	 * {@link ManagedObjectFlowMetaData} but linking a process is done with an
	 * index.
	 */
	public void testLinkProcessWithoutRequiredKey() {

		// Record
		this.record_basicMetaData(OneKey.KEY);
		this
				.record_issue("ManagedObjectFlowMetaData requires linking by keys (not indexes)");

		// Attempt to load
		this.loadManagedObjectType(false, new Init<OneKey>() {
			@Override
			public void init(ManagedObjectSourceContext<OneKey> context,
					InitUtil util) {
				context.linkProcess(0, "WORK", "TASK");
			}
		});
	}

	/**
	 * Ensures issue if {@link ManagedObjectSourceMetaData} uses keys for
	 * {@link ManagedObjectFlowMetaData} but linking by different key type.
	 */
	@SuppressWarnings("unchecked")
	public void testLinkProcessWithInvalidKey() {

		// Record
		this.record_basicMetaData(OneKey.KEY);
		this
				.record_issue("Link key does not match type for ManagedObjectFlowMetaData (meta-data key type="
						+ OneKey.class.getName()
						+ ", link key type="
						+ InvalidKey.class.getName()
						+ ", link key="
						+ InvalidKey.INVALID + ")");

		// Attempt to load
		this.loadManagedObjectType(false, new Init() {
			@Override
			public void init(ManagedObjectSourceContext context, InitUtil util) {
				context.linkProcess(InvalidKey.INVALID, "WORK", "TASK");
			}
		});
	}

	/**
	 * Ensures issue if {@link ManagedObjectSourceMetaData} uses indexing for
	 * {@link ManagedObjectFlowMetaData} but linking a process is done with a
	 * key.
	 */
	@SuppressWarnings("unchecked")
	public void testLinkProcessWithKeyWhenIndexed() {

		// Record
		this.record_basicMetaData((Indexed) null);
		this
				.record_issue("ManagedObjectFlowMetaData requires linking by indexes (not keys)");

		// Attempt to load
		this.loadManagedObjectType(false, new Init() {
			@Override
			public void init(ManagedObjectSourceContext context, InitUtil util) {
				context.linkProcess(OneKey.KEY, "WORK", "TASK");
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
		this
				.record_issue("ManagedObjectFlowMetaData does not define index (index=1)");

		// Attempt to load
		this.loadManagedObjectType(false, new Init<Indexed>() {
			@Override
			public void init(ManagedObjectSourceContext<Indexed> context,
					InitUtil util) {
				context.linkProcess(0, "LINKED", "LINKED");
				context.linkProcess(1, "WORK", "TASK");

				// Add the linked task
				context.addWork("LINKED", util.getWorkFactory()).addTask(
						"LINKED", util.getTaskFactory()).setTeam("TEAM");
			}
		});
	}

	/**
	 * Ensures issue if link process without a {@link Work} name.
	 */
	public void testLinkProcessWithoutWorkName() {

		// Record
		this.record_basicMetaData((Indexed) null);
		this
				.record_issue("Must provide work name for linked process 0 (key=<indexed>)");

		// Attempt to load
		this.loadManagedObjectType(false, new Init<Indexed>() {
			@Override
			public void init(ManagedObjectSourceContext<Indexed> context,
					InitUtil util) {
				context.linkProcess(0, null, "TASK");
			}
		});
	}

	/**
	 * Ensures issue if link process without a {@link Task} name.
	 */
	public void testLinkProcessWithoutTaskName() {

		// Record
		this.record_basicMetaData(OneKey.KEY);
		this.record_issue("Must provide task name for linked process 0 (key="
				+ OneKey.KEY + ")");

		// Attempt to load
		this.loadManagedObjectType(false, new Init<OneKey>() {
			@Override
			public void init(ManagedObjectSourceContext<OneKey> context,
					InitUtil util) {
				context.linkProcess(OneKey.KEY, "WORK", null);
			}
		});
	}

	/**
	 * Ensures issue if linking {@link ManagedObjectFlowMetaData} to an unknown
	 * added {@link Task}.
	 */
	public void testLinkProcessToUnknownTask() {

		// Record
		this.record_basicMetaData(OneKey.KEY);
		this.record_issue("Unknown task for linked process 0 (key="
				+ OneKey.KEY + ", link work=WORK, link task=TASK)");

		// Attempt to load
		this.loadManagedObjectType(false, new Init<OneKey>() {
			@Override
			public void init(ManagedObjectSourceContext<OneKey> context,
					InitUtil util) {
				context.linkProcess(OneKey.KEY, "WORK", "TASK");
			}
		});
	}

	/**
	 * Ensures can link to added {@link Task} which removes the
	 * {@link ManagedObjectFlowType} for the {@link ManagedObjectFlowMetaData}
	 * from being included in the {@link ManagedObjectType}.
	 */
	public void testLinkProcessToTask() {

		// Record
		this.record_basicMetaData((Indexed) null);

		// Attempt to load
		ManagedObjectType<?> moType = this.loadManagedObjectType(true,
				new Init<Indexed>() {
					@Override
					public void init(
							ManagedObjectSourceContext<Indexed> context,
							InitUtil util) {
						// Add task being linked too
						context.addWork("WORK", util.getWorkFactory()).addTask(
								"TASK", util.getTaskFactory()).setTeam("TEAM");

						// Link to task
						context.linkProcess(0, "WORK", "TASK");
					}
				});

		// Should only have team of task (as linked process hidden)
		assertEquals("Should have not flows", 0, moType.getFlowTypes().length);
		assertEquals("Incorrect number of teams", 1,
				moType.getTeamTypes().length);
		assertEquals("Incorrect team", "TEAM", moType.getTeamTypes()[0]
				.getTeamName());
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
		this.recordReturn(this.metaData, this.metaData
				.getExtensionInterfacesMetaData(), null);
	}

	/**
	 * Records an issue.
	 * 
	 * @param issueDescription
	 *            Description of the issue.
	 */
	private void record_issue(String issueDescription) {
		this.issues
				.addIssue(LocationType.OFFICE_FLOOR, OFFICE_FLOOR_LOCATION,
						AssetType.MANAGED_OBJECT, MANAGED_OBJECT_NAME,
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
		this.issues.addIssue(LocationType.OFFICE_FLOOR, OFFICE_FLOOR_LOCATION,
				AssetType.MANAGED_OBJECT, MANAGED_OBJECT_NAME,
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
		ManagedObjectLoader moLoader = new ManagedObjectLoaderImpl(
				LocationType.OFFICE_FLOOR, OFFICE_FLOOR_LOCATION,
				MANAGED_OBJECT_NAME);
		MockManagedObjectSource.init = init;
		ManagedObjectType<?> moType = moLoader.loadManagedObject(
				MockManagedObjectSource.class, propertyList,
				LoadManagedObjectTypeTest.class.getClassLoader(), this.issues);

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
			return LoadManagedObjectTypeTest.this.workFactory;
		}

		/**
		 * Obtains the {@link TaskFactory}.
		 * 
		 * @return {@link TaskFactory}.
		 */
		public TaskFactory<Work, ?, ?> getTaskFactory() {
			return LoadManagedObjectTypeTest.this.taskFactory;
		}
	}

	/**
	 * Mock {@link ManagedObjectSource}.
	 */
	@SuppressWarnings("unchecked")
	public static class MockManagedObjectSource implements ManagedObjectSource {

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
			return;
		}

		@Override
		public void sourceManagedObject(ManagedObjectUser user) {
			fail("Should not source ManagedObject");
			return;
		}
	}

}