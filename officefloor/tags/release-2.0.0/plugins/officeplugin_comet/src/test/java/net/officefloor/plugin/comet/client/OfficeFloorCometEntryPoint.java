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
package net.officefloor.plugin.comet.client;

import net.officefloor.plugin.comet.api.OfficeFloorComet;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * {@link EntryPoint} for testing {@link OfficeFloorComet}.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeFloorCometEntryPoint implements EntryPoint {

	/*
	 * ====================== EntryPoint ======================
	 */

	@Override
	public void onModuleLoad() {

		// Create publisher
		final MockCometListener publisher = OfficeFloorComet.createPublisher(
				MockCometListener.class, null);

		// Provide the widgets
		Panel root = RootPanel.get("comet");
		VerticalPanel panel = new VerticalPanel();
		root.add(panel);

		// Provide widgets to publish event
		HorizontalPanel publishPanel = new HorizontalPanel();
		panel.add(publishPanel);
		final TextBox eventText = new TextBox();
		eventText.setText("TEST");
		publishPanel.add(eventText);
		Button publishButton = new Button("Publish");
		publishPanel.add(publishButton);
		publishButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				// Publish event
				publisher.handleEvent(eventText.getText());
			}
		});

		// Provide the widgets to subscribe to event
		HorizontalPanel subscribePanel = new HorizontalPanel();
		panel.add(subscribePanel);
		final Label label = new Label("Subscribed events:");
		subscribePanel.add(label);

		// Create handler for handling events
		final MockCometListener handler = new MockCometListener() {
			@Override
			public void handleEvent(String event) {
				label.setText(label.getText() + " " + event);
			}
		};

		// Subscribe to events
		OfficeFloorComet.subscribe(MockCometListener.class, handler, null);
	}

}