package net.officefloor.tutorial.springrestservlet;

import jakarta.servlet.http.HttpServletRequest;
import net.officefloor.web.ObjectResponse;

// START SNIPPET: tutorial
public class RequestInfoService {

	public void service(HttpServletRequest request, ObjectResponse<String> response) {
		String name = request.getParameter("name");
		String userAgent = request.getHeader("User-Agent");
		response.send("name=" + name + ", agent=" + userAgent);
	}
}
// END SNIPPET: tutorial
