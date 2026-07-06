package net.officefloor.tutorial.servlethttpserver;

import java.io.IOException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import net.officefloor.plugin.clazz.Dependency;

@SuppressWarnings("serial")
// START SNIPPET: tutorial
public class TutorialFilter extends HttpFilter {

	@Dependency
	private InjectedDependency dependency;

	@Override
	protected void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws IOException, ServletException {

		// Determine if filter
		String isFilter = request.getParameter("filter");
		if (Boolean.parseBoolean(isFilter)) {

			// Provide filter response
			response.getWriter().write("FILTER " + this.dependency.getMessage());

		} else {
			
			// Carry on filter chain
			chain.doFilter(request, response);
		}
	}
}
// END SNIPPET: tutorial