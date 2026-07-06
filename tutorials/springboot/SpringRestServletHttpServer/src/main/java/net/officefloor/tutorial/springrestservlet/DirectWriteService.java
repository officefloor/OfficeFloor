package net.officefloor.tutorial.springrestservlet;

import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

// START SNIPPET: tutorial
public class DirectWriteService {

	public void service(HttpServletResponse response) throws IOException {
		response.setContentType("text/plain");
		response.getWriter().write("Written directly to servlet response");
	}
}
// END SNIPPET: tutorial
