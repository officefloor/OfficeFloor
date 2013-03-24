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
package net.officefloor.autowire.supplier;

import net.officefloor.autowire.AutoWireSection;
import net.officefloor.compile.spi.section.SectionInput;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.spi.managedobject.ManagedObject;

/**
 * <code>Type definition</code> of a flow instigated by the supplied
 * {@link ManagedObject} or one of its {@link Task} instances.
 * 
 * @author Daniel Sagenschneider
 */
public interface SuppliedManagedObjectFlowType {

	/**
	 * Obtains the name of the flow.
	 * 
	 * @return Name of the flow.
	 */
	String getFlowName();

	/**
	 * Obtains the name of the {@link AutoWireSection} containing the flow to be
	 * instigated.
	 * 
	 * @return Name of the {@link AutoWireSection} containing the flow to be
	 *         instigated.
	 */
	String getSectionName();

	/**
	 * Obtains the name of the {@link SectionInput} of the
	 * {@link AutoWireSection} that is the flow to be instigated.
	 * 
	 * @return Name of the {@link SectionInput} of the {@link AutoWireSection}
	 *         that is the flow to be instigated.
	 */
	String getSectionInputName();

	/**
	 * Obtains the type of the argument passed to the flow.
	 * 
	 * @return Type of argument passed to the flow. May be <code>null</code> to
	 *         indicate no argument.
	 */
	Class<?> getArgumentType();

}