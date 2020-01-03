package net.officefloor.compile.spi.administration.source;

import net.officefloor.frame.api.administration.Administration;
import net.officefloor.frame.api.escalate.Escalation;

/**
 * Describes a {@link Escalation} from the {@link Administration}.
 * 
 * @author Daniel Sagenschneider
 */
public interface AdministrationEscalationMetaData {

	/**
	 * Obtains the {@link Class} of the {@link Escalation}.
	 * 
	 * @param <E>
	 *            {@link Escalation} type.
	 * @return {@link Class} of the {@link Escalation}.
	 */
	<E extends Throwable> Class<E> getEscalationType();

	/**
	 * Provides a descriptive name for this {@link Escalation}. This is useful to
	 * better describe the {@link Escalation}.
	 * 
	 * @return Descriptive name for this {@link Escalation}.
	 */
	String getLabel();

}