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
import net.officefloor.frame.internal.structure.ActiveGovernance;
import net.officefloor.frame.internal.structure.GovernanceContainer;
import net.officefloor.frame.internal.structure.GovernanceMetaData;
import net.officefloor.frame.internal.structure.ManagedObjectContainer;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.frame.spi.administration.GovernanceManager;
import net.officefloor.frame.spi.governance.Governance;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.frame.test.match.TypeMatcher;

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
	private final Governance<MockExtensionInterface> governance = this
			.createMock(Governance.class);

	/**
	 * {@link GovernanceContainer} to test.
	 */
	private final GovernanceContainerImpl<MockExtensionInterface, Indexed> container = new GovernanceContainerImpl<MockExtensionInterface, Indexed>(
			this.metaData, "LOCK");

	/**
	 * {@link MockExtensionInterface}.
	 */
	private final MockExtensionInterface extension = this
			.createMock(MockExtensionInterface.class);

	/**
	 * {@link ManagedObjectContainer}.
	 */
	private final ManagedObjectContainer managedObject = this
			.createMock(ManagedObjectContainer.class);

	/**
	 * Ensure not applies {@link Governance} if not active.
	 */
	public void testNotActiveGovernance() throws Exception {

		// Test
		this.replayMockObjects();

		// Govern managed object
		ActiveGovernance active = this.container.governManagedObject(
				this.extension, this.managedObject);
		assertNull("Ensure no active governance as not active", active);
		this.verifyMockObjects();
	}

	/**
	 * Ensure disregards {@link Governance} at end of {@link ProcessState}.
	 */
	public void testEndOfProcessDisregardGovernance() throws Exception {

		// Record activating governance
		this.recordReturn(this.metaData,
				this.metaData.createGovernance(this.container), this.governance);
		this.governance.startGovernance();

		// Record governing extension
		this.governance.governManagedObject(this.extension);

		// Record disregard governance
		this.governance.stopGovernance();
		this.managedObject.unregisterManagedObjectFromGovernance(null);
		this.control(this.managedObject).setMatcher(
				new TypeMatcher(ActiveGovernance.class));

		// Test
		this.replayMockObjects();

		// Ensure governance is active
		GovernanceManager manager = this.container.getGovernanceManager();
		manager.activateGovernance();

		// Govern managed object
		ActiveGovernance active = this.container.governManagedObject(
				this.extension, this.managedObject);
		assertTrue("Ensure active", active.isActive());

		// Disregard the governance (at end of process and not applied)
		this.container.disregardGovernance();
		assertFalse("Ensure no longer active", active.isActive());
		this.verifyMockObjects();
	}

	/**
	 * Ensure enforce {@link Governance}.
	 */
	public void testEnforceGovernance() throws Exception {

		// Record activating governance
		this.recordReturn(this.metaData,
				this.metaData.createGovernance(this.container), this.governance);
		this.governance.startGovernance();

		// Record governing extension
		this.governance.governManagedObject(this.extension);

		// Record enforce governance
		this.governance.applyGovernance();

		// Record stopping governance
		this.governance.stopGovernance();
		this.managedObject.unregisterManagedObjectFromGovernance(null);
		this.control(this.managedObject).setMatcher(
				new TypeMatcher(ActiveGovernance.class));

		// Test
		this.replayMockObjects();

		// Ensure governance is active
		GovernanceManager manager = this.container.getGovernanceManager();
		manager.activateGovernance();

		// Govern managed object
		ActiveGovernance active = this.container.governManagedObject(
				this.extension, this.managedObject);
		assertTrue("Ensure active", active.isActive());

		// Enforce the governance
		manager.enforceGovernance();
		assertFalse("Ensure no longer active", active.isActive());
		this.verifyMockObjects();
	}

	/**
	 * Ensure disregard {@link Governance}.
	 */
	public void testDisregardGovernance() throws Exception {

		// Record activating governance
		this.recordReturn(this.metaData,
				this.metaData.createGovernance(this.container), this.governance);
		this.governance.startGovernance();

		// Record governing extension
		this.governance.governManagedObject(this.extension);

		// Record disregard governance
		this.governance.stopGovernance();
		this.managedObject.unregisterManagedObjectFromGovernance(null);
		this.control(this.managedObject).setMatcher(
				new TypeMatcher(ActiveGovernance.class));

		// Test
		this.replayMockObjects();

		// Ensure governance is active
		GovernanceManager manager = this.container.getGovernanceManager();
		manager.activateGovernance();

		// Govern managed object
		ActiveGovernance active = this.container.governManagedObject(
				this.extension, this.managedObject);
		assertTrue("Ensure active", active.isActive());

		// Disregard the governance
		manager.disregardGovernance();
		assertFalse("Ensure no longer active", active.isActive());
		this.verifyMockObjects();
	}

	/**
	 * Ensure apply {@link Governance} across multiple {@link ManagedObject}
	 * instances.
	 */
	public void testGovernMultipleManagedObjects() throws Exception {

		final MockExtensionInterface anotherExtension = this
				.createMock(MockExtensionInterface.class);
		final ManagedObjectContainer anotherManagedObject = this
				.createMock(ManagedObjectContainer.class);

		// Record activating governance
		this.recordReturn(this.metaData,
				this.metaData.createGovernance(this.container), this.governance);
		this.governance.startGovernance();

		// Record governing multiple managed objects
		this.governance.governManagedObject(this.extension);
		this.governance.governManagedObject(anotherExtension);

		// Record enforce governance
		this.governance.applyGovernance();

		// Record stopping governance
		this.governance.stopGovernance();
		this.managedObject.unregisterManagedObjectFromGovernance(null);
		this.control(this.managedObject).setMatcher(
				new TypeMatcher(ActiveGovernance.class));
		anotherManagedObject.unregisterManagedObjectFromGovernance(null);
		this.control(anotherManagedObject).setMatcher(
				new TypeMatcher(ActiveGovernance.class));

		// Test
		this.replayMockObjects();

		// Ensure governance is active
		GovernanceManager manager = this.container.getGovernanceManager();
		manager.activateGovernance();

		// Govern managed object
		ActiveGovernance active = this.container.governManagedObject(
				this.extension, this.managedObject);
		assertTrue("Ensure active", active.isActive());

		// Govern another managed object
		ActiveGovernance anotherActive = this.container.governManagedObject(
				anotherExtension, anotherManagedObject);
		assertTrue("Ensure another active", anotherActive.isActive());

		// Enforce the governance
		manager.enforceGovernance();
		assertFalse("Ensure no longer active", active.isActive());
		assertFalse("Ensure another no longer active", anotherActive.isActive());
		this.verifyMockObjects();
	}

	/**
	 * Ensure apply {@link Governance} across no {@link ManagedObject}
	 * instances.
	 */
	public void testGovernNoManagedObjects() throws Exception {

		// Record activating governance
		this.recordReturn(this.metaData,
				this.metaData.createGovernance(this.container), this.governance);
		this.governance.startGovernance();

		// Record enforce governance
		this.governance.applyGovernance();

		// Record stopping governance
		this.governance.stopGovernance();

		// Test
		this.replayMockObjects();

		// Ensure governance is active
		GovernanceManager manager = this.container.getGovernanceManager();
		manager.activateGovernance();

		// Enforce the governance
		manager.enforceGovernance();
		this.verifyMockObjects();
	}

	/**
	 * Extension interface for testing.
	 */
	private static interface MockExtensionInterface {
	}

}