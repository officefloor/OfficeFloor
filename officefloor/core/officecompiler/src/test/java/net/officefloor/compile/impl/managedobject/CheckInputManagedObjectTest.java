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
package net.officefloor.compile.impl.managedobject;

import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.managedobject.ManagedObjectDependencyType;
import net.officefloor.compile.managedobject.ManagedObjectFlowType;
import net.officefloor.compile.managedobject.ManagedObjectLoader;
import net.officefloor.compile.managedobject.ManagedObjectTeamType;
import net.officefloor.compile.managedobject.ManagedObjectType;
import net.officefloor.compile.spi.officefloor.OfficeFloorInputManagedObject;
import net.officefloor.frame.api.execute.ManagedFunction;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.test.OfficeFrameTestCase;

/**
 * <p>
 * Tests the {@link ManagedObjectLoader} to determine if
 * {@link ManagedObjectType} is to represent a
 * {@link OfficeFloorInputManagedObject}.
 * <p>
 * This is added to the {@link ManagedObjectLoader} to keep the logic central
 * and also allow clearer understanding of the purpose of an
 * {@link OfficeFloorInputManagedObject}.
 * 
 * @author Daniel Sagenschneider
 */
public class CheckInputManagedObjectTest extends OfficeFrameTestCase {

	/**
	 * Mock {@link ManagedObjectType}.
	 */
	private final ManagedObjectType<?> managedObjectType = this
			.createMock(ManagedObjectType.class);

	/**
	 * Mock {@link ManagedObjectFlowType}.
	 */
	private final ManagedObjectFlowType<?> flowType = this
			.createMock(ManagedObjectFlowType.class);

	/**
	 * {@link ManagedObjectLoader}.
	 */
	private final ManagedObjectLoader loader = OfficeFloorCompiler
			.newOfficeFloorCompiler(null).getManagedObjectLoader();

	/**
	 * Validates that determine not an {@link OfficeFloorInputManagedObject}.
	 */
	public void testNotInputManagedObject() {

		// Record checking if input managed object
		this.recordReturn(this.managedObjectType,
				this.managedObjectType.getFlowTypes(),
				new ManagedObjectFlowType<?>[0]);
		this.recordReturn(this.managedObjectType,
				this.managedObjectType.getTeamTypes(),
				new ManagedObjectTeamType[0]);

		// Test
		this.replayMockObjects();
		boolean isInput = this.loader
				.isInputManagedObject(this.managedObjectType);
		this.verifyMockObjects();

		// Verify that not input
		assertFalse("Should NOT be input managed object", isInput);
	}

	/**
	 * Validates that determine is {@link OfficeFloorInputManagedObject} as has
	 * {@link ManagedObjectFlowType} instances requiring the
	 * {@link ManagedObject} binding to possibly be shared.
	 */
	public void testInputManagedObjectByFlows() {

		// Record checking if input managed object
		this.recordReturn(this.managedObjectType,
				this.managedObjectType.getFlowTypes(),
				new ManagedObjectFlowType<?>[] { this.flowType });

		// Test
		this.replayMockObjects();
		boolean isInput = this.loader
				.isInputManagedObject(this.managedObjectType);
		this.verifyMockObjects();

		// Verify that input
		assertTrue("Should NOT be input managed object", isInput);
	}

	/**
	 * Validates that determine is {@link OfficeFloorInputManagedObject} as
	 * {@link ManagedObjectDependencyType} instances that need configuring as it
	 * has private {@link ManagedFunction} instances for the {@link ManagedObjectSource}
	 * due to having {@link ManagedObjectTeamType} instances.
	 */
	public void testInputManagedObjectByTeams() {

		final ManagedObjectTeamType teamType = this
				.createMock(ManagedObjectTeamType.class);
		final ManagedObjectDependencyType<?> dependencyType = this
				.createMock(ManagedObjectDependencyType.class);

		// Record checking if input managed object
		this.recordReturn(this.managedObjectType,
				this.managedObjectType.getFlowTypes(),
				new ManagedObjectFlowType<?>[0]);
		this.recordReturn(this.managedObjectType,
				this.managedObjectType.getTeamTypes(),
				new ManagedObjectTeamType[] { teamType });
		this.recordReturn(this.managedObjectType,
				this.managedObjectType.getDependencyTypes(),
				new ManagedObjectDependencyType<?>[] { dependencyType });

		// Test
		this.replayMockObjects();
		boolean isInput = this.loader
				.isInputManagedObject(this.managedObjectType);
		this.verifyMockObjects();

		// Verify that input
		assertTrue("Should be an input managed object", isInput);
	}

}