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

package net.officefloor.compile.spi.office.source;

import net.officefloor.compile.office.OfficeType;
import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.frame.api.manage.Office;

/**
 * Sources the {@link OfficeType}.
 * 
 * @author Daniel Sagenschneider
 */
public interface OfficeSource {

	/**
	 * <p>
	 * Obtains the {@link OfficeSourceSpecification} for this
	 * {@link OfficeSource}.
	 * <p>
	 * This enables the {@link OfficeSourceContext} to be populated with the
	 * necessary details as per this {@link OfficeSourceSpecification} in
	 * loading the {@link OfficeType}.
	 * 
	 * @return {@link OfficeSourceSpecification}.
	 */
	OfficeSourceSpecification getSpecification();

	/**
	 * Sources the {@link OfficeType} by constructing it via the input
	 * {@link OfficeArchitect}.
	 * 
	 * @param officeArchitect
	 *            {@link OfficeArchitect} to structure the {@link Office}.
	 * @param context
	 *            {@link OfficeSourceContext} to source details to structure the
	 *            {@link Office}.
	 * @throws Exception
	 *             If fails to construct the {@link Office}.
	 */
	void sourceOffice(OfficeArchitect officeArchitect,
			OfficeSourceContext context) throws Exception;

}
