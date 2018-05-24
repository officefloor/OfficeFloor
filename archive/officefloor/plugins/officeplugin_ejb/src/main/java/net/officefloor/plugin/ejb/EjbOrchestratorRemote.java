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

import javax.ejb.Remote;
import javax.naming.NamingException;

import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.manage.OfficeFloor;

/**
 * {@link Remote} interface to orchestrate EJBs within an {@link OfficeFloor}.
 * 
 * @author Daniel Sagenschneider
 */
@Remote
public interface EjbOrchestratorRemote {

	/**
	 * Remotely orchestrates EJBs.
	 * 
	 * @param <P>
	 *            Parameter type.
	 * @param parameter
	 *            Parameter for the {@link ManagedFunction} of the
	 *            {@link OfficeFloor} initiating the orchestration of the EJBs.
	 * @return As {@link Remote} invocations pass by value (serialised), the
	 *         input parameter is returned to allow changes to the parameter to
	 *         be obtained by the caller.
	 * @throws NamingException
	 *             On failure of orchestration.
	 */
	<P> P orchestrateRemotely(P parameter) throws NamingException;

}