/*
 * Office Floor, Application Server
 * Copyright (C) 2005-2009 Daniel Sagenschneider
 *
 * This program is free software; you can redistribute it and/or modify it under the terms
 * of the GNU General Public License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program;
 * if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 * MA 02111-1307 USA
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