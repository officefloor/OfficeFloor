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
package net.officefloor.plugin.comet.api;

import java.util.Map;

import net.officefloor.plugin.comet.internal.CometListenerAdapter;
import net.officefloor.plugin.comet.internal.CometListenerMap;
import net.officefloor.plugin.comet.internal.CometListenerService;
import net.officefloor.plugin.comet.internal.CometListenerServiceAsync;
import net.officefloor.plugin.comet.internal.CometRequest;
import net.officefloor.plugin.comet.internal.CometResponse;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Provides means to register an event listener.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeFloorComet {

	/**
	 * Obtain the mapping of {@link CometListener} interface type to its
	 * {@link CometListenerAdapter}.
	 */
	private static final Map<Class<?>, CometListenerAdapter> adapters = ((CometListenerMap) GWT
			.create(CometListenerMap.class)).getMap();

	/**
	 * Registers a listener for asynchronous events.
	 * 
	 * @param listenerType
	 *            Listener interface type that should be marked by extending
	 *            {@link CometListener}.
	 * @param handler
	 *            Handles the event.
	 * @param filterKey
	 *            Key to filter events. The {@link Object#equals(Object)} is
	 *            used to match event meta-data to determine filtering. This may
	 *            be <code>null</code> to receive all events.
	 */
	public static <L extends CometListener> void registerListener(
			Class<L> listenerType, final L handler, Object filterKey) {

		// Obtain the adapter
		final CometListenerAdapter adapter = adapters.get(listenerType);

		// Start listening
		CometListenerServiceAsync service = GWT
				.create(CometListenerService.class);
		service.listen(new CometRequest(), new AsyncCallback<CometResponse>() {

			@Override
			public void onSuccess(CometResponse result) {

				// TODO obtain the event
				final Object event = "EVENT";

				// Handle the event
				adapter.handleEvent(handler, event);
			}

			@Override
			public void onFailure(Throwable caught) {
				Window.alert("COMET FAILURE: " + caught.getMessage() + " ["
						+ caught.getClass().getName() + "]");
			}
		});
	}

	/**
	 * All access via static methods.
	 */
	private OfficeFloorComet() {
	}

}