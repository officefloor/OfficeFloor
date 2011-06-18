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

package net.officefloor.demo.macrolist;

import net.officefloor.demo.macro.Macro;

/**
 * Listener to actions on the {@link MacroList}.
 * 
 * @author Daniel Sagenschneider
 */
public interface MacroListListener {

	/**
	 * Indicates a {@link Macro} was added to the {@link MacroList}.
	 * 
	 * @param item
	 *            {@link MacroItem} containing the added {@link Macro}.
	 * @param index
	 *            Index of the added {@link MacroItem}.
	 */
	void macroAdded(MacroItem item, int index);

	/**
	 * Indicates a {@link Macro} was removed from the {@link MacroList}.
	 * 
	 * @param item
	 *            {@link MacroItem} containing the removed {@link Macro}.
	 * @param index
	 *            Index that the {@link MacroItem} previous had before being
	 *            removed.
	 */
	void macroRemoved(MacroItem item, int index);

}