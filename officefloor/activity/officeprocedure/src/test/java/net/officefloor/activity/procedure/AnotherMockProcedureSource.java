package net.officefloor.activity.procedure;

import java.lang.reflect.Method;

import net.officefloor.activity.procedure.spi.ProcedureSource;
import net.officefloor.activity.procedure.spi.ProcedureListContext;
import net.officefloor.activity.procedure.spi.ProcedureMethodContext;

/**
 * Another mock {@link ProcedureSource} for testing.
 * 
 * @author Daniel Sagenschneider
 */
public class AnotherMockProcedureSource extends MockProcedureSource {

	/**
	 * Indicates whether to run.
	 */
	private static boolean isRun = false;

	/**
	 * Runs the mock {@link Logic}.
	 * 
	 * @param <R>   Result type.
	 * @param <T>   Possible failure type.
	 * @param logic {@link Logic}.
	 * @return Result.
	 * @throws T Possible failure.
	 */
	public static <R, T extends Throwable> R run(Logic<R, T> logic) throws T {
		try {
			// Setup to run
			isRun = true;

			// Run the test logic
			return logic.run();

		} finally {
			// Reset
			isRun = false;
		}
	}

	/*
	 * ========================== ProcedureSource ======================
	 */

	@Override
	public String getSourceName() {
		return "AnotherMock";
	}

	@Override
	public void listProcedures(ProcedureListContext context) throws Exception {
		if (isRun) {
			super.listProcedures(context);
		}
	}

	@Override
	public Method loadMethod(ProcedureMethodContext context) throws Exception {
		return isRun ? super.loadMethod(context) : null;
	}

}