package net.officefloor.model.impl.change;

import net.officefloor.model.change.Conflict;

/**
 * {@link Conflict} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class ConflictImpl implements Conflict {

	/**
	 * Description of the {@link Conflict}.
	 */
	private final String conflictDescription;

	/**
	 * Cuase of the {@link Conflict}. May be <code>null</code>.
	 */
	private final Throwable cause;

	/**
	 * Initiate.
	 * 
	 * @param conflictDescription
	 *            Description of the {@link Conflict}.
	 * @param cause
	 *            Cause of the {@link Conflict}. May be <code>null</code>.
	 */
	public ConflictImpl(String conflictDescription, Throwable cause) {
		this.conflictDescription = conflictDescription;
		this.cause = cause;
	}

	/*
	 * ====================== Conflict ==================================
	 */

	@Override
	public String getConflictDescription() {
		return this.conflictDescription;
	}

	@Override
	public Throwable getConflictCause() {
		return this.cause;
	}

}