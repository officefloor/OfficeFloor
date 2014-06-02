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
package net.officefloor.eclipse.gwt;

import net.officefloor.compile.properties.Property;
import net.officefloor.eclipse.common.dialog.input.Input;
import net.officefloor.eclipse.common.dialog.input.InputHandler;
import net.officefloor.eclipse.common.dialog.input.InputListener;
import net.officefloor.eclipse.common.dialog.input.csv.InputFactory;
import net.officefloor.eclipse.common.dialog.input.csv.ListInput;
import net.officefloor.eclipse.common.dialog.input.impl.ClasspathClassInput;
import net.officefloor.eclipse.extension.template.WoofTemplateExtensionSourceExtension;
import net.officefloor.eclipse.extension.template.WoofTemplateExtensionSourceExtensionContext;
import net.officefloor.eclipse.extension.util.WoofSourceExtensionUtil;
import net.officefloor.plugin.gwt.woof.GwtWoofTemplateExtensionSource;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

/**
 * GWT {@link WoofTemplateExtensionSourceExtension}.
 * 
 * @author Daniel Sagenschneider
 */
public class GwtWoofTemplateExtensionSourceExtension implements
		WoofTemplateExtensionSourceExtension<GwtWoofTemplateExtensionSource> {

	/*
	 * ================ WoofTemplateExtensionSourceExtension ==================
	 */

	@Override
	public Class<GwtWoofTemplateExtensionSource> getWoofTemplateExtensionSourceClass() {
		return GwtWoofTemplateExtensionSource.class;
	}

	@Override
	public String getWoofTemplateExtensionSourceLabel() {
		return "GWT";
	}

	@Override
	public void createControl(final Composite page,
			final WoofTemplateExtensionSourceExtensionContext context) {

		// Load layout
		WoofSourceExtensionUtil.loadPropertyLayout(page);

		// EntryPoint class name
		WoofSourceExtensionUtil
				.createPropertyClass(
						"EntryPoint Class",
						GwtWoofTemplateExtensionSource.PROPERTY_GWT_ENTRY_POINT_CLASS_NAME,
						page, context, null);

		// Obtain the initial GWT Async Interfaces
		final Property gwtServiceAsyncInterfacesProperty = context
				.getPropertyList()
				.getOrAddProperty(
						GwtWoofTemplateExtensionSource.PROPERTY_GWT_ASYNC_SERVICE_INTERFACES);
		String gwtServiceAsyncInterfacesValue = gwtServiceAsyncInterfacesProperty
				.getValue();
		String[] gwtServiceAsyncInterfaces = (gwtServiceAsyncInterfacesValue == null ? new String[0]
				: gwtServiceAsyncInterfacesValue.split(","));

		// Provide means to specify GWT Service Async Interfaces
		new Label(page, SWT.NONE).setText("GWT Service Async Interfaces: ");
		ListInput<Composite> gwtServicesAsyncInterfacesListInput = new ListInput<Composite>(
				String.class, page, new InputFactory<Composite>() {
					@Override
					public Input<Composite> createInput() {
						return new ClasspathClassInput(context.getProject(),
								page.getShell());
					}
				});
		InputHandler<String[]> gwtAsyncServices = new InputHandler<String[]>(
				page, gwtServicesAsyncInterfacesListInput,
				gwtServiceAsyncInterfaces, new InputListener() {
					@Override
					public void notifyValueChanged(Object value) {

						// Obtain the value
						String[] gwtServiceAsyncInterfaces = (String[]) value;

						// Specify the value
						StringBuilder interfacesValue = new StringBuilder();
						if (gwtServiceAsyncInterfaces != null) {
							boolean isFirst = true;
							for (String gwtServiceAsyncInterface : gwtServiceAsyncInterfaces) {
								if (!isFirst) {
									interfacesValue.append(",");
								}
								isFirst = false;
								interfacesValue
										.append(gwtServiceAsyncInterface);
							}
						}
						gwtServiceAsyncInterfacesProperty
								.setValue(interfacesValue.toString());

						// Notify value changed
						context.notifyPropertiesChanged();
					}

					@Override
					public void notifyValueInvalid(String message) {
						gwtServiceAsyncInterfacesProperty.setValue("");
						context.notifyPropertiesChanged();
					}
				});
		gwtAsyncServices.getControl().setLayoutData(
				new GridData(SWT.FILL, SWT.BEGINNING, true, false));

		// Provide means to enable Comet
		WoofSourceExtensionUtil.createPropertyCheckbox("Enable Comet",
				GwtWoofTemplateExtensionSource.PROPERTY_ENABLE_COMET, false,
				String.valueOf(true), String.valueOf(false), page, context,
				null);

		// Provide means to specify manual publish handle method name
		WoofSourceExtensionUtil
				.createPropertyText(
						"Comet Manual Publish Method",
						GwtWoofTemplateExtensionSource.PROPERTY_COMET_MANUAL_PUBLISH_METHOD_NAME,
						"", page, context, null);
	}

}