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
package net.officefloor.compile.administrator;

import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.spi.administration.Duty;

/**
 * <code>Type definition</code> of as {@link Duty}.
 * 
 * @author Daniel Sagenschneider
 */
public interface DutyType<A extends Enum<A>, F extends Enum<F>> {

	/**
	 * Obtains the name for the {@link Duty}.
	 * 
	 * @return Name for the {@link Duty}.
	 */
	String getDutyName();

	/**
	 * Obtains the key for this {@link Duty}.
	 * 
	 * @return Key for this {@link Duty}.
	 */
	A getDutyKey();

	/**
	 * Obtains the {@link Enum} providing the keys for the {@link Flow}
	 * instances instigated by the {@link Duty}.
	 * 
	 * @return {@link Enum} providing instigated {@link Flow} keys or
	 *         <code>null</code> if {@link Indexed} or no instigated
	 *         {@link Flow} instances.
	 */
	Class<F> getFlowKeyClass();

	/**
	 * Obtains the {@link DutyFlowType} definitions for the possible
	 * {@link Flow} instances instigated by the {@link Duty}.
	 * 
	 * @return {@link DutyFlowType} definitions for the possible
	 *         {@link Flow} instances instigated by the {@link Duty}.
	 */
	DutyFlowType<F>[] getFlowTypes();

	/**
	 * Obtains the {@link DutyEscalationType} definitions for the possible
	 * {@link EscalationFlow} instances by the {@link Duty}.
	 * 
	 * @return {@link DutyEscalationType} definitions for the possible
	 *         {@link EscalationFlow} instances by the {@link Duty}.
	 */
	// TODO provide getEscalationTypes() from DutyType
	// DutyEscalationType[] getEscalationTypes();

}