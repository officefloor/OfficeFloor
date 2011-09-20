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

import net.officefloor.frame.spi.governance.Governance;
import net.officefloor.frame.spi.managedobject.ManagedObject;

/**
 * Manager over a particular {@link Governance}.
 * 
 * @author Daniel Sagenschneider
 */
public interface GovernanceManager {

	/**
	 * <p>
	 * Triggers the activation of the {@link Governance}.
	 * <p>
	 * The {@link Governance} may not be active on the return of this method as
	 * it may be waiting for a {@link ManagedObject} to be ready to be governed.
	 */
	void activateGovernance();

	/**
	 * <p>
	 * Triggers enforcing the {@link Governance}.
	 * <p>
	 * The enforcement my not have completed on the return of this method. Any
	 * issues with enforcement will however be reported the next time a governed
	 * {@link ManagedObject} is attempted to be used.
	 */
	void enforceGovernance();

	/**
	 * <p>
	 * Triggers disregarding the {@link Governance}.
	 * <p>
	 * The {@link Governance} may not have been disregarded on the return of
	 * this method. Any issues will however be reported the next time a governed
	 * {@link ManagedObject} is attempted to be used.
	 */
	void disregardGovernance();

}