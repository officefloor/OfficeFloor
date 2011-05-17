/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2011 Daniel Sagenschneider
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

import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.plugin.autowire.AutoWirePropertiesImpl;
import net.officefloor.plugin.autowire.AutoWireSection;
import net.officefloor.plugin.autowire.AutoWireSectionImpl;
import net.officefloor.plugin.web.http.template.parse.HttpTemplate;
import net.officefloor.plugin.web.http.template.section.HttpTemplateSectionExtension;

/**
 * Allows wiring the flows of the {@link HttpTemplate}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpTemplateAutoWireSectionImpl extends AutoWireSectionImpl
		implements HttpTemplateAutoWireSection {

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