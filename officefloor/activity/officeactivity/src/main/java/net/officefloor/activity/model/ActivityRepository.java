/*-
 * #%L
 * Activity
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

package net.officefloor.activity.model;

import net.officefloor.configuration.ConfigurationItem;
import net.officefloor.configuration.WritableConfigurationItem;

/**
 * Repository for obtaining the Activity model.
 * 
 * @author Daniel Sagenschneider
 */
public interface ActivityRepository {

	/**
	 * Retrieves the {@link ActivityModel} from the {@link ConfigurationItem}.
	 * 
	 * @param activity      {@link ActivityModel}.
	 * @param configuration {@link ConfigurationItem}.
	 * @throws Exception If fails to retrieve the {@link ActivityModel}.
	 */
	void retrieveActivity(ActivityModel activity, ConfigurationItem configuration) throws Exception;

	/**
	 * Stores the {@link ActivityModel} within the
	 * {@link WritableConfigurationItem}.
	 * 
	 * @param activity      {@link ActivityModel}.
	 * @param configuration {@link WritableConfigurationItem}.
	 * @throws Exception If fails to store the {@link ActivityModel}.
	 */
	void storeActivity(ActivityModel activity, WritableConfigurationItem configuration) throws Exception;

}
