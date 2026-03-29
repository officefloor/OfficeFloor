package net.officefloor.tutorial.corshttpserver;

import lombok.AllArgsConstructor;
import lombok.Data;
import net.officefloor.web.ObjectResponse;

/**
 * Logic.
 * 
 * @author Daniel Sagenschneider
 */
public class Logic {

	@Data
	@AllArgsConstructor
	public static class Response {
		private String message;
	}

	public void service(ObjectResponse<Response> response) {
		response.send(new Response("TEST"));
	}

}