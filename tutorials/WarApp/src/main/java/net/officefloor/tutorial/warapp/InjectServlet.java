package net.officefloor.tutorial.warapp;

import java.io.IOException;

import jakarta.inject.Inject;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

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