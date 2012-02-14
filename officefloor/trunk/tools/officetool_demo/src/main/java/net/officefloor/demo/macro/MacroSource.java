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

/**
 * Source for the creation of a {@link Macro}.
 * 
 * @author Daniel Sagenschneider
 */
public interface MacroSource {

	/**
	 * Obtains the display name for the {@link Macro}.
	 * 
	 * @return Display name for the {@link Macro}.
	 */
	String getDisplayName();

	/**
	 * Sources a new {@link Macro}.
	 * 
	 * @param context
	 *            {@link MacroSourceContext}.
	 * @return New {@link Macro}.
	 */
	void sourceMacro(MacroSourceContext context);

}