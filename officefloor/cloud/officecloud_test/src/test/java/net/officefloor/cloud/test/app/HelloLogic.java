package net.officefloor.cloud.test.app;

import net.officefloor.web.ObjectResponse;

/**
 * Hello logic.
 * 
 * @author Daniel Sagenschneider
 */
public class HelloLogic {

	public void hello(ObjectResponse<MockDocument> response) {
		response.send(new MockDocument("Hello from Cloud"));
	}

}
