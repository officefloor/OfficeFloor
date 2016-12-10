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

import net.officefloor.frame.spi.managedobject.ManagedObject;

/**
 * {@link Duty} to be undertaken to administer the {@link ManagedObject}
 * instances.
 * 
 * @author Daniel Sagenschneider
 */
public interface Duty<I, F extends Enum<F>, G extends Enum<G>> {

	/**
	 * Administers the {@link ManagedObject} instances.
	 * 
	 * @param context
	 *            {@link DutyContext}.
	 * @throws Throwable
	 *             If fails to do duty.
	 */
	void doDuty(DutyContext<I, F, G> context) throws Throwable;

}