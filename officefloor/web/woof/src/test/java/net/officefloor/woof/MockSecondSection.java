/*-
 * #%L
 * Web on OfficeFloor
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

package net.officefloor.woof;

import net.officefloor.frame.api.team.Team;
import net.officefloor.plugin.section.clazz.Next;
import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.web.ObjectResponse;

/**
 * Mock logic for the section.
 * 
 * @author Daniel Sagenschneider
 */
public class MockSecondSection {

	/**
	 * Provides testing of objects.
	 * 
	 * @param object   {@link MockSecondObject} injected from configuration.
	 * @param response Sends the {@link MockObject} as a response.
	 */
	public void objects(MockSecondObject object, ObjectResponse<MockHttpObject> response) {
		response.send(new MockHttpObject(object.getMessage()));
	}

	/**
	 * Initial service method for testing {@link Team}
	 * 
	 * @param flows {@link TeamFlows}.
	 */
	@Next("teamsDifferent")
	public String teams() {
		return Thread.currentThread().getName();
	}

	/**
	 * Ensure different {@link Team}.
	 */
	public void teamsDifferent(@Parameter String threadName, MockObject object, ObjectResponse<String> response) {
		boolean isSameThread = Thread.currentThread().getName().equals(threadName);
		response.send(isSameThread ? "SAME SECOND THREAD" : "DIFFERENT SECOND THREAD");
	}

}
