/*-
 * #%L
 * Web Plug-in
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

package net.officefloor.web.build;

import net.officefloor.compile.spi.office.OfficeFlowSourceNode;
import net.officefloor.web.HttpInputPath;

/**
 * HTTP input.
 * 
 * @author Daniel Sagenschneider
 */
public interface HttpInput {

	/**
	 * Obtains the {@link OfficeFlowSourceNode} to link for handling the
	 * {@link HttpInput}.
	 * 
	 * @return {@link OfficeFlowSourceNode} to link for handling the
	 *         {@link HttpInput}.
	 */
	OfficeFlowSourceNode getInput();

	/**
	 * Obtains the {@link HttpInputPath} for this {@link HttpInput}.
	 * 
	 * @return {@link HttpInputPath} for this {@link HttpInput}.
	 */
	HttpInputPath getPath();

	/**
	 * Specifies documentation to describe this {@link HttpInput}.
	 * 
	 * @param documentation Documentation to describe this {@link HttpInput}.
	 */
	void setDocumentation(String documentation);
}
