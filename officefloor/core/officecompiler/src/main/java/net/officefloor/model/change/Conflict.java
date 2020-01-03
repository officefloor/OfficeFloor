package net.officefloor.model.change;

/**
 * Obtains a {@link Conflict} preventing a {@link Change} from being applied.
 * 
 * @author Daniel Sagenschneider
 */
public interface Conflict {

	/**
	 * Obtains a description of the {@link Conflict}.
	 * 
	 * @return Description of the {@link Conflict}.
	 */
	String getConflictDescription();

	/**
	 * Obtains the cause of the {@link Conflict}.
	 * 
	 * @return Cause of the {@link Conflict}. May be <code>null</code>.
	 */
	Throwable getConflictCause();

}