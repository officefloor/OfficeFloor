/*-
 * #%L
 * OfficeCompiler
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

package net.officefloor.model.office;

import net.officefloor.configuration.ConfigurationItem;
import net.officefloor.configuration.WritableConfigurationItem;

/**
 * Repository of the {@link OfficeModel} instances.
 * 
 * @author Daniel Sagenschneider
 */
public interface OfficeRepository {

	/**
	 * Retrieves the {@link OfficeModel} from the {@link ConfigurationItem}.
	 * 
	 * @param office
	 *            {@link OfficeModel}.
	 * @param configuration
	 *            {@link ConfigurationItem} containing the {@link OfficeModel}.
	 * @throws Exception
	 *             If fails to retrieve the {@link OfficeModel}.
	 */
	void retrieveOffice(OfficeModel office, ConfigurationItem configuration) throws Exception;

	/**
	 * Stores the {@link OfficeModel} into the {@link ConfigurationItem}.
	 * 
	 * @param office
	 *            {@link OfficeModel}.
	 * @param configuration
	 *            {@link WritableConfigurationItem} to contain the
	 *            {@link OfficeModel}.
	 * @throws Exception
	 *             If fails to store the {@link OfficeModel}.
	 */
	void storeOffice(OfficeModel office, WritableConfigurationItem configuration) throws Exception;

}
