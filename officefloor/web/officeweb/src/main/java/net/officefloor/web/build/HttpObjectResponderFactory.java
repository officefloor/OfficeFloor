package net.officefloor.web.build;

import net.officefloor.frame.api.escalate.Escalation;

/**
 * Factory for the creation of {@link HttpObjectResponder} instances.
 * 
 * @author Daniel Sagenschneider
 */
public interface HttpObjectResponderFactory {

	/**
	 * Obtains the <code>Content-Type</code> supported by the create
	 * {@link HttpObjectResponder} instances.
	 * 
	 * @return <code>Content-Type</code>.
	 */
	String getContentType();

	/**
	 * Creates the {@link HttpObjectResponder} for the {@link Object} type.
	 *
	 * @param <T>
	 *            Object type.
	 * @param objectType
	 *            {@link Object} type.
	 * @return {@link HttpObjectResponder} for the {@link Object} type.
	 */
	<T> HttpObjectResponder<T> createHttpObjectResponder(Class<T> objectType);

	/**
	 * Creates the {@link HttpObjectResponder} for the {@link Escalation} type.
	 * 
	 * @param <E>
	 *            {@link Escalation} type.
	 * @param escalationType
	 *            {@link Escalation} type.
	 * @return {@link HttpObjectResponder} for the {@link Escalation} type.
	 */
	<E extends Throwable> HttpObjectResponder<E> createHttpEscalationResponder(Class<E> escalationType);

}