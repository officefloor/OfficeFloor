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

import net.officefloor.frame.api.function.FunctionFlowContext;
import net.officefloor.frame.api.managedobject.ManagedObject;

/**
 * Context in which the {@link Administration} executes.
 * 
 * @author Daniel Sagenschneider
 */
public interface AdministrationContext<E extends Object, F extends Enum<F>, G extends Enum<G>>
		extends FunctionFlowContext<F> {

	/**
	 * Obtains the particular extensions.
	 * 
	 * @return Extension for the {@link ManagedObject} instances to be
	 *         administered.
	 */
	E[] getExtensions();

	/**
	 * Obtains the {@link GovernanceManager} for the particular key.
	 * 
	 * @param key
	 *            Key identifying the {@link GovernanceManager}.
	 * @return {@link GovernanceManager}.
	 */
	GovernanceManager getGovernance(G key);

	/**
	 * Obtains the {@link GovernanceManager} for the index.
	 * 
	 * @param governanceIndex
	 *            Index identifying the {@link GovernanceManager}.
	 * @return {@link GovernanceManager}.
	 */
	GovernanceManager getGovernance(int governanceIndex);

}