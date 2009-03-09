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
import net.officefloor.frame.impl.construct.managedobjectsource.RawManagedObjectMetaData;
import net.officefloor.frame.internal.configuration.ManagedObjectConfiguration;
import net.officefloor.frame.internal.configuration.ManagedObjectDependencyConfiguration;
import net.officefloor.frame.internal.structure.Asset;
import net.officefloor.frame.internal.structure.ManagedObjectMetaData;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceMetaData;
import net.officefloor.frame.test.OfficeFrameTestCase;

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
	private ManagedObjectScope managedObjectScope = ManagedObjectScope.PROCESS;

	/**
	 * {@link AssetType}.
	 */
	private AssetType assetType = AssetType.OFFICE;

	/**
	 * Name of the {@link Asset}.
	 */
	private String assetName = "OFFICE";

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

		// Construct and obtain the managed object meta-data
		this.replayMockObjects();
		RawBoundManagedObjectMetaData<?>[] rawBoundMoMetaData = this
				.constructRawBoundManagedObjectMetaData(1,
						this.managedObjectConfiguration);
		ManagedObjectMetaData<?> moMetaData = rawBoundMoMetaData[0]
				.getManagedObjectMetaData();
		this.verifyMockObjects();

		// Verify the managed object meta-data contents
		assertEquals("Incorrect managed object source", null, moMetaData
				.getManagedObjectSource());
		assertEquals("Incorrect managed object pool", null, moMetaData
				.getManagedObjectPool());
		assertEquals("Incorrect operations manager", null, moMetaData
				.getOperationsManager());
		assertEquals("Incorrect sourcing manager", null, moMetaData
				.getSourcingManager());
		assertEquals("Incorrect timeout", -1, moMetaData.getTimeout());
		assertFalse("Not asynchronous", moMetaData
				.isManagedObjectAsynchronous());
		assertFalse("Not coordinating", moMetaData
				.isCoordinatingManagedObject());

		// Verify managed object meta-data

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

		// Managed object configuration
		ManagedObjectConfiguration<?> oneConfig = this
				.createMock(ManagedObjectConfiguration.class);
		RawManagedObjectMetaData<?, ?> oneMetaData = this
				.registerRawManagedObjectMetaData("ONE");
		ManagedObjectConfiguration<?> twoConfig = this
				.createMock(ManagedObjectConfiguration.class);
		RawManagedObjectMetaData<?, ?> twoMetaData = this
				.registerRawManagedObjectMetaData("TWO");

		ManagedObjectDependencyConfiguration<?> dependencyConfig = this
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

		// Attempt to construct
		this.replayMockObjects();
		RawBoundManagedObjectMetaData<?>[] metaData = this
				.constructRawBoundManagedObjectMetaData(2, oneConfig, twoConfig);
		this.verifyMockObjects();

		// Validate dependency mapping
		RawBoundManagedObjectMetaData<DependencyKey> one = (RawBoundManagedObjectMetaData<DependencyKey>) metaData[0];
		assertEquals("Incorrect number of dependency keys", 1, one
				.getDependencyKeys().length);
		assertEquals("Incorrect dependency key", DependencyKey.KEY, one
				.getDependencyKeys()[0]);
		RawBoundManagedObjectMetaData<?> two = metaData[1];
		assertEquals("Incorrect dependency", two, one
				.getDependency(DependencyKey.KEY));
	}

	/**
	 * Ensure can construct {@link RawBoundManagedObjectMetaData} with
	 * dependency on a {@link RawBoundManagedObjectMetaData} from scope.
	 */
	@SuppressWarnings("unchecked")
	public void testDependencyOnScopeBound() {

		// Managed object configuration
		RawManagedObjectMetaData<?, ?> rawMetaData = this
				.registerRawManagedObjectMetaData("OFFICE_MO");
		RawBoundManagedObjectMetaData<?> scopeMetaData = this
				.scopeRawManagedObjectMetaData("SCOPE");

		ManagedObjectDependencyConfiguration<?> dependencyConfig = this
				.createMock(ManagedObjectDependencyConfiguration.class);

		// Record construction
		this.recordReturn(this.managedObjectConfiguration,
				this.managedObjectConfiguration.getBoundManagedObjectName(),
				"BOUND");
		this.recordReturn(this.managedObjectConfiguration,
				this.managedObjectConfiguration.getOfficeManagedObjectName(),
				"OFFICE_MO");
		this.recordReturn(rawMetaData, rawMetaData
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

		// Attempt to construct
		this.replayMockObjects();
		RawBoundManagedObjectMetaData<?>[] metaData = this
				.constructRawBoundManagedObjectMetaData(1,
						this.managedObjectConfiguration);
		this.verifyMockObjects();

		// Validate dependency mapping
		RawBoundManagedObjectMetaData<DependencyKey> dependency = (RawBoundManagedObjectMetaData<DependencyKey>) metaData[0];
		assertEquals("Incorrect number of dependency keys", 1, dependency
				.getDependencyKeys().length);
		assertEquals("Incorrect dependency key", DependencyKey.KEY, dependency
				.getDependencyKeys()[0]);
		assertEquals("Incorrect dependency", scopeMetaData, dependency
				.getDependency(DependencyKey.KEY));
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
				.getFactory().constructBoundManagedObjectMetaData(
						boundManagedObjectConfiguration, this.issues,
						this.managedObjectScope, this.assetType,
						this.assetName, this.registeredManagedObjects,
						this.scopeManagedObjects);

		// Ensure correct number constructed
		assertEquals("Incorrect number of bound managed objects",
				expectedNumberConstructed, metaData.length);

		// Return the meta-data
		return metaData;
	}
}