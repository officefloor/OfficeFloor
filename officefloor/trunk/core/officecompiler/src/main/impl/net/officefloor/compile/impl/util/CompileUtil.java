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
package net.officefloor.compile.impl.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import net.officefloor.compile.issues.CompilerIssues;
import net.officefloor.compile.issues.CompilerIssues.LocationType;
import net.officefloor.frame.api.build.OfficeFloorIssues.AssetType;
import net.officefloor.frame.spi.source.SourceContext;

/**
 * Utility methods to aid in compiling.
 * 
 * @author Daniel Sagenschneider
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
	public static <T> T[] toArray(Collection<T> list, Object[] type) {
		Object[] array = list.toArray(type);
		return (T[]) array;
	}

	/**
	 * Convenience method to create a sorted array from a {@link Collection}
	 * that passes compiler warnings for generic typed array along with handling
	 * <code>null</code> <code>compareTo</code> values.
	 * 
	 * @param <T>
	 *            Collection element type.
	 * @param collection
	 *            Collection to transform into a sorted array.
	 * @param type
	 *            Type of the array.
	 * @param extractor
	 *            {@link StringExtractor} to obtain compare key to sort.
	 * @return Collection as a sorted array.
	 */
	public static <T> T[] toSortedArray(Collection<T> collection,
			Object[] type, final StringExtractor<? super T> extractor) {

		// Create the array
		T[] array = toArray(collection, type);

		// Sort the array
		Arrays.sort(array, new Comparator<T>() {
			@Override
			public int compare(T a, T b) {
				// Obtain the string keys for comparing
				String aKey = extractor.toString(a);
				String bKey = extractor.toString(b);

				// Return the comparison (handling possible null key)
				return (aKey == null ? "" : aKey).compareTo(bKey);
			}
		});

		// Return the sorted array
		return array;
	}

	/**
	 * Obtains the {@link Class}.
	 * 
	 * @param <T>
	 *            Expected type.
	 * @param className
	 *            Fully qualified name of the {@link Class} to obtain.
	 * @param expectedType
	 *            Expected type of the {@link Class} to return.
	 * @param aliases
	 *            Map of alias name to {@link Class}. May be <code>null</code>.
	 * @param context
	 *            {@link SourceContext}.
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
			Class<T> expectedType, Map<String, Class<?>> aliases,
			SourceContext context, LocationType locationType, String location,
			AssetType assetType, String assetName, CompilerIssues issues) {
		try {

			// Obtain the class (first checking for an alias)
			Class<?> clazz = (aliases != null ? aliases.get(className) : null);
			if (clazz == null) {
				// Not alias, so load the class
				clazz = context.loadClass(className);
			}

			// Ensure class of expected type
			if (!expectedType.isAssignableFrom(clazz)) {
				// Not of expected type
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
	 * @param <T>
	 *            Type of instance.
	 * @param <E>
	 *            Expected type.
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
					"Failed to instantiate "
							+ (clazz != null ? clazz.getName() : null)
							+ " by default constructor", ex);
			return null; // no instance
		}
	}

	/**
	 * Convenience method to instantiate and instance of a {@link Class} from
	 * its fully qualified name.
	 * 
	 * @param <T>
	 *            Expected type.
	 * @param className
	 *            Fully qualified name of the {@link Class}.
	 * @param expectedType
	 *            Expected type that {@link Class} instance must be assignable.
	 * @param aliases
	 *            Map of alias name to {@link Class}. May be <code>null</code>.
	 * @param context
	 *            {@link SourceContext}.
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
	public static <T> T newInstance(String className, Class<T> expectedType,
			Map<String, Class<?>> aliases, SourceContext context,
			LocationType locationType, String location, AssetType assetType,
			String assetName, CompilerIssues issues) {

		// Obtain the class
		Class<? extends T> clazz = obtainClass(className, expectedType,
				aliases, context, locationType, location, assetType, assetName,
				issues);
		if (clazz == null) {
			return null; // must have class
		}

		// Create an instance of the class
		T instance = newInstance(clazz, expectedType, locationType, location,
				assetType, assetName, issues);
		if (instance == null) {
			return null; // must have instance
		}

		// Return the instance
		return instance;
	}

	/**
	 * All access via static methods.
	 */
	private CompileUtil() {
	}

}