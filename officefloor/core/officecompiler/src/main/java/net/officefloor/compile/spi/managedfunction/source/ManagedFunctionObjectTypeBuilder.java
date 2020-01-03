package net.officefloor.compile.spi.managedfunction.source;

import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.function.ManagedFunction;

/**
 * Provides means for the {@link ManagedFunctionSource} to provide a
 * <code>type definition</code> of a dependency {@link Object} required by the
 * {@link ManagedFunction}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ManagedFunctionObjectTypeBuilder<M extends Enum<M>> {

	/**
	 * Specifies the {@link Enum} for this
	 * {@link ManagedFunctionObjectTypeBuilder}. This is required to be set if
	 * <code>M</code> is not {@link None} or {@link Indexed}.
	 * 
	 * @param key
	 *            {@link Enum} for this
	 *            {@link ManagedFunctionObjectTypeBuilder}.
	 */
	void setKey(M key);

	/**
	 * Specifies the type qualifier.
	 * 
	 * @param qualifier
	 *            Type qualifier.
	 */
	void setTypeQualifier(String qualifier);

	/**
	 * <p>
	 * Provides means to specify a display label for the {@link Object}.
	 * <p>
	 * This need not be set as is only an aid to better identify the
	 * {@link Object}. If not set the {@link ManagedFunctionTypeBuilder} will
	 * use the following order to get a display label:
	 * <ol>
	 * <li>{@link Enum} key name</li>
	 * <li>index value</li>
	 * </ol>
	 * 
	 * @param label
	 *            Display label for the {@link Object}.
	 */
	void setLabel(String label);

	/**
	 * Adds an annotation.
	 * 
	 * @param annotation
	 *            Annotation.
	 */
	void addAnnotation(Object annotation);

}