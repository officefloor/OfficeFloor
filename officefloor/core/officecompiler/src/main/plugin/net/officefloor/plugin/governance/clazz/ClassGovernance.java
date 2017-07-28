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
package net.officefloor.plugin.governance.clazz;

import java.lang.reflect.Method;

import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.governance.Governance;
import net.officefloor.frame.api.governance.GovernanceContext;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.plugin.managedfunction.clazz.ClassFunction;

/**
 * {@link Class} {@link Governance}.
 * 
 * @author Daniel Sagenschneider
 */
public class ClassGovernance implements Governance<Object, Indexed> {

	/**
	 * Instance of the {@link Class}.
	 */
	private final Object instance;

	/**
	 * {@link Method} for governing the {@link ManagedObject}.
	 */
	private final Method governMethod;

	/**
	 * {@link Method} to enforce the {@link Governance}.
	 */
	private final Method enforceMethod;

	/**
	 * {@link Method} to disregard the {@link Governance}.
	 */
	private final Method disregardMethod;

	/**
	 * Initiate.
	 * 
	 * @param instance
	 *            Instance of the {@link Class}.
	 * @param governMethod
	 *            {@link Method} for governing the {@link ManagedObject}.
	 * @param enforceMethod
	 *            {@link Method} to enforce the {@link Governance}.
	 * @param disregardMethod
	 *            {@link Method} to disregard the {@link Governance}. May be
	 *            <code>null</code> if no functionality required for
	 *            disregarding.
	 */
	public ClassGovernance(Object instance, Method governMethod,
			Method enforceMethod, Method disregardMethod) {
		this.instance = instance;
		this.governMethod = governMethod;
		this.enforceMethod = enforceMethod;
		this.disregardMethod = disregardMethod;
	}

	/*
	 * ====================== Governance =========================
	 */

	@Override
	public void governManagedObject(Object extensionInterface,
			GovernanceContext<Indexed> context) throws Throwable {
		ClassFunction.invokeMethod(this.instance, this.governMethod,
				new Object[] { extensionInterface });
	}

	@Override
	public void enforceGovernance(GovernanceContext<Indexed> context)
			throws Throwable {
		ClassFunction
				.invokeMethod(this.instance, this.enforceMethod, new Object[0]);
	}

	@Override
	public void disregardGovernance(GovernanceContext<Indexed> context)
			throws Throwable {
		// Disregard if method to do so
		if (this.disregardMethod != null) {
			ClassFunction.invokeMethod(this.instance, this.disregardMethod,
					new Object[0]);
		}
	}

}