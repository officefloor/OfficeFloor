/*-
 * #%L
 * OfficeFrame
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

package net.officefloor.frame.impl.construct.managedobject;

import java.util.HashMap;
import java.util.Map;

import net.officefloor.frame.api.administration.Administration;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.build.OfficeFloorIssues;
import net.officefloor.frame.api.build.OfficeFloorIssues.AssetType;
import net.officefloor.frame.api.governance.Governance;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.impl.construct.MockConstruct;
import net.officefloor.frame.impl.construct.MockConstruct.ManagedObjectSourceMetaDataMockBuilder;
import net.officefloor.frame.impl.construct.MockConstruct.OfficeMetaDataMockBuilder;
import net.officefloor.frame.impl.construct.MockConstruct.RawBoundManagedObjectInstanceMetaDataMockBuilder;
import net.officefloor.frame.impl.construct.MockConstruct.RawBoundManagedObjectMetaDataMockBuilder;
import net.officefloor.frame.impl.construct.MockConstruct.RawManagedObjectMetaDataMockBuilder;
import net.officefloor.frame.impl.construct.MockConstruct.RawManagingOfficeMetaDataMockBuilder;
import net.officefloor.frame.impl.construct.MockConstruct.RawOfficeMetaDataMockBuilder;
import net.officefloor.frame.impl.construct.administration.RawAdministrationMetaDataFactory;
import net.officefloor.frame.impl.construct.asset.AssetManagerRegistry;
import net.officefloor.frame.impl.construct.escalation.EscalationFlowFactory;
import net.officefloor.frame.impl.construct.flow.FlowMetaDataFactory;
import net.officefloor.frame.impl.construct.managedobjectsource.RawManagedObjectMetaData;
import net.officefloor.frame.impl.construct.managedobjectsource.RawManagingOfficeMetaData;
import net.officefloor.frame.impl.construct.office.RawOfficeMetaData;
import net.officefloor.frame.internal.configuration.InputManagedObjectConfiguration;
import net.officefloor.frame.internal.configuration.ManagedObjectConfiguration;
import net.officefloor.frame.internal.structure.ManagedObjectAdministrationMetaData;
import net.officefloor.frame.internal.structure.ManagedObjectIndex;
import net.officefloor.frame.internal.structure.ManagedObjectMetaData;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.internal.structure.OfficeMetaData;
import net.officefloor.frame.test.Closure;
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
	 * {@link AssetManagerRegistry}.
	 */
	private AssetManagerRegistry assetManagerRegistry = MockConstruct.mockAssetManagerRegistry();

	/**
	 * {@link OfficeMetaData}.
	 */
	private final OfficeMetaDataMockBuilder officeMetaData = MockConstruct.mockOfficeMetaData(OFFICE_NAME);

	/**
	 * {@link RawOfficeMetaData}.
	 */
	private final RawOfficeMetaDataMockBuilder rawOfficeMetaData = MockConstruct.mockRawOfficeMetaData(OFFICE_NAME);

	/**
	 * Input {@link ManagedObject} defaults.
	 */
	private final Map<String, String> inputManagedObjectDefaults = new HashMap<String, String>();

	/**
	 * {@link OfficeFloorIssues}.
	 */
	private final OfficeFloorIssues issues = this.createMock(OfficeFloorIssues.class);

	/**
	 * Ensure issue if no bound name.
	 */
	public void testNoBoundName() {

		// Record
		this.issues.addIssue(AssetType.OFFICE, OFFICE_NAME, "No bound name for managed object");

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
		this.issues.addIssue(AssetType.OFFICE, OFFICE_NAME, "No office name for bound managed object of name 'BOUND'");

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
		this.issues.addIssue(AssetType.OFFICE, OFFICE_NAME,
				"No managed object by name 'BOUND_MO' registered with the Office");

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
		this.rawOfficeMetaData.addRegisteredManagedObject("BOUND_MO");

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
		RawManagedObjectMetaDataMockBuilder<?, ?> rawManagedObject = this.rawOfficeMetaData
				.addRegisteredManagedObject("BOUND_MO");
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
		this.rawOfficeMetaData.addRegisteredManagedObject("BOUND_MO");

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
		RawManagedObjectMetaDataMockBuilder<DependencyKey, ?> bound = this.rawOfficeMetaData
				.addRegisteredManagedObject("BOUND_MO");
		bound.getMetaDataBuilder().addDependency(null, DependencyKey.KEY);
		this.issues.addIssue(AssetType.MANAGED_OBJECT, "BOUND",
				"No mapping configured for dependency 0 (key=" + DependencyKey.KEY + ", label=<no label>)");

		// Construct and obtain the managed object meta-data
		this.replayMockObjects();
		this.constructRawBoundManagedObjectMetaData(1, configuration);
		this.verifyMockObjects();
	}

	/**
	 * Ensure can construct {@link RawBoundManagedObjectMetaData} with dependency
	 * not available.
	 */
	public void testDependencyNotAvailable() {

		// Managed object configuration
		DependencyMappingBuilderImpl<?> configuration = new DependencyMappingBuilderImpl<>("BOUND", "BOUND_MO");
		configuration.mapDependency(DependencyKey.KEY, "NOT_AVAILABLE");
		RawManagedObjectMetaDataMockBuilder<DependencyKey, ?> bound = this.rawOfficeMetaData
				.addRegisteredManagedObject("BOUND_MO");
		bound.getMetaDataBuilder().addDependency(null, DependencyKey.KEY);
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

		RawManagedObjectMetaDataMockBuilder<DependencyKey, ?> dependency = this.rawOfficeMetaData
				.addRegisteredManagedObject("BOUND_MO");
		dependency.getMetaDataBuilder().addDependency(String.class, DependencyKey.KEY);

		this.rawOfficeMetaData.addScopeBoundManagedObject("DEPENDENCY").getRawBoundManagedObjectMetaData()
				.getRawManagedObjectBuilder().getMetaDataBuilder().setObjectClass(Integer.class);

		this.issues.addIssue(AssetType.MANAGED_OBJECT, "BOUND",
				"Incompatible dependency for dependency 0 (key=KEY, label=<no label>) (required type="
						+ String.class.getName() + ", dependency type=" + Integer.class.getName()
						+ ", ManagedObjectSource=DEPENDENCY)");

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
		RawManagedObjectMetaDataMockBuilder<DependencyKey, ?> bound = this.rawOfficeMetaData
				.addRegisteredManagedObject("BOUND_MO");
		bound.getMetaDataBuilder().addDependency(Object.class, DependencyKey.KEY);
		this.rawOfficeMetaData.addScopeBoundManagedObject("DEPENDENCY");
		this.issues.addIssue(AssetType.MANAGED_OBJECT, "BOUND",
				"Extra dependencies configured than required by ManagedObjectSourceMetaData");

		// Construct
		this.replayMockObjects();
		this.constructRawBoundManagedObjectMetaData(1, configuration);
		this.verifyMockObjects();
	}

	/**
	 * Ensure can construct {@link RawBoundManagedObjectMetaData} with dependency on
	 * another {@link RawBoundManagedObjectMetaData}.
	 */
	public void testDependencyOnAnotherBound() {

		// Record
		DependencyMappingBuilderImpl<?> oneConfig = new DependencyMappingBuilderImpl<>("BOUND", "BOUND_MO");
		oneConfig.mapDependency(DependencyKey.KEY, "DEPENDENCY");
		RawManagedObjectMetaDataMockBuilder<DependencyKey, ?> bound = this.rawOfficeMetaData
				.addRegisteredManagedObject("BOUND_MO");
		bound.getMetaDataBuilder().addDependency(Object.class, DependencyKey.KEY);
		DependencyMappingBuilderImpl<?> twoConfig = new DependencyMappingBuilderImpl<>("DEPENDENCY", "DEPENDENCY_MO");
		this.rawOfficeMetaData.addRegisteredManagedObject("DEPENDENCY_MO");

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
	 * Ensures the order of {@link RawBoundManagedObjectMetaData} is order to clean
	 * up the {@link ManagedObject} instances.
	 */
	public void testOrderingManagedObjectsByDependenciesForCleanup() {

		// Record
		DependencyMappingBuilderImpl<?> oneConfig = new DependencyMappingBuilderImpl<>("ONE", "ONE_MO");
		this.rawOfficeMetaData.addRegisteredManagedObject("ONE_MO");

		DependencyMappingBuilderImpl<?> twoConfig = new DependencyMappingBuilderImpl<>("TWO", "TWO_MO");
		twoConfig.mapDependency(DependencyKey.KEY, "ONE");
		RawManagedObjectMetaDataMockBuilder<DependencyKey, ?> two = this.rawOfficeMetaData
				.addRegisteredManagedObject("TWO_MO");
		two.getMetaDataBuilder().addDependency(Object.class, DependencyKey.KEY);

		DependencyMappingBuilderImpl<?> threeConfig = new DependencyMappingBuilderImpl<>("THREE", "THREE_MO");
		threeConfig.mapDependency(DependencyKey.KEY, "TWO");
		RawManagedObjectMetaDataMockBuilder<DependencyKey, ?> three = this.rawOfficeMetaData
				.addRegisteredManagedObject("THREE_MO");
		three.getMetaDataBuilder().addDependency(Object.class, DependencyKey.KEY);

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
		RawManagedObjectMetaDataMockBuilder<DependencyKey, ?> one = this.rawOfficeMetaData
				.addRegisteredManagedObject("ONE_MO");
		one.getMetaDataBuilder().addDependency(Object.class, DependencyKey.KEY);

		DependencyMappingBuilderImpl<?> twoConfig = new DependencyMappingBuilderImpl<>("TWO", "TWO_MO");
		twoConfig.mapDependency(DependencyKey.KEY, "ONE");
		RawManagedObjectMetaDataMockBuilder<DependencyKey, ?> two = this.rawOfficeMetaData
				.addRegisteredManagedObject("TWO_MO");
		two.getMetaDataBuilder().addDependency(Object.class, DependencyKey.KEY);

		this.issues.addIssue(AssetType.OFFICE, OFFICE_NAME,
				"Cyclic dependency between bound ManagedObjects (TWO, ONE)");

		this.replayMockObjects();
		this.constructRawBoundManagedObjectMetaData(2, oneConfig, twoConfig);
		this.verifyMockObjects();
	}

	/**
	 * Ensure can construct {@link RawBoundManagedObjectMetaData} with dependency on
	 * a {@link RawBoundManagedObjectMetaData} from scope.
	 */
	public void testDependencyOnScopeBound() {

		// Record
		DependencyMappingBuilderImpl<?> configuration = new DependencyMappingBuilderImpl<>("ONE", "ONE_MO");
		configuration.mapDependency(DependencyKey.KEY, "TWO");
		RawManagedObjectMetaDataMockBuilder<DependencyKey, ?> one = this.rawOfficeMetaData
				.addRegisteredManagedObject("ONE_MO");
		one.getMetaDataBuilder().addDependency(Object.class, DependencyKey.KEY);

		RawBoundManagedObjectMetaDataMockBuilder<?, ?> scopeMoMetaData = this.rawOfficeMetaData
				.addScopeBoundManagedObject("TWO").getRawBoundManagedObjectMetaData();

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
		RawManagingOfficeMetaDataMockBuilder<?> inputMo = MockConstruct.mockRawManagingOfficeMetaData(OFFICE_NAME,
				"MOS");
		inputMo.getBuilder().setInputManagedObjectName(null);
		this.issues.addIssue(AssetType.OFFICE, "OFFICE", "No bound name for input managed object");

		// Construct
		this.replayMockObjects();
		this.constructRawBoundManagedObjectMetaData(0, inputMo.build());
		this.verifyMockObjects();
	}

	/**
	 * Ensures reports issue if class between bound and input {@link ManagedObject}
	 * name.
	 */
	public void testClashBetweenBoundAndInputManagedObject() {

		// Record
		final String CLASH_NAME = "CLASH";
		DependencyMappingBuilderImpl<?> boundConfig = new DependencyMappingBuilderImpl<>(CLASH_NAME, "BOUND_MO");
		RawManagedObjectMetaDataMockBuilder<?, ?> bound = this.rawOfficeMetaData.addRegisteredManagedObject("BOUND_MO");

		RawManagingOfficeMetaDataMockBuilder<?> inputMo = MockConstruct.mockRawManagingOfficeMetaData(OFFICE_NAME,
				"MOS");
		inputMo.getBuilder().setInputManagedObjectName(CLASH_NAME);

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
		RawManagedObjectMetaDataMockBuilder<DependencyKey, None> bound = this.rawOfficeMetaData
				.addRegisteredManagedObject("BOUND_MO");
		RawManagingOfficeMetaDataMockBuilder<None> sameInputMo = MockConstruct
				.mockRawManagingOfficeMetaData(OFFICE_NAME, "MOS");
		sameInputMo.getBuilder().setInputManagedObjectName(CLASH_NAME);
		RawManagedObjectMetaData<DependencyKey, None> sameMo = bound.build(sameInputMo.build());
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
		RawManagingOfficeMetaDataMockBuilder<None> inputMo = MockConstruct.mockRawManagingOfficeMetaData(OFFICE_NAME,
				"MOS");
		inputMo.getBuilder().setInputManagedObjectName("MO");
		RawManagedObjectMetaDataMockBuilder<DependencyKey, None> rawManagedObject = MockConstruct
				.mockRawManagedObjectMetaData("MOS");
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
	 * Ensure issue if binding Input {@link ManagedObject} instances to same name
	 * without a default instance specified.
	 */
	public void testBindInputManagedObjectInstancesWithNoDefault() {

		// Record
		RawManagingOfficeMetaDataMockBuilder<None> inputOne = MockConstruct.mockRawManagingOfficeMetaData(OFFICE_NAME,
				"MOS_ONE");
		inputOne.getBuilder().setInputManagedObjectName("MO");
		RawManagedObjectMetaDataMockBuilder<DependencyKey, None> moOne = MockConstruct
				.mockRawManagedObjectMetaData("MOS_ONE");
		inputOne.build().setRawManagedObjectMetaData(moOne.build(inputOne.build()));

		RawManagingOfficeMetaDataMockBuilder<None> inputTwo = MockConstruct.mockRawManagingOfficeMetaData(OFFICE_NAME,
				"MOS_TWO");
		inputTwo.getBuilder().setInputManagedObjectName("MO");
		RawManagedObjectMetaDataMockBuilder<DependencyKey, None> moTwo = MockConstruct
				.mockRawManagedObjectMetaData("MOS_TWO");
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
	 * Ensure issue if binding Input {@link ManagedObject} instances to same name
	 * without a default instance of not bound {@link ManagedObjectSource}.
	 */
	public void testBindInputManagedObjectInstancesWithUnknownBoundManagedObjectSource() {

		// Record
		RawManagingOfficeMetaDataMockBuilder<None> inputOne = MockConstruct.mockRawManagingOfficeMetaData(OFFICE_NAME,
				"MOS_ONE");
		inputOne.getBuilder().setInputManagedObjectName("MO");
		RawManagedObjectMetaDataMockBuilder<DependencyKey, None> moOne = MockConstruct
				.mockRawManagedObjectMetaData("MOS_ONE");
		inputOne.build().setRawManagedObjectMetaData(moOne.build(inputOne.build()));

		RawManagingOfficeMetaDataMockBuilder<None> inputTwo = MockConstruct.mockRawManagingOfficeMetaData(OFFICE_NAME,
				"MOS_TWO");
		inputTwo.getBuilder().setInputManagedObjectName("MO");
		RawManagedObjectMetaDataMockBuilder<DependencyKey, None> moTwo = MockConstruct
				.mockRawManagedObjectMetaData("MOS_TWO");
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
		RawManagingOfficeMetaDataMockBuilder<None> inputOne = MockConstruct.mockRawManagingOfficeMetaData(OFFICE_NAME,
				"MOS_ONE");
		inputOne.getBuilder().setInputManagedObjectName("MO");
		RawManagedObjectMetaDataMockBuilder<DependencyKey, None> moOne = MockConstruct
				.mockRawManagedObjectMetaData("MOS_ONE");
		inputOne.build().setRawManagedObjectMetaData(moOne.build(inputOne.build()));

		RawManagingOfficeMetaDataMockBuilder<None> inputTwo = MockConstruct.mockRawManagingOfficeMetaData(OFFICE_NAME,
				"MOS_TWO");
		inputTwo.getBuilder().setInputManagedObjectName("MO");
		RawManagedObjectMetaDataMockBuilder<DependencyKey, None> moTwo = MockConstruct
				.mockRawManagedObjectMetaData("MOS_TWO");
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
		RawManagedObjectMetaDataMockBuilder<DependencyKey, ?> boundMo = this.rawOfficeMetaData
				.addRegisteredManagedObject("BOUND_MO");
		boundMo.getMetaDataBuilder().addDependency(Object.class, DependencyKey.KEY);

		RawManagingOfficeMetaDataMockBuilder<None> inputOffice = MockConstruct
				.mockRawManagingOfficeMetaData(OFFICE_NAME, "INPUT_MO");
		inputOffice.getBuilder().setInputManagedObjectName("INPUT");
		RawManagedObjectMetaDataMockBuilder<DependencyKey, None> inputMo = MockConstruct
				.mockRawManagedObjectMetaData("INPUT_MO");
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
		this.rawOfficeMetaData.addRegisteredManagedObject("BOUND_MO");

		RawManagingOfficeMetaDataMockBuilder<None> inputOffice = MockConstruct
				.mockRawManagingOfficeMetaData(OFFICE_NAME, "INPUT_MO");
		inputOffice.getBuilder().setInputManagedObjectName("INPUT").mapDependency(DependencyKey.KEY, "BOUND");
		RawManagedObjectMetaDataMockBuilder<DependencyKey, None> inputMo = MockConstruct
				.mockRawManagedObjectMetaData("INPUT_MO");
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

		// Record
		this.rawOfficeMetaData.addGovernance("GOVERNANCE", Object.class);
		DependencyMappingBuilderImpl<?> configuration = new DependencyMappingBuilderImpl<>("BOUND", "BOUND_MO");
		configuration.mapGovernance("GOVERNANCE");
		RawManagedObjectMetaDataMockBuilder<?, ?> rawMoMetaData = this.rawOfficeMetaData
				.addRegisteredManagedObject("BOUND_MO");
		Closure<ManagedObject> closure = new Closure<>();
		rawMoMetaData.getMetaDataBuilder().addExtension(Object.class, (mo) -> {
			closure.value = mo;
			return mo;
		});

		// Construct
		this.replayMockObjects();
		RawBoundManagedObjectMetaData rawMetaData = this.constructRawBoundManagedObjectMetaData(1, configuration)[0];
		this.verifyMockObjects();

		// Ensure have governance
		ManagedObjectMetaData<?> moMetaData = rawMetaData.getRawBoundManagedObjectInstanceMetaData()[0]
				.getManagedObjectMetaData();
		ManagedObject mo = this.createMock(ManagedObject.class);
		assertSame("Ensure have governance", mo, moMetaData.getGovernanceMetaData()[0].getExtensionInterfaceExtractor()
				.extractExtension(mo, moMetaData));
		assertSame("Ensure extension factory invoked", mo, closure.value);
	}

	/**
	 * Ensure issue if unknown {@link Governance}.
	 */
	public void testUnknownGovernance() {

		// Record
		DependencyMappingBuilderImpl<?> configuration = new DependencyMappingBuilderImpl<>("BOUND", "BOUND_MO");
		configuration.mapGovernance("GOVERNANCE");
		this.rawOfficeMetaData.addRegisteredManagedObject("BOUND_MO");
		this.issues.addIssue(AssetType.MANAGED_OBJECT, "BOUND", "Unknown governance 'GOVERNANCE'");

		// Construct
		this.replayMockObjects();
		this.constructRawBoundManagedObjectMetaData(1, configuration);
		this.verifyMockObjects();
	}

	/**
	 * Ensure issue if extension for {@link Governance} is not available for the
	 * {@link ManagedObject}.
	 */
	public void testUnsupportedGovernance() {

		// Record
		this.rawOfficeMetaData.addGovernance("GOVERNANCE", String.class);
		DependencyMappingBuilderImpl<?> configuration = new DependencyMappingBuilderImpl<>("BOUND", "BOUND_MO");
		configuration.mapGovernance("GOVERNANCE");
		this.rawOfficeMetaData.addRegisteredManagedObject("BOUND_MO");
		this.issues.addIssue(AssetType.MANAGED_OBJECT, "BOUND", "Extension of type " + String.class.getName()
				+ " is not available from Managed Object for Governance 'GOVERNANCE'");

		// Construct
		this.replayMockObjects();
		this.constructRawBoundManagedObjectMetaData(1, configuration);
		this.verifyMockObjects();
	}

	/**
	 * Ensure issue if no pre-load {@link Administration}.
	 */
	public void testNoPreLoadAdministrationName() {

		// Record
		DependencyMappingBuilderImpl<?> configuration = new DependencyMappingBuilderImpl<>("BOUND", "BOUND_MO");
		configuration.preLoadAdminister(null, Object.class, () -> null);
		this.rawOfficeMetaData.addRegisteredManagedObject("BOUND_MO");
		this.issues.addIssue(AssetType.MANAGED_OBJECT, "BOUND", "Administration added without a name");

		// Construct
		this.replayMockObjects();
		ManagedObjectMetaData<?> metaData = this.constructRawBoundManagedObjectMetaData(1, configuration)[0]
				.getRawBoundManagedObjectInstanceMetaData()[0].getManagedObjectMetaData();
		this.verifyMockObjects();

		// Ensure no pre-load administration
		assertNull("Should be no pre-load administration", metaData.getPreLoadAdministration());
	}

	/**
	 * Ensure able add pre-load {@link Administration}.
	 */
	public void testPreLoadAdministration() {

		// Record
		DependencyMappingBuilderImpl<?> configuration = new DependencyMappingBuilderImpl<>("BOUND", "BOUND_MO");
		configuration.preLoadAdminister("ADMIN", Object.class, () -> null);
		this.rawOfficeMetaData.addRegisteredManagedObject("BOUND_MO");

		// Construct
		this.replayMockObjects();
		ManagedObjectMetaData<?> metaData = this.constructRawBoundManagedObjectMetaData(1, configuration)[0]
				.getRawBoundManagedObjectInstanceMetaData()[0].getManagedObjectMetaData();
		this.verifyMockObjects();

		// Ensure pre-load administration
		ManagedObjectAdministrationMetaData<?, ?, ?>[] admins = metaData.getPreLoadAdministration();
		assertEquals("Should be pre-load administration", 1, admins.length);
		assertEquals("Incorrect pre-load adminstration", "ADMIN",
				admins[0].getAdministrationMetaData().getAdministrationName());
		assertEquals("Should not be managed object extensions", 0, admins[0].getRequiredManagedObjects().length);
	}

	/**
	 * Ensure able to add pre-load {@link Administration} with administered
	 * {@link ManagedObject}.
	 */
	public void testPreLoadAdministrationWithManagedObject() {

		// Record
		DependencyMappingBuilderImpl<?> configuration = new DependencyMappingBuilderImpl<>("BOUND", "BOUND_MO");
		configuration.preLoadAdminister("ADMIN", Object.class, () -> null).administerManagedObject("ADMINISTERED_MO");
		this.rawOfficeMetaData.addRegisteredManagedObject("BOUND_MO");
		RawBoundManagedObjectInstanceMetaDataMockBuilder<?, ?> administered = this.rawOfficeMetaData
				.addScopeBoundManagedObject("ADMINISTERED_MO");
		administered.getRawBoundManagedObjectMetaData().getRawManagedObjectBuilder().getMetaDataBuilder()
				.addExtension(Object.class, (mo) -> mo);

		// Construct
		this.replayMockObjects();
		ManagedObjectMetaData<?> metaData = this.constructRawBoundManagedObjectMetaData(1, configuration)[0]
				.getRawBoundManagedObjectInstanceMetaData()[0].getManagedObjectMetaData();
		this.verifyMockObjects();

		// Ensure pre-load administration
		ManagedObjectAdministrationMetaData<?, ?, ?> admin = metaData.getPreLoadAdministration()[0];
		assertEquals("Incorrect pre-load adminstration", "ADMIN",
				admin.getAdministrationMetaData().getAdministrationName());
		ManagedObjectIndex[] required = admin.getRequiredManagedObjects();
		assertEquals("Should be managed object extensions", 1, admin.getRequiredManagedObjects().length);
		assertSame("Incorrect reuqired managed object",
				administered.getRawBoundManagedObjectMetaData().build().getManagedObjectIndex(), required[0]);
	}

	/**
	 * Ensure able to add pre-load {@link Administration} with administered
	 * {@link ManagedObject} that has a dependency.
	 */
	public void testPreLoadAdministrationWithManagedObjectAndDependency() {

		// Record
		DependencyMappingBuilderImpl<?> configuration = new DependencyMappingBuilderImpl<>("BOUND", "BOUND_MO");
		configuration.preLoadAdminister("ADMIN", Object.class, () -> null).administerManagedObject("ADMINISTERED_MO");
		this.rawOfficeMetaData.addRegisteredManagedObject("BOUND_MO");

		RawBoundManagedObjectInstanceMetaDataMockBuilder<?, ?> administered = this.rawOfficeMetaData
				.addScopeBoundManagedObject("ADMINISTERED_MO");
		ManagedObjectSourceMetaDataMockBuilder<?, ?> metaDataBuilder = administered.getRawBoundManagedObjectMetaData()
				.getRawManagedObjectBuilder().getMetaDataBuilder();
		metaDataBuilder.addExtension(Object.class, (mo) -> mo);
		metaDataBuilder.addDependency(Object.class, null);
		administered.getBuilder().mapDependency(0, "DEPENDENCY_MO");

		RawBoundManagedObjectInstanceMetaDataMockBuilder<?, ?> dependency = this.rawOfficeMetaData
				.addScopeBoundManagedObject("DEPENDENCY_MO");

		Map<String, RawBoundManagedObjectMetaData> dependencies = new HashMap<>();
		dependencies.put("DEPENDENCY_MO", dependency.getRawBoundManagedObjectMetaData().build());
		administered.build().loadDependencies(this.issues, dependencies);

		// Construct
		this.replayMockObjects();
		RawBoundManagedObjectMetaData[] bound = this.constructRawBoundManagedObjectMetaData(1, configuration);
		ManagedObjectMetaData<?> metaData = bound[0].getRawBoundManagedObjectInstanceMetaData()[0]
				.getManagedObjectMetaData();
		this.verifyMockObjects();

		// Ensure pre-load administration
		ManagedObjectAdministrationMetaData<?, ?, ?> admin = metaData.getPreLoadAdministration()[0];
		assertEquals("Incorrect pre-load adminstration", "ADMIN",
				admin.getAdministrationMetaData().getAdministrationName());
		ManagedObjectIndex[] required = admin.getRequiredManagedObjects();
		assertEquals("Should be managed object extension and its dependency", 2,
				admin.getRequiredManagedObjects().length);
		assertSame("Should have dependency",
				dependency.getRawBoundManagedObjectMetaData().build().getManagedObjectIndex(), required[0]);
		assertSame("Extension after the dependency",
				administered.getRawBoundManagedObjectMetaData().build().getManagedObjectIndex(), required[1]);
	}

	/**
	 * Constructs the {@link RawBoundManagedObjectMetaData} instances.
	 * 
	 * @param expectedNumberConstructed       Expected number of
	 *                                        {@link RawBoundManagedObjectMetaData}
	 *                                        instances to be constructed.
	 * @param boundManagedObjectConfiguration {@link ManagedObjectConfiguration}
	 *                                        instances.
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
	 * @param expectedNumberConstructed Expected number of
	 *                                  {@link RawBoundManagedObjectMetaData}
	 *                                  instances to be constructed.
	 * @param inputManagedObjects       {@link RawManagingOfficeMetaData} instances.
	 * @return Constructed {@link RawBoundManagedObjectMetaData}.
	 */
	private RawBoundManagedObjectMetaData[] constructRawBoundManagedObjectMetaData(int expectedNumberConstructed,
			RawManagingOfficeMetaData<?>... inputManagedObjects) {
		return this.constructRawBoundManagedObjectMetaData(expectedNumberConstructed, null, inputManagedObjects);
	}

	/**
	 * Constructs the {@link RawBoundManagedObjectMetaData} instances.
	 * 
	 * @param expectedNumberConstructed       Expected number of
	 *                                        {@link RawBoundManagedObjectMetaData}
	 *                                        instances to be constructed.
	 * @param boundManagedObjectConfiguration {@link ManagedObjectConfiguration}
	 *                                        instances.
	 * @param inputManagedObjects             {@link RawManagingOfficeMetaData}
	 *                                        instances.
	 * @return Constructed {@link RawBoundManagedObjectMetaData}.
	 */
	private RawBoundManagedObjectMetaData[] constructRawBoundManagedObjectMetaData(int expectedNumberConstructed,
			ManagedObjectConfiguration<?>[] boundManagedObjectConfiguration,
			RawManagingOfficeMetaData<?>[] inputManagedObjects) {

		// Obtain input managed object defaults
		Map<String, String> inputDefaults = (this.inputManagedObjectDefaults.size() == 0 ? null
				: this.inputManagedObjectDefaults);

		// Attempt to construct
		RawBoundManagedObjectMetaDataFactory factory = new RawBoundManagedObjectMetaDataFactory(
				this.assetManagerRegistry, this.rawOfficeMetaData.build().getManagedObjectMetaData(),
				this.rawOfficeMetaData.build().getGovernanceMetaData());
		RawBoundManagedObjectMetaData[] metaData = factory.constructBoundManagedObjectMetaData(
				boundManagedObjectConfiguration, this.managedObjectScope,
				this.rawOfficeMetaData.build().getOfficeScopeManagedObjects(), inputManagedObjects, inputDefaults,
				AssetType.OFFICE, OFFICE_NAME, 1, this.issues);

		// Ensure correct number constructed
		assertEquals("Incorrect number of bound managed objects", expectedNumberConstructed, metaData.length);

		// Load the remaining state
		OfficeMetaData officeMetaData = this.officeMetaData.build();
		FlowMetaDataFactory flowFactory = new FlowMetaDataFactory(officeMetaData);
		EscalationFlowFactory escalationFactory = new EscalationFlowFactory(officeMetaData);
		RawAdministrationMetaDataFactory rawAdminFactory = new RawAdministrationMetaDataFactory(officeMetaData,
				flowFactory, escalationFactory, new HashMap<>());
		ManagedObjectAdministrationMetaDataFactory moAdminFactory = new ManagedObjectAdministrationMetaDataFactory(
				rawAdminFactory, new HashMap<>(), this.rawOfficeMetaData.build().getOfficeScopeManagedObjects());
		for (RawBoundManagedObjectMetaData bound : metaData) {
			for (RawBoundManagedObjectInstanceMetaData<?> instance : bound.getRawBoundManagedObjectInstanceMetaData()) {
				if (instance.getManagedObjectMetaData() != null) {
					instance.loadRemainingState(officeMetaData, null, null, moAdminFactory,
							new AssetManagerRegistry(null, null), 1, this.issues);
				}
			}
		}

		// Return the meta-data
		return metaData;
	}

}
