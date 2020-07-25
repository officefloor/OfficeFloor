package net.officefloor.plugin.clazz.dependency;

/**
 * Context for the {@link ClassDependencies}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ClassDependenciesContext extends ClassDependenciesFlowContext {

	/**
	 * Adds a dependency.
	 * 
	 * @param dependencyName Name of the dependency.
	 * @param qualifier      Qualifier. May be <code>null</code>.
	 * @param objectType     Dependency {@link Class}.
	 * @param annotations    Annotations.
	 * @return {@link ClassItemIndex} of the dependency.
	 */
	ClassItemIndex addDependency(String dependencyName, String qualifier, Class<?> objectType, Object[] annotations);

}