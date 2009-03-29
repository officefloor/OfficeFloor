/*
 *  Office Floor, Application Server
 *  Copyright (C) 2006 Daniel Sagenschneider
 *
 *  This program is free software; you can redistribute it and/or modify it under the terms 
 *  of the GNU General Public License as published by the Free Software Foundation; either 
 *  version 2 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program; 
 *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, 
 *  MA 02111-1307 USA
 */
package net.officefloor.plugin.work.clazz;

import java.lang.reflect.Method;

import net.officefloor.compile.spi.work.source.TaskFactoryManufacturer;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.build.TaskFactory;

/**
 * {@link TaskFactoryManufacturer} for a {@link ClassTaskFactory}.
 * 
 * @author Daniel
 */
public class ClassTaskFactoryManufacturer implements
		TaskFactoryManufacturer<ClassWork, Indexed, Indexed> {

	/**
	 * {@link Method}.
	 */
	private final Method method;

	/**
	 * Listing of {@link ParameterFactory} instances.
	 */
	private final ParameterFactory[] parameters;

	/**
	 * Initiate.
	 * 
	 * @param method
	 *            {@link Method}.
	 * @param parameters
	 *            Listing of {@link ParameterFactory} instances for the
	 *            parameters of the {@link Method}.
	 */
	public ClassTaskFactoryManufacturer(Method method,
			ParameterFactory[] parameters) {
		this.method = method;
		this.parameters = parameters;
	}

	/*
	 * ================ TaskFactoryManufacturer ================================
	 */

	@Override
	public TaskFactory<ClassWork, Indexed, Indexed> createTaskFactory() {

		// Clone the parameters.
		// Necessary for task used for two flow items.
		ParameterFactory[] clone = new ParameterFactory[this.parameters.length];
		for (int i = 0; i < clone.length; i++) {
			clone[i] = this.parameters[i];
		}

		// Return a new task factory
		return new ClassTaskFactory(this.method, clone);
	}

}