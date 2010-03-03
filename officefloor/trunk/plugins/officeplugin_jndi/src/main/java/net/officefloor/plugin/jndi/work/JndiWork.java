/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2010 Daniel Sagenschneider
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
package net.officefloor.plugin.jndi.work;

import javax.naming.Context;
import javax.naming.NamingException;

import net.officefloor.frame.api.execute.Work;

/**
 * JNDI {@link Work}.
 * 
 * @author Daniel Sagenschneider
 */
public class JndiWork implements Work {

	/**
	 * JNDI name of the {@link Work} Object.
	 */
	private final String jndiName;

	/**
	 * Facade {@link Class}. May be <code>null</code>.
	 */
	private final Class<?> facadeClass;

	/**
	 * Cached JNDI Object.
	 */
	private Object jndiObject = null;

	/**
	 * Cache facade.
	 */
	private Object facade = null;

	/**
	 * Initiate.
	 * 
	 * @param jndiName
	 *            JNDI name of the {@link Work} Object.
	 * @param facadeClass
	 *            Facade {@link Class}. May be <code>null</code>.
	 */
	public JndiWork(String jndiName, Class<?> facadeClass) {
		this.jndiName = jndiName;
		this.facadeClass = facadeClass;
	}

	/**
	 * Obtains the JNDI Object.
	 * 
	 * @param context
	 *            {@link Context}.
	 * @return JNDI Object.
	 * @throws NamingException
	 *             If fails to obtain the JNDI Object.
	 */
	public Object getJndiObject(Context context) throws NamingException {

		// Lazy load the JNDI Object
		if (this.jndiObject == null) {
			this.jndiObject = context.lookup(this.jndiName);
		}

		// Return the JNDI Object
		return this.jndiObject;
	}

	/**
	 * Obtains the facade.
	 * 
	 * @return Facade.
	 * @throws Exception
	 *             If fails to create the Facade.
	 */
	public Object getFacade() throws Exception {

		// Lazy load the Facade
		if (this.facade == null) {
			this.facade = this.facadeClass.newInstance();
		}

		// Return the Facade
		return this.facade;
	}

}