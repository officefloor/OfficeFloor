/*-
 * #%L
 * OfficeCompiler
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package net.officefloor.compile.type;

import java.lang.reflect.Array;
import java.util.LinkedList;
import java.util.List;

/**
 * <p>
 * Identifies types that may be annotated.
 * <p>
 * Also, provides additional helper methods on the annotated type.
 * 
 * @author Daniel Sagenschneider
 */
public interface AnnotatedType {

	/**
	 * Obtains the annotations.
	 * 
	 * @return Annotations.
	 */
	Object[] getAnnotations();

	/**
	 * Obtains the first annotation matching the type.
	 * 
	 * @param <A>  Type of annotation.
	 * @param type Type of annotation.
	 * @return First annotation matching the type.
	 */
	@SuppressWarnings("unchecked")
	default <A> A getAnnotation(Class<A> type) {

		// Obtain the annotations
		Object[] annotations = this.getAnnotations();
		if (annotations == null) {
			return null;
		}

		// First pass to find by exact type
		for (Object annotation : annotations) {
			Class<?> annotationClass = annotation.getClass();

			// Determine if exact match
			if (annotationClass.equals(type)) {
				return (A) annotation; // found
			}
		}

		// Next pass to match by sub type
		for (Object annotation : annotations) {
			Class<?> annotationClass = annotation.getClass();

			// Determine if child match
			// (Note: annotations can not be extended)
			if (type.isAssignableFrom(annotationClass)) {
				return (A) annotation;
			}
		}

		// As here, no match for type
		return null;
	}

	/**
	 * Obtain all annotations matching the type.
	 * 
	 * @param <A>  Type of annotation.
	 * @param type Type of annotation.
	 * @return All annotations matching the type. May be empty array if no matches.
	 */
	@SuppressWarnings("unchecked")
	default <A> A[] getAnnotations(Class<A> type) {

		// Obtain the annotations
		Object[] annotations = this.getAnnotations();
		if (annotations == null) {
			return (A[]) Array.newInstance(type, 0);
		}

		// Obtain all the annotations
		List<A> matchingAnnotations = new LinkedList<>();
		for (Object annotation : annotations) {
			Class<?> annotationClass = annotation.getClass();

			// Determine if child match
			if (type.isAssignableFrom(annotationClass)) {
				matchingAnnotations.add((A) annotation);
			}
		}

		// Return the list of annotations
		return matchingAnnotations.toArray((A[]) Array.newInstance(type, matchingAnnotations.size()));
	}

}
