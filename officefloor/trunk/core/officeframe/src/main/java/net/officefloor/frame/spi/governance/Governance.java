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
package net.officefloor.frame.spi.governance;

import net.officefloor.frame.spi.managedobject.ManagedObject;

/**
 * <p>
 * Provides {@link Governance} over the {@link ManagedObject} instances.
 * <p>
 * The extension interface of the {@link ManagedObject} is used to provide the
 * {@link Governance}.
 * 
 * @author Daniel Sagenschneider
 */
public interface Governance<I, F extends Enum<F>> {

	/**
	 * <p>
	 * Initialises the {@link Governance}.
	 * <p>
	 * This enables identifying the start of {@link Governance} and allow batch
	 * loading of {@link ManagedObject} instances to govern. In other words, the
	 * following steps occur:
	 * <ol>
	 * <li>{@link #init(GovernanceContext)} is invoked and any setup work can be
	 * accomplished.</li>
	 * <li>{@link #governManagedObject(Object)} is invoked potentially multiple
	 * times for the {@link ManagedObject} instances to be governed.</li>
	 * <li>
	 * <li>{@link #startGovernance()} is invoked to indicate to begin governing.
	 * </li>
	 * <li>Note that further {@link ManagedObject} instances may be added for
	 * {@link Governance} after starting.</li>
	 * </ol>
	 * 
	 * @param context
	 *            {@link GovernanceContext}.
	 * @throws Exception
	 *             If fails to initialise the {@link Governance}.
	 */
	void init(GovernanceContext<F> context) throws Exception;

	/**
	 * Registers the {@link ManagedObject} for {@link Governance}.
	 * 
	 * @param extensionInterface
	 *            Extension interface of the {@link ManagedObject}.
	 * @throws Exception
	 *             If fails to govern the {@link ManagedObject}.
	 */
	void governManagedObject(I extensionInterface) throws Exception;

	/**
	 * Starts the {@link Governance} of the {@link ManagedObject} instances.
	 */
	void startGovernance();

	/**
	 * Applies the {@link Governance} to the {@link ManagedObject} instances
	 * under {@link Governance}.
	 */
	void applyGovernance();

	/**
	 * Stops {@link Governance} of the {@link ManagedObject} instances.
	 */
	void stopGovernance();

}