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

import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import net.officefloor.gef.editor.AdaptedChildVisualFactoryContext;
import net.officefloor.gef.editor.DefaultConnectors;
import net.officefloor.gef.ide.editor.AbstractItem;
import net.officefloor.woof.model.woof.WoofChanges;
import net.officefloor.woof.model.woof.WoofExceptionToWoofSectionInputModel;
import net.officefloor.woof.model.woof.WoofHttpContinuationToWoofSectionInputModel;
import net.officefloor.woof.model.woof.WoofHttpInputToWoofSectionInputModel;
import net.officefloor.woof.model.woof.WoofModel;
import net.officefloor.woof.model.woof.WoofProcedureNextToWoofSectionInputModel;
import net.officefloor.woof.model.woof.WoofProcedureOutputToWoofSectionInputModel;
import net.officefloor.woof.model.woof.WoofSectionInputModel;
import net.officefloor.woof.model.woof.WoofSectionInputModel.WoofSectionInputEvent;
import net.officefloor.woof.model.woof.WoofSectionModel;
import net.officefloor.woof.model.woof.WoofSectionOutputToWoofSectionInputModel;
import net.officefloor.woof.model.woof.WoofSecurityOutputToWoofSectionInputModel;
import net.officefloor.woof.model.woof.WoofStartToWoofSectionInputModel;
import net.officefloor.woof.model.woof.WoofTemplateOutputToWoofSectionInputModel;
import net.officefloor.woof.model.woof.WoofSectionModel.WoofSectionEvent;

/**
 * Configuration for the {@link WoofSectionInputModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class WoofSectionInputItem extends
		AbstractItem<WoofModel, WoofChanges, WoofSectionModel, WoofSectionEvent, WoofSectionInputModel, WoofSectionInputEvent> {

	@Override
	public WoofSectionInputModel prototype() {
		return new WoofSectionInputModel("Input", null);
	}

	@Override
	public IdeExtractor extract() {
		return new IdeExtractor((parent) -> parent.getInputs(), WoofSectionEvent.ADD_INPUT,
				WoofSectionEvent.REMOVE_INPUT);
	}

	@Override
	public void loadToParent(WoofSectionModel parentModel, WoofSectionInputModel itemModel) {
		parentModel.addInput(itemModel);
	}

	@Override
	public Pane visual(WoofSectionInputModel model, AdaptedChildVisualFactoryContext<WoofSectionInputModel> context) {
		HBox container = new HBox();
		context.addNode(container,
				context.connector(DefaultConnectors.FLOW, WoofHttpContinuationToWoofSectionInputModel.class,
						WoofHttpInputToWoofSectionInputModel.class, WoofTemplateOutputToWoofSectionInputModel.class,
						WoofSecurityOutputToWoofSectionInputModel.class, WoofSectionOutputToWoofSectionInputModel.class,
						WoofExceptionToWoofSectionInputModel.class, WoofStartToWoofSectionInputModel.class,
						WoofProcedureNextToWoofSectionInputModel.class,
						WoofProcedureOutputToWoofSectionInputModel.class).getNode());
		context.label(container);
		return container;
	}

	@Override
	public IdeLabeller label() {
		return new IdeLabeller((model) -> model.getWoofSectionInputName(),
				WoofSectionInputEvent.CHANGE_WOOF_SECTION_INPUT_NAME);
	}

}
