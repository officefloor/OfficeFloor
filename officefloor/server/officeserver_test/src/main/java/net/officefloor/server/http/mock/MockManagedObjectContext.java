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
package net.officefloor.server.http.mock;

import static org.junit.Assert.fail;

import java.util.logging.Logger;

import net.officefloor.frame.api.managedobject.ManagedObjectContext;
import net.officefloor.frame.api.managedobject.ProcessSafeOperation;

/**
 * Mock {@link ManagedObjectContext} that just runs the
 * {@link ProcessSafeOperation}.
 * 
 * @author Daniel Sagenschneider
 */
public class MockManagedObjectContext implements ManagedObjectContext {

	/*
	 * =================== ManagedObjectContext =====================
	 */

	@Override
	public String getBoundName() {
		fail("Should not require bound name");
		return null;
	}

	@Override
	public Logger getLogger() {
		fail("Should not require logger");
		return null;
	}

	@Override
	public <R, T extends Throwable> R run(ProcessSafeOperation<R, T> operation) throws T {
		return operation.run();
	}

}