/*-
 * #%L
 * [bundle] Section Editor
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

package net.officefloor.gef.woof;

import java.util.List;

import net.officefloor.configuration.ConfigurationItem;
import net.officefloor.configuration.WritableConfigurationItem;
import net.officefloor.gef.bridge.EnvironmentBridge;
import net.officefloor.gef.ide.editor.AbstractAdaptedIdeEditor;
import net.officefloor.gef.ide.editor.AbstractConfigurableItem;
import net.officefloor.model.impl.repository.ModelRepositoryImpl;
import net.officefloor.woof.WoofLoaderSettings;
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
public class WoofEditor extends AbstractAdaptedIdeEditor<WoofModel, WoofEvent, WoofChanges> {

	/**
	 * {@link WoofRepository}.
	 */
	private static final WoofRepository WOOF_REPOSITORY = new WoofRepositoryImpl(new ModelRepositoryImpl());

	/**
	 * Instantiate.
	 * 
	 * @param envBridge {@link EnvironmentBridge}.
	 */
	public WoofEditor(EnvironmentBridge envBridge) {
		super(WoofModel.class, (model) -> new WoofChangesImpl(model), envBridge);
	}

	/*
	 * ================= AbstractIdeEditor ==================
	 */

	@Override
	public String fileName() {
		return WoofLoaderSettings.DEFAULT_WOOF_PATH;
	}

	@Override
	public WoofModel newFileRoot() {
		return new WoofModel();
	}

	@Override
	public WoofModel prototype() {
		return new WoofModel();
	}

	@Override
	public String paletteStyle() {
		return ".palette { -fx-background-color: cornsilk }";
	}

	@Override
	public String paletteIndicatorStyle() {
		return ".palette-indicator { -fx-background-color: bisque }";
	}

	@Override
	public String editorStyle() {
		return ".connection Path { -fx-stroke: royalblue; -fx-opacity: 0.6 }";
	}

	@Override
	protected void loadParents(List<AbstractConfigurableItem<WoofModel, WoofEvent, WoofChanges, ?, ?, ?>> parents) {
		parents.add(new WoofHttpContinuationItem());
		parents.add(new WoofHttpInputItem());
		parents.add(new WoofProcedureItem());
		parents.add(new WoofSectionItem());
		parents.add(new WoofResourceItem());
		parents.add(new WoofExceptionItem());
		parents.add(new WoofGovernanceItem());
		parents.add(new WoofTemplateItem());
		parents.add(new WoofSecurityItem());
		parents.add(new WoofStartItem());
	}

	@Override
	protected WoofModel loadRootModel(ConfigurationItem configurationItem) throws Exception {
		WoofModel woof = new WoofModel();
		WOOF_REPOSITORY.retrieveWoof(woof, configurationItem);
		return woof;
	}

	@Override
	public void saveRootModel(WoofModel model, WritableConfigurationItem configurationItem) throws Exception {
		WOOF_REPOSITORY.storeWoof(model, configurationItem);
	}

}
