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
package net.officefloor.plugin.ejb;

import javax.ejb.Local;
import javax.naming.NamingException;

import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.api.manage.OfficeFloor;

/**
 * {@link Local} interface to orchestrate EJBs within an {@link OfficeFloor}.
 * 
 * @author Daniel Sagenschneider
 */
@Local
public interface EjbOrchestrator {

	/**
	 * Orchestrates the EJBs.
	 * 
	 * @param parameter
	 *            Parameter for the initial {@link Work} of the
	 *            {@link OfficeFloor} initiating the orchestration of the EJBs.
	 * @throws NamingException
	 *             If failure in orchestration.
	 */
	void orchestrate(Object parameter) throws NamingException;

}