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