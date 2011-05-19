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
package net.officefloor.plugin.gwt.web.http.section;

import net.officefloor.plugin.gwt.template.transform.HtmlTemplateTransformerImpl;
import net.officefloor.plugin.web.http.template.section.HttpTemplateSectionExtension;
import net.officefloor.plugin.web.http.template.section.HttpTemplateSectionExtensionContext;

/**
 * {@link HttpTemplateSectionExtension} to include GWT.
 * 
 * @author Daniel Sagenschneider
 */
public class GwtHttpTemplateSectionExtension implements
		HttpTemplateSectionExtension {

	/**
	 * Name of property specifying the URI for the template.
	 */
	public static final String PROPERTY_TEMPLATE_URI = "template.uri";

	/**
	 * Id for the GWT History IFrame.
	 */
	private static final String GWT_HISTORY_ID = "__gwt_historyFrame";

	/**
	 * GWT History IFrame.
	 */
	private static final String GWT_HISTORY_IFRAME = "<iframe src=\"javascript:''\" id=\""
			+ GWT_HISTORY_ID
			+ "\" tabIndex='-1' style=\"position:absolute;width:0;height:0;border:0\"></iframe>";

	/*
	 * ================== HttpTemplateSectionExtension ====================
	 */

	@Override
	public void extendTemplate(HttpTemplateSectionExtensionContext context)
			throws Exception {

		// Obtain the template
		String template = context.getTemplateContent();

		// Construct GWT script
		String templateUri = context.getProperty(PROPERTY_TEMPLATE_URI);
		String scriptPath = templateUri + "/" + templateUri + ".nocache.js";
		String script = "<script type=\"text/javascript\" language=\"javascript\" src=\""
				+ scriptPath + "\"></script>";

		// Transform the content
		template = new HtmlTemplateTransformerImpl().transform(template, null);

		// Write back the template
		context.setTemplateContent(template);
	}

}