package net.officefloor.woof;

import net.officefloor.activity.procedure.Procedure;
import net.officefloor.web.ObjectResponse;

/**
 * Mock {@link Procedure}.
 * 
 * @author Daniel Sagenschneider
 */
public class MockProcedure {

	/**
	 * {@link Procedure}.
	 * 
	 * @param response Sends the response.
	 */
	public void procedure(ObjectResponse<String> response) {
		response.send("PROCEDURE");
	}

}