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

import net.officefloor.frame.api.execute.TaskContext;

/**
 * {@link ParameterFactory} for an {@link Object}.
 * 
 * @author Daniel
 */
public class ObjectParameterFactory implements ParameterFactory {

	/**
	 * Index of the {@link Object}.
	 */
	private final int objectIndex;

	/**
	 * Initiate.
	 * 
	 * @param objectIndex
	 *            Index of the {@link Object}.
	 */
	public ObjectParameterFactory(int objectIndex) {
		this.objectIndex = objectIndex;
	}

	/*
	 * ================== ParameterFactory ====================================
	 */

	@Override
	public Object createParameter(TaskContext<?, ?, ?> context) {
		return context.getObject(this.objectIndex);
	}

}