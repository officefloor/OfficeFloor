/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
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
package net.officefloor.jdbc;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;

/**
 * Identifies the {@link Connection} as being wrapped for appropriate handling
 * by recycling.
 * 
 * @author Daniel Sagenschneider
 */
public interface ConnectionRecycleWrapper {

	/**
	 * {@link #isRealConnection()} {@link Method} name.
	 */
	String IS_REAL_CONNECTION_METHOD_NAME = "isRealConnection";

	/**
	 * Indicates if {@link Method} is the {@link #isRealConnection()}.
	 * 
	 * @param method {@link Method}.
	 * @return <code>true</code> if {@link #isRealConnection()}.
	 */
	static boolean isRealConnectionMethod(Method method) {
		return IS_REAL_CONNECTION_METHOD_NAME.equals(method.getName());
	}

	/**
	 * Obtains the {@link #isRealConnection()} {@link Method}.
	 * 
	 * @return {@link #isRealConnection()} {@link Method}.
	 */
	static Method isRealConnectionMethod() {
		try {
			return ConnectionRecycleWrapper.class.getMethod(IS_REAL_CONNECTION_METHOD_NAME);
		} catch (NoSuchMethodException | SecurityException e) {
			throw new IllegalStateException("Unable to obtain " + IS_REAL_CONNECTION_METHOD_NAME + " method");
		}
	}

	/**
	 * <p>
	 * Allows to determine if there is an actual {@link Connection} to be recycled.
	 * <p>
	 * Some implementations may {@link Proxy} the {@link Connection} to source on
	 * invocation of first {@link Connection} method. Therefore, in inspecting the
	 * {@link Connection} on recycle, it will actually acquire a {@link Connection}
	 * when none was required to be recycled (creating unnecessary
	 * {@link Connection} instances).
	 * <p>
	 * This enables the {@link Connection} recycle to be aware if the
	 * {@link Connection} contains a &quot;real&quot; {@link Connection}.
	 * 
	 * @return <code>true</code> if connection.
	 */
	boolean isRealConnection();

}