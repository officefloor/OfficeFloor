/*-
 * #%L
 * OfficeFrame
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

package net.officefloor.frame.impl.execute.startup;

import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.test.AbstractOfficeConstructTestCase;

/**
 * Ensure startup {@link ManagedFunction} instances are invoked.
 *
 * @author Daniel Sagenschneider
 */
public class StartupFunctionTest extends AbstractOfficeConstructTestCase {

	/**
	 * Ensure invoke startup function.
	 */
	public void testStartupFunction() throws Exception {

		// Construct the function
		TestWork work = new TestWork();
		this.constructFunction(work, "startup");

		// Construct startup
		this.getOfficeBuilder().addStartupFunction("startup");

		// Open the office
		this.constructOfficeFloor().openOfficeFloor();

		// Ensure the startup function is invoked
		assertTrue("Should have invoked startup function", work.isStartupInvoked);
	}

	/**
	 * Test functionality.
	 */
	public class TestWork {

		public boolean isStartupInvoked = false;

		public void startup() {
			this.isStartupInvoked = true;
		}
	}

}
