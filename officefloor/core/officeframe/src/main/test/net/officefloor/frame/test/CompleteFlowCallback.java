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

package net.officefloor.frame.test;

import org.junit.Assert;

import net.officefloor.frame.api.function.FlowCallback;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.ThreadState;

/**
 * {@link FlowCallback} that checks on completion and propagates failures.
 *
 * @author Daniel Sagenschneider
 */
public class CompleteFlowCallback implements FlowCallback {

	/**
	 * Indicates if {@link Flow} is complete.
	 */
	protected volatile boolean isComplete = false;

	/**
	 * Possible failure of {@link Flow}.
	 */
	private volatile Throwable failure = null;

	/**
	 * Asserts the {@link Flow} is complete.
	 * 
	 * @throws Exception If failure in execution.
	 */
	public void assertComplete() throws Exception {
		this.ensureNoFailure();
		Assert.assertTrue("Flow should be complete", this.isComplete);
	}

	/**
	 * Asserts the {@link Flow} is complete.
	 * 
	 * @param threading {@link ThreadedTestSupport} to wait for completion.
	 *                  Necessary if spawning {@link ThreadState}.
	 * @throws Exception If failure in execution.
	 */
	public void assertComplete(ThreadedTestSupport threading) throws Exception {
		threading.waitForTrue(() -> this.isComplete);
		this.ensureNoFailure();
		Assert.assertTrue("Flow should be complete", this.isComplete);
	}

	/**
	 * Asserts the {@link Flow} is not complete.
	 * 
	 * @throws Exception If failure in execution.
	 */
	public void assertNotComplete() throws Exception {
		this.ensureNoFailure();
		Assert.assertFalse("Flow should not be complete", this.isComplete);
	}

	/**
	 * Asserts the {@link Flow} is not complete.
	 * 
	 * @param threading {@link ThreadedTestSupport} to wait for completion.
	 *                  Necessary if spawning {@link ThreadState}.
	 * @throws Exception If failure in execution.
	 */
	public void assertNotComplete(ThreadedTestSupport threading) throws Exception {
		threading.waitForTrue(() -> this.isComplete);
		this.ensureNoFailure();
		Assert.assertFalse("Flow should not be complete", this.isComplete);
	}

	/**
	 * Ensure no failure in {@link Flow}.
	 */
	private void ensureNoFailure() throws Exception {
		if (this.failure != null) {
			throw OfficeFrameTestCase.fail(this.failure);
		}
	}

	/*
	 * ==================== FlowCallback ========================
	 */

	@Override
	public void run(Throwable escalation) throws Throwable {
		Assert.assertFalse("Flow already flagged as complete", this.isComplete);
		this.failure = escalation;
		this.isComplete = true;
	}

}
