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
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.RootPanel;
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

		// Provide the widgets
		Panel root = RootPanel.get("comet");
		VerticalPanel panel = new VerticalPanel();
		root.add(panel);
		final Label label = new Label("Press button");
		panel.add(label);
		Button button = new Button("Comet");
		panel.add(button);

		// Create the listener
		final MockCometListener listener = new MockCometListener() {
			@Override
			public void handleEvent(String event) {
				label.setText(event);
			}
		};

		// Register listener on button press
		button.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				// Register the listener
				OfficeFloorComet.subscribe(MockCometListener.class, listener,
						null);
			}
		});
	}

}