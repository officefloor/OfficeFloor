/*-
 * #%L
 * OfficeFloor Cache of static data
 * %%
 * Copyright (C) 2005 - 2021 Daniel Sagenschneider
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

package net.officefloor.cache.constant;

import java.util.Map;

/**
 * Retrieves the data for the {@link ConstantCacheManagedObjectSource}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ConstantCacheDataRetriever<K, V> {

	/**
	 * Obtains the data.
	 * 
	 * @return {@link Map} containing the cached key/value pairs.
	 * @throws Exception If fails to obtain data.
	 */
	Map<K, V> getData() throws Exception;

}
