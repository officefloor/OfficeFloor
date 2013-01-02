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
package net.officefloor.plugin.comet.client;

import net.officefloor.plugin.comet.api.OfficeFloorComet;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
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

	/**
	 * Notification toggle.
	 */
	private boolean notificationToggle = false;

	/**
	 * Indicates the number of events received.
	 */
	private int eventCount = 0;

	/**
	 * Indicates the number of match keys received.
	 */
	private int filterKeyCount = 0;

	/**
	 * Indicates the number of match keys received on <code>null</code> filter
	 * key.
	 */
	private int nullFilterKeyCount = 0;

	/**
	 * {@link MockFilterKey}.
	 */
	private MockFilterKey filterKey = new MockFilterKey(null);

	/*
	 * ====================== EntryPoint ======================
	 */

	@Override
	public void onModuleLoad() {

		// Create the publishers
		final MockCometNotificationListener notificationPublisher = OfficeFloorComet
				.createPublisher(MockCometNotificationListener.class);
		final MockCometEventListener eventPublisher = OfficeFloorComet
				.createPublisher(MockCometEventListener.class);
		final MockCometMatchKeyListener matchKeyPublisher = OfficeFloorComet
				.createPublisher(MockCometMatchKeyListener.class);
		final MockCometNullFilterKeyListener nullFilterKeyPublisher = OfficeFloorComet
				.createPublisher(MockCometNullFilterKeyListener.class);

		// Provide the widgets
		Panel root = RootPanel.get("comet");
		root.clear();
		VerticalPanel panel = new VerticalPanel();
		root.add(panel);

		// Provide event text widget
		HorizontalPanel eventPanel = new HorizontalPanel();
		panel.add(eventPanel);
		eventPanel.add(new Label("Event text: "));
		final TextBox eventText = new TextBox();
		eventText.setText("TEST");
		eventPanel.add(eventText);

		// Provide match key widget
		HorizontalPanel matchKeyPanel = new HorizontalPanel();
		panel.add(matchKeyPanel);
		matchKeyPanel.add(new Label("Match key: "));
		final TextBox matchKeyText = new TextBox();
		matchKeyText.setText("MATCH");
		matchKeyPanel.add(matchKeyText);
		matchKeyPanel.add(new Label("Additional: "));
		final TextBox additionalText = new TextBox();
		additionalText.setText("additional");
		matchKeyPanel.add(additionalText);

		// Multiple subscriptions
		OfficeFloorComet.setMultipleSubscriptions(true);

		// Provide notification subscription
		HorizontalPanel notificationSubscribePanel = new HorizontalPanel();
		panel.add(notificationSubscribePanel);
		notificationSubscribePanel.add(new Label("Subscribe notification: "));
		final CheckBox notificationCheckBox = new CheckBox();
		notificationSubscribePanel.add(notificationCheckBox);
		OfficeFloorComet.subscribe(MockCometNotificationListener.class,
				new MockCometNotificationListener() {
					@Override
					public void handleNotification() {
						OfficeFloorCometEntryPoint.this.notificationToggle = !OfficeFloorCometEntryPoint.this.notificationToggle;
						notificationCheckBox.setValue(Boolean
								.valueOf(OfficeFloorCometEntryPoint.this.notificationToggle));
					}
				}, null);

		// Provide event subscription
		HorizontalPanel eventSubscribePanel = new HorizontalPanel();
		panel.add(eventSubscribePanel);
		eventSubscribePanel.add(new Label("Subscribed event: "));
		final Label eventSubscribeLabel = new Label("[no event]");
		eventSubscribePanel.add(eventSubscribeLabel);
		OfficeFloorComet.subscribe(MockCometEventListener.class,
				new MockCometEventListener() {
					@Override
					public void handleEvent(String event) {
						eventSubscribeLabel
								.setText(event
										+ " ("
										+ (++OfficeFloorCometEntryPoint.this.eventCount)
										+ ")");
					}
				}, null);

		// Provide filter key subscription
		HorizontalPanel filterKeySubscribePanel = new HorizontalPanel();
		panel.add(filterKeySubscribePanel);
		filterKeySubscribePanel.add(new Label("Filter Key: "));
		final TextBox filterKeyText = new TextBox();
		filterKeyText.setText(matchKeyText.getText());
		filterKeySubscribePanel.add(filterKeyText);
		filterKeySubscribePanel.add(new Label("Subscribe filter key: "));
		final Label matchKeySubscribeLabel = new Label("[no event]");
		filterKeySubscribePanel.add(matchKeySubscribeLabel);
		OfficeFloorComet.subscribe(MockCometMatchKeyListener.class,
				new MockCometMatchKeyListener() {
					@Override
					public void handleEvent(String event, MockFilterKey matchKey) {
						matchKeySubscribeLabel
								.setText(event
										+ "("
										+ (++OfficeFloorCometEntryPoint.this.filterKeyCount)
										+ ") - "
										+ (matchKey == null ? "NONE" : matchKey
												.getAdditionalText()));
					}
				}, this.filterKey);

		// Provide null filter key subscription
		HorizontalPanel nullFilterKeySubscribePanel = new HorizontalPanel();
		panel.add(nullFilterKeySubscribePanel);
		nullFilterKeySubscribePanel
				.add(new Label("Subscribe null filter key: "));
		final Label nullMatchKeySubscribeLabel = new Label("[no event]");
		nullFilterKeySubscribePanel.add(nullMatchKeySubscribeLabel);
		OfficeFloorComet.subscribe(MockCometNullFilterKeyListener.class,
				new MockCometNullFilterKeyListener() {
					@Override
					public void handleEvent(String event, MockFilterKey matchKey) {
						nullMatchKeySubscribeLabel
								.setText(event
										+ "("
										+ (++OfficeFloorCometEntryPoint.this.nullFilterKeyCount)
										+ ") - "
										+ (matchKey == null ? "NONE" : matchKey
												.getAdditionalText()));
					}
				}, null);

		// Subscribe
		OfficeFloorComet.subscribe();

		// Provide button to trigger publishing
		Button publishButton = new Button("Publish");
		panel.add(publishButton);
		publishButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent clickEvent) {

				// Specify the filter key (may take two events to update)
				String filterKey = filterKeyText.getText();
				OfficeFloorCometEntryPoint.this.filterKey
						.setFilterText(filterKey);

				// Create the match key
				MockFilterKey matchKey;
				String matchKeyValue = matchKeyText.getText();
				if ((matchKeyValue == null)
						|| (matchKeyValue.trim().length() == 0)) {
					// No match key
					matchKey = null;
				} else {
					// Provide the match key
					matchKey = new MockFilterKey(additionalText.getText());
					matchKey.setFilterText(matchKeyValue);
				}

				// Publish events
				String event = eventText.getText();
				notificationPublisher.handleNotification();
				eventPublisher.handleEvent(event);
				matchKeyPublisher.handleEvent(event, matchKey);
				nullFilterKeyPublisher.handleEvent(event, matchKey);
			}
		});
	}

}