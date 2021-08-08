/*-
 * #%L
 * Web on OfficeFloor
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
	public void objects(MockSecondObject object, ObjectResponse<String> response) {
		response.send(object.getMessage());
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
	public void teamsDifferent(@Parameter String threadName, MockSecondObject object, ObjectResponse<String> response) {
		boolean isSameThread = Thread.currentThread().getName().equals(threadName);
		response.send(isSameThread ? "SAME SECOND THREAD" : "DIFFERENT SECOND THREAD");
	}

}
