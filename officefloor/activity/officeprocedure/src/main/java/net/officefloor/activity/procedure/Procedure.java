package net.officefloor.activity.procedure;

import net.officefloor.activity.procedure.spi.ProcedureSource;

/**
 * Procedure.
 * 
 * @author Daniel Sagenschneider
 */
public interface Procedure {

	/**
	 * Obtains the name of the {@link ProcedureSource}.
	 * 
	 * @return Name of the {@link ProcedureSource}.
	 */
	String getServiceName();

	/**
	 * Obtains the name of the {@link Procedure}.
	 * 
	 * @return Name of the {@link Procedure}. May be <code>null</code> to indicate
	 *         manually selected.
	 */
	String getProcedureName();

	/**
	 * Obtains the specification of properties for the {@link Procedure}.
	 * 
	 * @return Property specification.
	 */
	ProcedureProperty[] getProperties();

	/**
	 * Determines if the {@link Procedure}.
	 * 
	 * @param serviceName   Service name.
	 * @param procedureName Name of {@link Procedure}. May be <code>null</code> for
	 *                      manually selected.
	 * @return <code>true</code> if this {@link Procedure} matches.
	 */
	default boolean isProcedure(String serviceName, String procedureName) {
		boolean isServiceNameMatch = serviceName == null ? false : serviceName.equals(this.getServiceName());
		boolean isProcedureNameMatch = procedureName == null ? (this.getProcedureName() == null)
				: procedureName.equals(this.getProcedureName());
		return isServiceNameMatch && isProcedureNameMatch;
	}
}