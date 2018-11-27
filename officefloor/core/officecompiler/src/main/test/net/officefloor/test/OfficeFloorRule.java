/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.officefloor.test;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.manage.FunctionManager;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.internal.structure.ProcessState;

/**
 * {@link TestRule} for running {@link OfficeFloor} around tests.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeFloorRule implements TestRule {

	/**
	 * {@link OfficeFloor}.
	 */
	private OfficeFloor officeFloor = null;

	/**
	 * Obtains the {@link OfficeFloor}.
	 * 
	 * @return {@link OfficeFloor}.
	 */
	public OfficeFloor getOfficeFloor() {
		if (this.officeFloor == null) {
			throw new IllegalStateException("OfficeFloor only available within test");
		}
		return this.officeFloor;
	}

	/**
	 * Convenience method to invoke the {@link ProcessState} for the
	 * {@link ManagedFunction} within the default {@link Office}.
	 * 
	 * @param functionName Name of the {@link ManagedFunction}.
	 * @param parameter    Parameter to the {@link ManagedFunction}.
	 */
	public void invokeProcess(String functionName, Object parameter) {
		this.invokeProcess(functionName, parameter, 3000);
	}

	/**
	 * Convenience method to invoke the {@link ProcessState} for the
	 * {@link ManagedFunction} within the default {@link Office}.
	 * 
	 * @param functionName Name of the {@link ManagedFunction}.
	 * @param parameter    Parameter to the {@link ManagedFunction}.
	 * @param waitTime     Time in milliseconds to wait for {@link ProcessState} to
	 *                     complete.
	 */
	public void invokeProcess(String functionName, Object parameter, long waitTime) {
		this.invokeProcess("OFFICE", functionName, parameter, waitTime);
	}

	/**
	 * Convenience method to invoke the {@link ProcessState} for the
	 * {@link ManagedFunction}.
	 * 
	 * @param officeName   Name of the {@link Office} containing the
	 *                     {@link ManagedFunction}.
	 * @param functionName Name of the {@link ManagedFunction}.
	 * @param parameter    Parameter to the {@link ManagedFunction}.
	 * @param waitTime     Time in milliseconds to wait for {@link ProcessState} to
	 *                     complete.
	 */
	public void invokeProcess(String officeName, String functionName, Object parameter, long waitTime) {

		// Obtain the OfficeFloor
		OfficeFloor officeFloor = this.getOfficeFloor();

		try {
			// Obtain the function
			FunctionManager function = officeFloor.getOffice(officeName).getFunctionManager(functionName);

			// Invoke the function (ensuring completes within reasonable time)
			long startTimestamp = System.currentTimeMillis();
			boolean[] isComplete = new boolean[] { false };
			Throwable[] failure = new Throwable[] { null };
			function.invokeProcess(parameter, (exception) -> {
				synchronized (isComplete) {
					failure[0] = exception;
					isComplete[0] = true;
					isComplete.notify(); // wake up immediately
				}
			});
			synchronized (isComplete) {
				while (!isComplete[0]) {

					// Determine if timed out
					long currentTimestamp = System.currentTimeMillis();
					if ((startTimestamp + waitTime) < currentTimestamp) {
						throw new Exception("Timed out waiting on process (" + officeName + "." + functionName
								+ ") to complete (" + (currentTimestamp - startTimestamp) + " milliseconds)");
					}

					// Sleep some time
					isComplete.wait(100);
				}

				// Determine if failure
				if (failure[0] != null) {
					throw failure[0];
				}
			}

		} catch (Throwable ex) {
			// Consider any start up failure to be invalid test
			if (ex instanceof RuntimeException) {
				throw (RuntimeException) ex;
			} else if (ex instanceof Error) {
				throw (Error) ex;
			} else {
				throw new RuntimeException(ex);
			}
		}
	}

	/*
	 * ================== TestRule ============================
	 */

	@Override
	public Statement apply(Statement base, Description description) {
		return new Statement() {

			@Override
			public void evaluate() throws Throwable {

				// Create and compile the OfficeFloor
				OfficeFloorCompiler compiler = OfficeFloorCompiler.newOfficeFloorCompiler(null);
				OfficeFloorRule.this.officeFloor = compiler.compile("OfficeFloor");
				try {
					OfficeFloorRule.this.officeFloor.openOfficeFloor();

					// Run the test
					base.evaluate();

				} finally {
					// Ensure close and clear the OfficeFloor
					try {
						OfficeFloorRule.this.officeFloor.closeOfficeFloor();
					} finally {
						OfficeFloorRule.this.officeFloor = null;
					}
				}
			}
		};
	}

}