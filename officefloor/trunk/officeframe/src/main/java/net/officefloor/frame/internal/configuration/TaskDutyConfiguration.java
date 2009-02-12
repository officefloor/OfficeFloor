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

/**
 * Configuration of a administration duty configuration for a {@link Task}.
 * 
 * @author Daniel
 */
public interface TaskDutyConfiguration<A extends Enum<A>> {

	/**
	 * Obtains the name of the {@link Administrator} local to the {@link Work}.
	 * 
	 * @return Name of the {@link Administrator} local to the {@link Work}.
	 */
	String getWorkAdministratorName();

	/**
	 * Obtains the key identifying the duty to administer.
	 * 
	 * @return Key identifying the duty to administer.
	 */
	A getDuty();

}
