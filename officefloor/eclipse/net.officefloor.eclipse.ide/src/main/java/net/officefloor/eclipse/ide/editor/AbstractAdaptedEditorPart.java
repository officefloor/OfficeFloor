/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2019 Daniel Sagenschneider
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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.gef.mvc.fx.ui.MvcFxUiModule;
import org.eclipse.gef.mvc.fx.ui.parts.AbstractFXEditor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.EditorPart;

import com.google.inject.Injector;

import javafx.scene.Scene;
import net.officefloor.configuration.WritableConfigurationItem;
import net.officefloor.eclipse.ide.preferences.PreferencesEditorInput;
import net.officefloor.eclipse.osgi.ProjectConfigurationContext;
import net.officefloor.model.Model;

/**
 * {@link EditorPart} for the {@link AbstractAdaptedIdeEditor}.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractAdaptedEditorPart<R extends Model, RE extends Enum<RE>, O> extends EditorPart {

	/**
	 * {@link AbstractAdaptedIdeEditor}.
	 */
	private AbstractAdaptedIdeEditor<R, RE, O> editor;

	/**
	 * {@link FxToSwt}.
	 */
	private FxToSwt fxToSwt;

	/**
	 * Creates the {@link AbstractAdaptedIdeEditor}.
	 * 
	 * @param <R>  Root {@link Model}.
	 * @param <RE> Root event {@link Enum}.
	 * @param <O>  Operations.
	 * @return {@link AbstractAdaptedIdeEditor}.
	 */
	protected abstract AbstractAdaptedIdeEditor<R, RE, O> createEditor();

	/*
	 * ====================== EditorPart ============================
	 */

	@Override
	public void init(IEditorSite site, IEditorInput input) throws PartInitException {

		// Create the editor
		this.editor = this.createEditor();
		this.editor.init(new MvcFxUiModule(), (injector) -> {
			this.fxToSwt = new FxToSwt(injector);
			return this.fxToSwt.getDomain();
		});

		// Initialise
		this.fxToSwt.init(site, input);

		// Specify the input
		this.setInput(input);
		this.setSite(site);

		// Obtain the input configuration
		if (input instanceof IFileEditorInput) {
			// Load the configuration item from the file
			IFileEditorInput fileInput = (IFileEditorInput) input;
			IFile configurationFile = fileInput.getFile();
			WritableConfigurationItem configurationItem = ProjectConfigurationContext
					.getWritableConfigurationItem(configurationFile, null);
			this.editor.setConfigurationItem(configurationItem);

		} else if (input instanceof PreferencesEditorInput) {
			// Provided the model
			PreferencesEditorInput preferencesInput = (PreferencesEditorInput) input;
			R model = (R) preferencesInput.getRootModel();
			this.editor.setModel(model);

		} else {
			// Unknown editor input
			throw new IllegalStateException("Unable to edit input " + input.getClass().getName());
		}
	}

	@Override
	public void createPartControl(Composite parent) {

		// Create the part control
		this.fxToSwt.createPartControl(parent);
	}

	@Override
	public void setFocus() {
		this.fxToSwt.setFocus();
	}

	@Override
	public boolean isDirty() {
		return this.fxToSwt.isDirty();
	}

	@Override
	public boolean isSaveAsAllowed() {
		return this.fxToSwt.isSaveAsAllowed();
	}

	@Override
	public void doSave(IProgressMonitor monitor) {
		this.fxToSwt.doSave(monitor);
	}

	@Override
	public void doSaveAs() {
		this.fxToSwt.doSaveAs();
	}

	/**
	 * FX to SWT adapting.
	 */
	private class FxToSwt extends AbstractFXEditor {

		/**
		 * Instantiate.
		 * 
		 * @param injector {@link Injector}.
		 */
		public FxToSwt(Injector injector) {
			super(injector);
		}

		@Override
		protected void hookViewers() {
			AbstractAdaptedEditorPart.this.editor.loadView((view) -> {
				this.getCanvas().setScene(new Scene(view));
			});
		}

		@Override
		public boolean isSaveAsAllowed() {
			return true;
		}

		@Override
		public void doSaveAs() {
			// TODO implement EditorPart.doSaveAs
			throw new UnsupportedOperationException("TODO implement EditorPart.doSaveAs");
		}

		@Override
		public void doSave(IProgressMonitor monitor) {
			AbstractAdaptedEditorPart.this.editor.save();
		}
	}

}