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
package net.officefloor.model;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.List;

/**
 * Provides top level functionality for all model elements.
 * 
 * @author Daniel Sagenschneider
 */
public class AbstractModel implements Model {

	/**
	 * Support in handling property changes.
	 */
	private final transient PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

	/**
	 * The X location for the model.
	 */
	private int x = -1;

	/**
	 * The Y location for the model.
	 */
	private int y = -1;

	/**
	 * Fires a property change event.
	 * 
	 * @param property
	 *            Property related to the event.
	 * @param oldValue
	 *            Old value of property.
	 * @param newValue
	 *            New value of property.
	 */
	public void firePropertyChange(String property, Object oldValue, Object newValue) {
		if (this.propertyChangeSupport.hasListeners(property)) {
			this.propertyChangeSupport.firePropertyChange(property, oldValue, newValue);
		}
	}

	/**
	 * Helper method to add an Item to a List.
	 * 
	 * @param <T>
	 *            Item type.
	 * @param item
	 *            Item to add to list.
	 * @param list
	 *            List to have item to add.
	 * @param addEvent
	 *            Event to fire on item being added.
	 */
	protected <T> void addItemToList(T item, List<T> list, Enum<?> addEvent) {
		if (!list.contains(item)) {
			list.add(item);

			// Notify item added
			this.firePropertyChange(addEvent.name(), null, item);
		}
	}

	/**
	 * Helper method to remove an Item from a List.
	 * 
	 * @param <T>
	 *            Item type.
	 * @param item
	 *            Item to remove from list.
	 * @param list
	 *            List to have item removed.
	 * @param removeEvent
	 *            Event to fire on item being removed.
	 */
	protected <T> void removeItemFromList(T item, List<T> list, Enum<?> removeEvent) {
		if (list.contains(item)) {
			list.remove(item);

			// Notify item removed
			this.firePropertyChange(removeEvent.name(), item, null);
		}
	}

	/**
	 * Helper method to change a field.
	 * 
	 * @param <T>
	 *            Value type.
	 * @param oldValue
	 *            Old value of field.
	 * @param newValue
	 *            New value of field.
	 * @param changeEvent
	 *            Event to fire if field is being changed.
	 */
	protected <T> void changeField(T oldValue, T newValue, Enum<?> changeEvent) {
		// Determine if both null
		if ((oldValue == null) && (newValue == null)) {
			// No change and values both null
			return;

		} else if ((oldValue != null) && (newValue != null)) {
			// Both not null thus check for equality
			if (oldValue.equals(newValue)) {
				// No change as values equal
				return;
			} else {
				// Values different
				this.firePropertyChange(changeEvent.name(), oldValue, newValue);
				return;
			}

		} else {
			// Either being set or nulled
			this.firePropertyChange(changeEvent.name(), oldValue, newValue);
			return;
		}
	}

	/*
	 * =================== Model ==========================================
	 */

	@Override
	public int getX() {
		return this.x;
	}

	@Override
	public void setX(int x) {
		this.x = x;
	}

	@Override
	public int getY() {
		return this.y;
	}

	@Override
	public void setY(int y) {
		this.y = y;
	}

	@Override
	public void addPropertyChangeListener(PropertyChangeListener listener) {
		this.propertyChangeSupport.addPropertyChangeListener(listener);
	}

	@Override
	public void removePropertyChangeListener(PropertyChangeListener listener) {
		this.propertyChangeSupport.removePropertyChangeListener(listener);
	}

}