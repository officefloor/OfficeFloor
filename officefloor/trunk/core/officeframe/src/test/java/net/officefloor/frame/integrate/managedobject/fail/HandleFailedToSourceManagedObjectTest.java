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
package net.officefloor.frame.integrate.managedobject.fail;

import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.escalate.FailedToSourceManagedObjectEscalation;
import net.officefloor.frame.impl.spi.team.OnePersonTeamSource;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.spi.TestSource;
import net.officefloor.frame.spi.managedobject.AsynchronousListener;
import net.officefloor.frame.spi.managedobject.AsynchronousManagedObject;
import net.officefloor.frame.spi.managedobject.CoordinatingManagedObject;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.ObjectRegistry;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectUser;
import net.officefloor.frame.spi.managedobject.source.impl.AbstractAsyncManagedObjectSource;
import net.officefloor.frame.test.AbstractOfficeConstructTestCase;
import net.officefloor.frame.test.ReflectiveWorkBuilder;
import net.officefloor.frame.test.ReflectiveWorkBuilder.ReflectiveTaskBuilder;

/**
 * Tests handling {@link FailedToSourceManagedObjectEscalation}.
 * 
 * @author Daniel Sagenschneider
 */
public class HandleFailedToSourceManagedObjectTest extends
		AbstractOfficeConstructTestCase {

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		MockManagedObjectSource.reset();
	}

	/**
	 * Ensures handles failure thrown on attempt to source the
	 * {@link ManagedObject}.
	 */
	public void testSourceThrowFailure() throws Exception {

		// Indicate failure thrown when source managed object
		NullPointerException failure = new NullPointerException(
				"source managed object thrown failure");
		MockManagedObjectSource.sourceThrowFailure = failure;

		// Test
		this.doTest(failure);
	}

	/**
	 * Ensures handles failure set when attempting to source
	 * {@link ManagedObject}.
	 */
	public void testSourceSetFailure() throws Exception {

		// Indicate failure thrown when source managed object
		Throwable failure = new Throwable("source managed object set failure");
		MockManagedObjectSource.sourceSetFailure = failure;

		// Test
		this.doTest(failure);
	}

	/**
	 * Ensures handles failure on attempting to register the
	 * {@link AsynchronousListener}.
	 */
	public void testRegisterAsynchronousListenerFailure() throws Exception {

		// Indicate failure thrown when source managed object
		Error failure = new Error("register asynchronous listener failure");
		MockManagedObjectSource.registerCompletionListenerFailure = failure;

		// Test
		this.doTest(failure);
	}

	/**
	 * Ensures handles failure on getting the {@link Object} from the
	 * {@link ManagedObject}.
	 */
	public void testObjectGetFailure() throws Exception {

		// Indicate failure thrown when get object
		Exception failure = new Exception("get object failure");
		MockManagedObjectSource.objectGetFailure = failure;

		// Test
		this.doTest(failure);
	}

	/**
	 * Ensures handles failure in coordinating the
	 * {@link CoordinatingManagedObject}.
	 */
	public void testCoordinateFailure() throws Exception {

		// Indicate failure thrown when coordinating
		Exception failure = new Exception("coordinate failure");
		MockManagedObjectSource.coordinateFailure = failure;

		// Test
		this.doTest(failure);
	}

	/**
	 * Does the test expecting to handle input failure.
	 * 
	 * @param failure
	 *            Cause of failing to source {@link ManagedObject}.
	 */
	private void doTest(Throwable cause) throws Exception {

		// Obtain the managing office name
		String officeName = this.getOfficeName();

		// Construct the managed object and team
		this.constructManagedObject("MO", MockManagedObjectSource.class,
				officeName).setTimeout(1000);
		this.constructTeam("TEAM", OnePersonTeamSource.class);

		// Construct the work
		Tasks tasks = new Tasks();
		ReflectiveWorkBuilder work = this.constructWork(tasks, "WORK",
				"obtainObject");

		// Construct the task attempting to obtain the object
		ReflectiveTaskBuilder obtainObject = work.buildTask("obtainObject",
				"TEAM");
		obtainObject.buildObject("MO", ManagedObjectScope.WORK);
		obtainObject.getBuilder()
				.addEscalation(FailedToSourceManagedObjectEscalation.class,
						"handleEscalation");

		// Construct the task to handle failure to source managed object
		ReflectiveTaskBuilder handleEscalation = work.buildTask(
				"handleEscalation", "TEAM");
		handleEscalation.buildParameter();

		// Invoke work
		this.invokeWork("WORK", null);

		// Ensure escalation handled
		assertNotNull("Should have handled escalation", tasks.escalation);
		assertEquals("Incorrect cause of source failure", cause,
				tasks.escalation.getCause());
		assertEquals("Incorrect object type", MockManagedObjectSource.class,
				tasks.escalation.getObjectType());
	}

	/**
	 * Tasks for testing.
	 */
	public static class Tasks {

		/**
		 * Handled escalation.
		 */
		public volatile FailedToSourceManagedObjectEscalation escalation = null;

		/**
		 * Task to attempt to obtain the object.
		 */
		public void obtainObject(MockManagedObjectSource object) {
			fail("Should never actual run this task");
		}

		/**
		 * Task to handle the escalation.
		 */
		public void handleEscalation(
				FailedToSourceManagedObjectEscalation escalation) {
			this.escalation = escalation;
		}
	}

	/**
	 * {@link ManagedObjectSource} that fails.
	 */
	@TestSource
	public static class MockManagedObjectSource extends
			AbstractAsyncManagedObjectSource<None, None> implements
			AsynchronousManagedObject, CoordinatingManagedObject<None> {

		/**
		 * Failure thrown in attempting to source the
		 * {@link ManagedObjectSource}.
		 */
		public static RuntimeException sourceThrowFailure = null;

		/**
		 * Failure set on the {@link ManagedObjectUser}.
		 */
		public static Throwable sourceSetFailure = null;

		/**
		 * Failure on registering the {@link AsynchronousListener}.
		 */
		public static Error registerCompletionListenerFailure = null;

		/**
		 * Failure on attempting to get the {@link Object} from the
		 * {@link ManagedObject}.
		 */
		public static Exception objectGetFailure = null;

		/**
		 * Failure on attempting to coordinate the
		 * {@link CoordinatingManagedObject}.
		 */
		public static Throwable coordinateFailure = null;

		/**
		 * Resets for the next test.
		 */
		public static void reset() {
			sourceThrowFailure = null;
			sourceSetFailure = null;
			registerCompletionListenerFailure = null;
			objectGetFailure = null;
			coordinateFailure = null;
		}

		/*
		 * ================ AbstractAsyncManagedObjectSource =================
		 */

		@Override
		protected void loadSpecification(SpecificationContext context) {
			// No specification
		}

		@Override
		protected void loadMetaData(MetaDataContext<None, None> context)
				throws Exception {
			context.setManagedObjectClass(MockManagedObjectSource.class);
			context.setObjectClass(MockManagedObjectSource.class);
		}

		@Override
		public void sourceManagedObject(ManagedObjectUser user) {

			// Determine if set failure
			if (sourceSetFailure != null) {
				user.setFailure(sourceSetFailure);
			}

			// Set the managed object (always to check handling)
			user.setManagedObject(this);

			// Determine if throw failure
			if (sourceThrowFailure != null) {
				throw sourceThrowFailure;
			}
		}

		/*
		 * =================== ManagedObject =============================
		 */

		@Override
		public void registerAsynchronousCompletionListener(
				AsynchronousListener listener) {
			// Determine if throw failure
			if (registerCompletionListenerFailure != null) {
				throw registerCompletionListenerFailure;
			}
		}

		@Override
		public Object getObject() throws Exception {

			// Determine if throw get object failure
			if (objectGetFailure != null) {
				throw objectGetFailure;
			}

			// Return this as the object
			return this;
		}

		@Override
		public void loadObjects(ObjectRegistry<None> registry) throws Throwable {
			// Determine if throw failure
			if (coordinateFailure != null) {
				throw coordinateFailure;
			}
		}
	}

}