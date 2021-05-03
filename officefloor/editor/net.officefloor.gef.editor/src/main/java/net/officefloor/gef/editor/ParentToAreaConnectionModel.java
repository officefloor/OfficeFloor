/*-
 * #%L
 * [bundle] OfficeFloor Editor
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

package net.officefloor.gef.editor;

import net.officefloor.model.AbstractModel;
import net.officefloor.model.ConnectionModel;
import net.officefloor.model.Model;

/**
 * {@link AdaptedParent} to {@link AdaptedArea} {@link ConnectionModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class ParentToAreaConnectionModel extends AbstractModel implements ConnectionModel {

	/**
	 * Parent {@link Model}.
	 */
	private final Model parent;

	/**
	 * Area {@link Model}.
	 */
	private final Model area;

	/**
	 * Instantiate.
	 * 
	 * @param parent Parent {@link Model}.
	 * @param area   Area {@link Model}.
	 */
	public ParentToAreaConnectionModel(Model parent, Model area) {
		this.parent = parent;
		this.area = area;
	}

	/**
	 * Obtains the parent {@link Model}.
	 * 
	 * @return Parent {@link Model}.
	 */
	public Model getParentModel() {
		return this.parent;
	}

	/**
	 * Obtains the area {@link Model}.
	 * 
	 * @return Area {@link Model}.
	 */
	public Model getAreaModel() {
		return this.area;
	}

	/*
	 * ===================== ConnectionModel ========================
	 */

	@Override
	public void connect() {
	}

	@Override
	public boolean isRemovable() {
		return false;
	}

	@Override
	public void remove() {
	}

}
