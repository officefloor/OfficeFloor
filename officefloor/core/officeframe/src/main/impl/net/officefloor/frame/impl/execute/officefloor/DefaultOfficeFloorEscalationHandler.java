/*-
 * #%L
 * OfficeFrame
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

package net.officefloor.frame.impl.execute.officefloor;

import java.io.PrintWriter;
import java.io.StringWriter;

import net.officefloor.frame.api.escalate.EscalationHandler;
import net.officefloor.frame.api.manage.OfficeFloor;

/**
 * Default {@link OfficeFloor} {@link EscalationHandler} that prints issue to
 * {@link System#err}.
 * 
 * @author Daniel Sagenschneider
 */
public class DefaultOfficeFloorEscalationHandler implements EscalationHandler {

	/*
	 * =================== EscalationHandler ==================================
	 */

	@Override
	public void handleEscalation(Throwable escalation) throws Throwable {

		// Obtain the stack trace
		StringWriter buffer = new StringWriter();
		PrintWriter writer = new PrintWriter(buffer);
		writer.println("FAILURE: Office not handling:");
		escalation.printStackTrace(writer);
		writer.flush();

		// Prints details of the error
		System.err.println(buffer.toString());
	}

}
