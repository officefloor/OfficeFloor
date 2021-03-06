package net.officefloor.tutorial.warapp;

import java.io.IOException;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Simple {@link HttpServlet}.
 * 
 * @author Daniel Sagenschneider
 */
@SuppressWarnings("serial")
// START SNIPPET: tutorial
@WebServlet(urlPatterns = "/inject")
public class InjectServlet extends HttpServlet {

	@Inject
	private ServletDependency dependency;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String value = this.dependency != null ? this.dependency.getMessage() : "NO DEPENDENCY";
		resp.getWriter().write(value);
	}
}
// END SNIPPET: tutorial