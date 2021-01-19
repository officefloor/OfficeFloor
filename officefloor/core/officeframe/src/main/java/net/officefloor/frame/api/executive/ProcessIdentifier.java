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

package net.officefloor.frame.api.executive;

import net.officefloor.frame.internal.structure.ProcessState;

/**
 * <p>
 * Identifier for {@link ProcessState}.
 * <p>
 * The requirements are that:
 * <ul>
 * <li>the same {@link ProcessIdentifier} equals itself</li>
 * <li>no two separate {@link ProcessIdentifier} instance equal each other</li>
 * <ul>
 * <p>
 * The easiest way to ensure this is create a new instance each time and allow
 * default {@link Object} equality.
 * <p>
 * Other than the above, the {@link Executive} is free to provide any
 * implementation of this interface as a momento about the {@link ProcessState}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ProcessIdentifier {
}
