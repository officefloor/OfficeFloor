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
package net.officefloor.plugin.gwt.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * Example for using GWT.
 * 
 * @author Daniel Sagenschneider
 */
public class Example implements EntryPoint {

	@Override
	public void onModuleLoad() {

		RootPanel root = RootPanel.get("exampleContainer");

		VerticalPanel panel = new VerticalPanel();
		final Label label = new Label("1");
		panel.add(label);

		// Increment
		Timer timer = new Timer() {
			@Override
			public void run() {
				String text = label.getText();
				int value = Integer.valueOf(text);
				label.setText(String.valueOf(value + 1));
			}
		};
		timer.scheduleRepeating(1000);

		// Check for service
		final Label name = new Label("-Press Button");
		panel.add(name);
		Button button = new Button("Service");
		panel.add(button);
		button.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				SomeServiceAsync service = GWT.create(SomeService.class);
				service.doService("test", new AsyncCallback<String>() {
					@Override
					public void onSuccess(String result) {
						name.setText("-Successful");
					}

					@Override
					public void onFailure(Throwable caught) {
						name.setText("-Failed: " + caught.getMessage() + " ["
								+ caught.getClass().getName() + "]");
					}
				});
			}
		});

		root.add(panel);
	}

}