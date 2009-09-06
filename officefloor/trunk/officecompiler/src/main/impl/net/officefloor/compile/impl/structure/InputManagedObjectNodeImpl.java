/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2009 Daniel Sagenschneider
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

import net.officefloor.compile.internal.structure.InputManagedObjectNode;
import net.officefloor.compile.internal.structure.LinkObjectNode;
import net.officefloor.compile.internal.structure.OfficeNode;
import net.officefloor.frame.api.build.OfficeBuilder;

/**
 * {@link InputManagedObjectNode} implementation.
 *
 * @author Daniel Sagenschneider
 */
public class InputManagedObjectNodeImpl implements InputManagedObjectNode {

	/**
	 * Name of this {@link InputManagedObjectNode}.
	 */
	private final String inputManagedObjectName;

	/**
	 * Initiate.
	 *
	 * @param inputManagedObjectName
	 *            Name of this {@link InputManagedObjectNode}.
	 */
	public InputManagedObjectNodeImpl(String inputManagedObjectName) {
		this.inputManagedObjectName = inputManagedObjectName;
	}

	/*
	 * ======================= BoundManagedObjectNode =========================
	 */

	@Override
	public String getBoundManagedObjectName() {
		return this.inputManagedObjectName;
	}

	@Override
	public void buildOfficeManagedObject(OfficeNode office,
			OfficeBuilder officeBuilder) {
		// Nothing to build for input managed object
	}

	/*
	 * ================== OfficeFloorInputManagedObject =======================
	 */

	@Override
	public String getOfficeFloorInputManagedObjectName() {
		return this.inputManagedObjectName;
	}

	/*
	 * =================== LinkObjectNode ======================================
	 */

	/**
	 * Linked {@link LinkObjectNode}.
	 */
	private LinkObjectNode linkedObjectName;

	@Override
	public boolean linkObjectNode(LinkObjectNode node) {
		// Link
		this.linkedObjectName = node;
		return true;
	}

	@Override
	public LinkObjectNode getLinkedObjectNode() {
		return this.linkedObjectName;
	}

}