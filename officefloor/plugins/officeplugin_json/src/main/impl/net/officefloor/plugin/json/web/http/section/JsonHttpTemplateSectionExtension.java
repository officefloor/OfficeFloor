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
package net.officefloor.plugin.json.web.http.section;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

import net.officefloor.compile.spi.section.SectionDesigner;
import net.officefloor.compile.spi.section.SectionManagedObjectSource;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.plugin.json.HttpJson;
import net.officefloor.plugin.json.JsonResponseWriter;
import net.officefloor.plugin.json.read.JsonRequestReaderManagedObjectSource;
import net.officefloor.plugin.json.write.JsonResponseWriterManagedObjectSource;
import net.officefloor.plugin.web.http.template.section.HttpTemplateSectionExtension;
import net.officefloor.plugin.web.http.template.section.HttpTemplateSectionExtensionContext;

/**
 * {@link HttpTemplateSectionExtension} for JSON.
 * 
 * @author Daniel Sagenschneider
 */
public class JsonHttpTemplateSectionExtension implements HttpTemplateSectionExtension {

	/*
	 * ================= HttpTemplateSectionExtension ===================
	 */

	@Override
	public void extendTemplate(HttpTemplateSectionExtensionContext context) throws Exception {

		// Obtain the template logic class
		Class<?> logicClass = context.getTemplateClass();
		if (logicClass == null) {
			return; // No logic class, no JSON functionality
		}

		// Iterate over methods (in order for deterministic adding)
		Method[] methods = logicClass.getMethods();
		Arrays.sort(methods, new Comparator<Method>() {
			@Override
			public int compare(Method a, Method b) {
				return String.CASE_INSENSITIVE_ORDER.compare(a.getName(), b.getName());
			}
		});

		// Obtain the section designer
		SectionDesigner designer = context.getSectionDesigner();

		// Iterate over the methods adding necessary JSON functionality
		Set<String> jsonResponseWriterMethods = new HashSet<String>();
		for (Method method : methods) {

			// Iterate over parameters for method
			for (Class<?> parameterType : method.getParameterTypes()) {

				// Determine if have HttpJson parameter
				HttpJson annotation = parameterType.getAnnotation(HttpJson.class);
				if (annotation != null) {

					// Add the JSON request reader for parameter type
					SectionManagedObjectSource readObjectMos = designer.addSectionManagedObjectSource(
							parameterType.getName(), JsonRequestReaderManagedObjectSource.class.getName());
					readObjectMos.addProperty(JsonRequestReaderManagedObjectSource.PROPERTY_JSON_OBJECT_CLASS,
							parameterType.getName());
					readObjectMos.addSectionManagedObject(parameterType.getName(), ManagedObjectScope.PROCESS);

					// Providing binding name if specified
					String bindName = annotation.value();
					if ((bindName != null) && (bindName.length() > 0)) {
						readObjectMos.addProperty(JsonRequestReaderManagedObjectSource.PROPERTY_BIND_NAME, bindName);

					}
				}

				// Determine if parameter is JsonResponseWriter
				if (parameterType.getName().equals(JsonResponseWriter.class.getName())) {

					// Include the method
					jsonResponseWriterMethods.add(method.getName());

					// Flag as non-render template method
					context.flagAsNonRenderTemplateMethod(method.getName());
				}
			}
		}

		// Determine if include JsonResponseWriter
		if (jsonResponseWriterMethods.size() > 0) {

			// Include the JsonResponseWriter
			designer.addSectionManagedObjectSource("JSON_WRITER",
					JsonResponseWriterManagedObjectSource.class.getName());
		}
	}

}