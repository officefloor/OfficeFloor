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

package net.officefloor.frame.spi.administration;

import net.officefloor.frame.spi.managedobject.ManagedObject;

/**
 * Provides administration of the {@link ManagedObject} instances.
 * 
 * @author Daniel Sagenschneider
 */
public interface Administrator<I, A extends Enum<A>> {

	/**
	 * {@link Duty} to be undertaken on the {@link ManagedObject} extension
	 * interfaces.
	 * 
	 * @param dutyKey
	 *            {@link DutyKey} identifying the {@link Duty}.
	 * @return {@link Duty}.
	 */
	Duty<I, ?, ?> getDuty(DutyKey<A> dutyKey);

}