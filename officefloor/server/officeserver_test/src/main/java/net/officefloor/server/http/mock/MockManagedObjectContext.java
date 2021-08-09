/*-
 * #%L
 * Testing of HTTP Server
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
