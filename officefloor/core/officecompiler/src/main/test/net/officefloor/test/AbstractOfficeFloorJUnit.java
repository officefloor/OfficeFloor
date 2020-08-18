package net.officefloor.test;

import net.officefloor.frame.api.manage.FunctionManager;
import net.officefloor.frame.api.manage.OfficeFloor;

/**
 * Abstract JUnit functionality for {@link OfficeFloor}.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractOfficeFloorJUnit implements OfficeFloorJUnit {

	/**
	 * {@link OfficeFloor}.
	 */
	protected OfficeFloor officeFloor = null;

	/*
	 * ====================== OfficeFloorJUnit ==========================
	 */

	@Override
	public OfficeFloor getOfficeFloor() {
		if (this.officeFloor == null) {
			throw new IllegalStateException("OfficeFloor only available within test");
		}
		return this.officeFloor;
	}

	@Override
	public void invokeProcess(String functionName, Object parameter) {
		this.invokeProcess(functionName, parameter, 3000);
	}

	@Override
	public void invokeProcess(String functionName, Object parameter, long waitTime) {
		this.invokeProcess("OFFICE", functionName, parameter, waitTime);
	}

	@Override
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

}