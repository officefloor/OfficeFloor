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

package net.officefloor.demo.record;

import net.officefloor.demo.macro.Macro;

/**
 * Listens to actions of the {@link RecordComponent}.
 *
 * @author Daniel Sagenschneider
 */
public interface RecordListener {

	/**
	 * Adds a {@link Macro}.
	 *
	 * @param macro
	 *            {@link Macro}.
	 */
	void addMacro(Macro macro);

}