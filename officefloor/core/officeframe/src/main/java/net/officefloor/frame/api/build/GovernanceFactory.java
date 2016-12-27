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
package net.officefloor.frame.api.build;

import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.spi.governance.Governance;
import net.officefloor.frame.spi.managedobject.ManagedObject;

/**
 * Factory for the creation of the {@link Governance}.
 * 
 * @param <E>
 *            Extension interface type for the {@link ManagedObject} instances
 *            to be under this {@link Governance}.
 * @param <F>
 *            {@link Flow} keys for the {@link Governance}.
 * 
 * @author Daniel Sagenschneider
 */
public interface GovernanceFactory<E, F extends Enum<F>> {

	/**
	 * Creates the {@link Governance}.
	 * 
	 * @return {@link Governance}.
	 * @throws Throwable
	 *             If fails to create the {@link Governance}.
	 */
	Governance<E, F> createGovernance() throws Throwable;

}