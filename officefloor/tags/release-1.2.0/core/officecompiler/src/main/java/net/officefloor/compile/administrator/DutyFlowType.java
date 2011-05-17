/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2011 Daniel Sagenschneider
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

package net.officefloor.compile.administrator;

import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.spi.administration.Duty;

/**
 * <code>Type definition</code> of a {@link Flow} instigated by a {@link Duty}.
 * 
 * @author Daniel Sagenschneider
 */
public interface DutyFlowType<F extends Enum<F>> {

	/**
	 * Obtains the name of the {@link Flow}.
	 * 
	 * @return Name of the {@link Flow}.
	 */
	String getFlowName();

	/**
	 * Obtains the index identifying the {@link Flow}.
	 * 
	 * @return Index identifying the {@link Flow}.
	 */
	int getIndex();

	/**
	 * Obtains the type of the argument passed by the {@link Duty} to the
	 * {@link Flow}.
	 * 
	 * @return Type of argument passed by the {@link Duty}. May be
	 *         <code>null</code> to indicate no argument.
	 */
	Class<?> getArgumentType();

	/**
	 * Obtains the key identifying the {@link Flow}.
	 * 
	 * @return Key identifying the {@link Flow}.
	 */
	F getKey();

}