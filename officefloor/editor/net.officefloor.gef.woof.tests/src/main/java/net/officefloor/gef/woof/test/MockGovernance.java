/*-
 * #%L
 * net.officefloor.gef.woof.tests
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

package net.officefloor.gef.woof.test;

import java.sql.Connection;

import net.officefloor.frame.api.governance.Governance;
import net.officefloor.plugin.governance.clazz.Disregard;
import net.officefloor.plugin.governance.clazz.Enforce;
import net.officefloor.plugin.governance.clazz.Govern;

/**
 * Mock {@link Governance}.
 * 
 * @author Daniel Sagenschneider
 */
public class MockGovernance {

	@Govern
	public void register(Connection connection) {
		// only mocking
	}

	@Enforce
	public void commit() {
		// would commit transaction
	}

	@Disregard
	public void rollback() {
		// would rollback transaction
	}
}
