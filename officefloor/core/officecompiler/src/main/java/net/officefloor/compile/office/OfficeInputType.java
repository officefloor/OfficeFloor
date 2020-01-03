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

package net.officefloor.compile.office;

import net.officefloor.compile.spi.office.OfficeInput;
import net.officefloor.frame.api.manage.Office;

/**
 * <code>Type definition</code> of an {@link OfficeInput} into the
 * {@link Office}.
 * 
 * @author Daniel Sagenschneider
 */
public interface OfficeInputType {

	/**
	 * Obtains the name of {@link OfficeInput}.
	 * 
	 * @return Name of this {@link OfficeInput}.
	 */
	String getOfficeInputName();

	/**
	 * Obtains the fully qualified class name of the parameter type to this
	 * {@link OfficeInput}.
	 * 
	 * @return Parameter type to this {@link OfficeInput}.
	 */
	String getParameterType();

}
