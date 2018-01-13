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

import org.easymock.AbstractMatcher;

import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.build.OfficeFloorIssues;
import net.officefloor.frame.api.build.OfficeFloorIssues.AssetType;
import net.officefloor.frame.api.governance.Governance;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.extension.ExtensionFactory;
import net.officefloor.frame.api.managedobject.source.ManagedObjectDependencyMetaData;
import net.officefloor.frame.api.managedobject.source.ManagedObjectExtensionMetaData;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSourceMetaData;
import net.officefloor.frame.impl.construct.MockConstruct;
import net.officefloor.frame.impl.construct.MockConstruct.MockRawBoundManagedObjectMetaDataBuilder;
import net.officefloor.frame.impl.construct.MockConstruct.MockRawManagedObjectMetaDataBuilder;
import net.officefloor.frame.impl.construct.MockConstruct.MockRawManagingOfficeMetaDataBuilder;
import net.officefloor.frame.impl.construct.asset.AssetManagerFactory;
import net.officefloor.frame.impl.construct.governance.RawGovernanceMetaData;
import net.officefloor.frame.impl.construct.managedobjectsource.RawManagedObjectMetaData;
import net.officefloor.frame.impl.construct.managedobjectsource.RawManagingOfficeMetaData;
import net.officefloor.frame.internal.configuration.InputManagedObjectConfiguration;
import net.officefloor.frame.internal.configuration.ManagedObjectConfiguration;
import net.officefloor.frame.internal.configuration.ManagedObjectDependencyConfiguration;
import net.officefloor.frame.internal.configuration.ManagedObjectGovernanceConfiguration;
import net.officefloor.frame.internal.structure.ManagedObjectExtensionExtractor;
import net.officefloor.frame.internal.structure.ManagedObjectGovernanceMetaData;
import net.officefloor.frame.internal.structure.ManagedObjectIndex;
import net.officefloor.frame.internal.structure.ManagedObjectMetaData;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.test.OfficeFrameTestCase;

/**
 * Test the {@link RawBoundManagedObjectMetaData}.
 * 
 * @author Daniel Sagenschneider
 */
public class RawBoundManagedObjectMetaDataTest extends OfficeFrameTestCase {

	/**
	 * Dependency key {@link Enum}.
	 */
	private enum DependencyKey {
		KEY
	}

	/**
	 * {@link ManagedObjectScope}.
	 */
	private ManagedObjectScope managedObjectScope = ManagedObjectScope.THREAD;

	/**
	 * Name of the managing {@link Office}.
	 */
	private static final String OFFICE_NAME = "OFFICE";

	/**
	 * {@link AssetManagerFactory}.
	 */
	private AssetManagerFactory assetManagerFactory = MockConstruct.mockAssetManagerFactory();

	/**
	 * Registered {@link RawManagedObjectMetaData} instances by their name.
	 */
	private final Map<String, MockRawManagedObjectMetaDataBuilder<?, ?>> registeredManagedObjects = new HashMap<>();

	/**
	 * Scope bound {@link RawBoundManagedObjectMetaData} instances by their
	 * scope name.
	 */
	private final Map<String, MockRawBoundManagedObjectMetaDataBuilder<?, ?>> scopeManagedObjects = new HashMap<>();

	/**
	 * Input {@link ManagedObject} defaults.
	 */
	private final Map<String, String> inputManagedObjectDefaults = new HashMap<String, String>();

	/**
	 * {@link RawGovernanceMetaData}.
	 */
	private final Map<String, RawGovernanceMetaData<?, ?>> rawGovernanceMetaDatas = new HashMap<String, RawGovernanceMetaData<?, ?>>();

	/**
	 * {@link OfficeFloorIssues}.
	 */
	private final OfficeFloorIssues issues = this.createMock(OfficeFloorIssues.class);

	/**
	 * Ensure issue if no bound name.
	 */
	public void testNoBoundName() {

		// Record
		this.record_office_issue("No bound name for managed object");

		// Attempt to construct
		this.replayMockObjects();
		this.constructRawBoundManagedObjectMetaData(0, new DependencyMappingBuilderImpl<>(null, null));
		this.verifyMockObjects();
	}

	/**
	 * Ensure issue if no office name.
	 */
	public void testNoOfficeName() {

		// Record
		this.record_office_issue("No office name for bound managed object of name 'BOUND'");

		// Attempt to construct
		this.replayMockObjects();
		this.constructRawBoundManagedObjectMetaData(0, new DependencyMappingBuilderImpl<>("BOUND", null));
		this.verifyMockObjects();
	}

	/**
	 * Ensure issue if no corresponding {@link RawManagedObjectMetaData}.
	 */
	public void testNoManagedObjectMetaData() {

		// Record
		this.record_office_issue("No managed object by name 'BOUND_MO' registered with the Office");

		// Attempt to construct
		this.replayMockObjects();
		this.constructRawBoundManagedObjectMetaData(0, new DependencyMappingBuilderImpl<>("BOUND", "BOUND_MO"));
		this.verifyMockObjects();
	}

	/**
	 * Ensure able to provide no scope {@link ManagedObject} instances.
	 */
	public void testNullScopeManagedObjects() {

		// Record
		ManagedObjectConfiguration<?> configuration = new DependencyMappingBuilderImpl<>("BOUND", "BOUND_MO");
		this.registerRawManagedObjectMetaData("BOUND_MO");

		// Construct bound meta-data without scope managed objects
		this.replayMockObjects();
		RawBoundManagedObjectMetaData[] rawBoundMoMetaData = this.constructRawBoundManagedObjectMetaData(1,
				configuration);
		ManagedObjectMetaData<?> boundMoMetaData = rawBoundMoMetaData[0].getRawBoundManagedObjectInstanceMetaData()[0]
				.getManagedObjectMetaData();
		this.verifyMockObjects();

		// Ensure have meta-data
		assertEquals("Incorrect meta-data", "BOUND", boundMoMetaData.getBoundManagedObjectName());
	}

	/**
	 * Ensures reports issue if name clash between bound {@link ManagedObject}
	 * instances.
	 */
	public void testNameClashBetweenBoundManagedObjects() {

		// Record
		final String CLASH_NAME = "CLASH";
		ManagedObjectConfiguration<?> oneConfiguration = new DependencyMappingBuilderImpl<>(CLASH_NAME, "BOUND_MO");
		ManagedObjectConfiguration<?> twoConfiguration = new DependencyMappingBuilderImpl<>(CLASH_NAME, "ANOTHER_MO");
		MockRawManagedObjectMetaDataBuilder<?, ?> rawManagedObject = this.registerRawManagedObjectMetaData("BOUND_MO");
		this.issues.addIssue(AssetType.MANAGED_OBJECT, CLASH_NAME,
				"Name clash between bound Managed Objects (name=" + CLASH_NAME + ")");

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
		assertSame("Should only return first bound managed object", rawManagedObject.getBuilt(),
				instanceMetaData.getRawManagedObjectMetaData());
	}

	/**
	 * Ensure can construct {@link RawBoundManagedObjectMetaData} with no
	 * dependencies.
	 */
	public void testBindManagedObject() {

		// Record
		ManagedObjectConfiguration<?> configuration = new DependencyMappingBuilderImpl<>("BOUND", "BOUND_MO");
		this.registerRawManagedObjectMetaData("BOUND_MO");

		// Construct and obtain the managed object meta-data
		this.replayMockObjects();
		RawBoundManagedObjectMetaData rawBoundMoMetaData = this.constructRawBoundManagedObjectMetaData(1,
				configuration)[0];
		assertEquals("Incorrect default instance index", 0, rawBoundMoMetaData.getDefaultInstanceIndex());
		assertEquals("Incorrect managed object instances", 1,
				rawBoundMoMetaData.getRawBoundManagedObjectInstanceMetaData().length);
		RawBoundManagedObjectInstanceMetaData<?> rawBoundMoInstanceMetaData = rawBoundMoMetaData
				.getRawBoundManagedObjectInstanceMetaData()[0];
		ManagedObjectMetaData<?> metaData = rawBoundMoInstanceMetaData.getManagedObjectMetaData();
		assertNotNull("Should have managed object meta-data", metaData);
		assertEquals("Incorrect managed object meta-data", "BOUND", metaData.getBoundManagedObjectName());
		this.verifyMockObjects();
	}

	/**
	 * Ensure issue if dependency not configured for a dependency key.
	 */
	public void testMissingDependencyConfiguration() {

		// Managed object configuration
		ManagedObjectConfiguration<?> configuration = new DependencyMappingBuilderImpl<>("BOUND", "BOUND_MO");
		this.registerRawManagedObjectMetaData("BOUND_MO").getMetaDataBuilder().addDependency(null, DependencyKey.KEY);
		this.issues.addIssue(AssetType.MANAGED_OBJECT, "BOUND",
				"No mapping configured for dependency 0 (key=" + DependencyKey.KEY + ", label=<no label>)");

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
		DependencyMappingBuilderImpl<?> configuration = new DependencyMappingBuilderImpl<>("BOUND", "BOUND_MO");
		configuration.mapDependency(DependencyKey.KEY, "NOT_AVAILABLE");
		this.registerRawManagedObjectMetaData("BOUND_MO").getMetaDataBuilder().addDependency(null, DependencyKey.KEY);
		this.issues.addIssue(AssetType.MANAGED_OBJECT, "BOUND",
				"No dependent ManagedObject by name 'NOT_AVAILABLE' for dependency 0 (key=" + DependencyKey.KEY
						+ ", label=<no label>)");

		// Attempt to construct
		this.replayMockObjects();
		this.constructRawBoundManagedObjectMetaData(1, configuration);
		this.verifyMockObjects();
	}

	/**
	 * Ensures issue if the dependency is incompatible.
	 */
	public void testIncompatibleDependency() {

		// Record
		DependencyMappingBuilderImpl<?> configuration = new DependencyMappingBuilderImpl<>("BOUND", "BOUND_MO");
		configuration.mapDependency(DependencyKey.KEY, "DEPENDENCY");
		this.registerRawManagedObjectMetaData("BOUND_MO").getMetaDataBuilder().addDependency(String.class,
				DependencyKey.KEY);
		MockRawManagedObjectMetaDataBuilder<?, ?> dependency = this.registerRawManagedObjectMetaData("DEPENDENCY_MO");
		dependency.getMetaDataBuilder().setObjectClass(Integer.class);
		this.scopeRawManagedObjectMetaData("DEPENDENCY", dependency);
		this.issues.addIssue(AssetType.MANAGED_OBJECT, "BOUND",
				"Incompatible dependency for dependency 0 (key=KEY, label=<no label>) (required type="
						+ String.class.getName() + ", dependency type=" + Integer.class.getName()
						+ ", ManagedObjectSource=DEPENDENCY_MO)");

		// Attempt to construct
		this.replayMockObjects();
		this.constructRawBoundManagedObjectMetaData(1, configuration);
		this.verifyMockObjects();
	}

	/**
	 * Ensures issue if extra dependencies are configured.
	 */
	public void testExtraDependenciesConfigured() {

		// Record
		DependencyMappingBuilderImpl<?> configuration = new DependencyMappingBuilderImpl<>("BOUND", "BOUND_MO");
		configuration.mapDependency(DependencyKey.KEY, "DEPENDENCY");
		configuration.mapDependency(1, "EXTRA");
		this.registerRawManagedObjectMetaData("BOUND_MO").getMetaDataBuilder().addDependency(Object.class,
				DependencyKey.KEY);
		MockRawManagedObjectMetaDataBuilder<?, ?> dependency = this.registerRawManagedObjectMetaData("DEPENDENCY_MO");
		this.scopeRawManagedObjectMetaData("DEPENDENCY", dependency);
		this.issues.addIssue(AssetType.MANAGED_OBJECT, "BOUND",
				"Extra dependencies configured than required by ManagedObjectSourceMetaData");

		// Construct
		this.replayMockObjects();
		this.constructRawBoundManagedObjectMetaData(1, configuration);
		this.verifyMockObjects();
	}

	/**
	 * Ensure can construct {@link RawBoundManagedObjectMetaData} with
	 * dependency on another {@link RawBoundManagedObjectMetaData}.
	 */
	public void testDependencyOnAnotherBound() {

		// Record
		DependencyMappingBuilderImpl<?> oneConfig = new DependencyMappingBuilderImpl<>("BOUND", "BOUND_MO");
		oneConfig.mapDependency(DependencyKey.KEY, "DEPENDENCY");
		this.registerRawManagedObjectMetaData("BOUND_MO").getMetaDataBuilder().addDependency(Object.class,
				DependencyKey.KEY);
		DependencyMappingBuilderImpl<?> twoConfig = new DependencyMappingBuilderImpl<>("DEPENDENCY", "DEPENDENCY_MO");
		this.registerRawManagedObjectMetaData("DEPENDENCY_MO");

		// Build
		this.replayMockObjects();
		RawBoundManagedObjectMetaData[] rawMetaData = this.constructRawBoundManagedObjectMetaData(2, oneConfig,
				twoConfig);
		this.verifyMockObjects();

		// Validate dependencies
		RawBoundManagedObjectMetaData one = rawMetaData[0];
		assertEquals("Incorrect first bound managed object", "BOUND", one.getBoundManagedObjectName());
		assertEquals("Incorrect number of one instances", 1, one.getRawBoundManagedObjectInstanceMetaData().length);
		RawBoundManagedObjectInstanceMetaData<?> oneInstance = one.getRawBoundManagedObjectInstanceMetaData()[0];
		assertEquals("Incorrect number of dependencies", 1, oneInstance.getDependencies().length);
		RawBoundManagedObjectMetaData two = rawMetaData[1];
		assertEquals("Incorrect second bound managed object", "DEPENDENCY", two.getBoundManagedObjectName());
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

		// Record
		DependencyMappingBuilderImpl<?> oneConfig = new DependencyMappingBuilderImpl<>("ONE", "ONE_MO");
		this.registerRawManagedObjectMetaData("ONE_MO");

		DependencyMappingBuilderImpl<?> twoConfig = new DependencyMappingBuilderImpl<>("TWO", "TWO_MO");
		twoConfig.mapDependency(DependencyKey.KEY, "ONE");
		this.registerRawManagedObjectMetaData("TWO_MO").getMetaDataBuilder().addDependency(Object.class,
				DependencyKey.KEY);

		DependencyMappingBuilderImpl<?> threeConfig = new DependencyMappingBuilderImpl<>("THREE", "THREE_MO");
		threeConfig.mapDependency(DependencyKey.KEY, "TWO");
		this.registerRawManagedObjectMetaData("THREE_MO").getMetaDataBuilder().addDependency(Object.class,
				DependencyKey.KEY);

		// Construct
		this.replayMockObjects();
		RawBoundManagedObjectMetaData[] rawMetaData = this.constructRawBoundManagedObjectMetaData(3, oneConfig,
				twoConfig, threeConfig);
		this.verifyMockObjects();

		// Validate order (clean up to enable dependencies to be available)
		assertEquals("THREE", rawMetaData[0].getBoundManagedObjectName());
		assertEquals("TWO", rawMetaData[1].getBoundManagedObjectName());
		assertEquals("ONE", rawMetaData[2].getBoundManagedObjectName());
	}

	/**
	 * Ensure able to detect cyclic dependencies.
	 */
	public void testDetectCyclicDependencies() {

		// Record
		DependencyMappingBuilderImpl<?> oneConfig = new DependencyMappingBuilderImpl<>("ONE", "ONE_MO");
		oneConfig.mapDependency(DependencyKey.KEY, "TWO");
		this.registerRawManagedObjectMetaData("ONE_MO").getMetaDataBuilder().addDependency(Object.class,
				DependencyKey.KEY);

		DependencyMappingBuilderImpl<?> twoConfig = new DependencyMappingBuilderImpl<>("TWO", "TWO_MO");
		twoConfig.mapDependency(DependencyKey.KEY, "ONE");
		this.registerRawManagedObjectMetaData("TWO_MO").getMetaDataBuilder().addDependency(Object.class,
				DependencyKey.KEY);

		this.record_office_issue("Cyclic dependency between bound ManagedObjects (TWO, ONE)");

		this.replayMockObjects();
		this.constructRawBoundManagedObjectMetaData(2, oneConfig, twoConfig);
		this.verifyMockObjects();
	}

	/**
	 * Ensure can construct {@link RawBoundManagedObjectMetaData} with
	 * dependency on a {@link RawBoundManagedObjectMetaData} from scope.
	 */
	public void testDependencyOnScopeBound() {

		// Record
		DependencyMappingBuilderImpl<?> configuration = new DependencyMappingBuilderImpl<>("ONE", "ONE_MO");
		configuration.mapDependency(DependencyKey.KEY, "TWO");
		this.registerRawManagedObjectMetaData("ONE_MO").getMetaDataBuilder().addDependency(Object.class,
				DependencyKey.KEY);

		MockRawBoundManagedObjectMetaDataBuilder<?, ?> scopeMoMetaData = this.scopeRawManagedObjectMetaData("TWO",
				this.registerRawManagedObjectMetaData("TWO_MO"));

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
		assertEquals("Incorrect dependency", scopeMoMetaData.build(), dependencyInstance.getDependencies()[0]);
	}

	/**
	 * Ensure not create binding if no {@link InputManagedObjectConfiguration}.
	 */
	public void testNoInputManagedObjectConfiguration() {

		RawManagingOfficeMetaData<?> inputMo = MockConstruct.mockRawManagingOfficeMetaData(OFFICE_NAME, "MOS").build();

		// Construct
		this.replayMockObjects();
		this.constructRawBoundManagedObjectMetaData(0, inputMo);
		this.verifyMockObjects();
	}

	/**
	 * Ensure issue if no Input {@link ManagedObject} name.
	 */
	public void testNoInputManagedObjectName() {

		// Record
		MockRawManagingOfficeMetaDataBuilder<?> inputMo = MockConstruct.mockRawManagingOfficeMetaData(OFFICE_NAME,
				"MOS");
		inputMo.setInputManagedObject(null);
		this.issues.addIssue(AssetType.OFFICE, "OFFICE", "No bound name for input managed object");

		// Construct
		this.replayMockObjects();
		this.constructRawBoundManagedObjectMetaData(0, inputMo.build());
		this.verifyMockObjects();
	}

	/**
	 * Ensures reports issue if class between bound and input
	 * {@link ManagedObject} name.
	 */
	public void testClashBetweenBoundAndInputManagedObject() {

		// Record
		final String CLASH_NAME = "CLASH";
		DependencyMappingBuilderImpl<?> boundConfig = new DependencyMappingBuilderImpl<>(CLASH_NAME, "BOUND_MO");
		MockRawManagedObjectMetaDataBuilder<?, ?> bound = this.registerRawManagedObjectMetaData("BOUND_MO");

		MockRawManagingOfficeMetaDataBuilder<?> inputMo = MockConstruct.mockRawManagingOfficeMetaData(OFFICE_NAME,
				"MOS");
		inputMo.setInputManagedObject(CLASH_NAME);

		this.issues.addIssue(AssetType.MANAGED_OBJECT, CLASH_NAME,
				"Name clash between bound and input Managed Objects (name=" + CLASH_NAME + ")");

		// Construct
		this.replayMockObjects();
		RawBoundManagedObjectMetaData boundMetaData = this.constructRawBoundManagedObjectMetaData(1,
				new ManagedObjectConfiguration[] { boundConfig },
				new RawManagingOfficeMetaData[] { inputMo.build() })[0];
		this.verifyMockObjects();

		// Ensure only the bound managed object constructed
		assertEquals("Should not return input managed object", 1,
				boundMetaData.getRawBoundManagedObjectInstanceMetaData().length);
		RawBoundManagedObjectInstanceMetaData<?> instanceMetaData = boundMetaData
				.getRawBoundManagedObjectInstanceMetaData()[0];
		assertEquals("Should only return bound managed object", bound.getBuilt(),
				instanceMetaData.getRawManagedObjectMetaData());
	}

	/**
	 * Ensures no issue if name clash is on same {@link ManagedObject}.
	 */
	public void testNoClashBetweenSameBoundAndInputManagedObject() {

		final String CLASH_NAME = "CLASH";
		DependencyMappingBuilderImpl<?> boundConfig = new DependencyMappingBuilderImpl<>(CLASH_NAME, "BOUND_MO");
		MockRawManagedObjectMetaDataBuilder<?, ?> bound = this.registerRawManagedObjectMetaData("BOUND_MO");
		MockRawManagingOfficeMetaDataBuilder sameInputMo = MockConstruct.mockRawManagingOfficeMetaData(OFFICE_NAME,
				"MOS");
		sameInputMo.setInputManagedObject(CLASH_NAME);
		RawManagedObjectMetaData<?, ?> sameMo = bound.build(sameInputMo.build());
		sameInputMo.build().setRawManagedObjectMetaData(sameMo);

		// Construct
		this.replayMockObjects();
		RawBoundManagedObjectMetaData boundMetaData = this.constructRawBoundManagedObjectMetaData(1,
				new ManagedObjectConfiguration[] { boundConfig },
				new RawManagingOfficeMetaData[] { sameInputMo.build() })[0];
		this.verifyMockObjects();

		// Ensure only the bound managed object constructed
		assertEquals("Should not return input managed object", 1,
				boundMetaData.getRawBoundManagedObjectInstanceMetaData().length);
		RawBoundManagedObjectInstanceMetaData<?> instanceMetaData = boundMetaData
				.getRawBoundManagedObjectInstanceMetaData()[0];
		assertEquals("Should only return bound managed object", bound.getBuilt(),
				instanceMetaData.getRawManagedObjectMetaData());
	}

	/**
	 * Ensure able to bind Input {@link ManagedObject} to the same name.
	 */
	public void testBindInputManagedObject() {

		// Record
		MockRawManagingOfficeMetaDataBuilder inputMo = MockConstruct.mockRawManagingOfficeMetaData(OFFICE_NAME, "MOS");
		inputMo.setInputManagedObject("MO");
		MockRawManagedObjectMetaDataBuilder<?, ?> rawManagedObject = MockConstruct.mockRawManagedObjectMetaData("MOS");
		inputMo.build().setRawManagedObjectMetaData(rawManagedObject.build(inputMo.build()));

		// Construct
		this.replayMockObjects();
		RawBoundManagedObjectMetaData metaData = this.constructRawBoundManagedObjectMetaData(1, inputMo.build())[0];
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
		assertEquals("Incorrect raw managed object meta-data", rawManagedObject.getBuilt(),
				instanceMetaData.getRawManagedObjectMetaData());
		assertNotNull("Should have managed object meta-data", instanceMetaData.getManagedObjectMetaData());
		assertEquals("Incorrect bound instance", "MO",
				instanceMetaData.getManagedObjectMetaData().getBoundManagedObjectName());
	}

	/**
	 * Ensure issue if binding Input {@link ManagedObject} instances to same
	 * name without a default instance specified.
	 */
	public void testBindInputManagedObjectInstancesWithNoDefault() {

		// Record
		MockRawManagingOfficeMetaDataBuilder inputOne = MockConstruct.mockRawManagingOfficeMetaData(OFFICE_NAME,
				"MOS_ONE");
		inputOne.setInputManagedObject("MO");
		MockRawManagedObjectMetaDataBuilder<?, ?> moOne = MockConstruct.mockRawManagedObjectMetaData("MOS_ONE");
		inputOne.build().setRawManagedObjectMetaData(moOne.build(inputOne.build()));

		MockRawManagingOfficeMetaDataBuilder inputTwo = MockConstruct.mockRawManagingOfficeMetaData(OFFICE_NAME,
				"MOS_TWO");
		inputTwo.setInputManagedObject("MO");
		MockRawManagedObjectMetaDataBuilder<?, ?> moTwo = MockConstruct.mockRawManagedObjectMetaData("MOS_TWO");
		inputTwo.build().setRawManagedObjectMetaData(moTwo.build(inputOne.build()));

		this.issues.addIssue(AssetType.MANAGED_OBJECT, "MO",
				"Bound Managed Object Source must be specified for Input Managed Object 'MO'");

		// Construct
		this.replayMockObjects();
		RawBoundManagedObjectMetaData metaData = this.constructRawBoundManagedObjectMetaData(1, inputOne.build(),
				inputTwo.build())[0];
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

		// Record
		MockRawManagingOfficeMetaDataBuilder inputOne = MockConstruct.mockRawManagingOfficeMetaData(OFFICE_NAME,
				"MOS_ONE");
		inputOne.setInputManagedObject("MO");
		MockRawManagedObjectMetaDataBuilder<?, ?> moOne = MockConstruct.mockRawManagedObjectMetaData("MOS_ONE");
		inputOne.build().setRawManagedObjectMetaData(moOne.build(inputOne.build()));

		MockRawManagingOfficeMetaDataBuilder inputTwo = MockConstruct.mockRawManagingOfficeMetaData(OFFICE_NAME,
				"MOS_TWO");
		inputTwo.setInputManagedObject("MO");
		MockRawManagedObjectMetaDataBuilder<?, ?> moTwo = MockConstruct.mockRawManagedObjectMetaData("MOS_TWO");
		inputTwo.build().setRawManagedObjectMetaData(moTwo.build(inputOne.build()));

		this.inputManagedObjectDefaults.put("MO", "UNKNOWN_MANAGED_OBJECT_SOURCE");

		this.issues.addIssue(AssetType.MANAGED_OBJECT, "MO",
				"Managed Object Source 'UNKNOWN_MANAGED_OBJECT_SOURCE' not linked to Input Managed Object 'MO' for being the bound instance");

		// Construct
		this.replayMockObjects();
		RawBoundManagedObjectMetaData metaData = this.constructRawBoundManagedObjectMetaData(1, inputOne.build(),
				inputTwo.build())[0];
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

		// Record
		MockRawManagingOfficeMetaDataBuilder inputOne = MockConstruct.mockRawManagingOfficeMetaData(OFFICE_NAME,
				"MOS_ONE");
		inputOne.setInputManagedObject("MO");
		MockRawManagedObjectMetaDataBuilder<?, ?> moOne = MockConstruct.mockRawManagedObjectMetaData("MOS_ONE");
		inputOne.build().setRawManagedObjectMetaData(moOne.build(inputOne.build()));

		MockRawManagingOfficeMetaDataBuilder inputTwo = MockConstruct.mockRawManagingOfficeMetaData(OFFICE_NAME,
				"MOS_TWO");
		inputTwo.setInputManagedObject("MO");
		MockRawManagedObjectMetaDataBuilder<?, ?> moTwo = MockConstruct.mockRawManagedObjectMetaData("MOS_TWO");
		inputTwo.build().setRawManagedObjectMetaData(moTwo.build(inputOne.build()));

		// Specify second managed object source as default
		this.inputManagedObjectDefaults.put("MO", "MOS_TWO");

		// Construct
		this.replayMockObjects();
		RawBoundManagedObjectMetaData metaData = this.constructRawBoundManagedObjectMetaData(1, inputOne.build(),
				inputTwo.build())[0];
		this.verifyMockObjects();

		// Verify instances
		assertEquals("Incorrect bound managed object name", "MO", metaData.getBoundManagedObjectName());
		assertEquals("Incorrect default instance index", 1, metaData.getDefaultInstanceIndex());
		RawBoundManagedObjectInstanceMetaData<?>[] instanceMetaData = metaData
				.getRawBoundManagedObjectInstanceMetaData();
		assertEquals("Incorrect number of instances", 2, instanceMetaData.length);

		// Verify first instance
		RawBoundManagedObjectInstanceMetaData<?> oneInstance = instanceMetaData[0];
		assertEquals("Incorrect instance one raw meta-data", moOne.getBuilt(),
				oneInstance.getRawManagedObjectMetaData());
		assertEquals("Incorrect instance one meta-data", "MO",
				oneInstance.getManagedObjectMetaData().getBoundManagedObjectName());
		assertEquals("Should be no dependencies for instance one", 0, oneInstance.getDependencies().length);

		// Verify second instance
		RawBoundManagedObjectInstanceMetaData<?> twoInstance = instanceMetaData[1];
		assertEquals("Incorrect instance two raw meta-data", moTwo.getBuilt(),
				twoInstance.getRawManagedObjectMetaData());
		assertEquals("Incorrect instance two meta-data", "MO",
				twoInstance.getManagedObjectMetaData().getBoundManagedObjectName());
		assertEquals("Should be no dependencies for instance two", 0, twoInstance.getDependencies().length);
	}

	/**
	 * Ensure bound {@link ManagedObject} can be dependent on an Input
	 * {@link ManagedObject}.
	 */
	public void testBoundDependentOnInput() {

		// Record
		DependencyMappingBuilderImpl<?> boundConfig = new DependencyMappingBuilderImpl<>("BOUND", "BOUND_MO");
		boundConfig.mapDependency(DependencyKey.KEY, "INPUT");
		this.registerRawManagedObjectMetaData("BOUND_MO").getMetaDataBuilder().addDependency(Object.class,
				DependencyKey.KEY);

		MockRawManagingOfficeMetaDataBuilder inputOffice = MockConstruct.mockRawManagingOfficeMetaData(OFFICE_NAME,
				"INPUT_MO");
		inputOffice.setInputManagedObject("INPUT");
		MockRawManagedObjectMetaDataBuilder<?, ?> inputMo = MockConstruct.mockRawManagedObjectMetaData("INPUT_MO");
		inputOffice.build().setRawManagedObjectMetaData(inputMo.build(inputOffice.build()));

		// Construct
		this.replayMockObjects();
		RawBoundManagedObjectMetaData[] rawMetaData = this.constructRawBoundManagedObjectMetaData(2,
				new ManagedObjectConfiguration[] { boundConfig },
				new RawManagingOfficeMetaData[] { inputOffice.build() });
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

		// Record
		DependencyMappingBuilderImpl<?> boundConfig = new DependencyMappingBuilderImpl<>("BOUND", "BOUND_MO");
		this.registerRawManagedObjectMetaData("BOUND_MO");

		MockRawManagingOfficeMetaDataBuilder inputOffice = MockConstruct.mockRawManagingOfficeMetaData(OFFICE_NAME,
				"INPUT_MO");
		inputOffice.setInputManagedObject("INPUT").mapDependency(DependencyKey.KEY, "BOUND");
		MockRawManagedObjectMetaDataBuilder inputMo = MockConstruct.mockRawManagedObjectMetaData("INPUT_MO");
		inputMo.getMetaDataBuilder().addDependency(Object.class, DependencyKey.KEY);
		inputOffice.build().setRawManagedObjectMetaData(inputMo.build(inputOffice.build()));

		// Construct
		this.replayMockObjects();
		RawBoundManagedObjectMetaData[] rawMetaData = this.constructRawBoundManagedObjectMetaData(2,
				new ManagedObjectConfiguration[] { boundConfig },
				new RawManagingOfficeMetaData[] { inputOffice.build() });
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
	public void testGovernManagedObject() throws Throwable {

		final ManagedObjectGovernanceConfiguration governanceConfiguration = this
				.createMock(ManagedObjectGovernanceConfiguration.class);
		final ManagedObjectGovernanceStruct[] governances = new ManagedObjectGovernanceStruct[] {
				new ManagedObjectGovernanceStruct(1) };
		final ManagedObject managedObject = this.createMock(ManagedObject.class);
		final Object extensionInterface = "EXTENSION INTERFACE";

		// Managed object configuration
		// RawManagedObjectMetaData<?, ?> rawMoMetaData =
		// this.registerRawManagedObjectMetaData("OFFICE_MO");

		// Record construction
		ManagedObjectConfiguration<?> configuration = this.record_initManagedObject("BOUND", "OFFICE_MO",
				governanceConfiguration);
		// this.record_getDependencyMetaData(rawMoMetaData);
		// ExtensionFactory<?>[] eiFactories =
		// this.record_governManagedObject(String.class, rawMoMetaData,
		// configuration,
		// new ManagedObjectGovernanceConfiguration[] { governanceConfiguration
		// }, String.class);
		// ManagedObjectMetaData<?> moMetaData =
		// this.record_loadManagedObjectMetaData(rawMoMetaData, "BOUND", 0,
		// governances);

		// Record using extension interface factory
		// ExtensionFactory<?> eiFactory = eiFactories[0];
		// this.recordReturn(eiFactory,
		// eiFactory.createExtension(managedObject), extensionInterface);

		// Construct and obtain the managed object meta-data
		this.replayMockObjects();

		// Construction of ManagedObjectMetaData tests governance
		this.constructRawBoundManagedObjectMetaData(1, configuration);

		// Ensure correct extension factory
		// Object ei =
		// governances[0].extensionInterfaceExtractor.extractExtension(managedObject,
		// moMetaData);
		// assertEquals("Incorrect extension interface", extensionInterface,
		// ei);

		this.verifyMockObjects();
	}

	/**
	 * Ensure issue if unknown {@link Governance}.
	 */
	public void testUnknownGovernance() {

		final ManagedObjectGovernanceConfiguration governanceConfiguration = this
				.createMock(ManagedObjectGovernanceConfiguration.class);

		// Managed object configuration
		// RawManagedObjectMetaData<?, ?> rawMoMetaData =
		// this.registerRawManagedObjectMetaData("OFFICE_MO");

		// Record unknown Governance
		ManagedObjectConfiguration<?> configuration = this.record_initManagedObject("BOUND", "OFFICE_MO",
				governanceConfiguration);
		// this.record_getDependencyMetaData(rawMoMetaData);
		this.recordReturn(governanceConfiguration, governanceConfiguration.getGovernanceName(), "GOVERNANCE");
		this.issues.addIssue(AssetType.MANAGED_OBJECT, "BOUND", "Unknown governance 'GOVERNANCE'");

		// Record remaining aspect of loading Managed Object
		// this.record_loadManagedObjectMetaData(rawMoMetaData, "BOUND", 0, new
		// ManagedObjectGovernanceStruct[] { null });

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
		// RawManagedObjectMetaData<?, ?> rawMoMetaData =
		// this.registerRawManagedObjectMetaData("OFFICE_MO");

		// Record unsupported Governance by Managed Object
		ManagedObjectConfiguration<?> configuration = this.record_initManagedObject("BOUND", "OFFICE_MO",
				governanceConfiguration);
		// this.record_getDependencyMetaData(rawMoMetaData);
		// this.record_governManagedObject(String.class, rawMoMetaData,
		// configuration, governanceConfigurations,
		// Integer.class);
		this.issues.addIssue(AssetType.MANAGED_OBJECT, "BOUND", "Extension interface of type " + String.class.getName()
				+ " is not available from Managed Object for Governance 'GOVERNANCE'");

		// Record remaining aspect of loading Managed Object
		// this.record_loadManagedObjectMetaData(rawMoMetaData, "BOUND", 0,
		// governances);

		// Construct and obtain the managed object meta-data
		this.replayMockObjects();

		// Construction of ManagedObjectMetaData tests governance
		this.constructRawBoundManagedObjectMetaData(1, configuration);

		this.verifyMockObjects();
	}

	/**
	 * Provides a scope {@link RawBoundManagedObjectMetaData}.
	 * 
	 * @param scopeManagedObjectName
	 *            Name of the scope {@link RawBoundManagedObjectMetaData}.
	 * @return Scope {@link RawBoundManagedObjectMetaData}.
	 */
	private MockRawBoundManagedObjectMetaDataBuilder<?, ?> scopeRawManagedObjectMetaData(String scopeManagedObjectName,
			MockRawManagedObjectMetaDataBuilder<?, ?> rawManagedObjectMetaData) {
		MockRawBoundManagedObjectMetaDataBuilder<?, ?> metaData = MockConstruct
				.mockRawBoundManagedObjectMetaData(scopeManagedObjectName, rawManagedObjectMetaData);
		metaData.addRawBoundManagedObjectInstanceMetaData();
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
	private MockRawManagedObjectMetaDataBuilder<DependencyKey, ?> registerRawManagedObjectMetaData(
			String registeredManagedObjectName) {
		MockRawManagedObjectMetaDataBuilder<DependencyKey, ?> rawManagedObjectBuilder = MockConstruct
				.mockRawManagedObjectMetaData(registeredManagedObjectName);
		this.registeredManagedObjects.put(registeredManagedObjectName, rawManagedObjectBuilder);
		return rawManagedObjectBuilder;
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
	private ExtensionFactory<?>[] record_governManagedObject(Class<?> governanceExtensionInterfaceType,
			RawManagedObjectMetaData<?, ?> rawMoMetaData, ManagedObjectConfiguration<?> configuration,
			ManagedObjectGovernanceConfiguration[] governanceConfigurations, Class<?>... extensionInterfaceTypes) {

		// Governance meta-data
		final RawGovernanceMetaData<?, ?> rawGovernanceMetaData = this.createMock(RawGovernanceMetaData.class);
		final String GOVERNANCE_NAME = "GOVERNANCE";
		this.rawGovernanceMetaDatas.put(GOVERNANCE_NAME, rawGovernanceMetaData);

		// Extension interface meta-data
		ManagedObjectExtensionMetaData<?>[] eiMetaDatas = new ManagedObjectExtensionMetaData<?>[extensionInterfaceTypes.length];
		for (int i = 0; i < eiMetaDatas.length; i++) {
			eiMetaDatas[i] = this.createMock(ManagedObjectExtensionMetaData.class);
		}

		// Ensure have Managed Object Governance configuration
		governanceConfigurations = (governanceConfigurations == null ? new ManagedObjectGovernanceConfiguration[0]
				: governanceConfigurations);

		// Configure the governance
		ExtensionFactory<?>[] eiFactories = new ExtensionFactory<?>[governanceConfigurations.length];
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
				ManagedObjectExtensionMetaData<?> eiMetaData = eiMetaDatas[e];
				Class<?> extensionInterfaceType = extensionInterfaceTypes[e];
				this.recordReturn(eiMetaData, eiMetaData.getExtensionType(), extensionInterfaceType);

				// Determine if a match
				if (governanceExtensionInterfaceType.isAssignableFrom(extensionInterfaceType)) {
					// Match, so load governance
					final ExtensionFactory<?> eiFactory = this.createMock(ExtensionFactory.class);
					this.recordReturn(eiMetaData, eiMetaData.getExtensionFactory(), eiFactory);

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

		// Create the necessary mock objects
		ManagedObjectMetaData<?> moMetaData = this.createMock(ManagedObjectMetaData.class);

		// Record creating the managed object meta-data
		this.recordReturn(rawMoMetaData,
				rawMoMetaData.createManagedObjectMetaData(AssetType.OFFICE, OFFICE_NAME, null, instanceIndex, null,
						dependencies, new ManagedObjectGovernanceMetaData<?>[0], this.assetManagerFactory, this.issues),
				moMetaData, new AbstractMatcher() {
					@Override
					public boolean matches(Object[] expected, Object[] actual) {

						// Ensure asset type and name are correct
						assertEquals("Incorrect asset type", AssetType.OFFICE, actual[0]);
						assertEquals("Incorrect asset name", OFFICE_NAME, actual[1]);

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
	 * Record an issue.
	 * 
	 * @param issueDescription
	 *            Issue description.
	 */
	private void record_office_issue(String issueDescription) {
		this.issues.addIssue(AssetType.OFFICE, OFFICE_NAME, issueDescription);
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

		// Build the registered managed objects
		Map<String, RawManagedObjectMetaData<?, ?>> registeredManagedObjects = new HashMap<>();
		for (String name : this.registeredManagedObjects.keySet()) {
			MockRawManagedObjectMetaDataBuilder<?, ?> registeredManagedObject = this.registeredManagedObjects.get(name);
			if (registeredManagedObject.isBuilt()) {
				registeredManagedObjects.put(name, registeredManagedObject.getBuilt());
			} else {
				RawManagingOfficeMetaData rawManagingOffice = MockConstruct
						.mockRawManagingOfficeMetaData(OFFICE_NAME, name).build();
				registeredManagedObjects.put(name, this.registeredManagedObjects.get(name).build(rawManagingOffice));
			}
		}

		// Build the scope managed objects
		Map<String, RawBoundManagedObjectMetaData> scopeManagedObjects = new HashMap<>();
		for (String name : this.scopeManagedObjects.keySet()) {
			scopeManagedObjects.put(name, this.scopeManagedObjects.get(name).build());
		}

		// Attempt to construct
		RawBoundManagedObjectMetaData[] metaData = new RawBoundManagedObjectMetaDataFactory()
				.constructBoundManagedObjectMetaData(boundManagedObjectConfiguration, this.issues,
						this.managedObjectScope, AssetType.OFFICE, OFFICE_NAME, this.assetManagerFactory,
						registeredManagedObjects, scopeManagedObjects, inputManagedObjects, inputDefaults,
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
		 * Actual {@link ManagedObjectExtensionExtractor}.
		 */
		public ManagedObjectExtensionExtractor<?> extensionInterfaceExtractor = null;

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