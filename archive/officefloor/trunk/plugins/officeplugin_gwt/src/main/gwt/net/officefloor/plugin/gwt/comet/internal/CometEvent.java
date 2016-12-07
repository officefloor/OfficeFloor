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

import java.io.Serializable;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Comet event.
 * 
 * @author Daniel Sagenschneider
 */
public class CometEvent implements IsSerializable {

	/**
	 * Value indicating no sequence number.
	 */
	public static final long NO_SEQUENCE_NUMBER = CometRequest.FIRST_REQUEST_SEQUENCE_NUMBER;

	/**
	 * Sequence number for this {@link CometEvent}.
	 */
	private long sequenceNumber;

	/**
	 * Listener type name.
	 */
	private String listenerTypeName;

	/**
	 * {@link Serializable} data.
	 */
	private Serializable data_Serializable = null;

	/**
	 * {@link IsSerializable} data.
	 */
	private IsSerializable data_IsSerializable = null;

	/**
	 * {@link Serializable} match key. May be <code>null</code>.
	 */
	private Serializable matchKey_Serializable = null;

	/**
	 * {@link IsSerializable} match key. May be <code>null</code>.
	 */
	private IsSerializable matchKey_IsSerializable = null;

	/**
	 * Initiate for publishing without a sequence number. Sequence number to be
	 * assigned.
	 * 
	 * @param listenerTypeName
	 *            Listener type name.
	 * @param data
	 *            Data.
	 * @param matchKey
	 *            Match key. May be <code>null</code>.
	 */
	public CometEvent(String listenerTypeName, Object data, Object matchKey) {
		this(NO_SEQUENCE_NUMBER, listenerTypeName, data, matchKey);
	}

	/**
	 * Initiate for specifying the sequence number.
	 * 
	 * @param sequenceNumber
	 *            Sequence number for this {@link CometEvent}.
	 * @param listenerTypeName
	 *            Listener type name.
	 * @param data
	 *            Data.
	 * @param matchKey
	 *            Match key. May be <code>null</code>.
	 */
	public CometEvent(long sequenceNumber, String listenerTypeName,
			Object data, Object matchKey) {
		this.sequenceNumber = sequenceNumber;
		this.listenerTypeName = listenerTypeName;

		// Specify the data
		if (data != null) {
			if (data instanceof IsSerializable) {
				this.data_IsSerializable = (IsSerializable) data;
			} else if (data instanceof Serializable) {
				this.data_Serializable = (Serializable) data;
			} else {
				throw new IllegalArgumentException("Type for data ("
						+ data.getClass().getName() + ") is not serialisable");
			}
		}

		// Specify the match key
		if (matchKey != null) {
			if (matchKey instanceof IsSerializable) {
				this.matchKey_IsSerializable = (IsSerializable) matchKey;
			} else if (matchKey instanceof Serializable) {
				this.matchKey_Serializable = (Serializable) matchKey;
			} else {
				throw new IllegalArgumentException("Type for matchKey ("
						+ matchKey.getClass().getName()
						+ ") is not serialisable");
			}
		}
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
		return (this.data_IsSerializable != null ? this.data_IsSerializable
				: this.data_Serializable);
	}

	/**
	 * Obtains the match key.
	 * 
	 * @return Match key. May be <code>null</code>.
	 */
	public Object getMatchKey() {
		return (this.matchKey_IsSerializable != null ? this.matchKey_IsSerializable
				: this.matchKey_Serializable);
	}

}