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
package net.officefloor.plugin.web.http.security;

import java.lang.reflect.Array;

/**
 * Utility class that adapts objects for the delegate objects.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AdaptFactory<A, O> {

	/**
	 * Adapts the object.
	 * 
	 * @param delegateObject
	 *            Delegate object.
	 * @param adaptFactory
	 *            {@link AdaptFactory}.
	 * @return Adapted object.
	 */
	public static <A, O> A adaptObject(O delegateObject,
			AdaptFactory<A, O> adaptFactory) {

		// Ensure have delegate object to adapt
		if (delegateObject == null) {
			return null;
		}

		// Adapt the object
		A adaptedObject = adaptFactory.createAdaptedObject(delegateObject);

		// Return the adapted object
		return adaptedObject;
	}

	/**
	 * Adapts the array.
	 * 
	 * @param delegateArray
	 *            Array of delegate objects.
	 * @param adaptComponentType
	 *            Adapt component type.
	 * @param adaptFactory
	 *            {@link AdaptFactory}.
	 * @return Adapted array.
	 */
	@SuppressWarnings("unchecked")
	public static <A, O> A[] adaptArray(O[] delegateArray,
			Class<A> adaptComponentType, AdaptFactory<A, O> adaptFactory) {

		// Ensure have delegate array to adapt
		if (delegateArray == null) {
			return null;
		}

		// Adapt the array
		A[] adaptedArray = (A[]) Array.newInstance(adaptComponentType,
				delegateArray.length);
		for (int i = 0; i < delegateArray.length; i++) {
			O delegateObject = delegateArray[i];
			if (delegateObject != null) {
				adaptedArray[i] = adaptFactory
						.createAdaptedObject(delegateObject);
			}
		}

		// Return the adapted array
		return adaptedArray;
	}

	/**
	 * Creates the adapted object for the delegate object.
	 * 
	 * @param delegate
	 *            Delegate object to wrap.
	 * @return Adapted object.
	 */
	public abstract A createAdaptedObject(O delegate);

}