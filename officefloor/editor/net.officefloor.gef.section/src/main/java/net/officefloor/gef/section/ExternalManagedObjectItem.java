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
