/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2009 Daniel Sagenschneider
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
package net.officefloor.frame.impl.construct.managedobject;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

import net.officefloor.frame.api.build.OfficeFloorIssues;
import net.officefloor.frame.api.build.OfficeFloorIssues.AssetType;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.impl.execute.managedobject.ManagedObjectIndexImpl;
import net.officefloor.frame.internal.configuration.ManagedObjectConfiguration;
import net.officefloor.frame.internal.configuration.ManagedObjectDependencyConfiguration;
import net.officefloor.frame.internal.construct.AssetManagerFactory;
import net.officefloor.frame.internal.construct.RawBoundManagedObjectMetaData;
import net.officefloor.frame.internal.construct.RawManagedObjectMetaData;
import net.officefloor.frame.internal.construct.RawManagingOfficeMetaData;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.ManagedObjectIndex;
import net.officefloor.frame.internal.structure.ManagedObjectMetaData;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectDependencyMetaData;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceMetaData;
import net.officefloor.frame.test.OfficeFrameTestCase;

import org.easymock.AbstractMatcher;

/**
 * Test the {@link RawBoundManagedObjectMetaDataImpl}.
 * 
 * @author Daniel Sagenschneider
 */
public class RawBoundManagedObjectMetaDataTest extends OfficeFrameTestCase {

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
	 * Name of the {@link Office}.
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
	 * Ensure issue if no bound name.
	 */
	public void testNoBoundName() {

		final ManagedObjectConfiguration<?> configuration = this
				.createMock(ManagedObjectConfiguration.class);

		// Record construction
		this.recordReturn(configuration, configuration
				.getBoundManagedObjectName(), null);
		this.issues.addIssue(this.assetType, this.assetName,
				"No bound name for managed object");

		// Attempt to construct
		this.replayMockObjects();
		this.constructRawBoundManagedObjectMetaData(0, configuration);
		this.verifyMockObjects();
	}

	/**
	 * Ensure issue if no office name.
	 */
	public void testNoOfficeName() {

		final ManagedObjectConfiguration<?> configuration = this
				.createMock(ManagedObjectConfiguration.class);

		// Record construction
		this.recordReturn(configuration, configuration
				.getBoundManagedObjectName(), "BOUND");
		this.recordReturn(configuration, configuration
				.getOfficeManagedObjectName(), "");
		this.issues.addIssue(this.assetType, this.assetName,
				"No office name for bound managed object of name 'BOUND'");

		// Attempt to construct
		this.replayMockObjects();
		this.constructRawBoundManagedObjectMetaData(0, configuration);
		this.verifyMockObjects();
	}

	/**
	 * Ensure issue if no corresponding {@link RawManagedObjectMetaData}.
	 */
	public void testNoManagedObjectMetaData() {

		// Record construction
		final ManagedObjectConfiguration<?> configuration = this
				.record_initManagedObject("BOUND", "OFFICE_MO");
		this.issues
				.addIssue(this.assetType, this.assetName,
						"No managed object by name 'OFFICE_MO' registered with the Office");

		// Attempt to construct
		this.replayMockObjects();
		this.constructRawBoundManagedObjectMetaData(0, configuration);
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
		ManagedObjectConfiguration<?> configuration = this
				.record_initManagedObject("BOUND", "OFFICE_MO");
		this.record_getDependencyMetaData(rawMoMetaData);
		this.record_getDependencyConfiguration(configuration);
		ManagedObjectMetaData<?> moMetaData = this
				.record_loadManagedObjectMetaData(rawMoMetaData, "BOUND");

		// Construct bound meta-data without scope managed objects
		this.replayMockObjects();
		RawBoundManagedObjectMetaData<?>[] rawBoundMoMetaData = RawBoundManagedObjectMetaDataImpl
				.getFactory().constructBoundManagedObjectMetaData(
						new ManagedObjectConfiguration[] { configuration },
						this.issues, this.managedObjectScope, this.assetType,
						this.assetName, this.assetManagerFactory,
						this.registeredManagedObjects, null);
		ManagedObjectMetaData<?> boundMoMetaData = rawBoundMoMetaData[0]
				.getManagedObjectMetaData();
		this.verifyMockObjects();

		// Verify correct managed object meta-data
		assertEquals("Incorrect managed object meta-data", moMetaData,
				boundMoMetaData);
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
		ManagedObjectConfiguration<?> configuration = this
				.record_initManagedObject("BOUND", "OFFICE_MO");
		this.record_getDependencyMetaData(rawMoMetaData);
		this.record_getDependencyConfiguration(configuration);
		ManagedObjectMetaData<?> moMetaData = this
				.record_loadManagedObjectMetaData(rawMoMetaData, "BOUND");

		// Construct and obtain the managed object meta-data
		this.replayMockObjects();
		RawBoundManagedObjectMetaData<?> rawBoundMoMetaData = this
				.constructRawBoundManagedObjectMetaData(1, configuration)[0];
		assertEquals("Incorrect managed object meta-data", moMetaData,
				rawBoundMoMetaData.getManagedObjectMetaData());
		this.verifyMockObjects();
	}

	/**
	 * Ensure issue if dependency not configured for a dependency key.
	 */
	public void testMissingDependencyConfiguration() {

		// Managed object configuration
		RawManagedObjectMetaData<?, ?> rawMoMetaData = this
				.registerRawManagedObjectMetaData("OFFICE_MO");
		ManagedObjectDependencyMetaData<?> dependencyMetaData = this
				.createMock(ManagedObjectDependencyMetaData.class);

		// Record construction
		final ManagedObjectConfiguration<?> configuration = this
				.record_initManagedObject("BOUND", "OFFICE_MO");
		this.record_getDependencyMetaData(rawMoMetaData, dependencyMetaData);
		this.record_getDependencyConfiguration(configuration);
		this.record_dependencyMetaDataDetails(dependencyMetaData,
				DependencyKey.KEY, null);
		this.issues.addIssue(AssetType.MANAGED_OBJECT, "BOUND",
				"No mapping configured for dependency 0 (key="
						+ DependencyKey.KEY + ", label=<no label>)");
		this.record_loadManagedObjectMetaData(rawMoMetaData, "BOUND");

		// Construct and obtain the managed object meta-data
		this.replayMockObjects();
		this.constructRawBoundManagedObjectMetaData(1, configuration);
		this.verifyMockObjects();
	}

	/**
	 * Ensure can construct {@link RawBoundManagedObjectMetaData} with
	 * dependency not available.
	 */
	public void testDependencyNotAvailable() {

		// Managed object configuration
		RawManagedObjectMetaData<?, ?> rawMoMetaData = this
				.registerRawManagedObjectMetaData("OFFICE_MO");

		ManagedObjectDependencyConfiguration<?> dependencyConfig = this
				.createMock(ManagedObjectDependencyConfiguration.class);
		ManagedObjectDependencyMetaData<?> dependencyMetaData = this
				.createMock(ManagedObjectDependencyMetaData.class);

		// Record construction
		final ManagedObjectConfiguration<?> configuration = this
				.record_initManagedObject("BOUND", "OFFICE_MO");
		this.record_getDependencyMetaData(rawMoMetaData, dependencyMetaData);
		this.record_getDependencyConfiguration(configuration, dependencyConfig);
		this.recordReturn(dependencyConfig,
				dependencyConfig.getDependencyKey(), DependencyKey.KEY);
		this.record_dependencyMetaDataDetails(dependencyMetaData,
				DependencyKey.KEY, "LABEL");
		this.recordReturn(dependencyConfig, dependencyConfig
				.getScopeManagedObjectName(), "NOT AVAILABLE");
		this.issues.addIssue(AssetType.MANAGED_OBJECT, "BOUND",
				"No dependent ManagedObject by name 'NOT AVAILABLE' for dependency 0 (key="
						+ DependencyKey.KEY + ", label=LABEL)");
		this.record_loadManagedObjectMetaData(rawMoMetaData, "BOUND");

		// Attempt to construct
		this.replayMockObjects();
		this.constructRawBoundManagedObjectMetaData(1, configuration);
		this.verifyMockObjects();
	}

	/**
	 * Ensures issue if the dependency is incompatible.
	 */
	public void testIncompatibleDependency() {

		// Managed object configuration
		RawManagedObjectMetaData<?, ?> rawMoMetaData = this
				.registerRawManagedObjectMetaData("OFFICE_MO");
		RawBoundManagedObjectMetaData<?> dependency = this
				.scopeRawManagedObjectMetaData("DEPENDENCY");

		ManagedObjectDependencyConfiguration<?> dependencyConfig = this
				.createMock(ManagedObjectDependencyConfiguration.class);
		ManagedObjectDependencyMetaData<?> dependencyMetaData = this
				.createMock(ManagedObjectDependencyMetaData.class);
		RawManagedObjectMetaData<?, ?> rawDependency = this
				.createMock(RawManagedObjectMetaData.class);

		// Record construction
		ManagedObjectConfiguration<?> configuration = this
				.record_initManagedObject("BOUND", "OFFICE_MO");
		this.record_getDependencyMetaData(rawMoMetaData, dependencyMetaData);
		this.record_getDependencyConfiguration(configuration, dependencyConfig);
		this.recordReturn(dependencyConfig,
				dependencyConfig.getDependencyKey(), null);
		this.record_dependencyMetaDataDetails(dependencyMetaData,
				(DependencyKey) null, null);
		this.recordReturn(dependencyConfig, dependencyConfig
				.getScopeManagedObjectName(), "DEPENDENCY");
		this.recordReturn(dependencyMetaData, dependencyMetaData.getType(),
				Connection.class);
		this.recordReturn(dependency, dependency.getRawManagedObjectMetaData(),
				rawDependency);
		this.recordReturn(rawDependency, rawDependency.getObjectType(),
				Integer.class);
		this.issues
				.addIssue(
						AssetType.MANAGED_OBJECT,
						"BOUND",
						"Incompatible dependency for dependency 0 (key=<indexed>, label=<no label>) (required type="
								+ Connection.class.getName()
								+ ", dependency type="
								+ Integer.class.getName() + ")");
		this.record_loadManagedObjectMetaData(rawMoMetaData, "BOUND");

		// Attempt to construct
		this.replayMockObjects();
		this.constructRawBoundManagedObjectMetaData(1, configuration);
		this.verifyMockObjects();
	}

	/**
	 * Ensures issue if extra dependencies are configured.
	 */
	public void testExtraDependenciesConfigured() {

		// Managed object configuration
		RawManagedObjectMetaData<?, ?> rawMoMetaData = this
				.registerRawManagedObjectMetaData("OFFICE_MO");
		RawBoundManagedObjectMetaData<?> dependency = this
				.scopeRawManagedObjectMetaData("DEPENDENCY");

		ManagedObjectDependencyConfiguration<?> dependencyConfig = this
				.createMock(ManagedObjectDependencyConfiguration.class);
		ManagedObjectDependencyConfiguration<?> extraDependencyConfig = this
				.createMock(ManagedObjectDependencyConfiguration.class);
		ManagedObjectDependencyMetaData<?> dependencyMetaData = this
				.createMock(ManagedObjectDependencyMetaData.class);

		// Record construction
		ManagedObjectConfiguration<?> configuration = this
				.record_initManagedObject("BOUND", "OFFICE_MO");
		this.record_getDependencyMetaData(rawMoMetaData, dependencyMetaData);
		this.record_getDependencyConfiguration(configuration, dependencyConfig,
				extraDependencyConfig);
		this.recordReturn(dependencyConfig,
				dependencyConfig.getDependencyKey(), null);
		this.recordReturn(extraDependencyConfig, extraDependencyConfig
				.getDependencyKey(), null);
		this.record_dependencyMetaDataDetails(dependencyMetaData,
				(DependencyKey) null, null);
		this.recordReturn(dependencyConfig, dependencyConfig
				.getScopeManagedObjectName(), "DEPENDENCY");
		this.record_matchingDependencyType(dependencyMetaData, dependency);
		this.issues
				.addIssue(AssetType.MANAGED_OBJECT, "BOUND",
						"Extra dependencies configured than required by ManagedObjectSourceMetaData");
		this.record_loadManagedObjectMetaData(rawMoMetaData, "BOUND");

		// Attempt to construct
		this.replayMockObjects();
		this.constructRawBoundManagedObjectMetaData(1, configuration);
		this.verifyMockObjects();
	}

	/**
	 * Ensure can construct {@link RawBoundManagedObjectMetaData} with
	 * dependency on another {@link RawBoundManagedObjectMetaData}.
	 */
	public void testDependencyOnAnotherBound() {

		final RawManagedObjectMetaData<?, ?> oneMetaData = this
				.registerRawManagedObjectMetaData("ONE");
		final ManagedObjectDependencyMetaData<?> oneDependencyMetaData = this
				.createMock(ManagedObjectDependencyMetaData.class);
		final ManagedObjectDependencyConfiguration<?> oneDependencyConfig = this
				.createMock(ManagedObjectDependencyConfiguration.class);
		final RawManagedObjectMetaData<?, ?> twoMetaData = this
				.registerRawManagedObjectMetaData("TWO");

		// Record construction
		final ManagedObjectConfiguration<?> oneConfig = this
				.record_initManagedObject("MO_A", "ONE");
		final ManagedObjectConfiguration<?> twoConfig = this
				.record_initManagedObject("MO_B", "TWO");
		this.record_getDependencyMetaData(oneMetaData, oneDependencyMetaData);
		this.record_getDependencyConfiguration(oneConfig, oneDependencyConfig);
		this.recordReturn(oneDependencyConfig, oneDependencyConfig
				.getDependencyKey(), DependencyKey.KEY);
		this.record_dependencyMetaDataDetails(oneDependencyMetaData,
				DependencyKey.KEY, null);
		this.recordReturn(oneDependencyConfig, oneDependencyConfig
				.getScopeManagedObjectName(), "MO_B");
		this.record_matchingDependencyType(oneDependencyMetaData, twoMetaData);
		this.record_loadManagedObjectMetaData(oneMetaData, "MO_A",
				new ManagedObjectIndexImpl(this.managedObjectScope, 1));
		this.record_getDependencyMetaData(twoMetaData);
		this.record_getDependencyConfiguration(twoConfig);
		this.record_loadManagedObjectMetaData(twoMetaData, "MO_B");

		this.replayMockObjects();
		RawBoundManagedObjectMetaData<?>[] rawMetaData = this
				.constructRawBoundManagedObjectMetaData(2, oneConfig, twoConfig);
		this.verifyMockObjects();

		// Validate dependencies
		RawBoundManagedObjectMetaData<?> one = rawMetaData[0];
		assertEquals("Incorrect number of dependencies", 1, one
				.getDependencies().length);
		RawBoundManagedObjectMetaData<?> two = rawMetaData[1];
		assertEquals("Incorrect dependency", two, one.getDependencies()[0]);
		assertEquals("Should be no dependencies for second", 0, rawMetaData[1]
				.getDependencies().length);
	}

	/**
	 * Ensure can construct {@link RawBoundManagedObjectMetaData} with
	 * dependency on a {@link RawBoundManagedObjectMetaData} from scope.
	 */
	public void testDependencyOnScopeBound() {

		final RawManagedObjectMetaData<?, ?> rawMoMetaData = this
				.registerRawManagedObjectMetaData("OFFICE_MO");
		ManagedObjectDependencyMetaData<?> dependencyMetaData = this
				.createMock(ManagedObjectDependencyMetaData.class);
		final ManagedObjectDependencyConfiguration<?> dependencyConfig = this
				.createMock(ManagedObjectDependencyConfiguration.class);

		final RawBoundManagedObjectMetaData<?> scopeMoMetaData = this
				.scopeRawManagedObjectMetaData("SCOPE");
		final ManagedObjectIndex dependencyMoIndex = this
				.createMock(ManagedObjectIndex.class);

		// Record construction
		ManagedObjectConfiguration<?> configuration = this
				.record_initManagedObject("BOUND", "OFFICE_MO");
		this.record_getDependencyMetaData(rawMoMetaData, dependencyMetaData);
		this.record_getDependencyConfiguration(configuration, dependencyConfig);
		this.recordReturn(dependencyConfig,
				dependencyConfig.getDependencyKey(), DependencyKey.KEY);
		this.record_dependencyMetaDataDetails(dependencyMetaData,
				DependencyKey.KEY, null);
		this.recordReturn(dependencyConfig, dependencyConfig
				.getScopeManagedObjectName(), "SCOPE");
		this.record_matchingDependencyType(dependencyMetaData, scopeMoMetaData);
		this.recordReturn(scopeMoMetaData, scopeMoMetaData
				.getManagedObjectIndex(), dependencyMoIndex);
		this.record_loadManagedObjectMetaData(rawMoMetaData, "BOUND",
				dependencyMoIndex);

		// Construct
		this.replayMockObjects();
		RawBoundManagedObjectMetaData<?>[] rawMetaData = this
				.constructRawBoundManagedObjectMetaData(1, configuration);
		this.verifyMockObjects();

		// Validate dependency mapping
		RawBoundManagedObjectMetaData<?> dependency = rawMetaData[0];
		assertEquals("Incorrect number of dependency keys", 1, dependency
				.getDependencies().length);
		assertEquals("Incorrect dependency", scopeMoMetaData, dependency
				.getDependencies()[0]);
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
	 * {@link Flow} instances.
	 */
	public void testNotAffixManagedObjectRequiringNoFlows() {

		final RawManagingOfficeMetaData<?> officeMo = this
				.createMock(RawManagingOfficeMetaData.class);

		// Record not affix as not require flows
		this.recordReturn(officeMo, officeMo.isRequireFlows(), false);

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

		final RawManagingOfficeMetaData<?> officeMo = this
				.createMock(RawManagingOfficeMetaData.class);

		// Record not affix as not require flows
		this.recordReturn(officeMo, officeMo.isRequireFlows(), true);
		this.record_initOfficeManagedObject(officeMo, "MO");
		this.recordReturn(officeMo, officeMo.getProcessBoundName(), null);
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
	 * Ensures issue if {@link ProcessState} bound {@link ManagedObject} does
	 * not match {@link RawManagedObjectMetaData} (in other words a different
	 * {@link ManagedObject}.
	 */
	public void testAffixToWrongBoundManagedObject() {

		final String BOUND_MO_NAME = "BOUND_MO";

		final RawBoundManagedObjectMetaData<?> boundMo = this
				.createMock(RawBoundManagedObjectMetaData.class);
		final ManagedObjectIndex boundMoIndex = new ManagedObjectIndexImpl(
				ManagedObjectScope.PROCESS, 0);
		final RawManagedObjectMetaData<?, ?> boundRawMoMetaData = this
				.createMock(RawManagedObjectMetaData.class);
		final RawManagingOfficeMetaData<?> officeMo = this
				.createMock(RawManagingOfficeMetaData.class);
		final RawManagedObjectMetaData<?, ?> officeRawMoMetaData = this
				.createMock(RawManagedObjectMetaData.class);

		// Record affixing the already bound managed object
		this.recordReturn(boundMo, boundMo.getBoundManagedObjectName(),
				BOUND_MO_NAME);
		this.recordReturn(boundMo, boundMo.getManagedObjectIndex(),
				boundMoIndex);
		this.recordReturn(officeMo, officeMo.isRequireFlows(), true);
		this.recordReturn(officeMo, officeMo.getRawManagedObjectMetaData(),
				officeRawMoMetaData);
		this.recordReturn(officeRawMoMetaData, officeRawMoMetaData
				.getManagedObjectName(), "MO");
		this.recordReturn(officeMo, officeMo.getProcessBoundName(),
				BOUND_MO_NAME);
		this.recordReturn(boundMo, boundMo.getRawManagedObjectMetaData(),
				boundRawMoMetaData);
		this.recordReturn(boundRawMoMetaData, boundRawMoMetaData
				.getManagedObjectName(), "WRONG");
		this.issues.addIssue(AssetType.MANAGED_OBJECT, "MO",
				"Process bound ManagedObject " + BOUND_MO_NAME
						+ " is different (bound managed object source=WRONG)");

		// Affix the managed objects
		this.replayMockObjects();
		this.affixOfficeManagingManagedObjects(1,
				new RawBoundManagedObjectMetaData[] { boundMo }, officeMo);
		this.verifyMockObjects();
	}

	/**
	 * Ensures able to affix a {@link RawManagingOfficeMetaData} that is already
	 * {@link ProcessState} bound to the {@link Office}.
	 */
	public void testAffixBoundManagedObjects() {

		final String BOUND_MO_NAME = "BOUND_MO";

		final RawBoundManagedObjectMetaData<?> boundMo = this
				.createMock(RawBoundManagedObjectMetaData.class);
		final ManagedObjectIndex boundMoIndex = new ManagedObjectIndexImpl(
				ManagedObjectScope.PROCESS, 0);
		final RawManagingOfficeMetaData<?> officeMo = this
				.createMock(RawManagingOfficeMetaData.class);
		final RawManagedObjectMetaData<?, ?> rawMoMetaData = this
				.createMock(RawManagedObjectMetaData.class);

		// Record affixing the already bound managed object
		this.recordReturn(boundMo, boundMo.getBoundManagedObjectName(),
				BOUND_MO_NAME);
		this.recordReturn(boundMo, boundMo.getManagedObjectIndex(),
				boundMoIndex);
		this.recordReturn(officeMo, officeMo.isRequireFlows(), true);
		this.recordReturn(officeMo, officeMo.getRawManagedObjectMetaData(),
				rawMoMetaData);
		this.recordReturn(rawMoMetaData, rawMoMetaData.getManagedObjectName(),
				"MO");
		this.recordReturn(officeMo, officeMo.getProcessBoundName(),
				BOUND_MO_NAME);
		this.recordReturn(boundMo, boundMo.getRawManagedObjectMetaData(),
				rawMoMetaData);

		// Affix the managed objects
		this.replayMockObjects();
		RawBoundManagedObjectMetaData<?>[] returnedBoundMos = this
				.affixOfficeManagingManagedObjects(1,
						new RawBoundManagedObjectMetaData[] { boundMo },
						officeMo);
		this.verifyMockObjects();

		// Verify the bound managed object
		assertEquals("Incorrect bound managed object", boundMo,
				returnedBoundMos[0]);
	}

	/**
	 * Ensures issue if affixing a {@link ManagedObject} with dependencies that
	 * is not {@link ProcessState} bound.
	 */
	public void testAffixNonBoundManagedObjectWithDependencies() {

		final RawManagingOfficeMetaData<?> officeMo = this
				.createMock(RawManagingOfficeMetaData.class);
		final ManagedObjectDependencyMetaData<?> dependencyMetaData = this
				.createMock(ManagedObjectDependencyMetaData.class);

		// Record affixing the non bound managed object
		this.recordReturn(officeMo, officeMo.isRequireFlows(), true);
		final RawManagedObjectMetaData<?, ?> rawMoMetaData = this
				.record_initOfficeManagedObject(officeMo, "MO");
		this
				.recordReturn(officeMo, officeMo.getProcessBoundName(),
						"NOT_BOUND");
		this.record_getDependencyMetaData(rawMoMetaData, dependencyMetaData);
		this.issues
				.addIssue(
						AssetType.MANAGED_OBJECT,
						"MO",
						"Must map dependencies for affixed ManagedObjectSource. Please add to process and map dependencies.");

		// Affix the managed objects
		this.replayMockObjects();
		this.affixOfficeManagingManagedObjects(0,
				new RawBoundManagedObjectMetaData[0], officeMo);
		this.verifyMockObjects();
	}

	/**
	 * Ensures able to affix a {@link RawManagingOfficeMetaData} that is not
	 * {@link ProcessState} bound to the {@link Office}.
	 */
	public void testAffixNonBoundManagedObjects() {

		final RawManagingOfficeMetaData<?> officeMo = this
				.createMock(RawManagingOfficeMetaData.class);

		// Record affixing the non bound managed object
		this.recordReturn(officeMo, officeMo.isRequireFlows(), true);
		final RawManagedObjectMetaData<?, ?> rawMoMetaData = this
				.record_initOfficeManagedObject(officeMo, "MO");
		this
				.recordReturn(officeMo, officeMo.getProcessBoundName(),
						"NOT_BOUND");
		this.record_getDependencyMetaData(rawMoMetaData);
		ManagedObjectMetaData<?> moMetaData = this
				.record_loadManagedObjectMetaData(rawMoMetaData, "NOT_BOUND");

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
		assertEquals("Incorrect bound managed object meta-data", moMetaData,
				returnedBoundMo.getManagedObjectMetaData());
	}

	/**
	 * Ensures able to affix a {@link RawManagingOfficeMetaData} to the
	 * {@link ProcessState} with a {@link ManagedObject} already exists.
	 */
	public void testAffixNonBoundManagedObjectWithExistingBound() {

		final RawBoundManagedObjectMetaData<?> alreadyBoundMo = this
				.createMock(RawBoundManagedObjectMetaData.class);
		final ManagedObjectIndex alreadyBoundIndex = new ManagedObjectIndexImpl(
				ManagedObjectScope.PROCESS, 0);
		final RawManagingOfficeMetaData<?> officeMo = this
				.createMock(RawManagingOfficeMetaData.class);

		// Record affixing the non bound managed object with existing
		this.recordReturn(alreadyBoundMo, alreadyBoundMo
				.getBoundManagedObjectName(), "ALREADY_BOUND");
		this.recordReturn(alreadyBoundMo, alreadyBoundMo
				.getManagedObjectIndex(), alreadyBoundIndex);
		this.recordReturn(officeMo, officeMo.isRequireFlows(), true);
		final RawManagedObjectMetaData<?, ?> rawMoMetaData = this
				.record_initOfficeManagedObject(officeMo, "MO");
		this
				.recordReturn(officeMo, officeMo.getProcessBoundName(),
						"NEW_BOUND");
		this.record_getDependencyMetaData(rawMoMetaData);
		ManagedObjectMetaData<?> moMetaData = this
				.record_loadManagedObjectMetaData(rawMoMetaData, "NEW_BOUND");

		// Affix the managed objects
		this.replayMockObjects();
		RawBoundManagedObjectMetaData<?>[] returnedBoundMos = this
				.affixOfficeManagingManagedObjects(2,
						new RawBoundManagedObjectMetaData[] { alreadyBoundMo },
						officeMo);
		this.verifyMockObjects();

		// Verify bound
		assertEquals("Incorrect already bound managed object", alreadyBoundMo,
				returnedBoundMos[0]);
		ManagedObjectIndex moIndex = returnedBoundMos[1]
				.getManagedObjectIndex();
		assertEquals("Incorrect managed object scope of affixed",
				ManagedObjectScope.PROCESS, moIndex.getManagedObjectScope());
		assertEquals("Incorrect managed object index in scope of affixed", 1,
				moIndex.getIndexOfManagedObjectWithinScope());
		assertEquals("Incorrect bound managed object meta-data of affixed",
				moMetaData, returnedBoundMos[1].getManagedObjectMetaData());
	}

	/**
	 * Dependency key {@link Enum}.
	 */
	private enum DependencyKey {
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
	 * Records initialising the {@link RawBoundManagedObjectMetaData}.
	 * 
	 * @param boundName
	 *            Bound name.
	 * @param officeName
	 *            {@link Office} name.
	 * @return {@link ManagedObjectConfiguration}.
	 */
	private ManagedObjectConfiguration<?> record_initManagedObject(
			String boundName, String officeName) {

		// Create the mock objects
		final ManagedObjectConfiguration<?> configuration = this
				.createMock(ManagedObjectConfiguration.class);

		// Record initiating the managed object meta-data
		this.recordReturn(configuration, configuration
				.getBoundManagedObjectName(), boundName);
		this.recordReturn(configuration, configuration
				.getOfficeManagedObjectName(), officeName);

		// Return the configuration
		return configuration;
	}

	/**
	 * Records obtaining the {@link ManagedObjectDependencyMetaData}.
	 * 
	 * @param rawMoMetaData
	 *            {@link RawManagedObjectMetaData}.
	 * @param dependencyMetaData
	 *            {@link ManagedObjectDependencyMetaData} instances.
	 */
	private void record_getDependencyMetaData(
			RawManagedObjectMetaData<?, ?> rawMoMetaData,
			ManagedObjectDependencyMetaData<?>... dependencyMetaData) {

		// Create the mocks
		final ManagedObjectSourceMetaData<?, ?> mosMetaData = this
				.createMock(ManagedObjectSourceMetaData.class);

		// Record obtaining the dependency meta-data
		this.recordReturn(rawMoMetaData, rawMoMetaData
				.getManagedObjectSourceMetaData(), mosMetaData);
		this.recordReturn(mosMetaData, mosMetaData.getDependencyMetaData(),
				dependencyMetaData);
	}

	/**
	 * Records obtaining the {@link ManagedObjectDependencyConfiguration}.
	 * 
	 * @param configuration
	 *            {@link ManagedObjectConfiguration}.
	 * @param dependencyConfiguration
	 *            {@link ManagedObjectDependencyConfiguration}.
	 */
	private void record_getDependencyConfiguration(
			ManagedObjectConfiguration<?> configuration,
			ManagedObjectDependencyConfiguration<?>... dependencyConfiguration) {

		// Record obtaining the dependency configuration
		this.recordReturn(configuration, configuration
				.getDependencyConfiguration(), dependencyConfiguration);
	}

	/**
	 * Records obtaining the {@link ManagedObjectDependencyMetaData} details.
	 * 
	 * @param dependency
	 *            {@link ManagedObjectDependencyMetaData}.
	 * @param key
	 *            Dependency key.
	 * @param label
	 *            Label for the dependency.
	 */
	private <D extends Enum<D>> void record_dependencyMetaDataDetails(
			ManagedObjectDependencyMetaData<?> dependencyMetaData, D key,
			String label) {
		// Record obtaining the dependency meta-data key and label
		this.recordReturn(dependencyMetaData, dependencyMetaData.getKey(), key);
		this.recordReturn(dependencyMetaData, dependencyMetaData.getLabel(),
				label);
	}

	/**
	 * Records matching type for a dependency on another
	 * {@link RawManagedObjectMetaData} in same bounding scope.
	 * 
	 * @param dependencyMetaData
	 *            {@link ManagedObjectDependencyMetaData}.
	 * @param dependency
	 *            {@link RawManagedObjectMetaData} of dependency is same bound
	 *            scope.
	 */
	private void record_matchingDependencyType(
			ManagedObjectDependencyMetaData<?> dependencyMetaData,
			RawManagedObjectMetaData<?, ?> dependency) {

		// Records matching type for the dependency
		this.recordReturn(dependencyMetaData, dependencyMetaData.getType(),
				Connection.class);
		this.recordReturn(dependency, dependency.getObjectType(),
				Connection.class);
	}

	/**
	 * Records matching type for a dependency on another scope.
	 * 
	 * @param dependencyMetaData
	 *            {@link ManagedObjectDependencyMetaData}.
	 * @param dependency
	 *            {@link RawBoundManagedObjectMetaData} of another scope.
	 */
	private void record_matchingDependencyType(
			ManagedObjectDependencyMetaData<?> dependencyMetaData,
			RawBoundManagedObjectMetaData<?> dependency) {

		RawManagedObjectMetaData<?, ?> rawDependency = this
				.createMock(RawManagedObjectMetaData.class);

		// Records matching type for the dependency
		this.recordReturn(dependencyMetaData, dependencyMetaData.getType(),
				Connection.class);
		this.recordReturn(dependency, dependency.getRawManagedObjectMetaData(),
				rawDependency);
		this.recordReturn(rawDependency, rawDependency.getObjectType(),
				Connection.class);
	}

	/**
	 * Records loading the {@link ManagedObjectMetaData} from the
	 * {@link RawManagedObjectMetaData}.
	 * 
	 * @param rawMoMetaData
	 *            {@link RawManagedObjectMetaData}.
	 * @param dependencies
	 *            Dependency {@link ManagedObjectIndex}.
	 * @param {@link ManagedObjectMetaData} created.
	 */
	private ManagedObjectMetaData<?> record_loadManagedObjectMetaData(
			RawManagedObjectMetaData<?, ?> rawMoMetaData,
			final String boundName, final ManagedObjectIndex... dependencies) {

		// Create the necessary mock objects
		ManagedObjectMetaData<?> moMetaData = this
				.createMock(ManagedObjectMetaData.class);

		// Record creating the managed object meta-data
		this.recordReturn(rawMoMetaData, rawMoMetaData
				.createManagedObjectMetaData(null, dependencies,
						this.assetManagerFactory, this.issues), moMetaData,
				new AbstractMatcher() {
					@Override
					public boolean matches(Object[] expected, Object[] actual) {
						// Verify the raw bound managed object
						RawBoundManagedObjectMetaData<?> boundMo = (RawBoundManagedObjectMetaData<?>) actual[0];
						assertEquals("Incorrect bound managed object",
								boundName, boundMo.getBoundManagedObjectName());

						// Verify the dependencies
						ManagedObjectIndex[] indexes = (ManagedObjectIndex[]) actual[1];
						assertEquals("Incorrect number of dependencies",
								dependencies.length, (indexes == null ? 0
										: indexes.length));
						for (int i = 0; i < dependencies.length; i++) {
							ManagedObjectIndex dependency = dependencies[i];
							ManagedObjectIndex index = indexes[i];
							if (dependency == index) {
								return true; // same as from another scope
							}
							// Verify created index of same scope is correct
							assertEquals("Incorrect dependency scope for " + i,
									dependency.getManagedObjectScope(), index
											.getManagedObjectScope());
							assertEquals(
									"Incorrect dependency index for " + i,
									dependency
											.getIndexOfManagedObjectWithinScope(),
									index.getIndexOfManagedObjectWithinScope());
						}

						// Ensure other items are correct
						assertEquals(
								"Incorrect asset manager factory",
								RawBoundManagedObjectMetaDataTest.this.assetManagerFactory,
								actual[2]);
						assertEquals("Incorrect issues",
								RawBoundManagedObjectMetaDataTest.this.issues,
								actual[3]);

						// Matches if at this point
						return true;
					}
				});

		// Return the managed object meta-data
		return moMetaData;
	}

	/**
	 * Records initialising the {@link RawManagingOfficeMetaData}.
	 * 
	 * @param officeMo
	 *            {@link RawManagingOfficeMetaData}.
	 * @param managedObjectSourceName
	 *            Name of the {@link ManagedObjectSource}.
	 * @return {@link RawManagedObjectMetaData} for the
	 *         {@link RawManagingOfficeMetaData}.
	 */
	private RawManagedObjectMetaData<?, ?> record_initOfficeManagedObject(
			RawManagingOfficeMetaData<?> officeMo,
			String managedObjectSourceName) {

		final RawManagedObjectMetaData<?, ?> rawMoMetaData = this
				.createMock(RawManagedObjectMetaData.class);

		// Record obtaining the managed object source name
		this.recordReturn(officeMo, officeMo.getRawManagedObjectMetaData(),
				rawMoMetaData);
		this.recordReturn(rawMoMetaData, rawMoMetaData.getManagedObjectName(),
				managedObjectSourceName);

		// Return the raw managed object meta-data
		return rawMoMetaData;
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
	 * Affixes the {@link RawManagingOfficeMetaData} instances.
	 * 
	 * @param expectedNumberReturned
	 *            Expected number of {@link RawBoundManagedObjectMetaData}
	 *            instances to be returned.
	 * @param processBoundManagedObjectMetaData
	 *            {@link ProcessState} {@link RawBoundManagedObjectMetaData}
	 *            instances.
	 * @param officeManagingManagedObjects
	 *            {@link RawManagingOfficeMetaData} instances.
	 * @return Returned {@link RawBoundManagedObjectMetaData}.
	 */
	private RawBoundManagedObjectMetaData<?>[] affixOfficeManagingManagedObjects(
			int expectedNumberReturned,
			RawBoundManagedObjectMetaData<?>[] processBoundManagedObjectMetaData,
			RawManagingOfficeMetaData<?>... officeManagingManagedObjects) {

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