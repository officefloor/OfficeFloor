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

import net.officefloor.eclipse.extension.template.WoofTemplateExtensionSourceExtension;
import net.officefloor.eclipse.extension.template.WoofTemplateExtensionSourceExtensionContext;
import net.officefloor.plugin.woof.gwt.GwtWoofTemplateExtensionSource;

import org.eclipse.swt.SWT;
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
	public void createControl(Composite page,
			WoofTemplateExtensionSourceExtensionContext context) {

		// TODO provide details
		Label todo = new Label(page, SWT.NONE);
		todo.setText("TODO provide means to configure GWT");
	}

}