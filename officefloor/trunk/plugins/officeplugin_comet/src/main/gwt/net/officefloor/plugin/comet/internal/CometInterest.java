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

import net.officefloor.plugin.comet.api.CometListener;

/**
 * A particular interest in certain types of {@link CometEvent} instances.
 * 
 * @author Daniel Sagenschneider
 */
public class CometInterest implements IsSerializable {

	/**
	 * Listener type.
	 */
	private Class<? extends CometListener> listenerType;

	/**
	 * Filter key. May be <code>null</code> to receive all {@link CometEvent}
	 * instances for the listener type.
	 */
	private Object filterKey;

	/**
	 * Initiate.
	 * 
	 * @param listenerType
	 *            Listener type.
	 * @param filterKey
	 *            Filter key. May be <code>null</code> to receive all
	 *            {@link CometEvent} instances for the listener type.
	 */
	public CometInterest(Class<? extends CometListener> listenerType,
			Object filterKey) {
		this.listenerType = listenerType;
		this.filterKey = filterKey;
	}

	/**
	 * Default constructor required for {@link IsSerializable}.
	 */
	public CometInterest() {
	}

	/**
	 * Obtains the listener type.
	 * 
	 * @return Listener type.
	 */
	public Class<? extends CometListener> getListenerType() {
		return this.listenerType;
	}

	/**
	 * Obtains the filter key.
	 * 
	 * @return Filter key. May be <code>null</code>.
	 */
	public Object getFilterKey() {
		return this.filterKey;
	}

}