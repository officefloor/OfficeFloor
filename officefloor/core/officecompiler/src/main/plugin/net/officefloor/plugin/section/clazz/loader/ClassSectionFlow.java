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

import net.officefloor.compile.spi.section.SectionFlowSinkNode;

/**
 * {@link SectionFlowSinkNode} with meta-data for {@link ClassSectionLoader}.
 * 
 * @author Daniel Sagenschneider
 */
public class ClassSectionFlow {

	/**
	 * {@link SectionFlowSinkNode}.
	 */
	private final SectionFlowSinkNode flowSink;

	/**
	 * Argument type for the {@link SectionFlowSinkNode}. May be <code>null</code>
	 * for no argument.
	 */
	private final Class<?> argumentType;

	/**
	 * Instantiate.
	 * 
	 * @param flowSink     {@link SectionFlowSinkNode}.
	 * @param argumentType Argument type for the {@link SectionFlowSinkNode}. May be
	 *                     <code>null</code> for no argument.
	 */
	public ClassSectionFlow(SectionFlowSinkNode flowSink, Class<?> argumentType) {
		this.flowSink = flowSink;
		this.argumentType = argumentType;
	}

	/**
	 * Obtains the {@link SectionFlowSinkNode}.
	 * 
	 * @return {@link SectionFlowSinkNode}.
	 */
	public SectionFlowSinkNode getFlowSink() {
		return flowSink;
	}

	/**
	 * Obtains the argument type.
	 * 
	 * @return Argument type for the {@link SectionFlowSinkNode}. May be
	 *         <code>null</code> for no argument.
	 */
	public Class<?> getArgumentType() {
		return argumentType;
	}

}
