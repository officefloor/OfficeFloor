package net.officefloor.compile.spi.office;

import net.officefloor.frame.api.escalate.Escalation;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.internal.structure.EscalationProcedure;

/**
 * {@link EscalationProcedure} of {@link Escalation} to the {@link Office}.
 * 
 * @author Daniel Sagenschneider
 */
public interface OfficeEscalation extends OfficeFlowSourceNode {

	/**
	 * Obtains the type of escalation.
	 * 
	 * @return Type of escalation.
	 */
	String getOfficeEscalationType();

}