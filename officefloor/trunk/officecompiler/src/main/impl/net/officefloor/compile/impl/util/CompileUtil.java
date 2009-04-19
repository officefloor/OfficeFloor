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
package net.officefloor.compile.impl.util;

import java.util.List;

import net.officefloor.compile.issues.CompilerIssues;
import net.officefloor.compile.issues.CompilerIssues.LocationType;
import net.officefloor.frame.api.build.OfficeFloorIssues.AssetType;

/**
 * Utility methods to aid in compiling.
 * 
 * @author Daniel
 */
public class CompileUtil {

	/**
	 * Indicates whether the input {@link String} is either <code>null</code> or
	 * empty.
	 * 
	 * @param value
	 *            Value to check.
	 * @return <code>true</code> if blank.
	 */
	public static boolean isBlank(String value) {
		return ((value == null) || (value.trim().length() == 0));
	}

	/**
	 * Convenience method for {@link List#toArray(Object[])} to pass compiler
	 * warnings for generic typed array.
	 * 
	 * @param list
	 *            List to transform into an array.
	 * @param type
	 *            Type of the array.
	 * @return List as an array.
	 */
	@SuppressWarnings("unchecked")
	public static <T> T[] toArray(List<T> list, Object[] type) {
		Object[] array = list.toArray(type);
		return (T[]) array;
	}

	/**
	 * Obtains the {@link Class}.
	 * 
	 * @param className
	 *            Fully qualified name of the {@link Class} to obtain.
	 * @param expectedType
	 *            Expected type of the {@link Class} to return.
	 * @param classLoader
	 *            {@link ClassLoader}.
	 * @param locationType
	 *            {@link LocationType}.
	 * @param location
	 *            Location.
	 * @param assetType
	 *            {@link AssetType}.
	 * @param assetName
	 *            Name of asset.
	 * @param issues
	 *            {@link CompilerIssues}.
	 * @return {@link Class}.
	 */
	@SuppressWarnings("unchecked")
	public static <T> Class<? extends T> obtainClass(String className,
			Class<T> expectedType, ClassLoader classLoader,
			LocationType locationType, String location, AssetType assetType,
			String assetName, CompilerIssues issues) {
		try {
			// Load the class
			Class<?> clazz = classLoader.loadClass(className);

			// Ensure class of expected type
			if (!expectedType.isAssignableFrom(clazz)) {
				issues.addIssue(locationType, location, assetType, assetName,
						"Must implement " + expectedType.getSimpleName()
								+ " (class=" + clazz.getName() + ")");
				return null; // instance not of type
			}

			// Return the obtained class
			return (Class<? extends T>) clazz;

		} catch (Throwable ex) {
			// Indicate issue
			issues.addIssue(locationType, location, assetType, assetName,
					"Failed to obtain class " + className, ex);
			return null; // no class
		}
	}

	/**
	 * Instantiates a new instance of the input {@link Class} by its default
	 * constructor. If fails to instantiate, then reports issue via
	 * {@link CompilerIssues}.
	 * 
	 * @param clazz
	 *            {@link Class} to instantiate.
	 * @param expectedType
	 *            Expected type that is to be instantiated.
	 * @param locationType
	 *            {@link LocationType}.
	 * @param location
	 *            Location.
	 * @param assetType
	 *            {@link AssetType}.
	 * @param assetName
	 *            Name of asset.
	 * @param issues
	 *            {@link CompilerIssues}.
	 * @return New instance or <code>null</code> if not able to instantiate.
	 */
	public static <T, E> T newInstance(Class<T> clazz, Class<E> expectedType,
			LocationType locationType, String location, AssetType assetType,
			String assetName, CompilerIssues issues) {
		try {
			// Create the instance
			T instance = clazz.newInstance();

			// Ensure the instance is of the expected type
			if (!expectedType.isInstance(instance)) {
				// Indicate issue
				issues.addIssue(locationType, location, assetType, assetName,
						"Must implement " + expectedType.getSimpleName()
								+ " (class=" + clazz.getName() + ")");
				return null; // instance not of type
			}

			// Return the instance
			return instance;

		} catch (Throwable ex) {
			// Indicate issue (catching exception from constructor)
			issues.addIssue(locationType, location, assetType, assetName,
					"Failed to instantiate " + clazz.getName()
							+ " by default constructor", ex);
			return null; // no instance
		}
	}

	/**
	 * All access via static methods.
	 */
	private CompileUtil() {
	}

}