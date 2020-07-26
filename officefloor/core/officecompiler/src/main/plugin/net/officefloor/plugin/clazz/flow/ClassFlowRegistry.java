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

package net.officefloor.plugin.clazz.flow;

import net.officefloor.frame.internal.structure.Flow;

/**
 * Registry of {@link Flow}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ClassFlowRegistry {

	/**
	 * Registers the {@link Flow}.
	 * 
	 * @param context {@link ClassFlowContext}.
	 * @return Index of the {@link Flow}.
	 */
	int registerFlow(ClassFlowContext context);

}