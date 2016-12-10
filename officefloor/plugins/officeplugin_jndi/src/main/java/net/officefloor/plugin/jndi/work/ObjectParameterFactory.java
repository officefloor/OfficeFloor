/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2013 Daniel Sagenschneider
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
package net.officefloor.plugin.jndi.work;

import net.officefloor.frame.api.execute.TaskContext;

/**
 * {@link ParameterFactory} to obtain an Object from the {@link TaskContext}.
 * 
 * @author Daniel Sagenschneider
 */
public class ObjectParameterFactory implements ParameterFactory {

	/**
	 * Dependency index of the Object from the {@link TaskContext}.
	 */
	private final int dependencyIndex;

	/**
	 * Initiate.
	 * 
	 * @param dependencyIndex
	 *            Dependency index of the Object from the {@link TaskContext}.
	 */
	public ObjectParameterFactory(int dependencyIndex) {
		this.dependencyIndex = dependencyIndex;
	}

	/*
	 * ================= ParameterFactory =======================
	 */

	@Override
	public Object createParameter(Object jndiWorkObject,
			TaskContext<?, ?, ?> context) throws Exception {

		// Obtain the object
		Object object = context.getObject(this.dependencyIndex);

		// Return the object
		return object;
	}

}