package net.officefloor.model.change;

/**
 * List of {@link Change} instances.
 * 
 * @author Daniel Sagenschneider
 */
public interface ChangeList<T> extends Change<T> {

	/**
	 * Obtains the {@link Change} instances within this {@link ChangeList}.
	 * 
	 * @return {@link Change} instances within this {@link ChangeList}.
	 */
	Change<?>[] getChanges();

}