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
package net.officefloor.plugin.woof.gwt;

import net.officefloor.compile.properties.PropertyList;
import net.officefloor.plugin.gwt.web.http.section.GwtHttpTemplateSectionExtension;
import net.officefloor.plugin.web.http.application.HttpTemplateAutoWireSection;
import net.officefloor.plugin.woof.WoofTemplateExtensionService;

/**
 * {@link WoofTemplateExtensionService} for GWT.
 * 
 * @author Daniel Sagenschneider
 */
public class GwtWoofTemplateExtensionService implements
		WoofTemplateExtensionService {

	/*
	 * ==================== WoofTemplateExtensionService ======================
	 */

	@Override
	public String getTemplateExtensionAlias() {
		return "GWT";
	}

	@Override
	public void extendTemplate(HttpTemplateAutoWireSection template,
			PropertyList properties) throws Exception {
		GwtHttpTemplateSectionExtension.extendTemplate(template);
	}

}