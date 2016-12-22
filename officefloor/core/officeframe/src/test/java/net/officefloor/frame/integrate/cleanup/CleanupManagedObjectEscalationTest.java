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
package net.officefloor.frame.integrate.cleanup;

import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.build.ManagedObjectBuilder;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.build.OfficeEnhancer;
import net.officefloor.frame.api.build.OfficeEnhancerContext;
import net.officefloor.frame.api.build.ManagedFunctionFactory;
import net.officefloor.frame.api.build.WorkFactory;
import net.officefloor.frame.api.escalate.Escalation;
import net.officefloor.frame.api.execute.ManagedFunction;
import net.officefloor.frame.api.execute.ManagedFunctionContext;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.impl.spi.team.PassiveTeamSource;
import net.officefloor.frame.spi.TestSource;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.recycle.CleanupEscalation;
import net.officefloor.frame.spi.managedobject.recycle.RecycleManagedObjectParameter;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceContext;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectTaskBuilder;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectWorkBuilder;
import net.officefloor.frame.spi.managedobject.source.impl.AbstractManagedObjectSource;
import net.officefloor.frame.test.AbstractOfficeConstructTestCase;
import net.officefloor.frame.test.ReflectiveWorkBuilder;
import net.officefloor.frame.test.ReflectiveWorkBuilder.ReflectiveTaskBuilder;

/**
 * Tests cleanup {@link Escalation} handling of {@link ManagedObject} instances.
 *
 * @author Daniel Sagenschneider
 */
public class CleanupManagedObjectEscalationTest extends
		AbstractOfficeConstructTestCase {

	/**
	 * Ensure handles cleanup escalation, making it available to further
	 * cleanups of {@link ManagedObject} instances.
	 */
	public void testHandleCleanupEscalation() throws Exception {

		// Create the escalation
		final Throwable escalation = new Throwable("TEST");

		// Construct the team
		this.constructTeam("TEAM", PassiveTeamSource.class);

		// Register two managed objects for clean up
		AbstractTestManagedObjectSource moEscalate = new EscalateCleanupManagedObjectSource(
				escalation);
		HandleEscalationManagedObjectSource moHandle = new HandleEscalationManagedObjectSource();
		ManagedObjectBuilder<None> moBuilderEscalate = this
				.getOfficeFloorBuilder().addManagedObject("ESCALATE",
						moEscalate);
		moBuilderEscalate.setManagingOffice(this.getOfficeName());
		ManagedObjectBuilder<None> moBuilderHandle = this
				.getOfficeFloorBuilder().addManagedObject("HANDLE", moHandle);
		moBuilderHandle.setManagingOffice(this.getOfficeName());

		// Configure the teams for recycling the managed object
		this.getOfficeBuilder().addOfficeEnhancer(new OfficeEnhancer() {
			@Override
			public void enhanceOffice(OfficeEnhancerContext context) {
				context.getFlowNodeBuilder("ESCALATE", "#recycle#", "cleanup")
						.setTeam("TEAM");
				context.getFlowNodeBuilder("HANDLE", "#recycle#", "cleanup")
						.setTeam("TEAM");
			}
		});

		// Add the managed objects
		this.getOfficeBuilder().addProcessManagedObject("MO_ESCALATE",
				"ESCALATE");
		this.getOfficeBuilder().registerManagedObjectSource("ESCALATE",
				"ESCALATE");
		this.getOfficeBuilder().addProcessManagedObject("MO_HANDLE", "HANDLE");
		this.getOfficeBuilder().registerManagedObjectSource("HANDLE", "HANDLE");

		// Construct the work
		ReflectiveWorkBuilder workBuilder = this.constructWork(new MockWork(),
				"WORK", "task");
		ReflectiveTaskBuilder taskBuilder = workBuilder.buildTask("task",
				"TEAM");
		taskBuilder.buildObject("MO_ESCALATE");
		taskBuilder.buildObject("MO_HANDLE");

		// Invoke the work
		this.invokeWork("WORK", null);

		// Ensure able to receive the escalation
		assertEquals("Should have escalation", escalation,
				moHandle.handledEscalation);
	}

	/**
	 * Mock {@link Work}.
	 */
	public static class MockWork {
		public void task(AbstractTestManagedObjectSource moA,
				AbstractTestManagedObjectSource moB) {
		}
	}

	/**
	 * Abstract test {@link ManagedObjectSource} for clean up.
	 */
	@TestSource
	public static abstract class AbstractTestManagedObjectSource extends
			AbstractManagedObjectSource<None, None> implements ManagedObject,
			WorkFactory<Work>, Work, ManagedFunctionFactory<Work, Indexed, None>,
			ManagedFunction<Work, Indexed, None> {

		/**
		 * ==================== ManagedObjectSource ==========================
		 */

		@Override
		protected void loadSpecification(SpecificationContext context) {
			// No properties required
		}

		@Override
		protected void loadMetaData(MetaDataContext<None, None> context)
				throws Exception {

			// Configure
			context.setObjectClass(this.getClass());

			// Provide recycle task
			ManagedObjectSourceContext<None> mos = context
					.getManagedObjectSourceContext();
			ManagedObjectWorkBuilder<Work> cleanup = mos.getRecycleWork(this);
			ManagedObjectTaskBuilder<Indexed, None> recycleTask = cleanup
					.addTask("cleanup", this);
			recycleTask.linkParameter(0, RecycleManagedObjectParameter.class);
		}

		@Override
		protected ManagedObject getManagedObject() throws Throwable {
			return this;
		}

		/*
		 * ===================== ManagedObject ===============================
		 */

		@Override
		public Object getObject() throws Throwable {
			return this;
		}

		/*
		 * ===================== WorkFactory =================================
		 */

		@Override
		public Work createWork() {
			return this;
		}

		/*
		 * ===================== TaskFactory =================================
		 */

		@Override
		public ManagedFunction<Work, Indexed, None> createManagedFunction(Work work) {
			return this;
		}
	}

	/**
	 * {@link ManagedObjectSource} to trigger the {@link Escalation}.
	 */
	public static class EscalateCleanupManagedObjectSource extends
			AbstractTestManagedObjectSource {

		/**
		 * {@link Escalation}.
		 */
		private final Throwable escalation;

		/**
		 * Initiate with {@link Escalation}.
		 * 
		 * @param escalation
		 *            {@link Escalation}.
		 */
		public EscalateCleanupManagedObjectSource(Throwable escalation) {
			this.escalation = escalation;
		}

		/*
		 * ===================== TaskFactory =================================
		 */

		@Override
		public Object execute(ManagedFunctionContext<Work, Indexed, None> context)
				throws Throwable {
			// Throw the escalation
			throw this.escalation;
		}
	}

	/**
	 * {@link ManagedObjectSource} to handle the {@link Escalation}.
	 */
	public static class HandleEscalationManagedObjectSource extends
			AbstractTestManagedObjectSource {

		/**
		 * {@link Escalation} handled.
		 */
		public Throwable handledEscalation = null;

		/*
		 * ===================== TaskFactory =================================
		 */

		@Override
		@SuppressWarnings("unchecked")
		public Object execute(ManagedFunctionContext<Work, Indexed, None> context)
				throws Throwable {

			// Obtain the recycle parameter
			RecycleManagedObjectParameter<HandleEscalationManagedObjectSource> parameter = (RecycleManagedObjectParameter<HandleEscalationManagedObjectSource>) context
					.getObject(0);

			// Ensure correct managed object
			assertSame("Incorrect managed object", this,
					parameter.getManagedObject());

			// Validate the escalation information
			CleanupEscalation[] escalations = parameter.getCleanupEscalations();
			assertEquals("Should have a cleanup escalation", 1,
					escalations.length);
			CleanupEscalation escalation = escalations[0];
			assertEquals("Incorrect object type",
					EscalateCleanupManagedObjectSource.class,
					escalation.getObjectType());

			// Obtain the escalation
			this.handledEscalation = escalation.getEscalation();

			// No further tasks
			return null;
		}
	}

}