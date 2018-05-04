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
package net.officefloor.eclipse.ide.preferences;

import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorActionBarContributor;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IKeyBindingService;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;

import net.officefloor.eclipse.ide.editor.AbstractIdeEditor;

/**
 * {@link IEditorSite} to load {@link AbstractIdeEditor} for preference
 * configuration.
 * 
 * @author Daniel Sagenschneider
 */
@SuppressWarnings("deprecation")
public class PreferencesEditorSite implements IEditorSite {

	/**
	 * Name of the {@link AbstractIdeEditor}.
	 */
	private final String editorName;

	/**
	 * {@link IWorkbench}.
	 */
	private final IWorkbench workbench;

	/**
	 * Parent {@link Shell}.
	 */
	private final Shell parentShell;

	/**
	 * Instantiate.
	 * 
	 * @param workbench
	 *            {@link IWorkbench}.
	 */
	public PreferencesEditorSite(String editorName, IWorkbench workbench, Shell parentShell) {
		this.editorName = editorName;
		this.workbench = workbench;
		this.parentShell = parentShell;
	}

	/*
	 * ============ IEditorSite ==================
	 */

	@Override
	public String getId() {
		return this.getPluginId() + ".preferences";
	}

	@Override
	public String getPluginId() {
		return "net.officefloor.eclipse.ide";
	}

	@Override
	public String getRegisteredName() {
		return this.editorName;
	}

	@Override
	public void registerContextMenu(String menuId, MenuManager menuManager, ISelectionProvider selectionProvider) {
	}

	@Override
	public void registerContextMenu(MenuManager menuManager, ISelectionProvider selectionProvider) {
	}

	@Override
	public IKeyBindingService getKeyBindingService() {
		return null;
	}

	@Override
	public IWorkbenchPart getPart() {
		return this.getPage().getActivePart();
	}

	@Override
	public IWorkbenchPage getPage() {
		return this.getWorkbenchWindow().getActivePage();
	}

	@Override
	public ISelectionProvider getSelectionProvider() {
		return null;
	}

	@Override
	public Shell getShell() {
		return this.parentShell;
	}

	@Override
	public IWorkbenchWindow getWorkbenchWindow() {
		return this.workbench.getActiveWorkbenchWindow();
	}

	@Override
	public void setSelectionProvider(ISelectionProvider provider) {
	}

	@Override
	public <T> T getAdapter(Class<T> adapter) {
		return null;
	}

	@Override
	public <T> T getService(Class<T> api) {
		return null;
	}

	@Override
	public boolean hasService(Class<?> api) {
		return false;
	}

	@Override
	public IEditorActionBarContributor getActionBarContributor() {
		return null;
	}

	@Override
	public IActionBars getActionBars() {
		return null;
	}

	@Override
	public void registerContextMenu(MenuManager menuManager, ISelectionProvider selectionProvider,
			boolean includeEditorInput) {
	}

	@Override
	public void registerContextMenu(String menuId, MenuManager menuManager, ISelectionProvider selectionProvider,
			boolean includeEditorInput) {
	}

}