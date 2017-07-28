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
package net.officefloor.plugin.clazz;

/**
 * Sequence to obtain the next index.
 * 
 * @author Daniel Sagenschneider
 */
public class Sequence {

	/**
	 * Value for next index in the sequence.
	 */
	private int nextIndex = 0;

	/**
	 * Obtains the next index in the sequence.
	 * 
	 * @return Next index in the sequence.
	 */
	public int nextIndex() {
		return this.nextIndex++;
	}

}