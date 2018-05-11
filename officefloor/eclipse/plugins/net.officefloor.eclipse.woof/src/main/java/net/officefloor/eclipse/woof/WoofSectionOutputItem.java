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

import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import net.officefloor.eclipse.editor.AdaptedModelVisualFactoryContext;
import net.officefloor.eclipse.ide.editor.AbstractItem;
import net.officefloor.model.ConnectionModel;
import net.officefloor.woof.model.woof.WoofChanges;
import net.officefloor.woof.model.woof.WoofModel;
import net.officefloor.woof.model.woof.WoofSectionModel;
import net.officefloor.woof.model.woof.WoofSectionModel.WoofSectionEvent;
import net.officefloor.woof.model.woof.WoofSectionOutputModel;
import net.officefloor.woof.model.woof.WoofSectionOutputModel.WoofSectionOutputEvent;

/**
 * Configuration for the {@link WoofSectionOutputModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class WoofSectionOutputItem extends
		AbstractItem<WoofModel, WoofChanges, WoofSectionModel, WoofSectionEvent, WoofSectionOutputModel, WoofSectionOutputEvent> {

	@Override
	public WoofSectionOutputModel prototype() {
		return new WoofSectionOutputModel("Output", null);
	}

	@Override
	public IdeExtractor extract() {
		return new IdeExtractor((parent) -> parent.getOutputs(), WoofSectionEvent.ADD_OUTPUT,
				WoofSectionEvent.REMOVE_OUTPUT);
	}

	@Override
	public void loadToParent(WoofSectionModel parentModel, WoofSectionOutputModel itemModel) {
		parentModel.addOutput(itemModel);
	}

	@Override
	public Pane visual(WoofSectionOutputModel model, AdaptedModelVisualFactoryContext<WoofSectionOutputModel> context) {
		HBox container = new HBox();
		context.label(container);
		return container;
	}

	@Override
	public IdeLabeller label() {
		return new IdeLabeller((model) -> model.getWoofSectionOutputName(),
				WoofSectionOutputEvent.CHANGE_WOOF_SECTION_OUTPUT_NAME);
	}

	@Override
	protected void connections(List<IdeConnectionTarget<? extends ConnectionModel, ?, ?>> connections) {

	}

}