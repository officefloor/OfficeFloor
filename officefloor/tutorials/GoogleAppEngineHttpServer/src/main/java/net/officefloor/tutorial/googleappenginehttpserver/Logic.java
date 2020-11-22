package net.officefloor.tutorial.googleappenginehttpserver;

import com.googlecode.objectify.Objectify;

import lombok.Value;
import net.officefloor.web.HttpPathParameter;
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

	public void datastore(@HttpPathParameter("id") String identifier, Objectify objectify,
			ObjectResponse<Post> response) {
		response.send(objectify.load().type(Post.class).id(Long.parseLong(identifier)).now());
	}

	public void secure(ObjectResponse<Message> response) {
		response.send(new Message("Secure hello from GCP"));
	}

}