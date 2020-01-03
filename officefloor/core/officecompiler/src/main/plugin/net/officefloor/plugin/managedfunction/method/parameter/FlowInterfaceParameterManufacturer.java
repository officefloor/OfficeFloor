package net.officefloor.plugin.managedfunction.method.parameter;

import net.officefloor.plugin.clazz.FlowInterface;
import net.officefloor.plugin.managedfunction.method.MethodParameterManufacturer;

/**
 * {@link MethodParameterManufacturer} for {@link FlowInterface}.
 * 
 * @author Daniel Sagenschneider
 */
public class FlowInterfaceParameterManufacturer extends AbstractFlowParameterManufacturer<FlowInterface> {

	@Override
	protected Class<FlowInterface> getFlowAnnotation() {
		return FlowInterface.class;
	}

}