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

import java.util.function.Consumer;

import org.eclipse.ui.IWorkbench;

import net.officefloor.eclipse.ide.editor.AbstractAdaptedEditorPart;
import net.officefloor.eclipse.ide.editor.AbstractAdaptedIdeEditor;
import net.officefloor.eclipse.ide.editor.AbstractConfigurableItem;
import net.officefloor.model.Model;
import net.officefloor.woof.model.woof.WoofChanges;
import net.officefloor.woof.model.woof.WoofModel;
import net.officefloor.woof.model.woof.WoofModel.WoofEvent;

/**
 * Web on OfficeFloor (WoOF) Editor.
 * 
 * @author Daniel Sagenschneider
 */
public class WoofEditor extends AbstractAdaptedEditorPart<WoofModel, WoofEvent, WoofChanges> {

	/**
	 * Convenience method to launch {@link AbstractConfigurableItem} outside
	 * {@link IWorkbench}.
	 *
	 * @param <M>                {@link Model} type.
	 * @param <E>                {@link Model} event type.
	 * @param <I>                Item type.
	 * @param configurableItem   {@link AbstractConfigurableItem}.
	 * @param prototypeDecorator Optional prototype decorator.
	 */
	public static <M extends Model, E extends Enum<E>, I> void launchConfigurer(
			AbstractConfigurableItem<WoofModel, WoofEvent, WoofChanges, M, E, I> configurableItem,
			Consumer<M> prototypeDecorator) {
		configurableItem.main(new WoofModel(), WoofEditorBackup.class, prototypeDecorator);
	}

	@Override
	protected AbstractAdaptedIdeEditor<WoofModel, WoofEvent, WoofChanges> createEditor() {
		return new WoofEditorRefactor();
	}

}