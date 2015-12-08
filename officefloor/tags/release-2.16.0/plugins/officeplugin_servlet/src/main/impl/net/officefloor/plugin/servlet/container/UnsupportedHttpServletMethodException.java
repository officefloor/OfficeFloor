package net.officefloor.plugin.servlet.container;

import javax.servlet.http.HttpServlet;

/**
 * <p>
 * Only keeping simple support for a {@link HttpServlet}.
 * <p>
 * This is thrown to indicate methods are not supported.
 * 
 * @author Daniel Sagenschneider
 */
public class UnsupportedHttpServletMethodException extends RuntimeException {

	/**
	 * Invoked to indicate {@link HttpServlet} method is not supported.
	 * 
	 * @param <T>
	 *            Any type for ease of use in methods requiring a return value.
	 * @return Allow for use in methods requiring a return value.
	 * @throws UnsupportedHttpServletMethodException
	 *             {@link UnsupportedHttpServletMethodException}.
	 */
	public static <T> T notSupported()
			throws UnsupportedHttpServletMethodException {
		throw new UnsupportedHttpServletMethodException(
				"WoOF does not support this method.  "
						+ "Focus of WoOF is to support only minimal Servlet functionality for migration, "
						+ "as it's aim is not to be a Servlet container");
	}

	/**
	 * All access via static methods.
	 */
	private UnsupportedHttpServletMethodException(String message) {
		super(message);
	}
}