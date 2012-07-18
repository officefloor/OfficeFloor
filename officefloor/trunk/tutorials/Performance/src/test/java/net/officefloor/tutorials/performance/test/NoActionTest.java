/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2012 Daniel Sagenschneider
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
package net.officefloor.tutorials.performance.test;

import net.officefloor.tutorials.performance.Request;
import net.officefloor.tutorials.performance.Servicer;

/**
 * Tests the client framework to not have significant overheads by running
 * without sending {@link Request} instances.
 * 
 * @author Daniel Sagenschneider
 */
public class NoActionTest extends AbstractRunnerTestCase {

	@Override
	protected Integer getServerPort() {
		return null;
	}

	@Override
	protected Servicer getServicer() {
		return null;
	}

}