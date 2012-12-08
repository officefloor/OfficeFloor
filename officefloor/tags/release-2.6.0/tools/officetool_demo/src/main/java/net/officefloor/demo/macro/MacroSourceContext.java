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

import java.awt.Frame;
import java.awt.Point;

import javax.swing.JDialog;

/**
 * Context for the {@link MacroSource}.
 * 
 * @author Daniel Sagenschneider
 */
public interface MacroSourceContext {

	/**
	 * Call back to specify the {@link Macro} being created.
	 * 
	 * @param macro
	 *            {@link Macro} created.
	 */
	void setNewMacro(Macro macro);
	
	/**
	 * Obtains the location for the {@link Macro}.
	 * 
	 * @return Location.
	 */
	Point getLocation();

	/**
	 * <p>
	 * Obtains another location for the {@link Macro}.
	 * <p>
	 * An example use of this is dragging:
	 * <ol>
	 * <li>{@link #getLocation()} provides item location</li>
	 * <li>use this method to obtain target location to drag item</li>
	 * </ol>
	 * 
	 * @return Another location for the {@link Macro}.
	 */
	Point getAnotherLocation();

	/**
	 * Obtains the absolute location for the relative location.
	 * 
	 * @param relativeLocation
	 *            Relative location.
	 * @return Absolute location.
	 */
	Point getAbsoluteLocation(Point relativeLocation);

	/**
	 * Obtains the relative location for the absolute location.
	 * 
	 * @param absoluteLocation
	 *            Absolute location.
	 * @return Relative location.
	 */
	Point getRelativeLocation(Point absoluteLocation);

	/**
	 * <p>
	 * Obtains the owner {@link Frame}.
	 * <p>
	 * This is useful should there for example be a {@link JDialog} necessary to
	 * obtain information for creating the {@link Macro}.
	 * 
	 * @return Owner {@link Frame}.
	 */
	Frame getOwnerFrame();

}