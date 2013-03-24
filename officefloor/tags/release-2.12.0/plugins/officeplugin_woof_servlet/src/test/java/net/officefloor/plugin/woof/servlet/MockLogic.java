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
package net.officefloor.plugin.woof.servlet;

import net.officefloor.plugin.comet.CometPublisherInterface;
import net.officefloor.plugin.comet.api.CometSubscriber;
import net.officefloor.plugin.section.clazz.Parameter;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Mock logic for the templates.
 * 
 * @author Daniel Sagenschneider
 */
public class MockLogic {

	/**
	 * Mock content for the template.
	 */
	public static class MockContent {

		private String text;

		public MockContent(String text) {
			this.text = text;
		}

		public String getText() {
			return this.text;
		}
	}

	/**
	 * Obtains the data for the template.
	 * 
	 * @param dependency
	 *            {@link MockDependency}.
	 * @return {@link MockContent} for rendering template.
	 */
	public MockContent getTemplateData(MockDependency dependency) {
		Thread thread = Thread.currentThread();
		return new MockContent(dependency.getMessage() + " " + thread.getName());
	}

	/**
	 * Provides the servicing of the GWT AJAX call.
	 * 
	 * @param text
	 *            Text from the client.
	 * @param callback
	 *            {@link AsyncCallback}.
	 */
	public void gwtService(@Parameter String text,
			AsyncCallback<String> callback) {
		callback.onSuccess("AJAX-" + text);
	}

	/**
	 * Comet event trigger.
	 */
	@CometPublisherInterface
	public static interface CometTrigger extends CometSubscriber {

		void trigger(String event);
	}

	/**
	 * Triggers the comet event.
	 * 
	 * @param trigger
	 *            {@link CometTrigger}.
	 * @param text
	 *            Text for the event.
	 * @param callback
	 *            {@link AsyncCallback}.
	 */
	public void cometTrigger(CometTrigger trigger, @Parameter String text,
			AsyncCallback<Void> callback) {
		trigger.trigger(text);
		callback.onSuccess(null);
	}

}