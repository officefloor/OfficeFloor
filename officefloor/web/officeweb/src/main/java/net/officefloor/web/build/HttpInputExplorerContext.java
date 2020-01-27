package net.officefloor.web.build;

import net.officefloor.compile.spi.office.ExecutionExplorerContext;
import net.officefloor.server.http.HttpMethod;

/**
 * Context for the {@link HttpInputExplorer}.
 * 
 * @author Daniel Sagenschneider
 */
public interface HttpInputExplorerContext extends ExecutionExplorerContext {

	/**
	 * Indicates if secure.
	 * 
	 * @return <code>true</code> if secure.
	 */
	boolean isSecure();

	/**
	 * {@link HttpMethod}.
	 * 
	 * @return {@link HttpMethod}.
	 */
	HttpMethod getHttpMethod();

	/**
	 * Obtains the possible context path.
	 * 
	 * @return Context path. May be <code>null</code>.
	 */
	String getContextPath();

	/**
	 * Obtains the {@link HttpInput} path minus the context path.
	 * 
	 * @return {@link HttpInput} path minus the context path.
	 */
	String getRoutePath();

	/**
	 * Application path (includes context path).
	 * 
	 * @return Application path.
	 */
	String getApplicationPath();

}