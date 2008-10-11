/*
 *  Office Floor, Application Server
 *  Copyright (C) 2006 Daniel Sagenschneider
 *
 *  This program is free software; you can redistribute it and/or modify it under the terms 
 *  of the GNU General Public License as published by the Free Software Foundation; either 
 *  version 2 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program; 
 *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, 
 *  MA 02111-1307 USA
 */
package net.officefloor.eclipse.common;

import net.officefloor.eclipse.desk.DeskEditor;

import org.eclipse.gef.ui.actions.ActionRegistry;
import org.eclipse.jface.action.IAction;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorActionBarContributor;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.actions.ActionFactory;

/**
 * {@link IEditorActionBarContributor} for the {@link DeskEditor}.
 * 
 * @author Daniel
 */
public class CoreEditorActionContributor implements IEditorActionBarContributor {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.IEditorActionBarContributor#init(org.eclipse.ui.IActionBars
	 * , org.eclipse.ui.IWorkbenchPage)
	 */
	@Override
	public void init(IActionBars bars, IWorkbenchPage page) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.IEditorActionBarContributor#setActiveEditor(org.eclipse
	 * .ui.IEditorPart)
	 */
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
	protected void hookInAction(ActionFactory actionFactory,
			IEditorPart targetEditor) {

		// Obtain the action registry
		ActionRegistry actionRegistry = (ActionRegistry) targetEditor
				.getAdapter(ActionRegistry.class);

		// Obtain the action
		String actionId = actionFactory.getId();
		IAction action = actionRegistry.getAction(actionId);

		// Only register if have action
		if (action == null) {
			return;
		}

		// Register the action
		targetEditor.getEditorSite().getActionBars().setGlobalActionHandler(
				actionId, action);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IEditorActionBarContributor#dispose()
	 */
	@Override
	public void dispose() {
	}

}
