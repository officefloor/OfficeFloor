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

/**
 * Contract for top level functionality for all model elements.
 * 
 * @author Daniel Sagenschneider
 */
public interface Model {

	/**
	 * Obtains the X co-ordinate for the model.
	 * 
	 * @return X co-ordinate for the model.
	 */
	int getX();

	/**
	 * Specifies the X co-ordinate for the model.
	 * 
	 * @param x
	 *            X co-ordinate for the model.
	 */
	void setX(int x);

	/**
	 * Obtains the Y co-ordinate for the model.
	 * 
	 * @return Y co-ordinate for the model.
	 */
	int getY();

	/**
	 * Specifies the Y co-ordinate for the model.
	 * 
	 * @param y
	 *            Y co-ordinate for the model.
	 */
	void setY(int y);

	/**
	 * Adds a {@link PropertyChangeListener} to this model element.
	 * 
	 * @param listener
	 *            {@link PropertyChangeListener} to this model element.
	 */
	void addPropertyChangeListener(PropertyChangeListener listener);

	/**
	 * Removes a {@link PropertyChangeListener} from this model element.
	 * 
	 * @param listener
	 *            {@link PropertyChangeListener}.
	 */
	void removePropertyChangeListener(PropertyChangeListener listener);

}
