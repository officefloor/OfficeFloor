package net.officefloor.web.template.section;

import java.io.IOException;

import net.officefloor.server.http.HttpException;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.server.stream.ServerWriter;

/**
 * {@link WebTemplateWriter} to write the link.
 * 
 * @author Daniel Sagenschneider
 */
public class LinkWebTemplateWriter implements WebTemplateWriter {

	/**
	 * Indicates if the link is to be submitted over a secure
	 * {@link ServerHttpConnection}.
	 */
	private final boolean isLinkSecure;

	/**
	 * Suffix to the path for this link.
	 */
	private final String linkPathSuffix;

	/**
	 * Initiate.
	 * 
	 * @param linkName
	 *            Link name.
	 * @param isLinkSecure
	 *            Indicates if the link is to be submitted over a secure
	 *            {@link ServerHttpConnection}.
	 * @param linkSeparator
	 *            Link separator {@link Character}.
	 */
	public LinkWebTemplateWriter(String linkName, boolean isLinkSecure, char linkSeparator) {
		this.isLinkSecure = isLinkSecure;
		this.linkPathSuffix = String.valueOf(linkSeparator) + linkName;
	}

	/*
	 * ================== WebTemplateWriter ===========================
	 */

	@Override
	public void write(ServerWriter writer, boolean isDefaultCharset, Object bean, ServerHttpConnection connection,
			String templatePath) throws HttpException {

		// Obtain the link path (determining if require secure link)
		if (this.isLinkSecure && (!connection.isSecure())) {
			templatePath = connection.getServerLocation().createClientUrl(this.isLinkSecure, templatePath);
		}

		try {
			// Write the content
			writer.write(templatePath);
			writer.write(this.linkPathSuffix);

		} catch (IOException ex) {
			throw new HttpException(ex);
		}
	}

}