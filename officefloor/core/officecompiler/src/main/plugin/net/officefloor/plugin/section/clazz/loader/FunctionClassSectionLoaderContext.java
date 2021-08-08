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

package net.officefloor.plugin.section.clazz.loader;

import net.officefloor.compile.managedfunction.ManagedFunctionType;
import net.officefloor.compile.spi.section.FunctionFlow;
import net.officefloor.compile.spi.section.FunctionObject;
import net.officefloor.compile.spi.section.SectionFlowSinkNode;
import net.officefloor.compile.spi.section.SectionFunction;

/**
 * Context for the {@link ClassSectionLoader}.
 * 
 * @author Daniel Sagenschneider
 */
public interface FunctionClassSectionLoaderContext extends ClassSectionLoaderContext {

	/**
	 * Obtains the {@link SectionFunction}.
	 * 
	 * @return {@link SectionFunction}.
	 */
	SectionFunction getSectionFunction();

	/**
	 * Obtains the {@link ManagedFunctionType}.
	 * 
	 * @return {@link ManagedFunctionType}.
	 */
	ManagedFunctionType<?, ?> getManagedFunctionType();

	/**
	 * Obtains the parameter type.
	 * 
	 * @return Parameter type.
	 */
	Class<?> getParameterType();

	/**
	 * Flags the next {@link SectionFlowSinkNode} linked.
	 */
	void flagNextLinked();

	/**
	 * Flags the {@link FunctionObject} linked.
	 * 
	 * @param objectIndex Index of the {@link FunctionObject}.
	 */
	void flagFunctionObjectLinked(int objectIndex);

	/**
	 * Flags the {@link FunctionFlow} linked.
	 * 
	 * @param flowIndex Index of the {@link FunctionFlow}.
	 */
	void flagFunctionFlowLinked(int flowIndex);

}
