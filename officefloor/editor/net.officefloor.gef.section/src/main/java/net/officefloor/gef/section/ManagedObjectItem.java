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

import java.util.List;

import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import net.officefloor.gef.configurer.ValueValidator;
import net.officefloor.gef.editor.AdaptedChildVisualFactoryContext;
import net.officefloor.gef.editor.DefaultConnectors;
import net.officefloor.gef.ide.editor.AbstractConfigurableItem;
import net.officefloor.model.ConnectionModel;
import net.officefloor.model.section.ManagedFunctionObjectToSectionManagedObjectModel;
import net.officefloor.model.section.SectionChanges;
import net.officefloor.model.section.SectionManagedObjectDependencyToSectionManagedObjectModel;
import net.officefloor.model.section.SectionManagedObjectModel;
import net.officefloor.model.section.SectionManagedObjectModel.SectionManagedObjectEvent;
import net.officefloor.model.section.SectionManagedObjectSourceModel;
import net.officefloor.model.section.SectionManagedObjectSourceModel.SectionManagedObjectSourceEvent;
import net.officefloor.model.section.SectionManagedObjectToSectionManagedObjectSourceModel;
import net.officefloor.model.section.SectionModel;
import net.officefloor.model.section.SectionModel.SectionEvent;

/**
 * Configuration for the {@link SectionManagedObjectModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class ManagedObjectItem extends
		AbstractConfigurableItem<SectionModel, SectionEvent, SectionChanges, SectionManagedObjectModel, SectionManagedObjectEvent, ManagedObjectItem> {

	/**
	 * Name.
	 */
	private String name;

	/*
	 * ============== AbstractConfigurableItem ===============
	 */

	@Override
	public SectionManagedObjectModel prototype() {
		return new SectionManagedObjectModel("Managed Object", null);
	}

	@Override
	public ManagedObjectItem item(SectionManagedObjectModel model) {
		ManagedObjectItem item = new ManagedObjectItem();
		if (model != null) {
			item.name = model.getSectionManagedObjectName();
		}
		return item;
	}

	@Override
	public IdeExtractor extract() {
		return new IdeExtractor((parent) -> parent.getSectionManagedObjects(), SectionEvent.ADD_SECTION_MANAGED_OBJECT,
				SectionEvent.REMOVE_SECTION_MANAGED_OBJECT);
	}

	@Override
	public void loadToParent(SectionModel parentModel, SectionManagedObjectModel itemModel) {
		parentModel.addSectionManagedObject(itemModel);
	}

	@Override
	public Pane visual(SectionManagedObjectModel model,
			AdaptedChildVisualFactoryContext<SectionManagedObjectModel> context) {
		VBox container = new VBox();
		HBox heading = context.addNode(container, new HBox());
		context.addNode(heading,
				context.connector(DefaultConnectors.OBJECT, SectionManagedObjectToSectionManagedObjectSourceModel.class,
						SectionManagedObjectDependencyToSectionManagedObjectModel.class,
						ManagedFunctionObjectToSectionManagedObjectModel.class).getNode());
		context.label(heading);
		context.addNode(container, context.childGroup(ManagedObjectDependencyItem.class.getSimpleName(), new VBox()));
		return container;
	}

	@Override
	public IdeLabeller label() {
		return new IdeLabeller((model) -> model.getSectionManagedObjectName(),
				SectionManagedObjectEvent.CHANGE_SECTION_MANAGED_OBJECT_NAME);
	}

	@Override
	public String style() {
		return new IdeStyle().rule("-fx-background-color", "darkseagreen").toString();
	}

	@Override
	public IdeConfigurer configure() {
		return new IdeConfigurer().refactor((builder, context) -> {
			builder.title("Managed Object");
			builder.text("Name").init((item) -> item.name).validate(ValueValidator.notEmptyString("Must provide name"))
					.setValue((item, value) -> item.name = value);
			builder.apply("Refactor", (item) -> {
				context.execute(context.getOperations().renameSectionManagedObject(context.getModel(), item.name));
			});
		}).delete((context) -> {
			context.execute(context.getOperations().removeSectionManagedObject(context.getModel()));
		});
	}

	@Override
	protected void children(List<IdeChildrenGroup> childGroups) {
		childGroups.add(new IdeChildrenGroup(new ManagedObjectDependencyItem()));
	}

	@Override
	protected void connections(List<IdeConnectionTarget<? extends ConnectionModel, ?, ?>> connections) {

		// Managed Object Source
		connections.add(new IdeConnection<>(SectionManagedObjectToSectionManagedObjectSourceModel.class)
				.connectOne((s) -> s.getSectionManagedObjectSource(), (c) -> c.getSectionManagedObject(),
						SectionManagedObjectEvent.CHANGE_SECTION_MANAGED_OBJECT_SOURCE)
				.to(SectionManagedObjectSourceModel.class).many((t) -> t.getSectionManagedObjects(),
						(c) -> c.getSectionManagedObjectSource(),
						SectionManagedObjectSourceEvent.ADD_SECTION_MANAGED_OBJECT,
						SectionManagedObjectSourceEvent.REMOVE_SECTION_MANAGED_OBJECT));
	}

}
