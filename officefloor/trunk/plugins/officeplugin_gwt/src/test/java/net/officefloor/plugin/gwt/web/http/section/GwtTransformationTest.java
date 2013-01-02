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
package net.officefloor.plugin.gwt.web.http.section;

import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.web.http.template.section.HttpTemplateSectionExtension;
import net.officefloor.plugin.web.http.template.section.HttpTemplateSectionExtensionContext;

/**
 * Tests the {@link GwtHttpTemplateSectionExtension}.
 * 
 * @author Daniel Sagenschneider
 */
public class GwtTransformationTest extends OfficeFrameTestCase {

	/**
	 * Script to be added to the template.
	 */
	private static final String SCRIPT = "<script type=\"text/javascript\" language=\"javascript\" src=\"URI/URI.nocache.js\"></script>";

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
	 * Ensure can include GWT on empty HTML.
	 */
	public void testEmptyHtml() throws Exception {
		this.doTest("<html/>",
				"<html><head>SCRIPT</head><body>IFRAME</body></html>");
	}

	/**
	 * Ensure can include on simple HTML.
	 */
	public void testSimpleHtml() throws Exception {
		this.doTest("<html><head>SCRIPT</head><body>IFRAME</body></html>");
	}

	/**
	 * Ensure can include on BODY only HTML.
	 */
	public void testBodyOnlyHtml() throws Exception {
		this.doTest("<html><body/></html>",
				"<html><head>SCRIPT</head><body>IFRAME</body></html>");
	}

	/**
	 * Ensure can include on HEAD only HTML.
	 */
	public void testHeadOnlyHtml() throws Exception {
		this.doTest("<html><head/></html>",
				"<html><head>SCRIPT</head><body>IFRAME</body></html>");
	}

	/**
	 * Ensure can include with additional content.
	 */
	public void testAdditionalContent() throws Exception {
		this.doTest("<html><head>SCRIPT<title>Test</title><body>IFRAME<p>Hellow World</p></body></html>");
	}

	/**
	 * Ensure can handle root template.
	 */
	public void testRootTemplate() throws Exception {
		this.doTest("<html/>",
				"<html><head>SCRIPT</head><body>IFRAME</body></html>", "/",
				"root");
	}

	/**
	 * Undertakes the test.
	 * 
	 * @param templateHtml
	 *            Template HTML.
	 */
	private void doTest(String templateHtml) throws Exception {
		this.doTest(templateHtml, templateHtml);
	}

	/**
	 * Undertakes the test.
	 * 
	 * @param rawHtml
	 *            Raw HTML.
	 * @param transformedHtml
	 *            Transformed HTML.
	 */
	private void doTest(String rawHtml, String transformedHtml)
			throws Exception {
		this.doTest(rawHtml, transformedHtml, "template", "template");
	}

	/**
	 * Undertakes the test.
	 * 
	 * @param rawHtml
	 *            Raw HTML.
	 * @param transformedHtml
	 *            Transformed HTML.
	 * @param templateUri
	 *            Template URI.
	 * @param gwtServiceUri
	 *            GWT Service URI.
	 */
	private void doTest(String rawHtml, String transformedHtml,
			String templateUri, String gwtServiceUri) throws Exception {

		// Transform the the script
		String scriptValue = SCRIPT.replace("URI", gwtServiceUri);

		// Transform template
		final String SCRIPT_TAG = "SCRIPT";
		final String IFRAME_TAG = "IFRAME";
		rawHtml = rawHtml.replace(SCRIPT_TAG, "").replace(IFRAME_TAG, "");
		transformedHtml = transformedHtml.replace(SCRIPT_TAG, scriptValue)
				.replace(IFRAME_TAG, IFRAME);

		// Record adding GWT
		this.recordReturn(this.context, this.context.getTemplateContent(),
				rawHtml);
		this.recordReturn(
				this.context,
				this.context
						.getProperty(GwtHttpTemplateSectionExtension.PROPERTY_TEMPLATE_URI),
				templateUri);
		this.context.setTemplateContent(transformedHtml);
		this.recordReturn(
				this.context,
				this.context
						.getProperty(
								GwtHttpTemplateSectionExtension.PROPERTY_GWT_ASYNC_SERVICE_INTERFACES,
								null), null);

		// Test
		this.replayMockObjects();
		this.gwt.extendTemplate(this.context);
		this.verifyMockObjects();
	}

}