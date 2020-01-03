package net.officefloor.web.template.section;

import net.officefloor.server.http.HttpMethod;
import net.officefloor.web.build.HttpInput;
import net.officefloor.web.template.build.WebTemplate;

/**
 * Annotation identifying a {@link HttpInput} link for the {@link WebTemplate}.
 * 
 * @author Daniel Sagenschneider
 */
public class WebTemplateLinkAnnotation {

	/**
	 * Indicates if the link is secure.
	 */
	private final boolean isLinkSecure;

	/**
	 * Name of link.
	 */
	private final String linkName;

	/**
	 * {@link HttpMethod} names supported by the link.
	 */
	private final String[] httpMethodNames;

	/**
	 * Instantiate.
	 * 
	 * @param isLinkSecure
	 *            Indicates if the link is secure.
	 * @param linkName
	 *            Name of link.
	 * @param httpMethodNames
	 *            {@link HttpMethod} names supported by the link.
	 */
	public WebTemplateLinkAnnotation(boolean isLinkSecure, String linkName, String... httpMethodNames) {
		this.isLinkSecure = isLinkSecure;
		this.linkName = linkName;
		this.httpMethodNames = httpMethodNames;
	}

	/**
	 * Indicates if the link is secure.
	 * 
	 * @return <code>true</code> if link is secure.
	 */
	public boolean isLinkSecure() {
		return this.isLinkSecure;
	}

	/**
	 * Obtains the link name.
	 * 
	 * @return Link name.
	 */
	public String getLinkName() {
		return this.linkName;
	}

	/**
	 * Obtains the {@link HttpMethod} names for the link.
	 * 
	 * @return {@link HttpMethod} names for the link.
	 */
	public String[] getHttpMethods() {
		return this.httpMethodNames;
	}

}