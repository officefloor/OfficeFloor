package net.officefloor.servlet;

import javax.servlet.Servlet;

/**
 * <p>
 * Manager of {@link Servlet} instances for {@link ServletServicer}.
 * <p>
 * It is also a {@link ServletServicer} to handle routing to appropriate
 * {@link Servlet} for servicing.
 * 
 * @author Daniel Sagenschneider
 */
public interface ServletManager extends ServletServicer {

	/**
	 * Adds a {@link Servlet}.
	 * 
	 * @param name         Name of {@link Servlet}.
	 * @param servletClass {@link Servlet} {@link Class}.
	 * @return {@link ServletServicer}.
	 */
	ServletServicer addServlet(String name, Class<? extends Servlet> servletClass);

}