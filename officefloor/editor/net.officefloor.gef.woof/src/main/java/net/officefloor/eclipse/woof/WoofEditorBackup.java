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
package net.officefloor.eclipse.woof;

import java.util.List;
import java.util.function.Consumer;

import org.eclipse.ui.IWorkbench;

import net.officefloor.configuration.ConfigurationItem;
import net.officefloor.configuration.WritableConfigurationItem;
import net.officefloor.eclipse.ide.editor.AbstractConfigurableItem;
import net.officefloor.eclipse.ide.editor.AbstractIdeEclipseEditor;
import net.officefloor.model.Model;
import net.officefloor.model.impl.repository.ModelRepositoryImpl;
import net.officefloor.woof.model.woof.WoofChanges;
import net.officefloor.woof.model.woof.WoofChangesImpl;
import net.officefloor.woof.model.woof.WoofModel;
import net.officefloor.woof.model.woof.WoofModel.WoofEvent;
import net.officefloor.woof.model.woof.WoofRepository;
import net.officefloor.woof.model.woof.WoofRepositoryImpl;

/**
 * Web on OfficeFloor (WoOF) Editor.
 * 
 * @author Daniel Sagenschneider
 */
public class WoofEditorBackup extends AbstractIdeEclipseEditor<WoofModel, WoofEvent, WoofChanges> {

	/**
	 * Test editor.
	 * 
	 * @param args Command line arguments.
	 * @throws Exception If fails to run.
	 */
	public static void main(String[] args) throws Exception {
		WoofEditorBackup.launch("<woof />");
	}

	/**
	 * {@link WoofRepository}.
	 */
	private static final WoofRepository WOOF_REPOSITORY = new WoofRepositoryImpl(new ModelRepositoryImpl());

	/**
	 * Convenience method to launch {@link AbstractConfigurableItem} outside
	 * {@link IWorkbench}.
	 *
	 * @param                    <M> {@link Model} type.
	 * @param                    <E> {@link Model} event type.
	 * @param                    <I> Item type.
	 * @param configurableItem   {@link AbstractConfigurableItem}.
	 * @param prototypeDecorator Optional prototype decorator.
	 */
	public static <M extends Model, E extends Enum<E>, I> void launchConfigurer(
			AbstractConfigurableItem<WoofModel, WoofEvent, WoofChanges, M, E, I> configurableItem,
			Consumer<M> prototypeDecorator) {
		configurableItem.main(new WoofModel(), WoofEditorBackup.class, prototypeDecorator);
	}

	/**
	 * Instantiate.
	 */
	public WoofEditorBackup() {
		super(WoofModel.class, (model) -> new WoofChangesImpl(model));
	}

	/*
	 * ================= AbstractIdeEditor ==================
	 */

	@Override
	public String fileName() {
		return "application.woof";
	}

	@Override
	public WoofModel prototype() {
		return new WoofModel();
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
		return ".connection Path { -fx-stroke: royalblue; -fx-opacity: 0.6 }";
	}

	@Override
	protected void loadParents(List<AbstractConfigurableItem<WoofModel, WoofEvent, WoofChanges, ?, ?, ?>> parents) {
		parents.add(new WoofHttpContinuationItem());
		parents.add(new WoofHttpInputItem());
		parents.add(new WoofTemplateItem());
		parents.add(new WoofSecurityItem());
		parents.add(new WoofSectionItem());
		parents.add(new WoofGovernanceItem());
		parents.add(new WoofResourceItem());
		parents.add(new WoofExceptionItem());
		parents.add(new WoofStartItem());
	}

	@Override
	protected WoofModel loadRootModel(ConfigurationItem configurationItem) throws Exception {
		WoofModel woof = new WoofModel();
		WOOF_REPOSITORY.retrieveWoof(woof, configurationItem);
		return woof;
	}

	@Override
	public void saveRootModel(WoofModel model, WritableConfigurationItem configurationItem) throws Exception {
		WOOF_REPOSITORY.storeWoof(model, configurationItem);
	}

}