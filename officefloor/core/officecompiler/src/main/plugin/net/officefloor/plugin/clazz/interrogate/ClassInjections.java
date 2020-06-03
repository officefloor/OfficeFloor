package net.officefloor.plugin.clazz.interrogate;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.officefloor.frame.api.source.SourceContext;

/**
 * {@link Class} injections.
 * 
 * @author Daniel Sagenschneider
 */
public class ClassInjections implements ClassInjectionInterrogatorContext {

	/**
	 * Object {@link Class}.
	 */
	private final Class<?> objectClass;

	/**
	 * Dependency injection {@link Field} instances.
	 */
	private final Set<Field> fields = new HashSet<>();

	/**
	 * Dependency injection {@link Method} instances.
	 */
	private final Set<Method> methods = new HashSet<>();

	/**
	 * Post construct {@link Method} instances.
	 */
	private final Set<Method> postConstructs = new HashSet<>();

	/**
	 * {@link AnnotatedElement}.
	 */
	private AnnotatedElement annotatedElement = null;

	/**
	 * Instantiate.
	 * 
	 * @param objectClass   Object {@link Class}.
	 * @param sourceContext {@link SourceContext}.
	 * @throws Exception If fails to load injections.
	 */
	public ClassInjections(Class<?> objectClass, SourceContext sourceContext) throws Exception {
		this.objectClass = objectClass;

		// Interrogate the fields and methods
		for (ClassInjectionInterrogator interrogator : sourceContext
				.loadServices(ClassInjectionInterrogatorServiceFactory.class, null)) {
			Class<?> clazz = this.objectClass;
			while (clazz != null) {

				// Load all field injections
				for (Field field : clazz.getDeclaredFields()) {
					this.annotatedElement = field;
					interrogator.interrogate(this);
				}

				// Load all method injections
				for (Method method : clazz.getDeclaredMethods()) {
					this.annotatedElement = method;
					interrogator.interrogate(this);
				}

				// Interrogate parent class
				clazz = clazz.getSuperclass();
			}
		}
	}

	/**
	 * Obtains the {@link Field} instances for injection.
	 * 
	 * @return {@link Field} instances for injection.
	 */
	public List<Field> getInjectionFields() {
		List<Field> fields = new ArrayList<>(this.fields);
		Collections.sort(fields, (a, b) -> String.CASE_INSENSITIVE_ORDER.compare(a.getName(), b.getName()));
		return fields;
	}

	/**
	 * Obtains the {@link Method} instances for injection.
	 * 
	 * @return {@link Method} instances for injection.
	 */
	public List<Method> getInjectionMethods() {
		List<Method> methods = new ArrayList<>(this.methods);
		Collections.sort(methods, (a, b) -> String.CASE_INSENSITIVE_ORDER.compare(a.getName(), b.getName()));
		return methods;
	}

	/**
	 * Obtains the post construct {@link Method} instances.
	 * 
	 * @return Post construct {@link Method} instances.
	 */
	public List<Method> getPostConstructMethods() {
		List<Method> methods = new ArrayList<>(this.postConstructs);
		Collections.sort(methods, (a, b) -> String.CASE_INSENSITIVE_ORDER.compare(a.getName(), b.getName()));
		return methods;
	}

	/*
	 * ===================== ClassInjectionInterrogatorContext ===================
	 */

	@Override
	public AnnotatedElement getAnnotatedElement() {
		return this.annotatedElement;
	}

	@Override
	public Class<?> getObjectClass() {
		return this.objectClass;
	}

	@Override
	public void registerInjectionPoint(AnnotatedElement member) {
		if (member instanceof Field) {
			this.fields.add((Field) member);
		} else if (member instanceof Method) {
			this.methods.add((Method) member);
		} else {
			throw new IllegalArgumentException("Invalid injection point type " + member.getClass().getName());
		}
	}

	@Override
	public void registerPostConstruct(Method method) {
		this.postConstructs.add(method);
	}

}