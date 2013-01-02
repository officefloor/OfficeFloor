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
package net.officefloor.tutorial.gwtapp.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * GWT App {@link EntryPoint}.
 * 
 * @author Daniel Sagenschneider
 */
// START SNIPPET: example
public class GwtAppEntryPoint implements EntryPoint {

	@Override
	public void onModuleLoad() {

		// Provide the time label
		RootPanel panel = RootPanel.get("timer");
		final Label label = new Label("0");
		panel.add(label);

		// Increment time each second
		Timer timer = new Timer() {
			@Override
			public void run() {
				long time = Long.parseLong(label.getText());
				time++;
				label.setText(String.valueOf(time));
			}
		};
		timer.scheduleRepeating(1000);
	}

}
// END SNIPPET: example