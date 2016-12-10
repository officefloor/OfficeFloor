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
package net.officefloor.eclipse.util;

import java.util.List;

import net.officefloor.eclipse.OfficeFloorPlugin;
import net.officefloor.eclipse.common.editor.AbstractOfficeFloorEditor;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

/**
 * Utility methods for working in Eclipse.
 * 
 * @author Daniel Sagenschneider
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
	 * @param <S>
	 *            Super type.
	 * @param className
	 *            Fully qualified name of the class.
	 * @param superType
	 *            Type that the class must be a sub type.
	 * @param editor
	 *            {@link AbstractOfficeFloorEditor} to report issues.
	 * @return {@link Class} or <code>null</code> if could not obtain.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static <S> Class<S> obtainClass(String className,
			Class<S> superType, AbstractOfficeFloorEditor<?, ?> editor) {
		try {
			// Create the class
			Class clazz = Class.forName(className);

			// Ensure correct super type
			if (!(superType.isAssignableFrom(clazz))) {
				editor.messageError("Class '" + clazz.getName()
						+ "' must be a sub type of " + superType.getName());
			}

			// Return the class
			return (Class<S>) clazz;

		} catch (Throwable ex) {
			editor.messageError("Failed to obtain class " + className, ex);
			return null; // can not obtain class
		}
	}

	/**
	 * Creates an instance of the class.
	 * 
	 * @param <T>
	 *            Type.
	 * @param clazz
	 *            Class to create an instance from.
	 * @param editor
	 *            {@link AbstractOfficeFloorEditor} to report issues.
	 * @return Instance of the class or <code>null</code> if unable to
	 *         instantiate.
	 */
	public static <T> T createInstance(Class<T> clazz,
			AbstractOfficeFloorEditor<?, ?> editor) {
		try {
			return clazz.newInstance();
		} catch (Throwable ex) {
			editor.messageError("Failed to instantiate " + clazz.getName(), ex);
			return null;
		}
	}

	/**
	 * Convenience method to create an instance of a class by its name.
	 * 
	 * @param <S>
	 *            Super type.
	 * @param className
	 *            Fully qualified name of class.
	 * @param superType
	 *            Type that the class of instance must be a sub type.
	 * @param editor
	 *            {@link AbstractOfficeFloorEditor} to report issues.
	 * @return Instance or <code>null</code> if unable to instantiate.
	 */
	public static <S> S createInstance(String className, Class<S> superType,
			AbstractOfficeFloorEditor<?, ?> editor) {
		return createInstance(obtainClass(className, superType, editor), editor);
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
	 * Convenience method to add an item to a list. Will not add to list if
	 * <code>null</code>.
	 * 
	 * @param <L>
	 *            Element type of list.
	 * @param <I>
	 *            Element type.
	 * @param list
	 *            List.
	 * @param item
	 *            Item to be added to the list. Will not be added if
	 *            <code>null</code>.
	 */
	public static <L, I extends L> void addToList(List<L> list, I item) {
		if (item != null) {
			list.add(item);
		}
	}

	/**
	 * Access only via static methods.
	 */
	private EclipseUtil() {
	}

}