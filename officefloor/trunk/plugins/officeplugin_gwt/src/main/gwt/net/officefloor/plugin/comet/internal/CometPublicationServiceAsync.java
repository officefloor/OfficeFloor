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
package net.officefloor.plugin.comet.internal;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Async RPC Service for publishing a {@link CometEvent}.
 * 
 * @author Daniel Sagenschneider
 */
public interface CometPublicationServiceAsync {

	/**
	 * Publishes a {@link CometEvent}.
	 * 
	 * @param event
	 *            {@link CometEvent}.
	 * @param callback
	 *            {@link AsyncCallback} to be notified on success with the
	 *            {@link CometEvent} sequence number.
	 */
	void publish(CometEvent event, AsyncCallback<Long> callback);

}