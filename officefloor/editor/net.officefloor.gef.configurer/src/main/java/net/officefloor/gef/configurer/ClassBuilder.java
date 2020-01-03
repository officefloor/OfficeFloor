package net.officefloor.gef.configurer;

/**
 * Builder of a {@link Class} value.
 * 
 * @author Daniel Sagenschneider
 */
public interface ClassBuilder<M> extends Builder<M, String, ClassBuilder<M>> {

	/**
	 * Super type of the required {@link Class}.
	 * 
	 * @param superType
	 *            Super type of the {@link Class}.
	 * @return <code>this</code>.
	 */
	ClassBuilder<M> superType(Class<?> superType);

}