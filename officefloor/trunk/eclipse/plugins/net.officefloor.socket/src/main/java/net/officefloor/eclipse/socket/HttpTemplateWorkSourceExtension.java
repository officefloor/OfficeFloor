/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2009 Daniel Sagenschneider
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

import net.officefloor.compile.properties.PropertyList;
import net.officefloor.eclipse.extension.util.SourceExtensionUtil;
import net.officefloor.eclipse.extension.worksource.WorkSourceExtension;
import net.officefloor.eclipse.extension.worksource.WorkSourceExtensionContext;
import net.officefloor.plugin.socket.server.http.template.HttpTemplateWork;
import net.officefloor.plugin.socket.server.http.template.HttpTemplateWorkSource;

import org.eclipse.swt.widgets.Composite;

/**
 * {@link WorkSourceExtension} for the {@link HttpHtmlTemplateWorkSource}.
 *
 * @author Daniel Sagenschneider
 */
public class HttpTemplateWorkSourceExtension implements
		WorkSourceExtension<HttpTemplateWork, HttpTemplateWorkSource> {

	/*
	 * ================== WorkSourceExtension =================================
	 */

	@Override
	public Class<HttpTemplateWorkSource> getWorkSourceClass() {
		return HttpTemplateWorkSource.class;
	}

	@Override
	public String getWorkSourceLabel() {
		return "HTTP Template";
	}

	@Override
	public void createControl(Composite page, WorkSourceExtensionContext context) {

		// Provide properties
		SourceExtensionUtil.loadPropertyLayout(page);
		SourceExtensionUtil.createPropertyText("Template",
				HttpTemplateWorkSource.PROPERTY_TEMPLATE_FILE, "", page,
				context);
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
		return "Http Template " + templateName;
	}

}