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
package net.officefloor.frame.impl.execute.work;

import net.officefloor.frame.api.execute.ManagedFunction;
import net.officefloor.frame.internal.structure.AdministratorIndex;
import net.officefloor.frame.internal.structure.AdministratorScope;
import net.officefloor.frame.internal.structure.ManagedObjectIndex;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.internal.structure.WorkContainer;

/**
 * Tests life cycle of the {@link WorkContainer}.
 *
 * @author Daniel Sagenschneider
 */
public class LifeCycleWorkContainerTest extends AbstractWorkContainerTest {

	/**
	 * Tests the full life cycle of the {@link WorkContainer}.
	 */
	public void testLifeCycle() throws Throwable {

		// Add managed object of each scope
		final ManagedObjectIndex moWork = this
				.addManagedObjectIndex(ManagedObjectScope.WORK);
		final ManagedObjectIndex moThread = this
				.addManagedObjectIndex(ManagedObjectScope.THREAD);
		final ManagedObjectIndex moProcess = this
				.addManagedObjectIndex(ManagedObjectScope.PROCESS);

		// Add administrator of each scope
		final AdministratorIndex adminWork = this
				.addAdministratorIndex(AdministratorScope.WORK);
		final AdministratorIndex adminThread = this
				.addAdministratorIndex(AdministratorScope.THREAD);
		final AdministratorIndex adminProcess = this
				.addAdministratorIndex(AdministratorScope.PROCESS);

		// Record creating and loading the work container
		this.record_WorkContainer_init();
		this.record_WorkContainer_loadManagedObjects(moWork, moThread,
				moProcess);
		this.record_WorkContainer_coordinateManagedObjects(moWork, moThread,
				moProcess);
		this.record_WorkContainer_isManagedObjectsReady(moWork, moThread,
				moProcess);

		// Record pre-administration of the work container
		this.record_WorkContainer_administerManagedObjects(adminWork, moWork,
				moThread, moProcess);
		this.record_WorkContainer_administerManagedObjects(adminThread,
				moThread, moProcess);
		this.record_WorkContainer_administerManagedObjects(adminProcess,
				moProcess);

		// Record doing work with the managed objects
		this.record_WorkContainer_getObject(moWork, "WORK");
		this.record_WorkContainer_getObject(moThread, "THREAD");
		this.record_WorkContainer_getObject(moProcess, "PROCESS");

		// Record post-administration of the work container
		this.record_WorkContainer_administerManagedObjects(adminProcess,
				moProcess);
		this.record_WorkContainer_administerManagedObjects(adminThread,
				moThread, moProcess);
		this.record_WorkContainer_administerManagedObjects(adminWork, moWork,
				moThread, moProcess);

		// Record unloading the work container
		this.record_WorkContainer_unloadWork();

		// Replay mocks
		this.replayMockObjects();

		// Create the work container
		WorkContainer<?> work = this.createWorkContainer();

		// Should fail to obtain managed object container before loaded
		this.loadManagedObjects(work, moWork, moThread, moProcess);
		this.coordinateManagedObject(work, moWork, moThread, moProcess);
		this.isManagedObjectsReady(work, true, moWork, moThread, moProcess);

		// Pre-administer the managed objects
		this.administerManagedObjects(work);
		this.administerManagedObjects(work);
		this.administerManagedObjects(work);

		// Do work with the managed objects
		this.getObject(work, moWork, "WORK");
		this.getObject(work, moThread, "THREAD");
		this.getObject(work, moProcess, "PROCESS");

		// Post-administer the managed objects
		this.administerManagedObjects(work);
		this.administerManagedObjects(work);
		this.administerManagedObjects(work);

		// Unload the work
		this.unloadWork(work);

		// Verify mocks
		this.verifyMockObjects();
	}

	/**
	 * Tests the full life cycle of the {@link WorkContainer} when re-used by
	 * different {@link ManagedFunction}.
	 */
	public void testReuse() throws Throwable {

		// Add managed object of each scope
		ManagedObjectIndex moOne = this
				.addManagedObjectIndex(ManagedObjectScope.WORK);
		ManagedObjectIndex moTwo = this
				.addManagedObjectIndex(ManagedObjectScope.THREAD);
		ManagedObjectIndex moThree = this
				.addManagedObjectIndex(ManagedObjectScope.PROCESS);
		ManagedObjectIndex moFour = this
				.addManagedObjectIndex(ManagedObjectScope.WORK);

		// Tasks use separate managed objects except one which is re-used and of
		// work scope.
		final ManagedObjectIndex[] taskOneManagedObjects = new ManagedObjectIndex[] {
				moOne, moThree, moFour };
		final ManagedObjectIndex[] taskTwoManagedObjects = new ManagedObjectIndex[] {
				moTwo, moFour };

		// Add an unused managed object (for a task three)
		this.addManagedObjectIndex(ManagedObjectScope.WORK);

		// Add administrator for each task
		final AdministratorIndex taskOneAdmin = this
				.addAdministratorIndex(AdministratorScope.WORK);
		final AdministratorIndex taskTwoAdmin = this
				.addAdministratorIndex(AdministratorScope.WORK);

		// Add an unused administrator (for a task three)
		this.addAdministratorIndex(AdministratorScope.WORK);

		// Task one initialises the container and its managed objects
		this.record_WorkContainer_init();
		this.record_WorkContainer_loadManagedObjects(taskOneManagedObjects);
		this
				.record_WorkContainer_coordinateManagedObjects(taskOneManagedObjects);
		this.record_WorkContainer_isManagedObjectsReady(taskOneManagedObjects);

		// Task one does pre-administration
		this.record_WorkContainer_administerManagedObjects(taskOneAdmin,
				moThree);

		// Task two initialises its managed objects
		this.record_WorkContainer_loadManagedObjects(taskTwoManagedObjects);
		this
				.record_WorkContainer_coordinateManagedObjects(taskTwoManagedObjects);
		this.record_WorkContainer_isManagedObjectsReady(taskTwoManagedObjects);

		// Task one does work with the managed objects. Two is only dependency
		this.record_WorkContainer_getObject(moOne, "ONE");
		this.record_WorkContainer_getObject(moFour, "FOUR");

		// Task two does pre-administration
		this.record_WorkContainer_administerManagedObjects(taskTwoAdmin, moTwo);

		// Task one does post-administration
		this.record_WorkContainer_administerManagedObjects(taskOneAdmin,
				moThree);

		// Task two does work with the managed objects
		this.record_WorkContainer_getObject(moTwo, "TWO");
		this.record_WorkContainer_getObject(moFour, "FOUR");

		// Task two does post-administration
		this.record_WorkContainer_administerManagedObjects(taskTwoAdmin, moTwo);

		// Tasks complete so unload work container
		this.record_WorkContainer_unloadWork();

		// Replay mocks
		this.replayMockObjects();

		// Create the work container
		WorkContainer<?> work = this.createWorkContainer();

		// Task one initialising its managed objects
		this.loadManagedObjects(work, taskOneManagedObjects);
		this.coordinateManagedObject(work, taskOneManagedObjects);
		this.isManagedObjectsReady(work, true, taskOneManagedObjects);

		// Task one pre-administration
		this.administerManagedObjects(work);

		// Task two initialising its managed objects
		this.loadManagedObjects(work, taskTwoManagedObjects);
		this.coordinateManagedObject(work, taskTwoManagedObjects);
		this.isManagedObjectsReady(work, true, taskTwoManagedObjects);

		// Task one does work with managed objects
		this.getObject(work, moOne, "ONE");
		this.getObject(work, moFour, "FOUR");

		// Task two does pre-administration
		this.administerManagedObjects(work);

		// Task one does post-administration
		this.administerManagedObjects(work);

		// Task two does work with managed objects
		this.getObject(work, moTwo, "TWO");
		this.getObject(work, moFour, "FOUR");

		// Task two does post-administration
		this.administerManagedObjects(work);

		// Unload the work
		this.unloadWork(work);

		// Verify mocks
		this.verifyMockObjects();
	}

}