package net.officefloor.activity.procedure;

/**
 * <code>Type definition</code> of a dependent {@link Object} required by the
 * {@link Procedure}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ProcedureObjectType {

	/**
	 * Obtains the name for the {@link ProcedureObjectType}.
	 * 
	 * @return Name for the {@link ProcedureObjectType}.
	 */
	String getObjectName();

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

}