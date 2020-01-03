package net.officefloor.web.build;

import net.officefloor.server.http.ServerHttpConnection;

/**
 * Parses the {@link ServerHttpConnection}.
 * 
 * @author Daniel Sagenschneider
 */
public interface HttpContentParser {

	/**
	 * Obtains the <code>Content-Type</code> handled by this
	 * {@link HttpArgumentParser}.
	 * 
	 * @return <code>Content-Type</code> handled by this
	 *         {@link HttpArgumentParser}.
	 */
	String getContentType();

}