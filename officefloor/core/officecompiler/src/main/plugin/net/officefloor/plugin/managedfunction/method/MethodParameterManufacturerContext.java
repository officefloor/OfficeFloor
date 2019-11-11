/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2019 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.officefloor.plugin.managedfunction.method;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.function.Consumer;

import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionEscalationTypeBuilder;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionFlowTypeBuilder;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionObjectTypeBuilder;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionTypeBuilder;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.escalate.Escalation;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.source.SourceContext;
import net.officefloor.frame.internal.structure.EscalationFlow;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.plugin.clazz.Qualified;
import net.officefloor.plugin.clazz.Qualifier;

/**
 * Context for the {@link MethodParameterManufacturer}.
 * 
 * @author Daniel Sagenschneider
 */
public interface MethodParameterManufacturerContext {

	/**
	 * Obtains the {@link Class} of the parameter.
	 * 
	 * @return {@link Class} of the parameter.
	 */
	Class<?> getParameterClass();

	/**
	 * Obtains the {@link Type} of the parameter.
	 * 
	 * @return {@link Type} of the parameter.
	 */
	Type getParameterType();

	/**
	 * Obtains the {@link Annotation} instances for the parameter.
	 * 
	 * @return {@link Annotation} instances for the parameter.
	 */
	Annotation[] getParameterAnnotations();

	/**
	 * <p>
	 * Obtains the parameter qualifier.
	 * <p>
	 * This is via {@link Qualifier} or {@link Qualified} {@link Annotation} on the
	 * parameter.
	 * <p>
	 * This provides standard means to obtain the qualifier and avoid each
	 * {@link MethodParameterManufacturer} handling {@link Annotation} to determine.
	 * 
	 * @return Qualifier for the parameter.
	 */
	String getParameterQualifier();

	/**
	 * Adds a {@link ManagedFunctionObjectTypeBuilder} to the
	 * {@link ManagedFunctionTypeBuilder} definition.
	 * 
	 * @param objectType Type of the dependent {@link Object}.
	 * @param builder    Means to build the
	 *                   {@link ManagedFunctionObjectTypeBuilder}.
	 * @return Index for the added {@link Object}.
	 */
	int addObject(Class<?> objectType, Consumer<ManagedFunctionObjectTypeBuilder<Indexed>> builder);

	/**
	 * Adds a {@link ManagedFunctionFlowTypeBuilder} to the
	 * {@link ManagedFunctionTypeBuilder} definition.
	 * 
	 * @param builder Means to build the {@link ManagedFunctionFlowTypeBuilder}.
	 * @return Index for the added {@link Flow}.
	 */
	int addFlow(Consumer<ManagedFunctionFlowTypeBuilder<Indexed>> builder);

	/**
	 * Obtains the name of the {@link ManagedFunction}.
	 * 
	 * @return Name of the {@link ManagedFunction}.
	 */
	String getFunctionName();

	/**
	 * <p>
	 * Adds a {@link ManagedFunctionEscalationTypeBuilder} to the
	 * {@link ManagedFunctionTypeBuilder} definition.
	 * <p>
	 * It is possible the {@link MethodParameterFactory} will throw an
	 * {@link Escalation}. While this should be avoided, this allows registering
	 * {@link Escalation} for being handled.
	 * 
	 * @param <E>            {@link Escalation} type.
	 * @param escalationType Type to be handled by an {@link EscalationFlow}.
	 * @return {@link ManagedFunctionEscalationTypeBuilder} to provide the
	 *         <code>type definition</code>.
	 */
	<E extends Throwable> ManagedFunctionEscalationTypeBuilder addEscalation(Class<E> escalationType);

	/**
	 * Obtains the {@link SourceContext}.
	 * 
	 * @return {@link SourceContext}.
	 */
	SourceContext getSourceContext();

}