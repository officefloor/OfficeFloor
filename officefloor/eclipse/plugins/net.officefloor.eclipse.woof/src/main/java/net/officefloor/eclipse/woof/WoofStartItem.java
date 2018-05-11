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

import javafx.scene.Node;
import net.officefloor.eclipse.editor.AdaptedModelVisualFactoryContext;
import net.officefloor.eclipse.editor.DefaultConnectors;
import net.officefloor.eclipse.ide.editor.AbstractConfigurableItem;
import net.officefloor.woof.model.woof.WoofChanges;
import net.officefloor.woof.model.woof.WoofModel;
import net.officefloor.woof.model.woof.WoofModel.WoofEvent;
import net.officefloor.woof.model.woof.WoofStartModel;
import net.officefloor.woof.model.woof.WoofStartModel.WoofStartEvent;

/**
 * Configuration for the {@link WoofStartModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class WoofStartItem extends
		AbstractConfigurableItem<WoofModel, WoofEvent, WoofChanges, WoofStartModel, WoofStartEvent, WoofStartItem> {

	/**
	 * Test configuration.
	 */
	public static void main(String[] args) {
		WoofEditor.launchConfigurer(new WoofStartItem(), null);
	}

	/*
	 * ======================= AbstractConfigurableItem =====================
	 */

	@Override
	public WoofStartModel prototype() {
		return new WoofStartModel();
	}

	@Override
	public IdeExtractor extract() {
		return new IdeExtractor((parent) -> parent.getWoofStarts(), WoofEvent.ADD_WOOF_START,
				WoofEvent.REMOVE_WOOF_START);
	}

	@Override
	public Node visual(WoofStartModel model, AdaptedModelVisualFactoryContext<WoofStartModel> context) {
		return DefaultConnectors.OBJECT.createGeometryNode(context);
	}

	@Override
	public IdeLabeller label() {
		return null;
	}

	@Override
	public void loadToParent(WoofModel parentModel, WoofStartModel itemModel) {
		parentModel.addWoofStart(itemModel);
	}

	@Override
	protected WoofStartItem item(WoofStartModel model) {
		return new WoofStartItem();
	}

	@Override
	public IdeConfigurer configure() {
		return new IdeConfigurer().add((context) -> {
			context.execute(context.getOperations().addStart());
		}).delete((context) -> {
			context.execute(context.getOperations().removeStart(context.getModel()));
		});
	}

}