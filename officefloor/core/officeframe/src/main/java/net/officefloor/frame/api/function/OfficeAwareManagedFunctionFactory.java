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
package net.officefloor.frame.api.function;

import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.internal.structure.ProcessState;

/**
 * <p>
 * {@link Office} aware {@link ManagedFunctionFactory}.
 * <p>
 * This allows the {@link ManagedFunctionFactory} to:
 * <ol>
 * <li>obtain the dynamic meta-data of its containing {@link Office}</li>
 * <li>ability to spawn {@link ProcessState} instances</li>
 * </ol>
 * 
 * @author Daniel Sagenschneider
 */
public interface OfficeAwareManagedFunctionFactory<O extends Enum<O>, F extends Enum<F>>
		extends ManagedFunctionFactory<O, F> {

	/**
	 * Provides the {@link ManagedFunctionFactory} its containing
	 * {@link Office}.
	 * 
	 * @param office
	 *            {@link Office} containing this {@link ManagedFunctionFactory}.
	 * @throws Exception
	 *             If fails to use the {@link Office}.
	 */
	void setOffice(Office office) throws Exception;

}