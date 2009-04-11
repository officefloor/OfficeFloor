/*
 *  Office Floor, Application Server
 *  Copyright (C) 2006 Daniel Sagenschneider
 *
 *  This program is free software; you can redistribute it and/or modify it under the terms 
 *  of the GNU General Public License as published by the Free Software Foundation; either 
 *  version 2 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program; 
 *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, 
 *  MA 02111-1307 USA
 */
package net.officefloor.compile.spi.office;

import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.internal.structure.EscalationProcedure;

/**
 * Output from the {@link OfficeSection}.
 * 
 * @author Daniel
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