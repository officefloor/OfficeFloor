/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.officefloor.eclipse.section;

import java.util.List;
import java.util.function.Consumer;

import javafx.scene.control.Label;
import net.officefloor.configuration.ConfigurationItem;
import net.officefloor.configuration.WritableConfigurationItem;
import net.officefloor.eclipse.ide.editor.AbstractConfigurableItem;
import net.officefloor.eclipse.ide.editor.AbstractIdeEclipseEditor;
import net.officefloor.eclipse.ide.editor.AbstractItem.ConfigurableContext;
import net.officefloor.model.Model;
import net.officefloor.model.impl.repository.ModelRepositoryImpl;
import net.officefloor.model.impl.section.SectionChangesImpl;
import net.officefloor.model.impl.section.SectionRepositoryImpl;
import net.officefloor.model.section.SectionChanges;
import net.officefloor.model.section.SectionModel;
import net.officefloor.model.section.SectionModel.SectionEvent;
import net.officefloor.model.section.SectionRepository;

/**
 * {@link SectionModel} editor.
 * 
 * @author Daniel Sagenschneider
 */
public class SectionEditor extends AbstractIdeEclipseEditor<SectionModel, SectionEvent, SectionChanges> {

	/**
	 * Test editor.
	 * 
	 * @param args Command line arguments.
	 * @throws Exception If fails to run.
	 */
	public static void main(String[] args) throws Exception {
		SectionEditor.launch("<section />");
	}

	/**
	 * {@link SectionRepository}.
	 */
	private static final SectionRepository SECTION_REPOSITORY = new SectionRepositoryImpl(new ModelRepositoryImpl());

	/**
	 * Convenience method to launch {@link AbstractConfigurableItem} outside
	 * workbench.
	 * 
	 * @param                    <M> {@link Model} type.
	 * @param                    <E> {@link Model} event type.
	 * @param                    <I> Item type.
	 * @param configurableItem   {@link AbstractConfigurableItem}.
	 * @param prototypeDecorator Optional prototype decorator.
	 */
	public static <M extends Model, E extends Enum<E>, I> void launchConfigurer(
			AbstractConfigurableItem<SectionModel, SectionEvent, SectionChanges, M, E, I> configurableItem,
			Consumer<M> prototypeDecorator) {
		configurableItem.main(new SectionModel(), SectionEditor.class, prototypeDecorator);
	}

	/**
	 * Default instantiate.
	 */
	public SectionEditor() {
		super(SectionModel.class, (model) -> new SectionChangesImpl(model));
	}

	/*
	 * =============== AbstractIdeEditor ======================
	 */

	@Override
	protected void init(ConfigurableContext<SectionModel, SectionChanges> context) {
		context.getRootBuilder().overlay(10, 400, (overlay) -> {
			overlay.getOverlayParent().getChildren()
					.add(new Label("WARNING: The " + this.getClass().getSimpleName() + " is only to prove concepts.\n"
							+ "It should NEVER be used for application developement.\n\n"
							+ "The purpose of this editor is to prove the raw OfficeFloor model.\n"
							+ "Much of the functionality is not complete for this editor (or likely very buggy).\n"
							+ "For application development please use WoOF."));
		});
	}

	@Override
	public String paletteStyle() {
		return ".palette { -fx-background-color: cornsilk }";
	}

	@Override
	public String paletteIndicatorStyle() {
		return ".palette-indicator { -fx-background-color: bisque }";
	}

	@Override
	public String editorStyle() {
		return ".editor { -fx-background-color: burlywood } .connection Path { -fx-stroke: royalblue }";
	}

	@Override
	public String fileName() {
		return "new.section";
	}

	@Override
	public SectionModel prototype() {
		return new SectionModel();
	}

	@Override
	protected void loadParents(
			List<AbstractConfigurableItem<SectionModel, SectionEvent, SectionChanges, ?, ?, ?>> parents) {
		parents.add(new FunctionNamespaceItem());
		parents.add(new FunctionItem());
		parents.add(new SubSectionItem());
		parents.add(new ExternalFlowItem());
		parents.add(new ManagedObjectSourceItem());
		parents.add(new ManagedObjectItem());
		parents.add(new ExternalManagedObjectItem());
	}

	@Override
	protected SectionModel loadRootModel(ConfigurationItem configurationItem) throws Exception {
		SectionModel section = new SectionModel();
		SECTION_REPOSITORY.retrieveSection(section, configurationItem);
		return section;
	}

	@Override
	public void saveRootModel(SectionModel model, WritableConfigurationItem configurationItem) throws Exception {
		SECTION_REPOSITORY.storeSection(model, configurationItem);
	}

}