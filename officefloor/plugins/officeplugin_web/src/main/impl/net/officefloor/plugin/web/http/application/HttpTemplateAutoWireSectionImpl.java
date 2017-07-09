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

import net.officefloor.autowire.AutoWireSection;
import net.officefloor.autowire.impl.AutoWirePropertiesImpl;
import net.officefloor.autowire.impl.AutoWireSectionImpl;
import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.plugin.web.http.template.parse.HttpTemplate;
import net.officefloor.plugin.web.http.template.section.HttpTemplateSectionExtension;

/**
 * Allows wiring the flows of the {@link HttpTemplate}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpTemplateAutoWireSectionImpl extends AutoWireSectionImpl
		implements HttpTemplateSection {

	/**
	 * Logic class for the template.
	 */
	private final Class<?> templateLogicClass;

	/**
	 * URI to the template. May be <code>null</code> if not publicly exposed
	 * template.
	 */
	private final String templateUri;

	/**
	 * {@link OfficeFloorCompiler}.
	 */
	private final OfficeFloorCompiler compiler;

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
	 * @param compiler
	 *            {@link OfficeFloorCompiler}.
	 * @param section
	 *            {@link AutoWireSection}.
	 * @param templateLogicClass
	 *            Logic class for the template.
	 * @param templateUri
	 *            URI to the template. May be <code>null</code> if not publicly
	 *            exposed template.
	 */
	public HttpTemplateAutoWireSectionImpl(OfficeFloorCompiler compiler,
			AutoWireSection section, Class<?> templateLogicClass,
			String templateUri) {
		super(compiler, section);
		this.templateLogicClass = templateLogicClass;
		this.templateUri = templateUri;
		this.compiler = compiler;
	}

	/*
	 * ====================== HttpTemplateAutoWireSection =====================
	 */

	@Override
	public String getTemplatePath() {
		return this.getSectionLocation();
	}

	@Override
	public Class<?> getTemplateLogicClass() {
		return this.templateLogicClass;
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
		return this.renderRedirectHttpMethods
				.toArray(new String[this.renderRedirectHttpMethods.size()]);
	}

	@Override
	public HttpTemplateAutoWireSectionExtension addTemplateExtension(
			Class<? extends HttpTemplateSectionExtension> extensionClass) {
		return new HttpTemplateAutoWireSectionExtensionImpl(
				this.nextExtensionIndex++, extensionClass, this.compiler);
	}

	/**
	 * {@link HttpTemplateAutoWireSectionExtension} implementation.
	 */
	private class HttpTemplateAutoWireSectionExtensionImpl extends
			AutoWirePropertiesImpl implements
			HttpTemplateAutoWireSectionExtension {

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
		 * @param compiler
		 *            {@link OfficeFloorCompiler}.
		 */
		public HttpTemplateAutoWireSectionExtensionImpl(int extensionIndex,
				Class<? extends HttpTemplateSectionExtension> extensionClass,
				OfficeFloorCompiler compiler) {
			super(compiler, compiler.createPropertyList());
			this.extensionIndex = extensionIndex;

			// Add property for the extension class
			HttpTemplateAutoWireSectionImpl.this.addProperty("extension."
					+ this.extensionIndex, extensionClass.getName());
		}

		/*
		 * ================ HttpTemplateAutoWireSectionExtension ========
		 */

		@Override
		public void addProperty(String name, String value) {

			// Add extension property to section
			HttpTemplateAutoWireSectionImpl.this.addProperty("extension."
					+ this.extensionIndex + "." + name, value);

			// Continue to add to parent for extension specific properties
			super.addProperty(name, value);
		}
	}

}