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

package net.officefloor.gef.section;

import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import net.officefloor.gef.editor.AdaptedChildVisualFactoryContext;
import net.officefloor.gef.editor.DefaultConnectors;
import net.officefloor.gef.ide.editor.AbstractItem;
import net.officefloor.model.section.FunctionEscalationToSubSectionInputModel;
import net.officefloor.model.section.FunctionFlowToSubSectionInputModel;
import net.officefloor.model.section.FunctionToNextSubSectionInputModel;
import net.officefloor.model.section.SectionChanges;
import net.officefloor.model.section.SectionModel;
import net.officefloor.model.section.SubSectionInputModel;
import net.officefloor.model.section.SubSectionInputModel.SubSectionInputEvent;
import net.officefloor.model.section.SubSectionModel;
import net.officefloor.model.section.SubSectionModel.SubSectionEvent;

/**
 * Configuration for the {@link SubSectionInputModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class SubSectionInputItem extends
		AbstractItem<SectionModel, SectionChanges, SubSectionModel, SubSectionEvent, SubSectionInputModel, SubSectionInputEvent> {

	@Override
	public SubSectionInputModel prototype() {
		return new SubSectionInputModel("Input", null, false, null);
	}

	@Override
	public IdeExtractor extract() {
		return new IdeExtractor((parent) -> parent.getSubSectionInputs(), SubSectionEvent.ADD_SUB_SECTION_INPUT,
				SubSectionEvent.REMOVE_SUB_SECTION_INPUT);
	}

	@Override
	public void loadToParent(SubSectionModel parentModel, SubSectionInputModel itemModel) {
		parentModel.addSubSectionInput(itemModel);
	}

	@Override
	public Pane visual(SubSectionInputModel model, AdaptedChildVisualFactoryContext<SubSectionInputModel> context) {
		HBox container = new HBox();
		context.addNode(container,
				context.connector(DefaultConnectors.FLOW, FunctionToNextSubSectionInputModel.class,
						FunctionFlowToSubSectionInputModel.class, FunctionEscalationToSubSectionInputModel.class)
						.getNode());
		context.label(container);
		return container;
	}

	@Override
	public IdeLabeller label() {
		return new IdeLabeller((model) -> model.getSubSectionInputName(),
				SubSectionInputEvent.CHANGE_PUBLIC_INPUT_NAME);
	}

}
