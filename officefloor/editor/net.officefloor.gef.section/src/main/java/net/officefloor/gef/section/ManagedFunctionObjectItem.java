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
import net.officefloor.gef.editor.AdaptedChildVisualFactoryContext;
import net.officefloor.gef.editor.DefaultConnectors;
import net.officefloor.gef.ide.editor.AbstractItem;
import net.officefloor.model.ConnectionModel;
import net.officefloor.model.section.ExternalManagedObjectModel;
import net.officefloor.model.section.ExternalManagedObjectModel.ExternalManagedObjectEvent;
import net.officefloor.model.section.ManagedFunctionModel;
import net.officefloor.model.section.ManagedFunctionModel.ManagedFunctionEvent;
import net.officefloor.model.section.ManagedFunctionObjectModel;
import net.officefloor.model.section.ManagedFunctionObjectModel.ManagedFunctionObjectEvent;
import net.officefloor.model.section.ManagedFunctionObjectToExternalManagedObjectModel;
import net.officefloor.model.section.ManagedFunctionObjectToSectionManagedObjectModel;
import net.officefloor.model.section.SectionChanges;
import net.officefloor.model.section.SectionManagedObjectModel;
import net.officefloor.model.section.SectionManagedObjectModel.SectionManagedObjectEvent;
import net.officefloor.model.section.SectionModel;

/**
 * Configuration for the {@link ManagedFunctionObjectModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class ManagedFunctionObjectItem extends
		AbstractItem<SectionModel, SectionChanges, ManagedFunctionModel, ManagedFunctionEvent, ManagedFunctionObjectModel, ManagedFunctionObjectEvent> {

	@Override
	public ManagedFunctionObjectModel prototype() {
		return new ManagedFunctionObjectModel("Dependency", null, null, false);
	}

	@Override
	public IdeExtractor extract() {
		return new IdeExtractor((parent) -> parent.getManagedFunctionObjects(),
				ManagedFunctionEvent.ADD_MANAGED_FUNCTION_OBJECT, ManagedFunctionEvent.REMOVE_MANAGED_FUNCTION_OBJECT);
	}

	@Override
	public void loadToParent(ManagedFunctionModel parentModel, ManagedFunctionObjectModel itemModel) {
		parentModel.addManagedFunctionObject(itemModel);
	}

	@Override
	public Pane visual(ManagedFunctionObjectModel model,
			AdaptedChildVisualFactoryContext<ManagedFunctionObjectModel> context) {
		HBox container = new HBox();
		context.label(container);
		context.addNode(container,
				context.connector(DefaultConnectors.OBJECT, ManagedFunctionObjectToSectionManagedObjectModel.class,
						ManagedFunctionObjectToExternalManagedObjectModel.class).getNode());
		return container;
	}

	@Override
	public IdeLabeller label() {
		return new IdeLabeller((model) -> model.getObjectName(), ManagedFunctionObjectEvent.CHANGE_OBJECT_NAME);
	}

	@Override
	protected void connections(List<IdeConnectionTarget<? extends ConnectionModel, ?, ?>> connections) {

		// Managed Object
		connections
				.add(new IdeConnection<>(ManagedFunctionObjectToSectionManagedObjectModel.class)
						.connectOne((s) -> s.getSectionManagedObject(), (c) -> c.getManagedFunctionObject(),
								ManagedFunctionObjectEvent.CHANGE_SECTION_MANAGED_OBJECT)
						.to(SectionManagedObjectModel.class)
						.many((t) -> t.getManagedFunctionObjects(), (c) -> c.getSectionManagedObject(),
								SectionManagedObjectEvent.ADD_DEPENDENT_SECTION_MANAGED_OBJECT,
								SectionManagedObjectEvent.REMOVE_DEPENDENT_SECTION_MANAGED_OBJECT)
						.create((s, t, ctx) -> {
							ctx.getChangeExecutor()
									.execute(ctx.getOperations().linkManagedFunctionObjectToSectionManagedObject(s, t));
						}).delete((ctx) -> {
							ctx.getChangeExecutor().execute(ctx.getOperations()
									.removeManagedFunctionObjectToSectionManagedObject(ctx.getModel()));
						}));

		// External Object
		connections
				.add(new IdeConnection<>(ManagedFunctionObjectToExternalManagedObjectModel.class)
						.connectOne((s) -> s.getExternalManagedObject(), (c) -> c.getManagedFunctionObject(),
								ManagedFunctionObjectEvent.CHANGE_EXTERNAL_MANAGED_OBJECT)
						.to(ExternalManagedObjectModel.class)
						.many((t) -> t.getManagedFunctionObjects(), (c) -> c.getExternalManagedObject(),
								ExternalManagedObjectEvent.ADD_MANAGED_FUNCTION_OBJECT,
								ExternalManagedObjectEvent.REMOVE_MANAGED_FUNCTION_OBJECT)
						.create((s, t, ctx) -> {
							ctx.getChangeExecutor().execute(
									ctx.getOperations().linkManagedFunctionObjectToExternalManagedObject(s, t));
						}).delete((ctx) -> {
							ctx.getChangeExecutor().execute(ctx.getOperations()
									.removeManagedFunctionObjectToExternalManagedObject(ctx.getModel()));
						}));
	}

}
