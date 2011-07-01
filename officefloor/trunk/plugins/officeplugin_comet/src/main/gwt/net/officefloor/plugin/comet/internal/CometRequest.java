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

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Request for the {@link CometListenerService}.
 * 
 * @author Daniel Sagenschneider
 */
public class CometRequest implements IsSerializable {

	/**
	 * {@link CometEvent} Id to use should no {@link CometEvent} instances been
	 * previously provided to the client.
	 */
	public static final long FIRST_REQUEST_EVENT_ID = -1;

	/**
	 * Last {@link CometEvent} Id received by the client.
	 */
	private long lastEventId = -1;

	/**
	 * Listing of {@link CometInterest}.
	 */
	private CometInterest[] interests;

	/**
	 * Initiate.
	 * 
	 * @param lastEventId
	 *            Last {@link CometEvent} Id received by the client.
	 * @param interests
	 *            Listing of {@link CometInterest}.
	 */
	public CometRequest(long lastEventId, CometInterest... interests) {
		this.lastEventId = lastEventId;
		this.interests = interests;
	}

	/**
	 * Default constructor required as per {@link IsSerializable}.
	 */
	public CometRequest() {
	}

	/**
	 * Obtains the last {@link CometEvent} Id received by the client.
	 * 
	 * @return Last {@link CometEvent} Id received by the client.
	 */
	public long getLastEventId() {
		return this.lastEventId;
	}

	/**
	 * Obtains the listing of {@link CometInterest}.
	 * 
	 * @return Listing of {@link CometInterest}.
	 */
	public CometInterest[] getInterests() {
		return this.interests;
	}

}