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
package net.officefloor.frame.impl.execute.work;

import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.internal.structure.AdministratorScope;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.internal.structure.WorkContainer;

/**
 * Tests life cycle of the {@link WorkContainer}.
 * 
 * @author Daniel
 */
public class LifeCycleWorkContainerTest extends AbstractWorkContainerTest {

	/**
	 * Tests the full life cycle of the {@link WorkContainer}.
	 */
	public void testLifeCycle() throws Throwable {

		// Add managed object of each scope
		this.addManagedObjectIndex(ManagedObjectScope.WORK);
		this.addManagedObjectIndex(ManagedObjectScope.THREAD);
		this.addManagedObjectIndex(ManagedObjectScope.PROCESS);

		// Add administrator of each scope
		this.addAdministratorIndex(AdministratorScope.WORK);
		this.addAdministratorIndex(AdministratorScope.THREAD);
		this.addAdministratorIndex(AdministratorScope.PROCESS);

		// Record creating and loading the work container
		this.record_WorkContainer_init();
		this.record_WorkContainer_loadManagedObjects(0, 1, 2);
		this.record_WorkContainer_coordinateManagedObjects(0, 1, 2);
		this.record_WorkContainer_isManagedObjectsReady(0, 1, 2);

		// Record pre-administration of the work container
		this.record_WorkContainer_administerManagedObjects(0, 0, 1, 2);
		this.record_WorkContainer_administerManagedObjects(1, 1, 2);
		this.record_WorkContainer_administerManagedObjects(2, 2);

		// Record doing work with the managed objects
		this.record_WorkContainer_getObject(0, "WORK");
		this.record_WorkContainer_getObject(1, "THREAD");
		this.record_WorkContainer_getObject(2, "PROCESS");

		// Record post-administration of the work container
		this.record_WorkContainer_administerManagedObjects(2, 2);
		this.record_WorkContainer_administerManagedObjects(1, 1, 2);
		this.record_WorkContainer_administerManagedObjects(0, 0, 1, 2);

		// Record unloading the work container
		this.record_WorkContainer_unloadWork();

		// Replay mocks
		this.replayMockObjects();

		// Create the work container
		WorkContainer<?> work = this.createWorkContainer();
		this.loadManagedObjects(work, true, 0, 1, 2);
		this.coordinateManagedObject(work, 0, 1, 2);
		this.isManagedObjectsReady(work, true, 0, 1, 2);

		// Pre-administer the managed objects
		this.administerManagedObjects(work);
		this.administerManagedObjects(work);
		this.administerManagedObjects(work);

		// Do work with the managed objects
		this.getObject(work, 0, "WORK");
		this.getObject(work, 1, "THREAD");
		this.getObject(work, 2, "PROCESS");

		// Post-administer the managed objects
		this.administerManagedObjects(work);
		this.administerManagedObjects(work);
		this.administerManagedObjects(work);

		// Unload the work
		work.unloadWork();

		// Verify mocks
		this.verifyMockObjects();
	}

	/**
	 * Tests the full life cycle of the {@link WorkContainer} when re-used by
	 * different {@link Task}.
	 */
	public void testReuse() throws Throwable {

		// Tasks use separate managed objects except one which is re-used and of
		// work scope.
		final int[] taskOneManagedObjects = new int[] { 0, 2, 3 };
		final int[] taskTwoManagedObjects = new int[] { 1, 3 };

		// Add managed object of each scope
		this.addManagedObjectIndex(ManagedObjectScope.WORK);
		this.addManagedObjectIndex(ManagedObjectScope.THREAD);
		this.addManagedObjectIndex(ManagedObjectScope.PROCESS);
		this.addManagedObjectIndex(ManagedObjectScope.WORK);

		// Add an unused managed object (for a task three)
		this.addManagedObjectIndex(ManagedObjectScope.WORK);

		// Add administrator for each task
		final int taskOneAdmin = 0;
		this.addAdministratorIndex(AdministratorScope.WORK);
		final int taskTwoAdmin = 1;
		this.addAdministratorIndex(AdministratorScope.WORK);

		// Add an unused administrator (for a task three)
		this.addAdministratorIndex(AdministratorScope.WORK);

		// Task one initialises the container and its managed objects
		this.record_WorkContainer_init();
		this.record_WorkContainer_loadManagedObjects(taskOneManagedObjects);
		this
				.record_WorkContainer_coordinateManagedObjects(taskOneManagedObjects);
		this.record_WorkContainer_isManagedObjectsReady(taskOneManagedObjects);

		// Task one does pre-administration
		this.record_WorkContainer_administerManagedObjects(taskOneAdmin, 2);

		// Task two initialises its managed objects
		this.record_WorkContainer_loadManagedObjects(taskTwoManagedObjects);
		this
				.record_WorkContainer_coordinateManagedObjects(taskTwoManagedObjects);
		this.record_WorkContainer_isManagedObjectsReady(taskTwoManagedObjects);

		// Task one does work with the managed objects. Two is only dependency
		this.record_WorkContainer_getObject(0, "ZERO");
		this.record_WorkContainer_getObject(3, "THREE");

		// Task two does pre-administration
		this.record_WorkContainer_administerManagedObjects(taskTwoAdmin, 1);

		// Task one does post-administration
		this.record_WorkContainer_administerManagedObjects(taskOneAdmin, 2);

		// Task two does work with the managed objects
		this.record_WorkContainer_getObject(1, "ONE");
		this.record_WorkContainer_getObject(3, "THREE");

		// Task two does post-administration
		this.record_WorkContainer_administerManagedObjects(taskTwoAdmin, 1);

		// Tasks complete so unload work container
		this.record_WorkContainer_unloadWork();

		// Replay mocks
		this.replayMockObjects();

		// Create the work container
		WorkContainer<?> work = this.createWorkContainer();

		// Task one initialising its managed objects
		this.loadManagedObjects(work, true, taskOneManagedObjects);
		this.coordinateManagedObject(work, taskOneManagedObjects);
		this.isManagedObjectsReady(work, true, taskOneManagedObjects);

		// Task one pre-administration
		this.administerManagedObjects(work);

		// Task two initialising its managed objects
		this.loadManagedObjects(work, true, taskTwoManagedObjects);
		this.coordinateManagedObject(work, taskTwoManagedObjects);
		this.isManagedObjectsReady(work, true, taskTwoManagedObjects);

		// Task one does work with managed objects
		this.getObject(work, 0, "ZERO");
		this.getObject(work, 3, "THREE");

		// Task two does pre-administration
		this.administerManagedObjects(work);

		// Task one does post-administration
		this.administerManagedObjects(work);

		// Task two does work with managed objects
		this.getObject(work, 1, "ONE");
		this.getObject(work, 3, "THREE");

		// Task two does post-administration
		this.administerManagedObjects(work);

		// Unload the work
		work.unloadWork();

		// Verify mocks
		this.verifyMockObjects();
	}

}
