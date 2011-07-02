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
package net.officefloor.plugin.comet;

import net.officefloor.frame.impl.construct.source.SourcePropertiesImpl;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.comet.api.OfficeFloorComet;
import net.officefloor.plugin.comet.client.MockCometListener;
import net.officefloor.plugin.comet.internal.CometEvent;
import net.officefloor.plugin.comet.internal.CometRequest;
import net.officefloor.plugin.comet.internal.CometResponse;
import net.officefloor.plugin.comet.internal.CometSubscriptionServiceAsync;
import net.officefloor.plugin.gwt.web.http.section.GwtHttpTemplateSectionExtension;
import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.plugin.web.http.application.HttpTemplateAutoWireSection;
import net.officefloor.plugin.web.http.server.HttpServerAutoWireOfficeFloorSource;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Tests the {@link OfficeFloorComet} functionality.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeFloorCometTest extends OfficeFrameTestCase {

	/**
	 * Ensure able to &quot;long poll&quot; for an event.
	 */
	public void testLongPoll() {
		// TODO provide long poll request
		fail("TODO implement");
	}

	/**
	 * Main method to manually test with a browser.
	 * 
	 * @param args
	 *            Command line arguments.
	 */
	public static void main(String... args) throws Exception {

		// Indicate running manually
		System.out.println("Manually running Comet test application");

		// Obtain the path to the template
		String templatePath = OfficeFloorCometTest.class.getPackage().getName()
				.replace('.', '/')
				+ "/Template.html";

		// Start server with GWT extension
		HttpServerAutoWireOfficeFloorSource source = new HttpServerAutoWireOfficeFloorSource();
		HttpTemplateAutoWireSection template = source.addHttpTemplate(
				templatePath, TemplateLogic.class, "template");

		// Extend the template
		SourcePropertiesImpl properties = new SourcePropertiesImpl();
		properties
				.addProperty(
						GwtHttpTemplateSectionExtension.PROPERTY_GWT_ASYNC_SERVICE_INTERFACES,
						CometSubscriptionServiceAsync.class.getName());
		GwtHttpTemplateSectionExtension.extendTemplate(template, source,
				properties, Thread.currentThread().getContextClassLoader());

		source.openOfficeFloor();
	}

	/**
	 * Template logic class.
	 */
	public static class TemplateLogic {
		public void listen(@Parameter CometRequest request,
				AsyncCallback<CometResponse> callback) {
			callback.onSuccess(new CometResponse(new CometEvent(1,
					MockCometListener.class.getName(), "EVENT", "FILTER_KEY")));
		}
	}

}