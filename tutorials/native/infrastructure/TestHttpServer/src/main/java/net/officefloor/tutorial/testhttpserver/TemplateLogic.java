package net.officefloor.tutorial.testhttpserver;

import java.io.IOException;
import java.io.Serializable;

import lombok.Data;
import net.officefloor.server.http.HttpStatus;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.web.HttpParameters;

/**
 * Logic for the template.
 * 
 * @author Daniel Sagenschneider
 */
@SuppressWarnings("serial")
// START SNIPPET: tutorial
public class TemplateLogic {

	@Data
	@HttpParameters
	public static class Parameters implements Serializable {
		private String a;
		private String b;
		private String result;
	}

	public void add(Parameters parameters, Calculator calculator) {
		int a = Integer.parseInt(parameters.getA());
		int b = Integer.parseInt(parameters.getB());
		parameters.setResult(String.valueOf(calculator.plus(a, b)));
	}

	public Parameters getTemplateData(Parameters parameters) {
		return parameters;
	}

	public void redirectToTemplate(Parameters parameters, ServerHttpConnection connection) throws IOException {
		connection.getResponse().setStatus(HttpStatus.SEE_OTHER);
		connection.getResponse().getHeaders().addHeader("location",
				"/template?a=" + parameters.getA() + "&b=" + parameters.getB() + "&result=" + parameters.getResult());
	}

}
// END SNIPPET: tutorial