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

/**
 * {@link Thread} safe {@link CompleteFlowCallback}.
 * 
 * @author Daniel Sagenschneider
 */
public class SafeCompleteFlowCallback extends CompleteFlowCallback {

	/**
	 * {@link Thread} used for completion.
	 */
	private volatile Thread completionThread = null;

	/**
	 * Waits until {@link FlowCallback} is complete.
	 * 
	 * @param maxWaitTimeInMilliseconds Maximum wait time in milliseconds.
	 */
	public synchronized Thread waitUntilComplete(int maxWaitTimeInMilliseconds) {

		// Obtain the max time to run until
		long maxTime = System.currentTimeMillis() + maxWaitTimeInMilliseconds;

		// Loop until completes (or times out)
		while (!this.isComplete) {

			// Determine if timed out
			Assert.assertFalse(
					"Timed out waiting for callback to complete (" + maxWaitTimeInMilliseconds + " milliseconds)",
					System.currentTimeMillis() > maxTime);

			// Wait some time
			try {
				this.wait((maxWaitTimeInMilliseconds / 10) + 1);
			} catch (InterruptedException ex) {
				Assert.fail("Should not interrupt wait");
			}
		}

		// Return the completion thread
		return this.completionThread;
	}

	/*
	 * ================ CompleteFlowCallback ================
	 */

	@Override
	public synchronized void assertComplete() throws Exception {
		super.assertComplete();
	}

	@Override
	public synchronized void assertNotComplete() throws Exception {
		super.assertNotComplete();
	}

	@Override
	public synchronized void run(Throwable escalation) throws Throwable {

		// Capture the completion thread
		this.completionThread = Thread.currentThread();

		// Undertake completion
		super.run(escalation);

		// Notify immediately that complete
		this.notifyAll();
	}

}
