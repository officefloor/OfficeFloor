package net.officefloor.tutorial.googleappenginehttpserver;

import lombok.Value;
import net.officefloor.web.ObjectResponse;

/**
 * Logic for the GCP Http Server.
 * 
 * @author Daniel Sagenschneider
 */
public class Logic {

	@Value
	public static class Message {
		private String message;
	}

	public void helloWorld(ObjectResponse<Message> response) {
		response.send(new Message("Hello from GCP"));
	}
}