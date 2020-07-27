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
