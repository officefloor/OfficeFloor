/*-
 * #%L
 * Web Executive
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

package net.officefloor.web.executive;

import java.io.IOException;
import java.util.BitSet;
import java.util.concurrent.ThreadFactory;

import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.officefloor.OfficeFloorDeployer;
import net.officefloor.compile.spi.officefloor.OfficeFloorTeam;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.executive.ExecutionStrategy;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.source.ManagedObjectExecuteContext;
import net.officefloor.frame.api.managedobject.source.impl.AbstractManagedObjectSource;
import net.officefloor.frame.api.source.TestSource;
import net.officefloor.frame.impl.spi.team.ExecutorFixedTeamSource;
import net.officefloor.frame.test.Closure;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.frame.test.ThreadSafeClosure;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.server.http.mock.MockHttpResponse;
import net.officefloor.server.http.mock.MockHttpServer;
import net.officefloor.web.compile.WebCompileOfficeFloor;
import net.officefloor.web.executive.CpuCore.LogicalCpu;
import net.openhft.affinity.Affinity;
import net.openhft.affinity.AffinityLock;

/**
 * Tests the {@link WebThreadAffinityExecutiveSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class WebThreadAffinityExecutiveSourceTest extends OfficeFrameTestCase {

	/**
	 * Indicates if {@link Affinity} is active.
	 */
	private static boolean isThreadAffinityActive = WebThreadAffinityExecutiveSource.isThreadAffinityAvailable();

	/**
	 * {@link WebCompileOfficeFloor}.
	 */
	protected final WebCompileOfficeFloor compile = new WebCompileOfficeFloor();

	/**
	 * {@link MockHttpServer}.
	 */
	protected MockHttpServer server;

	/**
	 * {@link OfficeFloor}.
	 */
	protected OfficeFloor officeFloor;

	/**
	 * Initial {@link Affinity} to reset.
	 */
	private BitSet initialAffinity;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		// Configure mock server
		this.compile.mockHttpServer((server) -> this.server = server);

		// Capture initial affinity (to allow reset on tear down)
		this.initialAffinity = Affinity.getAffinity();

		// Determine if affinity available
		if (!isThreadAffinityActive) {
			System.err.println("WARNING: Thread Affinity not available.  Tests not being run");
		}

		// Provide web thread affinity
		this.compile.officeFloor((context) -> {
			OfficeFloorDeployer deployer = context.getOfficeFloorDeployer();

			// Provide team for servicing
			deployer.enableAutoWireTeams();
			OfficeFloorTeam team = deployer.addTeam("TEAM", ExecutorFixedTeamSource.class.getName());
			team.setTeamSize(50);
			team.addTypeQualification(null, ServerHttpConnection.class.getName());

			// Web Thread Affinity configured with extension
		});
	}

	@Override
	protected void tearDown() throws Exception {

		// Reset the thread affinity
		Affinity.setAffinity(this.initialAffinity);

		// Ensure close OfficeFloor
		if (this.officeFloor != null) {
			this.officeFloor.closeOfficeFloor();
		}

		// Continue tear down
		super.tearDown();
	}

	/**
	 * Ensure {@link ExecutionStrategy} provides affinity.
	 */
	public void testExecutionStrategyAffinity() throws Exception {

		// Ensure active for valid test
		if (!isThreadAffinityActive) {
			System.err.println("Not running " + this.getName());
			return;
		}

		TestThreadAffinityManagedObjectSource mos = new TestThreadAffinityManagedObjectSource();
		this.compile.web((context) -> {
			context.getOfficeArchitect().addOfficeManagedObjectSource("MOS", mos);
		});
		this.officeFloor = this.compile.compileAndOpenOfficeFloor();

		// Determine number of CPUs
		int cpuCount = AffinityLock.cpuLayout().cpus();

		// Ensure have affinity
		assertNotNull("Should have thread factories", mos.threadFactories);
		assertEquals("Incorrect number of thread factories", cpuCount, mos.threadFactories.length);

		// Ensure threads are bound to respective CPUs
		int index = 0;
		for (CpuCore core : CpuCore.getCores()) {
			for (LogicalCpu cpu : core.getCpus()) {

				// Ensure thread factory provide CPU affinity
				ThreadSafeClosure<BitSet> affinity = new ThreadSafeClosure<>();
				mos.threadFactories[index].newThread(() -> {
					affinity.set(Affinity.getAffinity());
				}).start();
				this.waitForTrue(() -> affinity.get() != null);

				// Ensure thread bound to appropriate cpu
				assertEquals("Incorrect affinity", cpu.getCpuAffinity(), affinity.get());

				// Increment for next cpu
				index++;
			}
		}
	}

	@TestSource
	private static class TestThreadAffinityManagedObjectSource extends AbstractManagedObjectSource<None, None> {

		private ThreadFactory[] threadFactories;

		private int executionStrategyIndex;

		@Override
		protected void loadSpecification(SpecificationContext context) {
		}

		@Override
		protected void loadMetaData(MetaDataContext<None, None> context) throws Exception {
			context.setObjectClass(this.getClass());
			ExecutionLabeller execution = context.addExecutionStrategy();
			this.executionStrategyIndex = execution.getIndex();
			execution.setLabel("EXECUTION_STRATEGY");
		}

		@Override
		public void start(ManagedObjectExecuteContext<None> context) throws Exception {
			this.threadFactories = context.getExecutionStrategy(this.executionStrategyIndex);
		}

		@Override
		protected ManagedObject getManagedObject() throws Throwable {
			fail("Should not require managed object");
			return null;
		}
	}

	/**
	 * Ensure function run with affinity.
	 */
	public void testTeamAffinity() throws Exception {

		// Ensure active for valid test
		if (!isThreadAffinityActive) {
			System.err.println("Not running " + this.getName());
			return;
		}

		this.compile.web((context) -> {
			OfficeArchitect architect = context.getOfficeArchitect();

			// Configure team
			architect.enableAutoWireTeams();
			architect.addOfficeTeam("TEAM").addTypeQualification(null, ServerHttpConnection.class.getName());

			// Configure web handling
			context.link(false, "/path", EnsureThreadAffinity.class);
		});
		this.officeFloor = this.compile.compileAndOpenOfficeFloor();

		// Consumer to ensure core affinity
		TriConsumer<String, BitSet, Closure<BitSet>> assertCoreAffinity = (message, bitSet, previous) -> {
			boolean isBoundToCore = false;
			for (CpuCore core : CpuCore.getCores()) {
				if (bitSet.equals(core.getCoreAffinity())) {
					isBoundToCore = true;
				}
			}
			assertTrue(message + " should be bound to a core", isBoundToCore);
			if (previous.value == null) {
				previous.value = bitSet;
			}
			assertEquals(message + " change affinity from previous send", previous.value, bitSet);
		};

		// Ensure on multiple calls that uses same affinity
		Closure<BitSet> previousExecuteAffinity = new Closure<>();
		Closure<BitSet> previousCurrentAffinity = new Closure<>();
		for (int i = 0; i < 10; i++) {

			// Service request and capture affinity
			EnsureThreadAffinity.executingThread = null;
			EnsureThreadAffinity.affinity = null;
			MockHttpResponse response = this.server.send(MockHttpServer.mockRequest("/path"));
			response.assertResponse(200, "TEST");

			// Ensure have affinity
			assertNotNull("Should have executing thread", EnsureThreadAffinity.executingThread);
			assertNotSame("Should be executed with different thread", Thread.currentThread(),
					EnsureThreadAffinity.executingThread);
			assertNotNull("Should have affinity", EnsureThreadAffinity.affinity);

			// Ensure thread bound to only one core
			assertCoreAffinity.accept("Executing thread", EnsureThreadAffinity.affinity, previousExecuteAffinity);

			// Ensure current thread bound also (so repeat calls keep using warm CPU cache)
			assertCoreAffinity.accept("Current thread", Affinity.getAffinity(), previousCurrentAffinity);
		}
	}

	@FunctionalInterface
	private static interface TriConsumer<T, U, V> {
		void accept(T t, U u, V v);
	}

	public static class EnsureThreadAffinity {

		private static volatile Thread executingThread;

		private static volatile BitSet affinity;

		public static void service(ServerHttpConnection connection) throws IOException {
			executingThread = Thread.currentThread();
			affinity = Affinity.getAffinity();
			connection.getResponse().getEntityWriter().write("TEST");
		}
	}

}
