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
package net.officefloor.frame.impl.execute.cleanup;

import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.build.ManagedObjectBuilder;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.build.OfficeEnhancer;
import net.officefloor.frame.api.build.OfficeEnhancerContext;
import net.officefloor.frame.api.escalate.Escalation;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.function.ManagedFunctionContext;
import net.officefloor.frame.api.function.ManagedFunctionFactory;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.recycle.CleanupEscalation;
import net.officefloor.frame.api.managedobject.recycle.RecycleManagedObjectParameter;
import net.officefloor.frame.api.managedobject.source.ManagedObjectFunctionBuilder;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSourceContext;
import net.officefloor.frame.api.managedobject.source.impl.AbstractManagedObjectSource;
import net.officefloor.frame.api.source.TestSource;
import net.officefloor.frame.impl.spi.team.PassiveTeamSource;
import net.officefloor.frame.test.AbstractOfficeConstructTestCase;
import net.officefloor.frame.test.ReflectiveFunctionBuilder;

/**
 * Tests cleanup {@link Escalation} handling of {@link ManagedObject} instances.
 *
 * @author Daniel Sagenschneider
 */
public class CleanupManagedObjectEscalationTest extends AbstractOfficeConstructTestCase {

	/**
	 * Ensure handles cleanup escalation, making it available to further
	 * cleanups of {@link ManagedObject} instances.
	 */
	public void testHandleCleanupEscalation() throws Exception {

		fail("TODO fix infinite loop");
		
		// Create the escalation
		final Throwable escalation = new Throwable("TEST");

		// Construct the team
		this.constructTeam("TEAM", PassiveTeamSource.class);

		// Register two managed objects for clean up
		AbstractTestManagedObjectSource moEscalate = new EscalateCleanupManagedObjectSource(escalation);
		HandleEscalationManagedObjectSource moHandle = new HandleEscalationManagedObjectSource();
		ManagedObjectBuilder<None> moBuilderEscalate = this.getOfficeFloorBuilder().addManagedObject("ESCALATE",
				moEscalate);
		moBuilderEscalate.setManagingOffice(this.getOfficeName());
		ManagedObjectBuilder<None> moBuilderHandle = this.getOfficeFloorBuilder().addManagedObject("HANDLE", moHandle);
		moBuilderHandle.setManagingOffice(this.getOfficeName());

		// Configure the teams for recycling the managed object
		this.getOfficeBuilder().addOfficeEnhancer(new OfficeEnhancer() {
			@Override
			public void enhanceOffice(OfficeEnhancerContext context) {
				context.getFlowBuilder("ESCALATE", "#recycle#").setResponsibleTeam("TEAM");
				context.getFlowBuilder("HANDLE", "#recycle#").setResponsibleTeam("TEAM");
			}
		});

		// Add the managed objects
		this.getOfficeBuilder().addProcessManagedObject("MO_ESCALATE", "ESCALATE");
		this.getOfficeBuilder().registerManagedObjectSource("ESCALATE", "ESCALATE");
		this.getOfficeBuilder().addProcessManagedObject("MO_HANDLE", "HANDLE");
		this.getOfficeBuilder().registerManagedObjectSource("HANDLE", "HANDLE");

		// Construct the work
		ReflectiveFunctionBuilder taskBuilder = this.constructFunction(new MockWork(), "task");
		taskBuilder.buildObject("MO_ESCALATE");
		taskBuilder.buildObject("MO_HANDLE");

		// Invoke the function
		this.invokeFunction("task", null);

		// Ensure able to receive the escalation
		assertEquals("Should have escalation", escalation, moHandle.handledEscalation);
	}

	/**
	 * Mock functionality.
	 */
	public static class MockWork {
		public void task(AbstractTestManagedObjectSource moA, AbstractTestManagedObjectSource moB) {
		}
	}

	/**
	 * Abstract test {@link ManagedObjectSource} for clean up.
	 */
	@TestSource
	public static abstract class AbstractTestManagedObjectSource extends AbstractManagedObjectSource<None, None>
			implements ManagedObject, ManagedFunctionFactory<Indexed, None>, ManagedFunction<Indexed, None> {

		/**
		 * ==================== ManagedObjectSource ==========================
		 */

		@Override
		protected void loadSpecification(SpecificationContext context) {
			// No properties required
		}

		@Override
		protected void loadMetaData(MetaDataContext<None, None> context) throws Exception {

			// Configure
			context.setObjectClass(this.getClass());

			// Provide recycle function
			ManagedObjectSourceContext<None> mos = context.getManagedObjectSourceContext();
			ManagedObjectFunctionBuilder<?, ?> cleanup = mos.getRecycleFunction(this);
			cleanup.linkParameter(0, RecycleManagedObjectParameter.class);
		}

		@Override
		protected ManagedObject getManagedObject() throws Throwable {
			return this;
		}

		/*
		 * ===================== ManagedObject ====================
		 */

		@Override
		public Object getObject() throws Throwable {
			return this;
		}

		/*
		 * ================= MnagedFunctionFactory =================
		 */

		@Override
		public ManagedFunction<Indexed, None> createManagedFunction() {
			return this;
		}
	}

	/**
	 * {@link ManagedObjectSource} to trigger the {@link Escalation}.
	 */
	public static class EscalateCleanupManagedObjectSource extends AbstractTestManagedObjectSource {

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
		 * ===================== ManagedFunctionFactory =====================
		 */

		@Override
		public Object execute(ManagedFunctionContext<Indexed, None> context) throws Throwable {
			// Throw the escalation
			throw this.escalation;
		}
	}

	/**
	 * {@link ManagedObjectSource} to handle the {@link Escalation}.
	 */
	public static class HandleEscalationManagedObjectSource extends AbstractTestManagedObjectSource {

		/**
		 * {@link Escalation} handled.
		 */
		public Throwable handledEscalation = null;

		/*
		 * ===================== ManagedFunction =====================
		 */

		@Override
		@SuppressWarnings("unchecked")
		public Object execute(ManagedFunctionContext<Indexed, None> context) throws Throwable {

			// Obtain the recycle parameter
			RecycleManagedObjectParameter<HandleEscalationManagedObjectSource> parameter = (RecycleManagedObjectParameter<HandleEscalationManagedObjectSource>) context
					.getObject(0);

			// Ensure correct managed object
			assertSame("Incorrect managed object", this, parameter.getManagedObject());

			// Validate the escalation information
			CleanupEscalation[] escalations = parameter.getCleanupEscalations();
			assertEquals("Should have a cleanup escalation", 1, escalations.length);
			CleanupEscalation escalation = escalations[0];
			assertEquals("Incorrect object type", EscalateCleanupManagedObjectSource.class, escalation.getObjectType());

			// Obtain the escalation
			this.handledEscalation = escalation.getEscalation();

			// No further tasks
			return null;
		}
	}

}