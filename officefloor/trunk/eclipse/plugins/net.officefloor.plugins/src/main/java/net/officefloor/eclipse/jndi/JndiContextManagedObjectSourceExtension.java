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
package net.officefloor.eclipse.jndi;

import net.officefloor.compile.properties.PropertyList;
import net.officefloor.eclipse.common.dialog.input.InputAdapter;
import net.officefloor.eclipse.common.dialog.input.InputHandler;
import net.officefloor.eclipse.common.dialog.input.impl.PropertyListInput;
import net.officefloor.eclipse.extension.classpath.ClasspathProvision;
import net.officefloor.eclipse.extension.classpath.ExtensionClasspathProvider;
import net.officefloor.eclipse.extension.classpath.TypeClasspathProvision;
import net.officefloor.eclipse.extension.managedobjectsource.ManagedObjectSourceExtension;
import net.officefloor.eclipse.extension.managedobjectsource.ManagedObjectSourceExtensionContext;
import net.officefloor.eclipse.extension.util.SourceExtensionUtil;
import net.officefloor.frame.api.build.None;
import net.officefloor.plugin.jndi.context.JndiContextManagedObjectSource;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

/**
 * {@link ManagedObjectSourceExtension} for the
 * {@link JndiContextManagedObjectSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class JndiContextManagedObjectSourceExtension
		implements
		ManagedObjectSourceExtension<None, None, JndiContextManagedObjectSource>,
		ExtensionClasspathProvider {

	/*
	 * ===================== ManagedObjectSourceExtension =====================
	 */

	@Override
	public Class<JndiContextManagedObjectSource> getManagedObjectSourceClass() {
		return JndiContextManagedObjectSource.class;
	}

	@Override
	public String getManagedObjectSourceLabel() {
		return "JNDI Context";
	}

	@Override
	public void createControl(Composite page,
			final ManagedObjectSourceExtensionContext context) {

		// Obtain the properties
		PropertyList properties = context.getPropertyList();

		// Specify layout
		page.setLayout(new GridLayout(1, false));

		// Create Panel for inputs
		Composite subContextPanel = new Composite(page, SWT.NONE);
		SourceExtensionUtil.loadPropertyLayout(subContextPanel);

		// Add the Sub Context Name
		SourceExtensionUtil.createPropertyText("Sub-context JNDI name",
				JndiContextManagedObjectSource.PROPERTY_SUB_CONTEXT_NAME,
				"java:comp/env", subContextPanel, context, null);

		// Add checkbox to validate Context
		SourceExtensionUtil.createPropertyCheckbox("Validate",
				JndiContextManagedObjectSource.PROPERTY_VALIDATE, false,
				"true", "false", subContextPanel, context, null);

		// Input for the properties
		final PropertyListInput propertiesInput = new PropertyListInput(
				properties);
		propertiesInput
				.hideProperty(JndiContextManagedObjectSource.PROPERTY_SUB_CONTEXT_NAME);
		propertiesInput
				.hideProperty(JndiContextManagedObjectSource.PROPERTY_VALIDATE);
		new InputHandler<PropertyList>(page, propertiesInput,
				new InputAdapter() {
					@Override
					public void notifyValueChanged(Object value) {
						context.notifyPropertiesChanged();
					}
				});
	}

	@Override
	public String getSuggestedManagedObjectSourceName(PropertyList properties) {
		return "Context";
	}

	/*
	 * ==================== ExtensionClasspathProvider ======================
	 */

	@Override
	public ClasspathProvision[] getClasspathProvisions() {
		return new ClasspathProvision[] { new TypeClasspathProvision(
				JndiContextManagedObjectSource.class) };
	}

}