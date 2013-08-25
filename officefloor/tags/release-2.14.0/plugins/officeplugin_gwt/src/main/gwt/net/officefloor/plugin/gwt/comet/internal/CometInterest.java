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
 * A particular interest in certain types of {@link CometEvent} instances.
 * 
 * @author Daniel Sagenschneider
 */
public class CometInterest implements IsSerializable {

	/**
	 * Listener type name.
	 */
	private String listenerTypeName;

	/**
	 * {@link Serializable} filter key. May be <code>null</code> to receive all
	 * {@link CometEvent} instances for the listener type.
	 */
	private Serializable filterKey_Serializable;

	/**
	 * {@link IsSerializable} filter key. May be <code>null</code> to receive
	 * all {@link CometEvent} instances for the listener type.
	 */
	private IsSerializable filterKey_IsSerializable;

	/**
	 * Initiate.
	 * 
	 * @param listenerTypeName
	 *            Listener type name.
	 * @param filterKey
	 *            Filter key. May be <code>null</code> to receive all
	 *            {@link CometEvent} instances for the listener type.
	 */
	public CometInterest(String listenerTypeName, Object filterKey) {
		this.listenerTypeName = listenerTypeName;

		// Specify the filter key
		if (filterKey != null) {
			if (filterKey instanceof IsSerializable) {
				this.filterKey_IsSerializable = (IsSerializable) filterKey;
			} else if (filterKey instanceof Serializable) {
				this.filterKey_Serializable = (Serializable) filterKey;
			} else {
				throw new IllegalArgumentException("Type for filterKey ("
						+ filterKey.getClass().getName()
						+ ") is not serialisable");
			}
		}
	}

	/**
	 * Default constructor required for {@link IsSerializable}.
	 */
	public CometInterest() {
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
	 * Obtains the filter key.
	 * 
	 * @return Filter key. May be <code>null</code>.
	 */
	public Object getFilterKey() {
		return (this.filterKey_IsSerializable != null ? this.filterKey_IsSerializable
				: this.filterKey_Serializable);
	}

}