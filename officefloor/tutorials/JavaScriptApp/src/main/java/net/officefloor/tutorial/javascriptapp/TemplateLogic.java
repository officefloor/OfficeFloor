package net.officefloor.tutorial.javascriptapp;

import java.io.IOException;
import java.io.Serializable;

import lombok.Data;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.web.HttpObject;
import net.officefloor.web.HttpParameters;
import net.officefloor.web.ObjectResponse;
import net.officefloor.web.template.NotRenderTemplateAfter;

/**
 * Logic for the <code>template.woof.html</code>.
 * 
 * @author Daniel Sagenschneider
 */
// START SNIPPET: HttpParameters
public class TemplateLogic {

	@Data
	@HttpParameters
	public static class AdditionRequest implements Serializable {
		private static final long serialVersionUID = 1L;

		private String numberOne;
		private String numberTwo;
	}

	@NotRenderTemplateAfter
	public void addition(AdditionRequest request, ServerHttpConnection connection) throws IOException {

		// Add the numbers
		int result = Integer.parseInt(request.getNumberOne()) + Integer.parseInt(request.getNumberTwo());

		// Return the result
		connection.getResponse().getEntityWriter().write(String.valueOf(result));
	}
	// END SNIPPET: HttpParameters

	// START SNIPPET: HttpJson
	@Data
	@HttpObject
	public static class SubtractionRequest implements Serializable {
		private static final long serialVersionUID = 1L;

		private String numberOne;
		private String numberTwo;
	}

	@Data
	public static class JsonResponse {
		private final String result;
	}

	@NotRenderTemplateAfter
	public void subtraction(SubtractionRequest request, ObjectResponse<JsonResponse> response) throws IOException {

		// Subtract the numbers
		int result = Integer.parseInt(request.getNumberOne()) - Integer.parseInt(request.getNumberTwo());

		// Return the result
		response.send(new JsonResponse(String.valueOf(result)));
	}

}
// END SNIPPET: HttpJson