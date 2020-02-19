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

package net.officefloor.web.resource.spi;

import java.io.IOException;

/**
 * Service for the creation of a {@link ResourceSystem}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ResourceSystemFactory {

	/**
	 * <p>
	 * Obtains the protocol name for the created {@link ResourceSystem}.
	 * <p>
	 * The protocol name is used as follows <code>[protocol]:location</code> to
	 * configure a {@link ResourceSystem}.
	 * 
	 * @return Protocol name for the created {@link ResourceSystem}.
	 */
	String getProtocolName();

	/**
	 * Creates the {@link ResourceSystem}.
	 * 
	 * @param context
	 *            {@link ResourceSystemContext}.
	 * @return {@link ResourceSystem}.
	 * @throws IOException
	 *             If fails to create the {@link ResourceSystem}.
	 */
	ResourceSystem createResourceSystem(ResourceSystemContext context) throws IOException;

}
