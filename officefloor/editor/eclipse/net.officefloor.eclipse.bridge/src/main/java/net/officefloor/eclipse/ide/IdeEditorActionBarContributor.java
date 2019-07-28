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
package net.officefloor.eclipse.ide.editor;

import org.eclipse.gef.mvc.fx.ui.parts.FXEditorActionBarContributor;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.part.EditorActionBarContributor;

import net.officefloor.gef.editor.AdaptedEditorModule;

/**
 * {@link EditorActionBarContributor} for the {@link AdaptedEditorModule}
 * {@link IEditorPart}.
 * 
 * @author Daniel Sagenschneider
 */
public class IdeEditorActionBarContributor extends FXEditorActionBarContributor {

	@Override
	public void setActiveEditor(IEditorPart activeEditor) {
		super.setActiveEditor(activeEditor);

		// Provide save action
		this.getActionBars().setGlobalActionHandler(ActionFactory.SAVE.getId(),
				ActionFactory.SAVE.create(activeEditor.getSite().getWorkbenchWindow()));

		// Provide save as action
		this.getActionBars().setGlobalActionHandler(ActionFactory.SAVE_AS.getId(),
				ActionFactory.SAVE_AS.create(activeEditor.getSite().getWorkbenchWindow()));
	}

}