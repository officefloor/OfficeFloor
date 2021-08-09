/*-
 * #%L
 * OfficeCompiler
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

package net.officefloor.compile.impl.util;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

import net.officefloor.compile.internal.structure.Node;
import net.officefloor.compile.issues.CompilerIssues;
import net.officefloor.compile.issues.IssueCapture;
import net.officefloor.compile.issues.SourceIssues;
import net.officefloor.frame.api.source.SourceContext;

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
	 * @param value Value to check.
	 * @return <code>true</code> if blank.
	 */
	public static boolean isBlank(String value) {
		return ((value == null) || (value.trim().length() == 0));
	}

	/**
	 * Convenience method to compare two strings for sorting.
	 * 
	 * @param a First string.
	 * @param b Second string.
	 * @return Compare result for sorting.
	 */
	public static int sortCompare(String a, String b) {
		return String.CASE_INSENSITIVE_ORDER.compare(a == null ? "" : a, b == null ? "" : b);
	}

	/**
	 * Obtains the {@link Class}.
	 * 
	 * @param              <T> Expected type.
	 * @param className    Fully qualified name of the {@link Class} to obtain.
	 * @param expectedType Expected type of the {@link Class} to return.
	 * @param aliases      Map of alias name to {@link Class}. May be
	 *                     <code>null</code>.
	 * @param context      {@link SourceContext}.
	 * @param node         {@link Node}.
	 * @param issues       {@link CompilerIssues}.
	 * @return {@link Class}.
	 */
	@SuppressWarnings("unchecked")
	public static <T> Class<? extends T> obtainClass(String className, Class<T> expectedType,
			Map<String, Class<?>> aliases, SourceContext context, Node node, CompilerIssues issues) {
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
				issues.addIssue(node,
						"Must implement " + expectedType.getSimpleName() + " (class=" + clazz.getName() + ")");
				return null; // instance not of type
			}

			// Return the obtained class
			return (Class<? extends T>) clazz;

		} catch (Throwable ex) {
			// Indicate issue
			issues.addIssue(node, "Failed to obtain class " + className, ex);
			return null; // no class
		}
	}

	/**
	 * Instantiates a new instance of the input {@link Class} by its default
	 * constructor. If fails to instantiate, then reports issue via
	 * {@link CompilerIssues}.
	 * 
	 * @param              <T> Type of instance.
	 * @param              <E> Expected type.
	 * @param clazz        {@link Class} to instantiate.
	 * @param expectedType Expected type that is to be instantiated.
	 * @param node         {@link Node}.
	 * @param issues       {@link CompilerIssues}.
	 * @return New instance or <code>null</code> if not able to instantiate.
	 */
	public static <T, E> T newInstance(Class<T> clazz, Class<E> expectedType, Node node, CompilerIssues issues) {
		try {
			// Create the instance
			T instance = clazz.getDeclaredConstructor().newInstance();

			// Ensure the instance is of the expected type
			if (!expectedType.isInstance(instance)) {
				// Indicate issue
				issues.addIssue(node,
						"Must implement " + expectedType.getSimpleName() + " (class=" + clazz.getName() + ")");
				return null; // instance not of type
			}

			// Return the instance
			return instance;

		} catch (Throwable ex) {
			// Handle invocation target on constructor
			if (ex instanceof InvocationTargetException) {
				ex = ex.getCause();
			}

			// Indicate issue (catching exception from constructor)
			issues.addIssue(node,
					"Failed to instantiate " + (clazz != null ? clazz.getName() : null) + " by default constructor",
					ex);
			return null; // no instance
		}
	}

	/**
	 * Instantiates a new instance of the input {@link Class} by its default
	 * constructor. If fails to instantiate, then reports issue via
	 * {@link SourceIssues}.
	 * 
	 * @param              <T> Type of instance.
	 * @param              <E> Expected type.
	 * @param clazz        {@link Class} to instantiate.
	 * @param expectedType Expected type that is to be instantiated.
	 * @param issues       {@link SourceIssues}.
	 * @return New instance or <code>null</code> if not able to instantiate.
	 */
	public static <T, E> T newInstance(Class<T> clazz, Class<E> expectedType, SourceIssues issues) {
		try {
			// Create the instance
			T instance = clazz.getDeclaredConstructor().newInstance();

			// Ensure the instance is of the expected type
			if (!expectedType.isInstance(instance)) {
				// Indicate issue
				issues.addIssue("Must implement " + expectedType.getSimpleName() + " (class=" + clazz.getName() + ")");
				return null; // instance not of type
			}

			// Return the instance
			return instance;

		} catch (Throwable ex) {
			// Indicate issue (catching exception from constructor)
			issues.addIssue(
					"Failed to instantiate " + (clazz != null ? clazz.getName() : null) + " by default constructor",
					ex);
			return null; // no instance
		}
	}

	/**
	 * Convenience method to instantiate and instance of a {@link Class} from its
	 * fully qualified name.
	 * 
	 * @param              <T> Expected type.
	 * @param className    Fully qualified name of the {@link Class}.
	 * @param expectedType Expected type that {@link Class} instance must be
	 *                     assignable.
	 * @param aliases      Map of alias name to {@link Class}. May be
	 *                     <code>null</code>.
	 * @param context      {@link SourceContext}.
	 * @param node         {@link Node}.
	 * @param issues       {@link CompilerIssues}.
	 * @return New instance or <code>null</code> if not able to instantiate.
	 */
	public static <T> T newInstance(String className, Class<T> expectedType, Map<String, Class<?>> aliases,
			SourceContext context, Node node, CompilerIssues issues) {

		// Obtain the class
		Class<? extends T> clazz = obtainClass(className, expectedType, aliases, context, node, issues);
		if (clazz == null) {
			return null; // must have class
		}

		// Create an instance of the class
		T instance = newInstance(clazz, expectedType, node, issues);
		if (instance == null) {
			return null; // must have instance
		}

		// Return the instance
		return instance;
	}

	/**
	 * Convenience method to load a type.
	 * 
	 * @param                 <T> Type to be loaded.
	 * @param type            Type to be loaded.
	 * @param sourceClassName Source {@link Class} name for the type.
	 * @param issues          {@link CompilerIssues}.
	 * @param supplier        {@link Supplier} of the type.
	 * @return Type.
	 * @throws LoadTypeError If fails to load the type.
	 */
	public static <T> T loadType(Class<T> type, String sourceClassName, CompilerIssues issues, Supplier<T> supplier)
			throws LoadTypeError {

		// Load the type
		IssueCapture<T> capture = issues.captureIssues(supplier);

		// Ensure have type
		T loadedType = capture.getReturnValue();
		if (loadedType == null) {
			// Failed to load type
			throw new LoadTypeError(type, sourceClassName, capture.getCompilerIssues());
		}

		// Return the loaded type
		return loadedType;
	}

	/**
	 * Convenience method to load a listing of types.
	 * 
	 * @param                  <N> {@link Node} type within the listing.
	 * @param                  <T> Type returned from the {@link Node} instances
	 *                         within the listing.
	 * @param nodesMap         {@link Map} of {@link Node} instances by their names
	 *                         to load types from.
	 * @param sortKeyExtractor {@link Function} to extract the sort key (to enable
	 *                         deterministic order of loading the types).
	 * @param typeLoader       {@link Function} to load the type from the
	 *                         {@link Node}.
	 * @param arrayGenerator   {@link Function} to generate the array of types.
	 * @return Array of types or <code>null</code> with issues reported to the
	 *         {@link CompilerIssues}.
	 */
	public static <N, T> T[] loadTypes(Map<String, N> nodesMap, Function<N, String> sortKeyExtractor,
			Function<N, T> typeLoader, IntFunction<T[]> arrayGenerator) {
		return loadTypes(nodesMap.values().stream(), sortKeyExtractor, typeLoader, arrayGenerator);
	}

	/**
	 * Convenience method to load a listing of types.
	 * 
	 * @param                  <N> {@link Node} type within the listing.
	 * @param                  <T> Type returned from the {@link Node} instances
	 *                         within the listing.
	 * @param nodes            {@link Stream} of {@link Node} instances to load
	 *                         types from.
	 * @param sortKeyExtractor {@link Function} to extract the sort key (to enable
	 *                         deterministic order of loading the types).
	 * @param typeLoader       {@link Function} to load the type from the
	 *                         {@link Node}.
	 * @param arrayGenerator   {@link Function} to generate the array of types.
	 * @return Array of types or <code>null</code> with issues reported to the
	 *         {@link CompilerIssues}.
	 */
	public static <N, T> T[] loadTypes(Stream<N> nodes, Function<N, String> sortKeyExtractor, Function<N, T> typeLoader,
			IntFunction<T[]> arrayGenerator) {
		try {
			// Load the types
			return nodes.sorted((a, b) -> CompileUtil.sortCompare(sortKeyExtractor.apply(a), sortKeyExtractor.apply(b)))
					.map(typeLoader).filter((type) -> {
						if (type == null) {
							throw new LoadTypesException();
						}
						return true;
					}).toArray(arrayGenerator);

		} catch (LoadTypesException ex) {
			return null; // failed to load type, so no types
		}
	}

	/**
	 * Indicates failure in loading types.
	 */
	private static class LoadTypesException extends RuntimeException {

		/**
		 * Serial version UID.
		 */
		private static final long serialVersionUID = 1L;
	}

	/**
	 * Convenience method to source a listing of sub {@link Node} instances.
	 * 
	 * @param                  <N> {@link Node} type.
	 * @param nodesMap         {@link Map} of {@link Node} instances by their names
	 *                         to source.
	 * @param sortKeyExtractor {@link Function} to extract the sort key (to enable
	 *                         deterministic order of sourcing the sub {@link Node}
	 *                         instances).
	 * @param sourcer          {@link Predicate} to source the sub {@link Node}.
	 * @return <code>true</code> if all sub {@link Node} instances sourced.
	 *         Otherwise, <code>false</code> with issue reported to the
	 *         {@link CompilerIssues}.
	 */
	public static <N> boolean source(Map<String, N> nodesMap, Function<N, String> sortKeyExtractor,
			Predicate<N> sourcer) {
		return nodesMap.values().stream()
				.sorted((a, b) -> CompileUtil.sortCompare(sortKeyExtractor.apply(a), sortKeyExtractor.apply(b)))
				.allMatch(sourcer);
	}

	/**
	 * All access via static methods.
	 */
	private CompileUtil() {
	}

}
