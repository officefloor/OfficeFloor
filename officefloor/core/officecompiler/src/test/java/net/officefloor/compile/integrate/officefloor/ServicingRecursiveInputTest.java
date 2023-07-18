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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.Arrays;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import net.officefloor.compile.impl.ApplicationOfficeFloorSource;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.office.OfficeTeam;
import net.officefloor.compile.spi.officefloor.DeployedOffice;
import net.officefloor.compile.spi.officefloor.ExternalServiceInput;
import net.officefloor.compile.spi.officefloor.OfficeFloorDeployer;
import net.officefloor.compile.test.officefloor.CompileOfficeFloor;
import net.officefloor.frame.api.function.AsynchronousFlow;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.impl.spi.team.ThreadLocalAwareTeamSource;
import net.officefloor.frame.test.TestSupportExtension;
import net.officefloor.frame.test.ThreadedTestSupport;
import net.officefloor.plugin.clazz.Qualified;

/**
 * Ensure appropriate sequencing of service recursive
 * {@link ExternalServiceInput} calls.
 * 
 * @author Daniel Sagenschneider
 */
@ExtendWith(TestSupportExtension.class)
public class ServicingRecursiveInputTest {

	public enum Action {
		START_EXTERNAL, INTERNAL, INTERNAL_COMPLETE, END_EXTERNAL, EXTERNAL_COMPLETE
	}

	/**
	 * Ensure completion order is respected when synchronous.
	 */
	@Test
	public void synchronous() throws Exception {
		this.doTest(SynchronousSection.class, false, Action.START_EXTERNAL, Action.INTERNAL, Action.INTERNAL_COMPLETE,
				Action.END_EXTERNAL, Action.EXTERNAL_COMPLETE);
	}

	/**
	 * Ensure completion order is respected when asynchronous.
	 */
	@Test
	public void asynchronous() throws Exception {
		this.doTest(AsynchronousSection.class, false, Action.START_EXTERNAL, Action.END_EXTERNAL,
				Action.EXTERNAL_COMPLETE, Action.INTERNAL, Action.INTERNAL_COMPLETE);
	}

	/**
	 * Ensure completion order is respected when asynchronous.
	 */
	@Test
	public void asynchronousThreadAwareLocalTeam() throws Exception {
		// Uses same internal team, so does not block internal service input
		this.doTest(AsynchronousSection.class, true, Action.START_EXTERNAL, Action.END_EXTERNAL,
				Action.EXTERNAL_COMPLETE, Action.INTERNAL, Action.INTERNAL_COMPLETE);
	}

	public static class SynchronousSection {

		public void externalServiceInput(@Qualified(EXTERNAL) ServiceInputObject object) {
			object.completions.add(Action.START_EXTERNAL);
			object.internalServiceInput.service(object, (ex) -> {
				object.completions.add(Action.INTERNAL_COMPLETE);
				isInternalComplete = true;
			});
			object.completions.add(Action.END_EXTERNAL);
		}

		public void internalServiceInput(@Qualified(INTERNAL) ServiceInputObject object) {
			object.completions.add(Action.INTERNAL);
		}
	}

	public static class AsynchronousSection {

		public void externalServiceInput(@Qualified(EXTERNAL) ServiceInputObject object) {
			object.completions.add(Action.START_EXTERNAL);
			object.internalServiceInput.service(object, (ex) -> {
				object.completions.add(Action.INTERNAL_COMPLETE);
				isInternalComplete = true;
			});
			object.completions.add(Action.END_EXTERNAL);
		}

		public void internalServiceInput(@Qualified(INTERNAL) ServiceInputObject object, AsynchronousFlow async) {
			new Thread(() -> {

				// Give chance for synchronous code to complete
				try {
					Thread.sleep(10);
				} catch (Exception ex) {
					// Ignore
				}

				// Asynchronously complete
				async.complete(() -> {
					object.completions.add(Action.INTERNAL);
				});

			}).start();
		}
	}

	/**
	 * {@link ThreadedTestSupport}.
	 */
	private static final ThreadedTestSupport threaded = new ThreadedTestSupport();

	/**
	 * External.
	 */
	private static final String EXTERNAL = "EXTERNAL";

	/**
	 * Internal.
	 */
	private static final String INTERNAL = "INTERNAL";

	/**
	 * Ensure wait until external complete.
	 */
	private static volatile boolean isExternalComplete = false;

	/**
	 * Ensure wait until internal complete.
	 */
	private static volatile boolean isInternalComplete = false;

	/**
	 * {@link OfficeFloor}.
	 */
	private OfficeFloor officeFloor;

	@SuppressWarnings("unchecked")
	private void doTest(Class<?> sectionClass, boolean isThreadAwareTeam, Action... expectedOrder) throws Exception {

		// Capture the external service input
		ExternalServiceInput<ServiceInputObject, ServiceInputObject>[] externalServiceInput = new ExternalServiceInput[1];
		ExternalServiceInput<ServiceInputObject, ServiceInputObject>[] internalServiceInput = new ExternalServiceInput[1];

		// Compile the OfficeFloor with extension
		CompileOfficeFloor compile = new CompileOfficeFloor();
		compile.officeFloor((context) -> {

			// Obtain the existing office (added above)
			OfficeFloorDeployer deployer = context.getOfficeFloorDeployer();
			DeployedOffice office = deployer.getDeployedOffice(ApplicationOfficeFloorSource.OFFICE_NAME);

			// External service input
			externalServiceInput[0] = office.getDeployedOfficeInput("SECTION", "externalServiceInput")
					.addExternalServiceInput(ServiceInputObject.class, EXTERNAL, ServiceInputObject.class,
							(inputManagedObject, escalations) -> fail("External should have no escalations"));

			// External service input
			internalServiceInput[0] = office.getDeployedOfficeInput("SECTION", "internalServiceInput")
					.addExternalServiceInput(ServiceInputObject.class, INTERNAL, ServiceInputObject.class,
							(inputManagedObject, escalations) -> fail("Internal should have no escalations"));

			// Configure thread aware local team
			if (isThreadAwareTeam) {

				// Add the Thread aware team
				deployer.link(office.getDeployedOfficeTeam("THREAD_LOCAL_AWARE"),
						deployer.addTeam("THREAD_LOCAL_AWARE", ThreadLocalAwareTeamSource.class.getName()));

			}
		});
		compile.office((context) -> {

			// Configure the section
			OfficeSection section = context.addSection("SECTION", sectionClass);

			// Configure thread aware local team
			if (isThreadAwareTeam) {

				// Configure blocking until serviced
				OfficeTeam team = context.getOfficeArchitect().addOfficeTeam("THREAD_LOCAL_AWARE");
				context.getOfficeArchitect()
						.link(section.getOfficeSectionFunction("externalServiceInput").getResponsibleTeam(), team);
				context.getOfficeArchitect()
						.link(section.getOfficeSectionFunction("internalServiceInput").getResponsibleTeam(), team);
			}
		});
		this.officeFloor = compile.compileAndOpenOfficeFloor();

		// Undertake request
		ServiceInputObject input = new ServiceInputObject(internalServiceInput[0]);

		// Invoke the process
		isExternalComplete = false;
		isInternalComplete = false;
		externalServiceInput[0].service(input, (ex) -> {
			input.completions.add(Action.EXTERNAL_COMPLETE);
			isExternalComplete = true;
		});

		// Wait until complete
		threaded.waitForTrue(() -> isInternalComplete);
		threaded.waitForTrue(() -> isExternalComplete);

		// Ensure appropriate order of completion
		String expectedOrderText = String.join(",",
				Arrays.asList(expectedOrder).stream().map((action) -> action.name()).toList());
		String actualOrderText = String.join(",", input.completions.stream().map((action) -> action.name()).toList());
		assertEquals(expectedOrderText, actualOrderText, "Incorrect order of execution");
	}

	@AfterEach
	protected void tearDown() throws Exception {
		if (this.officeFloor != null) {
			this.officeFloor.closeOfficeFloor();
		}
	}

	public static class ServiceInputObject implements ManagedObject {

		private final ExternalServiceInput<ServiceInputObject, ServiceInputObject> internalServiceInput;

		public final Queue<Action> completions = new ConcurrentLinkedQueue<>();

		private ServiceInputObject(ExternalServiceInput<ServiceInputObject, ServiceInputObject> internalServiceInput) {
			this.internalServiceInput = internalServiceInput;
		}

		@Override
		public Object getObject() throws Throwable {
			return this;
		}
	}

}
