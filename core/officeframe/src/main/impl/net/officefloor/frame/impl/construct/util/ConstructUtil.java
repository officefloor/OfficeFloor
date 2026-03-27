/*-
 * #%L
 * OfficeFrame
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package net.officefloor.frame.impl.construct.util;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;

import net.officefloor.frame.api.build.OfficeFloorIssues;
import net.officefloor.frame.api.build.OfficeFloorIssues.AssetType;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.impl.execute.flow.FlowMetaDataImpl;
import net.officefloor.frame.internal.configuration.ManagedFunctionReference;
import net.officefloor.frame.internal.structure.Asset;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.FlowMetaData;
import net.officefloor.frame.internal.structure.ManagedFunctionLocator;
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
	 * @param value Value to check.
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
	 * @param                   <T> Type of object to instantiate.
	 * @param                   <E> Expected type that object is assignable.
	 * @param clazz             {@link Class} to instantiate.
	 * @param expectedType      Expected type that is to be instantiated.
	 * @param creatingAssetName Name of the {@link Asset} being created.
	 * @param assetType         {@link AssetType}.
	 * @param assetName         Name of {@link AssetType}.
	 * @param issues            {@link OfficeFloorIssues}.
	 * @return New instance or <code>null</code> if not able to instantiate.
	 */
	public static <T, E> T newInstance(Class<T> clazz, Class<E> expectedType, String creatingAssetName,
			AssetType assetType, String assetName, OfficeFloorIssues issues) {
		try {
			// Create the instance
			T instance = clazz.getDeclaredConstructor().newInstance();

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
			// Handle invocation exception
			if (ex instanceof InvocationTargetException) {
				ex = ex.getCause();
			}

			// Indicate issue (catching exception from constructor)
			issues.addIssue(assetType, assetName, "Failed to instantiate " + clazz.getName(), ex);
			return null; // no instance
		}
	}

	/**
	 * Transforms the input {@link Map} into an array by the <code>type</code> from
	 * indexes of the {@link Map}.
	 * 
	 * @param      <O> Element type.
	 * @param map  {@link Map} to be transformed into an array.
	 * @param type Type of the array.
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
	 * @param      <T> Element type.
	 * @param list List to transform into an array.
	 * @param type Type of the array.
	 * @return List as an array.
	 */
	@SuppressWarnings("unchecked")
	public static <T> T[] toArray(List<T> list, Object[] type) {
		Object[] array = list.toArray(type);
		return (T[]) array;
	}

	/**
	 * Obtains the {@link ManagedFunctionMetaData} reporting any failure to find to
	 * the {@link OfficeFloorIssues}.
	 * 
	 * @param functionReference  {@link ManagedFunctionReference}.
	 * @param functionLocator    {@link ManagedFunctionLocator} to use to locate the
	 *                           {@link ManagedFunctionMetaData}.
	 * @param issues             {@link OfficeFloorIssues}.
	 * @param assetType          {@link AssetType} for reporting issues.
	 * @param assetName          {@link Asset} name for reporting issues.
	 * @param forItemDescription Description after &quot;for&quot; indicating what
	 *                           the {@link ManagedFunctionMetaData} is for.
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

			// Ensure argument may be passed as a parameter to the function
			if (!parameterType.isAssignableFrom(argumentType)) {

				// Not match, attempt to determine if need boxing
				Class<?> boxedArgumentType = null;
				if (argumentType.equals(boolean.class)) {
					boxedArgumentType = Boolean.class;
				} else if (argumentType.equals(byte.class)) {
					boxedArgumentType = Byte.class;
				} else if (argumentType.equals(short.class)) {
					boxedArgumentType = Short.class;
				} else if (argumentType.equals(char.class)) {
					boxedArgumentType = Character.class;
				} else if (argumentType.equals(int.class)) {
					boxedArgumentType = Integer.class;
				} else if (argumentType.equals(long.class)) {
					boxedArgumentType = Long.class;
				} else if (argumentType.equals(float.class)) {
					boxedArgumentType = Float.class;
				} else if (argumentType.equals(double.class)) {
					boxedArgumentType = Double.class;
				}

				// Ensure boxed argument may be passed
				if ((boxedArgumentType == null) || (!parameterType.isAssignableFrom(boxedArgumentType))) {
					// Invalid argument
					issues.addIssue(assetType, assetName,
							"Argument is not compatible with function parameter (argument=" + argumentType.getName()
									+ ", parameter=" + parameterType.getName() + ", function=" + functionName + ") for "
									+ forItemDescription);
					return null; // must have compatible argument to parameter
				}
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
	 * @param functionMetaData   {@link ManagedFunctionMetaData}.
	 * @param isSpawnThreadState <code>true</code> for {@link Flow} to spawn a
	 *                           {@link ThreadState}.
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
