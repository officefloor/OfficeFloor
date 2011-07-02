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
 * Comet event.
 * 
 * @author Daniel Sagenschneider
 */
public class CometEvent implements IsSerializable {

	/**
	 * Sequence number for this {@link CometEvent}.
	 */
	private long sequenceNumber;

	/**
	 * Listener type name.
	 */
	private String listenerTypeName;

	/**
	 * Data.
	 */
	private Object data;

	/**
	 * Filter key used on the {@link CometInterest}. May be <code>null</code>.
	 */
	private Object filterKey;

	/**
	 * Initiate.
	 * 
	 * @param sequenceNumber
	 *            Sequence number for this {@link CometEvent}.
	 * @param listenerTypeName
	 *            Listener type name.
	 * @param data
	 *            Data.
	 * @param filterKey
	 *            Filter key used on the {@link CometInterest}. May be
	 *            <code>null</code>.
	 */
	public CometEvent(long sequenceNumber, String listenerTypeName,
			Object data, Object filterKey) {
		this.sequenceNumber = sequenceNumber;
		this.listenerTypeName = listenerTypeName;
		this.data = data;
		this.filterKey = filterKey;
	}

	/**
	 * Default constructor required for {@link IsSerializable}.
	 */
	public CometEvent() {
	}

	/**
	 * Obtains the sequence number for this {@link CometEvent}.
	 * 
	 * @return Sequence number for this {@link CometEvent}.
	 */
	public long getSequenceNumber() {
		return this.sequenceNumber;
	}

	/**
	 * Obtains the listener type name.
	 * 
	 * @return Listener type name.
	 */
	public String getListenerTypeName() {
		return this.listenerTypeName;
	}

	/**
	 * Obtains the data of this {@link CometEvent}.
	 * 
	 * @return Data.
	 */
	public Object getData() {
		return this.data;
	}

	/**
	 * Filter key used on the {@link CometInterest}. May be <code>null</code>.
	 * 
	 * @return Filter key used on the {@link CometInterest}. May be
	 *         <code>null</code>.
	 */
	public Object getFilterKey() {
		return this.filterKey;
	}

}