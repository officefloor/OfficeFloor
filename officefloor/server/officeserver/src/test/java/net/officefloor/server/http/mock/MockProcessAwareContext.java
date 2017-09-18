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
package net.officefloor.server.http.mock;

import net.officefloor.frame.api.managedobject.ProcessAwareContext;
import net.officefloor.frame.api.managedobject.ProcessSafeOperation;

/**
 * Mock {@link ProcessAwareContext} that just runs the
 * {@link ProcessSafeOperation}.
 * 
 * @author Daniel Sagenschneider
 */
public class MockProcessAwareContext implements ProcessAwareContext {

	/*
	 * =================== ProcessAwareContext =====================
	 */

	@Override
	public <R, T extends Throwable> R run(ProcessSafeOperation<R, T> operation) throws T {
		return operation.run();
	}

}