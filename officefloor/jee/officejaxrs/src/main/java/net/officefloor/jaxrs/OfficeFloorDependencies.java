package net.officefloor.jaxrs;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import org.glassfish.jersey.internal.inject.Injectee;

import net.officefloor.frame.api.manage.OfficeFloor;

/**
 * Dependencies available from {@link OfficeFloor}.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeFloorDependencies {

	/**
	 * Dependencies.
	 */
	private final Map<AnnotatedElement, Object> dependencies = new HashMap<>();

	/**
	 * Registers a {@link Field} dependency.
	 * 
	 * @param field      {@link Field}.
	 * @param dependency Dependency.
	 */
	public void registerFieldDependency(Field field, Object dependency) {
		this.dependencies.put(field, dependency);
	}

	/**
	 * Obtains the dependency for the {@link Injectee}.
	 * 
	 * @param annotatedElement {@link AnnotatedElement}. Will gracefully handle
	 *                         <code>null</code>.
	 * @return Dependency or <code>null</code> if not matched.
	 */
	public Object getDependency(AnnotatedElement annotatedElement) {
		return annotatedElement == null ? null : this.dependencies.get(annotatedElement);
	}

}