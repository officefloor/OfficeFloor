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

package net.officefloor.frame.impl.construct.executive;

import static org.junit.jupiter.api.Assertions.fail;

import java.util.concurrent.Executor;

import net.officefloor.frame.api.executive.ExecutionStrategy;
import net.officefloor.frame.api.executive.Executive;
import net.officefloor.frame.api.executive.ExecutiveOfficeContext;
import net.officefloor.frame.api.executive.ExecutiveStartContext;
import net.officefloor.frame.api.executive.ProcessIdentifier;
import net.officefloor.frame.api.executive.TeamOversight;
import net.officefloor.frame.api.executive.source.ExecutiveSource;
import net.officefloor.frame.api.executive.source.ExecutiveSourceContext;
import net.officefloor.frame.api.executive.source.ExecutiveSourceSpecification;
import net.officefloor.frame.api.manage.ProcessManager;
import net.officefloor.frame.api.source.TestSource;
import net.officefloor.frame.internal.structure.Execution;
import net.officefloor.frame.internal.structure.OfficeManager;

/**
 * Adapter providing empty {@link ExecutiveSource} methods.
 * 
 * @author Daniel Sagenschneider
 */
@TestSource
public class ExecutiveSourceAdapter implements ExecutiveSource, Executive {

	/*
	 * =============== ExecutiveSource =====================
	 */

	@Override
	public ExecutiveSourceSpecification getSpecification() {
		return null;
	}

	@Override
	public Executive createExecutive(ExecutiveSourceContext context) throws Exception {
		return this;
	}

	/*
	 * ================== Executive ========================
	 */

	@Override
	public Thread createThread(String threadName, ThreadGroup threadGroup, Runnable runnable) {
		return fail("Should not be invoked");
	}

	@Override
	public <T extends Throwable> ProcessManager manageExecution(Execution<T> execution) throws T {
		return fail("Should not be invoked");
	}

	@Override
	public ExecutionStrategy[] getExcutionStrategies() {
		return fail("Should not be invoked");
	}

	@Override
	public TeamOversight getTeamOversight() {
		return fail("Should not be invoked");
	}

	@Override
	public void startManaging(ExecutiveStartContext context) throws Exception {
		fail("Should not be invoked");
	}

	@Override
	public ProcessIdentifier createProcessIdentifier(ExecutiveOfficeContext context) {
		return fail("Should not be invoked");
	}

	@Override
	public OfficeManager getOfficeManager(ProcessIdentifier processIdentifier, OfficeManager defaultOfficeManager) {
		return fail("Should not be invoked");
	}

	@Override
	public Executor createExecutor(ProcessIdentifier processIdentifier) {
		return fail("Should not be invoked");
	}

	@Override
	public void schedule(ProcessIdentifier processIdentifier, long delay, Runnable runnable) {
		fail("Should not be invoked");
	}

	@Override
	public void processComplete(ProcessIdentifier processIdentifier) {
		fail("Should not be invoked");
	}

	@Override
	public void stopManaging() throws Exception {
		fail("Should not be invoked");
	}

}
