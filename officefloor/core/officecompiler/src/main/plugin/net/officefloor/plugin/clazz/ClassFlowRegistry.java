/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2017 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.officefloor.plugin.clazz;

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
	 * @param label
	 *            Label for the {@link Flow}.
	 * @param flowParameterType
	 *            {@link Class} for the parameter to the {@link Flow}. May be
	 *            <code>null</code>.
	 */
	void registerFlow(String label, Class<?> flowParameterType);

}