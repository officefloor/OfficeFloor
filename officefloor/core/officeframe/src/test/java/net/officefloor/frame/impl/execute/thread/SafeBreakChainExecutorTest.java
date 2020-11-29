/*-
 * #%L
 * OfficeFrame
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package net.officefloor.frame.impl.execute.thread;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import net.officefloor.frame.api.executive.ProcessIdentifier;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.source.TestSource;
import net.officefloor.frame.api.team.Job;
import net.officefloor.frame.impl.execute.executive.DefaultExecutive;
import net.officefloor.frame.test.ConstructTestSupport;
import net.officefloor.frame.test.TestSupportExtension;

/**
 * <p>
 * Ensures fails start if unsafe break chain {@link Executor}.
 * <p>
 * Break chain {@link Executor} is checked for safety by running {@link Job} and
 * confirming run on different {@link Thread}.
 * 
 * @author Daniel Sagenschneider
 */
@ExtendWith(TestSupportExtension.class)
public class SafeBreakChainExecutorTest {

	private final ConstructTestSupport construct = new ConstructTestSupport();

	/**
	 * Ensure default {@link Executor} is safe.
	 */
	@Test
	public void safeDefaultExecutor() throws Exception {
		this.construct.constructOfficeFloor().openOfficeFloor();
	}

	/**
	 * Ensure provided safe {@link Executor} is ok.
	 */
	@Test
	public void safeBreakChainTeam() throws Exception {
		ExecutorService executorService = Executors.newCachedThreadPool();
		try {
			this.construct.getOfficeFloorBuilder().setExecutive(new MockExecutive(executorService));
			this.construct.constructOfficeFloor().openOfficeFloor();
		} finally {
			executorService.shutdown();
		}
	}

	/**
	 * Ensure provided unsafe {@link Executor} fails startup.
	 */
	@Test
	public void unsafeBreakChainTeam() throws Exception {

		// Should be safe to compile
		this.construct.getOfficeFloorBuilder().setExecutive(new MockExecutive((runnable) -> runnable.run()));
		OfficeFloor officeFloor = this.construct.constructOfficeFloor();

		// However, on startup should fail due to unsafe break chain team
		try {
			officeFloor.openOfficeFloor();
			fail("Should not successfully open");
		} catch (IllegalStateException ex) {
			assertEquals(
					"Break thread stack is not safe.  The configured Executive must not re-use the current Thread to break the thread stack.",
					ex.getMessage(), "Incorrect cause");
		}
	}

	@TestSource
	private static class MockExecutive extends DefaultExecutive {

		private final Executor executor;

		private MockExecutive(Executor executor) {
			this.executor = executor;
		}

		/*
		 * ===================== Executive ========================
		 */

		@Override
		public Executor createExecutor(ProcessIdentifier processIdentifier) {
			return this.executor;
		}
	}

}
