/*-
 * #%L
 * Web Security
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

package net.officefloor.web.security.type;

import java.lang.reflect.Array;

/**
 * Utility class that adapts objects for the delegate objects.
 * 
 * @author Daniel Sagenschneider
 */
public abstract interface AdaptFactory<A, O> {

	/**
	 * Adapts the object.
	 * 
	 * @param                <A> Required type.
	 * @param                <O> Original type.
	 * @param delegateObject Delegate object.
	 * @param adaptFactory   {@link AdaptFactory}.
	 * @return Adapted object.
	 */
	static <A, O> A adaptObject(O delegateObject, AdaptFactory<A, O> adaptFactory) {

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
	 * @param                    <A> Required type.
	 * @param                    <O> Original type.
	 * @param delegateArray      Array of delegate objects.
	 * @param adaptComponentType Adapt component type.
	 * @param adaptFactory       {@link AdaptFactory}.
	 * @return Adapted array.
	 */
	@SuppressWarnings("unchecked")
	static <A, O> A[] adaptArray(O[] delegateArray, Class<A> adaptComponentType, AdaptFactory<A, O> adaptFactory) {

		// Ensure have delegate array to adapt
		if (delegateArray == null) {
			return null;
		}

		// Adapt the array
		A[] adaptedArray = (A[]) Array.newInstance(adaptComponentType, delegateArray.length);
		for (int i = 0; i < delegateArray.length; i++) {
			O delegateObject = delegateArray[i];
			if (delegateObject != null) {
				adaptedArray[i] = adaptFactory.createAdaptedObject(delegateObject);
			}
		}

		// Return the adapted array
		return adaptedArray;
	}

	/**
	 * Creates the adapted object for the delegate object.
	 * 
	 * @param delegate Delegate object to wrap.
	 * @return Adapted object.
	 */
	A createAdaptedObject(O delegate);

}
