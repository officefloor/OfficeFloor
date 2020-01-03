package net.officefloor.compile.impl.managedfunction;

import net.officefloor.compile.impl.properties.PropertyListSourceProperties;
import net.officefloor.compile.internal.structure.NodeContext;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionSource;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionSourceContext;
import net.officefloor.frame.impl.construct.source.SourceContextImpl;

/**
 * {@link ManagedFunctionSourceContext} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class ManagedFunctionSourceContextImpl extends SourceContextImpl implements ManagedFunctionSourceContext {

	/**
	 * Initiate.
	 * 
	 * @param managedFunctionSourceName Name of {@link ManagedFunctionSource}.
	 * @param isLoadingType             Indicates if loading type.
	 * @param propertyList              {@link PropertyList}.
	 * @param context                   {@link NodeContext}.
	 */
	public ManagedFunctionSourceContextImpl(String managedFunctionSourceName, boolean isLoadingType,
			PropertyList propertyList, NodeContext context) {
		super(managedFunctionSourceName, isLoadingType, context.getRootSourceContext(),
				new PropertyListSourceProperties(propertyList));
	}

}