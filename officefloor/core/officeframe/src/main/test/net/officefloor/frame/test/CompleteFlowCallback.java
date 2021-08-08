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
