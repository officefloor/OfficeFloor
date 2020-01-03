package net.officefloor.compile.internal.structure;

import net.officefloor.frame.api.thread.OptionalThreadLocal;

/**
 * Receives the {@link OptionalThreadLocal}.
 * 
 * @author Daniel Sagenschneider
 */
public interface OptionalThreadLocalReceiver {

	/**
	 * Receives the {@link OptionalThreadLocal}.
	 * 
	 * @param optionalThreadLocal {@link OptionalThreadLocal}.
	 */
	void setOptionalThreadLocal(OptionalThreadLocal<?> optionalThreadLocal);

}