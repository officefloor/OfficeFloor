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
import net.officefloor.model.section.ExternalManagedObjectModel;
import net.officefloor.model.section.ExternalManagedObjectModel.ExternalManagedObjectEvent;
import net.officefloor.model.section.ManagedFunctionObjectToExternalManagedObjectModel;
import net.officefloor.model.section.SectionChanges;
import net.officefloor.model.section.SectionManagedObjectDependencyToExternalManagedObjectModel;
import net.officefloor.model.section.SectionModel;
import net.officefloor.model.section.SectionModel.SectionEvent;

/**
 * Configuration for {@link ExternalManagedObjectModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class ExternalManagedObjectItem extends
		AbstractConfigurableItem<SectionModel, SectionEvent, SectionChanges, ExternalManagedObjectModel, ExternalManagedObjectEvent, ExternalManagedObjectItem> {

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
	public ExternalManagedObjectModel prototype() {
		return new ExternalManagedObjectModel("External Object", null);
	}

	@Override
	public IdeExtractor extract() {
		return new IdeExtractor((parent) -> parent.getExternalManagedObjects(),
				SectionEvent.ADD_EXTERNAL_MANAGED_OBJECT, SectionEvent.REMOVE_EXTERNAL_MANAGED_OBJECT);
	}

	@Override
	public void loadToParent(SectionModel parentModel, ExternalManagedObjectModel itemModel) {
		parentModel.addExternalManagedObject(itemModel);
	}

	@Override
	public Pane visual(ExternalManagedObjectModel model,
			AdaptedChildVisualFactoryContext<ExternalManagedObjectModel> context) {
		HBox container = new HBox();
		context.addNode(container,
				context.connector(DefaultConnectors.OBJECT,
						SectionManagedObjectDependencyToExternalManagedObjectModel.class,
						ManagedFunctionObjectToExternalManagedObjectModel.class).getNode());
		context.label(container);
		return container;
	}

	@Override
	public IdeLabeller label() {
		return new IdeLabeller((model) -> model.getExternalManagedObjectName(),
				ExternalManagedObjectEvent.CHANGE_EXTERNAL_MANAGED_OBJECT_NAME);
	}

	@Override
	public String style() {
		return new IdeStyle().rule("-fx-background-color", "yellowgreen").toString();
	}

	@Override
	public ExternalManagedObjectItem item(ExternalManagedObjectModel model) {
		ExternalManagedObjectItem item = new ExternalManagedObjectItem();
		if (model != null) {
			item.name = model.getExternalManagedObjectName();
			item.argumentType = model.getObjectType();
		}
		return item;
	}

	@Override
	public IdeConfigurer configure() {
		return new IdeConfigurer().addAndRefactor((builder, context) -> {
			builder.title("External Object");
			builder.text("Name").init((model) -> model.name).setValue((model, value) -> model.name = value)
					.validate(ValueValidator.notEmptyString("Must specify name"));
			builder.clazz("Argument").init((model) -> model.argumentType)
					.setValue((model, value) -> model.argumentType = value);
		}).add((builder, context) -> {
			builder.apply("Add", (item) -> context
					.execute(context.getOperations().addExternalManagedObject(item.name, item.argumentType)));
		}).refactor((builder, context) -> {
			builder.apply("Refactor", (item) -> {
				// TODO implement refactor of external object
				throw new UnsupportedOperationException(
						"TODO renameExternalManagedObject to be refactorExternalManagedObject");
			});
		}).delete((context) -> {
			context.execute(context.getOperations().removeExternalManagedObject(context.getModel()));
		});
	}

}
