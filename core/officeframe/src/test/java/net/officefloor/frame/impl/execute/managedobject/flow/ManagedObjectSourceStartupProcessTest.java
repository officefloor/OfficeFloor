/*-
 * #%L
 * OfficeFrame
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

package net.officefloor.frame.impl.execute.managedobject.flow;

import static org.junit.Assert.assertNotEquals;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.officefloor.frame.api.build.ManagedObjectBuilder;
import net.officefloor.frame.api.build.ManagingOfficeBuilder;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.source.ManagedObjectExecuteContext;
import net.officefloor.frame.api.managedobject.source.ManagedObjectServiceContext;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.managedobject.source.ManagedObjectStartupProcess;
import net.officefloor.frame.api.managedobject.source.impl.AbstractManagedObjectSource;
import net.officefloor.frame.api.source.TestSource;
import net.officefloor.frame.api.team.Job;
import net.officefloor.frame.api.team.Team;
import net.officefloor.frame.api.team.TeamOverloadException;
import net.officefloor.frame.api.team.source.TeamSourceContext;
import net.officefloor.frame.api.team.source.impl.AbstractTeamSource;
import net.officefloor.frame.impl.execute.service.SafeManagedObjectService;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.frame.test.AbstractOfficeConstructTestCase;
import net.officefloor.frame.test.ReflectiveFunctionBuilder;

/**
 * Tests registering a startup {@link ProcessState} from
 * {@link ManagedObjectSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class ManagedObjectSourceStartupProcessTest extends AbstractOfficeConstructTestCase {

	/**
	 * Ensure startup {@link ProcessState} instances are invoked after all
	 * {@link ManagedObjectSource} instances and {@link Team} instances are started.
	 */
	public void testStartupProcess() throws Exception {
		this.doStartupProcessTest(false);
	}

	/**
	 * Ensure can concurrently run startup {@link ProcessState} instances.
	 */
	public void testConcurrentStartupProcess() throws Exception {
		this.doStartupProcessTest(true);
	}

	/**
	 * Undertakes start up process testing.
	 * 
	 * @param isConcurrent Indicates if concurrent start up.
	 */
	private void doStartupProcessTest(boolean isConcurrent) throws Exception {

		// Obtain Office name
		String officeName = this.getOfficeName();

		// Load the managed object sources
		MockStartupManagedObjectSource[] sources = new MockStartupManagedObjectSource[] {
				new MockStartupManagedObjectSource("One", isConcurrent),
				new MockStartupManagedObjectSource("Two", isConcurrent) };
		for (MockStartupManagedObjectSource source : sources) {

			// Construct the managed object
			ManagedObjectBuilder<Flows> moBuilder = this.constructManagedObject("Startup" + source.name, source, null);

			// Provide flow
			ManagingOfficeBuilder<Flows> managingOfficeBuilder = moBuilder.setManagingOffice(officeName);
			managingOfficeBuilder.setInputManagedObjectName("Input" + source.name);
			managingOfficeBuilder.linkFlow(Flows.FLOW, "handle");
		}

		// Provide team (to ensure processing after teams started)
		MockStartupTeamSource team = new MockStartupTeamSource();
		this.constructTeam("TEAM", team);

		// Provide function for managed object source input process
		MockWork handler = new MockWork(sources);
		ReflectiveFunctionBuilder function = this.constructFunction(handler, "handle");
		function.buildParameter();
		function.getBuilder().setResponsibleTeam("TEAM");

		// Build the OfficeFloor
		try (OfficeFloor officeFloor = this.constructOfficeFloor()) {

			// Ensure sources not yet started
			for (MockStartupManagedObjectSource source : sources) {
				assertFalse("Source " + source.name + " should not yet be started", source.isStarted);
			}

			// Open the OfficeFloor
			officeFloor.openOfficeFloor();

			// Ensure the sources are started
			for (MockStartupManagedObjectSource source : sources) {
				assertTrue("Source " + source.name + " not started", source.isStarted);
			}

			// Ensure invoked start up process
			for (MockStartupManagedObjectSource source : sources) {
				this.waitForTrue(() -> handler.startedSources.contains(source));
			}
			for (MockStartupManagedObjectSource source : sources) {
				assertTrue("Source " + source.name + " startup handler not invoked",
						handler.startedSources.remove(source));
			}
			assertEquals("Additional startup invocations", 0, handler.startedSources.size());

			// Ensure invoked via team (ensures team started before processing)
			assertTrue("Team should execute function", team.isAssignedJob);

			// Wait for start up completions
			for (MockStartupManagedObjectSource source : sources) {
				this.waitForTrue(() -> source.isStartupProcessComplete);
			}

			// Ensure start up process correctly executed
			for (MockStartupManagedObjectSource source : sources) {
				synchronized (handler.executingThreads) {
					if (isConcurrent) {
						// Should be started on another thread
						assertNotEquals("Start up process for source " + source.name + " should be on another thread",
								Thread.currentThread(), source.startupProcessThread);
						assertFalse("Handling method should be invoked by same thread",
								handler.executingThreads.contains(Thread.currentThread()));
						assertTrue("Should have other threads invoke", handler.executingThreads.size() > 0);
					} else {
						// Not concurrent, so should be same thread
						assertSame("Should be same startup thread for source " + source.name, Thread.currentThread(),
								source.startupProcessThread);
						assertTrue("Handling method should be invoked by this thread",
								handler.executingThreads.contains(Thread.currentThread()));
						assertEquals("Should be no other threads invoking start up processes", 1,
								handler.executingThreads.size());
					}
				}
			}

			// Ensure no longer able to invoke start up process
			MockStartupManagedObjectSource source = sources[0];
			try {
				source.executeContext.invokeStartupProcess(Flows.FLOW, null, source, null);
				fail("Should not be able to start up process now running");
			} catch (IllegalStateException ex) {
				assertEquals("Incorrect cause", "May only register start up processes during start(...) method",
						ex.getMessage());
			}

			// Ensure, however, now able to invoke processes
			source.serviceContext.invokeProcess(Flows.FLOW, source, source, 0, null);
			assertEquals("Should invoke process", 1, handler.startedSources.size());
			assertSame("Incorrect source for invoked process", source, handler.startedSources.get(0));
		}
	}

	public static class MockWork {

		private final MockStartupManagedObjectSource[] sources;

		private final List<MockStartupManagedObjectSource> startedSources = new ArrayList<>(2);

		private Set<Thread> executingThreads = new HashSet<>();

		private MockWork(MockStartupManagedObjectSource[] sources) {
			this.sources = sources;
		}

		public void handle(MockStartupManagedObjectSource startedSource) {

			// Capture the executing thread
			synchronized (this.executingThreads) {
				this.executingThreads.add(Thread.currentThread());
			}

			// Ensure all sources are started
			for (MockStartupManagedObjectSource source : this.sources) {
				assertTrue("Managed Object Source " + source.name + " should be started", source.isStarted);
			}

			// Add the source as started
			this.startedSources.add(startedSource);
		}
	}

	private static enum Flows {
		FLOW
	}

	@TestSource
	private static class MockStartupManagedObjectSource extends AbstractManagedObjectSource<None, Flows>
			implements ManagedObject {

		private final String name;

		private final boolean isConcurrent;

		private boolean isStarted = false;

		private volatile Thread startupProcessThread = null;

		private volatile boolean isStartupProcessComplete = false;

		private ManagedObjectExecuteContext<Flows> executeContext;

		private ManagedObjectServiceContext<Flows> serviceContext;

		private MockStartupManagedObjectSource(String name, boolean isConcurrent) {
			this.name = name;
			this.isConcurrent = isConcurrent;
		}

		/*
		 * ================= ManagedObjectSource =====================
		 */

		@Override
		protected void loadSpecification(SpecificationContext context) {
			// No specification
		}

		@Override
		protected void loadMetaData(MetaDataContext<None, Flows> context) throws Exception {
			context.setObjectClass(MockStartupManagedObjectSource.class);
			context.addFlow(Flows.FLOW, MockStartupManagedObjectSource.class);
		}

		@Override
		public void start(ManagedObjectExecuteContext<Flows> context) throws Exception {
			this.executeContext = context;
			this.serviceContext = new SafeManagedObjectService<>(context);

			// Ensure can register startup processes
			ManagedObjectStartupProcess startup = context.invokeStartupProcess(Flows.FLOW, this, this, (error) -> {
				this.startupProcessThread = Thread.currentThread();
				this.isStartupProcessComplete = true;
			});

			// Flag appropriately concurrent
			startup.setConcurrent(this.isConcurrent);

			// Indicate started
			this.isStarted = true;
		}

		@Override
		protected ManagedObject getManagedObject() throws Throwable {
			return this;
		}

		/*
		 * ===================== ManagedObject =========================
		 */

		@Override
		public Object getObject() throws Throwable {
			return this;
		}
	}

	@TestSource
	private static class MockStartupTeamSource extends AbstractTeamSource implements Team {

		private boolean isStarted = false;

		private boolean isAssignedJob = false;

		/*
		 * =================== TeamSource =========================
		 */

		@Override
		protected void loadSpecification(SpecificationContext context) {
			// No specification
		}

		@Override
		public Team createTeam(TeamSourceContext context) throws Exception {
			return this;
		}

		/*
		 * ======================= Team ============================
		 */

		@Override
		public void startWorking() {
			this.isStarted = true;
		}

		@Override
		public void assignJob(Job job) throws TeamOverloadException, Exception {
			assertTrue("Team must be started before assigning job", this.isStarted);
			this.isAssignedJob = true;
			job.run();
		}

		@Override
		public void stopWorking() {
			// Do nothing
		}
	}

}
