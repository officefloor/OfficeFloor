package net.officefloor.plugin.clazz.dependency;

import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.Parameter;

/**
 * {@link Class} dependencies.
 * 
 * @author Daniel Sagenschneider
 */
public interface ClassDependencies {

	/**
	 * Creates the {@link ClassDependencyFactory} for a {@link Field}.
	 * 
	 * @param field     {@link Field}.
	 * @param qualifier Qualifier.
	 * @return {@link ClassDependencyFactory}.
	 * @throws Exception If fails to create.
	 */
	ClassDependencyFactory createClassDependencyFactory(Field field, String qualifier) throws Exception;

	/**
	 * Creates the {@link ClassDependencyFactory} for an {@link Executable}
	 * {@link Parameter}.
	 * 
	 * @param executable     {@link Executable}.
	 * @param parameterIndex Index of the {@link Parameter}.
	 * @param qualifier      Qualifier.
	 * @return {@link ClassDependencyFactory}.
	 * @throws Exception If fails to create.
	 */
	ClassDependencyFactory createClassDependencyFactory(Executable executable, int parameterIndex, String qualifier)
			throws Exception;

	/**
	 * Creates a {@link ClassDependencyFactory} of a particular {@link Class}.
	 * 
	 * @param dependencyName Name of dependency.
	 * @param dependencyType {@link Class} of the dependency.
	 * @param qualifier      Qualifier.
	 * @return {@link ClassDependencyFactory}.
	 * @throws Exception If fails to create.
	 */
	ClassDependencyFactory createClassDependencyFactory(String dependencyName, Class<?> dependencyType,
			String qualifier) throws Exception;

}