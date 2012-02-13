/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2012 Daniel Sagenschneider
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

package net.officefloor.frame.spi.managedobject.source;

import net.officefloor.frame.api.build.FlowNodeBuilder;
import net.officefloor.frame.api.execute.Task;

/**
 * Builds the {@link Task} necessary for the {@link ManagedObjectSource}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ManagedObjectTaskBuilder<D extends Enum<D>, F extends Enum<F>>
		extends FlowNodeBuilder<F> {

	/**
	 * Links in the parameter for this {@link Task}.
	 * 
	 * @param key
	 *            Key identifying the parameter.
	 * @param parameterType
	 *            Type of the parameter.
	 */
	void linkParameter(D key, Class<?> parameterType);

	/**
	 * Links in the parameter for this {@link Task}.
	 * 
	 * @param index
	 *            Index identifying the parameter.
	 * @param parameterType
	 *            Type of the parameter.
	 */
	void linkParameter(int index, Class<?> parameterType);

}