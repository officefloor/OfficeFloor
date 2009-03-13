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
package net.officefloor.frame.integrate;

import net.officefloor.frame.api.build.AdministratorBuilder;
import net.officefloor.frame.api.build.DependencyMappingBuilder;
import net.officefloor.frame.api.build.OfficeBuilder;
import net.officefloor.frame.api.build.TaskBuilder;
import net.officefloor.frame.api.build.WorkBuilder;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.manage.WorkManager;
import net.officefloor.frame.impl.spi.team.PassiveTeam;
import net.officefloor.frame.spi.administration.Administrator;
import net.officefloor.frame.spi.administration.Duty;
import net.officefloor.frame.spi.administration.DutyContext;
import net.officefloor.frame.spi.administration.source.AdministratorSourceMetaData;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.extension.ExtensionInterfaceFactory;
import net.officefloor.frame.spi.managedobject.extension.ManagedObjectExtensionInterfaceMetaData;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceMetaData;
import net.officefloor.frame.spi.team.Job;
import net.officefloor.frame.test.AbstractOfficeConstructTestCase;
import net.officefloor.frame.test.match.TypeMatcher;

/**
 * Tests the construction of an {@link OfficeBuilder}.
 * 
 * @author Daniel
 */
public class OfficeConstructTest extends AbstractOfficeConstructTestCase {

	/**
	 * Ensures able to construct an {@link OfficeBuilder}.
	 */
	@SuppressWarnings("unchecked")
	public void testConstructOffice() throws Throwable {

		// Managed Object One
		ManagedObject moOne = this.createMock(ManagedObject.class);
		ManagedObjectSourceMetaData moOneMetaData = this
				.createMock(ManagedObjectSourceMetaData.class);
		this.constructManagedObject("mo-one", moOneMetaData, moOne, "test");

		// Managed Object Two
		ManagedObject moTwo = this.createMock(ManagedObject.class);
		this.constructManagedObject("mo-two", moTwo, "test");

		// Administrator One
		Administrator adminOne = this.createMock(Administrator.class);
		AdministratorSourceMetaData adminOneMetaData = this
				.createMock(AdministratorSourceMetaData.class);
		AdministratorBuilder<?> adminOneBuilder = this.constructAdministrator(
				"a-one", adminOne, adminOneMetaData, "team-one");
		adminOneBuilder.administerManagedObject("W-MO-ONE");

		// Administrator Two
		Administrator adminTwo = this.createMock(Administrator.class);
		AdministratorSourceMetaData adminTwoMetaData = this
				.createMock(AdministratorSourceMetaData.class);
		AdministratorBuilder<?> adminTwoBuilder = this.constructAdministrator(
				"a-two", adminTwo, adminTwoMetaData, "team-one");
		adminTwoBuilder.administerManagedObject("W-MO-ONE");

		// Team
		this.constructTeam("team-one", new PassiveTeam());

		// Work
		TestWork work = this.createMock(TestWork.class);
		WorkBuilder<TestWork> workBuilder = this.constructWork("work-one",
				work, "task-one");

		// Link Managed Object One into Work
		DependencyMappingBuilder dependencyBuilder = workBuilder
				.addWorkManagedObject("W-MO-ONE", "mo-one");
		dependencyBuilder.mapDependency(DependenciesEnum.DEPENDENCY_ONE,
				"W-MO-TWO");
		workBuilder.addWorkManagedObject("W-MO-TWO", "mo-two");

		// Link Administrator One into Work
		workBuilder.linkAdministrator("W-ADMIN-ONE", "a-one");

		// Link Administrator Two into Work
		workBuilder.linkAdministrator("W-ADMIN-TWO", "a-two");

		// Task for Work
		TestTask task = this.createMock(TestTask.class);
		TaskBuilder<Object, TestWork, ManagedObjectsEnum, TaskFlowsEnum> taskBuilder = this
				.constructTask("task-one", task, "team-one", null);
		taskBuilder.linkManagedObject(ManagedObjectsEnum.MANAGED_OBJECT_ONE,
				"W-MO-ONE");
		taskBuilder.linkPreTaskAdministration("W-ADMIN-ONE",
				DutyOneEnum.DUTY_ONE);
		taskBuilder.linkPostTaskAdministration("W-ADMIN-TWO",
				DutyTwoEnum.DUTY_TWO);

		// ------------------------------------
		// Additional mock objects for creation
		// ------------------------------------

		// Managed Object Extension Interface meta-data
		ManagedObjectExtensionInterfaceMetaData moeiMetaData = this
				.createMock(ManagedObjectExtensionInterfaceMetaData.class);
		ManagedObjectExtensionInterfaceMetaData[] moeiListing = new ManagedObjectExtensionInterfaceMetaData[] { moeiMetaData };

		// Extension Interface Factory
		ExtensionInterfaceFactory eiFactory = this
				.createMock(ExtensionInterfaceFactory.class);

		// ------------------------------------
		// Record registering the office
		// ------------------------------------

		// MANAGED OBJECT ONE (INIT)
		// No handlers so no need as process managed object
		moOneMetaData.getHandlerKeys();
		this.control(moOneMetaData).setReturnValue(null);
		// Load as work scoped managed object
		// Dependency keys (no dependencies)
		moOneMetaData.getDependencyKeys();
		this.control(moOneMetaData).setReturnValue(null);
		// Managed Object Class of managed object
		moOneMetaData.getManagedObjectClass();
		this.control(moOneMetaData).setReturnValue(moOne.getClass());

		// MANAGED OBJECT TWO (INIT)
		// Initialised via mock implementation

		// ADMINISTRATOR ONE
		// Obtain the extension interface for administering
		adminOneMetaData.getExtensionInterface();
		this.control(adminOneMetaData).setReturnValue(
				MockExtensionInterface.class);
		// Managed Object One extension interfaces
		moOneMetaData.getExtensionInterfacesMetaData();
		this.control(moOneMetaData).setReturnValue(moeiListing);
		// Obtain matching extension interface type
		moeiMetaData.getExtensionInterfaceType();
		this.control(moeiMetaData).setReturnValue(MockExtensionInterface.class);
		// As matching, return the extension interface factory
		moeiMetaData.getExtensionInterfaceFactory();
		this.control(moeiMetaData).setReturnValue(eiFactory);

		// ADMINISTRATOR TWO
		// Obtain the extension interface for administering
		adminTwoMetaData.getExtensionInterface();
		this.control(adminTwoMetaData).setReturnValue(
				MockExtensionInterface.class);
		// Managed Object One extension interfaces
		moOneMetaData.getExtensionInterfacesMetaData();
		this.control(moOneMetaData).setReturnValue(moeiListing);
		// Obtain matching extension interface type
		moeiMetaData.getExtensionInterfaceType();
		this.control(moeiMetaData).setReturnValue(MockExtensionInterface.class);
		// As matching, return the extension interface factory
		moeiMetaData.getExtensionInterfaceFactory();
		this.control(moeiMetaData).setReturnValue(eiFactory);

		// MANAGED OBJECT ONE (START)
		// No handlers
		moOneMetaData.getHandlerKeys();
		this.control(moOneMetaData).setReturnValue(null);

		// MANAGED OBJECT TWO (START)
		// Loaded via mock implementation

		// ADMINISTRATOR ONE (remaining state)
		// Duty keys
		adminOneMetaData.getAministratorDutyKeys();
		this.control(adminOneMetaData).setReturnValue(DutyOneEnum.class);

		// ADMINISTRATOR TWO (remaining state)
		// Duty keys
		adminTwoMetaData.getAministratorDutyKeys();
		this.control(adminTwoMetaData).setReturnValue(DutyOneEnum.class);

		// ------------------------------------
		// Additional mock objects to running
		// ------------------------------------

		// Object from Managed Object
		Object object = new Object();

		// Mock extension interface
		MockExtensionInterface ei = this
				.createMock(MockExtensionInterface.class);

		// Duty
		Duty duty = this.createMock(Duty.class);

		// ------------------------------------
		// Record running the office
		// ------------------------------------

		// MANAGED OBJECT ONE
		// Obtain the Object
		this.recordReturn(moOne, moOne.getObject(), object);

		// MANAGED OBJECT TWO
		// Obtain the Object
		this.recordReturn(moTwo, moTwo.getObject(), object);

		// PRE-TASK ADMINISTRATION
		// Managed Object Extension Interface
		this.recordReturn(eiFactory, eiFactory.createExtensionInterface(moOne),
				ei);
		// Obtain the duty
		this.recordReturn(adminOne, adminOne.getDuty(DutyOneEnum.DUTY_ONE),
				duty);
		// Do the duty
		duty.doDuty(null);
		this.control(duty)
				.setDefaultMatcher(new TypeMatcher(DutyContext.class));

		// TASK
		// Do the task
		this.recordReturn(task, task.doTask(null), null, new TypeMatcher(
				Job.class));

		// POST-TASK ADMINISTRATOR
		// Managed Object Extension Interface
		this.recordReturn(eiFactory, eiFactory.createExtensionInterface(moOne),
				ei);
		// Obtain the duty
		this.recordReturn(adminTwo, adminTwo.getDuty(DutyTwoEnum.DUTY_TWO),
				duty);
		// Do the duty
		duty.doDuty(null);
		this.control(duty).setMatcher(new TypeMatcher(DutyContext.class));

		// ------------------------------------
		// Run the Office
		// ------------------------------------
		this.replayMockObjects();

		// Register the Office Floor
		String officeName = this.getOfficeName();
		final OfficeFloor officeFloor = this.constructOfficeFloor();

		// Open the Office Floor
		officeFloor.openOfficeFloor();

		// Invoke the Work
		WorkManager workManager = officeFloor.getOffice(officeName)
				.getWorkManager("work-one");
		workManager.invokeWork(null);

		// Allow some time for processing to occur
		Thread.sleep(100);

		// Close the Office Floor
		officeFloor.closeOfficeFloor();

		// Verify functionality
		this.verifyMockObjects();
	}

}

interface TestWork extends Work {
}

interface TestTask extends
		Task<Object, TestWork, ManagedObjectsEnum, TaskFlowsEnum> {
}

enum TasksEnum {
	TASK_ONE
}

enum ManagedObjectsEnum {

	MANAGED_OBJECT_ONE
}

enum DependenciesEnum {

	DEPENDENCY_ONE,

	DEPENDENCY_TWO
}

enum TaskFlowsEnum {

	FLOW_ONE
}

enum DutyOneEnum {
	DUTY_ONE
}

enum DutyTwoEnum {
	DUTY_TWO
}

interface MockExtensionInterface {
}