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
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.impl.execute.flow.FlowMetaDataImpl;
import net.officefloor.frame.internal.configuration.ManagedFunctionReference;
import net.officefloor.frame.internal.construct.AssetManagerFactory;
import net.officefloor.frame.internal.construct.ManagedFunctionLocator;
import net.officefloor.frame.internal.structure.Asset;
import net.officefloor.frame.internal.structure.AssetManager;
import net.officefloor.frame.internal.structure.FlowInstigationStrategyEnum;
import net.officefloor.frame.internal.structure.FlowMetaData;
import net.officefloor.frame.internal.structure.ManagedFunctionMetaData;

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
	public static <T, E> T newInstance(Class<T> clazz, Class<E> expectedType,
			String creatingAssetName, AssetType assetType, String assetName,
			OfficeFloorIssues issues) {
		try {
			// Create the instance
			T instance = clazz.newInstance();

			// Ensure the instance is of the expected type
			if (!expectedType.isInstance(instance)) {
				// Indicate issue
				issues.addIssue(assetType, assetName,
						creatingAssetName + " class must implement "
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
	 * Obtains the {@link ManagedFunctionMetaData} reporting any failure to find to the
	 * {@link OfficeFloorIssues}.
	 * 
	 * @param taskNodeReference
	 *            {@link ManagedFunctionReference}.
	 * @param taskLocator
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
	 * @param isWorkNameRequired
	 *            Flags indicating if {@link Work} name is required.
	 *            <code>false</code> indicates that the
	 *            {@link ManagedFunctionLocator} has a default {@link Work} to
	 *            find {@link ManagedFunctionMetaData}.
	 * @return {@link ManagedFunctionMetaData} or <code>null</code> if not found with
	 *         issues reported to the {@link OfficeFloorIssues}.
	 */
	public static ManagedFunctionMetaData<?, ?, ?> getTaskMetaData(
			ManagedFunctionReference taskNodeReference,
			ManagedFunctionLocator taskLocator, OfficeFloorIssues issues,
			AssetType assetType, String assetName, String forItemDescription,
			boolean isWorkNameRequired) {

		// Obtain the work name
		String workName = taskNodeReference.getWorkName();

		// Determine if have the work name
		boolean isHaveWorkName = !ConstructUtil.isBlank(workName);
		if (isWorkNameRequired && (!isHaveWorkName)) {
			issues.addIssue(assetType, assetName, "No work name provided for "
					+ forItemDescription);
			return null; // must have the work name
		}

		// Obtain the task name
		String taskName = taskNodeReference.getTaskName();
		if (ConstructUtil.isBlank(taskName)) {
			issues.addIssue(assetType, assetName, "No task name provided for "
					+ forItemDescription);
			return null; // must have the task name
		}

		// Obtain the task meta-data
		ManagedFunctionMetaData<?, ?, ?> taskMetaData;
		if (isHaveWorkName) {
			taskMetaData = taskLocator.getTaskMetaData(workName, taskName);
		} else {
			taskMetaData = taskLocator.getTaskMetaData(taskName);
		}

		// Ensure have the task meta-data
		if (taskMetaData == null) {

			// Ensure have the name of the work being searched
			if (!isHaveWorkName) {
				workName = taskLocator.getDefaultWorkMetaData().getWorkName();
			}

			// Indicate issue as can not find meta-data
			issues.addIssue(assetType, assetName,
					"Can not find task meta-data (work=" + workName + ", task="
							+ taskName + ") for " + forItemDescription);
			return null; // must find the task meta-data
		}

		// Ensure correct argument type as parameter for the task
		Class<?> argumentType = taskNodeReference.getArgumentType();
		Class<?> parameterType = taskMetaData.getParameterType();
		if ((argumentType != null) && (parameterType != null)) {
			// Ensure argument may be passed as a parameter to the task
			if (!parameterType.isAssignableFrom(argumentType)) {

				// Ensure have the name of the work being searched
				if (!isHaveWorkName) {
					workName = taskLocator.getDefaultWorkMetaData()
							.getWorkName();
				}

				// Indicate issue that incompatible types
				issues.addIssue(assetType, assetName,
						"Argument is not compatible with task parameter (argument="
								+ argumentType.getName() + ", parameter="
								+ parameterType.getName() + ", work="
								+ workName + ", task=" + taskName + ") for "
								+ forItemDescription);
				return null; // must have compatible argument to parameter
			}
		}

		// Return the task meta-data
		return taskMetaData;
	}

	/**
	 * <p>
	 * Creates a new {@link FlowMetaData}.
	 * <p>
	 * This provides generic type safe creation.
	 * 
	 * @param <W>
	 *            {@link Work} type.
	 * @param instigationStrategy
	 *            {@link FlowInstigationStrategyEnum}.
	 * @param taskMetaData
	 *            {@link ManagedFunctionMetaData}.
	 * @param assetManagerFactory
	 *            {@link AssetManagerFactory}.
	 * @param assetType
	 *            {@link AssetType} requiring the {@link FlowMetaData}.
	 * @param assetName
	 *            Name of the {@link Asset} requiring the {@link FlowMetaData}.
	 * @param responsibility
	 *            Responsibility for the possible {@link AssetManager}.
	 * @param issues
	 *            {@link OfficeFloorIssues}.
	 * @return New {@link FlowMetaData}.
	 */
	public static <W extends Work> FlowMetaData<W> newFlowMetaData(
			FlowInstigationStrategyEnum instigationStrategy,
			ManagedFunctionMetaData<W, ?, ?> taskMetaData,
			AssetManagerFactory assetManagerFactory, AssetType assetType,
			String assetName, String responsibility, OfficeFloorIssues issues) {

		// Only create the asset manager if asynchronous flow
		AssetManager flowAssetManager = null;
		if (instigationStrategy == FlowInstigationStrategyEnum.ASYNCHRONOUS) {
			flowAssetManager = assetManagerFactory.createAssetManager(
					assetType, assetName, responsibility, issues);
		}

		// Create and return the flow meta-data
		return new FlowMetaDataImpl<W>(instigationStrategy, taskMetaData,
				flowAssetManager);
	}

	/**
	 * All access via static methods.
	 */
	private ConstructUtil() {
	}

}