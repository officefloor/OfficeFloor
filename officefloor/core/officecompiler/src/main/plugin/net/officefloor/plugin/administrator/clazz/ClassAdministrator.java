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
package net.officefloor.plugin.administrator.clazz;

import java.lang.reflect.Method;

import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.spi.administration.Administrator;
import net.officefloor.frame.spi.administration.Duty;
import net.officefloor.frame.spi.administration.DutyKey;

/**
 * {@link Administrator} that delegates to {@link Method} instances of an
 * {@link Object} to do administration.
 * 
 * @author Daniel Sagenschneider
 */
public class ClassAdministrator implements Administrator<Object, Indexed> {

	/**
	 * {@link Object} providing the administration {@link Method} instances.
	 */
	private final Object object;

	/**
	 * Array type for the array of extension interfaces to pass to the
	 * administration {@link Method}.
	 */
	private final Class<?> extensionInterfaceArrayType;

	/**
	 * {@link Duty} method instances in index order as per the {@link DutyKey}.
	 */
	private final Method[] dutyMethods;

	/**
	 * Initiate.
	 * 
	 * @param object
	 *            {@link Object} providing the administration {@link Method}
	 *            instances.
	 * @param extensionInterfaceArrayType
	 *            Array type for the array of extension interfaces to pass to
	 *            the administration {@link Method}.
	 * @param dutyMethods
	 *            {@link Duty} method instances in index order as per the
	 *            {@link DutyKey}.
	 */
	public ClassAdministrator(Object object,
			Class<?> extensionInterfaceArrayType, Method[] dutyMethods) {
		this.object = object;
		this.extensionInterfaceArrayType = extensionInterfaceArrayType;
		this.dutyMethods = dutyMethods;
	}

	/*
	 * ================== Administrator ===================================
	 */

	@Override
	public Duty<Object, ?, ?> getDuty(DutyKey<Indexed> dutyKey) {

		// Obtain the method for the duty
		Method dutyMethod = this.dutyMethods[dutyKey.getIndex()];

		// Return the duty
		return new ClassDuty(this.object, this.extensionInterfaceArrayType,
				dutyMethod);
	}

}