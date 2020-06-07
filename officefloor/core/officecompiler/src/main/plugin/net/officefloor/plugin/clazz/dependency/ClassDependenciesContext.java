package net.officefloor.plugin.clazz.dependency;

import net.officefloor.frame.internal.structure.Flow;

/**
 * Context for the {@link ClassDependencies}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ClassDependenciesContext {

	/**
	 * Adds a dependency.
	 * 
	 * @param qualifier   Qualifier. May be <code>null</code>.
	 * @param objectType  Dependency {@link Class}.
	 * @param annotations Annotations.
	 * @return Index of the dependency.
	 */
	int addDependency(String qualifier, Class<?> objectType, Object[] annotations);

	/**
	 * Adds a {@link Flow}.
	 * 
	 * @param flowName     Name of {@link Flow}.
	 * @param argumentType Argument {@link Class} to {@link Flow}.
	 * @param annotations  Annotations.
	 * @return Index of the {@link Flow}.
	 */
	int addFlow(String flowName, Class<?> argumentType, Object[] annotations);

}