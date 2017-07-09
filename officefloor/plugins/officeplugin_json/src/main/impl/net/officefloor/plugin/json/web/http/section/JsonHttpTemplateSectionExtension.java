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

import net.officefloor.autowire.AutoWireObject;
import net.officefloor.compile.internal.structure.AutoWire;
import net.officefloor.plugin.json.HttpJson;
import net.officefloor.plugin.json.JsonResponseWriter;
import net.officefloor.plugin.json.read.JsonRequestReaderManagedObjectSource;
import net.officefloor.plugin.json.write.JsonResponseWriterManagedObjectSource;
import net.officefloor.plugin.web.http.application.HttpTemplateSection;
import net.officefloor.plugin.web.http.application.HttpTemplateAutoWireSectionExtension;
import net.officefloor.plugin.web.http.application.WebArchitect;
import net.officefloor.plugin.web.http.template.section.HttpTemplateSectionExtension;
import net.officefloor.plugin.web.http.template.section.HttpTemplateSectionExtensionContext;

/**
 * {@link HttpTemplateSectionExtension} for JSON.
 * 
 * @author Daniel Sagenschneider
 */
public class JsonHttpTemplateSectionExtension implements HttpTemplateSectionExtension {

	/**
	 * Comma separate list of {@link Method} names having an
	 * {@link JsonResponseWriter} parameter.
	 */
	public static final String PROPERTY_JSON_AJAX_METHOD_NAMES = "json.ajax.method.names";

	/**
	 * Extends the {@link HttpTemplateSection} by:
	 * <ul>
	 * <li>providing the necessary {@link JsonRequestReaderManagedObjectSource}
	 * instances</li>
	 * <li>adding this as a {@link HttpTemplateSectionExtension}.
	 * </ul>
	 * 
	 * @param template
	 *            {@link HttpTemplateSection}.
	 * @param application
	 *            {@link WebArchitect}.
	 */
	public static void extendTemplate(HttpTemplateSection template, WebArchitect application) {

		// Obtain the template logic class
		Class<?> logicClass = template.getTemplateLogicClass();
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

		// Iterate over the methods adding necessary JSON functionality
		Set<String> jsonResponseWriterMethods = new HashSet<String>();
		for (Method method : methods) {

			// Iterate over parameters for method
			for (Class<?> parameterType : method.getParameterTypes()) {

				// Determine if have HttpJson parameter
				HttpJson annotation = parameterType.getAnnotation(HttpJson.class);
				if (annotation != null) {

					// Include JSON reader for parameter type
					AutoWire readAutoWire = new AutoWire(parameterType);
					if (!(application.isObjectAvailable(readAutoWire))) {

						// Add the JSON request reader for parameter type
						AutoWireObject readObject = application.addManagedObject(
								JsonRequestReaderManagedObjectSource.class.getName(), null, readAutoWire);
						readObject.addProperty(JsonRequestReaderManagedObjectSource.PROPERTY_JSON_OBJECT_CLASS,
								parameterType.getName());

						// Providing binding name if specified
						String bindName = annotation.value();
						if ((bindName != null) && (bindName.length() > 0)) {
							readObject.addProperty(JsonRequestReaderManagedObjectSource.PROPERTY_BIND_NAME, bindName);
						}
					}
				}

				// Determine if parameter is JsonResponseWriter
				if (parameterType.getName().equals(JsonResponseWriter.class.getName())) {

					// Include the method
					jsonResponseWriterMethods.add(method.getName());
				}
			}
		}

		// Determine if include JsonResponseWriter
		if (jsonResponseWriterMethods.size() > 0) {

			// Include the JsonResponseWriter (if not already added)
			AutoWire writeAutoWire = new AutoWire(JsonResponseWriter.class);
			if (!(application.isObjectAvailable(writeAutoWire))) {
				application.addManagedObject(JsonResponseWriterManagedObjectSource.class.getName(), null,
						writeAutoWire);
			}

			// Extend HTTP template for JsonResponseWriter methods
			HttpTemplateAutoWireSectionExtension extension = template
					.addTemplateExtension(JsonHttpTemplateSectionExtension.class);

			// Provide listing of method names (in deterministic order)
			String[] methodNames = jsonResponseWriterMethods.toArray(new String[jsonResponseWriterMethods.size()]);
			Arrays.sort(methodNames);
			StringBuilder methodNamesValue = new StringBuilder();
			boolean isFirst = true;
			for (String methodName : methodNames) {
				if (!isFirst) {
					methodNamesValue.append(",");
				}
				isFirst = false;
				methodNamesValue.append(methodName);
			}
			extension.addProperty(PROPERTY_JSON_AJAX_METHOD_NAMES, methodNamesValue.toString());
		}
	}

	/*
	 * ================= HttpTemplateSectionExtension ===================
	 */

	@Override
	public void extendTemplate(HttpTemplateSectionExtensionContext context) throws Exception {

		// Flag non render methods
		String methodNamesValue = context.getProperty(PROPERTY_JSON_AJAX_METHOD_NAMES);
		for (String methodName : methodNamesValue.split(",")) {
			methodName = methodName.trim();
			context.flagAsNonRenderTemplateMethod(methodName);
		}
	}

}