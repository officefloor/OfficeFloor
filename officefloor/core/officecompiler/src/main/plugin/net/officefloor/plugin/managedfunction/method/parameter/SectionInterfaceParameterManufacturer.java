package net.officefloor.plugin.managedfunction.method.parameter;

import net.officefloor.plugin.managedfunction.method.MethodParameterManufacturer;
import net.officefloor.plugin.section.clazz.SectionInterface;

/**
 * {@link MethodParameterManufacturer} for {@link SectionInterface}.
 * 
 * @author Daniel Sagenschneider
 */
public class SectionInterfaceParameterManufacturer extends AbstractFlowParameterManufacturer<SectionInterface> {

	@Override
	protected Class<SectionInterface> getFlowAnnotation() {
		return SectionInterface.class;
	}

}