/*-
 * #%L
 * OfficeCompiler
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package net.officefloor.plugin.clazz.method;

import java.lang.reflect.Method;

import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionEscalationTypeBuilder;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionTypeBuilder;
import net.officefloor.frame.api.escalate.Escalation;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.source.SourceContext;
import net.officefloor.frame.internal.structure.EscalationFlow;
import net.officefloor.plugin.clazz.dependency.ClassDependencies;

/**
 * Context for the {@link MethodReturnManufacturer}.
 * 
 * @author Daniel Sagenschneider
 */
public interface MethodObjectManufacturerContext {

	/**
	 * Obtains the {@link Method}.
	 * 
	 * @return {@link Method}.
	 */
	Method getMethod();

	/**
	 * Obtains the name of the {@link ManagedFunction}.
	 * 
	 * @return Name of the {@link ManagedFunction}.
	 */
	String getFunctionName();

	/**
	 * Obtains the {@link ClassDependencies} to create dependencies.
	 * 
	 * @return {@link ClassDependencies}.
	 */
	ClassDependencies getClassDependencies();

	/**
	 * <p>
	 * Adds a {@link ManagedFunctionEscalationTypeBuilder} to the
	 * {@link ManagedFunctionTypeBuilder} definition.
	 * <p>
	 * It is possible the {@link MethodReturnTranslator} will throw an
	 * {@link Escalation}. While this should be avoided, this allows registering
	 * {@link Escalation} for being handled.
	 * 
	 * @param <E>            {@link Escalation} type.
	 * @param escalationType Type to be handled by an {@link EscalationFlow}.
	 */
	<E extends Throwable> void addEscalation(Class<E> escalationType);

	/**
	 * Obtains the {@link SourceContext}.
	 * 
	 * @return {@link SourceContext}.
	 */
	SourceContext getSourceContext();

}
