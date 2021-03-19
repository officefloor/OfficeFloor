/*-
 * #%L
 * Servlet
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

package net.officefloor.servlet.supply.extension;

import javax.servlet.Servlet;

import net.officefloor.servlet.supply.ServletSupplierSource;

/**
 * Extension to {@link ServletSupplierSource}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ServletSupplierExtension {

	/**
	 * <p>
	 * Invoked before completing.
	 * <p>
	 * Note that the {@link Servlet} container may have been force started before
	 * invoking this.
	 * 
	 * @param context {@link BeforeCompleteServletSupplierExtensionContext}.
	 * @throws Exception If fails completion.
	 */
	default void beforeCompletion(BeforeCompleteServletSupplierExtensionContext context) throws Exception {
		// does nothing by default
	}

}
