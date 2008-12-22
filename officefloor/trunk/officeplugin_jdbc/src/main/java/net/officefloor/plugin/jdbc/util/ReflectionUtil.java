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
package net.officefloor.plugin.jdbc.util;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.LinkedList;
import java.util.List;

/**
 * Utility class for reflection operations.
 * 
 * @author Daniel
 */
public class ReflectionUtil {

	/**
	 * Obtains the {@link Setter} instances from the input bean {@link Class}.
	 * 
	 * @param clazz
	 *            Bean {@link Class}.
	 * @return {@link Setter} instances.
	 */
	@SuppressWarnings("unchecked")
	public static <B> Setter<B>[] getSetters(Class<B> clazz) {
		
		// Obtain the setters from the class
		List<Setter<B>> setters = new LinkedList<Setter<B>>();
		for (Method method : clazz.getMethods()) {

			// Ensure the method is a public setter with only one argument
			if (!Modifier.isPublic(method.getModifiers())) {
				continue;
			}
			if (!method.getName().startsWith("set")) {
				continue;
			}
			Class<?>[] parameterTypes = method.getParameterTypes();
			if (parameterTypes.length != 1) {
				continue;
			}
			
			// Create and add the setter
			Setter<B> setter = new Setter<B>(clazz, method);
			setters.add(setter);
		}
		
		// Return the setters
		return setters.toArray(new Setter[0]);
	}

	/**
	 * All access via static methods.
	 */
	private ReflectionUtil() {
	}
}
