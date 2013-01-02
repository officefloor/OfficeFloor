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
package net.officefloor.plugin.woof.gwt;

import net.officefloor.plugin.gwt.web.http.section.GwtHttpTemplateSectionExtension;
import net.officefloor.plugin.woof.WoofTemplateExtensionService;
import net.officefloor.plugin.woof.WoofTemplateExtensionServiceContext;

/**
 * {@link WoofTemplateExtensionService} for GWT.
 * 
 * @author Daniel Sagenschneider
 */
public class GwtWoofTemplateExtensionService implements
		WoofTemplateExtensionService {

	/**
	 * Extension alias.
	 */
	public static final String EXTENSION_ALIAS = "GWT";

	/*
	 * ==================== WoofTemplateExtensionService ======================
	 */

	@Override
	public String getTemplateExtensionAlias() {
		return EXTENSION_ALIAS;
	}

	@Override
	public void extendTemplate(WoofTemplateExtensionServiceContext context)
			throws Exception {
		GwtHttpTemplateSectionExtension.extendTemplate(context.getTemplate(),
				context.getWebApplication(), context, context.getClassLoader());
	}

}