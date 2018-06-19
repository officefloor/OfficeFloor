/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2013 Daniel Sagenschneider
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
package net.officefloor.eclipse.common.editor;

import org.eclipse.gef.ui.actions.ActionRegistry;
import org.eclipse.jface.action.IAction;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorActionBarContributor;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.actions.ActionFactory;

import net.officefloor.eclipse.office.OfficeEditor;
import net.officefloor.eclipse.officefloor.OfficeFloorEditor;
import net.officefloor.eclipse.section.SectionEditor;

/**
 * {@link IEditorActionBarContributor} for the {@link SectionEditor},
 * {@link OfficeEditor} and {@link OfficeFloorEditor}.
 * 
 * @author Daniel Sagenschneider
 */
public class CoreEditorActionContributor implements IEditorActionBarContributor {

	/*
	 * ================ IEditorActionBarContributor =========================
	 */

	@Override
	public void init(IActionBars bars, IWorkbenchPage page) {
	}

	@Override
	public void setActiveEditor(IEditorPart targetEditor) {

		// Hook in the actions
		this.hookInActions(targetEditor);

		// Update the action bars
		targetEditor.getEditorSite().getActionBars().updateActionBars();
	}

	/**
	 * Hook in the {@link IAction} instances.
	 * 
	 * @param targetEditor
	 *            {@link IEditorPart}.
	 */
	protected void hookInActions(IEditorPart targetEditor) {
		// FILE actions
		this.hookInAction(ActionFactory.SAVE, targetEditor);

		// EDIT actions
		this.hookInAction(ActionFactory.UNDO, targetEditor);
		this.hookInAction(ActionFactory.REDO, targetEditor);
		this.hookInAction(ActionFactory.SELECT_ALL, targetEditor);
	}

	/**
	 * Hooks in the {@link IAction}.
	 * 
	 * @param actionFactory
	 *            {@link ActionFactory} specifying the {@link IAction}.
	 * @param targetEditor
	 *            {@link IEditorPart}.
	 */
	protected void hookInAction(ActionFactory actionFactory, IEditorPart targetEditor) {

		// Obtain the action registry
		ActionRegistry actionRegistry = (ActionRegistry) targetEditor.getAdapter(ActionRegistry.class);

		// Obtain the action
		String actionId = actionFactory.getId();
		IAction action = actionRegistry.getAction(actionId);

		// Only register if have action
		if (action == null) {
			return;
		}

		// Register the action
		targetEditor.getEditorSite().getActionBars().setGlobalActionHandler(actionId, action);
	}

	@Override
	public void dispose() {
	}

}