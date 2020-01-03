/*-
 * #%L
 * Web resources
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

package net.officefloor.web.resource;

import java.io.IOException;

/**
 * Store of {@link HttpResource} instances.
 * 
 * @author Daniel Sagenschneider
 */
public interface HttpResourceStore {

	/**
	 * Obtains the {@link HttpResource}.
	 * 
	 * @param path Path to the {@link HttpResource}.
	 * @return {@link HttpResource}.
	 * @throws IOException If failure in finding the {@link HttpResource}.
	 */
	HttpResource getHttpResource(String path) throws IOException;

	/**
	 * Obtains the default {@link HttpFile} for the {@link HttpDirectory}.
	 * 
	 * @param directory {@link HttpDirectory}.
	 * @return {@link HttpFile} for the {@link HttpDirectory} or <code>null</code>
	 *         if no default {@link HttpFile}.
	 * @throws IOException If failure in obtaining default {@link HttpFile}.
	 */
	HttpFile getDefaultHttpFile(HttpDirectory directory) throws IOException;

}
