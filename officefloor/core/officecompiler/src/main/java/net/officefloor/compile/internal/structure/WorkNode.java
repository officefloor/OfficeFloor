/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2013 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.officefloor.compile.internal.structure;

import net.officefloor.compile.managedfunction.FunctionNamespaceType;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionSource;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.section.SectionWork;
import net.officefloor.compile.type.TypeContext;
import net.officefloor.frame.api.build.OfficeBuilder;
import net.officefloor.frame.api.build.WorkBuilder;
import net.officefloor.frame.api.function.Work;

/**
 * {@link SectionWork} node.
 * 
 * @author Daniel Sagenschneider
 */
public interface WorkNode extends Node, SectionWork {

	/**
	 * {@link Node} type.
	 */
	static String TYPE = "Work";

	/**
	 * Initialises the {@link WorkNode}.
	 * 
	 * @param workSourceClassName
	 *            {@link Class} name of the {@link ManagedFunctionSource}.
	 * @param workSource
	 *            Optional instantiated {@link ManagedFunctionSource}. May be
	 *            <code>null</code>.
	 */
	void initialise(String workSourceClassName, ManagedFunctionSource<?> workSource);

	/**
	 * Obtains the {@link SectionNode} containing this {@link WorkNode}.
	 * 
	 * @return {@link SectionNode} containing this {@link WorkNode}.
	 */
	SectionNode getSectionNode();

	/**
	 * Obtains the name of this {@link Work} qualified with the
	 * {@link OfficeSection} hierarchy containing this {@link Work}.
	 * 
	 * @return Qualified name of this {@link Work}.
	 */
	String getQualifiedWorkName();

	/**
	 * Obtains the {@link FunctionNamespaceType} for this {@link WorkNode}.
	 * 
	 * @return {@link FunctionNamespaceType} for this {@link WorkNode}. May be
	 *         <code>null</code> if can not load the {@link FunctionNamespaceType}.
	 */
	FunctionNamespaceType<?> loadWorkType();

	/**
	 * Builds the {@link Work} for this {@link SectionWork}.
	 * 
	 * @param builder
	 *            {@link OfficeBuilder}.
	 * @param typeContext
	 *            {@link TypeContext}.
	 * @return {@link WorkBuilder} to enable building the {@link TaskNode}
	 *         instances.
	 */
	WorkBuilder<?> buildWork(OfficeBuilder builder, TypeContext typeContext);

}