/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.officefloor.eclipse.section;

import java.util.List;
import java.util.function.Consumer;

import org.eclipse.ui.IWorkbench;

import net.officefloor.configuration.ConfigurationItem;
import net.officefloor.configuration.WritableConfigurationItem;
import net.officefloor.eclipse.ide.editor.AbstractIdeEditor;
import net.officefloor.eclipse.ide.editor.AbstractConfigurableItem;
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
public class SectionEditor extends AbstractIdeEditor<SectionModel, SectionEvent, SectionChanges> {

	/**
	 * Test editor.
	 */
	public static void main(String[] args) {
		SectionEditor.launch("<section />");
	}

	/**
	 * {@link SectionRepository}.
	 */
	private static final SectionRepository SECTION_REPOSITORY = new SectionRepositoryImpl(new ModelRepositoryImpl());

	/**
	 * Convenience method to launch {@link AbstractConfigurableItem} outside
	 * {@link IWorkbench}.
	 * 
	 * @param configurableItem
	 *            {@link AbstractConfigurableItem}.
	 * @param prototypeDecorator
	 *            Optional prototype decorator.
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
	protected void loadParents(
			List<AbstractConfigurableItem<SectionModel, SectionEvent, SectionChanges, ?, ?, ?>> parents) {
		parents.add(new FunctionNamespaceItem());
		parents.add(new FunctionItem());
		parents.add(new SubSectionItem());
		parents.add(new ExternalFlowItem());
	}

	@Override
	protected SectionModel loadRootModel(ConfigurationItem configurationItem) throws Exception {
		SectionModel section = new SectionModel();
		SECTION_REPOSITORY.retrieveSection(section, configurationItem);
		return section;
	}

	@Override
	protected void saveRootModel(SectionModel model, WritableConfigurationItem configurationItem) throws Exception {
		SECTION_REPOSITORY.storeSection(model, configurationItem);
	}

}