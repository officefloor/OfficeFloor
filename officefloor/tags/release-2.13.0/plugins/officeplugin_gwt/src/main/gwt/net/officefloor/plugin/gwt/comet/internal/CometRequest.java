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
package net.officefloor.plugin.gwt.comet.internal;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Request for the {@link CometSubscriptionService}.
 * 
 * @author Daniel Sagenschneider
 */
public class CometRequest implements IsSerializable {

	/**
	 * {@link CometEvent} sequence number to use should no {@link CometEvent}
	 * instances been previously provided to the client.
	 */
	public static final long FIRST_REQUEST_SEQUENCE_NUMBER = -1;

	/**
	 * Last {@link CometEvent} sequence number received by the client.
	 */
	private long lastSequenceNumber = -1;

	/**
	 * Listing of {@link CometInterest}.
	 */
	private CometInterest[] interests;

	/**
	 * Initiate.
	 * 
	 * @param lastSequenceNumber
	 *            Last {@link CometEvent} sequence number received by the
	 *            client.
	 * @param interests
	 *            Listing of {@link CometInterest}.
	 */
	public CometRequest(long lastSequenceNumber, CometInterest... interests) {
		this.lastSequenceNumber = lastSequenceNumber;
		this.interests = interests;
	}

	/**
	 * Default constructor required as per {@link IsSerializable}.
	 */
	public CometRequest() {
	}

	/**
	 * Obtains the last {@link CometEvent} sequence number received by the
	 * client.
	 * 
	 * @return Last {@link CometEvent} sequence number received by the client.
	 */
	public long getLastSequenceNumber() {
		return this.lastSequenceNumber;
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