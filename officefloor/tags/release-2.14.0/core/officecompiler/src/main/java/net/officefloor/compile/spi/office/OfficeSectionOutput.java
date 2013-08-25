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
package net.officefloor.compile.spi.office;

import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.internal.structure.EscalationProcedure;

/**
 * Output from the {@link OfficeSection}.
 * 
 * @author Daniel Sagenschneider
 */
public interface OfficeSectionOutput {

	/**
	 * Obtains the name of this {@link OfficeSectionOutput}.
	 * 
	 * @return Name of this {@link OfficeSectionOutput}.
	 */
	String getOfficeSectionOutputName();

	/**
	 * Obtains the argument type from this {@link OfficeSectionOutput}.
	 * 
	 * @return Argument type.
	 */
	String getArgumentType();

	/**
	 * Indicates if this {@link OfficeSectionOutput} is escalation only. In
	 * other words it can be handled by an {@link Office}
	 * {@link EscalationProcedure}.
	 * 
	 * @return <code>true</code> if escalation only.
	 */
	boolean isEscalationOnly();

}