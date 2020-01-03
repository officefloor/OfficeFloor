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