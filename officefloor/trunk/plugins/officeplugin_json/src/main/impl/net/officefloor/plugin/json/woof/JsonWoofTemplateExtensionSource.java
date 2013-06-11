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
package net.officefloor.plugin.json.woof;

import net.officefloor.plugin.json.web.http.section.JsonHttpTemplateSectionExtension;
import net.officefloor.plugin.woof.template.WoofTemplateExtensionSource;
import net.officefloor.plugin.woof.template.WoofTemplateExtensionSourceContext;
import net.officefloor.plugin.woof.template.WoofTemplateExtensionSourceService;
import net.officefloor.plugin.woof.template.impl.AbstractWoofTemplateExtensionSource;

/**
 * {@link WoofTemplateExtensionSource} for JSON.
 * 
 * @author Daniel Sagenschneider
 */
public class JsonWoofTemplateExtensionSource extends
		AbstractWoofTemplateExtensionSource implements
		WoofTemplateExtensionSourceService<JsonWoofTemplateExtensionSource> {

	/*
	 * ============= WoofTemplateExtensionSourceService ================
	 */

	@Override
	public boolean isImplicitExtension() {
		return true;
	}

	@Override
	public Class<JsonWoofTemplateExtensionSource> getWoofTemplateExtensionSourceClass() {
		return JsonWoofTemplateExtensionSource.class;
	}

	/*
	 * ================= WoofTemplateExtensionSource ===================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		// No properties required
	}

	@Override
	public void extendTemplate(WoofTemplateExtensionSourceContext context)
			throws Exception {
		JsonHttpTemplateSectionExtension.extendTemplate(context.getTemplate(),
				context.getWebApplication());
	}

}