/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2011 Daniel Sagenschneider
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
package net.officefloor.frame.impl.execute.governance;

import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.execute.TaskContext;
import net.officefloor.frame.internal.structure.ActiveGovernance;
import net.officefloor.frame.internal.structure.ActiveGovernanceManager;
import net.officefloor.frame.internal.structure.ContainerContext;
import net.officefloor.frame.internal.structure.FlowMetaData;
import net.officefloor.frame.internal.structure.GovernanceContainer;
import net.officefloor.frame.internal.structure.GovernanceMetaData;
import net.officefloor.frame.internal.structure.ManagedObjectContainer;
import net.officefloor.frame.spi.governance.Governance;
import net.officefloor.frame.spi.governance.GovernanceContext;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.test.OfficeFrameTestCase;

/**
 * Tests the {@link GovernanceContainer}.
 * 
 * @author Daniel Sagenschneider
 */
public class GovernanceContainerTest extends OfficeFrameTestCase {

	/**
	 * {@link GovernanceMetaData}.
	 */
	@SuppressWarnings("unchecked")
	private final GovernanceMetaData<MockExtensionInterface, Indexed> metaData = this
			.createMock(GovernanceMetaData.class);

	/**
	 * {@link Governance}.
	 */
	@SuppressWarnings("unchecked")
	private final Governance<MockExtensionInterface, Indexed> governance = this
			.createMock(Governance.class);

	/**
	 * {@link GovernanceContainer} to test.
	 */
	private final GovernanceContainerImpl<MockExtensionInterface, Indexed> container = new GovernanceContainerImpl<MockExtensionInterface, Indexed>(
			this.metaData, "LOCK");

	/**
	 * {@link ContainerContext}.
	 */
	private final ContainerContext containerContext = this
			.createMock(ContainerContext.class);

	/**
	 * {@link GovernanceContext}.
	 */
	@SuppressWarnings("unchecked")
	private final GovernanceContext<Indexed> governanceContext = this
			.createMock(GovernanceContext.class);

	/**
	 * {@link TaskContext}.
	 */
	@SuppressWarnings("unchecked")
	private final TaskContext<?, ?, Indexed> taskContext = this
			.createMock(TaskContext.class);

	/**
	 * Ensure activate {@link Governance}.
	 */
	public void testActivateGovernance() throws Throwable {

		final FlowMetaData<?> activateFlowMetaData = this
				.createMock(FlowMetaData.class);

		// Record triggering activating governance
		this.recordReturn(this.metaData,
				this.metaData.getActivateFlowMetaData(), activateFlowMetaData);
		this.containerContext.addSetupJob(activateFlowMetaData, this.container);

		// Record activating governance
		this.record_activateGovernance();

		// Test
		this.replayMockObjects();

		// Trigger governance activation
		this.container.activateGovernance(this.containerContext);
		assertFalse("Should not be activate until activate job executed",
				this.container.isActive());

		// Undertake the activation
		this.container.activateGovernance(this.taskContext);
		assertTrue("Ensure now active", this.container.isActive());

		this.verifyMockObjects();
	}

	/**
	 * Ensure enforce {@link Governance}.
	 */
	public void testEnforceGovernance() throws Throwable {

		final FlowMetaData<?> enforceFlowMetaData = this
				.createMock(FlowMetaData.class);

		// Record activating governance
		this.record_activateGovernance();

		// Record triggering enforcing governance
		this.recordReturn(this.metaData,
				this.metaData.getEnforceFlowMetaData(), enforceFlowMetaData);
		this.containerContext.addSetupJob(enforceFlowMetaData, this.container);

		// Record enforce governance
		this.recordReturn(this.metaData,
				this.metaData.createGovernanceContext(this.taskContext),
				this.governanceContext);
		this.governance.enforceGovernance(this.governanceContext);

		// Test
		this.replayMockObjects();

		// Ensure governance is active
		this.container.activateGovernance(this.taskContext);
		assertTrue("Governance should be active", this.container.isActive());

		// Trigger enforcing the governance
		this.container.enforceGovernance(this.containerContext);

		// Undertake the enforcing
		this.container.enforceGovernance(this.taskContext);
		assertFalse("Ensure no longer active", this.container.isActive());

		this.verifyMockObjects();
	}

	/**
	 * Ensure enforce {@link Governance} with {@link ManagedObject}.
	 */
	public void testEnforceGovernanceWithManagedObject() throws Throwable {

		final MockExtensionInterface extension = this
				.createMock(MockExtensionInterface.class);
		final ManagedObjectContainer managedObject = this
				.createMock(ManagedObjectContainer.class);

		// Record activating governance
		this.record_activateGovernance();

		// Record governing the managed object
		ActiveGovernanceManager manager = this.record_createActiveGovernance(
				extension, managedObject);

		// Record enforce governance
		this.recordReturn(this.metaData,
				this.metaData.createGovernanceContext(this.taskContext),
				this.governanceContext);
		this.governance.enforceGovernance(this.governanceContext);

		// Record unregistering managed object
		manager.unregisterManagedObject();

		// Test
		this.replayMockObjects();

		// Ensure governance is active
		this.container.activateGovernance(this.taskContext);
		assertTrue("Governance should be active", this.container.isActive());

		// Create the active governance
		ActiveGovernance activeGovernance = this.container
				.createActiveGovernance(extension, managedObject);
		assertTrue("Should be active", activeGovernance.isActive());

		// Undertake the enforcing
		this.container.enforceGovernance(this.taskContext);
		assertFalse("Ensure governance no longer active",
				this.container.isActive());
		assertFalse("Ensure active governance no longer active",
				activeGovernance.isActive());

		this.verifyMockObjects();
	}

	/**
	 * Ensure disregard {@link Governance}.
	 */
	public void testDisregardGovernance() throws Throwable {

		final FlowMetaData<?> disregardFlowMetaData = this
				.createMock(FlowMetaData.class);

		// Record activating governance
		this.record_activateGovernance();

		// Record triggering disregarding governance
		this.recordReturn(this.metaData,
				this.metaData.getDisregardFlowMetaData(), disregardFlowMetaData);
		this.containerContext
				.addSetupJob(disregardFlowMetaData, this.container);

		// Record disregard governance
		this.recordReturn(this.metaData,
				this.metaData.createGovernanceContext(this.taskContext),
				this.governanceContext);
		this.governance.disregardGovernance(this.governanceContext);

		// Test
		this.replayMockObjects();

		// Ensure governance is active
		this.container.activateGovernance(this.taskContext);
		assertTrue("Governance should be active", this.container.isActive());

		// Trigger disregarding the governance
		this.container.disregardGovernance(this.containerContext);

		// Undertake the disregard
		this.container.disregardGovernance(this.taskContext);
		assertFalse("Ensure no longer active", this.container.isActive());

		this.verifyMockObjects();
	}

	/**
	 * Ensure disregard {@link Governance} with {@link ManagedObject}.
	 */
	public void testDisregardGovernanceWithManagedObject() throws Throwable {

		final MockExtensionInterface extension = this
				.createMock(MockExtensionInterface.class);
		final ManagedObjectContainer managedObject = this
				.createMock(ManagedObjectContainer.class);

		// Record activating governance
		this.record_activateGovernance();

		// Record governing the managed object
		ActiveGovernanceManager manager = this.record_createActiveGovernance(
				extension, managedObject);

		// Record disregard governance
		this.recordReturn(this.metaData,
				this.metaData.createGovernanceContext(this.taskContext),
				this.governanceContext);
		this.governance.disregardGovernance(this.governanceContext);

		// Record unregistering managed object
		manager.unregisterManagedObject();

		// Test
		this.replayMockObjects();

		// Ensure governance is active
		this.container.activateGovernance(this.taskContext);
		assertTrue("Governance should be active", this.container.isActive());

		// Create the active governance
		ActiveGovernance activeGovernance = this.container
				.createActiveGovernance(extension, managedObject);
		assertTrue("Should be active", activeGovernance.isActive());

		// Undertake the disregarding
		this.container.disregardGovernance(this.taskContext);
		assertFalse("Ensure governance no longer active",
				this.container.isActive());
		assertFalse("Ensure active governance no longer active",
				activeGovernance.isActive());

		this.verifyMockObjects();
	}

	/**
	 * Ensure not applies {@link Governance} if not active.
	 */
	public void testNotActiveGovernance() throws Exception {

		final MockExtensionInterface extension = this
				.createMock(MockExtensionInterface.class);
		final ManagedObjectContainer managedObject = this
				.createMock(ManagedObjectContainer.class);

		// Test
		this.replayMockObjects();

		// By default not active
		assertFalse("Ensure governance as not active",
				this.container.isActive());

		// Ensure not able to create active governance unless active
		try {
			this.container.createActiveGovernance(extension, managedObject);
			fail("Should not be successful");
		} catch (IllegalStateException ex) {
			assertEquals("Incorrect cause",
					"Can only create ActiveGovernance for active Governance",
					ex.getMessage());
		}

		this.verifyMockObjects();
	}

	/**
	 * Ensure apply {@link Governance} across multiple {@link ManagedObject}
	 * instances.
	 */
	public void testGovernMultipleManagedObjects() throws Throwable {

		// Record activating governance
		this.record_activateGovernance();

		// Record governing multiple managed object
		final int MANAGED_OBJECT_COUNT = 5;
		MockExtensionInterface[] extensions = new MockExtensionInterface[MANAGED_OBJECT_COUNT];
		ManagedObjectContainer[] managedObjects = new ManagedObjectContainer[MANAGED_OBJECT_COUNT];
		ActiveGovernanceManager[] managers = new ActiveGovernanceManager[MANAGED_OBJECT_COUNT];
		for (int i = 0; i < managers.length; i++) {
			extensions[i] = this.createMock(MockExtensionInterface.class);
			managedObjects[i] = this.createMock(ManagedObjectContainer.class);
			managers[i] = this.record_createActiveGovernance(extensions[i],
					managedObjects[i]);
		}

		// Record enforce governance
		this.recordReturn(this.metaData,
				this.metaData.createGovernanceContext(this.taskContext),
				this.governanceContext);
		this.governance.enforceGovernance(this.governanceContext);

		// Record unregistering managed object
		for (int i = 0; i < managers.length; i++) {
			managers[i].unregisterManagedObject();
		}

		// Test
		this.replayMockObjects();

		// Ensure governance is active
		this.container.activateGovernance(this.taskContext);
		assertTrue("Governance should be active", this.container.isActive());

		// Create the active governances
		final ActiveGovernance[] activeGovernances = new ActiveGovernance[MANAGED_OBJECT_COUNT];
		for (int i = 0; i < managers.length; i++) {
			activeGovernances[i] = this.container.createActiveGovernance(
					extensions[i], managedObjects[i]);
			assertTrue("Should be active", activeGovernances[i].isActive());
		}

		// Undertake the enforcing
		this.container.enforceGovernance(this.taskContext);
		assertFalse("Ensure no longer active", this.container.isActive());

		this.verifyMockObjects();
	}

	/**
	 * Record activating the {@link Governance}.
	 */
	private void record_activateGovernance() {
		try {
			this.recordReturn(this.metaData, this.metaData.createGovernance(),
					this.governance);
		} catch (Throwable ex) {
			throw fail(ex);
		}
	}

	/**
	 * Records the creating the {@link ActiveGovernance}.
	 */
	private ActiveGovernanceManager record_createActiveGovernance(
			MockExtensionInterface extension,
			ManagedObjectContainer managedObject) {

		final ActiveGovernanceManager activeGovernanceManager = this
				.createMock(ActiveGovernanceManager.class);

		final ActiveGovernance activeGovernance = new ActiveGovernanceImpl<MockExtensionInterface, Indexed>(
				this.container, this.metaData, this.governance, extension,
				managedObject);

		// Record creating the active governance
		this.recordReturn(this.metaData, this.metaData.createActiveGovernance(
				this.container, this.governance, extension, managedObject),
				activeGovernanceManager);
		this.recordReturn(activeGovernanceManager,
				activeGovernanceManager.getActiveGovernance(), activeGovernance);

		// Return the active governance manager
		return activeGovernanceManager;
	}

	/**
	 * Extension interface for testing.
	 */
	private static interface MockExtensionInterface {
	}

}