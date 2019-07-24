/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
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
package net.officefloor.eclipse.woof.test;

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