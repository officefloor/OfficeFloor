/*-
 * #%L
 * OfficeCompiler
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package net.officefloor.compile.integrate.officefloor;

import net.officefloor.compile.spi.officefloor.DeployedOffice;
import net.officefloor.compile.spi.officefloor.ExternalServiceCleanupEscalationHandler;
import net.officefloor.compile.spi.officefloor.ExternalServiceInput;
import net.officefloor.compile.test.officefloor.CompileOfficeFloor;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.escalate.Escalation;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.function.ManagedFunctionFactory;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.recycle.CleanupEscalation;
import net.officefloor.frame.api.managedobject.source.impl.AbstractManagedObjectSource;
import net.officefloor.frame.api.source.TestSource;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.test.Closure;
import net.officefloor.frame.test.OfficeFrameTestCase;

/**
 * Ensure {@link ExternalServiceInput} can handle {@link CleanupEscalation}
 * instances.
 * 
 * @author Daniel Sagenschneider
 */
public class ServiceInputHandleEscalationTest extends OfficeFrameTestCase {

	/**
	 * {@link ExternalServiceInput}.
	 */
	private ExternalServiceInput<MockInput, MockInput> serviceInput;

	/**
	 * {@link MockInput}.
	 */
	private final MockInput inputObject = new MockInput();

	/**
	 * {@link Escalation} for clean up.
	 */
	private Throwable escalation = new Exception("CLEANUP ESCALATION");

	/**
	 * Indicates if the {@link ExternalServiceCleanupEscalationHandler} was invoked.
	 */
	private boolean isCleanupHandlingInvoked = false;

	/**
	 * {@link CleanupEscalation} instances.
	 */
	private CleanupEscalation[] cleanupEscalations = null;

	/**
	 * {@link OfficeFloor}.
	 */
	private OfficeFloor officeFloor;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		// Compile the OfficeFloor with extension
		CompileOfficeFloor compile = new CompileOfficeFloor();
		compile.officeFloor((extension) -> {

			// Configure the input
			DeployedOffice office = extension.getDeployedOffice();
			this.serviceInput = office.getDeployedOfficeInput("SECTION", "input").addExternalServiceInput(
					MockInput.class, MockInput.class, (inputManagedObject, cleanupEscalations) -> {
						assertSame("Incorrect clean up managed object", this.inputObject, inputManagedObject);
						this.cleanupEscalations = cleanupEscalations;
						this.isCleanupHandlingInvoked = true;
					});
		});
		compile.office((extension) -> {
			extension.getOfficeArchitect().enableAutoWireObjects();
			extension.addSection("SECTION", MockSection.class);
			extension.getOfficeArchitect()
					.addOfficeManagedObjectSource("CleanupEscalator", new MockCleanupEscalationObject())
					.addOfficeManagedObject("CleanupEscalator", ManagedObjectScope.THREAD);
		});
		this.officeFloor = compile.compileAndOpenOfficeFloor();
	}

	@Override
	protected void tearDown() throws Exception {

		// Close the OfficeFloor
		if (this.officeFloor != null) {
			this.officeFloor.closeOfficeFloor();
		}

		// Continue tear down
		super.tearDown();
	}

	/**
	 * Ensure can handle {@link CleanupEscalation}.
	 */
	public void testHandleEscalation() {
		Closure<Boolean> isCallbackInvoked = new Closure<Boolean>(false);
		this.serviceInput.service(this.inputObject, (serviceEscalation) -> {
			assertNull("Should be no service escalation", serviceEscalation);

			// Clean up handler should be invoked before callback
			assertTrue("Should invoke clean up before service call back", this.isCleanupHandlingInvoked);

			// Ensure captured clean up escalation
			assertNotNull("Should have cleanup escalations", this.cleanupEscalations);
			assertEquals("Incorrect number of cleanup escalations", 1, this.cleanupEscalations.length);

			// Ensure correct clean up escalation
			CleanupEscalation cleanupEscalation = this.cleanupEscalations[0];
			assertEquals("Incorrect object type", MockCleanupEscalationObject.class, cleanupEscalation.getObjectType());
			assertSame("Incorrect escalation", this.escalation, cleanupEscalation.getEscalation());

			// Invoked call back
			isCallbackInvoked.value = true;
		});
		assertTrue("Should have callback invoked", isCallbackInvoked.value);
	}

	/**
	 * Ensure handle no escalation
	 */
	public void testHandleNoEscalation() {

		// Clear escalation, so no escalation
		this.escalation = null;

		// Ensure handle escalation
		Closure<Boolean> isCallbackInvoked = new Closure<Boolean>(false);
		this.serviceInput.service(this.inputObject, (serviceEscalation) -> {
			assertNull("Should be no service escalation", serviceEscalation);

			// Clean up handler should be invoked before callback
			assertTrue("Should invoke clean up before service call back", this.isCleanupHandlingInvoked);

			// Ensure captured clean up escalation
			assertEquals("Should have no cleanup escalations", 0, this.cleanupEscalations.length);

			// Invoked call back
			isCallbackInvoked.value = true;
		});
		assertTrue("Should have callback invoked", isCallbackInvoked.value);
	}

	/**
	 * Mock input {@link ManagedObject}.
	 */
	public static class MockInput implements ManagedObject {

		@Override
		public Object getObject() throws Throwable {
			return this;
		}
	}

	/**
	 * Mock {@link ManagedFunction}.
	 */
	public static class MockSection {
		public void input(MockCleanupEscalationObject object) {
		}
	}

	/**
	 * Mock {@link ManagedObject} to trigger a {@link CleanupEscalation}.
	 */
	@TestSource
	public class MockCleanupEscalationObject extends AbstractManagedObjectSource<None, None> implements ManagedObject {

		@Override
		protected void loadSpecification(SpecificationContext context) {
		}

		@Override
		protected void loadMetaData(MetaDataContext<None, None> context) throws Exception {
			context.setObjectClass(MockCleanupEscalationObject.class);

			// Trigger clean up escalation
			context.getManagedObjectSourceContext().getRecycleFunction(new ManagedFunctionFactory<None, None>() {
				@Override
				public ManagedFunction<None, None> createManagedFunction() throws Throwable {
					return (context) -> {

						// Escalation from clean up
						if (ServiceInputHandleEscalationTest.this.escalation != null) {
							throw ServiceInputHandleEscalationTest.this.escalation;
						}
					};
				}
			});
		}

		@Override
		protected ManagedObject getManagedObject() throws Throwable {
			return this;
		}

		@Override
		public Object getObject() throws Throwable {
			return this;
		}
	}

}
