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
package net.officefloor.plugin.section.clazz;

import java.lang.annotation.Documented;

import net.officefloor.compile.spi.section.ManagedObjectFlow;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;

/**
 * {@link FlowLink} of a {@link ManagedObjectSource}.
 * 
 * @author Daniel Sagenschneider
 */
@Documented
public @interface FlowLink {

	/**
	 * Obtains the name of the {@link ManagedObjectFlow}.
	 * 
	 * @return Name of the {@link ManagedObjectFlow}.
	 */
	String name();

	/**
	 * Obtains the name of the method to link the {@link FlowLink}.
	 * 
	 * @return Name of the method to link the {@link FlowLink}.
	 */
	String method();

}