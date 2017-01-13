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
package net.officefloor.frame.api.administration;

import net.officefloor.frame.api.governance.Governance;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.internal.structure.Flow;

/**
 * Factory for the creation of an {@link Administration}.
 * 
 * @param <E>
 *            Extension interface used to administer the {@link ManagedObject}
 *            instances.
 * @param <F>
 *            {@link Flow} keys for invoked {@link Flow} instances from this
 *            {@link Administration}.
 * @param <G>
 *            {@link Governance} keys identifying the {@link Governance} that
 *            may be under {@link Administration}.
 * 
 * @author Daniel Sagenschneider
 */
public interface AdministrationFactory<E, F extends Enum<F>, G extends Enum<G>> {

	/**
	 * Creates the {@link Administration}.
	 * 
	 * @return {@link Administration}.
	 */
	Administration<E, F, G> createAdministration();

}