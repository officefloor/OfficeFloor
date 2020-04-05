package net.officefloor.tutorial.servlethttpserver;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.officefloor.plugin.managedobject.clazz.Dependency;
import net.officefloor.servlet.procedure.FilterProcedureSource;

/**
 * {@link HttpFilter} for the {@link FilterProcedureSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class TutorialFilter extends HttpFilter {

	private static final long serialVersionUID = 1L;

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