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
package net.officefloor.plugin.comet.internal;

import net.officefloor.plugin.comet.api.CometSubscriber;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Async RPC Service for the {@link CometSubscriber}.
 * 
 * @author Daniel Sagenschneider
 */
public interface CometSubscriptionServiceAsync {

	/**
	 * Listens by sending a {@link CometRequest} and when an event is available,
	 * responds with {@link CometResponse} containing details of the event(s).
	 * 
	 * @param request
	 *            {@link CometRequest}.
	 * @param callback
	 *            Invoked with the {@link CometResponse} when event(s) are
	 *            available.
	 */
	void listen(CometRequest request, AsyncCallback<CometResponse> callback);

}