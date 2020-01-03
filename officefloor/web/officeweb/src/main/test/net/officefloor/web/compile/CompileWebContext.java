package net.officefloor.web.compile;

import net.officefloor.compile.test.officefloor.CompileOfficeContext;
import net.officefloor.server.http.HttpMethod;
import net.officefloor.web.build.HttpInput;
import net.officefloor.web.build.HttpUrlContinuation;
import net.officefloor.web.build.WebArchitect;

/**
 * Context for the {@link CompileWebExtension}.
 * 
 * @author Daniel Sagenschneider
 */
public interface CompileWebContext extends CompileOfficeContext {

	/**
	 * Obtains the {@link WebArchitect}.
	 * 
	 * @return {@link WebArchitect}.
	 */
	WebArchitect getWebArchitect();

	/**
	 * Convenience method to link a URI to the <code>service</code> method of
	 * the {@link Class}.
	 * 
	 * @param applicationPath
	 *            Application path.
	 * @param isSecure
	 *            Indicates if secure.
	 * @param sectionClass
	 *            {@link Class} containing a <code>service</code> method.
	 * @return {@link HttpUrlContinuation}.
	 */
	HttpUrlContinuation link(boolean isSecure, String applicationPath, Class<?> sectionClass);

	/**
	 * Convenience method to link a URL to the <code>service</code> method of
	 * the {@link Class}.
	 * 
	 * @param isSecure
	 *            Indicates if secure.
	 * @param httpMethodName
	 *            Name of the {@link HttpMethod}.
	 * @param applicationPath
	 *            Application path.
	 * @param sectionClass
	 *            {@link Class} containing a <code>service</code> method.
	 * @return {@link HttpInput}.
	 */
	HttpInput link(boolean isSecure, String httpMethodName, String applicationPath, Class<?> sectionClass);

}