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

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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

/**
 * Ensure {@link ExternalServiceInput} can handle {@link CleanupEscalation}
 * instances.
 * 
 * @author Daniel Sagenschneider
 */
public class ServiceInputHandleEscalationTest {

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

	@BeforeEach
	protected void setUp() throws Exception {

		// Compile the OfficeFloor with extension
		CompileOfficeFloor compile = new CompileOfficeFloor();
		compile.officeFloor((extension) -> {

			// Configure the input
			DeployedOffice office = extension.getDeployedOffice();
			this.serviceInput = office.getDeployedOfficeInput("SECTION", "input").addExternalServiceInput(
					MockInput.class, MockInput.class, (inputManagedObject, cleanupEscalations) -> {
						assertSame(this.inputObject, inputManagedObject, "Incorrect clean up managed object");
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

	@AfterEach
	protected void tearDown() throws Exception {

		// Close the OfficeFloor
		if (this.officeFloor != null) {
			this.officeFloor.closeOfficeFloor();
		}
	}

	/**
	 * Ensure can handle {@link CleanupEscalation}.
	 */
	@Test
	public void handleEscalation() {
		Closure<Boolean> isCallbackInvoked = new Closure<Boolean>(false);
		this.serviceInput.service(this.inputObject, (serviceEscalation) -> {
			assertNull(serviceEscalation, "Should be no service escalation");

			// Clean up handler should be invoked before callback
			assertTrue(this.isCleanupHandlingInvoked, "Should invoke clean up before service call back");

			// Ensure captured clean up escalation
			assertNotNull(this.cleanupEscalations, "Should have cleanup escalations");
			assertEquals(1, this.cleanupEscalations.length, "Incorrect number of cleanup escalations");

			// Ensure correct clean up escalation
			CleanupEscalation cleanupEscalation = this.cleanupEscalations[0];
			assertEquals(MockCleanupEscalationObject.class, cleanupEscalation.getObjectType(), "Incorrect object type");
			assertSame(this.escalation, cleanupEscalation.getEscalation(), "Incorrect escalation");

			// Invoked call back
			isCallbackInvoked.value = true;
		});
		assertTrue(isCallbackInvoked.value, "Should have callback invoked");
	}

	/**
	 * Ensure handle no escalation
	 */
	@Test
	public void handleNoEscalation() {

		// Clear escalation, so no escalation
		this.escalation = null;

		// Ensure handle escalation
		Closure<Boolean> isCallbackInvoked = new Closure<Boolean>(false);
		this.serviceInput.service(this.inputObject, (serviceEscalation) -> {
			assertNull(serviceEscalation, "Should be no service escalation");

			// Clean up handler should be invoked before callback
			assertTrue(this.isCleanupHandlingInvoked, "Should invoke clean up before service call back");

			// Ensure captured clean up escalation
			assertEquals(0, this.cleanupEscalations.length, "Should have no cleanup escalations");

			// Invoked call back
			isCallbackInvoked.value = true;
		});
		assertTrue(isCallbackInvoked.value, "Should have callback invoked");
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
