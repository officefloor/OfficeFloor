/*-
 * #%L
 * Procedure
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
