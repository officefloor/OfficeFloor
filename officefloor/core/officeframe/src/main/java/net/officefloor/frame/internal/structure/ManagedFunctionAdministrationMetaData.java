/*-
 * #%L
 * OfficeFrame
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package net.officefloor.frame.internal.structure;

import java.util.logging.Logger;

import net.officefloor.frame.api.administration.Administration;
import net.officefloor.frame.api.administration.AdministrationContext;
import net.officefloor.frame.api.function.ManagedFunction;

/**
 * Meta-data for the {@link Administration} of the {@link ManagedFunction}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ManagedFunctionAdministrationMetaData<E, F extends Enum<F>, G extends Enum<G>> {

	/**
	 * Obtains the {@link Logger} for the {@link AdministrationContext}.
	 * 
	 * @return {@link Logger} for the {@link AdministrationContext}.
	 */
	Logger getLogger();

	/**
	 * Obtains the {@link AdministrationMetaData}.
	 * 
	 * @return {@link AdministrationMetaData}.
	 */
	AdministrationMetaData<E, F, G> getAdministrationMetaData();

}
