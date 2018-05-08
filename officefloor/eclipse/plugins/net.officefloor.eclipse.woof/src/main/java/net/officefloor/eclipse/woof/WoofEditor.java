/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
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
package net.officefloor.eclipse.woof;

import java.util.List;

import net.officefloor.configuration.ConfigurationItem;
import net.officefloor.configuration.WritableConfigurationItem;
import net.officefloor.eclipse.ide.editor.AbstractConfigurableItem;
import net.officefloor.eclipse.ide.editor.AbstractIdeEditor;
import net.officefloor.model.impl.repository.ModelRepositoryImpl;
import net.officefloor.woof.model.woof.WoofChanges;
import net.officefloor.woof.model.woof.WoofChangesImpl;
import net.officefloor.woof.model.woof.WoofModel;
import net.officefloor.woof.model.woof.WoofModel.WoofEvent;
import net.officefloor.woof.model.woof.WoofRepository;
import net.officefloor.woof.model.woof.WoofRepositoryImpl;

/**
 * Web on OfficeFloor (WoOF) Editor.
 * 
 * @author Daniel Sagenschneider
 */
public class WoofEditor extends AbstractIdeEditor<WoofModel, WoofEvent, WoofChanges> {

	/**
	 * Test editor.
	 */
	public static void main(String[] args) throws Exception {
		WoofEditor.launch("<woof />");
	}

	/**
	 * {@link WoofRepository}.
	 */
	private static final WoofRepository WOOF_REPOSITORY = new WoofRepositoryImpl(new ModelRepositoryImpl());

	/**
	 * Instantiate.
	 */
	public WoofEditor() {
		super(WoofModel.class, (model) -> new WoofChangesImpl(model));
	}

	/*
	 * ================= AbstractIdeEditor ==================
	 */

	@Override
	public WoofModel prototype() {
		return new WoofModel();
	}

	@Override
	protected void loadParents(List<AbstractConfigurableItem<WoofModel, WoofEvent, WoofChanges, ?, ?, ?>> parents) {
	}

	@Override
	protected WoofModel loadRootModel(ConfigurationItem configurationItem) throws Exception {
		WoofModel woof = new WoofModel();
		WOOF_REPOSITORY.retrieveWoof(woof, configurationItem);
		return woof;
	}

	@Override
	protected void saveRootModel(WoofModel model, WritableConfigurationItem configurationItem) throws Exception {
		WOOF_REPOSITORY.storeWoof(model, configurationItem);
	}

}