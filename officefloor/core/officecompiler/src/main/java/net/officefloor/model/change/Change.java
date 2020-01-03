package net.officefloor.model.change;

/**
 * <p>
 * A change to be applied/reverted.
 * <p>
 * All operations utilise this to enable do/undo functionality.
 * 
 * @author Daniel Sagenschneider
 */
public interface Change<T> {

	/**
	 * Obtains the target to which this {@link Change} applies.
	 * 
	 * @return Target to which this {@link Change} applies.
	 */
	T getTarget();

	/**
	 * Obtains a description of the {@link Change}.
	 * 
	 * @return Description of the {@link Change}.
	 */
	String getChangeDescription();

	/**
	 * <p>
	 * Indicates if can apply this {@link Change}.
	 * <p>
	 * Typically there will be {@link Conflict} instances providing detail on
	 * why the {@link Change} can not be applied.
	 * 
	 * @return <code>true</code> if can apply this {@link Change}.
	 * 
	 * @see #getConflicts()
	 */
	boolean canApply();

	/**
	 * Applies this {@link Change}.
	 */
	void apply();

	/**
	 * <p>
	 * Reverts this {@link Change} (after being applied).
	 * <p>
	 * This enables do/undo functionality.
	 */
	void revert();

	/**
	 * <p>
	 * Obtains the {@link Conflict} instances preventing this {@link Change}
	 * from being applied.
	 * <p>
	 * A {@link Change} can only be applied if this returns an empty array.
	 * 
	 * @return Any {@link Conflict} instances preventing applying this
	 *         {@link Change}.
	 */
	Conflict[] getConflicts();

}