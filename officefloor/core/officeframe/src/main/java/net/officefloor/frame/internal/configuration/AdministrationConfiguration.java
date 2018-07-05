/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.officefloor.frame.internal.configuration;

import net.officefloor.frame.api.administration.Administration;
import net.officefloor.frame.api.administration.AdministrationFactory;
import net.officefloor.frame.api.governance.Governance;
import net.officefloor.frame.api.managedobject.ManagedObject;

/**
 * Configuration of the {@link Administration}.
 * 
 * @author Daniel Sagenschneider
 */
public interface AdministrationConfiguration<E, F extends Enum<F>, G extends Enum<G>> extends FunctionConfiguration<F> {

	/**
	 * Obtains the name of the {@link Administration}.
	 * 
	 * @return Name of the {@link Administration}.
	 */
	String getAdministrationName();

	/**
	 * Obtains the {@link AdministrationFactory}.
	 * 
	 * @return {@link AdministrationFactory}.
	 */
	AdministrationFactory<E, F, G> getAdministrationFactory();

	/**
	 * Obtains the extension interface.
	 * 
	 * @return Extension interface.
	 */
	Class<E> getExtensionType();

	/**
	 * Obtains the names of the {@link ManagedObject} instances to be
	 * administered.
	 * 
	 * @return Names of the {@link ManagedObject} instances to be administered.
	 */
	String[] getAdministeredManagedObjectNames();

	/**
	 * Obtains the configuration for the linked {@link Governance}.
	 * 
	 * @return {@link AdministrationGovernanceConfiguration} specifying the
	 *         linked {@link Governance}.
	 */
	AdministrationGovernanceConfiguration<?>[] getGovernanceConfiguration();

}