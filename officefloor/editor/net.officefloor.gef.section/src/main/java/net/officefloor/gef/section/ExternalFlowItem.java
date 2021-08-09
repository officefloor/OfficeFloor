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
import net.officefloor.gef.configurer.ValueValidator;
import net.officefloor.gef.editor.AdaptedChildVisualFactoryContext;
import net.officefloor.gef.editor.DefaultConnectors;
import net.officefloor.gef.ide.editor.AbstractConfigurableItem;
import net.officefloor.model.section.ExternalFlowModel;
import net.officefloor.model.section.ExternalFlowModel.ExternalFlowEvent;
import net.officefloor.model.section.FunctionEscalationToExternalFlowModel;
import net.officefloor.model.section.FunctionFlowToExternalFlowModel;
import net.officefloor.model.section.FunctionToNextExternalFlowModel;
import net.officefloor.model.section.SectionChanges;
import net.officefloor.model.section.SectionModel;
import net.officefloor.model.section.SectionModel.SectionEvent;
import net.officefloor.model.section.SubSectionOutputToExternalFlowModel;

/**
 * Configuration for {@link ExternalFlowModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class ExternalFlowItem extends
		AbstractConfigurableItem<SectionModel, SectionEvent, SectionChanges, ExternalFlowModel, ExternalFlowEvent, ExternalFlowItem> {

	/**
	 * Name.
	 */
	private String name;

	/**
	 * Argument type.
	 */
	private String argumentType;

	/*
	 * ================= AbstractParentConfigurableItem ============
	 */

	@Override
	public ExternalFlowModel prototype() {
		return new ExternalFlowModel("External Flow", null);
	}

	@Override
	public IdeExtractor extract() {
		return new IdeExtractor((parent) -> parent.getExternalFlows(), SectionEvent.ADD_EXTERNAL_FLOW,
				SectionEvent.REMOVE_EXTERNAL_FLOW);
	}

	@Override
	public void loadToParent(SectionModel parentModel, ExternalFlowModel itemModel) {
		parentModel.addExternalFlow(itemModel);
	}

	@Override
	public Pane visual(ExternalFlowModel model, AdaptedChildVisualFactoryContext<ExternalFlowModel> context) {
		HBox container = new HBox();
		context.addNode(container,
				context.connector(DefaultConnectors.FLOW, FunctionToNextExternalFlowModel.class,
						FunctionFlowToExternalFlowModel.class, FunctionEscalationToExternalFlowModel.class,
						SubSectionOutputToExternalFlowModel.class).getNode());
		context.label(container);
		return container;
	}

	@Override
	public IdeLabeller label() {
		return new IdeLabeller((model) -> model.getExternalFlowName(), ExternalFlowEvent.CHANGE_EXTERNAL_FLOW_NAME);
	}

	@Override
	public String style() {
		return new IdeStyle().rule("-fx-background-color", "orchid").toString();
	}

	@Override
	public ExternalFlowItem item(ExternalFlowModel model) {
		ExternalFlowItem item = new ExternalFlowItem();
		if (model != null) {
			item.name = model.getExternalFlowName();
			item.argumentType = model.getArgumentType();
		}
		return item;
	}

	@Override
	public IdeConfigurer configure() {
		return new IdeConfigurer().addAndRefactor((builder, context) -> {
			builder.title("External Flow");
			builder.text("Name").init((model) -> model.name).setValue((model, value) -> model.name = value)
					.validate(ValueValidator.notEmptyString("Must specify name"));
			builder.clazz("Argument").init((model) -> model.argumentType)
					.setValue((model, value) -> model.argumentType = value);
		}).add((builder, context) -> {
			builder.apply("Add",
					(item) -> context.execute(context.getOperations().addExternalFlow(item.name, item.argumentType)));
		}).refactor((builder, context) -> {
			builder.apply("Refactor", (item) -> {
				// TODO implement refactor of external flow
				throw new UnsupportedOperationException("TODO renameExternalFlow to be refactorExternalFlow");
			});
		}).delete((context) -> {
			context.execute(context.getOperations().removeExternalFlow(context.getModel()));
		});
	}

}
