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
package net.officefloor.compile.spi.office;

/**
 * <p>
 * Special class returned by {@link ObjectDependency} to indicate the type is
 * unknown.
 * <p>
 * The class is <code>final</code> and with a <code>private</code> constructor
 * to ensure it stays only as a type indicator (not to actually be used and
 * therefore never a real dependency).
 * 
 * @author Daniel Sagenschneider
 */
public final class UnknownType {

	/**
	 * Ensure can not initiate this class as is only an type indicator.
	 */
	private UnknownType() {
	}

}