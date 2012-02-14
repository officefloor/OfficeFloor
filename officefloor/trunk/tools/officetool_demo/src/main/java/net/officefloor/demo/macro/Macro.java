/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2012 Daniel Sagenschneider
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

package net.officefloor.demo.macro;

import java.awt.Point;

/**
 * Provides wrapping of a possible action to record.
 * 
 * @author Daniel Sagenschneider
 */
public interface Macro {

	/**
	 * Initialises this {@link Macro} from the configuration memento.
	 * 
	 * @param memento
	 *            Memento containing the configuration.
	 */
	void setConfigurationMemento(String memento);

	/**
	 * Obtains the configuration memento to reinitialise a new instance of this
	 * {@link Macro}.
	 * 
	 * @return Configuration memento.
	 */
	String getConfigurationMemento();

	/**
	 * Obtains the label for displaying this {@link Macro}.
	 * 
	 * @return Label for displaying this {@link Macro}. May return
	 *         <code>null</code> for label based on type.
	 */
	String getDisplayLabel();

	/**
	 * <p>
	 * Obtains the {@link Point} where the mouse cursor must be to start this
	 * {@link Macro}.
	 * <p>
	 * This allows for visually moving the mouse to this location before running
	 * this {@link Macro}.
	 * 
	 * @return {@link Point} where the mouse cursor must be to start this
	 *         {@link Macro}. <code>null</code> indicates non-mouse
	 *         {@link Macro} (typically keyboard entry).
	 */
	Point getStartingMouseLocation();

	/**
	 * Obtains the listing of {@link MacroTask} instances.
	 * 
	 * @return Listing of {@link MacroTask} instances.
	 */
	MacroTask[] getMacroTasks();

}