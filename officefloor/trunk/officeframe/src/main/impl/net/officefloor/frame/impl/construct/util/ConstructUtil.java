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
package net.officefloor.frame.impl.construct.util;

import java.lang.reflect.Array;
import java.util.Map;

import net.officefloor.frame.api.build.OfficeFloorIssues;
import net.officefloor.frame.api.build.OfficeFloorIssues.AssetType;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.internal.structure.Asset;

/**
 * Utility class to aid in construction of the {@link OfficeFloor}.
 * 
 * @author Daniel
 */
public class ConstructUtil {

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
	 * Constructs a new instance of the input {@link Class} by its default
	 * constructor. If fails to instantiate, then reports issue via
	 * {@link OfficeFloorIssues}.
	 * 
	 * @param clazz
	 *            {@link Class} to instantiate.
	 * @param expectedType
	 *            Expected type that is to be instantiated.
	 * @param creatingAssetName
	 *            Name of the {@link Asset} being created.
	 * @param assetType
	 *            {@link AssetType}.
	 * @param assetName
	 *            Name of {@link AssetType}.
	 * @param issues
	 *            {@link OfficeFloorIssues}.
	 * @return New instance or <code>null</code> if not able to instantiate.
	 */
	public static <T, E> T newInstance(Class<T> clazz, Class<E> expectedType,
			String creatingAssetName, AssetType assetType, String assetName,
			OfficeFloorIssues issues) {
		try {
			// Create the instance
			T instance = clazz.newInstance();

			// Ensure the instance is of the expected type
			if (!expectedType.isInstance(instance)) {
				// Indicate issue
				issues.addIssue(assetType, assetName, creatingAssetName
						+ " class must implement "
						+ expectedType.getSimpleName() + " (class="
						+ clazz.getName() + ")");
				return null; // instance not of type
			}

			// Return the instance
			return instance;

		} catch (Throwable ex) {
			// Indicate issue (catching exception from constructor)
			issues.addIssue(assetType, assetName, "Failed to instantiate "
					+ clazz.getName(), ex);
			return null; // no instance
		}
	}

	/**
	 * Transforms the input {@link Map} into an array by the <code>type</code>
	 * from indexes of the {@link Map}.
	 * 
	 * @param map
	 *            {@link Map} to be transformed into an array.
	 * @param type
	 *            Type of the array.
	 * @return {@link Map} contents as an array.
	 */
	@SuppressWarnings("unchecked")
	public static <O> O[] toArray(Map<Integer, ? extends O> map, O[] type) {

		// Obtain the array size
		int arraySize = -1;
		for (Integer key : map.keySet()) {
			int index = key.intValue();
			if (index > arraySize) {
				arraySize = index;
			}
		}
		arraySize += 1; // size is one bigger than max index

		// Instantiate the array
		O[] array = (O[]) Array.newInstance(type.getClass().getComponentType(),
				arraySize);

		// Load the array from the map
		for (Integer key : map.keySet()) {
			O o = map.get(key);
			int index = key.intValue();
			array[index] = o;
		}

		// Return the array
		return array;
	}

	/**
	 * All access via static methods.
	 */
	private ConstructUtil() {
	}
}
