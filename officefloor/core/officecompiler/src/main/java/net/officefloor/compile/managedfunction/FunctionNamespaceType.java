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
package net.officefloor.compile.managedfunction;

import net.officefloor.frame.api.build.WorkFactory;
import net.officefloor.frame.api.execute.Work;

/**
 * <code>Type definition</code> of a {@link Work}.
 * 
 * @author Daniel Sagenschneider
 */
public interface FunctionNamespaceType<W extends Work> {

	/**
	 * Obtains the {@link WorkFactory} to create the {@link Work} instances.
	 * 
	 * @return {@link WorkFactory}.
	 */
	WorkFactory<W> getWorkFactory();

	/**
	 * Obtains the {@link ManagedFunctionType} definitions.
	 * 
	 * @return {@link ManagedFunctionType} definitions.
	 */
	ManagedFunctionType<W, ?, ?>[] getManagedFunctionTypes();

}