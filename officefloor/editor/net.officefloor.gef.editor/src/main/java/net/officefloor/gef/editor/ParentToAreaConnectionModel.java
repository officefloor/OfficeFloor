/*-
 * #%L
 * [bundle] OfficeFloor Editor
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
