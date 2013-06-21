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

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Response from {@link CometSubscriptionService}.
 * 
 * @author Daniel Sagenschneider
 */
public class CometResponse implements IsSerializable {

	/**
	 * Listing of {@link CometEvent}.
	 */
	private CometEvent[] events;

	/**
	 * Initiate.
	 * 
	 * @param events
	 *            Listing of {@link CometEvent}.
	 */
	public CometResponse(CometEvent... events) {
		this.events = events;
	}

	/**
	 * Default constructor as required by {@link IsSerializable}.
	 */
	public CometResponse() {
	}

	/**
	 * Obtains the listing of {@link CometEvent}.
	 * 
	 * @return Listing of {@link CometEvent}.
	 */
	public CometEvent[] getEvents() {
		return this.events;
	}

}