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
package net.officefloor.compile.impl.structure;

import net.officefloor.compile.internal.structure.LinkSynchronousNode;
import net.officefloor.compile.internal.structure.OfficeInputNode;
import net.officefloor.compile.office.OfficeInputType;
import net.officefloor.compile.office.OfficeOutputType;

/**
 * Implementation of the {@link OfficeFloorInputNode}.
 *
 * @author Daniel Sagenschneider
 */
public class OfficeInputNodeImpl implements OfficeInputNode,
		OfficeInputType {

	/**
	 * Name of this {@link OfficeFloorInput}.
	 */
	private final String name;

	/**
	 * Parameter type of this {@link OfficeFloorInput}.
	 */
	private final String parameterType;

	/**
	 * Initialise.
	 * 
	 * @param name
	 *            Name of {@link OfficeFloorInput}.
	 * @param parameterType
	 *            Parameter type of {@link OfficeFloorInput}.
	 */
	public OfficeInputNodeImpl(String name, String parameterType) {
		this.name = name;
		this.parameterType = parameterType;
	}

	/*
	 * ======================= OfficeInputNode ===========================
	 */

	@Override
	public boolean linkSynchronousNode(LinkSynchronousNode node) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public LinkSynchronousNode getLinkedSynchronousNode() {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * ========================= OfficeInput =============================
	 */

	@Override
	public String getOfficeInputName() {
		return this.name;
	}

	@Override
	public String getParameterType() {
		return this.parameterType;
	}

	/*
	 * ======================= OfficeInputType ===========================
	 */

	@Override
	public OfficeOutputType getResponseOfficeOutputType() {
		// TODO Auto-generated method stub
		return null;
	}

}