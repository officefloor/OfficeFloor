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
package net.officefloor.plugin.web.http.template.section;

import net.officefloor.compile.spi.section.SectionDesigner;
import net.officefloor.compile.spi.section.SectionManagedObject;
import net.officefloor.compile.spi.section.SectionObject;
import net.officefloor.compile.spi.section.SectionOutput;
import net.officefloor.compile.spi.section.SectionTask;
import net.officefloor.compile.spi.section.source.SectionSourceContext;
import net.officefloor.frame.spi.source.SourceProperties;
import net.officefloor.plugin.socket.server.http.HttpResponse;

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
	 * Flags that the method on the template class should not have the template
	 * rendered to the {@link HttpResponse} by default on its completion.
	 * 
	 * @param templateClassMethodName
	 *            Name of the method on the template class to be flagged to not
	 *            have template rendered on its completion.
	 */
	void flagAsNonRenderTemplateMethod(String templateClassMethodName);

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

	/**
	 * Obtains the {@link SectionManagedObject} for the template logic object.
	 * 
	 * @return {@link SectionManagedObject}.
	 */
	SectionManagedObject getTemplateLogicObject();

	/**
	 * Obtains the {@link SectionTask} by the name.
	 * 
	 * @param taskName
	 *            {@link SectionTask} name.
	 * @return {@link SectionTask} or <code>null</code> if no
	 *         {@link SectionTask} by name.
	 */
	SectionTask getTask(String taskName);

	/**
	 * Obtains or creates the {@link SectionObject} for the type name.
	 * 
	 * @param typeName
	 *            Type name.
	 * @return {@link SectionObject}.
	 */
	SectionObject getOrCreateSectionObject(String typeName);

	/**
	 * Obtains or creates the {@link SectionOutput}.
	 * 
	 * @param name
	 *            {@link SectionOutput} name.
	 * @param argumentType
	 *            Argument type. May be <code>null</code> if no argument.
	 * @param isEscalationOnly
	 *            <code>true</code> if escalation only.
	 * @return {@link SectionOutput}.
	 */
	SectionOutput getOrCreateSectionOutput(String name, String argumentType,
			boolean isEscalationOnly);

}