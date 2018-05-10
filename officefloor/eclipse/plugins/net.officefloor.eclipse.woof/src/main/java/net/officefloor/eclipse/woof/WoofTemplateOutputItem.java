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
import net.officefloor.woof.model.woof.WoofTemplateModel;
import net.officefloor.woof.model.woof.WoofTemplateModel.WoofTemplateEvent;
import net.officefloor.woof.model.woof.WoofTemplateOutputModel;
import net.officefloor.woof.model.woof.WoofTemplateOutputModel.WoofTemplateOutputEvent;

/**
 * Configuration for the {@link WoofTemplateOutputModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class WoofTemplateOutputItem extends
		AbstractItem<WoofModel, WoofChanges, WoofTemplateModel, WoofTemplateEvent, WoofTemplateOutputModel, WoofTemplateOutputEvent> {

	/*
	 * ============= AbstractItem ==================
	 */

	@Override
	public WoofTemplateOutputModel prototype() {
		return new WoofTemplateOutputModel("Output", null);
	}

	@Override
	public IdeExtractor extract() {
		return new IdeExtractor((parent) -> parent.getOutputs(), WoofTemplateEvent.ADD_OUTPUT,
				WoofTemplateEvent.REMOVE_OUTPUT);
	}

	@Override
	public void loadToParent(WoofTemplateModel parentModel, WoofTemplateOutputModel itemModel) {
		parentModel.addOutput(itemModel);
	}

	@Override
	public Pane visual(WoofTemplateOutputModel model,
			AdaptedModelVisualFactoryContext<WoofTemplateOutputModel> context) {
		HBox container = new HBox();
		context.label(container);
		return container;
	}

	@Override
	public IdeLabeller label() {
		return new IdeLabeller((model) -> model.getWoofTemplateOutputName(),
				WoofTemplateOutputEvent.CHANGE_WOOF_TEMPLATE_OUTPUT_NAME);
	}

}