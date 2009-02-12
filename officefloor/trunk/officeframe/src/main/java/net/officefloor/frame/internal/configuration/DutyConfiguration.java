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
package net.officefloor.frame.internal.configuration;

import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.spi.administration.Administrator;
import net.officefloor.frame.spi.administration.Duty;

/**
 * Configuration for a {@link Duty}.
 * 
 * @author Daniel
 */
public interface DutyConfiguration<A extends Enum<A>> {

	/**
	 * Obtains key identifying the {@link Duty} of the {@link Administrator}.
	 * 
	 * @return Key identifying the {@link Duty} on the {@link Administrator}.
	 */
	A getDutyKey();

	/**
	 * Obtains the configuration for the {@link Flow} instances invoked by the
	 * {@link Duty}.
	 * 
	 * @return {@link TaskNodeReference} specifying the first {@link Task} of
	 *         the linked {@link Flow}.
	 */
	TaskNodeReference[] getLinkedProcessConfiguration();

}
