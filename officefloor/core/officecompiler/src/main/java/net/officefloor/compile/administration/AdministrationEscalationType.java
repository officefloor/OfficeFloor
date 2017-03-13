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
package net.officefloor.compile.administration;

import net.officefloor.frame.api.administration.Administration;
import net.officefloor.frame.api.escalate.Escalation;
import net.officefloor.frame.internal.structure.EscalationFlow;

/**
 * <code>Type definition</code> of a possible {@link EscalationFlow} by the
 * {@link Administration}.
 *
 * @author Daniel Sagenschneider
 */
public interface AdministrationEscalationType {

	/**
	 * Obtains the name for the {@link AdministrationEscalationType}.
	 * 
	 * @return Name for the {@link AdministrationEscalationType}.
	 */
	String getEscalationName();

	/**
	 * Obtains the type of {@link EscalationFlow} by the {@link Administration}.
	 * 
	 * @param <E>
	 *            {@link Escalation} type.
	 * @return Type of {@link EscalationFlow} by the {@link Administration}.
	 */
	<E extends Throwable> Class<E> getEscalationType();

}