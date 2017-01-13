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
package net.officefloor.frame.impl.construct.managedobject;

import java.sql.Connection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.build.OfficeFloorIssues;
import net.officefloor.frame.api.build.OfficeFloorIssues.AssetType;
import net.officefloor.frame.api.governance.Governance;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.extension.ExtensionInterfaceFactory;
import net.officefloor.frame.api.managedobject.source.ManagedObjectDependencyMetaData;
import net.officefloor.frame.api.managedobject.source.ManagedObjectExtensionInterfaceMetaData;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSourceMetaData;
import net.officefloor.frame.impl.execute.managedobject.ManagedObjectIndexImpl;
import net.officefloor.frame.internal.configuration.InputManagedObjectConfiguration;
import net.officefloor.frame.internal.configuration.ManagedObjectConfiguration;
import net.officefloor.frame.internal.configuration.ManagedObjectDependencyConfiguration;
import net.officefloor.frame.internal.configuration.ManagedObjectGovernanceConfiguration;
import net.officefloor.frame.internal.construct.AssetManagerFactory;
import net.officefloor.frame.internal.construct.RawBoundManagedObjectInstanceMetaData;
import net.officefloor.frame.internal.construct.RawBoundManagedObjectMetaData;
import net.officefloor.frame.internal.construct.RawGovernanceMetaData;
import net.officefloor.frame.internal.construct.RawManagedObjectMetaData;
import net.officefloor.frame.internal.construct.RawManagingOfficeMetaData;
import net.officefloor.frame.internal.structure.ExtensionInterfaceExtractor;
import net.officefloor.frame.internal.structure.ManagedObjectGovernanceMetaData;
import net.officefloor.frame.internal.structure.ManagedObjectIndex;
import net.officefloor.frame.internal.structure.ManagedObjectMetaData;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
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
	private final OfficeFloorIssues issues = this.createMock(OfficeFloorIssues.class);

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
	private AssetManagerFactory assetManagerFactory = this.createMock(AssetManagerFactory.class);

	/**
	 * Registered {@link RawManagedObjectMetaData} instances by their name.
	 */
	private final Map<String, RawManagedObjectMetaData<?, ?>> registeredManagedObjects = new HashMap<String, RawManagedObjectMetaData<?, ?>>();

	/**
	 * Scope bound {@link RawBoundManagedObjectMetaData} instances by their
	 * scope name.
	 */
	private final Map<String, RawBoundManagedObjectMetaData> scopeManagedObjects = new HashMap<String, RawBoundManagedObjectMetaData>();

	/**
	 * Input {@link ManagedObject} defaults.
	 */
	private final Map<String, String> inputManagedObjectDefaults = new HashMap<String, String>();

	/**
	 * {@link RawGovernanceMetaData}.
	 */
	private final Map<String, RawGovernanceMetaData<?, ?>> rawGovernanceMetaDatas = new HashMap<String, RawGovernanceMetaData<?, ?>>();

	/**
	 * Ensure issue if no bound name.
	 */
	public void testNoBoundName() {

		final ManagedObjectConfiguration<?> configuration = this.createMock(ManagedObjectConfiguration.class);

		// Record construction
		this.recordReturn(configuration, configuration.getBoundManagedObjectName(), null);
		this.issues.addIssue(this.assetType, this.assetName, "No bound name for managed object");

		// Attempt to construct
		this.replayMockObjects();
		this.constructRawBoundManagedObjectMetaData(0, configuration);
		this.verifyMockObjects();
	}

	/**
	 * Ensure issue if no office name.
	 */
	public void testNoOfficeName() {

		final ManagedObjectConfiguration<?> configuration = this.createMock(ManagedObjectConfiguration.class);

		// Record construction
		this.recordReturn(configuration, configuration.getBoundManagedObjectName(), "BOUND");
		this.recordReturn(configuration, configuration.getOfficeManagedObjectName(), "");
		this.issues.addIssue(this.assetType, this.assetName, "No office name for bound managed object of name 'BOUND'");

		// Attempt to construct
		this.replayMockObjects();
		this.constructRawBoundManagedObjectMetaData(0, configuration);
		this.verifyMockObjects();
	}

	/**
	 * Ensure issue if no corresponding {@link RawManagedObjectMetaData}.
	 */
	public void testNoManagedObjectMetaData() {

		final ManagedObjectConfiguration<?> configuration = this.createMock(ManagedObjectConfiguration.class);

		// Record construction
		this.recordReturn(configuration, configuration.getBoundManagedObjectName(), "BOUND_MO");
		this.recordReturn(configuration, configuration.getOfficeManagedObjectName(), "OFFICE_MO");
		this.issues.addIssue(this.assetType, this.assetName,
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
		RawManagedObjectMetaData<?, ?> rawMoMetaData = this.registerRawManagedObjectMetaData("OFFICE_MO");

		// Record construction
		ManagedObjectConfiguration<?> configuration = this.record_initManagedObject("BOUND", "OFFICE_MO");
		this.record_getDependencyMetaData(rawMoMetaData);
		ManagedObjectMetaData<?> moMetaData = this.record_loadManagedObjectMetaData(rawMoMetaData, "BOUND", 0, null);

		// Construct bound meta-data without scope managed objects
		this.replayMockObjects();
		RawBoundManagedObjectMetaData[] rawBoundMoMetaData = RawBoundManagedObjectMetaDataImpl.getFactory()
				.constructBoundManagedObjectMetaData(new ManagedObjectConfiguration[] { configuration }, this.issues,
						this.managedObjectScope, this.assetType, this.assetName, this.assetManagerFactory,
						this.registeredManagedObjects, null, null, null, null);
		ManagedObjectMetaData<?> boundMoMetaData = rawBoundMoMetaData[0].getRawBoundManagedObjectInstanceMetaData()[0]
				.getManagedObjectMetaData();
		this.verifyMockObjects();

		// Verify correct managed object meta-data
		assertEquals("Incorrect managed object meta-data", moMetaData, boundMoMetaData);
	}

	/**
	 * Ensures reports issue if name clash between bound {@link ManagedObject}
	 * instances.
	 */
	public void testNameClashBetweenBoundManagedObjects() {

		final ManagedObjectConfiguration<?> oneConfiguration = this.createMock(ManagedObjectConfiguration.class);
		final RawManagedObjectMetaData<?, ?> oneMetaData = this.registerRawManagedObjectMetaData("ONE_MO");
		final ManagedObjectConfiguration<?> twoConfiguration = this.createMock(ManagedObjectConfiguration.class);

		final String CLASH_NAME = "CLASH";

		// Record clash between bound names
		this.recordReturn(oneConfiguration, oneConfiguration.getBoundManagedObjectName(), CLASH_NAME);
		this.recordReturn(oneConfiguration, oneConfiguration.getOfficeManagedObjectName(), "ONE_MO");
		this.recordReturn(oneConfiguration, oneConfiguration.getDependencyConfiguration(), null);
		this.recordReturn(oneConfiguration, oneConfiguration.getGovernanceConfiguration(), null);
		this.recordReturn(twoConfiguration, twoConfiguration.getBoundManagedObjectName(), CLASH_NAME);
		this.issues.addIssue(AssetType.MANAGED_OBJECT, CLASH_NAME,
				"Name clash between bound Managed Objects (name=" + CLASH_NAME + ")");
		this.record_getDependencyMetaData(oneMetaData);
		this.record_loadManagedObjectMetaData(oneMetaData, CLASH_NAME, 0, null);

		// Construct
		this.replayMockObjects();
		RawBoundManagedObjectMetaData boundMetaData = this.constructRawBoundManagedObjectMetaData(1, oneConfiguration,
				twoConfiguration)[0];
		this.verifyMockObjects();

		// Ensure only the bound managed object constructed
		assertEquals("Should not return second managed object", 1,
				boundMetaData.getRawBoundManagedObjectInstanceMetaData().length);
		RawBoundManagedObjectInstanceMetaData<?> instanceMetaData = boundMetaData
				.getRawBoundManagedObjectInstanceMetaData()[0];
		assertEquals("Should only return first bound managed object", oneMetaData,
				instanceMetaData.getRawManagedObjectMetaData());
	}

	/**
	 * Ensure can construct {@link RawBoundManagedObjectMetaData} with no
	 * dependencies.
	 */
	public void testBindManagedObject() {

		// Managed object configuration
		RawManagedObjectMetaData<?, ?> rawMoMetaData = this.registerRawManagedObjectMetaData("OFFICE_MO");

		// Record construction
		ManagedObjectConfiguration<?> configuration = this.record_initManagedObject("BOUND", "OFFICE_MO");
		this.record_getDependencyMetaData(rawMoMetaData);
		ManagedObjectMetaData<?> moMetaData = this.record_loadManagedObjectMetaData(rawMoMetaData, "BOUND", 0, null);

		// Construct and obtain the managed object meta-data
		this.replayMockObjects();
		RawBoundManagedObjectMetaData rawBoundMoMetaData = this.constructRawBoundManagedObjectMetaData(1,
				configuration)[0];
		assertEquals("Incorrect default instance index", 0, rawBoundMoMetaData.getDefaultInstanceIndex());
		assertEquals("Incorrect managed object instances", 1,
				rawBoundMoMetaData.getRawBoundManagedObjectInstanceMetaData().length);
		RawBoundManagedObjectInstanceMetaData<?> rawBoundMoInstanceMetaData = rawBoundMoMetaData
				.getRawBoundManagedObjectInstanceMetaData()[0];
		assertEquals("Incorrect managed object meta-data", moMetaData,
				rawBoundMoInstanceMetaData.getManagedObjectMetaData());
		this.verifyMockObjects();
	}

	/**
	 * Ensure issue if dependency not configured for a dependency key.
	 */
	public void testMissingDependencyConfiguration() {

		// Managed object configuration
		RawManagedObjectMetaData<?, ?> rawMoMetaData = this.registerRawManagedObjectMetaData("OFFICE_MO");
		ManagedObjectDependencyMetaData<?> dependencyMetaData = this.createMock(ManagedObjectDependencyMetaData.class);

		// Record construction
		final ManagedObjectConfiguration<?> configuration = this.record_initManagedObject("BOUND", "OFFICE_MO");
		this.record_getDependencyMetaData(rawMoMetaData, dependencyMetaData);
		this.record_dependencyMetaDataDetails(dependencyMetaData, DependencyKey.KEY, null);
		this.issues.addIssue(AssetType.MANAGED_OBJECT, "BOUND",
				"No mapping configured for dependency 0 (key=" + DependencyKey.KEY + ", label=<no label>)");
		this.record_loadManagedObjectMetaData(rawMoMetaData, "BOUND", 0, null);

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
		RawManagedObjectMetaData<?, ?> rawMoMetaData = this.registerRawManagedObjectMetaData("OFFICE_MO");

		ManagedObjectDependencyConfiguration<?> dependencyConfig = this
				.createMock(ManagedObjectDependencyConfiguration.class);
		ManagedObjectDependencyMetaData<?> dependencyMetaData = this.createMock(ManagedObjectDependencyMetaData.class);

		// Record construction
		final ManagedObjectConfiguration<?> configuration = this.record_initManagedObject("BOUND", "OFFICE_MO",
				dependencyConfig);
		this.record_getDependencyMetaData(rawMoMetaData, dependencyMetaData);
		this.recordReturn(dependencyConfig, dependencyConfig.getDependencyKey(), DependencyKey.KEY);
		this.record_dependencyMetaDataDetails(dependencyMetaData, DependencyKey.KEY, "LABEL");
		this.recordReturn(dependencyConfig, dependencyConfig.getScopeManagedObjectName(), "NOT AVAILABLE");
		this.issues.addIssue(AssetType.MANAGED_OBJECT, "BOUND",
				"No dependent ManagedObject by name 'NOT AVAILABLE' for dependency 0 (key=" + DependencyKey.KEY
						+ ", label=LABEL)");
		this.record_loadManagedObjectMetaData(rawMoMetaData, "BOUND", 0, null);

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
		RawManagedObjectMetaData<?, ?> rawMoMetaData = this.registerRawManagedObjectMetaData("OFFICE_MO");
		RawBoundManagedObjectMetaData dependency = this.scopeRawManagedObjectMetaData("DEPENDENCY");
		RawBoundManagedObjectInstanceMetaData<?> dependencyInstance = this
				.createMock(RawBoundManagedObjectInstanceMetaData.class);

		ManagedObjectDependencyConfiguration<?> dependencyConfig = this
				.createMock(ManagedObjectDependencyConfiguration.class);
		ManagedObjectDependencyMetaData<?> dependencyMetaData = this.createMock(ManagedObjectDependencyMetaData.class);
		RawManagedObjectMetaData<?, ?> rawDependency = this.createMock(RawManagedObjectMetaData.class);

		// Record construction
		ManagedObjectConfiguration<?> configuration = this.record_initManagedObject("BOUND", "OFFICE_MO",
				dependencyConfig);
		this.record_getDependencyMetaData(rawMoMetaData, dependencyMetaData);
		this.recordReturn(dependencyConfig, dependencyConfig.getDependencyKey(), null);
		this.record_dependencyMetaDataDetails(dependencyMetaData, (DependencyKey) null, null);
		this.recordReturn(dependencyConfig, dependencyConfig.getScopeManagedObjectName(), "DEPENDENCY");
		this.recordReturn(dependencyMetaData, dependencyMetaData.getType(), Connection.class);
		this.recordReturn(dependency, dependency.getRawBoundManagedObjectInstanceMetaData(),
				new RawBoundManagedObjectInstanceMetaData[] { dependencyInstance });
		this.recordReturn(dependencyInstance, dependencyInstance.getRawManagedObjectMetaData(), rawDependency);
		this.recordReturn(rawDependency, rawDependency.getObjectType(), Integer.class);
		this.recordReturn(rawDependency, rawDependency.getManagedObjectName(), "MANAGED_OBJECT_SOURCE");
		this.issues.addIssue(AssetType.MANAGED_OBJECT, "BOUND",
				"Incompatible dependency for dependency 0 (key=<indexed>, label=<no label>) (required type="
						+ Connection.class.getName() + ", dependency type=" + Integer.class.getName()
						+ ", ManagedObjectSource=MANAGED_OBJECT_SOURCE)");
		this.record_loadManagedObjectMetaData(rawMoMetaData, "BOUND", 0, null);

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
		RawManagedObjectMetaData<?, ?> rawMoMetaData = this.registerRawManagedObjectMetaData("OFFICE_MO");
		RawBoundManagedObjectMetaData dependency = this.scopeRawManagedObjectMetaData("DEPENDENCY");

		ManagedObjectDependencyConfiguration<?> dependencyConfig = this
				.createMock(ManagedObjectDependencyConfiguration.class);
		ManagedObjectDependencyConfiguration<?> extraDependencyConfig = this
				.createMock(ManagedObjectDependencyConfiguration.class);
		ManagedObjectDependencyMetaData<?> dependencyMetaData = this.createMock(ManagedObjectDependencyMetaData.class);

		// Record construction
		ManagedObjectConfiguration<?> configuration = this.record_initManagedObject("BOUND", "OFFICE_MO",
				dependencyConfig, extraDependencyConfig);
		this.record_getDependencyMetaData(rawMoMetaData, dependencyMetaData);
		this.recordReturn(dependencyConfig, dependencyConfig.getDependencyKey(), null);
		this.recordReturn(extraDependencyConfig, extraDependencyConfig.getDependencyKey(), null);
		this.record_dependencyMetaDataDetails(dependencyMetaData, (DependencyKey) null, null);
		this.recordReturn(dependencyConfig, dependencyConfig.getScopeManagedObjectName(), "DEPENDENCY");
		this.record_matchingDependencyType(dependencyMetaData, dependency);
		this.issues.addIssue(AssetType.MANAGED_OBJECT, "BOUND",
				"Extra dependencies configured than required by ManagedObjectSourceMetaData");
		this.record_loadManagedObjectMetaData(rawMoMetaData, "BOUND", 0, null);

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

		final RawManagedObjectMetaData<?, ?> oneMetaData = this.registerRawManagedObjectMetaData("ONE");
		final ManagedObjectDependencyMetaData<?> oneDependencyMetaData = this
				.createMock(ManagedObjectDependencyMetaData.class);
		final ManagedObjectDependencyConfiguration<?> oneDependencyConfig = this
				.createMock(ManagedObjectDependencyConfiguration.class);
		final RawManagedObjectMetaData<?, ?> twoMetaData = this.registerRawManagedObjectMetaData("TWO");

		// Record construction
		final ManagedObjectConfiguration<?> oneConfig = this.record_initManagedObject("MO_A", "ONE",
				oneDependencyConfig);
		final ManagedObjectConfiguration<?> twoConfig = this.record_initManagedObject("MO_B", "TWO");

		// Record dependencies
		this.record_getDependencyMetaData(oneMetaData, oneDependencyMetaData);
		this.recordReturn(oneDependencyConfig, oneDependencyConfig.getDependencyKey(), DependencyKey.KEY);
		this.record_dependencyMetaDataDetails(oneDependencyMetaData, DependencyKey.KEY, null);
		this.recordReturn(oneDependencyConfig, oneDependencyConfig.getScopeManagedObjectName(), "MO_B");
		this.record_matchingDependencyType(oneDependencyMetaData, twoMetaData);
		this.record_getDependencyMetaData(twoMetaData);

		// Record loading meta-data
		this.record_loadManagedObjectMetaData(oneMetaData, "MO_A", 0, null,
				new ManagedObjectIndexImpl(this.managedObjectScope, 1));
		this.record_loadManagedObjectMetaData(twoMetaData, "MO_B", 0, null);

		this.replayMockObjects();
		RawBoundManagedObjectMetaData[] rawMetaData = this.constructRawBoundManagedObjectMetaData(2, oneConfig,
				twoConfig);
		this.verifyMockObjects();

		// Validate dependencies
		RawBoundManagedObjectMetaData one = rawMetaData[0];
		assertEquals("Incorrect number of one instances", 1, one.getRawBoundManagedObjectInstanceMetaData().length);
		RawBoundManagedObjectInstanceMetaData<?> oneInstance = one.getRawBoundManagedObjectInstanceMetaData()[0];
		assertEquals("Incorrect number of dependencies", 1, oneInstance.getDependencies().length);
		RawBoundManagedObjectMetaData two = rawMetaData[1];
		assertEquals("Incorrect number of two instances", 1, two.getRawBoundManagedObjectInstanceMetaData().length);
		RawBoundManagedObjectInstanceMetaData<?> twoInstance = two.getRawBoundManagedObjectInstanceMetaData()[0];
		assertEquals("Incorrect dependency", two, oneInstance.getDependencies()[0]);
		assertEquals("Should be no dependencies for second", 0, twoInstance.getDependencies().length);
	}

	/**
	 * Ensures the order of {@link RawBoundManagedObjectMetaData} is order to
	 * clean up the {@link ManagedObject} instances.
	 */
	public void testOrderingManagedObjectsByDependenciesForCleanup() {

		final RawManagedObjectMetaData<?, ?> oneMetaData = this.registerRawManagedObjectMetaData("ONE");
		final RawManagedObjectMetaData<?, ?> twoMetaData = this.registerRawManagedObjectMetaData("TWO");
		final ManagedObjectDependencyConfiguration<?> twoDependencyConfig = this
				.createMock(ManagedObjectDependencyConfiguration.class);
		final ManagedObjectDependencyMetaData<?> twoDependencyMetaData = this
				.createMock(ManagedObjectDependencyMetaData.class);
		final RawManagedObjectMetaData<?, ?> threeMetaData = this.registerRawManagedObjectMetaData("THREE");
		final ManagedObjectDependencyConfiguration<?> threeDependencyConfig = this
				.createMock(ManagedObjectDependencyConfiguration.class);
		final ManagedObjectDependencyMetaData<?> threeDependencyMetaData = this
				.createMock(ManagedObjectDependencyMetaData.class);

		// Record construction
		final ManagedObjectConfiguration<?> oneConfig = this.record_initManagedObject("MO_ONE", "ONE");
		final ManagedObjectConfiguration<?> twoConfig = this.record_initManagedObject("MO_TWO", "TWO",
				twoDependencyConfig);
		final ManagedObjectConfiguration<?> threeConfig = this.record_initManagedObject("MO_THREE", "THREE",
				threeDependencyConfig);

		// Record loading dependencies for first managed object
		this.record_getDependencyMetaData(oneMetaData);

		// Record loading dependencies for second managed object
		this.record_getDependencyMetaData(twoMetaData, twoDependencyMetaData);
		this.recordReturn(twoDependencyConfig, twoDependencyConfig.getDependencyKey(), DependencyKey.KEY);
		this.record_dependencyMetaDataDetails(twoDependencyMetaData, DependencyKey.KEY, null);
		this.recordReturn(twoDependencyConfig, twoDependencyConfig.getScopeManagedObjectName(), "MO_ONE");
		this.record_matchingDependencyType(twoDependencyMetaData, oneMetaData);

		// Record loading dependencies for third managed object
		this.record_getDependencyMetaData(threeMetaData, threeDependencyMetaData);
		this.recordReturn(threeDependencyConfig, threeDependencyConfig.getDependencyKey(), DependencyKey.KEY);
		this.record_dependencyMetaDataDetails(threeDependencyMetaData, DependencyKey.KEY, null);
		this.recordReturn(threeDependencyConfig, threeDependencyConfig.getScopeManagedObjectName(), "MO_TWO");
		this.record_matchingDependencyType(threeDependencyMetaData, twoMetaData);

		// Record loading the meta data
		this.record_loadManagedObjectMetaData(threeMetaData, "MO_THREE", 0, null,
				new ManagedObjectIndexImpl(this.managedObjectScope, 1));
		this.record_loadManagedObjectMetaData(twoMetaData, "MO_TWO", 0, null,
				new ManagedObjectIndexImpl(this.managedObjectScope, 2));
		this.record_loadManagedObjectMetaData(oneMetaData, "MO_ONE", 0, null);

		this.replayMockObjects();
		RawBoundManagedObjectMetaData[] rawMetaData = this.constructRawBoundManagedObjectMetaData(3, oneConfig,
				twoConfig, threeConfig);
		this.verifyMockObjects();

		// Validate order
		RawBoundManagedObjectMetaData three = rawMetaData[0];
		assertEquals("MO_THREE", three.getBoundManagedObjectName());
		RawBoundManagedObjectMetaData two = rawMetaData[1];
		assertEquals("MO_TWO", two.getBoundManagedObjectName());
		RawBoundManagedObjectMetaData one = rawMetaData[2];
		assertEquals("MO_ONE", one.getBoundManagedObjectName());
	}

	/**
	 * Ensure able to detect cyclic dependencies.
	 */
	public void testDetectCyclicDependencies() {

		final RawManagedObjectMetaData<?, ?> oneMetaData = this.registerRawManagedObjectMetaData("ONE");
		final ManagedObjectDependencyMetaData<?> oneDependencyMetaData = this
				.createMock(ManagedObjectDependencyMetaData.class);
		final ManagedObjectDependencyConfiguration<?> oneDependencyConfig = this
				.createMock(ManagedObjectDependencyConfiguration.class);
		final RawManagedObjectMetaData<?, ?> twoMetaData = this.registerRawManagedObjectMetaData("TWO");
		final ManagedObjectDependencyMetaData<?> twoDependencyMetaData = this
				.createMock(ManagedObjectDependencyMetaData.class);
		final ManagedObjectDependencyConfiguration<?> twoDependencyConfig = this
				.createMock(ManagedObjectDependencyConfiguration.class);

		// Record construction
		final ManagedObjectConfiguration<?> oneConfig = this.record_initManagedObject("MO_A", "ONE",
				oneDependencyConfig);
		final ManagedObjectConfiguration<?> twoConfig = this.record_initManagedObject("MO_B", "TWO",
				twoDependencyConfig);

		// Record managed object one
		this.record_getDependencyMetaData(oneMetaData, oneDependencyMetaData);
		this.recordReturn(oneDependencyConfig, oneDependencyConfig.getDependencyKey(), DependencyKey.KEY);
		this.record_dependencyMetaDataDetails(oneDependencyMetaData, DependencyKey.KEY, null);
		this.recordReturn(oneDependencyConfig, oneDependencyConfig.getScopeManagedObjectName(), "MO_B");
		this.record_matchingDependencyType(oneDependencyMetaData, twoMetaData);

		// Record managed object two
		this.record_getDependencyMetaData(twoMetaData, twoDependencyMetaData);
		this.recordReturn(twoDependencyConfig, twoDependencyConfig.getDependencyKey(), DependencyKey.KEY);
		this.record_dependencyMetaDataDetails(twoDependencyMetaData, DependencyKey.KEY, null);
		this.recordReturn(twoDependencyConfig, twoDependencyConfig.getScopeManagedObjectName(), "MO_A");
		this.record_matchingDependencyType(twoDependencyMetaData, oneMetaData);

		// Record issue in cyclic dependencies
		this.issues.addIssue(this.assetType, this.assetName,
				"Cyclic dependency between bound ManagedObjects (MO_B, MO_A)");

		// Record loading meta data
		this.record_loadManagedObjectMetaData(oneMetaData, "MO_A", 0, null,
				new ManagedObjectIndexImpl(this.managedObjectScope, 1));
		this.record_loadManagedObjectMetaData(twoMetaData, "MO_B", 0, null,
				new ManagedObjectIndexImpl(this.managedObjectScope, 0));

		this.replayMockObjects();
		this.constructRawBoundManagedObjectMetaData(2, oneConfig, twoConfig);
		this.verifyMockObjects();
	}

	/**
	 * Ensure can construct {@link RawBoundManagedObjectMetaData} with
	 * dependency on a {@link RawBoundManagedObjectMetaData} from scope.
	 */
	public void testDependencyOnScopeBound() {

		final RawManagedObjectMetaData<?, ?> rawMoMetaData = this.registerRawManagedObjectMetaData("OFFICE_MO");
		ManagedObjectDependencyMetaData<?> dependencyMetaData = this.createMock(ManagedObjectDependencyMetaData.class);
		final ManagedObjectDependencyConfiguration<?> dependencyConfig = this
				.createMock(ManagedObjectDependencyConfiguration.class);

		final RawBoundManagedObjectMetaData scopeMoMetaData = this.scopeRawManagedObjectMetaData("SCOPE");
		final ManagedObjectIndex dependencyMoIndex = this.createMock(ManagedObjectIndex.class);

		// Record construction
		ManagedObjectConfiguration<?> configuration = this.record_initManagedObject("BOUND", "OFFICE_MO",
				dependencyConfig);
		this.record_getDependencyMetaData(rawMoMetaData, dependencyMetaData);
		this.recordReturn(dependencyConfig, dependencyConfig.getDependencyKey(), DependencyKey.KEY);
		this.record_dependencyMetaDataDetails(dependencyMetaData, DependencyKey.KEY, null);
		this.recordReturn(dependencyConfig, dependencyConfig.getScopeManagedObjectName(), "SCOPE");
		this.record_matchingDependencyType(dependencyMetaData, scopeMoMetaData);
		this.recordReturn(scopeMoMetaData, scopeMoMetaData.getManagedObjectIndex(), dependencyMoIndex);
		this.record_loadManagedObjectMetaData(rawMoMetaData, "BOUND", 0, null, dependencyMoIndex);

		// Construct
		this.replayMockObjects();
		RawBoundManagedObjectMetaData[] rawMetaData = this.constructRawBoundManagedObjectMetaData(1, configuration);
		this.verifyMockObjects();

		// Validate dependency mapping
		RawBoundManagedObjectMetaData dependency = rawMetaData[0];
		assertEquals("Incorrect number of dependency instances", 1,
				dependency.getRawBoundManagedObjectInstanceMetaData().length);
		RawBoundManagedObjectInstanceMetaData<?> dependencyInstance = dependency
				.getRawBoundManagedObjectInstanceMetaData()[0];
		assertEquals("Incorrect number of dependency keys", 1, dependencyInstance.getDependencies().length);
		assertEquals("Incorrect dependency", scopeMoMetaData, dependencyInstance.getDependencies()[0]);
	}

	/**
	 * Ensure not create binding if no {@link InputManagedObjectConfiguration}.
	 */
	public void testNoInputManagedObjectConfiguration() {

		final RawManagingOfficeMetaData<?> inputMo = this.createMock(RawManagingOfficeMetaData.class);

		// Record no Input ManagedObject configuration
		this.recordReturn(inputMo, inputMo.getInputManagedObjectConfiguration(), null);

		// Construct
		this.replayMockObjects();
		this.constructRawBoundManagedObjectMetaData(0, inputMo);
		this.verifyMockObjects();
	}

	/**
	 * Ensure issue if no Input {@link ManagedObject} name.
	 */
	public void testNoInputManagedObjectName() {

		final RawManagingOfficeMetaData<?> inputMo = this.createMock(RawManagingOfficeMetaData.class);
		final InputManagedObjectConfiguration<?> inputConfiguration = this
				.createMock(InputManagedObjectConfiguration.class);

		// Record no Input ManagedObject name
		this.recordReturn(inputMo, inputMo.getInputManagedObjectConfiguration(), inputConfiguration);
		this.recordReturn(inputConfiguration, inputConfiguration.getBoundManagedObjectName(), null);
		this.issues.addIssue(AssetType.OFFICE, "OFFICE", "No bound name for input managed object");

		// Construct
		this.replayMockObjects();
		this.constructRawBoundManagedObjectMetaData(0, inputMo);
		this.verifyMockObjects();
	}

	/**
	 * Ensures reports issue if class between bound and input
	 * {@link ManagedObject} name.
	 */
	public void testClashBetweenBoundAndInputManagedObject() {

		final ManagedObjectConfiguration<?> boundConfiguration = this.createMock(ManagedObjectConfiguration.class);
		final RawManagedObjectMetaData<?, ?> rawBoundMetaData = this.registerRawManagedObjectMetaData("OFFICE_MO");

		final RawManagingOfficeMetaData<?> inputMo = this.createMock(RawManagingOfficeMetaData.class);
		final InputManagedObjectConfiguration<?> inputConfiguration = this
				.createMock(InputManagedObjectConfiguration.class);
		final RawManagedObjectMetaData<?, ?> inputRawMetaData = this.createMock(RawManagedObjectMetaData.class);

		final String CLASH_NAME = "CLASH";

		// Record clash between bound and input names
		this.recordReturn(boundConfiguration, boundConfiguration.getBoundManagedObjectName(), CLASH_NAME);
		this.recordReturn(boundConfiguration, boundConfiguration.getOfficeManagedObjectName(), "OFFICE_MO");
		this.recordReturn(boundConfiguration, boundConfiguration.getDependencyConfiguration(), null);
		this.recordReturn(boundConfiguration, boundConfiguration.getGovernanceConfiguration(), null);
		this.recordReturn(inputMo, inputMo.getInputManagedObjectConfiguration(), inputConfiguration);
		this.recordReturn(inputConfiguration, inputConfiguration.getBoundManagedObjectName(), CLASH_NAME);
		this.recordReturn(inputMo, inputMo.getRawManagedObjectMetaData(), inputRawMetaData);
		this.issues.addIssue(AssetType.MANAGED_OBJECT, CLASH_NAME,
				"Name clash between bound and input Managed Objects (name=" + CLASH_NAME + ")");
		this.record_getDependencyMetaData(rawBoundMetaData);
		this.record_loadManagedObjectMetaData(rawBoundMetaData, CLASH_NAME, 0, null);

		// Construct
		this.replayMockObjects();
		RawBoundManagedObjectMetaData boundMetaData = this.constructRawBoundManagedObjectMetaData(1,
				new ManagedObjectConfiguration[] { boundConfiguration },
				new RawManagingOfficeMetaData[] { inputMo })[0];
		this.verifyMockObjects();

		// Ensure only the bound managed object constructed
		assertEquals("Should not return input managed object", 1,
				boundMetaData.getRawBoundManagedObjectInstanceMetaData().length);
		RawBoundManagedObjectInstanceMetaData<?> instanceMetaData = boundMetaData
				.getRawBoundManagedObjectInstanceMetaData()[0];
		assertEquals("Should only return bound managed object", rawBoundMetaData,
				instanceMetaData.getRawManagedObjectMetaData());
	}

	/**
	 * Ensures no issue if name clash is on same {@link ManagedObject}.
	 */
	public void testNoClashBetweenSameBoundAndInputManagedObject() {

		final ManagedObjectConfiguration<?> boundConfiguration = this.createMock(ManagedObjectConfiguration.class);
		final RawManagedObjectMetaData<?, ?> rawBoundMetaData = this.registerRawManagedObjectMetaData("OFFICE_MO");

		final RawManagingOfficeMetaData<?> inputMo = this.createMock(RawManagingOfficeMetaData.class);
		final InputManagedObjectConfiguration<?> inputConfiguration = this
				.createMock(InputManagedObjectConfiguration.class);

		final String CLASH_NAME = "CLASH";

		// Record clash between bound and input names
		this.recordReturn(boundConfiguration, boundConfiguration.getBoundManagedObjectName(), CLASH_NAME);
		this.recordReturn(boundConfiguration, boundConfiguration.getOfficeManagedObjectName(), "OFFICE_MO");
		this.recordReturn(boundConfiguration, boundConfiguration.getDependencyConfiguration(), null);
		this.recordReturn(boundConfiguration, boundConfiguration.getGovernanceConfiguration(), null);
		this.recordReturn(inputMo, inputMo.getInputManagedObjectConfiguration(), inputConfiguration);
		this.recordReturn(inputConfiguration, inputConfiguration.getBoundManagedObjectName(), CLASH_NAME);
		this.recordReturn(inputMo, inputMo.getRawManagedObjectMetaData(), rawBoundMetaData);
		this.record_getDependencyMetaData(rawBoundMetaData);
		this.record_loadManagedObjectMetaData(rawBoundMetaData, CLASH_NAME, 0, null);

		// Construct
		this.replayMockObjects();
		RawBoundManagedObjectMetaData boundMetaData = this.constructRawBoundManagedObjectMetaData(1,
				new ManagedObjectConfiguration[] { boundConfiguration },
				new RawManagingOfficeMetaData[] { inputMo })[0];
		this.verifyMockObjects();

		// Ensure only the bound managed object constructed
		assertEquals("Should not return input managed object", 1,
				boundMetaData.getRawBoundManagedObjectInstanceMetaData().length);
		RawBoundManagedObjectInstanceMetaData<?> instanceMetaData = boundMetaData
				.getRawBoundManagedObjectInstanceMetaData()[0];
		assertEquals("Should only return bound managed object", rawBoundMetaData,
				instanceMetaData.getRawManagedObjectMetaData());
	}

	/**
	 * Ensure able to bind Input {@link ManagedObject} to the same name.
	 */
	public void testBindInputManagedObject() {

		final RawManagingOfficeMetaData<?> inputMo = this.createMock(RawManagingOfficeMetaData.class);
		final InputManagedObjectConfiguration<?> inputConfiguration = this
				.createMock(InputManagedObjectConfiguration.class);
		final RawManagedObjectMetaData<?, ?> rawMetaData = this.registerRawManagedObjectMetaData("OFFICE_MO");

		// Record bind Input ManagedObject
		this.recordReturn(inputMo, inputMo.getInputManagedObjectConfiguration(), inputConfiguration);
		this.recordReturn(inputConfiguration, inputConfiguration.getBoundManagedObjectName(), "MO");
		this.recordReturn(inputMo, inputMo.getRawManagedObjectMetaData(), rawMetaData);
		this.recordReturn(inputConfiguration, inputConfiguration.getDependencyConfiguration(), null);
		this.recordReturn(inputConfiguration, inputConfiguration.getGovernanceConfiguration(), null);
		this.record_getDependencyMetaData(rawMetaData);
		ManagedObjectMetaData<?> moMetaData = this.record_loadManagedObjectMetaData(rawMetaData, "MO", 0, null);

		// Construct
		this.replayMockObjects();
		RawBoundManagedObjectMetaData metaData = this.constructRawBoundManagedObjectMetaData(1, inputMo)[0];
		this.verifyMockObjects();

		// Verify raw bound meta-data
		assertEquals("Incorrect bound managed object name", "MO", metaData.getBoundManagedObjectName());
		assertEquals("Incorrect default instance index", 0, metaData.getDefaultInstanceIndex());
		ManagedObjectIndex index = metaData.getManagedObjectIndex();
		assertEquals("Incorrect bound scope", this.managedObjectScope, index.getManagedObjectScope());
		assertEquals("Incorrect bound index", 0, index.getIndexOfManagedObjectWithinScope());
		assertEquals("Incorrect number of instances", 1, metaData.getRawBoundManagedObjectInstanceMetaData().length);
		RawBoundManagedObjectInstanceMetaData<?> instanceMetaData = metaData
				.getRawBoundManagedObjectInstanceMetaData()[0];
		assertEquals("Should be no dependencies", 0, instanceMetaData.getDependencies().length);
		assertEquals("Incorrect raw managed object meta-data", rawMetaData,
				instanceMetaData.getRawManagedObjectMetaData());
		assertEquals("Incorrect managed object meta-data", moMetaData, instanceMetaData.getManagedObjectMetaData());
	}

	/**
	 * Ensure issue if binding Input {@link ManagedObject} instances to same
	 * name without a default instance specified.
	 */
	public void testBindInputManagedObjectInstancesWithNoDefault() {

		final RawManagingOfficeMetaData<?> oneMo = this.createMock(RawManagingOfficeMetaData.class);
		final InputManagedObjectConfiguration<?> oneConfiguration = this
				.createMock(InputManagedObjectConfiguration.class);
		final RawManagedObjectMetaData<?, ?> rawOneMetaData = this.registerRawManagedObjectMetaData("ONE_MO");

		final RawManagingOfficeMetaData<?> twoMo = this.createMock(RawManagingOfficeMetaData.class);
		final InputManagedObjectConfiguration<?> twoConfiguration = this
				.createMock(InputManagedObjectConfiguration.class);
		final RawManagedObjectMetaData<?, ?> rawTwoMetaData = this.registerRawManagedObjectMetaData("ONE_MO");

		// Record bind Input ManagedObject instances
		this.recordReturn(oneMo, oneMo.getInputManagedObjectConfiguration(), oneConfiguration);
		this.recordReturn(oneConfiguration, oneConfiguration.getBoundManagedObjectName(), "MO");
		this.recordReturn(oneMo, oneMo.getRawManagedObjectMetaData(), rawOneMetaData);
		this.recordReturn(oneConfiguration, oneConfiguration.getDependencyConfiguration(), null);
		this.recordReturn(oneConfiguration, oneConfiguration.getGovernanceConfiguration(), null);
		this.recordReturn(twoMo, twoMo.getInputManagedObjectConfiguration(), twoConfiguration);
		this.recordReturn(twoConfiguration, twoConfiguration.getBoundManagedObjectName(), "MO");
		this.recordReturn(twoMo, twoMo.getRawManagedObjectMetaData(), rawTwoMetaData);
		this.recordReturn(twoConfiguration, twoConfiguration.getDependencyConfiguration(), null);
		this.recordReturn(twoConfiguration, twoConfiguration.getGovernanceConfiguration(), null);
		this.issues.addIssue(AssetType.MANAGED_OBJECT, "MO",
				"Bound Managed Object Source must be specified for Input Managed Object 'MO'");

		// Construct
		this.replayMockObjects();
		RawBoundManagedObjectMetaData metaData = this.constructRawBoundManagedObjectMetaData(1, oneMo, twoMo)[0];
		this.verifyMockObjects();

		// Verify instances
		RawBoundManagedObjectInstanceMetaData<?>[] instanceMetaData = metaData
				.getRawBoundManagedObjectInstanceMetaData();
		assertEquals("Incorrect number of instances", 2, instanceMetaData.length);
	}

	/**
	 * Ensure issue if binding Input {@link ManagedObject} instances to same
	 * name without a default instance of not bound {@link ManagedObjectSource}.
	 */
	public void testBindInputManagedObjectInstancesWithUnknownBoundManagedObjectSource() {

		// Specify second managed object source as default
		this.inputManagedObjectDefaults.put("MO", "UNKNOWN_MANAGED_OBJECT_SOURCE");

		final RawManagingOfficeMetaData<?> oneMo = this.createMock(RawManagingOfficeMetaData.class);
		final InputManagedObjectConfiguration<?> oneConfiguration = this
				.createMock(InputManagedObjectConfiguration.class);
		final RawManagedObjectMetaData<?, ?> rawOneMetaData = this.registerRawManagedObjectMetaData("ONE_MO");

		final RawManagingOfficeMetaData<?> twoMo = this.createMock(RawManagingOfficeMetaData.class);
		final InputManagedObjectConfiguration<?> twoConfiguration = this
				.createMock(InputManagedObjectConfiguration.class);
		final RawManagedObjectMetaData<?, ?> rawTwoMetaData = this.registerRawManagedObjectMetaData("ONE_MO");

		// Record bind Input ManagedObject instances
		this.recordReturn(oneMo, oneMo.getInputManagedObjectConfiguration(), oneConfiguration);
		this.recordReturn(oneConfiguration, oneConfiguration.getBoundManagedObjectName(), "MO");
		this.recordReturn(oneMo, oneMo.getRawManagedObjectMetaData(), rawOneMetaData);
		this.recordReturn(oneConfiguration, oneConfiguration.getDependencyConfiguration(), null);
		this.recordReturn(oneConfiguration, oneConfiguration.getGovernanceConfiguration(), null);
		this.recordReturn(twoMo, twoMo.getInputManagedObjectConfiguration(), twoConfiguration);
		this.recordReturn(twoConfiguration, twoConfiguration.getBoundManagedObjectName(), "MO");
		this.recordReturn(twoMo, twoMo.getRawManagedObjectMetaData(), rawTwoMetaData);
		this.recordReturn(twoConfiguration, twoConfiguration.getDependencyConfiguration(), null);
		this.recordReturn(twoConfiguration, twoConfiguration.getGovernanceConfiguration(), null);
		this.recordReturn(rawOneMetaData, rawOneMetaData.getManagedObjectName(), "ONE_MO");
		this.recordReturn(rawTwoMetaData, rawTwoMetaData.getManagedObjectName(), "TWO_MO");
		this.issues.addIssue(AssetType.MANAGED_OBJECT, "MO",
				"Managed Object Source 'UNKNOWN_MANAGED_OBJECT_SOURCE' not linked to Input Managed Object 'MO' for being the bound instance");

		// Construct
		this.replayMockObjects();
		RawBoundManagedObjectMetaData metaData = this.constructRawBoundManagedObjectMetaData(1, oneMo, twoMo)[0];
		this.verifyMockObjects();

		// Verify instances
		RawBoundManagedObjectInstanceMetaData<?>[] instanceMetaData = metaData
				.getRawBoundManagedObjectInstanceMetaData();
		assertEquals("Incorrect number of instances", 2, instanceMetaData.length);
	}

	/**
	 * Ensure able to bind Input {@link ManagedObject} instances to same name.
	 */
	public void testBindInputManagedObjectInstances() {

		// Specify second managed object source as default
		this.inputManagedObjectDefaults.put("MO", "TWO_MO");

		final RawManagingOfficeMetaData<?> oneMo = this.createMock(RawManagingOfficeMetaData.class);
		final InputManagedObjectConfiguration<?> oneConfiguration = this
				.createMock(InputManagedObjectConfiguration.class);
		final RawManagedObjectMetaData<?, ?> rawOneMetaData = this.registerRawManagedObjectMetaData("ONE_MO");

		final RawManagingOfficeMetaData<?> twoMo = this.createMock(RawManagingOfficeMetaData.class);
		final InputManagedObjectConfiguration<?> twoConfiguration = this
				.createMock(InputManagedObjectConfiguration.class);
		final RawManagedObjectMetaData<?, ?> rawTwoMetaData = this.registerRawManagedObjectMetaData("TWO_MO");

		// Record bind Input ManagedObject instances
		this.recordReturn(oneMo, oneMo.getInputManagedObjectConfiguration(), oneConfiguration);
		this.recordReturn(oneConfiguration, oneConfiguration.getBoundManagedObjectName(), "MO");
		this.recordReturn(oneMo, oneMo.getRawManagedObjectMetaData(), rawOneMetaData);
		this.recordReturn(oneConfiguration, oneConfiguration.getDependencyConfiguration(), null);
		this.recordReturn(oneConfiguration, oneConfiguration.getGovernanceConfiguration(), null);
		this.recordReturn(twoMo, twoMo.getInputManagedObjectConfiguration(), twoConfiguration);
		this.recordReturn(twoConfiguration, twoConfiguration.getBoundManagedObjectName(), "MO");
		this.recordReturn(twoMo, twoMo.getRawManagedObjectMetaData(), rawTwoMetaData);
		this.recordReturn(twoConfiguration, twoConfiguration.getDependencyConfiguration(), null);
		this.recordReturn(twoConfiguration, twoConfiguration.getGovernanceConfiguration(), null);
		this.recordReturn(rawOneMetaData, rawOneMetaData.getManagedObjectName(), "ONE_MO");
		this.recordReturn(rawTwoMetaData, rawTwoMetaData.getManagedObjectName(), "TWO_MO");
		this.record_getDependencyMetaData(rawOneMetaData);
		ManagedObjectMetaData<?> moOneMetaData = this.record_loadManagedObjectMetaData(rawOneMetaData, "MO", 0, null);
		this.record_getDependencyMetaData(rawTwoMetaData);
		ManagedObjectMetaData<?> moTwoMetaData = this.record_loadManagedObjectMetaData(rawTwoMetaData, "MO", 1, null);

		// Construct
		this.replayMockObjects();
		RawBoundManagedObjectMetaData metaData = this.constructRawBoundManagedObjectMetaData(1, oneMo, twoMo)[0];
		this.verifyMockObjects();

		// Verify instances
		assertEquals("Incorrect bound managed object name", "MO", metaData.getBoundManagedObjectName());
		assertEquals("Incorrect default instance index", 1, metaData.getDefaultInstanceIndex());
		RawBoundManagedObjectInstanceMetaData<?>[] instanceMetaData = metaData
				.getRawBoundManagedObjectInstanceMetaData();
		assertEquals("Incorrect number of instances", 2, instanceMetaData.length);

		// Verify first instance
		RawBoundManagedObjectInstanceMetaData<?> oneInstance = instanceMetaData[0];
		assertEquals("Incorrect instance one raw meta-data", rawOneMetaData, oneInstance.getRawManagedObjectMetaData());
		assertEquals("Incorrect instance one meta-data", moOneMetaData, oneInstance.getManagedObjectMetaData());
		assertEquals("Should be no dependencies for instance one", 0, oneInstance.getDependencies().length);

		// Verify second instance
		RawBoundManagedObjectInstanceMetaData<?> twoInstance = instanceMetaData[1];
		assertEquals("Incorrect instance two raw meta-data", rawTwoMetaData, twoInstance.getRawManagedObjectMetaData());
		assertEquals("Incorrect instance two meta-data", moTwoMetaData, twoInstance.getManagedObjectMetaData());
		assertEquals("Should be no dependencies for instance two", 0, twoInstance.getDependencies().length);
	}

	/**
	 * Ensure bound {@link ManagedObject} can be dependent on an Input
	 * {@link ManagedObject}.
	 */
	public void testBoundDependentOnInput() {

		final RawManagedObjectMetaData<?, ?> boundMetaData = this.registerRawManagedObjectMetaData("BOUND_MO");

		final RawManagingOfficeMetaData<?> inputMetaData = this.createMock(RawManagingOfficeMetaData.class);
		final RawManagedObjectMetaData<?, ?> rawInputMetaData = this.registerRawManagedObjectMetaData("INPUT_MO");

		final ManagedObjectDependencyMetaData<?> boundDependencyMetaData = this
				.createMock(ManagedObjectDependencyMetaData.class);
		final ManagedObjectDependencyConfiguration<?> boundDependencyConfig = this
				.createMock(ManagedObjectDependencyConfiguration.class);

		// Record construction
		final ManagedObjectConfiguration<?> boundConfig = this.record_initManagedObject("BOUND", "BOUND_MO",
				boundDependencyConfig);
		this.record_initInputManagedObject("INPUT", inputMetaData, rawInputMetaData);
		this.record_getDependencyMetaData(boundMetaData, boundDependencyMetaData);
		this.recordReturn(boundDependencyConfig, boundDependencyConfig.getDependencyKey(), null);
		this.record_dependencyMetaDataDetails(boundDependencyMetaData, (Indexed) null, "DEPENDENCY");
		this.recordReturn(boundDependencyConfig, boundDependencyConfig.getScopeManagedObjectName(), "INPUT");
		this.record_matchingDependencyType(boundDependencyMetaData, rawInputMetaData);
		this.record_loadManagedObjectMetaData(boundMetaData, "BOUND", 0, null,
				new ManagedObjectIndexImpl(this.managedObjectScope, 1));
		this.record_getDependencyMetaData(rawInputMetaData);
		this.record_loadManagedObjectMetaData(rawInputMetaData, "INPUT", 0, null);

		// Construct
		this.replayMockObjects();
		RawBoundManagedObjectMetaData[] rawMetaData = this.constructRawBoundManagedObjectMetaData(2,
				new ManagedObjectConfiguration[] { boundConfig }, new RawManagingOfficeMetaData[] { inputMetaData });
		this.verifyMockObjects();

		// Validate dependencies
		RawBoundManagedObjectMetaData bound = rawMetaData[0];
		assertEquals("Incorrect number of bound instances", 1, bound.getRawBoundManagedObjectInstanceMetaData().length);
		RawBoundManagedObjectInstanceMetaData<?> boundInstance = bound.getRawBoundManagedObjectInstanceMetaData()[0];
		assertEquals("Incorrect number of dependencies", 1, boundInstance.getDependencies().length);
		RawBoundManagedObjectMetaData input = rawMetaData[1];
		assertEquals("Incorrect number of input instances", 1, input.getRawBoundManagedObjectInstanceMetaData().length);
		RawBoundManagedObjectInstanceMetaData<?> inputInstance = input.getRawBoundManagedObjectInstanceMetaData()[0];
		assertEquals("Incorrect number of bound dependencies", 1, boundInstance.getDependencies().length);
		assertEquals("Incorrect bound dependencies", input, boundInstance.getDependencies()[0]);
		assertEquals("Should be no dependencies for second", 0, inputInstance.getDependencies().length);
	}

	/**
	 * Ensure Input {@link ManagedObject} can be dependent on a bound
	 * {@link ManagedObject}.
	 */
	public void testInputDependentOnBound() {

		final RawManagedObjectMetaData<?, ?> boundMetaData = this.registerRawManagedObjectMetaData("BOUND_MO");

		final RawManagingOfficeMetaData<?> inputMetaData = this.createMock(RawManagingOfficeMetaData.class);
		final RawManagedObjectMetaData<?, ?> rawInputMetaData = this.registerRawManagedObjectMetaData("INPUT_MO");

		final ManagedObjectDependencyMetaData<?> inputDependencyMetaData = this
				.createMock(ManagedObjectDependencyMetaData.class);
		final ManagedObjectDependencyConfiguration<?> inputDependencyConfig = this
				.createMock(ManagedObjectDependencyConfiguration.class);

		// Record construction
		final ManagedObjectConfiguration<?> boundConfig = this.record_initManagedObject("BOUND", "BOUND_MO");
		this.record_initInputManagedObject("INPUT", inputMetaData, rawInputMetaData, inputDependencyConfig);

		// Record dependencies
		this.record_getDependencyMetaData(boundMetaData);
		this.record_getDependencyMetaData(rawInputMetaData, inputDependencyMetaData);
		this.recordReturn(inputDependencyConfig, inputDependencyConfig.getDependencyKey(), DependencyKey.KEY);
		this.record_dependencyMetaDataDetails(inputDependencyMetaData, DependencyKey.KEY, null);
		this.recordReturn(inputDependencyConfig, inputDependencyConfig.getScopeManagedObjectName(), "BOUND");
		this.record_matchingDependencyType(inputDependencyMetaData, boundMetaData);

		// Record loading meta data
		this.record_loadManagedObjectMetaData(boundMetaData, "BOUND", 0, null);
		this.record_loadManagedObjectMetaData(rawInputMetaData, "INPUT", 0, null,
				new ManagedObjectIndexImpl(this.managedObjectScope, 1));

		// Construct
		this.replayMockObjects();
		RawBoundManagedObjectMetaData[] rawMetaData = this.constructRawBoundManagedObjectMetaData(2,
				new ManagedObjectConfiguration[] { boundConfig }, new RawManagingOfficeMetaData[] { inputMetaData });
		this.verifyMockObjects();

		// Validate dependencies
		RawBoundManagedObjectMetaData input = rawMetaData[0];
		assertEquals("Incorrect number of input instances", 1, input.getRawBoundManagedObjectInstanceMetaData().length);
		RawBoundManagedObjectInstanceMetaData<?> inputInstance = input.getRawBoundManagedObjectInstanceMetaData()[0];
		assertEquals("Incorrect number of input dependencies", 1, inputInstance.getDependencies().length);

		RawBoundManagedObjectMetaData bound = rawMetaData[1];
		assertEquals("Incorrect number of bound instances", 1, bound.getRawBoundManagedObjectInstanceMetaData().length);
		assertEquals("Incorrect input dependencies", bound, inputInstance.getDependencies()[0]);
		RawBoundManagedObjectInstanceMetaData<?> boundInstance = bound.getRawBoundManagedObjectInstanceMetaData()[0];
		assertEquals("Should be no dependencies for bound", 0, boundInstance.getDependencies().length);
	}

	/**
	 * Ensure can configure {@link Governance} for the {@link ManagedObject}.
	 */
	public void testGovernManagedObject() {

		final ManagedObjectGovernanceConfiguration governanceConfiguration = this
				.createMock(ManagedObjectGovernanceConfiguration.class);
		final ManagedObjectGovernanceStruct[] governances = new ManagedObjectGovernanceStruct[] {
				new ManagedObjectGovernanceStruct(1) };
		final ManagedObject managedObject = this.createMock(ManagedObject.class);
		final Object extensionInterface = "EXTENSION INTERFACE";

		// Managed object configuration
		RawManagedObjectMetaData<?, ?> rawMoMetaData = this.registerRawManagedObjectMetaData("OFFICE_MO");

		// Record construction
		ManagedObjectConfiguration<?> configuration = this.record_initManagedObject("BOUND", "OFFICE_MO",
				governanceConfiguration);
		this.record_getDependencyMetaData(rawMoMetaData);
		ExtensionInterfaceFactory<?>[] eiFactories = this.record_governManagedObject(String.class, rawMoMetaData,
				configuration, new ManagedObjectGovernanceConfiguration[] { governanceConfiguration }, String.class);
		ManagedObjectMetaData<?> moMetaData = this.record_loadManagedObjectMetaData(rawMoMetaData, "BOUND", 0,
				governances);

		// Record using extension interface factory
		ExtensionInterfaceFactory<?> eiFactory = eiFactories[0];
		this.recordReturn(eiFactory, eiFactory.createExtensionInterface(managedObject), extensionInterface);

		// Construct and obtain the managed object meta-data
		this.replayMockObjects();

		// Construction of ManagedObjectMetaData tests governance
		this.constructRawBoundManagedObjectMetaData(1, configuration);

		// Ensure correct extension factory
		Object ei = governances[0].extensionInterfaceExtractor.extractExtensionInterface(managedObject, moMetaData);
		assertEquals("Incorrect extension interface", extensionInterface, ei);

		this.verifyMockObjects();
	}

	/**
	 * Ensure issue if unknown {@link Governance}.
	 */
	public void testUnknownGovernance() {

		final ManagedObjectGovernanceConfiguration governanceConfiguration = this
				.createMock(ManagedObjectGovernanceConfiguration.class);

		// Managed object configuration
		RawManagedObjectMetaData<?, ?> rawMoMetaData = this.registerRawManagedObjectMetaData("OFFICE_MO");

		// Record unknown Governance
		ManagedObjectConfiguration<?> configuration = this.record_initManagedObject("BOUND", "OFFICE_MO",
				governanceConfiguration);
		this.record_getDependencyMetaData(rawMoMetaData);
		this.recordReturn(governanceConfiguration, governanceConfiguration.getGovernanceName(), "GOVERNANCE");
		this.issues.addIssue(AssetType.MANAGED_OBJECT, "BOUND", "Unknown governance 'GOVERNANCE'");

		// Record remaining aspect of loading Managed Object
		this.record_loadManagedObjectMetaData(rawMoMetaData, "BOUND", 0, new ManagedObjectGovernanceStruct[] { null });

		// Construct and obtain the managed object meta-data
		this.replayMockObjects();

		// Construction of ManagedObjectMetaData tests governance
		this.constructRawBoundManagedObjectMetaData(1, configuration);

		this.verifyMockObjects();
	}

	/**
	 * Ensure issue if extension interface for {@link Governance} is not
	 * available for the {@link ManagedObject}.
	 */
	public void testUnsupportedGovernance() {

		final ManagedObjectGovernanceStruct[] governances = new ManagedObjectGovernanceStruct[] { null };
		final ManagedObjectGovernanceConfiguration governanceConfiguration = this
				.createMock(ManagedObjectGovernanceConfiguration.class);
		final ManagedObjectGovernanceConfiguration[] governanceConfigurations = new ManagedObjectGovernanceConfiguration[] {
				governanceConfiguration };

		// Managed object configuration
		RawManagedObjectMetaData<?, ?> rawMoMetaData = this.registerRawManagedObjectMetaData("OFFICE_MO");

		// Record unsupported Governance by Managed Object
		ManagedObjectConfiguration<?> configuration = this.record_initManagedObject("BOUND", "OFFICE_MO",
				governanceConfiguration);
		this.record_getDependencyMetaData(rawMoMetaData);
		this.record_governManagedObject(String.class, rawMoMetaData, configuration, governanceConfigurations,
				Integer.class);
		this.issues.addIssue(AssetType.MANAGED_OBJECT, "BOUND", "Extension interface of type " + String.class.getName()
				+ " is not available from Managed Object for Governance 'GOVERNANCE'");

		// Record remaining aspect of loading Managed Object
		this.record_loadManagedObjectMetaData(rawMoMetaData, "BOUND", 0, governances);

		// Construct and obtain the managed object meta-data
		this.replayMockObjects();

		// Construction of ManagedObjectMetaData tests governance
		this.constructRawBoundManagedObjectMetaData(1, configuration);

		this.verifyMockObjects();
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
	private RawBoundManagedObjectMetaData scopeRawManagedObjectMetaData(String scopeManagedObjectName) {
		RawBoundManagedObjectMetaData metaData = this.createMock(RawBoundManagedObjectMetaData.class);
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
	private RawManagedObjectMetaData<?, ?> registerRawManagedObjectMetaData(String registeredManagedObjectName) {
		RawManagedObjectMetaData<?, ?> metaData = this.createMock(RawManagedObjectMetaData.class);
		this.registeredManagedObjects.put(registeredManagedObjectName, metaData);
		return metaData;
	}

	/**
	 * Records initialising the {@link RawBoundManagedObjectMetaData} for the
	 * bound {@link ManagedObject}.
	 * 
	 * @param boundName
	 *            Bound name.
	 * @param officeName
	 *            {@link Office} name.
	 * @param configurations
	 *            {@link ManagedObjectDependencyConfiguration} and
	 *            {@link ManagedObjectGovernanceConfiguration} for the bound
	 *            {@link ManagedObject}.
	 * @return {@link ManagedObjectConfiguration}.
	 */
	private ManagedObjectConfiguration<?> record_initManagedObject(String boundName, String officeName,
			Object... configurations) {

		// Create the listing of configurations
		List<ManagedObjectDependencyConfiguration<?>> dependencyConfigurations = new LinkedList<ManagedObjectDependencyConfiguration<?>>();
		List<ManagedObjectGovernanceConfiguration> governanceConfigurations = new LinkedList<ManagedObjectGovernanceConfiguration>();
		for (Object configuration : configurations) {
			if (configuration instanceof ManagedObjectDependencyConfiguration) {
				dependencyConfigurations.add((ManagedObjectDependencyConfiguration<?>) configuration);
			} else if (configuration instanceof ManagedObjectGovernanceConfiguration) {
				governanceConfigurations.add((ManagedObjectGovernanceConfiguration) configuration);
			} else {
				fail("Unknown configuration type " + configuration.getClass().getName());
			}
		}

		// Create the mock objects
		final ManagedObjectConfiguration<?> configuration = this.createMock(ManagedObjectConfiguration.class);

		// Record initiating the managed object meta-data
		this.recordReturn(configuration, configuration.getBoundManagedObjectName(), boundName);
		this.recordReturn(configuration, configuration.getOfficeManagedObjectName(), officeName);
		this.recordReturn(configuration, configuration.getDependencyConfiguration(), dependencyConfigurations
				.toArray(new ManagedObjectDependencyConfiguration<?>[dependencyConfigurations.size()]));
		this.recordReturn(configuration, configuration.getGovernanceConfiguration(), governanceConfigurations
				.toArray(new ManagedObjectGovernanceConfiguration[governanceConfigurations.size()]));

		// Return the configuration
		return configuration;
	}

	/**
	 * Records initialising the {@link RawBoundManagedObjectMetaData} for the
	 * Input {@link ManagedObject}.
	 * 
	 * @param inputName
	 *            Input name.
	 * @param inputMo
	 *            {@link RawManagingOfficeMetaData} of the Input
	 *            {@link ManagedObject}.
	 * @param rawInputMetaData
	 *            {@link RawManagedObjectMetaData} for the Input
	 *            {@link ManagedObject}.
	 * @param dependencyConfiguration
	 *            {@link ManagedObjectDependencyConfiguration} instances of the
	 *            Input {@link ManagedObject}.
	 * @return {@link InputManagedObjectConfiguration}.
	 */
	private InputManagedObjectConfiguration<?> record_initInputManagedObject(String inputName,
			RawManagingOfficeMetaData<?> inputMo, RawManagedObjectMetaData<?, ?> rawInputMetaData,
			ManagedObjectDependencyConfiguration<?>... dependencyConfiguration) {

		// Create the mock objects
		final InputManagedObjectConfiguration<?> configuration = this.createMock(InputManagedObjectConfiguration.class);

		// Record initiating the input managed object meta-data
		this.recordReturn(inputMo, inputMo.getInputManagedObjectConfiguration(), configuration);
		this.recordReturn(configuration, configuration.getBoundManagedObjectName(), inputName);
		this.recordReturn(inputMo, inputMo.getRawManagedObjectMetaData(), rawInputMetaData);
		this.recordReturn(configuration, configuration.getDependencyConfiguration(), dependencyConfiguration);
		this.recordReturn(configuration, configuration.getGovernanceConfiguration(), null);

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
	private void record_getDependencyMetaData(RawManagedObjectMetaData<?, ?> rawMoMetaData,
			ManagedObjectDependencyMetaData<?>... dependencyMetaData) {

		// Create the mocks
		final ManagedObjectSourceMetaData<?, ?> mosMetaData = this.createMock(ManagedObjectSourceMetaData.class);

		// Record obtaining the dependency meta-data
		this.recordReturn(rawMoMetaData, rawMoMetaData.getManagedObjectSourceMetaData(), mosMetaData);
		this.recordReturn(mosMetaData, mosMetaData.getDependencyMetaData(), dependencyMetaData);
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
			ManagedObjectDependencyMetaData<?> dependencyMetaData, D key, String label) {
		// Record obtaining the dependency meta-data key and label
		this.recordReturn(dependencyMetaData, dependencyMetaData.getKey(), key);
		this.recordReturn(dependencyMetaData, dependencyMetaData.getLabel(), label);
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
	private void record_matchingDependencyType(ManagedObjectDependencyMetaData<?> dependencyMetaData,
			RawManagedObjectMetaData<?, ?> dependency) {

		// Records matching type for the dependency
		this.recordReturn(dependencyMetaData, dependencyMetaData.getType(), Connection.class);
		this.recordReturn(dependency, dependency.getObjectType(), Connection.class);
	}

	/**
	 * Records matching type for a dependency on another scope.
	 * 
	 * @param dependencyMetaData
	 *            {@link ManagedObjectDependencyMetaData}.
	 * @param dependency
	 *            {@link RawBoundManagedObjectMetaData} of another scope.
	 */
	private void record_matchingDependencyType(ManagedObjectDependencyMetaData<?> dependencyMetaData,
			RawBoundManagedObjectMetaData dependency) {

		RawBoundManagedObjectInstanceMetaData<?> dependencyInstance = this
				.createMock(RawBoundManagedObjectInstanceMetaData.class);
		RawManagedObjectMetaData<?, ?> rawDependency = this.createMock(RawManagedObjectMetaData.class);

		// Records matching type for the dependency (for single instance)
		this.recordReturn(dependencyMetaData, dependencyMetaData.getType(), Connection.class);
		this.recordReturn(dependency, dependency.getRawBoundManagedObjectInstanceMetaData(),
				new RawBoundManagedObjectInstanceMetaData[] { dependencyInstance });
		this.recordReturn(dependencyInstance, dependencyInstance.getRawManagedObjectMetaData(), rawDependency);
		this.recordReturn(rawDependency, rawDependency.getObjectType(), Connection.class);
	}

	/**
	 * Records mapping the {@link Governance} for the {@link ManagedObject}.
	 */
	private ExtensionInterfaceFactory<?>[] record_governManagedObject(Class<?> governanceExtensionInterfaceType,
			RawManagedObjectMetaData<?, ?> rawMoMetaData, ManagedObjectConfiguration<?> configuration,
			ManagedObjectGovernanceConfiguration[] governanceConfigurations, Class<?>... extensionInterfaceTypes) {

		// Governance meta-data
		final RawGovernanceMetaData<?, ?> rawGovernanceMetaData = this.createMock(RawGovernanceMetaData.class);
		final String GOVERNANCE_NAME = "GOVERNANCE";
		this.rawGovernanceMetaDatas.put(GOVERNANCE_NAME, rawGovernanceMetaData);

		// Extension interface meta-data
		ManagedObjectExtensionInterfaceMetaData<?>[] eiMetaDatas = new ManagedObjectExtensionInterfaceMetaData<?>[extensionInterfaceTypes.length];
		for (int i = 0; i < eiMetaDatas.length; i++) {
			eiMetaDatas[i] = this.createMock(ManagedObjectExtensionInterfaceMetaData.class);
		}

		// Ensure have Managed Object Governance configuration
		governanceConfigurations = (governanceConfigurations == null ? new ManagedObjectGovernanceConfiguration[0]
				: governanceConfigurations);

		// Configure the governance
		ExtensionInterfaceFactory<?>[] eiFactories = new ExtensionInterfaceFactory<?>[governanceConfigurations.length];
		for (int i = 0; i < governanceConfigurations.length; i++) {
			ManagedObjectGovernanceConfiguration governanceConfiguration = governanceConfigurations[i];

			// Obtain the governance name
			this.recordReturn(governanceConfiguration, governanceConfiguration.getGovernanceName(), GOVERNANCE_NAME);

			// Obtain the Governance Index
			final int GOVERNANCE_INDEX = 1;
			this.recordReturn(rawGovernanceMetaData, rawGovernanceMetaData.getGovernanceIndex(), GOVERNANCE_INDEX);

			// Obtain the governance extension interface
			this.recordReturn(rawGovernanceMetaData, rawGovernanceMetaData.getExtensionInterfaceType(),
					governanceExtensionInterfaceType);

			// Mocks for extension interface extractor
			final ManagedObjectSourceMetaData<?, ?> moSourceMetaData = this
					.createMock(ManagedObjectSourceMetaData.class);

			// Obtain the extension interface extractor
			this.recordReturn(rawMoMetaData, rawMoMetaData.getManagedObjectSourceMetaData(), moSourceMetaData);
			this.recordReturn(moSourceMetaData, moSourceMetaData.getExtensionInterfacesMetaData(), eiMetaDatas);

			// Iterate over extensions to find match
			NEXT_GOVERNANCE: for (int e = 0; e < eiMetaDatas.length; e++) {
				ManagedObjectExtensionInterfaceMetaData<?> eiMetaData = eiMetaDatas[e];
				Class<?> extensionInterfaceType = extensionInterfaceTypes[e];
				this.recordReturn(eiMetaData, eiMetaData.getExtensionInterfaceType(), extensionInterfaceType);

				// Determine if a match
				if (governanceExtensionInterfaceType.isAssignableFrom(extensionInterfaceType)) {
					// Match, so load governance
					final ExtensionInterfaceFactory<?> eiFactory = this.createMock(ExtensionInterfaceFactory.class);
					this.recordReturn(eiMetaData, eiMetaData.getExtensionInterfaceFactory(), eiFactory);

					// Set factory for return
					eiFactories[i] = eiFactory;

					// Mapped in governance
					continue NEXT_GOVERNANCE;
				}
			}
		}

		// Return the Extension Interface Factories
		return eiFactories;
	}

	/**
	 * Records loading the {@link ManagedObjectMetaData} from the
	 * {@link RawManagedObjectMetaData}.
	 * 
	 * @param rawMoMetaData
	 *            {@link RawManagedObjectMetaData}.
	 * @param instanceIndex
	 *            Instance index of the
	 *            {@link RawBoundManagedObjectInstanceMetaData}.
	 * @param governanceMetaData
	 *            {@link ManagedObjectGovernanceMetaData}.
	 * @param dependencies
	 *            Dependency {@link ManagedObjectIndex}.
	 * @param {@link
	 * 			ManagedObjectMetaData} created.
	 */
	private ManagedObjectMetaData<?> record_loadManagedObjectMetaData(RawManagedObjectMetaData<?, ?> rawMoMetaData,
			final String boundName, final int instanceIndex, final ManagedObjectGovernanceStruct[] governanceMetaData,
			final ManagedObjectIndex... dependencies) {

		final AssetType assetType = AssetType.FUNCTION;
		final String assetName = "testFunction";

		// Create the necessary mock objects
		ManagedObjectMetaData<?> moMetaData = this.createMock(ManagedObjectMetaData.class);

		// Record creating the managed object meta-data
		this.recordReturn(rawMoMetaData,
				rawMoMetaData.createManagedObjectMetaData(assetType, assetName, null, instanceIndex, null, dependencies,
						new ManagedObjectGovernanceMetaData<?>[0], this.assetManagerFactory, this.issues),
				moMetaData, new AbstractMatcher() {
					@Override
					public boolean matches(Object[] expected, Object[] actual) {
						
						// Ensure asset type and name are correct
						assertEquals("Incorrect asset type", assetType, actual[0]);
						assertEquals("Incorrect asset name", assetName, actual[1]);
						
						// Verify the raw bound managed object
						RawBoundManagedObjectMetaData boundMo = (RawBoundManagedObjectMetaData) actual[2];
						assertEquals("Incorrect bound managed object", boundName, boundMo.getBoundManagedObjectName());

						// Verify the instance index
						Integer actualInstanceIndex = (Integer) actual[3];
						assertEquals("Incorrect instance index", instanceIndex, actualInstanceIndex.intValue());

						// Verify the raw bound managed object instance
						RawBoundManagedObjectInstanceMetaData<?> boundMoInstance = (RawBoundManagedObjectInstanceMetaData<?>) actual[4];
						assertTrue("Should have bound managed object instance",
								(boundMoInstance instanceof RawBoundManagedObjectInstanceMetaData<?>));

						// Verify the dependencies
						ManagedObjectIndex[] indexes = (ManagedObjectIndex[]) actual[5];
						assertEquals("Incorrect number of dependencies", dependencies.length,
								(indexes == null ? 0 : indexes.length));
						for (int i = 0; i < dependencies.length; i++) {
							ManagedObjectIndex dependency = dependencies[i];
							ManagedObjectIndex index = indexes[i];
							if (dependency == index) {
								return true; // same as from another scope
							}
							// Verify created index of same scope is correct
							assertEquals("Incorrect dependency scope for " + i, dependency.getManagedObjectScope(),
									index.getManagedObjectScope());
							assertEquals("Incorrect dependency index for " + i,
									dependency.getIndexOfManagedObjectWithinScope(),
									index.getIndexOfManagedObjectWithinScope());
						}

						// Verify the governance
						ManagedObjectGovernanceMetaData<?>[] moGovernance = (ManagedObjectGovernanceMetaData[]) actual[6];
						if (governanceMetaData == null) {
							assertEquals("Should not have governance meta data", 0, moGovernance.length);
						} else {
							// Ensure correct governance meta-data
							assertEquals("Incorrect number of governance meta-data instances",
									governanceMetaData.length, moGovernance.length);
							for (int i = 0; i < governanceMetaData.length; i++) {
								ManagedObjectGovernanceStruct expectedGovernance = governanceMetaData[i];
								ManagedObjectGovernanceMetaData<?> actualGovernance = moGovernance[i];
								if (expectedGovernance == null) {
									// Expecting issue with governance
									assertNull("Should not have Governance for Managed Object Governance " + i,
											actualGovernance);

								} else {
									// Expecting governance
									assertEquals("Incorrect Governance index for Managed Object Governance " + i,
											expectedGovernance.governanceIndex, actualGovernance.getGovernanceIndex());

									// Load extractor for testing correct
									// factory
									expectedGovernance.extensionInterfaceExtractor = actualGovernance
											.getExtensionInterfaceExtractor();
								}
							}
						}

						// Ensure other items are correct
						AssetManagerFactory actualFactory = (AssetManagerFactory) actual[7];
						assertEquals("Incorrect asset manager factory",
								RawBoundManagedObjectMetaDataTest.this.assetManagerFactory, actualFactory);
						OfficeFloorIssues actualIssues = (OfficeFloorIssues) actual[8];
						assertEquals("Incorrect issues", RawBoundManagedObjectMetaDataTest.this.issues, actualIssues);

						// Matches if at this point
						return true;
					}
				});

		// Return the managed object meta-data
		return moMetaData;
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
	private RawBoundManagedObjectMetaData[] constructRawBoundManagedObjectMetaData(int expectedNumberConstructed,
			ManagedObjectConfiguration<?>... boundManagedObjectConfiguration) {
		return this.constructRawBoundManagedObjectMetaData(expectedNumberConstructed, boundManagedObjectConfiguration,
				null);
	}

	/**
	 * Constructs the {@link RawBoundManagedObjectMetaData} instances.
	 * 
	 * @param expectedNumberConstructed
	 *            Expected number of {@link RawBoundManagedObjectMetaData}
	 *            instances to be constructed.
	 * @param inputManagedObjects
	 *            {@link RawManagingOfficeMetaData} instances.
	 * @return Constructed {@link RawBoundManagedObjectMetaData}.
	 */
	private RawBoundManagedObjectMetaData[] constructRawBoundManagedObjectMetaData(int expectedNumberConstructed,
			RawManagingOfficeMetaData<?>... inputManagedObjects) {
		return this.constructRawBoundManagedObjectMetaData(expectedNumberConstructed, null, inputManagedObjects);
	}

	/**
	 * Constructs the {@link RawBoundManagedObjectMetaData} instances.
	 * 
	 * @param expectedNumberConstructed
	 *            Expected number of {@link RawBoundManagedObjectMetaData}
	 *            instances to be constructed.
	 * @param boundManagedObjectConfiguration
	 *            {@link ManagedObjectConfiguration} instances.
	 * @param inputManagedObjects
	 *            {@link RawManagingOfficeMetaData} instances.
	 * @return Constructed {@link RawBoundManagedObjectMetaData}.
	 */
	private RawBoundManagedObjectMetaData[] constructRawBoundManagedObjectMetaData(int expectedNumberConstructed,
			ManagedObjectConfiguration<?>[] boundManagedObjectConfiguration,
			RawManagingOfficeMetaData<?>[] inputManagedObjects) {

		// Obtain input managed object defaults
		Map<String, String> inputDefaults = (this.inputManagedObjectDefaults.size() == 0 ? null
				: this.inputManagedObjectDefaults);

		// Attempt to construct
		RawBoundManagedObjectMetaData[] metaData = RawBoundManagedObjectMetaDataImpl.getFactory()
				.constructBoundManagedObjectMetaData(boundManagedObjectConfiguration, this.issues,
						this.managedObjectScope, this.assetType, this.assetName, this.assetManagerFactory,
						this.registeredManagedObjects, this.scopeManagedObjects, inputManagedObjects, inputDefaults,
						this.rawGovernanceMetaDatas);

		// Ensure correct number constructed
		assertEquals("Incorrect number of bound managed objects", expectedNumberConstructed, metaData.length);

		// Return the meta-data
		return metaData;
	}

	/**
	 * Struct to contain details for testing the
	 * {@link ManagedObjectGovernanceMetaData}.
	 */
	private static class ManagedObjectGovernanceStruct {

		/**
		 * Expected {@link Governance} index.
		 */
		public final int governanceIndex;

		/**
		 * Actual {@link ExtensionInterfaceExtractor}.
		 */
		public ExtensionInterfaceExtractor<?> extensionInterfaceExtractor = null;

		/**
		 * Initiate.
		 * 
		 * @param governanceIndex
		 *            Expected {@link Governance} index.
		 */
		public ManagedObjectGovernanceStruct(int governanceIndex) {
			this.governanceIndex = governanceIndex;
		}
	}

}