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
package net.officefloor.plugin.jndi.work;

import net.officefloor.frame.api.build.WorkFactory;
import net.officefloor.frame.api.execute.Work;

/**
 * {@link WorkFactory} for a JNDI Object.
 * 
 * @author Daniel Sagenschneider
 */
public class JndiWorkFactory implements WorkFactory<JndiWork> {

	/**
	 * JNDI name for the {@link Work} Object.
	 */
	private final String jndiName;

	/**
	 * Facade {@link Class}.
	 */
	private final Class<?> facadeClass;

	/**
	 * Initiate.
	 * 
	 * @param jndiName
	 *            JNDI name for the {@link Work} Object.
	 * @param facadeClass
	 *            Facade {@link Class}.
	 */
	public JndiWorkFactory(String jndiName, Class<?> facadeClass) {
		this.jndiName = jndiName;
		this.facadeClass = facadeClass;
	}

	/*
	 * ====================== WorkFactory ============================
	 */

	@Override
	public JndiWork createWork() {
		return new JndiWork(this.jndiName, this.facadeClass);
	}

}