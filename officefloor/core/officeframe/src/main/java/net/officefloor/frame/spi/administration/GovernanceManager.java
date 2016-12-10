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

import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.frame.spi.governance.Governance;

/**
 * Manager over a particular {@link Governance}.
 * 
 * @author Daniel Sagenschneider
 */
public interface GovernanceManager {

	/**
	 * <p>
	 * Triggers activating the {@link Governance}.
	 * <p>
	 * The activation may not have completed on the return of this method. The
	 * {@link Governance} will however be active before the next {@link Task} is
	 * executed.
	 */
	void activateGovernance();

	/**
	 * <p>
	 * Triggers enforcing the {@link Governance}.
	 * <p>
	 * The enforcement may not have completed on the return of this method. The
	 * {@link Governance} will however be enforced before the next {@link Task}
	 * is executed or end of {@link ProcessState}.
	 */
	void enforceGovernance();

	/**
	 * <p>
	 * Triggers disregarding the {@link Governance}.
	 * <p>
	 * The {@link Governance} may not have been disregarded on the return of
	 * this method. The {@link Governance} will however be disregarded before
	 * the next {@link Task} is executed or end of {@link ProcessState}.
	 */
	void disregardGovernance();

}