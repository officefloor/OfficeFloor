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
package net.officefloor.plugin.web.http.application;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.plugin.web.http.template.section.HttpTemplateSectionExtension;

/**
 * {@link HttpTemplateSection} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpTemplateSectionImpl implements HttpTemplateSection {

	/**
	 * {@link OfficeSection}.
	 */
	private final OfficeSection section;

	/**
	 * Logic class for the template.
	 */
	private final Class<?> templateLogicClass;

	/**
	 * Location of the template.
	 */
	private final String templateLocation;

	/**
	 * URI to the template. May be <code>null</code> if not publicly exposed
	 * template.
	 */
	private final String templateUri;

	/**
	 * URI suffix for the template.
	 */
	private String templateUriSuffix = null;

	/**
	 * Content-type for the template.
	 */
	private String contentType = null;

	/**
	 * Indicates if the template is to be secure.
	 */
	private boolean isTemplateSecure = false;

	/**
	 * Specific configuration of whether links are to be secure. This overrides
	 * {@link #isTemplateSecure}.
	 */
	private final Map<String, Boolean> secureLinks = new HashMap<String, Boolean>();

	/**
	 * Render redirect HTTP methods.
	 */
	private final List<String> renderRedirectHttpMethods = new LinkedList<String>();

	/**
	 * Index of the next extension.
	 */
	private int nextExtensionIndex = 1;

	/**
	 * Initiate.
	 * 
	 * @param section
	 *            {@link OfficeSection}.
	 * @param templateLogicClass
	 *            Logic class for the template.
	 * @param templateLocation
	 *            Location of the template.
	 * @param templateUri
	 *            URI to the template. May be <code>null</code> if not publicly
	 *            exposed template.
	 */
	public HttpTemplateSectionImpl(OfficeSection section, Class<?> templateLogicClass, String templateLocation,
			String templateUri) {
		this.section = section;
		this.templateLogicClass = templateLogicClass;
		this.templateLocation = templateLocation;
		this.templateUri = templateUri;
	}

	/*
	 * ====================== HttpTemplateSection =====================
	 */

	@Override
	public OfficeSection getOfficeSection() {
		return this.section;
	}

	@Override
	public Class<?> getTemplateLogicClass() {
		return this.templateLogicClass;
	}

	@Override
	public String getTemplateLocation() {
		return this.templateLocation;
	}

	@Override
	public String getTemplateUri() {
		return this.templateUri;
	}

	@Override
	public void setTemplateUriSuffix(String uriSuffix) {
		this.templateUriSuffix = uriSuffix;
	}

	@Override
	public String getTemplateUriSuffix() {
		return this.templateUriSuffix;
	}

	@Override
	public void setTemplateContentType(String contentType) {
		this.contentType = contentType;
	}

	@Override
	public String getTemplateContentType() {
		return this.contentType;
	}

	@Override
	public void setTemplateSecure(boolean isSecure) {
		this.isTemplateSecure = isSecure;
	}

	@Override
	public boolean isTemplateSecure() {
		return this.isTemplateSecure;
	}

	@Override
	public void setLinkSecure(String linkName, boolean isSecure) {
		this.secureLinks.put(linkName, Boolean.valueOf(isSecure));
	}

	@Override
	public Map<String, Boolean> getSecureLinks() {
		return Collections.unmodifiableMap(this.secureLinks);
	}

	@Override
	public void addRenderRedirectHttpMethod(String renderRedirectHttpMethod) {
		this.renderRedirectHttpMethods.add(renderRedirectHttpMethod);
	}

	@Override
	public String[] getRenderRedirectHttpMethods() {
		return this.renderRedirectHttpMethods.toArray(new String[this.renderRedirectHttpMethods.size()]);
	}

	@Override
	public HttpTemplateAutoWireSectionExtension addTemplateExtension(
			Class<? extends HttpTemplateSectionExtension> extensionClass) {
		return new HttpTemplateAutoWireSectionExtensionImpl(this.nextExtensionIndex++, extensionClass);
	}

	/**
	 * {@link HttpTemplateAutoWireSectionExtension} implementation.
	 */
	private class HttpTemplateAutoWireSectionExtensionImpl implements HttpTemplateAutoWireSectionExtension {

		/**
		 * Index of this extension.
		 */
		private final int extensionIndex;

		/**
		 * Initiate.
		 * 
		 * @param extensionIndex
		 *            Index of this extension.
		 * @param extensionClass
		 *            Extension class.
		 */
		public HttpTemplateAutoWireSectionExtensionImpl(int extensionIndex,
				Class<? extends HttpTemplateSectionExtension> extensionClass) {
			this.extensionIndex = extensionIndex;

			// Add property for the extension class
			HttpTemplateSectionImpl.this.section.addProperty("extension." + this.extensionIndex,
					extensionClass.getName());
		}

		/*
		 * ================ HttpTemplateAutoWireSectionExtension ========
		 */

		@Override
		public void addProperty(String name, String value) {

			// Add extension property to section
			HttpTemplateSectionImpl.this.section.addProperty("extension." + this.extensionIndex + "." + name, value);
		}
	}

}