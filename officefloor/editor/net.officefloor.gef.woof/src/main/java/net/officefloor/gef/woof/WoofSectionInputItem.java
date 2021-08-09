/*-
 * #%L
 * [bundle] Section Editor
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
