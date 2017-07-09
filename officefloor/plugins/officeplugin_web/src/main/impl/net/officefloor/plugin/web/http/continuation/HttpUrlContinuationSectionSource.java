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

import net.officefloor.autowire.AutoWireSection;
import net.officefloor.autowire.AutoWireSectionTransformer;
import net.officefloor.autowire.AutoWireSectionTransformerContext;
import net.officefloor.compile.properties.Property;
import net.officefloor.compile.spi.office.OfficeSectionInput;
import net.officefloor.compile.spi.section.SectionInput;
import net.officefloor.compile.spi.section.SectionTask;
import net.officefloor.compile.spi.section.SectionWork;
import net.officefloor.compile.spi.section.SubSection;
import net.officefloor.compile.spi.section.SubSectionInput;
import net.officefloor.compile.spi.section.source.SectionSourceContext;
import net.officefloor.plugin.section.transform.TransformSectionDesigner;
import net.officefloor.plugin.section.transform.TransformSectionSource;
import net.officefloor.plugin.socket.server.http.ServerHttpConnection;
import net.officefloor.plugin.web.http.application.HttpUriLink;

/**
 * {@link AutoWireSectionTransformer} to provide the HTTP URL continuations.
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
	 * @param uri
	 *            URI to be linked.
	 * @param section
	 *            {@link AutoWireSection} servicing the URI.
	 * @param inputName
	 *            Name of the {@link OfficeSectionInput} servicing the URI.
	 * @return {@link HttpUriLink} to configure handling the URI.
	 */
	public HttpUriLink linkUri(String uri, AutoWireSection section,
			String inputName) {

		// Create and register the link
		UriLink link = new UriLink(uri, section, inputName);
		this.uriLinks.add(link);

		// Return the link
		return link;
	}

	/**
	 * Obtains the registered {@link HttpUriLink} instances.
	 * 
	 * @return Registered {@link HttpUriLink} instances.
	 */
	public HttpUriLink[] getRegisteredHttpUriLinks() {
		return this.uriLinks.toArray(new HttpUriLink[this.uriLinks.size()]);
	}

	/*
	 * ======================= TransformSectionSource =========================
	 */

	@Override
	public AutoWireSection transformAutoWireSection(
			AutoWireSectionTransformerContext context) {

		// Obtain the transformed auto-wire section
		AutoWireSection section = super.transformAutoWireSection(context);

		// Obtain the name of section being transformed
		String sectionName = section.getSectionName();

		// Add properties for URL continuations of the section
		for (UriLink link : this.uriLinks) {

			// Ignore if link not for section
			if (!(sectionName.equals(link.section.getSectionName()))) {
				continue; // link not for section
			}

			// Add properties for the link
			section.addProperty(PROPERTY_URL_LINK_PREFIX
					+ link.applicationUriPath, link.inputName);
			section.addProperty(PROPERTY_URL_SECURE_PREFIX
					+ link.applicationUriPath, String.valueOf(link.isSecure));
		}

		// Return the transformed auto-wire section
		return section;
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
				String uriPath = propertyName
						.substring(PROPERTY_URL_LINK_PREFIX.length());

				// Transform URI Path to task name
				String taskName = uriPath;
				while (taskName.startsWith("/")) {
					taskName = taskName.substring("/".length());
				}
				if (taskName.trim().length() == 0) {
					// Root path
					taskName = "_root_";
				}

				// Determine if secure
				String isSecureText = context.getProperty(
						PROPERTY_URL_SECURE_PREFIX + uriPath, null);
				Boolean isSecure = null;
				if (isSecureText != null) {
					isSecure = Boolean.valueOf(isSecureText);
				}

				// Add the URL continuation
				SectionWork work = designer.addSectionWork(taskName,
						HttpUrlContinuationManagedFunctionSource.class.getName());
				work.addProperty(
						HttpUrlContinuationManagedFunctionSource.PROPERTY_URI_PATH,
						uriPath);
				if (isSecure != null) {
					work.addProperty(
							HttpUrlContinuationManagedFunctionSource.PROPERTY_SECURE,
							String.valueOf(isSecure));
				}
				SectionTask task = work.addSectionTask(taskName,
						HttpUrlContinuationManagedFunctionSource.FUNCTION_NAME);

				// Obtain the input to service the URL continuation
				SubSectionInput servicingInput = subSection
						.getSubSectionInput(inputName);

				// Link the URL continuation for servicing
				designer.link(task, servicingInput);
			}
		}
	}

	/**
	 * URI link.
	 */
	private static class UriLink implements HttpUriLink {

		/**
		 * Application URI path.
		 */
		public final String applicationUriPath;

		/**
		 * {@link AutoWireSection} to handle the URI.
		 */
		public final AutoWireSection section;

		/**
		 * Name {@link SectionInput} to handle the URI.
		 */
		public final String inputName;

		/**
		 * Indicates if requires secure {@link ServerHttpConnection}.
		 */
		public boolean isSecure = false;

		/**
		 * Initiate.
		 * 
		 * @param applicationUriPath
		 *            Application URI path.
		 * @param section
		 *            {@link AutoWireSection} to handle the URI.
		 * @param inputName
		 *            Name {@link SectionInput} to handle the URI.
		 */
		public UriLink(String applicationUriPath, AutoWireSection section,
				String inputName) {
			this.applicationUriPath = applicationUriPath;
			this.section = section;
			this.inputName = inputName;
		}

		/*
		 * ===================== HttpUriLink ===========================
		 */

		@Override
		public String getApplicationUriPath() {
			return this.applicationUriPath;
		}

		@Override
		public AutoWireSection getAutoWireSection() {
			return this.section;
		}

		@Override
		public String getAutoWireSectionInputName() {
			return this.inputName;
		}

		@Override
		public void setUriSecure(boolean isSecure) {
			this.isSecure = isSecure;
		}
	}

}