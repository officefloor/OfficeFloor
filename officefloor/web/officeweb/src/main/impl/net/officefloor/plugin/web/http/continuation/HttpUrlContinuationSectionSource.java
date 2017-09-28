/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2013 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.officefloor.plugin.web.http.continuation;

import java.util.LinkedList;
import java.util.List;

import net.officefloor.compile.properties.Property;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.office.OfficeSectionInput;
import net.officefloor.compile.spi.office.OfficeSectionTransformer;
import net.officefloor.compile.spi.office.OfficeSectionTransformerContext;
import net.officefloor.compile.spi.section.SectionFunction;
import net.officefloor.compile.spi.section.SectionFunctionNamespace;
import net.officefloor.compile.spi.section.SectionInput;
import net.officefloor.compile.spi.section.SubSection;
import net.officefloor.compile.spi.section.SubSectionInput;
import net.officefloor.compile.spi.section.source.SectionSourceContext;
import net.officefloor.plugin.section.transform.TransformSectionDesigner;
import net.officefloor.plugin.section.transform.TransformSectionSource;
import net.officefloor.server.http.HttpMethod;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.web.HttpUrlContinuation;

/**
 * {@link OfficeSectionTransformer} to provide the HTTP URL continuations.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpUrlContinuationSectionSource extends TransformSectionSource {

	/**
	 * Name of {@link Property} prefix for URL to {@link SectionInput} name.
	 */
	public static final String PROPERTY_URL_LINK_PREFIX = "url.continuation.link.";

	/**
	 * Name of {@link Property} prefix for URL to indicate if requires secure
	 * {@link ServerHttpConnection}.
	 */
	public static final String PROPERTY_URL_SECURE_PREFIX = "url.continuation.secure.";

	/**
	 * {@link UriLink} instances.
	 */
	private final List<UriLink> uriLinks = new LinkedList<UriLink>();

	/**
	 * Links a URI to an {@link OfficeSectionInput}.
	 * 
	 * @param httpMethod
	 *            Name of the {@link HttpMethod}.
	 * @param uri
	 *            URI to be linked.
	 * @param sectionInput
	 *            {@link OfficeSectionInput} servicing the URI.
	 * @return {@link HttpUrlContinuation} to configure handling the URI.
	 */
	public HttpUrlContinuation linkUri(String httpMethod, String uri, OfficeSectionInput sectionInput) {

		// Create and register the link
		UriLink link = new UriLink(httpMethod, uri, sectionInput);
		this.uriLinks.add(link);

		// Return the link
		return link;
	}

	/**
	 * Obtains the registered HTTP URIs.
	 * 
	 * @return Registered HTTP URIs.
	 */
	public String[] getRegisteredHttpUris() {
		return this.uriLinks.stream().map((link) -> link.applicationUriPath).toArray(String[]::new);
	}

	/*
	 * ======================= TransformSectionSource =========================
	 */

	@Override
	public void configureProperties(OfficeSectionTransformerContext context, PropertyList properties) {

		// Obtain the name of section being transformed
		String sectionName = context.getOfficeSectionName();

		// Add properties for URL continuations of the section
		for (UriLink link : this.uriLinks) {

			// Ignore if link not for section
			if (!(sectionName.equals(link.sectionInput.getOfficeSection().getOfficeSectionName()))) {
				continue; // link not for section
			}

			// Add properties for the link
			properties.addProperty(PROPERTY_URL_LINK_PREFIX + link.applicationUriPath)
					.setValue(link.sectionInput.getOfficeSectionInputName());
			properties.addProperty(PROPERTY_URL_SECURE_PREFIX + link.applicationUriPath)
					.setValue(String.valueOf(link.isSecure));
		}
	}

	@Override
	protected void loadEnhancements() throws Exception {

		// Obtain the context
		SectionSourceContext context = this.getContext();
		TransformSectionDesigner designer = this.getDesginer();

		// Obtain the section being transformed
		SubSection subSection = designer.getSubSection(SUB_SECTION_NAME);

		// Load the URL continuations
		for (String propertyName : context.getPropertyNames()) {
			if (propertyName.startsWith(PROPERTY_URL_LINK_PREFIX)) {

				// Obtain the input name
				String inputName = context.getProperty(propertyName);

				// Obtain the URI path
				String uriPath = propertyName.substring(PROPERTY_URL_LINK_PREFIX.length());

				// Transform URI Path to function name
				String functionName = uriPath;
				while (functionName.startsWith("/")) {
					functionName = functionName.substring("/".length());
				}
				if (functionName.trim().length() == 0) {
					// Root path
					functionName = "_root_";
				}

				// Determine if secure
				String isSecureText = context.getProperty(PROPERTY_URL_SECURE_PREFIX + uriPath, null);
				Boolean isSecure = null;
				if (isSecureText != null) {
					isSecure = Boolean.valueOf(isSecureText);
				}

				// Add the URL continuation
				SectionFunctionNamespace namespace = designer.addSectionFunctionNamespace(functionName,
						HttpUrlContinuationManagedFunctionSource.class.getName());
				namespace.addProperty(HttpUrlContinuationManagedFunctionSource.PROPERTY_URI_PATH, uriPath);
				if (isSecure != null) {
					namespace.addProperty(HttpUrlContinuationManagedFunctionSource.PROPERTY_SECURE,
							String.valueOf(isSecure));
				}
				SectionFunction function = namespace.addSectionFunction(functionName,
						HttpUrlContinuationManagedFunctionSource.FUNCTION_NAME);

				// Obtain the input to service the URL continuation
				SubSectionInput servicingInput = subSection.getSubSectionInput(inputName);

				// Link the URL continuation for servicing
				designer.link(function, servicingInput);
			}
		}
	}

	/**
	 * URI link.
	 */
	private static class UriLink implements HttpUrlContinuation {

		/**
		 * Name of the {@link HttpMethod}.
		 */
		private final String httpMethod;

		/**
		 * Application URI path.
		 */
		private final String applicationUriPath;

		/**
		 * {@link OfficeSectionInput} to handle the URI.
		 */
		private final OfficeSectionInput sectionInput;

		/**
		 * Indicates if requires secure {@link ServerHttpConnection}.
		 */
		private boolean isSecure = false;

		/**
		 * Initiate.
		 * 
		 * @param httpMethod
		 *            Name of the {@link HttpMethod}.
		 * @param applicationUriPath
		 *            Application URI path.
		 * @param sectionInput
		 *            {@link OfficeSectionInput} to handle the URI.
		 */
		public UriLink(String httpMethod, String applicationUriPath, OfficeSectionInput sectionInput) {
			this.httpMethod = httpMethod;
			this.applicationUriPath = applicationUriPath;
			this.sectionInput = sectionInput;
		}

		/*
		 * ===================== HttpUriLink ===========================
		 */

		@Override
		public void setUriSecure(boolean isSecure) {
			this.isSecure = isSecure;
		}
	}

}