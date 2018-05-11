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

import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import net.officefloor.eclipse.editor.AdaptedModelVisualFactoryContext;
import net.officefloor.eclipse.ide.editor.AbstractItem;
import net.officefloor.woof.model.woof.WoofChanges;
import net.officefloor.woof.model.woof.WoofModel;
import net.officefloor.woof.model.woof.WoofSecurityModel;
import net.officefloor.woof.model.woof.WoofSecurityModel.WoofSecurityEvent;
import net.officefloor.woof.model.woof.WoofSecurityOutputModel;
import net.officefloor.woof.model.woof.WoofSecurityOutputModel.WoofSecurityOutputEvent;

/**
 * Configuration for the {@link WoofSecurityOutputModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class WoofSecurityOutputItem extends
		AbstractItem<WoofModel, WoofChanges, WoofSecurityModel, WoofSecurityEvent, WoofSecurityOutputModel, WoofSecurityOutputEvent> {

	/*
	 * ============= AbstractItem ==================
	 */

	@Override
	public WoofSecurityOutputModel prototype() {
		return new WoofSecurityOutputModel("Output", null);
	}

	@Override
	public IdeExtractor extract() {
		return new IdeExtractor((parent) -> parent.getOutputs(), WoofSecurityEvent.ADD_OUTPUT,
				WoofSecurityEvent.REMOVE_OUTPUT);
	}

	@Override
	public void loadToParent(WoofSecurityModel parentModel, WoofSecurityOutputModel itemModel) {
		parentModel.addOutput(itemModel);
	}

	@Override
	public Pane visual(WoofSecurityOutputModel model,
			AdaptedModelVisualFactoryContext<WoofSecurityOutputModel> context) {
		HBox container = new HBox();
		context.label(container);
		return container;
	}

	@Override
	public IdeLabeller label() {
		return new IdeLabeller((model) -> model.getWoofSecurityOutputName(),
				WoofSecurityOutputEvent.CHANGE_WOOF_SECURITY_OUTPUT_NAME);
	}

}