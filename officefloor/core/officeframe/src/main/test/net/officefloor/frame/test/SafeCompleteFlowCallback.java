/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2017 Daniel Sagenschneider
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
	 * Waits until {@link FlowCallback} is complete.
	 * 
	 * @param maxWaitTimeInMilliseconds
	 *            Maximum wait time in milliseconds.
	 */
	public synchronized void waitUntilComplete(int maxWaitTimeInMilliseconds) {

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

		// Undertake completion
		super.run(escalation);

		// Notify immediately that complete
		this.notifyAll();
	}

}