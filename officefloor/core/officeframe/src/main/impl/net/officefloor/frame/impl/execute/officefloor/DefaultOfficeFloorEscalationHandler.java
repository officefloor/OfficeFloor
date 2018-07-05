/*
 * OfficeFloor - http://www.officefloor.net
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
		StringWriter stackTrace = new StringWriter();
		escalation.printStackTrace(new PrintWriter(stackTrace));

		// Prints details of the error
		System.err.println("FAILURE: Office not handling:\n"
				+ stackTrace.toString());
	}

}