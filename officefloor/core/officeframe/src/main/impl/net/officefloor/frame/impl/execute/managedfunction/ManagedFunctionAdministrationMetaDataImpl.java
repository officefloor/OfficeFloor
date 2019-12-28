/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2019 Daniel Sagenschneider
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
package net.officefloor.frame.impl.execute.managedfunction;

import java.util.logging.Logger;

import net.officefloor.frame.api.administration.AdministrationContext;
import net.officefloor.frame.internal.structure.AdministrationMetaData;
import net.officefloor.frame.internal.structure.ManagedFunctionAdministrationMetaData;

/**
 * {@link ManagedFunctionAdministrationMetaData} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class ManagedFunctionAdministrationMetaDataImpl<E, F extends Enum<F>, G extends Enum<G>>
		implements ManagedFunctionAdministrationMetaData<E, F, G> {

	/**
	 * {@link Logger} for {@link AdministrationContext}.
	 */
	private final Logger logger;

	/**
	 * {@link AdministrationMetaData}.
	 */
	private final AdministrationMetaData<E, F, G> administrationMetaData;

	/**
	 * Instantiate.
	 * 
	 * @param logger                 {@link Logger} for
	 *                               {@link AdministrationContext}.
	 * @param administrationMetaData {@link AdministrationMetaData}.
	 */
	public ManagedFunctionAdministrationMetaDataImpl(Logger logger,
			AdministrationMetaData<E, F, G> administrationMetaData) {
		this.logger = logger;
		this.administrationMetaData = administrationMetaData;
	}

	/*
	 * =================== ManagedFunctionAdministrationMetaData ==================
	 */

	@Override
	public Logger getLogger() {
		return this.logger;
	}

	@Override
	public AdministrationMetaData<E, F, G> getAdministrationMetaData() {
		return this.administrationMetaData;
	}

}