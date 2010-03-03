/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2010 Daniel Sagenschneider
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

package net.officefloor.frame.integrate.stress;

import junit.framework.TestCase;
import net.officefloor.frame.api.build.AdministratorBuilder;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.build.OfficeBuilder;
import net.officefloor.frame.api.build.TaskBuilder;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.TaskContext;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.impl.spi.team.LeaderFollowerTeam;
import net.officefloor.frame.impl.spi.team.OnePersonTeam;
import net.officefloor.frame.internal.structure.FlowInstigationStrategyEnum;
import net.officefloor.frame.spi.TestSource;
import net.officefloor.frame.spi.administration.Administrator;
import net.officefloor.frame.spi.administration.Duty;
import net.officefloor.frame.spi.administration.DutyContext;
import net.officefloor.frame.spi.administration.DutyKey;
import net.officefloor.frame.spi.administration.source.impl.AbstractAdministratorSource;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.extension.ExtensionInterfaceFactory;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.spi.managedobject.source.impl.AbstractManagedObjectSource;
import net.officefloor.frame.spi.team.Team;
import net.officefloor.frame.test.AbstractOfficeConstructTestCase;
import net.officefloor.frame.test.ReflectiveFlow;
import net.officefloor.frame.test.ReflectiveWorkBuilder;
import net.officefloor.frame.test.ReflectiveWorkBuilder.ReflectiveTaskBuilder;

/**
 * Tests the {@link Administrator}.
 *
 * @author Daniel Sagenschneider
 */
public class AdministratorStressTest extends AbstractOfficeConstructTestCase {

	/**
	 * Ensures no issues arising in stress {@link Administrator} with a
	 * {@link OnePersonTeam}.
	 */
	@StressTest
	public void test_StressAdministrator_OnePersonTeam() throws Exception {
		this.doTest(new OnePersonTeam("TEST", 100));
	}

	/**
	 * Ensures no issues arising in stress {@link Administrator} with a
	 * {@link LeaderFollowerTeam}.
	 */
	@StressTest
	public void test_StressAdministrator_LeaderFollowerTeam() throws Exception {
		this.doTest(new LeaderFollowerTeam("TEST", 5, 100));
	}

	/**
	 * Does the {@link Administrator} stress test.
	 *
	 * @param team
	 *            {@link Team} to use to run the {@link Task} instances.
	 */
	public void doTest(Team team) throws Exception {

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

		// Create the pre administration
		AdministratorBuilder<Indexed> preTaskAdmin = this
				.constructAdministrator("PRE", Administration.class, "TEAM");
		preTaskAdmin.addProperty(
				Administration.ADMINISTRATION_VALUE_PROPERTY_NAME,
				PRE_TASK_VALUE);
		preTaskAdmin.addDuty("DUTY");
		preTaskAdmin.administerManagedObject("MO");

		// Create the post administration
		AdministratorBuilder<Indexed> postTaskAdmin = this
				.constructAdministrator("POST", Administration.class, "TEAM");
		postTaskAdmin.addProperty(
				Administration.ADMINISTRATION_VALUE_PROPERTY_NAME,
				POST_TASK_VALUE);
		postTaskAdmin.addDuty("DUTY");
		postTaskAdmin.administerManagedObject("MO");

		// Create the administered managed object
		this.constructManagedObject("ADMIN_MO", AdministeredObject.class,
				officeName);
		officeBuilder.addThreadManagedObject("MO", "ADMIN_MO");

		// Create the administered work
		AdministeredWork work = new AdministeredWork(PRE_TASK_VALUE,
				POST_TASK_VALUE, ADMIN_TASK_COUNT);
		ReflectiveWorkBuilder workBuilder = this.constructWork(work, "WORK",
				"setupTask");

		// Create the setup task
		ReflectiveTaskBuilder setupTask = workBuilder.buildTask("setupTask",
				"TEAM");
		setupTask.buildFlow("administeredTask",
				FlowInstigationStrategyEnum.PARALLEL, null);
		setupTask.buildTaskContext();
		setupTask.buildObject("MO");

		// Create the administered task
		ReflectiveTaskBuilder administeredTask = workBuilder.buildTask(
				"administeredTask", "TEAM");
		administeredTask.buildObject("MO");
		TaskBuilder<?, ?, ?> adminTaskBuilder = administeredTask.getBuilder();
		adminTaskBuilder.linkPreTaskAdministration("PRE", "DUTY");
		adminTaskBuilder.linkPostTaskAdministration("POST", "DUTY");

		// Run the repeats
		this.invokeWork("WORK", new Integer(1), MAX_RUN_TIME);

		// Ensure run enough times
		assertEquals("Incorrect number of admin tasks run", ADMIN_TASK_COUNT,
				work.adminTaskCount);
	}

	/**
	 * Administered {@link Work}.
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
		public AdministeredWork(String preTaskValue, String postTaskValue,
				int maxAdminTasks) {
			this.preTaskValue = preTaskValue;
			this.postTaskValue = postTaskValue;
			this.maxAdminTasks = maxAdminTasks;
		}

		/**
		 * Setup task that runs the administered {@link Task}.
		 *
		 * @param flow
		 *            {@link ReflectiveFlow}.
		 * @param context
		 *            {@link TaskContext}.
		 * @param object
		 *            {@link AdministeredObject}.
		 */
		public void setupTask(ReflectiveFlow flow,
				TaskContext<?, ?, ?> context, AdministeredObject object) {

			// Determine if first time run
			if (this.adminTaskCount > 0) {
				// Not first time, so ensure admin task run
				TestCase.assertTrue("Administered task should be run",
						this.isAdminTaskRun);

				// Ensure post administration occurred
				TestCase.assertEquals("Incorrect post task value",
						this.postTaskValue, object.administrationValue);
			}

			// Invoke the administered task
			this.isAdminTaskRun = false;
			flow.doFlow(null);

			// Determine if require invoking another administered task
			this.adminTaskCount++;
			if (this.adminTaskCount < this.maxAdminTasks) {
				context.setComplete(false);
			}
		}

		/**
		 * Administered {@link Task}.
		 *
		 * @param object
		 *            {@link AdministeredObject}.
		 */
		public void administeredTask(AdministeredObject object) {

			// Ensure pre administration occurred
			TestCase.assertEquals("Incorrect pre task value",
					this.preTaskValue, object.administrationValue);

			// Flag that administered task run
			this.isAdminTaskRun = true;
		}
	}

	/**
	 * Administered extension interface.
	 */
	public static interface AdministeredExtensionInterface {

		/**
		 * Invoked to do the administration by the {@link Administrator}.
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
	public static class AdministeredObject extends
			AbstractManagedObjectSource<None, None> implements ManagedObject,
			ExtensionInterfaceFactory<AdministeredExtensionInterface>,
			AdministeredExtensionInterface {

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
		protected void loadMetaData(MetaDataContext<None, None> context)
				throws Exception {
			context.setObjectClass(AdministeredObject.class);
			context.addManagedObjectExtensionInterface(
					AdministeredExtensionInterface.class, this);
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
		public AdministeredExtensionInterface createExtensionInterface(
				ManagedObject managedObject) {
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

	/**
	 * {@link Administrator}.
	 */
	@TestSource
	public static class Administration
			extends
			AbstractAdministratorSource<AdministeredExtensionInterface, Indexed>
			implements Administrator<AdministeredExtensionInterface, Indexed>,
			Duty<AdministeredExtensionInterface, None> {

		/**
		 * Administration value property name.
		 */
		public static final String ADMINISTRATION_VALUE_PROPERTY_NAME = "administration.value";

		/**
		 * Administration value.
		 */
		private String administrationValue;

		/*
		 * ================== AbstractAdministratorSource ======================
		 */

		@Override
		protected void loadSpecification(SpecificationContext context) {
			context.addProperty(ADMINISTRATION_VALUE_PROPERTY_NAME);
		}

		@Override
		protected void loadMetaData(
				MetaDataContext<AdministeredExtensionInterface, Indexed> context)
				throws Exception {

			// Obtain the administration value
			this.administrationValue = context.getAdministratorSourceContext()
					.getProperty(ADMINISTRATION_VALUE_PROPERTY_NAME);

			// Load meta-data
			context.setExtensionInterface(AdministeredExtensionInterface.class);
			context.addDuty("DUTY");
		}

		@Override
		public Administrator<AdministeredExtensionInterface, Indexed> createAdministrator() {
			return this;
		}

		/*
		 * ======================= Administrator =============================
		 */

		@Override
		public Duty<AdministeredExtensionInterface, ?> getDuty(
				DutyKey<Indexed> dutyKey) {
			return this;
		}

		/*
		 * ========================== Duty ===================================
		 */

		@Override
		public void doDuty(
				DutyContext<AdministeredExtensionInterface, None> context)
				throws Throwable {
			for (AdministeredExtensionInterface object : context
					.getExtensionInterfaces()) {
				object.doAdministration(this.administrationValue);
			}
		}
	}

}