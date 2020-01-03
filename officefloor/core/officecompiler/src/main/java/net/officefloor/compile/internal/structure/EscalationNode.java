package net.officefloor.compile.internal.structure;

import net.officefloor.compile.spi.office.OfficeEscalation;
import net.officefloor.frame.api.escalate.Escalation;

/**
 * {@link Escalation} node.
 * 
 * @author Daniel Sagenschneider
 */
public interface EscalationNode extends LinkFlowNode, OfficeEscalation {

	/**
	 * {@link Node} type.
	 */
	static String TYPE = "Escalation";

	/**
	 * Initialises the {@link EscalationNode}.
	 */
	void initialise();

}