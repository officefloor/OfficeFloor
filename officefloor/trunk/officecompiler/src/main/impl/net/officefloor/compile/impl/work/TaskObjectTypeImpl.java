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
package net.officefloor.compile.impl.work;

import net.officefloor.compile.impl.util.CompileUtil;
import net.officefloor.compile.spi.work.source.TaskObjectTypeBuilder;
import net.officefloor.compile.work.TaskObjectType;

/**
 * {@link TaskObjectType} implementation.
 * 
 * @author Daniel
 */
public class TaskObjectTypeImpl<M extends Enum<M>> implements
		TaskObjectType<M>, TaskObjectTypeBuilder<M> {

	/**
	 * Type of the dependency {@link Object}.
	 */
	private final Class<?> objectType;

	/**
	 * Label describing this {@link TaskObjectType}.
	 */
	private String label = null;

	/**
	 * Index identifying this {@link TaskObjectType}.
	 */
	private int index;

	/**
	 * {@link Enum} key identifying this {@link TaskObjectType}.
	 */
	private M key = null;

	/**
	 * Initiate with the index of the {@link TaskObjectType}.
	 * 
	 * @param objectType
	 *            Type of the dependency {@link Object}.
	 * @param index
	 *            Index identifying this {@link TaskObjectType}.
	 */
	public TaskObjectTypeImpl(Class<?> objectType, int index) {
		this.objectType = objectType;
		this.index = index;
	}

	/*
	 * ================= TaskObjectTypeBuilder ==========================
	 */

	@Override
	public void setKey(M key) {
		this.key = key;
		this.index = key.ordinal();
	}

	@Override
	public void setLabel(String label) {
		this.label = label;
	}

	/*
	 * ================== TaskObjectType ==================================
	 */

	@Override
	public String getObjectName() {
		// Follow priorities to obtain the object name
		if (!CompileUtil.isBlank(this.label)) {
			return this.label;
		} else if (this.key != null) {
			return this.key.toString();
		} else {
			return String.valueOf(this.index);
		}
	}

	@Override
	public int getIndex() {
		return this.index;
	}

	@Override
	public Class<?> getObjectType() {
		return this.objectType;
	}

	@Override
	public M getKey() {
		return this.key;
	}

}