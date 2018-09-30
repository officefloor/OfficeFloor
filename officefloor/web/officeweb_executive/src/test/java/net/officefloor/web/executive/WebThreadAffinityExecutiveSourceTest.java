/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
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
package net.officefloor.web.executive;

import java.io.IOException;
import java.util.BitSet;
import java.util.concurrent.ThreadFactory;

import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.officefloor.OfficeFloorDeployer;
import net.officefloor.compile.spi.officefloor.OfficeFloorTeam;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.executive.ExecutionStrategy;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.source.ManagedObjectExecuteContext;
import net.officefloor.frame.api.managedobject.source.impl.AbstractManagedObjectSource;
import net.officefloor.frame.api.source.TestSource;
import net.officefloor.frame.impl.spi.team.ExecutorFixedTeamSource;
import net.officefloor.frame.test.Closure;
import net.officefloor.frame.test.ThreadSafeClosure;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.server.http.mock.MockHttpResponse;
import net.officefloor.server.http.mock.MockHttpServer;
import net.officefloor.web.compile.AbstractWebCompileTestCase;
import net.officefloor.web.executive.CpuCore.LogicalCpu;
import net.openhft.affinity.Affinity;

/**
 * Tests the {@link WebThreadAffinityExecutiveSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class WebThreadAffinityExecutiveSourceTest extends AbstractWebCompileTestCase {

	/**
	 * Initial {@link Affinity} to reset.
	 */
	private BitSet initialAffinity;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		// Capture initial affinity (to allow reset on tear down)
		this.initialAffinity = Affinity.getAffinity();

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

		// Continue tear down
		super.tearDown();
	}

	/**
	 * Ensure {@link ExecutionStrategy} provides affinity.
	 */
	public void testExecutionStrategyAffinity() throws Exception {

		TestThreadAffinityManagedObjectSource mos = new TestThreadAffinityManagedObjectSource();
		this.compile.web((context) -> {
			context.getOfficeArchitect().addOfficeManagedObjectSource("MOS", mos);
		});
		this.officeFloor = this.compile.compileAndOpenOfficeFloor();

		// Ensure have affinity
		assertNotNull("Should have thread factories", mos.threadFactories);
		assertEquals("Incorrect number of thread factories", Runtime.getRuntime().availableProcessors(),
				mos.threadFactories.length);

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

		@Override
		protected void loadSpecification(SpecificationContext context) {
		}

		@Override
		protected void loadMetaData(MetaDataContext<None, None> context) throws Exception {
			context.setObjectClass(this.getClass());
			context.addExecutionStrategy().setLabel("EXECUTION_STRATEGY");
		}

		@Override
		public void start(ManagedObjectExecuteContext<None> context) throws Exception {
			this.threadFactories = context.getExecutionStrategy(0);
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