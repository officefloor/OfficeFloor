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
package net.officefloor.officefloor;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.spi.ObjectFactory;

import net.officefloor.plugin.jndi.context.OfficeFloorContext;

/**
 * <p>
 * URL Context Factory for the &quot;<code>officefloor</code>&quot; schema.
 * <p>
 * This class is with the &quot;<code>net.officefloor</code>&quot; package
 * prefix.
 * <p>
 * This class is provided so that the default JNDI package prefix is not
 * required to be included, as it may result in side effects by making other URL
 * Context Factories available.
 * 
 * @author Daniel Sagenschneider
 */
public class officefloorURLContextFactory implements ObjectFactory {

	/**
	 * Obtains the {@link OfficeFloorContext}.
	 * 
	 * @param obj
	 *            Object.
	 * @param name
	 *            Name.
	 * @param nameCtx
	 *            Name {@link Context}.
	 * @param environment
	 *            Environment.
	 * @return New {@link OfficeFloorContext}.
	 * @throws Exception
	 *             If fails to create {@link OfficeFloorContext}.
	 */
	public static OfficeFloorContext getOfficeFloorContext(Object obj,
			Name name, Context nameCtx, Hashtable<?, ?> environment)
			throws Exception {
		return new OfficeFloorContext();
	}

	/*
	 * ================== ObjectFactory ===============================
	 */

	@Override
	public Object getObjectInstance(Object obj, Name name, Context nameCtx,
			Hashtable<?, ?> environment) throws Exception {
		return getOfficeFloorContext(obj, name, nameCtx, environment);
	}

}