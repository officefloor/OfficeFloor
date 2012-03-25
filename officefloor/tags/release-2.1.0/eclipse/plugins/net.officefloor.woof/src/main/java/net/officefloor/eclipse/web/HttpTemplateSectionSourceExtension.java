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

package net.officefloor.eclipse.web;

import net.officefloor.compile.properties.Property;
import net.officefloor.eclipse.common.dialog.input.InputHandler;
import net.officefloor.eclipse.common.dialog.input.InputListener;
import net.officefloor.eclipse.common.dialog.input.impl.ClasspathClassInput;
import net.officefloor.eclipse.extension.sectionsource.SectionSourceExtension;
import net.officefloor.eclipse.extension.sectionsource.SectionSourceExtensionContext;
import net.officefloor.eclipse.extension.util.SourceExtensionUtil;
import net.officefloor.plugin.web.http.template.section.HttpTemplateSectionSource;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

/**
 * {@link SectionSourceExtension} for the {@link HttpTemplateSectionSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpTemplateSectionSourceExtension implements
		SectionSourceExtension<HttpTemplateSectionSource> {

	/*
	 * ======================== SectionSourceExtension ======================
	 */

	@Override
	public Class<HttpTemplateSectionSource> getSectionSourceClass() {
		return HttpTemplateSectionSource.class;
	}

	@Override
	public String getSectionSourceLabel() {
		return "HTTP Template";
	}

	@Override
	public void createControl(Composite page,
			final SectionSourceExtensionContext context) {

		// Provide layout if necessary.
		// (Used within HttpTemplateWizardPage)
		if (page.getLayout() == null) {
			SourceExtensionUtil.loadPropertyLayout(page);
		}

		// Obtain the template path property
		final Property propertyLogicClass = context
				.getPropertyList()
				.getOrAddProperty(HttpTemplateSectionSource.PROPERTY_CLASS_NAME);

		// Provide means to specify logic class
		new Label(page, SWT.NONE).setText("Logic class: ");
		InputHandler<String> logicClass = new InputHandler<String>(page,
				new ClasspathClassInput(context.getProject(),
						propertyLogicClass.getValue(), page.getShell()),
				new InputListener() {
					@Override
					public void notifyValueChanged(Object value) {
						// Specify the logic class name
						String logicClassName = (value == null ? "" : value
								.toString());
						propertyLogicClass.setValue(logicClassName);
						context.notifyPropertiesChanged();
					}

					@Override
					public void notifyValueInvalid(String message) {
						context.setErrorMessage(message);
					}
				});
		logicClass.getControl().setLayoutData(
				new GridData(SWT.FILL, SWT.BEGINNING, true, false));
	}

}