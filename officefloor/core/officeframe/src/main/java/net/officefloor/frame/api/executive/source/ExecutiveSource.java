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

package net.officefloor.frame.api.executive.source;

import net.officefloor.frame.api.executive.Executive;

/**
 * Source to obtain the {@link Executive}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ExecutiveSource {

	/**
	 * <p>
	 * Obtains the specification for this.
	 * <p>
	 * This will be called before any other methods, therefore this method must
	 * be able to return the specification immediately after a default
	 * constructor instantiation.
	 * 
	 * @return Specification of this.
	 */
	ExecutiveSourceSpecification getSpecification();

	/**
	 * Creates the {@link Executive}.
	 * 
	 * @param context
	 *            {@link ExecutiveSourceContext}.
	 * @return {@link Executive}.
	 * @throws Exception
	 *             If fails to configure the {@link ExecutiveSource}.
	 */
	Executive createExecutive(ExecutiveSourceContext context) throws Exception;

}
