package net.officefloor.compile.test.governance;

import net.officefloor.compile.governance.GovernanceFlowType;
import net.officefloor.compile.governance.GovernanceType;
import net.officefloor.compile.spi.governance.source.GovernanceSource;
import net.officefloor.frame.api.escalate.Escalation;
import net.officefloor.frame.internal.structure.Flow;

/**
 * Builder of the {@link GovernanceType} to validate the loaded
 * {@link GovernanceType} from the {@link GovernanceSource}.
 * 
 * @author Daniel Sagenschneider
 */
public interface GovernanceTypeBuilder<F extends Enum<F>> {

	/**
	 * Specifies the extension interface type.
	 * 
	 * @param extensionInterface
	 *            Extension interface type.
	 */
	void setExtensionInterface(Class<?> extensionInterface);

	/**
	 * Adds a {@link GovernanceFlowType}.
	 * 
	 * @param flowName
	 *            Name of the {@link Flow}.
	 * @param argumentType
	 *            Argument type.
	 * @param index
	 *            Index of the {@link Flow}.
	 * @param flowKey
	 *            Key of the {@link Flow}.
	 */
	void addFlow(String flowName, Class<?> argumentType, int index, F flowKey);

	/**
	 * Adds an {@link Escalation}.
	 * 
	 * @param escalationType
	 *            {@link Escalation} type.
	 */
	void addEscalation(Class<?> escalationType);

}