package net.officefloor.compile.managedfunction;

import net.officefloor.compile.type.AnnotatedType;
import net.officefloor.frame.api.function.ManagedFunction;

/**
 * <code>Type definition</code> of a dependent {@link Object} required by the
 * {@link ManagedFunction}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ManagedFunctionObjectType<M extends Enum<M>> extends AnnotatedType {

	/**
	 * Obtains the name for the {@link ManagedFunctionObjectType}.
	 * 
	 * @return Name for the {@link ManagedFunctionObjectType}.
	 */
	String getObjectName();

	/**
	 * <p>
	 * Obtains the index for the {@link ManagedFunctionObjectType}.
	 * <p>
	 * Should there be an {@link Enum} then will be the {@link Enum#ordinal()}
	 * value. Otherwise will be the index that this was added.
	 * 
	 * @return Index for the {@link ManagedFunctionObjectType}.
	 */
	int getIndex();

	/**
	 * Obtains the required type of the dependent {@link Object}.
	 * 
	 * @return Required type of the dependent {@link Object}.
	 */
	Class<?> getObjectType();

	/**
	 * <p>
	 * Obtains the qualifier on the type.
	 * <p>
	 * This is to enable qualifying the type of dependency required.
	 * 
	 * @return Qualifier on the type. May be <code>null</code> if not qualifying the
	 *         type.
	 */
	String getTypeQualifier();

	/**
	 * Obtains the {@link Enum} key for the {@link ManagedFunctionObjectType}.
	 * 
	 * @return {@link Enum} key for the {@link ManagedFunctionObjectType}. May be
	 *         <code>null</code> if no {@link Enum} for objects.
	 */
	M getKey();

}