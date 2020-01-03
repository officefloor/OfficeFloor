package net.officefloor.compile.spi.managedfunction.source;

import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.internal.structure.EscalationFlow;

/**
 * Provides means for the {@link ManagedFunctionSource} to provide a
 * <code>type definition</code> of a possible {@link EscalationFlow} by the
 * {@link ManagedFunction}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ManagedFunctionEscalationTypeBuilder {

	/**
	 * <p>
	 * Provides means to specify a display label for the {@link EscalationFlow}.
	 * <p>
	 * This need not be set as is only an aid to better identify the
	 * {@link EscalationFlow}. If not set it will use the <code>Simple</code>
	 * name of the {@link EscalationFlow} {@link Class}.
	 * 
	 * @param label
	 *            Display label for the {@link EscalationFlow}.
	 */
	void setLabel(String label);

}