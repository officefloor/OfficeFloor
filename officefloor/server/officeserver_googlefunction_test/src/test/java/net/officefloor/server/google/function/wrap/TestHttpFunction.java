package net.officefloor.server.google.function.wrap;

import com.google.cloud.functions.HttpFunction;
import com.google.cloud.functions.HttpRequest;
import com.google.cloud.functions.HttpResponse;

/**
 * {@link HttpFunction} for testing.
 */
public class TestHttpFunction implements HttpFunction {

	/*
	 * ================= HttpFunction =======================
	 */

	@Override
	public void service(HttpRequest request, HttpResponse response) throws Exception {
		boolean isSecure = request.getUri().startsWith("https");
		response.getWriter().write("TEST" + (isSecure ? "-secure" : ""));
	}

}
