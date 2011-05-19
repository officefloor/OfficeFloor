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

import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.web.http.template.section.HttpTemplateSectionExtension;
import net.officefloor.plugin.web.http.template.section.HttpTemplateSectionExtensionContext;

/**
 * Tests the {@link GwtHttpTemplateSectionExtension}.
 * 
 * @author Daniel Sagenschneider
 */
public class GwtHttpTemplateSectionExtensionTest extends OfficeFrameTestCase {

	/**
	 * Script to be added to the template.
	 */
	private static final String SCRIPT = "<script type=\"text/javascript\" language=\"javascript\" src=\"template/template.nocache.js\"></script>";

	/**
	 * IFrame to be added to the template.
	 */
	private static final String IFRAME = "<iframe src=\"javascript:''\" id=\"__gwt_historyFrame\" tabIndex='-1' style=\"position:absolute;width:0;height:0;border:0\"></iframe>";

	/**
	 * {@link GwtHttpTemplateSectionExtension} to test.
	 */
	private final HttpTemplateSectionExtension gwt = new GwtHttpTemplateSectionExtension();

	/**
	 * Mock {@link HttpTemplateSectionExtensionContext}.
	 */
	private final HttpTemplateSectionExtensionContext context = this
			.createMock(HttpTemplateSectionExtensionContext.class);

	/**
	 * Ensure can include GWT.
	 */
	public void testSimpleIncludeGwt() throws Exception {

		// Record adding GWT
		this.recordReturn(this.context, this.context.getTemplateContent(),
				"<html><head></head><body></body></html>");
		this.recordReturn(
				this.context,
				this.context
						.getProperty(GwtHttpTemplateSectionExtension.PROPERTY_TEMPLATE_URI),
				"template");
		this.context.setTemplateContent("<html><head>" + SCRIPT
				+ "</head><body>" + IFRAME + "</body></html>");

		// Test
		this.replayMockObjects();
		this.gwt.extendTemplate(this.context);
		this.verifyMockObjects();
	}

}