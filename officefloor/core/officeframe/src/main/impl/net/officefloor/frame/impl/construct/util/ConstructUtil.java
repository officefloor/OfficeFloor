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
package net.officefloor.frame.impl.construct.util;

import java.lang.reflect.Array;
import java.util.List;
import java.util.Map;

import net.officefloor.frame.api.build.OfficeFloorIssues;
import net.officefloor.frame.api.build.OfficeFloorIssues.AssetType;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.impl.execute.flow.FlowMetaDataImpl;
import net.officefloor.frame.internal.configuration.ManagedFunctionReference;
import net.officefloor.frame.internal.construct.ManagedFunctionLocator;
import net.officefloor.frame.internal.structure.Asset;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.FlowMetaData;
import net.officefloor.frame.internal.structure.ManagedFunctionMetaData;
import net.officefloor.frame.internal.structure.ThreadState;

/**
 * Utility class to aid in construction of the {@link OfficeFloor}.
 * 
 * @author Daniel Sagenschneider
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
	 * @param <T>
	 *            Type of object to instantiate.
	 * @param <E>
	 *            Expected type that object is assignable.
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
	public static <T, E> T newInstance(Class<T> clazz, Class<E> expectedType, String creatingAssetName,
			AssetType assetType, String assetName, OfficeFloorIssues issues) {
		try {
			// Create the instance
			T instance = clazz.newInstance();

			// Ensure the instance is of the expected type
			if (!expectedType.isInstance(instance)) {
				// Indicate issue
				issues.addIssue(assetType, assetName, creatingAssetName + " class must implement "
						+ expectedType.getSimpleName() + " (class=" + clazz.getName() + ")");
				return null; // instance not of type
			}

			// Return the instance
			return instance;

		} catch (Throwable ex) {
			// Indicate issue (catching exception from constructor)
			issues.addIssue(assetType, assetName, "Failed to instantiate " + clazz.getName(), ex);
			return null; // no instance
		}
	}

	/**
	 * Transforms the input {@link Map} into an array by the <code>type</code>
	 * from indexes of the {@link Map}.
	 * 
	 * @param <O>
	 *            Element type.
	 * @param map
	 *            {@link Map} to be transformed into an array.
	 * @param type
	 *            Type of the array.
	 * @return {@link Map} contents as an array.
	 */
	@SuppressWarnings("unchecked")
	public static <O> O[] toArray(Map<Integer, ? extends O> map, Object[] type) {

		// Obtain the array size (by finding max index)
		int arraySize = -1;
		for (Integer key : map.keySet()) {
			int index = key.intValue();
			if (index > arraySize) {
				arraySize = index;
			}
		}
		arraySize += 1; // size is one bigger than max index

		// Instantiate the array
		O[] array = (O[]) Array.newInstance(type.getClass().getComponentType(), arraySize);

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
	 * Convenience method for {@link List#toArray(Object[])} to pass compiler
	 * warnings for generic typed array.
	 * 
	 * @param <T>
	 *            Element type.
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
	 * Obtains the {@link ManagedFunctionMetaData} reporting any failure to find
	 * to the {@link OfficeFloorIssues}.
	 * 
	 * @param functionReference
	 *            {@link ManagedFunctionReference}.
	 * @param functionLocator
	 *            {@link ManagedFunctionLocator} to use to locate the
	 *            {@link ManagedFunctionMetaData}.
	 * @param issues
	 *            {@link OfficeFloorIssues}.
	 * @param assetType
	 *            {@link AssetType} for reporting issues.
	 * @param assetName
	 *            {@link Asset} name for reporting issues.
	 * @param forItemDescription
	 *            Description after &quot;for&quot; indicating what the
	 *            {@link ManagedFunctionMetaData} is for.
	 * @return {@link ManagedFunctionMetaData} or <code>null</code> if not found
	 *         with issues reported to the {@link OfficeFloorIssues}.
	 */
	public static ManagedFunctionMetaData<?, ?> getFunctionMetaData(ManagedFunctionReference functionReference,
			ManagedFunctionLocator functionLocator, OfficeFloorIssues issues, AssetType assetType, String assetName,
			String forItemDescription) {

		// Obtain the function name
		String functionName = functionReference.getFunctionName();
		if (ConstructUtil.isBlank(functionName)) {
			issues.addIssue(assetType, assetName, "No function name provided for " + forItemDescription);
			return null; // must have the function name
		}

		// Obtain the function meta-data
		ManagedFunctionMetaData<?, ?> functionMetaData = functionLocator.getManagedFunctionMetaData(functionName);

		// Ensure have the function meta-data
		if (functionMetaData == null) {
			issues.addIssue(assetType, assetName,
					"Can not find function meta-data " + functionName + " for " + forItemDescription);
			return null; // must find the task meta-data
		}

		// Ensure correct argument type as parameter for the function
		Class<?> argumentType = functionReference.getArgumentType();
		Class<?> parameterType = functionMetaData.getParameterType();
		if ((argumentType != null) && (parameterType != null)) {
			// Ensure argument may be passed as a parameter to the task
			if (!parameterType.isAssignableFrom(argumentType)) {
				issues.addIssue(assetType, assetName,
						"Argument is not compatible with function parameter (argument=" + argumentType.getName()
								+ ", parameter=" + parameterType.getName() + ", function=" + functionName + ") for "
								+ forItemDescription);
				return null; // must have compatible argument to parameter
			}
		}

		// Return the function meta-data
		return functionMetaData;
	}

	/**
	 * <p>
	 * Creates a new {@link FlowMetaData}.
	 * <p>
	 * This provides generic type safe creation.
	 * 
	 * @param functionMetaData
	 *            {@link ManagedFunctionMetaData}.
	 * @param isSpawnThreadState
	 *            <code>true</code> for {@link Flow} to spawn a
	 *            {@link ThreadState}.
	 * @return New {@link FlowMetaData}.
	 */
	public static FlowMetaData newFlowMetaData(ManagedFunctionMetaData<?, ?> functionMetaData,
			boolean isSpawnThreadState) {
		// Create and return the flow meta-data
		return new FlowMetaDataImpl(isSpawnThreadState, functionMetaData);
	}

	/**
	 * All access via static methods.
	 */
	private ConstructUtil() {
	}

}