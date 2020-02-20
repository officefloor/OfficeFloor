/*-
 * #%L
 * Web on OfficeFloor Testing
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

package net.officefloor.woof.compile;

import net.officefloor.frame.api.manage.Office;
import net.officefloor.woof.WoofContext;

/**
 * Extension to compile WoOF into the {@link Office}.
 * 
 * @author Daniel Sagenschneider
 */
public interface CompileWoofExtension {

	/**
	 * Extends the {@link Office}.
	 * 
	 * @param context {@link WoofContext}.
	 * @throws Exception If fails to extend.
	 */
	void extend(WoofContext context) throws Exception;

}
