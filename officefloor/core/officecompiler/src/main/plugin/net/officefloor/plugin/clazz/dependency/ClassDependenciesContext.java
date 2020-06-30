package net.officefloor.plugin.clazz.dependency;

import net.officefloor.frame.api.escalate.Escalation;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.managedobject.ManagedObject;
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
	 * @param dependencyName Name of the dependency.
	 * @param qualifier      Qualifier. May be <code>null</code>.
	 * @param objectType     Dependency {@link Class}.
	 * @param annotations    Annotations.
	 * @return {@link ClassItemIndex} of the dependency.
	 */
	ClassItemIndex addDependency(String dependencyName, String qualifier, Class<?> objectType, Object[] annotations);

	/**
	 * Adds a {@link Flow}.
	 * 
	 * @param flowName     Name of {@link Flow}.
	 * @param argumentType Argument {@link Class} to {@link Flow}.
	 * @param annotations  Annotations.
	 * @return {@link ClassItemIndex} of the {@link Flow}.
	 */
	ClassItemIndex addFlow(String flowName, Class<?> argumentType, Object[] annotations);

	/**
	 * Adds an {@link Escalation}.
	 * 
	 * @param escalationType Type of {@link Escalation}.
	 */
	void addEscalation(Class<? extends Throwable> escalationType);

	/**
	 * Adds an annotation to the {@link ManagedFunction} / {@link ManagedObject}
	 * requiring the dependency.
	 * 
	 * @param annotation Annotation.
	 */
	void addAnnotation(Object annotation);
}