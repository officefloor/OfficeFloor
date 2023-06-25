package net.officefloor.server.google.function.test;

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
		response.getWriter().write("TEST");
	}

}