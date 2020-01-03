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

package net.officefloor.plugin.section.clazz;

import net.officefloor.compile.spi.managedobject.ManagedObjectFlow;

/**
 * {@link FlowLink} annotation.
 * 
 * @author Daniel Sagenschneider
 */
public class FlowLinkAnnotation {

	/**
	 * Name of the {@link ManagedObjectFlow}.
	 */
	private final String name;

	/**
	 * Name of the method to link the {@link FlowLink}.
	 */
	private final String method;

	/**
	 * Instantiate.
	 * 
	 * @param name   Name of the {@link ManagedObjectFlow}.
	 * @param method Name of the method to link the {@link FlowLink}.
	 */
	public FlowLinkAnnotation(String name, String method) {
		this.name = name;
		this.method = method;
	}

	/**
	 * Instantiate.
	 * 
	 * @param flowLink {@link FlowLink}.
	 */
	public FlowLinkAnnotation(FlowLink flowLink) {
		this(flowLink.name(), flowLink.method());
	}

	/**
	 * Obtains the name of the {@link ManagedObjectFlow}.
	 * 
	 * @return Name of the {@link ManagedObjectFlow}.
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * Obtains the name of the method to link the {@link FlowLink}.
	 * 
	 * @return Name of the method to link the {@link FlowLink}.
	 */
	public String getMethod() {
		return this.method;
	}

}
