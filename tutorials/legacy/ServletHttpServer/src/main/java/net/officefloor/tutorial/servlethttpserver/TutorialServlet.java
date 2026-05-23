package net.officefloor.tutorial.servlethttpserver;

import java.io.IOException;

import jakarta.inject.Inject;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import net.officefloor.servlet.procedure.ServletProcedureSource;

/**
 * {@link HttpServlet} for {@link ServletProcedureSource}.
 * 
 * @author Daniel Sagenschneider
 */
@SuppressWarnings("serial")
// START SNIPPET: tutorial
public class TutorialServlet extends HttpServlet {

	@Inject
	private InjectedDependency dependency;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		resp.getWriter().write("SERVLET " + this.dependency.getMessage());
	}
}
// END SNIPPET: tutorial