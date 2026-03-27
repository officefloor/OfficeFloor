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
import net.officefloor.compile.spi.section.SectionFlowSinkNode;
import net.officefloor.compile.spi.section.SectionFunction;

/**
 * {@link SectionFunction} with meta-data for {@link ClassSectionLoader}.
 * 
 * @author Daniel Sagenschneider
 */
public class ClassSectionManagedFunction extends ClassSectionFlow {

	/**
	 * {@link ManagedFunctionType}.
	 */
	private final ManagedFunctionType<?, ?> managedFunctionType;

	/**
	 * Instantiate.
	 * 
	 * @param function            {@link SectionFunction}.
	 * @param argumentType        Argument type for the {@link SectionFlowSinkNode}.
	 *                            May be <code>null</code> for no argument.
	 * @param managedFunctionType {@link ManagedFunctionType}.
	 */
	public ClassSectionManagedFunction(SectionFunction function, ManagedFunctionType<?, ?> managedFunctionType,
			Class<?> argumentType) {
		super(function, argumentType);
		this.managedFunctionType = managedFunctionType;
	}

	/**
	 * Obtains the {@link SectionFunction}.
	 * 
	 * @return {@link SectionFunction}.
	 */
	public SectionFunction getFunction() {
		return this.getFlowSink();
	}

	/**
	 * Obtains the {@link ManagedFunctionType}.
	 * 
	 * @return {@link ManagedFunctionType}.
	 */
	public ManagedFunctionType<?, ?> getManagedFunctionType() {
		return this.managedFunctionType;
	}

	/*
	 * ==================== ClassSectionFlow ======================
	 */

	@Override
	public SectionFunction getFlowSink() {
		return (SectionFunction) super.getFlowSink();
	}

}
