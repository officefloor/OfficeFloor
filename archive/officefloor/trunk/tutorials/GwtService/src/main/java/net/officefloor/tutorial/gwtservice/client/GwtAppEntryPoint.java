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
package net.officefloor.tutorial.gwtservice.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * GWT App {@link EntryPoint}.
 * 
 * @author Daniel Sagenschneider
 */
// START SNIPPET: example
public class GwtAppEntryPoint implements EntryPoint {

	private final HighLowGameAsync service = GWT.create(HighLowGame.class);

	@Override
	public void onModuleLoad() {

		// Obtain panel to load game
		Panel panel = RootPanel.get("HighLowGame");

		// Add Game widgets
		VerticalPanel gamePanel = new VerticalPanel();
		panel.add(gamePanel);
		gamePanel.add(new Label("Enter a number (between 1 and 100):"));
		HorizontalPanel tryPanel = new HorizontalPanel();
		gamePanel.add(tryPanel);
		final TextBox text = new TextBox();
		tryPanel.add(text);
		final Button button = new Button("Try");
		tryPanel.add(button);
		final Label message = new Label();
		gamePanel.add(message);

		// Handle try button click
		button.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {

				// Obtain the value to try
				Integer value;
				try {
					value = Integer.valueOf(text.getText());
				} catch (NumberFormatException ex) {
					Window.alert("Please enter a valid number");
					return;
				}

				// Attempt try (calls on Server)
				service.attempt(value, new AsyncCallback<Result>() {
					@Override
					public void onSuccess(Result result) {
						switch (result.getAnswer()) {
						case HIGHER:
							message.setText("Higher");
							text.setText("");
							break;
						case LOWER:
							message.setText("Lower");
							text.setText("");
							break;
						case CORRECT:
							message.setText("Correct. Well done!");
							text.setEnabled(false);
							button.setEnabled(false);
							break;
						case NO_FURTHER_ATTEMPTS:
							message.setText("No further attempts. Number was "
									+ result.getNumber());
							text.setEnabled(false);
							button.setEnabled(false);
							break;
						}
					}

					@Override
					public void onFailure(Throwable caught) {
						Window.alert("Technical failure: "
								+ caught.getMessage() + " ["
								+ caught.getClass().getName() + "]");
					}
				});
			}
		});
	}

}
// END SNIPPET: example