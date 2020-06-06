package net.officefloor.plugin.managedobject.clazz;

import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.util.InvokedProcessServicer;

/**
 * <p>
 * Loads {@link Class} via {@link ClassManagedObjectSource} for stand alone use.
 * <p>
 * This is typically for unit testing of the {@link Class} with mock injections.
 * 
 * @author Daniel Sagenschneider
 */
public class ClassStandAlone {

	/**
	 * Registers an unqualified dependency for concrete type.
	 * 
	 * @param dependency Dependency.
	 */
	public void registerDependency(Object dependency) {
		this.registerDependency((String) null, dependency);
	}

	/**
	 * Registers a qualified dependency for concrete type.
	 * 
	 * @param qualifier  Qualifier.
	 * @param dependency Dependency.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void registerDependency(String qualifier, Object dependency) {
		this.registerDependency(qualifier, (Class) dependency.getClass(), dependency);
	}

	/**
	 * Registers an unqualified dependency for a dependency type.
	 * 
	 * @param <T>        Dependency type.
	 * @param <I>        Implementation type.
	 * @param type       Dependency type.
	 * @param dependency Implementing dependency.
	 */
	public <T, I extends T> void registerDependency(Class<T> type, I dependency) {
		this.registerDependency(null, type, dependency);
	}

	/**
	 * Registers a dependency for a dependency type.
	 * 
	 * @param <T>        Dependency type.
	 * @param <I>        Implementation type.
	 * @param qualifier  Qualifier.
	 * @param type       Dependency type.
	 * @param dependency Implementing dependency.
	 */
	public <T, I extends T> void registerDependency(String qualifier, Class<T> type, I dependency) {

	}

	/**
	 * Registers an invoked {@link Flow} (process).
	 * 
	 * @param flowName Name of {@link Flow}.
	 * @param servicer {@link InvokedProcessServicer}.
	 */
	public void registerFlow(String flowName, InvokedProcessServicer servicer) {

	}

	/**
	 * Instantiates the objects and injects the dependencies.
	 * 
	 * @param <T>   Object type.
	 * @param clazz Object {@link Class}.
	 * @return Instantiated object with dependencies injected.
	 */
	public <T> T create(Class<T> clazz) {
		return null;
	}
}