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

package net.officefloor.compile.test.managedfunction;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import net.officefloor.frame.api.function.AsynchronousFlow;
import net.officefloor.frame.api.function.AsynchronousFlowCompletion;

/**
 * Mock {@link AsynchronousFlow}.
 * 
 * @author Daniel Sagenschneider
 */
public class MockAsynchronousFlow implements AsynchronousFlow {

	/**
	 * Indicates if complete.
	 */
	private boolean isComplete = false;

	/**
	 * {@link AsynchronousFlowCompletion}. May be <code>null</code> if not supplied.
	 */
	private AsynchronousFlowCompletion completion;

	/**
	 * Indicates if {@link AsynchronousFlow} is complete.
	 * 
	 * @return <code>true</code> if {@link AsynchronousFlow} is complete.
	 */
	public synchronized boolean isComplete() {
		return this.isComplete;
	}

	/**
	 * <p>
	 * Obtains the provided {@link AsynchronousFlowCompletion}.
	 * <p>
	 * Note: will fail if this {@link AsynchronousFlow} is not complete.
	 * 
	 * @return {@link AsynchronousFlowCompletion} or <code>null</code> if not
	 *         provided.
	 */
	public synchronized AsynchronousFlowCompletion getCompletion() {
		assertTrue("Flow not complete", this.isComplete);
		return this.completion;
	}

	/**
	 * Waits for completion.
	 * 
	 * @param timeToWait Time in milliseconds to wait.
	 * @return Provided {@link AsynchronousFlowCompletion}.
	 */
	public synchronized AsynchronousFlowCompletion waitOnCompletion(int timeToWait) {
		long endTime = System.currentTimeMillis() + timeToWait;
		while (!this.isComplete) {

			// Determine if complete
			if (endTime < System.currentTimeMillis()) {
				fail("Timed out after " + timeToWait + " milliseconds waiting on "
						+ AsynchronousFlow.class.getSimpleName() + " to complete");
			}

			// Wait some time
			try {
				this.wait(100);
			} catch (InterruptedException ex) {
				fail("Interrupted wait on completion");
			}
		}

		// Return the asynchronous flow completion
		return this.completion;
	}

	/**
	 * Waits a default period of time for completion.
	 * 
	 * @return Provided {@link AsynchronousFlowCompletion}.
	 */
	public AsynchronousFlowCompletion waitOnCompletion() {
		return this.waitOnCompletion(3000);
	}

	/*
	 * ================= AsynchronousFlow =======================
	 */

	@Override
	public synchronized void complete(AsynchronousFlowCompletion completion) {

		// Undertake completion
		assertFalse("Already completed " + AsynchronousFlow.class.getSimpleName(), this.isComplete);
		this.isComplete = true;
		this.completion = completion;

		// Notify complete
		this.notify();
	}

}
