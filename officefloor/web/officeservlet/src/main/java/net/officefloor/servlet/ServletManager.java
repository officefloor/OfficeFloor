package net.officefloor.servlet;

import javax.servlet.Filter;
import javax.servlet.Servlet;

/**
 * Manager of {@link Servlet} instances for {@link ServletServicer}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ServletManager {

	/**
	 * Adds a {@link Servlet}.
	 * 
	 * @param name         Name of {@link Servlet}.
	 * @param servletClass {@link Servlet} {@link Class}.
	 * @return {@link ServletServicer}.
	 */
	ServletServicer addServlet(String name, Class<? extends Servlet> servletClass);

	/**
	 * Adds a {@link Filter}.
	 * 
	 * @param name        Name of {@link Filter}.
	 * @param filterClass {@link Filter} {@link Class}.
	 * @return {@link FilterServicer}.
	 */
	FilterServicer addFilter(String name, Class<? extends Filter> filterClass);

}