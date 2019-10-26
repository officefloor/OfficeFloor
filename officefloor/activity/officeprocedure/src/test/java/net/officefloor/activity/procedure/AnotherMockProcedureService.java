/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2019 Daniel Sagenschneider
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
package net.officefloor.activity.procedure;

import java.lang.reflect.Method;

import net.officefloor.activity.procedure.spi.ProcedureService;
import net.officefloor.activity.procedure.spi.ProcedureServiceContext;

/**
 * Another mock {@link ProcedureService} for testing.
 * 
 * @author Daniel Sagenschneider
 */
public class AnotherMockProcedureService extends MockProcedureService {

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
	 * ========================== ProcedureService ======================
	 */

	@Override
	public String getServiceName() {
		return "AnotherMock";
	}

	@Override
	public String[] listProcedures(String resource) throws Exception {
		return isRun ? super.listProcedures(resource) : null;
	}

	@Override
	public Method loadMethod(ProcedureServiceContext context) throws Exception {
		return isRun ? super.loadMethod(context) : null;
	}

}