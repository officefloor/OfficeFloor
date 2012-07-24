/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2012 Daniel Sagenschneider
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

package net.officefloor.eclipse.socket;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import net.officefloor.compile.impl.properties.PropertyListSourceProperties;
import net.officefloor.compile.properties.Property;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.eclipse.common.dialog.input.InputHandler;
import net.officefloor.eclipse.common.dialog.input.InputListener;
import net.officefloor.eclipse.common.dialog.input.impl.BeanListInput;
import net.officefloor.eclipse.extension.open.ExtensionOpener;
import net.officefloor.eclipse.extension.open.ExtensionOpenerContext;
import net.officefloor.eclipse.extension.util.PropertyValueChangeEvent;
import net.officefloor.eclipse.extension.util.PropertyValueChangeListener;
import net.officefloor.eclipse.extension.util.SourceExtensionUtil;
import net.officefloor.eclipse.extension.worksource.TaskDocumentationContext;
import net.officefloor.eclipse.extension.worksource.WorkSourceExtension;
import net.officefloor.eclipse.extension.worksource.WorkSourceExtensionContext;
import net.officefloor.eclipse.socket.HttpRouteWorkSourceExtension.RoutingEntry;
import net.officefloor.eclipse.util.EclipseUtil;
import net.officefloor.frame.impl.construct.source.SourceContextImpl;
import net.officefloor.frame.spi.source.SourceContext;
import net.officefloor.frame.spi.source.UnknownResourceError;
import net.officefloor.plugin.socket.server.http.HttpResponse;
import net.officefloor.plugin.web.http.template.HttpTemplateWork;
import net.officefloor.plugin.web.http.template.HttpTemplateWorkSource;
import net.officefloor.plugin.web.http.template.parse.HttpTemplate;
import net.officefloor.plugin.web.http.template.parse.HttpTemplateSection;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

/**
 * {@link WorkSourceExtension} for the {@link HttpHtmlTemplateWorkSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpTemplateWorkSourceExtension
		extends
		AbstractSocketWorkSourceExtension<HttpTemplateWork, HttpTemplateWorkSource>
		implements ExtensionOpener {

	/**
	 * Initiate.
	 */
	public HttpTemplateWorkSourceExtension() {
		super(HttpTemplateWorkSource.class, "Http Template");
	}

	/*
	 * ================== WorkSourceExtension =================================
	 */

	@Override
	public void createControl(Composite page,
			final WorkSourceExtensionContext context) {

		// Provide listing of section names to bean types
		final BeanListInput<SectionToBeanTypeMapping> input = new BeanListInput<SectionToBeanTypeMapping>(
				SectionToBeanTypeMapping.class, false);
		input.addProperty("Section", 1);
		input.addProperty("Type", 2);

		// Add initial section to bean type properties
		loadSectionToBeanTypeMappings(input, context);

		// Provide means to specify template file
		page.setLayout(new GridLayout(1, false));
		Composite template = new Composite(page, SWT.NONE);
		template.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true,
				false));
		SourceExtensionUtil.loadPropertyLayout(template);
		SourceExtensionUtil.createPropertyResource("Template",
				HttpTemplateWorkSource.PROPERTY_TEMPLATE_FILE, template,
				context, new PropertyValueChangeListener() {
					@Override
					public void propertyValueChanged(
							PropertyValueChangeEvent event) {
						try {
							// Reset section to bean properties
							HttpTemplateWorkSourceExtension
									.resetSectionToBeanTypeMappings(input,
											context);
						} catch (IOException ex) {
							// Provide detail of failure
							context.setErrorMessage(ex.getMessage() + " ["
									+ ex.getClass().getName() + "]");
						}
					}
				});

		// Add control to alter properties
		new InputHandler<List<RoutingEntry>>(page, input, new InputListener() {
			@Override
			public void notifyValueChanged(Object value) {
				// Notify of change
				context.notifyPropertiesChanged();
			}

			@Override
			public void notifyValueInvalid(String message) {
				context.setErrorMessage(message);
			}
		});
	}

	/**
	 * Resets the {@link SectionToBeanTypeMapping} instances and associated
	 * {@link Property} instances to be for the
	 * {@link HttpTemplateWorkSource#PROPERTY_TEMPLATE_FILE}.
	 * 
	 * @param input
	 *            {@link SectionToBeanTypeMapping}.
	 * @param context
	 *            {@link WorkSourceExtensionContext}.
	 * @throws IOException
	 *             If fails to reset.
	 */
	private static void resetSectionToBeanTypeMappings(
			BeanListInput<SectionToBeanTypeMapping> input,
			WorkSourceExtensionContext context) throws IOException {

		// Clear the beans from input
		input.clearBeans();

		// Remove the section to bean type mapping properties
		PropertyList properties = context.getPropertyList();
		for (String name : properties.getPropertyNames()) {
			if ((name == null)
					|| (!name
							.startsWith(HttpTemplateWorkSource.PROPERTY_BEAN_PREFIX))) {
				continue; // not a mapping property
			}

			// Mapping property so remove
			properties.removeProperty(properties.getProperty(name));
		}

		// Create the source context
		SourceContext sourceContext = new SourceContextImpl(true,
				context.getClassLoader());
		sourceContext = new SourceContextImpl(true, sourceContext,
				new PropertyListSourceProperties(properties));

		// Obtain the template file
		HttpTemplate template;
		try {
			template = HttpTemplateWorkSource.getHttpTemplate(sourceContext);
		} catch (UnknownResourceError ex) {
			// No file so no properties
			return;
		}

		// Iterate over segments providing necessary properties
		for (HttpTemplateSection section : template.getSections()) {

			// Only include section if requires bean
			if (!HttpTemplateWorkSource
					.isHttpTemplateSectionRequireBean(section)) {
				continue; // ignore as does not require bean
			}

			// Add the property for the bean type
			String propertyName = HttpTemplateWorkSource.PROPERTY_BEAN_PREFIX
					+ section.getSectionName();
			properties.addProperty(propertyName);
		}

		// Load the section to bean mappings (now properties available)
		loadSectionToBeanTypeMappings(input, context);
	}

	/**
	 * Loads the mappings of section to bean type.
	 * 
	 * @param input
	 *            {@Link BeanListInput} containing the mappings.
	 * @param context
	 *            {@link WorkSourceExtensionContext}.
	 */
	private static void loadSectionToBeanTypeMappings(
			BeanListInput<SectionToBeanTypeMapping> input,
			WorkSourceExtensionContext context) {

		// Add the initial section to bean type properties
		for (Property property : context.getPropertyList()) {
			String name = property.getName();
			if ((name == null)
					|| (!name
							.startsWith(HttpTemplateWorkSource.PROPERTY_BEAN_PREFIX))) {
				continue; // not a mapping property
			}
			name = name.substring(HttpTemplateWorkSource.PROPERTY_BEAN_PREFIX
					.length());

			// Add the initial mappings
			input.addBean(new SectionToBeanTypeMapping(property, name));
		}
	}

	@Override
	public String getSuggestedWorkName(PropertyList properties) {
		// Obtain the template name
		String templateName = properties.getProperty(
				HttpTemplateWorkSource.PROPERTY_TEMPLATE_FILE).getValue();
		int simpleNameIndex = templateName.lastIndexOf('/');
		if (simpleNameIndex >= 0) {
			// Strip to simple name (+1 to ignore '.')
			templateName = templateName.substring(simpleNameIndex + 1);
		}

		// Return the name
		return "HttpTemplate-" + templateName;
	}

	@Override
	public String getTaskDocumentation(TaskDocumentationContext context)
			throws Throwable {

		// Obtain the task name as the section name
		String sectionName = context.getTaskName();

		// Obtain the property template file
		String templateName = context.getPropertyList()
				.getPropertyValue(
						HttpTemplateWorkSource.PROPERTY_TEMPLATE_FILE,
						"<not specified");

		// Provide documentation
		return "Writes section " + sectionName + " of template file "
				+ templateName + " to the "
				+ HttpResponse.class.getSimpleName();
	}

	/**
	 * Mapping of the {@link HttpTemplateSection} to bean type.
	 */
	public static class SectionToBeanTypeMapping {

		/**
		 * {@link Property}.
		 */
		private final Property property;

		/**
		 * Name of the {@link HttpTemplateSection}.
		 */
		private final String sectionName;

		/**
		 * Initiate.
		 * 
		 * @param property
		 *            {@link Property} to be configured.
		 * @param sectionName
		 *            Name of the {@link HttpTemplateSection}.
		 */
		public SectionToBeanTypeMapping(Property property, String sectionName) {
			this.property = property;
			this.sectionName = sectionName;
		}

		/**
		 * Obtains the section name.
		 * 
		 * @return Section name.
		 */
		public String getSection() {
			return this.sectionName;
		}

		/**
		 * Obtains the bean type.
		 * 
		 * @return Bean type.
		 */
		public String getType() {
			// Property value is the type
			return this.property.getValue();
		}

		/**
		 * Specifies the bean type.
		 * 
		 * @param type
		 *            Bean type.
		 */
		public void setType(String type) {
			// Property value is the type
			this.property.setValue(type);
		}
	}

	/*
	 * ======================== ExtensionOpener ==========================
	 */

	@Override
	public void openSource(ExtensionOpenerContext context) throws Exception {

		// Obtain the location of the template
		String location = context.getPropertyList().getPropertyValue(
				HttpTemplateWorkSource.PROPERTY_TEMPLATE_FILE, null);

		// Ensure have the template location
		if (EclipseUtil.isBlank(location)) {
			throw new FileNotFoundException("Template file not specified");
		}

		// Open the template file
		context.openClasspathResource(location);
	}

}