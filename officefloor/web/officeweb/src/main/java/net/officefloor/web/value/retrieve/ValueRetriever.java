package net.officefloor.web.value.retrieve;

import java.lang.annotation.Annotation;

import net.officefloor.server.http.HttpException;

/**
 * Retrieves a value from an object graph.
 * 
 * @author Daniel Sagenschneider
 */
public interface ValueRetriever<T> {

	/**
	 * Retrieves the value from the object graph.
	 * 
	 * @param object
	 *            Root object of the object graph.
	 * @param name
	 *            Property name.
	 * @return Property value.
	 * @throws HttpException
	 *             If fails to retrieve the value.
	 */
	Object retrieveValue(T object, String name) throws HttpException;

	/**
	 * Obtains the value type for the property on the object graph. It may be
	 * <code>null</code> indicating the path does not exist on the bean graph.
	 * 
	 * @param name
	 *            Property name.
	 * @return Value type. May be <code>null</code> if the path not exists.
	 * @throws HttpException
	 *             If fails to determine if value is retrievable.
	 */
	Class<?> getValueType(String name) throws HttpException;

	/**
	 * Obtains the value annotation for the property on the object graph. It may be
	 * <code>null</code> indicating either:
	 * <ul>
	 * <li>the path does not exist on the bean graph, or</li>
	 * <li>no annotation by type for property</li>
	 * </ul>
	 * 
	 * @param <A>
	 *            {@link Annotation} type.
	 * @param name
	 *            Property name.
	 * @param annotationType
	 *            Annotation type.
	 * @return Annotation. May be <code>null</code>.
	 * @throws HttpException
	 *             If fails to obtain annotation.
	 */
	<A> A getValueAnnotation(String name, Class<A> annotationType) throws HttpException;

}