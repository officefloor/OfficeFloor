/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.officefloor.compile;

import java.lang.reflect.Proxy;

/**
 * <p>
 * Runnable using {@link ClassLoader} of the {@link OfficeFloorCompiler}.
 * <p>
 * This is typically used for graphical configuration to run extended
 * functionality adapted for the {@link ClassLoader} of the
 * {@link OfficeFloorCompiler}.
 * 
 * @author Daniel Sagenschneider
 */
public interface OfficeFloorCompilerRunnable<T> {

	/**
	 * Contains the runnable functionality.
	 * 
	 * @param compiler
	 *            {@link OfficeFloorCompiler} loaded with the
	 *            {@link ClassLoader}.
	 * @param parameters
	 *            Parameters. As {@link Proxy} instances are used to bridge
	 *            {@link Class} compatibility issues of using different
	 *            {@link ClassLoader} instances, parameters should only be
	 *            referenced by their implementing interfaces.
	 * @return Result from runnable.
	 * @throws Exception
	 *             If failure in running.
	 */
	T run(OfficeFloorCompiler compiler, Object[] parameters) throws Exception;

}