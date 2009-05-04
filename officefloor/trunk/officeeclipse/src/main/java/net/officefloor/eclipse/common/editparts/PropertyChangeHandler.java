/*
 *  Office Floor, Application Server
 *  Copyright (C) 2006 Daniel Sagenschneider
 *
 *  This program is free software; you can redistribute it and/or modify it under the terms 
 *  of the GNU General Public License as published by the Free Software Foundation; either 
 *  version 2 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program; 
 *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, 
 *  MA 02111-1307 USA
 */
package net.officefloor.eclipse.common.editparts;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * Property change handler.
 * 
 * @author Daniel
 */
public abstract class PropertyChangeHandler<E extends Enum<E>> implements
		PropertyChangeListener {

	/**
	 * List of events to be handled.
	 */
	private final E[] events;

	/**
	 * Initiate with the list of events to handle.
	 * 
	 * @param events
	 *            List of events.
	 */
	// TODO change to addPropertyChangeHandler method by EditPart
	public PropertyChangeHandler(E[] events) {
		this.events = events;
	}

	/*
	 * ================ PropertyChangeListener ==========================
	 */

	@Override
	public void propertyChange(PropertyChangeEvent evt) {

		// Obtain the event name
		String eventName = evt.getPropertyName();

		// Obtain the event property
		E event = null;
		for (E currentEvent : events) {
			if (currentEvent.name().equals(eventName)) {
				event = currentEvent;
			}
		}

		// Handle the property
		if (event != null) {
			this.handlePropertyChange(event, evt);
		}
	}

	/**
	 * Handles the property changes.
	 * 
	 * @param property
	 *            Event property.
	 * @param evt
	 *            Event.
	 */
	protected abstract void handlePropertyChange(E property,
			PropertyChangeEvent evt);

}