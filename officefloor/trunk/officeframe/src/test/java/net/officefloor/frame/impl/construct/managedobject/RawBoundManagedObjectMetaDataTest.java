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
package net.officefloor.frame.impl.construct.managedobject;

import java.util.HashMap;
import java.util.Map;

import net.officefloor.frame.api.build.OfficeFloorIssues;
import net.officefloor.frame.api.build.OfficeFloorIssues.AssetType;
import net.officefloor.frame.api.execute.Handler;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.impl.execute.managedobject.ManagedObjectIndexImpl;
import net.officefloor.frame.internal.configuration.ManagedObjectConfiguration;
import net.officefloor.frame.internal.configuration.ManagedObjectDependencyConfiguration;
import net.officefloor.frame.internal.construct.AssetManagerFactory;
import net.officefloor.frame.internal.construct.OfficeMetaDataLocator;
import net.officefloor.frame.internal.construct.RawBoundManagedObjectMetaData;
import net.officefloor.frame.internal.construct.RawManagedObjectMetaData;
import net.officefloor.frame.internal.construct.RawOfficeManagingManagedObjectMetaData;
import net.officefloor.frame.internal.structure.Asset;
import net.officefloor.frame.internal.structure.AssetManager;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.FlowMetaData;
import net.officefloor.frame.internal.structure.JobNode;
import net.officefloor.frame.internal.structure.ManagedObjectIndex;
import net.officefloor.frame.internal.structure.ManagedObjectMetaData;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.internal.structure.OfficeMetaData;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.frame.internal.structure.ThreadState;
import net.officefloor.frame.internal.structure.WorkContainer;
import net.officefloor.frame.internal.structure.WorkMetaData;
import net.officefloor.frame.spi.managedobject.AsynchronousManagedObject;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.ObjectRegistry;
import net.officefloor.frame.spi.managedobject.pool.ManagedObjectPool;
import net.officefloor.frame.spi.managedobject.recycle.RecycleManagedObjectParameter;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceMetaData;
import net.officefloor.frame.test.OfficeFrameTestCase;

import org.easymock.AbstractMatcher;

/**
 * Test the {@link RawBoundManagedObjectMetaDataImpl}.
 * 
 * @author Daniel
 */
public class RawBoundManagedObjectMetaDataTest extends OfficeFrameTestCase {

	/**
	 * {@link ManagedObjectConfiguration} for single {@link ManagedObject}
	 * testing.
	 */
	ManagedObjectConfiguration<?> managedObjectConfiguration = this
			.createMock(ManagedObjectConfiguration.class);

	/**
	 * {@link OfficeFloorIssues}.
	 */
	private final OfficeFloorIssues issues = this
			.createMock(OfficeFloorIssues.class);

	/**
	 * {@link ManagedObjectScope}.
	 */
	private ManagedObjectScope managedObjectScope = ManagedObjectScope.THREAD;

	/**
	 * {@link AssetType}.
	 */
	private AssetType assetType = AssetType.OFFICE;

	/**
	 * Name of the {@link Asset}.
	 */
	private String assetName = "OFFICE";

	/**
	 * {@link AssetManagerFactory}.
	 */
	private AssetManagerFactory assetManagerFactory = this
			.createMock(AssetManagerFactory.class);

	/**
	 * Registered {@link RawManagedObjectMetaData} instances by their name.
	 */
	private final Map<String, RawManagedObjectMetaData<?, ?>> registeredManagedObjects = new HashMap<String, RawManagedObjectMetaData<?, ?>>();

	/**
	 * Scope bound {@link RawBoundManagedObjectMetaData} instances by their
	 * scope name.
	 */
	private final Map<String, RawBoundManagedObjectMetaData<?>> scopeManagedObjects = new HashMap<String, RawBoundManagedObjectMetaData<?>>();

	/**
	 * {@link ManagedObjectSourceMetaData}.
	 */
	private final ManagedObjectSourceMetaData<?, ?> managedObjectSourceMetaData = this
			.createMock(ManagedObjectSourceMetaData.class);

	/**
	 * {@link ManagedObjectSource}.
	 */
	private final ManagedObjectSource<?, ?> managedObjectSource = this
			.createMock(ManagedObjectSource.class);

	/**
	 * {@link ManagedObjectPool}.
	 */
	private final ManagedObjectPool managedObjectPool = this
			.createMock(ManagedObjectPool.class);

	/**
	 * Sourcing {@link AssetManager}.
	 */
	private final AssetManager sourcingAssetManager = this
			.createMock(AssetManager.class);

	/**
	 * Operations {@link AssetManager}.
	 */
	private final AssetManager operationsAssetManager = this
			.createMock(AssetManager.class);

	/**
	 * {@link WorkContainer}.
	 */
	private final WorkContainer<?> workContainer = this
			.createMock(WorkContainer.class);

	/**
	 * {@link ThreadState}.
	 */
	private final ThreadState threadState = this.createMock(ThreadState.class);

	/**
	 * {@link ProcessState}.
	 */
	private final ProcessState processState = this
			.createMock(ProcessState.class);

	/**
	 * {@link OfficeMetaData}.
	 */
	private final OfficeMetaData officeMetaData = this
			.createMock(OfficeMetaData.class);

	/**
	 * {@link OfficeMetaDataLocator}.
	 */
	private final OfficeMetaDataLocator taskMetaDataLocator = this
			.createMock(OfficeMetaDataLocator.class);

	/**
	 * Ensure issue if no bound name.
	 */
	public void testNoBoundName() {

		// Record construction
		this.recordReturn(this.managedObjectConfiguration,
				this.managedObjectConfiguration.getBoundManagedObjectName(),
				null);
		this.issues.addIssue(this.assetType, this.assetName,
				"No bound name for managed object");

		// Attempt to construct
		this.replayMockObjects();
		this.constructRawBoundManagedObjectMetaData(0,
				this.managedObjectConfiguration);
		this.verifyMockObjects();
	}

	/**
	 * Ensure issue if no office name.
	 */
	public void testNoOfficeName() {

		// Record construction
		this.recordReturn(this.managedObjectConfiguration,
				this.managedObjectConfiguration.getBoundManagedObjectName(),
				"BOUND");
		this.recordReturn(this.managedObjectConfiguration,
				this.managedObjectConfiguration.getOfficeManagedObjectName(),
				"");
		this.issues.addIssue(this.assetType, this.assetName,
				"No office name for bound managed object of name 'BOUND'");

		// Attempt to construct
		this.replayMockObjects();
		this.constructRawBoundManagedObjectMetaData(0,
				this.managedObjectConfiguration);
		this.verifyMockObjects();
	}

	/**
	 * Ensure issue if no corresponding {@link RawManagedObjectMetaData}.
	 */
	public void testNoManagedObjectMetaData() {

		// Record construction
		this.recordReturn(this.managedObjectConfiguration,
				this.managedObjectConfiguration.getBoundManagedObjectName(),
				"BOUND");
		this.recordReturn(this.managedObjectConfiguration,
				this.managedObjectConfiguration.getOfficeManagedObjectName(),
				"OFFICE_MO");
		this.issues
				.addIssue(this.assetType, this.assetName,
						"No managed object by name 'OFFICE_MO' registered with the Office");

		// Attempt to construct
		this.replayMockObjects();
		this.constructRawBoundManagedObjectMetaData(0,
				this.managedObjectConfiguration);
		this.verifyMockObjects();
	}

	/**
	 * Ensure able to provide no scope {@link ManagedObject} instances.
	 */
	public void testNullScopeManagedObjects() {

		// Managed object configuration
		RawManagedObjectMetaData<?, ?> rawMoMetaData = this
				.registerRawManagedObjectMetaData("OFFICE_MO");

		// Record construction
		this.recordReturn(this.managedObjectConfiguration,
				this.managedObjectConfiguration.getBoundManagedObjectName(),
				"BOUND");
		this.recordReturn(this.managedObjectConfiguration,
				this.managedObjectConfiguration.getOfficeManagedObjectName(),
				"OFFICE_MO");
		this.recordReturn(rawMoMetaData, rawMoMetaData
				.getManagedObjectSourceMetaData(),
				this.managedObjectSourceMetaData);
		this.recordReturn(this.managedObjectSourceMetaData,
				this.managedObjectSourceMetaData.getDependencyKeys(), null);
		this.record_getManagedObjectDetails("BOUND", rawMoMetaData, true);

		// Construct bound meta-data without scope managed objects
		this.replayMockObjects();
		RawBoundManagedObjectMetaData<?>[] rawBoundMoMetaData = RawBoundManagedObjectMetaDataImpl
				.getFactory()
				.constructBoundManagedObjectMetaData(
						new ManagedObjectConfiguration[] { this.managedObjectConfiguration },
						this.issues, this.managedObjectScope, this.assetType,
						this.assetName, this.assetManagerFactory,
						this.registeredManagedObjects, null);
		ManagedObjectMetaData<?> boundMoMetaData = rawBoundMoMetaData[0]
				.getManagedObjectMetaData();
		this.verifyMockObjects();

		// Verify constructed
		assertEquals("Incorrect managed object source",
				this.managedObjectSource, boundMoMetaData
						.getManagedObjectSource());
	}

	/**
	 * Ensure can construct {@link RawBoundManagedObjectMetaData} with no
	 * dependencies.
	 */
	public void testNoDependencies() {

		// Managed object configuration
		RawManagedObjectMetaData<?, ?> rawMoMetaData = this
				.registerRawManagedObjectMetaData("OFFICE_MO");

		// Record construction
		this.recordReturn(this.managedObjectConfiguration,
				this.managedObjectConfiguration.getBoundManagedObjectName(),
				"BOUND");
		this.recordReturn(this.managedObjectConfiguration,
				this.managedObjectConfiguration.getOfficeManagedObjectName(),
				"OFFICE_MO");
		this.recordReturn(rawMoMetaData, rawMoMetaData
				.getManagedObjectSourceMetaData(),
				this.managedObjectSourceMetaData);
		this.recordReturn(this.managedObjectSourceMetaData,
				this.managedObjectSourceMetaData.getDependencyKeys(), null);
		this.record_getManagedObjectDetails("BOUND", rawMoMetaData, true);

		// Construct and obtain the managed object meta-data
		this.replayMockObjects();
		RawBoundManagedObjectMetaData<?>[] rawBoundMoMetaData = this
				.constructRawBoundManagedObjectMetaData(1,
						this.managedObjectConfiguration);
		ManagedObjectMetaData<?> moMetaData = rawBoundMoMetaData[0]
				.getManagedObjectMetaData();
		this.verifyMockObjects();

		// Verify the managed object meta-data contents
		assertEquals("Incorrect managed object source",
				this.managedObjectSource, moMetaData.getManagedObjectSource());
		assertEquals("Incorrect managed object pool", this.managedObjectPool,
				moMetaData.getManagedObjectPool());
		assertEquals("Incorrect sourcing manager", this.sourcingAssetManager,
				moMetaData.getSourcingManager());
		assertTrue("Should be asynchronous", moMetaData
				.isManagedObjectAsynchronous());
		assertEquals("Incorrect operations manager",
				this.operationsAssetManager, moMetaData.getOperationsManager());
		assertFalse("Not coordinating", moMetaData
				.isCoordinatingManagedObject());
		assertEquals("Incorrect timeout", 0, moMetaData.getTimeout());
	}

	/**
	 * Ensure issue if dependency not configured for a dependency key.
	 */
	public void testMissingDependencyConfiguration() {

		// Managed object configuration
		RawManagedObjectMetaData<?, ?> rawMoMetaData = this
				.registerRawManagedObjectMetaData("OFFICE_MO");

		// Record construction
		this.recordReturn(this.managedObjectConfiguration,
				this.managedObjectConfiguration.getBoundManagedObjectName(),
				"BOUND");
		this.recordReturn(this.managedObjectConfiguration,
				this.managedObjectConfiguration.getOfficeManagedObjectName(),
				"OFFICE_MO");
		this.recordReturn(rawMoMetaData, rawMoMetaData
				.getManagedObjectSourceMetaData(),
				this.managedObjectSourceMetaData);
		this.recordReturn(this.managedObjectSourceMetaData,
				this.managedObjectSourceMetaData.getDependencyKeys(),
				DependencyKey.class);
		this.recordReturn(this.managedObjectConfiguration,
				this.managedObjectConfiguration.getDependencyConfiguration(),
				new ManagedObjectDependencyConfiguration[0]);
		this.issues.addIssue(AssetType.OFFICE, "OFFICE",
				"No mapping for dependency key '" + DependencyKey.KEY
						+ "' of managed object BOUND");
		this.record_getManagedObjectDetails("BOUND", rawMoMetaData, true);

		// Construct and obtain the managed object meta-data
		this.replayMockObjects();
		this.constructRawBoundManagedObjectMetaData(1,
				this.managedObjectConfiguration);
		this.verifyMockObjects();
	}

	/**
	 * Ensure can construct {@link RawBoundManagedObjectMetaData} with
	 * dependency not available.
	 */
	public void testDependencyNotAvailable() {

		// Managed object configuration
		RawManagedObjectMetaData<?, ?> metaData = this
				.registerRawManagedObjectMetaData("OFFICE_MO");

		ManagedObjectDependencyConfiguration<?> dependencyConfig = this
				.createMock(ManagedObjectDependencyConfiguration.class);

		// Record construction
		this.recordReturn(this.managedObjectConfiguration,
				this.managedObjectConfiguration.getBoundManagedObjectName(),
				"BOUND");
		this.recordReturn(this.managedObjectConfiguration,
				this.managedObjectConfiguration.getOfficeManagedObjectName(),
				"OFFICE_MO");
		this.recordReturn(metaData, metaData.getManagedObjectSourceMetaData(),
				this.managedObjectSourceMetaData);
		this.recordReturn(this.managedObjectSourceMetaData,
				this.managedObjectSourceMetaData.getDependencyKeys(),
				DependencyKey.class);
		this
				.recordReturn(
						this.managedObjectConfiguration,
						this.managedObjectConfiguration
								.getDependencyConfiguration(),
						new ManagedObjectDependencyConfiguration[] { dependencyConfig });
		this.recordReturn(dependencyConfig,
				dependencyConfig.getDependencyKey(), DependencyKey.KEY);
		this.recordReturn(dependencyConfig, dependencyConfig
				.getScopeManagedObjectName(), "NOT AVAILABLE");
		this.issues.addIssue(this.assetType, this.assetName,
				"No dependent managed object by name 'NOT AVAILABLE'");
		this.record_getManagedObjectDetails("BOUND", metaData, false);

		// Attempt to construct
		this.replayMockObjects();
		this.constructRawBoundManagedObjectMetaData(1,
				this.managedObjectConfiguration);
		this.verifyMockObjects();
	}

	/**
	 * Ensure can construct {@link RawBoundManagedObjectMetaData} with
	 * dependency on another {@link RawBoundManagedObjectMetaData}.
	 */
	@SuppressWarnings("unchecked")
	public void testDependencyOnAnotherBound() {

		final Object dependencyObject = "dependency object";

		// Managed object configuration
		final ManagedObjectConfiguration<?> oneConfig = this
				.createMock(ManagedObjectConfiguration.class);
		final RawManagedObjectMetaData<?, ?> oneMetaData = this
				.registerRawManagedObjectMetaData("ONE");
		final ManagedObjectConfiguration<?> twoConfig = this
				.createMock(ManagedObjectConfiguration.class);
		final RawManagedObjectMetaData<?, ?> twoMetaData = this
				.registerRawManagedObjectMetaData("TWO");

		final ManagedObjectDependencyConfiguration<?> dependencyConfig = this
				.createMock(ManagedObjectDependencyConfiguration.class);

		// Record construction
		this.recordReturn(oneConfig, oneConfig.getBoundManagedObjectName(),
				"MO_A");
		this.recordReturn(oneConfig, oneConfig.getOfficeManagedObjectName(),
				"ONE");
		this.recordReturn(oneMetaData, oneMetaData
				.getManagedObjectSourceMetaData(),
				this.managedObjectSourceMetaData);
		this.recordReturn(this.managedObjectSourceMetaData,
				this.managedObjectSourceMetaData.getDependencyKeys(),
				DependencyKey.class);
		this.recordReturn(twoConfig, twoConfig.getBoundManagedObjectName(),
				"MO_B");
		this.recordReturn(twoConfig, twoConfig.getOfficeManagedObjectName(),
				"TWO");
		this.recordReturn(twoMetaData, twoMetaData
				.getManagedObjectSourceMetaData(),
				this.managedObjectSourceMetaData);
		this.recordReturn(this.managedObjectSourceMetaData,
				this.managedObjectSourceMetaData.getDependencyKeys(), null);
		this
				.recordReturn(
						oneConfig,
						oneConfig.getDependencyConfiguration(),
						new ManagedObjectDependencyConfiguration[] { dependencyConfig });
		this.recordReturn(dependencyConfig,
				dependencyConfig.getDependencyKey(), DependencyKey.KEY);
		this.recordReturn(dependencyConfig, dependencyConfig
				.getScopeManagedObjectName(), "MO_B");
		this.record_getManagedObjectDetails("MO_A", oneMetaData, false);
		this.record_getManagedObjectDetails("MO_B", twoMetaData, true);

		// Record creating object registry
		this.recordReturn(this.workContainer, this.workContainer.getObject(
				null, this.threadState), dependencyObject,
				new AbstractMatcher() {
					@Override
					public boolean matches(Object[] expected, Object[] actual) {
						ManagedObjectIndex index = (ManagedObjectIndex) actual[0];
						assertEquals(
								"Incorrect managed object scope",
								RawBoundManagedObjectMetaDataTest.this.managedObjectScope,
								index.getManagedObjectScope());
						assertEquals("Dependency should be second bound", 1,
								index.getIndexOfManagedObjectWithinScope());
						assertEquals(
								"Incorrect threadState",
								RawBoundManagedObjectMetaDataTest.this.threadState,
								actual[1]);
						return true;
					}
				});

		this.replayMockObjects();

		// Construct
		RawBoundManagedObjectMetaData[] rawMetaData = this
				.constructRawBoundManagedObjectMetaData(2, oneConfig, twoConfig);
		ManagedObjectMetaData<DependencyKey> moMetaData = (ManagedObjectMetaData<DependencyKey>) rawMetaData[0]
				.getManagedObjectMetaData();

		// Create the object registry and object the dependent object
		ObjectRegistry<DependencyKey> objectRegistry = moMetaData
				.createObjectRegistry(this.workContainer, this.threadState);
		Object object = objectRegistry.getObject(DependencyKey.KEY);

		this.verifyMockObjects();

		// Validate dependency mapping
		RawBoundManagedObjectMetaData<DependencyKey> one = (RawBoundManagedObjectMetaData<DependencyKey>) rawMetaData[0];
		assertEquals("Incorrect number of dependency keys", 1, one
				.getDependencyKeys().length);
		assertEquals("Incorrect dependency key", DependencyKey.KEY, one
				.getDependencyKeys()[0]);
		RawBoundManagedObjectMetaData<?> two = rawMetaData[1];
		assertEquals("Incorrect dependency", two, one
				.getDependency(DependencyKey.KEY));

		// Validate object registry
		assertEquals("Incorrect dependency object", dependencyObject, object);

		// Verify asynchronous details
		assertFalse("Should not be asynchronous", moMetaData
				.isManagedObjectAsynchronous());
		assertNull(
				"Should not have operations asset manager as not asynchronous",
				moMetaData.getOperationsManager());
	}

	/**
	 * Ensure can construct {@link RawBoundManagedObjectMetaData} with
	 * dependency on a {@link RawBoundManagedObjectMetaData} from scope.
	 */
	@SuppressWarnings("unchecked")
	public void testDependencyOnScopeBound() {

		final Object dependencyObject = "dependency object";
		final ManagedObjectIndex dependencyMoIndex = this
				.createMock(ManagedObjectIndex.class);

		// Managed object configuration
		final RawManagedObjectMetaData<?, ?> rawMoMetaData = this
				.registerRawManagedObjectMetaData("OFFICE_MO");
		final RawBoundManagedObjectMetaData<?> scopeMoMetaData = this
				.scopeRawManagedObjectMetaData("SCOPE");

		final ManagedObjectDependencyConfiguration<?> dependencyConfig = this
				.createMock(ManagedObjectDependencyConfiguration.class);

		// Record construction
		this.recordReturn(this.managedObjectConfiguration,
				this.managedObjectConfiguration.getBoundManagedObjectName(),
				"BOUND");
		this.recordReturn(this.managedObjectConfiguration,
				this.managedObjectConfiguration.getOfficeManagedObjectName(),
				"OFFICE_MO");
		this.recordReturn(rawMoMetaData, rawMoMetaData
				.getManagedObjectSourceMetaData(),
				this.managedObjectSourceMetaData);
		this.recordReturn(this.managedObjectSourceMetaData,
				this.managedObjectSourceMetaData.getDependencyKeys(),
				DependencyKey.class);
		this
				.recordReturn(
						this.managedObjectConfiguration,
						this.managedObjectConfiguration
								.getDependencyConfiguration(),
						new ManagedObjectDependencyConfiguration[] { dependencyConfig });
		this.recordReturn(dependencyConfig,
				dependencyConfig.getDependencyKey(), DependencyKey.KEY);
		this.recordReturn(dependencyConfig, dependencyConfig
				.getScopeManagedObjectName(), "SCOPE");
		this.recordReturn(scopeMoMetaData, scopeMoMetaData
				.getManagedObjectIndex(), dependencyMoIndex);
		this.record_getManagedObjectDetails("BOUND", rawMoMetaData, false);

		// Record creating object registry
		this.recordReturn(this.workContainer, this.workContainer.getObject(
				dependencyMoIndex, this.threadState), dependencyObject);

		this.replayMockObjects();

		// Construct
		RawBoundManagedObjectMetaData[] rawMetaData = this
				.constructRawBoundManagedObjectMetaData(1,
						this.managedObjectConfiguration);
		ManagedObjectMetaData<DependencyKey> moMetaData = (ManagedObjectMetaData<DependencyKey>) rawMetaData[0]
				.getManagedObjectMetaData();

		// Create the object registry and object the dependent object
		ObjectRegistry<DependencyKey> objectRegistry = moMetaData
				.createObjectRegistry(this.workContainer, this.threadState);
		Object object = objectRegistry.getObject(DependencyKey.KEY);

		this.verifyMockObjects();

		// Validate dependency mapping
		RawBoundManagedObjectMetaData<DependencyKey> dependency = (RawBoundManagedObjectMetaData<DependencyKey>) rawMetaData[0];
		assertEquals("Incorrect number of dependency keys", 1, dependency
				.getDependencyKeys().length);
		assertEquals("Incorrect dependency key", DependencyKey.KEY, dependency
				.getDependencyKeys()[0]);
		assertEquals("Incorrect dependency", scopeMoMetaData, dependency
				.getDependency(DependencyKey.KEY));

		// Verify creation of object registry
		assertEquals("Incorrect dependency object", dependencyObject, object);
	}

	/**
	 * Ensure able to not have a recycle {@link Task}.
	 */
	public void testNoRecycleTask() {

		final ManagedObject managedObject = this
				.createMock(ManagedObject.class);

		// Record construction
		RawManagedObjectMetaData<?, ?> rawMoMetaData = this
				.record_constructManagedObject(false);

		// Record linking tasks
		this.recordReturn(this.taskMetaDataLocator, this.taskMetaDataLocator
				.getOfficeMetaData(), this.officeMetaData);
		this.recordReturn(rawMoMetaData, rawMoMetaData.getRecycleWorkName(),
				null);

		this.replayMockObjects();

		// Construct and link tasks
		RawBoundManagedObjectMetaData<?> rawMetaData = this
				.constructRawBoundManagedObjectMetaData(1,
						this.managedObjectConfiguration)[0];
		rawMetaData.linkTasks(this.taskMetaDataLocator, this.issues);
		ManagedObjectMetaData<?> moMetaData = rawMetaData
				.getManagedObjectMetaData();

		// Create the recycle task
		JobNode recycleTask = moMetaData.createRecycleJobNode(managedObject);

		this.verifyMockObjects();

		// Ensure no recycle task
		assertNull("Should not have recycle task", recycleTask);
	}

	/**
	 * Ensure issue if no {@link WorkMetaData} for recycle {@link Task}.
	 */
	public void testNoWorkForRecycleTask() {

		final ManagedObject managedObject = this
				.createMock(ManagedObject.class);

		// Record construction
		RawManagedObjectMetaData<?, ?> rawMoMetaData = this
				.record_constructManagedObject(false);

		// Record linking tasks
		this.recordReturn(this.taskMetaDataLocator, this.taskMetaDataLocator
				.getOfficeMetaData(), this.officeMetaData);
		this.recordReturn(rawMoMetaData, rawMoMetaData.getRecycleWorkName(),
				"RECYCLE_WORK");
		this.recordReturn(this.taskMetaDataLocator, this.taskMetaDataLocator
				.getWorkMetaData("RECYCLE_WORK"), null);
		this.issues.addIssue(AssetType.MANAGED_OBJECT, "BOUND",
				"Recycle work 'RECYCLE_WORK' not found");

		this.replayMockObjects();

		// Construct and link tasks
		RawBoundManagedObjectMetaData<?> rawMetaData = this
				.constructRawBoundManagedObjectMetaData(1,
						this.managedObjectConfiguration)[0];
		rawMetaData.linkTasks(this.taskMetaDataLocator, this.issues);
		ManagedObjectMetaData<?> moMetaData = rawMetaData
				.getManagedObjectMetaData();

		// Create the recycle task
		JobNode recycleTask = moMetaData.createRecycleJobNode(managedObject);

		this.verifyMockObjects();

		// Ensure no recycle task
		assertNull("Should not have recycle task", recycleTask);
	}

	/**
	 * Ensure issue if no initial {@link FlowMetaData} for recycle {@link Task}.
	 */
	public void testNoInitialFlowForRecycleTask() {

		final WorkMetaData<?> workMetaData = this
				.createMock(WorkMetaData.class);
		final ManagedObject managedObject = this
				.createMock(ManagedObject.class);

		// Record construction
		RawManagedObjectMetaData<?, ?> rawMoMetaData = this
				.record_constructManagedObject(false);

		// Record linking tasks
		this.recordReturn(this.taskMetaDataLocator, this.taskMetaDataLocator
				.getOfficeMetaData(), this.officeMetaData);
		this.recordReturn(rawMoMetaData, rawMoMetaData.getRecycleWorkName(),
				"RECYCLE_WORK");
		this.recordReturn(this.taskMetaDataLocator, this.taskMetaDataLocator
				.getWorkMetaData("RECYCLE_WORK"), workMetaData);
		this.recordReturn(workMetaData, workMetaData.getInitialFlowMetaData(),
				null);
		this.issues.addIssue(AssetType.MANAGED_OBJECT, "BOUND",
				"No initial flow on work 'RECYCLE_WORK' for recycle task");

		this.replayMockObjects();

		// Construct and link tasks
		RawBoundManagedObjectMetaData<?> rawMetaData = this
				.constructRawBoundManagedObjectMetaData(1,
						this.managedObjectConfiguration)[0];
		rawMetaData.linkTasks(this.taskMetaDataLocator, this.issues);
		ManagedObjectMetaData<?> moMetaData = rawMetaData
				.getManagedObjectMetaData();

		// Create the recycle task
		JobNode recycleTask = moMetaData.createRecycleJobNode(managedObject);

		this.verifyMockObjects();

		// Ensure no recycle task
		assertNull("Should not have recycle task", recycleTask);
	}

	/**
	 * Ensure able to link the recycle {@link Task}.
	 */
	public void testLinkRecycleTask() {

		final WorkMetaData<?> recycleWorkMetaData = this
				.createMock(WorkMetaData.class);
		final FlowMetaData<?> recycleFlowMetaData = this
				.createMock(FlowMetaData.class);
		final JobNode recycleJobNode = this.createMock(JobNode.class);
		final Flow recycleFlow = this.createMock(Flow.class);
		final ManagedObject managedObject = this
				.createMock(ManagedObject.class);

		// Record construction
		final RawManagedObjectMetaData<?, ?> rawMoMetaData = this
				.record_constructManagedObject(false);

		// Record linking tasks
		this.recordReturn(this.taskMetaDataLocator, this.taskMetaDataLocator
				.getOfficeMetaData(), this.officeMetaData);
		this.recordReturn(rawMoMetaData, rawMoMetaData.getRecycleWorkName(),
				"RECYCLE_WORK");
		this.recordReturn(this.taskMetaDataLocator, this.taskMetaDataLocator
				.getWorkMetaData("RECYCLE_WORK"), recycleWorkMetaData);
		this.recordReturn(recycleWorkMetaData, recycleWorkMetaData
				.getInitialFlowMetaData(), recycleFlowMetaData);

		// Record creating recycle job node
		this.recordReturn(this.officeMetaData, this.officeMetaData
				.createProcess(recycleFlowMetaData, null), recycleJobNode,
				new AbstractMatcher() {
					@Override
					@SuppressWarnings("unchecked")
					public boolean matches(Object[] expected, Object[] actual) {
						assertEquals("Incorrect flow meta-data",
								recycleFlowMetaData, actual[0]);
						RecycleManagedObjectParameter<ManagedObject> parameter = (RecycleManagedObjectParameter<ManagedObject>) actual[1];
						assertEquals("Incorrect managed object", managedObject,
								parameter.getManagedObject());
						return true;
					}
				});
		this
				.recordReturn(recycleJobNode, recycleJobNode.getFlow(),
						recycleFlow);
		this.recordReturn(recycleFlow, recycleFlow.getThreadState(),
				this.threadState);
		this.recordReturn(this.threadState, this.threadState.getProcessState(),
				this.processState);
		this.processState.registerProcessCompletionListener(null);
		this.control(this.processState).setMatcher(new AbstractMatcher() {
			@Override
			@SuppressWarnings("unchecked")
			public boolean matches(Object[] expected, Object[] actual) {
				// Parameter also given as completion listener
				RecycleManagedObjectParameter<ManagedObject> parameter = (RecycleManagedObjectParameter<ManagedObject>) actual[0];
				assertEquals("Incorrect managed object", managedObject,
						parameter.getManagedObject());
				return true;
			}
		});

		this.replayMockObjects();

		// Construct and link in recycle task
		RawBoundManagedObjectMetaData<?> rawMetaData = this
				.constructRawBoundManagedObjectMetaData(1,
						this.managedObjectConfiguration)[0];
		rawMetaData.linkTasks(this.taskMetaDataLocator, this.issues);
		ManagedObjectMetaData<?> moMetaData = rawMetaData
				.getManagedObjectMetaData();

		// Create the recycle task
		JobNode recycleTask = moMetaData.createRecycleJobNode(managedObject);

		this.verifyMockObjects();

		// Ensure have recycle task
		assertNotNull("Must have recycle task", recycleTask);
	}

	/**
	 * Ensures issue if {@link RawBoundManagedObjectMetaData} input is not bound
	 * to the {@link ProcessState}.
	 */
	public void testAffixToNonProcessBoundManagedObjects() {

		final RawBoundManagedObjectMetaData<?> boundMo = this
				.createMock(RawBoundManagedObjectMetaData.class);
		final ManagedObjectIndex boundMoIndex = new ManagedObjectIndexImpl(
				ManagedObjectScope.THREAD, 0);

		// Record affixing the already bound managed object
		this.recordReturn(boundMo, boundMo.getBoundManagedObjectName(),
				"NON_PROCESS_MO");
		this.recordReturn(boundMo, boundMo.getManagedObjectIndex(),
				boundMoIndex);
		this.issues
				.addIssue(
						AssetType.OFFICE,
						"OFFICE",
						"Attempting to affix managed objects to listing of managed objects that are not all process bound");

		// Affix the managed objects
		this.replayMockObjects();
		RawBoundManagedObjectMetaData<?> returnedBoundMo = this
				.affixOfficeManagingManagedObjects(1,
						new RawBoundManagedObjectMetaData[] { boundMo })[0];
		this.verifyMockObjects();

		// Verify the bound managed object
		assertEquals("Incorrect bound managed object", boundMo, returnedBoundMo);
	}

	/**
	 * Ensures not affixes a {@link ManagedObject} that does not require
	 * {@link Handler} instances.
	 */
	public void testNotAffixManagedObjectRequiringNoHandlers() {

		final RawOfficeManagingManagedObjectMetaData officeMo = this
				.createMock(RawOfficeManagingManagedObjectMetaData.class);
		final RawManagedObjectMetaData<?, ?> rawMoMetaData = this
				.createMock(RawManagedObjectMetaData.class);

		// Record not affix as not require handlers
		this.recordReturn(officeMo, officeMo.getRawManagedObjectMetaData(),
				rawMoMetaData);
		this.recordReturn(rawMoMetaData, rawMoMetaData.getHandlerKeys(), null);

		// Affix the managed objects
		this.replayMockObjects();
		this.affixOfficeManagingManagedObjects(0,
				new RawBoundManagedObjectMetaData[0], officeMo);
		this.verifyMockObjects();
	}

	/**
	 * Ensures issue if no {@link ProcessState} bound name to affix the
	 * {@link ManagedObject}.
	 */
	public void testNoProcessManagedObjectNameForAffixing() {

		final RawOfficeManagingManagedObjectMetaData officeMo = this
				.createMock(RawOfficeManagingManagedObjectMetaData.class);
		final RawManagedObjectMetaData<?, ?> rawMoMetaData = this
				.createMock(RawManagedObjectMetaData.class);

		// Record not affix as not require handlers
		this.recordReturn(officeMo, officeMo.getRawManagedObjectMetaData(),
				rawMoMetaData);
		this.recordReturn(rawMoMetaData, rawMoMetaData.getHandlerKeys(),
				HandlerKey.class.getEnumConstants());
		this.recordReturn(officeMo, officeMo.getProcessBoundName(), null);
		this.recordReturn(rawMoMetaData, rawMoMetaData.getManagedObjectName(),
				"MO");
		this.issues
				.addIssue(AssetType.MANAGED_OBJECT, "MO",
						"Must provide process bound name as requires managing by an office");

		// Affix the managed objects
		this.replayMockObjects();
		this.affixOfficeManagingManagedObjects(0,
				new RawBoundManagedObjectMetaData[0], officeMo);
		this.verifyMockObjects();
	}

	/**
	 * Ensures able to affix a {@link RawOfficeManagingManagedObjectMetaData}
	 * that is already {@link ProcessState} bound to the {@link Office}.
	 */
	public void testAffixBoundManagedObjects() {

		final String BOUND_MO_NAME = "BOUND_MO";

		final RawBoundManagedObjectMetaData<?> boundMo = this
				.createMock(RawBoundManagedObjectMetaData.class);
		final ManagedObjectIndex boundMoIndex = new ManagedObjectIndexImpl(
				ManagedObjectScope.PROCESS, 0);
		final RawOfficeManagingManagedObjectMetaData officeMo = this
				.createMock(RawOfficeManagingManagedObjectMetaData.class);
		final RawManagedObjectMetaData<?, ?> rawMoMetaData = this
				.createMock(RawManagedObjectMetaData.class);

		// Record affixing the already bound managed object
		this.recordReturn(boundMo, boundMo.getBoundManagedObjectName(),
				BOUND_MO_NAME);
		this.recordReturn(boundMo, boundMo.getManagedObjectIndex(),
				boundMoIndex);
		this.recordReturn(officeMo, officeMo.getRawManagedObjectMetaData(),
				rawMoMetaData);
		this.recordReturn(rawMoMetaData, rawMoMetaData.getHandlerKeys(),
				HandlerKey.class.getEnumConstants());
		this.recordReturn(officeMo, officeMo.getProcessBoundName(),
				BOUND_MO_NAME);
		this.recordReturn(boundMo, boundMo.getRawManagedObjectMetaData(),
				rawMoMetaData);

		// Affix the managed objects
		this.replayMockObjects();
		RawBoundManagedObjectMetaData<?> returnedBoundMo = this
				.affixOfficeManagingManagedObjects(1,
						new RawBoundManagedObjectMetaData[] { boundMo },
						officeMo)[0];
		this.verifyMockObjects();

		// Verify the bound managed object
		assertEquals("Incorrect bound managed object", boundMo, returnedBoundMo);
	}

	/**
	 * Ensures able to affix a {@link RawOfficeManagingManagedObjectMetaData}
	 * that is not {@link ProcessState} bound to the {@link Office}.
	 */
	public void testAffixNonBoundManagedObjects() {

		final RawOfficeManagingManagedObjectMetaData officeMo = this
				.createMock(RawOfficeManagingManagedObjectMetaData.class);
		final RawManagedObjectMetaData<?, ?> rawMoMetaData = this
				.createMock(RawManagedObjectMetaData.class);

		// Record affixing the non bound managed object
		this.recordReturn(officeMo, officeMo.getRawManagedObjectMetaData(),
				rawMoMetaData);
		this.recordReturn(rawMoMetaData, rawMoMetaData.getHandlerKeys(),
				HandlerKey.class.getEnumConstants());
		this
				.recordReturn(officeMo, officeMo.getProcessBoundName(),
						"NOT_BOUND");
		this.record_getManagedObjectDetails("NOT_BOUND", rawMoMetaData, true);

		// Affix the managed objects
		this.replayMockObjects();
		RawBoundManagedObjectMetaData<?> returnedBoundMo = this
				.affixOfficeManagingManagedObjects(1,
						new RawBoundManagedObjectMetaData[0], officeMo)[0];
		this.verifyMockObjects();

		// Verify bound
		ManagedObjectIndex moIndex = returnedBoundMo.getManagedObjectIndex();
		assertEquals("Incorrect managed object scope",
				ManagedObjectScope.PROCESS, moIndex.getManagedObjectScope());
		assertEquals("Incorrect managed object index in scope", 0, moIndex
				.getIndexOfManagedObjectWithinScope());
		assertEquals("Incorrect bound managed object",
				this.managedObjectSource, returnedBoundMo
						.getManagedObjectMetaData().getManagedObjectSource());
	}

	/**
	 * Dependency key {@link Enum}.
	 */
	private enum DependencyKey {
		KEY
	}

	/**
	 * {@link Handler} key {@link Enum}.
	 */
	private enum HandlerKey {
		KEY
	}

	/**
	 * Provides a scope {@link RawBoundManagedObjectMetaData}.
	 * 
	 * @param scopeManagedObjectName
	 *            Name of the scope {@link RawBoundManagedObjectMetaData}.
	 * @return Scope {@link RawBoundManagedObjectMetaData}.
	 */
	private RawBoundManagedObjectMetaData<?> scopeRawManagedObjectMetaData(
			String scopeManagedObjectName) {
		RawBoundManagedObjectMetaData<?> metaData = this
				.createMock(RawBoundManagedObjectMetaData.class);
		this.scopeManagedObjects.put(scopeManagedObjectName, metaData);
		return metaData;
	}

	/**
	 * Registers and returns a {@link RawManagedObjectMetaData}.
	 * 
	 * @param registeredManagedObjectName
	 *            Name to registered the {@link RawManagedObjectMetaData} under.
	 * @return Registered {@link RawManagedObjectMetaData}.
	 */
	private RawManagedObjectMetaData<?, ?> registerRawManagedObjectMetaData(
			String registeredManagedObjectName) {
		RawManagedObjectMetaData<?, ?> metaData = this
				.createMock(RawManagedObjectMetaData.class);
		this.registeredManagedObjects
				.put(registeredManagedObjectName, metaData);
		return metaData;
	}

	/**
	 * Records constructing a single {@link RawManagedObjectMetaData}.
	 */
	private RawManagedObjectMetaData<?, ?> record_constructManagedObject(
			boolean isAsynchronous) {

		final RawManagedObjectMetaData<?, ?> rawMoMetaData = this
				.registerRawManagedObjectMetaData("OFFICE_MO");

		// Record the construction
		this.recordReturn(this.managedObjectConfiguration,
				this.managedObjectConfiguration.getBoundManagedObjectName(),
				"BOUND");
		this.recordReturn(this.managedObjectConfiguration,
				this.managedObjectConfiguration.getOfficeManagedObjectName(),
				"OFFICE_MO");
		this.recordReturn(rawMoMetaData, rawMoMetaData
				.getManagedObjectSourceMetaData(),
				this.managedObjectSourceMetaData);
		this.recordReturn(this.managedObjectSourceMetaData,
				this.managedObjectSourceMetaData.getDependencyKeys(), null);
		this.record_getManagedObjectDetails("BOUND", rawMoMetaData,
				isAsynchronous);

		// Return the raw managed object meta-data constructed
		return rawMoMetaData;
	}

	/**
	 * Records obtaining the {@link ManagedObjectMetaData} details from the
	 * {@link RawManagedObjectMetaData}.
	 * 
	 * @param boundMoName
	 *            Name of the {@link RawBoundManagedObjectMetaData}.
	 * @param rawMoMetaData
	 *            {@link RawManagedObjectMetaData}.
	 * @param isAsynchronous
	 *            Flag indicating if {@link AsynchronousManagedObject}.
	 */
	private void record_getManagedObjectDetails(String boundMoName,
			RawManagedObjectMetaData<?, ?> rawMoMetaData, boolean isAsynchronous) {
		this.recordReturn(rawMoMetaData,
				rawMoMetaData.getManagedObjectSource(),
				this.managedObjectSource);
		this.recordReturn(rawMoMetaData, rawMoMetaData.getManagedObjectPool(),
				this.managedObjectPool);
		this.recordReturn(rawMoMetaData, rawMoMetaData.isAsynchronous(),
				isAsynchronous);
		this.recordReturn(rawMoMetaData, rawMoMetaData.isCoordinating(), false);
		this.recordReturn(rawMoMetaData, rawMoMetaData.getDefaultTimeout(), 0);
		this.recordReturn(this.assetManagerFactory, this.assetManagerFactory
				.createAssetManager(AssetType.MANAGED_OBJECT, boundMoName,
						"sourcing", this.issues), this.sourcingAssetManager);
		if (isAsynchronous) {
			this.recordReturn(this.assetManagerFactory,
					this.assetManagerFactory.createAssetManager(
							AssetType.MANAGED_OBJECT, boundMoName,
							"operations", this.issues),
					this.operationsAssetManager);
		}
	}

	/**
	 * Constructs the {@link RawBoundManagedObjectMetaData} instances.
	 * 
	 * @param expectedNumberConstructed
	 *            Expected number of {@link RawBoundManagedObjectMetaData}
	 *            instances to be constructed.
	 * @param boundManagedObjectConfiguration
	 *            {@link ManagedObjectConfiguration} instances.
	 * @return Constructed {@link RawBoundManagedObjectMetaData}.
	 */
	private RawBoundManagedObjectMetaData<?>[] constructRawBoundManagedObjectMetaData(
			int expectedNumberConstructed,
			ManagedObjectConfiguration<?>... boundManagedObjectConfiguration) {

		// Attempt to construct
		RawBoundManagedObjectMetaData<?>[] metaData = RawBoundManagedObjectMetaDataImpl
				.getFactory()
				.constructBoundManagedObjectMetaData(
						boundManagedObjectConfiguration, this.issues,
						this.managedObjectScope, this.assetType,
						this.assetName, this.assetManagerFactory,
						this.registeredManagedObjects, this.scopeManagedObjects);

		// Ensure correct number constructed
		assertEquals("Incorrect number of bound managed objects",
				expectedNumberConstructed, metaData.length);

		// Return the meta-data
		return metaData;
	}

	/**
	 * Affixes the {@link RawOfficeManagingManagedObjectMetaData} instances.
	 * 
	 * @param expectedNumberReturned
	 *            Expected number of {@link RawBoundManagedObjectMetaData}
	 *            instances to be returned.
	 * @param processBoundManagedObjectMetaData
	 *            {@link ProcessState} {@link RawBoundManagedObjectMetaData}
	 *            instances.
	 * @param officeManagingManagedObjects
	 *            {@link RawOfficeManagingManagedObjectMetaData} instances.
	 * @return Returned {@link RawBoundManagedObjectMetaData}.
	 */
	private RawBoundManagedObjectMetaData<?>[] affixOfficeManagingManagedObjects(
			int expectedNumberReturned,
			RawBoundManagedObjectMetaData<?>[] processBoundManagedObjectMetaData,
			RawOfficeManagingManagedObjectMetaData... officeManagingManagedObjects) {

		// Attempt to affix office managed objects
		RawBoundManagedObjectMetaData<?>[] metaData = RawBoundManagedObjectMetaDataImpl
				.getFactory().affixOfficeManagingManagedObjects("OFFICE",
						processBoundManagedObjectMetaData,
						officeManagingManagedObjects, this.assetManagerFactory,
						this.issues);

		// Ensure correct number returned
		assertEquals("Incorrect number of bound managed objects returned",
				expectedNumberReturned, metaData.length);

		// Return the meta-data
		return metaData;
	}
}