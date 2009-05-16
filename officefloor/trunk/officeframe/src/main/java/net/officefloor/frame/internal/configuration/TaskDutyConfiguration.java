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
import net.officefloor.frame.internal.structure.AdministratorScope;
import net.officefloor.frame.spi.administration.Administrator;
import net.officefloor.frame.spi.administration.Duty;

/**
 * Configuration of a {@link Administrator} {@link Duty} for a {@link Task}.
 * 
 * @author Daniel
 */
public interface TaskDutyConfiguration<A extends Enum<A>> {

	/**
	 * Obtains the name of the {@link Administrator} within the
	 * {@link AdministratorScope}.
	 * 
	 * @return Name of the {@link Administrator} within the
	 *         {@link AdministratorScope}.
	 */
	String getScopeAdministratorName();

	/**
	 * Obtains the name identifying the {@link Duty}.
	 * 
	 * @return Name of the {@link Duty} or <code>null</code> if identified by
	 *         key.
	 */
	String getDutyName();

	/**
	 * Obtains the key identifying the {@link Duty}.
	 * 
	 * @return Key identifying the {@link Duty} or <code>null</code> if
	 *         identified by name.
	 */
	A getDutyKey();

}