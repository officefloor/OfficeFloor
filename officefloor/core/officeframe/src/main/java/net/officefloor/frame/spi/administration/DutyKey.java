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
package net.officefloor.frame.spi.administration;

import net.officefloor.frame.api.build.Indexed;

/**
 * Key identifying a {@link Duty} for the {@link Administrator}.
 * 
 * @author Daniel Sagenschneider
 */
public interface DutyKey<A> {

	/**
	 * <p>
	 * Obtains the {@link Enum} identifying the {@link Duty}.
	 * <p>
	 * This will be <code>null</code> if {@link Indexed} identification.
	 * 
	 * @return {@link Enum} identifying the {@link Duty} or <code>null</code> if
	 *         {@link Indexed}.
	 */
	A getKey();

	/**
	 * Obtains the index identifying the {@link Duty}.
	 * 
	 * @return Index identifying the {@link Duty}.
	 */
	int getIndex();

}