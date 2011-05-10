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
package net.officefloor.plugin.web.http.template.section;

import net.officefloor.compile.spi.section.SectionDesigner;
import net.officefloor.compile.spi.section.source.SectionSourceContext;
import net.officefloor.frame.spi.source.SourceProperties;

/**
 * Context for the {@link HttpTemplateSectionExtension}.
 * 
 * @author Daniel Sagenschneider
 */
public interface HttpTemplateSectionExtensionContext extends SourceProperties {

	/**
	 * Obtains the content of the template.
	 * 
	 * @return Content of the template.
	 */
	String getTemplateContent();

	/**
	 * <p>
	 * Enables overriding the content of the template.
	 * <p>
	 * This need not be called, however is available should the extension wish
	 * to change the template content.
	 * 
	 * @param templateContent
	 *            Content of the template.
	 */
	void setTemplateContent(String templateContent);

	/**
	 * Obtains the logic class for the template.
	 * 
	 * @return Logic class for the template.
	 */
	Class<?> getTemplateClass();

	/**
	 * <p>
	 * Enables overriding the class of the template.
	 * <p>
	 * This need not be called, however is available should the extension wish
	 * to change (enhance) the template logic class.
	 * 
	 * @param templateClass
	 *            Template logic class.
	 */
	void setTemplateClass(Class<?> templateClass);

	/**
	 * <p>
	 * Obtains the {@link SectionSourceContext} for the
	 * {@link HttpTemplateSectionSource} to be extended.
	 * <p>
	 * Please be aware that the returned {@link SectionSourceContext} does not
	 * filter the properties. Therefore please use the property methods on this
	 * interface to obtain the extension specific properties.
	 * 
	 * @return {@link SectionSourceContext} for the
	 *         {@link HttpTemplateSectionSource} to be extended.
	 */
	SectionSourceContext getSectionSourceContext();

	/**
	 * Obtains the {@link SectionDesigner} for the
	 * {@link HttpTemplateSectionSource} being extended.
	 * 
	 * @return {@link SectionDesigner} for the {@link HttpTemplateSectionSource}
	 *         being extended.
	 */
	SectionDesigner getSectionDesigner();

}