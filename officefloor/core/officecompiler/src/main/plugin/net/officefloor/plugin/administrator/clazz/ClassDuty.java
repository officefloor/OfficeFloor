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

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import net.officefloor.frame.api.build.None;
import net.officefloor.frame.spi.administration.Duty;
import net.officefloor.frame.spi.administration.DutyContext;

/**
 * {@link Duty} that delegates to a {@link Method} of an {@link Object} for
 * administration.
 * 
 * @author Daniel Sagenschneider
 */
public class ClassDuty implements Duty<Object, None, None> {

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
	 * {@link Method} to invoke on the {@link Object} for this {@link Duty}.
	 */
	private final Method dutyMethod;

	/**
	 * Initiate.
	 * 
	 * @param object
	 *            {@link Object} providing the administration {@link Method}
	 *            instances.
	 * @param extensionInterfaceArrayType
	 *            Array type for the array of extension interfaces to pass to
	 *            the administration {@link Method}.
	 * @param dutyMethod
	 *            {@link Method} to invoke on the {@link Object} for this
	 *            {@link Duty}.
	 */
	public ClassDuty(Object object, Class<?> extensionInterfaceArrayType,
			Method dutyMethod) {
		this.object = object;
		this.extensionInterfaceArrayType = extensionInterfaceArrayType;
		this.dutyMethod = dutyMethod;
	}

	/*
	 * ===================== Duty ==============================
	 */

	@Override
	public void doDuty(DutyContext<Object, None, None> context)
			throws Throwable {

		// Obtain the listing of extension interfaces
		List<Object> extensionInterfaces = context.getExtensionInterfaces();

		// Transform into an array of extension interfaces
		Object[] extensionInterfaceArray = (Object[]) Array.newInstance(
				this.extensionInterfaceArrayType, extensionInterfaces.size());
		for (int i = 0; i < extensionInterfaceArray.length; i++) {
			extensionInterfaceArray[i] = extensionInterfaces.get(i);
		}

		try {
			// Invoke the method to administer extension interfaces
			this.dutyMethod.invoke(this.object,
					(Object) extensionInterfaceArray);

		} catch (InvocationTargetException ex) {
			// Propagate cause
			throw ex.getCause();
		}
	}

}