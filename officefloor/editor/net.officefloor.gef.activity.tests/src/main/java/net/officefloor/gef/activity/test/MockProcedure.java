/*
Procedure * OfficeFloor - http://www.officefloor.net
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
package net.officefloor.gef.activity.test;

import net.officefloor.activity.procedure.Procedure;
import net.officefloor.plugin.clazz.FlowInterface;

/**
 * Mock {@link Procedure} for testing.
 * 
 * @author Daniel Sagenschneider
 */
public class MockProcedure {

	@FlowInterface
	public static interface Flows {
		void flow();
	}

	public void procedure(Flows flows) {
		// Test method
	}

}