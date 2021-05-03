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
import net.officefloor.model.section.SectionChanges;
import net.officefloor.model.section.SectionManagedObjectDependencyModel;
import net.officefloor.model.section.SectionManagedObjectDependencyModel.SectionManagedObjectDependencyEvent;
import net.officefloor.model.section.SectionManagedObjectDependencyToExternalManagedObjectModel;
import net.officefloor.model.section.SectionManagedObjectDependencyToSectionManagedObjectModel;
import net.officefloor.model.section.SectionManagedObjectModel;
import net.officefloor.model.section.SectionManagedObjectModel.SectionManagedObjectEvent;
import net.officefloor.model.section.SectionModel;

/**
 * Configuration for the {@link SectionManagedObjectDependencyModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class ManagedObjectDependencyItem extends
		AbstractItem<SectionModel, SectionChanges, SectionManagedObjectModel, SectionManagedObjectEvent, SectionManagedObjectDependencyModel, SectionManagedObjectDependencyEvent> {

	@Override
	public SectionManagedObjectDependencyModel prototype() {
		return new SectionManagedObjectDependencyModel("Dependency", null);
	}

	@Override
	public IdeExtractor extract() {
		return new IdeExtractor((p) -> p.getSectionManagedObjectDependencies(),
				SectionManagedObjectEvent.ADD_SECTION_MANAGED_OBJECT_DEPENDENCY,
				SectionManagedObjectEvent.REMOVE_SECTION_MANAGED_OBJECT_DEPENDENCY);
	}

	@Override
	public void loadToParent(SectionManagedObjectModel parentModel, SectionManagedObjectDependencyModel itemModel) {
		parentModel.addSectionManagedObjectDependency(itemModel);
	}

	@Override
	public Pane visual(SectionManagedObjectDependencyModel model,
			AdaptedChildVisualFactoryContext<SectionManagedObjectDependencyModel> context) {
		HBox container = new HBox();
		context.label(container);
		context.addNode(container,
				context.connector(DefaultConnectors.OBJECT,
						SectionManagedObjectDependencyToSectionManagedObjectModel.class,
						SectionManagedObjectDependencyToExternalManagedObjectModel.class).getNode());
		return container;
	}

	@Override
	public IdeLabeller label() {
		return new IdeLabeller((m) -> m.getSectionManagedObjectDependencyName(),
				SectionManagedObjectDependencyEvent.CHANGE_SECTION_MANAGED_OBJECT_DEPENDENCY_NAME);
	}

	@Override
	protected void connections(List<IdeConnectionTarget<? extends ConnectionModel, ?, ?>> connections) {

		// Managed Object
		connections.add(new IdeConnection<>(SectionManagedObjectDependencyToSectionManagedObjectModel.class)
				.connectOne((s) -> s.getSectionManagedObject(), (c) -> c.getSectionManagedObjectDependency(),
						SectionManagedObjectDependencyEvent.CHANGE_SECTION_MANAGED_OBJECT)
				.to(SectionManagedObjectModel.class)
				.many((t) -> t.getDependentSectionManagedObjects(), (c) -> c.getSectionManagedObject(),
						SectionManagedObjectEvent.ADD_SECTION_MANAGED_OBJECT_DEPENDENCY,
						SectionManagedObjectEvent.REMOVE_SECTION_MANAGED_OBJECT_DEPENDENCY)
				.create((s, t, ctx) -> {
					ctx.getChangeExecutor().execute(
							ctx.getOperations().linkSectionManagedObjectDependencyToSectionManagedObject(s, t));
				}).delete((ctx) -> {
					ctx.getChangeExecutor().execute(ctx.getOperations()
							.removeSectionManagedObjectDependencyToSectionManagedObject(ctx.getModel()));
				}));

		// External Object
		connections.add(new IdeConnection<>(SectionManagedObjectDependencyToExternalManagedObjectModel.class)
				.connectOne((s) -> s.getExternalManagedObject(), (c) -> c.getSectionManagedObjectDependency(),
						SectionManagedObjectDependencyEvent.CHANGE_EXTERNAL_MANAGED_OBJECT)
				.to(ExternalManagedObjectModel.class)
				.many((t) -> t.getDependentSectionManagedObjects(), (c) -> c.getExternalManagedObject(),
						ExternalManagedObjectEvent.ADD_DEPENDENT_SECTION_MANAGED_OBJECT,
						ExternalManagedObjectEvent.REMOVE_DEPENDENT_SECTION_MANAGED_OBJECT)
				.create((s, t, ctx) -> {
					ctx.getChangeExecutor().execute(
							ctx.getOperations().linkSectionManagedObjectDependencyToExternalManagedObject(s, t));
				}).delete((ctx) -> {
					ctx.getChangeExecutor().execute(ctx.getOperations()
							.removeSectionManagedObjectDependencyToExternalManagedObject(ctx.getModel()));
				}));
	}

}
