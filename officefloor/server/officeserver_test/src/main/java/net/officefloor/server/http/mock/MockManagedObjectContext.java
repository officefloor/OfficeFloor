/*-
 * #%L
 * Testing of HTTP Server
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

package net.officefloor.server.http.mock;

import java.util.logging.Logger;

import net.officefloor.frame.api.managedobject.ManagedObjectContext;
import net.officefloor.frame.api.managedobject.ProcessSafeOperation;
import net.officefloor.test.JUnitAgnosticAssert;

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
		return JUnitAgnosticAssert.fail("Should not require bound name");
	}

	@Override
	public Logger getLogger() {
		return JUnitAgnosticAssert.fail("Should not require logger");
	}

	@Override
	public <R, T extends Throwable> R run(ProcessSafeOperation<R, T> operation) throws T {
		return operation.run();
	}

}
