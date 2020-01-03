package net.officefloor.web.template.build;

import java.io.Reader;

import net.officefloor.server.http.ServerHttpConnection;

/**
 * Factory for the creation of a {@link WebTemplate}.
 * 
 * @author Daniel Sagenschneider
 */
public interface WebTemplateFactory {

	/**
	 * Adds a {@link WebTemplate}.
	 * 
	 * @param isSecure
	 *            Indicates if requires secure {@link ServerHttpConnection} to
	 *            render the {@link WebTemplate}.
	 * @param applicationPath
	 *            Application path to the {@link WebTemplate}. May contain path
	 *            parameters.
	 * @param templateContent
	 *            {@link Reader} to the template content.
	 * @return {@link WebTemplate}.
	 */
	WebTemplate addTemplate(boolean isSecure, String applicationPath, Reader templateContent);

	/**
	 * Adds a {@link WebTemplate}.
	 * 
	 * @param isSecure
	 *            Indicates if requires secure {@link ServerHttpConnection} to
	 *            render the {@link WebTemplate}.
	 * @param applicationPath
	 *            Application path to the {@link WebTemplate}. May contain path
	 *            parameters.
	 * @param locationOfTemplate
	 *            Location of the template content.
	 * @return {@link WebTemplate}.
	 */
	WebTemplate addTemplate(boolean isSecure, String applicationPath, String locationOfTemplate);

}