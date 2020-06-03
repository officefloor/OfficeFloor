/*-
 * #%L
 * OfficeCompiler
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package net.officefloor.plugin.managedfunction.method.parameter;

import java.lang.annotation.Annotation;

import net.officefloor.frame.api.source.ServiceContext;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.plugin.clazz.flow.ClassFlowBuilder;
import net.officefloor.plugin.clazz.flow.ClassFlowInterfaceFactory;
import net.officefloor.plugin.clazz.flow.ClassFlowRegistry;
import net.officefloor.plugin.managedfunction.method.MethodParameterFactory;
import net.officefloor.plugin.managedfunction.method.MethodParameterManufacturer;
import net.officefloor.plugin.managedfunction.method.MethodParameterManufacturerContext;
import net.officefloor.plugin.managedfunction.method.MethodParameterManufacturerServiceFactory;

/**
 * Abstract {@link MethodParameterManufacturer} for the annotated {@link Flow}
 * parameters.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractFlowParameterManufacturer<A extends Annotation>
		implements MethodParameterManufacturer, MethodParameterManufacturerServiceFactory {

	/**
	 * Obtains the {@link Class} of the {@link Annotation}.
	 * 
	 * @return {@link Class} of the {@link Annotation}.
	 */
	protected abstract Class<A> getFlowAnnotation();

	/*
	 * ============ MethodParameterManufacturerServiceFactory ===========
	 */

	@Override
	public MethodParameterManufacturer createService(ServiceContext context) throws Throwable {
		return this;
	}

	/*
	 * =================== MethodParameterManufacturer ===================
	 */

	@Override
	public MethodParameterFactory createParameterFactory(MethodParameterManufacturerContext context) throws Exception {

		// Create the flow registry
		ClassFlowRegistry flowRegistry = (label, flowParameterType) -> {
			return context.addFlow((builder) -> {
				builder.setLabel(label);
				if (flowParameterType != null) {
					builder.setArgumentType(flowParameterType);
				}
			});
		};

		// Attempt to build flow parameter factory
		ClassFlowInterfaceFactory flowParameterFactory = new ClassFlowBuilder<A>(this.getFlowAnnotation())
				.buildFlowParameterFactory(context.getParameterClass(), flowRegistry, context.getSourceContext());
		if (flowParameterFactory == null) {
			return null; // not flow interface
		}

		// Return wrapping managed function flow parameter factory
		return new FlowInterfaceParameterFactory(flowParameterFactory);
	}

}
