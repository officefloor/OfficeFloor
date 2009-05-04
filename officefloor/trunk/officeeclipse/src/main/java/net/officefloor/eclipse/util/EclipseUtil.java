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
package net.officefloor.eclipse.util;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import net.officefloor.eclipse.OfficeFloorPlugin;
import net.officefloor.eclipse.OfficeFloorPluginFailure;

/**
 * Utility methods for working in Eclipse.
 * 
 * @author Daniel
 */
public class EclipseUtil {

	/**
	 * Utility method to indicate if the input {@link String} is
	 * <code>null</code> or empty.
	 * 
	 * @param value
	 *            Value to check if blank.
	 * @return True if value is <code>null</code> or empty.
	 */
	public static boolean isBlank(String value) {
		return ((value == null) || (value.trim().length() == 0));
	}

	/**
	 * Obtains the {@link Class} by its name.
	 * 
	 * @param className
	 *            Fully qualified name of the class.
	 * @param superType
	 *            Type that the class must be a sub type.
	 * @return {@link Class}.
	 * @throws OfficeFloorPluginFailure
	 *             If unknown class or is not of superType.
	 */
	@SuppressWarnings("unchecked")
	public static <S> Class<S> obtainClass(String className, Class<S> superType)
			throws OfficeFloorPluginFailure {
		try {
			// Create the class
			Class clazz = Class.forName(className);

			// Ensure correct super type
			if (!(superType.isAssignableFrom(clazz))) {
				throw new OfficeFloorPluginFailure("Class '" + clazz.getName()
						+ "' must be a sub type of " + superType.getName());
			}

			// Return the class
			return (Class<S>) clazz;

		} catch (ClassNotFoundException ex) {
			throw new OfficeFloorPluginFailure(ex);
		}
	}

	/**
	 * Creates an instance of the class.
	 * 
	 * @param T
	 *            Type of class.
	 * @param clazz
	 *            Class to create an instance from.
	 * @return Instance of the class.
	 * @throws OfficeFloorPluginFailure
	 *             If fails to create an instance.
	 */
	public static <T> T createInstance(Class<T> clazz)
			throws OfficeFloorPluginFailure {
		try {
			return clazz.newInstance();
		} catch (InstantiationException ex) {
			throw new OfficeFloorPluginFailure(ex);
		} catch (IllegalAccessException ex) {
			throw new OfficeFloorPluginFailure(ex);
		}
	}

	/**
	 * Convenience method to create an instance of a class by its name.
	 * 
	 * @param className
	 *            Fully qualified name of class.
	 * @param superType
	 *            Type that the class of instance must be a sub type.
	 * @return Instance.
	 * @throws OfficeFloorPluginFailure
	 *             If fails to create the instance.
	 */
	public static <S> S createInstance(String className, Class<S> superType)
			throws OfficeFloorPluginFailure {
		return createInstance(obtainClass(className, superType));
	}

	/**
	 * Creates a {@link CoreException} from the input {@link Throwable}.
	 * 
	 * @param failure
	 *            {@link Throwable}.
	 * @return {@link CoreException}.
	 */
	public static CoreException createCoreException(Throwable failure) {

		// Ensure not already a core exception
		if (failure instanceof CoreException) {
			return (CoreException) failure;
		}

		// Create and return core exception for failure
		return new CoreException(new Status(IStatus.ERROR,
				OfficeFloorPlugin.PLUGIN_ID, failure.getMessage(), failure));
	}

	/**
	 * Access only via static methods.
	 */
	private EclipseUtil() {
	}

}