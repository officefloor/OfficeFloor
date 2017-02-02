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
package net.officefloor.frame.stress;

import junit.framework.TestCase;
import net.officefloor.frame.api.administration.Administration;
import net.officefloor.frame.api.build.ManagedFunctionBuilder;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.build.OfficeBuilder;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.extension.ExtensionInterfaceFactory;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.managedobject.source.impl.AbstractManagedObjectSource;
import net.officefloor.frame.api.source.TestSource;
import net.officefloor.frame.api.team.Team;
import net.officefloor.frame.impl.spi.team.ExecutorFixedTeamSource;
import net.officefloor.frame.impl.spi.team.LeaderFollowerTeam;
import net.officefloor.frame.impl.spi.team.OnePersonTeam;
import net.officefloor.frame.test.AbstractOfficeConstructTestCase;
import net.officefloor.frame.test.MockTeamSource;
import net.officefloor.frame.test.ReflectiveFlow;
import net.officefloor.frame.test.ReflectiveFunctionBuilder;

/**
 * Tests the {@link Administration}.
 * 
 * @author Daniel Sagenschneider
 */
public class AdministratorStressTest extends AbstractOfficeConstructTestCase {

	/**
	 * Ensures no issues arising in stress {@link Administration} with a
	 * {@link OnePersonTeam}.
	 */
	@StressTest
	public void test_StressAdministrator_OnePersonTeam() throws Exception {
		this.doTest(new OnePersonTeam("TEST", 100));
	}

	/**
	 * Ensures no issues arising in stress {@link Administration} with a
	 * {@link LeaderFollowerTeam}.
	 */
	@StressTest
	public void test_StressAdministrator_LeaderFollowerTeam() throws Exception {
		this.doTest(new LeaderFollowerTeam("TEST", MockTeamSource.createTeamIdentifier(), 5, 100));
	}

	/**
	 * Ensures no issues arising in stress {@link Administration} with a
	 * {@link ExecutorFixedTeamSource}.
	 */
	@StressTest
	public void test_StressAdministrator_ExecutorFixedTeam() throws Exception {
		this.doTest(ExecutorFixedTeamSource.createTeam("TEST", MockTeamSource.createTeamIdentifier(), 5));
	}

	/**
	 * Does the {@link Administration} stress test.
	 * 
	 * @param team
	 *            {@link Team} to use to run the {@link ManagedFunction}
	 *            instances.
	 */
	public void doTest(Team team) throws Exception {
		
		fail("TODO fix infinite loop");

		int ADMIN_TASK_COUNT = 1000000;
		int MAX_RUN_TIME = 100;
		String PRE_TASK_VALUE = "pre-task";
		String POST_TASK_VALUE = "post-task";
		this.setVerbose(true);

		// Obtain the office name and builder
		String officeName = this.getOfficeName();
		OfficeBuilder officeBuilder = this.getOfficeBuilder();

		// Construct the team
		this.constructTeam("TEAM", team);

//		// Create the pre administration
//		AdministrationBuilder<Indexed> preTaskAdmin = this.constructAdministrator("PRE", Administration.class, "TEAM");
//		preTaskAdmin.addProperty(Administration.ADMINISTRATION_VALUE_PROPERTY_NAME, PRE_TASK_VALUE);
//		preTaskAdmin.addDuty("DUTY");
//		preTaskAdmin.administerManagedObject("MO");
//
//		// Create the post administration
//		AdministrationBuilder<Indexed> postTaskAdmin = this.constructAdministrator("POST", Administration.class, "TEAM");
//		postTaskAdmin.addProperty(Administration.ADMINISTRATION_VALUE_PROPERTY_NAME, POST_TASK_VALUE);
//		postTaskAdmin.addDuty("DUTY");
//		postTaskAdmin.administerManagedObject("MO");

		// Create the administered managed object
		this.constructManagedObject("ADMIN_MO", AdministeredObject.class, officeName);
		officeBuilder.addThreadManagedObject("MO", "ADMIN_MO");

		// Create the administered work
		AdministeredWork work = new AdministeredWork(PRE_TASK_VALUE, POST_TASK_VALUE, ADMIN_TASK_COUNT);

		// Create the setup task
		ReflectiveFunctionBuilder setupTask = this.constructFunction(work, "setupTask");
		setupTask.getBuilder().setResponsibleTeam("TEAM");
		setupTask.buildFlow("administeredTask", null, false);
		setupTask.buildObject("MO");
		setupTask.buildFlow("setupTask", null, false);

		// Create the administered task
		ReflectiveFunctionBuilder administeredTask = this.constructFunction(work, "administeredTask");
		administeredTask.getBuilder().setResponsibleTeam("TEAM");
		administeredTask.buildObject("MO");
		ManagedFunctionBuilder<?, ?> adminTaskBuilder = administeredTask.getBuilder();
//		adminTaskBuilder.linkPreFunctionAdministration("PRE", "DUTY");
//		adminTaskBuilder.linkPostFunctionAdministration("POST", "DUTY");

		// Run the repeats
		this.invokeFunction("setupTask", new Integer(1), MAX_RUN_TIME);

		// Ensure run enough times
		assertEquals("Incorrect number of admin tasks run", ADMIN_TASK_COUNT, work.adminTaskCount);
	}

	/**
	 * Administered functionality.
	 */
	public static class AdministeredWork {

		/**
		 * Pre task value.
		 */
		private final String preTaskValue;

		/**
		 * Post task value.
		 */
		private final String postTaskValue;

		/**
		 * Maximum number of administered tasks.
		 */
		private final int maxAdminTasks;

		/**
		 * Number of administered tasks run.
		 */
		public volatile int adminTaskCount = 0;

		/**
		 * Flag indicating if the administered task is run.
		 */
		private boolean isAdminTaskRun = false;

		/**
		 * Initiate.
		 * 
		 * @param preTaskValue
		 *            Pre task value.
		 * @param postTaskValue
		 *            Post task value.
		 * @param maxAdminTasks
		 *            Maximum number of administered tasks.
		 */
		public AdministeredWork(String preTaskValue, String postTaskValue, int maxAdminTasks) {
			this.preTaskValue = preTaskValue;
			this.postTaskValue = postTaskValue;
			this.maxAdminTasks = maxAdminTasks;
		}

		/**
		 * Setup task that runs the administered {@link ManagedFunction}.
		 * 
		 * @param flow
		 *            {@link ReflectiveFlow}.
		 * @param object
		 *            {@link AdministeredObject}.
		 * @param repeat
		 *            {@link ReflectiveFlow} to repeat.
		 */
		public void setupTask(ReflectiveFlow flow, AdministeredObject object, ReflectiveFlow repeat) {

			// Determine if first time run
			if (this.adminTaskCount > 0) {
				// Not first time, so ensure admin task run
				TestCase.assertTrue("Administered task should be run", this.isAdminTaskRun);

				// Ensure post administration occurred
				TestCase.assertEquals("Incorrect post task value", this.postTaskValue, object.administrationValue);
			}

			// Invoke the administered task
			this.isAdminTaskRun = false;
			flow.doFlow(null, null);

			// Determine if require invoking another administered task
			this.adminTaskCount++;
			if (this.adminTaskCount < this.maxAdminTasks) {
				repeat.doFlow(null, null);
			}
		}

		/**
		 * Administered {@link ManagedFunction}.
		 * 
		 * @param object
		 *            {@link AdministeredObject}.
		 */
		public void administeredTask(AdministeredObject object) {

			// Ensure pre administration occurred
			TestCase.assertEquals("Incorrect pre task value", this.preTaskValue, object.administrationValue);

			// Flag that administered task run
			this.isAdminTaskRun = true;
		}
	}

	/**
	 * Administered extension interface.
	 */
	public static interface AdministeredExtensionInterface {

		/**
		 * Invoked to do the administration by the {@link Administration}.
		 * 
		 * @param administeredValue
		 *            Administered value.
		 */
		void doAdministration(String administeredValue);
	}

	/**
	 * Administered {@link ManagedObjectSource}.
	 */
	@TestSource
	public static class AdministeredObject extends AbstractManagedObjectSource<None, None> implements ManagedObject,
			ExtensionInterfaceFactory<AdministeredExtensionInterface>, AdministeredExtensionInterface {

		/**
		 * Administered type.
		 */
		public volatile String administrationValue;

		/*
		 * ================ AbstractManagedObjectSource =======================
		 */

		@Override
		protected void loadSpecification(SpecificationContext context) {
			// No specification
		}

		@Override
		protected void loadMetaData(MetaDataContext<None, None> context) throws Exception {
			context.setObjectClass(AdministeredObject.class);
			context.addManagedObjectExtensionInterface(AdministeredExtensionInterface.class, this);
		}

		@Override
		protected ManagedObject getManagedObject() throws Throwable {
			return this;
		}

		/*
		 * ================== ManagedObject ===================================
		 */

		@Override
		public Object getObject() throws Exception {
			return this;
		}

		/*
		 * ================ ExtensionInterfaceFactory =======================
		 */
		@Override
		public AdministeredExtensionInterface createExtensionInterface(ManagedObject managedObject) {
			return (AdministeredExtensionInterface) managedObject;
		}

		/*
		 * ============== AdministeredExtensionInterface ======================
		 */

		@Override
		public void doAdministration(String administeredValue) {
			this.administrationValue = administeredValue;
		}
	}

}